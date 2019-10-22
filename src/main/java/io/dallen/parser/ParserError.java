package io.dallen.parser;

import io.dallen.tokenizer.Token;

/**
 * Errors from the parser on a token
 */
public class ParserError extends Exception {
    public final String msg;
    public final Token on;

    public ParserError(String msg, Token t) {
        this.msg = msg;
        this.on = t;
    }

    /**
     * An unchecked version of the parser error
     */
    public static class NoCatchParseError extends RuntimeException {
        public final String msg;
        public final Token on;

        public NoCatchParseError(String msg, Token t) {
            this.msg = msg;
            this.on = t;
        }
    }
}
