package engine.opcodes;

import java.util.Map;
import java.util.Stack;

import engine.StackFrame;
import engine.heap.Heap;
import types.Value;

/**
 * Stores a value from the stack into a local variable.
 */
public class StoreLocalOp extends Opcode {
    private String varName;


    public StoreLocalOp(String varName) {
        this.varName = varName;
    }

    @Override
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        Map<String, Value> localVars = callStack.peek().getLocalVars();
        localVars.put(varName, opStack.pop());
    }

    @Override
    public String toString() {
        return "store_local " + varName;
    }
}
