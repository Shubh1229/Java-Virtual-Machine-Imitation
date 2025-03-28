package ast.model;

import ast.visitors.Visitable;
import ast.visitors.Visitor;
import types.DataType;

/**
 * Represents a parameter definition, e.g. "int x".
 */
public class ParameterDefinition implements Visitable {
    private String name;
    private DataType type;

    public ParameterDefinition(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

}
