package io.dallen.compiler;

import io.dallen.ast.AST;
import io.dallen.errors.ErrorCollector;
import io.dallen.errors.ErrorPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CompileContext implements ErrorCollector<AST.Statement> {

    public final static String INDENT = "    ";

    private final CompileScope scope;
    private String indent = "";
    private final CompileContext parent;
    private final List<String> errors;
    private final List<String> dependents;
    private final String code;
    private String scopePrefix = "";
    private CompiledType containingClass = null;
    private int globalCounter = 1;
    private final boolean debug;

    private int refStackSize = 0;

    private boolean onStack = true;

    public CompileContext(String code, boolean debug) {
        this.parent = null;
        this.errors = new ArrayList<>();
        this.code = code;
        this.dependents = new ArrayList<>();
        this.debug = debug;
        this.scope = new CompileScope(null);
        this.scope.loadBuiltins();
    }

    public CompileContext(CompileContext parent) {
        this(parent, false);
    }


    public CompileContext(CompileContext parent, boolean funcContext) {
        this.parent = parent;
        this.errors = null;
        this.code = null;
        if(funcContext) {
            this.dependents = new ArrayList<>();
        } else {
            this.dependents = null;
        }
        this.scope = new CompileScope(parent.scope);
        this.indent = parent.indent;
        this.containingClass = parent.containingClass;
        this.onStack = parent.onStack;
        this.debug = parent.debug;
    }

    public CompileContext addIndent() {
        addIndent(INDENT);
        return this;
    }

    public void declareObject(CompiledObject decVar) {
        scope.declareObject(decVar);
    }

    public CompiledObject getObject(String name) throws NoSuchElementException {
        return scope.getObject(name);
    }

    public String getIndent() {
        return indent;
    }

    public CompileContext setIndent(String indent) {
        this.indent = indent;
        return this;
    }

    public void addIndent(String newIndent) {
        indent = indent + newIndent;
    }

    public int getRefStackSize() {
        return refStackSize;
    }

    public int getFullRefStackSize() {
        if(this.parent != null) {
            return refStackSize + this.parent.getFullRefStackSize();
        } else {
            return refStackSize;
        }
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

    public CompiledType getContainingClass() {
        return containingClass;
    }

    public CompileContext setContainingClass(CompiledType containingClass) {
        this.containingClass = containingClass;
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

    public int getGlobalCounter() {
        if(parent == null) {
            return this.globalCounter++;
        } else {
            return this.parent.getGlobalCounter();
        }
    }

    public void addDependentCode(String code) {
        if(this.dependents == null) {
            this.parent.addDependentCode(code);
        } else {
            this.dependents.add(code);
        }
    }

    public List<String> getDependentCode() {
        if(this.dependents == null) {
            return this.parent.getDependentCode();
        } else {
            return dependents;
        }
    }

    public boolean isDebug() {
        return debug;
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
