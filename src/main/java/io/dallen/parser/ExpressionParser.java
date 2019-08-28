package io.dallen.parser;

import io.dallen.AST;
import io.dallen.ASTEnums;
import io.dallen.compiler.CompileError;
import io.dallen.parser.splitter.BraceSplitter;
import io.dallen.tokenizer.Token;
import io.dallen.tokenizer.Token.Keyword;
import io.dallen.tokenizer.Token.Symbol;
import io.dallen.tokenizer.Token.Textless;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ExpressionParser {

    private Parser parser;

    private List<Token> workingTokens;


    private static final AdvancedSwitch<Token.TokenType, AST.Statement, ExpressionParser> expressionSwitch =
        new AdvancedSwitch<Token.TokenType, AST.Statement, ExpressionParser>()
            .addCase(Keyword.NEW::equals, ExpressionParser::parseNew)
            .addCase(Symbol.LEFT_PAREN::equals, context -> {
                context.parser.consumeExpected(Token.Symbol.LEFT_PAREN);
                if(!context.parser.containsBefore(Token.Symbol.ARROW, Token.Symbol.SEMICOLON)){
                    List<Token> tokens = context.parser.consumeTo(Token.Symbol.RIGHT_PAREN);
                    AST.Statement sub = new Parser(
                            tokens,
                            context.parser
                    ).parseExpression();
                    return new AST.Parened(sub, tokens);
                }
                return context.parseAnonFunc();
            })
            .addCase(Textless.NAME::equals, context -> context.handleNameToken(context.workingTokens))
            .addCase(Symbol.DOUBLE_MINUS::equals, context -> context.parseIncDec(Symbol.DOUBLE_MINUS, ASTEnums.MathOp.MINUS))
            .addCase(Symbol.DOUBLE_PLUS::equals, context -> context.parseIncDec(Symbol.DOUBLE_PLUS, ASTEnums.MathOp.MINUS))
            .addCase(Keyword.TRUE::equals, context -> context.parseLiteral((j, t) -> new AST.BooleanLiteral(Boolean.TRUE, t)))
            .addCase(Keyword.FALSE::equals, context -> context.parseLiteral((j, t) -> new AST.BooleanLiteral(Boolean.FALSE, t)))
            .addCase(Textless.NUMBER_LITERAL::equals, context ->
                    context.parseLiteral((lit, t) -> new AST.NumberLiteral(Double.parseDouble(lit), t)))
            .addCase(Textless.REGEX_LITERAL::equals, context -> {
                Token t = context.parser.consume();
                String[] seg = t.literal.split("\0");
                AST.RegexLiteral lit = new AST.RegexLiteral(seg[0], seg[1], List.of(t));
                context.parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
                return lit;
            })
            .addCase(Textless.SEQUENCE_LITERAL::equals, context -> context.parseLiteral(AST.SequenceLiteral::new))
            .addCase(Textless.STRING_LITERAL::equals, context -> context.parseLiteral(AST.StringLiteral::new))
            .addCase(Keyword.BREAK::equals, context -> context.parseLiteral((j, t) -> new AST.BreakStatement(t)))
            .addCase(Keyword.NEXT::equals, context -> context.parseLiteral((j, t) -> new AST.ContinueStatement(t)))
            .addCase(Symbol.UNDERSCORE::equals, context -> context.parseLiteral((j, t) -> new AST.Variable("_", t)))
            .setDefault(context -> {
                context.parser.throwError("Token did not match any known patterns", context.parser.current());
                return null;
            });

    ExpressionParser(Parser parser) {
        this.parser = parser;
    }

    AST.Statement parseExpression() {
        workingTokens = parser.selectTo(Token.Symbol.SEMICOLON);

        AST.Statement parsed = ExpressionSplitParser.split(parser, workingTokens);
        if (parsed != null) {
            parser.pos += workingTokens.size() + 1;
            return parsed;
        }

        return expressionSwitch.execute(parser.current().type, this);
    }

    private AST.Statement parseNew() {
        List<Token> tokens = parser.selectToEOF();
        parser.consumeExpected(Token.Keyword.NEW);
        List<Token> name = parser.consumeTo(Token.Symbol.LEFT_PAREN);
        List<List<Token>> paramz = null;
        List<Token> paramTokens = parser.consumeTo(Symbol.RIGHT_PAREN);
        try {
            paramz = BraceSplitter.splitAll(paramTokens, Symbol.COMMA);
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return null;
        }
        AST.Type typeStmt = new Parser(name, parser).getCommon().parseType();
        List<AST.Statement> params = paramz
                .stream()
                .map(e -> new Parser(e, parser).parseExpression())
                .collect(Collectors.toList());
        parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
        return new AST.New(typeStmt, params, tokens);
    }

    private AST.Statement parseAnonFunc() {
        List<Token> paramTokens = parser.consumeTo(Token.Symbol.RIGHT_PAREN);

        List<AST.FunctionParam> params;
        try {
            params = parser.getCommon().parseFunctionDecArgs(paramTokens);
        } catch (IndexOutOfBoundsException ex) {
            throw new CompileError("Failed to parse function args for anon func");
        }

        AST.Type returns = Parser.VOID;
        if(parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            returns = new Parser(parser.consumeTo(Token.Symbol.ARROW), parser).getCommon().parseType();
        }

        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser).parseBlock();

        return new AST.AnonFunctionDef(returns, params, body, paramTokens);
    }

    private AST.Statement parseIncDec(Token.TokenType consume, ASTEnums.MathOp type) {
        List<Token> tokens = parser.selectToEOF();
        Token t = parser.consumeExpected(consume);
        AST.Statement sub = new Parser(parser.consumeTo(Token.Symbol.SEMICOLON), parser).parseExpression();
        parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
        return new AST.MathSelfMod(sub, type, ASTEnums.SelfModTime.PRE, tokens);
    }

    private AST.Statement parseLiteral(LiteralTokenConstructor constructor) {
        Token t = parser.consume();
        AST.Statement lit = constructor.construct(t.literal, List.of(t));
        parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
        return lit;
    }

    private interface LiteralTokenConstructor {
        AST.Statement construct(String lit, List<Token> tokens);
    }

    private static final AdvancedSwitch<Token.TokenType, AST.Statement, ExpressionParser> nameTokenSwitch =
            new AdvancedSwitch<Token.TokenType, AST.Statement, ExpressionParser>()
                    .addCase(Textless.NAME::equals, context -> context.handleNameToken(context.workingTokens))
                    .addCase(Symbol.DOUBLE_MINUS::equals, context -> context.parseIncDec(Symbol.DOUBLE_MINUS, ASTEnums.MathOp.MINUS))
                    .addCase(Symbol.DOUBLE_PLUS::equals, context -> context.parseIncDec(Symbol.DOUBLE_PLUS, ASTEnums.MathOp.MINUS));

    private AST.Statement handleNameToken(List<Token> workingTokens) {

        if(parser.containsBefore(Token.Symbol.COLON, Token.Symbol.SEMICOLON)) {
            Token name = parser.consume();
            parser.consumeExpected(Token.Symbol.COLON);
            AST.Type type = new Parser(parser.consumeTo(Token.Symbol.SEMICOLON), parser).getCommon().parseType();
            return new AST.Declare(type, name.literal, workingTokens);
        } else if(parser.containsBefore(Token.Symbol.LEFT_PAREN, Token.Symbol.SEMICOLON)) {
            return parseFunctionCall();
        } else if(parser.containsBefore(Token.Symbol.DOUBLE_MINUS, Token.Symbol.SEMICOLON)) {
            return parsePreIncDec(Token.Symbol.DOUBLE_MINUS, ASTEnums.MathOp.MINUS);
        } else if(parser.containsBefore(Token.Symbol.DOUBLE_PLUS, Token.Symbol.SEMICOLON)) {
            return parsePreIncDec(Token.Symbol.DOUBLE_PLUS, ASTEnums.MathOp.PLUS);
        } else if(parser.containsBefore(Token.Symbol.LEFT_BRACKET, Token.Symbol.SEMICOLON)) {
            List<Token> tokens = parser.selectToEOF();
            List<Token> name = parser.consumeTo(Token.Symbol.LEFT_BRACKET);
            AST.Statement left = new Parser(name, parser).parseExpression();
            List<Token> sub = parser.consumeTo(Token.Symbol.RIGHT_BRACKET);
            AST.Statement inner = new Parser(sub, parser).parseExpression();
            return new AST.Subscript(left, inner, tokens);
        } else {
            Token name = parser.consume();
            parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
            return new AST.Variable(name.literal, workingTokens);
        }
    }

    private AST.Statement parseFunctionCall() {
        List<Token> tokens = parser.selectTo(Symbol.SEMICOLON);
        List<Token> funcName;
        List<AST.Type> generics = new ArrayList<>();
        if(parser.containsBefore(Token.Symbol.LEFT_ANGLE, Token.Symbol.LEFT_PAREN)) {
            funcName = parser.consumeTo(Token.Symbol.LEFT_ANGLE);
            List<List<Token>> genericTokens;
            try {
                genericTokens = BraceSplitter.customSplitAll(
                        BraceManager.leftToRightAngle, parser.consumeTo(Symbol.RIGHT_ANGLE), Symbol.COMMA);
            } catch (ParserError parserError) {
                parser.throwError(parserError.msg, parserError.on);
                return null;
            }
            generics = genericTokens.stream().map(tokenList -> new Parser(tokenList, parser).getCommon().parseType())
                    .collect(Collectors.toList());

            parser.consumeExpected(Token.Symbol.LEFT_PAREN);
        } else {
            funcName = parser.consumeTo(Token.Symbol.LEFT_PAREN);
        }

        if(funcName.size() > 1) {
            parser.throwError("Function call name was multi token", funcName.get(0));
            return null;
        }
        List<AST.Statement> funcParams = consumeFunctionParams();
        parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
        return new AST.FunctionCall(funcName.get(0).literal, funcParams, generics, tokens);
    }

    private AST.Statement parsePreIncDec(Token.TokenType consume, ASTEnums.MathOp type) {
        List<Token> tokens = parser.selectToEOF();
        List<Token> name = parser.consumeTo(consume);
        AST.Statement left = new Parser(name, parser).parseExpression();
        parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
        return new AST.MathSelfMod(left, type, ASTEnums.SelfModTime.POST, tokens);
    }

    private List<AST.Statement> consumeFunctionParams() {
        List<Token> params = parser.consumeTo(Token.Symbol.RIGHT_PAREN);
        List<List<Token>> paramTokens;
        try {
            paramTokens = BraceSplitter.splitAll(params, Symbol.COMMA);
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return List.of();
        }
        return paramTokens.stream()
                .filter(arr -> !arr.isEmpty())
                .map(t -> new Parser(t, parser))
                .map(Parser::parseExpression)
                .collect(Collectors.toList());
    }
}
