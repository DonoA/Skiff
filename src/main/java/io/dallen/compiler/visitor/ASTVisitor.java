package io.dallen.compiler.visitor;

import io.dallen.AST.*;
import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.List;
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
        String name = typ.getCompiledName();
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
        String sb = type.getCompiledText() + " " + stmt.name;
        return new CompiledCode()
                .withText(sb)
                .withType((CompiledType) type.getBinding());
    }

    public CompiledCode compileClassDef(ClassDef stmt, CompileContext context) {
        return ClassDefCompiler.compileClassDef(stmt, context);
    }

    public CompiledCode compileIfBlock(IfBlock stmt, CompileContext context) {
        return ConditionBlockCompiler.compileIfBlock(stmt, context);
    }

    public CompiledCode compileElseBlock(ElseBlock stmt, CompileContext context) {
        return new CompiledCode().withText("");
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
        return ConditionBlockCompiler.compileWhile(stmt, context);
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

    public CompiledCode compileCaseMatchStatement(CaseMatchStatement stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileBreakStatement(BreakStatement stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileContinueStatement(ContinueStatement stmt, CompileContext context) {
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

        VisitorUtils.cleanupScope(sb, context);
        sb.append(context.getIndent()).append("return ").append(code.getCompiledText());
        return new CompiledCode()
                .withText(sb.toString())
                .withSemicolon(true)
                .withType(CompiledType.VOID);
    }

    public CompiledCode compileNew(New stmt, CompileContext context) {
        CompiledType typeCode = (CompiledType) stmt.type.compile(context).getBinding();
        List<CompiledType> genericTypes = stmt.type.genericTypes
                .stream()
                .map(type -> (CompiledType) type.compile(context).getBinding())
                .collect(Collectors.toList());
        String functionName = VisitorUtils.underscoreJoin("skiff", typeCode.getName(), "new");
        String allocateNewInstace = "(" + typeCode.getCompiledName() +
                ") skalloc(1, sizeof(" + typeCode.getStructName() + "))";
        StringBuilder sb = new StringBuilder();
        sb.append(functionName).append("(");
        List<String> argz = new ArrayList<>();
        argz.add(allocateNewInstace);
        argz.add("1");
        argz.addAll(stmt.argz
            .stream()
            .map(arg -> arg.compile(context).getCompiledText())
            .collect(Collectors.toList()));

        sb.append(String.join(", ", argz))
            .append(")");

        CompiledType exactType = typeCode.fillGenericTypes(genericTypes);

        return new CompiledCode()
            .withType(exactType)
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

        String text = lhs.getCompiledText() + " " + stmt.op.getSymbol() + " " + rhs.getCompiledText();

        return new CompiledCode()
                .withText(text)
                .withType(lhs.getType());
    }

    public CompiledCode compileMathAssign(MathAssign stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileMathSelfMod(MathSelfMod stmt, CompileContext context) {
//        CompiledCode on = stmt.left.compile(context);
//        String onText;
//        if(on.getType().isRef()) {
//            onText = "(**" + on.getCompiledText() + ")";
//        } else {
//            onText = "(*" + on.getCompiledText() + ")";
//        }
//
//        String op = stmt.op == MathOp.MINUS ? "--" : "++";
//        String text;
//        if(stmt.time == SelfModTime.POST) {
//            text = onText + op;
//        } else {
//            text = op + onText;
//        }
//
//        // TODO: make the return from this block work
//        return new CompiledCode()
//                .withText(text)
//                .withBinding(on.getBinding())
//                .withType(on.getType());
        return null;
    }

    public CompiledCode compileSubscript(Subscript stmt, CompileContext context) {
        CompiledCode left = stmt.left.compile(context);
        CompiledFunction subscrCall = left.getType().getMethod("getSub");
        CompiledCode sub = stmt.sub.compile(context);
        String name = (left.onStack() ? "(*" : "(") + left.getCompiledText() + ")";

        String text = name + "->class_ptr->" + subscrCall.getName() + "(" + name + ", " + sub.getCompiledText() +")";
        return new CompiledCode()
            .withText(text)
            .withType(subscrCall.getReturns());
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
        // Each stack location should represent exactly one named var
        CompiledCode name = stmt.name.compile(context);
        CompiledCode value = stmt.value.compile(context);

        boolean lhsDeRef = name.onStack();
        if(name.getBinding() instanceof CompiledVar) {
            lhsDeRef = lhsDeRef && ((CompiledVar) name.getBinding()).getType().isRef();
        }

        boolean rhsDeRef = value.onStack();

        String cast = "";
        if(!name.getType().getName().equals(value.getType().getName())) {
            cast = "(" + name.getType().getCompiledName() + ")";
        }

        String text = (lhsDeRef ? "*" : "") + "(" + name.getCompiledText() + ") = " + cast +
                (rhsDeRef ? "*" : "") + "(" + value.getCompiledText() + ")";
        return new CompiledCode()
            .withText(text)
            .withBinding(name.getBinding())
            .withType(name.getType());
    }

    public CompiledCode compileDeclare(Declare stmt, CompileContext context) {
        CompiledCode typeCode = stmt.type.compile(context);
        List<CompiledType> genericTypes = stmt.type.genericTypes
                .stream()
                .map(type -> (CompiledType) type.compile(context).getBinding())
                .collect(Collectors.toList());
        CompiledType type = ((CompiledType) typeCode.getBinding()).fillGenericTypes(genericTypes);
        CompiledVar binding = new CompiledVar(stmt.name, false, type);
        context.declareObject(binding);

        boolean isRef = type.isRef() && context.isOnStack();

        if(isRef) {
            context.addRefStackSize(1);
        }

        String text = typeCode.getCompiledText() + (isRef ? "* " : " ") + stmt.name + (isRef ? " = skalloc_ref_stack()": "");
        return new CompiledCode()
                .withBinding(binding)
                .withText(text)
                .withType(CompiledType.VOID);
    }

    public CompiledCode compileDeclareAssign(DeclareAssign stmt, CompileContext context) {
        CompiledCode dec = this.compileDeclare(new Declare(stmt.type, stmt.name, stmt.tokens), context);
        CompiledCode value = this.compileAssign(new Assign(new Variable(stmt.name, stmt.tokens), stmt.value, stmt.tokens), context);

        String text = dec.getCompiledText() + "; " + value.getCompiledText() + ";";

        return new CompiledCode()
                .withBinding(dec.getBinding())
                .withText(text)
                .withSemicolon(false)
                .withType(CompiledType.VOID);
    }

    public CompiledCode compileNumberLiteral(NumberLiteral stmt, CompileContext context) {
        return new CompiledCode()
                .withText(String.valueOf(stmt.value.intValue()))
                .withType(CompiledType.INT);
    }

    public CompiledCode compileStringLiteral(StringLiteral stmt, CompileContext context) {
        return new CompiledCode()
                .withText("skiff_string_allocate_new(\"" + stmt.value + "\")")
                .withType(CompiledType.STRING);
    }

    public CompiledCode compileSequenceLiteral(SequenceLiteral stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileBooleanLiteral(BooleanLiteral stmt, CompileContext context) {
        return new CompiledCode()
                .withText(stmt.value ? "1" : "0")
                .withType(CompiledType.BOOL);
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
            if(compiledObject instanceof CompiledVar) {
                CompiledVar cv = ((CompiledVar) compiledObject);
                onStack = !cv.isParam();
            }
        } catch (NoSuchObjectException ex) {
            if(context.getParentClass() == null) {
                throw ex;
            }
            compiledObject = context.getParentClass().getObject(stmt.name);
            text = "this->" + stmt.name;
            onStack = false;
        }
        CompiledType objType = CompiledType.CLASS;
        if (compiledObject instanceof CompiledVar) {
            objType = ((CompiledVar) compiledObject).getType();
        }
        return new CompiledCode()
                .withText(text)
                .withBinding(compiledObject)
                .onStack(onStack)
                .withType(objType);
    }

}
