package engine.opcodes;

import java.util.Stack;

import engine.StackFrame;
import engine.heap.Heap;
import types.Value;

/**
 * Returns from a function call.
 */
public class ReturnOp extends Opcode {

    @Override
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        callStack.pop();
    }

    @Override
    public String toString() {
        return "return";
    }
}
