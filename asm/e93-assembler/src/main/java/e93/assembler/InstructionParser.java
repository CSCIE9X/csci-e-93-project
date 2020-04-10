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

        int getRegister(int group) {
            String s = getMatcher().group(group);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new InvalidRegisterException(s);
            }
        }

        int getImmediate(int group) {
            String s = getMatcher().group(group);
            if (s.startsWith("0x")) {
                return Integer.parseInt(s.substring(2), 16);
            } else {
                return Integer.parseInt(s);
            }
        }

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
        if (rawLine.contains("--")) {
            String[] lineAndComment = rawLine.split("--");
            line = lineAndComment[0];
        } else {
            line = rawLine;
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
                            return new And()
                                    .setR1(sourceMatch.getRegister(1))
                                    .setR2(sourceMatch.getRegister(2));
                        } else if (sourceMatch.getSourcePattern() == Instructions.ADDI) {
                            return new AddImmediate()
                                    .setR1(sourceMatch.getRegister(1))
                                    .setImmediate(sourceMatch.getImmediate(2));
                        } else if (sourceMatch.getSourcePattern() == Instructions.SW) {
                            return new StoreWord()
                                    .setR1(sourceMatch.getRegister(1))
                                    .setR2(sourceMatch.getRegister(2));
                        } else if (sourceMatch.getSourcePattern() == Instructions.LW) {
                            return new LoadWord()
                                    .setR1(sourceMatch.getRegister(1))
                                    .setR2(sourceMatch.getRegister(2));
                        } else if (sourceMatch.getSourcePattern() == Instructions.JUMP) {
                            return new JumpImmediate()
                                    .setImmediate(sourceMatch.getImmediate(1)<<1);
                        } else if (sourceMatch.getSourcePattern() == Instructions.ORI) {
                            return new OrImmediate()
                                    .setR1(sourceMatch.getRegister(1))
                                    .setImmediate(sourceMatch.getImmediate(2));
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
}
