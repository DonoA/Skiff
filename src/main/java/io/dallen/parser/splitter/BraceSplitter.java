package io.dallen.parser.splitter;

import io.dallen.parser.BraceManager;
import io.dallen.tokenizer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class BraceSplitter {

    public static List<List<Token>> splitAll(List<Token> tokens, Token.TokenType on) {
        return braceSplitLeftToRight(tokens, on, Integer.MAX_VALUE);
    }

    public static List<List<Token>> braceSplitLeftToRight(List<Token> tokens, Token.TokenType on, int limit) {
        BraceManager braceManager = new BraceManager(BraceManager.leftToRight);

        List<List<Token>> segments = new ArrayList<>();
        segments.add(new ArrayList<>());
        ListIterator<Token> itr = tokens.listIterator();
        while (itr.hasNext()) {
            Token t = itr.next();

            braceManager.check(t);

            if (braceManager.isEmpty() && segments.size() < limit && t.type == on) {
                segments.add(new ArrayList<>());
            } else { // just add to results
                segments.get(segments.size() - 1).add(t);
            }
        }
        return segments;
    }

    public static List<List<Token>> braceSplitRightToLeft(List<Token> tokens, Token.TokenType on, int limit) {
        BraceManager braceManager = new BraceManager(BraceManager.rightToLeft);

        List<List<Token>> segments = new ArrayList<>();
        segments.add(new ArrayList<>());
        ListIterator<Token> itr = tokens.listIterator();
        while (itr.hasPrevious()) {
            Token t = itr.previous();

            braceManager.check(t);

            if (braceManager.isEmpty() && segments.size() < limit && t.type == on) {
                segments.add(new ArrayList<>());
            } else { // just add to results
                segments.get(segments.size() - 1).add(t);
            }
        }
        return segments;
    }
}
