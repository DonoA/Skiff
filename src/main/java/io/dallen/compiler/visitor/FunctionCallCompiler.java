package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

class FunctionCallCompiler {

    static CompiledCode compileFunctionCall(AST.FunctionCall stmt, CompileContext context) {
        CompiledFunction func;
        try {
            func = context.getScope().getFunction(stmt.name);
        } catch(CompileException | NoSuchElementException ex) {
            if(context.getContainingClass() == null) {
                context.throwError(ex.getMessage(), stmt);
                return new CompiledCode();
            }

            AST.Variable self = new AST.Variable("this", stmt.tokens);
            return DottedCompiler.compileFunctionDot(self.compile(context), stmt, context);
        }

        List<CompiledCode> compArgs = stmt.args.stream().map(e -> e.compile(context))
                .collect(Collectors.toList());

        if (func.getArgs().size() != compArgs.size()) {
            context.throwError("Differing param count", stmt);
        }

        for (int i = 0; i < func.getArgs().size(); i++) {
            CompiledType expected = func.getArgs().get(i).getType();
            CompiledType found = compArgs.get(i).getType();
            if (!expected.equals(found)) {
                context.throwError("Differing param types", stmt.args.get(i));
            }
        }

        boolean isSuper = stmt.name.equals("super");

        StringBuilder sb = new StringBuilder();
        if(isSuper) {
            String superName = context.getContainingClass().getParent().getName();
            sb.append(VisitorUtils.underscoreJoin("skiff", superName, "new"));
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
