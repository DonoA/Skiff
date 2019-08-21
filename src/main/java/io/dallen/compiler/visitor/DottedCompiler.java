package io.dallen.compiler.visitor;

import io.dallen.AST;
import io.dallen.compiler.*;

class DottedCompiler {

    static CompiledCode compileDotted(AST.Dotted stmt, CompileContext context) {
        CompiledCode lhs = stmt.left.compile(context);
        if(stmt.right instanceof AST.FunctionCall) {
            return compileFunctionDot(lhs, (AST.FunctionCall) stmt.right, context);
        }
        if(stmt.right instanceof AST.Variable) {
            return compileVarDot(lhs, (AST.Variable) stmt.right);
        }
        throw new CompileError("Dotted on invalid type " + stmt.right.toFlatString());
    }

    private static CompiledCode compileFunctionDot(CompiledCode lhs, AST.FunctionCall call, CompileContext context) {
        CompiledObject nameVar = lhs.getType().getObject(call.name);
        if (!(nameVar instanceof CompiledFunction)) {
            throw new CompileError("Variable not function " + call.name);
        }
        CompiledFunction func = (CompiledFunction) nameVar;
        StringBuilder sb = new StringBuilder();
        sb.append(CompileUtilities.underscoreJoin("skiff", lhs.getType().getName(), func.getName()))
                .append("(*").append(lhs.getCompiledText());
        call.args.stream()
                .map(e -> e.compile(context))
                .map(arg -> {
                    if(arg.onStack()) {
                        return  "*(" + arg.getCompiledText() + ")";
                    }
                    return arg.getCompiledText();
                })
                .forEach(e -> sb.append(", ").append(e));
        sb.append(")");

        return new CompiledCode()
                .withText(sb.toString())
                .withType(func.getReturns());
    }

    private static CompiledCode compileVarDot(CompiledCode lhs, AST.Variable v) {
        StringBuilder sb = new StringBuilder();
        CompiledObject obj = lhs.getType().getObject(v.name);
        CompiledVar objVar = (CompiledVar) obj;
        sb.append(lhs.onStack() ? "(*" : "(").append(lhs.getCompiledText()).append(")->").append(v.name);
        return new CompiledCode()
                .withText(sb.toString())
                .withType(objVar.getType());
    }

}
