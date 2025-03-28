package ast.model;

import ast.visitors.Visitor;

/**
 * Represents a function call.
 */
public class MethodCallExpression extends Expression {
    private String methodName;
    private Expression target;
    private ArgumentList arguments;

    public MethodCallExpression(String methodName, Expression target, ArgumentList arguments) {
        this.methodName = methodName;
        this.target = target;
        this.arguments = arguments;
    }

    public String getMethodName() {
        return methodName;
    }

    public ArgumentList getArguments() {
        return arguments;
    }

    public Expression getTarget() {
        return target;
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        arguments.accept(v);
        v.preTargetVisit(this);
        if (target != null) {
            target.accept(v);
        }
        v.postVisit(this);
    }

}
