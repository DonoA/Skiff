package io.dallen.compiler.visitor;

import io.dallen.AST;
import io.dallen.compiler.*;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

class FunctionCallCompiler {

    static CompiledCode compileFunctionCall(AST.FunctionCall stmt, CompileContext context) {
        CompiledObject nameVar = context.getObject(stmt.name);
        if (!(nameVar instanceof CompiledFunction)) {
            throw new CompileError("Variable not function " + stmt.name);
        }

        CompiledFunction func = (CompiledFunction) nameVar;

        List<CompiledCode> compArgs = stmt.args.stream().map(e -> e.compile(context))
                .collect(Collectors.toList());

//        checkVarTypes(func, compArgs);

        StringBuilder sb = new StringBuilder();
        sb.append(func.getCompiledName());
        sb.append("(");

        String argText = compArgs
                .stream()
                .map(arg -> {
                    if(arg.onStack()) {
                        return  "*(" + arg.getCompiledText() + ")";
                    }
                    return  "(" + arg.getCompiledText() + ")";
                }).collect(Collectors.joining(", "));

        sb.append(argText);

        sb.append(")");

        context.trackObjCreation(func.getReturns());

        return new CompiledCode()
                .withText(sb.toString())
                .withType(func.getReturns());
    }

    private static void checkVarTypes(CompiledFunction func, List<CompiledCode> compArgs) {
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
    }

}
