package io.dallen.compiler;

public class BuiltinTypes {
    public static final CompiledType VOID = new CompiledType("Void", false, true)
            .setCompiledName("void");
    public static final CompiledType ANY = new CompiledType("Any", false, false);

    public static final CompiledType INT = new CompiledType("Int", false, false)
            .setParent(BuiltinTypes.ANY)
            .setCompiledName("int32_t");

    public static final CompiledType BOOL = new CompiledType("Bool" ,false, false)
            .setCompiledName("uint8_t")
            .setParent(BuiltinTypes.ANY);

    public static final CompiledType ANYREF = new CompiledType("AnyRef", true, false)
            .setParent(BuiltinTypes.ANY);
    public static final CompiledType CLASS = new CompiledType("Class", true, false)
            .setParent(BuiltinTypes.ANYREF);
    public static final CompiledType FUNCTION = new CompiledType("Function", true, false)
            .setParent(BuiltinTypes.ANYREF);

    public static final CompiledType STRING = new CompiledType("String", true, false)
            .setParent(BuiltinTypes.ANYREF);

}
