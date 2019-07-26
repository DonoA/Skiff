package io.dallen.parser;

import io.dallen.AST;
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
                    AST.Statement sub = new Parser(context.parser.consumeTo(Token.Symbol.RIGHT_PAREN)).parseExpression();
                    return new AST.Parened(sub);
                }
                return context.parseAnonFunc();
            })
            .addCase(Textless.NAME::equals, context -> context.handleNameToken(context.workingTokens))
            .addCase(Symbol.DOUBLE_MINUS::equals, context -> context.parseIncDec(Symbol.DOUBLE_MINUS, AST.MathOp.MINUS))
            .addCase(Symbol.DOUBLE_PLUS::equals, context -> context.parseIncDec(Symbol.DOUBLE_PLUS, AST.MathOp.MINUS))
            .addCase(Keyword.TRUE::equals, context -> context.parseLiteral(j -> new AST.BooleanLiteral(Boolean.TRUE)))
            .addCase(Keyword.FALSE::equals, context -> context.parseLiteral(j -> new AST.BooleanLiteral(Boolean.FALSE)))
            .addCase(Textless.NUMBER_LITERAL::equals, context ->
                    context.parseLiteral(lit -> new AST.NumberLiteral(Double.parseDouble(lit))))
            .addCase(Textless.REGEX_LITERAL::equals, context -> {
                String[] seg = context.parser.consume().literal.split("\0");
                AST.RegexLiteral lit = new AST.RegexLiteral(seg[0], seg[1]);
                context.parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
                return lit;
            })
            .addCase(Textless.SEQUENCE_LITERAL::equals, context -> context.parseLiteral(AST.SequenceLiteral::new))
            .addCase(Textless.STRING_LITERAL::equals, context -> context.parseLiteral(AST.StringLiteral::new))
            .addCase(Keyword.BREAK::equals, context -> context.parseLiteral(j -> new AST.BreakStatement()))
            .addCase(Keyword.NEXT::equals, context -> context.parseLiteral(j -> new AST.ContinueStatement()))
            .addCase(Symbol.UNDERSCORE::equals, context -> context.parseLiteral(j -> new AST.Variable("_")))
            .setDefault(context -> {
                throw new ParserError("Unknown token sequence", context.parser.current());
            });

    ExpressionParser(Parser parser) {
        this.parser = parser;
    }

    AST.Statement parseExpression() {
        workingTokens = parser.selectTo(Token.Symbol.SEMICOLON);

        AST.Statement parsed = ExpressionSplitParser.split(workingTokens);
        if (parsed != null) {
            parser.pos += workingTokens.size() + 1;
            return parsed;
        }

        return expressionSwitch.execute(parser.current().type, this);
    }

    private AST.Statement parseNew() {
        parser.consumeExpected(Token.Keyword.NEW);
        List<Token> name = parser.consumeTo(Token.Symbol.LEFT_PAREN);
        List<List<Token>> paramz = BraceSplitter.splitAll(parser.consumeTo(Token.Symbol.RIGHT_PAREN), Token.Symbol.COMMA);
        AST.Type typeStmt = new Parser(name).getCommon().parseType();
        List<AST.Statement> params = paramz
                .stream()
                .map(e -> new Parser(e).parseExpression())
                .collect(Collectors.toList());
        parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
        return new AST.New(typeStmt, params);
    }

    private AST.Statement parseAnonFunc() {
        List<Token> paramTokens = parser.consumeTo(Token.Symbol.RIGHT_PAREN);

        List<AST.FunctionParam> params;
        try {
            params = parser.getCommon().parseFunctionDecArgs(paramTokens);
        } catch (IndexOutOfBoundsException ex) {
            throw new CompileError("Failed to parse function args for anon func");
        }

        AST.Type returns = AST.Type.VOID;
        if(parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            returns = new Parser(parser.consumeTo(Token.Symbol.ARROW)).getCommon().parseType();
        }

        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens).parseBlock();

        return new AST.AnonFunctionDef(returns, params, body);
    }

    private AST.Statement parseIncDec(Token.TokenType consume, AST.MathOp type) {
        parser.consumeExpected(consume);
        AST.Statement sub = new Parser(parser.consumeTo(Token.Symbol.SEMICOLON)).parseExpression();
        parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
        return new AST.MathSelfMod(sub, type, AST.SelfModTime.PRE);
    }

    private AST.Statement parseLiteral(LiteralConstructor constructor) {
        AST.Statement lit = constructor.construct(parser.consume().literal);
        parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
        return lit;
    }

    private interface LiteralConstructor {
        AST.Statement construct(String lit);
    }

    private static final AdvancedSwitch<Token.TokenType, AST.Statement, ExpressionParser> nameTokenSwitch =
            new AdvancedSwitch<Token.TokenType, AST.Statement, ExpressionParser>()
                    .addCase(Textless.NAME::equals, context -> context.handleNameToken(context.workingTokens))
                    .addCase(Symbol.DOUBLE_MINUS::equals, context -> context.parseIncDec(Symbol.DOUBLE_MINUS, AST.MathOp.MINUS))
                    .addCase(Symbol.DOUBLE_PLUS::equals, context -> context.parseIncDec(Symbol.DOUBLE_PLUS, AST.MathOp.MINUS));

    private AST.Statement handleNameToken(List<Token> workingTokens) {

        if(parser.containsBefore(Token.Symbol.COLON, Token.Symbol.SEMICOLON)) {
            Token name = parser.consume();
            parser.consumeExpected(Token.Symbol.COLON);
            AST.Type type = new Parser(parser.consumeTo(Token.Symbol.SEMICOLON)).getCommon().parseType();
            return new AST.Declare(type, name.literal);
        } else if(parser.containsBefore(Token.Symbol.LEFT_PAREN, Token.Symbol.SEMICOLON)) {
            return parseFunctionCall();
        } else if(parser.containsBefore(Token.Symbol.DOUBLE_MINUS, Token.Symbol.SEMICOLON)) {
            return parsePreIncDec(Token.Symbol.DOUBLE_MINUS, AST.MathOp.MINUS);
        } else if(parser.containsBefore(Token.Symbol.DOUBLE_PLUS, Token.Symbol.SEMICOLON)) {
            return parsePreIncDec(Token.Symbol.DOUBLE_PLUS, AST.MathOp.PLUS);
        } else if(parser.containsBefore(Token.Symbol.LEFT_BRACKET, Token.Symbol.SEMICOLON)) {
            List<Token> name = parser.consumeTo(Token.Symbol.LEFT_BRACKET);
            AST.Statement left = new Parser(name).parseExpression();
            List<Token> sub = parser.consumeTo(Token.Symbol.RIGHT_BRACKET);
            AST.Statement inner = new Parser(sub).parseExpression();
            return new AST.Subscript(left, inner);
        } else {
            Token name = parser.consume();
            parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
            return new AST.Variable(name.literal);
        }
    }

    private AST.Statement parseFunctionCall() {
        List<Token> funcName;
        List<AST.Type> generics = new ArrayList<>();
        if(parser.containsBefore(Token.Symbol.LEFT_ANGLE, Token.Symbol.LEFT_PAREN)) {
            funcName = parser.consumeTo(Token.Symbol.LEFT_ANGLE);
            List<List<Token>> genericTokens = BraceSplitter.customSplitAll(
                    BraceManager.leftToRightAngle, parser.consumeTo(Token.Symbol.RIGHT_ANGLE), Token.Symbol.COMMA);
            generics = genericTokens.stream().map(tokenList -> new Parser(tokenList).getCommon().parseType())
                    .collect(Collectors.toList());

            parser.consumeExpected(Token.Symbol.LEFT_PAREN);
        } else {
            funcName = parser.consumeTo(Token.Symbol.LEFT_PAREN);
        }

        if(funcName.size() > 1) {
            throw new ParserError("Function call name was multi token", funcName.get(0));
        }
        List<AST.Statement> funcParams = consumeFunctionParams();
        parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
        return new AST.FunctionCall(funcName.get(0).literal, funcParams, generics);
    }

    private AST.Statement parsePreIncDec(Token.TokenType consume, AST.MathOp type) {
        List<Token> name = parser.consumeTo(consume);
        AST.Statement left = new Parser(name).parseExpression();
        parser.tryConsumeExpected(Token.Symbol.SEMICOLON);
        return new AST.MathSelfMod(left, type, AST.SelfModTime.POST);
    }

    private List<AST.Statement> consumeFunctionParams() {
        List<Token> params = parser.consumeTo(Token.Symbol.RIGHT_PAREN);
        List<List<Token>> paramTokens = BraceSplitter.splitAll(params, Token.Symbol.COMMA);
        return paramTokens.stream()
                .filter(arr -> !arr.isEmpty())
                .map(Parser::new)
                .map(Parser::parseExpression)
                .collect(Collectors.toList());
    }
}
