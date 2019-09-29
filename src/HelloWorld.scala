import java.awt.{FlowLayout, Image}
import java.awt.image.BufferedImage

import com.chip8emulator.CpuInstructionType.CpuInstructionType
import com.chip8emulator.{Chip8VM, CpuInstruction, DisplayEmulator, bitOperations}
import javax.swing.{ImageIcon, JFrame, JLabel}


object HelloWorld {
  /* This is my first java program.
  * This will print 'Hello World' as the output
  */
  def main(args: Array[String]) {
    println("Hello, world!") // prints Hello World
    var c = new Chip8VM()
    c.reset()
//    c.loadIbmLogoProgram()
//    c.loadShowTestSpriteProgram()
    c.loadShowNumber5SpriteProgram()

    var display = new DisplayEmulator(c.GraphicsBufferWidth, c.GraphicsBufferHeight, 8)
    while (true) {
      display.fillWithDataFromList(c.graphicsMemory)
      display.show()
      c.executeSingleCycle()
      Thread.sleep((1000.0 / 60.0).toInt)
    }
  }
}


//TODO:
//wiecej testow
//obsluga: +call/return, +memory, +keyboard(z grubsza jest...), +timer, +display, +other
//obsluga czestotliwosci taktowanai procesora (== ograniczyc szybkosc), wczytywanie ROM, ograniczenie rozmiaru stosu,
// io: klawiatura, +wyswietlacz, glosnik
//zrobione opcody: 0-15
//+odczytywanie kolejnych instrukcji w petli
//+obsluga wyjatkow dla nieznanych instrukcji