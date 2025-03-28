package ast.model;

import java.util.List;

import ast.visitors.Visitor;

/**
 * Represents a compound statement.
 */
public class CompoundStatement extends Statement {
    private List<Statement> body;

    public CompoundStatement(List<Statement> body) {
        this.body = body;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public void accept(Visitor v) {
        v.preVisit(this);
        for (Statement stmt : body) {
            stmt.accept(v);
        }
        v.postVisit(this);
    }

}
