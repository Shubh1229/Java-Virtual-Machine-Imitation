package ast.visitors;

import java.util.HashSet;
import java.util.Set;

import ast.model.AssignStatement;
import ast.model.VariableExpression;

public class WriteTrackingVisitor extends AbstractVisitor {
    Set<String> varsWritten = new HashSet<>();

    @Override
    public void postVisit(AssignStatement stmt) {
        if (stmt.getLValue() instanceof VariableExpression varExpr) {
            varsWritten.add(varExpr.getName());
        }
    }

    public Set<String> getVarsWritten() {
        return varsWritten;
    }
}
