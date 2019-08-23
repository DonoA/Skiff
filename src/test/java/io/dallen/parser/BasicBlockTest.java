package io.dallen.parser;

import io.dallen.AST;
import io.dallen.AST.*;
import io.dallen.ASTUtil;
import io.dallen.tokenizer.Token;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class BasicBlockTest {

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
                new Token(Token.Keyword.DEF, 0),
                new Token(Token.Textless.NAME, "func", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "Int", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "Int", 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Keyword.RETURN, 0),
                new Token(Token.Textless.NAME, "x", 0),
                new Token(Token.Symbol.PLUS, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "1", 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
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
                new Token(Token.Keyword.CLASS, 0),
                new Token(Token.Textless.NAME, "Cls", 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "age", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "Int", 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Keyword.DEF, 0),
                new Token(Token.Textless.NAME, "Cls", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "age", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "Int", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "this", 0),
                new Token(Token.Symbol.DOT, 0),
                new Token(Token.Textless.NAME, "age", 0),
                new Token(Token.Symbol.EQUAL, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "10", 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected =
                new ClassDef(
                        "Cls",
                        List.of(),
                        Optional.empty(),
                        List.of(
                                new Declare(ASTUtil.simpleType("Int"), "age"),
                                new FunctionDef(
                                        List.of(),
                                        ASTUtil.simpleType("Void"),
                                        "Cls",
                                        List.of(
                                                new FunctionParam(ASTUtil.simpleType("Int"), "age")
                                        ),
                                        List.of(
                                                new Assign(
                                                        new Dotted(new Variable("this"), new Variable("age")),
                                                        new NumberLiteral(10.0)
                                                )
                                        )
                                )
                        )
                ).toFlatString();

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
                new Token(Token.Keyword.CLASS, 0),
                new Token(Token.Textless.NAME, "GClass", 0),
                new Token(Token.Symbol.LEFT_ANGLE, 0),
                new Token(Token.Textless.NAME, "U", 0),
                new Token(Token.Symbol.COMMA, 0),
                new Token(Token.Textless.NAME, "V", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "String", 0),
                new Token(Token.Symbol.RIGHT_ANGLE, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "a", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "U", 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new ClassDef(
                "GClass",
                List.of(
                        new GenericType("U", List.of()),
                        new GenericType("V", List.of(ASTUtil.simpleType("String")))
                ),
                Optional.empty(),
                List.of(new Declare(ASTUtil.simpleType("U"), "a"))
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

    /*
    def genFunc<U, V>(a: U): V { return a.getV(); }
     */

    @org.junit.Test
    public void parseGenericFunctionDef() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.DEF, 0),
                new Token(Token.Textless.NAME, "genFunc", 0),
                new Token(Token.Symbol.LEFT_ANGLE, 0),
                new Token(Token.Textless.NAME, "U", 0),
                new Token(Token.Symbol.COMMA, 0),
                new Token(Token.Textless.NAME, "V", 0),
                new Token(Token.Symbol.RIGHT_ANGLE, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "a", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "U", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "V", 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Keyword.RETURN, 0),
                new Token(Token.Textless.NAME, "a", 0),
                new Token(Token.Symbol.DOT, 0),
                new Token(Token.Textless.NAME, "getV", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
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
                new Token(Token.Keyword.CLASS, 0),
                new Token(Token.Textless.NAME, "CLS", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "Object", 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new ClassDef(
                "CLS",
                List.of(),
                Optional.of(ASTUtil.simpleType("Object")),
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
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "p", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "T", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "Returns", 0),
                new Token(Token.Symbol.ARROW, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new AST.AnonFunctionDef(
                ASTUtil.simpleType("Returns"),
                List.of(new FunctionParam(ASTUtil.simpleType("T"), "p")),
                List.of()
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        if(isWorking()) { run(); }
         */

    @org.junit.Test
    public void parseIf() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.IF, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "isWorking", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "run", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
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
                new Token(Token.Keyword.IF, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "isWorking", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "run", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Keyword.ELSE, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "stop", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.NAME, "setIsWorking", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Keyword.TRUE, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
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
                new Token(Token.Keyword.IF, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "isWorking", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "run", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Keyword.ELSE, 0),
                new Token(Token.Keyword.IF, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "readyToWork", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "stop", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.NAME, "setIsWorking", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Keyword.TRUE, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
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
                new Token(Token.Keyword.WHILE, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "notReady", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "wait", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
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
                new Token(Token.Keyword.LOOP, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "think", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
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
                new Token(Token.Keyword.FOR, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "i", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "Int", 0),
                new Token(Token.Symbol.EQUAL, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "0", 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.NAME, "i", 0),
                new Token(Token.Symbol.LEFT_ANGLE, 0),
                new Token(Token.Textless.NAME, "mySize", 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Textless.NAME, "i", 0),
                new Token(Token.Symbol.DOUBLE_PLUS, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "exec", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "i", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
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
        switch(i) { case 5 => runFive(); break; case _ => runOther(); }
         */

    @org.junit.Test
    public void parseSwitch() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.SWITCH, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "i", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Keyword.CASE, 0),
                new Token(Token.Textless.NUMBER_LITERAL, "5", 0),
                new Token(Token.Symbol.ARROW, 0),
                new Token(Token.Textless.NAME, "runFive", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Keyword.BREAK, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Keyword.CASE, 0),
                new Token(Token.Symbol.UNDERSCORE, 0),
                new Token(Token.Symbol.ARROW, 0),
                new Token(Token.Textless.NAME, "runOther", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new SwitchBlock(
                new Variable("i"),
                List.of(
                        new CaseStatement(new NumberLiteral(5.0)),
                        ASTUtil.simpleFuncCall("runFive"),
                        new BreakStatement(),
                        new CaseStatement(new Variable("_")),
                        ASTUtil.simpleFuncCall("runOther")
                )
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        match(i) { case v: Int => checkInt(); break; case v: _ => checkOther(); }
         */

    @org.junit.Test
    public void parseMatch() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.MATCH, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "i", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Keyword.CASE, 0),
                new Token(Token.Textless.NAME, "v", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "Int", 0),
                new Token(Token.Symbol.ARROW, 0),
                new Token(Token.Textless.NAME, "checkInt", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Keyword.BREAK, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Keyword.CASE, 0),
                new Token(Token.Textless.NAME, "v", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Symbol.UNDERSCORE, 0),
                new Token(Token.Symbol.ARROW, 0),
                new Token(Token.Textless.NAME, "checkOther", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        String expected = new MatchBlock(
                new Variable("i"),
                List.of(
                        new CaseMatchStatement(new Declare(ASTUtil.simpleType("Int"), "v")),
                        ASTUtil.simpleFuncCall("checkInt"),
                        new BreakStatement(),
                        new CaseMatchStatement(new Declare(ASTUtil.simpleType("_"), "v")),
                        ASTUtil.simpleFuncCall("checkOther")
                )
        ).toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }

        /*
        try { errorFunc(); } catch(ex: Error) { print(ex); }
         */

    @org.junit.Test
    public void parseTryCatch() {
        List<Token> tokens = List.of(
                new Token(Token.Keyword.TRY, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "errorFunc", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Keyword.CATCH, 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "ex", 0),
                new Token(Token.Symbol.COLON, 0),
                new Token(Token.Textless.NAME, "Error", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.LEFT_BRACE, 0),
                new Token(Token.Textless.NAME, "print", 0),
                new Token(Token.Symbol.LEFT_PAREN, 0),
                new Token(Token.Textless.NAME, "ex", 0),
                new Token(Token.Symbol.RIGHT_PAREN, 0),
                new Token(Token.Symbol.SEMICOLON, 0),
                new Token(Token.Symbol.RIGHT_BRACE, 0),
                new Token(Token.Textless.EOF, 0));
        List<Statement> statements = new Parser(tokens).parseBlock();

        assertEquals(1, statements.size());

        TryBlock tryBlock = new TryBlock(
                List.of(ASTUtil.simpleFuncCall("errorFunc"))
        );

        tryBlock.catchBlock = new CatchBlock(new Declare(ASTUtil.simpleType("Error"), "ex"), List.of(
                new FunctionCall("print", List.of(new Variable("ex")), List.of())
        ));

        String expected = tryBlock.toFlatString();

        assertEquals(expected, statements.get(0).toFlatString());
    }
}
