package e93.assembler.ast;

public interface AssemblyVisitor<R> {
    default R visit(And and) {
        return null;
    }
    default R visit(AddImmediate andi) {
        return null;
    }
    default R visit(JumpImmediate jumpImmediate) {
        return null;
    }
    default R visit(LoadWord loadWord) {
        return null;
    }
    default R visit(OrImmediate orImmediate) {
        return null;
    }
    default R visit(StoreWord storeWord) {
        return null;
    }
    default R visit(Asciiz asciiz) {
        return null;
    }
    default R visit(ErrorLine errorLine) {
        throw new IllegalStateException("should detect errors before writing mif");
    }
}
