package engine.opcodes;

import java.util.Map;
import java.util.Stack;

import engine.StackFrame;
import engine.heap.Heap;
import types.Value;

/**
 * Loads a local variable onto the stack.
 */
public class LoadLocalOp extends Opcode {
    private String varName;

    public LoadLocalOp(String varName) {
        this.varName = varName;
    }

    @Override
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        Map<String, Value> localVars = callStack.peek().getLocalVars();
        opStack.push(localVars.get(varName));
    }

    @Override
    public Opcode convertToStore() {
        return new StoreLocalOp(varName);
    }

    @Override
    public String toString() {
        return "load_local " + varName;
    }
}
