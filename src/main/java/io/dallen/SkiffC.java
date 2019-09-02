package io.dallen;

import io.dallen.ast.AST;
import io.dallen.compiler.CompileContext;
import io.dallen.compiler.CompiledCode;
import io.dallen.parser.Parser;
import io.dallen.tokenizer.Lexer;
import io.dallen.tokenizer.Token;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class SkiffC {

    public static boolean DEBUG = true;

    private static void printTokenStream(List<Token> tokens) {
        tokens.forEach(e -> System.out.print(" " + e.toString()));
        System.out.println();
    }

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static void main(String[] argz) {
        compile("test.skiff", "test.c", true);
    }

    public static boolean compile(String infile, String outfile, boolean debug) {
        DEBUG = debug;
        String preamble = "#include \"" + new File("lib/skiff.h").getAbsolutePath() + "\"\n\n";

        boolean passed = true;

        String programText;
        try {
            programText = readFile(infile);
        } catch(IOException err) {
            System.err.println("Bad file");
            return false;
        }
        Lexer lexer = new Lexer(programText);
        List<Token> tokenStream = null;
        try {
            tokenStream = lexer.lex();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
//        printTokenStream(tokenStream);
            if(!lexer.getErrors().isEmpty()) {
                System.out.println(String.join("\n", lexer.getErrors()));
                passed = false;
            }
        }

        if(tokenStream == null || tokenStream.isEmpty()) {
            return false;
        }

        if(DEBUG) {
            System.out.println(" ======== PARSE =========== ");
        }

        Parser parser = new Parser(tokenStream, programText);
        List<AST.Statement> statements = null;
        try {
            statements = parser.parseBlock();
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
//        statements.forEach(System.out::println);
            if(!parser.getErrors().isEmpty()) {
                System.out.println(String.join("\n", parser.getErrors()));
                passed = false;
            }
        }

        if(statements == null || statements.isEmpty()) {
            return false;
        }
        if(DEBUG) {
            System.out.println(" ======== COMPILE =========== ");
        }

        CompileContext context = new CompileContext(programText);
        List<String> compiledText = null;
        try {
            compiledText = statements
                    .stream()
                    .map(e -> e.compile(context))
                    .map(CompiledCode::getCompiledText)
                    .collect(Collectors.toList());

        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            if(!context.getErrors().isEmpty()) {
                System.out.println(String.join("\n", context.getErrors()));
                passed = false;
            }
            // print errors from context
        }

        if(compiledText == null || compiledText.isEmpty()) {
            return false;
        }
        String code = preamble + String.join("\n", compiledText);

//        System.out.println(code);

        try (PrintWriter out = new PrintWriter(outfile)) {
            out.println(code);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return passed;
    }
}
