package io.dallen.compiler;

import java.util.List;

public class CompiledMethod extends CompiledFunction {
    private final boolean mine;
    private final boolean priv;

    public CompiledMethod(String name, String compiledName, boolean isConstructor, CompiledType returns,
                          List<CompiledVar> args, boolean mine, boolean priv) {
        super(name, compiledName, isConstructor, returns, args);
        this.mine = mine;
        this.priv = priv;
    }

    public CompiledMethod(CompiledFunction func, boolean mine, boolean priv) {
        this(func.getName(), func.getCompiledName(), func.isConstructor(), func.getReturns(), func.getArgs(), mine, priv);
    }

    public boolean isMine() {
        return mine;
    }

    public boolean isPrivate() {
        return priv;
    }
}
