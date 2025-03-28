package ast.visitors;

import java.util.Stack;

import ast.model.ArgumentList;
import ast.model.ArraySelectorExpression;
import ast.model.AssignStatement;
import ast.model.BinaryExpression;
import ast.model.ClassDefinition;
import ast.model.CompoundStatement;
import ast.model.ConstantExpression;
import ast.model.ExpressionStatement;
import ast.model.FieldAccessExpression;
import ast.model.FieldDefinition;
import ast.model.IfStatement;
import ast.model.MethodCallExpression;
import ast.model.MethodDefinition;
import ast.model.NewArrayExpression;
import ast.model.NewObjectExpression;
import ast.model.NullExpression;
import ast.model.ParameterDefinition;
import ast.model.ParameterList;
import ast.model.ReturnStatement;
import ast.model.VariableExpression;
import ast.model.WhileStatement;

/**
 * Visitor that turns an AST back into code.
 */
public class PrintVisitor extends AbstractVisitor {
    /** The code we're building */
    private StringBuilder sb = new StringBuilder(500);
    /** The current level of indentation */
    private StringBuilder indent = new StringBuilder();

    /** The starting offsets of code snippets we're tracking */
    private Stack<Integer> snippetStarts = new Stack<>();
    /** Code snippets of the right side of assign statements */
    private Stack<String> assignSnips = new Stack<>();
    /** Code snippets of method arguments */
    private Stack<String> methodArgumentSnips = new Stack<>();
    /** Code snippets of array selectors */
    private Stack<String> arraySelectorSnips = new Stack<>();

    @Override
    public void visit(ConstantExpression expr) {
        sb.append(expr.getValue());
    }

    @Override
    public void visit(VariableExpression expr) {
        sb.append(expr.getName());
    }

    @Override
    public void visit(NullExpression expr) {
        sb.append("null");
    }

    @Override
    public void preVisit(BinaryExpression expr) {
        sb.append('(');
    }

    @Override
    public void visit(BinaryExpression expr) {
        sb.append(' ').append(expr.getOperator()).append(' ');
    }

    @Override
    public void postVisit(BinaryExpression expr) {
        sb.append(')');
    }

    @Override
    public void preVisit(AssignStatement stmt) {
        // We visit the right side of the statement first, so start a snip to capture
        // it.
        startSnippet();
    }

    @Override
    public void visit(AssignStatement stmt) {
        // Store the snip of the right side.
        assignSnips.push(endSnippet());
        sb.append(indent);
    }

    @Override
    public void postVisit(AssignStatement stmt) {
        // Use the snip of the right side.
        sb.append(" = ").append(assignSnips.pop()).append(";\n");
    }

    @Override
    public void preVisit(WhileStatement stmt) {
        sb.append(indent);
        sb.append("while ");
    }

    @Override
    public void preBodyVisit(WhileStatement stmt) {
    }

    @Override
    public void postVisit(WhileStatement stmt) {
        sb.append("\n");
    }

    @Override
    public void preVisit(ArgumentList args) {
        sb.append('(');
    }

    @Override
    public void betweenArgumentVisit(ArgumentList args) {
        sb.append(", ");
    }

    @Override
    public void postVisit(ArgumentList args) {
        sb.append(')');
    }

    @Override
    public void preVisit(MethodCallExpression expr) {
        // We visit the arguments before the target of the call, so start a snip to
        // capture them.
        startSnippet();
    }

    @Override
    public void preTargetVisit(MethodCallExpression expr) {
        // Capture the arguments.
        methodArgumentSnips.push(endSnippet());
    }

    @Override
    public void postVisit(MethodCallExpression expr) {
        if (expr.getTarget() != null) {
            sb.append('.');
        }
        // Use the captured arguments.
        sb.append(expr.getMethodName()).append(methodArgumentSnips.pop());
    }

    @Override
    public void postTargetVisit(FieldAccessExpression expr) {
        sb.append('.').append(expr.getFieldName());
    }

    @Override
    public void preVisit(ArraySelectorExpression expr) {
        // We visit the array indices before the target, so start a snip to capture
        // them.
        startSnippet();
    }

    @Override
    public void postIndexVisit(ArraySelectorExpression expr) {
        // Capture the indices.
        arraySelectorSnips.push(endSnippet());
    }

    @Override
    public void postVisit(ArraySelectorExpression expr) {
        // Use the captured indices.
        sb.append('[').append(arraySelectorSnips.pop()).append(']');
    }

    @Override
    public void preVisit(NewObjectExpression expr) {
        sb.append("new ").append(expr.getClassName());
    }

    @Override
    public void preVisit(NewArrayExpression expr) {
        sb.append("new ").append(expr.getType()).append('[');
    }

    @Override
    public void betweenDimVisit(NewArrayExpression expr) {
        sb.append("][");
    }

    @Override
    public void postVisit(NewArrayExpression expr) {
        sb.append(']');
    }

    @Override
    public void preVisit(ReturnStatement stmt) {
        sb.append(indent).append("return");
        if (stmt.getReturnValue() != null) {
            sb.append(' ');
        }
    }

    @Override
    public void postVisit(ReturnStatement stmt) {
        sb.append(";\n");
    }

    @Override
    public void preVisit(IfStatement stmt) {
        sb.append(indent);
        sb.append("if ");
    }

    @Override
    public void preThenVisit(IfStatement stmt) {
    }

    @Override
    public void preElseVisit(IfStatement stmt) {
        sb.append(" else");
    }

    @Override
    public void postVisit(IfStatement stmt) {
        sb.append("\n");
    }

    @Override
    public void postVisit(ExpressionStatement stmt) {
        sb.append(";\n");
    }

    @Override
    public void preVisit(CompoundStatement stmt) {
        sb.append(" {\n");
        indent.append("  ");
    }

    @Override
    public void postVisit(CompoundStatement stmt) {
        indent.delete(indent.length() - 2, indent.length());
        sb.append(indent).append("}");
    }

    @Override
    public void visit(ParameterDefinition param) {
        sb.append(param.getType()).append(' ').append(param.getName());
    }

    @Override
    public void preVisit(ParameterList params) {
        sb.append('(');
    }

    @Override
    public void betweenParameterVisit(ParameterList params) {
        sb.append(", ");
    }

    @Override
    public void postVisit(ParameterList params) {
        sb.append(')');
    }

    @Override
    public void visit(FieldDefinition field) {
        sb.append(indent).append("field ").append(field.getType()).append(' ').append(field.getName()).append(";\n");
    }

    @Override
    public void preVisit(MethodDefinition method) {
        sb.append(indent).append(method.getReturnType()).append(' ').append(method.getName());
    }

    @Override
    public void postVisit(MethodDefinition method) {
        sb.append("\n");
    }

    @Override
    public void preVisit(ClassDefinition clazz) {
        sb.append("class ").append(clazz.getName()).append(" {\n");
        indent.append("  ");
    }

    @Override
    public void postVisit(ClassDefinition clazz) {
        indent.delete(indent.length() - 2, indent.length());
        sb.append("}\n");
    }

    /**
     * Start capturing a snippet.
     */
    private void startSnippet() {
        snippetStarts.push(sb.length());
    }

    /**
     * Finish capturing a snippet and return it.
     * 
     * @return the snippet
     */
    private String endSnippet() {
        int start = snippetStarts.pop();
        String snippet = sb.substring(start, sb.length());
        sb.delete(start, sb.length());
        return snippet;
    }

    public String getResult() {
        return sb.toString();
    }
}
