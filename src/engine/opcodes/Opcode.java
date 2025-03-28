package engine.opcodes;

import java.util.Stack;

import engine.StackFrame;
import engine.VMThreadState;
import engine.VMThreadState.State;
import engine.heap.Heap;
import types.Value;

public abstract class Opcode {
    /**
     * Executes the opcode.
     *
     * @param callStack the call stack
     * @param opStack   the op stack
     */
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        throw new RuntimeException("Either override this or override executeWithState");
    }

    /**
     * Executes the opcode, and returns the state of the thread.
     *
     * @param callStack the call stack
     * @param heap      the heap
     * @param opStack   the op stack
     * @return the state of the thread
     */
    public VMThreadState executeWithState(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        execute(callStack, heap, opStack);
        return new VMThreadState(State.Running, 0);
    }

    /**
     * Converts this load_ opcode into an equivalent store_ operation. Not supported
     * for most opcodes.
     *
     * @return the store op
     */
    public Opcode convertToStore() {
        throw new RuntimeException(this.getClass().getSimpleName() + " can't be converted to a store opcode");
    }
}
