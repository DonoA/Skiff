package io.dallen.parser;

import io.dallen.ast.AST.*;
import io.dallen.ast.ASTEnums;
import io.dallen.parser.splitter.*;
import io.dallen.tokenizer.Token;

import java.util.List;

class ExpressionSplitParser {
    // defines a multipass split. When a successful split is made, the resulting action will be executed on the result
    private static final SplitSettings splitSettings = new SplitSettings()
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.EQUAL, ExpressionSplitParser::parseAssignment))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.PLUS_EQUAL, ExpressionSplitParser.mathAssignAction(ASTEnums.MathOp.PLUS))
                    .addSplitRule(Token.Symbol.MINUS_EQUAL, ExpressionSplitParser.mathAssignAction(ASTEnums.MathOp.MINUS)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.DOUBLE_AND, ExpressionSplitParser.boolCombineAction(ASTEnums.BoolOp.AND)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.DOUBLE_OR, ExpressionSplitParser.boolCombineAction(ASTEnums.BoolOp.OR)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.BANG_EQUAL, ExpressionSplitParser.compareAction(ASTEnums.CompareOp.NE))
                    .addSplitRule(Token.Symbol.DOUBLE_EQUAL, ExpressionSplitParser.compareAction(ASTEnums.CompareOp.EQ))
                    .addSplitRule(Token.Symbol.LEFT_ANGLE, ExpressionSplitParser.compareAction(ASTEnums.CompareOp.LT))
                    .addSplitRule(Token.Symbol.LEFT_ANGLE_EQUAL, ExpressionSplitParser.compareAction(ASTEnums.CompareOp.LE))
                    .addSplitRule(Token.Symbol.RIGHT_ANGLE, ExpressionSplitParser.compareAction(ASTEnums.CompareOp.GT))
                    .addSplitRule(Token.Symbol.RIGHT_ANGLE_EQUAL, ExpressionSplitParser.compareAction(ASTEnums.CompareOp.GE)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.SLASH, ExpressionSplitParser.mathAction(ASTEnums.MathOp.DIV))
                    .addSplitRule(Token.Symbol.STAR, ExpressionSplitParser.mathAction(ASTEnums.MathOp.MUL)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.PLUS, ExpressionSplitParser.mathAction(ASTEnums.MathOp.PLUS))
                    .addSplitRule(Token.Symbol.MINUS, ExpressionSplitParser.mathAction(ASTEnums.MathOp.MINUS)))
            .addLayer(new SplitLayer()
                    .addSplitRule(Token.Symbol.DOT, ExpressionSplitParser.statementAction(Dotted::new))).leftToRight(false);

    static Statement split(Parser parser, List<Token> workingTokens) {
        return new LayeredSplitter(splitSettings, parser).execute(workingTokens);
    }

    private static Statement parseAssignment(Parser parser, List<Token> first, List<Token> second, List<Token> allTokens) {
        List<List<Token>> res;
        try {
            res = BraceSplitter.splitAll(first, Token.Symbol.COLON);
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return null;
        }
        if(res.size() == 1) {
            Statement firstS = new Parser(first, parser).parseExpression();
            Statement secondS = new Parser(second, parser).parseExpression();
            return new Assign(firstS, secondS, allTokens);
        } else if(res.size() == 2) {
            Type typ = new Parser(res.get(1), parser).getCommon().parseType();
            if(res.get(0).size() != 1) {
                parser.throwError("Declare assign name had multiple parts", res.get(0).get(0));
                return null;
            }
            String name = res.get(0).get(0).literal;
            Statement secondS = new Parser(second, parser).parseExpression();
            return new DeclareAssign(secondS, typ, name, allTokens);
        }
        parser.throwError("Assign name had many colons", res.get(0).get(0));
        return null;
    }

    private static SplitAction boolCombineAction(ASTEnums.BoolOp op) {
        return statementAction((first, second, tokens) -> new BoolCombine(first, op, second, tokens));
    }

    private static SplitAction compareAction(ASTEnums.CompareOp op) {
        return (parser, first, second, tokens) -> {
            if(first.get(first.size() - 1).ident == Token.IdentifierType.TYPE) {
                return null;
            }

            Parser rhsParser = new Parser(second, parser);

            if(rhsParser.containsBefore(Token.Symbol.RIGHT_ANGLE, Token.Symbol.SEMICOLON)) {
                List<Token> rhs = rhsParser.selectTo(Token.Symbol.RIGHT_ANGLE);
                if(rhs.get(rhs.size() - 1).ident == Token.IdentifierType.TYPE) {
                    return null;
                }
            }

            Statement firstS = new Parser(first, parser).parseExpression();
            Statement secondS = new Parser(second, parser).parseExpression();
            return new Compare(firstS, op, secondS, tokens);
        };
    }

    private static SplitAction mathAction(ASTEnums.MathOp op) {
        return statementAction((first, second, tokens) -> new MathStatement(first, op, second, tokens));
    }

    private static SplitAction mathAssignAction(ASTEnums.MathOp op) {
        return statementAction((first, second, tokens) -> new MathAssign(first, op, second, tokens));
    }

    private static SplitAction statementAction(StatementAction action) {
        return (parser, first, second, token) -> {
            Statement firstS = new Parser(first, parser).parseExpression();
            Statement secondS = new Parser(second, parser).parseExpression();
            List<Token> workingTokens;
            if(token.get(token.size() - 1).type == Token.Textless.EOF) {
                workingTokens = token.subList(0, token.size()-1);
            } else {
                workingTokens = token;
            }
            return action.handle(firstS, secondS, workingTokens);
        };
    }

    private interface StatementAction {
        Statement handle(Statement first, Statement second, List<Token> tokens);
    }
}
