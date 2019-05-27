package io.dallen.compiler;

import java.util.HashMap;
import java.util.Map;

public class CompileContext {
    private Map<String, CompiledVar> variableTable = new HashMap<>();
    private Map<String, CompiledType> knownTypes = new HashMap<>();

    private CompileContext parent;
    private String indent = "";

    public CompileContext(CompileContext parent) {
        this.parent = parent;
    }

    public void delcareVar(CompiledVar decVar) {
        variableTable.put(decVar.getName(), decVar);
    }

    public CompiledVar getVar(String name) {
        CompiledVar varFor = variableTable.get(name);
        if(varFor != null) {
            return varFor;
        }

        if(parent == null) {
            throw new CompileError("Variable " + name + " not bound");
        }

        return parent.getVar(name);
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

    public void defineType(CompiledType typ) {
        knownTypes.put(typ.getCompiledText(), typ);
    }

    public CompiledType getType(String name) {
        CompiledType typeFor = knownTypes.get(name);
        if(typeFor != null) {
            return typeFor;
        }

        if(parent == null) {
            throw new CompileError("Type " + name + " not bound");
        }

        return parent.getType(name);
    }
}
