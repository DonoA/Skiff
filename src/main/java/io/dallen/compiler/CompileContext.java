package io.dallen.compiler;

import java.util.*;

public class CompileContext {
    private Map<String, CompiledObject> variableTable = new HashMap<>();

    private CompileContext parent;
    private String indent = "";

    private int dataStackSize = 0;
    private int refStackSize = 0;

    public CompileContext(CompileContext parent) {
        this.parent = parent;
        if(parent != null) {
            indent = parent.indent;
            addIndent("    ");
        } else {
            loadBuiltins();
        }
    }

    private void loadBuiltins() {
        declareObject(CompiledType.VOID);
        declareObject(CompiledType.STRING);
        declareObject(CompiledType.INT);
        declareObject(CompiledType.BOOL);
        declareObject(CompiledType.LIST);

        declareObject(new CompiledFunction(
                "println",
                CompiledType.VOID,
                Collections.singletonList(CompiledType.STRING)));
    }

    public void declareObject(CompiledObject decVar) {
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

    public List<CompiledObject> getLocals() {
        return new ArrayList<>(variableTable.values());
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
