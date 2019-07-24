package io.dallen.tokenizer;

import java.util.HashMap;
import java.util.Map;

public class LexerTable {
    private final Map<String, Token.IdentifierType> symbolMap = new HashMap<>();
    private final LexerTable parent;

    public LexerTable(LexerTable parent) {
        this.parent = parent;
    }

    public Token.IdentifierType getIdent(String symbol) {
        Token.IdentifierType typ = symbolMap.get(symbol);
        if(typ != null) {
            return typ;
        }

        if(parent != null) {
            return parent.getIdent(symbol);
        }

        return null;
    }

    public void defineIdent(String symbol, Token.IdentifierType typ) {
        symbolMap.put(symbol, typ);
    }
}
