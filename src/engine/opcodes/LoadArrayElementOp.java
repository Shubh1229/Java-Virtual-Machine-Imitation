package engine.opcodes;

import java.util.Stack;

import engine.StackFrame;
import engine.exceptions.TypeMismatchException;
import engine.heap.Heap;
import engine.heap.HeapArray;
import types.DataType;
import types.Value;

/**
 * Loads an array element onto the stack.
 */
public class LoadArrayElementOp extends Opcode {

    @Override
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        Value val = opStack.pop();
        if (val.getType().getNumDims() == 0) {
            throw new TypeMismatchException("Indexing into non-array: " + val.getType());
        }

        HeapArray arr = (HeapArray) heap.getEntry(val.getIntValue());
        Value index = opStack.pop();
        if (!index.getType().equals(DataType.INT)) {
            throw new TypeMismatchException("Array index must be an int, got: " + index.getType());
        }
        opStack.push(arr.getAt(index.getIntValue()));
    }

    @Override
    public Opcode convertToStore() {
        return new StoreArrayElementOp();
    }

    @Override
    public String toString() {
        return "load_array_element";
    }

}
