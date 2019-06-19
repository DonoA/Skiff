package io.dallen.compiler.visitor;

import io.dallen.AST.*;
import io.dallen.SkiffC;

import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ASTVisitor {

    public CompiledCode compileStatement(Statement stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Statement");
    }

    public CompiledCode compileExpression(Expression stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Expression");
    }

    public CompiledCode compileType(Type stmt, CompileContext context) {
        CompiledCode typeName = stmt.name.compile(this, context);
        CompiledType typ = ((CompiledType) typeName.getBinding());
        String name = typ.getCompiledName() + (typ.isRef() ? " *" : "");
        return new CompiledCode()
                .withText(name)
                .withBinding(typeName.getBinding())
                .withType(CompiledType.CLASS);
    }

    public CompiledCode compileBlockStatement(BlockStatement stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Block");
    }

    public CompiledCode compileFunctionDef(FunctionDef stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        CompiledCode returns = stmt.returns.compile(this, context);
        String returnText = returns.getCompiledText();

        if(!returns.getBinding().equals(CompiledType.VOID)) {
            returnText += " *";
        }

        CompileContext innerContext = new CompileContext(context)
            .addIndent();

        String compiledName = CompileUtilities.underscoreJoin("skiff", context.getScopePrefix(), stmt.name);

        boolean isConstructor = context.getParentClass() != null &&
            stmt.name.equals(context.getParentClass().getName());

        if(isConstructor) {
            compiledName = CompileUtilities.underscoreJoin("skiff", stmt.name, "new");
            returnText = context.getParentClass().getCompiledName() + " **";
        }

        sb.append(returnText);
        sb.append(" ");
        sb.append(compiledName);
        sb.append("(");

        List<CompiledCode> compiledArgs = stmt.args
                .stream()
                .map(e -> e.compile(this, context))
                .collect(Collectors.toList());

        ListIterator<FunctionParam> paramItr = stmt.args.listIterator();

        for (CompiledCode arg : compiledArgs) {
            innerContext.declareObject(new CompiledVar(paramItr.next().name, arg.getType()));
        }

        List<String> stringArgs = new ArrayList<>();

        if(context.getParentClass() != null && !isConstructor) {
                stringArgs.add(context.getParentClass().getCompiledName() + " ** this");
        }

        stringArgs.addAll(compiledArgs
                .stream()
                .map(CompiledCode::getCompiledText)
                .collect(Collectors.toList()));

        sb.append(String.join(", ", stringArgs));

        sb.append(")");

        CompiledFunction func = new CompiledFunction(stmt.name, compiledName, (CompiledType) returns.getBinding(),
            compiledArgs.stream().map(CompiledCode::getType).collect(Collectors.toList()));
        context.declareObject(func);

        sb.append("\n");
        sb.append(context.getIndent());
        sb.append("{\n");

        if(!returns.getBinding().equals(CompiledType.VOID) && isConstructor) {
            throw new CompileError("Constructor must return void");
        }

        if(isConstructor) {
            sb.append(innerContext.getIndent())
                .append(context.getParentClass().getCompiledName())
                .append(" ** this = (")
                .append(context.getParentClass().getCompiledName())
                .append(" **) skalloc_heap(1, sizeof(")
                .append(context.getParentClass().getCompiledName())
                .append("));\n");
        }

        sb.append(innerContext.getIndent())
                .append("// Copy formals\n");
        if(!returns.getBinding().equals(CompiledType.VOID)){
            sb.append(innerContext.getIndent()).append(returns.getCompiledText()).append("* rtn = ");
            if(returns.getType().isRef()) {
                sb.append("skalloc_ref_stack();\n");
            } else {
                int size = ((CompiledType) returns.getBinding()).getSize();
                sb.append("skalloc_data_stack(").append(size).append(");\n");
            }
        }
        stmt.args.forEach(arg -> {
            CompiledCode code = arg.type.compile(this, innerContext);
            sb.append(innerContext.getIndent()).append(code.getCompiledText()).append("* ").append(arg.name);
            sb.append(" = ");
            CompiledType type = ((CompiledType) code.getBinding());
            innerContext.trackObjCreation(type);
            if(type.isRef()) {
                sb.append("skalloc_ref_stack();\n");
            } else {
                sb.append("skalloc_data_stack(").append(type.getSize()).append(");\n");
            }
            sb.append(innerContext.getIndent()).append("*").append(arg.name).append(" = ");
            sb.append("*formal_").append(arg.name).append(";\n");
        });
        sb.append("\n");

        stmt.body.forEach(compileToStringBuilder(sb, innerContext));

        boolean hasReturn = stmt.body.get(stmt.body.size() - 1) instanceof Return;

        if(!hasReturn) {
            cleanupScope(sb, innerContext);
        }

        if(isConstructor) {
            if(hasReturn) {
                throw new CompileError("Constructor cannot return!");
            }

            sb.append(innerContext.getIndent())
                .append("return this;\n");
        }

        sb.append(context.getIndent());
        sb.append("}");

        return new CompiledCode()
                .withText(sb.toString())
                .withType(CompiledType.VOID)
                .withBinding(func)
                .withSemicolon(false);
    }

    public CompiledCode compileFunctionParam(FunctionParam stmt, CompileContext context) {
        CompiledCode type = stmt.type.compile(this, context);
        String sb = type.getCompiledText() + "* formal_" + stmt.name;
        return new CompiledCode()
                .withText(sb)
                .withType((CompiledType) type.getBinding());
    }

    public CompiledCode compileClassDef(ClassDef stmt, CompileContext context) {

        CompiledType cls = new CompiledType(stmt.name, -1)
            .setParent(CompiledType.ANYREF);

        context.declareObject(cls);

        CompileContext innerContext = new CompileContext(context)
            .setScopePrefix(stmt.name)
            .setParentClass(cls);

        innerContext.declareObject(new CompiledVar("this", cls));

        StringBuilder methods = new StringBuilder();

        List<CompiledCode> fields = new ArrayList<>();

        stmt.body.forEach(line -> {
            if(line instanceof Declare){
                CompileContext anonContext = new CompileContext(innerContext)
                    .setOnStack(false);
                CompiledCode code = line.compile(this, anonContext);
                fields.add(code);
                cls.addClassObject(code.getBinding());
            } else if(line instanceof DeclareAssign) {
                throw new CompileError("Declare assign not supported yet");
            }
        });

        stmt.body.forEach(line -> {
            if(line instanceof FunctionDef) {
                CompiledCode code = line.compile(this, innerContext);
                methods.append(code.getCompiledText())
                    .append("\n\n");
                cls.addClassObject(code.getBinding());
            }
        });

        StringBuilder text = new StringBuilder();
        text.append("typedef struct ")
            .append(CompileUtilities.underscoreJoin("skiff", stmt.name, "struct"))
            .append(" ")
            .append(cls.getCompiledName())
            .append(";\n");

        text.append("struct ")
            .append(CompileUtilities.underscoreJoin("skiff", stmt.name, "struct"))
            .append("\n{\n");

        fields.forEach(code -> {
            text.append("    ");
            text.append(code.getCompiledText());
            text.append(";\n");
        });

        text.append("};\n\n");

        text.append(methods);

        return new CompiledCode()
            .withType(CompiledType.VOID)
            .withText(text.toString())
            .withBinding(cls)
            .withSemicolon(false);
    }

    public CompiledCode compileIfBlock(IfBlock stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("if(");
        sb.append(stmt.condition.compile(this, context).getCompiledText());
        sb.append(")\n");
        sb.append(context.getIndent());
        sb.append("{\n");
        CompileContext innerContext = new CompileContext(context).addIndent();
        stmt.body.forEach(compileToStringBuilder(sb, innerContext));
        sb.append(context.getIndent());
        sb.append("}");

        if (stmt.elseBlock != null) {
            sb.append("\n");
            sb.append(context.getIndent());
            sb.append(stmt.elseBlock.compile(this, context).getCompiledText());
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
        CompiledCode code = stmt.on.compile(this, context);
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
        stmt.body.forEach(compileToStringBuilder(sb, elseInnerContext));

        sb.append(context.getIndent());
        sb.append("}");

        return new CompiledCode()
                .withText(sb.toString())
                .withType(CompiledType.VOID)
                .withSemicolon(false);
    }

    private Consumer<Statement> compileToStringBuilder(StringBuilder sb, CompileContext context) {
        return stmt -> {
            CompiledCode s = stmt.compile(this, context);
            if(SkiffC.DEBUG && !(stmt instanceof BlockStatement)) {
                sb.append(context.getIndent()).append("/* ").append(stmt.toFlatString()).append(" */\n");
            }
            sb.append(context.getIndent());
            sb.append(s.getCompiledText());
            sb.append(s.isRequiresSemicolon() ? ";" : "");
            if(SkiffC.DEBUG && !(stmt instanceof BlockStatement)) {
                sb.append(" /* End ").append(stmt.getClass().getSimpleName()).append(" */");
            }
            sb.append("\n");
        };
    }

    public CompiledCode compileWhileBlock(WhileBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileForBlock(ForBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileFunctionCall(FunctionCall stmt, CompileContext context) {
        CompiledObject nameVar = context.getObject(stmt.name);
        if (!(nameVar instanceof CompiledFunction)) {
            throw new CompileError("Variable not function " + stmt.name);
        }

        CompiledFunction func = (CompiledFunction) nameVar;

        List<CompiledCode> compArgs = stmt.args.stream().map(e -> e.compile(this, context))
                .collect(Collectors.toList());

        if (func.getArgs().size() != compArgs.size()) {
            throw new CompileError("Differing param count " + func.getName());
        }

        ListIterator<CompiledType> expected = func.getArgs().listIterator();
        ListIterator<CompiledCode> found = compArgs.listIterator();

        while (expected.hasNext()) {
            CompiledType typ1 = expected.next();
            CompiledType typ2 = found.next().getType();
            if (!typ1.equals(typ2)) {
                throw new CompileError("Differing param types " + func.getName());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(func.getCompiledName());
        sb.append("(");

        List<String> evalArgs = compArgs
                .stream()
                .map(code -> {
                    if(code.isOnStack()) {
                        return code.getCompiledText();
                    } else {
                        return "&(" + code.getCompiledText() + ")";
                    }
                })
                .collect(Collectors.toList());

        sb.append(String.join(", ", evalArgs));

        sb.append(")");

        context.trackObjCreation(func.getReturns());

        return new CompiledCode()
                .withText(sb.toString())
                .withType(func.getReturns());
    }

    public CompiledCode compileParened(Parened stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileDotted(Dotted stmt, CompileContext context) {
        CompiledCode lhs = stmt.left.compile(this, context);
        if(stmt.right instanceof FunctionCall) {
            FunctionCall call = (FunctionCall) stmt.right;
            CompiledObject nameVar = lhs.getType().getObject(call.name);
            if (!(nameVar instanceof CompiledFunction)) {
                throw new CompileError("Variable not function " + call.name);
            }
            CompiledFunction func = (CompiledFunction) nameVar;
            StringBuilder sb = new StringBuilder();
            sb.append(CompileUtilities.underscoreJoin("skiff", lhs.getType().getName(), func.getName()))
                    .append("(").append(lhs.getCompiledText());
            call.args.stream().map(e -> e.compile(this, context))
                    .forEach(e -> sb.append(", ").append(e.getCompiledText()));
            sb.append(")");

            context.trackObjCreation(func.getReturns());

            return new CompiledCode()
                    .withText(sb.toString())
                    .withType(func.getReturns());
        }
        if(stmt.right instanceof Variable) {
            StringBuilder sb = new StringBuilder();
            Variable v = (Variable) stmt.right;
            CompiledObject obj = lhs.getType().getObject(v.name);
            CompiledVar objVar = (CompiledVar) obj;
            sb.append("(*").append(lhs.getCompiledText()).append(")->").append(v.name);
            return new CompiledCode()
                    .setOnStack(false)
                    .withText(sb.toString())
                    .withType(objVar.getType());
        }
        throw new CompileError("Dotted on invalid type " + stmt.right.toFlatString());
    }

    public CompiledCode compileArrowed(Arrowed stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileReturn(Return stmt, CompileContext context) {
        CompiledCode code = stmt.value.compile(this, context);
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
        cleanupScope(sb, context);
        sb.append(context.getIndent()).append("return rtn;");
        return new CompiledCode()
                .withText(sb.toString())
                .withSemicolon(false)
                .withType(CompiledType.VOID);
    }

    public CompiledCode compileNew(New stmt, CompileContext context) {
        CompiledType typeCode = (CompiledType) stmt.type.compile(this, context).getBinding();
        String functionName = CompileUtilities.underscoreJoin("skiff", typeCode.getName(), "new");
        StringBuilder sb = new StringBuilder();
        sb.append(functionName).append("(");
        List<String> argz = stmt.argz
            .stream()
            .map(arg -> arg.compile(this, context).getCompiledText())
            .collect(Collectors.toList());

        sb.append(String.join(",", argz))
            .append(")");

        return new CompiledCode()
            .withType(typeCode)
            .withText(sb.toString());
    }

    private void cleanupScope(StringBuilder sb, CompileContext context) {
        sb.append(context.getIndent());
        sb.append("// Cleanup scope\n");

        sb.append(context.getIndent()).append("skiff_gc_clean(").append(context.getDataStackSize()).append(", ")
                .append(context.getRefStackSize()).append(");\n");
    }

    public CompiledCode compileMathStatement(MathStatement stmt, CompileContext context) {
        CompiledCode lhs = stmt.left.compile(this, context);
        CompiledCode rhs = stmt.right.compile(this, context);

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

    public CompiledCode compileSubscript(Subscript stmt, CompileContext context) {
        CompiledCode left = stmt.left.compile(this, context);
        CompiledObject subscrCall = left.getType().getObject("getSub");
        if(!(subscrCall instanceof CompiledFunction)) {
            throw new CompileError("getSub is not a function");
        }
        CompiledCode sub = stmt.sub.compile(this, context);

        String cFunc = CompileUtilities.underscoreJoin("skiff", left.getType().getName(), "get", "sub", sub.getType().getName());
        String text = cFunc + "(" + left.getCompiledText() + ", " + sub.getCompiledText() + ")";
        return new CompiledCode()
            .withText(text)
            .withType(((CompiledFunction) subscrCall).getReturns());
    }

    public CompiledCode compileCompare(Compare stmt, CompileContext context) {
        CompiledCode cc = compileBinary(stmt.left, stmt.right, stmt.op, context);
        return cc.withType(CompiledType.BOOL);
    }

    public CompiledCode compileBoolCombine(BoolCombine stmt, CompileContext context) {
        CompiledCode cc = compileBinary(stmt.left, stmt.right, stmt.op, context);
        return cc.withType(CompiledType.BOOL);
    }

    public CompiledCode compileAssign(Assign stmt, CompileContext context) {
        // Vars with different names must have different stack locations
        CompiledCode name = stmt.name.compile(this, context);
        CompiledCode value = stmt.value.compile(this, context);

        boolean lhsDeRef = name.isOnStack();
        boolean rhsDeRef = value.isOnStack();

        String sb = (lhsDeRef ? "*" : "") + "(" + name.getCompiledText() + ") = " + (rhsDeRef ? "*" : "") + "(" + value.getCompiledText() + ")";
        return new CompiledCode()
            .withText(sb)
            .withBinding(name.getBinding())
            .withType(name.getType());
    }

    private CompiledCode compileBinary(Statement l, Statement r, HasRaw op, CompileContext context) {
        CompiledCode lhs = l.compile(this, context);
        CompiledCode rhs = r.compile(this, context);
        String text = lhs.getCompiledText() + " " + op.getRawOp() + " " + rhs.getCompiledText();
        return new CompiledCode()
                .withText(text);
    }

    public CompiledCode compileDeclare(Declare stmt, CompileContext context) {
        CompiledCode type = stmt.type.compile(this, context);
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
