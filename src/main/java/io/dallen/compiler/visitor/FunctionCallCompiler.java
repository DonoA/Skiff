package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

class FunctionCallCompiler {

    static CompiledCode compileFunctionCall(AST.FunctionCall stmt, CompileContext context) {
        List<CompiledCode> compArgs = stmt.args.stream().map(e -> e.compile(context))
                .collect(Collectors.toList());

        CompiledFunction func;
        try {
            func = context.getScope().getFunction(stmt.name,
                    compArgs.stream().map(CompiledCode::getType).collect(Collectors.toList()));
        } catch(CompileException | NoSuchElementException ex) {
            if(context.getContainingClass() == null) {
                context.throwError(ex.getMessage(), stmt);
                return new CompiledCode();
            }

            AST.Variable self = new AST.Variable("this", stmt.tokens);
            return DottedCompiler.compileFunctionDot(self.compile(context), stmt, context);
        }

        boolean isSuper = stmt.name.equals("super");

        StringBuilder sb = new StringBuilder();
        if(isSuper) {
            CompiledFunction ctr = context.getContainingClass().getParent().getConstructor(compArgs.stream().map(CompiledCode::getType)
                    .collect(Collectors.toList()));
            sb.append(ctr.getCompiledName());
        } else {
            sb.append(func.getCompiledName());
        }
        sb.append("(");

        List<String> argList = new ArrayList<>();

        if(isSuper) {
            argList.add("(" + context.getContainingClass().getParent().getCompiledName() + ") this");
        }

        argList.addAll(compArgs
                .stream()
                .map(arg -> {
                    if(arg.onStack()) {
                        return  "*(" + arg.getCompiledText() + ")";
                    }
                    return  "(" + arg.getCompiledText() + ")";
                }).collect(Collectors.toList()));

        sb.append(String.join(", ", argList));

        sb.append(")");

        return new CompiledCode()
                .withText(sb.toString())
                .withType(func.getReturns());
    }
}
