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
import java.util.Optional;
import java.util.stream.Collectors;

public class SkiffC {

    public final static int MAX_COL = 120;
    public final static String stdFolder = "lib/std";

    private static void printTokenStream(List<Token> tokens) {
        tokens.forEach(e -> System.out.print(" " + e.toString()));
        System.out.println();
    }

    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static void main(String[] argz) {
        compile("test.skiff", "test.c", true);
    }

    public static boolean compile(String infile, String outfile, boolean debug) {
        Optional<String> code = compile(infile, debug);
        try (PrintWriter out = new PrintWriter(outfile)) {
            code.ifPresent(out::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return code.isPresent();
    }

    public static Optional<String> compile(String infile, boolean debug) {
        String programText;
        try {
            programText = readFile(infile);
        } catch(IOException err) {
            System.err.println("Bad file");
            return Optional.empty();
        }

        StringBuilder preamble = new StringBuilder();

        preamble.append("#include \"" + new File(stdFolder + "/skiff.h").getAbsolutePath() + "\"\n\n");

        CompileContext context = new CompileContext(programText, infile, debug);
        context.getScope().loadBuiltins();
        try {
            for(File f : new File(stdFolder).listFiles()) {
                if(f.getName().endsWith(".skiff")) {
                    String importText = readFile(f.getPath());
                    context.setFilename(f.getName());
                    Optional<String> importCode = compile(importText, context);
                    if(importCode.isEmpty()) {
                        System.err.println("Issue with compile std lib file");
                    } else {
                        preamble.append(importCode.get());
                    }
                }
            }
        } catch(IOException err) {
            System.err.println("Failed to get std lib skiff file");
            err.printStackTrace();
            return Optional.empty();
        }

        context.setFilename(infile);
        Optional<String> code = compile(programText, context);
        return code.map(c -> preamble + c);
    }

    public static Optional<String> compile(String programText, CompileContext context) {
        boolean passed = true;

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
            return Optional.empty();
        }

        if(context.isDebug()) {
            System.out.println(" ======== PARSE " + context.getFilename() + " =========== ");
        }

        Parser parser = new Parser(tokenStream, programText);
        List<AST.Statement> statements = null;
        try {
            statements = parser.parseAll();
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
            return Optional.empty();
        }
        if(context.isDebug()) {
            System.out.println(" ======== COMPILE " + context.getFilename() + " =========== ");
        }

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
            return Optional.empty();
        }
        String code = String.join("\n", compiledText);

//        System.out.println(code);

        if(!passed && !context.isDebug()) {
            return Optional.empty();
        } else {
            return Optional.of(code);
        }
    }



}
