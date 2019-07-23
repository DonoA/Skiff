package io.dallen.parser;

import io.dallen.AST.*;
import io.dallen.parser.splitter.BraceSplitter;
import io.dallen.parser.splitter.SplitAction;
import io.dallen.tokenizer.Token;

import java.text.ParseException;
import java.util.List;

public class ExpressionParser {
    public static Statement parseAssignment(List<Token> first, List<Token> second) {
        List<List<Token>> res = BraceSplitter.splitAll(first, Token.Symbol.COLON);
        if(res.size() == 1) {
            Statement firstS = new Parser(first).parseExpression();
            Statement secondS = new Parser(second).parseExpression();
            return new Assign(firstS, secondS);
        } else if(res.size() == 2) {
            Type typ = new Parser(res.get(1)).parseType();
            if(res.get(0).size() != 1) {
                throw new ParserError("Declare assign name had multiple parts", res.get(0).get(0));
            }
            String name = res.get(0).get(0).literal;
            Statement secondS = new Parser(second).parseExpression();
            return new DeclareAssign(typ, name, secondS);
        }
        throw new ParserError("Assign name had many colons", res.get(0).get(0));
    }

    public static SplitAction boolCombineAction(BoolOp op) {
        return statementAction((first, second) -> new BoolCombine(first, op, second));
    }

    public static SplitAction compareAction(CompareOp op) {
        return statementAction((first, second) -> new Compare(first, op, second));
    }

    public static SplitAction mathAction(MathOp op) {
        return statementAction((first, second) -> new MathStatement(first, op, second));
    }

    public static SplitAction mathAssignAction(MathOp op) {
        return statementAction((first, second) -> new MathAssign(first, op, second));
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
