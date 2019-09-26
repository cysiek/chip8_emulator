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
//obsluga: +call/return, memory(prawie gotowe, brakuje tylko fx29 - sprite), keyboard, +timer, +display, other
//zrobione opcody: 0-11, 13
//+odczytywanie kolejnych instrukcji w petli
//+obsluga wyjatkow dla nieznanych instrukcji