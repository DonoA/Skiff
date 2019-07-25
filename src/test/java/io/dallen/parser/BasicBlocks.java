package io.dallen.parser;

import io.dallen.AST.*;
import io.dallen.ASTUtil;
import io.dallen.tokenizer.Token;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BasicBlocks {

//    @Rule
//    public Timeout globalTimeout = Timeout.seconds(1);

    /*

    @org.junit.Test
    public void parse() {
        List<Token> tokens = ;
        List<Statement> statements = new Parser(tokens).parseBlock();

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
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new FunctionDef(
                List.of(),
                ASTUtil.simpleType("Int"),
                "func",
                List.of(new FunctionParam(ASTUtil.simpleType("Int"), "x")),
                List.of(new Return(new MathStatement(new Variable("x"), MathOp.PLUS, new NumberLiteral(1.0))))
        ).toFlatString();

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
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "ClassDef(" +
                "name = Cls, " +
                "genericTypes = [ ], " +
                "extendClasses = [ ], " +
                "body = [" +
                "Declare(type = Type(name = Variable(name = Int), arraySize = 0, genericTypes = [ ]), name = age), " +
                "FunctionDef(" +
                "genericTypes = [ ], " +
                "returns = Type(name = Variable(name = Void), arraySize = 0, genericTypes = [ ]), " +
                "name = Cls, " +
                "args = [" +
                "FunctionParam(type = Type(name = Variable(name = Int), arraySize = 0, genericTypes = [ ]), name = age) " +
                "], " +
                "body = [" +
                "Assign(name = " +
                "Dotted(left = Variable(name = this), right = Variable(name = age)), value = NumberLiteral(value = 10.0)) " +
                "]) " +
                "])";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    /*
    struct DataClass { a: Int; }
     */

//    @org.junit.Test
//    public void parseDataClassDef() {
//        List<Token> tokens = ;
//        List<Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }

    /*
    class GClass<U, V : String> { a: U; }
     */

    @org.junit.Test
    public void parseGenericClassDef() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.CLASS),
                new Token(Token.Textless.NAME, "GClass"),
                new Token(Token.Symbol.LEFT_ANGLE),
                new Token(Token.Textless.NAME, "U"),
                new Token(Token.Symbol.COMMA),
                new Token(Token.Textless.NAME, "V"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "String"),
                new Token(Token.Symbol.RIGHT_ANGLE),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "a"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "U"),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "ClassDef(" +
                "name = GClass, " +
                "genericTypes = [" +
                "GenericType(name = U, reqExtend = [ ]), " +
                "GenericType(name = V, reqExtend = [" +
                "Type(name = Variable(name = String), arraySize = 0, genericTypes = [ ]) ]) ], " +
                "extendClasses = [ ], " +
                "body = [" +
                "Declare(type = Type(name = Variable(name = U), arraySize = 0, genericTypes = [ ]), name = a) " +
                "])";

        assertEquals(expected, statements.get(0).toFlatString());
    }

    /*
    def genFunc<U, V>(a: U): V { return a.getV(); }
     */

    @org.junit.Test
    public void parseGenericFunctionDef() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.DEF),
                new Token(Token.Textless.NAME, "genFunc"),
                new Token(Token.Symbol.LEFT_ANGLE),
                new Token(Token.Textless.NAME, "U"),
                new Token(Token.Symbol.COMMA),
                new Token(Token.Textless.NAME, "V"),
                new Token(Token.Symbol.RIGHT_ANGLE),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "a"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "U"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "V"),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Keyword.RETURN),
                new Token(Token.Textless.NAME, "a"),
                new Token(Token.Symbol.DOT),
                new Token(Token.Textless.NAME, "getV"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new FunctionDef(
                List.of(
                        new GenericType("U", List.of()),
                        new GenericType("V", List.of())
                        ),
                ASTUtil.simpleType("V"),
                "genFunc",
                List.of(new FunctionParam(ASTUtil.simpleType("U"), "a")),
                List.of(
                        new Return(
                                new Dotted(
                                        new Variable("a"),
                                        ASTUtil.simpleFuncCall("getV")))
                )
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    /*
    class CLS : Object { }
     */

    @org.junit.Test
    public void parseClassInheritance() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.CLASS),
                new Token(Token.Textless.NAME, "CLS"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "Object"),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new ClassDef(
                "CLS",
                List.of(),
                List.of(ASTUtil.simpleType("Object")),
                List.of()
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    /*
    (p: T): Returns => {}
     */

    @org.junit.Test
    public void parseAnonFuncDef() {
        List<Token> tokens = List.of(
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "p"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "T"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "Returns"),
                new Token(Token.Symbol.ARROW),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        if(isWorking()) { run(); }
         */

    @org.junit.Test
    public void parseIf() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.IF),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "isWorking"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "run"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new IfBlock(
                ASTUtil.simpleFuncCall("isWorking"),
                List.of(ASTUtil.simpleFuncCall("run"))
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        if(isWorking()) { run(); } else { stop(); setIsWorking(true); }
         */

    @org.junit.Test
    public void parseIfElse() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.IF),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "isWorking"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "run"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Keyword.ELSE),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "stop"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.NAME, "setIsWorking"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Keyword.TRUE),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        IfBlock start = new IfBlock(
                ASTUtil.simpleFuncCall("isWorking"),
                List.of(ASTUtil.simpleFuncCall("run"))
        );

        start.elseBlock = new ElseAlwaysBlock(
          List.of(
                  ASTUtil.simpleFuncCall("stop"),
                  new FunctionCall("setIsWorking", List.of(new BooleanLiteral(true)), List.of())
          )
        );

        String expected = start.toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        if(isWorking()) { run(); } else if(readyToWork()) { stop(); setIsWorking(true); }
         */

    @org.junit.Test
    public void parseIfElseIf() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.IF),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "isWorking"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "run"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Keyword.ELSE),
                new Token(Token.Keyword.IF),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "readyToWork"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "stop"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.NAME, "setIsWorking"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Keyword.TRUE),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        IfBlock ifBlock = new IfBlock(
                ASTUtil.simpleFuncCall("isWorking"),
                List.of(ASTUtil.simpleFuncCall("run"))
        );

        ifBlock.elseBlock = new ElseIfBlock(
                new IfBlock(
                        ASTUtil.simpleFuncCall("readyToWork"), List.of(
                        ASTUtil.simpleFuncCall("stop"),
                        new FunctionCall("setIsWorking", List.of(new BooleanLiteral(true)), List.of())
                ))
        );

        String expected = ifBlock.toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        while(notReady()) { wait(); }
         */

    @org.junit.Test
    public void parseWhile() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.WHILE),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "notReady"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "wait"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "WhileBlock(" +
                "condition = FunctionCall(name = notReady, args = [ ], genericTypes = [ ]), " +
                "body = [FunctionCall(name = wait, args = [ ], genericTypes = [ ]) ])";

        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        loop { think(); }
         */

    @org.junit.Test
    public void parseLoop() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.LOOP),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "think"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

//        String expected = "LoopBlock(body = [FunctionCall(name = think, args = [ ]) ])";
        String expected = new LoopBlock(List.of(
                ASTUtil.simpleFuncCall("think")
        )).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        for(i: Int = 0; i < mySize; i++) { exec(i); }
         */

    @org.junit.Test
    public void parseFor() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.FOR),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "i"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "Int"),
                new Token(Token.Symbol.EQUAL),
                new Token(Token.Textless.NUMBER_LITERAL, "0"),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.NAME, "i"),
                new Token(Token.Symbol.LEFT_ANGLE),
                new Token(Token.Textless.NAME, "mySize"),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.NAME, "i"),
                new Token(Token.Symbol.DOUBLE_PLUS),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "exec"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "i"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new ForBlock(
                new DeclareAssign(
                        ASTUtil.simpleType("Int"),
                        "i",
                        new NumberLiteral(0.0)
                ),
                new Compare(new Variable("i"), CompareOp.LT, new Variable("mySize")),
                new MathSelfMod(new Variable("i"), MathOp.PLUS, SelfModTime.POST),
                List.of(new FunctionCall("exec", List.of(new Variable("i")), List.of()))
        ).toFlatString();


        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        for(i: Int in myList) { exec(i); }
         */

//    @org.junit.Test
//    public void parseForIter() {
//        List<Token> tokens = ;
//        List<Statement> statements = new Parser(tokens).parseBlock();
//
//        assertEquals(1, statements.size());
//
//        String expected = "";
//
//        assertEquals(expected, statements.get(0).toFlatString());
//    }

        /*
        switch(i) { case 5: runFive(); break; default: runOther(); }
         */

    @org.junit.Test
    public void parseSwitch() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.SWITCH),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "i"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Keyword.CASE),
                new Token(Token.Textless.NUMBER_LITERAL, "5"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "runFive"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Keyword.BREAK),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.NAME, "default"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "runOther"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        match(i) { case Int: checkInt(); break; default: checkOther(); }
         */

    @org.junit.Test
    public void parseMatch() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.MATCH),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "i"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Keyword.CASE),
                new Token(Token.Textless.NAME, "Int"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "checkInt"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Keyword.BREAK),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Textless.NAME, "default"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "checkOther"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        try { errorFunc(); } catch(ex: Error) { print(ex); }
         */

    @org.junit.Test
    public void parseTryCatch() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.TRY),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "errorFunc"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Keyword.CATCH),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "ex"),
                new Token(Token.Symbol.COLON),
                new Token(Token.Textless.NAME, "Error"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.LEFT_BRACE),
                new Token(Token.Textless.NAME, "print"),
                new Token(Token.Symbol.LEFT_PAREN),
                new Token(Token.Textless.NAME, "ex"),
                new Token(Token.Symbol.RIGHT_PAREN),
                new Token(Token.Symbol.SEMICOLON),
                new Token(Token.Symbol.RIGHT_BRACE),
                new Token(Token.Textless.EOF));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = "";

        assertEquals(expected, statements.get(0).toFlatString());
    }
}
