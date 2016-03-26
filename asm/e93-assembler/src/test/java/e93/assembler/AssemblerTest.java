package e93.assembler;

import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author markford
 */
public class AssemblerTest {

    Assembler assembler = new Assembler();

    @Test
    public void parseTwoRegisterType() throws Exception {
        Instruction instruction = assembler.parse("AND, $r1, $r2");
        assertEquals(new Instruction().setOpcode(OpCode.ALU).setR1(1).setR2(2).setFunc(1), instruction);
    }

    @Test
    public void parseOneRegisterImmediateType() throws Exception {
        Instruction instruction = assembler.parse("ADDI, $r1, 123");
        assertEquals(new Instruction().setOpcode(OpCode.ADDI).setR1(1).setImmediate(123), instruction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidFormat() throws Exception {
        assembler.parse("AND $r1 $r2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidRegisterFormat() throws Exception {
        assembler.parse("AND, r1, r2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidRegister() throws Exception {
        assembler.parse("AND, r16, r2");
    }

    @Test
    public void encodeTwoRegisterType() throws Exception {
        Instruction instruction = assembler.parse("AND, $r1, $r2");
        int encoded = assembler.encode(instruction);

        // 4385 in decimal == 0001000100100001

        // opc   r1   r2  func
        // 0001 0001 0010 0001

        assertEquals(4385, encoded);
    }

    @Test
    public void decodeTwoRegisterType() throws Exception {
        Instruction expected = assembler.parse("AND, $r1, $r2");
        int encoded = assembler.encode(expected);
        assertEquals(expected, assembler.decode(encoded));
    }

    @Test
    public void parse() throws Exception {
        String program = "AND, $r1, $r0\n" +
                         "ADDI, $r1, 123";
        List<Instruction> instructions = assembler.parse(new StringReader(program));
        assertEquals(2, instructions.size());
    }
}
