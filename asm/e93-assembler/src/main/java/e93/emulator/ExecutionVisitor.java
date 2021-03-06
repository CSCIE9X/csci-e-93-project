package e93.emulator;

import e93.assembler.ast.AddImmediate;
import e93.assembler.ast.And;
import e93.assembler.ast.Asciiz;
import e93.assembler.ast.AssemblyVisitor;
import e93.assembler.ast.JumpImmediate;
import e93.assembler.ast.LoadWord;
import e93.assembler.ast.OrImmediate;
import e93.assembler.ast.StoreWord;
import lombok.Getter;
import lombok.Setter;

public class ExecutionVisitor implements AssemblyVisitor<Integer> {

    @Getter @Setter
    private int pc = 0;
    private MemorySubsystem memorySubsystem;
    private int[] registers;

    public ExecutionVisitor(MemorySubsystem memorySubsystem, int[] registers) {
        this.memorySubsystem = memorySubsystem;
        this.registers = registers;
    }

    @Override
    public Integer visit(final And and) {
        int op1 = registers[and.getR1()];
        int op2 = registers[and.getR2()];
        int result = op1 & op2;
        registers[and.getR1()] = result;
        incrementPc();
        return getPc();
    }

    @Override
    public Integer visit(final AddImmediate andi) {
        int op1 = registers[andi.getR1()];
        int immediate = andi.getImmediate();
        int result = op1 + immediate;
        registers[andi.getR1()] = result;
        incrementPc();
        return getPc();
    }

    @Override
    public Integer visit(final JumpImmediate jumpImmediate) {
        incrementPc();
        int high7bits = pc & (0xff<<9);
        // immediate has already been shifted
        int low9bits = jumpImmediate.getImmediate();
        pc = high7bits | low9bits;
        return getPc();
    }

    @Override
    public Integer visit(final LoadWord loadWord) {
        int address = registers[loadWord.getR2()];
        int value = memorySubsystem.readInt(address);
        registers[loadWord.getR1()] = value;
        incrementPc();
        return getPc();
    }

    @Override
    public Integer visit(final OrImmediate ori) {
        int op1 = registers[ori.getR1()];
        int immediate = ori.getImmediate();
        int result = op1 | immediate;
        registers[ori.getR1()] = result;
        incrementPc();
        return getPc();
    }

    @Override
    public Integer visit(final StoreWord storeWord) {
        int address = registers[storeWord.getR2()];
        int value = registers[storeWord.getR1()];
        memorySubsystem.writeInt(address, value);
        incrementPc();
        return getPc();
    }

    @Override
    public Integer visit(Asciiz asciiz) {
        throw new IllegalStateException("if you're executing a directive, something is wrong");
    }

    private void incrementPc() {
        pc += 2;
    }

    public int[] getRegisters() {
        return registers.clone();
    }
}
