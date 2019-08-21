package io.dallen.compiler;

import java.util.List;

public class CompiledFunction extends CompiledVar {
    private final String compiledName;
    private final CompiledType returns;
    private final List<CompiledType> args;
    private final boolean isConstructor;

    public CompiledFunction(String name, String compiledName, boolean isConstructor, CompiledType returns, List<CompiledType> args) {
        super(name, false, CompiledType.FUNCTION);
        this.isConstructor = isConstructor;
        this.compiledName = compiledName;
        this.returns = returns;
        this.args = args;
    }

    public CompiledFunction(String name, String compiledName, List<CompiledType> args) {
        this(name, compiledName, false, CompiledType.VOID, args);
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

    public boolean isConstructor() {
        return isConstructor;
    }
}
