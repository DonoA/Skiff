package io.dallen.compiler;

public class CompiledCode {
    private String compiledText;
    private CompiledType returnType;

    public CompiledCode(String compiledText, CompiledType returnType) {
        this.compiledText = compiledText;
        this.returnType = returnType;
    }

    public String getCompiledText() {
        return compiledText;
    }

    public void setCompiledText(String compiledText) {
        this.compiledText = compiledText;
    }

    public CompiledType getReturnType() {
        return returnType;
    }

    public void setReturnType(CompiledType returnType) {
        this.returnType = returnType;
    }
}
