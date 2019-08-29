package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

class FunctionCallCompiler {

    static CompiledCode compileFunctionCall(AST.FunctionCall stmt, CompileContext context) {
        CompiledFunction func = context.getScope().getFunction(stmt.name);

        List<CompiledCode> compArgs = stmt.args.stream().map(e -> e.compile(context))
                .collect(Collectors.toList());

//        checkVarTypes(func, compArgs);

        boolean isSuper = stmt.name.equals("super");

        StringBuilder sb = new StringBuilder();
        if(isSuper) {
            String superName = context.getParentClass().getParent().getName();
            sb.append(VisitorUtils.underscoreJoin("skiff", superName, "new"));
        } else {
            sb.append(func.getCompiledName());
        }
        sb.append("(");

        List<String> argList = new ArrayList<>();

        if(isSuper) {
            argList.add("(" + context.getParentClass().getParent().getCompiledName() + ") this");
            argList.add("0");
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
