package engine.heap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import engine.exceptions.MissingFieldException;
import engine.exceptions.UninitializedFieldException;
import types.Clazz;
import types.DataType;
import types.Field;
import types.Value;

/**
 * HeapObject represents an object on the heap.
 */
public class HeapObject extends HeapEntry {
    private Clazz clazz;
    private Map<String, Value> fieldValues;

    /**
     * Creates a HeapObject.
     *
     * @param id    the heap id
     * @param clazz the clazz
     */
    public HeapObject(int id, Clazz clazz) {
        super(id);
        this.clazz = clazz;
        this.fieldValues = new HashMap<>();
    }

    @Override
    public DataType getType() {
        return clazz.getType();
    }

    public Clazz getClazz() {
        return clazz;
    }

    @Override
    public Value getFieldValue(String name) {
        Field field = clazz.getField(name);
        if (field == null) {
            throw new MissingFieldException("class " + clazz.getName() + " does not have a field called " + name);
        }

        Value value = fieldValues.get(name);
        if (value == null) {
            throw new UninitializedFieldException(clazz.getName() + "." + name + " read, but not initialized");
        }
        return value;
    }

    public void setFieldValue(String name, Value value) {
        fieldValues.put(name, value);
    }

    @Override
    public Collection<Value> getReferencedValues() {
        return fieldValues.values();
    }

}
