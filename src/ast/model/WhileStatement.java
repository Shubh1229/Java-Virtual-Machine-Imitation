package ast.model;

import ast.visitors.Visitor;

/**
 * Represents a while statement.
 */
public class WhileStatement extends Statement {
    private Expression condition;
    private Statement body;

    public WhileStatement(Expression aCondition,
        Statement aBody) {
        this.condition = aCondition;
        this.body = aBody;
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getBody() {
        return body;
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        condition.accept(v);
        v.preBodyVisit(this);
        body.accept(v);
        v.postVisit(this);
    }

}
