
import com.chip8emulator.{Chip8VM, bitOperations}
import org.scalatest._

class CpuInstructionExecutorTests extends FlatSpec {

  def initializeGeneralRegisters(emulator: Chip8VM): Unit = {
    for {regIndex <- emulator.registers.indices} {
      emulator.registers(regIndex) = (regIndex * 3 + 5).toByte
    }
  }

  def clearGeneralRegisters(emulator: Chip8VM): Unit = {
    for {regIndex <- emulator.registers.indices} {
      emulator.registers(regIndex) = 0
    }
  }

  def setMemoryForSimpleCallAndReturnTests(memory: Array[Byte]): Unit = {
    // set (6) register (2) to 31 (31 == 1 << 4 | 15)
    memory(0) = bitOperations.pack2ValuesToByte(6, 2)
    memory(1) = bitOperations.pack2ValuesToByte(1, 15)

    // add (7) to register (14) value:  33 (2 << 4 | 1)
    memory(2) = bitOperations.pack2ValuesToByte(7, 14)
    memory(3) = bitOperations.pack2ValuesToByte(2, 1)

    // add (7) to register (14) value:  66 (4 << 4 | 2)
    memory(4) = bitOperations.pack2ValuesToByte(7, 14)
    memory(5) = bitOperations.pack2ValuesToByte(4, 2)

    // math operation(8): add(4) values from to register (14) and register (2) and store result in register(14)
    memory(6) = bitOperations.pack2ValuesToByte(8, 14)
    memory(7) = bitOperations.pack2ValuesToByte(2, 4)

    // math operation(8): XOR(4) values from to register (14) and register (2) and store result in register(14)
    memory(8) = bitOperations.pack2ValuesToByte(8, 14)
    memory(9) = bitOperations.pack2ValuesToByte(2, 3)

    // set (6) register (1) to 67 (67 == 4 << 4 | 3)
    memory(10) = bitOperations.pack2ValuesToByte(6, 1)
    memory(11) = bitOperations.pack2ValuesToByte(4, 3)

    // set (6) register (15) to 3
    memory(12) = bitOperations.pack2ValuesToByte(6, 15)
    memory(13) = bitOperations.pack2ValuesToByte(0, 0)

    // math operation(8): rotate right(6) by 1 value from to register (1) and store result in register(1)
    memory(14) = bitOperations.pack2ValuesToByte(8, 1)
    memory(15) = bitOperations.pack2ValuesToByte(0, 6)

    //simple procedure:
    // reg(1) = reg(1) >> 1
    // reg(1) = reg(1) - reg(15)
    // math operation(8): rotate right(6) by 1 value from to register (1) and store result in register(1)
    memory(140) = bitOperations.pack2ValuesToByte(8, 1)
    memory(141) = bitOperations.pack2ValuesToByte(0, 6)
    // math operation(8): subtract(5): value from to register (1) and register (15) and store result in register(1)
    memory(142) = bitOperations.pack2ValuesToByte(8, 1)
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
  }

  "A Chip8Emulator" should "have clean memory after calling reset method" in {
    var emulator = new Chip8VM()
    emulator.reset()
    assert(emulator.memory.drop(512).forall(_ == 0))
  }

  //0x9XY0
  "A conditional jump instruction " should "trigger jump only if condition is satisfied" in {
    var emulator = new Chip8VM()
    emulator.reset()
    emulator.pcRegister = 0x200

    //set V0 (register 0) to 3
    emulator.memory(0x200) = 0x60.toByte
    emulator.memory(0x201) = 0x03.toByte

    //set V1 (register 1) to 5
    emulator.memory(0x202) = 0x61.toByte
    emulator.memory(0x203) = 0x05.toByte

    //skip next instruction if V0 != V1
    emulator.memory(0x204) = 0x90.toByte
    emulator.memory(0x205) = 0x10.toByte

    emulator.executeSingleCycle(0)
    emulator.executeSingleCycle(0)
    emulator.executeSingleCycle(0)

    //V0 and V1 are not equal, therefor jump should be taken -> pcRegister should be equal to addres of comparison + 4
    assert(emulator.pcRegister == 0x208)
  }

  //0xBNNN
  "An unconditional absolute jump instruction " should " jump to proper location" in {
    var emulator = new Chip8VM()
    emulator.reset()
    emulator.pcRegister = 0x200

    emulator.registers(0) = 8

    //jump to NNN( == 0x128) + V0
    emulator.memory(0x200) = 0xB1.toByte
    emulator.memory(0x201) = 0x28.toByte

    emulator.executeSingleCycle(0)

    assert(emulator.pcRegister == 0x128 + 8)
  }


  //0xANNN
  "A set address register instruction " should " set address register to proper value" in {
    var emulator = new Chip8VM()
    emulator.reset()
    emulator.pcRegister = 0x200

    emulator.iRegister = 0
    assert(emulator.iRegister == 0)

    //set addres register to NNN( == 0x123)
    emulator.memory(0x200) = 0xA1.toByte
    emulator.memory(0x201) = 0x23.toByte

    emulator.executeSingleCycle(0)

    assert(emulator.iRegister == 0x123)
  }


  //0xANNN
  "A set memory dump and load instructions " should " work" in {
    var emulator = new Chip8VM()
    emulator.reset()
    emulator.pcRegister = 0x200

    emulator.iRegister = 0
    assert(emulator.iRegister == 0)

    initializeGeneralRegisters(emulator)

    //FA55 - dump registers 0-0xA (inclusive)
    emulator.memory(0x200) = 0xFA.toByte
    emulator.memory(0x201) = 0x55.toByte

    //6411 - assign 0x21 to register 0x4
    emulator.memory(0x202) = 0x64.toByte
    emulator.memory(0x203) = 0x21.toByte

    //FA65 - load registers 0-0xA (inclusive)
    emulator.memory(0x204) = 0xFA.toByte
    emulator.memory(0x205) = 0x65.toByte


    emulator.executeSingleCycle(0) //dump
    clearGeneralRegisters(emulator)
    for {regIndex <- emulator.registers.indices} {
      assert(emulator.registers(regIndex) == 0)
    }

    emulator.executeSingleCycle(0) //assign
    assert(emulator.registers(0x4) == 0x21)

    emulator.executeSingleCycle(0) //load

    for {regIndex <- 0 to 10} {
      assert(emulator.registers(regIndex) == (regIndex * 3 + 5).toByte)
    }
  }

  //0xANNN
  "A call and return instructions " should " work" in {
    var emulator = new Chip8VM()
    emulator.reset()
    emulator.pcRegister = 0
    emulator.iRegister = 0
    setMemoryForSimpleCallAndReturnTests(emulator.memory)

    while (emulator.pcRegister != 20) {
      emulator.executeSingleCycle(0)
    }
  }
}


//todo: testy: call, return, +0xANNN ( I = NNN), +reg_dump, +reg_load
