package ast.model;

import ast.visitors.Visitor;

/**
 * Represents a return statement.
 */
public class ReturnStatement extends Statement {
    private Expression returnValue;

    public ReturnStatement(Expression returnValue) {
        this.returnValue = returnValue;
    }

    public Expression getReturnValue() {
        return returnValue;
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        if (returnValue != null) {
            returnValue.accept(v);
        }
        v.postVisit(this);
    }

}
