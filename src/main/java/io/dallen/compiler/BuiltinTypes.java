package io.dallen.compiler;

import java.util.List;

/**
 * Types too integral to the operation of Skiff to be defined naturally. Also used for quick reference to common types.
 */
public class BuiltinTypes {
    public static final CompiledType VOID = new CompiledType("Void", false, true)
            .setCompiledName("void");
    public static final CompiledType ANY = new CompiledType("Any",  false, false);

    public static final CompiledType INT = new CompiledType("Int", false, false)
            .setParent(BuiltinTypes.ANY)
            .setCompiledName("int32_t");

    public static final CompiledType FLOAT = new CompiledType("Float", false, false)
            .setParent(BuiltinTypes.ANY)
            .setCompiledName("float");

    public static final CompiledType BOOL = new CompiledType("Bool" ,false, false)
            .setCompiledName("uint8_t")
            .setParent(BuiltinTypes.ANY);

    public static final CompiledType BYTE = new CompiledType("Byte" ,false, false)
            .setCompiledName("int8_t")
            .setParent(BuiltinTypes.ANY);

    public static final CompiledType ANYREF = new CompiledType("AnyRef", true, false)
            .setParent(BuiltinTypes.ANY);
    public static final CompiledType CLASS = new CompiledType("Class", true, false)
            .setParent(BuiltinTypes.ANYREF);
    public static final CompiledType FUNCTION = new CompiledType("Function", true, false)
            .setParent(BuiltinTypes.ANYREF);

    public static final CompiledType STRING = new CompiledType("String", true, false)
            .setParent(BuiltinTypes.ANYREF);

    private static boolean setup = false;

    public static void finishSetup() {
        if(setup) {
            return;
        }

        BuiltinTypes.INT.addMethod(new CompiledMethod("toString", "skiff_int_to_string",
                List.of(), BuiltinTypes.STRING, true, false));

        BuiltinTypes.FLOAT.addMethod(new CompiledMethod("toString", "skiff_float_to_string",
                List.of(), BuiltinTypes.STRING, true, false));

        setup = true;
    }

}
