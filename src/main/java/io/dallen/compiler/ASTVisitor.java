package io.dallen.compiler;

import io.dallen.AST.*;
import io.dallen.SkiffC;

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
        String name = nativeTypeFor(typeName.getCompiledText());
        return new CompiledCode()
                .withText(name)
                .withBinding(typeName.getBinding())
                .withReturn(CompiledType.CLASS);
    }

    private static String nativeTypeFor(String name) {
        switch (name) {
            case "Int":
                return "int32_t";
            case "Void":
                return "void";
            default:
                StringBuilder newName = new StringBuilder();
                newName.append("skiff");
                for(char c : name.toCharArray()) {
                    if(Character.isUpperCase(c)) {
                        newName.append("_");
                    }
                    newName.append(Character.toLowerCase(c));
                }
                newName.append("_t");
                return newName.toString();
        }
    }

    public CompiledCode compileBlockStatement(BlockStatement stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Block");
    }

    public CompiledCode compileFunctionDef(FunctionDef stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        CompiledCode returns = stmt.returns.compile(this, context);

        CompileContext innerContext = new CompileContext(context);

        sb.append(returns.getCompiledText());
        sb.append(" * ");
        sb.append(stmt.name);
        sb.append("(");

        List<CompiledCode> compiledArgs = stmt.args
                .stream()
                .map(e -> e.compile(this, context))
                .collect(Collectors.toList());

        ListIterator<FunctionParam> paramItr = stmt.args.listIterator();

        for (CompiledCode arg : compiledArgs) {
            innerContext.delcareObject(new CompiledVar(paramItr.next().name, arg.getType()));
        }

        List<String> stringArgs = compiledArgs
                .stream()
                .map(CompiledCode::getCompiledText)
                .collect(Collectors.toList());

        sb.append(String.join(", ", stringArgs));

        sb.append(")");

        context.delcareObject(new CompiledVar(stmt.name, CompiledType.FUNCTION));

        sb.append("\n");
        sb.append(context.getIndent());
        sb.append("{\n");

        sb.append(innerContext.getIndent())
                .append("// Copy formals\n");
        stmt.args.forEach(arg -> {
            sb.append(innerContext.getIndent());
            sb.append(arg.type.compile(this, innerContext).getCompiledText());
            sb.append(" * ");
            sb.append(arg.name);
            sb.append(" = ");
            sb.append("skiff_int_new(*formal_");
            sb.append(arg.name);
            sb.append(");\n");
        });
        sb.append("\n");

        stmt.body.forEach(compileToStringBuilder(sb, innerContext));

        if(!(stmt.body.get(stmt.body.size() - 1) instanceof Return)) {
            cleanupScope(sb, innerContext);
        }

        sb.append(context.getIndent());
        sb.append("}");

        return new CompiledCode()
                .withText(sb.toString())
                .withReturn(CompiledType.VOID)
                .withSemicolon(false);
    }

    public CompiledCode compileFunctionParam(FunctionParam stmt, CompileContext context) {
        CompiledCode type = stmt.type.compile(this, context);
        String sb = type.getCompiledText() + " * formal_" + stmt.name;
        return new CompiledCode()
                .withText(sb)
                .withReturn((CompiledType) type.getBinding());
    }

    public CompiledCode compileIfBlock(IfBlock stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("if(");
        sb.append(stmt.condition.compile(this, context).getCompiledText());
        sb.append(")\n");
        sb.append(context.getIndent());
        sb.append("{\n");
        CompileContext innerContext = new CompileContext(context);
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
                .withReturn(CompiledType.VOID)
                .withSemicolon(false);
    }

    public CompiledCode compileElseBlock(ElseBlock stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type ElseBlock");
    }

    public CompiledCode compileElseIfBlock(ElseIfBlock stmt, CompileContext context) {
        CompiledCode code = stmt.on.compile(this, context);
        return new CompiledCode()
                .withText("else " + code.getCompiledText())
                .withReturn(code.getType())
                .withSemicolon(false);
    }

    public CompiledCode compileElseAlwaysBlock(ElseAlwaysBlock stmt, CompileContext context) {
        CompileContext elseInnerContext = new CompileContext(context);
        StringBuilder sb = new StringBuilder();
        sb.append("else\n"); // first line does not need indent
        sb.append(context.getIndent());
        sb.append("{\n");
        stmt.body.forEach(compileToStringBuilder(sb, elseInnerContext));

        sb.append(context.getIndent());
        sb.append("}");

        return new CompiledCode()
                .withText(sb.toString())
                .withReturn(CompiledType.VOID)
                .withSemicolon(false);
    }

    private Consumer<Statement> compileToStringBuilder(StringBuilder sb, CompileContext context) {
        return stmt -> {
            CompiledCode s = stmt.compile(this, context);
            sb.append(context.getIndent());
            sb.append(s.getCompiledText());
            sb.append(s.isRequiresSemicolon() ? ";" : "");
            if(SkiffC.DEBUG && !(stmt instanceof BlockStatement)) {
                sb.append("/* ");
                sb.append(stmt.toFlatString());
                sb.append(" */");
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
        CompiledObject nameVar = stmt.name.compile(this, context).getBinding();
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
                throw new CompileError("Differing param type " + func.getName());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(func.getName());
        sb.append("(");

        List<String> evalArgs = compArgs
                .stream()
                .map(CompiledCode::getCompiledText)
                .collect(Collectors.toList());

        sb.append(String.join(", ", evalArgs));

        sb.append(")");
        return new CompiledCode()
                .withText(sb.toString())
                .withReturn(func.getReturns());
    }

    public CompiledCode compileParened(Parened stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileDotted(Dotted stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileArrowed(Arrowed stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileReturn(Return stmt, CompileContext context) {
        CompiledCode code = stmt.value.compile(this, context);
        if(stmt.value instanceof Variable) { // Should cover more cases
            String rtnVar ="return " + code.getCompiledText();
            return new CompiledCode()
                    .withText(rtnVar)
                    .withReturn(CompiledType.VOID);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(nativeTypeFor(code.getType().getName()));
        sb.append(" * rtn = skiff_int_new(0);\n");
        sb.append(context.getIndent());
        sb.append("*rtn = ");
        sb.append(code.getCompiledText());
        sb.append(";\n");
        cleanupScope(sb, context);
        sb.append(context.getIndent());
        sb.append("return rtn");
        return new CompiledCode()
                .withText(sb.toString())
                .withReturn(CompiledType.VOID);
    }

    private void cleanupScope(StringBuilder sb, CompileContext context) {

    }

    public CompiledCode compileMathStatement(MathStatement stmt, CompileContext context) {
        CompileContext ctx = new CompileContext(context, true);
        CompiledCode lhs = stmt.left.compile(this, ctx);
        CompiledCode rhs = stmt.right.compile(this, ctx);

        String c = "(" + lhs.getCompiledText() + ") " + stmt.op.getRawOp() + " (" + rhs.getCompiledText() + ")";

        return new CompiledCode()
                .withText(c)
                .withReturn(lhs.getType());
    }

    public CompiledCode compileMathAssign(MathAssign stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileSubscript(Subscript stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileCompare(Compare stmt, CompileContext context) {
        CompiledCode cc = compileBinary(stmt.left, stmt.right, stmt.op, context);
        return cc.withReturn(CompiledType.BOOL);
    }

    public CompiledCode compileBoolCombine(BoolCombine stmt, CompileContext context) {
        CompiledCode cc = compileBinary(stmt.left, stmt.right, stmt.op, context);
        return cc.withReturn(CompiledType.BOOL);
    }

    public CompiledCode compileAssign(Assign stmt, CompileContext context) {
        CompiledCode name = stmt.name.compile(this, context);
        CompiledCode value = stmt.value.compile(this, new CompileContext(context, true));

        String sb = "*(" + name.getCompiledText() + ") = " + value.getCompiledText();
        return new CompiledCode()
                .withText(sb)
                .withBinding(name.getBinding())
                .withReturn(name.getType());
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
        context.delcareObject(new CompiledVar(stmt.name, type.getType()));

        String sb = type.getCompiledText() + " * " + stmt.name;
        return new CompiledCode()
                .withText(sb)
                .withReturn(CompiledType.VOID);
    }

    public CompiledCode compileDeclareAssign(DeclareAssign stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileNumberLiteral(NumberLiteral stmt, CompileContext context) {
        return new CompiledCode()
                .withText("skiff_int_new(" + stmt.value.toString() + ")")
                .withReturn(context.getType("Int"));
    }

    public CompiledCode compileStringLiteral(StringLiteral stmt, CompileContext context) {
        return new CompiledCode()
                .withText("skiff_string_new(\"" + stmt.value + "\")")
                .withReturn(context.getType("String"));
    }

    public CompiledCode compileVariable(Variable stmt, CompileContext context) {
        CompiledObject compiledObject = context.getObject(stmt.name);
        CompiledType objType = CompiledType.CLASS;
        String text = stmt.name;
        if (compiledObject instanceof CompiledVar) {
            objType = ((CompiledVar) compiledObject).getType();
        }
        if(context.isNeedDeref()) {
            text = "*" + text;
        }
        return new CompiledCode()
                .withText(text)
                .withBinding(compiledObject)
                .withReturn(objType);
    }

}
