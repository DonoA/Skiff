package io.dallen.parser;

import io.dallen.ast.AST.*;
import io.dallen.ast.ASTEnums;
import io.dallen.parser.splitter.*;
import io.dallen.tokenizer.Token;

import java.util.ArrayList;
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
                    .addSplitRule(Token.Symbol.DOT,
                            ExpressionSplitParser.statementAction(ExpressionSplitParser::parseNew)
                    )).leftToRight(false);

    static Statement split(Parser parser) {
        return new LayeredSplitter(splitSettings, parser).execute();
    }

    // Handle equal signs, picks up assign and declare assign.
    private static Statement parseAssignment(Parser parent, Parser first, Parser second) {
        List<Parser> res;
        try {
            res = BraceSplitter.splitAll(first, Token.Symbol.COLON);
        } catch (ParserError parserError) {
            parent.throwError(parserError.msg, parserError.on);
            return null;
        }
        if (res.size() == 1) {
            Statement firstS = first.parseExpression();
            Statement secondS = second.parseExpression();
            return new Assign(firstS, secondS, first.absoluteStart(), second.absoluteStop());
        } else if (res.size() == 2) {
            Type typ = CommonParsing.parseType(res.get(1));
            if (res.get(0).tokenCount() != 1) {
                parent.throwError("Declare assign name had multiple parts", res.get(0).get(0));
                return null;
            }
            String name = res.get(0).get(0).literal;
            Statement secondS = second.parseExpression();
            return new DeclareAssign(secondS, typ, name, new ArrayList<>(), first.absoluteStart(), second.absoluteStop());
        }
        parent.throwError("Assign name had many colons", res.get(0).get(0));
        return null;
    }

    // handles '&&' and '||'
    private static SplitAction boolCombineAction(ASTEnums.BoolOp op) {
        return statementAction((first, second) -> new BoolCombine(first, op, second, first.token_start, second.token_end));
    }

    // handles number comparisons including '==' and '!='. Check if ident after '<' is class to defer generic function
    // calls.
    private static SplitAction compareAction(ASTEnums.CompareOp op) {
        return (parser, first, second) -> {
            if (parser.current().type == Token.Keyword.NEW) {
                return null;
            }

            // As in the List in List<String>
            if (first.getBack(1).ident == Token.IdentifierType.TYPE) {
                return null;
            }

            // no:  s(  func<List,String>(x)   )
            // no:  s(  func<List , String>(x) )
            // yes: s( x<List.y , String.y>(x) )
            int braceLoc = parser.indexOf(Token.Symbol.RIGHT_ANGLE);
            if (braceLoc != -1 && parser.get(braceLoc - 1).ident == Token.IdentifierType.TYPE) {
                return null;
            }

            Statement firstS = first.parseExpression();
            Statement secondS = second.parseExpression();
            return new Compare(firstS, op, secondS, first.absoluteStart(), second.absoluteStop());
        };
    }

    private static Statement parseNew(Statement left, Statement right) {
        return new Dotted(left, right, left.token_start, right.token_end);
    }

    // handles simple math ops '+' '-' '*' '/' '**' '%'
    private static SplitAction mathAction(ASTEnums.MathOp op) {
        return statementAction((first, second) -> new MathStatement(first, op, second, first.token_start,
                second.token_end));
    }

    // same as above with assignment, '+=', '-='
    private static SplitAction mathAssignAction(ASTEnums.MathOp op) {
        return statementAction((first, second) -> new MathAssign(first, op, second, first.token_start,
                second.token_end));
    }

    // takes split result, parses both sides, and call action on the result
    private static SplitAction statementAction(StatementAction action) {
        return (parser, first, second) -> {
            Statement firstS = first.parseExpression();
            Statement secondS = second.parseExpression();
            return action.handle(firstS, secondS);
        };
    }

    // Functional interface for passing to statementAction
    private interface StatementAction {
        Statement handle(Statement first, Statement second);
    }
}
