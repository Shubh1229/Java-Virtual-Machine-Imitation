import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.Test;

import ast.Parser;
import ast.Tokenizer;
import ast.model.AssignStatement;
import ast.model.BinaryExpression;
import ast.model.ConstantExpression;
import ast.model.Expression;
import ast.model.ExpressionStatement;
import ast.model.IfStatement;
import ast.model.MethodDefinition;
import ast.model.ReturnStatement;
import ast.model.Statement;
import ast.model.VariableExpression;
import ast.model.WhileStatement;

public class Part03Test extends TestBase {

  @Test
  public void testTokenizer() {
    Tokenizer t = new Tokenizer("for(int x = 0; x<2; x=x + 1)");
    assertIterableEquals(
      List.of("for", "(", "int", "x", "=", "0", ";", "x", "<", "2", ";", "x", "=", "x", "+", "1", ")"),
      t.getTokens());
    t = new Tokenizer("for ( int x = 0; x <2; x= x + 1)");
    assertIterableEquals(
      List.of("for", "(", "int", "x", "=", "0", ";", "x", "<", "2", ";", "x", "=", "x", "+", "1", ")"),
      t.getTokens());
    t = new Tokenizer("(x==(y<=2))");
    assertIterableEquals(List.of("(", "x", "==", "(", "y", "<=", "2", ")", ")"), t.getTokens());
    t = new Tokenizer("(x == 3)");
    assertIterableEquals(List.of("(", "x", "==", "3", ")"), t.getTokens());
  }

  @Test
  public void testParseBasicExpressions() {
    /** #score(5) */
    hintContext = "parsing constant and variable expressions";

    Parser p = new Parser("3");
    Expression e = p.parseExpression();
    assertTrue(e instanceof ConstantExpression);
    TestUtil.testPrintAST(e, "3");

    p = new Parser("x");
    e = p.parseExpression();
    assertTrue(e instanceof VariableExpression);
    TestUtil.testPrintAST(e, "x");
  }

  @Test
  public void testParseSimpleExpressions() {
    /** #score(5) */
    hintContext = "parsing simple expressions";

    Parser p = new Parser("(x+7)");
    Expression e = p.parseExpression();
    assertTrue(e instanceof BinaryExpression);
    TestUtil.testPrintAST(e, "(x + 7)");

    p = new Parser("(x-10)");
    e = p.parseExpression();
    assertTrue(e instanceof BinaryExpression);
    TestUtil.testPrintAST(e, "(x - 10)");

    p = new Parser("( x *23)");
    e = p.parseExpression();
    assertTrue(e instanceof BinaryExpression);
    TestUtil.testPrintAST(e, "(x * 23)");

    p = new Parser("(x/ 2 )");
    e = p.parseExpression();
    assertTrue(e instanceof BinaryExpression);
    TestUtil.testPrintAST(e, "(x / 2)");

    p = new Parser("(x == 3)");
    e = p.parseExpression();
    assertTrue(e instanceof BinaryExpression);
    TestUtil.testPrintAST(e, "(x == 3)");

    p = new Parser("(x!= 3)");
    e = p.parseExpression();
    assertTrue(e instanceof BinaryExpression);
    TestUtil.testPrintAST(e, "(x != 3)");

    p = new Parser("(x <=3)");
    e = p.parseExpression();
    assertTrue(e instanceof BinaryExpression);
    TestUtil.testPrintAST(e, "(x <= 3)");

    p = new Parser("(x>=3)");
    e = p.parseExpression();
    assertTrue(e instanceof BinaryExpression);
    TestUtil.testPrintAST(e, "(x >= 3)");

    p = new Parser("(x>3)");
    e = p.parseExpression();
    assertTrue(e instanceof BinaryExpression);
    TestUtil.testPrintAST(e, "(x > 3)");

    p = new Parser("(x< 3)");
    e = p.parseExpression();
    assertTrue(e instanceof BinaryExpression);
    TestUtil.testPrintAST(e, "(x < 3)");
  }

  @Test
  public void testParseAssignStatement() {
    /** #score(4) */
    hintContext = "parsing assign statement";

    Parser p = new Parser("x = (y +z);");
    AssignStatement s = (AssignStatement) p.parseExpressionStatement();
    TestUtil.testPrintAST(s, "x = (y + z);\n");

    p = new Parser("x = (y +(3*z));");
    s = (AssignStatement) p.parseExpressionStatement();
    TestUtil.testPrintAST(s, "x = (y + (3 * z));\n");
  }

  @Test
  public void testParseWhileStatement() {
    /** #score(4) */
    hintContext = "parsing while statement";

    Parser p = new Parser("""
      while (x < 3) {
      x = (x + 2);
      }
      """);
    WhileStatement s = p.parseWhileStatement();
    TestUtil.testPrintAST(s, """
      while (x < 3) {
        x = (x + 2);
      }
      """);

    p = new Parser("""
      while (x < 3) {
        x = (x + 2);
        while (y < 1) {
          y = (x + 1);
        }
      }
      """);
    s = p.parseWhileStatement();
    TestUtil.testPrintAST(s, """
      while (x < 3) {
        x = (x + 2);
        while (y < 1) {
          y = (x + 1);
        }
      }
      """);
  }

  @Test
  public void testParseIfStatement() {
    /** #score(4) */
    hintContext = "parsing if statement";

    Parser p = new Parser("""
      if (x < 3) {
        x = (x + 2);
      }
      """);
    IfStatement s = p.parseIfStatement();
    TestUtil.testPrintAST(s, """
      if (x < 3) {
        x = (x + 2);
      }
      """);

    p = new Parser("""
      if (x < 3) {
        x = (x + 2);
      } else {
        x = (x * 7);
      }
      """);
    s = p.parseIfStatement();
    TestUtil.testPrintAST(s, """
      if (x < 3) {
        x = (x + 2);
      } else {
        x = (x * 7);
      }
      """);
  }

  @Test
  public void testParseReturnStatement() {
    /** #score(4) */
    ReturnStatement stmt = new Parser("return;").parseReturnStatement();
    TestUtil.testPrintAST(stmt, "return;\n");

    stmt = new Parser("return 3;").parseReturnStatement();
    TestUtil.testPrintAST(stmt, "return 3;\n");
  }

  @Test
  public void testNestingStatements() {
    /** #score(3) */
    hintContext = "parsing nested statements";

    Parser p = new Parser("""
      while (x < 3) {
        if (x < 3) {
          x = (x + 2);
        } else {
          while (y < 1) {
            y = (x + 1);
          }
          x = (x * 7);
        }
      }
      """);
    Statement s = p.parseSimpleStatement();
    TestUtil.testPrintAST(s, """
      while (x < 3) {
        if (x < 3) {
          x = (x + 2);
        } else {
          while (y < 1) {
            y = (x + 1);
          }
          x = (x * 7);
        }
      }
      """);
  }

  @Test
  public void testParseFunctionCalls() {
    /** #score(3) */
    hintContext = "parsing function calls";

    Parser p = new Parser(
      "foo();\n");
    ExpressionStatement stmt = (ExpressionStatement) p.parseSimpleStatement();
    TestUtil.testPrintAST(stmt,
      "foo();\n");

    p = new Parser(
      "foo(3);\n");
    stmt = (ExpressionStatement) p.parseSimpleStatement();
    TestUtil.testPrintAST(stmt,
      "foo(3);\n");

    p = new Parser(
      "foo(3, x);\n");
    stmt = (ExpressionStatement) p.parseSimpleStatement();
    TestUtil.testPrintAST(stmt,
      "foo(3, x);\n");

    p = new Parser(
      "foo((x + 3), bar(y, z));\n");
    stmt = (ExpressionStatement) p.parseSimpleStatement();
    TestUtil.testPrintAST(stmt,
      "foo((x + 3), bar(y, z));\n");
  }

  @Test
  public void testParseMethod() {
    /** #score(3) */
    hintContext = "parsing methods";

    Parser p = new Parser("""
      void foo() {
        x = (x + 1);
      }
      """);
    MethodDefinition m = p.parseMethod();
    TestUtil.testPrintAST(m, """
      void foo() {
        x = (x + 1);
      }
      """);

    p = new Parser("""
      int foo(int x, int y, int z, int a) {
        x = (x + 1);
        y = (y * 3);
        return (x + y);
      }
      """);
    m = p.parseMethod();
    TestUtil.testPrintAST(m, """
      int foo(int x, int y, int z, int a) {
        x = (x + 1);
        y = (y * 3);
        return (x + y);
      }
      """);

    p = new Parser("""
      int max(int x, int y) {
        if (x > y) {
          return x;
        } else {
          return y;
        }
      }
      """);
    m = p.parseMethod();
    TestUtil.testPrintAST(m, """
      int max(int x, int y) {
        if (x > y) {
          return x;
        } else {
          return y;
        }
      }
      """);
  }
}
