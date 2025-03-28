package ast.model;

import java.util.List;

import ast.visitors.Visitor;

/**
 * Represents a new array expressions, e.g. "new int[x + 3]".
 */
public class NewArrayExpression extends Expression {
    private String type;
    private List<Expression> dims;

    public NewArrayExpression(String type, List<Expression> dims) {
        this.type = type;
        this.dims = dims;
    }

    public String getType() {
        return type;
    }

    public int getNumDimensions() {
        return dims.size();
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        for (int i = 0; i < dims.size(); i++) {
            dims.get(i).accept(v);
            if (i < dims.size() - 1) {
                v.betweenDimVisit(this);
            }
        }
        v.postVisit(this);
    }

}
