package e93.assembler.ast;

import e93.assembler.Instruction;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Asciiz extends Instruction {

    private String value;

    public Asciiz(String value) {
        this.value = value;
    }

    @Override
    public <R> R accept(AssemblyVisitor<R> assemblyVisitor) {
        return assemblyVisitor.visit(this);
    }
}
