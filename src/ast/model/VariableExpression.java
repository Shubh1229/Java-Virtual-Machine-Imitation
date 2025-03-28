package ast.model;

import ast.visitors.Visitor;

/**
 * Represents a variable name.
 */
public class VariableExpression extends Expression {
    private String name;

    public VariableExpression(String aName) {
        this.name = aName;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

}
