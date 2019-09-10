package io.dallen.compiler.visitor;

import io.dallen.SkiffC;
import io.dallen.ast.AST;
import io.dallen.ast.AST.*;
import io.dallen.ast.ASTEnums;
import io.dallen.compiler.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
        return new CompiledCode();
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
                .withSemicolon(false);
    }

    public CompiledCode compileForBlock(ForBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileForIterBlock(ForIterBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileMatchBlock(MatchBlock stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        CompiledCode onCode = stmt.on.compile(context);
        String deref = (onCode.onStack() ? "*" : "");
        for (int i = 0; i < stmt.body.size(); i++) {
            if(!(stmt.body.get(i) instanceof AST.CaseMatchStatement)) {
                continue;
            }
            AST.CaseMatchStatement caseStatement = (AST.CaseMatchStatement) stmt.body.get(i);
            if(i != 0) {
                sb.append(context.getIndent()).append("else ");
            }

            CompileContext innerContext = new CompileContext(context).addIndent();
            sb.append("if(instance_of((skiff_any_ref_t *)").append(deref).append(onCode.getCompiledText()).append(", &");
            if(caseStatement.on instanceof FunctionCall) {
                FunctionCall func = (FunctionCall) caseStatement.on;
                CompiledObject intoObj = context.getObject(func.name);
                if(!(intoObj instanceof CompiledType)) {
                    context.throwError("Match type is not a type", func);
                    return new CompiledCode();
                }
                CompiledType into = (CompiledType) intoObj;
                if(!into.isDataClass()) {
                    context.throwError("Cannot deconstruct non data class", func);
                    return new CompiledCode();
                }
                sb.append(into.getInterfaceName()).append("))\n")
                        .append(context.getIndent()).append("{\n");
                func.args.forEach(stmtArg -> {
                    if(!(stmtArg instanceof Variable)) {
                        context.throwError("Args of type deconstruction must be Variables", stmtArg);
                        return;
                    }
                    Variable v = (Variable) stmtArg;
                    CompiledField field = into.getField(v.name);
                    sb.append(innerContext.getIndent()).append(field.getType().getCompiledName());
                    if(field.getType().isRef()) {
                        sb.append("*");
                    }
                    sb.append(" ").append(v.name);
                    if(field.getType().isRef()) {
                        sb.append(" = skalloc_ref_stack()");
                    }
                    sb.append(";\n");
                    sb.append(innerContext.getIndent());
                    if(field.getType().isRef()) {
                        sb.append("*");
                    }
                    sb.append(v.name).append(" = ((").append(into.getCompiledName()).append(") ").append(deref)
                            .append(onCode.getCompiledText()).append(")->").append(field.getName()).append(";\n");

                    innerContext.declareObject(new CompiledVar(v.name, false, field.getType()));
                });

            } else if(caseStatement.on instanceof Declare) {
                Declare dec  = (Declare) caseStatement.on;
                CompiledType type = (CompiledType) dec.type.compile(context).getBinding();
                sb.append(type.getInterfaceName()).append("))\n")
                        .append(context.getIndent()).append("{\n")
                        .append(innerContext.getIndent()).append(dec.compile(innerContext).getCompiledText()).append(";\n")
                        .append(innerContext.getIndent()).append("*").append(dec.name).append(" = (")
                        .append(type.getCompiledName()).append(") *").append(onCode.getCompiledText()).append(";\n");
            } else {
                context.throwError("Invalid statement type", caseStatement.on);
            }
            for (int j = i + 1; j < stmt.body.size(); j++) {
                if(stmt.body.get(j) instanceof AST.BreakStatement) {
                    break;
                }
                if(stmt.body.get(j) instanceof AST.CaseMatchStatement) {
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
        CompiledCode code = stmt.value.compile(context);
        StringBuilder sb = new StringBuilder();

        // Calculate how much needs to be deleted from the last function
        VisitorUtils.cleanupScope(sb, context, false);
        sb.append(context.getIndent()).append("return ").append(code.getCompiledText());
        return new CompiledCode()
                .withText(sb.toString())
                .withSemicolon(true);
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
                .withText("skiff_throw(" + inner.getCompiledText() + ")");
    }

    public CompiledCode compileImportStatement(ImportStatement stmt, CompileContext context) {
        String importText;
        try {
            importText = SkiffC.readFile("lib/" + stmt.value + ".skiff");
        } catch (IOException e) {
            context.throwError("Cannot find import file", stmt);
            return new CompiledCode();
        }

        String currentFile = context.getFilename();
        context.setFilename(stmt.value);
        Optional<String> importCode = SkiffC.compile(importText, context);
        context.setFilename(currentFile);

        if(importCode.isEmpty()) {
            return new CompiledCode();
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
        return cc.withType(BuiltinTypes.BOOL);
    }

    public CompiledCode compileBoolCombine(BoolCombine stmt, CompileContext context) {
        CompiledCode cc = VisitorUtils.compileBinary(stmt.left, stmt.right, stmt.op, context);
        return cc.withType(BuiltinTypes.BOOL);
    }

    public CompiledCode compileAssign(Assign stmt, CompileContext context) {
        // Vars with different names must have different stack locations
        // Each stack location should represent exactly one named var
        CompiledCode value = stmt.value.compile(context);

        if(stmt.name instanceof FunctionCall) {
            FunctionCall func = (FunctionCall) stmt.name;
            CompiledType intoType = (CompiledType) context.getObject(func.name);

            StringBuilder sb = new StringBuilder();

            String deref = (value.onStack() ? "*" : "");

            for (int i = 0; i < func.args.size(); i++) {
                if(!(func.args.get(i) instanceof Variable)) {
                    context.throwError("Args of type deconstruction must be Variables", func.args.get(i));
                    continue;
                }
                Variable v = (Variable) func.args.get(i);
                CompiledField field = intoType.getAllFields().get(i);
                sb.append(context.getIndent()).append(field.getType().getCompiledName());
                if(field.getType().isRef()) {
                    sb.append("*");
                }
                sb.append(" ").append(v.name);
                if(field.getType().isRef()) {
                    sb.append(" = skalloc_ref_stack()");
                }
                sb.append(";\n");
                sb.append(context.getIndent());
                if(field.getType().isRef()) {
                    sb.append("*");
                }
                sb.append(v.name).append(" = ((").append(intoType.getCompiledName()).append(") ").append(deref)
                        .append(value.getCompiledText()).append(")->").append(field.getName()).append(";\n");

                context.declareObject(new CompiledVar(v.name, false, field.getType()));
            }

            return new CompiledCode()
                    .withText(sb.toString())
                    .withSemicolon(false);
        }
        CompiledCode name = stmt.name.compile(context);

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
                .withText(text);
    }

    public CompiledCode compileDeclareAssign(DeclareAssign stmt, CompileContext context) {
        CompiledCode dec = this.compileDeclare(new Declare(stmt.type, stmt.name, List.of(), stmt.tokens), context);
        CompiledCode value = this.compileAssign(new Assign(new Variable(stmt.name, stmt.tokens), stmt.value, stmt.tokens), context);

        String text = dec.getCompiledText() + ";\n" + context.getIndent() + value.getCompiledText() + ";";

        return new CompiledCode()
                .withBinding(dec.getBinding())
                .withText(text)
                .withSemicolon(false);
    }

    public CompiledCode compileNumberLiteral(NumberLiteral stmt, CompileContext context) {
        return new CompiledCode()
                .withText(String.valueOf(stmt.value.intValue()))
                .withType(BuiltinTypes.INT);
    }

    public CompiledCode compileStringLiteral(StringLiteral stmt, CompileContext context) {
        return new CompiledCode()
                .withText("skiff_string_allocate_new(\"" + stmt.value + "\")")
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
        CompiledObject compiledObject;
        String text = stmt.name;
        boolean onStack = true;
        try {
            compiledObject = context.getObject(stmt.name);
            if(compiledObject instanceof CompiledVar) {
                CompiledVar cv = ((CompiledVar) compiledObject);
                onStack = !cv.isParam();
            }
        } catch (NoSuchElementException ex) {
            if(stmt.name.equals("null")) {
                text = "0";
                compiledObject = CompiledVar.NULL;
                onStack = false;
            } else  if(context.getContainingClass() == null) {
                throw ex;
            } else {
                compiledObject = context.getContainingClass().getObject(stmt.name);
                text = "this->" + stmt.name;
                onStack = false;
            }
        }
        CompiledType objType = BuiltinTypes.CLASS;
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
