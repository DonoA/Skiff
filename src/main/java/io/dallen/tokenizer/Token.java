package io.dallen.tokenizer;

public class Token {

    public interface TokenType {
        String getText();
        String getName();
    }

    public enum Symbol implements TokenType {

        DOT("."),
        ARROW("->"),
        COLON(":"),

        LEFT_PAREN("("),
        RIGHT_PAREN(")"),
        COMMA(","),

        EQUAL("="),
        DOUBLE_EQUAL("=="),
        LEFT_ANGLE("<"),
        RIGHT_ANGLE(">"),

        SEMICOLON(";"),
        LEFT_BRACE("{"),
        RIGHT_BRACE("}"),
        LEFT_BRACKET("["),
        RIGHT_BRACKET("]"),
        STAR("*"),
        PLUS("+"),
        MINUS("-"),
        SLASH("/"),
        BOOL_AND("&&"),
        BOOL_OR("||");

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
        FOR("for"),
        RETURN("return"),
        DEF("def"),
        CLASS("class"),
        STATIC("static"),
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
}
