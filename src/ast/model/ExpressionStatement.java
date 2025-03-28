package ast.model;

import ast.visitors.Visitor;

/**
 * Represents a statement that is itself an expression, e.g. a method call.
 */
public class ExpressionStatement extends Statement {
    private Expression expression;

    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void accept(Visitor v) {
        expression.accept(v);
        v.postVisit(this);
    }

}
