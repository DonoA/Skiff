package io.dallen.tokenizer;

import io.dallen.errors.ErrorCollector;

import java.util.List;
import java.util.stream.Collectors;

public class EnrichLexer {

    private final List<Token> tokens;

    private LexerTable table = new LexerTable(
            "String", "Int", "List"
    );

    private final ErrorCollector errors;

    public EnrichLexer(List<Token> tokens, ErrorCollector errors) {
        this.tokens = tokens;
        this.errors = errors;
    }

    public List<Token> enrich() {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if(token.type == Token.Symbol.LEFT_BRACE) {
                table.newChild();
            }

            if(token.type == Token.Symbol.RIGHT_BRACE) {
                if(table.getCurrent() == null) {
                    errors.throwError("Too many close braces", token);
                    return List.of();
                }
                table.toParent();
            }

            if(token.type == Token.Keyword.CLASS) {
                Token next = tokens.get(i + 1);
                table.defineIdent(next.literal, Token.IdentifierType.TYPE);
            }
        }

        if(table.getCurrent() == null) {
            errors.throwError("Too many close braces", tokens.get(tokens.size() - 1));
            return List.of();
        }

        if(table.getCurrent().getParent() != null) {
            errors.throwError("Expected more close braces", tokens.get(tokens.size() - 1));
            return List.of();
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
                        return new Token(token.type, token.literal, ident, token.pos);
                    } else {
                        return token;
                    }
                })
                .collect(Collectors.toList());
    }
}
