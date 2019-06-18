package io.dallen.compiler;

import java.util.ArrayList;

public class CompiledType extends CompiledObject {
    public static final CompiledType VOID = new CompiledType("Void", 0)
        .setCompiledName("void");
    public static final CompiledType ANY = new CompiledType("Any", 0);

    public static final CompiledType INT = new CompiledType("Int", 4)
        .setParent(CompiledType.ANY)
        .setCompiledName("int32_t *");
    public static final CompiledType BOOL = new CompiledType("Bool" ,1)
        .setParent(CompiledType.ANY);

    public static final CompiledType ANYREF = new CompiledType("AnyRef", -1)
        .setParent(CompiledType.ANY);
    public static final CompiledType CLASS = new CompiledType("Class", -1)
        .setParent(CompiledType.ANYREF);
    public static final CompiledType FUNCTION = new CompiledType("Function", -1)
        .setParent(CompiledType.ANYREF);
    public static final CompiledType STRING = new CompiledType("String", -1)
        .setParent(CompiledType.ANYREF);

    public static final CompiledType LIST = new CompiledType("List" ,-1)
        .setParent(CompiledType.ANYREF)
        .addClassObject(new CompiledVar("size", CompiledType.INT))
        .addClassObject(new CompiledFunction("getSize", "skiff_get_size", CompiledType.INT, new ArrayList<>()))
        .addClassObject(new CompiledFunction("getSub", "skiff_get_sub", CompiledType.ANYREF, new ArrayList<>()));


    private final int size;
    private final boolean isRef;
    private final CompileScope clazz;
    private CompiledType parent = null;
    private String compiledName;

    public CompiledType(String className, int size) {
        super(className);
        this.compiledName = ASTVisitor.underscoreJoin("skiff", className, "t");
        this.size = size;
        this.isRef = (size == -1);
        clazz = new CompileScope(null);
    }

    public CompiledType addClassObject(CompiledObject obj) {
        clazz.declareObject(obj);
        return this;
    }

    public CompiledObject getObject(String name) {
        return clazz.getObject(name);
    }

    public int getSize() {
        return size;
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
        return this;
    }
}
