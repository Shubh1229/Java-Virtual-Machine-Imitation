package engine.opcodes;

import java.util.Stack;

import engine.StackFrame;
import engine.exceptions.TypeMismatchException;
import engine.heap.Heap;
import engine.heap.HeapEntry;
import types.Value;

/**
 * Loads an field onto the stack.
 */
public class LoadFieldOp extends Opcode {
    private String fieldName;

    public LoadFieldOp(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public Opcode convertToStore() {
        return new StoreFieldOp(fieldName);
    }

    @Override
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        Value objId = opStack.pop();
        if (!objId.isObject()) {
            throw new TypeMismatchException("Field access on non-object type: " + objId.getType());
        }
        HeapEntry object = (HeapEntry) heap.getEntry(objId.getIntValue());
        opStack.push(object.getFieldValue(fieldName));
    }

    @Override
    public String toString() {
        return "load_field " + fieldName;
    }

}
