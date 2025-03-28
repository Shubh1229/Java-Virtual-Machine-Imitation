package ast.model;

import ast.visitors.Visitable;
import ast.visitors.Visitor;
import types.DataType;

/**
 * Represents a method definition.
 */
public class MethodDefinition implements Visitable {
    private String name;
    private DataType returnType;
    private ParameterList parameters;
    private CompoundStatement body;

    public MethodDefinition(DataType returnType, String name, ParameterList parameters,
        CompoundStatement body) {

        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public ParameterList getParameters() {
        return parameters;
    }

    public DataType getReturnType() {
        return returnType;
    }

    public CompoundStatement getBody() {
        return body;
    }

    public void setBody(CompoundStatement body) {
        this.body = body;
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        parameters.accept(v);
        v.preBodyVisit(this);
        body.accept(v);
        v.postVisit(this);
    }

}
