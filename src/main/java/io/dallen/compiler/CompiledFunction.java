package io.dallen.compiler;

import io.dallen.ast.AST;

import java.util.List;
import java.util.Optional;

public class CompiledFunction extends CompiledVar {
    private final String compiledName;
    private final CompiledType returns;
    private final List<CompiledVar> args;
    private final boolean isConstructor;

    public CompiledFunction(String name, String compiledName, boolean isConstructor, CompiledType returns,
                            List<CompiledVar> args) {
        super(name, false, BuiltinTypes.FUNCTION);
        this.isConstructor = isConstructor;
        this.compiledName = compiledName;
        this.returns = returns;
        this.args = args;
    }



    public CompiledFunction(String name, String compiledName, List<CompiledVar> args) {
        this(name, compiledName, false, BuiltinTypes.VOID, args);
    }

    public CompiledType getReturns() {
        return returns;
    }

    public List<CompiledVar> getArgs() {
        return args;
    }

    public String getCompiledName() {
        return compiledName;
    }

    public boolean isConstructor() {
        return isConstructor;
    }
}
