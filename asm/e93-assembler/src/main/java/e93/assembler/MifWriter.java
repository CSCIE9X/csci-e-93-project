package e93.assembler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static e93.assembler.IOUtils.readAllIntoString;

public class MifWriter {
    public static String writeToString(List<Instruction> instructions) throws IOException {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            write(instructions, pw);
        }
        return sw.toString();
    }

    public static void write(List<Instruction> instructions, PrintWriter pw) throws IOException {
        InputStream template = IOUtils.asStream("/mif-16bit-template.txt");
        String templateStr = readAllIntoString(template);

        AtomicInteger address = new AtomicInteger(0);

        String allInstructions = instructions
                .stream()
                .map(instruction -> instruction.setAddress(address.getAndIncrement()))
                .map(instruction -> String.format("%04x : %04x;%s",
                        instruction.getAddress(),
                        Assembler.encode(instruction),
                        Optional.ofNullable(instruction.getSourceLine()).map(line -> String.format(" -- %s", line)).orElse("")))
                .collect(Collectors.joining("\n"));

        pw.print(String.format(templateStr, allInstructions));
    }
}
