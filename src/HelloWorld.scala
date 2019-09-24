import com.chip8emulator.CpuInstructionType.CpuInstructionType
import com.chip8emulator.{Chip8VM, CpuInstruction, bitOperations}



object HelloWorld {
  /* This is my first java program.
  * This will print 'Hello World' as the output
  */
  def main(args: Array[String]) {
    println("Hello, world!") // prints Hello World
    var c = new Chip8VM()
    c.reset()
    while(true)
      {
        c.executeSingleCycle()
      }
  }
}


//TODO:
//wiecej testow
//obsluga: +call/return, memory, keyboard, timer, display, other
//zrobione opcody: 0 oprocz display, 1-9, 11,
//+odczytywanie kolejnych instrukcji w petli
//+obsluga wyjatkow dla nieznanych instrukcji