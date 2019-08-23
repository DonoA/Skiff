package io.dallen.parser;

import static io.dallen.AST.*;
import static org.junit.Assert.*;

import io.dallen.ASTUtil;
import io.dallen.tokenizer.Token;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.util.List;

public class BasicExpressionTest {

//    @Rule
//    public Timeout globalTimeout = Timeout.seconds(1);

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
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "Int", 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new Declare(ASTUtil.simpleType("Int"), "x").toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseAssign() {
        // x = 5;
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.EQUAL, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "5", 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected =  new Assign(new Variable("x"), new NumberLiteral(5.0)).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseDeclareAssign() {
        // x: String = "Hello World";
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "String", 0),
                new Token(Token.Symbol.EQUAL, 0),
                new Token(Token.Textless.STRING_LITERAL, "Hello World", 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new DeclareAssign(
                ASTUtil.simpleType("String"),
                "x",
                new StringLiteral("Hello World")
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseFunctionCall() {
        // println(x, "Hello", 5);
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "println", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.COMMA, 0),
                new Token(Token.Textless.STRING_LITERAL, "Hello", 0),
                new Token(Token.Symbol.COMMA, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "5", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new FunctionCall(
                "println",
                List.of(new Variable("x"), new StringLiteral("Hello"), new NumberLiteral(5.0)),
                List.of()
        ).toFlatString();
//                "FunctionCall(" +
//                "name = println, " +
//                "args = [Variable(name = x), StringLiteral(value = \"Hello\"), NumberLiteral(value = 5.0) ], " +
//                "genericTypes = [ ])";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseNew() {
        // new MyClass(x, "Hello", 5)
        List<Token> tokens = List.of(
                new Token(Token.Keyword.NEW, 0),
                new Token(Token.Textless.NAME, "MyClass", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.COMMA, 0),
                new Token(Token.Textless.STRING_LITERAL, "Hello", 0),
                new Token(Token.Symbol.COMMA, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "5", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new New(
                ASTUtil.simpleType("MyClass"),
                List.of(new Variable("x"), new StringLiteral("Hello"), new NumberLiteral(5.0))
            ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseReturn() {
        // return x;
        List<Token> tokens = List.of(
                new Token(Token.Keyword.RETURN, 0),
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new Return(new Variable("x")).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseStringLiteral() {
        // "Hello"
        List<Token> tokens = List.of(
                new Token(Token.Textless.STRING_LITERAL, "Hello", 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new StringLiteral("Hello").toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseSequenceLiteral() {
        // 'Simple Sequence'
        List<Token> tokens = List.of(
                new Token(Token.Textless.SEQUENCE_LITERAL, "Simple Sequence", 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new SequenceLiteral("Simple Sequence").toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseRegexLiteral() {
        // r/$Regex[Pat-trn]^/gi
        List<Token> tokens = List.of(
                new Token(Token.Textless.REGEX_LITERAL, "$Regex[Pat-trn]^\0gi", 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new RegexLiteral("$Regex[Pat-trn]^", "gi").toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseNumberLiteral() {
        // 3.141592
        List<Token> tokens = List.of(
                new Token(Token.Textless.NUMBER_LITERAL, "3.141592", 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new NumberLiteral(3.141592).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseBooleanCombine() {
        // true && false || false
        List<Token> tokens = List.of(
                new Token(Token.Keyword.TRUE, 0),
                new Token(Token.Symbol.DOUBLE_AND, 0),
                new Token(Token.Keyword.FALSE, 0),
                new Token(Token.Symbol.DOUBLE_OR, 0),
                new Token(Token.Keyword.FALSE, 0),
                new Token(Token.Textless.EOF, 0));

        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new BoolCombine(
                new BooleanLiteral(true), BoolOp.AND, new BoolCombine(
                        new BooleanLiteral(false), BoolOp.OR, new BooleanLiteral(false)
            )
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseCompare() {
        // x != 1 && x == 1 && x <= 1 && < 1 && x > 1 && x >= 1
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.BANG_EQUAL, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "1", 0),
                new Token(Token.Symbol.DOUBLE_AND, 0),
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.DOUBLE_EQUAL, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "1", 0),
                new Token(Token.Symbol.DOUBLE_AND, 0),
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.LEFT_ANGLE_EQUAL, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "1", 0),
                new Token(Token.Symbol.DOUBLE_AND, 0),
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.LEFT_ANGLE, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "1", 0),
                new Token(Token.Symbol.DOUBLE_AND, 0),
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.RIGHT_ANGLE, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "1", 0),
                new Token(Token.Symbol.DOUBLE_AND, 0),
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.RIGHT_ANGLE_EQUAL, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "1", 0),
                new Token(Token.Textless.EOF, 0));

        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "BoolCombine(left = Variable(name = x), op = AND, " +
                "right = BoolCombine(left = Compare(left = Variable(name = x), op = EQ, " +
                "right = NumberLiteral(value = 1.0)), op = AND, " +
                "right = BoolCombine(left = Variable(name = x), op = AND, " +
                "right = BoolCombine(left = Compare(left = Variable(name = x), op = LT, " +
                "right = NumberLiteral(value = 1.0)), op = AND, " +
                "right = BoolCombine(left = Compare(left = Variable(name = x), op = GT, " +
                "right = NumberLiteral(value = 1.0)), op = AND, " +
                "right = Variable(name = x))))))";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseMath() {
        // 15 - y + x * 15 / 12
        // Parens: (15 - y) + ((x * 15) / 12)
        List<Token> tokens = List.of(
                new Token(Token.Textless.NUMBER_LITERAL, "15", 0),
                new Token(Token.Symbol.MINUS, 0),
                new Token(Token.Textless.NAME, "y", 0),
                new Token(Token.Symbol.PLUS, 0),
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.STAR, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "15", 0),
                new Token(Token.Symbol.SLASH, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "12", 0),
                new Token(Token.Textless.EOF, 0));
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
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.PLUS_EQUAL, 0),
                new Token(Token.Textless.NAME, "y", 0),
                new Token(Token.Symbol.MINUS_EQUAL, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "5", 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "MathAssign(left = Variable(name = x), op = PLUS, " +
                "right = MathAssign(left = Variable(name = y), op = MINUS, right = NumberLiteral(value = 5.0)))";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseInc() {
        // x++
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.DOUBLE_PLUS, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new MathSelfMod(new Variable("x"), MathOp.PLUS, SelfModTime.POST).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseDec() {
        // --y
        List<Token> tokens = List.of(
                new Token(Token.Symbol.DOUBLE_MINUS, 0),
                new Token(Token.Textless.NAME, "y", 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new MathSelfMod(
                new Variable("y"), MathOp.MINUS, SelfModTime.PRE
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseNewGenericClass() {
        // new MyClass<String, Int>(15);
        List<Token> tokens = List.of(
                new Token(Token.Keyword.NEW, 0),
                new Token(Token.Textless.NAME, "MyClass", Token.IdentifierType.TYPE, 0),
                new Token(Token.Symbol.LEFT_ANGLE, 0),
                new Token(Token.Textless.NAME, "String", Token.IdentifierType.TYPE, 0),
                new Token(Token.Symbol.COMMA, 0),
                new Token(Token.Textless.NAME, "Int", Token.IdentifierType.TYPE, 0),
                new Token(Token.Symbol.RIGHT_ANGLE, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "15", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new New(
                new Type(new Variable("MyClass"), 0, List.of(
                        ASTUtil.simpleType("String"),
                        ASTUtil.simpleType("Int")
                )),
                List.of(
                        new NumberLiteral(15.0)
                )
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseGenericFunctionCall() {
        // println<String, Int>(15);
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "println", 0),
                new Token(Token.Symbol.LEFT_ANGLE, 0),
                new Token(Token.Textless.NAME, "String", Token.IdentifierType.TYPE, 0),
                new Token(Token.Symbol.COMMA, 0),
                new Token(Token.Textless.NAME, "Int", Token.IdentifierType.TYPE, 0),
                new Token(Token.Symbol.RIGHT_ANGLE, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "15", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new FunctionCall(
                "println",
                List.of(new NumberLiteral(15.0)),
                List.of(ASTUtil.simpleType("String"), ASTUtil.simpleType("Int"))
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseListIndex() {
        // x[y[z]]
        List<Token> tokens = List.of(
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.LEFT_BRACKET, 0),
                new Token(Token.Textless.NAME, "y", 0),
                new Token(Token.Symbol.LEFT_BRACKET, 0),
                new Token(Token.Textless.NAME, "z", 0),
                new Token(Token.Symbol.RIGHT_BRACKET, 0),
                new Token(Token.Symbol.RIGHT_BRACKET, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new Subscript(
                new Variable("x"), new Subscript(new Variable("y"), new Variable("z"))
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    @org.junit.Test
    public void parseImport() {
        // import <myPackage>
        List<Token> tokens = List.of(
                new Token(Token.Keyword.IMPORT, 0),
                new Token(Token.Symbol.LEFT_ANGLE, 0),
                new Token(Token.Textless.NAME, "myPackage", 0),
                new Token(Token.Symbol.RIGHT_ANGLE, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new ImportStatement(ImportType.SYSTEM, "myPackage").toFlatString();

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
                new Token(Token.Keyword.THROW, 0),
                new Token(Token.Textless.NAME, "myExistingError", 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new ThrowStatement(new Variable("myExistingError")).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }
}
