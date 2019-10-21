package io.dallen.compiler;

public class BuiltinTypes {
    public static final CompiledType VOID = new CompiledType("Void", null, false, true)
            .setCompiledName("void");
    public static final CompiledType ANY = new CompiledType("Any", null, false, false);

    public static final CompiledType INT = new CompiledType("Int", null, false, false)
            .setParent(BuiltinTypes.ANY)
            .setCompiledName("int32_t");

    public static final CompiledType BOOL = new CompiledType("Bool" ,null, false, false)
            .setCompiledName("uint8_t")
            .setParent(BuiltinTypes.ANY);

    public static final CompiledType ANYREF = new CompiledType("AnyRef", null, true, false)
            .setParent(BuiltinTypes.ANY);
    public static final CompiledType CLASS = new CompiledType("Class", null, true, false)
            .setParent(BuiltinTypes.ANYREF);
    public static final CompiledType FUNCTION = new CompiledType("Function", null, true, false)
            .setParent(BuiltinTypes.ANYREF);

    public static final CompiledType STRING = new CompiledType("String", null, true, false)
            .setParent(BuiltinTypes.ANYREF);

}
