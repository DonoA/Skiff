package io.dallen.compiler;

public class CompiledMethod extends CompiledFunction {
    private final boolean mine;
    private final boolean priv;

    public CompiledMethod(CompiledFunction func, boolean mine, boolean priv) {
        super(func.getName(), func.getCompiledName(), func.isConstructor(), func.getReturns(), func.getArgs());
        this.mine = mine;
        this.priv = priv;
    }

    public boolean isMine() {
        return mine;
    }

    public boolean isPrivate() {
        return priv;
    }
}
