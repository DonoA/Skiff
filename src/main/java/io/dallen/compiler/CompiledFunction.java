package io.dallen.compiler;

import java.util.List;

public class CompiledFunction extends CompiledVar {
    private final CompiledType returns;
    private final List<CompiledType> args;

    public CompiledFunction(String name, CompiledType returns, List<CompiledType> args) {
        super(name, CompiledType.FUNCTION);
        this.returns = returns;
        this.args = args;
    }

    public CompiledType getReturns() {
        return returns;
    }

    public List<CompiledType> getArgs() {
        return args;
    }
}
