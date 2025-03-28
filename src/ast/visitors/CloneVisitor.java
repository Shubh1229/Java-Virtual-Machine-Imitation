package ast.visitors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import ast.model.ArgumentList;
import ast.model.ArraySelectorExpression;
import ast.model.AssignStatement;
import ast.model.BinaryExpression;
import ast.model.CompoundStatement;
import ast.model.ConstantExpression;
import ast.model.Expression;
import ast.model.ExpressionStatement;
import ast.model.FieldAccessExpression;
import ast.model.IfStatement;
import ast.model.MethodCallExpression;
import ast.model.MethodDefinition;
import ast.model.NewArrayExpression;
import ast.model.NewObjectExpression;
import ast.model.ReturnStatement;
import ast.model.Statement;
import ast.model.VariableExpression;
import ast.model.WhileStatement;

public class CloneVisitor extends AbstractVisitor {
    // Holds expressions that haven't been used by their parents yet.
    protected Stack<Expression> exprStack = new Stack<>();
    // Holds an ArgumentList until its parent uses it.
    protected ArgumentList argList;
    // Holds statements until their parent uses them.
    protected List<Statement> statements = new ArrayList<>();
    // Handles nesting of compound statements.
    protected Stack<List<Statement>> statementLists = new Stack<>();

    @Override
    public void visit(ConstantExpression expr) {
        exprStack.push(expr);
    }

    @Override
    public void visit(VariableExpression expr) {
        exprStack.push(expr);
    }

    @Override
    public void postVisit(BinaryExpression expr) {
        Expression right = exprStack.pop();
        Expression left = exprStack.pop();
        exprStack.push(new BinaryExpression(left, expr.getOperator(), right));
    }

    @Override
    public void postVisit(ArgumentList args) {
        LinkedList<Expression> arguments = new LinkedList<>();
        for (int i = 0; i < args.size(); i++) {
            arguments.addFirst(exprStack.pop());
        }
        argList = new ArgumentList(arguments);
    }

    @Override
    public void postVisit(MethodCallExpression expr) {
        Expression targetExpression = null;
        if (expr.getTarget() != null) {
            targetExpression = exprStack.pop();
        }
        exprStack.push(new MethodCallExpression(expr.getMethodName(), targetExpression, argList));
    }

    @Override
    public void postTargetVisit(FieldAccessExpression expr) {
        Expression targetExpression = exprStack.pop();
        exprStack.push(new FieldAccessExpression(expr.getFieldName(), targetExpression));
    }

    @Override
    public void postVisit(ArraySelectorExpression expr) {
        Expression targetExpression = exprStack.pop();
        Expression indexExpression = exprStack.pop();
        exprStack.push(new ArraySelectorExpression(targetExpression, indexExpression));
    }

    @Override
    public void postVisit(NewObjectExpression expr) {
        exprStack.push(new NewObjectExpression(expr.getClassName(), argList));
    }

    @Override
    public void postVisit(NewArrayExpression expr) {
        List<Expression> dims = new LinkedList<>();
        for (int i = 0; i < expr.getNumDimensions(); i++) {
            dims.add(0,exprStack.pop());
        }
        exprStack.push(new NewArrayExpression(expr.getType(), dims));
    }

    @Override
    public void postVisit(AssignStatement stmt) {
        Expression lValue = exprStack.pop();
        Expression value = exprStack.pop();
        statements.add(new AssignStatement(lValue, value));
    }

    @Override
    public void postVisit(WhileStatement stmt) {
        Expression condition = exprStack.pop();
        Statement body = statements.remove(statements.size()-1);
        statements.add(new WhileStatement(condition, body));
    }

    @Override
    public void postVisit(IfStatement stmt) {
        Statement elseBlock = null;
        if (stmt.getElseBlock() != null) {
            elseBlock = statements.remove(statements.size()-1);
        }
        Statement thenBlock = statements.remove(statements.size()-1);
        Expression condition = exprStack.pop();
        statements.add(new IfStatement(condition, thenBlock, elseBlock));
    }

    @Override
    public void postVisit(ReturnStatement stmt) {
        Expression returnValue = null;
        if (stmt.getReturnValue() != null) {
            returnValue = exprStack.pop();
        }
        statements.add(new ReturnStatement(returnValue));
    }

    @Override
    public void postVisit(ExpressionStatement stmt) {
        statements.add(new ExpressionStatement(exprStack.pop()));
    }

    @Override
    public void preVisit(CompoundStatement stmt) {
        //if(!statements.isEmpty()){
            statementLists.push(statements);
            statements = new ArrayList<>();
        //}
    }

    @Override
    public void postVisit(CompoundStatement stmt) {
        List<Statement> statementList = statements;
        statements = statementLists.pop();
        statements.add(new CompoundStatement(statementList));
    }

    @Override
    public void postVisit(MethodDefinition method) {
        method.setBody((CompoundStatement) statements.remove(statements.size()-1));
    }
}
