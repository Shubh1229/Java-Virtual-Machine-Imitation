package ast.model;

import ast.visitors.Visitable;
import ast.visitors.Visitor;
import types.DataType;

/**
 * Represents a field definition.
 */
public class FieldDefinition implements Visitable {
    private DataType type;
    private String name;

    public FieldDefinition(DataType type, String name) {
        this.type = type;
        this.name = name;
    }

    public DataType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

}
