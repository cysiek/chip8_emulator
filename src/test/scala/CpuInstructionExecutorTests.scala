
import com.chip8emulator.Chip8VM
import org.scalatest._

class CpuInstructionExecutorTests extends FlatSpec {

  "A Chip8Emulator" should "have clean memory after calling reset method" in {
    var emulator = new Chip8VM()
    emulator.reset()
    assert(emulator.memory.drop(512).forall(_ == 0))
  }

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
}
