package io.dallen.parser;

import io.dallen.tokenizer.Token;
import io.dallen.parser.AST.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Parser {

    public static class ParserError extends RuntimeException {
        public ParserError(String msg, Token t) {
            super(msg + " Token: " + t.toString());
        }
    }

    private List<Token> tokens;

    private int pos;

    // this list defines a multipass split. When a successful split is made, the resulting action will be executed on the result
    private static final List<Map<Token.TokenType, Splitter.SplitAction>> splitRules =
            new ArrayList<Map<Token.TokenType, Splitter.SplitAction>>() {{
                add(new HashMap<Token.TokenType, Splitter.SplitAction>() {{
                    put(Token.Symbol.EQUAL, (Splitter.RawSplitAction) ExpressionParser::parseAssignment);
                }});
//                add(new HashMap<Token.TokenType, Splitter.SplitAction>() {{
//                    put(Token.Symbol.MINUS_EQUAL, (Splitter.StatementSplitAction) (first, second) -> new AST.MathAssign(first, MathOp.MINUS, second));
//                    put(Token.Symbol.PLUS_EQUAL, (Splitter.StatementSplitAction) (first, second) -> new AST.MathAssign(first, MathOp.PLUS, second));
//                    put(Token.Symbol.STAR_EQUAL, (Splitter.StatementSplitAction) (first, second) -> new AST.MathAssign(first, MathOp.MUL, second));
//                    put(Token.Symbol.SLASH_EQUAL, (Splitter.StatementSplitAction) (first, second) -> new AST.MathAssign(first, MathOp.DIV, second));
//                }});
                add(new HashMap<Token.TokenType, Splitter.SplitAction>() {{
                    put(Token.Symbol.BOOL_AND, (Splitter.StatementSplitAction) (first, second) -> new AST.BoolCombine(first, BoolOp.AND, second));
                }});
                add(new HashMap<Token.TokenType, Splitter.SplitAction>() {{
                    put(Token.Symbol.BOOL_OR, (Splitter.StatementSplitAction) (first, second) -> new AST.BoolCombine(first, BoolOp.OR, second));
                }});
                add(new HashMap<Token.TokenType, Splitter.SplitAction>() {{
                    put(Token.Symbol.DOUBLE_EQUAL, (Splitter.StatementSplitAction) (first, second) -> new AST.Compare(first, CompareOp.EQ, second));
                    put(Token.Symbol.LEFT_ANGLE, (Splitter.StatementSplitAction) (first, second) -> new AST.Compare(first, CompareOp.LT, second));
                    put(Token.Symbol.RIGHT_ANGLE, (Splitter.StatementSplitAction) (first, second) -> new AST.Compare(first, CompareOp.GT, second));
//                    put(Token.Symbol.LESS_EQUAL, (Splitter.StatementSplitAction) (first, second) -> new AST.Compare(first, CompareOp.LE, second));
//                    put(Token.Symbol.GREATER_EQUAL, (Splitter.StatementSplitAction) (first, second) -> new AST.Compare(first, CompareOp.GE, second));
                }});
                add(new HashMap<Token.TokenType, Splitter.SplitAction>() {{
                    put(Token.Symbol.SLASH, (Splitter.StatementSplitAction) (first, second) -> new AST.Math(first, MathOp.DIV, second));
                }});
                add(new HashMap<Token.TokenType, Splitter.SplitAction>() {{
                    put(Token.Symbol.PLUS, (Splitter.StatementSplitAction) (first, second) -> new AST.Math(first, MathOp.PLUS, second));
                    put(Token.Symbol.MINUS, (Splitter.StatementSplitAction) (first, second) -> new AST.Math(first, MathOp.MINUS, second));
                }});
                add(new HashMap<Token.TokenType, Splitter.SplitAction>() {{
                    put(Token.Symbol.DOT, (Splitter.StatementSplitAction) Dotted::new);
                }});
            }};

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token current() {
        if (pos >= tokens.size()) {
            return Token.EOF;
        }

        return tokens.get(pos);
    }

    private Token consume() {
        Token tok = current();
        next();
        return tok;
    }

    private Token consumeExpected(Token.TokenType type) {
        Token t;
        if ((t = consume()).type != type) {
            throw new ParserError("Parse error Expected: " + type.toString(), t);
        }
        return t;
    }

    private Token tryConsumeExpected(Token.TokenType type) {
        Token t = current();
        if (current().type == type) {
            consume();
        } else {
            return null;
        }
        return t;
    }

    private void next() {
        pos++;
    }

    private Token peek() {
        if (pos + 1 >= tokens.size()) {
            return Token.EOF;
        }

        return tokens.get(pos + 1);
    }

    // Advances current location
    private List<Token> consumeTo(Token.TokenType type) {
        List<Token> tokens = new ArrayList<>();
        while (current().type != Token.Textless.EOF && !current().type.equals(type)) {
            tokens.add(current());
            next();
        }
        next();
        return tokens;
    }

    private List<Token> consumeToBraceAware(Token.TokenType type) {
        List<Token> tokens = new ArrayList<>();
        BraceManager braceManager = new BraceManager(false);
        while (true) {
            if (current().type == Token.Textless.EOF) {
                if (braceManager.isEmpty()) {
                    break;
                }
                throw new ParserError("Parse error", current());
            }
            if (current().type == type && braceManager.isEmpty()) {
                break;
            }
            braceManager.check(current());
            tokens.add(current());
            next();
        }
        next();
        return tokens;
    }

    // Just selects the tokens, does not advance the current location
    private List<Token> selectTo(Token.TokenType type) {
        List<Token> selected = new ArrayList<>();
        int loc = pos;
        while (loc < tokens.size() && tokens.get(loc).type != type) {
            selected.add(tokens.get(loc));
            loc++;
        }
        return selected;
    }

    // Check if one token comes before another
    private boolean containsBefore(Token.TokenType what, Token.TokenType before) {
        for (int lpos = pos; lpos < tokens.size(); lpos++) {
            if (tokens.get(lpos).type == what) {
                return true;
            }
            if (tokens.get(lpos).type == before) {
                return false;
            }
        }
        return before == Token.Textless.EOF;
    }

    public List<Statement> parseBlock() {
        ArrayList<Statement> statements = new ArrayList<>();

        while (!current().type.equals(Token.Textless.EOF)) {
            Token.TokenType i = current().type;
            if (Token.Keyword.WHILE.equals(i)) {
                statements.add(parseWhileBlock());
                continue;
            }
            if (Token.Keyword.IF.equals(i)) {
                statements.add(parseIfBlock());
                continue;
            }
            if (Token.Keyword.DEF.equals(i)) {
                statements.add(parseFunctionDef());
                continue;
            }
            statements.add(parseExpression());
        }
        return statements;
    }

    private IfBlock parseIfBlock() {
        next();

        consumeExpected(Token.Symbol.LEFT_PAREN);
        List<Token> condTokens = consumeToBraceAware(Token.Symbol.RIGHT_PAREN);
        consumeExpected(Token.Symbol.LEFT_BRACE);
        Statement cond = new Parser(condTokens).parseExpression();

        List<Token> bodyTokens = consumeToBraceAware(Token.Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        return new IfBlock(cond, body);
    }

    private WhileBlock parseWhileBlock() {
        next();

        consumeExpected(Token.Symbol.LEFT_PAREN);
        List<Token> condTokens = consumeToBraceAware(Token.Symbol.RIGHT_PAREN);
        consumeExpected(Token.Symbol.LEFT_BRACE);
        Statement cond = new Parser(condTokens).parseExpression();

        List<Token> bodyTokens = consumeTo(Token.Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        return new WhileBlock(cond, body);
    }

    private FunctionDef parseFunctionDef() {
        consumeExpected(Token.Keyword.DEF);

        String funcName = consume().literal;
        consumeExpected(Token.Symbol.LEFT_PAREN);
        List<Token> paramTokens = consumeToBraceAware(Token.Symbol.RIGHT_PAREN);

        List<FunctionParam> params = Splitter.braceSplit(paramTokens, Token.Symbol.COMMA, -1)
                .stream()
                .map(e -> Splitter.braceSplit(e, Token.Symbol.COLON, -1))
                .map(e -> new FunctionParam(new Parser(e.get(0)).parseType(), e.get(0).get(0).literal))
                .collect(Collectors.toList());

        consumeExpected(Token.Symbol.COLON);
        List<Token> returnTypeTokens = consumeToBraceAware(Token.Symbol.LEFT_BRACE);
        Type returnType = new Parser(returnTypeTokens).parseType();

        List<Token> bodyTokens = consumeTo(Token.Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        return new FunctionDef(returnType, funcName, params, body);
    }

    private Type parseType() {
        String typeName = consume().literal;
        return new Type(typeName, 0);
    }

    public Statement parseExpression() {
        List<Token> workingTokens = selectTo(Token.Symbol.SEMICOLON);

        Statement parsed = Splitter.rankedSingleSplit(workingTokens, splitRules);
        if (parsed != null) {
            pos += workingTokens.size() + 1;
            return parsed;
        }

        if (current().type == Token.Symbol.LEFT_PAREN) {
            consumeExpected(Token.Symbol.LEFT_PAREN);
            Statement sub = new Parser(consumeToBraceAware(Token.Symbol.RIGHT_PAREN)).parseExpression();
            return new Parened(sub);
        } else if (current().type == Token.Textless.NAME && containsBefore(Token.Symbol.LEFT_PAREN, Token.Symbol.SEMICOLON)) {
            Token name = consume();
            consumeExpected(Token.Symbol.LEFT_PAREN);
            List<Statement> funcParams = consumeFunctionParams();
            tryConsumeExpected(Token.Symbol.SEMICOLON);
            return new FunctionCall(name.literal, funcParams);
        } else if (current().type == Token.Textless.NUMBER_LITERAL) {
            tryConsumeExpected(Token.Symbol.SEMICOLON);
            return new NumberLiteral(Double.parseDouble(current().literal));
        } else if (current().type == Token.Textless.STRING_LITERAL) {
            tryConsumeExpected(Token.Symbol.SEMICOLON);
            return new StringLiteral(current().literal);
        } else if (current().type == Token.Textless.NAME) {
            tryConsumeExpected(Token.Symbol.SEMICOLON);
            return new Variable(current().literal);
        } else {
            throw new ParserError("Unknown token sequence", current());
        }
    }

    private List<Statement> consumeFunctionParams() {
        List<Token> params = consumeToBraceAware(Token.Symbol.RIGHT_PAREN);
        List<List<Token>> paramTokens = Splitter.braceSplit(params, Token.Symbol.COMMA, -1);
        return paramTokens.stream()
                .filter(arr -> !arr.isEmpty())
                .map(Parser::new)
                .map(Parser::parseExpression)
                .collect(Collectors.toList());
    }
}


