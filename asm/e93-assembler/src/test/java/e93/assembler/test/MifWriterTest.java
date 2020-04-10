package e93.assembler.test;

import e93.assembler.Instruction;
import e93.assembler.InstructionParser;
import e93.assembler.MifWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static e93.assembler.IOUtils.asString;
import static org.junit.Assert.assertEquals;

public class MifWriterTest {
    @Test
    public void simple() throws IOException {
        List<Instruction> instructions = InstructionParser.parse(new StringReader(asString("/sample.asm")));
        String actual = MifWriter.writeToString(instructions);
        String expected = asString("/sample.mif");
        assertEquals(expected, actual);
    }
}
