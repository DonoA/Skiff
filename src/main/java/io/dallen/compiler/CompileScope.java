package io.dallen.compiler;

import java.util.*;
import java.util.stream.Collectors;

public class CompileScope {
    private Map<String, CompiledObject> variableTable = new HashMap<>();
    private Map<String, List<CompiledFunction>> functionTable = new HashMap<>();

    private CompileScope parent;

    public CompileScope(CompileScope parent) {
        this.parent = parent;
    }

    public void loadBuiltins() {
        declareObject(BuiltinTypes.VOID);
        declareObject(BuiltinTypes.STRING);
        declareObject(BuiltinTypes.INT);
        declareObject(BuiltinTypes.BOOL);
        declareObject(BuiltinTypes.ANYREF);
//        declareObject(BuiltinTypes.CLASS);
    }

    public void declareObject(CompiledObject decVar) {
        try {
            getObject(decVar.getName());
            throw new UnsupportedOperationException("Cannot redefine variable " + decVar.getName());
        } catch(NoSuchElementException ex) {
            variableTable.put(decVar.getName(), decVar);
        }
    }

    public void declareFunction(CompiledFunction decFunc) {
        try {
            getFunction(decFunc.getName(), decFunc.getArgs().stream().map(CompiledVar::getType).collect(Collectors.toList()));
            throw new UnsupportedOperationException("Cannot redefine variable " + decFunc.getName());
        } catch(NoSuchElementException ex) {
            if(functionTable.containsKey(decFunc.getName())) {
                functionTable.get(decFunc.getName()).add(decFunc);
            } else {
                List<CompiledFunction> funcs = new ArrayList<>();
                funcs.add(decFunc);
                functionTable.put(decFunc.getName(), funcs);
            }
        }
    }

    public CompiledObject getObject(String name) throws NoSuchElementException {
        CompiledObject varFor = variableTable.get(name);
        if(varFor != null) {
            return varFor;
        }

        if(parent == null) {
            throw new NoSuchElementException(name);
        }

        return parent.getObject(name);
    }

    public CompiledFunction getFunction(String name, List<CompiledType> args) throws CompileException {
        List<CompiledFunction> posFuncs = functionTable.get(name);
        if(posFuncs != null) {
            Optional<CompiledFunction> targetFunc = posFuncs.stream().filter(func -> {
                if(args.size() != func.getArgs().size()) {
                    return false;
                }

                for (int i = 0; i < args.size(); i++) {
                    if(args.get(i).equals(func.getArgs().get(i).getType())) {
                        continue;
                    }
                    // TODO: make this check the type of the invoked function
                    if(func.getArgs().get(i).getType().isGenericPlaceholder()) {
                        continue;
                    }
                    return false;

                }

                return true;
            }).findFirst();

            if(targetFunc.isPresent()) {
                return targetFunc.get();
            }
        }

        if(parent == null) {
            String message = "Could not find function " + name +
                    " with args " + args.stream()
                    .map(CompiledObject::getName)
                    .collect(Collectors.joining(", "));
            throw new NoSuchElementException(message);
        }

        return parent.getFunction(name, args);
    }

    public List<CompiledObject> getLocals() {
        return new ArrayList<>(variableTable.values());
    }

    public List<CompiledObject> getAllVars() {
        List<CompiledObject> all = new ArrayList<>(variableTable.values());
        if(this.parent != null) {
            all.addAll(this.parent.getAllVars());
        }
        return all;
    }

    public List<CompiledFunction> getAllFuncs() {
        List<CompiledFunction> all = functionTable
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        if(this.parent != null) {
            all.addAll(this.parent.getAllFuncs());
        }
        return all;
    }
}
