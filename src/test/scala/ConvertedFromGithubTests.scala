
import com.chip8emulator.{Chip8VM, bitOperations}
import org.scalatest._

class ConvertedFromGithubTests extends FlatSpec {
  "A opcode op_annn" should "work as in tests from github" in {
    var chip8 = new Chip8VM();

    // 0xA2F0 - mvi 2F0h - move 2F0 in I
    val source = List(0xA2, 0xF0).map(_.toByte);
    chip8.loadProgramFromBuffer(source, 0x200);
    chip8.executeSingleCycle(0);

    val i = chip8.iRegister;
    assert(0x2F0 == i);
    assert(0x202 == chip8.pcRegister);
  }

  "A opcode op_2nnn" should "work as in tests from github" in {
    var chip8 = new Chip8VM();

    // 0x2204 - execute subroutine at index 204.
    val source = List(0x22, 0x04, 0xA2, 0xF0, 0xA2, 0xFF).map(_.toByte);
    chip8.loadProgramFromBuffer(source, 0x200);
    chip8.executeSingleCycle(0);

    // stack should be one. pc would be stored as 0x200. Current pc is 0x204.
    assert(1 == chip8.stackPointer);
    assert(0x204 == chip8.pcRegister);
    assert(0x200 == chip8.stack(0));

    // to confirm execute the next cycle
    chip8.executeSingleCycle(0);
    val i = chip8.iRegister;
    assert(0x2FF == i);
  }

  "A opcode return_sub_op_00ee" should "work as in tests from github" in {

    var chip8 = new Chip8VM();

    // 0x2204 - execute subroutine at index 204.
    val source = List(
      0x22, 0x04, // execute 0xA2FF subroutine
      0xA2, 0xF0,
      0xA2, 0xFF, // subroutine start.
      0x00, 0xEE).map(_.toByte);
    chip8.loadProgramFromBuffer(source, 0x200);
    chip8.executeSingleCycle(0);

    // stack should be one. pc would be stored as 0x200. Current pc is 0x204.
    assert(1 == chip8.stackPointer);
    assert(0x204 == chip8.pcRegister);
    assert(0x200 == chip8.stack(0));

    // to confirm execute the next cycle
    chip8.executeSingleCycle(0);
    assert(0x2FF == chip8.iRegister);

    chip8.executeSingleCycle(0); // return;
    assert(0 == chip8.stackPointer);
    assert(0x202 == chip8.pcRegister);

    chip8.executeSingleCycle(0);
    assert(0x2F0 == chip8.iRegister);
  }

  "A opcode assign_6xNN" should "work as in tests from github" in {
    var chip8 = new Chip8VM();

    // Assign 4 to V[1].
    val source = List(0x61, 0x04).map(_.toByte);
    chip8.loadProgramFromBuffer(source, 0x200);
    chip8.executeSingleCycle(0);

    assert(0x202 == chip8.pcRegister);
    assert(4 == chip8.registers(1));

  }

  "A opcode add_8xy4_no_carry" should "work as in tests from github" in {
    // Add y to x and store in x.
    var chip8 = new Chip8VM();

    // Assign 4 to V[1], assign 6 to v[2], add V[1] to v[2] and store in v[1]
    val source = List(0x61, 0x04, 0x62, 0x06, 0x81, 0x24).map(_.toByte);
    chip8.loadProgramFromBuffer(source, 0x200);
    chip8.executeSingleCycle(0);
    assert(4 == chip8.registers(1));
    chip8.executeSingleCycle(0);
    assert(6 == chip8.registers(2));
    chip8.executeSingleCycle(0);

    assert(0x206 == chip8.pcRegister);
    assert(10 == chip8.registers(1));
    assert(0 == chip8.registers(0xF));
  }

  "A opcode add_8xy4_carry" should "work as in tests from github" in {
    // Add y to x and store in x.
    var chip8 = new Chip8VM();

    // Assign 4 to V[1], assign 6 to v[2], add V[1] to v[2] and store in v[1]
    val source = List(0x61, 0xF4, 0x62, 0x10, 0x81, 0x24).map(_.toByte);
    chip8.loadProgramFromBuffer(source, 0x200);
    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);

    assert(0x206 == chip8.pcRegister);

    // 0xF4 + 0x10 = 260 -> should have 4 in the register + carry flag
    assert(4 == chip8.registers(1));
    assert(1 == chip8.registers(0xF));
  }

  "A opcode draw_dxyn" should "work as in tests from github" in {
    /*
     * HEX    BIN        Sprite
        0x3C   00111100     ****
        0xC3   11000011   **    **
        0xFF   11111111   ********
     */

    // The sprite is at location I. Heigh is 3.
    var chip8 = new Chip8VM();

    // x = 4, y = 6
    val source = List(0x61, 0x04, 0x62, 0x06,
      0xA2,
      0x08, // push 208 in I
      0xD1, // draw at coord x = v[1]
      0x23, // y = v[2], heigh = 3
      // encode the sprite here. Most likely that will be done in other place...
      0x3C,
      0xC3,
      0xFF).map(_.toByte);

    chip8.loadProgramFromBuffer(source, 0x200);
    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
    assert(0x208 == chip8.iRegister);

    // then draw
    chip8.executeSingleCycle(0);

    // val gfx = chip8.gfx();
    // note - this is the end of original test. Most likely it was not implemented fully....

  }

  "A opcode cond_3xnn" should "work as in tests from github" in {
    var chip8 = new Chip8VM();

    // x = 4, y = 6
    val source1 = List(0x61, 0x04, 0x31, 0x04).map(_.toByte);
    chip8.loadProgramFromBuffer(source1, 0x200);
    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
    assert(0x206 == chip8.pcRegister);


    val source2 = List(0x61, 0x04, 0x31, 0x05).map(_.toByte);
    var chip82 = new Chip8VM();
    chip82.loadProgramFromBuffer(source2, 0x200);
    chip82.executeSingleCycle(0);
    chip82.executeSingleCycle(0);
    assert(0x204 == chip82.pcRegister);
  }

  "A opcode cond_4xnn" should "work as in tests from github" in {
    var chip8 = new Chip8VM();

    val source1 = List(0x61, 0x04, 0x41, 0x04).map(_.toByte);
    chip8.loadProgramFromBuffer(source1, 0x200);
    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
    assert(0x204 == chip8.pcRegister);


    val source2 = List(0x61, 0x04, 0x41, 0x05).map(_.toByte);
    var chip82 = new Chip8VM();
    chip82.loadProgramFromBuffer(source2, 0x200);
    chip82.executeSingleCycle(0);
    chip82.executeSingleCycle(0);
    assert(0x206 == chip82.pcRegister);
  }

  "A opcode add_constant_7xnn" should "work as in tests from github" in {

    var chip8 = new Chip8VM();

    // add 4 to V[1] which is 4
    val source = List(0x61, 0x04, 0x71, 0x04).map(_.toByte);
    chip8.loadProgramFromBuffer(source, 0x200);

    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
    assert(8 == chip8.registers(1));
  }

  "A opcode jump_1NNN" should "work as in tests from github" in {
    var chip8 = new Chip8VM();

    // 0x1204 - jump at index 204.
    val source = List(0x12, 0x04, 0xA2, 0xF0, 0xA2, 0xFF).map(_.toByte);
    chip8.loadProgramFromBuffer(source, 0x200);
    chip8.executeSingleCycle(0);

    // stack should be one. pc would be stored as 0x200. Current pc is 0x204.
    assert(0 == chip8.stackPointer);
    assert(0x204 == chip8.pcRegister);

    // to confirm execute the next cycle
    chip8.executeSingleCycle(0);
    val i = chip8.iRegister;
    assert(0x2FF == i);
  }

  // set the delay timer to VX
  "A opcode delay_FX15" should "work as in tests from github" in {
    var chip8 = new Chip8VM();

    val source = List(0x61, 0x04, 0xF1, 0x15).map(_.toByte);
    chip8.loadProgramFromBuffer(source, 0x200);

    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
//    assert(0x04 == chip8.delayTimer);
    assert(0x03 == chip8.delayTimer);
  }

  // set the sound timer to VX
  "A opcode sound_FX18" should "work as in tests from github" in {

    var chip8 = new Chip8VM();

    val source = List(0x61, 0x04, 0xF1, 0x18).map(_.toByte);
    chip8.loadProgramFromBuffer(source, 0x200);

    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
//    assert(0x04 == chip8.soundTimer);
    assert(0x03 == chip8.soundTimer);
  }
  // vx = delay timer
  "A opcode delay_FX07" should "work as in tests from github" in {
    var chip8 = new Chip8VM();

    val source = List(0x61, 0x04, 0xF1, 0x15, 0xFB, 0x07).map(_.toByte);
    chip8.loadProgramFromBuffer(source, 0x200);

    // chip8.executeSingleCycle(0);
    // chip8.executeSingleCycle(0);
    // chip8.decrease_timers(); // delay timer will beb 0x03
    // assert(0x03 == chip8.delayTimer);
    // chip8.executeSingleCycle(0);
    // assert(0x03 == chip8.registers(0xB));

    //most likely working version:
    chip8.executeSingleCycle(0); // V1 = 04
    chip8.executeSingleCycle(0); // delay timer = V1 and of course decrease delay timer
    // chip8.decrease_timers(); // delay timer will beb 0x03 <--- removed, bacause in my implementation delay is executed inside executeSingleCycle
    assert(0x03 == chip8.delayTimer);
    chip8.executeSingleCycle(0);
    assert(0x03 == chip8.registers(0xB));
  }

//  "A opcode keyboard_notpressed_op_EXA1_keypressed" should "work as in tests from github" in {
//    // will skip next instrution if key stored in VX is pressed.
//    var chip8 = new Chip8VM();
//
//    val source = List(
//      0x61, 0x04, // VX = 4
//      0xE1, 0xA1, // skip if 4 is not pressed
//      0x62, 0x01, // set v2 to 1
//      0x62, 0x00).map(_.toByte);; // set V2 to 0
//    chip8.loadProgramFromBuffer(source, 0x200);
//
//    chip8.executeSingleCycle(0);
//    // do notpress
//    chip8.set_key_pressed(4);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    assert(0x01 == chip8.registers(0x2));
//  }

  "A opcode keyboard_notpressed_op_EXA1_keynotpressed" should "work as in tests from github" in {
    var chip8 = new Chip8VM();

    val source = List(
      0x61, 0x04, // VX = 4
      0xE1, 0xA1, // skip if v1=4 is not pressed
      0x62, 0x01, // set v2 to 1
      0x62, 0x00).map(_.toByte);; // set V2 to 0
    chip8.loadProgramFromBuffer(source, 0x200);

    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
    assert(0x00 == chip8.registers(0x2));
  }


  "A opcode keyboard_pressed_op_EX9E_keynotpressed" should "work as in tests from github" in {
    // will skip next instrution if key stored in VX is pressed.
    var chip8 = new Chip8VM();

    val source = List(
      0x61, 0x04, // VX = 4
      0xE1, 0x9E, // skip if 4 is pressed
      0x62, 0x01, // set v2 to 1
      0x62, 0x00).map(_.toByte);; // set V2 to 0
    chip8.loadProgramFromBuffer(source, 0x200);

    chip8.executeSingleCycle(0);
    // do notpress
    // chip8.set_key_pressed(4);
//    chip8.executeSingleCycle(0);

    chip8.executeSingleCycle('q'); //'q' translates to 4 on "my" keyboard :)
    chip8.executeSingleCycle(0);
    assert(0x01 == chip8.registers(0x2));
  }

//  "A opcode keyboard_pressed_op_EX9E_keypressed" should "work as in tests from github" in {
//    // will skip next instrution if key stored in VX is pressed.
//    var chip8 = new Chip8VM();
//
//    val source = List(
//      0x61, 0x04, // VX = 4
//      0xE1, 0x9E, // skip if 4 is pressed
//      0x62, 0x01, // set v2 to 1
//      0x62, 0x00).map(_.toByte); // set V2 to 0
//    chip8.loadProgramFromBuffer(source, 0x200);
//
//    chip8.executeSingleCycle(0);
//    // press
//    chip8.set_key_pressed(4);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    assert(0x00 == chip8.registers(0x2));
//  }

//  "A opcode wait_key_op_FX0A" should "work as in tests from github" in {
//    var chip8 = new Chip8VM();
//
//    val source = List(
//      0xF1, 0x0A).map(_.toByte); // wait for key and store value in V1
//
//    chip8.loadProgramFromBuffer(source, 0x200);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//    chip8.executeSingleCycle(0);
//
//    // key not pressed so nothing happens.
//    assert(0x200 == chip8.pcRegister);
//    chip8.set_key_pressed(5);
//    chip8.executeSingleCycle(0);
//    assert(0x202 == chip8.pcRegister);
//    assert(0x05 == chip8.registers(0x01));
//  }

  // flow control
  "A opcode op_BNNN" should "work as in tests from github" in {

    var chip8 = new Chip8VM();

    val source = List(
      0x60, 0x0A, // V0 = A
      0xB2, 0x00).map(_.toByte); // set pc = 200 + v0

    chip8.loadProgramFromBuffer(source, 0x200);

    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
    assert(0x20A == chip8.pcRegister);
  }

  // decimal representation
  "A opcode op_FX33" should "work as in tests from github" in {
    var chip8 = new Chip8VM();

    // set memory(I) = 0x02
    // memory(I+1) = 0x05
    // memory(I+2) = 0x05
    val source = List(
      0x61, 0xFF, // V0 = FF (255)
      0xA2, 0x00, // I = 200
      0xF1, 0x33).map(_.toByte); // set pc = 200 + v0

    chip8.loadProgramFromBuffer(source, 0x200);


    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);
    chip8.executeSingleCycle(0);

    // std::cout << chip8.iRegister << std::endl;
    // std::cout << chip8.pcRegister << std::endl;
    assert(0x02 == chip8.memory(0x200));
    assert(0x05 == chip8.memory(0x201));
    assert(0x05 == chip8.memory(0x202));
  }

}
