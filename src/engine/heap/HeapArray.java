package engine.heap;

import java.util.Arrays;
import java.util.Collection;

import engine.exceptions.MissingFieldException;
import types.DataType;
import types.Value;

/**
 * HeapArray represents an array on the heap.
 */
public class HeapArray extends HeapEntry {
    private DataType type;
    private Value[] data;

    /**
     * Creates an array. This is always a one-dimensional array. Multiple HeapArrays
     * must be created to represent multi-dimenional arrays.
     *
     * @param id     the heap id
     * @param type   the array type
     * @param length the array length
     */
    public HeapArray(int id, DataType type, int length) {
        super(id);
        this.type = type;
        this.data = new Value[length];
    }

    /**
     * Returns the value an an index.
     *
     * @param index the index
     * @return the value
     */
    public Value getAt(int index) {
        return data[index];
    }

    /**
     * Sets the value at an index.
     *
     * @param index the index
     * @param value the value
     */
    public void setAt(int index, Value value) {
        data[index] = value;
    }

    /**
     * Returns the length of the array.
     *
     * @return the length of the array
     */
    public int getLength() {
        return data.length;
    }

    @Override
    public DataType getType() {
        return type;
    }

    @Override
    public Value getFieldValue(String name) {
        if ("length".equals(name)) {
            return new Value(data.length);
        }
        throw new MissingFieldException("Arrays do not have field: " + name);
    }

    @Override
    public Collection<Value> getReferencedValues() {
        return Arrays.asList(data);
    }
}
