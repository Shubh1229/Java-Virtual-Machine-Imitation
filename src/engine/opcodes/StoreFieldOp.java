package engine.opcodes;

import java.util.Stack;

import engine.StackFrame;
import engine.exceptions.MissingFieldException;
import engine.exceptions.TypeMismatchException;
import engine.heap.Heap;
import engine.heap.HeapObject;
import types.Clazz;
import types.Field;
import types.Value;

/**
 * Store a value from the stack into a field.
 */
public class StoreFieldOp extends Opcode {
    private String fieldName;

    public StoreFieldOp(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        Value objId = opStack.pop();
        if (!objId.isObject()) {
            throw new TypeMismatchException("Field access on non-object type: " + objId.getType());
        }
        HeapObject object = (HeapObject) heap.getEntry(objId.getIntValue());

        Clazz clazz = object.getClazz();
        Field field = clazz.getField(fieldName);
        if (field == null) {
            throw new MissingFieldException("class " + clazz.getName() + " does not have a field called " + fieldName);
        }

        Value value = opStack.pop();
        if (value.getIntValue() != 0 && !field.getType().equals(value.getType())) {
            throw new TypeMismatchException(
                clazz.getName() + "." + fieldName + " is " + field.getType() + ", value is " + value.getType());
        }

        object.setFieldValue(fieldName, value);
    }

    @Override
    public String toString() {
        return "store_field " + fieldName;
    }

}
