package engine.opcodes;

import java.util.Stack;

import engine.StackFrame;
import engine.heap.Heap;
import types.Value;

/**
 * Possibly branches to another location in the code.
 */
public class BranchOp extends Opcode {
    public enum Type {
        UNCONDITIONAL,
        TRUE,
        FALSE
    }

    private Type type;
    private int dest;

    public BranchOp(Type type, int dest) {
        this.type = type;
        this.dest = dest;
    }

    @Override
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        if (type == Type.UNCONDITIONAL ||
        // Only one of these pops will happen, because of short-circuiting.
            (type == Type.TRUE && opStack.pop().getIntValue() == 1) ||
            (type == Type.FALSE && opStack.pop().getIntValue() != 1)) {
            callStack.peek().jumpTo(dest);
        }
    }

    @Override
    public String toString() {
        String branchInstruction;
        switch (type) {
        case UNCONDITIONAL -> branchInstruction = "branch";
        case TRUE -> branchInstruction = "branchT";
        case FALSE -> branchInstruction = "branchF";
        default -> throw new RuntimeException("Unknown branch type: " + type);
        }
        return branchInstruction + " " + dest;
    }
}
