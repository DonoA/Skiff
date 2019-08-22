package io.dallen.compiler;

public class CompileContext {

    public final static String INDENT = "    ";

    private CompileScope scope;
    private String indent = "";
    private CompileContext parent;
    private String scopePrefix = "";
    private CompiledType parentClass = null;

    private int refStackSize = 0;

    private boolean onStack = true;

    public CompileContext(CompileContext parent) {
        this.parent = parent;
        if(parent != null) {
            this.scope = new CompileScope(parent.scope);
            this.indent = parent.indent;
            this.parentClass = parent.parentClass;
            this.onStack = parent.onStack;
        } else {
            this.scope = new CompileScope(null);
            this.scope.loadBuiltins();
        }
    }

    public CompileContext addIndent() {
        addIndent(INDENT);
        return this;
    }

    public void declareObject(CompiledObject decVar) {
        scope.declareObject(decVar);
    }

    public CompiledObject getObject(String name) throws NoSuchObjectException {
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

    public int getRefStackSize() {
        return refStackSize;
    }

    public void addRefStackSize(int refStackSize) {
        this.refStackSize += refStackSize;
    }

    public String getScopePrefix() {
        return scopePrefix;
    }

    public CompileContext setScopePrefix(String scopePrefix) {
        this.scopePrefix = scopePrefix;
        return this;
    }

    public CompiledType getParentClass() {
        return parentClass;
    }

    public CompileContext setParentClass(CompiledType parentClass) {
        this.parentClass = parentClass;
        return this;
    }

    public boolean isOnStack() {
    return onStack;
    }

    public CompileContext setOnStack(boolean onStack) {
    this.onStack = onStack;
    return this;
    }

    public CompileScope getScope() {
        return scope;
    }
}
