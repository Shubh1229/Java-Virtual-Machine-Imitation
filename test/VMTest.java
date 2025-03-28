import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import types.Value;

public class VMTest {
    @Test
    public void testConst() {
        Map<String, Value> vars = TestUtil.testCode("""
            load_const 42
            store_local a
            """);

        assertEquals(42, vars.get("a").getIntValue());
    }
}
