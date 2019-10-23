package io.dallen.compiler;

import io.dallen.ast.AST;

import java.util.List;

/**
 * A compiled function that belongs to a compiled type
 */
public class CompiledMethod extends CompiledFunction {
    private final boolean mine;
    private final boolean priv;
    private final boolean ctr;

    public CompiledMethod(String name, String compiledName, List<CompiledVar> args, CompiledType returns, boolean mine,
                          boolean priv, boolean isConstructor, AST.FunctionDef originalDef) {
        super(name, compiledName, args, returns, originalDef);
        this.mine = mine;
        this.priv = priv;
        this.ctr = isConstructor;
    }

    public CompiledMethod(String name, String compiledName, List<CompiledVar> args, CompiledType returns,
                          boolean mine, boolean priv) {
        this(name, compiledName, args,returns,  mine, priv, false, null);
    }

    public CompiledMethod(CompiledFunction func, boolean mine, boolean priv, boolean isConstructor) {
        this(func.getName(), func.getCompiledName(), func.getArgs(), func.getReturns(), mine, priv, isConstructor,
                null);
    }

    public boolean isMine() {
        return mine;
    }

    public boolean isPrivate() {
        return priv;
    }

    public boolean isConstructor() {
        return ctr;
    }
}
