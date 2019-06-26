package io.dallen.tokenizer;

import io.dallen.tokenizer.Lexer;
import io.dallen.tokenizer.Token;

import java.util.List;
import java.util.stream.Collectors;

public class LexUtil {

    public static String createTokensFor(String stmt) {
        return createStaticListOf(new Lexer(stmt).lex());
    }

    public static String createStaticListOf(List<Token> tokens) {
        String list = "List.of(\n";

        list += tokens.stream().map(token -> {
            String tokenTypeName = "Token." + token.type.getClass().getSimpleName() + "." + token.type.getName();
            String str = "new Token(" + tokenTypeName;
            if(!token.literal.isEmpty()) {
                str += ", \"" + token.literal + "\"";
            }
            str += ")";
            return str;
        }).collect(Collectors.joining(",\n"));

        list += ")";
        return list;
    }
}
