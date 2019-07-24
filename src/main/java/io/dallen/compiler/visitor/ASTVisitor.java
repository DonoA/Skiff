package io.dallen.compiler.visitor;

import io.dallen.AST.*;

import io.dallen.compiler.*;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class ASTVisitor {

    public static final ASTVisitor instance = new ASTVisitor();

    private ASTVisitor() { }

    public CompiledCode compileStatement(Statement stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Statement");
    }

    public CompiledCode compileExpression(Expression stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Expression");
    }

    public CompiledCode compileType(Type stmt, CompileContext context) {
        CompiledCode typeName = stmt.name.compile(context);
        CompiledType typ = ((CompiledType) typeName.getBinding());
        String name = typ.getCompiledName() + (typ.isRef() ? " *" : "");
        return new CompiledCode()
                .withText(name)
                .withBinding(typeName.getBinding())
                .withType(CompiledType.CLASS);
    }

    public CompiledCode compileGenericType(GenericType stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileBlockStatement(BlockStatement stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Block");
    }

    public CompiledCode compileFunctionDef(FunctionDef stmt, CompileContext context) {
        return FunctionDefCompiler.compileFunctionDef(stmt, context);
    }

    public CompiledCode compileAnonFunctionDef(AnonFunctionDef stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileFunctionParam(FunctionParam stmt, CompileContext context) {
        CompiledCode type = stmt.type.compile(context);
        String sb = type.getCompiledText() + "* formal_" + stmt.name;
        return new CompiledCode()
                .withText(sb)
                .withType((CompiledType) type.getBinding());
    }

    public CompiledCode compileClassDef(ClassDef stmt, CompileContext context) {
        return ClassDefCompiler.compileClassDef(stmt, context);
    }

    public CompiledCode compileIfBlock(IfBlock stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("if(");
        sb.append(stmt.condition.compile(context).getCompiledText());
        sb.append(")\n");
        sb.append(context.getIndent());
        sb.append("{\n");
        CompileContext innerContext = new CompileContext(context).addIndent();
        stmt.body.forEach(VisitorUtils.compileToStringBuilder(sb, innerContext));
        sb.append(context.getIndent());
        sb.append("}");

        if (stmt.elseBlock != null) {
            sb.append("\n");
            sb.append(context.getIndent());
            sb.append(stmt.elseBlock.compile(context).getCompiledText());
        }

        return new CompiledCode()
                .withText(sb.toString())
                .withType(CompiledType.VOID)
                .withSemicolon(false);
    }

    public CompiledCode compileElseBlock(ElseBlock stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type ElseBlock");
    }

    public CompiledCode compileElseIfBlock(ElseIfBlock stmt, CompileContext context) {
        CompiledCode code = stmt.on.compile(context);
        return new CompiledCode()
                .withText("else " + code.getCompiledText())
                .withType(code.getType())
                .withSemicolon(false);
    }

    public CompiledCode compileElseAlwaysBlock(ElseAlwaysBlock stmt, CompileContext context) {
        CompileContext elseInnerContext = new CompileContext(context).addIndent();
        StringBuilder sb = new StringBuilder();
        sb.append("else\n"); // first line does not need indent
        sb.append(context.getIndent());
        sb.append("{\n");
        stmt.body.forEach(VisitorUtils.compileToStringBuilder(sb, elseInnerContext));

        sb.append(context.getIndent());
        sb.append("}");

        return new CompiledCode()
                .withText(sb.toString())
                .withType(CompiledType.VOID)
                .withSemicolon(false);
    }

    public CompiledCode compileWhileBlock(WhileBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileLoopBlock(LoopBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileForBlock(ForBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileForIterBlock(ForIterBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileMatchBlock(MatchBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileSwitchBlock(SwitchBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileCaseStatement(CaseStatement stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileTryBlock(TryBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileCatchBlock(CatchBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileFinallyBlock(FinallyBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileFunctionCall(FunctionCall stmt, CompileContext context) {
        return FunctionCallCompiler.compileFunctionCall(stmt, context);
    }

    public CompiledCode compileParened(Parened stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileDotted(Dotted stmt, CompileContext context) {
        return DottedCompiler.compileDotted(stmt, context);
    }

    public CompiledCode compileArrowed(Arrowed stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileReturn(Return stmt, CompileContext context) {
        CompiledCode code = stmt.value.compile(context);
        StringBuilder sb = new StringBuilder();
        // rtn already created at start of function
        if(stmt.value instanceof NumberLiteral) {
            sb.append("rtn = ");
            sb.append(code.getCompiledText());
        } else {
            sb.append("*rtn = ");
            sb.append(code.isOnStack() ? "*(" : "(").append(code.getCompiledText()).append(")");
        }
        sb.append(";\n\n");
        VisitorUtils.cleanupScope(sb, context);
        sb.append(context.getIndent()).append("return rtn;");
        return new CompiledCode()
                .withText(sb.toString())
                .withSemicolon(false)
                .withType(CompiledType.VOID);
    }

    public CompiledCode compileNew(New stmt, CompileContext context) {
        CompiledType typeCode = (CompiledType) stmt.type.compile(context).getBinding();
        String functionName = CompileUtilities.underscoreJoin("skiff", typeCode.getName(), "new");
        StringBuilder sb = new StringBuilder();
        sb.append(functionName).append("(");
        List<String> argz = stmt.argz
            .stream()
            .map(arg -> arg.compile(context).getCompiledText())
            .collect(Collectors.toList());

        sb.append(String.join(",", argz))
            .append(")");

        return new CompiledCode()
            .withType(typeCode)
            .withText(sb.toString());
    }

    public CompiledCode compileThrowStatement(ThrowStatement stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileImportStatement(ImportStatement stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileMathStatement(MathStatement stmt, CompileContext context) {
        CompiledCode lhs = stmt.left.compile(context);
        CompiledCode rhs = stmt.right.compile(context);

        String lhsText = lhs.getCompiledText();

        if(!lhs.isOnStack()) {
            lhsText = "&(" + lhsText + ")";
        }

        String rhsText = rhs.getCompiledText();

        if(!rhs.isOnStack()) {
            rhsText = "&(" + rhsText + ")";
        }

        String c = "skiff_int_" +  stmt.op.getRawOp() + "(" + lhsText + ", " + rhsText + ")";

        context.addDataStackSize(CompiledType.INT.getSize());

        return new CompiledCode()
                .withText(c)
                .withType(lhs.getType());
    }

    public CompiledCode compileMathAssign(MathAssign stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileMathSelfMod(MathSelfMod stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileSubscript(Subscript stmt, CompileContext context) {
        CompiledCode left = stmt.left.compile(context);
        CompiledObject subscrCall = left.getType().getObject("getSub");
        if(!(subscrCall instanceof CompiledFunction)) {
            throw new CompileError("getSub is not a function");
        }
        CompiledCode sub = stmt.sub.compile(context);

        String cFunc = CompileUtilities.underscoreJoin("skiff", left.getType().getName(), "get", "sub", sub.getType().getName());
        String text = cFunc + "(" + left.getCompiledText() + ", " + sub.getCompiledText() + ")";
        return new CompiledCode()
            .withText(text)
            .withType(((CompiledFunction) subscrCall).getReturns());
    }

    public CompiledCode compileCompare(Compare stmt, CompileContext context) {
        CompiledCode cc = VisitorUtils.compileBinary(stmt.left, stmt.right, stmt.op, context);
        return cc.withType(CompiledType.BOOL);
    }

    public CompiledCode compileBoolCombine(BoolCombine stmt, CompileContext context) {
        CompiledCode cc = VisitorUtils.compileBinary(stmt.left, stmt.right, stmt.op, context);
        return cc.withType(CompiledType.BOOL);
    }

    public CompiledCode compileAssign(Assign stmt, CompileContext context) {
        // Vars with different names must have different stack locations
        CompiledCode name = stmt.name.compile(context);
        CompiledCode value = stmt.value.compile(context);

        boolean lhsDeRef = name.isOnStack();
        boolean rhsDeRef = value.isOnStack();

        String text = (lhsDeRef ? "*" : "") + "(" + name.getCompiledText() + ") = " +
                (rhsDeRef ? "*" : "") + "(" + value.getCompiledText() + ")";
        return new CompiledCode()
            .withText(text)
            .withBinding(name.getBinding())
            .withType(name.getType());
    }

    public CompiledCode compileDeclare(Declare stmt, CompileContext context) {
        CompiledCode type = stmt.type.compile(context);
        CompiledVar binding = new CompiledVar(stmt.name, (CompiledType) type.getBinding());
        context.declareObject(binding);

        String sb = type.getCompiledText() + (context.isOnStack() ? "* " : " ") + stmt.name;
        return new CompiledCode()
                .withBinding(binding)
                .withText(sb)
                .withType(CompiledType.VOID);
    }

    public CompiledCode compileDeclareAssign(DeclareAssign stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileNumberLiteral(NumberLiteral stmt, CompileContext context) {
        context.addDataStackSize(CompiledType.INT.getSize());

        return new CompiledCode()
                .withText("skiff_int_new(" + stmt.value.toString() + ")")
                .withType(CompiledType.INT);
    }

    public CompiledCode compileStringLiteral(StringLiteral stmt, CompileContext context) {
        return new CompiledCode()
                .withText("skiff_string_new(\"" + stmt.value + "\")")
                .withType(context.getType("String"));
    }

    public CompiledCode compileSequenceLiteral(SequenceLiteral stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileBooleanLiteral(BooleanLiteral stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileRegexLiteral(RegexLiteral stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileVariable(Variable stmt, CompileContext context) {
        CompiledObject compiledObject;
        String text = stmt.name;
        boolean onStack = true;
        try {
            compiledObject = context.getObject(stmt.name);
        } catch (NoSuchObjectException ex) {
            if(context.getParentClass() == null) {
                throw ex;
            }
            compiledObject = context.getParentClass().getObject(stmt.name);
            text = "(*this)->" + stmt.name;
            onStack = false;
        }
        CompiledType objType = CompiledType.CLASS;
        if (compiledObject instanceof CompiledVar) {
            objType = ((CompiledVar) compiledObject).getType();
        }
        return new CompiledCode()
                .withText(text)
                .setOnStack(onStack)
                .withBinding(compiledObject)
                .withType(objType);
    }

}
