package io.dallen;

import io.dallen.tokenizer.Token;

import java.util.List;

public class ASTUtil {

    public static AST.NumberLiteral simpleNumLit(Double value) {
        return new AST.NumberLiteral(value, List.of(new Token(Token.Textless.NUMBER_LITERAL,
                String.valueOf(value.intValue()), 0)));
    }

    public static AST.Variable simpleVar(String name) {
        return new AST.Variable(name, List.of(new Token(Token.Textless.NAME, name, 0)));
    }

    public static AST.Type simpleType(String name) {
        return new AST.Type(new AST.Variable(name, List.of(new Token(Token.Textless.NAME, name, 0))), List.of());
    }

    public static AST.FunctionCall simpleFuncCall(String name) {
        return new AST.FunctionCall(name, List.of(), List.of(), List.of(
                new Token(Token.Textless.NAME, "think", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0)
        ));
    }
}
