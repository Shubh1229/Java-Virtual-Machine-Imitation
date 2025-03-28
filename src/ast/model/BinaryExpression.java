package ast.model;

import ast.visitors.Visitor;
import engine.opcodes.Operator;

/**
 * Represents a binary expression.
 *
 */
public class BinaryExpression extends Expression {
    private Expression leftChild;
    private Expression rightChild;
    private Operator operator;

    public BinaryExpression(Expression left, Operator op, Expression right) {
        leftChild = left;
        operator = op;
        rightChild = right;
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression getLeftChild() {
        return leftChild;
    }

    public Expression getRightChild() {
        return rightChild;
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        leftChild.accept(v);
        v.visit(this);
        rightChild.accept(v);
        v.postVisit(this);
    }
}
