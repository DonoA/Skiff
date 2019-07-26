package io.dallen.parser;

import io.dallen.AST;
import io.dallen.compiler.CompileError;
import io.dallen.parser.splitter.BraceSplitter;
import io.dallen.tokenizer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.dallen.tokenizer.Token.Keyword;

class BlockParser {

    private Parser parser;

    private ArrayList<AST.Statement> statements = new ArrayList<>();

    private static final AdvancedSwitch<Token.TokenType, AST.Statement, BlockParser> blockSwitcher =
            new AdvancedSwitch<Token.TokenType, AST.Statement, BlockParser>()
            .addCase(Keyword.WHILE::equals, BlockParser::parseWhileBlock)
            .addCase(Keyword.SWITCH::equals, BlockParser::parseSwitchBlock)
            .addCase(Keyword.MATCH::equals, BlockParser::parseMatchBlock)
            .addCase(Keyword.CASE::equals, context -> {
                if(context.parser.isInMatch()) {
                    return context.parseMatchCase();
                } else {
                    return context.parseCase();
                }
            })
            .addCase(Keyword.LOOP::equals, BlockParser::parseLoopBlock)
            .addCase(Keyword.FOR::equals, BlockParser::parseForBlock)
            .addCase(Keyword.IF::equals, BlockParser::parseIfBlock)
            .addCase(Keyword.ELSE::equals, context -> {
                context.attachElseBlock(context.statements);
                return null;
            })
            .addCase(Keyword.DEF::equals, BlockParser::parseFunctionDef)
            .addCase(Keyword.CLASS::equals, BlockParser::parseClassDef)
            .addCase(Keyword.RETURN::equals, BlockParser::parseReturn)
            .addCase(Keyword.IMPORT::equals, BlockParser::parseImport)
            .addCase(Keyword.THROW::equals, BlockParser::parseThrow)
            .addCase(Keyword.TRY::equals, BlockParser::parseTryBlock)
            .addCase(Keyword.CATCH::equals, context -> {
                context.attachCatchBlock(context.statements);
                return null;
            })
            .setDefault(context -> context.parser.parseExpression());


    BlockParser(Parser parser) {
        this.parser = parser;
    }

    List<AST.Statement> parseBlock() {
        while (!parser.current().type.equals(Token.Textless.EOF)) {
            Token.TokenType i = parser.current().type;
            AST.Statement result = blockSwitcher.execute(i, this);
            if(result != null) {
                statements.add(result);
            }
        }
        return statements;
    }

    private AST.ClassDef parseClassDef() {
        parser.consumeExpected(Token.Keyword.CLASS);
        Token name = parser.consumeExpected(Token.Textless.NAME);
        List<AST.GenericType> genericTypes = new ArrayList<>();
        List<AST.Type> extendList = new ArrayList<>();
        if(parser.current().type == Token.Symbol.LEFT_ANGLE) {
            genericTypes = parser.getCommon().consumeGenericList();
        }

        if(parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            List<Token> extendsTokens = parser.consumeTo(Token.Symbol.LEFT_BRACE);
            List<List<Token>> extendsTokenSeg = BraceSplitter.splitAll(extendsTokens, Token.Symbol.COMMA);
            extendList = extendsTokenSeg
                    .stream()
                    .map(seg -> new Parser(seg).getCommon().parseType())
                    .collect(Collectors.toList());
        }

        parser.tryConsumeExpected(Token.Symbol.LEFT_BRACE);
        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens).parseBlock();

        return new AST.ClassDef(name.literal, genericTypes, extendList, body);
    }



    private void attachCatchBlock(ArrayList<AST.Statement> statements) {
        if(statements.size() < 1) {
            throw new CompileError("Else statement requires If, none found");
        }
        AST.Statement parentStmt = statements.get(statements.size() - 1);

        parser.consumeExpected(Token.Keyword.CATCH);

        parser.consumeExpected(Token.Symbol.LEFT_PAREN);
        List<Token> condTokens = parser.consumeTo(Token.Symbol.RIGHT_PAREN);
        parser.consumeExpected(Token.Symbol.LEFT_BRACE);
        AST.Statement cond = new Parser(condTokens).parseExpression();

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens).parseBlock();

        if(parentStmt instanceof AST.TryBlock) {
            ((AST.TryBlock) parentStmt).catchBlock = new AST.CatchBlock(cond, body);
        } else {
            throw new CompileError("Catch statement requires Try, " + parentStmt.getClass().getName() + " found");
        }
    }

    private void attachElseBlock(ArrayList<AST.Statement> statements) {
        if(statements.size() < 1) {
            throw new CompileError("Else statement requires If, none found");
        }
        AST.Statement parentStmt = statements.get(statements.size() - 1);

        parser.consumeExpected(Token.Keyword.ELSE);
        AST.ElseBlock toAttach;
        if(parser.current().type == Token.Keyword.IF) {
            AST.IfBlock on = parseIfBlock();
            toAttach = new AST.ElseIfBlock(on);
        } else {
            parser.consumeExpected(Token.Symbol.LEFT_BRACE);
            List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
            List<AST.Statement> body = new Parser(bodyTokens).parseBlock();
            toAttach = new AST.ElseAlwaysBlock(body);
        }

        if(parentStmt instanceof AST.IfBlock) {
            ((AST.IfBlock) parentStmt).elseBlock = toAttach;
        } else if(parentStmt instanceof AST.ElseIfBlock) {
            ((AST.ElseIfBlock) parentStmt).elseBlock = toAttach;
        } else {
            throw new CompileError("Else statement requires If, " + parentStmt.getClass().getName() + " found");
        }
    }

    private AST.Statement consumeAndParseParens() {
        return new Parser(consumeParens()).parseExpression();
    }

    private List<Token> consumeParens() {
        parser.consumeExpected(Token.Symbol.LEFT_PAREN);
        List<Token> condTokens = parser.consumeTo(Token.Symbol.RIGHT_PAREN);
        parser.consumeExpected(Token.Symbol.LEFT_BRACE);
        return condTokens;
    }

    private List<AST.Statement> consumeAndParseBody() {
        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        return new Parser(bodyTokens).parseBlock();
    }

    private AST.ForBlock parseForBlock() {
        parser.consumeExpected(Token.Keyword.FOR);

        List<List<Token>> seg = BraceSplitter.splitAll(consumeParens(), Token.Symbol.SEMICOLON);

        AST.Statement initStmt = new Parser(seg.get(0)).parseExpression();
        AST.Statement condStmt = new Parser(seg.get(1)).parseExpression();
        AST.Statement stepStmt = new Parser(seg.get(2)).parseExpression();

        List<AST.Statement> body = consumeAndParseBody();

        return new AST.ForBlock(initStmt, condStmt, stepStmt, body);
    }

    private AST.SwitchBlock parseSwitchBlock() {
        parser.consumeExpected(Token.Keyword.SWITCH);

        AST.Statement cond = consumeAndParseParens();
        List<AST.Statement> body = consumeAndParseBody();

        return new AST.SwitchBlock(cond, body);
    }

    private AST.MatchBlock parseMatchBlock() {
        parser.consumeExpected(Token.Keyword.MATCH);

        AST.Statement cond = consumeAndParseParens();

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, true).parseBlock();

        return new AST.MatchBlock(cond, body);
    }

    private AST.CaseStatement parseCase() {
        parser.consumeExpected(Token.Keyword.CASE);

        List<Token> onTokens = parser.consumeTo(Token.Symbol.COLON);
        AST.Statement on = new Parser(onTokens).parseExpression();

        return new AST.CaseStatement(on);
    }

    private AST.CaseMatchStatement parseMatchCase() {
        parser.consumeExpected(Token.Keyword.CASE);

        List<Token> onTokens = parser.consumeTo(Token.Symbol.COLON);
        AST.Type on = new Parser(onTokens).getCommon().parseType();

        return new AST.CaseMatchStatement(on);
    }

    private AST.IfBlock parseIfBlock() {
        parser.consumeExpected(Token.Keyword.IF);

        AST.Statement cond = consumeAndParseParens();
        List<AST.Statement> body = consumeAndParseBody();

        return new AST.IfBlock(cond, body);
    }

    private AST.WhileBlock parseWhileBlock() {
        parser.consumeExpected(Keyword.WHILE);

        AST.Statement cond = consumeAndParseParens();
        List<AST.Statement> body = consumeAndParseBody();

        return new AST.WhileBlock(cond, body);
    }

    private List<AST.Statement> parseSimpleBlock() {
        parser.next();
        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        return new Parser(bodyTokens).parseBlock();
    }

    private AST.TryBlock parseTryBlock() {
        return new AST.TryBlock(parseSimpleBlock());
    }

    private AST.LoopBlock parseLoopBlock() {
        return new AST.LoopBlock(parseSimpleBlock());
    }

    private AST.FunctionDef parseFunctionDef() {
        parser.consumeExpected(Token.Keyword.DEF);

        List<AST.GenericType> genericTypes = new ArrayList<>();
        String funcName = parser.consume().literal;
        if(parser.current().type == Token.Symbol.LEFT_ANGLE) {
            genericTypes = parser.getCommon().consumeGenericList();
        }

        parser.consumeExpected(Token.Symbol.LEFT_PAREN);
        List<Token> paramTokens = parser.consumeTo(Token.Symbol.RIGHT_PAREN);

        List<AST.FunctionParam> params;
        try {
            params = parser.getCommon().parseFunctionDecArgs(paramTokens);
        } catch (IndexOutOfBoundsException ex) {
            throw new CompileError("Failed to parse function args for " + funcName);
        }

        AST.Type returnType = AST.Type.VOID;

        if(parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            List<Token> returnTypeTokens = parser.consumeTo(Token.Symbol.LEFT_BRACE);
            returnType = new Parser(returnTypeTokens).getCommon().parseType();
        } else {
            parser.consumeExpected(Token.Symbol.LEFT_BRACE);
        }

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens).parseBlock();

        return new AST.FunctionDef(genericTypes, returnType, funcName, params, body);
    }

    private AST.Return parseReturn() {
        parser.consumeExpected(Token.Keyword.RETURN);

        AST.Statement value = new Parser(parser.consumeTo(Token.Symbol.SEMICOLON)).parseExpression();
        return new AST.Return(value);
    }

    private AST.ImportStatement parseImport() {
        parser.consumeExpected(Token.Keyword.IMPORT);

        AST.ImportType typ;
        String location;
        if(parser.current().type == Token.Textless.STRING_LITERAL) {
            location = parser.consume().literal;
            typ = AST.ImportType.LOCAL;
        } else {
            parser.consumeExpected(Token.Symbol.LEFT_ANGLE);
            location = parser.consumeExpected(Token.Textless.NAME).literal;
            parser.consumeExpected(Token.Symbol.RIGHT_ANGLE);
            typ = AST.ImportType.SYSTEM;
        }
        return new AST.ImportStatement(typ, location);
    }

    private AST.ThrowStatement parseThrow() {
        parser.consumeExpected(Token.Keyword.THROW);

        AST.Statement value = new Parser(parser.consumeTo(Token.Symbol.SEMICOLON)).parseExpression();
        return new AST.ThrowStatement(value);
    }
}
