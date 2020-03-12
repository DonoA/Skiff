package io.dallen.parser.splitter;

import io.dallen.parser.BraceManager;
import io.dallen.parser.Parser;
import io.dallen.parser.ParserError;
import io.dallen.tokenizer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Splitter helper class for splitting streams of tokens on a given token type. All splits are brace aware.
 */
public class BraceSplitter {

    public static List<Parser> splitAll(Parser parser, Token.TokenType on) throws ParserError {
        return braceSplitLeftToRight(parser, on, Integer.MAX_VALUE);
    }

    public static List<Parser> customSplitAll(BraceManager.BraceProfile braces, Parser parser,
                                                   Token.TokenType on) throws ParserError {
        return customBraceSplitLeftToRight(braces, parser, on, Integer.MAX_VALUE);
    }

    public static List<Parser> braceSplitLeftToRight(Parser parser, Token.TokenType on,
                                                          int limit) throws ParserError {
        return customBraceSplitLeftToRight(BraceManager.leftToRight, parser, on, limit);
    }

    public static List<Parser> customBraceSplitLeftToRight(BraceManager.BraceProfile braces, Parser parser,
                                                                Token.TokenType on, int limit) throws ParserError {
        BraceManager braceManager = new BraceManager(braces);

        List<Parser> segments = new ArrayList<>();
        int j = 0;
        for(int i = 0; i < parser.tokenCount(); i++) {
            Token t = parser.get(i);
            braceManager.check(t);

            if (braceManager.isEmpty() && segments.size() < limit && t.type == on) {
                segments.add(new Parser(parser, parser.absoluteStart() + j, parser.absoluteStart() + i));
                j = i + 1;
            }
        }

        if(parser.absoluteStop() - (parser.absoluteStart() + j) != 0) {
            segments.add(new Parser(parser, parser.absoluteStart() + j, parser.absoluteStop()));
        }

        return segments;
    }
}
