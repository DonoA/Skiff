package io.dallen.compiler;

public class CompiledVar extends CompiledObject {

    private final CompiledType type;
    private final boolean isParam;

    public CompiledVar(String name, boolean isParam, CompiledType type) {
        super(name);
        this.type = type;
        this.isParam = isParam;
    }

    public CompiledType getType() {
        return type;
    }

    public boolean isParam() {
        return isParam;
    }
}
