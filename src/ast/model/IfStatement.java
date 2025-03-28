package ast.model;

import ast.visitors.Visitor;

/**
 * Represents an if statement.
 */
public class IfStatement extends Statement {
    private Expression condition;
    private Statement thenBlock;
    private Statement elseBlock;

    public IfStatement(Expression aCondition, Statement aThenBlock) {
        this.condition = aCondition;
        this.thenBlock = aThenBlock;
    }

    public IfStatement(Expression aCondition, Statement aThenBlock, Statement anElseBlock) {
        this(aCondition, aThenBlock);
        this.elseBlock = anElseBlock;
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getThenBlock() {
        return thenBlock;
    }

    public Statement getElseBlock() {
        return elseBlock;
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        condition.accept(v);
        v.preThenVisit(this);
        thenBlock.accept(v);

        if (elseBlock != null) {
            v.preElseVisit(this);
            elseBlock.accept(v);
        }

        v.postVisit(this);
    }
}
