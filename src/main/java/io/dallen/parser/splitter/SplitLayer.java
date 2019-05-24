package io.dallen.parser.splitter;

import io.dallen.tokenizer.Token;

import java.util.HashMap;

public class SplitLayer {

    private HashMap<Token.TokenType, SplitAction> entries = new HashMap<>();
    private boolean leftToRight = true;

    public SplitLayer addSplitRule(Token.TokenType type, SplitAction action) {
        entries.put(type, action);
        return this;
    }

    SplitAction actionFor(Token.TokenType type) {
        return entries.get(type);
    }

    SplitLayer leftToRight(boolean leftToRight) {
        this.leftToRight = leftToRight;
        return this;
    }

    boolean isLeftToRight() {
        return leftToRight;
    }
}
