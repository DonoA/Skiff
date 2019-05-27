package io.dallen.compiler;

public class CompiledVar {
    private final String name;
    private final CompiledType type;

    public CompiledVar(String name, CompiledType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public CompiledType getType() {
        return type;
    }
}
