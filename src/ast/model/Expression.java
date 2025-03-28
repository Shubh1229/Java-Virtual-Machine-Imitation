package ast.model;

import ast.visitors.Visitable;

/**
 * Base type for expressions.
 */
public abstract class Expression implements Visitable {
    private boolean isLValue;

    public boolean isLValue() {
        return isLValue;
    }

    public void setLValue(boolean isLValue) {
        this.isLValue = isLValue;
    }
}
