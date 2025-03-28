import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Stack;

import org.junit.Test;

import ast.Parser;
import ast.model.Expression;
import ast.model.MethodDefinition;
import ast.model.NullExpression;
import ast.model.Statement;
import ast.visitors.PrintVisitor;
import ast.visitors.Visitable;
import engine.CompiledClassCache;
import engine.VMThread;
import engine.heap.Heap;
import engine.heap.HeapObject;
import engine.opcodes.LoadFieldOp;
import engine.opcodes.NewObjectOp;
import engine.opcodes.StoreFieldOp;
import types.Clazz;
import types.DataType;
import types.Field;
import types.Value;

public class Part06Test extends TestBase {

  @Test
  public void testParseFieldAccessSelector() {
    /** #score(2) */
    hintContext = "parse field access";
    Statement stmt = new Parser(
      "this.x = x;\n").parseSimpleStatement();

    PrintVisitor v = new PrintVisitor();
    stmt.accept(v);
    assertEquals(hint("code incorrect"),
      "this.x = x;\n",
      v.getResult());

    stmt = new Parser(
      "y = ((this.x * 3) + this.y);\n").parseSimpleStatement();

    v = new PrintVisitor();
    stmt.accept(v);
    assertEquals(hint("code incorrect"),
      "y = ((this.x * 3) + this.y);\n",
      v.getResult());
  }

  @Test
  public void testParseMethodCallSelector() {
    /** #score(2) */
    hintContext = "parse method call";
    Statement stmt = new Parser(
      "o.foo();\n").parseSimpleStatement();

    PrintVisitor v = new PrintVisitor();
    stmt.accept(v);
    assertEquals(hint("code incorrect"),
      "o.foo();\n",
      v.getResult());

    stmt = new Parser(
      "y = ((this.foo() * 3) + obj.bar(2, x));\n").parseSimpleStatement();

    v = new PrintVisitor();
    stmt.accept(v);
    assertEquals(hint("code incorrect"),
      "y = ((this.foo() * 3) + obj.bar(2, x));\n",
      v.getResult());
  }

  @Test
  public void testParseChainedSelectors() {
    /** #score(2) */
    hintContext = "parse chained selectors";
    Statement stmt = new Parser(
      "o.foo().bar(2, 3);\n").parseSimpleStatement();

    PrintVisitor v = new PrintVisitor();
    stmt.accept(v);
    assertEquals(hint("code incorrect"),
      "o.foo().bar(2, 3);\n",
      v.getResult());

    stmt = new Parser(
      "x = o.a.b.c;\n").parseSimpleStatement();

    v = new PrintVisitor();
    stmt.accept(v);
    assertEquals(hint("code incorrect"),
      "x = o.a.b.c;\n",
      v.getResult());

    stmt = new Parser(
      "y = o.foo().a.bar().b;\n").parseSimpleStatement();

    v = new PrintVisitor();
    stmt.accept(v);
    assertEquals(hint("code incorrect"),
      "y = o.foo().a.bar().b;\n",
      v.getResult());
  }

  @Test
  public void testParseNullExpression() {
    /** #score(1) */
    hintContext = "parsing null expression";
    Expression e = TestUtil.testParseExpression("null");
    assertTrue("Parsing null should return a NullExpression", e instanceof NullExpression);
    TestUtil.testParseExpression("(x == null)");
  }

  @Test
  public void testParseNewObjectExpressions() {
    /** #score(2) */
    hintContext = "parsing new object expressions";
    TestUtil.testParseExpression("new MyClass()");
    TestUtil.testParseExpression("new MyClass(x)");
    TestUtil.testParseExpression("new MyClass(x, y, (3 + 5), new OtherThing())");
  }

  @Test
  public void testParseFieldDefinition() {
    /** #score(2) */
    hintContext = "parsing field definition";
    TestUtil.testParseField("field int x;\n");
    TestUtil.testParseField("field MyClass y;\n");
  }

  @Test
  public void testParseClass() {
    /** #score(2) */
    hintContext = "parsing class";

    TestUtil.testParseClass("""
      class MyClass {
        field int x;
        void foo() {
          this.x = (this.x + 1);
        }
        int bar(int x, SomeObject y) {
          return 3;
        }
      }
      """);
  }

  @Test
  public void testHeap() {
    /** #score(2) */
    Clazz c = new Clazz("AClass");
    Heap h = new Heap();
    HeapObject o = h.createObject(c);
    assertEquals("object id incorrect (first id should be 1)", 1, o.getId());
    HeapObject o2 = h.createObject(c);
    assertEquals("object id incorrect", 2, o2.getId());
    assertSame("heap returned wrong object", h.getEntry(1), o);
    assertSame("heap returned wrong object", h.getEntry(2), o2);
    assertNull("heap should return null", h.getEntry(3));
  }

  @Test
  public void testHeapObject() {
    /** #score(2) */
    Clazz clazz = new Clazz("MyClass");
    clazz.addField(new Field("f1", DataType.INT));
    clazz.addField(new Field("f2", DataType.INT));
    HeapObject obj = new HeapObject(1, clazz);
    assertEquals("returned wrong id", 1, obj.getId());
    assertSame("returned wrong clazz", clazz, obj.getClazz());

    obj.setFieldValue("f1", new Value(1));
    obj.setFieldValue("f2", new Value(2));
    assertEquals("field value incorrect", 1, obj.getFieldValue("f1").getIntValue());
    assertEquals("field value incorrect", 2, obj.getFieldValue("f2").getIntValue());

    obj.setFieldValue("f1", new Value(3));
    assertEquals("updated field value incorrect", 3, obj.getFieldValue("f1").getIntValue());
  }

  @Test
  public void testNewObjectOp() {
    /** #score(2) */
    Heap heap = new Heap();
    Clazz myClass = new Clazz("MyClass");
    CompiledClassCache.instance().saveClass(myClass);
    Stack<Value> opStack = new Stack<>();
    NewObjectOp op = new NewObjectOp("MyClass");
    op.execute(null, heap, opStack);

    HeapObject obj = (HeapObject) heap.getEntry(1);
    assertNotNull("should create HeapObject");
    assertEquals("createdHeapObject with incorrect type", "MyClass", obj.getClazz().getName());
    assertEquals("should push object id", 1, opStack.size());
    assertEquals("pushed incorrect object id", 1, opStack.pop().getIntValue());
  }

  @Test
  public void testLoadFieldOp() {
    /** #score(2) */
    Heap heap = new Heap();
    Stack<Value> opStack = new Stack<>();

    Clazz myClass = new Clazz("MyClass");
    myClass.addField(new Field("f1", DataType.INT));
    myClass.addField(new Field("f2", DataType.INT));
    HeapObject obj = heap.createObject(myClass);
    obj.setFieldValue("f1", new Value(11));
    obj.setFieldValue("f2", new Value(12));
    HeapObject obj2 = heap.createObject(myClass);
    obj2.setFieldValue("f1", new Value(21));
    obj2.setFieldValue("f2", new Value(22));

    opStack.push(new Value(obj.getId(), myClass.getType()));
    new LoadFieldOp("f1").execute(null, heap, opStack);
    assertEquals("opStack is wrong size", 1, opStack.size());
    assertEquals("field value should be on opStack", 11, opStack.pop().getIntValue());

    opStack.push(new Value(obj2));
    new LoadFieldOp("f1").execute(null, heap, opStack);
    opStack.push(new Value(obj));
    new LoadFieldOp("f2").execute(null, heap, opStack);
    opStack.push(new Value(obj2));
    new LoadFieldOp("f2").execute(null, heap, opStack);
    assertEquals("opStack is wrong size", 3, opStack.size());
    assertEquals("field value should be on opStack", 22, opStack.pop().getIntValue());
    assertEquals("field value should be on opStack", 12, opStack.pop().getIntValue());
    assertEquals("field value should be on opStack", 21, opStack.pop().getIntValue());
  }

  @Test
  public void testStoreFieldOp() {
    /** #score(2) */
    Heap heap = new Heap();
    Stack<Value> opStack = new Stack<>();

    Clazz myClass = new Clazz("MyClass");
    myClass.addField(new Field("f1", DataType.INT));
    myClass.addField(new Field("f2", DataType.INT));
    HeapObject obj = heap.createObject(myClass);
    HeapObject obj2 = heap.createObject(myClass);

    opStack.push(new Value(11));
    opStack.push(new Value(obj));
    opStack.push(new Value(22));
    opStack.push(new Value(obj2));
    opStack.push(new Value(21));
    opStack.push(new Value(obj2));
    opStack.push(new Value(12));
    opStack.push(new Value(obj));

    new StoreFieldOp("f2").execute(null, heap, opStack);
    assertEquals("opStack is wrong size", 6, opStack.size());
    assertEquals("field value not set", 12, obj.getFieldValue("f2").getIntValue());

    new StoreFieldOp("f1").execute(null, heap, opStack);
    new StoreFieldOp("f2").execute(null, heap, opStack);
    new StoreFieldOp("f1").execute(null, heap, opStack);

    assertEquals("opStack is wrong size", 0, opStack.size());
    assertEquals("field value not set", 11, obj.getFieldValue("f1").getIntValue());
    assertEquals("field value not set", 12, obj.getFieldValue("f2").getIntValue());
    assertEquals("field value not set", 22, obj2.getFieldValue("f2").getIntValue());
  }

  @Test
  public void testCompileNullExpression() {
    /** #score(1) */
    hintContext = "compile null expression";
    Visitable ast = TestUtil.testParseStatement("""
      x = (y == null);
      """);
    TestUtil.testCompileAST(ast, """
      load_local y
      load_const 0
      cmpEQ
      store_local x
      """);
  }

  @Test
  public void testCompileMethodCall() {
    /** #score(2) */
    hintContext = "compile method call";
    Visitable ast = TestUtil.testParseStatement("""
      foo();
      """);
    TestUtil.testCompileAST(ast, """
      load_local this
      call foo
      """);

    ast = TestUtil.testParseStatement("""
      foo(2, 4);
      """);
    TestUtil.testCompileAST(ast, """
      load_const 2
      load_const 4
      load_local this
      call foo
      """);

    ast = TestUtil.testParseStatement("""
      obj2.foo();
      """);
    TestUtil.testCompileAST(ast, """
      load_local obj2
      call foo
      """);

    ast = TestUtil.testParseStatement("""
      obj.foo(2, 4);
      """);
    TestUtil.testCompileAST(ast, """
      load_const 2
      load_const 4
      load_local obj
      call foo
      """);
  }

  @Test
  public void testMethodCall() {
    /** #score(2) */
    hintContext = "execute method call";
    TestUtil.compileClass("""
      class Adder {
        int add(int x, int y) {
          return (x + y);
        }
      }
      """);

    TestUtil.compileClass("""
      class Main {
        void main() {
          obj = new Adder();
          a = obj.add(2, 3);
          b = obj.add(a, 7);
        }
      }
      """);

    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    assertNotNull(hint("new expr didn't create object"), heap.getEntry(1));
    Value obj = locals.get("obj");
    assertNotNull(hint("new assignment didn't store object"), obj);
    assertEquals(hint("method call result incorrect"), 5, locals.get("a").getIntValue());
    assertEquals(hint("method call result incorrect"), 12, locals.get("b").getIntValue());
  }

  @Test
  public void testCompileFieldAccess() {
    /** #score(2) */
    hintContext = "compile field access";

    Visitable ast = TestUtil.testParseStatement("""
      x = obj3.field1;
      """);
    TestUtil.testCompileAST(ast, """
      load_local obj3
      load_field field1
      store_local x
      """);

    ast = TestUtil.testParseStatement("""
      obj4.myField = 3;
      """);
    TestUtil.testCompileAST(ast, """
      load_const 3
      load_local obj4
      store_field myField
      """);
  }

  @Test
  public void testCompileNewObjectExpression() {
    /** #score(2) */
    hintContext = "compile new object expression";
    Visitable ast = TestUtil.testParseStatement("""
      o = new MyClass();
      """);
    TestUtil.testCompileAST(ast, """
      new_object MyClass
      call init
      store_local o
      """);

    ast = TestUtil.testParseStatement("""
      o = new MyClass(x, y);
      """);
    TestUtil.testCompileAST(ast, """
      load_local x
      load_local y
      new_object MyClass
      call init
      store_local o
      """);
  }

  @Test
  public void testCompileConstructor() {
    /** #score(2) */
    hintContext = "compile constructor";

    MethodDefinition method = TestUtil.testParseMethod("""
      void init() {
      }
      """);
    TestUtil.testCompileMethod(method, """
      load_local this
      return
      """);

    method = TestUtil.testParseMethod("""
      void init(int x) {
        this.x = 3;
      }
      """);
    TestUtil.testCompileMethod(method, """
      load_const 3
      load_local this
      store_field x
      load_local this
      return
      """);
  }

  @Test
  public void testCompileClass() {
    /** #score(2) */
    hintContext = "compiling entire class";
    TestUtil.compileClass("""
      class MyClass {
        void foo() {
          x = (x + 1);
        }
        int bar(int x, SomeObject y) {
          return 3;
        }
      }
      """);

    TestUtil.checkMethod("MyClass", "init", """
      load_local this
      return
      """);

    TestUtil.checkMethod("MyClass", "foo", """
      load_local x
      load_const 1
      add
      store_local x
      return
      """);

    TestUtil.checkMethod("MyClass", "bar", """
      load_const 3
      return
      """);
  }

  @Test
  public void testExecuteConstructor() {
    /** #score(2) */
    hintContext = "compiling class";

    TestUtil.compileClass("""
      class Box {
        field int x;
        void init(int x) {
          this.x = x;
        }
        void main() {
          a = new Box(3);
          b = new Box(5);
        }
      }
      """);

    hintContext = "checking init method";
    TestUtil.checkMethod("Box", "init", """
      load_local x
      load_local this
      store_field x
      load_local this
      return
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Box", "main", """
      load_const 3
      new_object Box
      call init
      store_local a
      load_const 5
      new_object Box
      call init
      store_local b
      return
      """);

    hintContext = "executing main method";
    Heap heap = new Heap();
    VMThread e = new VMThread("Box", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    HeapObject obj = (HeapObject) heap.getEntry(locals.get("a").getIntValue());
    assertEquals(hint("object type incorrect"), "Box", obj.getClazz().getName());
    Value x = obj.getFieldValue("x");
    assertEquals(hint("field value incorrect"), 3, x.getIntValue());
    obj = (HeapObject) heap.getEntry(locals.get("b").getIntValue());
    x = obj.getFieldValue("x");
    assertEquals(hint("field value incorrect"), 5, x.getIntValue());
  }

  @Test
  public void testExecuteFieldAccess() {
    /** #score(2) */
    hintContext = "execute field access";

    TestUtil.compileClass("""
      class Main {
        field int x;
        field int y;
        void foo() {
          this.x = 3;
          this.y = (this.x + 2);
        }
        void main() {
        o = new Main();
        o.foo();
        }
      }
      """);

    TestUtil.checkMethod("Main", "foo", """
      load_const 3
      load_local this
      store_field x
      load_local this
      load_field x
      load_const 2
      add
      load_local this
      store_field y
      return
      """);

    TestUtil.checkMethod("Main", "main", """
      new_object Main
      call init
      store_local o
      load_local o
      call foo
      return
      """);

    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    Map<String, Value> locals = e.getEntryPointLocals();
    e.run();

    assertEquals(hint("object variable has incorrect value"), 1, locals.get("o").getIntValue());
    HeapObject obj = (HeapObject) heap.getEntry(1);
    assertEquals(hint("field value incorrect"), 3, obj.getFieldValue("x").getIntValue());
    assertEquals(hint("field value incorrect"), 5, obj.getFieldValue("y").getIntValue());
  }

  @Test
  public void testComplicatedStuff() {
    /** #score(2) */
    hintContext = "test complicated expressions";
    Visitable ast = TestUtil.testParseStatement("""
      o.foo().field1 = o2.bar((o3.foo() + 3)).field2.baz();
      """);
    TestUtil.testCompileAST(ast, """
      load_local o3
      call foo
      load_const 3
      add
      load_local o2
      call bar
      load_field field2
      call baz
      load_local o
      call foo
      store_field field1
      """);
  }

  @Test
  public void testBinaryTree() {
    /** #score(3) */
    hintContext = "execute method call";
    TestUtil.compileClass("""
      class Node {
        field int value;
        field Node left;
        field Node right;
        void init(int value) {
          this.value = value;
          this.left = null;
          this.right = null;
        }
        Node add(int value) {
          if (value < this.value) {
            if (this.left == null) {
              this.left = new Node(value);
              return this.left;
            } else {
              return this.left.add(value);
            }
          } else {
            if (this.right == null) {
              this.right = new Node(value);
              return this.right;
            } else {
              return this.right.add(value);
            }
          }
        }
        int getHeight() {
          leftHeight = 0;
          if (this.left != null) {
            leftHeight = this.left.getHeight();
          }

          rightHeight = 0;
          if (this.right != null) {
            rightHeight = this.right.getHeight();
          }

          if (leftHeight > rightHeight) {
            return (leftHeight + 1);
          } else {
            return (rightHeight + 1);
          }
        }
      }
      """);

    TestUtil.compileClass("""
      class Main {
        void main() {
          root = new Node(10);
          n5 = root.add(5);
          n17 = root.add(17);
          n8 = root.add(8);
          n2 = root.add(2);
          n3 = root.add(3);
          n13 = root.add(13);
          rh = root.getHeight();
          h5 = n5.getHeight();
          h2 = n2.getHeight();
          h17 = n17.getHeight();
          h13 = n13.getHeight();
        }
      }
      """);

    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();
    assertEquals("node height incorrect", 4, locals.get("rh").getIntValue());
    assertEquals("node height incorrect", 3, locals.get("h5").getIntValue());
    assertEquals("node height incorrect", 2, locals.get("h2").getIntValue());
    assertEquals("node height incorrect", 2, locals.get("h17").getIntValue());
    assertEquals("node height incorrect", 1, locals.get("h13").getIntValue());
  }

}
