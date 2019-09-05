package io.dallen.compiler;

public class CompiledCode {
    private String compiledText;
    private CompiledObject binding;
    private CompiledType type;
    private boolean requiresSemicolon;
    private boolean onStack;

    public CompiledCode() {
        this.compiledText = "";
        this.binding = null;
        this.type = null;
        this.requiresSemicolon = true;
    }

    public CompiledCode withText(String compiledText) {
        this.compiledText = compiledText;
        return this;
    }

    public CompiledCode withBinding(CompiledObject binding) {
        this.binding = binding;
        return this;
    }

    public CompiledCode withType(CompiledType type) {
        this.type = type;
        return this;
    }

    public CompiledCode withSemicolon(boolean withSemi) {
        this.requiresSemicolon = withSemi;
        return this;
    }

    public CompiledCode onStack(boolean declaredThisScope) {
        this.onStack = declaredThisScope;
        return this;
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

    public boolean isRequiresSemicolon() {
        return requiresSemicolon;
    }

    public boolean onStack() {
        return onStack;
    }
}
