package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.ast.AST.*;
import io.dallen.ast.ASTEnums;
import io.dallen.compiler.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class ASTVisitor {

    public static final ASTVisitor instance = new ASTVisitor();

    private ASTVisitor() {
    }

    public CompiledCode compileStatement(Statement stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Statement");
    }

    public CompiledCode compileExpression(Expression stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Expression");
    }

    public CompiledCode compileType(Type stmt, CompileContext context) {

        CompiledCode typeName = stmt.name.compile(context);
        CompiledType typ = ((CompiledType) typeName.getBinding());
        if (typ == null) {
            context.throwError("Type not found", stmt);
            return new CompiledCode()
                    .withBinding(typeName.getBinding())
                    .withType(BuiltinTypes.CLASS);
        }
        String name = typ.getCompiledName();
        return new CompiledCode()
                .withText(name)
                .withBinding(typeName.getBinding())
                .withType(BuiltinTypes.CLASS);
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
        CompiledCode typeCode = stmt.type.compile(context);
        CompiledType type = (CompiledType) typeCode.getBinding();
        String prefix = "";
        if (type.isRef()) {
            prefix = "formal_";
        }
        String sb = typeCode.getCompiledText() + " " + prefix + stmt.name;
        CompiledVar v = new CompiledVar(stmt.name, false, type);
        return new CompiledCode()
                .withText(sb)
                .withType(type)
                .withBinding(v);
    }

    public CompiledCode compileClassDef(ClassDef stmt, CompileContext context) {
        return ClassDefCompiler.compileClassDef(stmt, context);
    }

    public CompiledCode compileIfBlock(IfBlock stmt, CompileContext context) {
        return ConditionBlockCompiler.compileIfBlock(stmt, context);
    }

    public CompiledCode compileElseBlock(ElseBlock stmt, CompileContext context) {
        return new CompiledCode();
    }

    public CompiledCode compileElseIfBlock(ElseIfBlock stmt, CompileContext context) {
        return ConditionBlockCompiler.compileElseIfBlock(stmt, context);
    }

    public CompiledCode compileElseAlwaysBlock(ElseAlwaysBlock stmt, CompileContext context) {
        return ConditionBlockCompiler.compileElseAlwaysBlock(stmt, context);
    }

    public CompiledCode compileWhileBlock(WhileBlock stmt, CompileContext context) {
        return ConditionBlockCompiler.compileWhile(stmt, context);
    }

    public CompiledCode compileLoopBlock(LoopBlock stmt, CompileContext context) {
        CompileContext innerContext = new CompileContext(context);
        innerContext.addIndent();
        StringBuilder sb = new StringBuilder();
        sb.append("while(1)\n").append(context.getIndent()).append("{\n");
        stmt.body.forEach(VisitorUtils.compileToStringBuilder(sb, innerContext));
        VisitorUtils.cleanupScope(sb, innerContext, true);
        sb.append(context.getIndent()).append("}");
        return new CompiledCode()
                .withText(sb.toString())
                .withSemicolon(false);
    }

    public CompiledCode compileForBlock(ForBlock stmt, CompileContext context) {
        return ConditionBlockCompiler.compileFor(stmt, context);
    }

    public CompiledCode compileForIterBlock(ForIterBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileMatchBlock(AST.MatchBlock stmt, CompileContext context) {
        return SwitchMatchCompiler.compileMatchBlock(stmt, context);
    }

    public CompiledCode compileSwitchBlock(AST.SwitchBlock stmt, CompileContext context) {
        return SwitchMatchCompiler.compileSwitchBlock(stmt, context);
    }

    public CompiledCode compileCaseStatement(CaseStatement stmt, CompileContext context) {
        throw new UnsupportedOperationException("Case statement should not be compiled directly");
    }

    public CompiledCode compileCaseMatchStatement(CaseMatchStatement stmt, CompileContext context) {
        throw new UnsupportedOperationException("Match case statement should not be compiled directly");
    }

    public CompiledCode compileBreakStatement(BreakStatement stmt, CompileContext context) {
        return VisitorUtils.compileFlowKeyword("break", context);

    }

    public CompiledCode compileContinueStatement(ContinueStatement stmt, CompileContext context) {
        return VisitorUtils.compileFlowKeyword("continue", context);
    }

    public CompiledCode compileTryBlock(TryBlock stmt, CompileContext context) {
        return TryCatchCompiler.compileTryBlock(stmt, context);
    }

    public CompiledCode compileCatchBlock(CatchBlock stmt, CompileContext context) {
        throw new UnsupportedOperationException("Catch compile should not be called directly!");
    }

    public CompiledCode compileFinallyBlock(FinallyBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileFunctionCall(FunctionCall stmt, CompileContext context) {
        return FunctionCallCompiler.compileFunctionCall(stmt, context);
    }

    public CompiledCode compileParened(Parened stmt, CompileContext context) {
        CompiledCode sub = stmt.sub.compile(context);
        return new CompiledCode()
                .withText("(" + sub.getCompiledText() + ")")
                .withType(sub.getType())
                .withBinding(sub.getBinding());
    }

    public CompiledCode compileDotted(Dotted stmt, CompileContext context) {
        return DottedCompiler.compileDotted(stmt, context);
    }

    public CompiledCode compileReturn(Return stmt, CompileContext context) {
        String valueText = "";
        if (stmt.value.isPresent()) {
            valueText = " " + stmt.value.get().compile(context).getCompiledText();
        }
        StringBuilder sb = new StringBuilder();

        // Calculate how much needs to be deleted from the last function
        VisitorUtils.cleanupScope(sb, context, false);
        sb.append(context.getIndent()).append("return").append(valueText);
        return new CompiledCode()
                .withText(sb.toString())
                .withSemicolon(true);
    }

    public CompiledCode compileNew(AST.New stmt, CompileContext context) {
        return NewCompiler.compileNew(stmt, context);
    }

    public CompiledCode compileThrowStatement(ThrowStatement stmt, CompileContext context) {
        CompiledCode inner = stmt.value.compile(context);

        return new CompiledCode()
                .withText("skiff_throw(" + inner.getCompiledText() + ")");
    }

    public CompiledCode compileImportStatement(AST.ImportStatement stmt, CompileContext context) {
        return ImportCompiler.compileImportStatement(stmt, context);
    }

    public CompiledCode compileMathStatement(MathStatement stmt, CompileContext context) {
        CompiledCode lhs = stmt.left.compile(context);
        CompiledCode rhs = stmt.right.compile(context);

        String text = lhs.getCompiledText() + " " + stmt.op.getSymbol() + " " + rhs.getCompiledText();

        return new CompiledCode()
                .withText(text)
                .withType(lhs.getType());
    }

    public CompiledCode compileMathAssign(MathAssign stmt, CompileContext context) {
        CompiledCode lhs = stmt.left.compile(context);
        CompiledCode rhs = stmt.right.compile(context);

        String text = lhs.getCompiledText() + " = " + lhs.getCompiledText() + " " + stmt.op.getSymbol() + " " +
                rhs.getCompiledText();
        return new CompiledCode()
                .withText(text)
                .withType(lhs.getType());
    }

    public CompiledCode compileMathSelfMod(MathSelfMod stmt, CompileContext context) {
        CompiledCode on = stmt.left.compile(context);
        String op = stmt.op == ASTEnums.MathOp.MINUS ? "--" : "++";
        String text = on.getCompiledText();
        if (stmt.time == ASTEnums.SelfModTime.POST) {
            text = "(" + text + op + ")";
        } else {
            text = "(" + op + text + ")";
        }

        return new CompiledCode()
                .withText(text)
                .withBinding(on.getBinding())
                .withType(on.getType());
    }

    public CompiledCode compileSubscript(Subscript stmt, CompileContext context) {
        CompiledCode left = stmt.left.compile(context);
        CompiledFunction subscrCall = left.getType().getMethod("getSub", List.of(BuiltinTypes.INT));
        CompiledCode sub = stmt.sub.compile(context);
        String name = (left.onStack() ? "(*" : "(") + left.getCompiledText() + ")";

        String text = name + "->class_ptr->" + subscrCall.getName() + "(" + name + ", " + sub.getCompiledText() + ")";
        return new CompiledCode()
                .withText(text)
                .withType(subscrCall.getReturns());
    }

    public CompiledCode compileCompare(Compare stmt, CompileContext context) {
        CompiledCode cc = VisitorUtils.compileBinary(stmt.left, stmt.right, stmt.op, context);
        return cc.withType(BuiltinTypes.BOOL);
    }

    public CompiledCode compileBoolCombine(BoolCombine stmt, CompileContext context) {
        CompiledCode cc = VisitorUtils.compileBinary(stmt.left, stmt.right, stmt.op, context);
        return cc.withType(BuiltinTypes.BOOL);
    }

    public CompiledCode compileAssign(Assign stmt, CompileContext context) {
        return AssignmentCompiler.compileAssign(stmt, context);
    }

    public CompiledCode compileDeclare(Declare stmt, CompileContext context) {
        CompiledCode typeCode = stmt.type.compile(context);
        List<CompiledType> genericTypes = stmt.type.genericTypes
                .stream()
                .map(type -> (CompiledType) type.compile(context).getBinding())
                .collect(Collectors.toList());
        CompiledType type = ((CompiledType) typeCode.getBinding()).fillGenericTypes(genericTypes, false);
        CompiledVar binding = new CompiledVar(stmt.name, false, type);
        context.declareObject(binding);

        boolean isRef = type.isRef() && context.isOnStack();

        if (isRef) {
            context.addRefStackSize(1);
        }

        String text = typeCode.getCompiledText() + (isRef ? "* " : " ") + stmt.name + (isRef ? " = skalloc_ref_stack()" : "");
        return new CompiledCode()
                .withBinding(binding)
                .withText(text);
    }

    public CompiledCode compileDeclareAssign(DeclareAssign stmt, CompileContext context) {
        // TODO: fix this hack
        CompiledCode dec = this.compileDeclare(new Declare(stmt.type, stmt.name, List.of(), stmt.token_start,
                stmt.token_end), context);
        CompiledCode value = this.compileAssign(new Assign(new Variable(stmt.name, stmt.token_start, stmt.token_end),
                stmt.value, stmt.token_start, stmt.token_end), context);

        String text = dec.getCompiledText() + ";\n" + context.getIndent() + value.getCompiledText() + ";";

        return new CompiledCode()
                .withBinding(dec.getBinding())
                .withText(text)
                .withSemicolon(false);
    }

    public CompiledCode compileDeconstructAssign(DeconstructAssign stmt, CompileContext context) {
        return AssignmentCompiler.compileDeconstructionAssign(stmt, context);
    }

    public CompiledCode compileNumberLiteral(NumberLiteral stmt, CompileContext context) {
        boolean decimal = (int) stmt.value != stmt.value;
        String strVal;
        if (decimal) {
            strVal = String.valueOf(stmt.value);
        } else {
            strVal = String.valueOf((int) stmt.value);
        }
        CompiledType typ = decimal ? BuiltinTypes.FLOAT : BuiltinTypes.INT;
        return new CompiledCode()
                .withText(strVal)
                .withType(typ);
    }

    public CompiledCode compileStringLiteral(StringLiteral stmt, CompileContext context) {
        return new CompiledCode()
                .withText("skiff_string_new_0(0, \"" + stmt.value + "\")")
                .withType(BuiltinTypes.STRING);
    }

    public CompiledCode compileSequenceLiteral(SequenceLiteral stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileBooleanLiteral(BooleanLiteral stmt, CompileContext context) {
        return new CompiledCode()
                .withText(stmt.value ? "1" : "0")
                .withType(BuiltinTypes.BOOL);
    }

    public CompiledCode compileRegexLiteral(RegexLiteral stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileVariable(Variable stmt, CompileContext context) {
        CompiledObject compiledObject = null;
        String text = stmt.name;
        boolean onStack = true;
        CompiledType objType = null;
        try {
            compiledObject = context.getObject(stmt.name);
            if (compiledObject instanceof CompiledVar) {
                CompiledVar cv = ((CompiledVar) compiledObject);
                onStack = !cv.isParam();
            }
        } catch (NoSuchElementException ex) {
            if (stmt.name.equals("null")) {
                text = "0";
                compiledObject = CompiledVar.NULL;
                onStack = false;
            } else if (context.getContainingClass() == null) {
                context.throwError("Undefined variable", stmt);
                return new CompiledCode();
            } else {
                try {
                    compiledObject = context.getContainingClass().getField(stmt.name);
                } catch (NoSuchElementException ex2) {
                    context.throwError("Undefined variable", stmt);
                    return new CompiledCode();
                }
                text = "this->" + stmt.name;
                onStack = false;
            }
        }
        objType = BuiltinTypes.CLASS;
        if (compiledObject instanceof CompiledVar) {
            objType = ((CompiledVar) compiledObject).getType();
        }

        return new CompiledCode()
                .withText(text)
                .withBinding(compiledObject)
                .onStack(onStack)
                .withType(objType);
    }

    public CompiledCode compileProgram(Program program, CompileContext context) {
        context.setTokenStream(program.tokens);

        String text = program.body
                .stream()
                .map(stmt -> stmt.compile(context))
                .map(CompiledCode::getCompiledText)
                .collect(Collectors.joining("\n"));

        return new CompiledCode().withText(text);
    }
}
