package e93.assembler.test;

import e93.assembler.Instruction;
import e93.assembler.InstructionParser;
import e93.assembler.ast.AddImmediate;
import e93.assembler.ast.And;
import e93.assembler.ast.JumpImmediate;
import e93.assembler.ast.LoadWord;
import e93.assembler.ast.OrImmediate;
import e93.assembler.ast.StoreWord;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class InstructionParserTest {
    @Test
    public void parseTwoRegisterType() {
        Instruction actual = InstructionParser.parse("AND $r1, $r2");
        assertEquals(new And()
                        .setR1(1)
                        .setR2(2)
                        .setFunc(1),
                actual);
    }

    @Test
    public void parseAddImmediate() {
        Instruction instruction = InstructionParser.parse("ADDI $r1, 0x123");
        assertEquals(new AddImmediate().setR1(1).setImmediate(0x123), instruction);
    }

    @Test
    public void parseJumpImmediate() {
        Instruction instruction = InstructionParser.parse("J 0x123");
        assertEquals(new JumpImmediate().setImmediate(0x123<<1), instruction);
    }

    @Test
    public void parseLoadWord() {
        Instruction instruction = InstructionParser.parse("LW $r1, $r2");
        assertEquals(new LoadWord().setR1(1).setR2(2), instruction);
    }

    @Test
    public void parseStoreWord() {
        Instruction instruction = InstructionParser.parse("SW $r1, $r2");
        assertEquals(new StoreWord().setR1(1).setR2(2), instruction);
    }

    @Test
    public void parseOrImmediate() {
        Instruction instruction = InstructionParser.parse("ORI $r1, 0x123");
        assertEquals(new OrImmediate().setR1(1).setImmediate(0x123), instruction);
    }

    @Test
    public void invalidRegisterFormat() {
        Instruction instruction = InstructionParser.parse("AND r1, r2");
        assertFalse(instruction.isValid());
    }

    @Test
    public void invalidRegister() {
        Instruction instruction = InstructionParser.parse("AND $r16, r2");
        assertFalse(instruction.isValid());
    }
}
