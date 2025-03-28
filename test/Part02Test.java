import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import ast.model.ParameterDefinition;
import engine.CompiledClassCache;
import types.DataType;
import types.Method;
import types.Value;

public class Part02Test extends TestBase {
    /**
     * Test old code still runs.
     */
    @Test
    public void testSimpleCodeStillWorks() {
        /** #score(5) */
        hintContext = "load_const, store_local";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 42
            store_local a
            """);

        assertEquals(hint("var had wrong value"), 42, vars.get("a").getIntValue());

        hintContext = "binary operations";
        vars = TestUtil.testCode("""
            load_const 12
            load_const 23
            add
            load_const 21
            sub
            store_local a
            """);

        assertEquals(hint("var had wrong value"), 14, vars.get("a").getIntValue());
    }

    /**
     * Test branching code still runs.
     */
    @Test
    public void testBranchingCodeStillWorks() {
        /** #score(5) */
        hintContext = "complicated example";
        Map<String, Value> vars = TestUtil.testCode("""
            0: load_const 0
            1: store_local a
            2: load_const 0
            3: store_local b
            4: load_local b
            5: load_const 100
            6: cmpGT
            7: branchT 17
            8: load_local a
            9: load_local b
            10: add
            11: store_local a
            12: load_local b
            13: load_const 1
            14: add
            15: store_local b
            16: branch 4
            17: load_const 1
            18: store_local c
            """);

        assertEquals(hint("var had wrong value"), 5050, vars.get("a").getIntValue());
        assertEquals(hint("var had wrong value"), 101, vars.get("b").getIntValue());
        assertEquals(hint("var had wrong value"), 1, vars.get("c").getIntValue());
    }

    @Test
    public void testCallAndReturn() {
        /** #score(5) */
        hintContext = "call a function and return";
        Method ctor = new Method(
            "init",
            List.of(), TestUtil.parseOpcodes(
                "return\n"));
        CompiledClassCache.instance().saveMethod("Main", ctor);

        Method foo = new Method(
            "foo",
            List.of(),
            TestUtil.parseOpcodes("""
                load_const 5
                return
                """));
        CompiledClassCache.instance().saveMethod("Main", foo);

        Map<String, Value> vars = TestUtil.testCode("""
            new_object Main
            store_local o
            load_local o
            call foo
            store_local a
            load_const 123
            load_local o
            call foo
            load_const 234
            load_local o
            call foo
            store_local b
            store_local c
            store_local d
            store_local e
            """);

        assertEquals(hint("local had wrong value"), 5, vars.get("a").getIntValue());
        assertEquals(hint("local had wrong value"), 5, vars.get("b").getIntValue());
        assertEquals(hint("local had wrong value"), 234, vars.get("c").getIntValue());
        assertEquals(hint("local had wrong value"), 5, vars.get("d").getIntValue());
        assertEquals(hint("local had wrong value"), 123, vars.get("e").getIntValue());
    }

    @Test
    public void testFuncWithArgs() {
        /** #score(5) */
        hintContext = "function call with arguments";

        Method addMethod = new Method(
            "sub",
            List.of(
                new ParameterDefinition("x", DataType.INT),
                new ParameterDefinition("y", DataType.INT)),
            TestUtil.parseOpcodes("""
                load_local x
                load_local y
                sub
                return
                """));
        CompiledClassCache.instance().saveMethod("Main", addMethod);

        Method maxMethod = new Method(
            "max",
            List.of(
                new ParameterDefinition("x", DataType.INT),
                new ParameterDefinition("y", DataType.INT)),
            TestUtil.parseOpcodes("""
                0: load_local x
                1: load_local y
                2: cmpGT
                3: branchF 7
                4: load_local x
                5: return
                6: branch 9
                7: load_local y
                8: return
                """));
        CompiledClassCache.instance().saveMethod("Main", maxMethod);

        Map<String, Value> vars = TestUtil.testCode("""
            new_object Main
            store_local o
            load_const 33
            load_const 12
            load_local o
            call sub
            store_local x
            load_const 12
            load_const 33
            load_local o
            call max
            store_local y
            load_const 33
            load_const 12
            load_local o
            call max
            store_local z
            """);

        assertEquals(hint("local had wrong value"), 21, vars.get("x").getIntValue());
        assertEquals(hint("local had wrong value"), 33, vars.get("y").getIntValue());
        assertEquals(hint("local had wrong value"), 33, vars.get("z").getIntValue());
    }

    @Test
    public void testFuncNested() {
        /** #score(5) */
        hintContext = "function calling another function";

        Method addMethod = new Method(
            "add",
            List.of(
                new ParameterDefinition("x", DataType.INT),
                new ParameterDefinition("y", DataType.INT)),
            TestUtil.parseOpcodes("""
                load_local x
                load_local y
                add
                return
                """));
        CompiledClassCache.instance().saveMethod("Main", addMethod);

        Method fooMethod = new Method(
            "foo",
            List.of(
                new ParameterDefinition("x", DataType.INT),
                new ParameterDefinition("y", DataType.INT)),
            TestUtil.parseOpcodes("""
                load_local x
                load_local y
                load_local this
                call add
                load_local y
                load_local this
                call add
                return
                """));
        CompiledClassCache.instance().saveMethod("Main", fooMethod);

        Map<String, Value> vars = TestUtil.testCode("""
            new_object Main
            store_local o
            load_const 12
            load_const 33
            load_local o
            call foo
            store_local x
            """);

        assertEquals(hint("local had wrong value"), 78, vars.get("x").getIntValue());
    }

    @Test
    public void testRecursion() {
        /** #score(5) */
        hintContext = "recursive function";

        Method factorialMethod = new Method(
            "factorial",
            List.of(new ParameterDefinition("x", DataType.INT)),
            TestUtil.parseOpcodes("""
                0: load_local x
                1: load_const 1
                2: cmpGT
                3: branchT 6
                4: load_const 1
                5: return
                6: load_local x
                7: load_local x
                8: load_const 1
                9: sub
                10: load_local this
                11: call factorial
                12: mul
                13: return
                """));
        CompiledClassCache.instance().saveMethod("Main", factorialMethod);

        Map<String, Value> vars = TestUtil.testCode("""
            new_object Main
            store_local o
            load_const 3
            load_local o
            call factorial
            load_const 4
            load_local o
            call factorial
            store_local y
            store_local x
            """);

        assertEquals(hint("local had wrong value"), 6, vars.get("x").getIntValue());
        assertEquals(hint("local had wrong value"), 24, vars.get("y").getIntValue());
    }

    @Test
    public void testRecursion2() {
        /** #score(5) */
        hintContext = "recursive function with multiple recursive calls";

        Method factorialMethod = new Method(
            "fib",
            List.of(new ParameterDefinition("x", DataType.INT)),
            TestUtil.parseOpcodes("""
                0: load_local x
                1: load_const 2
                2: cmpGT
                3: branchT 6
                4: load_const 1
                5: return
                6: load_local x
                7: load_const 2
                8: sub
                9: load_local this
                10: call fib
                11: load_local x
                12: load_const 1
                13: sub
                14: load_local this
                15: call fib
                16: add
                17: return
                """));
        CompiledClassCache.instance().saveMethod("Main", factorialMethod);

        Map<String, Value> vars = TestUtil.testCode("""
            new_object Main
            store_local o
            load_const 5
            load_local o
            call fib
            load_const 7
            load_local o
            call fib
            store_local y
            store_local x
            """);

        assertEquals(hint("local had wrong value"), 5, vars.get("x").getIntValue());
        assertEquals(hint("local had wrong value"), 13, vars.get("y").getIntValue());
    }
}
