import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import engine.PriorityScheduler;
import engine.VMThread;
import engine.heap.Heap;
import types.Value;

public class Part05Test extends TestBase {

    /** Test execution. */
    @Test
    public void testSimpleExec() {
        /** #score(6) */
        hintContext = "(prio)";
        TestUtil.compileMethod("Main", "t1", """
            load_const 5
            store_local x
            load_const 3
            load_local x
            add
            store_local y
            """);
        Heap heap = new Heap();
        VMThread t1 = new VMThread("Main", "t1", heap);
        Map<String, Value> t1Symbols = t1.getEntryPointLocals();
        t1Symbols.remove("this");

        List<VMThread> threadList = new ArrayList<VMThread>();
        threadList.add(t1);

        PriorityScheduler scheduler = new PriorityScheduler(threadList);

        assertSame(hint("getCurrentThread returns wrong object"), t1, scheduler.getCurrentThread());
        assertEquals(hint("symbol table should be empty initially"), 0, t1Symbols.size());

        scheduler.run(1);
        assertEquals(hint("symbol table should still be empty"), 0, t1Symbols.size());

        scheduler.run(1);
        assertEquals(hint("symbol table contains wrong number of entries"), 1, t1Symbols.size());
        assertEquals(hint("t1 symbol didn't have expected value"), 5, t1Symbols.get("x").getIntValue());

        scheduler.run(3);
        assertEquals(hint("symbol table contains wrong number of entries"), 1, t1Symbols.size());
        assertSame(hint("getCurrentThread returns wrong object"), t1, scheduler.getCurrentThread());

        scheduler.run(1);
        assertEquals(hint("symbol table contains wrong number of entries"), 2, t1Symbols.size());
        assertEquals(hint("t1 symbol didn't have expected value"), 5, t1Symbols.get("x").getIntValue());
        assertEquals(hint("symbol didn't have expected value"), 8, t1Symbols.get("y").getIntValue());
        assertNull(hint("getCurrentThread should return null"), scheduler.getCurrentThread());
    }

    /** Test completion. */
    @Test
    public void testCompletion() {
        /** #score(6) */
        hintContext = "(prio)";

        TestUtil.compileMethod("Main", "t1", """
            load_const 5
            store_local x
            load_const 7
            store_local y
            """);
        Heap heap = new Heap();
        VMThread t1 = new VMThread("Main", "t1", heap);
        Map<String, Value> t1Symbols = t1.getEntryPointLocals();
        t1Symbols.remove("this");

        TestUtil.compileMethod("Main", "t2", """
            load_const 6
            store_local z
            """);
        VMThread t2 = new VMThread("Main", "t2", heap);
        Map<String, Value> t2Symbols = t2.getEntryPointLocals();
        t2Symbols.remove("this");

        List<VMThread> threadList = new ArrayList<VMThread>();
        threadList.add(t1);
        threadList.add(t2);

        PriorityScheduler scheduler = new PriorityScheduler(threadList);

        assertSame(hint("getCurrentThread returns wrong object"), t1, scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table should be empty initially"), 0, t1Symbols.size());

        scheduler.run(3);
        assertSame(hint("current thread changed prematurely"), t1, scheduler.getCurrentThread());

        scheduler.run(1);
        assertEquals(hint("t1 t1 symbol table contains wrong number of entries"), 2, t1Symbols.size());
        assertEquals(hint("t1 symbol didn't have expected value after completion"), 5,
            t1Symbols.get("x").getIntValue());
        assertEquals(hint("t1 symbol didn't have expected value after completion"), 7,
            t1Symbols.get("y").getIntValue());
        assertSame(hint("didn't switch to second thread after first completed"), t2, scheduler.getCurrentThread());

        scheduler.run(2);
        assertEquals(hint("t2 symbol table contains wrong number of entries"), 1, t2Symbols.size());
        assertEquals(hint("t1 t1 symbol didn't have expected value"), 5, t1Symbols.get("x").getIntValue());
        assertEquals(hint("t1 symbol didn't have expected value"), 7, t1Symbols.get("y").getIntValue());
        assertEquals(hint("t2 symbol didn't have expected value"), 6, t2Symbols.get("z").getIntValue());
        assertNull(hint("current thread should be null after all threads complete"), scheduler.getCurrentThread());
    }

    /** Test yield. */
    @Test
    public void testYield() {
        /** #score(6) */
        hintContext = "(prio)";

        TestUtil.compileMethod("Main", "t1", """
            load_const 5
            store_local x
            yield
            load_const 7
            store_local y
            """);

        Heap heap = new Heap();
        VMThread t1 = new VMThread("Main", "t1", heap);
        Map<String, Value> t1Symbols = t1.getEntryPointLocals();
        t1Symbols.remove("this");

        TestUtil.compileMethod("Main", "t2", """
            load_const 6
            store_local z
            yield
            """);
        VMThread t2 = new VMThread("Main", "t2", heap);
        Map<String, Value> t2Symbols = t2.getEntryPointLocals();
        t2Symbols.remove("this");

        List<VMThread> threadList = new ArrayList<VMThread>();
        threadList.add(t1);
        threadList.add(t2);

        PriorityScheduler scheduler = new PriorityScheduler(threadList);

        assertSame(hint("getCurrentThread returns wrong object"), t1, scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table should be empty initially"), 0, t1Symbols.size());

        scheduler.run(2);
        assertEquals(hint("t1 symbol table contains wrong number of entries"), 1, t1Symbols.size());
        assertEquals(hint("t1 t1 symbol didn't have expected value"), 5, t1Symbols.get("x").getIntValue());
        assertSame(hint("current thread changed prematurely"), t1, scheduler.getCurrentThread());

        scheduler.run(1);
        assertSame(hint("yield did not change current thread"), t2, scheduler.getCurrentThread());

        scheduler.run(2);
        assertSame(hint("current thread changed prematurely"), t2, scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table contains wrong number of entries"), 1, t1Symbols.size());
        assertEquals(hint("t2 symbol table contains wrong number of entries"), 1, t2Symbols.size());
        assertEquals(hint("t1 t1 symbol didn't have expected value"), 5, t1Symbols.get("x").getIntValue());
        assertEquals(hint("t2 symbol didn't have expected value"), 6, t2Symbols.get("z").getIntValue());

        scheduler.run(1);
        assertSame(hint("yield didn't change back to first thread"), t1, scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table contains wrong number of entries"), 1, t1Symbols.size());

        scheduler.run(2);
        assertEquals(hint("t1 symbol table contains wrong number of entries"), 2, t1Symbols.size());
        assertEquals(hint("t1 t1 symbol didn't have expected value"), 5, t1Symbols.get("x").getIntValue());
        assertEquals(hint("t1 symbol didn't have expected value"), 7, t1Symbols.get("y").getIntValue());
        assertEquals(hint("t2 symbol didn't have expected value"), 6, t2Symbols.get("z").getIntValue());
    }

    /** Test sleep. */
    @Test
    public void testSleep() {
        /** #score(3) */
        hintContext = "(prio)";
        TestUtil.compileMethod("Main", "t1", """
            load_const 5
            store_local x
            sleep 1
            load_const 7
            store_local y
            """);
        Heap heap = new Heap();
        VMThread t1 = new VMThread("Main", "t1", heap);
        Map<String, Value> t1Symbols = t1.getEntryPointLocals();
        t1Symbols.remove("this");

        TestUtil.compileMethod("Main", "t2", """
            load_const 6
            store_local z
            """);
        VMThread t2 = new VMThread("Main", "t2", heap);
        Map<String, Value> t2Symbols = t2.getEntryPointLocals();
        t2Symbols.remove("this");

        List<VMThread> threadList = new ArrayList<VMThread>();
        threadList.add(t1);
        threadList.add(t2);

        PriorityScheduler scheduler = new PriorityScheduler(threadList);

        assertSame(hint("getCurrentThread returns wrong object"), t1, scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table should be empty initially"), 0, t1Symbols.size());

        scheduler.run(3);
        assertSame(hint("sleep did not change current thread"), t2, scheduler.getCurrentThread());

        scheduler.run(1);
        assertSame(hint("current thread changed prematurely when sleep expired"), t2, scheduler.getCurrentThread());

        scheduler.run(1);
        assertSame(hint("thread didn't wake up after sleeping"), t1, scheduler.getCurrentThread());

        scheduler.run(2);
        assertEquals(hint("t1 symbol table contains wrong number of entries"), 2, t1Symbols.size());
        assertEquals(hint("t2 symbol table contains wrong number of entries"), 1, t2Symbols.size());
        assertEquals(hint("t1 symbol didn't have expected value"), 5, t1Symbols.get("x").getIntValue());
        assertEquals(hint("t1 symbol didn't have expected value"), 7, t1Symbols.get("y").getIntValue());
        assertEquals(hint("t2 symbol didn't have expected value"), 6, t2Symbols.get("z").getIntValue());
    }

    /** Test sleep. */
    @Test
    public void testSleep2() {
        /** #score(3) */
        hintContext = "(prio)";
        TestUtil.compileMethod("Main", "t1", """
            load_const 5
            store_local x
            sleep 3
            load_const 7
            store_local y
            """);
        Heap heap = new Heap();
        VMThread t1 = new VMThread("Main", "t1", heap);
        Map<String, Value> t1Symbols = t1.getEntryPointLocals();
        t1Symbols.remove("this");

        TestUtil.compileMethod("Main", "t2", """
            load_const 6
            store_local z
            """);
        VMThread t2 = new VMThread("Main", "t2", heap);
        Map<String, Value> t2Symbols = t2.getEntryPointLocals();
        t2Symbols.remove("this");

        List<VMThread> threadList = new ArrayList<VMThread>();
        threadList.add(t1);
        threadList.add(t2);

        PriorityScheduler scheduler = new PriorityScheduler(threadList);

        assertSame(hint("getCurrentThread returns wrong object"), t1, scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table should be empty initially"), 0, t1Symbols.size());

        scheduler.run(3);
        assertSame(hint("sleep did not change current thread"), t2, scheduler.getCurrentThread());

        scheduler.run(2);
        assertNull(hint("current thread should be null when all threads are sleeping"), scheduler.getCurrentThread());

        scheduler.run(1);
        assertSame(hint("thread didn't wake correctly after sleep"), t1, scheduler.getCurrentThread());

        scheduler.run(2);
        assertEquals(hint("t1 symbol table contains wrong number of entries"), 2, t1Symbols.size());
        assertEquals(hint("t2 symbol table contains wrong number of entries"), 1, t2Symbols.size());
        assertEquals(hint("t1 symbol didn't have expected value"), 5, t1Symbols.get("x").getIntValue());
        assertEquals(hint("t1 symbol didn't have expected value"), 7, t1Symbols.get("y").getIntValue());
        assertEquals(hint("t2 symbol didn't have expected value"), 6, t2Symbols.get("z").getIntValue());
    }

    /** Test sleep. */
    @Test
    public void testSleep3() {
        /** #score(3) */
        hintContext = "(prio)";
        TestUtil.compileMethod("Main", "t1", """
            load_const 5
            sleep 3
            store_local x
            """);
        Heap heap = new Heap();
        VMThread t1 = new VMThread("Main", "t1", heap);
        Map<String, Value> t1Symbols = t1.getEntryPointLocals();
        t1Symbols.remove("this");

        TestUtil.compileMethod("Main", "t2", """
            load_const 6
            sleep 2
            store_local z
            """);
        VMThread t2 = new VMThread("Main", "t2", heap);
        Map<String, Value> t2Symbols = t2.getEntryPointLocals();
        t2Symbols.remove("this");

        List<VMThread> threadList = new ArrayList<VMThread>();
        threadList.add(t1);
        threadList.add(t2);

        PriorityScheduler scheduler = new PriorityScheduler(threadList);

        assertSame(hint("getCurrentThread returns wrong object"), t1, scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table should be empty initially"), 0, t1Symbols.size());

        scheduler.run(2);
        assertSame(hint("sleep did not change current thread"), t2, scheduler.getCurrentThread());

        scheduler.run(2);
        assertNull(hint("there should be no current thread when all threads are sleeping"),
            scheduler.getCurrentThread());

        scheduler.run(1);
        assertSame(hint("thread didn't wake correctly after sleep"), t1, scheduler.getCurrentThread());

        scheduler.run(1);
        assertSame(hint("thread didn't wake correctly after sleep"), t2, scheduler.getCurrentThread());

        scheduler.run(1);
        assertNull(hint("current thread should be null after threads complete"), scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table contains wrong number of entries"), 1, t1Symbols.size());
        assertEquals(hint("t2 symbol table contains wrong number of entries"), 1, t2Symbols.size());
        assertEquals(hint("t1 symbol didn't have expected value"), 5, t1Symbols.get("x").getIntValue());
        assertEquals(hint("t2 symbol didn't have expected value"), 6, t2Symbols.get("z").getIntValue());
    }

    /** Test priorities. */
    @Test
    public void testPriorities() {
        /** #score(6) */
        hintContext = "(prio)";
        TestUtil.compileMethod("Main", "t1", """
            load_const 5
            store_local x
            """);
        Heap heap = new Heap();
        VMThread t1 = new VMThread("Main", "t1", 0, heap);
        Map<String, Value> t1Symbols = t1.getEntryPointLocals();
        t1Symbols.remove("this");

        TestUtil.compileMethod("Main", "t2", """
            load_const 6
            store_local z
            """);
        VMThread t2 = new VMThread("Main", "t2", 1, heap);
        Map<String, Value> t2Symbols = t2.getEntryPointLocals();
        t2Symbols.remove("this");

        List<VMThread> threadList = new ArrayList<VMThread>();
        threadList.add(t1);
        threadList.add(t2);

        PriorityScheduler scheduler = new PriorityScheduler(threadList);

        assertSame(hint("getCurrentThread returns wrong object"), t2, scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table should be empty initially"), 0, t1Symbols.size());

        scheduler.run(2);
        assertSame(hint("current thread wrong after thread ended"), t1, scheduler.getCurrentThread());

        scheduler.run(2);
        assertNull(hint("current thread should be null"), scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table contains wrong number of entries"), 1, t1Symbols.size());
        assertEquals(hint("t2 symbol table contains wrong number of entries"), 1, t2Symbols.size());
        assertEquals(hint("t1 symbol didn't have expected value"), 5, t1Symbols.get("x").getIntValue());
        assertEquals(hint("t2 symbol didn't have expected value"), 6, t2Symbols.get("z").getIntValue());
    }

    /** Test priorities. */
    @Test
    public void testPriorities2() {
        /** #score(6) */
        hintContext = "(prio)";

        TestUtil.compileMethod("Main", "t1", """
            load_const 5
            store_local x
            """);
        Heap heap = new Heap();
        VMThread t1 = new VMThread("Main", "t1", 0, heap);
        Map<String, Value> t1Symbols = t1.getEntryPointLocals();
        t1Symbols.remove("this");

        TestUtil.compileMethod("Main", "t2", """
            load_const 6
            sleep 1
            store_local z
            """);
        VMThread t2 = new VMThread("Main", "t2", 1, heap);
        Map<String, Value> t2Symbols = t2.getEntryPointLocals();
        t2Symbols.remove("this");

        List<VMThread> threadList = new ArrayList<VMThread>();
        threadList.add(t1);
        threadList.add(t2);

        PriorityScheduler scheduler = new PriorityScheduler(threadList);

        assertSame(hint("getCurrentThread returns wrong object"), t2, scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table should be empty initially"), 0, t1Symbols.size());

        scheduler.run(2);
        assertSame(hint("current thread wrong after sleep"), t1, scheduler.getCurrentThread());

        scheduler.run(1);
        assertSame(hint("high priority thread waking up should have preempted running thread"), t2,
            scheduler.getCurrentThread());
        assertEquals(hint("t2 symbol table contains wrong number of entries"), 0, t2Symbols.size());

        scheduler.run(1);
        assertSame(hint("current thread wrong after thread ended"), t1, scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table contains wrong number of entries"), 1, t2Symbols.size());
        assertEquals(hint("t2 symbol didn't have expected value"), 6, t2Symbols.get("z").getIntValue());

        scheduler.run(1);
        assertNull(hint("current thread should be null"), scheduler.getCurrentThread());
        assertEquals(hint("t1 symbol table contains wrong number of entries"), 1, t1Symbols.size());
        assertEquals(hint("t2 symbol table contains wrong number of entries"), 1, t2Symbols.size());
        assertEquals(hint("t1 symbol didn't have expected value"), 5, t1Symbols.get("x").getIntValue());
        assertEquals(hint("t2 symbol didn't have expected value"), 6, t2Symbols.get("z").getIntValue());
    }

}
