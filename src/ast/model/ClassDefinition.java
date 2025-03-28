package ast.model;

import java.util.LinkedHashMap;
import java.util.List;

import ast.visitors.Visitable;
import ast.visitors.Visitor;

/**
 * Represents a class definition.
 */
public class ClassDefinition implements Visitable {
    private String name;
    // These are LinkedHashMap so that we can regenerate the source in the right
    // order.
    private LinkedHashMap<String, FieldDefinition> fields;
    private LinkedHashMap<String, MethodDefinition> methods;

    public ClassDefinition(String name, List<FieldDefinition> fields, List<MethodDefinition> methods) {
        this.name = name;

        this.fields = new LinkedHashMap<>();
        for (FieldDefinition f : fields) {
            this.fields.put(f.getName(), f);
        }

        this.methods = new LinkedHashMap<>();
        for (MethodDefinition m : methods) {
            this.methods.put(m.getName(), m);
        }
    }

    public String getName() {
        return name;
    }

    public MethodDefinition getMethod(String name) {
        return methods.get(name);
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        for (FieldDefinition f : fields.values()) {
            f.accept(v);
        }
        for (MethodDefinition m : methods.values()) {
            m.accept(v);
        }
        v.postVisit(this);
    }

}
