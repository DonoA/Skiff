package io.dallen.compiler;

public class CompileContext {

    private CompileScope scope;
    private String indent = "";
    private CompileContext parent;

    private int dataStackSize = 0;
    private int refStackSize = 0;

    public CompileContext(CompileContext parent) {
        this.parent = parent;
        if(parent != null) {
            this.scope = new CompileScope(parent.scope);
            addIndent("    ");
        } else {
            this.scope = new CompileScope(null);
            this.scope.loadBuiltins();
        }
    }

    public void declareObject(CompiledObject decVar) {
        scope.declareObject(decVar);
    }

    public CompiledObject getObject(String name) {
        return scope.getObject(name);
    }

    public CompiledType getType(String name) {
        return scope.getType(name);
    }

    public String getIndent() {
        return indent;
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    public void addIndent(String newIndent) {
        indent = indent + newIndent;
    }

    public int getDataStackSize() {
        return dataStackSize;
    }

    public void addDataStackSize(int dataStackSize) {
        this.dataStackSize += dataStackSize;
    }

    public int getRefStackSize() {
        return refStackSize;
    }

    public void addRefStackSize(int refStackSize) {
        this.refStackSize += refStackSize;
    }
}
