package engine.opcodes;

import java.util.Stack;

import engine.StackFrame;
import engine.heap.Heap;
import engine.heap.HeapArray;
import types.DataType;
import types.Value;

/**
 * Creates an array object, and puts its id on the stack.
 */
public class NewArrayOp extends Opcode {
    private String type;
    private int numDimensions;

    public NewArrayOp(String type, int numDimensions) {
        this.type = type;
        this.numDimensions = numDimensions;
    }

    @Override
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        int[] dims = new int[numDimensions];
        for (int i = numDimensions - 1; i >= 0; i--) {
            dims[i] = opStack.pop().getIntValue();
        }

        DataType arrType = new DataType(type, dims.length);
        HeapArray arr = heap.createArray(arrType, dims);
        opStack.push(new Value(arr));
    }

    @Override
    public String toString() {
        return "new_array " + type + " " + numDimensions;
    }

}
