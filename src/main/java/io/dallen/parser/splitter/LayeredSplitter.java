package io.dallen.parser.splitter;

import io.dallen.ast.AST;
import io.dallen.parser.BraceManager;
import io.dallen.parser.Parser;
import io.dallen.parser.ParserError;
import io.dallen.tokenizer.Token;

import java.util.List;

/**
 * Allows for splitting with precedence. Each layer in the settings is checked in order. Within each layer, the cases
 * are checked in order. The first hit recorded while parsing will be executed and have its result passed back to the
 * original caller.
 */
public class LayeredSplitter {

    private final SplitSettings settings;
    private final Parser parser;

    public LayeredSplitter(SplitSettings settings, Parser parser) {
        this.parser = parser;
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

        try {
            braceManager.check(t);
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return null;
        }
        SplitAction action;

        if (braceManager.isEmpty() && (action = layer.actionFor(t.type)) != null) {
            List<Token> first = tokens.subList(0, loc);
            List<Token> second = tokens.subList(loc + 1, tokens.size());
            return action.handle(parser, first, second, tokens);
        }
        return null;
    }
}
