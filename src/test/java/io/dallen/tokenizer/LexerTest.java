package io.dallen.tokenizer;

import org.hamcrest.CoreMatchers;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LexerTest {
    @org.junit.Test
    public void lexSimpleSymbols() {
        String symbols = ": = , ( ) { } ; == < > <= >= [ ] * + - / % ** && || += -= ++ -- => _";

        List<Token> tokens = new Lexer(symbols).lex();
        List<Token> expected = new ArrayList<>(Arrays.asList(
                new Token(Token.Symbol.COLON),
                new Token(Token.Symbol.EQUAL),
                new Token(Token.Symbol.COMMA),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.DOUBLE_EQUAL),
                new Token(Token.Symbol.LEFT_ANGLE),
                new Token(Token.Symbol.RIGHT_ANGLE),
                new Token(Token.Symbol.LEFT_ANGLE_EQUAL),
                new Token(Token.Symbol.RIGHT_ANGLE_EQUAL),
                new Token(Token.Symbol.LEFT_BRACKET),
                new Token(Token.Symbol.RIGHT_BRACKET),
                new Token(Token.Symbol.STAR),
                new Token(Token.Symbol.PLUS),
                new Token(Token.Symbol.MINUS),
                new Token(Token.Symbol.SLASH),
                new Token(Token.Symbol.PERCENT),
                new Token(Token.Symbol.DOUBLE_STAR),
                new Token(Token.Symbol.DOUBLE_AND),
                new Token(Token.Symbol.DOUBLE_OR),
                new Token(Token.Symbol.PLUS_EQUAL),
                new Token(Token.Symbol.MINUS_EQUAL),
                new Token(Token.Symbol.DOUBLE_PLUS),
                new Token(Token.Symbol.DOUBLE_MINUS),
                new Token(Token.Symbol.ARROW),
                new Token(Token.Symbol.UNDERSCORE),
                new Token(Token.Textless.EOF)
        ));
        assertThat(tokens, is(expected));
    }

    @org.junit.Test
    public void lexKeywords() {
        String symbols = "if else while for return def class struct private static new switch match import loop case " +
                "true false next break try catch throw";

        List<Token> tokens = new Lexer(symbols).lex();
        List<Token> expected = new ArrayList<>(Arrays.asList(
                new Token(Token.Keyword.IF),
                new Token(Token.Keyword.ELSE),
                new Token(Token.Keyword.WHILE),
                new Token(Token.Keyword.FOR),
                new Token(Token.Keyword.RETURN),
                new Token(Token.Keyword.DEF),
                new Token(Token.Keyword.CLASS),
                new Token(Token.Keyword.STRUCT),
                new Token(Token.Keyword.PRIVATE),
                new Token(Token.Keyword.STATIC),
                new Token(Token.Keyword.NEW),
                new Token(Token.Keyword.SWITCH),
                new Token(Token.Keyword.MATCH),
                new Token(Token.Keyword.IMPORT),
                new Token(Token.Keyword.LOOP),
                new Token(Token.Keyword.CASE),
                new Token(Token.Keyword.TRUE),
                new Token(Token.Keyword.FALSE),
                new Token(Token.Keyword.NEXT),
                new Token(Token.Keyword.BREAK),
                new Token(Token.Keyword.TRY),
                new Token(Token.Keyword.CATCH),
                new Token(Token.Keyword.THROW),
                new Token(Token.Textless.EOF)
        ));
        assertThat(tokens, is(expected));
    }

    @org.junit.Test
    public void lexTextless() {
        String symbols = "varName var_name VARNAME \"Hello String\" 'Hello Sequence' varName25 varName_25 10 10.5";

        List<Token> tokens = new Lexer(symbols).lex();
        List<Token> expected = new ArrayList<>(Arrays.asList(
                new Token(Token.Textless.NAME, "varName"),
                new Token(Token.Textless.NAME, "var_name"),
                new Token(Token.Textless.NAME, "VARNAME"),
                new Token(Token.Textless.STRING_LITERAL, "Hello String"),
                new Token(Token.Textless.SEQUENCE_LITERAL, "Hello Sequence"),
                new Token(Token.Textless.NAME, "varName25"),
                new Token(Token.Textless.NAME, "varName_25"),
                new Token(Token.Textless.NUMBER_LITERAL, "10"),
                new Token(Token.Textless.NUMBER_LITERAL, "10.5"),
                new Token(Token.Textless.EOF)
        ));
        assertThat(tokens, is(expected));
    }
}
