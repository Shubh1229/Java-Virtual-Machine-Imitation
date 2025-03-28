package engine.opcodes;

import java.util.Stack;

import engine.StackFrame;
import engine.VMThreadState;
import engine.VMThreadState.State;
import engine.heap.Heap;
import types.Value;

/**
 * Causes the current thread to sleep for a certain number of ticks.
 */
public class SleepOp extends Opcode {
    private int ticksToSleep;

    public SleepOp(int ticksToSleep) {
        this.ticksToSleep = ticksToSleep;
    }

    @Override
    public VMThreadState executeWithState(Stack<StackFrame> callStack, Heap heap, Stack<Value> opStack) {
        return new VMThreadState(State.Sleeping, ticksToSleep);
    }

}
