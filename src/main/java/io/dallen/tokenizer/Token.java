package io.dallen.tokenizer;

import java.util.Objects;

/**
 * A single quantum of information for consumption by the parser
 */
public class Token {

    public interface TokenType {
        String getText();
        String getName();
    }

    public enum Symbol implements TokenType {

        DOT("."),
        ARROW("=>"),
        COLON(":"),

        LEFT_PAREN("("),
        RIGHT_PAREN(")"),
        COMMA(","),

        BANG("!"),
        EQUAL("="),
        BANG_EQUAL("!="),
        DOUBLE_EQUAL("=="),
        LEFT_ANGLE("<"),
        LEFT_ANGLE_EQUAL("<="),
        RIGHT_ANGLE(">"),
        RIGHT_ANGLE_EQUAL(">="),

        SEMICOLON(";"),
        LEFT_BRACE("{"),
        RIGHT_BRACE("}"),
        LEFT_BRACKET("["),
        RIGHT_BRACKET("]"),
        STAR("*"),
        PLUS("+"),
        PLUS_EQUAL("+="),
        MINUS("-"),
        MINUS_EQUAL("-="),
        SLASH("/"),
        PERCENT("%"),
        DOUBLE_STAR("**"),
        DOUBLE_PLUS("++"),
        DOUBLE_MINUS("--"),
        DOUBLE_AND("&&"),
        DOUBLE_OR("||"),

        UNDERSCORE("_");

        private final String text;

        Symbol(String text) {
            this.text = text;
        }

        @Override
        public String getText() {
            return this.text;
        }

        @Override
        public String getName() {
            return super.name();
        }
    }

    public enum Keyword implements TokenType {

        IF("if"),
        ELSE("else"),
        WHILE("while"),
        LOOP("loop"),
        FOR("for"),
        SWITCH("switch"),
        MATCH("match"),
        CASE("case"),
        RETURN("return"),
        DEF("def"),
        NATIVE("native"),
        IMPORT("import"),
        CLASS("class"),
        STRUCT("struct"),
        STATIC("static"),
        PRIVATE("private"),
        TRUE("true"),
        FALSE("false"),
        NEXT("next"),
        BREAK("break"),
        TRY("try"),
        CATCH("catch"),
        THROW("throw"),
        NEW("new");

        private final String text;

        Keyword(String text) {
            this.text = text;
        }


        @Override
        public String getText() {
            return this.text;
        }

        @Override
        public String getName() {
            return super.name();
        }
    }

    public enum Textless implements TokenType {
        NAME,
        STRING_LITERAL,
        SEQUENCE_LITERAL,
        NUMBER_LITERAL,
        REGEX_LITERAL,
        EOF;

        @Override
        public String getText() {
            return "";
        }

        @Override
        public String getName() {
            return super.name();
        }
    }

    public enum IdentifierType {
        TYPE, VARIABLE
    }

    /**
     * The type of information segment this token represents
     */
    public final TokenType type;

    /**
     * The string value of the token before wrapping
     */
    public final String literal;

    /**
     * The type of identifier this token represents. Used to determine classes from other variable types
     */
    public final IdentifierType ident;

    /**
     * The location where this token was found so it can be pointed out later in error messages
     */
    public final int pos;

    public static final Token EOF = new Token(Textless.EOF, 0);

    public Token(TokenType type, int pos) {
        this(type, "", pos);
    }

    public Token(TokenType type, String lit, int pos) {
        this(type, lit, null, pos);
    }

    public Token(TokenType type, String lit, IdentifierType ident, int pos) {
        this.type = type;
        this.literal = lit;
        this.ident = ident;
        this.pos = pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;
        Token token = (Token) o;
        return type.equals(token.type) &&
                literal.equals(token.literal) &&
                ident == token.ident;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, literal, ident);
    }

    @Override
    public String toString() {
        String str = type.getName() + "(" + literal + ")";
        if(ident != null) {
            str += "(" + ident + ")";
        }
        return str;
    }

    public boolean isEOF() {
        return this.type == Textless.EOF;
    }

}
