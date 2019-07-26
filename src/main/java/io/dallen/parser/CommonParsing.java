package io.dallen.parser;

import io.dallen.AST;
import io.dallen.parser.splitter.BraceSplitter;
import io.dallen.tokenizer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class CommonParsing {

    private Parser parser;

    CommonParsing(Parser parser) {
        this.parser = parser;
    }

    List<AST.GenericType> consumeGenericList() {
        parser.consumeExpected(Token.Symbol.LEFT_ANGLE);
        List<Token> genericTokens = parser.consumeTo(Token.Symbol.RIGHT_ANGLE, BraceManager.leftToRightAngle);
        List<List<Token>> genericTokenSeg = BraceSplitter.splitAll(genericTokens, Token.Symbol.COMMA);
        return genericTokenSeg
                .stream()
                .map(seg -> new Parser(seg).getCommon().parseGenericType())
                .collect(Collectors.toList());
    }

    AST.GenericType parseGenericType() {
        Token name = parser.consumeExpected(Token.Textless.NAME);

        List<AST.Type> subTypes = new ArrayList<>();
        if(parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            subTypes.add(parseType());
        }

        return new AST.GenericType(name.literal, subTypes);
    }

    AST.Type parseType() {
        AST.Statement typeName = new Parser(parser.consumeTo(Token.Symbol.LEFT_ANGLE)).parseExpression();
        if(parser.current().isEOF()) {
            return new AST.Type(typeName, 0, new ArrayList<>());
        }
        List<AST.Type> genericParams = BraceSplitter
                .customSplitAll(BraceManager.leftToRightAngle, parser.consumeTo(Token.Symbol.RIGHT_ANGLE), Token.Symbol.COMMA)
                .stream()
                .map(e -> new Parser(e).getCommon().parseType())
                .collect(Collectors.toList());

        return new AST.Type(typeName, 0, genericParams);
    }

    List<AST.FunctionParam> parseFunctionDecArgs(List<Token> paramTokens) {
        return BraceSplitter.splitAll(paramTokens, Token.Symbol.COMMA)
                .stream()
                .map(e -> BraceSplitter.splitAll(e, Token.Symbol.COLON))
                .map(e -> new AST.FunctionParam(new Parser(e.get(1)).getCommon().parseType(), e.get(0).get(0).literal))
                .collect(Collectors.toList());
    }

}
