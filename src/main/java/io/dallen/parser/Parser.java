package io.dallen.parser;

import io.dallen.AST;
import io.dallen.errors.ErrorCollector;
import io.dallen.errors.ErrorPrinter;
import io.dallen.tokenizer.Token;
import io.dallen.AST.*;

import io.dallen.tokenizer.Token.Textless;
import java.util.ArrayList;
import java.util.List;

public class Parser implements ErrorCollector {

    public final static AST.Type VOID = new Type(
            new Variable(
                    "Void",
                    List.of(new Token(Token.Textless.NAME, "Void", 0))
            ),
            List.of());

    private List<Token> tokens;

    int pos;

    private final boolean inMatch;

    private final BlockParser blockParser;
    private final ExpressionParser expressionParser;
    private final CommonParsing common;
    private final Parser parent;
    private final List<String> errorMsg;
    private final String code;

    public Parser(List<Token> tokens) {
        this(tokens, null, false, null);
    }

    public Parser(List<Token> tokens, String code) {
        this(tokens, null, false, code);
    }

    public Parser(List<Token> tokens, Parser parent) {
        this(tokens, parent, false, null);
    }

    Parser(List<Token> tokens, Parser parent, boolean inMatch) {
        this(tokens, parent, inMatch, null);
    }

    Parser(List<Token> tokens, Parser parent, boolean inMatch, String code) {
        this.tokens = tokens;
        this.inMatch = inMatch;
        this.parent = parent;

        if(this.parent == null) {
            this.errorMsg = new ArrayList<>();
            this.code = code;
        } else {
            this.errorMsg = null;
            this.code = null;
        }

        this.blockParser = new BlockParser(this);
        this.expressionParser = new ExpressionParser(this);
        this.common = new CommonParsing(this);
    }

    public void throwError(String msg, Token on) {
        if (parent == null) {
            errorMsg.add(ErrorPrinter.pointToPos(code, on.pos, msg));
        } else {
            parent.throwError(msg, on);
        }
    }

    public List<String> getErrors() {
        return errorMsg;
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
            throwError("Parse error Expected: " + type.toString(), t);
            // TODO: make sure parser can recover from this
            return null;
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
                // TODO: allow parser to recover from this
                throwError("Parse error", current());
                return null;
            }
            if (current().type == type && braceManager.isEmpty()) {
                break;
            }
            try {
                braceManager.check(current());
            } catch(ParserError ex) {
                // TODO: allow parser to recover from this
                throwError(ex.msg, ex.on);
                return null;
            }
            tokens.add(current());
            next();
        }
        next();
        return tokens;
    }

    // Just selects the tokens, does not advance the current location
    List<Token> selectTo(List<Token.TokenType> types, boolean includeStop) {
        List<Token> tkns = new ArrayList<>();
        int loc = pos;
        BraceManager braceManager = new BraceManager(BraceManager.leftToRight);
        while (loc < tokens.size()) {
            if (tokens.get(loc).type == Textless.EOF) {
                if (braceManager.isEmpty()) {
                    break;
                }
                // TODO: allow parser to recover from this
                throwError("Parse error", tokens.get(loc));
                return null;
            }
            if (types.contains(tokens.get(loc).type) && braceManager.isEmpty()) {
                if(includeStop) {
                    tkns.add(tokens.get(loc));
                }
                break;
            }
            try {
                braceManager.check(tokens.get(loc));
            } catch(ParserError ex) {
                // TODO: allow parser to recover from this
                throwError(ex.msg, ex.on);
                return null;
            }
            tkns.add(tokens.get(loc));
            loc++;
        }
        return tkns;
    }

    List<Token> selectTo(Token.TokenType typ) {
        return selectTo(List.of(typ), false);
    }

    // Just selects the tokens, does not advance the current location
    List<Token> selectToEOF() {
        return selectTo(List.of(Textless.EOF, Token.Symbol.SEMICOLON), false);
    }

    List<Token> selectToBlockEnd() {
        return selectTo(List.of(Textless.EOF, Token.Symbol.RIGHT_BRACE), true);
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


