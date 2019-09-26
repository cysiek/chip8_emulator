package com.chip8emulator

class Chip8VM()
{
  private val MemorySize = 4096
  private val KeyboardStateSize = 16
  private val NumberOfRegisters = 16
  private val GraphicsBufferWidth = 64
  private val GraphicsBufferHeight = 32
  private val StackSize = 24

  private var pcRegister, iRegister, stackPointer : Short = 0 //iRegister - address register, pc - program counter

  private var memory : Array[Byte] = new Array[Byte](MemorySize)
  private var keyboardState : Array[Byte] = new Array[Byte](KeyboardStateSize)
  //0x000-0x1FF - Chip 8 interpreter (contains font set in emu)
  //0x050-0x0A0 - Used for the built in 4x5 pixel font set (0-F)
  //0x200-0xFFF - Program ROM and work RAM

  private var registers : Array[Byte] = new Array[Byte](NumberOfRegisters)
  //first 15 - general registers. last one (aka VF) - "VF is the carry flag, while in subtraction, it is the "no borrow" flag. In the draw instruction VF is set upon pixel collision."

  private var graphicsMemory : Array[Boolean] = new Array[Boolean](GraphicsBufferWidth*GraphicsBufferHeight)

  private var stack : Array[Short] = new Array[Short](StackSize)

  private var delayTimer, soundTimer : Byte = 0

  def reset(): Unit =
  {
    pcRegister = 0
    // set (6) register (2) to 31 (31 == 1 << 4 | 15)
    memory(0) = bitOperations.pack2ValuesToByte(6,2)
    memory(1) = bitOperations.pack2ValuesToByte(1, 15)

    // add (7) to register (14) value:  33 (2 << 4 | 1)
    memory(2) = bitOperations.pack2ValuesToByte(7,14)
    memory(3) = bitOperations.pack2ValuesToByte(2, 1)

    // add (7) to register (14) value:  66 (4 << 4 | 2)
    memory(4) = bitOperations.pack2ValuesToByte(7,14)
    memory(5) = bitOperations.pack2ValuesToByte(4, 2)

    // math operation(8): add(4) values from to register (14) and register (2) and store result in register(14)
    memory(6) = bitOperations.pack2ValuesToByte(8,14)
    memory(7) = bitOperations.pack2ValuesToByte(2, 4)

    // math operation(8): XOR(4) values from to register (14) and register (2) and store result in register(14)
    memory(8) = bitOperations.pack2ValuesToByte(8,14)
    memory(9) = bitOperations.pack2ValuesToByte(2, 3)

    // set (6) register (1) to 67 (67 == 4 << 4 | 3)
    memory(10) = bitOperations.pack2ValuesToByte(6,1)
    memory(11) = bitOperations.pack2ValuesToByte(4, 3)

    // set (6) register (15) to 0
    memory(12) = bitOperations.pack2ValuesToByte(6,15)
    memory(13) = bitOperations.pack2ValuesToByte(0, 0)

    // math operation(8): rotate right(6) by 1 value from to register (1) and store result in register(1)
    memory(14) = bitOperations.pack2ValuesToByte(8,1)
    memory(15) = bitOperations.pack2ValuesToByte(0, 6)

    // math operation(8): rotate right(6) by 1 value from to register (1) and store result in register(1)
    memory(140) = bitOperations.pack2ValuesToByte(8,1)
    memory(141) = bitOperations.pack2ValuesToByte(0, 6)
    // math operation(8): subtract(5): value from to register (1) and register (15) and store result in register(1)
    memory(142) = bitOperations.pack2ValuesToByte(8,1)
    memory(143) = bitOperations.pack2ValuesToByte(15, 5)
    // return from procedure
    memory(144) = 0
    memory(145) = 0xEE.toByte

    //call (2) procedure from memory: 140 (hex == 0x8C)
    memory(16) = 0x20.toByte
    memory(17) = 0x8C.toByte
    //call (2) procedure from memory: 140 (hex == 0x8C)
    memory(18) = 0x20.toByte
    memory(19) = 0x8C.toByte
    println("registers:", registers.mkString("<", ",", ">"))
  }

  def executeSingleCycle(): Unit =
  {
    //fetch opcode
    val currentOpcode : Short = bitOperations.pack2ValuesToShort(memory(pcRegister), memory(pcRegister+1))
    // decode, execute
    val cpuInstruction = new CpuInstruction(currentOpcode)
    println("executing instruction: ", (cpuInstruction.opcodeValue.toLong & 0xFFFF).toHexString, ", pc register: ", pcRegister)
    val updatePcRegister = executeSingleInstruction(cpuInstruction)

    println("registers:", registers.mkString("<", ",", ">"))

    //update pcRegister (program counter)
    if (updatePcRegister)
      pcRegister = (pcRegister + 2).toShort

    //update timers
    handleTimers()
  }

  def handleTimers(): Unit =
  {
    if (delayTimer > 0)
      delayTimer = (delayTimer - 1).toByte
    if (soundTimer > 0)
      {
        soundTimer = (soundTimer - 1).toByte
        if (delayTimer == 0)
          println("Beep or whatever")
      }
  }

  //returns true if update of program counter register is required (jump/call was not executed).
  def executeFlowControlOperation(instruction: CpuInstruction): Boolean =
  {
    if (instruction.parts(0) == 1 || instruction.parts(0) == 11) // absolute or relative jump
    {
      val jumpAddress : Short = (instruction.opcodeValue & 0xFFF).toShort //aka NNN value
      pcRegister = (jumpAddress + (if (instruction.parts(0) == 1) 0.toByte else registers(0))).toShort
      false
    }
    else if (instruction.parts(0) == 0 || instruction.parts(0) == 2) //call or return
    {
      if (instruction.opcodeValue == 0X00EE) //return
      {
        pcRegister = stack(stackPointer)
        stackPointer = (stackPointer - 1).toShort
        true //true because after returning from procedure we don't want to execute the same instruction (== call) once again
      }
      else //call
      {
        stackPointer = (stackPointer + 1).toShort
        stack(stackPointer) = pcRegister
        pcRegister = (instruction.opcodeValue & 0xFFF).toShort //aka NNN value
        false
      }
    }
    else //conditional jumps - 3,4,5,9
    {
      val vxRegisterValue : Byte = registers(instruction.operandX.get) //left hand side of comparison
      val rightHandSideOfComparison : Byte = if (instruction.parts(0) == 5) registers(instruction.operandY.get) else bitOperations.pack2ValuesToByte(instruction.parts(2), instruction.parts(3))
      var executeJump = vxRegisterValue == rightHandSideOfComparison
      if (instruction.parts(0) == 4 || instruction.parts(0) == 9) //inverted case
        executeJump = !executeJump
      if (executeJump)
      {
        pcRegister = (pcRegister + 2).toShort
      }
      !executeJump
    }
    //todo: add throwing exception here in parts(0) is not in 3,4,5,9
}

  //returns true if update of program counter register is required (usual case). false happens only on flow control instructions
  def executeSingleInstruction(instruction : CpuInstruction): Boolean =
  {
    instruction.opCodeType match
    {
      case CpuInstructionType.MATH_OPERATION => executeMathOperation(instruction); true // 6 or 7 or 8
      case CpuInstructionType.FLOW_CONTROL => executeFlowControlOperation(instruction)
      case CpuInstructionType.MEMORY => executeMemoryOperation(instruction); true
      case CpuInstructionType.TIMER => executeTimerOperation(instruction); true
      case CpuInstructionType.DISPLAY => executeDisplayOperation(instruction); true
      case _ => throw new  RuntimeException("Not supported opcode: " + instruction.opCodeType.toString)
    }
  }

  def calculateMathOperationResult(operationSpecificType: Byte, xOperand: Byte, yOperand: Byte): (Byte, Option[Byte]) =
  {
    //DONE: handle changing flags (VF used for carry and borrow) - important only when
    // operationSpecificType == 4 - 7 | 14 - most likely done, not sure whether conditions
    // for carry and borrow should be inclusive (e.g. >= ) or not ( > ) ? -- done, but tests would be nice :)

    val operationsMap = Map[Byte, (Byte, Byte) => (Byte, Option[Byte])](
      0.toByte -> ((x, y) => (y, None)),
      1.toByte -> ((x, y) => ((x|y).toByte, None)),
      2.toByte -> ((x, y) => ((x & y).toByte, None)),
      3.toByte -> ((x, y) => ((x^y).toByte, None)),
      4.toByte -> ((x, y) => {val res = x+y; (res.toByte, if (res > 127) Some(1.toByte) else Some(0.toByte))}),
      5.toByte -> ((x, y) => ((x-y).toByte, (if (x >= y) Some(1.toByte) else Some(0.toByte)))),
      6.toByte -> ((x, y) => ((x >>> 1).toByte, Some(bitOperations.getNthBitValue(x, 0)))),
      7.toByte -> ((x, y) => ((y-x).toByte, Some(if (y >= x) 1.toByte else 0.toByte))),
      14.toByte -> ((x, y) => ((x << 1).toByte, Some(bitOperations.getNthBitValue(x, 7))))
    )
    val operation = operationsMap.get(operationSpecificType)
    operation match
    {
      case Some(value) => value(xOperand, yOperand)
      case None => throw new RuntimeException(f"Not supported value in calculateMathOperationResult, operationsSpecificType: $operationSpecificType%s")
    }
  }

  def executeMathOperation(instruction : CpuInstruction): Unit =
  {
    val rightSide = bitOperations.pack2ValuesToByte(instruction.parts(2), instruction.parts(3))
    val xOperand = registers(instruction.operandX.get)
    var operationResult = instruction.parts(0) match
    {
      case 6 => rightSide
      case 7 => (xOperand + rightSide).toByte
        //Note: carry flag should not be changed here
      case 8 =>
      {
        var (newRegisterValue, flagRegisterValue)  = calculateMathOperationResult(instruction.parts(3), xOperand, registers(instruction.operandY.get));
        if (flagRegisterValue.isDefined)
          {
            registers(0xF) = flagRegisterValue.get
          }
        newRegisterValue
      }
      case _ => throw new RuntimeException("Not supported instruction: " + instruction.toString)
    }
    registers(instruction.operandX.get) = operationResult
  }

  def executeMemoryOperation(instruction: CpuInstruction): Unit =
  {
    if (instruction.parts(0) == 0xA)
      {
        iRegister = (instruction.opcodeValue & 0xFFF).toShort //aka NNN value
      }
    else if ((instruction.opcodeValue & 0x00FF) == 0x1E)
      {
        iRegister = (iRegister + registers(instruction.operandX.get)).toShort
      }
    else if ((instruction.opcodeValue & 0x00FF) == 0x29)
      {
        //todo: handle sprite loading
      }
    else if ((instruction.opcodeValue & 0x00FF) == 0x55)
      {
        //reg dump
        for {i <- 0 to instruction.operandX.get}
        {
          memory(iRegister + i) = registers(i)
        }
      }
    else if ((instruction.opcodeValue & 0x00FF) == 0x65)
    {
      //reg load
      for {i <- 0 to instruction.operandX.get}
      {
        registers(i) = memory(iRegister + i)
      }
    }else
    throw new RuntimeException("Not supported instruction: " + instruction.toString)
  }

  def executeTimerOperation(instruction: CpuInstruction): Unit =
  {
    //timer and sound
    if (instruction.parts(3) == 7)
      {
        registers(instruction.operandX.get) = delayTimer
      }
    else if (instruction.parts(3) == 0x15)
      {
        delayTimer = registers(instruction.operandX.get)
      }
    else if (instruction.parts(3) == 0x18)
      {
        soundTimer = registers(instruction.operandX.get)
      }else
      throw new RuntimeException("Not supported instruction: " + instruction.toString)
  }

  def executeDisplayOperation(instruction: CpuInstruction): Unit =
  {
    if (instruction.parts(0) == 0)
      {
        graphicsMemory = Array.fill[Boolean](GraphicsBufferWidth * GraphicsBufferHeight)(false)
      }
    else if (instruction.parts(0) == 0xD)
      {
        val xPos = instruction.operandX.get
        val yPos = instruction.operandY.get
        val spriteWidth = 8
        val spriteHeight = instruction.parts(2)
        var finalVFValue = false
        for {y <- yPos until spriteHeight+yPos}
          {
            for {x <- xPos until spriteWidth+xPos}
              {
                val memoryLocation = GraphicsBufferWidth / 8 * y + xPos / 8 //in bytes
                val bitIndex : Byte = (x - xPos).toByte
                val oldPixelValue = graphicsMemory(memoryLocation*8 + bitIndex)
                val spritePixelValue : Boolean = bitOperations.getNthBitValue(memory(iRegister + memoryLocation), bitIndex) == 1//todo: is it ok or maybe order of bits should be reverted ?
                finalVFValue = finalVFValue | (oldPixelValue ^ spritePixelValue)
                graphicsMemory(memoryLocation*8 + bitIndex) = spritePixelValue
              }
          }
        registers(0xF) = if (finalVFValue) 1 else 0
      }
    else
      throw new RuntimeException("Not supported instruction: " + instruction.toString)
  }
}