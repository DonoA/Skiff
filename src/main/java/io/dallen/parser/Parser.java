package io.dallen.parser;

import io.dallen.compiler.CompileError;
import io.dallen.parser.splitter.BraceSplitter;
import io.dallen.parser.splitter.LayeredSplitter;
import io.dallen.parser.splitter.SplitLayer;
import io.dallen.parser.splitter.SplitSettings;
import io.dallen.tokenizer.Token;
import io.dallen.AST.*;

import io.dallen.tokenizer.Token.Keyword;
import io.dallen.tokenizer.Token.Symbol;
import io.dallen.tokenizer.Token.Textless;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Parser {

    private List<Token> tokens;

    private int pos;

    private final boolean inMatch;

    // defines a multipass split. When a successful split is made, the resulting action will be executed on the result
    private static final SplitSettings splitSettings = new SplitSettings()
            .addLayer(new SplitLayer()
                .addSplitRule(Symbol.EQUAL, ExpressionParser::parseAssignment))
            .addLayer(new SplitLayer()
                    .addSplitRule(Symbol.PLUS_EQUAL, ExpressionParser.mathAssignAction(MathOp.PLUS))
                    .addSplitRule(Symbol.MINUS_EQUAL, ExpressionParser.mathAssignAction(MathOp.MINUS)))
            .addLayer(new SplitLayer()
                .addSplitRule(Symbol.DOUBLE_AND, ExpressionParser.boolCombineAction(BoolOp.AND)))
            .addLayer(new SplitLayer()
                .addSplitRule(Symbol.DOUBLE_OR, ExpressionParser.boolCombineAction(BoolOp.OR)))
            .addLayer(new SplitLayer()
                .addSplitRule(Symbol.DOUBLE_EQUAL, ExpressionParser.compareAction(CompareOp.EQ))
                .addSplitRule(Symbol.LEFT_ANGLE, ExpressionParser.compareAction(CompareOp.LT))
                .addSplitRule(Symbol.RIGHT_ANGLE, ExpressionParser.compareAction(CompareOp.GT)))
            .addLayer(new SplitLayer()
                .addSplitRule(Symbol.SLASH, ExpressionParser.mathAction(MathOp.DIV))
                .addSplitRule(Symbol.STAR, ExpressionParser.mathAction(MathOp.MUL)))
            .addLayer(new SplitLayer()
                .addSplitRule(Symbol.PLUS, ExpressionParser.mathAction(MathOp.PLUS))
                .addSplitRule(Symbol.MINUS, ExpressionParser.mathAction(MathOp.MINUS)))
            .addLayer(new SplitLayer()
                .addSplitRule(Symbol.DOT, ExpressionParser.statementAction(Dotted::new))).leftToRight(false);

    public Parser(List<Token> tokens) {
        this(tokens, false);
    }

    public Parser(List<Token> tokens, boolean inMatch) {
        this.tokens = tokens;
        this.inMatch = inMatch;
    }

    Token current() {
        if (pos >= tokens.size()) {
            return Token.EOF;
        }

        return tokens.get(pos);
    }

    Token consume() {
        Token tok = current();
        next();
        return tok;
    }

    Token consumeExpected(Token.TokenType type) {
        Token t;
        if ((t = consume()).type != type) {
            throw new ParserError("Parse error Expected: " + type.toString(), t);
        }
        return t;
    }

    Token tryConsumeExpected(Token.TokenType type) {
        Token t = current();
        if (current().type == type) {
            consume();
        } else {
            return null;
        }
        return t;
    }

    void next() {
        pos++;
    }

    Token peek() {
        if (pos + 1 >= tokens.size()) {
            return Token.EOF;
        }

        return tokens.get(pos + 1);
    }

    List<Token> consumeTo(Token.TokenType type) {
        return consumeTo(type, BraceManager.leftToRight);
    }

    List<Token> consumeTo(Token.TokenType type, BraceManager.BraceProfile braces) {
        List<Token> tokens = new ArrayList<>();
        BraceManager braceManager = new BraceManager(braces);
        while (true) {
            if (current().type == Textless.EOF) {
                if (braceManager.isEmpty()) {
                    break;
                }
                throw new ParserError("Parse error", current());
            }
            if (current().type == type && braceManager.isEmpty()) {
                break;
            }
            braceManager.check(current());
            tokens.add(current());
            next();
        }
        next();
        return tokens;
    }

    // Just selects the tokens, does not advance the current location
    List<Token> selectTo(Token.TokenType type) {
        List<Token> selected = new ArrayList<>();
        int loc = pos;
        while (loc < tokens.size() && tokens.get(loc).type != type) {
            selected.add(tokens.get(loc));
            loc++;
        }
        return selected;
    }

    // Check if one token comes before another
    boolean containsBefore(Token.TokenType what, Token.TokenType before) {
        for (int lpos = pos; lpos < tokens.size(); lpos++) {
            if (tokens.get(lpos).type == what) {
                return true;
            }
            if (tokens.get(lpos).type == before) {
                return false;
            }
        }
        return before == Textless.EOF;
    }

    public List<Statement> parseBlock() {
        ArrayList<Statement> statements = new ArrayList<>();

        while (!current().type.equals(Textless.EOF)) {
            Token.TokenType i = current().type;
            if (Keyword.WHILE.equals(i)) {
                statements.add(parseWhileBlock());
                continue;
            }
            if (Keyword.SWITCH.equals(i)) {
                statements.add(parseSwitchBlock());
                continue;
            }
            if (Keyword.MATCH.equals(i)) {
                statements.add(parseMatchBlock());
                continue;
            }
            if (Keyword.CASE.equals(i)) {
                if(inMatch) {
                    statements.add(parseMatchCase());
                } else {
                    statements.add(parseCase());
                }
                continue;
            }
            if (Keyword.LOOP.equals(i)) {
                statements.add(parseLoopBlock());
                continue;
            }
            if (Keyword.FOR.equals(i)) {
                statements.add(parseForBlock());
                continue;
            }
            if (Keyword.IF.equals(i)) {
                statements.add(parseIfBlock());
                continue;
            }
            if (Keyword.ELSE.equals(i)) {
                attachElseBlock(statements);
                continue;
            }
            if (Keyword.DEF.equals(i)) {
                statements.add(parseFunctionDef());
                continue;
            }
            if (Keyword.CLASS.equals(i)) {
                statements.add(parseClassDef());
                continue;
            }
            if (Keyword.RETURN.equals(i)) {
                statements.add(parseReturn());
                continue;
            }
            if (Keyword.IMPORT.equals(i)) {
                statements.add(parseImport());
                continue;
            }
            if (Keyword.THROW.equals(i)) {
                statements.add(parseThrow());
                continue;
            }
            if (Keyword.TRY.equals(i)) {
                statements.add(parseTryBlock());
                continue;
            }
            if (Keyword.CATCH.equals(i)) {
                attachCatchBlock(statements);
                continue;
            }
            statements.add(parseExpression());
        }
        return statements;
    }

    private List<GenericType> consumeGenericList() {
        consumeExpected(Symbol.LEFT_ANGLE);
        List<Token> genericTokens = consumeTo(Symbol.RIGHT_ANGLE, BraceManager.leftToRightAngle);
        List<List<Token>> genericTokenSeg = BraceSplitter.splitAll(genericTokens, Symbol.COMMA);
        return genericTokenSeg
                .stream()
                .map(seg -> new Parser(seg).parseGenericType())
                .collect(Collectors.toList());
    }

    private ClassDef parseClassDef() {
        consumeExpected(Keyword.CLASS);
        Token name = consumeExpected(Textless.NAME);
        List<GenericType> genericTypes = new ArrayList<>();
        List<Type> extendList = new ArrayList<>();
        if(current().type == Symbol.LEFT_ANGLE) {
            genericTypes = consumeGenericList();
        }

        if(current().type == Symbol.COLON) {
            consumeExpected(Symbol.COLON);
            List<Token> extendsTokens = consumeTo(Symbol.LEFT_BRACE);
            List<List<Token>> extendsTokenSeg = BraceSplitter.splitAll(extendsTokens, Symbol.COMMA);
            extendList = extendsTokenSeg
                    .stream()
                    .map(seg -> new Parser(seg).parseType())
                    .collect(Collectors.toList());
        }

        tryConsumeExpected(Symbol.LEFT_BRACE);
        List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        return new ClassDef(name.literal, genericTypes, extendList, body);
    }

    private void attachCatchBlock(ArrayList<Statement> statements) {
        if(statements.size() < 1) {
            throw new CompileError("Else statement requires If, none found");
        }
        Statement parentStmt = statements.get(statements.size() - 1);

        consumeExpected(Keyword.CATCH);

        consumeExpected(Symbol.LEFT_PAREN);
        List<Token> condTokens = consumeTo(Symbol.RIGHT_PAREN);
        consumeExpected(Symbol.LEFT_BRACE);
        Statement cond = new Parser(condTokens).parseExpression();

        List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        if(parentStmt instanceof TryBlock) {
            ((TryBlock) parentStmt).catchBlock = new CatchBlock(cond, body);
        } else {
            throw new CompileError("Catch statement requires Try, " + parentStmt.getClass().getName() + " found");
        }
    }

    private void attachElseBlock(ArrayList<Statement> statements) {
        if(statements.size() < 1) {
            throw new CompileError("Else statement requires If, none found");
        }
        Statement parentStmt = statements.get(statements.size() - 1);

        consumeExpected(Keyword.ELSE);
        ElseBlock toAttach;
        if(current().type == Keyword.IF) {
            IfBlock on = parseIfBlock();
            toAttach = new ElseIfBlock(on);
        } else {
            consumeExpected(Symbol.LEFT_BRACE);
            List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
            List<Statement> body = new Parser(bodyTokens).parseBlock();
            toAttach = new ElseAlwaysBlock(body);
        }

        if(parentStmt instanceof IfBlock) {
            ((IfBlock) parentStmt).elseBlock = toAttach;
        } else if(parentStmt instanceof ElseIfBlock) {
            ((ElseIfBlock) parentStmt).elseBlock = toAttach;
        } else {
            throw new CompileError("Else statement requires If, " + parentStmt.getClass().getName() + " found");
        }
    }

    private ForBlock parseForBlock() {
        consumeExpected(Keyword.FOR);

        consumeExpected(Symbol.LEFT_PAREN);
        List<Token> init = consumeTo(Symbol.SEMICOLON);
        List<Token> cond = consumeTo(Symbol.SEMICOLON);
        List<Token> step = consumeTo(Symbol.RIGHT_PAREN);
        consumeExpected(Symbol.LEFT_BRACE);
        Statement initStmt = new Parser(init).parseExpression();
        Statement condStmt = new Parser(cond).parseExpression();
        Statement stepStmt = new Parser(step).parseExpression();

        List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        return new ForBlock(initStmt, condStmt, stepStmt, body);
    }

    private SwitchBlock parseSwitchBlock() {
        consumeExpected(Keyword.SWITCH);

        consumeExpected(Symbol.LEFT_PAREN);
        List<Token> condTokens = consumeTo(Symbol.RIGHT_PAREN);
        consumeExpected(Symbol.LEFT_BRACE);
        Statement cond = new Parser(condTokens).parseExpression();

        List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        return new SwitchBlock(cond, body);
    }

    private MatchBlock parseMatchBlock() {
        consumeExpected(Keyword.MATCH);

        consumeExpected(Symbol.LEFT_PAREN);
        List<Token> condTokens = consumeTo(Symbol.RIGHT_PAREN);
        consumeExpected(Symbol.LEFT_BRACE);
        Statement cond = new Parser(condTokens).parseExpression();

        List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens, true).parseBlock();

        return new MatchBlock(cond, body);
    }

    private CaseStatement parseCase() {
        consumeExpected(Keyword.CASE);

        List<Token> onTokens = consumeTo(Symbol.COLON);
        Statement on = new Parser(onTokens).parseExpression();

        return new CaseStatement(on);
    }

    private CaseMatchStatement parseMatchCase() {
        consumeExpected(Keyword.CASE);

        List<Token> onTokens = consumeTo(Symbol.COLON);
        Type on = new Parser(onTokens).parseType();

        return new CaseMatchStatement(on);
    }

    private IfBlock parseIfBlock() {
        consumeExpected(Keyword.IF);

        consumeExpected(Symbol.LEFT_PAREN);
        List<Token> condTokens = consumeTo(Symbol.RIGHT_PAREN);
        consumeExpected(Symbol.LEFT_BRACE);
        Statement cond = new Parser(condTokens).parseExpression();

        List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        return new IfBlock(cond, body);
    }

    private WhileBlock parseWhileBlock() {
        consumeExpected(Keyword.WHILE);

        consumeExpected(Symbol.LEFT_PAREN);
        List<Token> condTokens = consumeTo(Symbol.RIGHT_PAREN);
        consumeExpected(Symbol.LEFT_BRACE);
        Statement cond = new Parser(condTokens).parseExpression();

        List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        return new WhileBlock(cond, body);
    }

    private TryBlock parseTryBlock() {
        next();
        consumeExpected(Symbol.LEFT_BRACE);

        List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        return new TryBlock(body);
    }

    private LoopBlock parseLoopBlock() {
        next();
        consumeExpected(Symbol.LEFT_BRACE);

        List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        return new LoopBlock(body);
    }

    private FunctionDef parseFunctionDef() {
        consumeExpected(Keyword.DEF);

        List<GenericType> genericTypes = new ArrayList<>();
        String funcName = consume().literal;
        if(current().type == Symbol.LEFT_ANGLE) {
            genericTypes = consumeGenericList();
        }

        consumeExpected(Symbol.LEFT_PAREN);
        List<Token> paramTokens = consumeTo(Symbol.RIGHT_PAREN);

        List<FunctionParam> params;
        try {
             params = BraceSplitter.splitAll(paramTokens, Symbol.COMMA)
                    .stream()
                    .map(e -> BraceSplitter.splitAll(e, Symbol.COLON))
                    .map(e -> new FunctionParam(new Parser(e.get(1)).parseType(), e.get(0).get(0).literal))
                    .collect(Collectors.toList());
        } catch (IndexOutOfBoundsException ex) {
            throw new CompileError("Failed to parse function args for " + funcName);
        }

        Type returnType = Type.VOID;

        if(current().type == Symbol.COLON) {
            consumeExpected(Symbol.COLON);
            List<Token> returnTypeTokens = consumeTo(Symbol.LEFT_BRACE);
            returnType = new Parser(returnTypeTokens).parseType();
        } else {
            consumeExpected(Symbol.LEFT_BRACE);
        }

        List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
        List<Statement> body = new Parser(bodyTokens).parseBlock();

        return new FunctionDef(genericTypes, returnType, funcName, params, body);
    }

    private GenericType parseGenericType() {
        Token name = consumeExpected(Textless.NAME);

        List<Type> subTypes = new ArrayList<>();
        if(current().type == Symbol.COLON) {
            consumeExpected(Symbol.COLON);
            subTypes.add(parseType());
        }

        return new GenericType(name.literal, subTypes);
    }

    private Return parseReturn() {
        consumeExpected(Keyword.RETURN);

        Statement value = new Parser(consumeTo(Symbol.SEMICOLON)).parseExpression();
        return new Return(value);
    }

    private ImportStatement parseImport() {
        consumeExpected(Keyword.IMPORT);

        ImportType typ;
        String location;
        if(current().type == Textless.STRING_LITERAL) {
            location = consume().literal;
            typ = ImportType.LOCAL;
        } else {
            consumeExpected(Symbol.LEFT_ANGLE);
            location = consumeExpected(Textless.NAME).literal;
            consumeExpected(Symbol.RIGHT_ANGLE);
            typ = ImportType.SYSTEM;
        }
        return new ImportStatement(typ, location);
    }

    private ThrowStatement parseThrow() {
        consumeExpected(Keyword.THROW);

        Statement value = new Parser(consumeTo(Symbol.SEMICOLON)).parseExpression();
        return new ThrowStatement(value);
    }

    public Type parseType() {
        Statement typeName = new Parser(consumeTo(Symbol.LEFT_ANGLE)).parseExpression();
        if(current().isEOF()) {
            return new Type(typeName, 0, new ArrayList<>());
        }
        List<Type> genericParams = BraceSplitter
                .customSplitAll(BraceManager.leftToRightAngle, consumeTo(Symbol.RIGHT_ANGLE), Symbol.COMMA)
                .stream()
                .map(e -> new Parser(e).parseType())
                .collect(Collectors.toList());

        return new Type(typeName, 0, genericParams);
    }

    public Statement parseExpression() {
        List<Token> workingTokens = selectTo(Symbol.SEMICOLON);

        Statement parsed = new LayeredSplitter(splitSettings).execute(workingTokens);
        if (parsed != null) {
            pos += workingTokens.size() + 1;
            return parsed;
        }
        if (current().type == Keyword.NEW) {
            consumeExpected(Keyword.NEW);
            List<Token> name = consumeTo(Symbol.LEFT_PAREN);
            List<List<Token>> paramz = BraceSplitter.splitAll(consumeTo(Symbol.RIGHT_PAREN), Symbol.COMMA);
            Type typeStmt = new Parser(name).parseType();
            List<Statement> params = paramz
                .stream()
                .map(e -> new Parser(e).parseExpression())
                .collect(Collectors.toList());
            tryConsumeExpected(Symbol.SEMICOLON);
            return new New(typeStmt, params);
        } else if (current().type == Symbol.LEFT_PAREN) {
            consumeExpected(Symbol.LEFT_PAREN);
            if(!containsBefore(Symbol.ARROW, Symbol.SEMICOLON)){
                Statement sub = new Parser(consumeTo(Symbol.RIGHT_PAREN)).parseExpression();
                return new Parened(sub);
            }
            List<Token> paramTokens = consumeTo(Symbol.RIGHT_PAREN);

            List<FunctionParam> params;
            try {
                params = BraceSplitter.splitAll(paramTokens, Symbol.COMMA)
                        .stream()
                        .map(e -> BraceSplitter.splitAll(e, Symbol.COLON))
                        .map(e -> new FunctionParam(new Parser(e.get(1)).parseType(), e.get(0).get(0).literal))
                        .collect(Collectors.toList());
            } catch (IndexOutOfBoundsException ex) {
                throw new CompileError("Failed to parse function args for anon func");
            }

            Type returns = Type.VOID;
            if(current().type == Symbol.COLON) {
                consumeExpected(Symbol.COLON);
                returns = new Parser(consumeTo(Symbol.ARROW)).parseType();
            }

            consumeExpected(Symbol.LEFT_BRACE);

            List<Token> bodyTokens = consumeTo(Symbol.RIGHT_BRACE);
            List<Statement> body = new Parser(bodyTokens).parseBlock();

            return new AnonFunctionDef(returns, params, body);
        } else if (current().type == Textless.NAME) {
            return handleNameToken(workingTokens);

        } else if (current().type == Symbol.DOUBLE_MINUS) {
            consumeExpected(Symbol.DOUBLE_MINUS);
            Statement sub = new Parser(consumeTo(Symbol.SEMICOLON)).parseExpression();
            tryConsumeExpected(Symbol.SEMICOLON);
            return new MathSelfMod(sub, MathOp.MINUS, SelfModTime.PRE);
        } else if (current().type == Symbol.DOUBLE_PLUS) {
            consumeExpected(Symbol.DOUBLE_PLUS);
            Statement sub = new Parser(consumeTo(Symbol.SEMICOLON)).parseExpression();
            tryConsumeExpected(Symbol.SEMICOLON);
            return new MathSelfMod(sub, MathOp.PLUS, SelfModTime.PRE);

        } else if (current().type == Keyword.TRUE) {
            BooleanLiteral lit = new BooleanLiteral(Boolean.TRUE);
            tryConsumeExpected(Symbol.SEMICOLON);
            return lit;
        } else if (current().type == Keyword.FALSE) {
            BooleanLiteral lit = new BooleanLiteral(Boolean.FALSE);
            tryConsumeExpected(Symbol.SEMICOLON);
            return lit;

        } else if (current().type == Textless.NUMBER_LITERAL) {
            NumberLiteral lit = new NumberLiteral(Double.parseDouble(consume().literal));
            tryConsumeExpected(Symbol.SEMICOLON);
            return lit;
        } else if (current().type == Textless.REGEX_LITERAL) {
            String[] seg = consume().literal.split("\0");
            RegexLiteral lit = new RegexLiteral(seg[0], seg[1]);
            tryConsumeExpected(Symbol.SEMICOLON);
            return lit;
        } else if (current().type == Textless.SEQUENCE_LITERAL) {
            SequenceLiteral lit = new SequenceLiteral(consume().literal);
            tryConsumeExpected(Symbol.SEMICOLON);
            return lit;
        } else if (current().type == Textless.STRING_LITERAL) {
            StringLiteral lit = new StringLiteral(consume().literal);
            tryConsumeExpected(Symbol.SEMICOLON);
            return lit;
        } else if (current().type == Keyword.BREAK) {
            consumeExpected(Keyword.BREAK);
            tryConsumeExpected(Symbol.SEMICOLON);
            return new BreakStatement();
        } else if (current().type == Keyword.NEXT) {
            consumeExpected(Keyword.NEXT);
            tryConsumeExpected(Symbol.SEMICOLON);
            return new ContinueStatement();
        } else if (current().type == Symbol.UNDERSCORE) {
            consumeExpected(Symbol.UNDERSCORE);
            tryConsumeExpected(Symbol.SEMICOLON);
            return new Variable("_");
        } else if (current().type == Textless.STRING_LITERAL) {
            StringLiteral lit = new StringLiteral(consume().literal);
            tryConsumeExpected(Symbol.SEMICOLON);
            return lit;
        } else {
            throw new ParserError("Unknown token sequence", current());
        }

    }

    private Statement handleNameToken(List<Token> workingTokens) {

        if(containsBefore(Symbol.COLON, Symbol.SEMICOLON)) {
            Token name = consume();
            consumeExpected(Symbol.COLON);
            Type type = new Parser(consumeTo(Symbol.SEMICOLON)).parseType();
            return new Declare(type, name.literal);

        } else if(containsBefore(Symbol.LEFT_PAREN, Symbol.SEMICOLON)) {
            List<Token> funcName;
            List<Type> generics = new ArrayList<>();
            if(containsBefore(Symbol.LEFT_ANGLE, Symbol.LEFT_PAREN)) {
                funcName = consumeTo(Symbol.LEFT_ANGLE);
                List<List<Token>> genericTokens = BraceSplitter.customSplitAll(
                        BraceManager.leftToRightAngle, consumeTo(Symbol.RIGHT_ANGLE), Symbol.COMMA);
                generics = genericTokens.stream().map(tokenList -> new Parser(tokenList).parseType())
                        .collect(Collectors.toList());

                consumeExpected(Symbol.LEFT_PAREN);
            } else {
                funcName = consumeTo(Symbol.LEFT_PAREN);
            }

            if(funcName.size() > 1) {
                throw new ParserError("Function call name was multi token", funcName.get(0));
            }
//            Statement parsedName = new Parser(funcName).parseExpression();
            List<Statement> funcParams = consumeFunctionParams();
            tryConsumeExpected(Symbol.SEMICOLON);
            return new FunctionCall(funcName.get(0).literal, funcParams, generics);

        } else if(containsBefore(Symbol.DOUBLE_MINUS, Symbol.SEMICOLON)) {
            List<Token> name = consumeTo(Symbol.DOUBLE_MINUS);
            Statement left = new Parser(name).parseExpression();
            tryConsumeExpected(Symbol.SEMICOLON);
            return new MathSelfMod(left, MathOp.MINUS, SelfModTime.POST);
        } else if(containsBefore(Symbol.DOUBLE_PLUS, Symbol.SEMICOLON)) {
            List<Token> name = consumeTo(Symbol.DOUBLE_PLUS);
            Statement left = new Parser(name).parseExpression();
            tryConsumeExpected(Symbol.SEMICOLON);
            return new MathSelfMod(left, MathOp.PLUS, SelfModTime.POST);
        } else if(containsBefore(Symbol.LEFT_BRACKET, Symbol.SEMICOLON)) {
            List<Token> name = consumeTo(Symbol.LEFT_BRACKET);
            Statement left = new Parser(name).parseExpression();
            List<Token> sub = consumeTo(Symbol.RIGHT_BRACKET);
            Statement inner = new Parser(sub).parseExpression();
            return new Subscript(left, inner);
        } else {
            Token name = consume();
            tryConsumeExpected(Symbol.SEMICOLON);
            return new Variable(name.literal);
        }
    }

    private List<Statement> consumeFunctionParams() {
        List<Token> params = consumeTo(Symbol.RIGHT_PAREN);
        List<List<Token>> paramTokens = BraceSplitter.splitAll(params, Symbol.COMMA);
        return paramTokens.stream()
                .filter(arr -> !arr.isEmpty())
                .map(Parser::new)
                .map(Parser::parseExpression)
                .collect(Collectors.toList());
    }
}


