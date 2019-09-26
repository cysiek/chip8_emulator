package com.chip8emulator

import com.chip8emulator.CpuInstructionType.{CpuInstructionType, Value}

object CpuInstructionType extends Enumeration
{
  type CpuInstructionType = Value
  val FLOW_CONTROL, MATH_OPERATION, TIMER, DISPLAY, MEMORY, KEYBOARD, OTHER = Value
  //SOUND is treated as TIMER
}

class CpuInstruction(val opcodeValue : Short)
{
  def decodeType(): CpuInstructionType =
  {
    var opCodeType : CpuInstructionType = parts(0) match
    {
      case 0 =>
      {
        if (opcodeValue == 0x00E0)
          CpuInstructionType.DISPLAY
        else
          CpuInstructionType.FLOW_CONTROL // either call or return (if (opcodeValue == 0x00EE))
      }
      case x if (x >= 1 && x <= 5) => CpuInstructionType.FLOW_CONTROL
      case 6 | 7 | 8 => CpuInstructionType.MATH_OPERATION
      case 9 | 11 => CpuInstructionType.FLOW_CONTROL
      case 10 => CpuInstructionType.MEMORY
      case 12 => CpuInstructionType.OTHER
      case 13 => CpuInstructionType.DISPLAY
      case 14 => CpuInstructionType.KEYBOARD
      case 15 =>
      {
        if (parts(3) == 10)
          CpuInstructionType.KEYBOARD
        else if (parts(3) == 3)
          CpuInstructionType.OTHER //BCD
        else
          {
            val last2Parts = bitOperations.pack2ValuesToByte(parts(2), parts(3))
            if (last2Parts > 0x18)
              CpuInstructionType.MEMORY
            else
              CpuInstructionType.TIMER
          }
      }
    }
    opCodeType
  }

  def determineXValue(): Option[Byte] =
  {
    parts(0) match
    {
      case 0 | 1 | 2 | 10 | 11 => None
      case _ => Some(parts(1))
    }
  }

  def determineYValue(): Option[Byte] =
  {
    parts(0) match
    {
      case 5 | 8 | 9 | 13 => Some(parts(2))
      case _  => None
    }
  }

  val parts : Array[Byte] = bitOperations.unpackTo4Bytes(opcodeValue)
  val opCodeType : CpuInstructionType = decodeType()
  val operandX : Option[Byte] = determineXValue()
  val operandY : Option[Byte] = determineYValue()
}
