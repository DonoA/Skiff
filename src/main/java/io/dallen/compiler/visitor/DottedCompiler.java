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
        CompiledFunction func = lhs.getType().getMethod(call.name);
        StringBuilder sb = new StringBuilder();
        sb.append("(*").append(lhs.getCompiledText()).append(")->class_ptr->").append(func.getName())
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
        CompiledVar obj = lhs.getType().getField(v.name);
        sb.append(lhs.onStack() ? "(*" : "(").append(lhs.getCompiledText()).append(")->").append(v.name);
        return new CompiledCode()
                .withText(sb.toString())
                .withType(obj.getType());
    }

}
