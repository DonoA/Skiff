package io.dallen.compiler;

/**
 * Common top level class for all objects in the skiff language
 */
public class CompiledObject {
    private final String name;

    CompiledObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
