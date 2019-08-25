package io.dallen.compiler.visitor;

import io.dallen.AST;
import io.dallen.ASTEnums;
import io.dallen.SkiffC;
import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VisitorUtils {

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
        sb.append("// Cleanup scope\n");

        sb.append(context.getIndent()).append("skfree_ref_stack(").append(context.getRefStackSize()).append(");\n");
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
            CompiledVar argVar = new CompiledVar(paramItr.next().name, true, arg.getType());
            innerContext.declareObject(argVar);
        });

        List<String> stringArgs = new ArrayList<>();

        if(context.getParentClass() != null) {
            stringArgs.add(context.getParentClass().getCompiledName() + " this");
        }

        if(isConstructor) {
            stringArgs.add("int new_inst");
        }

        stringArgs.addAll(compiledArgs
                .stream()
                .map(CompiledCode::getCompiledText)
                .collect(Collectors.toList()));

        sb.append(String.join(", ", stringArgs));

        sb.append(")");

        CompiledFunction func = new CompiledFunction(stmt.name, compiledName, isConstructor, (CompiledType) returnType.getBinding(),
                compiledArgs.stream().map(CompiledCode::getType).collect(Collectors.toList()));

        return new FunctionSig(func, sb.toString());
    }


    static String generateFuncName(boolean isConstructor, CompileContext context, String stmtName) {
        if(isConstructor) {
            return VisitorUtils.underscoreJoin("skiff", stmtName, "new");
        }

        return VisitorUtils.underscoreJoin("skiff", context.getScopePrefix(), stmtName);
    }

    static String generateReturnType(boolean isConstructor, CompileContext context, CompiledCode returnType) {
        if(isConstructor) {
            return context.getParentClass().getCompiledName();
        }

        if(returnType.getBinding().equals(CompiledType.VOID)) {
            return "void";
        }

        return returnType.getCompiledText();
    }

    static CompiledCode compileBinary(AST.Statement l, AST.Statement r, ASTEnums.HasRaw op, CompileContext context) {
        CompiledCode lhs = l.compile(context);
        CompiledCode rhs = r.compile(context);
        String text = lhs.getCompiledText() + " " + op.getRawOp() + " " + rhs.getCompiledText();
        return new CompiledCode()
                .withText(text);
    }

    static class StructEntry {
        private final String type;
        private final String name;

        public StructEntry(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return this.type + " " + this.name;
        }
    }

    static String compileStruct(String name, String outerIndent, String innerIndent, List<StructEntry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append(outerIndent).append("struct ").append(name).append(" \n{\n");
        for(StructEntry entry : entries) {
            sb.append(innerIndent).append(entry.toString()).append(";\n");
        }
        sb.append(outerIndent).append("};\n");

        return sb.toString();
    }

    public static String underscoreJoin(String... name) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < name.length; i++) {
            if(name[i].isEmpty()) {
                continue;
            }
            char[] n = name[i].toCharArray();
            for(int j = 0; j < n.length; j++) {
                if(Character.isUpperCase(n[j]) && j != 0) {
                    sb.append("_");
                }
                sb.append(Character.toLowerCase(n[j]));
            }
            if(i < name.length - 1) {
                sb.append("_");
            }
        }
        return sb.toString();
    }
}
