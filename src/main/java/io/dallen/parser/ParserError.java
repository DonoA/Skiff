package io.dallen.parser;

import io.dallen.tokenizer.Token;

public class ParserError extends Error {
    public ParserError(String msg, Token t) {
        super(msg + " Token: " + t.toString());
    }
}
