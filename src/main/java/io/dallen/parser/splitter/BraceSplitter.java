package io.dallen.parser.splitter;

import io.dallen.parser.BraceManager;
import io.dallen.parser.ParserError;
import io.dallen.tokenizer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class BraceSplitter {

    public static List<List<Token>> splitAll(List<Token> tokens, Token.TokenType on) throws ParserError {
        return braceSplitLeftToRight(tokens, on, Integer.MAX_VALUE);
    }

    public static List<List<Token>> customSplitAll(BraceManager.BraceProfile braces, List<Token> tokens,
                                                   Token.TokenType on) throws ParserError {
        return customBraceSplitLeftToRight(braces, tokens, on, Integer.MAX_VALUE);
    }

    public static List<List<Token>> braceSplitLeftToRight(List<Token> tokens, Token.TokenType on,
                                                          int limit) throws ParserError {
        return customBraceSplitLeftToRight(BraceManager.leftToRight, tokens, on, limit);
    }

    public static List<List<Token>> customBraceSplitLeftToRight(BraceManager.BraceProfile braces, List<Token> tokens,
                                                                Token.TokenType on, int limit) throws ParserError {
        BraceManager braceManager = new BraceManager(braces);

        List<List<Token>> segments = new ArrayList<>();
        List<Token> workingSeg = new ArrayList<>();
        for (Token t : tokens) {
            braceManager.check(t);

            if (braceManager.isEmpty() && segments.size() < limit && t.type == on) {
                segments.add(workingSeg);
                workingSeg = new ArrayList<>();
            } else { // just add to results
                workingSeg.add(t);
            }
        }

        if(!workingSeg.isEmpty()) {
            segments.add(workingSeg);
        }
        return segments;
    }

    public static List<List<Token>> braceSplitRightToLeft(List<Token> tokens, Token.TokenType on,
                                                          int limit) throws ParserError {
        BraceManager braceManager = new BraceManager(BraceManager.rightToLeft);

        List<List<Token>> segments = new ArrayList<>();
        List<Token> workingSeg = new ArrayList<>();
        segments.add(0, workingSeg);
        ListIterator<Token> itr = tokens.listIterator();
        while (itr.hasPrevious()) {
            Token t = itr.previous();

            braceManager.check(t);

            if (braceManager.isEmpty() && segments.size() < limit && t.type == on) {
                segments.add(0, workingSeg);
                workingSeg = new ArrayList<>();
            } else { // just add to results
                workingSeg.add(t);
            }
        }
        if(!workingSeg.isEmpty()) {
            segments.add(0, workingSeg);
        }
        return segments;
    }
}
