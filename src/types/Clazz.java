package types;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a class definition at runtime.
 */
public class Clazz {
    private String name;
    private DataType type;
    private Map<String, Field> fields;
    private Map<String, Method> methods;

    public Clazz(String name) {
        this.name = name;
        this.type = new DataType(name);
        this.fields = new LinkedHashMap<>();
        this.methods = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public void addField(Field field) {
        fields.put(field.getName(), field);
    }

    public Field getField(String fieldName) {
        return fields.get(fieldName);
    }

    public int getNumFields() {
        return fields.size();
    }

    public void addMethod(Method method) {
        methods.put(method.getName(), method);
    }

    public Method getMethod(String methodName) {
        return methods.get(methodName);
    }
}
