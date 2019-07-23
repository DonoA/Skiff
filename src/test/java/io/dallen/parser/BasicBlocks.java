package io.dallen.parser;

import io.dallen.AST;
import io.dallen.tokenizer.Token;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BasicBlocks {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(1);

    /*

    @org.junit.Test
    public void parse() {
        List<Token> tokens = ;
        List<AST.Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

     */

    @org.junit.Test
    public void parseFunctionDef() {
        /*
        def func(x: Int): Int { return x + 1; }
         */
        List<Token> tokens = List.of(
                new Token(Token.Keyword.DEF),
                new Token(Token.Textless.NAME, "func"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "Int"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "Int"),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Keyword.RETURN),
                new Token(Token.Textless.NAME, "x"),
                new Token(Token.Symbol.PLUS),
                new Token(Token.Textless.NUMBER_LITERAL, "1"),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<AST.Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "FunctionDef(" +
                "returns = Type(name = Variable(name = Int), arraySize = 0, genericTypes = [ ]), " +
                "name = func, " +
                "args = [" +
                "FunctionParam(type = Type(name = Variable(name = Int), arraySize = 0, genericTypes = [ ]), name = x) " +
                "], " +
                "body = [" +
                "Return(value = MathStatement(left = Variable(name = x), op = PLUS, right = NumberLiteral(value = 1.0))) " +
                "])";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    /*
    class Cls { age: Int; def Cls(age: Int) { this.age = 10; } }
     */


    @org.junit.Test
    public void parseClassDef() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.CLASS),
                new Token(Token.Textless.NAME, "Cls"),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "age"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "Int"),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Keyword.DEF),
                new Token(Token.Textless.NAME, "Cls"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "age"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "Int"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "this"),
                new Token(Token.Symbol.DOT),
                new Token(Token.Textless.NAME, "age"),
                new Token(Token.Symbol.EQUAL),
                new Token(Token.Textless.NUMBER_LITERAL, "10"),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<AST.Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "ClassDef(" +
                "name = Cls, " +
                "extendClasses = [ ], " +
                "body = [" +
                "Declare(type = Type(name = Variable(name = Int), arraySize = 0, genericTypes = [ ]), name = age), " +
                "FunctionDef(returns = Type(name = Variable(name = Void), arraySize = 0, genericTypes = [ ]), " +
                "name = Cls, " +
                "args = [" +
                "FunctionParam(type = Type(name = Variable(name = Int), arraySize = 0, genericTypes = [ ]), name = age) " +
                "], " +
                "body = [" +
                "Assign(name = Dotted(left = Variable(name = this), right = Variable(name = age)), " +
                "value = NumberLiteral(value = 10.0)) ]) ])";

        assertEquals(expected, statements.get(0).toFlatString());
    }
//
//    @org.junit.Test
//    public void parseDataClassDef() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }
//
//    @org.junit.Test
//    public void parseGenericClassDef() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }
//
//    @org.junit.Test
//    public void parseGenericFunctionDef() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }
//
//    @org.junit.Test
//    public void parseClassInheritance() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }
//
//    @org.junit.Test
//    public void parseAnonFuncDef() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }

        /*
        if(isWorking()) { run(); }
         */

//    @org.junit.Test
//    public void parseIf() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }

        /*
        if(isWorking()) { run(); } else { stop(); setIsWorking(true); }
         */

//    @org.junit.Test
//    public void parseIfElse() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }

        /*
        if(isWorking()) { run(); } else if(readyToWork()) { stop(); setIsWorking(true); }
         */

//    @org.junit.Test
//    public void parseIfElseIf() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }

        /*
        while(notReady()) { wait(); }
         */

//    @org.junit.Test
//    public void parseWhile() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }

        /*
        loop { think(); }
         */

//    @org.junit.Test
//    public void parseLoop() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }

        /*
        for(i: Int = 0; i < mySize; i++) { exec(i); }
         */

//    @org.junit.Test
//    public void parseFor() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }

        /*
        for(i: Int in myList) { exec(i); }
         */

//    @org.junit.Test
//    public void parseForIter() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }
//
//    @org.junit.Test
//    public void parseSwitch() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }
//
//    @org.junit.Test
//    public void parseMatch() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }
//
//    @org.junit.Test
//    public void parseTryCatch() {
//        List<Token> tokens = ;
//        List<AST.Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }
}
