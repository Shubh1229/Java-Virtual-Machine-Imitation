package ast.model;

import ast.visitors.Visitor;

/**
 * Represents a constant.
 */
public class ConstantExpression extends Expression {
    private int value;

    public ConstantExpression(int val) {
        this.value = val;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }
}
