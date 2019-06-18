package io.dallen.compiler;

import java.util.List;

public class CompiledFunction extends CompiledVar {
    private final String compiledName;
    private final CompiledType returns;
    private final List<CompiledType> args;

    public CompiledFunction(String name, String compiledName, CompiledType returns, List<CompiledType> args) {
        super(name, CompiledType.FUNCTION);
        this.compiledName = compiledName;
        this.returns = returns;
        this.args = args;
    }

    public CompiledType getReturns() {
        return returns;
    }

    public List<CompiledType> getArgs() {
        return args;
    }

    public String getCompiledName() {
        return compiledName;
    }
}
