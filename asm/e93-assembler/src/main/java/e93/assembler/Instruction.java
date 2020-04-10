package e93.assembler;

import e93.assembler.ast.AssemblyVisitor;
import lombok.Data;

/**
 * Simple data structure for an Instruction. All of the instructions in the
 * instruction set are modeled with this class.
 *
 * Note that not all instructions will have all of the fields populated.
 *
 * Also note that this assumes a two register instruction format only. If you have
 * a three register format then you should expand this.
 *
 *
 * @author markford
 */
@Data
public abstract class Instruction {

    /**
     * The OpCode for the instructions. All instructions have OpCodes.
     */
    private OpCode opcode;

    /**
     * Some instructions have a function code. This is a good technique to pack
     * more behavior into an instruction set. For example, all of the ALU related
     * instructions could have the same opcode value but then use a few bits
     * in the instruction to differentiate between them.
     */
    private int func;

    /**
     * Some instructions have a label. This is a reference to some other instruction
     * that they're jumping or branching to.
     *
     * In cases where the assembly programmer used a label in their instruction,
     * the Assembler is responsible for replacing this label reference with the
     * correct immediate. The way this is done is outlined in the principles of
     * operation document for your instruction set.
     *
     * For example, when handling a branch instruction, the assembler may take
     * address of the label and compute the difference between it and PC + 2.
     * When handling a jump instruction, it may take the address of the label and
     * compute a value for the lower 12 bits that can be OR'd in with the high
     * order bits of PC + 2.
     */
    private String label;

    private int lineNumber;

    private String sourceLine;

    public boolean isValid() {
        return opcode != null;
    }

    public abstract <R> R accept(AssemblyVisitor<R> assemblyVisitor);
}
