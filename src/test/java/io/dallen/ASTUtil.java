package io.dallen;

import java.util.List;

public class ASTUtil {

    public static AST.Type simpleType(String name) {
        return new AST.Type(new AST.Variable(name, List.of()), List.of());
    }

    public static AST.FunctionCall simpleFuncCall(String name) {
        return new AST.FunctionCall(name, List.of(), List.of(), List.of());
    }
}
