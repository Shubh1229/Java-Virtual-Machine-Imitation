package ast.model;

import java.util.ArrayList;
import java.util.List;

import ast.visitors.Visitable;
import ast.visitors.Visitor;

/**
 * Represents a parameter list, e.g. "(int x, MyClass y)".
 */
public class ParameterList extends ArrayList<ParameterDefinition> implements Visitable {

    public ParameterList(List<ParameterDefinition> parameters) {
        super(parameters);
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        for (int i = 0; i < size(); i++) {
            ParameterDefinition a = get(i);
            if (i > 0) {
                v.betweenParameterVisit(this);
            }
            a.accept(v);
        }
        v.postVisit(this);
    }

}
