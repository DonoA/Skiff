package io.dallen.parser;

import io.dallen.parser.splitter.SplitAction;
import io.dallen.tokenizer.Token;

import java.util.List;

public class ExpressionParser {
    public static AST.Statement parseAssignment(List<Token> first, List<Token> second) {
        return null;
    }

    public static SplitAction boolCombineAction(AST.BoolOp op) {
        return statementAction((first, second) -> new AST.BoolCombine(first, op, second));
    }

    public static SplitAction compareAction(AST.CompareOp op) {
        return statementAction((first, second) -> new AST.Compare(first, op, second));
    }

    public static SplitAction mathAction(AST.MathOp op) {
        return statementAction((first, second) -> new AST.Math(first, op, second));
    }

    public static SplitAction statementAction(StatementAction action) {
        return (first, second) -> {
            AST.Statement firstS = new Parser(first).parseExpression();
            AST.Statement secondS = new Parser(first).parseExpression();
            return action.handle(firstS, secondS);
        };
    }

    public interface StatementAction {
        AST.Statement handle(AST.Statement first, AST.Statement second);
    }
}
