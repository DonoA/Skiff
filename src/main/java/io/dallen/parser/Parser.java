package io.dallen.parser;

import io.dallen.ast.AST;
import io.dallen.errors.ErrorCollector;
import io.dallen.errors.ErrorPrinter;
import io.dallen.tokenizer.Token;
import io.dallen.ast.AST.*;

import io.dallen.tokenizer.Token.Textless;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a token stream into an AST
 */
public class Parser implements ErrorCollector<Token> {

    private List<Token> tokens;

    int pos;

    private final boolean inMatch;

    private final BlockParser blockParser;
    private final ExpressionParser expressionParser;
    private final CommonParsing common;
    private final Parser parent;
    private final List<String> errorMsg;
    private final String code;

    /**
     * A simple parser, no error handling provided
     * @param tokens to token stream to parse
     */
    public Parser(List<Token> tokens) {
        this(tokens, null, false, null);
    }

    /**
     * A parser with the code being parsed for error pointing, generally a top level parser
     * @param tokens to token stream to parse
     * @param code The code string used to generate the tokens
     */
    public Parser(List<Token> tokens, String code) {
        this(tokens, null, false, code);
    }

    /**
     * Creates a subparser, passes thrown errors to parent
     * @param tokens to token stream to parse
     * @param parent parent parser for errors to be passed to
     */
    public Parser(List<Token> tokens, Parser parent) {
        this(tokens, parent, false, null);
    }

    /**
     * Subparser in match block, treats case statements differently
     * @param tokens to token stream to parse
     * @param parent parent parser for errors to be passed to
     * @param inMatch define if parser is in match block
     */
    Parser(List<Token> tokens, Parser parent, boolean inMatch) {
        this(tokens, parent, inMatch, null);
    }

    private Parser(List<Token> tokens, Parser parent, boolean inMatch, String code) {
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

    Token tryConsumeOrEOF(Token.TokenType type) {
        Token t = current();
        if (current().type == type || current().type == Textless.EOF) {
            consume();
        } else {
            throwError("Unexpected token", current());
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

    // Consume tokens up to the provided type, will not end inside brace block, uses left to right braces
    List<Token> consumeTo(Token.TokenType type) {
        return consumeTo(type, BraceManager.leftToRight);
    }

    // Consume tokens up to the provided type, will not end inside brace block
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
    List<Token> selectTo(List<Token.TokenType> types, boolean includeEnd) {
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
        if(includeEnd && loc < tokens.size()) {
            tkns.add(tokens.get(loc));
        }
        return tkns;
    }

    List<Token> selectTo(Token.TokenType typ) {
        return selectTo(List.of(typ), false);
    }

    List<Token> selectToWithEnd(Token.TokenType typ) {
        return selectTo(List.of(typ), true);
    }

    // Just selects the tokens, does not advance the current location
    List<Token> selectToEOF() {
        return selectTo(List.of(Textless.EOF, Token.Symbol.SEMICOLON), false);
    }

    // Select tokens up to the next close brace. Brace aware using left to right braces. Ideal for selecting
    // blocks for if, for, while, class decs, etc.
    List<Token> selectToBlockEnd() {
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

            if ((tokens.get(loc).type == Textless.EOF || tokens.get(loc).type == Token.Symbol.RIGHT_BRACE) &&
                    (braceManager.isEmpty() || braceManager.stackDepth() == 1)) {
                tkns.add(tokens.get(loc));
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

    public List<AST.Statement> parseAll() {
        return blockParser.parseAll();
    }

    public AST.Statement parseBlock() {
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


