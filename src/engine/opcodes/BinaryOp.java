package engine.opcodes;

import java.util.Set;
import java.util.Stack;

import engine.StackFrame;
import engine.exceptions.TypeMismatchException;
import engine.heap.Heap;
import types.DataType;
import types.Value;

/**
 * Performs an operation with the two values on the top of the stack.
 *
 */
public class BinaryOp extends Opcode {
    /** The only ops that can be performed on reference types */
    private static Set<Operator> allowedForReferences = Set.of(Operator.EQ, Operator.NEQ);

    private Operator op;

    /**
     * Creates a BinaryOp.
     *
     * @param op the operator
     */
    public BinaryOp(Operator op) {
        this.op = op;
    }

    @Override
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        Value val2 = opStack.pop();
        Value val1 = opStack.pop();

        DataType type1 = val1.getType();
        DataType type2 = val2.getType();
        // Don't allow most operators to be used on object types.
        if ((type1.isObject() || type2.isObject()) && !allowedForReferences.contains(op)) {
            throw new TypeMismatchException(op.getSymbol() + " can't be used on objects");
        }

        if (allowedForReferences.contains(op) &&
            ((type1.isObject() && !type2.isObject() && val2.getIntValue() == 0) ||
                (type2.isObject() && !type1.isObject() && val1.getIntValue() == 0))) {
            // this is comparing null to an object, which is allowed.
        } else if ((type1.isObject() ^ type2.isObject()) ||
            (type1.getNumDims() != type2.getNumDims())) {
            // Can't apply an op to an object and non-object, or to arrays with different
            // numbers of dimensions.
            throw new TypeMismatchException(op.getSymbol() + " can't be used on mixed types");
        }

        int op2 = val2.getIntValue();
        int op1 = val1.getIntValue();
        opStack.push(new Value(op.apply(op1, op2)));
    }

    @Override
    public String toString() {
        return op.getInstruction();
    }
}
