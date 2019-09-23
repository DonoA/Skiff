package io.dallen.tokenizer;

import io.dallen.errors.ErrorCollector;

import java.util.List;
import java.util.stream.Collectors;

public class EnrichLexer {

    private final List<Token> tokens;

    private LexerTable table = new LexerTable(
            "String", "Int", "List"
    );

    private final ErrorCollector<Token> errors;

    public EnrichLexer(List<Token> tokens, ErrorCollector<Token> errors) {
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

            if(token.type != Token.Keyword.CLASS) {
                continue;
            }

            Token next = tokens.get(i + 1);
            table.defineIdent(next.literal, Token.IdentifierType.TYPE);

            if(tokens.get(i + 2).type == Token.Symbol.LEFT_ANGLE) {
                int index = i + 2;
                while(true) {
                    table.defineIdent(tokens.get(index + 1).literal, Token.IdentifierType.TYPE);
                    int nextIndex = indexOfNext(index, Token.Symbol.COMMA, Token.Symbol.RIGHT_ANGLE);
                    if(nextIndex == -1) {
                        i = index;
                        break;
                    }
                    index = nextIndex;
                }
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

    private int indexOfNext(int start, Token.TokenType search, Token.TokenType blockEnd) {
        for (int i = start; i < tokens.size(); i++) {
            if(tokens.get(i).type == search) {
                return i;
            }
            if(tokens.get(i).type == blockEnd) {
                return -1;
            }
        }
        return -1;
    }
}
