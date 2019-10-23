package io.dallen.compiler;


import io.dallen.ast.AST;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data class for a compiled skiff function
 */
public class CompiledFunction extends CompiledVar {
    private final String compiledName;
    private final CompiledType returns;
    private final List<CompiledVar> args;
    private final AST.FunctionDef originalDef;

    public CompiledFunction(String name, String compiledName, List<CompiledVar> args, CompiledType returns,
                            AST.FunctionDef originalDef) {
        super(name, false, BuiltinTypes.FUNCTION);
        this.compiledName = compiledName;
        this.returns = returns;
        this.args = args;
        this.originalDef = originalDef;
    }

    public CompiledFunction(String name, String compiledName, List<CompiledVar> args, CompiledType returns) {
        this(name, compiledName, args, returns, null);
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

    public AST.FunctionDef getOriginalDef() {
        return originalDef;
    }
}
