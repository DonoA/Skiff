package io.dallen.compiler.visitor;

import io.dallen.SkiffC;
import io.dallen.ast.AST;
import io.dallen.ast.AST.*;
import io.dallen.ast.ASTEnums;
import io.dallen.compiler.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        if(typ == null) {
            context.throwError("Type not found", stmt);
            return new CompiledCode()
                    .withText("")
                    .withBinding(typeName.getBinding())
                    .withType(CompiledType.CLASS);
        }
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

    public CompiledCode compileFunctionDefModifier(FunctionDefModifier stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileFieldModifier(FieldModifier stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileFunctionDef(FunctionDef stmt, CompileContext context) {
        return FunctionDefCompiler.compileFunctionDef(stmt, false, context);
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
        CompileContext innerContext = new CompileContext(context);
        innerContext.addIndent();
        StringBuilder sb = new StringBuilder();
        sb.append("while(1)\n").append(context.getIndent()).append("{\n");
        stmt.body.forEach(VisitorUtils.compileToStringBuilder(sb, innerContext));
        VisitorUtils.cleanupScope(sb, innerContext, true);
        sb.append(context.getIndent()).append("}");
        return new CompiledCode()
                .withText(sb.toString())
                .withType(CompiledType.VOID)
                .withSemicolon(false);
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
        StringBuilder sb = new StringBuilder();
        CompiledCode onCode = stmt.on.compile(context);
        for (int i = 0; i < stmt.body.size(); i++) {
            if(!(stmt.body.get(i) instanceof AST.CaseStatement)) {
                continue;
            }
            AST.CaseStatement caseStatement = (AST.CaseStatement) stmt.body.get(i);
            if(i != 0) {
                sb.append(context.getIndent()).append("else ");
            }
            CompiledCode equalCode = caseStatement.on.compile(context);
            sb.append("if(").append(onCode.getCompiledText()).append(" == ")
                    .append(equalCode.getCompiledText()).append(")\n").append(context.getIndent()).append("{\n");
            CompileContext innerContext = new CompileContext(context).addIndent();
            for (int j = i + 1; j < stmt.body.size(); j++) {
                if(stmt.body.get(j) instanceof AST.BreakStatement) {
                    break;
                }
                if(stmt.body.get(j) instanceof AST.CaseStatement) {
                    continue;
                }
                CompiledCode innerCode = stmt.body.get(j).compile(innerContext);
                sb.append(innerContext.getIndent()).append(innerCode.getCompiledText());
                if(innerCode.isRequiresSemicolon()) {
                    sb.append(";");
                }
                sb.append("\n");
            }
            sb.append(context.getIndent()).append("}\n");
        }
        return new CompiledCode()
                .withText(sb.toString())
                .withSemicolon(false);
    }

    public CompiledCode compileCaseStatement(CaseStatement stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileCaseMatchStatement(CaseMatchStatement stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileBreakStatement(BreakStatement stmt, CompileContext context) {
        return VisitorUtils.compileFlowKeyword("break", context);

    }

    public CompiledCode compileContinueStatement(ContinueStatement stmt, CompileContext context) {
        return VisitorUtils.compileFlowKeyword("continue", context);
    }

    public CompiledCode compileTryBlock(TryBlock stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        CatchBlock ctch = stmt.catchBlock;

        CompileContext catchContext = new CompileContext(context).setIndent(CompileContext.INDENT);
        CompiledCode exceptType = ctch.ex.type.compile(context);
        catchContext.declareObject(new CompiledVar(ctch.ex.name, true, (CompiledType) exceptType.getBinding()));

        String exceptionClassName = ((CompiledType) exceptType.getBinding()).getInterfaceName();
        int catchId = context.getGlobalCounter();
        String catchFuncName = "skiff_catch_" + String.valueOf(catchId);
        sb.append("skiff_start_try(")
                .append(catchFuncName)
                .append(", &").append(exceptionClassName).append(", sp_ref);\n");
        sb.append(context.getIndent())
                .append("int skiff_continue_exec_")
                .append(catchId)
                .append(" = setjmp(catch_layer_tail->current_catch_state);\n");
        sb.append(context.getIndent())
                .append("if(skiff_continue_exec_").append(catchId).append(" == 0)\n")
                .append(context.getIndent()).append("{\n");

        CompileContext innerContext = new CompileContext(context).addIndent();

        stmt.body.forEach(VisitorUtils.compileToStringBuilder(sb, innerContext));

        sb.append(context.getIndent()).append("}\n");
        sb.append(context.getIndent()).append("skiff_end_try();\n");

        StringBuilder catchText = new StringBuilder();
        catchText.append("void skiff_catch_")
                .append(catchId)
                .append("(skiff_catch_layer_t * layer, skiff_exception_t * ")
                .append(ctch.ex.name)
                .append(")\n");
        catchText.append("{\n");
        ctch.body.forEach(VisitorUtils.compileToStringBuilder(catchText, catchContext));
        catchText.append(catchContext.getIndent())
                .append("skfree_set_ref_stack(layer->sp_ref_val);\n");
        catchText.append(catchContext.getIndent())
                .append("longjmp(layer->current_catch_state, 1);\n");
        catchText.append("}\n");

        context.addDependentCode(catchText.toString());

        return new CompiledCode()
                .withText(sb.toString())
                .withSemicolon(false)
                .withType(CompiledType.VOID);
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
        CompiledCode code = stmt.value.compile(context);
        StringBuilder sb = new StringBuilder();

        // Calculate how much needs to be deleted from the last function
        VisitorUtils.cleanupScope(sb, context, false);
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
        if(stmt.argz.size() != typeCode.getConstructors().get(0).getArgs().size()) {
            context.throwError("Arg count does not match constructor!", stmt);
        }
        for (int i = 0; i < stmt.argz.size(); i++) {
            String argType = "";
            String argText = stmt.argz.get(i).compile(context).getCompiledText();
            if(typeCode.getConstructors().get(0).getArgs().get(0).isGenericPlaceholder()) {
                argType = "(void *)";
            }
            argz.add(argType + argText);
        }

        sb.append(String.join(", ", argz))
            .append(")");

        CompiledType exactType = typeCode.fillGenericTypes(genericTypes);

        return new CompiledCode()
            .withType(exactType)
            .withText(sb.toString());
    }

    public CompiledCode compileThrowStatement(ThrowStatement stmt, CompileContext context) {
        CompiledCode inner = stmt.value.compile(context);

        return new CompiledCode()
                .withText("skiff_throw(" + inner.getCompiledText() + ")")
                .withType(CompiledType.VOID);
    }

    public CompiledCode compileImportStatement(ImportStatement stmt, CompileContext context) {
        String systemPath = new File("lib/" + stmt.value + ".skiff").getAbsolutePath();
        Optional<String> importCode = SkiffC.compile(systemPath, context.isDebug());
        if(importCode.isEmpty()) {
            return new CompiledCode()
                    .withText("");
        }
        String text = "// Import " + stmt.value + "\n" + importCode.get() +"\n";
        return new CompiledCode()
                .withText(text);
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
        CompiledCode on = stmt.left.compile(context);
        String op = stmt.op == ASTEnums.MathOp.MINUS ? "--" : "++";
        String text = on.getCompiledText();
        if(stmt.time == ASTEnums.SelfModTime.POST) {
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
            if(stmt.name.equals("null")) {
                text = "0";
                compiledObject = CompiledVar.NULL;
                onStack = false;
            } else  if(context.getParentClass() == null) {
                throw ex;
            } else {
                compiledObject = context.getParentClass().getObject(stmt.name);
                text = "this->" + stmt.name;
                onStack = false;
            }
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
