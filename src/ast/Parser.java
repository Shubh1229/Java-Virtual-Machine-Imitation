package ast;

import java.util.ArrayList;
import java.util.List;

import ast.model.ArgumentList;
import ast.model.ArraySelectorExpression;
import ast.model.AssignStatement;
import ast.model.BinaryExpression;
import ast.model.ClassDefinition;
import ast.model.CompoundStatement;
import ast.model.ConstantExpression;
import ast.model.Expression;
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
import ast.model.Statement;
import ast.model.VariableExpression;
import ast.model.WhileStatement;
import engine.opcodes.Operator;
import types.DataType;

public class Parser {
    private Tokenizer tokens;

    public Parser(String code) {
        tokens = new Tokenizer(code);
    }

    /**
     * Parses an Operator
     *
     * @return the Operator
     */
    public Operator parseOperator() {
        // operator ::= '+' | '-' | '*' | '/' | '%' | '==' | '!=' | '>' | '>=' | '<' |
        // '<='
        String opStr = tokens.remove();
        Operator operator = Operator.fromSymbol(opStr);
        if (operator == null) {
            throw new RuntimeException("Unknown operator: " + opStr);
        }
        return operator;
    }

    /**
     * Parses arguments, e.g. "()" or "(x, 2, y)".
     *
     * @return the ArgumentList
     */
    public ArgumentList parseArguments() {
        // arguments ::= '(' (expression (',' expression)*)? ')'
        List<Expression> args = new ArrayList<>();

        tokens.consume("(");
        if (!tokens.lookahead(")")) {
            do {
                args.add(parseExpression());

                if (tokens.lookahead(",")) {
                    tokens.consume(",");
                }
            } while (!tokens.lookahead(")"));
        }

        tokens.consume(")");
        return new ArgumentList(args);
    }

    /**
     * Parses a method call expression, e.g. "someMethod(x, y)"
     *
     * @return the MethodCallExpression
     */
    public MethodCallExpression parseMethodCallExpression(Expression targetExpression) {
        // method_call_expression ::= identifier arguments
        String name = tokens.remove();
        return new MethodCallExpression(name, targetExpression, parseArguments());
    }

    /**
     * Parses a field access selector, e.g. ".someField"
     *
     * @param target the target object expression
     * @return the FieldAccessExpression
     */
    public FieldAccessExpression parseFieldAccessSelector(Expression target) {
        // field_access_selector ::= '.' identifier
        tokens.consume(".");
        String name = tokens.remove();
        return new FieldAccessExpression(name, target);
    }

    /**
     * Parses a method call selector, e.g. ".someMethod(x, y)"
     *
     * @param target the target object expression
     * @return the MethodCallExpression
     */
    public MethodCallExpression parseMethodCallSelector(Expression target) {
        // method_call_selector ::= '.' identifier arguments
        tokens.consume(".");
        String name = tokens.remove();
        return new MethodCallExpression(name, target, parseArguments());
    }

    /**
     * Parses an array selector, e.g. "[x][2 + y]".
     *
     * @param target the target array expression
     * @return
     */
    public ArraySelectorExpression parseArraySelector(Expression target) {
        // array_selector ::= '[' expression ']'
        tokens.consume("[");
        Expression expr = parseExpression();
        tokens.consume("]");
        return new ArraySelectorExpression(target, expr);
    }

    /**
     * Parses a new expression, e.g. "new MyClass(3)" or "new int[3]".
     *
     * @return the Expression
     */
    public Expression parseNewExpression() {
        // new_expression ::= 'new' data_type (arguments | ('[' expression ']')+)
        tokens.consume("new");
        String typeName = tokens.remove();
        if (tokens.lookahead("(")) {
            return new NewObjectExpression(typeName, parseArguments());
        }

        List<Expression> arrayDims = new ArrayList<>();
        while (tokens.lookahead("[")) {
            tokens.consume("[");
            arrayDims.add(parseExpression());
            tokens.consume("]");
        }
        return new NewArrayExpression(typeName, arrayDims);
    }

    /**
     * Parses a selector.
     *
     * @param target the target of the selector
     * @return an Expression wrapping the target with the selector
     */
    public Expression parseSelector(Expression target) {
        // @formatter:off
        // selector ::=
        //   field_access_selector |
        //   method_call_selector |
        //   array_selector
        // @formatter:on
        if (tokens.lookahead(".", "*", "(")) {
            return parseMethodCallSelector(target);
        }
        if (tokens.lookahead(".")) {
            return parseFieldAccessSelector(target);
        }
        return parseArraySelector(target);
    }

    /**
     * Parses an expression.
     *
     * @return the Expression
     */
    public Expression parseExpression() {
        // @formatter:off
        // expression ::=
        //   '(' expression operator expression ')' |
        //   new_expression |
        //   method_call_expression |
        //   null_expression |
        //   variable selector* |
        //   constant
        // @formatter:on
        if (tokens.lookahead("(")) {
            tokens.consume("(");
            Expression left = parseExpression();
            Operator op = parseOperator();
            Expression right = parseExpression();
            tokens.consume(")");
            return new BinaryExpression(left, op, right);
        } else if (tokens.lookahead("new")) {
            return parseNewExpression();
        } else if (tokens.lookahead("null")) {
            tokens.consume("null");
            return new NullExpression();
        } else if (Character.isAlphabetic(tokens.peek().charAt(0))) {
            if (tokens.lookahead("*", "(")) {
                return parseMethodCallExpression(null);
            }

            Expression expr = new VariableExpression(tokens.remove());
            while (tokens.lookahead(".") || tokens.lookahead("[")) {
                expr = parseSelector(expr);
            }
            return expr;
        } else {
            return new ConstantExpression(Integer.parseInt(tokens.remove()));
        }
    }

    /**
     * Parses an expression statement.
     *
     * @return the Statement
     */
    public Statement parseExpressionStatement() {
        // expression_statement ::= (expression '=')? expression ';'
        Expression first = parseExpression();

        if (tokens.lookahead("=")) {
            tokens.consume("=");
            AssignStatement assign = new AssignStatement(first, parseExpression());
            tokens.consume(";");
            return assign;
        } else {
            tokens.consume(";");
            return new ExpressionStatement(first);
        }
    }

    /**
     * Parses an if statement.
     *
     * @return the IfStatement
     */
    public IfStatement parseIfStatement() {
        // @formatter:off
        // if_statement ::=
        // 'if' expression compound_statement ('else' compound_statement)?
        // @formatter:on
        tokens.consume("if");
        Expression condition = parseExpression();
        Statement thenBlock = parseCompoundStatement();
        Statement elseBlock = null;
        if (tokens.lookahead("else")) {
            tokens.consume("else");
            elseBlock = parseCompoundStatement();
        }
        return new IfStatement(condition, thenBlock, elseBlock);
    }

    /**
     * Parses a while statement.
     *
     * @return the WhileStatement
     */
    public WhileStatement parseWhileStatement() {
        // while_statement ::= 'while' expression compound_statement
        tokens.consume("while");
        Expression condition = parseExpression();
        Statement body = parseCompoundStatement();
        return new WhileStatement(condition, body);
    }

    /**
     * Parses a return statement.
     *
     * @return the ReturnStatement
     */
    public ReturnStatement parseReturnStatement() {
        // return_statement ::= 'return' expression? ';'
        tokens.consume("return");
        Expression returnValue = null;
        if (!tokens.lookahead(";")) {
            returnValue = parseExpression();
        }
        tokens.consume(";");
        return new ReturnStatement(returnValue);
    }

    /**
     * Parses a simple statement.
     *
     * @return the Statement
     */
    public Statement parseSimpleStatement() {
        // @formatter:off
        // simple_statement ::=
        //   if_statement |
        //   while_statement |
        //   return_statement |
        //   expression_statement
        // @formatter:on
        String next = tokens.peek();
        if ("if".equals(next)) {
            return parseIfStatement();
        } else if ("while".equals(next)) {
            return parseWhileStatement();
        } else if ("return".equals(next)) {
            return parseReturnStatement();
        } else {
            return parseExpressionStatement();
        }
    }

    /**
     * Parses a compound statement.
     *
     * @return the CompoundStatement
     */
    public CompoundStatement parseCompoundStatement() {
        // compound_statement ::= '{' simple_statement+ '}'
        tokens.consume("{");

        List<Statement> statements = new ArrayList<>();
        while (!tokens.lookahead("}")) {
            statements.add(parseSimpleStatement());
        }

        tokens.consume("}");
        return new CompoundStatement(statements);
    }

    /**
     * Parses a data type, e.g. int or MyClass or int[][].
     *
     * @return the DataType
     */
    public DataType parseDataType() {
        // data_type ::= base_type empty_brackets*
        String baseType = tokens.remove();
        int numDims = 0;
        while (tokens.lookahead("[")) {
            tokens.consume("[");
            tokens.consume("]");
            numDims++;
        }
        return new DataType(baseType, numDims);
    }

    /**
     * Parses a return type.
     * 
     * @return the DataType
     */
    public DataType parseReturnType() {
        // return_type ::= 'void' | data_type
        if (tokens.lookahead("void")) {
            tokens.consume("void");
            return DataType.VOID;
        }

        return parseDataType();
    }

    /**
     * Parses a parameter definition.
     *
     * @return the ParameterDefinition
     */
    public ParameterDefinition parseParameter() {
        // parameter ::= data_type identifier
        DataType paramType = parseDataType();
        String paramName = tokens.remove();
        return new ParameterDefinition(paramName, paramType);
    }

    /**
     * Parses a parameter list, e.g. "()" or "(int x, int y)".
     *
     * @return the ParamaterList
     */
    public ParameterList parseParameters() {
        // parameters ::= '(' (parameter (',' parameter)*)? ')'
        List<ParameterDefinition> params = new ArrayList<>();

        tokens.consume("(");
        if (!tokens.lookahead(")")) {
            do {
                params.add(parseParameter());

                if (tokens.lookahead(",")) {
                    tokens.remove();
                }
            } while (!tokens.lookahead(")"));
        }

        tokens.consume(")");
        return new ParameterList(params);
    }

    /**
     * Parses a method definition.
     *
     * @return the MethodDefinition
     */
    public MethodDefinition parseMethod() {
        // method ::= return_type identifier parameters compound_statement
        DataType returnType = parseReturnType();
        String name = tokens.remove();
        ParameterList params = parseParameters();
        CompoundStatement body = parseCompoundStatement();
        return new MethodDefinition(returnType, name, params, body);
    }

    /**
     * Parses a field definition.
     *
     * @return the FieldDefinition
     */
    public FieldDefinition parseField() {
        // field ::= 'field' data_type identifier ';'
        tokens.consume("field");
        DataType type = parseDataType();
        String name = tokens.remove();
        tokens.consume(";");
        return new FieldDefinition(type, name);
    }

    /**
     * Parses a class definition.
     *
     * @return the ClassDefinition
     */
    public ClassDefinition parseClass() {
        // class ::= 'class' identifier '{' field* method* '}'
        tokens.consume("class");
        String name = tokens.remove();
        tokens.consume("{");

        List<FieldDefinition> fields = new ArrayList<>();
        while (tokens.lookahead("field")) {
            fields.add(parseField());
        }

        List<MethodDefinition> methods = new ArrayList<>();
        while (!tokens.lookahead("}")) {
            methods.add(parseMethod());
        }

        tokens.consume("}");

        return new ClassDefinition(name, fields, methods);
    }
}
