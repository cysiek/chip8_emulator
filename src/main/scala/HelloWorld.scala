import java.awt.{FlowLayout, Image}
import java.awt.image.BufferedImage

import com.chip8emulator.CpuInstructionType.CpuInstructionType
import com.chip8emulator.{Chip8VM, CpuInstruction, DisplayEmulator, bitOperations}
import javax.swing.{ImageIcon, JFrame, JLabel}


object HelloWorld {

  def main(args: Array[String]) {
    println("Hello, world!") // prints Hello World
    var emulator = new Chip8VM()
    emulator.reset()
//    c.loadIbmLogoProgram()
//    c.loadShowTestSpriteProgram()
//    c.loadShowNumber5SpriteProgram()
//    c.loadAnimalRaceProgram()
//    c.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\IBM Logo.ch8")
//    c.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\Life [GV Samways, 1980].ch8")
//    c.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\Zero Demo [zeroZshadow, 2007].ch8")
//    c.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\chip8-master\\programs\\Keypad Test [Hap, 2006].ch8")
//    c.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\chip8-master\\programs\\Clock Program [Bill Fisher, 1981].ch8")
//    c.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\chip8-master\\programs\\Random Number Test [Matthew Mikolay, 2010].ch8")
    emulator.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\chip8-master\\demos\\Particle Demo [zeroZshadow, 2008].ch8")

    var display = new DisplayEmulator(emulator.GraphicsBufferWidth, emulator.GraphicsBufferHeight, 8)
    while (true) {
      display.fillWithDataFromList(emulator.graphicsMemory)
      display.show()
      emulator.executeSingleCycle(display.lastpressedKey)
      Thread.sleep((1000.0 / 60.0).toInt)
    }
  }
}


//TODO:
//wiecej testow
//obsluga: +call/return, +memory, +keyboard(z grubsza jest...), +timer, +display, +other
//obsluga czestotliwosci taktowanai procesora (== ograniczyc szybkosc), wczytywanie ROM, ograniczenie rozmiaru stosu,
// io: klawiatura, +wyswietlacz, glosnik
//+sprawdzic czy wyswietlanie spritow jest ok - czemu w loadShowNumber5SpriteProgram cyfra wyswietla sie przy lewej krawedzi ekranu a nie 5 px od niej? -- poprawione
//+zrobione opcody: wszystkie
//+odczytywanie kolejnych instrukcji w petli
//+obsluga wyjatkow dla nieznanych instrukcji