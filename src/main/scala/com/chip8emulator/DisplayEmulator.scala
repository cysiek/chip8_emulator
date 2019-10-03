package com.chip8emulator

import java.awt.event.{KeyAdapter, KeyEvent}
import java.awt.{Color, Component, FlowLayout, Image}
import java.awt.image.BufferedImage

import javax.swing.{ImageIcon, JFrame, JLabel}

//class KeyboardEmulator extends java.awt.event.KeyListener {
//  var lastpressedKey : Int = 0
//  override def keyPressed(event: KeyEvent): Unit = {
//    lastpressedKey = event.getKeyCode()
//  }
//
//  override def keyReleased(keyEvent: KeyEvent): Unit = {
//    lastpressedKey = 0
//  }
//
//  override def keyTyped(keyEvent: KeyEvent): Unit = {}
//}

trait KeyboardEmulator extends java.awt.event.KeyListener {
  var lastpressedKey : Int = 0
  override def keyPressed(event: KeyEvent): Unit = {
    lastpressedKey = event.getKeyCode()
  }

  override def keyReleased(keyEvent: KeyEvent): Unit = {
    lastpressedKey = 0
  }

  override def keyTyped(keyEvent: KeyEvent): Unit = {}
}

class DisplayEmulator(val displayWidth: Int, val displayHeight: Int, val rescaleFactor: Int) extends KeyboardEmulator {
  val image = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_BYTE_BINARY)
  var (window, img_jlabel) = prepareWindow()

  def setPixelValue(x: Int, y: Int, newValue: Boolean): Unit = {
    image.setRGB(x, y, if (newValue) Color.WHITE.getRGB else Color.BLACK.getRGB)
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

  private def prepareWindow() = {
    val frame = new JFrame()
    frame.getContentPane.setLayout(new FlowLayout)
    val img_jlabel = new JLabel(new ImageIcon(image.getScaledInstance(image.getWidth() * rescaleFactor, image.getHeight() * rescaleFactor, Image.SCALE_REPLICATE)))
    frame.getContentPane.add(img_jlabel)
    frame.pack()
    frame.setVisible(true)
    frame.addKeyListener(this)
    (frame, img_jlabel)
  }

  def show(): Unit = {
    img_jlabel.setIcon(new ImageIcon(image.getScaledInstance(image.getWidth() * rescaleFactor, image.getHeight() * rescaleFactor, Image.SCALE_REPLICATE)))
    window.repaint()
  }
}



