package io.dallen.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class EnrichLexer {

    private final List<Token> tokens;

    private LexerTable table = new LexerTable(
            "String", "Int", "List"
    );

    public EnrichLexer(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Token> enrich() {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if(token.type == Token.Symbol.LEFT_BRACE) {
                table.newChild();
            }

            if(token.type == Token.Symbol.RIGHT_BRACE) {
                table.toParent();
            }

            if(token.type == Token.Keyword.CLASS) {
                Token next = tokens.get(i + 1);
                table.defineIdent(next.literal, Token.IdentifierType.TYPE);
            }
        }

        return tokens
                .stream()
                .map(token -> {
                    if(token.type == Token.Symbol.LEFT_BRACE) {
                        table.nextChild();
                    }

                    if(token.type == Token.Symbol.RIGHT_BRACE) {
                        table.toParent();
                    }

                    if(token.type == Token.Textless.NAME) {
                        Token.IdentifierType ident = table.getIdent(token.literal);
                        if(ident == null) {
                            ident = Token.IdentifierType.VARIABLE;
                        }
                        return new Token(token.type, token.literal, ident);
                    } else {
                        return token;
                    }
                })
                .collect(Collectors.toList());
    }
}
