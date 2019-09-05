package io.dallen.compiler;

import java.util.List;

public class BuiltinTypes {

    public static final CompiledType VOID = new CompiledType("Void", false)
            .setCompiledName("void");
    public static final CompiledType ANY = new CompiledType("Any", false);

    public static final CompiledType INT = new CompiledType("Int", false)
            .setParent(BuiltinTypes.ANY)
            .setCompiledName("int32_t");
    public static final CompiledType BOOL = new CompiledType("Bool" ,false)
            .setCompiledName("uint8_t")
            .setParent(BuiltinTypes.ANY);

    public static final CompiledType ANYREF = new CompiledType("AnyRef", true)
            .setParent(BuiltinTypes.ANY);
    public static final CompiledType CLASS = new CompiledType("Class", true)
            .setParent(BuiltinTypes.ANYREF);
    public static final CompiledType FUNCTION = new CompiledType("Function", true)
            .setParent(BuiltinTypes.ANYREF);
    public static final CompiledType STRING = new CompiledType("String", true)
            .setParent(BuiltinTypes.ANYREF);

    public static final CompiledType EXCEPTION = new CompiledType("Exception", true)
            .setParent(BuiltinTypes.ANYREF)
            .addConstructor(
                    new CompiledFunction(
                            "Exception",
                            "skiff_exception_new",
                            true,
                            BuiltinTypes.VOID,
                            List.of(BuiltinTypes.STRING)
                    )
            )
            .addMethod(new CompiledMethod(
                    new CompiledFunction("getMessage", "", false, BuiltinTypes.STRING,
                            List.of()), true, false));

    public static final CompiledType LIST = new CompiledType("List" ,true)
            .addGeneric("T")
            .setParent(BuiltinTypes.ANYREF)
//            .addField(new CompiledVar("size", false, CompiledType.INT))
            .addMethod(new CompiledMethod(
                    new CompiledFunction("getSize", "", false, BuiltinTypes.INT,
                            List.of()), true, false))
            .addMethod(new CompiledMethod(
                    new CompiledFunction("getSub", "", false, BuiltinTypes.ANYREF,
                            List.of()), true, false));


}
