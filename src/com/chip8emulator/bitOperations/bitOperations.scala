package com.chip8emulator

package object bitOperations {
  def unpackTo4Bytes(value: Short): Array[Byte] = {
    Array((value >> 12 & 15).toByte, (value >> 8 & 15).toByte,
      (value >> 4 & 15).toByte, (value & 15).toByte)
  }

  def pack2ValuesToByte(olderBits: Byte, youngerBits: Byte): Byte = {
    (olderBits << 4 | (youngerBits & 15)).toByte
  }

  def pack2ValuesToShort(olderBits: Byte, youngerBits: Byte): Short = {
    (olderBits << 8 | (youngerBits & 255)).toShort
  }

  def getLowerHalf(value: Short): Byte = {
    (value & 0xFF).toByte
  }

  def getUpperHalf(value: Short): Byte = {
    ((value >> 8) & 0xFF).toByte
  }

  // bitIndex is counted from the least significant (0 index -> last bit)
  def getNthBitValue(value: Byte, bitIndex: Byte): Byte = {
    (value & (1 << bitIndex)).toByte
  }

  def fillMemoryPart[T](memory: Array[T], srcStartIndex: Int, elements: Array[T]): Unit = {
    Array.copy(elements, 0, memory, srcStartIndex, elements.size)
  }

  def unpackToBitList(value : Byte, existingList : List[Boolean] = Nil) : List[Boolean] = {
    for {i <- 7 to 0 by -1}{
      existingList.appended(getNthBitValue(value, i.toByte) == 1)
    }
    existingList
  }
}
