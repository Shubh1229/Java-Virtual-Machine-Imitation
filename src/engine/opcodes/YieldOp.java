package engine.opcodes;

import java.util.Stack;

import engine.StackFrame;
import engine.VMThreadState;
import engine.VMThreadState.State;
import engine.heap.Heap;
import types.Value;

/**
 * Causes the current thread to give up its turn.
 */
public class YieldOp extends Opcode {

    @Override
    public VMThreadState executeWithState(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        return new VMThreadState(State.Yielded, 0);
    }

}
