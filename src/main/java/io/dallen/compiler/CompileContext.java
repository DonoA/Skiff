package io.dallen.compiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CompileContext {
    private Map<String, CompiledObject> variableTable = new HashMap<>();

    private CompileContext parent;
    private String indent = "";

    public CompileContext(CompileContext parent) {
        this.parent = parent;
        if(parent != null) {
            indent = parent.indent;
            addIndent("    ");
        }
        loadBuiltins();
    }

    private void loadBuiltins() {
        delcareObject(CompiledType.VOID);
        delcareObject(CompiledType.STRING);
        delcareObject(CompiledType.INT);
        delcareObject(CompiledType.BOOL);

        delcareObject(new CompiledFunction(
                "println",
                CompiledType.VOID,
                Collections.singletonList(CompiledType.STRING)));
    }

    public void delcareObject(CompiledObject decVar) {
        variableTable.put(decVar.getName(), decVar);
    }

    public CompiledObject getObject(String name) {
        CompiledObject varFor = variableTable.get(name);
        if(varFor != null) {
            return varFor;
        }

        if(parent == null) {
            throw new CompileError("Variable " + name + " not bound");
        }

        return parent.getObject(name);
    }

    public CompiledType getType(String name) {
        CompiledObject varFor = getObject(name);
        if(!(varFor instanceof CompiledType)) {
            throw new CompileError("Variable " + name + " is not a class");
        }

        return (CompiledType) varFor;
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

}
