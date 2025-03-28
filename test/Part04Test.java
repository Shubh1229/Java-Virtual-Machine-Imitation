import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ast.Parser;
import ast.model.AssignStatement;
import ast.model.BinaryExpression;
import ast.model.CompoundStatement;
import ast.model.ConstantExpression;
import ast.model.Expression;
import ast.model.ExpressionStatement;
import ast.model.IfStatement;
import ast.model.MethodDefinition;
import ast.model.ReturnStatement;
import ast.model.Statement;
import ast.model.VariableExpression;
import ast.model.WhileStatement;
import ast.visitors.CompileVisitor;

/**
 * Base class for tests.
 *
 * @author Tim
 * @version Feb 2, 2008
 */
public class Part04Test {

  /** Set up tests. */
  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testParseExpressions() {
    /** #score(2) */
    Parser p = new Parser("3");
    Expression e = p.parseExpression();
    assertTrue(e instanceof ConstantExpression);
    TestUtil.testPrintAST(e, "3");

    p = new Parser("x");
    e = p.parseExpression();
    assertTrue(e instanceof VariableExpression);
    TestUtil.testPrintAST(e, "x");

    p = new Parser("(x+7)");
    e = p.parseExpression();
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
    /** #score(2) */
    Parser p = new Parser("x = (y +z);");
    AssignStatement s = (AssignStatement) p.parseExpressionStatement();
    TestUtil.testPrintAST(s, "x = (y + z);\n");

    p = new Parser("x = (y +(3*z));");
    s = (AssignStatement) p.parseExpressionStatement();
    TestUtil.testPrintAST(s, "x = (y + (3 * z));\n");
  }

  @Test
  public void testParseWhileStatement() {
    /** #score(2) */
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
    /** #score(2) */
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
    /** #score(2) */
    ReturnStatement stmt = new Parser("return;").parseReturnStatement();
    TestUtil.testPrintAST(stmt, "return;\n");

    stmt = new Parser("return 3;").parseReturnStatement();
    TestUtil.testPrintAST(stmt, "return 3;\n");
  }

  @Test
  public void testParseNestedStatements() {
    /** #score(2) */
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
    /** #score(2) */
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
    /** #score(2) */
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

  /** Test simple expressions. */
  @Test
  public void testCompileSimpleExpressions() {
    /** #score(3) */
    ConstantExpression constEx = new ConstantExpression(5);
    TestUtil.testCompileAST(
      constEx,
      "0: load_const 5\n");

    VariableExpression varEx = new VariableExpression("x");
    TestUtil.testCompileAST(
      varEx,
      "0: load_local x\n");

    BinaryExpression simpleBinaryEx = (BinaryExpression) new Parser("(x + 5)").parseExpression();
    TestUtil.testCompileAST(simpleBinaryEx, """
      load_local x
      load_const 5
      add
      """);
  }

  /** Test complex expression. */
  @Test
  public void testCompileComplexExpression() {
    /** #score(2) */
    BinaryExpression complexEx = (BinaryExpression) new Parser("(((x + 5) / 3) + (3 * (y - 4)))").parseExpression();
    TestUtil.testCompileAST(complexEx, """
      load_local x
      load_const 5
      add
      load_const 3
      div
      load_const 3
      load_local y
      load_const 4
      sub
      mul
      add
      """);
  }

  /** Test assign. */
  @Test
  public void testCompileAssignStatement() {
    /** #score(3) */
    AssignStatement assignStmt = (AssignStatement) new Parser("x = (x + 5);").parseExpressionStatement();
    TestUtil.testCompileAST(assignStmt, """
      load_local x
      load_const 5
      add
      store_local x
      """);
  }

  /** Test compound. */
  @Test
  public void testCompileCompoundStatement() {
    /** #score(2) */
    CompoundStatement compStmt = new Parser("""
      {
        x = (x + 1);
        x = (x + 5);
      }
      """).parseCompoundStatement();
    TestUtil.testCompileAST(compStmt, """
      load_local x
      load_const 1
      add
      store_local x
      load_local x
      load_const 5
      add
      store_local x
      """);
  }

  @Test
  public void testCompileFunctionCalls() {
    /** #score(3) */
    Statement s = new Parser(
      "foo();\n").parseSimpleStatement();
    TestUtil.testCompileAST(s, """
      load_local this
      call foo
      """);

    s = new Parser("foo(3);\n").parseSimpleStatement();
    TestUtil.testCompileAST(s, """
      load_const 3
      load_local this
      call foo
      """);

    s = new Parser("z = foo(x, y);\n").parseSimpleStatement();
    TestUtil.testCompileAST(s, """
      load_local x
      load_local y
      load_local this
      call foo
      store_local z
      """);

    s = new Parser("res = foo((x + 3), bar(y, z));\n").parseSimpleStatement();
    TestUtil.testCompileAST(s, """
      load_local x
      load_const 3
      add
      load_local y
      load_local z
      load_local this
      call bar
      load_local this
      call foo
      store_local res
      """);
  }

  /** Test while. */
  @Test
  public void testCompileWhileStatement() {
    /** #score(4) */
    WhileStatement whileStmt = new Parser("""
      while (x < 10) {
        x = (x + 1);
        x = (x + 5);
      }
      """).parseWhileStatement();
    TestUtil.testCompileAST(whileStmt, """
      0: load_local x
      1: load_const 10
      2: cmpLT
      3: branchF 13
      4: load_local x
      5: load_const 1
      6: add
      7: store_local x
      8: load_local x
      9: load_const 5
      10: add
      11: store_local x
      12: branch 0
      """);
  }

  /** Test if. */
  @Test
  public void testCompileIfStatement() {
    /** #score(4) */
    IfStatement ifStmt = new Parser("""
      if (x < 10) {
        x = (x + 1);
        x = (x + 5);
      }
      """).parseIfStatement();
    TestUtil.testCompileAST(ifStmt, """
      0: load_local x
      1: load_const 10
      2: cmpLT
      3: branchF 12
      4: load_local x
      5: load_const 1
      6: add
      7: store_local x
      8: load_local x
      9: load_const 5
      10: add
      11: store_local x
      """);

    IfStatement ifElseStmt = new Parser("""
      if (x < 10) {
        x = (x + 5);
      } else {
        x = (x + 1);
      }
      """).parseIfStatement();
    TestUtil.testCompileAST(ifElseStmt, """
      0: load_local x
      1: load_const 10
      2: cmpLT
      3: branchF 9
      4: load_local x
      5: load_const 5
      6: add
      7: store_local x
      8: branch 13
      9: load_local x
      10: load_const 1
      11: add
      12: store_local x
      """);
  }

  /** Test consecutive whiles. */
  @Test
  public void testCompileTwoWhileStatements() {
    /** #score(4) */
    CompoundStatement twoWhiles = new Parser("""
      {
        while (x < 10) {
          x = (x + 1);
          x = (x + 5);
        }
        while (x < 10) {
          x = (x + 1);
          x = (x + 5);
        }
      }
      """).parseCompoundStatement();
    TestUtil.testCompileAST(twoWhiles, """
      0: load_local x
      1: load_const 10
      2: cmpLT
      3: branchF 13
      4: load_local x
      5: load_const 1
      6: add
      7: store_local x
      8: load_local x
      9: load_const 5
      10: add
      11: store_local x
      12: branch 0
      13: load_local x
      14: load_const 10
      15: cmpLT
      16: branchF 26
      17: load_local x
      18: load_const 1
      19: add
      20: store_local x
      21: load_local x
      22: load_const 5
      23: add
      24: store_local x
      25: branch 13
      """);
  }

  /** Test nesting. */
  @Test
  public void testCompileNesting() {
    /** #score(4) */
    CompoundStatement nestedStmts = new Parser("""
      {
        while (x<10)
        {
          x = (x+1);
          if (x<10)
          {
            while (x<10)
            {
              if (x<10)
              {
                x = (x+1);
              } else {
                x = (x+5);
              }
            }
          }
        }
        while (x<10)
        {
          x = (x+1);
          x = (x+5);
        }
        if (x<10)
        {
          x = (x+1);
          x = (x+5);
        }
      }
      """).parseCompoundStatement();
    TestUtil.testCompileAST(nestedStmts, """
      0: load_local x
      1: load_const 10
      2: cmpLT
      3: branchF 31
      4: load_local x
      5: load_const 1
      6: add
      7: store_local x
      8: load_local x
      9: load_const 10
      10: cmpLT
      11: branchF 30
      12: load_local x
      13: load_const 10
      14: cmpLT
      15: branchF 30
      16: load_local x
      17: load_const 10
      18: cmpLT
      19: branchF 25
      20: load_local x
      21: load_const 1
      22: add
      23: store_local x
      24: branch 29
      25: load_local x
      26: load_const 5
      27: add
      28: store_local x
      29: branch 12
      30: branch 0
      31: load_local x
      32: load_const 10
      33: cmpLT
      34: branchF 44
      35: load_local x
      36: load_const 1
      37: add
      38: store_local x
      39: load_local x
      40: load_const 5
      41: add
      42: store_local x
      43: branch 31
      44: load_local x
      45: load_const 10
      46: cmpLT
      47: branchF 56
      48: load_local x
      49: load_const 1
      50: add
      51: store_local x
      52: load_local x
      53: load_const 5
      54: add
      55: store_local x
      """);
  }

  @Test
  public void testCompileMethod() {
    /** #score(5) */
    MethodDefinition m = new Parser("""
      void foo() {
      }
      """).parseMethod();
    CompileVisitor v = new CompileVisitor();
    m.accept(v);
    TestUtil.checkMethod("Main", "foo",
      "return\n");

    m = new Parser("""
      int add(int x, int y) {
        return (x + y);
      }
      """).parseMethod();
    v = new CompileVisitor();
    m.accept(v);
    TestUtil.checkMethod("Main", "add", """
      load_local x
      load_local y
      add
      return
      """);

    m = new Parser("""
      int max(int x, int y) {
        if (x > y) {
          return x;
        } else {
          return y;
        }
         }
      """).parseMethod();
    v = new CompileVisitor();
    m.accept(v);
    TestUtil.checkMethod("Main", "max", """
      0: load_local x
      1: load_local y
      2: cmpGT
      3: branchF 7
      4: load_local x
      5: return
      6: branch 9
      7: load_local y
      8: return
      """);

    m = new Parser("""
      void foo(int x) {
        if (x < 3) {
          return;
        } else {
          x = (x + 1);
           }
         }
      """).parseMethod();
    v = new CompileVisitor();
    m.accept(v);
    TestUtil.checkMethod("Main", "foo", """
      0: load_local x
      1: load_const 3
      2: cmpLT
      3: branchF 6
      4: return
      5: branch 10
      6: load_local x
      7: load_const 1
      8: add
      9: store_local x
      10: return
      """);
  }
}
