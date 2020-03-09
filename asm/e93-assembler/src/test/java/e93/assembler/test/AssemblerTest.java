package e93.assembler.test;

import e93.assembler.Assembler;
import e93.assembler.Instruction;
import e93.assembler.OpCode;
import e93.assembler.ast.AddImmediate;
import e93.assembler.ast.And;
import e93.assembler.ast.JumpImmediate;
import e93.assembler.ast.LoadWord;
import e93.assembler.ast.OrImmediate;
import e93.assembler.ast.StoreWord;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author markford
 */
public class AssemblerTest {

    private Assembler assembler = new Assembler();

    // todo - most of these tests are about parsing instructions, move them to an InstructionParser and test
    @Test
    public void parseTwoRegisterType() {
        Instruction actual = assembler.parse("AND, $r1, $r2");
        assertEquals(new And()
                .setOpcode(OpCode.ALU)
                .setR1(1)
                .setR2(2)
                .setFunc(1),
        actual);
    }

    @Test
    public void parseAddImmediate() {
        Instruction instruction = assembler.parse("ADDI, $r1, 123");
        assertEquals(new AddImmediate().setOpcode(OpCode.ADDI).setR1(1).setImmediate(123), instruction);
    }

    @Test
    public void parseJumpImmediate() {
        Instruction instruction = assembler.parse("J, 0x123");
        assertEquals(new JumpImmediate().setOpcode(OpCode.J).setImmediate(0x123<<1), instruction);
    }

    @Test
    public void parseLoadWord() {
        Instruction instruction = assembler.parse("LW, $r1, $r2");
        assertEquals(new LoadWord().setOpcode(OpCode.LW).setR1(1).setR2(2), instruction);
    }

    @Test
    public void parseStoreWord() {
        Instruction instruction = assembler.parse("SW, $r1, $r2");
        assertEquals(new StoreWord().setOpcode(OpCode.SW).setR1(1).setR2(2), instruction);
    }

    @Test
    public void parseOrImmediate() {
        Instruction instruction = assembler.parse("ORI, $r1, 0x123");
        assertEquals(new OrImmediate().setOpcode(OpCode.ORI).setR1(1).setImmediate(0x123), instruction);
    }

    @Test
    public void invalidFormat() {
        Instruction instruction = assembler.parse("AND $r1 $r2");
        assertFalse(instruction.isValid());
    }

    @Test
    public void invalidRegisterFormat() {
        Instruction instruction = assembler.parse("AND, r1, r2");
        assertFalse(instruction.isValid());
    }

    @Test
    public void invalidRegister() {
        Instruction instruction = assembler.parse("AND, $r16, r2");
        assertFalse(instruction.isValid());
    }

    @Test
    public void encodeTwoRegisterType() {
        Instruction instruction = assembler.parse("AND, $r1, $r2");
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
        Instruction expected = assembler.parse("AND, $r1, $r2 -- this is a comment");
        int encoded = Assembler.encode(expected);
        assertEquals(expected, Assembler.decode(encoded));
    }

    @Test
    public void encodeJumpImmediate() {
        Instruction expected = assembler.parse("J, 0x6");
        int encoded = Assembler.encode(expected);
        assertEquals(expected, Assembler.decode(encoded));
    }

    @Test
    public void parse() throws Exception {
        String program = "AND, $r1, $r0\n" +
                         "ADDI, $r1, 123";
        List<Instruction> instructions = assembler.parse(new StringReader(program));
        assertEquals(2, instructions.size());
    }
}
