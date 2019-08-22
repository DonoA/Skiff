package io.dallen.compiler;

import java.util.*;

public class CompileScope {
    private Map<String, CompiledObject> variableTable = new HashMap<>();

    private CompileScope parent;

    public CompileScope(CompileScope parent) {
        this.parent = parent;
    }

    public void loadBuiltins() {
        declareObject(CompiledType.VOID);
        declareObject(CompiledType.STRING);
        declareObject(CompiledType.INT);
        declareObject(CompiledType.BOOL);
        declareObject(CompiledType.LIST);

        declareObject(new CompiledFunction(
            "println",
            "skiff_println",
              Collections.singletonList(CompiledType.STRING)));
    }

    public void declareObject(CompiledObject decVar) {
        variableTable.put(decVar.getName(), decVar);
    }

    public CompiledObject getObject(String name) throws NoSuchObjectException {
        CompiledObject varFor = variableTable.get(name);
        if(varFor != null) {
            return varFor;
        }

        if(parent == null) {

            throw new NoSuchObjectException(name);
        }

        return parent.getObject(name);
    }

    public CompiledType getType(String name) {
        CompiledObject varFor = getObject(name);
        if(!(varFor instanceof CompiledType)) {
            throw new CompileError("Variable '" + name + "' is not a class");
        }

        return (CompiledType) varFor;
    }

    public CompiledFunction getFunction(String name) {
        CompiledObject varFor = getObject(name);
        if(!(varFor instanceof CompiledFunction)) {
            throw new CompileError("Variable '" + name + "' is not a function");
        }

        return (CompiledFunction) varFor;
    }

    public CompiledVar getVar(String name) {
        CompiledObject varFor = getObject(name);
        if(!(varFor instanceof CompiledVar)) {
            throw new CompileError("Variable '" + name + "' is not a variable");
        }

        return (CompiledVar) varFor;
    }

    public List<CompiledObject> getLocals() {
        return new ArrayList<>(variableTable.values());
    }

}
