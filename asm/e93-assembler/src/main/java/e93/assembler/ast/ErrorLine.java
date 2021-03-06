package e93.assembler.ast;

import e93.assembler.Instruction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class ErrorLine extends Instruction {

    private String errorMessage;

    @Override
    public <R> R accept(AssemblyVisitor<R> assemblyVisitor) {
        return assemblyVisitor.visit(this);
    }
}
