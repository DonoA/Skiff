package io.dallen.parser;

import io.dallen.ast.AST;
import io.dallen.parser.splitter.BraceSplitter;
import io.dallen.tokenizer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class CommonParsing {
    // Used for generating void tokens when they are needed for error handling
    static AST.Type voidFor(Token t) {
        return new AST.Type(
                new AST.Variable(
                        "Void",
                        0, 0
                ),
                List.of(), 0, 0);
    }

    private Parser parser;

    CommonParsing(Parser parser) {
        this.parser = parser;
    }

    static List<AST.GenericType> consumeGenericList(Parser parser) {
        parser.consumeExpected(Token.Symbol.LEFT_ANGLE);
        Parser genericParser = parser.subParserTo(Token.Symbol.RIGHT_ANGLE, BraceManager.leftToRightAngle);
//        List<Token> genericTokens = parser.consumeTo(Token.Symbol.RIGHT_ANGLE, BraceManager.leftToRightAngle);
        List<Parser> genericTokenSeg;
        try {
            genericTokenSeg = BraceSplitter.splitAll(genericParser, Token.Symbol.COMMA);
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return List.of();
        }
        return genericTokenSeg
                .stream()
                .map(CommonParsing::parseGenericType)
                .collect(Collectors.toList());
    }

    static AST.GenericType parseGenericType(Parser parser) {
        Token name = parser.consumeExpected(Token.Textless.NAME);

        List<AST.Type> subTypes = new ArrayList<>();
        if (parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            subTypes.add(CommonParsing.parseType(parser));
        }

        return new AST.GenericType(name.literal, subTypes);
    }

    static AST.Type parseType(Parser parser) {
        AST.Statement typeName = parser.subParserTo(Token.Symbol.LEFT_ANGLE).parseExpression();
        if (parser.current().isEOF()) {
            return new AST.Type(typeName, List.of(), parser.absoluteStart(), parser.absoluteStop());
        }
        List<AST.Type> genericParams;
        try {
            genericParams = BraceSplitter
                    .customSplitAll(BraceManager.leftToRightAngle, parser.subParserTo(Token.Symbol.RIGHT_ANGLE), Token.Symbol.COMMA)
                    .stream()
                    .map(CommonParsing::parseType)
                    .collect(Collectors.toList());
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return null;
        }

        return new AST.Type(typeName, genericParams, parser.absoluteStart(), parser.absoluteStop());
    }

    static List<AST.FunctionParam> parseFunctionDecArgs(Parser parser) {
        try {
            return BraceSplitter.splitAll(parser, Token.Symbol.COMMA)
                    .stream()
                    .map(p -> {
                        Parser type = p.subParserTo(p.tokenCount() - 1);
                        AST.Type argType = CommonParsing.parseType(type);
                        Token name = p.consumeExpected(Token.Textless.NAME);
                        return new AST.FunctionParam(argType, name.literal);
                    })
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
