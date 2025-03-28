package ast.model;

import ast.visitors.Visitor;

/**
 * Represents a field access expression, e.g. ".member1".
 */
public class FieldAccessExpression extends Expression {
    private String fieldName;
    private Expression target;

    public FieldAccessExpression(String fieldName, Expression target) {
        this.fieldName = fieldName;
        this.target = target;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void accept(Visitor v) {
        target.accept(v);
        v.postTargetVisit(this);
    }

}
