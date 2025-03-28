package engine.opcodes;

import java.util.Stack;

import engine.StackFrame;
import engine.exceptions.TypeMismatchException;
import engine.heap.Heap;
import engine.heap.HeapArray;
import types.DataType;
import types.Value;

/**
 * Stores a value from the stack into an array element.
 */
public class StoreArrayElementOp extends Opcode {

    @Override
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        Value arrayId = opStack.pop();
        if (arrayId.getType().getNumDims() == 0) {
            throw new TypeMismatchException("Indexing into non-array: " + arrayId.getType());
        }
        HeapArray arr = (HeapArray) heap.getEntry(arrayId.getIntValue());

        Value index = opStack.pop();
        if (!index.getType().equals(DataType.INT)) {
            throw new TypeMismatchException("Array index must be an int, got: " + index.getType());
        }

        Value value = opStack.pop();
        if (!value.getType().equals(arr.getType().getElementType())) {
            throw new TypeMismatchException("Assigning wrong type to array element. Expected "
                + arr.getType().getElementType() + ", got " + value.getType());
        }

        arr.setAt(index.getIntValue(), value);
    }

    @Override
    public String toString() {
        return "store_array_element";
    }

}
