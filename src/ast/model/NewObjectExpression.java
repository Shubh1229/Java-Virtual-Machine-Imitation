package ast.model;

import ast.visitors.Visitor;

/**
 * Represents a new object expression, e.g. "new MyClass(3, 4)".
 */
public class NewObjectExpression extends Expression {
    private String className;
    private ArgumentList arguments;

    public NewObjectExpression(String className, ArgumentList arguments) {
        this.className = className;
        this.arguments = arguments;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        arguments.accept(v);
        v.postVisit(this);
    }
}
