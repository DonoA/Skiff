package io.dallen.parser;

import io.dallen.ast.AST;
import io.dallen.ast.ASTEnums;
import io.dallen.ast.ASTOptional;
import io.dallen.parser.splitter.BraceSplitter;
import io.dallen.tokenizer.Token;
import io.dallen.tokenizer.Token.Keyword;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class BlockParser {

//    private ArrayList<AST.Statement> statements = new ArrayList<>();

    private static final AdvancedSwitch<Token.TokenType, AST.Statement, Parser> blockSwitcher =
            new AdvancedSwitch<Token.TokenType, AST.Statement, Parser>()
            .addCase(Keyword.WHILE::equals, BlockParser::parseWhileBlock)
            .addCase(Keyword.SWITCH::equals, BlockParser::parseSwitchBlock)
            .addCase(Keyword.MATCH::equals, BlockParser::parseMatchBlock)
            .addCase(Keyword.CASE::equals, BlockParser::parseCases)
            .addCase(Keyword.LOOP::equals, BlockParser::parseLoopBlock)
            .addCase(Keyword.FOR::equals, BlockParser::parseForBlock)
            .addCase(Keyword.IF::equals, BlockParser::parseIfBlock)
            .addCase(Keyword.ELSE::equals, BlockParser::parseElse)
            .addCase(Keyword.DEF::equals, BlockParser::parseFunctionDef)
            .addCase(Keyword.CLASS::equals, BlockParser::parseClassDef)
            .addCase(Keyword.STRUCT::equals, BlockParser::parseClassDef)
            .addCase(Keyword.PRIVATE::equals, declarationModifierFor(Keyword.PRIVATE, ASTEnums.DecModType.PRIVATE))
            .addCase(Keyword.STATIC::equals, declarationModifierFor(Keyword.STATIC, ASTEnums.DecModType.STATIC))
            .addCase(Keyword.NATIVE::equals, declarationModifierFor(Keyword.NATIVE, ASTEnums.DecModType.NATIVE))
            .addCase(Keyword.RETURN::equals, BlockParser::parseReturn)
            .addCase(Keyword.IMPORT::equals, BlockParser::parseImport)
            .addCase(Keyword.THROW::equals, BlockParser::parseThrow)
            .addCase(Keyword.TRY::equals, BlockParser::parseTryBlock)
            .addCase(Keyword.CATCH::equals, BlockParser::parseCatch)
            .setDefault(Parser::parseExpression);

    static List<AST.Statement> parseAll(Parser parser) {
        List<AST.Statement> statements = new ArrayList<>();

        while (parser.current().type != Token.Textless.EOF) {
            Token.TokenType i = parser.current().type;
            AST.Statement result = blockSwitcher.execute(i, parser);
            if(result != null) {
                statements.add(result);
            }
        }
        return statements;
    }

    static AST.Statement parseBlock(Parser parser) {
        Token.TokenType i = parser.current().type;
        return blockSwitcher.execute(i, parser);
    }

    private static AST.ClassDef parseClassDef(Parser parser) {
        int startPos = parser.absolutePos();
        boolean isStruct = parser.current().type == Keyword.STRUCT;
        if(isStruct) {
            parser.consumeExpected(Keyword.STRUCT);
        } else {
            parser.consumeExpected(Keyword.CLASS);
        }

        Token name = parser.consumeExpected(Token.Textless.NAME);
        List<AST.GenericType> genericTypes = new ArrayList<>();
        Optional<AST.Type> extended = Optional.empty();
        if(parser.current().type == Token.Symbol.LEFT_ANGLE) {
            genericTypes = CommonParsing.consumeGenericList(parser);
        }

        if(parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            Parser typeParser = parser.subParserTo(Token.Symbol.LEFT_BRACE);
            extended = Optional.ofNullable(CommonParsing.parseType(typeParser));
        } else {
            parser.consumeExpected(Token.Symbol.LEFT_BRACE);
        }

        List<AST.Statement> body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE));

        return new AST.ClassDef(name.literal, genericTypes, isStruct, new ArrayList<>(), extended, body, startPos,
                parser.absolutePos());
    }

    private static AST.CatchBlock parseCatch(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Token.Keyword.CATCH);

        AST.FunctionParam cond = CommonParsing.parseFunctionDecArgs(parser.subParserParens()).get(0);

        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<AST.Statement> body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE));

        return new AST.CatchBlock(cond, body, startPos, parser.absolutePos());
    }

    private static AST.ElseBlock parseElse(Parser parser) {
        int startPos = parser.absolutePos();

        parser.consumeExpected(Token.Keyword.ELSE);
        if(parser.current().type == Token.Keyword.IF) {
            AST.IfBlock on = BlockParser.parseIfBlock(parser);
            int stopPos = parser.absolutePos();
            ASTOptional<AST.ElseBlock> elseBlock = ASTOptional.empty();
            if(parser.current().type == Keyword.ELSE) {
                elseBlock = ASTOptional.of(parseElse(parser));
            }
            return new AST.ElseIfBlock(on, elseBlock, startPos, stopPos);
        } else {
            parser.consumeExpected(Token.Symbol.LEFT_BRACE);
            List<AST.Statement> body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE));
            return new AST.ElseAlwaysBlock(body, startPos, parser.absolutePos());
        }
    }

    private static AST.ForBlock parseForBlock(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Token.Keyword.FOR);

        List<Parser> seg;
        try {
            seg = BraceSplitter.splitAll(parser.subParserParens(), Token.Symbol.SEMICOLON);
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return null;
        }

        AST.Statement initStmt = seg.get(0).parseExpression();
        AST.Statement condStmt = seg.get(1).parseExpression();
        AST.Statement stepStmt = seg.get(2).parseExpression();

        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<AST.Statement> body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE));

        return new AST.ForBlock(initStmt, condStmt, stepStmt, body, startPos, parser.absolutePos());
    }

    private static AST.SwitchBlock parseSwitchBlock(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Token.Keyword.SWITCH);

        AST.Statement cond = parser.subParserParens().parseExpression();

        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<AST.Statement> body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE));

        return new AST.SwitchBlock(cond, body, startPos, parser.absolutePos());
    }

    private static AST.MatchBlock parseMatchBlock(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Token.Keyword.MATCH);

        AST.Statement cond = parser.subParserParens().parseExpression();

        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<AST.Statement> body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE).inMatch());

        return new AST.MatchBlock(cond, body, startPos, parser.absolutePos());
    }

    private static AST.Statement parseCases(Parser parser) {
        if(parser.isInMatch()) {
            return BlockParser.parseMatchCase(parser);
        } else {
            return BlockParser.parseCase(parser);
        }
    }

    private static AST.CaseStatement parseCase(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Token.Keyword.CASE);

        AST.Statement on = parser.subParserTo(Token.Symbol.FAT_ARROW).parseExpression();

        return new AST.CaseStatement(on, startPos, parser.absolutePos());
    }

    private static AST.CaseMatchStatement parseMatchCase(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Token.Keyword.CASE);

        AST.Statement on = parser.subParserTo(Token.Symbol.FAT_ARROW).parseExpression();

        return new AST.CaseMatchStatement(on, startPos, parser.absolutePos());
    }

    private static AST.IfBlock parseIfBlock(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Token.Keyword.IF);

        AST.Statement cond = parser.subParserParens().parseExpression();

        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<AST.Statement> body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE));

        int stopPos = parser.absolutePos();
        ASTOptional<AST.ElseBlock> elseBlock = ASTOptional.empty();
        if(parser.current().type == Keyword.ELSE) {
            elseBlock = ASTOptional.of(parseElse(parser));
        }

        return new AST.IfBlock(cond, elseBlock, body, startPos, stopPos);
    }

    private static AST.WhileBlock parseWhileBlock(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Keyword.WHILE);

        AST.Statement cond = parser.subParserParens().parseExpression();

        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<AST.Statement> body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE));

        return new AST.WhileBlock(cond, body, startPos, parser.absolutePos());
    }

    private static AST.TryBlock parseTryBlock(Parser parser) {
        int startPos = parser.absolutePos();

        parser.consumeExpected(Keyword.TRY);
        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<AST.Statement> body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE));
        int stopPos = parser.absolutePos();
        AST.CatchBlock catchBlock = BlockParser.parseCatch(parser);

        return new AST.TryBlock(catchBlock, body, startPos, stopPos);
    }

    private static AST.LoopBlock parseLoopBlock(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Keyword.LOOP);
        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<AST.Statement> body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE));

        return new AST.LoopBlock(body, startPos, parser.absolutePos());
    }

    private static AdvancedSwitch.CaseHandler<Parser, AST.Statement> declarationModifierFor(Token.TokenType expected,
                                                                                          ASTEnums.DecModType type) {
        return (parser) -> {
            Token t = parser.consumeExpected(expected);
            if(parser.current().type == Keyword.IMPORT) {
                return BlockParser.parseNativeImport(parser, true);
            }
            if(t.type == Keyword.PRIVATE) {
                int i = 0;
            }
            AST.Statement on = BlockParser.parseBlock(parser);
            if(on instanceof AST.Declare) {
                ((AST.Declare) on).modifiers.add(type);
            } else if(on instanceof AST.FunctionDef) {
                ((AST.FunctionDef) on).modifiers.add(type);
            } else if(on instanceof AST.ClassDef) {
                ((AST.ClassDef) on).modifiers.add(type);
            } else {
                parser.throwError("Modifier " + type.getRawOp() + " used on bad thing", t);
                return null;
            }
            return on;
        };
    }

    private static AST.FunctionDef parseFunctionDef(Parser parser) {
        int startPos = parser.absolutePos();

        boolean isAbstract = parser.containsBefore(Token.Symbol.SEMICOLON, Token.Symbol.LEFT_BRACE);
        parser.consumeExpected(Token.Keyword.DEF);

        List<AST.GenericType> genericTypes = new ArrayList<>();
        String funcName = parser.consumeExpected(Token.Textless.NAME).literal;
        if(parser.current().type == Token.Symbol.LEFT_ANGLE) {
            genericTypes = CommonParsing.consumeGenericList(parser);
        }

        parser.consumeExpected(Token.Symbol.LEFT_PAREN);

        List<AST.FunctionParam> params;
        try {
            params = CommonParsing.parseFunctionDecArgs(parser.subParserTo(Token.Symbol.RIGHT_PAREN));
        } catch (IndexOutOfBoundsException ex) {
            parser.throwError("Catch statement requires Try, none found", parser.current());
            return null;
        }

        AST.Type returnType = CommonParsing.voidFor(parser.current());

        Token.TokenType endToken = isAbstract ? Token.Symbol.SEMICOLON : Token.Symbol.LEFT_BRACE;

        if(parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            returnType = CommonParsing.parseType(parser.subParserTo(endToken));
        } else {
            parser.consumeExpected(endToken);
        }

        List<AST.Statement> body;
        if (isAbstract) {
            body = List.of();
        } else {
            body = BlockParser.parseAll(parser.subParserTo(Token.Symbol.RIGHT_BRACE));
        }

        return new AST.FunctionDef(genericTypes, returnType, funcName, new ArrayList<>(), params, body, startPos,
                parser.absolutePos());
    }

    private static AST.Return parseReturn(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Token.Keyword.RETURN);

        ASTOptional<AST.Statement> value = ASTOptional.empty();
        if(parser.current().type != Token.Symbol.SEMICOLON) {
            value = ASTOptional.of(parser.subParserTo(Token.Symbol.SEMICOLON).parseExpression());
        }
        return new AST.Return(value, startPos, parser.absolutePos());
    }

    private static AST.ImportStatement parseNativeImport(Parser parser, boolean ntv) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Token.Keyword.IMPORT);

        String location = parser.consumeExpected(Token.Textless.STRING_LITERAL).literal;
        parser.consumeExpected(Token.Symbol.SEMICOLON);
        return new AST.ImportStatement(ntv ? ASTEnums.ImportType.NATIVE : ASTEnums.ImportType.NORMAL, location,
                startPos, parser.absolutePos());
    }

    private static AST.ImportStatement parseImport(Parser parser) {
        return parseNativeImport(parser, false);
    }

    private static AST.ThrowStatement parseThrow(Parser parser) {
        int startPos = parser.absolutePos();
        parser.consumeExpected(Token.Keyword.THROW);

        AST.Statement value = parser.subParserTo(Token.Symbol.SEMICOLON).parseExpression();
        return new AST.ThrowStatement(value, startPos, parser.absolutePos());
    }
}
