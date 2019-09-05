package io.dallen.compiler;

public class CompiledField extends CompiledVar {
    private final boolean priv;

    public CompiledField(CompiledVar v, boolean priv) {
        super(v.getName(), v.isParam(), v.getType());
        this.priv = priv;
    }

    public boolean isPrivate() {
        return priv;
    }
}
