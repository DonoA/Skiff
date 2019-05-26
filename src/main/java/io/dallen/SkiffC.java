package io.dallen;

import io.dallen.parser.Parser;
import io.dallen.tokenizer.Lexer;
import io.dallen.tokenizer.Token;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SkiffC {

    private static void printTokenStream(List<Token> tokens) {
        tokens.forEach(e -> System.out.print(" " + e.toString()));
        System.out.println();
    }

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.forName("UTF-8"));
    }


    public static void main(String[] argz) {
        String inFile = "test.skiff";
        String programText;
        try {
            programText = readFile(inFile);
        } catch(IOException err) {
            System.err.println("Bad file");
            return;
        }
        Lexer lexer = new Lexer(programText);
        List<Token> tokenStream = lexer.lex();
        printTokenStream(tokenStream);
        Parser parser = new Parser(tokenStream);
        List<AST.Statement> statements = parser.parseBlock();
        statements.forEach(System.out::println);

    }
}
