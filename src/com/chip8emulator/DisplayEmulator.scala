package com.chip8emulator

import java.awt.{Color, FlowLayout}
import java.awt.image.BufferedImage

import javax.swing.{ImageIcon, JFrame, JLabel}

class DisplayEmulator(val displayWidth: Int, val displayHeight: Int) {

  val image = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_BYTE_BINARY)
  val window = prepareWindow(image)

  def setPixelValue(x: Int, y: Int, newValue: Boolean): Unit = {
    image.setRGB(x, y, if (newValue) Color.WHITE.getRGB else Color.BLACK.getRGB)
//    if (newValue) println("Setting pixel: ", x, y)
  }

  def getPixelValue(x: Int, y: Int): Boolean = {
    val color = image.getRGB(x, y)
    color == Color.WHITE.getRGB
  }

  def fillWithDataFromList(bitList: Seq[Boolean]): Unit = {
    for {y <- 0 until displayHeight} {
      for {x <- 0 until displayWidth} {
        setPixelValue(x, y, bitList(x + y * displayWidth))
      }
    }
  }

  private def prepareWindow(img : BufferedImage) = {
    val frame = new JFrame()
    frame.getContentPane.setLayout(new FlowLayout)
    frame.getContentPane.add(new JLabel(new ImageIcon(img)))
    frame.pack()
    frame
  }

  def show() : Unit = {
    window.setVisible(true)
    window.repaint()
  }
}
