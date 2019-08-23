package io.dallen.parser;

import io.dallen.tokenizer.Token;

public class ParserError extends Exception {
    public final String msg;
    public final Token on;

    public ParserError(String msg, Token t) {
        this.msg = msg;
        this.on = t;
    }

    public static class NoCatchParseError extends RuntimeException {
        public final String msg;
        public final Token on;

        public NoCatchParseError(String msg, Token t) {
            this.msg = msg;
            this.on = t;
        }
    }
}
