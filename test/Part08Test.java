import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.junit.Test;

import ast.model.ParameterDefinition;
import engine.CompiledClassCache;
import engine.StackFrame;
import engine.VMThread;
import engine.exceptions.MissingFieldException;
import engine.exceptions.MissingMethodException;
import engine.exceptions.TypeMismatchException;
import engine.exceptions.UninitializedFieldException;
import engine.heap.Heap;
import engine.heap.HeapArray;
import engine.heap.HeapObject;
import engine.opcodes.BinaryOp;
import engine.opcodes.BranchOp;
import engine.opcodes.BranchOp.Type;
import engine.opcodes.CallOp;
import engine.opcodes.LoadArrayElementOp;
import engine.opcodes.LoadConstOp;
import engine.opcodes.LoadFieldOp;
import engine.opcodes.LoadLocalOp;
import engine.opcodes.NewObjectOp;
import engine.opcodes.Operator;
import engine.opcodes.StoreArrayElementOp;
import engine.opcodes.StoreFieldOp;
import engine.opcodes.StoreLocalOp;
import types.Clazz;
import types.DataType;
import types.Field;
import types.Method;
import types.Value;

public class Part08Test {

  private void checkValue(String hintPrefix, int expectedValue, String expectedType, Value v) {
    assertNotNull(hintPrefix + " null Value", v);
    assertEquals(hintPrefix + " wrong value", expectedValue, v.getIntValue());
    assertEquals(hintPrefix + " wrong type", expectedType, v.getType().toString());
  }

  @Test
  public void testBasicOpTypes() {
    /** #score(3) */
    Stack<Value> opStack = new Stack<>();
    Stack<StackFrame> callStack = new Stack<>();
    Map<String, Value> locals = new HashMap<>();
    callStack.push(new StackFrame(null, locals));

    new LoadConstOp(3).execute(null, null, opStack);
    checkValue("LoadConstOp pushed", 3, "int", opStack.pop());

    opStack.push(new Value(3));
    opStack.push(new Value(4));
    new BinaryOp(Operator.ADD).execute(null, null, opStack);
    checkValue("add pushed", 7, "int", opStack.pop());

    opStack.push(new Value(3));
    opStack.push(new Value(4));
    new BinaryOp(Operator.LT).execute(null, null, opStack);
    checkValue("cmpLT pushed", 1, "int", opStack.pop());

    opStack.push(new Value(0));
    new BranchOp(Type.TRUE, 99).execute(callStack, null, opStack);
    assertEquals("branchF shouldn't have branched", 0, callStack.peek().getProgramCounter());

    opStack.push(new Value(1));
    new BranchOp(Type.TRUE, 99).execute(callStack, null, opStack);
    assertEquals("branchF should have branched", 99, callStack.peek().getProgramCounter());

    callStack.peek().jumpTo(0);
    opStack.push(new Value(1));
    new BranchOp(Type.FALSE, 99).execute(callStack, null, opStack);
    assertEquals("branchF shouldn't have branched", 0, callStack.peek().getProgramCounter());

    opStack.push(new Value(0));
    new BranchOp(Type.FALSE, 99).execute(callStack, null, opStack);
    assertEquals("branchF should have branched", 99, callStack.peek().getProgramCounter());
  }

  @Test
  public void testLoadStoreLocalOpTypes() {
    /** #score(3) */
    Stack<Value> opStack = new Stack<>();
    Stack<StackFrame> callStack = new Stack<>();
    Map<String, Value> locals = new HashMap<>();
    callStack.push(new StackFrame(null, locals));

    opStack.push(new Value(3));

    new StoreLocalOp("x").execute(callStack, null, opStack);
    checkValue("StoreLocalOp stored", 3, "int", locals.get("x"));

    new LoadLocalOp("x").execute(callStack, null, opStack);
    checkValue("LoadLocalOp pushed", 3, "int", opStack.peek());

    DataType objType = new DataType("MyClass");
    opStack.push(new Value(7, objType));

    new StoreLocalOp("x").execute(callStack, null, opStack);
    checkValue("StoreLocalOp stored", 7, "MyClass", locals.get("x"));

    new LoadLocalOp("x").execute(callStack, null, opStack);
    checkValue("LoadLocalOp pushed", 7, "MyClass", opStack.peek());
  }

  @Test
  public void testCallOpTypes() {
    /** #score(3) */
    Stack<Value> opStack = new Stack<>();
    Stack<StackFrame> callStack = new Stack<>();
    Map<String, Value> locals = new HashMap<>();
    callStack.push(new StackFrame(null, locals));

    Clazz clazz = new Clazz("MyClass");
    Heap heap = new Heap();
    HeapObject obj = heap.createObject(clazz);
    HeapObject obj2 = heap.createObject(clazz);

    Method method = new Method(
      "foo",
      List.of(new ParameterDefinition("a", DataType.INT),
        new ParameterDefinition("b", new DataType("MyClass"))),
      null);
    CompiledClassCache.instance().saveMethod("MyClass", method);

    opStack.push(new Value(13));
    opStack.push(new Value(obj2));
    opStack.push(new Value(obj));

    new CallOp("foo").execute(callStack, heap, opStack);
    checkValue("CallOp passed",
      obj.getId(),
      "MyClass",
      callStack.peek().getLocalVars().get("this"));
    checkValue("CallOp passed",
      13,
      "int",
      callStack.peek().getLocalVars().get("a"));
    checkValue("CallOp passed",
      obj2.getId(),
      "MyClass",
      callStack.peek().getLocalVars().get("b"));

  }

  @Test
  public void testObjectOpTypes() {
    /** #score(3) */
    Stack<Value> opStack = new Stack<>();
    Stack<StackFrame> callStack = new Stack<>();
    Map<String, Value> locals = new HashMap<>();
    callStack.push(new StackFrame(null, locals));

    Heap heap = new Heap();
    Clazz clazz = new Clazz("MyClass");
    clazz.addField(new Field("someField", DataType.INT));
    clazz.addField(new Field("objField", clazz.getType()));
    CompiledClassCache.instance().saveClass(clazz);

    opStack.push(new Value(5));

    new NewObjectOp("MyClass").execute(callStack, heap, opStack);
    checkValue("NewObjectOp pushed", 1, "MyClass", opStack.peek());

    HeapObject obj = (HeapObject) heap.getEntry(opStack.peek().getIntValue());
    new StoreFieldOp("someField").execute(callStack, heap, opStack);
    checkValue("StoreFieldOp stored", 5, "int", obj.getFieldValue("someField"));

    opStack.push(new Value(obj));
    new LoadFieldOp("someField").execute(callStack, heap, opStack);
    checkValue("LoadFieldOp pushed", 5, "int", opStack.peek());

    HeapObject obj2 = heap.createObject(clazz);
    opStack.push(new Value(obj2));
    opStack.push(new Value(obj));
    new StoreFieldOp("objField").execute(callStack, heap, opStack);
    checkValue("StoreFieldOp stored", obj2.getId(), "MyClass", obj.getFieldValue("objField"));

    opStack.push(new Value(obj));
    new LoadFieldOp("objField").execute(callStack, heap, opStack);
    checkValue("LoadFieldOp pushed", obj2.getId(), "MyClass", opStack.peek());
  }

  private void checkArrayDims(int arrId, int index, Heap heap, String elemType, int... dims) {
    HeapArray arr = (HeapArray) heap.getEntry(arrId);
    assertNotNull("array object " + arrId + " in heap missing", arr);
    assertEquals("array length incorrect", dims[index], arr.getLength());
    if (index < dims.length - 1) {
      for (int i = 0; i < dims[index]; i++) {
        DataType expectedElemType = new DataType(elemType, dims.length - 1 - index);
        Value sub = arr.getAt(i);
        assertEquals("sub array type incorrect", expectedElemType, sub.getType());
        checkArrayDims(sub.getIntValue(), index + 1, heap, elemType, dims);
      }
    }
  }

  private void checkArrayDims(int arrId, Heap heap, String elemType, int... dims) {
    checkArrayDims(arrId, 0, heap, elemType, dims);
  }

  @Test
  public void testCreateArrayTypes() {
    /** #score(3) */
    Heap heap = new Heap();
    Clazz clazz = new Clazz("MyClass");
    CompiledClassCache.instance().saveClass(clazz);

    HeapArray arr = heap.createArray(new DataType("int", 1), new int[] { 3 });
    assertEquals("array object type incorrect", "int[]", arr.getType().toString());
    checkArrayDims(arr.getId(), heap, "int", 3);

    arr = heap.createArray(new DataType("MyClass", 1), new int[] { 4 });
    assertEquals("array object type incorrect", "MyClass[]", arr.getType().toString());
    checkArrayDims(arr.getId(), heap, "MyClass", 4);

    arr = heap.createArray(new DataType("MyClass", 2), new int[] { 4, 3 });
    assertEquals("array object type incorrect", "MyClass[][]", arr.getType().toString());
    checkArrayDims(arr.getId(), heap, "MyClass", 4, 3);

    arr = heap.createArray(new DataType("int", 4), new int[] { 4, 3, 1, 2 });
    assertEquals("array object type incorrect", "int[][][][]", arr.getType().toString());
    checkArrayDims(arr.getId(), heap, "int", 4, 3, 1, 2);
  }

  @Test
  public void testArrayOpTypes() {
    /** #score(3) */
    Stack<Value> opStack = new Stack<>();
    Stack<StackFrame> callStack = new Stack<>();
    Map<String, Value> locals = new HashMap<>();
    callStack.push(new StackFrame(null, locals));

    Heap heap = new Heap();
    Clazz clazz = new Clazz("MyClass");
    CompiledClassCache.instance().saveClass(clazz);

    HeapArray arr = heap.createArray(new DataType("int", 1), new int[] { 3 });
    HeapArray arr2 = heap.createArray(new DataType("MyClass", 1), new int[] { 5 });
    HeapObject obj = heap.createObject(clazz);

    opStack.push(new Value(5));
    opStack.push(new Value(1));
    opStack.push(new Value(arr));
    new StoreArrayElementOp().execute(callStack, heap, opStack);
    checkValue("StoreArrayElementOp stored", 5, "int", arr.getAt(1));

    opStack.push(new Value(1));
    opStack.push(new Value(arr));
    new LoadArrayElementOp().execute(callStack, heap, opStack);
    checkValue("LoadArrayElement pushed", 5, "int", opStack.peek());

    opStack.push(new Value(obj));
    opStack.push(new Value(3));
    opStack.push(new Value(arr2));
    new StoreArrayElementOp().execute(callStack, heap, opStack);
    checkValue("StoreArrayElementOp stored", obj.getId(), "MyClass", arr2.getAt(3));

    opStack.push(new Value(3));
    opStack.push(new Value(arr2));
    new LoadArrayElementOp().execute(callStack, heap, opStack);
    checkValue("LoadArrayElement pushed", obj.getId(), "MyClass", opStack.peek());
  }

  @Test
  public void testMultiDimArrayOpTypes() {
    /** #score(3) */
    Stack<Value> opStack = new Stack<>();
    Stack<StackFrame> callStack = new Stack<>();
    Map<String, Value> locals = new HashMap<>();
    callStack.push(new StackFrame(null, locals));

    Heap heap = new Heap();
    Clazz clazz = new Clazz("MyClass");
    CompiledClassCache.instance().saveClass(clazz);

    HeapArray arr = heap.createArray(new DataType("int", 3), new int[] { 2, 3, 1 });
    HeapArray arr2 = heap.createArray(new DataType("MyClass", 3), new int[] { 3, 4, 2 });
    HeapObject obj = heap.createObject(clazz);

    opStack.push(new Value(7));
    opStack.push(new Value(0));
    opStack.push(new Value(2));
    opStack.push(new Value(1));
    opStack.push(new Value(arr));

    new LoadArrayElementOp().execute(callStack, heap, opStack);
    int subArrId = arr.getAt(1).getIntValue();
    HeapArray subArr = (HeapArray) heap.getEntry(subArrId);
    checkValue("LoadArrayElementOp for subarray pushed",
      subArrId,
      "int[][]",
      opStack.peek());

    new LoadArrayElementOp().execute(callStack, heap, opStack);
    int subSubArrId = subArr.getAt(2).getIntValue();
    HeapArray subSubArr = (HeapArray) heap.getEntry(subSubArrId);
    checkValue("LoadArrayElementOp for subarray pushed",
      subSubArrId,
      "int[]",
      opStack.peek());

    new StoreArrayElementOp().execute(callStack, heap, opStack);
    checkValue("StoreArrayElementOp stored", 7, "int", subSubArr.getAt(0));

    opStack.push(new Value(obj));
    opStack.push(new Value(1));
    opStack.push(new Value(3));
    opStack.push(new Value(2));
    opStack.push(new Value(arr2));

    new LoadArrayElementOp().execute(callStack, heap, opStack);
    subArrId = arr2.getAt(2).getIntValue();
    subArr = (HeapArray) heap.getEntry(subArrId);
    checkValue("LoadArrayElementOp for subarray pushed",
      subArrId,
      "MyClass[][]",
      opStack.peek());

    new LoadArrayElementOp().execute(callStack, heap, opStack);
    subSubArrId = subArr.getAt(3).getIntValue();
    subSubArr = (HeapArray) heap.getEntry(subSubArrId);
    checkValue("LoadArrayElementOp for subarray pushed",
      subSubArrId,
      "MyClass[]",
      opStack.peek());

    new StoreArrayElementOp().execute(callStack, heap, opStack);
    checkValue("StoreArrayElementOp stored", obj.getId(), "MyClass", subSubArr.getAt(1));
  }

  private Set<Operator> allowedForReferences = Set.of(Operator.EQ, Operator.NEQ);

  private void testOpWithConstants(Operator op) {
    try {
      TestUtil.testCode("""
        load_const 12
        load_const 23
        """ +
        op.getInstruction() + "\n" +
        "store_local a\n");
    } catch (TypeMismatchException e) {
      fail("Unexpected exception thrown while testing operator " + op.getInstruction(), e);
    }
  }

  private void testException(String methodName, Class<? extends Exception> exClass, String msg) {
    Heap heap = new Heap();
    VMThread thread = new VMThread("Main", methodName, heap);
    try {
      thread.run();
      if (exClass != null) {
        fail("Expected " + exClass.getSimpleName() + ": " + msg + " shouldn't be allowed");
      }
    } catch (Exception e) {
      if (exClass == null) {
        fail("Unexpected exception: " + msg + " should be allowed", e);
      } else if (!exClass.isInstance(e)) {
        fail("Wrong exception type - expected " + exClass.getSimpleName(), e);
      }
    }
  }

  private void testOpWithObjects(Operator op) {
    TestUtil.compileClass("""
      class MyClass {
      }
      """);

    TestUtil.compileClass("""
      class Main {
        void twoObjs() {
          obj = new MyClass();
          res = (obj\s""" + op.getSymbol() + " obj);\n" + """
        }
      }
      """);

    testException("twoObjs", allowedForReferences.contains(op) ? null : TypeMismatchException.class,
      "using " + op.getSymbol() + " on objects");
  }

  private void testOpWithArrays(Operator op) {
    try {
      TestUtil.testCode("""
        load_const 3
        new_array int 1
        load_const 3
        new_array int 1
        """ + op.getInstruction() + "\n" +
        "store_local a\n");
      if (!allowedForReferences.contains(op)) {
        fail("Expected TypeMismatchException - using " + op.getSymbol() + " on arrays shouldn't be allowed");
      }
    } catch (TypeMismatchException e) {
      if (allowedForReferences.contains(op)) {
        fail("Unexpected exception - using " + op.getSymbol() + " on arrays should be allowed", e);
      }
    }
  }

  @Test
  public void testBinaryOps() {
    /** #score(3) */
    for (Operator op : Operator.values()) {
      testOpWithConstants(op);
      testOpWithObjects(op);
      testOpWithArrays(op);
    }
  }

  private void testOpWithMixedTypes(Operator op) {
    TestUtil.compileClass("""
      class MyClass {
      }
      """);

    TestUtil.compileClass("""
      class Main {
        void constAndObj() {
          obj = new MyClass();
          res = (3\s""" + op.getSymbol() + " obj);\n" + """
      }
      void objAndConst() {
        obj = new MyClass();
        res = (obj\s""" + op.getSymbol() + " 3);\n" + """
      }
      void constAndArr() {
        arr = new int[3];
        res = (3\s""" + op.getSymbol() + " arr);\n" + """
      }
      void arrAndConst() {
        arr = new int[3];
        res = (arr\s""" + op.getSymbol() + " 3);\n" + """
      }
      void objAndArr() {
        obj = new MyClass();
        arr = new int[3];
        res = (obj\s""" + op.getSymbol() + " arr);\n" + """
      }
      void arrAndObj() {
        obj = new MyClass();
        arr = new int[3];
        res = (arr\s""" + op.getSymbol() + " obj);\n" + """
        }
      }
      """);

    testException("constAndObj", TypeMismatchException.class, "using " + op + " on different types");
    testException("objAndConst", TypeMismatchException.class, "using " + op + " on different types");
    testException("constAndArr", TypeMismatchException.class, "using " + op + " on different types");
    testException("arrAndConst", TypeMismatchException.class, "using " + op + " on different types");
    testException("objAndArr", TypeMismatchException.class, "using " + op + " on different types");
    testException("arrAndObj", TypeMismatchException.class, "using " + op + " on different types");
  }

  @Test
  public void testBinaryOpsMixedTypes() {
    /** #score(3) */
    for (Operator op : Operator.values()) {
      testOpWithMixedTypes(op);
    }
  }

  @Test
  public void testNull() {
    /** #score(3) */
    TestUtil.compileClass("""
      class MyClass {
        field MyClass a;
      }
      """);

    TestUtil.compileClass("""
      class Main {
        void objEqNull() {
          obj = new MyClass();
          res = (obj == 0);
        }
        void objEqOne() {
          obj = new MyClass();
          res = (obj == 1);
        }
        void objNeqNull() {
          obj = new MyClass();
          res = (obj != 0);
        }
        void objNeqOne() {
          obj = new MyClass();
          res = (obj != 1);
        }
        void storeNull() {
          obj = new MyClass();
          obj.a = 0;
        }
      }
      """);

    testException("objEqNull", null, "comparing object reference to 0(null)");
    testException("objEqOne", TypeMismatchException.class, "comparing object reference to non-zero int");
    testException("objNeqNull", null, "comparing object reference to 0(null)");
    testException("objNeqOne", TypeMismatchException.class, "comparing object reference to non-zero int");
    testException("storeNull", null, "storing 0(null) in a field of object reference");
  }

  @Test
  public void testObjectFieldStore() {
    /** #score(3) */
    TestUtil.compileClass("""
      class MyClass {
        field int x;
        field MyClass o;
        field int[] arr;
        field MyClass[] oArr;
        field MyClass[][] oArr2d;
      }
      """);

    TestUtil.compileClass("""
      class Main {
        void notAnObjectStore() {
          x = 3;
          x.y = 4;
        }
        void notAnObjectLoad() {
          x = 3;
          y = x.y;
        }
        void missingFieldStore() {
          o = new MyClass();
          o.y = 3;
        }
        void missingFieldLoad() {
          o = new MyClass();
          y = o.y;
        }
        void uninitializedFieldLoad() {
          o = new MyClass();
          x = o.x;
        }
        void fieldTypeRight() {
          o = new MyClass();
          o.x = 3;
        }
        void fieldTypeWrong() {
          o = new MyClass();
          o.x = new MyClass();
        }
        void fieldTypeWrong2() {
          o = new MyClass();
          o.arr = new MyClass();
        }
        void fieldTypeRight2() {
          o = new MyClass();
          o.arr = new int[3];
        }
        void fieldTypeWrong3() {
          o = new MyClass();
          o.arr = new MyClass[3];
        }
        void fieldTypeRight3() {
          o = new MyClass();
          o.oArr = new MyClass[3];
        }
        void fieldTypeWrong4() {
          o = new MyClass();
          o.oArr = new int[3];
        }
        void fieldIntTooManyDims() {
          o = new MyClass();
          o.arr = new int[3][3];
        }
        void fieldObjNotEnoughDims() {
          o = new MyClass();
          o.oArr2d = new MyClass[3];
        }
      }
      """);

    testException("notAnObjectStore", TypeMismatchException.class, "storing field on non-object");
    testException("notAnObjectLoad", TypeMismatchException.class, "load field on non-object");
    testException("missingFieldStore", MissingFieldException.class, "store to undefined field");
    testException("missingFieldLoad", MissingFieldException.class, "load from undefined field");
    testException("uninitializedFieldLoad", UninitializedFieldException.class, "load from uninitialized field");
    testException("fieldTypeRight", null, "assigning to field of compatible type");
    testException("fieldTypeRight2", null, "assigning to field of compatible type");
    testException("fieldTypeRight3", null, "assigning to field of compatible type");
    testException("fieldTypeWrong", TypeMismatchException.class, "assigning to field of wrong type");
    testException("fieldTypeWrong2", TypeMismatchException.class, "assigning to field of wrong type");
    testException("fieldTypeWrong3", TypeMismatchException.class, "assigning to field of wrong type");
    testException("fieldTypeWrong4", TypeMismatchException.class, "assigning to field of wrong type");
    testException("fieldIntTooManyDims", TypeMismatchException.class,
      "assigning to array with wrong # of dimensions");
    testException("fieldObjNotEnoughDims", TypeMismatchException.class,
      "assigning to array with wrong # of dimensions");
  }

  @Test
  public void testParameterAssign() {
    /** #score(3) */
    TestUtil.compileClass("""
      class MyClass {
        void foo() {
        }
        int bar(int a, MyClass b, int[] c, MyClass[] d) {
        }
      }
      """);

    TestUtil.compileClass("""
      class Main {
        void notAnObject() {
          x = 3;
          x.foo();
        }
        void missingMethod() {
          o = new MyClass();
          o.notAThing();
        }
        void goodTypes() {
          o = new MyClass();
          o.bar(3,new MyClass(), new int[3], new MyClass[3]);
        }
        void badType() {
          o = new MyClass();
          o.bar(new MyClass(),new MyClass(), new int[3], new MyClass[3]);
        }
        void badType2() {
          o = new MyClass();
          o.bar(new int[3],new MyClass(), new int[3], new MyClass[3]);
        }
        void badType3() {
          o = new MyClass();
          o.bar(new MyClass[3],new MyClass(), new int[3], new MyClass[3]);
        }
        void badType4() {
          o = new MyClass();
          o.bar(3, 4, new int[3], new MyClass[3]);
        }
        void badType5() {
          o = new MyClass();
          o.bar(3, new MyClass(), 5, new MyClass[3]);
        }
        void badType6() {
          o = new MyClass();
          o.bar(3, new MyClass(), new int[3], new int[3]);
        }
      }
      """);

    testException("notAnObject", TypeMismatchException.class, "calling method of non-object");
    testException("missingMethod", MissingMethodException.class, "calling undefined method");
    testException("goodTypes", null, "calling method with params of compatible types");
    testException("badType", TypeMismatchException.class, "calling method with param of wrong type");
    testException("badType2", TypeMismatchException.class, "calling method with param of wrong type");
    testException("badType3", TypeMismatchException.class, "calling method with param of wrong type");
    testException("badType4", TypeMismatchException.class, "calling method with param of wrong type");
    testException("badType5", TypeMismatchException.class, "calling method with param of wrong type");
    testException("badType6", TypeMismatchException.class, "calling method with param of wrong type");
  }

  @Test
  public void testArrayAssign() {
    /** #score(3) */
    TestUtil.compileClass("""
      class MyClass {
        field int[] arr;
        field int[][] arr2d;
        field MyClass[] oArr;
        field MyClass[][] oArr2d;
      }
      """);

    TestUtil.compileClass("""
      class Main {
        void notAnArray() {
          x = 3;
          x[0] = 4;
        }
        void goodInt() {
          o = new MyClass();
          o.arr = new int[3];
          o.arr[0] = 1;
        }
        void goodObj() {
          o = new MyClass();
          o.oArr = new MyClass[3];
          o.oArr[0] = new MyClass();
        }
        void indexNotIntRead() {
          o = new MyClass();
          o.arr = new int[3];
          x = o.arr[o];
        }
        void indexNotIntWrite() {
          o = new MyClass();
          o.arr = new int[3];
          o.arr[o] = 1;
        }
        void tooManyDimsWrite() {
          o = new MyClass();
          o.arr = new int[3];
          o.arr[0][0] = 1;
        }
        void tooManyDimsRead() {
          o = new MyClass();
          o.arr = new int[3];
          x = o.arr[0][0];
        }
        void notEnoughDimsWrite() {
          o = new MyClass();
          o.arr2d = new int[3][3];
          o.arr2d[0] = 1;
        }
        void goodInt2d() {
          o = new MyClass();
          o.arr2d = new int[3][3];
          o.arr2d[0][0] = 1;
        }
        void goodObj2d() {
          o = new MyClass();
          o.oArr2d = new MyClass[3][3];
          o.oArr2d[0][0] = new MyClass();
        }
        void badInt2d() {
          o = new MyClass();
          o.arr2d = new int[3][3];
          o.arr2d[0][0] = new MyClass();
        }
        void badObj2d() {
          o = new MyClass();
          o.oArr2d = new MyClass[3][3];
          o.oArr2d[0][0] = 3;
        }
      }
      """);

    testException("notAnArray", TypeMismatchException.class, "assigning to element of non-array");
    testException("goodInt", null, "assigning to array");
    testException("goodObj", null, "assigning to array");
    testException("indexNotIntRead", TypeMismatchException.class, "reading an array index that isn't an int");
    testException("indexNotIntWrite", TypeMismatchException.class, "writing an array index that isn't an int");
    testException("tooManyDimsRead", TypeMismatchException.class, "using too many dimensions for array read");
    testException("tooManyDimsWrite", TypeMismatchException.class, "using too many dimensions for array write");
    testException("notEnoughDimsWrite", TypeMismatchException.class, "not using enough dimensions for array write");
    testException("goodInt2d", null, "assigning to 2d array");
    testException("goodObj2d", null, "assigning to 2d array");
    testException("badInt2d", TypeMismatchException.class, "assigning wrong type to 2d array");
    testException("badObj2d", TypeMismatchException.class, "assigning wrong type to 2d array");
  }
}
