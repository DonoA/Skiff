package io.dallen.compiler;

public class CompiledCode {
    private String compiledText;
    private CompiledObject binding;
    private CompiledType type;

    public CompiledCode(String compiledText, CompiledObject binding, CompiledType type) {
        this.compiledText = compiledText;
        this.binding = binding;
        this.type = type;
    }

    public CompiledCode(String compiledText, CompiledType type) {
        this(compiledText, null, type);
    }

    public String getCompiledText() {
        return compiledText;
    }

    public CompiledObject getBinding() {
        return binding;
    }

    public CompiledType getType() {
        return type;
    }
}
