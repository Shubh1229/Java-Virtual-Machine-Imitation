import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.junit.Test;

import engine.StackFrame;
import engine.heap.Heap;
import engine.heap.HeapArray;
import engine.heap.HeapObject;
import types.Clazz;
import types.DataType;
import types.Field;
import types.Value;

public class Part09Test {

    private void checkDeleted(Set<Integer> expectedDeletedIds, Set<Integer> actualDeletedIds, Heap heap) {
        assertEquals("set of deleted object ids incorrect", expectedDeletedIds, actualDeletedIds);
        for (int id : expectedDeletedIds) {
            assertNull("heap should no longer contain deleted objects", heap.getEntry(id));
        }
    }

    @Test
    public void testOpStackGC() {
        /** #score(5) */
        Heap heap = new Heap();
        Clazz myClass = new Clazz("MyClass");
        DataType myClassType = new DataType("MyClass");

        HeapObject o1 = heap.createObject(myClass);
        HeapObject o2 = heap.createObject(myClass);
        HeapObject o3 = heap.createObject(myClass);

        Stack<Value> opStack = new Stack<>();
        opStack.push(new Value(o2.getId(), DataType.INT));
        opStack.push(new Value(o1.getId(), myClassType));
        opStack.push(new Value(o3.getId(), myClassType));

        Set<Integer> deleted = heap.gc(new Stack<>(), opStack);
        assertFalse("integers should not be treated as object ids", deleted.isEmpty());
        checkDeleted(Set.of(2), deleted, heap);

        opStack.pop(); // o3 no longer referenced
        deleted = heap.gc(new Stack<>(), opStack);
        checkDeleted(Set.of(3), deleted, heap);
    }

    @Test
    public void testCallStackGC() {
        /** #score(5) */
        Heap heap = new Heap();
        Clazz myClass = new Clazz("MyClass");
        DataType myClassType = new DataType("MyClass");

        HeapObject o1 = heap.createObject(myClass);
        HeapObject o2 = heap.createObject(myClass);
        HeapObject o3 = heap.createObject(myClass);

        Stack<StackFrame> callStack = new Stack<>();

        Map<String, Value> localVars = new HashMap<>();
        localVars.put("o3", new Value(o3.getId(), myClassType));
        StackFrame frame = new StackFrame(new ArrayList<>(), localVars);
        callStack.push(frame);

        localVars = new HashMap<>();
        frame = new StackFrame(new ArrayList<>(), localVars);
        localVars.put("o2", new Value(o2.getId(), myClassType));
        localVars.put("o1", new Value(o1.getId(), DataType.INT));
        callStack.push(frame);

        Set<Integer> deleted = heap.gc(callStack, new Stack<>());
        assertFalse("int values should not be treated as object references", deleted.isEmpty());
        checkDeleted(Set.of(1), deleted, heap);

        callStack.pop(); // o2 no longer referenced
        deleted = heap.gc(callStack, new Stack<>());
        checkDeleted(Set.of(2), deleted, heap);
    }

    private List<HeapObject> setupGraph(Heap heap) {
        DataType myClassType = new DataType("MyClass");
        Clazz myClass = new Clazz("MyClass");
        myClass.addField(new Field("left", myClassType));
        myClass.addField(new Field("right", myClassType));
        myClass.addField(new Field("val", DataType.INT));

        List<HeapObject> objects = new ArrayList<>();
        objects.add(null);
        for (int i = 0; i < 8; i++) {
            HeapObject obj = heap.createObject(myClass);
            obj.setFieldValue("val", new Value(obj.getId() + 1, DataType.INT));
            objects.add(obj);
        }

        objects.get(4).setFieldValue("left", new Value(objects.get(5)));
        objects.get(4).setFieldValue("right", new Value(objects.get(6)));

        objects.get(3).setFieldValue("right", new Value(objects.get(4)));

        objects.get(1).setFieldValue("left", new Value(objects.get(2)));
        objects.get(1).setFieldValue("right", new Value(objects.get(3)));

        return objects;
    }

    @Test
    public void testObjectGraph() {
        /** #score(5) */
        DataType myClassType = new DataType("MyClass");
        Heap heap = new Heap();
        Stack<Value> opStack = new Stack<>();
        List<HeapObject> objects = setupGraph(heap);
        opStack.push(new Value(objects.get(1).getId(), myClassType));

        Set<Integer> deleted = heap.gc(new Stack<>(), opStack);
        assertFalse("integers should not be treated as object ids", deleted.isEmpty());
        checkDeleted(Set.of(7, 8), deleted, heap);

        // Link to 4,5,6 broken.
        objects.get(3).setFieldValue("right", new Value(0, myClassType));

        deleted = heap.gc(new Stack<>(), opStack);
        checkDeleted(Set.of(4, 5, 6), deleted, heap);

        // Link to 2 broken.
        objects.get(1).setFieldValue("left", new Value(0, myClassType));

        deleted = heap.gc(new Stack<>(), opStack);
        checkDeleted(Set.of(2), deleted, heap);
    }

    @Test
    public void testObjectGraphWithCycles() {
        /** #score(5) */
        DataType myClassType = new DataType("MyClass");
        Heap heap = new Heap();
        Stack<StackFrame> callStack = new Stack<>();
        Map<String, Value> localVars = new HashMap<>();
        StackFrame frame = new StackFrame(new ArrayList<>(), localVars);
        callStack.push(frame);

        List<HeapObject> objects = setupGraph(heap);
        localVars.put("o1", new Value(objects.get(1)));

        objects.get(7).setFieldValue("left", new Value(objects.get(8)));
        objects.get(8).setFieldValue("left", new Value(objects.get(7)));
        objects.get(3).setFieldValue("left", new Value(objects.get(1)));

        Set<Integer> deleted = heap.gc(callStack, new Stack<>());
        checkDeleted(Set.of(7, 8), deleted, heap);

        objects.get(1).setFieldValue("right", new Value(0, myClassType));

        deleted = heap.gc(callStack, new Stack<>());
        checkDeleted(Set.of(3, 4, 5, 6), deleted, heap);
    }

    @Test
    public void testArrays() {
        /** #score(5) */
        DataType myClassType = new DataType("MyClass");
        Clazz myClass = new Clazz("MyClass");
        Heap heap = new Heap();
        Stack<Value> opStack = new Stack<>();

        HeapArray a1 = heap.createArray(new DataType("MyClass", 1), new int[] { 3 });
        HeapArray a2 = heap.createArray(new DataType("int", 1), new int[] { 3 });

        HeapObject o3 = heap.createObject(myClass);
        HeapObject o4 = heap.createObject(myClass);
        HeapObject o5 = heap.createObject(myClass);

        a1.setAt(0, new Value(o3));
        a1.setAt(1, new Value(o4));
        a1.setAt(2, new Value(o5));

        a2.setAt(0, new Value(o3.getId(), DataType.INT));
        a2.setAt(1, new Value(o4.getId(), DataType.INT));
        a2.setAt(2, new Value(o5.getId(), DataType.INT));

        opStack.push(new Value(a2));
        opStack.push(new Value(a1));

        Set<Integer> deleted = heap.gc(new Stack<>(), opStack);
        checkDeleted(Set.of(), deleted, heap);

        // o4 disconnected
        a1.setAt(1, new Value(0, myClassType));
        deleted = heap.gc(new Stack<>(), opStack);
        assertFalse("integers should not be treated as object ids", deleted.isEmpty());
        checkDeleted(Set.of(4), deleted, heap);

        // a1 disconnected
        opStack.pop();
        deleted = heap.gc(new Stack<>(), opStack);
        checkDeleted(Set.of(1, 3, 5), deleted, heap);

        // a2 disconnected
        opStack.pop();
        deleted = heap.gc(new Stack<>(), opStack);
        checkDeleted(Set.of(2), deleted, heap);
    }

    private void setAt(Heap heap, HeapArray arr, HeapObject obj, int... indices) {
        HeapArray a = arr;
        for (int i = 0; i < indices.length - 1; i++) {
            a = (HeapArray) heap.getEntry(a.getAt(indices[i]).getIntValue());
        }

        Value val = obj != null ? new Value(obj) : new Value(0, a.getType());
        a.setAt(indices[indices.length - 1], val);
    }

    @Test
    public void testMultiArrays() {
        /** #score(5) */
        Clazz myClass = new Clazz("MyClass");
        Heap heap = new Heap();
        Stack<StackFrame> callStack = new Stack<>();
        Map<String, Value> localVars = new HashMap<>();
        StackFrame frame = new StackFrame(new ArrayList<>(), localVars);
        callStack.push(frame);

        HeapArray a1 = heap.createArray(new DataType("MyClass", 3), new int[] { 2, 2, 2 });
        HeapObject o2 = heap.createObject(myClass);
        HeapObject o3 = heap.createObject(myClass);
        HeapObject o4 = heap.createObject(myClass);
        HeapObject o5 = heap.createObject(myClass);

        setAt(heap, a1, o2, 0, 0, 0);
        setAt(heap, a1, o3, 0, 0, 1);
        setAt(heap, a1, o4, 0, 1, 0);
        setAt(heap, a1, o5, 1, 0, 1);

        localVars.put("a1", new Value(a1));

        Set<Integer> deleted = heap.gc(callStack, new Stack<>());
        checkDeleted(Set.of(), deleted, heap);

        // Clear 3 and 4
        setAt(heap, a1, null, 0, 0, 1);
        setAt(heap, a1, null, 0, 1, 0);
        deleted = heap.gc(callStack, new Stack<>());
        checkDeleted(Set.of(o3.getId(), o4.getId()), deleted, heap);
    }

    @Test
    public void testComplex() {
        /** #score(5) */
        DataType myClassType = new DataType("MyClass");
        Clazz myClass = new Clazz("MyClass");
        Heap heap = new Heap();
        Stack<Value> opStack = new Stack<>();

        List<HeapObject> objects = setupGraph(heap);
        objects.get(3).setFieldValue("left", new Value(objects.get(7)));
        objects.get(7).setFieldValue("left", new Value(objects.get(8)));
        objects.get(8).setFieldValue("left", new Value(objects.get(1)));

        HeapArray arr = heap.createArray(new DataType("MyClass", 3), new int[] { 2, 2, 2 });
        HeapObject o9 = heap.createObject(myClass);
        HeapObject o10 = heap.createObject(myClass);
        HeapObject o11 = heap.createObject(myClass);

        setAt(heap, arr, objects.get(1), 0, 0, 0);
        setAt(heap, arr, o9, 0, 0, 1);
        setAt(heap, arr, o10, 0, 1, 0);
        setAt(heap, arr, o11, 1, 0, 1);

        HeapArray arr2 = heap.createArray(new DataType("MyClass", 1), new int[] { 3 });
        o11.setFieldValue("left", new Value(arr2));
        setAt(heap, arr2, o9, 0);
        setAt(heap, arr2, o11, 1);
        setAt(heap, arr2, objects.get(5), 2);

        opStack.push(new Value(arr));

        Set<Integer> deleted = heap.gc(new Stack<>(), opStack);
        checkDeleted(Set.of(), deleted, heap);

        // Break links, nothing collected
        setAt(heap, arr, null, 0, 0, 1);
        objects.get(8).setFieldValue("left", new Value(0, myClassType));
        objects.get(4).setFieldValue("left", new Value(0, myClassType));
        deleted = heap.gc(new Stack<>(), opStack);
        checkDeleted(Set.of(), deleted, heap);

        setAt(heap, arr, null, 1, 0, 1);
        deleted = heap.gc(new Stack<>(), opStack);
        checkDeleted(Set.of(o11.getId(), arr2.getId(), o9.getId(), 5), deleted, heap);

        objects.get(3).setFieldValue("left", new Value(0, myClassType));
        deleted = heap.gc(new Stack<>(), opStack);
        checkDeleted(Set.of(7, 8), deleted, heap);

        opStack.pop();
        deleted = heap.gc(new Stack<>(), opStack);
        checkDeleted(Set.of(1, 2, 3, 4, 6, 9, 10, 11, 12, 13, 14, 15, 17), deleted, heap);
    }

    private void testEfficiency(Runnable r, String timeoutMsg) {
        Thread t = new Thread(r);
        t.start();

        try {
            for (int i = 0; i < 20; i++) {
                if (!t.isAlive()) {
                    return;
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
        }

        t.interrupt();
        fail(timeoutMsg);
    }

    @Test
    public void testEfficiency() {
        /** #score(5) */

        testEfficiency(() -> {
            DataType myClassArrType = new DataType("MyClass", 1);
            Clazz myClass = new Clazz("MyClass");
            Heap heap = new Heap();
            Stack<Value> opStack = new Stack<>();

            HeapArray arr = heap.createArray(myClassArrType, new int[] { 10 });
            Set<Integer> expectedDeletedIds = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                arr.setAt(i, new Value(heap.createObject(myClass)));
                expectedDeletedIds.add(heap.createObject(myClass).getId());
            }

            for (int i = 0; i < 20; i++) {
                HeapArray wrapArr = heap.createArray(myClassArrType, new int[] { 10 });
                for (int j = 0; j < 10; j++) {
                    wrapArr.setAt(j, new Value(arr));
                }
                arr = wrapArr;
            }

            opStack.push(new Value(arr));

            Set<Integer> deleted = heap.gc(new Stack<>(), opStack);
            checkDeleted(expectedDeletedIds, deleted, heap);
        }, "gc timed out");
    }
}
