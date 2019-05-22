import tokenizer.Lexer;
import tokenizer.Token;

import java.util.List;

public class SkiffC {

    public static void printTokenStream(List<Token> tokens) {
        tokens.forEach(e -> System.out.print(" " + e.toString()));
    }

    public static void main(String[] argz) {
        Lexer lexer = new Lexer("x: Int; x = 10;");
        List<Token> tokenStream = lexer.lex();
        printTokenStream(tokenStream);
    }
}
