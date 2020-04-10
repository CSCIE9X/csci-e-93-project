package e93.assembler.ast;

import e93.assembler.Instruction;
import e93.assembler.OpCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AddImmediate extends Instruction {

    /**
     * Some instructions have a value for the first register. This may be rd, rs,
     * or rt depending on your instruction set.
     */
    private int r1;

    private int immediate;

    public AddImmediate() {
        setOpcode(OpCode.ADDI);
    }

    @Override
    public void accept(final AssemblyVisitor assemblyVisitor) {
        assemblyVisitor.visit(this);
    }
}
