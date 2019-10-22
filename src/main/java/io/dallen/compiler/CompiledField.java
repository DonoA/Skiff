package io.dallen.compiler;

/**
 * A compiled var defined within a compiled class
 */
public class CompiledField extends CompiledVar {
    private final boolean priv;
    private final boolean mine;

    public CompiledField(CompiledVar v, boolean mine, boolean priv) {
        super(v.getName(), v.isParam(), v.getType());
        this.mine = mine;
        this.priv = priv;
    }

    public boolean isPrivate() {
        return priv;
    }

    public boolean isMine() {
        return mine;
    }
}
