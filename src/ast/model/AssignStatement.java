package ast.model;

import ast.visitors.Visitor;

/**
 * Represents an assign statement.
 */
public class AssignStatement extends Statement {
    private Expression lValue;
    private Expression value;

    /**
     * Creates an assign statement.
     *
     * @param lvalue the variable
     * @param value  the value
     */
    public AssignStatement(Expression lvalue, Expression value) {
        lvalue.setLValue(true);
        this.lValue = lvalue;
        this.value = value;
    }

    public Expression getLValue() {
        return lValue;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        value.accept(v);
        v.visit(this);
        lValue.accept(v);
        v.postVisit(this);
    }
}
