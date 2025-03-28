package engine.opcodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ast.model.ParameterDefinition;
import engine.CompiledClassCache;
import engine.StackFrame;
import engine.exceptions.MissingMethodException;
import engine.exceptions.TypeMismatchException;
import engine.heap.Heap;
import engine.heap.HeapObject;
import types.Method;
import types.Value;

/**
 * Calls a method.
 */
public class CallOp extends Opcode {
    private String methodName;

    public CallOp(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public void execute(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        Value objId = opStack.pop();
        if (!objId.isObject()) {
            throw new TypeMismatchException("Calling method on non-object: " + objId);
        }

        // Resolve the object and method.
        HeapObject obj = (HeapObject) heap.getEntry(objId.getIntValue());
        Method method = CompiledClassCache.instance().resolveMethod(obj.getClazz().getName(), methodName);
        if (method == null) {
            throw new MissingMethodException("Method " + obj.getClazz().getName() + "." + methodName + " not found");
        }

        // Create a new locals map, and populate it with the method arguments.
        Map<String, Value> locals = new HashMap<>();
        locals.put("this", new Value(obj));
        List<ParameterDefinition> params = method.getParameters();
        // Parameters are on the stack in reverse order.
        for (int i = params.size() - 1; i >= 0; i--) {
            ParameterDefinition param = params.get(i);
            Value value = opStack.pop();

            if (!value.getType().equals(param.getType())) {
                throw new TypeMismatchException(
                    "Expected " + param.getType() + " for param " + param.getName() + ", but got " + value.getType());
            }

            locals.put(param.getName(), value);
        }

        // Create the new stack frame. pc will default to zero.
        StackFrame newFrame = new StackFrame(method.getOpcodes(), locals);
        callStack.push(newFrame);
    }

    @Override
    public String toString() {
        return "call " + methodName;
    }

}
