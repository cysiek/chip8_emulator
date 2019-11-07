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
    //dzialajace:
    //emulator.loadShowTestSpriteProgram()
    //emulator.loadShowNumber5SpriteProgram()
    emulator.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\IBM Logo.ch8")
    //emulator.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\Zero Demo [zeroZshadow, 2007].ch8")
    //emulator.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\chip8-master\\programs\\Random Number Test [Matthew Mikolay, 2010].ch8")

    //wyglada ok, ale rzuca warningami - graphicsMemoryLocation: 2118 is outside graphics memory! Either bug or bigger screen should be used. xPos: 6, x: 6, yPos: 33, y: 33, spriteHeight: 1
    //emulator.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\chip8-master\\demos\\Particle Demo [zeroZshadow, 2008].ch8")



    //niedzialajace (w roznym stopniu):
    //wzglednie dziala ale chyba nie do konca...
//    emulator.loadAnimalRaceProgram()


    // uruchamia sie, wyswietla keypad, po kliknieciu czegkolwiek  - rzuca warninga i wyswietla jakis syf
//    emulator.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\chip8-master\\programs\\Keypad Test [Hap, 2006].ch8")

    //wyswietla cos (bez wiekszego sensu) po kliknieciu klawisza
//    emulator.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\Life [GV Samways, 1980].ch8")

    //po kliknieciu kilku przyciskow costam wyswietla, potem rzuca wyjatkiem Exception in thread "main" java.lang.RuntimeException: Not supported value in calculateMathOperationResult, operationsSpecificType: 10
    //	at com.chip8emulator.Chip8VM.calculateMathOperationResult(Chip8VM.scala:214)
//        emulator.loadProgramFromFile("C:\\Users\\cysie\\IdeaProjects\\chip8_emulator\\data\\chip8-master\\programs\\Clock Program [Bill Fisher, 1981].ch8")

    var display = new DisplayEmulator(emulator.GraphicsBufferWidth, emulator.GraphicsBufferHeight, 8)
    var quit = false
    while (!quit) {
      display.fillWithDataFromList(emulator.graphicsMemory)
      display.show()
      quit = display.lastpressedKey.toChar == 'P'
      emulator.executeSingleCycle(display.lastpressedKey)
      //      Thread.sleep((1000.0 / 60.0).toInt) //original speed
      Thread.sleep((1000.0 / 600.0).toInt)
    }
    println("papa!")
  }
}


//TODO:
//wiecej testow
//obsluga: +call/return, +memory, +keyboard(z grubsza jest...), +timer, +display, +other
//obsluga czestotliwosci taktowanai procesora (== ograniczyc szybkosc), wczytywanie ROM, ograniczenie rozmiaru stosu,
// io: klawiatura, +wyswietlacz, glosnik
//czemu nie dziala wychodzenie/zamykanie przy uzyciu klawiatury ?
//+sprawdzic czy wyswietlanie spritow jest ok - czemu w loadShowNumber5SpriteProgram cyfra wyswietla sie przy lewej krawedzi ekranu a nie 5 px od niej? -- poprawione
//+zrobione opcody: wszystkie
//+odczytywanie kolejnych instrukcji w petli
//+obsluga wyjatkow dla nieznanych instrukcji
//rozkminic czy w calculateMathOperationResult powinno byc "(res.toByte, if (res > 255) Some(1.toByte) else Some(0.toByte))" czy 255->127

//linki:
//https://en.wikipedia.org/wiki/CHIP-8#Virtual_machine_description
//http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#8xyE
//  https://github.com/Dillonb/chip8
//https://massung.github.io/CHIP-8/
//  https://github.com/DavidJowett/chip8-emulator/blob/master/test/chip8_test.c
//https://github.com/SnoozeTime/chip8/blob/master/test/opcode_test.cc