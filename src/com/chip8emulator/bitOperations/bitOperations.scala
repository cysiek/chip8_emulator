package com.chip8emulator

package object bitOperations
{
  def unpackTo4Bytes(value : Short) : Array[Byte] =
  {
    Array((value >> 12 & 15).toByte, (value >> 8 & 15).toByte,
    (value >> 4 & 15).toByte, (value & 15).toByte)
  }

  def pack2ValuesToByte(olderBits : Byte, youngerBits : Byte) : Byte =
  {
    (olderBits << 4 | (youngerBits & 15)).toByte
  }

  def pack2ValuesToShort(olderBits : Byte, youngerBits : Byte) : Short =
    {
      (olderBits << 8 | (youngerBits & 255)).toShort
    }

  // bitIndex is counted from the least significant (0 index -> last bit)
  def getNthBitValue(value : Byte, bitIndex : Byte): Byte =
  {
    (value & (1 << bitIndex)).toByte
  }
}
