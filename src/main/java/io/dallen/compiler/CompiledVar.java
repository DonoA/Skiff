package io.dallen.compiler;

public class CompiledVar extends CompiledObject {

    private final CompiledType type;

    public CompiledVar(String name, CompiledType type) {
        super(name);
        this.type = type;
    }

    public CompiledType getType() {
        return type;
    }
}
