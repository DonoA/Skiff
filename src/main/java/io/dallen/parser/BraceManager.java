package io.dallen.parser;

import io.dallen.tokenizer.Token;

import java.util.ArrayDeque;

import static io.dallen.tokenizer.Token.Symbol.*;

@SuppressWarnings("Duplicates")
public class BraceManager {

    public static final BraceProfile leftToRight = new BraceProfile() {
        @Override
        public Token.TokenType openBraceFor(Token.Symbol tt) {
            switch (tt) {
                case RIGHT_BRACE:
                    return LEFT_BRACE;
                case RIGHT_PAREN:
                    return LEFT_PAREN;
                case RIGHT_BRACKET:
                    return LEFT_BRACKET;
                default:
                    return null;
            }
        }

        @Override
        public boolean isOpenBrace(Token.Symbol tt) {
            switch (tt) {
                case LEFT_BRACE:
                case LEFT_PAREN:
                case LEFT_BRACKET:
                    return true;
                default:
                    return false;
            }
        }
    };

    public static final BraceProfile leftToRightAngle = new BraceProfile() {
        @Override
        public Token.TokenType openBraceFor(Token.Symbol tt) {
            switch (tt) {
                case RIGHT_ANGLE:
                    return LEFT_ANGLE;
                case RIGHT_BRACE:
                    return LEFT_BRACE;
                case RIGHT_PAREN:
                    return LEFT_PAREN;
            }

            return null;
        }

        @Override
        public boolean isOpenBrace(Token.Symbol tt) {
            switch (tt) {
                case LEFT_ANGLE:
                case LEFT_BRACE:
                case LEFT_PAREN:
                    return true;
                default:
                    return false;
            }
        }
    };

    public static final BraceProfile rightToLeft = new BraceProfile() {
        @Override
        public Token.TokenType openBraceFor(Token.Symbol tt) {
            switch (tt) {
                case LEFT_BRACE:
                    return RIGHT_BRACE;
                case LEFT_PAREN:
                    return RIGHT_PAREN;
                case LEFT_BRACKET:
                    return RIGHT_BRACKET;
                default:
                    return null;
            }
        }

        @Override
        public boolean isOpenBrace(Token.Symbol tt) {
            switch (tt) {
                case RIGHT_BRACE:
                case RIGHT_PAREN:
                case RIGHT_BRACKET:
                    return true;
                default:
                    return false;
            }
        }
    };

    public interface BraceProfile {
        Token.TokenType openBraceFor(Token.Symbol tt);
        boolean isOpenBrace(Token.Symbol tt);
    }


    private final ArrayDeque<Token.TokenType> braceStack = new ArrayDeque<>();
    private final BraceProfile braceProfile;

    public BraceManager(BraceProfile braceProfile) {
        this.braceProfile = braceProfile;
    }

    public void check(Token t) {
        if(!(t.type instanceof Token.Symbol)) {
            return;
        }

        Token.Symbol tokenType = (Token.Symbol) t.type;

        if (braceProfile.isOpenBrace(tokenType)) { // track open braces
            braceStack.push(t.type);
            return;
        }
        if (braceProfile.openBraceFor(tokenType) != null) { // pop braces as close braces are found
            if (braceStack.peek() == braceProfile.openBraceFor(tokenType)) {
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
