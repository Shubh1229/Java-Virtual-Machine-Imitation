package engine;

/**
 * Represents the current state of a thread.
 */
public class VMThreadState {
    public enum State {
        /** Thread is running. */
        Running,
        /** Thread has yielded the processor. */
        Yielded,
        /** Thread is sleeping. */
        Sleeping,
        /** Thread has completed. */
        Complete
    }

    private State state;
    private int ticksToSleep;

    public State getState() {
        return state;
    }

    /**
     * Returns the number of ticks to sleep. Only valid if state is Sleeping.
     *
     * @return the number of ticks to sleep
     */
    public int getTicksToSleep() {
        return ticksToSleep;
    }

    /**
     * Creates a VMThreadState.
     *
     * @param curState the state
     * @param ticks    the # of ticks to sleep - only populated if state is Sleeping
     */
    public VMThreadState(State curState, int ticks) {
        this.state = curState;
        this.ticksToSleep = ticks;
    }
}
