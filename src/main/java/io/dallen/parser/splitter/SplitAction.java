package io.dallen.parser.splitter;

import io.dallen.ast.AST;
import io.dallen.parser.Parser;

public interface SplitAction {
    AST.Statement handle(Parser parser, Parser first, Parser second);
}
