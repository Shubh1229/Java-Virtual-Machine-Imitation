import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;

import engine.VMThread;
import engine.heap.Heap;
import engine.heap.HeapArray;
import types.DataType;
import types.Value;

public class Part07Test extends TestBase {

  @Test
  public void testParseNewArrayExpressions() {
    /** #score(3) */
    hintContext = "parsing new array expressions";
    TestUtil.testParseStatement("a = new int[3];\n");
    TestUtil.testParseStatement("a = new int[x];\n");
    TestUtil.testParseStatement("a = new int[(x + 1)];\n");
    TestUtil.testParseStatement("a = new MyClass[3];\n");
    TestUtil.testParseStatement("a = new MyClass[x];\n");
    TestUtil.testParseStatement("a = new MyClass[(x + 1)];\n");
  }

  @Test
  public void testParseArrayExpressions() {
    /** #score(3) */
    hintContext = "parsing array expressions";
    TestUtil.testParseStatement("a[0] = 3;\n");
    TestUtil.testParseStatement("x = a[1];\n");
    TestUtil.testParseStatement("x = a[x];\n");
    TestUtil.testParseStatement("x = a[(x + 2)];\n");
    TestUtil.testParseStatement("x = foo(a[1]);\n");
    TestUtil.testParseStatement("bar(3, a[1][2], b[3][4]);\n");
  }

  @Test
  public void testParseLengthExpressions() {
    /** #score(3) */
    TestUtil.testParseStatement("x = a.length;\n");
    TestUtil.testParseStatement("x = a[0].length;\n");
  }

  @Test
  public void testCompileNewArrayExpressions() {
    /** #score(3) */
    hintContext = "compiling new array expressions";
    TestUtil.testCompileStatement("a = new int[10];\n", """
      load_const 10
      new_array int 1
      store_local a
      """);

    TestUtil.testCompileStatement("a = new MyClass[10];\n", """
      load_const 10
      new_array MyClass 1
      store_local a
      """);

    TestUtil.testCompileStatement("a = new int[(x + 10)];\n", """
      load_local x
      load_const 10
      add
      new_array int 1
      store_local a
      """);
  }

  @Test
  public void testCompileArrayExpressions() {
    /** #score(3) */
    hintContext = "compiling array expressions";
    TestUtil.testCompileStatement("a[0] = 3;\n", """
      load_const 3
      load_const 0
      load_local a
      store_array_element
      """);

    TestUtil.testCompileStatement("x = a[1];\n", """
      load_const 1
      load_local a
      load_array_element
      store_local x
      """);
  }

  @Test
  public void testCompileLengthExpressions() {
    /** #score(3) */
    TestUtil.testCompileStatement("x = a.length;\n", """
      load_local a
      load_field length
      store_local x
      """);
    TestUtil.testCompileStatement("x = a[0].length;\n", """
      load_const 0
      load_local a
      load_array_element
      load_field length
      store_local x
      """);
  }

  @Test
  public void testExecuteNewIntArrayExpressions() {
    /** #score(3) */
    hintContext = "parsing code";
    TestUtil.compileClass("""
      class Main {
        void main() {
          a = new int[5];
        }
      }
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Main", "main", """
      load_const 5
      new_array int 1
      store_local a
      return
      """);

    hintContext = "executing code";
    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    Value arrId = locals.get("a");
    HeapArray a = (HeapArray) heap.getEntry(arrId.getIntValue());
    assertNotNull(hint("array wasn't created"), a);
    assertEquals(hint("array had wrong type"), "int[]", a.getType().toString());
    checkArrayDims(arrId.getIntValue(), heap, "int", 5);
  }

  @Test
  public void testExecuteNewObjectArrayExpressions() {
    /** #score(3) */
    hintContext = "parsing code";
    TestUtil.compileClass("""
      class Main {
        void main() {
          a = new MyClass[5];
        }
      }
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Main", "main", """
      load_const 5
      new_array MyClass 1
      store_local a
      return
      """);

    hintContext = "executing code";
    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    Value arrId = locals.get("a");
    HeapArray a = (HeapArray) heap.getEntry(arrId.getIntValue());
    assertNotNull(hint("array wasn't created"), a);
    assertEquals(hint("array had wrong type"), "MyClass[]", a.getType().toString());
    checkArrayDims(arrId.getIntValue(), heap, "MyClass", 5);
  }

  @Test
  public void testExecuteArrayExpressions() {
    /** #score(3) */
    hintContext = "parsing code";
    TestUtil.compileClass("""
      class Main {
        void main() {
          a = new int[5];
          a[0] = 13;
          a[4] = 21;
          a[2] = (a[4] + 8);
        }
      }
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Main", "main", """
      load_const 5
      new_array int 1
      store_local a
      load_const 13
      load_const 0
      load_local a
      store_array_element
      load_const 21
      load_const 4
      load_local a
      store_array_element
      load_const 4
      load_local a
      load_array_element
      load_const 8
      add
      load_const 2
      load_local a
      store_array_element
      return
      """);

    hintContext = "executing code";
    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    checkArrayDims(locals.get("a").getIntValue(), heap, "int", 5);
    checkArrayVals(locals.get("a").getIntValue(), heap, new DataType("int", 1), new Integer[] { 13, 0, 29, 0, 21 });
  }

  @Test
  public void testExecuteLengthExpressions() {
    /** #score(3) */
    hintContext = "parsing code";
    TestUtil.compileClass("""
      class Main {
        void main() {
          a = new int[13];
          x = a.length;
        }
      }
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Main", "main", """
      load_const 13
      new_array int 1
      store_local a
      load_local a
      load_field length
      store_local x
      return
      """);

    hintContext = "executing code";
    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    Value x = locals.get("x");
    assertEquals(hint("array length incorrect"), 13, x.getIntValue());
  }

  @Test
  public void testParseNew2DArrayExpressions() {
    /** #score(1) */
    hintContext = "parsing new array expressions";
    TestUtil.testParseStatement("a = new int[3][4];\n");
    TestUtil.testParseStatement("a = new MyClass[3][x];\n");
    TestUtil.testParseStatement("a = new int[(3 + x)][(y - 2)];\n");
  }

  @Test
  public void testCompileNew2DArrayExpressions() {
    /** #score(1) */
    hintContext = "compiling new array expressions";
    TestUtil.testCompileStatement("a = new int[5][10];\n", """
      load_const 5
      load_const 10
      new_array int 2
      store_local a
      """);
    TestUtil.testCompileStatement("a = new MyClass[(x + 5)][(y + 10)];\n", """
      load_local x
      load_const 5
      add
      load_local y
      load_const 10
      add
      new_array MyClass 2
      store_local a
      """);
  }

  @Test
  public void testExecuteNew2DIntArrayExpressions() {
    /** #score(1) */
    hintContext = "parsing code";
    TestUtil.compileClass("""
      class Main {
        void main() {
          a = new int[5][4];
        }
      }
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Main", "main", """
      load_const 5
      load_const 4
      new_array int 2
      store_local a
      return
      """);

    hintContext = "executing code";
    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    Value arrId = locals.get("a");
    HeapArray a = (HeapArray) heap.getEntry(arrId.getIntValue());
    assertNotNull(hint("array wasn't created"), a);
    assertEquals(hint("array had wrong type"), "int[][]", a.getType().toString());
    checkArrayDims(locals.get("a").getIntValue(), heap, "int", 5, 4);
  }

  @Test
  public void testExecuteNew2DObjectArrayExpressions() {
    /** #score(1) */
    hintContext = "parsing code";
    TestUtil.compileClass("""
      class Main {
        void main() {
          a = new MyClass[5][4];
        }
      }
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Main", "main", """
      load_const 5
      load_const 4
      new_array MyClass 2
      store_local a
      return
      """);

    hintContext = "executing code";
    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    Value arrId = locals.get("a");
    HeapArray a = (HeapArray) heap.getEntry(arrId.getIntValue());
    assertNotNull(hint("array wasn't created"), a);
    assertEquals(hint("array had wrong type"), "MyClass[][]", a.getType().toString());
    checkArrayDims(locals.get("a").getIntValue(), heap, "MyClass", 5, 4);
  }

  @Test
  public void testExecuteNew4DIntArrayExpressions() {
    /** #score(1) */
    hintContext = "parsing code";
    TestUtil.compileClass("""
      class Main {
        void main() {
          a = new int[5][4][3][2];
        }
      }
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Main", "main", """
      load_const 5
      load_const 4
      load_const 3
      load_const 2
      new_array int 4
      store_local a
      return
      """);

    hintContext = "executing code";
    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    Value arrId = locals.get("a");
    HeapArray a = (HeapArray) heap.getEntry(arrId.getIntValue());
    assertNotNull(hint("array wasn't created"), a);
    assertEquals(hint("array had wrong type"), "int[][][][]", a.getType().toString());
    checkArrayDims(locals.get("a").getIntValue(), heap, "int", 5, 4, 3, 2);
  }

  @Test
  public void testExecuteNew4DObjectArrayExpressions() {
    /** #score(1) */
    hintContext = "parsing code";
    TestUtil.compileClass("""
      class Main {
        void main() {
          a = new MyClass[5][4][2][3];
        }
      }
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Main", "main", """
      load_const 5
      load_const 4
      load_const 2
      load_const 3
      new_array MyClass 4
      store_local a
      return
      """);

    hintContext = "executing code";
    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    Value arrId = locals.get("a");
    HeapArray a = (HeapArray) heap.getEntry(arrId.getIntValue());
    assertNotNull(hint("array wasn't created"), a);
    assertEquals(hint("array had wrong type"), "MyClass[][][][]", a.getType().toString());
    checkArrayDims(locals.get("a").getIntValue(), heap, "MyClass", 5, 4, 2, 3);
  }

  @Test
  public void testParse3DArrayExpressions() {
    /** #score(1) */
    TestUtil.testParseStatement("x = a[1][2][3];\n");
    TestUtil.testParseStatement("a[1][2][3] = x;\n");
  }

  @Test
  public void testCompile3DArrayExpressions() {
    /** #score(1) */
    TestUtil.testCompileStatement("x = a[1][2][3];\n", """
      load_const 3
      load_const 2
      load_const 1
      load_local a
      load_array_element
      load_array_element
      load_array_element
      store_local x
      """);

    TestUtil.testCompileStatement("a[1][2][3] = x;\n", """
      load_local x
      load_const 3
      load_const 2
      load_const 1
      load_local a
      load_array_element
      load_array_element
      store_array_element
      """);
  }

  @Test
  public void testExecute2DArrayExpressions() {
    /** #score(1) */
    hintContext = "parsing code";
    TestUtil.compileClass("""
      class Main {
        void main() {
          a = new int[3][2];
          a[0][0] = 3;
          a[1][0] = 5;
          a[0][1] = (a[1][0] + 8);
        }
      }
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Main", "main", """
      load_const 3
      load_const 2
      new_array int 2
      store_local a
      load_const 3
      load_const 0
      load_const 0
      load_local a
      load_array_element
      store_array_element
      load_const 5
      load_const 0
      load_const 1
      load_local a
      load_array_element
      store_array_element
      load_const 0
      load_const 1
      load_local a
      load_array_element
      load_array_element
      load_const 8
      add
      load_const 1
      load_const 0
      load_local a
      load_array_element
      store_array_element
      return
      """);

    hintContext = "executing code";
    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    checkArrayDims(locals.get("a").getIntValue(), heap, "int", 3, 2);
    checkArrayVals(locals.get("a").getIntValue(), heap, new DataType("int", 2),
      new Integer[][] { { 3, 13 }, { 5, 0 }, { 0, 0 } });
  }

  @Test
  public void testExecute3DLengthExpressions() {
    /** #score(1) */
    hintContext = "parsing code";
    TestUtil.compileClass("""
      class Main {
        void main() {
          a = new int[3][2][4];
          x = a.length;
          y = a[0].length;
          z = a[0][1].length;
        }
      }
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Main", "main", """
      load_const 3
      load_const 2
      load_const 4
      new_array int 3
      store_local a
      load_local a
      load_field length
      store_local x
      load_const 0
      load_local a
      load_array_element
      load_field length
      store_local y
      load_const 1
      load_const 0
      load_local a
      load_array_element
      load_array_element
      load_field length
      store_local z
      return
      """);

    hintContext = "executing code";
    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    Value x = locals.get("x");
    assertEquals(hint("base array length incorrect"), 3, x.getIntValue());
    Value y = locals.get("y");
    assertEquals(hint("sub-array length incorrect"), 2, y.getIntValue());
    Value z = locals.get("z");
    assertEquals(hint("sub-sub-array length incorrect"), 4, z.getIntValue());
  }

  @Test
  public void testCombinedExpressions() {
    /** #score(1) */
    hintContext = "parsing code";
    TestUtil.compileClass("""
      class Main {
        field int x;
        void init(int x) {
          this.x = x;
        }
        int getX() {
          return this.x;
        }
        void main() {
          a = new Main[3][2];
          a[0][0] = new Main(13);
          x = a.length;
          y = a[0].length;
          z = a[0][0].x;
          b = a[0][0].getX();
        }
      }
      """);

    hintContext = "checking main method";
    TestUtil.checkMethod("Main", "main", """
      load_const 3
      load_const 2
      new_array Main 2
      store_local a
      load_const 13
      new_object Main
      call init
      load_const 0
      load_const 0
      load_local a
      load_array_element
      store_array_element
      load_local a
      load_field length
      store_local x
      load_const 0
      load_local a
      load_array_element
      load_field length
      store_local y
      load_const 0
      load_const 0
      load_local a
      load_array_element
      load_array_element
      load_field x
      store_local z
      load_const 0
      load_const 0
      load_local a
      load_array_element
      load_array_element
      call getX
      store_local b
      return
      """);

    hintContext = "executing code";
    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    Value x = locals.get("x");
    assertEquals(hint("base array length incorrect"), 3, x.getIntValue());
    Value y = locals.get("y");
    assertEquals(hint("sub-array length incorrect"), 2, y.getIntValue());
    Value z = locals.get("z");
    assertEquals(hint("nested field value incorrect"), 13, z.getIntValue());
    Value b = locals.get("b");
    assertEquals(hint("nested method return value incorrect"), 13, b.getIntValue());
  }

  private void checkArrayDims(int arrId, int index, Heap heap, String elemType, int... dims) {
    HeapArray arr = (HeapArray) heap.getEntry(arrId);
    assertNotNull(hint("array object " + arrId + " in heap missing"), arr);
    assertEquals(hint("array length incorrect"), dims[index], arr.getLength());
    if (index < dims.length - 1) {
      for (int i = 0; i < dims[index]; i++) {
        Value sub = arr.getAt(i);
        checkArrayDims(sub.getIntValue(), index + 1, heap, elemType, dims);
      }
    }
  }

  private void checkArrayDims(int arrId, Heap heap, String elemType, int... dims) {
    checkArrayDims(arrId, 0, heap, elemType, dims);
  }

  private void checkArrayVals(int arrId, Heap heap, DataType expectedType, Object[] vals) {
    HeapArray arr = (HeapArray) heap.getEntry(arrId);
    assertNotNull(hint("array object " + arrId + " in heap missing"), arr);
    assertEquals(hint("array length incorrect"), vals.length, arr.getLength());
    assertEquals(hint("array type incorrect"), expectedType, arr.getType());

    DataType expectedElemType = expectedType.getElementType();
    if (vals instanceof Integer[]) {
      for (int i = 0; i < vals.length; i++) {
        Value elem = arr.getAt(i);
        assertEquals(hint("array value at index " + i + " incorrect"), vals[i], elem.getIntValue());
      }
    } else {
      for (int i = 0; i < vals.length; i++) {
        Value subArrId = arr.getAt(i);
        checkArrayVals(subArrId.getIntValue(), heap, expectedElemType, (Object[]) vals[i]);
      }
    }
  }

  @Test
  public void testBinaryTree() {
    /** #score(4) */
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
        void add(int value) {
          if (value < this.value) {
            if (this.left == null) {
              this.left = new Node(value);
            } else {
              this.left.add(value);
            }
          } else {
            if (this.right == null) {
              this.right = new Node(value);
            } else {
              this.right.add(value);
            }
          }
        }
        int preOrder(int[] values, int index) {
          values[index] = this.value;
          index = (index + 1);
          if (this.left != null) {
            index = this.left.preOrder(values, index);
          }
          if (this.right != null) {
            index = this.right.preOrder(values, index);
          }
          return index;
        }
      }
      """);

    TestUtil.compileClass("""
      class Main {
        void main() {
          root = new Node(10);
          root.add(5);
          root.add(17);
          root.add(8);
          root.add(2);
          root.add(3);
          root.add(13);
          values = new int[7];
          root.preOrder(values, 0);
        }
      }
      """);

    Heap heap = new Heap();
    VMThread e = new VMThread("Main", "main", heap);
    e.run();
    Map<String, Value> locals = e.getEntryPointLocals();

    Value values = locals.get("values");
    checkArrayVals(values.getIntValue(), heap, new DataType("int", 1), new Integer[] { 10, 5, 2, 3, 8, 17, 13 });
  }
}
