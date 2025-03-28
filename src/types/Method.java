package types;

import java.util.List;

import ast.model.ParameterDefinition;
import engine.opcodes.Opcode;

/**
 * Represents a compiled method.
 */
public class Method {
    private String name;
    private List<Opcode> opcodes;
    private List<ParameterDefinition> parameters;

    public Method(String name, List<ParameterDefinition> parameters, List<Opcode> opcodes) {
        this.name = name;
        this.parameters = parameters;
        this.opcodes = opcodes;
    }

    public String getName() {
        return name;
    }

    public List<Opcode> getOpcodes() {
        return opcodes;
    }

    public List<ParameterDefinition> getParameters() {
        return parameters;
    }
}
