package io.dallen.parser;

import io.dallen.parser.AST;
import io.dallen.parser.BraceManager;
import io.dallen.tokenizer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class Splitter {

    public static AST.Statement rankedSingleSplit(List<Token> tokens, List<Map<Token.TokenType, SplitAction>> splitRanks) {
        for (Map<Token.TokenType, SplitAction> splits : splitRanks) {
            AST.Statement rtn = braceActionSplit(tokens, splits);
            if (rtn != null) {
                return rtn;
            }
        }
        return null;
    }

    public static List<List<Token>> braceSplit(List<Token> tokens, Token.TokenType on, int limit) {
        return braceSplit(tokens, on, limit, false);
    }

    private static AST.Statement braceActionSplit(List<Token> tokens, Map<Token.TokenType, SplitAction> splits) {
        int loc = tokens.size() - 1;

        BraceManager braceManager = new BraceManager(true);
        while (loc >= 0) {
            Token t = tokens.get(loc);

            braceManager.check(t);

            if (braceManager.isEmpty() && splits.containsKey(t.type)) {
                List<Token> first = tokens.subList(0, loc);
                List<Token> second = tokens.subList(loc + 1, tokens.size());
                SplitAction action = splits.get(t.type);
                AST.Statement s = null;
                if (action instanceof RawSplitAction) {
                    s = ((RawSplitAction) action).handle(first, second);
                } else if (action instanceof StatementSplitAction) {
                    AST.Statement firstStmt = new Parser(first).parseExpression();
                    AST.Statement secondStmt = new Parser(second).parseExpression();
                    s = ((StatementSplitAction) action).handle(firstStmt, secondStmt);
                }
                return s;
            }
            loc--;
        }
        return null;
    }

    private static List<List<Token>> braceSplit(List<Token> tokens, Token.TokenType on, int limit, boolean reverse) {
        if (limit == -1) {
            limit = Integer.MAX_VALUE;
        }

        BraceManager braceManager = new BraceManager(reverse);

        List<List<Token>> segments = new ArrayList<>();
        segments.add(new ArrayList<>());
        ListIterator<Token> itr = tokens.listIterator();
        while ((!reverse && itr.hasNext()) || (reverse && itr.hasPrevious())) {
            Token t;
            if (!reverse) {
                t = itr.next();
            } else {
                t = itr.previous();
            }

            braceManager.check(t);

            if (braceManager.isEmpty() && segments.size() < limit && t.type == on) {
                segments.add(new ArrayList<>());
            } else { // just add to results
                segments.get(segments.size() - 1).add(t);
            }
        }
        return segments;
    }

    public interface SplitAction {
    }

    public interface RawSplitAction extends SplitAction {
        AST.Statement handle(List<Token> first, List<Token> second);
    }

    public interface StatementSplitAction extends SplitAction {
        AST.Statement handle(AST.Statement first, AST.Statement second);
    }
}
