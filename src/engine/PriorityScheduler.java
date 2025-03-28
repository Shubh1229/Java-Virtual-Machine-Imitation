package engine;

import java.util.List;
import java.util.PriorityQueue;

/**
 * Scheduler that runs the highest priority threads first.
 */
public class PriorityScheduler {

    /** Ready threads, with highest priority at the front. */
    protected PriorityQueue<VMThread> threads;
    /** Sleeping threads, with earliest to wake at the front. */
    protected PriorityQueue<SleepInfo> sleepingThreads;
    /** Current time. */
    private int currentTime;

    /**
     * Creates a CoopScheduler.
     *
     * @param list the list of threads to be executed
     */
    public PriorityScheduler(List<VMThread> list) {
        currentTime = 0;
        threads = new PriorityQueue<VMThread>(10,
            (o1, o2) -> o2.getPriority() - o1.getPriority());

        threads.addAll(list);
        sleepingThreads = new PriorityQueue<SleepInfo>();
    }

    /**
     * Returns the current thread.
     *
     * @return the current thread
     */
    public VMThread getCurrentThread() {
        return threads.peek();
    }

    /**
     * Executes the given number of instructions.
     *
     * @param numInstructions the number of instructions
     */
    public void run(int numInstructions) {
        for (int i = 0; i < numInstructions; i++) {
            currentTime++;
            tick();
            wakeThreads();
        }
    }

    /**
     * Executes one instruction.
     */
    private void tick() {
        if (threads.isEmpty()) {
            return;
        }

        VMThread currentThread = threads.peek();
        VMThreadState state = currentThread.runInstruction();

        switch (state.getState()) {
        case Running -> {
            // Do nothing.
        }
        case Yielded -> {
            // Give another thread a chance to run (only if it's of equal priority).
            threads.add(threads.remove());
        }
        case Sleeping -> {
            int wakeTime = currentTime + state.getTicksToSleep();
            sleepingThreads.add(new SleepInfo(threads.remove(), wakeTime));
        }
        case Complete -> threads.remove();
        }
    }

    /**
     * Find threads that are ready to wake up.
     */
    public void wakeThreads() {
        while (!sleepingThreads.isEmpty() &&
            sleepingThreads.peek().timeToWake <= currentTime) {
            threads.add(sleepingThreads.remove().thread);
        }
    }

    /**
     * Remembers how long a thread needs to sleep.
     */
    private static class SleepInfo implements Comparable<SleepInfo> {
        /** The thread. */
        private VMThread thread;
        /** Time the thread should wake. */
        private int timeToWake;

        /**
         * Creates a SleepInfo.
         *
         * @param athread     the thread
         * @param atimeToWake when the thread should wake
         */
        public SleepInfo(VMThread athread, int atimeToWake) {
            thread = athread;
            timeToWake = atimeToWake;
        }

        /**
         * Comparable implementation.
         */
        @Override
        public int compareTo(SleepInfo o) {
            return timeToWake - o.timeToWake;
        }
    }

}
