package io.dallen.parser.splitter;

import io.dallen.ast.AST;
import io.dallen.parser.BraceManager;
import io.dallen.parser.Parser;
import io.dallen.parser.ParserError;
import io.dallen.tokenizer.Token;

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
        if (parser == null) {
            int i = 0;
        }
        this.settings = settings;
    }

    public AST.Statement execute() {
        for (SplitLayer layer : settings.getSplitLayers()) {
            AST.Statement rtn;
            if (layer.isLeftToRight()) {
                rtn = executeLayerLeftToRight(layer);
            } else {
                rtn = executeLayerRightToLeft(layer);
            }
            if (rtn != null) {
                return rtn;
            }
        }
        return null;
    }

    private AST.Statement executeLayerRightToLeft(SplitLayer layer) {
        int loc = parser.tokenCount() - 1;

        BraceManager braceManager = new BraceManager(BraceManager.rightToLeft);
        while (loc >= 0) {

            AST.Statement result = handleToken(braceManager, layer, loc);
            if (result != null) {
                return result;
            }

            loc--;
        }
        return null;
    }

    private AST.Statement executeLayerLeftToRight(SplitLayer layer) {
        int loc = 0;

        BraceManager braceManager = new BraceManager(BraceManager.leftToRight);
        while (loc < parser.tokenCount()) {

            AST.Statement result = handleToken(braceManager, layer, loc);
            if (result != null) {
                return result;
            }

            loc++;
        }
        return null;
    }

    private AST.Statement handleToken(BraceManager braceManager, SplitLayer layer, int loc) {
        Token t = parser.get(loc);

        try {
            braceManager.check(t);
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return null;
        }
        SplitAction action;

        if (braceManager.isEmpty() && (action = layer.actionFor(t.type)) != null) {
            Parser first = new Parser(parser, parser.absoluteStart(), parser.absoluteStart() + loc);
            Parser second = new Parser(parser, parser.absoluteStart() + loc + 1, parser.absoluteStop());
            return action.handle(parser, first, second);
        }
        return null;
    }
}
