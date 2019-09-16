import CpuInstructionType.CpuInstructionType

package object bitOperations
{
  def unpackTo4Bytes(value : Short) : Array[Byte] =
  {
    Array((value >> 12 & 15).toByte, (value >> 8 & 15).toByte,
    (value >> 4 & 15).toByte, (value & 15).toByte)
  }

  def pack2ValuesToByte(olderBits : Byte, youngerBits : Byte) : Byte =
  {
    (olderBits << 4 | youngerBits).toByte
  }

  def pack2ValuesToShort(olderBits : Byte, youngerBits : Byte) : Short =
    {
      (olderBits << 8 | youngerBits).toShort
    }
}

class CpuInstruction(val opcodeValue : Short)
{
  def decodeType(): CpuInstructionType =
  {
    var opCodeType : CpuInstructionType = parts(0) match
    {
      case x if (x >= 1 && x <= 5) => CpuInstructionType.FLOW_CONTROL
      case 9 | 11 => CpuInstructionType.FLOW_CONTROL
      case 6 | 7 => CpuInstructionType.ASSIGN
      case 13 => CpuInstructionType.DISPLAY
    }
    opCodeType
  }

  def determineXValue(): Option[Byte] =
  {
    parts(0) match
    {
      case 0 | 1 | 2 | 10 | 11 => None
      case _ => Some(parts(1))
    }
  }

  def determineYValue(): Option[Byte] =
  {
    parts(0) match
    {
      case 5 | 8 | 9 | 13 => Some(parts(2))
      case _  => None
    }
  }

  val parts : Array[Byte] = bitOperations.unpackTo4Bytes(opcodeValue)
  val opCodeType : CpuInstructionType = decodeType()
  val XOperand : Option[Byte] = determineXValue()
  val yOperand : Option[Byte] = determineYValue()
}

object CpuInstructionType extends Enumeration
{
  type CpuInstructionType = Value
  val FLOW_CONTROL, ASSIGN, TIMER, DISPLAY, MATH = Value
}

//class CpuInstructionExecutor(instruction : CpuInstruction)
//{
//
//}

class Chip8VM()
{
  private var pcRegister, iRegister, stackPointer : Short = 0

  private var memory : Array[Byte] = new Array[Byte](4096)
  private var registers : Array[Byte] = new Array[Byte](16)
  //0x000-0x1FF - Chip 8 interpreter (contains font set in emu)
  //0x050-0x0A0 - Used for the built in 4x5 pixel font set (0-F)
  //0x200-0xFFF - Program ROM and work RAM

  private var keyboardState : Array[Byte] = new Array[Byte](16)
  private var graphicsMemory : Array[Byte] = new Array[Byte](64*32)
  // a moze bool ?

  private var stack : Array[Short] = new Array[Short](24)

  private var delayTimer, soundTimer : Byte = 0

  def reset(): Unit =
  {
    pcRegister = 0
    // set (6) register (2) to 31 (1 << 4 | 15)
    memory(0) = bitOperations.pack2ValuesToByte(6,2)
    memory(1) = bitOperations.pack2ValuesToByte(1, 15)

    // add (7) to register (15) value:  33 (2 << 4 | 1)
    memory(2) = bitOperations.pack2ValuesToByte(7,15)
    memory(3) = bitOperations.pack2ValuesToByte(2, 1)

    // add (7) to register (15) value:  66 (4 << 4 | 2)
    memory(4) = bitOperations.pack2ValuesToByte(7,15)
    memory(5) = bitOperations.pack2ValuesToByte(4, 2)
    println("registers:", registers.mkString("<", ",", ">"))
  }

  def executeSingleCycle(): Unit =
  {
    //fetch opcode
//    val currentOpcode : Short = (memory(pcRegister) << 8 | memory(pcRegister + 1)).toShort;
    val currentOpcode : Short = bitOperations.pack2ValuesToShort(memory(pcRegister), memory(pcRegister+1))
    val cpuInstruction = new CpuInstruction(currentOpcode)

    executeSingleInstruction(cpuInstruction)
    println("registers:", registers.mkString("<", ",", ">"))
    pcRegister = (pcRegister + 2).toShort
//    if (opCode.opcodeType)
    //
    // decode, execute
    //update timers
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

  def executeSingleInstruction(instruction : CpuInstruction): Unit =
  {
    instruction.opCodeType match
    {
      case CpuInstructionType.ASSIGN => executeAssignInstruction(instruction) // 6 or 7
//      case _ => exception or log - unknow instruction
    }

  }

  def executeAssignInstruction(instruction : CpuInstruction): Unit =
  {
//    val rightSide = (instruction.parts(2) << 4 | instruction.parts(3)).toByte
    val rightSide = bitOperations.pack2ValuesToByte(instruction.parts(2), instruction.parts(3))
    registers(instruction.XOperand.get) = instruction.parts(0) match
    {
      case 6 => rightSide
      case 7 => (registers(instruction.XOperand.get) + rightSide).toByte
        //Note: carry flag should not be changed here
    }
  }
}



object HelloWorld {
  /* This is my first java program.
  * This will print 'Hello World' as the output
  */
  def main(args: Array[String]) {
    println("Hello, world!") // prints Hello World
    var c = new Chip8VM()
    c.reset()
    c.executeSingleCycle()
    c.executeSingleCycle()
    c.executeSingleCycle()
  }
}
