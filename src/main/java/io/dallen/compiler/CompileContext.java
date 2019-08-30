package io.dallen.compiler;

import io.dallen.ast.AST;
import io.dallen.errors.ErrorCollector;
import io.dallen.errors.ErrorPrinter;

import java.util.ArrayList;
import java.util.List;

public class CompileContext implements ErrorCollector<AST.Statement> {

    public final static String INDENT = "    ";

    private final CompileScope scope;
    private String indent = "";
    private final CompileContext parent;
    private final List<String> errors;
    private final String code;
    private String scopePrefix = "";
    private CompiledType parentClass = null;

    private int refStackSize = 0;

    private boolean onStack = true;

    public CompileContext(String code) {
        this.parent = null;
        this.errors = new ArrayList<>();
        this.code = code;
        this.scope = new CompileScope(null);
        this.scope.loadBuiltins();
    }

    public CompileContext(CompileContext parent) {
        this.parent = parent;
        this.errors = null;
        this.code = null;
        this.scope = new CompileScope(parent.scope);
        this.indent = parent.indent;
        this.parentClass = parent.parentClass;
        this.onStack = parent.onStack;
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

    @Override
    public void throwError(String msg, AST.Statement stmt) {
        if(parent == null) {
            errors.add(ErrorPrinter.pointToPos(code, stmt.tokens.get(0).pos, msg));
        } else {
            parent.throwError(msg, stmt);
        }
    }

    @Override
    public List<String> getErrors() {
        if(parent == null) {
            return errors;
        }
        return parent.getErrors();
    }
}
