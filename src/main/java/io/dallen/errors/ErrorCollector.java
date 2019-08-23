package io.dallen.errors;

import io.dallen.tokenizer.Token;

import java.util.List;

public interface ErrorCollector {

    void throwError(String msg, Token on);

    List<String> getErrors();

}
