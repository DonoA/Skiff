package io.dallen.compiler;

import io.dallen.ast.AST;

public class CompileException extends RuntimeException {
    private final AST.Statement statement;

    public CompileException(String msg, AST.Statement statement) {
        super(msg);
        this.statement = statement;
    }

    public AST.Statement getStatement() {
        return statement;
    }
}
