package io.dallen.compiler;

import io.dallen.compiler.visitor.VisitorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompiledType extends CompiledObject {
    public static final CompiledType VOID = new CompiledType("Void", false)
        .setCompiledName("void");
    public static final CompiledType ANY = new CompiledType("Any", false);

    public static final CompiledType INT = new CompiledType("Int", false)
        .setParent(CompiledType.ANY)
        .setCompiledName("int32_t");
    public static final CompiledType BOOL = new CompiledType("Bool" ,false)
        .setParent(CompiledType.ANY);

    public static final CompiledType ANYREF = new CompiledType("AnyRef", true)
        .setParent(CompiledType.ANY);
    public static final CompiledType CLASS = new CompiledType("Class", true)
        .setParent(CompiledType.ANYREF);
    public static final CompiledType FUNCTION = new CompiledType("Function", true)
        .setParent(CompiledType.ANYREF);
    public static final CompiledType STRING = new CompiledType("String", true)
        .setParent(CompiledType.ANYREF);

    public static final CompiledType LIST = new CompiledType("List" ,true)
        .setParent(CompiledType.ANYREF)
        .addField(new CompiledVar("size", false, CompiledType.INT))
        .addMethod(new CompiledMethod(
                new CompiledFunction("getSize", "skiff_get_size", false, CompiledType.INT, new ArrayList<>()),
                true))
        .addMethod(new CompiledMethod(
                new CompiledFunction("getSub", "skiff_get_sub", false, CompiledType.ANYREF, new ArrayList<>()),
                true));



    private final boolean isRef;
    // order is vital for both declared vars and declared functions
    private List<CompiledVar> declaredVars = new ArrayList<>();
    private List<CompiledMethod> declaredMethods = new ArrayList<>();

    private Map<String, CompiledVar> declaredVarMap = new HashMap<>();
    private Map<String, CompiledMethod> declaredMethodMap = new HashMap<>();

    private CompiledType parent = null;
    private String structName;
    private String compiledName;
    private List<CompiledFunction> constructors = new ArrayList<>();

    public CompiledType(String className, boolean ref) {
        super(className);
        this.structName = VisitorUtils.underscoreJoin("skiff", className, "t");
        this.compiledName = this.structName + (ref ? " *" : "");
        this.isRef = ref;
    }

    public String getStructName() {
        return this.structName;
    }

    public CompiledObject getObject(String name) {
        CompiledMethod method = getMethod(name);
        if(method != null) {
            return method;
        }
        return getField(name);
    }

    public CompiledType addField(CompiledVar obj) {
        declaredVars.add(obj);
        declaredVarMap.put(obj.getName(), obj);
        return this;
    }

    public CompiledVar getField(String name) {
        return declaredVarMap.get(name);
    }

    public CompiledType addMethod(CompiledMethod obj) {
        declaredMethods.add(obj);
        declaredMethodMap.put(obj.getName(), obj);
        return this;
    }

    public CompiledMethod getMethod(String name) {
        return declaredMethodMap.get(name);
    }

    public CompiledType addConstructor(CompiledFunction func) {
        constructors.add(func);
        return this;
    }

    public List<CompiledVar> getAllFields() {
        return this.declaredVars;
    }

    public List<CompiledMethod> getAllMethods() {
        return this.declaredMethods;
    }

    public boolean isRef() {
        return isRef;
    }

    public CompiledType getParent() {
        return parent;
    }

    public CompiledType setParent(CompiledType parent) {
        this.parent = parent;
        return this;
    }

    public String getCompiledName() {
        return compiledName;
    }

    public CompiledType setCompiledName(String compiledName) {
        this.compiledName = compiledName;
        this.structName = compiledName;
        return this;
    }

    public static class CompiledMethod extends CompiledFunction {
        private final boolean mine;

        public CompiledMethod(CompiledFunction func, boolean mine) {
            super(func.getName(), func.getCompiledName(), func.isConstructor(), func.getReturns(), func.getArgs());
            this.mine = mine;
        }

        public boolean isMine() {
            return mine;
        }
    }
}
