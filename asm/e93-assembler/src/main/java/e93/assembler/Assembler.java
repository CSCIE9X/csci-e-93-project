package e93.assembler;

import e93.assembler.ast.AddImmediate;
import e93.assembler.ast.And;
import e93.assembler.ast.ErrorLine;
import e93.assembler.ast.JumpImmediate;
import e93.assembler.ast.LoadWord;
import e93.assembler.ast.OrImmediate;
import e93.assembler.ast.StoreWord;

import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * A starting point for an assembler. Very bare bones.
 *
 * @author markford
 */
public class Assembler {

    /**
     * A mask used to extract the register value from an encoded instruction.
     * This should be enough bits to mask all possible values of the register.
     */
    private static final int REGISTER_MASK = 0xf;

    private static final int IMMEDIATE_MASK = 0xff;

    /**
     * Encodes an instruction in order to write it to a MIF file or similar.
     *
     * At this point, your assembler should have resolved any labels to numeric
     * values in order to encode the instruction as all numbers.
     *
     * @param instruction an instruction that's had its label resolved (assuming it had one)
     * @return integer version of the instruction
     */
    public static int encode(Instruction instruction) {

        if (instruction instanceof ErrorLine) {
            throw new RuntimeException("[" + ((ErrorLine) instruction).getErrorMessage() + "]");
        }

        assert instruction.getOpcode() != null;

        switch(instruction.getOpcode()) {
            case ALU:
                if (instruction.getFunc() == ALUFunctionCodes.AND) {
                    And and = (And) instruction;
                    return
                            // opcode is bits 15..12
                            and.getOpcode().getValue() << 12 |
                                    // r1 is bits 11..8
                                    and.getR1() << 8 |
                                    // r2 is bits 7..4
                                    and.getR2() << 4 |
                                    // func is bits 3..0
                                    instruction.getFunc()
                            ;
                } else {
                    throw new IllegalStateException("Unexpected value: " + instruction.getFunc());
                }
            case SW: {
                StoreWord lw = (StoreWord) instruction;
                return lw.getOpcode().getValue() << 12 |
                        lw.getR1() << 8 |
                        lw.getR2() << 4;
            }
            case LW: {
                LoadWord sw = (LoadWord) instruction;
                return sw.getOpcode().getValue()<<12 |
                        sw.getR1() << 8 |
                        sw.getR2() << 4;
            }
            case ORI: {
                OrImmediate ori = (OrImmediate) instruction;
                return ori.getOpcode().getValue()<<12 |
                        ori.getR1() << 8 |
                        ori.getImmediate();

            }
            case ADDI: {
                AddImmediate addImmediate = (AddImmediate) instruction;
                return addImmediate.getOpcode().getValue()<<12 |
                        addImmediate.getR1() << 8 |
                        addImmediate.getImmediate();
            }
            case J: {
                JumpImmediate jumpImmediate = (JumpImmediate) instruction;
                return jumpImmediate.getOpcode().getValue()<<12 |
                        jumpImmediate.getImmediate()>>1;
            }
        }
        throw new IllegalArgumentException("unhandled instruction:" + instruction);
    }

    /**
     * Decodes an instruction from its encoded form. You'll need something like
     * this for when you write the emulator.
     *
     * @param encoded numeric form of the instruction that we'll decode
     * @return Instruction
     * @throws IllegalArgumentException if we don't know how to decode it
     */
    public static Instruction decode(int encoded) {
        // the opcode is always in 15..12 but the rest of the instruction is
        // unknown until we know what the opcode is so get that first!
        int value = encoded >> 12;
        OpCode opCode = OpCode.fromEncoded(value);
        switch (opCode) {
            case ALU:
                // get the function code to figure out what type of ALU operation
                // it is. The function code is the lower two bits which I can get
                // by AND'ing the number 3 (which is 11 in binary)
                int functionCode = encoded & 0x3;
                if (functionCode == ALUFunctionCodes.AND) {// r1 is always in 11..8
                    // shift right to drop the low order bits and then mask with
                    // REGISTER_MASK in order to get all of the
                    // bits for the register number
                    int r1 = (encoded >> 8) & REGISTER_MASK;
                    // r2 is always in 7..4
                    // shift right to drop the low order bits and then mask with
                    // REGISTER_MASK in order to get all of the
                    // bits for the register number
                    int r2 = (encoded >> 4) & REGISTER_MASK;
                    return new And()
                            .setR1(r1)
                            .setR2(r2);
                }
                throw new IllegalStateException("Unexpected value: " + functionCode);

            case SW:
                return new StoreWord()
                        .setR1((encoded >> 8) & REGISTER_MASK)
                        .setR2((encoded >> 4) & REGISTER_MASK);
            case LW:
                return new LoadWord()
                        .setR1((encoded >> 8) & REGISTER_MASK)
                        .setR2((encoded >> 4) & REGISTER_MASK);
            case ORI:
                return new OrImmediate()
                        .setR1((encoded >> 8) & REGISTER_MASK)
                        .setImmediate(encoded & IMMEDIATE_MASK);
            case ADDI:
                return new AddImmediate()
                        .setR1((encoded >> 8) & REGISTER_MASK)
                        .setImmediate(encoded & IMMEDIATE_MASK);
            case J:
                return new JumpImmediate()
                        .setImmediate((encoded & 0xfff)<<1);

        }
        throw new IllegalArgumentException("unhandled encoded instruction:" + encoded);
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            System.err.println("must pass name of input file");
            return;
        }

        File file = new File(args[0]);
        if (!file.isFile()) {
            System.err.println("file not found or not readable:" + args[0]);
            return;
        }

        try (FileReader fileReader = new FileReader(file)) {
            List<Instruction> instructions = InstructionParser.parse(fileReader);
            System.out.println(MifWriter.writeToString(instructions));
        }
    }
}
