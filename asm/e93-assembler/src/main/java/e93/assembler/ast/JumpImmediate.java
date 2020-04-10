package e93.assembler.ast;

import e93.assembler.Instruction;
import e93.assembler.OpCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JumpImmediate extends Instruction {

    private int immediate;

    public JumpImmediate() {
        setOpcode(OpCode.J);
    }

    @Override
    public void accept(final AssemblyVisitor assemblyVisitor) {
        assemblyVisitor.visit(this);
    }
}
