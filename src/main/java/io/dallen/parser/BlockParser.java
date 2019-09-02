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
            .addCase(Keyword.STRUCT::equals, BlockParser::parseStructDef)
            .addCase(Keyword.PRIVATE::equals, BlockParser::parsePrivate)
            .addCase(Keyword.STATIC::equals, BlockParser::parseStatic)
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

    List<AST.Statement> parseAll() {
        while (!parser.current().type.equals(Token.Textless.EOF)) {
            Token.TokenType i = parser.current().type;
            AST.Statement result = blockSwitcher.execute(i, this);
            if(result != null) {
                statements.add(result);
            }
        }
        return statements;
    }

    AST.Statement parseBlock() {
        Token.TokenType i = parser.current().type;
        return blockSwitcher.execute(i, this);
    }

    private AST.ClassDef parseClassDef() {
        List<Token> allTokens = parser.selectToBlockEnd();
        parser.consumeExpected(Token.Keyword.CLASS);
        Token name = parser.consumeExpected(Token.Textless.NAME);
        List<AST.GenericType> genericTypes = new ArrayList<>();
        Optional<AST.Type> extended = Optional.empty();
        if(parser.current().type == Token.Symbol.LEFT_ANGLE) {
            genericTypes = parser.getCommon().consumeGenericList();
        }

        if(parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            List<Token> extendsTokens = parser.consumeTo(Token.Symbol.LEFT_BRACE);
            extended = Optional.of(new Parser(extendsTokens, parser).getCommon().parseType());
        }

        parser.tryConsumeExpected(Token.Symbol.LEFT_BRACE);
        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser).parseAll();

        return new AST.ClassDef(name.literal, genericTypes, false, extended, body, allTokens);
    }

    private AST.ClassDef parseStructDef() {
        List<Token> allTokens = parser.selectToBlockEnd();
        parser.consumeExpected(Token.Keyword.CLASS);
        Token name = parser.consumeExpected(Token.Textless.NAME);
        List<AST.GenericType> genericTypes = new ArrayList<>();
        Optional<AST.Type> extended = Optional.empty();
        if(parser.current().type == Token.Symbol.LEFT_ANGLE) {
            genericTypes = parser.getCommon().consumeGenericList();
        }

        if(parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            List<Token> extendsTokens = parser.consumeTo(Token.Symbol.LEFT_BRACE);
            extended = Optional.of(new Parser(extendsTokens, parser).getCommon().parseType());
        }

        parser.tryConsumeExpected(Token.Symbol.LEFT_BRACE);
        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser).parseAll();

        return new AST.ClassDef(name.literal, genericTypes, false, extended, body, allTokens);
    }



    private void attachCatchBlock(ArrayList<AST.Statement> statements) {
        List<Token> allTokens = parser.selectToBlockEnd();
        if(statements.size() < 1) {
            parser.throwError("Catch statement requires Try, none found", allTokens.get(0));
            return;
        }
        AST.Statement parentStmt = statements.get(statements.size() - 1);

        parser.consumeExpected(Token.Keyword.CATCH);

        parser.consumeExpected(Token.Symbol.LEFT_PAREN);
        List<Token> condTokens = parser.consumeTo(Token.Symbol.RIGHT_PAREN);
        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        AST.FunctionParam cond = parser.getCommon().parseFunctionDecArgs(condTokens).get(0);

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser).parseAll();

        if(parentStmt instanceof AST.TryBlock) {
            ((AST.TryBlock) parentStmt).catchBlock = new AST.CatchBlock(cond, body, allTokens);
        } else {
            parser.throwError("Catch statement requires Try, " + parentStmt.getClass().getName() + " found",
                    allTokens.get(0));
        }
    }

    private void attachElseBlock(ArrayList<AST.Statement> statements) {
        List<Token> allTokens = parser.selectToBlockEnd();
        if(statements.size() < 1) {
            parser.throwError("Else statement requires If, none found", allTokens.get(0));
            return;
        }
        AST.Statement parentStmt = statements.get(statements.size() - 1);

        Token t = parser.consumeExpected(Token.Keyword.ELSE);
        AST.ElseBlock toAttach;
        if(parser.current().type == Token.Keyword.IF) {
            AST.IfBlock on = parseIfBlock();
            toAttach = new AST.ElseIfBlock(on, allTokens);
        } else {
            parser.consumeExpected(Token.Symbol.LEFT_BRACE);
            List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
            List<AST.Statement> body = new Parser(bodyTokens, parser).parseAll();
            toAttach = new AST.ElseAlwaysBlock(body, allTokens);
        }

        if(parentStmt instanceof AST.IfBlock) {
            AST.IfBlock ifBlock = (AST.IfBlock) parentStmt;
            ifBlock.elseBlock = ASTOptional.of(toAttach);
        } else if(parentStmt instanceof AST.ElseIfBlock) {
            AST.ElseIfBlock elseIfBlock = (AST.ElseIfBlock) parentStmt;
            elseIfBlock.elseBlock = ASTOptional.of(toAttach);
        } else {
            parser.throwError("Else statement requires If, " + parentStmt.getClass().getName() + " found",
                    allTokens.get(0));
        }
    }

    private AST.Statement consumeAndParseParens() {
        return new Parser(consumeParens(), parser).parseExpression();
    }

    private List<Token> consumeParens() {
        parser.consumeExpected(Token.Symbol.LEFT_PAREN);
        List<Token> condTokens = parser.consumeTo(Token.Symbol.RIGHT_PAREN);
        parser.consumeExpected(Token.Symbol.LEFT_BRACE);
        return condTokens;
    }

    private AST.ForBlock parseForBlock() {
        List<Token> allTokens = parser.selectToBlockEnd();
        parser.consumeExpected(Token.Keyword.FOR);

        List<List<Token>> seg;
        try {
            seg = BraceSplitter.splitAll(consumeParens(), Token.Symbol.SEMICOLON);
        } catch (ParserError parserError) {
            parser.throwError(parserError.msg, parserError.on);
            return null;
        }

        AST.Statement initStmt = new Parser(seg.get(0), parser).parseExpression();
        AST.Statement condStmt = new Parser(seg.get(1), parser).parseExpression();
        AST.Statement stepStmt = new Parser(seg.get(2), parser).parseExpression();

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser).parseAll();

        return new AST.ForBlock(initStmt, condStmt, stepStmt, body, allTokens);
    }

    private AST.SwitchBlock parseSwitchBlock() {
        List<Token> allTokens = parser.selectToBlockEnd();
        parser.consumeExpected(Token.Keyword.SWITCH);

        AST.Statement cond = consumeAndParseParens();
        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser).parseAll();

        return new AST.SwitchBlock(cond, body, allTokens);
    }

    private AST.MatchBlock parseMatchBlock() {
        List<Token> allTokens = parser.selectToBlockEnd();
        parser.consumeExpected(Token.Keyword.MATCH);

        AST.Statement cond = consumeAndParseParens();

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser, true).parseAll();

        return new AST.MatchBlock(cond, body, allTokens);
    }

    private AST.CaseStatement parseCase() {
        List<Token> allTokens = parser.selectToEOF();
        parser.consumeExpected(Token.Keyword.CASE);

        List<Token> onTokens = parser.consumeTo(Token.Symbol.ARROW);
        AST.Statement on = new Parser(onTokens, parser).parseExpression();

        return new AST.CaseStatement(on, allTokens);
    }

    private AST.CaseMatchStatement parseMatchCase() {
        List<Token> allTokens = parser.selectToEOF();
        parser.consumeExpected(Token.Keyword.CASE);

        List<Token> onTokens = parser.consumeTo(Token.Symbol.ARROW);
        AST.Statement on = new Parser(onTokens, parser).parseExpression();

        return new AST.CaseMatchStatement(on, allTokens);
    }

    private AST.IfBlock parseIfBlock() {
        List<Token> allTokens = parser.selectToBlockEnd();
        parser.consumeExpected(Token.Keyword.IF);

        AST.Statement cond = consumeAndParseParens();
        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser).parseAll();

        return new AST.IfBlock(cond, body, allTokens);
    }

    private AST.WhileBlock parseWhileBlock() {
        List<Token> allTokens = parser.selectToBlockEnd();
        parser.consumeExpected(Keyword.WHILE);

        AST.Statement cond = consumeAndParseParens();
        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser).parseAll();


        return new AST.WhileBlock(cond, body, allTokens);
    }

    private AST.TryBlock parseTryBlock() {
        List<Token> allTokens = parser.selectToBlockEnd();
        parser.next();
        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser).parseAll();
        return new AST.TryBlock(body, allTokens);
    }

    private AST.LoopBlock parseLoopBlock() {
        List<Token> allTokens = parser.selectToBlockEnd();

        parser.next();
        parser.consumeExpected(Token.Symbol.LEFT_BRACE);

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser).parseAll();
        return new AST.LoopBlock(body, allTokens);
    }

    private AST.Statement parsePrivate() {
        Token t = parser.consumeExpected(Keyword.PRIVATE);
        AST.Statement on = parser.parseBlock();
        if(on instanceof AST.Declare) {
            return new AST.FieldModifier(ASTEnums.DecModType.PRIVATE, (AST.Declare) on, on.tokens);
        } else if(on instanceof AST.FunctionDef) {
            return new AST.FunctionDefModifier(ASTEnums.DecModType.PRIVATE, (AST.FunctionDef) on, on.tokens);
        }
        parser.throwError("Private used on bad thing", t);
        return null;
    }

    private AST.Statement parseStatic() {
        Token t = parser.consumeExpected(Keyword.STATIC);
        AST.Statement on = parser.parseBlock();
        if(on instanceof AST.Declare) {
            return new AST.FieldModifier(ASTEnums.DecModType.STATIC, (AST.Declare) on, on.tokens);
        } else if(on instanceof AST.FunctionDef){
            return new AST.FunctionDefModifier(ASTEnums.DecModType.STATIC, (AST.FunctionDef) on, on.tokens);
        }
        parser.throwError("Private used on bad thing", t);
        return null;
    }

    private AST.FunctionDef parseFunctionDef() {
        List<Token> allTokens = parser.selectToBlockEnd();
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
            parser.throwError("Catch statement requires Try, none found", allTokens.get(0));
            return null;
        }

        AST.Type returnType = Parser.VOID;

        if(parser.current().type == Token.Symbol.COLON) {
            parser.consumeExpected(Token.Symbol.COLON);
            List<Token> returnTypeTokens = parser.consumeTo(Token.Symbol.LEFT_BRACE);
            returnType = new Parser(returnTypeTokens, parser).getCommon().parseType();
        } else {
            parser.consumeExpected(Token.Symbol.LEFT_BRACE);
        }

        List<Token> bodyTokens = parser.consumeTo(Token.Symbol.RIGHT_BRACE);
        List<AST.Statement> body = new Parser(bodyTokens, parser).parseAll();

        return new AST.FunctionDef(genericTypes, returnType, funcName, params, body, allTokens);
    }

    private AST.Return parseReturn() {
        List<Token> tokens = parser.selectTo(Token.Symbol.SEMICOLON);
        parser.consumeExpected(Token.Keyword.RETURN);

        List<Token> valueTokens = parser.consumeTo(Token.Symbol.SEMICOLON);
        AST.Statement value = new Parser(valueTokens, parser).parseExpression();
        return new AST.Return(value, tokens);
    }

    private AST.ImportStatement parseImport() {
        List<Token> tokens = parser.selectToEOF();
        parser.consumeExpected(Token.Keyword.IMPORT);

        ASTEnums.ImportType typ;
        String location;
        if(parser.current().type == Token.Textless.STRING_LITERAL) {
            location = parser.consume().literal;
            typ = ASTEnums.ImportType.LOCAL;
        } else {
            location = parser.consumeExpected(Token.Textless.NAME).literal;
            typ = ASTEnums.ImportType.SYSTEM;
        }
        parser.consumeExpected(Token.Symbol.SEMICOLON);
        return new AST.ImportStatement(typ, location, tokens);
    }

    private AST.ThrowStatement parseThrow() {
        List<Token> allTokens = parser.selectToEOF();
        parser.consumeExpected(Token.Keyword.THROW);

        List<Token> tokens = parser.consumeTo(Token.Symbol.SEMICOLON);
        AST.Statement value = new Parser(tokens, parser).parseExpression();
        return new AST.ThrowStatement(value, allTokens);
    }
}
