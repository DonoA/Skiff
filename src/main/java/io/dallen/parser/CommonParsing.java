package io.dallen.parser;

import io.dallen.AST;
import io.dallen.parser.splitter.BraceSplitter;
import io.dallen.tokenizer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class CommonParsing {

    static <T> List<T> joinLists(List<T> l1, List<T> l2) {
        List<T> l = new ArrayList<>();
        l.addAll(l1);
        l.addAll(l2);
        return l;
    }

    private Parser parser;

    CommonParsing(Parser parser) {
        this.parser = parser;
    }

    List<AST.GenericType> consumeGenericList() {
        parser.consumeExpected(Token.Symbol.LEFT_ANGLE);
        List<Token> genericTokens = parser.consumeTo(Token.Symbol.RIGHT_ANGLE, BraceManager.leftToRightAngle);
        List<List<Token>> genericTokenSeg;
        try {
            genericTokenSeg = BraceSplitter.splitAll(genericTokens, Token.Symbol.COMMA);
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return List.of();
        }
        return genericTokenSeg
                .stream()
                .map(seg -> new Parser(seg, parser).getCommon().parseGenericType())
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
        AST.Statement typeName = new Parser(parser.consumeTo(Token.Symbol.LEFT_ANGLE), parser).parseExpression();
        if(parser.current().isEOF()) {
            return new AST.Type(typeName, List.of());
        }
        List<AST.Type> genericParams;
        try {
            genericParams = BraceSplitter
                    .customSplitAll(BraceManager.leftToRightAngle, parser.consumeTo(Token.Symbol.RIGHT_ANGLE), Token.Symbol.COMMA)
                    .stream()
                    .map(e -> new Parser(e, parser).getCommon().parseType())
                    .collect(Collectors.toList());
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return null;
        }

        return new AST.Type(typeName, genericParams);
    }

    List<AST.FunctionParam> parseFunctionDecArgs(List<Token> paramTokens) {
        try {
            return BraceSplitter.splitAll(paramTokens, Token.Symbol.COMMA)
                    .stream()
                    .map(e -> {
                        try {
                            return BraceSplitter.splitAll(e, Token.Symbol.COLON);
                        } catch (ParserError parserError) {
                            throw new ParserError.NoCatchParseError(parserError.msg, parserError.on);
                        }
                    })
                    .map(e -> new AST.FunctionParam(new Parser(e.get(1), parser).getCommon().parseType(), e.get(0).get(0).literal))
                    .collect(Collectors.toList());
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return List.of();
        } catch (ParserError.NoCatchParseError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return List.of();
        }
    }

}
