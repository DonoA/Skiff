package io.dallen.compiler;

public class CompiledType extends CompiledObject {
    public static final CompiledType CLASS = new CompiledType("Class", 0);
    public static final CompiledType NONE = new CompiledType("None", 0); //  are these the same?
    public static final CompiledType VOID = new CompiledType("Void", 0);
    public static final CompiledType FUNCTION = new CompiledType("Function", 0);

    private final int size;

    public CompiledType(String className, int size) {
        super(className);
        this.size = size;
    }

    public int getSize() {
        return size;
    }

}
