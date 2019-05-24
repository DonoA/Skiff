package io.dallen.parser.splitter;

import io.dallen.parser.AST;
import io.dallen.parser.BraceManager;
import io.dallen.tokenizer.Token;

import java.util.List;

public class LayeredSplitter {

    private final SplitSettings settings;

    public LayeredSplitter(SplitSettings settings) {
        this.settings = settings;
    }

    public AST.Statement execute(List<Token> tokens) {
        for (SplitLayer layer : settings.getSplitLayers()) {
            AST.Statement rtn;
            if(layer.isLeftToRight()) {
                rtn = executeLayerLeftToRight(tokens, layer);
            } else {
                rtn = executeLayerRightToLeft(tokens, layer);
            }
            if (rtn != null) {
                return rtn;
            }
        }
        return null;
    }

    private AST.Statement executeLayerRightToLeft(List<Token> tokens, SplitLayer layer) {
        int loc = tokens.size() - 1;

        BraceManager braceManager = new BraceManager(BraceManager.rightToLeft);
        while (loc >= 0) {

            AST.Statement result = handleToken(tokens, braceManager, layer, loc);
            if(result != null) {
                return result;
            }

            loc--;
        }
        return null;
    }

    private AST.Statement executeLayerLeftToRight(List<Token> tokens, SplitLayer layer) {
        int loc = 0;

        BraceManager braceManager = new BraceManager(BraceManager.leftToRight);
        while (loc < tokens.size()) {

            AST.Statement result = handleToken(tokens, braceManager, layer, loc);
            if(result != null) {
                return result;
            }

            loc++;
        }
        return null;
    }

    private AST.Statement handleToken(List<Token> tokens, BraceManager braceManager, SplitLayer layer, int loc) {
        Token t = tokens.get(loc);

        braceManager.check(t);
        SplitAction action;

        if (braceManager.isEmpty() && (action = layer.actionFor(t.type)) != null) {
            List<Token> first = tokens.subList(0, loc);
            List<Token> second = tokens.subList(loc + 1, tokens.size());
            return action.handle(first, second);
        }
        return null;
    }
}
