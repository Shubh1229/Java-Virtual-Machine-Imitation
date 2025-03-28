package ast.visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import ast.model.ArraySelectorExpression;
import ast.model.AssignStatement;
import ast.model.BinaryExpression;
import ast.model.ClassDefinition;
import ast.model.ConstantExpression;
import ast.model.FieldAccessExpression;
import ast.model.FieldDefinition;
import ast.model.IfStatement;
import ast.model.MethodCallExpression;
import ast.model.MethodDefinition;
import ast.model.NewArrayExpression;
import ast.model.NewObjectExpression;
import ast.model.NullExpression;
import ast.model.ReturnStatement;
import ast.model.VariableExpression;
import ast.model.WhileStatement;
import engine.CompiledClassCache;
import engine.opcodes.BinaryOp;
import engine.opcodes.BranchOp;
import engine.opcodes.BranchOp.Type;
import engine.opcodes.CallOp;
import engine.opcodes.LoadArrayElementOp;
import engine.opcodes.LoadConstOp;
import engine.opcodes.LoadFieldOp;
import engine.opcodes.LoadLocalOp;
import engine.opcodes.NewArrayOp;
import engine.opcodes.NewObjectOp;
import engine.opcodes.Opcode;
import engine.opcodes.Operator;
import engine.opcodes.ReturnOp;
import types.Clazz;
import types.Field;
import types.Method;

/**
 * Visitor that generates opcodes from the AST.
 */
public class CompileVisitor extends AbstractVisitor {
    /** The code we've generated so far */
    private ArrayList<Opcode> code = new ArrayList<>();
    /** Tracks info on while loops we're currently inside of */
    private Stack<WhileLabelInfo> whileStack = new Stack<>();
    /** Tracks info on ifs we're currently inside of */
    private Stack<IfLabelInfo> ifStack = new Stack<>();
    /** The class we're currently compiling */
    private Clazz currentClass;

    public CompileVisitor() {
        // If we don't see the beginning of a class definition, we'll put methods in the
        // class Main.
        currentClass = new Clazz("Main");
        CompiledClassCache.instance().saveClass(currentClass);
    }

    @Override
    public void visit(ConstantExpression expr) {
        code.add(new LoadConstOp(expr.getValue()));
    }

    @Override
    public void visit(VariableExpression expr) {
        code.add(new LoadLocalOp(expr.getName()));
    }

    @Override
    public void visit(NullExpression expr) {
        code.add(new LoadConstOp(0));
    }

    @Override
    public void postVisit(BinaryExpression expr) {
        Operator oper = expr.getOperator();
        code.add(new BinaryOp(oper));
    }

    @Override
    public void postVisit(MethodCallExpression expr) {
        // If we're not calling the method on a specific target, we're calling it on
        // this object.
        if (expr.getTarget() == null) {
            code.add(new LoadLocalOp("this"));
        }
        code.add(new CallOp(expr.getMethodName()));
    }

    @Override
    public void postTargetVisit(FieldAccessExpression expr) {
        code.add(new LoadFieldOp(expr.getFieldName()));
    }

    @Override
    public void postVisit(ArraySelectorExpression expr) {
        code.add(new LoadArrayElementOp());
    }

    @Override
    public void postVisit(NewObjectExpression expr) {
        code.add(new NewObjectOp(expr.getClassName()));
        code.add(new CallOp("init"));
    }

    @Override
    public void postVisit(NewArrayExpression expr) {
        code.add(new NewArrayOp(expr.getType(), expr.getNumDimensions()));
    }

    @Override
    public void postVisit(AssignStatement stmt) {
        // When we reach the end of the assign, we've generated opcodes to load the
        // expression on the left side. Convert the last opcode to a store instead of a
        // load.
        int lastOpIndex = code.size() - 1;
        code.set(lastOpIndex, code.get(lastOpIndex).convertToStore());
    }

    @Override
    public void preVisit(WhileStatement stmt) {
        // Store the startIndex of this while.
        WhileLabelInfo info = new WhileLabelInfo();
        info.startIndex = code.size();
        whileStack.push(info);
    }

    @Override
    public void preBodyVisit(WhileStatement stmt) {
        // Store the index of the branch to the end of the while.
        whileStack.peek().branchToEndIndex = code.size();
        code.add(null);
    }

    @Override
    public void postVisit(WhileStatement stmt) {
        WhileLabelInfo info = whileStack.pop();
        // Branch back to the beginning of the loop.
        code.add(new BranchOp(Type.UNCONDITIONAL, info.startIndex));
        // Update the condition branch to exit the loop.
        code.set(info.branchToEndIndex, new BranchOp(Type.FALSE, code.size()));
    }

    @Override
    public void postVisit(ReturnStatement stmt) {
        code.add(new ReturnOp());
    }

    @Override
    public void preThenVisit(IfStatement stmt) {
        IfLabelInfo info = new IfLabelInfo();
        // The instruction before the then block will be the branch for the condition.
        // Store its index.
        info.branchAfterConditionIndex = code.size();
        ifStack.push(info);
        code.add(null);
    }

    @Override
    public void preElseVisit(IfStatement stmt) {
        IfLabelInfo info = ifStack.peek();
        // The instructino before the else block is the branch to the end of the if.
        // Store its index.
        info.branchAfterThenIndex = code.size();
        code.add(null);
        code.set(info.branchAfterConditionIndex, new BranchOp(Type.FALSE, code.size()));
    }

    @Override
    public void postVisit(IfStatement stmt) {
        IfLabelInfo info = ifStack.pop();
        if (stmt.getElseBlock() == null) {
            // If we have an else, jump to it when the codition is false...
            code.set(info.branchAfterConditionIndex, new BranchOp(Type.FALSE, code.size()));
        } else {
            // ...otherwise, jump to the end of the if.
            code.set(info.branchAfterThenIndex, new BranchOp(Type.UNCONDITIONAL, code.size()));
        }
    }

    @Override
    public void postVisit(MethodDefinition method) {
        // If we're compiling a constructor, we need to return this on top of the stack.
        if (method.getName().equals("init")) {
            code.add(new LoadLocalOp("this"));
        }

        // If the method didn't end with a return op, add one.
        if (code.isEmpty() || !(code.get(code.size() - 1) instanceof ReturnOp)) {
            code.add(new ReturnOp());
        }

        // Save the compiled method.
        Method compiledMethod = new Method(method.getName(), method.getParameters(), code);
        currentClass.addMethod(compiledMethod);
        code = new ArrayList<>();
    }

    @Override
    public void visit(FieldDefinition field) {
        // We don't generate code for fields, but we track them for type checking.
        currentClass.addField(new Field(field.getName(), field.getType()));
    }

    @Override
    public void preVisit(ClassDefinition clazz) {
        currentClass = new Clazz(clazz.getName());
        CompiledClassCache.instance().saveClass(currentClass);
    }

    @Override
    public void postVisit(ClassDefinition clazz) {
        // Generate the default constructor if there wasn't one.
        Method ctor = currentClass.getMethod("init");
        if (ctor == null) {
            currentClass.addMethod(new Method(
                "init",
                Collections.emptyList(),
                List.of(
                    new LoadLocalOp("this"),
                    new ReturnOp())));
        }
    }

    public List<Opcode> getOpcodes() {
        return code;
    }

    private static class WhileLabelInfo {
        int startIndex;
        int branchToEndIndex;
    }

    private static class IfLabelInfo {
        int branchAfterConditionIndex;
        int branchAfterThenIndex;
    }
}
