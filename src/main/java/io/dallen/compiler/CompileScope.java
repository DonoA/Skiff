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

}
