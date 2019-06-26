package io.dallen.parser;

import static io.dallen.AST.*;
import static org.junit.Assert.*;

import io.dallen.tokenizer.Token;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.util.List;

public class BasicExpressions {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(1);

    /*

        List<Token> tokens = ;
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());

     */

    @org.junit.Test
    public void parseDeclare() {
        // x: Int;
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "Int"),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "Declare(type = Type(name = Variable(name = Int), arraySize = 0, genericTypes = [ ]), name = x)";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseAssign() {
        // x = 5;
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.EQUAL),
                new Token(Token.Textless.NUMBER_LITERAL, "5"),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "Assign(name = Variable(name = x), value = NumberLiteral(value = 5.0))";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseDeclareAssign() {
        // x: String = "Hello World";
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "String"),
                new Token(Token.Symbol.EQUAL),
                new Token(Token.Textless.STRING_LITERAL, "Hello World"),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseFunctionCall() {
        // println(x, "Hello", 5);
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "println"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.COMMA),
                new Token(Token.Textless.STRING_LITERAL, "Hello"),
                new Token(Token.Symbol.COMMA),
                new Token(Token.Textless.NUMBER_LITERAL, "5"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "FunctionCall(name = println, args = " +
                "[Variable(name = x), StringLiteral(value = \"Hello\"), NumberLiteral(value = 5.0) ])";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseNew() {
        // new MyClass(x, "Hello", 5)
        List<Token> tokens = List.of(
                new Token(Token.Keyword.NEW),
                new Token(Token.Textless.NAME, "MyClass"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.COMMA),
                new Token(Token.Textless.STRING_LITERAL, "Hello"),
                new Token(Token.Symbol.COMMA),
                new Token(Token.Textless.NUMBER_LITERAL, "5"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "New(type = Variable(name = MyClass), " +
                "argz = [Variable(name = x), StringLiteral(value = \"Hello\"), NumberLiteral(value = 5.0) ])";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseReturn() {
        // return x;
        List<Token> tokens = List.of(
                new Token(Token.Keyword.RETURN),
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "Return(value = Variable(name = x))";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseStringLiteral() {
        // "Hello"
        List<Token> tokens = List.of(
                new Token(Token.Textless.STRING_LITERAL, "Hello"),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseSequenceLiteral() {
        // 'Simple Sequence'
        List<Token> tokens = List.of(
                new Token(Token.Textless.SEQUENCE_LITERAL, "Simple Sequence"),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseRegexLiteral() {
        // r/$Regex[Pat-trn]^/gi
        List<Token> tokens = List.of(
                new Token(Token.Textless.SEQUENCE_LITERAL, "$Regex[Pat-trn]^\0gi"),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseNumberLiteral() {
        // 3.141592
        List<Token> tokens = List.of(
                new Token(Token.Textless.NUMBER_LITERAL, "3.141592"),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseBooleanCombine() {
        // true && false || false
        List<Token> tokens = List.of(
                new Token(Token.Keyword.TRUE),
                new Token(Token.Symbol.DOUBLE_AND),
                new Token(Token.Keyword.FALSE),
                new Token(Token.Symbol.DOUBLE_OR),
                new Token(Token.Keyword.FALSE),
                new Token(Token.Textless.EOF));

        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseCompare() {
        // x != 1 && x == 1 && x <= 1 && < 1 && x > 1 && x >= 1
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.BANG_EQUAL),
                new Token(Token.Textless.NUMBER_LITERAL, "1"),
                new Token(Token.Symbol.DOUBLE_AND),
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.DOUBLE_EQUAL),
                new Token(Token.Textless.NUMBER_LITERAL, "1"),
                new Token(Token.Symbol.DOUBLE_AND),
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.LEFT_ANGLE_EQUAL),
                new Token(Token.Textless.NUMBER_LITERAL, "1"),
                new Token(Token.Symbol.DOUBLE_AND),
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.LEFT_ANGLE),
                new Token(Token.Textless.NUMBER_LITERAL, "1"),
                new Token(Token.Symbol.DOUBLE_AND),
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.RIGHT_ANGLE),
                new Token(Token.Textless.NUMBER_LITERAL, "1"),
                new Token(Token.Symbol.DOUBLE_AND),
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.RIGHT_ANGLE_EQUAL),
                new Token(Token.Textless.NUMBER_LITERAL, "1"),
                new Token(Token.Textless.EOF));

        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseMath() {
        // 15 - y + x * 15 / 12
        // Parens: (15 - y) + ((x * 15) / 12)
        List<Token> tokens = List.of(
                new Token(Token.Textless.NUMBER_LITERAL, "15"),
                new Token(Token.Symbol.MINUS),
                new Token(Token.Textless.NAME, "y"),
                new Token(Token.Symbol.PLUS),
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.STAR),
                new Token(Token.Textless.NUMBER_LITERAL, "15"),
                new Token(Token.Symbol.SLASH),
                new Token(Token.Textless.NUMBER_LITERAL, "12"),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "MathStatement(" +
                "left = MathStatement(" +
                "left = NumberLiteral(value = 15.0), " +
                "op = MINUS, " +
                "right = MathStatement(" +
                "left = Variable(name = y), " +
                "op = PLUS, " +
                "right = Variable(name = x)" +
                ")), " +
                "op = MUL, " +
                "right = MathStatement(" +
                "left = NumberLiteral(value = 15.0), " +
                "op = DIV, " +
                "right = NumberLiteral(value = 12.0)))";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseMathAssign() {
        // x += y -= 5
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.PLUS_EQUAL),
                new Token(Token.Textless.NAME, "y"),
                new Token(Token.Symbol.MINUS_EQUAL),
                new Token(Token.Textless.NUMBER_LITERAL, "5"),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseInc() {
        // x++
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.DOUBLE_PLUS),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseDec() {
        // y--
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "y"),
                new Token(Token.Symbol.DOUBLE_MINUS),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseNewGenericClass() {
        // new MyClass<String, Int>(15);
        List<Token> tokens = List.of(
                new Token(Token.Keyword.NEW),
                new Token(Token.Textless.NAME, "MyClass"),
                new Token(Token.Symbol.LEFT_ANGLE),
                new Token(Token.Textless.NAME, "String"),
                new Token(Token.Symbol.COMMA),
                new Token(Token.Textless.NAME, "Int"),
                new Token(Token.Symbol.RIGHT_ANGLE),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NUMBER_LITERAL, "15"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseGenericFunctionCall() {
        // println<String, Int>(15);
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "println"),
                new Token(Token.Symbol.LEFT_ANGLE),
                new Token(Token.Textless.NAME, "String"),
                new Token(Token.Symbol.COMMA),
                new Token(Token.Textless.NAME, "Int"),
                new Token(Token.Symbol.RIGHT_ANGLE),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NUMBER_LITERAL, "15"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseListIndex() {
        // x[y[z]]
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.LEFT_BRACKET),
                new Token(Token.Textless.NAME, "y"),
                new Token(Token.Symbol.LEFT_BRACKET),
                new Token(Token.Textless.NAME, "z"),
                new Token(Token.Symbol.RIGHT_BRACKET),
                new Token(Token.Symbol.RIGHT_BRACKET),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseImport() {
        // import <myPackage>
        List<Token> tokens = List.of(
                new Token(Token.Keyword.IMPORT),
                new Token(Token.Symbol.LEFT_ANGLE),
                new Token(Token.Textless.NAME, "myPackage"),
                new Token(Token.Symbol.RIGHT_ANGLE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseDeconstruction() {
//        List<Token> tokens = ;
//        List<Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseThrow() {
        // throw myExistingError;
        List<Token> tokens = List.of(
                new Token(Token.Keyword.THROW),
                new Token(Token.Textless.NAME, "myExistingError"),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }
}
