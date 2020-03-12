package io.dallen.parser;

import io.dallen.ast.AST;
import io.dallen.ast.ASTEnums;
import io.dallen.parser.splitter.BraceSplitter;
import io.dallen.tokenizer.Token;
import io.dallen.tokenizer.Token.Keyword;
import io.dallen.tokenizer.Token.Symbol;
import io.dallen.tokenizer.Token.Textless;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ExpressionParser {

    // Switch statement for enums from many classes.
    private static final AdvancedSwitch<Token.TokenType, AST.Statement, Parser> expressionSwitch =
            new AdvancedSwitch<Token.TokenType, AST.Statement, Parser>()
                    .addCase(Keyword.NEW::equals, ExpressionParser::parseNew)
                    .addCase(Symbol.LEFT_PAREN::equals, ExpressionParser::leftParenHandler)
                    .addCase(Textless.NAME::equals, ExpressionParser::handleNameToken)
                    .addCase(Symbol.DOUBLE_MINUS::equals, ExpressionParser.handleIncDec(Symbol.DOUBLE_MINUS,
                            ASTEnums.MathOp.MINUS))
                    .addCase(Symbol.DOUBLE_PLUS::equals, ExpressionParser.handleIncDec(Symbol.DOUBLE_PLUS,
                            ASTEnums.MathOp.MINUS))
                    .addCase(Keyword.TRUE::equals, ExpressionParser.handleBoolLiteral(Keyword.TRUE))
                    .addCase(Keyword.FALSE::equals, ExpressionParser.handleBoolLiteral(Keyword.FALSE))
                    .addCase(Textless.NUMBER_LITERAL::equals, ExpressionParser::handleNumLiteral)
                    .addCase(Textless.REGEX_LITERAL::equals, ExpressionParser::handleRegex)
                    .addCase(Textless.SEQUENCE_LITERAL::equals, ExpressionParser.handleLiteral(Textless.SEQUENCE_LITERAL,
                            AST.SequenceLiteral::new))
                    .addCase(Textless.STRING_LITERAL::equals, ExpressionParser.handleLiteral(Textless.STRING_LITERAL,
                            AST.StringLiteral::new))
                    .addCase(Keyword.BREAK::equals, ExpressionParser::handleBreakLiteral)
                    .addCase(Keyword.NEXT::equals, ExpressionParser::handleContinueLiteral)
                    .addCase(Symbol.UNDERSCORE::equals, ExpressionParser::handleUnderscore)
                    .setDefault(parser -> {
                        parser.throwError("Token did not match any known patterns", parser.current());
                        return null;
                    });

    // Parse a series of tokens that can be used at any point in the code. They can standalone or be part of larger
    // block statements or other expressions
    static AST.Statement parseExpression(Parser parser) {
        Parser workingParser = parser.subParserTo(Token.Symbol.SEMICOLON);

        AST.Statement parsed = ExpressionSplitParser.split(workingParser);
        if (parsed != null) {
//            parser.pos += workingParser.tokenCount() + 1;
            return parsed;
        }

        if (workingParser.current().type == Textless.EOF) {
            return null;
        }

        return expressionSwitch.execute(workingParser.current().type, workingParser);
    }

    private static AST.Statement leftParenHandler(Parser parser) {
        int startPos = parser.absolutePos();
        if (!parser.containsBefore(Token.Symbol.FAT_ARROW, Token.Symbol.SEMICOLON)) {
            parser.consumeExpected(Token.Symbol.LEFT_PAREN);
            AST.Statement sub = parser.subParserTo(Token.Symbol.RIGHT_PAREN).parseExpression();
            return new AST.Parened(sub, startPos, parser.absolutePos());
        }
        return ExpressionParser.parseAnonFunc(parser);
    }

    private static AdvancedSwitch.CaseHandler<Parser, AST.Statement> handleIncDec(Token.TokenType consume,
                                                                                  ASTEnums.MathOp type) {
        return (parser) -> {
            int startPos = parser.absolutePos();
            parser.consumeExpected(consume);
            AST.Statement sub = parser.subParserTo(Token.Symbol.SEMICOLON).parseExpression();
            return new AST.MathSelfMod(sub, type, ASTEnums.SelfModTime.PRE, startPos, parser.absolutePos());
        };
    }

    private interface LiteralTokenConstructor {
        AST.Statement construct(String lit, int tokenLoc, int tokenLocEnd);
    }

    private static AdvancedSwitch.CaseHandler<Parser, AST.Statement> handleBoolLiteral(Token.TokenType litType) {
        return (parser) -> {
            int startPos = parser.absolutePos();
            parser.consumeExpected(litType);
            return new AST.BooleanLiteral(litType == Keyword.TRUE, startPos, startPos);
        };
    }

    private static AST.BreakStatement handleBreakLiteral(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Keyword.BREAK);
        return new AST.BreakStatement(startPos, parser.absolutePos());
    }

    private static AST.ContinueStatement handleContinueLiteral(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Keyword.NEXT);
        return new AST.ContinueStatement(startPos, parser.absolutePos());
    }

    private static AST.NumberLiteral handleNumLiteral(Parser parser) {
        int startPos = parser.absolutePos();
        Token t = parser.consumeExpected(Textless.NUMBER_LITERAL);
        return new AST.NumberLiteral(Double.parseDouble(t.literal), startPos, startPos);
    }

    private static AST.Variable handleUnderscore(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Symbol.UNDERSCORE);
        return new AST.Variable("_", startPos, parser.absolutePos());
    }

    private static AdvancedSwitch.CaseHandler<Parser, AST.Statement> handleLiteral(Token.TokenType litType,
                                                                                   LiteralTokenConstructor constructor) {
        return (parser) -> {
            int startPos = parser.absolutePos();
            Token t = parser.consumeExpected(litType);
            return constructor.construct(t.literal, startPos, parser.absolutePos());
        };
    }

    private static AST.RegexLiteral handleRegex(Parser parser) {
        int startPos = parser.absolutePos();
        Token t = parser.consumeExpected(Textless.REGEX_LITERAL);
        String[] seg = t.literal.split("\0");
        return new AST.RegexLiteral(seg[0], seg[1], startPos, parser.absolutePos());
    }

    private static AST.Statement parseNew(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Token.Keyword.NEW);
        Parser name = parser.subParserTo(Token.Symbol.LEFT_PAREN);
        List<Parser> paramz = null;
        Parser paramTokens = parser.subParserTo(Symbol.RIGHT_PAREN);
        try {
            paramz = BraceSplitter.splitAll(paramTokens, Symbol.COMMA);
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return null;
        }
        AST.Type typeStmt = CommonParsing.parseType(name);
        List<AST.Statement> params = paramz
                .stream()
                .map(Parser::parseExpression)
                .collect(Collectors.toList());

        return new AST.New(typeStmt, params, startPos, parser.absolutePos());
    }

    private static AST.Statement parseAnonFunc(Parser parser) {
        int startPos = parser.absolutePos();

        parser.consumeExpected(Symbol.LEFT_PAREN);
        Parser paramTokens = parser.subParserTo(Token.Symbol.RIGHT_PAREN);

        List<AST.FunctionParam> params;
        try {
            params = CommonParsing.parseFunctionDecArgs(paramTokens);
        } catch (IndexOutOfBoundsException ex) {
            parser.throwError("Failed to parse function args for anon func", paramTokens.get(0));
            return null;
        }

        AST.Type returns = CommonParsing.voidFor(parser.current());
        if (parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            returns = CommonParsing.parseType(parser.subParserTo(Token.Symbol.FAT_ARROW));
        }

        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<AST.Statement> body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE));

        return new AST.AnonFunctionDef(returns, params, body, startPos, parser.absolutePos());
    }

    // Deal with the many possible options when encountering a name token
    private static AST.Statement handleNameToken(Parser parser) {
        int startPos = parser.absolutePos();
        if (parser.containsBefore(Token.Symbol.COLON, Token.Textless.EOF)) {
            Token name = parser.consumeExpected(Textless.NAME);
            parser.consumeExpected(Token.Symbol.COLON);
            AST.Type type = CommonParsing.parseType(parser.subParserTo(Token.Symbol.SEMICOLON));
            return new AST.Declare(type, name.literal, new ArrayList<>(), startPos, parser.absolutePos());
        } else if (parser.containsBefore(Token.Symbol.LEFT_PAREN, Token.Symbol.SEMICOLON)) {
            return parseFunctionCall(parser);
        } else if (parser.containsBefore(Token.Symbol.DOUBLE_MINUS, Token.Symbol.SEMICOLON)) {
            return parsePreIncDec(parser, Token.Symbol.DOUBLE_MINUS, ASTEnums.MathOp.MINUS);
        } else if (parser.containsBefore(Token.Symbol.DOUBLE_PLUS, Token.Symbol.SEMICOLON)) {
            return parsePreIncDec(parser, Token.Symbol.DOUBLE_PLUS, ASTEnums.MathOp.PLUS);
        } else if (parser.containsBefore(Token.Symbol.LEFT_BRACKET, Token.Symbol.SEMICOLON)) {
            AST.Statement left = parser.subParserTo(Token.Symbol.LEFT_BRACKET).parseExpression();
            AST.Statement inner = parser.subParserTo(Token.Symbol.RIGHT_BRACKET).parseExpression();
            return new AST.Subscript(left, inner, startPos, parser.absolutePos());
        } else {
            Token name = parser.consumeExpected(Textless.NAME);
            return new AST.Variable(name.literal, startPos, parser.absolutePos());
        }
    }

    private static AST.Statement parseFunctionCall(Parser parser) {
        int startPos = parser.absolutePos();

        Parser funcName;
        List<AST.Type> generics = new ArrayList<>();
        if (parser.containsBefore(Token.Symbol.LEFT_ANGLE, Token.Symbol.LEFT_PAREN)) {
            funcName = parser.subParserTo(Token.Symbol.LEFT_ANGLE);
            List<Parser> genericTokens;
            try {
                genericTokens = BraceSplitter.customSplitAll(
                        BraceManager.leftToRightAngle, parser.subParserTo(Symbol.RIGHT_ANGLE), Symbol.COMMA);
            } catch (ParserError parserError) {
                parser.throwError(parserError.msg, parserError.on);
                return null;
            }

            generics = genericTokens
                    .stream()
                    .map(CommonParsing::parseType)
                    .collect(Collectors.toList());

            parser.consumeExpected(Token.Symbol.LEFT_PAREN);
        } else {
            funcName = parser.subParserTo(Token.Symbol.LEFT_PAREN);
        }

        if (funcName.tokenCount() > 1) {
            parser.throwError("Function call name was multi token", funcName.get(0));
            return null;
        }
        List<AST.Statement> funcParams = consumeFunctionParams(parser);
        return new AST.FunctionCall(funcName.get(0).literal, funcParams, generics, startPos, parser.absolutePos());
    }

    private static AST.Statement parsePreIncDec(Parser parser, Token.TokenType consume, ASTEnums.MathOp type) {
        int startPos = parser.absolutePos();

        AST.Statement left = parser.subParserTo(consume).parseExpression();

        return new AST.MathSelfMod(left, type, ASTEnums.SelfModTime.POST, startPos, parser.absolutePos());
    }

    private static List<AST.Statement> consumeFunctionParams(Parser parser) {
        Parser params = parser.subParserTo(Token.Symbol.RIGHT_PAREN);
        List<Parser> paramTokens;
        try {
            paramTokens = BraceSplitter.splitAll(params, Symbol.COMMA);
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return List.of();
        }

        return paramTokens.stream()
                .filter(p -> p.tokenCount() != 0)
                .map(Parser::parseExpression)
                .collect(Collectors.toList());
    }
}
