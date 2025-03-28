package ast.model;

import ast.visitors.Visitor;

public class NullExpression extends Expression {

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        return "null";
    }
}
