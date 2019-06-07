package io.dallen.compiler;

public class CompiledType extends CompiledObject {
    public static final CompiledType CLASS = new CompiledType("Class", 0);
    public static final CompiledType VOID = new CompiledType("Void", 0);
    public static final CompiledType FUNCTION = new CompiledType("Function", 0);

    public static final CompiledType STRING = new CompiledType("String", 0);
    public static final CompiledType INT = new CompiledType("Int", 4);

    private final int size;

    public CompiledType(String className, int size) {
        super(className);
        this.size = size;
    }

    public int getSize() {
        return size;
    }

}
