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

//    @org.junit.Test
//    public void parseFunctionDef() {
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
//
//    @org.junit.Test
//    public void parseClassDef() {
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
//
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
//
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
//
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
//
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
//
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
//
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
//
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
