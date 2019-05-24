package io.dallen.parser.splitter;

import io.dallen.parser.AST;
import io.dallen.tokenizer.Token;

import java.util.List;

public interface SplitAction {
    AST.Statement handle(List<Token> first, List<Token> second);
}
