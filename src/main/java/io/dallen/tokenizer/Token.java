package io.dallen.tokenizer;

import java.util.Objects;

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

        EQUAL("="),
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


    public final TokenType type;
    public final String literal;

    public static final Token EOF = new Token(Textless.EOF);

    public Token(TokenType type) {
        this(type, "");
    }

    public Token(TokenType type, String lit) {
        this.type = type;
        this.literal = lit;
    }

    @Override
    public String toString() {
        return type.getName() + "(" + literal + ")";
    }

    public boolean isEOF() {
        return this.type == Textless.EOF;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;
        Token token = (Token) o;
        return Objects.equals(type, token.type) &&
                Objects.equals(literal, token.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, literal);
    }

}
