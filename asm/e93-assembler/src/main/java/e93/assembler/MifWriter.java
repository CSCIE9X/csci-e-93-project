package e93.assembler;

import e93.assembler.ast.Asciiz;
import e93.assembler.ast.AssemblyVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static e93.assembler.IOUtils.readAllIntoString;

public class MifWriter {

    public static final String MIF_LINE = "%04x : %04x;%s";

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

        AtomicInteger mifIndex = new AtomicInteger(0);

        String allInstructions = instructions
                .stream()
                .map(instruction -> {
                    if (instruction.getOpcode() != null) {
                        // it's an instruction, all of the instructions are handled the same way
                        return Collections.singletonList(String.format(MIF_LINE,
                                mifIndex.getAndIncrement(),
                                Assembler.encode(instruction),
                                Optional.ofNullable(instruction.getSourceLine()).map(line -> String.format(" -- %s", line)).orElse("")));
                    } else {
                        // it's a directive, need to see what kind in order to know how to emit it
                        List<String> output = instruction.accept(new AssemblyVisitor<List<String>>() {
                            @Override
                            public List<String> visit(Asciiz asciiz) {
                                // this version of asciiz only stores one byte per word
                                // you could reduce the size of the program in memory
                                // by packing two bytes per word
                                return Stream.concat(asciiz.getValue().chars().boxed(), Stream.of(0))
                                        .map(ascii -> String.format(MIF_LINE,
                                                mifIndex.getAndIncrement(),
                                                ascii,
                                                asCharacter(ascii)))
                                        .collect(Collectors.toList());
                            }
                        });
                        if (output == null) {
                            throw new IllegalStateException("mif support missing for:" + instruction.getSourceLine());
                        }
                        return output;
                    }
                })
                .flatMap(Collection::stream)
                .collect(Collectors.joining("\n"));

        pw.print(String.format(templateStr, allInstructions));
    }

    private static String asCharacter(Integer ascii) {
        if (ascii == 0) {
            return " -- <null>";
        }
        if (ascii == ' ') {
            return " -- <space>";
        }
        return String.format(" -- %c", ascii);
    }
}
