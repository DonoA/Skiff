package io.dallen.tokenizer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String data;
    private int pos = 0;

    public Lexer(String data) {
        this.data = data;
    }

    public List<Token> lex() {
        ArrayList<Token> tokens = new ArrayList<>();
        while (pos < data.length()) {
            // Skip spaces, new lines, tables, etc
            if (isIgnored(data.charAt(pos))) {
                pos++;
                continue;
            }

            // Select longest token that matches the stream so "<=" will be selected over "<"
            Token.TokenType bestTokenType = selectBestToken();
            if (bestTokenType != Token.Textless.EOF) {
                pos += bestTokenType.getText().length();
                tokens.add(new Token(bestTokenType));
                continue;
            }

            // We didn't find any token types that matched, select a literal.
            // This has data of what the literal is attached to it
            Token selected = selectLiteral();

            // If we didn't match any literals
            if (selected == null) {
                throw new RuntimeException("Unknown token: " + data.charAt(pos));
            }
            tokens.add(selected);
        }
        tokens.add(new Token(Token.Textless.EOF));
        return tokens;
    }

    private Token.TokenType selectBestToken() {
        int searchPos = pos;
        Token.TokenType longestType = Token.Textless.EOF;

        // Search symbol tokens
        for (Token.TokenType testType : Token.Symbol.values()) {
            if(tokenNotValid(testType, searchPos)) {
                continue;
            }

            if (longestType.getText().length() < testType.getText().length()) {
                longestType = testType;
            }
        }

        // Search keyword tokens
        for (Token.TokenType testType : Token.Keyword.values()) {
            if(tokenNotValid(testType, searchPos)) {
                continue;
            }

            int nextPos = searchPos + testType.getText().length();
            if (nextPos < data.length() && isValidKeywordChar(data.charAt(nextPos))) {
                continue;
            }

            if (longestType.getText().length() < testType.getText().length()) {
                longestType = testType;
            }
        }

        return longestType;
    }

    private Token selectLiteral() {
        char c = data.charAt(pos);
        // Select string
        if (c == '"') {
            return new Token(Token.Textless.STRING_LITERAL, selectTo('"'));
        }

        if (c == '\'') {
            return new Token(Token.Textless.SEQUENCE_LITERAL, selectTo('\''));
        }

        if (c == 'r' && data.length() > pos + 1 && data.charAt(pos + 1) == '/') {
            pos++;
            StringBuilder regex = new StringBuilder();
            regex.append(selectTo('/')).append('\0');
            while(pos < data.length() && isRegexFlag(data.charAt(pos))) {
                regex.append(data.charAt(pos));
                pos++;
            }
            return new Token(Token.Textless.REGEX_LITERAL, regex.toString());
        }

        // Select number
        if (Character.isDigit(c)) {
            StringBuilder sb = new StringBuilder();
            // Select digits and dots until we reach something that isn't one of those
            while (pos < data.length() &&
                    (Character.isDigit(data.charAt(pos)) ||
                            data.charAt(pos) == '.')) {
                sb.append(data.charAt(pos));
                pos++;
            }
            return new Token(Token.Textless.NUMBER_LITERAL, sb.toString());
        }

        // Select name tokens, we have no idea what these are, they are likely bound to something
        // like a class/function/variable
        StringBuilder sb = new StringBuilder();
        while (pos < data.length() && isValidNameChar(data.charAt(pos))) {
            sb.append(data.charAt(pos));
            pos++;
        }
        Token name = new Token(Token.Textless.NAME, sb.toString());

        // if we selected nothing, that isn't a valid name token
        if (name.literal.isEmpty()) {
            name = null;
        }

        return name;
    }

    private String selectTo(char c) {
        StringBuilder sb = new StringBuilder();
        pos++;
        while (true) {
            if(pos >= data.length()) {
                throw new RuntimeException("Failed to find " + c + " in lexing");
            }

            // check for escaped chars
            if(pos > 0 && data.charAt(pos - 1) != '\\' && data.charAt(pos) == c) {
                break;
            }

            sb.append(data.charAt(pos));
            pos++;
        }
        pos++;
        return sb.toString();
    }

    private boolean tokenNotValid(Token.TokenType testType, int searchPos) {
        if (searchPos + testType.getText().length() > data.length()) {
            return true;
        }
        String streamSeg = data.substring(searchPos, searchPos + testType.getText().length());
        return !testType.getText().equals(streamSeg);
    }

    private boolean isRegexFlag(char c) {
        return Character.isAlphabetic(c);
    }

    private boolean isValidKeywordChar(char c) {
        return Character.isAlphabetic(c);
    }

    private boolean isValidNameChar(char c) {
        return Character.isAlphabetic(c) || c == '_' || Character.isDigit(c);
    }

    private boolean isIgnored(char c) {
        return c == ' ' || c == '\n' || c == '\t';
    }

}
