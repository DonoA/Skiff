package io.dallen.compiler.visitor;

import io.dallen.AST;
import io.dallen.SkiffC;
import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class VisitorUtils {

    static Consumer<AST.Statement> compileToStringBuilder(StringBuilder sb, CompileContext context) {
        return stmt -> {
            CompiledCode s = stmt.compile(context);
            if(SkiffC.DEBUG && !(stmt instanceof AST.BlockStatement)) {
                sb.append(context.getIndent()).append("/* ").append(stmt.toFlatString()).append(" */\n");
            }
            sb.append(context.getIndent());
            sb.append(s.getCompiledText());
            sb.append(s.isRequiresSemicolon() ? ";" : "");
            if(SkiffC.DEBUG && !(stmt instanceof AST.BlockStatement)) {
                sb.append(" /* End ").append(stmt.getClass().getSimpleName()).append(" */");
            }
            sb.append("\n");
        };
    }

    static void cleanupScope(StringBuilder sb, CompileContext context) {
        sb.append(context.getIndent());
        sb.append("// Cleanup scope\n");

        sb.append(context.getIndent()).append("skiff_gc_clean(").append(context.getDataStackSize()).append(", ")
                .append(context.getRefStackSize()).append(");\n");
    }

    static class FunctionSig {
        private final CompiledFunction function;
        private final String text;

        FunctionSig(CompiledFunction function, String text) {
            this.function = function;
            this.text = text;
        }

        public CompiledFunction getFunction() {
            return function;
        }

        public String getText() {
            return text;
        }
    }

    static FunctionSig generateSig(boolean isConstructor, CompileContext context, CompiledCode returnType,
                                   AST.FunctionDef stmt, CompileContext innerContext) {
        StringBuilder sb = new StringBuilder();

        String compiledName = generateFuncName(isConstructor, context, stmt.name);

        sb.append(generateReturnType(isConstructor, context, returnType));
        sb.append(" ");
        sb.append(compiledName);
        sb.append("(");

        List<CompiledCode> compiledArgs = stmt.args
                .stream()
                .map(e -> e.compile(context))
                .collect(Collectors.toList());

        ListIterator<AST.FunctionParam> paramItr = stmt.args.listIterator();

        compiledArgs.forEach(arg -> {
            CompiledVar argVar = new CompiledVar(paramItr.next().name, arg.getType());
            innerContext.declareObject(argVar);
        });

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

        CompiledFunction func = new CompiledFunction(stmt.name, compiledName, (CompiledType) returnType.getBinding(),
                compiledArgs.stream().map(CompiledCode::getType).collect(Collectors.toList()));

        return new FunctionSig(func, sb.toString());
    }


    private static String generateFuncName(boolean isConstructor, CompileContext context, String stmtName) {
        if(isConstructor) {
            return CompileUtilities.underscoreJoin("skiff", stmtName, "new");
        }

        return CompileUtilities.underscoreJoin("skiff", context.getScopePrefix(), stmtName);
    }

    private static String generateReturnType(boolean isConstructor, CompileContext context, CompiledCode returnType) {
        if(isConstructor) {
            return context.getParentClass().getCompiledName() + " **";
        }

        if(returnType.getBinding().equals(CompiledType.VOID)) {
            return "void";
        }

        return returnType.getCompiledText() + " *";
    }
}
