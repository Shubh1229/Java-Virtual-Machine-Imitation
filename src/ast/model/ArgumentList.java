package ast.model;

import java.util.ArrayList;
import java.util.List;

import ast.visitors.Visitable;
import ast.visitors.Visitor;

/**
 * Represents the argument list in a method call.
 */
public class ArgumentList extends ArrayList<Expression> implements Visitable {

    public ArgumentList(List<Expression> arguments) {
        super(arguments);
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        for (int i = 0; i < size(); i++) {
            Expression a = get(i);
            if (i > 0) {
                v.betweenArgumentVisit(this);
            }
            a.accept(v);
        }
        v.postVisit(this);
    }

}
