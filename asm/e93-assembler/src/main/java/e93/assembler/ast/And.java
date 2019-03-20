package e93.assembler.ast;

import e93.assembler.ALUFunctionCodes;
import e93.assembler.Instruction;
import e93.assembler.OpCode;

public class And extends Instruction {

    public And() {
        setOpcode(OpCode.ALU);
        setFunc(ALUFunctionCodes.AND);
    }

    @Override
    public void accept(final AssemblyVisitor assemblyVisitor) {
        assemblyVisitor.visit(this);
    }
}
