package engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import engine.VMThreadState.State;
import engine.heap.Heap;
import engine.opcodes.Opcode;
import types.Method;
import types.Value;

/**
 * A thread of execution in the VM.
 */
public class VMThread {
    /** The method that will be executed when the thread starts */
    private Method entryPoint;
    /** The local variables for the entry point */
    private Map<String, Value> entryPointLocals;
    /** The thread's priority */
    private int priority;
    private Stack<StackFrame> callStack;
    private Stack<Value> opStack;
    private Heap heap;

    public VMThread(String className, String entryPointName, Heap heap) {
        this.heap = heap;
        entryPoint = CompiledClassCache.instance().resolveMethod(className, entryPointName);
        entryPointLocals = new HashMap<>();
        callStack = new Stack<>();
        callStack.push(new StackFrame(entryPoint.getOpcodes(), entryPointLocals));
        opStack = new Stack<>();
    }

    public VMThread(String className, String entryPointName, int priority, Heap heap) {
        this(className, entryPointName, heap);
        this.priority = priority;
    }

    /**
     * Runs one instruction, and returns the thread's state.
     *
     * @return the thread's state
     */
    public VMThreadState runInstruction() {
        StackFrame frame = callStack.peek();

        Opcode op = frame.getNextOpcode();
        if (op == null) {
            throw new RuntimeException("No instructions left to run");
        }

        int pc = frame.getProgramCounter();
        VMThreadState state = op.executeWithState(callStack, heap, opStack);
        // If the op wasn't a branch that updated pc, go to the next instruction.
        if (frame.getProgramCounter() == pc) {
            frame.advanceProgramCounter();
        }

        if (callStack.isEmpty() || callStack.peek().isComplete() && callStack.size() == 1) {
            return new VMThreadState(State.Complete, 0);
        }
        return state;
    }

    /**
     * Runs the thread, and returns the local variables from the entry point.
     * 
     * @return the locals from the entry point
     */
    public Map<String, Value> run() {
        VMThreadState state;
        do {
            state = runInstruction();
        } while (state.getState() != State.Complete);

        return entryPointLocals;
    }

    /**
     * Returns the locals from the entry point.
     * 
     * @return the locals from the entry point
     */
    public Map<String, Value> getEntryPointLocals() {
        return entryPointLocals;
    }

    public int getPriority() {
        return priority;
    }
}
