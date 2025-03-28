import static org.junit.Assert.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import ast.Parser;
import ast.model.ClassDefinition;
import ast.model.Expression;
import ast.model.FieldDefinition;
import ast.model.MethodDefinition;
import ast.model.Statement;
import ast.visitors.CloneVisitor;
import ast.visitors.CompileVisitor;
import ast.visitors.OptimizingVisitor;
import ast.visitors.PrintVisitor;
import ast.visitors.Visitable;
import engine.CompiledClassCache;
import engine.VMThread;
import engine.heap.Heap;
import engine.opcodes.BinaryOp;
import engine.opcodes.BranchOp;
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
import engine.opcodes.SleepOp;
import engine.opcodes.StoreArrayElementOp;
import engine.opcodes.StoreFieldOp;
import engine.opcodes.StoreLocalOp;
import engine.opcodes.YieldOp;
import types.Method;
import types.Value;

/**
 * Useful methods for testing.
 */
public class TestUtil {
    /**
     * Parses VM assembly into opcodes.
     *
     * @param code
     * @return
     */
    public static List<Opcode> parseOpcodes(String code) {
        String[] lines = code.split("\n");

        List<Opcode> ops = new ArrayList<>();
        for (String line : lines) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] tokens = line.split(" ");

            if (tokens[0].endsWith(":")) {
                tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
            }

            ops.add(createOpcode(tokens));
        }
        return ops;
    }

    /**
     * Creates an opcode.
     *
     * @param tokens the tokens for this opcode
     * @return the opcode
     */
    private static Opcode createOpcode(String[] tokens) {
        String opName = tokens[0];

        Operator type = Operator.fromInstruction(opName);
        if (type != null) {
            return new BinaryOp(type);
        }

        if ("load_const".equals(opName)) {
            return new LoadConstOp(Integer.parseInt(tokens[1]));
        }

        if ("load_local".equals(opName)) {
            return new LoadLocalOp(tokens[1]);
        }

        if ("store_local".equals(opName)) {
            return new StoreLocalOp(tokens[1]);
        }

        if ("branch".equals(opName)) {
            return new BranchOp(BranchOp.Type.UNCONDITIONAL, Integer.parseInt(tokens[1]));
        }

        if ("branchT".equals(opName)) {
            return new BranchOp(BranchOp.Type.TRUE, Integer.parseInt(tokens[1]));
        }

        if ("branchF".equals(opName)) {
            return new BranchOp(BranchOp.Type.FALSE, Integer.parseInt(tokens[1]));
        }

        if ("call".equals(opName)) {
            return new CallOp(tokens[1]);
        }

        if ("return".equals(opName)) {
            return new ReturnOp();
        }

        if ("yield".equals(opName)) {
            return new YieldOp();
        }

        if ("sleep".equals(opName)) {
            return new SleepOp(Integer.parseInt(tokens[1]));
        }

        if ("new_object".equals(opName)) {
            return new NewObjectOp(tokens[1]);
        }

        if ("load_field".equals(opName)) {
            return new LoadFieldOp(tokens[1]);
        }

        if ("store_field".equals(opName)) {
            return new StoreFieldOp(tokens[1]);
        }

        if ("load_array_element".equals(opName)) {
            return new LoadArrayElementOp();
        }

        if ("store_array_element".equals(opName)) {
            return new StoreArrayElementOp();
        }

        if ("new_array".equals(opName)) {
            return new NewArrayOp(tokens[1], Integer.parseInt(tokens[2]));
        }

        throw new RuntimeException("Unrecognized opcode: " + opName);
    }

    /**
     * Tests round-tripping the supplied code through the Parser and PrintVisitor.
     *
     * @param code        the code
     * @param parseMethod the Parser method to use for parsing
     */
    private static <T extends Visitable> T testParse(String code, Function<Parser, T> parseMethod) {
        T ast = parseMethod.apply(new Parser(code));
        PrintVisitor v = new PrintVisitor();
        ast.accept(v);
        assertEquals("code incorrect", code, v.getResult());
        return ast;
    }

    /**
     * Tests parsing an expression and converting it back to code.
     *
     * @param code the code
     */
    public static Expression testParseExpression(String code) {
        return TestUtil.testParse(code, Parser::parseExpression);
    }

    /**
     * Tests parsing a field definition and converting it back to code.
     *
     * @param code the code
     */
    public static FieldDefinition testParseField(String code) {
        return TestUtil.testParse(code, Parser::parseField);
    }

    /**
     * Tests parsing a statement and converting it back to code.
     *
     * @param code the code
     */
    public static Statement testParseStatement(String code) {
        return testParse(code, Parser::parseSimpleStatement);
    }

    /**
     * Tests parsing a method and converting it back to code.
     *
     * @param code the code
     */
    public static MethodDefinition testParseMethod(String code) {
        return testParse(code, Parser::parseMethod);
    }

    /**
     * Tests parsing a class and converting it back to code.
     *
     * @param code the code
     */
    public static ClassDefinition testParseClass(String code) {
        return testParse(code, Parser::parseClass);
    }
    
  
    /**
     * Executes the supplied code, and returns the local variables.
     *
     * @param code the code
     * @return the local variables
     */
    public static Map<String, Value> testCode(String code) {
        List<Opcode> ops = parseOpcodes(code);
        Method method = new Method("main", Collections.emptyList(), ops);
        CompiledClassCache.instance().saveMethod("Main", method);
        VMThread e = new VMThread("Main", "main", new Heap());
        return e.run();
    }

    public static void testPrintAST(Visitable root, String expectedCode) {
        PrintVisitor pv = new PrintVisitor();
        root.accept(pv);
        assertEquals("AST incorrect", expectedCode, pv.getResult());
    }

    /**
     * Turns opcodes back into VM assembly instructions.
     *
     * @param opcodes the opcodes
     * @return the disassembly
     */
    public static String disassemble(List<Opcode> opcodes) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < opcodes.size(); i++) {
            Opcode op = opcodes.get(i);
            sb.append(i).append(": ").append(op).append('\n');
        }

        return sb.toString();
    }

    /**
     * Adds line numbers to the supplied code, if they're not already present.
     *
     * @param code the code
     * @return the code with line numbers
     */
    private static String addLineNumbers(String code) {
        String[] lines = code.split("\n");
        if (lines[0].indexOf(":") >= 0) {
            return code;
        }
        for (int i = 0; i < lines.length; i++) {
            lines[i] = i + ": " + lines[i];
        }
        return String.join("\n", lines) + "\n";
    }

    /**
     * Runs the CompileVisitor over the supplied AST, and checks the disassembly.
     *
     * @param root                the AST
     * @param expectedDisassembly the expected disassembly
     */
    public static void testCompileAST(Visitable root, String expectedDisassembly) {
        CompileVisitor v = new CompileVisitor();
        root.accept(v);
        String actualDisassembly = disassemble(v.getOpcodes());
        expectedDisassembly = addLineNumbers(expectedDisassembly);
        assertEquals("generated code incorrect", expectedDisassembly, actualDisassembly);
    }

    public static void testCompileMethod(MethodDefinition method, String expectedDisassembly) {
        CompileVisitor v = new CompileVisitor();
        method.accept(v);

        Method m = CompiledClassCache.instance().resolveMethod("Main", method.getName());
        assertNotNull("Method missing from CompiledClassCache", m);

        String actualDisassembly = disassemble(m.getOpcodes());
        expectedDisassembly = addLineNumbers(expectedDisassembly);
        assertEquals("generated code incorrect", expectedDisassembly, actualDisassembly);
    }

    /**
     * Gets a method from the CompiledClassCache, and checks its disassembly.
     *
     * @param className           the class name
     * @param methodName          the method name
     * @param expectedDisassembly the expected disassembly
     */
    public static void checkMethod(String className, String methodName, String expectedDisassembly) {
        Method method = CompiledClassCache.instance().resolveMethod(className, methodName);
        assertNotNull("method not found", method);
        assertEquals("method name incorrect", methodName, method.getName());
        expectedDisassembly = TestUtil.addLineNumbers(expectedDisassembly);
        assertEquals("method opcodes incorrect", expectedDisassembly, TestUtil.disassemble(method.getOpcodes()));
    }

    /**
     * Compiles a method and saves it in the CompiledClassCache.
     *
     * @param methodName the method name
     * @param code       the code
     */
    public static void compileMethod(String className, String methodName, String code) {
        List<Opcode> ops = parseOpcodes(code);
        Method method = new Method(methodName, new ArrayList<>(), ops);
        CompiledClassCache.instance().saveMethod(className, method);
    }

    /**
     * Compiles a class.
     *
     * @param code the code
     */
    public static void compileClass(String code) {
        Parser parser = new Parser(code);
        Visitable ast = parser.parseClass();
        CompileVisitor v = new CompileVisitor();
        ast.accept(v);
    }

    public static void testCompileStatement(String code, String expectedDisassembly) {
        Statement s = testParseStatement(code);
        testCompileAST(s, expectedDisassembly);
    }

    /**
     * Checks optimization.
     * 
     * @param code         input code
     * @param expectedCode code after optimization
     */
    public static void checkOptimization(String code, String expectedCode) {
        MethodDefinition method = new Parser(code).parseMethod();
        CloneVisitor cv = new CloneVisitor();
        method.accept(cv);
        PrintVisitor pv = new PrintVisitor();
        method.accept(pv);
        assertEquals("clone failed", code, pv.getResult());
        int index = 0;
        while (true) {
            if(index > 10) break;
            OptimizingVisitor ov = new OptimizingVisitor();
            method.accept(ov);
            pv = new PrintVisitor();
            method.accept(pv);
            String newCode = pv.getResult();

            if (newCode.equals(code)) {
                break;
            }

            code = newCode;
            index++;
        }

        assertEquals("optimization failed", expectedCode, pv.getResult());
    }
}
