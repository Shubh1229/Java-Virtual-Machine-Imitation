package engine.heap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import engine.StackFrame;
import types.Clazz;
import types.DataType;
import types.Value;

/**
 * Heap stores objects and arrays.
 */
public class Heap {
    /** The next id to be assigned. */
    private int nextId = 1;
    /** The heap entries, by id. */
    private Map<Integer, HeapEntry> entries = new HashMap<>();

    /**
     * Gets an entry by id.
     *
     * @param id the id
     * @return the entry
     */
    public HeapEntry getEntry(int id) {
        return entries.get(id);
    }

    /**
     * Creates a new object type.
     *
     * @param clazz the clazz of the new object
     * @return the new object
     */
    public HeapObject createObject(Clazz clazz) {
        HeapObject newObj = new HeapObject(nextId, clazz);
        entries.put(nextId, newObj);
        nextId++;
        return newObj;
    }

    /**
     * Creates a new array.
     *
     * @param arrType the type of the array
     * @param dims    the array dimensions
     * @return the new array
     */
    public HeapArray createArray(DataType arrType, int[] dims) {
        return createArray(arrType, dims, 0);
    }

    private HeapArray createArray(DataType arrType, int[] dims, int index) {
        HeapArray arr = createHeapArray(arrType, dims[index]);

        // If we're on the innermost array, fill it with zeros, which represent null or
        // 0.
        if (index == dims.length - 1) {
            DataType elemType = arrType.getElementType();
            for (int i = 0; i < dims[index]; i++) {
                arr.setAt(i, new Value(0, elemType));
            }
            return arr;
        }

        // Recursively create the subarrays.
        DataType subArrType = arrType.getElementType();
        for (int i = 0; i < dims[index]; i++) {
            HeapArray subArr = createArray(subArrType, dims, index + 1);
            arr.setAt(i, new Value(subArr));
        }

        return arr;
    }

    private HeapArray createHeapArray(DataType type, int length) {
        HeapArray arr = new HeapArray(nextId, type, length);
        entries.put(nextId, arr);
        nextId++;
        return arr;
    }

    private void addObjectReferences(Queue<Integer> q, Set<Integer> seen, Collection<Value> values) {
        for (Value v : values) {
            if (!v.isObject()) {
                continue;
            }

            int objId = v.getIntValue();
            if (!seen.contains(objId)) {
                q.add(objId);
                seen.add(objId);
            }
        }
    }

    /**
     * Deletes unreferenced objects from the heap.
     *
     * @param callStack the callStack
     * @param opStack   the opStack
     * @return the object ids that were deleted
     */
    public Set<Integer> gc(Stack<StackFrame> callStack, Stack<Value> opStack) {
        Set<Integer> seen = new HashSet<>();
        Queue<Integer> q = new LinkedList<>();

        // Add references from opStack and callStack to q.
        addObjectReferences(q, seen, opStack);

        for (StackFrame frame : callStack) {
            addObjectReferences(q, seen, frame.getLocalVars().values());
        }

        // Follow the graph of references to all reachable objects.
        while (!q.isEmpty()) {
            int objId = q.remove();

            HeapEntry entry = entries.get(objId);
            if (entry != null) {
                addObjectReferences(q, seen, entry.getReferencedValues());
            }
        }

        // Delete all objects that were not reachable.
        Set<Integer> deleted = entries.values().stream()
            .map(HeapEntry::getId)
            .filter(id -> !seen.contains(id))
            .collect(Collectors.toSet());

        deleted.forEach(id -> entries.remove(id));

        return deleted;
    }
}
