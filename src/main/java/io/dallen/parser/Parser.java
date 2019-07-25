package io.dallen.parser;

import io.dallen.AST;
import io.dallen.tokenizer.Token;
import io.dallen.AST.*;

import io.dallen.tokenizer.Token.Textless;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private List<Token> tokens;

    int pos;

    private final boolean inMatch;

    private final BlockParser blockParser;
    private final ExpressionParser expressionParser;
    private final CommonParsing common;

    public Parser(List<Token> tokens) {
        this(tokens, false);
    }

    Parser(List<Token> tokens, boolean inMatch) {
        this.tokens = tokens;
        this.inMatch = inMatch;

        this.blockParser = new BlockParser(this);
        this.expressionParser = new ExpressionParser(this);
        this.common = new CommonParsing(this);
    }

    Token current() {
        if (pos >= tokens.size()) {
            return Token.EOF;
        }

        return tokens.get(pos);
    }

    Token consume() {
        Token tok = current();
        next();
        return tok;
    }

    Token consumeExpected(Token.TokenType type) {
        Token t;
        if ((t = consume()).type != type) {
            throw new ParserError("Parse error Expected: " + type.toString(), t);
        }
        return t;
    }

    Token tryConsumeExpected(Token.TokenType type) {
        Token t = current();
        if (current().type == type) {
            consume();
        } else {
            return null;
        }
        return t;
    }

    void next() {
        pos++;
    }

    Token peek() {
        if (pos + 1 >= tokens.size()) {
            return Token.EOF;
        }

        return tokens.get(pos + 1);
    }

    List<Token> consumeTo(Token.TokenType type) {
        return consumeTo(type, BraceManager.leftToRight);
    }

    List<Token> consumeTo(Token.TokenType type, BraceManager.BraceProfile braces) {
        List<Token> tokens = new ArrayList<>();
        BraceManager braceManager = new BraceManager(braces);
        while (true) {
            if (current().type == Textless.EOF) {
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
    List<Token> selectTo(Token.TokenType type) {
        List<Token> selected = new ArrayList<>();
        int loc = pos;
        while (loc < tokens.size() && tokens.get(loc).type != type) {
            selected.add(tokens.get(loc));
            loc++;
        }
        return selected;
    }

    // Check if one token comes before another
    boolean containsBefore(Token.TokenType what, Token.TokenType before) {
        for (int lpos = pos; lpos < tokens.size(); lpos++) {
            if (tokens.get(lpos).type == what) {
                return true;
            }
            if (tokens.get(lpos).type == before) {
                return false;
            }
        }
        return before == Textless.EOF;
    }

    public List<AST.Statement> parseBlock() {
        return blockParser.parseBlock();
    }

    Statement parseExpression() {
        return expressionParser.parseExpression();
    }

    boolean isInMatch() {
        return inMatch;
    }

    CommonParsing getCommon() {
        return common;
    }
}


