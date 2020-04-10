package e93.assembler;

import e93.assembler.ast.AddImmediate;
import e93.assembler.ast.And;
import e93.assembler.ast.Asciiz;
import e93.assembler.ast.ErrorLine;
import e93.assembler.ast.JumpImmediate;
import e93.assembler.ast.LoadWord;
import e93.assembler.ast.OrImmediate;
import e93.assembler.ast.StoreWord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class InstructionParser {
    interface SourcePattern {
        Pattern getPattern();

        default Optional<SourceMatch> matches(String line) {
            Matcher matcher = getPattern().matcher(line);
            if (matcher.matches()) {
                return Optional.of(new SourceMatch(this, matcher));
            } else {
                return Optional.empty();
            }
        }
    }

    @AllArgsConstructor
    @Getter
    enum Directives implements SourcePattern {
        ASCIIZ(".asciiz", Pattern.compile("\\.asciiz *\"([^\"]+)\""));
        private final String name;
        private final Pattern pattern;
    }

    @Value
    @AllArgsConstructor
    static class SourceMatch {
        SourcePattern sourcePattern;
        Matcher matcher;
    }

    @AllArgsConstructor
    @Getter
    enum Instructions implements SourcePattern {
        AND("AND", Pattern.compile("AND \\$r([0-9]+), \\$r([0-9]+)\\s*")),
        ADDI("ADDI", Pattern.compile("ADDI \\$r([0-9]+), (0x[0-9a-f]+)\\s*")),
        SW("SW", Pattern.compile("SW \\$r([0-9]+), \\$r([0-9]+)\\s*")),
        LW("LW", Pattern.compile("LW \\$r([0-9]+), \\$r([0-9]+)\\s*")),
        JUMP("J", Pattern.compile("J (0x[0-9a-f]+)\\s*")),
        ORI("ORI", Pattern.compile("ORI \\$r([0-9]+), (0x[0-9a-f]+)\\s*")),
        ;
        private final String name;
        private final Pattern pattern;
    }

    /**
     * Parses the source into a list of Instructions.
     *
     * @param reader Source for the program
     * @return list of instructions that are ready to encode
     * @throws IOException when there's an error reading a line
     */
    public static List<Instruction> parse(Reader reader) throws IOException {
        List<Instruction> instructions = new ArrayList<>();
        BufferedReader br = new BufferedReader(reader);
        String line;
        int lineNumber = 0;
        while ((line = br.readLine()) != null) {
            lineNumber++;
            if (line.startsWith("#")) {
                // it's a comment, ignore it
                continue;
            }

            Instruction instruction = parse(line);
            instruction.setLineNumber(lineNumber);
            instruction.setSourceLine(line);
            instructions.add(instruction);
        }

        return instructions;
    }

    /**
     * Parses a line of assembly and returns an instruction that the system will
     * know how to write to memory.
     * <p>
     * At this point, the instruction may refer to a label that needs to be
     * resolved.
     *
     * @param rawLine raw line of assembly to parse into an instruction
     * @return an instruction
     * @throws IllegalArgumentException if the line cannot be parsed.
     */
    public static Instruction parse(String rawLine) {
        String line;
        String comment; // todo could use this somewhere
        if (rawLine.contains("--")) {
            String[] lineAndComment = rawLine.split("--");
            line = lineAndComment[0];
            comment = lineAndComment[1];
        } else {
            line = rawLine;
            comment = null;
        }

        try {

            return Stream.of(Directives.values(), Instructions.values())
                    .flatMap(Arrays::stream)
                    .map(sourceMatcher -> sourceMatcher.matches(line))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(sourceMatch -> {
                        if (sourceMatch.getSourcePattern() == Directives.ASCIIZ) {
                            return new Asciiz(sourceMatch.getMatcher().group(1));
                        } else if (sourceMatch.getSourcePattern() == Instructions.AND) {
                            return new And().setR1(toRegister(sourceMatch.getMatcher().group(1))).setR2(toRegister(sourceMatch.getMatcher().group(2)));
                        } else if (sourceMatch.getSourcePattern() == Instructions.ADDI) {
                            return new AddImmediate().setR1(toRegister(sourceMatch.getMatcher().group(1))).setImmediate(toImmediate(sourceMatch.getMatcher().group(2)));
                        } else if (sourceMatch.getSourcePattern() == Instructions.SW) {
                            return new StoreWord().setR1(toRegister(sourceMatch.getMatcher().group(1))).setR2(toRegister(sourceMatch.getMatcher().group(2)));
                        } else if (sourceMatch.getSourcePattern() == Instructions.LW) {
                            return new LoadWord().setR1(toRegister(sourceMatch.getMatcher().group(1))).setR2(toRegister(sourceMatch.getMatcher().group(2)));
                        } else if (sourceMatch.getSourcePattern() == Instructions.JUMP) {
                            return new JumpImmediate().setImmediate(toImmediate(sourceMatch.getMatcher().group(1))<<1);
                        } else if (sourceMatch.getSourcePattern() == Instructions.ORI) {
                            return new OrImmediate().setR1(toRegister(sourceMatch.getMatcher().group(1))).setImmediate(toImmediate(sourceMatch.getMatcher().group(2)));
                        } else {
                            return new ErrorLine("unhandled instruction:" + sourceMatch.getMatcher().group(0));
                        }
                    })
                    .map(Instruction.class::cast)
                    .findFirst()
                    .orElseGet(() -> new ErrorLine("Unknown instruction: " + line));
        } catch (InvalidRegisterException e) {
            return new ErrorLine("Invalid register " + e.getValue());
        }
    }

    /**
     * Helper method that converts a reference to a register to an int
     *
     * @param s reference to a register in the format we're expecting
     * @return number of the register which is within range
     * @throws IllegalArgumentException if we can't parse it or it's out of range
     */
    private static int toRegister(String s) throws InvalidRegisterException {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new InvalidRegisterException(s);
        }
    }

    /**
     * Helper method that converts an immediate value to an integer
     *
     * @param s string version of a decimal number to use as an immediate
     * @return converted integer
     * @throws NumberFormatException if an unknown format
     */
    private static int toImmediate(String s) {
        // todo - assert the value can be encoded

        // With respect to the encoding assertion, keep in mind that you may only
        // have 8 bits for an immediate value in your instruction (more for a
        // JUMP). You may allow the programmer to use an immediate value outside
        // of this range but that requires your assembler to handle it. For
        // example, the assembler might see a large immediate value and then
        // generate a couple of instructions to handle it. It might use a
        // temporary register and LUI and similar instructions to put the large
        // immediate into the temporary register and then rewrite the instruction
        // to use this temporary register instead of an immediate.
        if (s.startsWith("0x")) {
            return Integer.parseInt(s.substring(2), 16);
        } else {
            return Integer.parseInt(s);
        }
    }
}
