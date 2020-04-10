package e93.assembler.test;

import e93.assembler.Assembler;
import e93.assembler.Instruction;
import e93.assembler.InstructionParser;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author markford
 */
public class AssemblerTest {

    @Test
    public void encodeTwoRegisterType() {
        Instruction instruction = InstructionParser.parse("AND $r1, $r2");
        int encoded = Assembler.encode(instruction);

        // instruction in binary == 0001000100100001
        // opc   r1   r2  func
        // 0001 0001 0010 0001
        // convert to hex
        //   1    1    2    1
        assertEquals(0x1121, encoded);
    }

    @Test
    public void decodeTwoRegisterType() {
        Instruction expected = InstructionParser.parse("AND $r1, $r2");
        int encoded = Assembler.encode(expected);
        assertEquals(expected, Assembler.decode(encoded));
    }

    @Test
    public void encodeJumpImmediate() {
        Instruction expected = InstructionParser.parse("J 0x6");
        int encoded = Assembler.encode(expected);
        assertEquals(expected, Assembler.decode(encoded));
    }

    @Test
    public void parse() throws Exception {
        String program = "AND $r1, $r0\n" +
                         "ADDI $r1, 123";
        List<Instruction> instructions = InstructionParser.parse(new StringReader(program));
        assertEquals(2, instructions.size());
    }
}
