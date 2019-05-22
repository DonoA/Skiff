package io.dallen.parser;

import io.dallen.tokenizer.Token;

import java.util.ArrayDeque;

import static io.dallen.tokenizer.Token.Symbol.*;

public class BraceManager {

    private final ArrayDeque<Token.TokenType> braceStack = new ArrayDeque<>();

    private final boolean reverse;

    private Token.TokenType openBraceFor(Token.Symbol tt) {
        if(!reverse) {
            switch (tt) {
                case RIGHT_ANGLE:
                    return LEFT_ANGLE;
                case RIGHT_BRACE:
                    return LEFT_BRACE;
                case RIGHT_PAREN:
                    return LEFT_PAREN;
            }
        } else {
            switch (tt) {
                case LEFT_ANGLE:
                    return RIGHT_ANGLE;
                case LEFT_BRACE:
                    return RIGHT_BRACE;
                case LEFT_PAREN:
                    return RIGHT_PAREN;
            }
        }

        return null;
    }

    private boolean isOpenBrace(Token.Symbol tt) {
        if(!reverse) {
            switch (tt) {
                case LEFT_ANGLE:
                case LEFT_BRACE:
                case LEFT_PAREN:
                    return true;
                default:
                    return false;
            }
        } else {
            switch (tt) {
                case RIGHT_ANGLE:
                case RIGHT_BRACE:
                case RIGHT_PAREN:
                    return true;
                default:
                    return false;
            }
        }
    }

    public BraceManager(boolean reverse) {
        this.reverse = reverse;
    }

    public void check(Token t) {
        if(!(t.type instanceof Token.Symbol)) {
            return;
        }

        Token.Symbol tokenType = (Token.Symbol) t.type;

        if (isOpenBrace(tokenType)) { // track open braces
            braceStack.push(t.type);
            return;
        }
        if (openBraceFor(tokenType) != null) { // pop braces as close braces are found
            if (braceStack.peek() == openBraceFor(tokenType)) {
                braceStack.pop();
            } else {
                throw new ParserError("Unknown token sequence", t);
            }
        }
    }

    public boolean isEmpty() {
        return braceStack.isEmpty();
    }

}
