package io.dallen;

import io.dallen.parser.Parser;
import io.dallen.tokenizer.Lexer;
import io.dallen.tokenizer.Token;

import java.util.List;

public class SkiffC {

    public static void printTokenStream(List<Token> tokens) {
        tokens.forEach(e -> System.out.print(" " + e.toString()));
    }

    public static void main(String[] argz) {
        Lexer lexer = new Lexer("x: Int; x = 10;");
        List<Token> tokenStream = lexer.lex();
        printTokenStream(tokenStream);
        Parser parser = new Parser(tokenStream);
        parser.parseBlock().forEach(System.out::println);
    }
}
