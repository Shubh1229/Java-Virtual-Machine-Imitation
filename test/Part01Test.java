import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import types.Value;

public class Part01Test extends TestBase {

    /**
     * Test load_const, store_local.
     */
    @Test
    public void testConstStore() {
        /** #score(5) */
        hintContext = "load_const, store_local";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 42
            store_local a
            """);

        assertEquals(hint("var had wrong value"), 42, vars.get("a").getIntValue());

        vars = TestUtil.testCode("""
            load_const 12
            load_const 23
            store_local a
            store_local b
            """);

        assertEquals(hint("var had wrong value"), 23, vars.get("a").getIntValue());
        assertEquals(hint("var had wrong value"), 12, vars.get("b").getIntValue());
    }

    /**
     * Test load_local.
     */
    @Test
    public void testLoadLocal() {
        /** #score(5) */
        hintContext = "load_local";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 42
            load_const 13
            store_local d
            store_local c
            load_local d
            store_local b
            load_local c
            store_local a
            """);

        assertEquals(hint("var had wrong value"), 42, vars.get("a").getIntValue());
        assertEquals(hint("var had wrong value"), 13, vars.get("b").getIntValue());
        assertEquals(hint("var had wrong value"), 42, vars.get("c").getIntValue());
        assertEquals(hint("var had wrong value"), 13, vars.get("d").getIntValue());
    }

    /**
     * Test add.
     */
    @Test
    public void testAdd() {
        /** #score(1) */
        hintContext = "add";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 42
            load_const 13
            add
            store_local a
            """);

        assertEquals(hint("var had wrong value"), 55, vars.get("a").getIntValue());

        vars = new HashMap<>();
        vars = TestUtil.testCode("""
            load_const 42
            load_const 13
            load_const 9
            add
            add
            store_local a
            """);

        assertEquals(hint("var had wrong value"), 64, vars.get("a").getIntValue());
    }

    /**
     * Test sub.
     */
    @Test
    public void testSub() {
        /** #score(1) */
        hintContext = "sub";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 42
            load_const 13
            sub
            store_local a
            """);

        assertEquals(hint("var had wrong value"), 29, vars.get("a").getIntValue());
    }

    /**
     * Test mul.
     */
    @Test
    public void testMul() {
        /** #score(1) */
        hintContext = "mul";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 4
            load_const 13
            mul
            store_local a
            """);

        assertEquals(hint("var had wrong value"), 52, vars.get("a").getIntValue());
    }

    /**
     * Test div.
     */
    @Test
    public void testDiv() {
        /** #score(1) */
        hintContext = "div";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 42
            load_const 7
            div
            store_local a
            """);

        assertEquals(hint("var had wrong value"), 6, vars.get("a").getIntValue());
    }

    /**
     * Test mod.
     */
    @Test
    public void testMod() {
        /** #score(1) */
        hintContext = "mod";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 42
            load_const 5
            mod
            store_local a
            """);

        assertEquals(hint("var had wrong value"), 2, vars.get("a").getIntValue());
    }

    /**
     * Test eq.
     */
    @Test
    public void testEq() {
        /** #score(1) */
        hintContext = "cmpEQ";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 9
            load_const 10
            cmpEQ
            store_local a
            load_const 10
            load_const 10
            cmpEQ
            store_local b
            load_const 11
            load_const 10
            cmpEQ
            store_local c
            """);

        assertEquals(hint("var had wrong value"), 0, vars.get("a").getIntValue());
        assertEquals(hint("var had wrong value"), 1, vars.get("b").getIntValue());
        assertEquals(hint("var had wrong value"), 0, vars.get("c").getIntValue());
    }

    /**
     * Test ne.
     */
    @Test
    public void testNe() {
        /** #score(1) */
        hintContext = "cmpNEQ";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 9
            load_const 10
            cmpNEQ
            store_local a
            load_const 10
            load_const 10
            cmpNEQ
            store_local b
            load_const 11
            load_const 10
            cmpNEQ
            store_local c
            """);

        assertEquals(hint("var had wrong value"), 1, vars.get("a").getIntValue());
        assertEquals(hint("var had wrong value"), 0, vars.get("b").getIntValue());
        assertEquals(hint("var had wrong value"), 1, vars.get("c").getIntValue());
    }

    /**
     * Test lt.
     */
    @Test
    public void testLt() {
        /** #score(1) */
        hintContext = "cmpLT";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 9
            load_const 10
            cmpLT
            store_local a
            load_const 10
            load_const 10
            cmpLT
            store_local b
            load_const 11
            load_const 10
            cmpLT
            store_local c
            """);

        assertEquals(hint("var had wrong value"), 1, vars.get("a").getIntValue());
        assertEquals(hint("var had wrong value"), 0, vars.get("b").getIntValue());
        assertEquals(hint("var had wrong value"), 0, vars.get("c").getIntValue());
    }

    /**
     * Test lte.
     */
    @Test
    public void testLte() {
        /** #score(1) */
        hintContext = "cmpLTE";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 9
            load_const 10
            cmpLTE
            store_local a
            load_const 10
            load_const 10
            cmpLTE
            store_local b
            load_const 11
            load_const 10
            cmpLTE
            store_local c
            """);

        assertEquals(hint("var had wrong value"), 1, vars.get("a").getIntValue());
        assertEquals(hint("var had wrong value"), 1, vars.get("b").getIntValue());
        assertEquals(hint("var had wrong value"), 0, vars.get("c").getIntValue());
    }

    /**
     * Test gt.
     */
    @Test
    public void testGt() {
        /** #score(1) */
        hintContext = "cmpGT";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 9
            load_const 10
            cmpGT
            store_local a
            load_const 10
            load_const 10
            cmpGT
            store_local b
            load_const 11
            load_const 10
            cmpGT
            store_local c
            """);

        assertEquals(hint("var had wrong value"), 0, vars.get("a").getIntValue());
        assertEquals(hint("var had wrong value"), 0, vars.get("b").getIntValue());
        assertEquals(hint("var had wrong value"), 1, vars.get("c").getIntValue());
    }

    /**
     * Test gte.
     */
    @Test
    public void testGte() {
        /** #score(1) */
        hintContext = "cmpGTE";
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 9
            load_const 10
            cmpGTE
            store_local a
            load_const 10
            load_const 10
            cmpGTE
            store_local b
            load_const 11
            load_const 10
            cmpGTE
            store_local c
            """);

        assertEquals(hint("var had wrong value"), 0, vars.get("a").getIntValue());
        assertEquals(hint("var had wrong value"), 1, vars.get("b").getIntValue());
        assertEquals(hint("var had wrong value"), 1, vars.get("c").getIntValue());
    }

    /**
     * Test branch.
     */
    @Test
    public void testBranch() {
        /** #score(4) */
        hintContext = "branch";
        Map<String, Value> vars = TestUtil.testCode("""
            0: load_const 12
            1: branch 3
            2: load_const 23
            3: store_local a
            """);

        assertEquals(hint("var had wrong value"), 12, vars.get("a").getIntValue());
    }

    /**
     * Test branchT.
     */
    @Test
    public void testBranchT() {
        /** #score(4) */
        hintContext = "branchT";
        Map<String, Value> vars = TestUtil.testCode("""
            0: load_const 0
            1: branchT 4
            2: load_const 1
            3: store_local a
            4: load_const 1
            5: branchT 8
            6: load_const 1
            7: store_local b
            8: load_const 1
            9: store_local c
            """);

        assertEquals(hint("var had wrong value"), 1, vars.get("a").getIntValue());
        assertNull(hint("var had wrong value"), vars.get("b"));
        assertEquals(hint("var had wrong value"), 1, vars.get("c").getIntValue());
    }

    /**
     * Test branchF.
     */
    @Test
    public void testBranchF() {
        /** #score(4) */
        hintContext = "branchF";
        Map<String, Value> vars = TestUtil.testCode("""
            0: load_const 0
            1: branchF 4
            2: load_const 1
            3: store_local a
            4: load_const 1
            5: branchF 8
            6: load_const 1
            7: store_local b
            8: load_const 1
            9: store_local c
            """);

        assertNull(hint("var had wrong value"), vars.get("a"));
        assertEquals(hint("var had wrong value"), 1, vars.get("b").getIntValue());
        assertEquals(hint("var had wrong value"), 1, vars.get("c").getIntValue());
    }

    /**
     * Test loop.
     */
    @Test
    public void testLoop() {
        /** #score(2) */
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
}
