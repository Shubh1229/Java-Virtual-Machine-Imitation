package types;

import engine.heap.HeapEntry;

/**
 * Represents a value in the VM.
 */
public class Value {
    /**
     * The value, which is either an integer value, or the id of an object in the
     * heap
     */
    private int intValue;
    /** The data type */
    private DataType type;

    public Value(int intValue) {
        this.intValue = intValue;
        this.type = DataType.INT;
    }

    public Value(int intValue, DataType type) {
        this.intValue = intValue;
        this.type = type;
    }

    /**
     * Creates a Value referencing the given HeapEntry.
     * 
     * @param entry an object or array from the Heap
     */
    public Value(HeapEntry entry) {
        this.intValue = entry.getId();
        this.type = entry.getType();
    }

    public int getIntValue() {
        return intValue;
    }

    public DataType getType() {
        return type;
    }

    public boolean isObject() {
        return type.isObject();
    }

    @Override
    public String toString() {
        if (isObject()) {
            return type + "@" + intValue;
        }
        return "" + intValue;
    }

}
