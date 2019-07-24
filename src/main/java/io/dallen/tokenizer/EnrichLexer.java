package io.dallen.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class EnrichLexer {

    private final List<Token> tokens;

    private final Stack<LexerTable> symbolTables = new Stack<>();

    private final static LexerTable baseTable = new LexerTable(null);

    static {
        baseTable.defineIdent("String", Token.IdentifierType.TYPE);
        baseTable.defineIdent("Int", Token.IdentifierType.TYPE);
        baseTable.defineIdent("List", Token.IdentifierType.TYPE);
    }

    public EnrichLexer(List<Token> tokens) {
        this.tokens = tokens;
        symbolTables.push(baseTable);
    }

    public List<Token> enrich() {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if(token.type == Token.Symbol.LEFT_BRACE) {
                symbolTables.push(new LexerTable(symbolTables.peek()));
            }

            if(token.type == Token.Symbol.RIGHT_BRACE) {
                symbolTables.pop();
            }

            if(token.type == Token.Keyword.CLASS) {
                Token next = tokens.get(i + 1);
                symbolTables.peek().defineIdent(next.literal, Token.IdentifierType.TYPE);
            }
        }

        return tokens
                .stream()
                .map(token -> {
                    if(token.type == Token.Textless.NAME) {
                        Token.IdentifierType ident = symbolTables.peek().getIdent(token.literal);
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
