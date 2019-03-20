package e93.assembler.test;

import e93.assembler.Instruction;
import e93.assembler.MifWriter;
import e93.assembler.ast.And;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static e93.assembler.IOUtils.asString;
import static org.junit.Assert.assertEquals;

public class MifWriterTest {
    @Test
    public void simple() throws IOException {
        Instruction instruction = new And().setR1(1).setR2(2);
        String actual = MifWriter.writeToString(Collections.singletonList(instruction));
        String expected = asString("/sample.mif");
        assertEquals(expected, actual);

    }
}
