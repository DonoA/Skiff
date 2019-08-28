package io.dallen.parser.splitter;

import io.dallen.AST;
import io.dallen.parser.Parser;
import io.dallen.tokenizer.Token;

import java.util.List;

public interface SplitAction {
    AST.Statement handle(Parser parser, List<Token> first, List<Token> second, List<Token> allTokens);
}
