package ast.model;

import ast.visitors.Visitor;

/**
 * Represents an array selector expressions, e.g. [1] or [x + 2]
 */
public class ArraySelectorExpression extends Expression {
    /** The thing we're indexing into */
    private Expression target;
    /** The index expression */
    private Expression indexExpression;

    public ArraySelectorExpression(Expression target, Expression indexExpression) {
        this.target = target;
        this.indexExpression = indexExpression;
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        indexExpression.accept(v);
        v.postIndexVisit(this);
        target.accept(v);
        v.postVisit(this);
    }
}
