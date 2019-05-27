package io.dallen.compiler;

public class CompiledType {
    public static final CompiledType CLASS = new CompiledType("Class", 0);
    public static final CompiledType NONE = new CompiledType("None", 0);
    public static final CompiledType VOID = new CompiledType("None", 0);
    public static final CompiledType FUNCTION = new CompiledType("Function", 0);

    public static final CompiledType STRING = new CompiledType("String", 0);
    public static final CompiledType NUMBER = new CompiledType("String", 0);

    private final String compiledText;
    private final int size;

    public CompiledType(String compiledText, int size) {
        this.compiledText = compiledText;
        this.size = size;
    }

    public String getCompiledText() {
        return compiledText;
    }

    public int getSize() {
        return size;
    }
}
