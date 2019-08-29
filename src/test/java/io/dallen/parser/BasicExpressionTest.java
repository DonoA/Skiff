package io.dallen.parser;

import static io.dallen.ast.AST.*;
import static org.junit.Assert.*;

import io.dallen.ast.ASTEnums;
import io.dallen.ASTUtil;
import io.dallen.tokenizer.Token;

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

        String expected = new Declare(
                ASTUtil.simpleType("Int"),
                "x",
                List.of(
                    new Token(Token.Textless.NAME, "x", 0),
                    new Token(Token.Symbol.COLON, 0),
                    new Token(Token.Textless.NAME, "Int", 0)
                )
        ).toString();

        assertEquals(expected, statements.get(0).toString());
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

        String expected =  new Assign(
                new Variable(
                        "x",
                        List.of(new Token(Token.Textless.NAME, "x", 0))
                ),
                new NumberLiteral(5.0, List.of(new Token(Token.Textless.NUMBER_LITERAL, "5", 0))),
                List.of(
                        new Token(Token.Textless.NAME, "x", 0),
                        new Token(Token.Symbol.EQUAL, 0),
                        new Token(Token.Textless.NUMBER_LITERAL, "5", 0)
                )
        ).toFlatString();

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
                new StringLiteral("Hello World", List.of(
                        new Token(Token.Textless.STRING_LITERAL, "Hello World", 0)
                )),
                List.of(
                        new Token(Token.Textless.NAME, "x", 0),
                        new Token(Token.Symbol.COLON, 0),
                        new Token(Token.Textless.NAME, "String", 0),
                        new Token(Token.Symbol.EQUAL, 0),
                        new Token(Token.Textless.STRING_LITERAL, "Hello World", 0)
                )
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
                List.of(
                        new Variable(
                                "x",
                                List.of(new Token(Token.Textless.NAME, "x", 0))
                        ),
                        new StringLiteral(
                                "Hello",
                                List.of(new Token(Token.Textless.STRING_LITERAL, "Hello", 0))
                        ),
                        new NumberLiteral(
                                5.0,
                                List.of(new Token(Token.Textless.NUMBER_LITERAL, "5", 0))
                        )
                ),
                List.of(),
                List.of(
                        new Token(Token.Textless.NAME, "println", 0),
                        new Token(Token.Symbol.LEFT_PAREN, 0),
                        new Token(Token.Textless.NAME, "x", 0),
                        new Token(Token.Symbol.COMMA, 0),
                        new Token(Token.Textless.STRING_LITERAL, "Hello", 0),
                        new Token(Token.Symbol.COMMA, 0),
                        new Token(Token.Textless.NUMBER_LITERAL, "5", 0),
                        new Token(Token.Symbol.RIGHT_PAREN, 0)
                )
        ).toFlatString();

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
                List.of(
                        new Variable(
                                "x",
                                List.of(new Token(Token.Textless.NAME, "x", 0))
                        ),
                        new StringLiteral(
                                "Hello",
                                List.of(new Token(Token.Textless.STRING_LITERAL, "Hello", 0))
                        ),
                        new NumberLiteral(
                                5.0,
                                List.of(new Token(Token.Textless.NUMBER_LITERAL, "5", 0))
                        )
                ),
                List.of(
                        new Token(Token.Keyword.NEW, 0),
                        new Token(Token.Textless.NAME, "MyClass", 0),
                        new Token(Token.Symbol.LEFT_PAREN, 0),
                        new Token(Token.Textless.NAME, "x", 0),
                        new Token(Token.Symbol.COMMA, 0),
                        new Token(Token.Textless.STRING_LITERAL, "Hello", 0),
                        new Token(Token.Symbol.COMMA, 0),
                        new Token(Token.Textless.NUMBER_LITERAL, "5", 0),
                        new Token(Token.Symbol.RIGHT_PAREN, 0)
                )
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

        String expected = new Return(
                new Variable("x", List.of(new Token(Token.Textless.NAME, "x", 0))),
                List.of(
                        new Token(Token.Keyword.RETURN, 0),
                        new Token(Token.Textless.NAME, "x", 0)
                )).toFlatString();

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

        String expected = new StringLiteral(
                "Hello",
                List.of(new Token(Token.Textless.STRING_LITERAL, "Hello", 0))
        ).toFlatString();

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

        String expected = new SequenceLiteral("Simple Sequence", List.of(
                new Token(Token.Textless.SEQUENCE_LITERAL, "Simple Sequence", 0)
        )).toFlatString();

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

        String expected = new RegexLiteral("$Regex[Pat-trn]^", "gi", List.of(
                new Token(Token.Textless.REGEX_LITERAL, "$Regex[Pat-trn]^\0gi", 0)
        )).toFlatString();

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

        String expected = new NumberLiteral(3.141592, List.of(
                new Token(Token.Textless.NUMBER_LITERAL, "3.141592", 0)
        )).toFlatString();

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
                new BooleanLiteral(
                        true,
                        List.of(new Token(Token.Keyword.TRUE, 0))
                ),
                ASTEnums.BoolOp.AND,
                new BoolCombine(
                        new BooleanLiteral(
                                false,
                                List.of(new Token(Token.Keyword.FALSE, 0))
                        ),
                        ASTEnums.BoolOp.OR,
                        new BooleanLiteral(
                                false,
                                List.of(new Token(Token.Keyword.FALSE, 0))
                        ),
                        List.of(
                                new Token(Token.Keyword.FALSE, 0),
                                new Token(Token.Symbol.DOUBLE_OR, 0),
                                new Token(Token.Keyword.FALSE, 0)
                        )
                ), List.of(
                    new Token(Token.Keyword.TRUE, 0),
                    new Token(Token.Symbol.DOUBLE_AND, 0),
                    new Token(Token.Keyword.FALSE, 0),
                    new Token(Token.Symbol.DOUBLE_OR, 0),
                    new Token(Token.Keyword.FALSE, 0)
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

        String expected = new BoolCombine(
                new Compare(
                        ASTUtil.simpleVar("x"),
                        ASTEnums.CompareOp.NE,
                        ASTUtil.simpleNumLit(1d),
                        List.of(
                                new Token(Token.Textless.NAME, "x", 0),
                                new Token(Token.Symbol.BANG_EQUAL, 0),
                                new Token(Token.Textless.NUMBER_LITERAL, "1", 0)
                        )
                ),
                ASTEnums.BoolOp.AND,
                new BoolCombine(
                        new Compare(
                                ASTUtil.simpleVar("x"),
                                ASTEnums.CompareOp.EQ,
                                ASTUtil.simpleNumLit(1d),
                                List.of(
                                        new Token(Token.Textless.NAME, "x", 0),
                                        new Token(Token.Symbol.DOUBLE_EQUAL, 0),
                                        new Token(Token.Textless.NUMBER_LITERAL, "1", 0)
                                )
                        ),
                        ASTEnums.BoolOp.AND,
                        new BoolCombine(
                                new Compare(
                                        ASTUtil.simpleVar("x"),
                                        ASTEnums.CompareOp.LE,
                                        ASTUtil.simpleNumLit(1d),
                                        List.of(
                                                new Token(Token.Textless.NAME, "x", 0),
                                                new Token(Token.Symbol.LEFT_ANGLE_EQUAL, 0),
                                                new Token(Token.Textless.NUMBER_LITERAL, "1", 0)
                                        )
                                ),
                                ASTEnums.BoolOp.AND,
                                new BoolCombine(
                                        new Compare(
                                                ASTUtil.simpleVar("x"),
                                                ASTEnums.CompareOp.LT,
                                                ASTUtil.simpleNumLit(1d),
                                                List.of(
                                                        new Token(Token.Textless.NAME, "x", 0),
                                                        new Token(Token.Symbol.LEFT_ANGLE, 0),
                                                        new Token(Token.Textless.NUMBER_LITERAL, "1", 0)
                                                )
                                        ),
                                        ASTEnums.BoolOp.AND,
                                        new BoolCombine(
                                                new Compare(
                                                        ASTUtil.simpleVar("x"),
                                                        ASTEnums.CompareOp.GT,
                                                        ASTUtil.simpleNumLit(1d),
                                                        List.of(
                                                                new Token(Token.Textless.NAME, "x", 0),
                                                                new Token(Token.Symbol.RIGHT_ANGLE, 0),
                                                                new Token(Token.Textless.NUMBER_LITERAL, "1", 0)
                                                        )
                                                ),
                                                ASTEnums.BoolOp.AND,
                                                new Compare(
                                                        ASTUtil.simpleVar("x"),
                                                        ASTEnums.CompareOp.GE,
                                                        ASTUtil.simpleNumLit(1d),
                                                        List.of(
                                                                new Token(Token.Textless.NAME, "x", 0),
                                                                new Token(Token.Symbol.RIGHT_ANGLE_EQUAL, 0),
                                                                new Token(Token.Textless.NUMBER_LITERAL, "1", 0)
                                                        )
                                                ),
                                                List.of(
                                                        new Token(Token.Textless.NAME, "x", 0),
                                                        new Token(Token.Symbol.RIGHT_ANGLE, 0),
                                                        new Token(Token.Textless.NUMBER_LITERAL, "1", 0),
                                                        new Token(Token.Symbol.DOUBLE_AND, 0),
                                                        new Token(Token.Textless.NAME, "x", 0),
                                                        new Token(Token.Symbol.RIGHT_ANGLE_EQUAL, 0),
                                                        new Token(Token.Textless.NUMBER_LITERAL, "1", 0)
                                                )
                                        ),
                                        List.of(
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
                                                new Token(Token.Textless.NUMBER_LITERAL, "1", 0)
                                        )
                                ),
                                List.of(
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
                                        new Token(Token.Textless.NUMBER_LITERAL, "1", 0)
                                )
                        ),
                        List.of(
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
                                new Token(Token.Textless.NUMBER_LITERAL, "1", 0)
                        )
                ),
                List.of(
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
                        new Token(Token.Textless.NUMBER_LITERAL, "1", 0)
                )
        ).toFlatString();

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

        String expected = new MathStatement(
                new MathStatement(
                        ASTUtil.simpleNumLit(15d),
                        ASTEnums.MathOp.MINUS,
                        new MathStatement(
                                ASTUtil.simpleVar("y"),
                                ASTEnums.MathOp.PLUS,
                                ASTUtil.simpleVar("x"),
                                List.of(
                                        new Token(Token.Textless.NAME, "y", 0),
                                        new Token(Token.Symbol.PLUS, 0),
                                        new Token(Token.Textless.NAME, "x", 0)
                                )
                        ),
                        List.of(
                                new Token(Token.Textless.NUMBER_LITERAL, "15", 0),
                                new Token(Token.Symbol.MINUS, 0),
                                new Token(Token.Textless.NAME, "y", 0),
                                new Token(Token.Symbol.PLUS, 0),
                                new Token(Token.Textless.NAME, "x", 0)
                        )
                ),
                ASTEnums.MathOp.MUL,
                new MathStatement(
                        ASTUtil.simpleNumLit(15d),
                        ASTEnums.MathOp.DIV,
                        ASTUtil.simpleNumLit(12d),
                        List.of(
                                new Token(Token.Textless.NUMBER_LITERAL, "15", 0),
                                new Token(Token.Symbol.SLASH, 0),
                                new Token(Token.Textless.NUMBER_LITERAL, "12", 0)
                        )
                ),
                List.of(
                        new Token(Token.Textless.NUMBER_LITERAL, "15", 0),
                        new Token(Token.Symbol.MINUS, 0),
                        new Token(Token.Textless.NAME, "y", 0),
                        new Token(Token.Symbol.PLUS, 0),
                        new Token(Token.Textless.NAME, "x", 0),
                        new Token(Token.Symbol.STAR, 0),
                        new Token(Token.Textless.NUMBER_LITERAL, "15", 0),
                        new Token(Token.Symbol.SLASH, 0),
                        new Token(Token.Textless.NUMBER_LITERAL, "12", 0)
                )
        ).toFlatString();


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

        String expected = new MathAssign(
                ASTUtil.simpleVar("x"),
                ASTEnums.MathOp.PLUS,
                new MathAssign(
                        ASTUtil.simpleVar("y"),
                        ASTEnums.MathOp.MINUS,
                        ASTUtil.simpleNumLit(5d),
                        List.of(
                                new Token(Token.Textless.NAME, "y", 0),
                                new Token(Token.Symbol.MINUS_EQUAL, 0),
                                new Token(Token.Textless.NUMBER_LITERAL, "5", 0)
                        )
                ),
                List.of(
                        new Token(Token.Textless.NAME, "x", 0),
                        new Token(Token.Symbol.PLUS_EQUAL, 0),
                        new Token(Token.Textless.NAME, "y", 0),
                        new Token(Token.Symbol.MINUS_EQUAL, 0),
                        new Token(Token.Textless.NUMBER_LITERAL, "5", 0)
                )
        ).toFlatString();

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

        String expected = new MathSelfMod(
                ASTUtil.simpleVar("x"),
                ASTEnums.MathOp.PLUS,
                ASTEnums.SelfModTime.POST,
                List.of(
                        new Token(Token.Textless.NAME, "x", 0),
                        new Token(Token.Symbol.DOUBLE_PLUS, 0)
                )).toFlatString();

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
                ASTUtil.simpleVar("y"),
                ASTEnums.MathOp.MINUS,
                ASTEnums.SelfModTime.PRE,
                List.of(
                        new Token(Token.Symbol.DOUBLE_MINUS, 0),
                        new Token(Token.Textless.NAME, "y", 0)
                )
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
                new Type(
                        new Variable(
                                "MyClass",
                                List.of(new Token(Token.Textless.NAME, "MyClass", Token.IdentifierType.TYPE, 0))
                        ),
                        List.of(
                                new Type(new Variable("String", List.of(new Token(Token.Textless.NAME, "String", Token.IdentifierType.TYPE, 0))), List.of()),
                                new Type(new Variable("Int", List.of(new Token(Token.Textless.NAME, "Int", Token.IdentifierType.TYPE, 0))), List.of())
                        )
                ),
                List.of(
                        ASTUtil.simpleNumLit(15d)
                ),
                List.of(
                        new Token(Token.Keyword.NEW, 0),
                        new Token(Token.Textless.NAME, "MyClass", Token.IdentifierType.TYPE, 0),
                        new Token(Token.Symbol.LEFT_ANGLE, 0),
                        new Token(Token.Textless.NAME, "String", Token.IdentifierType.TYPE, 0),
                        new Token(Token.Symbol.COMMA, 0),
                        new Token(Token.Textless.NAME, "Int", Token.IdentifierType.TYPE, 0),
                        new Token(Token.Symbol.RIGHT_ANGLE, 0),
                        new Token(Token.Symbol.LEFT_PAREN, 0),
                        new Token(Token.Textless.NUMBER_LITERAL, "15", 0),
                        new Token(Token.Symbol.RIGHT_PAREN, 0)
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
                List.of(ASTUtil.simpleNumLit(15d)),
                List.of(
                        new Type(new Variable("String", List.of(new Token(Token.Textless.NAME, "String", Token.IdentifierType.TYPE, 0))), List.of()),
                        new Type(new Variable("Int", List.of(new Token(Token.Textless.NAME, "Int", Token.IdentifierType.TYPE, 0))), List.of())
                ),
                List.of(new Token(Token.Textless.NAME, "println", 0),
                        new Token(Token.Symbol.LEFT_ANGLE, 0),
                        new Token(Token.Textless.NAME, "String", Token.IdentifierType.TYPE, 0),
                        new Token(Token.Symbol.COMMA, 0),
                        new Token(Token.Textless.NAME, "Int", Token.IdentifierType.TYPE, 0),
                        new Token(Token.Symbol.RIGHT_ANGLE, 0),
                        new Token(Token.Symbol.LEFT_PAREN, 0),
                        new Token(Token.Textless.NUMBER_LITERAL, "15", 0),
                        new Token(Token.Symbol.RIGHT_PAREN, 0))
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
                ASTUtil.simpleVar("x"),
                new Subscript(
                        ASTUtil.simpleVar("y"),
                        ASTUtil.simpleVar("z"),
                        List.of(
                                new Token(Token.Textless.NAME, "y", 0),
                                new Token(Token.Symbol.LEFT_BRACKET, 0),
                                new Token(Token.Textless.NAME, "z", 0),
                                new Token(Token.Symbol.RIGHT_BRACKET, 0)
                        )
                ),
                List.of(
                    new Token(Token.Textless.NAME, "x", 0),
                    new Token(Token.Symbol.LEFT_BRACKET, 0),
                    new Token(Token.Textless.NAME, "y", 0),
                    new Token(Token.Symbol.LEFT_BRACKET, 0),
                    new Token(Token.Textless.NAME, "z", 0),
                    new Token(Token.Symbol.RIGHT_BRACKET, 0),
                    new Token(Token.Symbol.RIGHT_BRACKET, 0)
            )
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

        String expected = new ImportStatement(
                ASTEnums.ImportType.SYSTEM,
                "myPackage",
                List.of(
                        new Token(Token.Keyword.IMPORT, 0),
                        new Token(Token.Symbol.LEFT_ANGLE, 0),
                        new Token(Token.Textless.NAME, "myPackage", 0),
                        new Token(Token.Symbol.RIGHT_ANGLE, 0)
                )).toFlatString();

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

        String expected = new ThrowStatement(
                ASTUtil.simpleVar("myExistingError"),
                List.of(
                        new Token(Token.Keyword.THROW, 0),
                        new Token(Token.Textless.NAME, "myExistingError", 0)
                )).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }
}
