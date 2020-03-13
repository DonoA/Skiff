package io.dallen.parser;

import io.dallen.ast.AST;
import io.dallen.errors.ErrorCollector;
import io.dallen.errors.ErrorPrinter;
import io.dallen.tokenizer.Token;
import io.dallen.tokenizer.Token.Textless;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a token stream into an AST
 */
public class Parser implements ErrorCollector<Token> {

    List<Token> tokens;
    int start;
    int pos;
    int stop;
    Parser parent;
    boolean inMatch = false;
    final List<String> errorMsg = new ArrayList<>();
    final String code;

    public Parser(String code, List<Token> tokens) {
        this.tokens = tokens;
        start = 0;
        stop = tokens.size() - 1;
        parent = null;
        this.code = code;
    }

    public Parser(Parser parent, int start, int stop) {
        this.start = start;
        this.stop = stop;
        this.parent = parent;
        this.tokens = parent.tokens;
        this.pos = 0;
        this.code = parent.code;
    }

    public Parser subParserTo(int count) {
        int startPos = absolutePos();
        this.pos += count;
        return new Parser(this, startPos, Math.min(startPos + count, absoluteStop()));
    }

    public Parser subParserTo(Token.TokenType endToken) {
        return subParserTo(endToken, BraceManager.leftToRight);
    }

    public Parser subParserTo(Token.TokenType end_token, BraceManager.BraceProfile braces) {
        int start = pos;
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
            if (current().type == end_token && braceManager.isEmpty()) {
                break;
            }
            try {
                braceManager.check(current());
            } catch (ParserError ex) {
                // TODO: allow parser to recover from this
                throwError(ex.msg, ex.on);
                return null;
            }

            pos++;
        }

        pos++;
        return new Parser(this, this.start + start, this.start + pos - 1);
    }

    public int absoluteStart() {
        return start;
    }

    public int absoluteStop() {
        return stop;
    }

    public int absolutePos() {
        return start + pos;
    }

    public int tokenCount() {
        return stop - absolutePos();
    }

    public Token get(int i) {
        int offset = absolutePos() + i;
        if (offset >= stop) {
            int best_guess_pos = tokens.get(stop).pos;
            return Token.EOF(best_guess_pos);
        }
        return this.tokens.get(start + pos + i);
    }

    public Token current() {
        return get(0);
    }

    public Token consumeExpected(Token.TokenType typ) {
        Token rtn = get(0);
        if (rtn.type != typ) {
            throwError("Expected token " + typ.getName() + " got " + rtn.type.getName(), rtn);
//            throw new ParserError.NoCatchParseError("Expected token " + typ.getName() + " got " + rtn.type.getName(), rtn);
        }
        pos++;
        return rtn;
    }

    public Parser subParserParens() {
        consumeExpected(Token.Symbol.LEFT_PAREN);
        Parser subParser = subParserTo(Token.Symbol.RIGHT_PAREN);
        return subParser;
    }

    public AST.Statement parseStatement() {
        return BlockParser.parseBlock(this);
    }

    public AST.Statement parseExpression() {
        return ExpressionParser.parseExpression(this);
    }

    public AST.Statement parse() {
        int startPos = absolutePos();
        List<AST.Statement> body = new ArrayList<>();

        while (true) {
            AST.Statement stmt = parseStatement();

            if (stmt == null) {
                break;
            }

            body.add(stmt);
        }

        return new AST.Program(body, tokens, startPos, absolutePos());
    }

    public int indexOf(Token.TokenType type) {
        for (int i = 0; get(i).type != Textless.EOF; i++) {
            if (get(i).type == type) {
                return i;
            }
        }
        return -1;
    }

    public Token getBack(int i) {
        return tokens.get(absoluteStop() - i);
    }

    public void throwError(String msg, Token on) {
        if (parent == null) {
            String pointer = ErrorPrinter.pointToPos(code, on.pos, msg);
            errorMsg.add(pointer);
        } else {
            parent.throwError(msg, on);
        }
    }

    public List<String> getErrors() {
        return errorMsg;
    }

    // Check if one token comes before another
    boolean containsBefore(Token.TokenType what, Token.TokenType before) {
        for (int lpos = absolutePos(); lpos < absoluteStop(); lpos++) {
            if (tokens.get(lpos).type == what) {
                return true;
            }
            if (tokens.get(lpos).type == before) {
                return false;
            }
        }
        return false;
    }

    boolean isInMatch() {
        return inMatch;
    }

    Parser inMatch() {
        inMatch = true;
        return this;
    }
}


