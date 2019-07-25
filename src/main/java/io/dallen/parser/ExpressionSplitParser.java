package io.dallen.parser;

import io.dallen.AST.*;
import io.dallen.parser.splitter.*;
import io.dallen.tokenizer.Token;

import java.text.ParseException;
import java.util.List;

class ExpressionSplitParser {
    // defines a multipass split. When a successful split is made, the resulting action will be executed on the result
    private static final SplitSettings splitSettings = new SplitSettings()
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.EQUAL, ExpressionSplitParser::parseAssignment))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.PLUS_EQUAL, ExpressionSplitParser.mathAssignAction(MathOp.PLUS))
                    .addSplitRule(Token.Symbol.MINUS_EQUAL, ExpressionSplitParser.mathAssignAction(MathOp.MINUS)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.DOUBLE_AND, ExpressionSplitParser.boolCombineAction(BoolOp.AND)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.DOUBLE_OR, ExpressionSplitParser.boolCombineAction(BoolOp.OR)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.DOUBLE_EQUAL, ExpressionSplitParser.compareAction(CompareOp.EQ))
                    .addSplitRule(Token.Symbol.LEFT_ANGLE, ExpressionSplitParser.compareAction(CompareOp.LT))
                    .addSplitRule(Token.Symbol.RIGHT_ANGLE, ExpressionSplitParser.compareAction(CompareOp.GT)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.SLASH, ExpressionSplitParser.mathAction(MathOp.DIV))
                    .addSplitRule(Token.Symbol.STAR, ExpressionSplitParser.mathAction(MathOp.MUL)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.PLUS, ExpressionSplitParser.mathAction(MathOp.PLUS))
                    .addSplitRule(Token.Symbol.MINUS, ExpressionSplitParser.mathAction(MathOp.MINUS)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.DOT, ExpressionSplitParser.statementAction(Dotted::new))).leftToRight(false);

    static Statement split(List<Token> workingTokens) {
        return new LayeredSplitter(splitSettings).execute(workingTokens);
    }

    private static Statement parseAssignment(List<Token> first, List<Token> second) {
        List<List<Token>> res = BraceSplitter.splitAll(first, Token.Symbol.COLON);
        if(res.size() == 1) {
            Statement firstS = new Parser(first).parseExpression();
            Statement secondS = new Parser(second).parseExpression();
            return new Assign(firstS, secondS);
        } else if(res.size() == 2) {
            Type typ = new Parser(res.get(1)).getCommon().parseType();
            if(res.get(0).size() != 1) {
                throw new ParserError("Declare assign name had multiple parts", res.get(0).get(0));
            }
            String name = res.get(0).get(0).literal;
            Statement secondS = new Parser(second).parseExpression();
            return new DeclareAssign(typ, name, secondS);
        }
        throw new ParserError("Assign name had many colons", res.get(0).get(0));
    }

    private static SplitAction boolCombineAction(BoolOp op) {
        return statementAction((first, second) -> new BoolCombine(first, op, second));
    }

    private static SplitAction compareAction(CompareOp op) {
        return (first, second) -> {
            if(first.get(first.size() - 1).ident == Token.IdentifierType.TYPE) {
                return null;
            }

            Parser rhsParser = new Parser(second);

            if(rhsParser.containsBefore(Token.Symbol.RIGHT_ANGLE, Token.Symbol.SEMICOLON)) {
                List<Token> rhs = rhsParser.selectTo(Token.Symbol.RIGHT_ANGLE);
                if(rhs.get(rhs.size() - 1).ident == Token.IdentifierType.TYPE) {
                    return null;
                }
            }

            Statement firstS = new Parser(first).parseExpression();
            Statement secondS = new Parser(second).parseExpression();
            return new Compare(firstS, op, secondS);
        };
    }

    private static SplitAction mathAction(MathOp op) {
        return statementAction((first, second) -> new MathStatement(first, op, second));
    }

    private static SplitAction mathAssignAction(MathOp op) {
        return statementAction((first, second) -> new MathAssign(first, op, second));
    }

    private static SplitAction statementAction(StatementAction action) {
        return (first, second) -> {
            Statement firstS = new Parser(first).parseExpression();
            Statement secondS = new Parser(second).parseExpression();
            return action.handle(firstS, secondS);
        };
    }

    private interface StatementAction {
        Statement handle(Statement first, Statement second);
    }


}
