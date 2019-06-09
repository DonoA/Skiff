package io.dallen.compiler;

import java.util.ArrayList;

public class CompiledType extends CompiledObject {
    public static final CompiledType CLASS = new CompiledType("Class", 8);
    public static final CompiledType VOID = new CompiledType("Void", 0);
    public static final CompiledType FUNCTION = new CompiledType("Function", -1);

    public static final CompiledType STRING = new CompiledType("String", -1);
    public static final CompiledType INT = new CompiledType("Int", 4);
    public static final CompiledType BOOL = new CompiledType("Bool" ,1);
    public static final CompiledType LIST = new CompiledType("List" ,-1)
            .addClassObject(new CompiledVar("size", CompiledType.INT))
            .addClassObject(new CompiledFunction("getSize", CompiledType.INT, new ArrayList<>()));

    private final int size;
    private final boolean isRef;
    private final CompileScope clazz;

    public CompiledType(String className, int size) {
        super(className);
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
}
