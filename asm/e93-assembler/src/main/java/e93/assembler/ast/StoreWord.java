package e93.assembler.ast;

import e93.assembler.Instruction;
import e93.assembler.OpCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class StoreWord extends Instruction {

    /**
     * Some instructions have a value for the first register. This may be rd, rs,
     * or rt depending on your instruction set.
     */
    private int r1;

    /**
     * Some instructions have a value for the second register. This may be rd, rs,
     * or rt depending on your instruction set.
     */
    private int r2;

    public StoreWord() {
        setOpcode(OpCode.SW);
    }

    @Override
    public <R> R accept(final AssemblyVisitor<R> assemblyVisitor) {
        return assemblyVisitor.visit(this);
    }
}
