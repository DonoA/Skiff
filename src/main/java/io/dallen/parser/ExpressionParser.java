package io.dallen.parser;

import io.dallen.AST;
import io.dallen.AST.*;
import io.dallen.parser.splitter.SplitAction;
import io.dallen.tokenizer.Token;


import java.util.List;

public class ExpressionParser {
    public static Statement parseAssignment(List<Token> first, List<Token> second) {
        Statement firstS = new Parser(first).parseExpression();
        Statement secondS = new Parser(second).parseExpression();
        return new Assign(firstS, secondS);
    }

    public static SplitAction boolCombineAction(BoolOp op) {
        return statementAction((first, second) -> new BoolCombine(first, op, second));
    }

    public static SplitAction compareAction(CompareOp op) {
        return statementAction((first, second) -> new Compare(first, op, second));
    }

    public static SplitAction mathAction(MathOp op) {
        return statementAction((first, second) -> new AST.Math(first, op, second));
    }

    public static SplitAction statementAction(StatementAction action) {
        return (first, second) -> {
            Statement firstS = new Parser(first).parseExpression();
            Statement secondS = new Parser(second).parseExpression();
            return action.handle(firstS, secondS);
        };
    }

    public interface StatementAction {
        Statement handle(Statement first, Statement second);
    }
}
