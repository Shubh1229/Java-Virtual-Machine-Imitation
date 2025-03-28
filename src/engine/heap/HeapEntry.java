package engine.heap;

import java.util.Collection;

import types.DataType;
import types.Value;

/**
 * HeapEntry is the base class for objects on the heap.
 */
public abstract class HeapEntry {
    /** The heap id */
    private int id;

    public HeapEntry(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return getType() + "@" + id;
    }

    /**
     * Returns the type of the object.
     *
     * @return the type of the object
     */
    public abstract DataType getType();

    /**
     * Returns the value of a field.
     *
     * @param name the field name
     * @return the value
     */
    public abstract Value getFieldValue(String name);

    /**
     * Returns a collection of values that are referenced by this object.
     * 
     * @return the collection
     */
    public abstract Collection<Value> getReferencedValues();
}
