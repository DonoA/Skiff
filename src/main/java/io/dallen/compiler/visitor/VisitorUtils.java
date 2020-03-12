package io.dallen.compiler.visitor;

import io.dallen.SkiffC;
import io.dallen.ast.AST;
import io.dallen.ast.ASTEnums;
import io.dallen.compiler.BuiltinTypes;
import io.dallen.compiler.CompileContext;
import io.dallen.compiler.CompiledCode;

import java.util.List;
import java.util.function.Consumer;

public class VisitorUtils {

    static Consumer<AST.Statement> compileToStringBuilder(StringBuilder sb, CompileContext context) {
        return stmt -> {
            CompiledCode s = stmt.compile(context);
            if (context.isDebug() && !(stmt instanceof AST.BlockStatement)) {
                String flat = stmt.toFlatString();
                sb.append(context.getIndent()).append("/* ");
                if (flat.length() < SkiffC.MAX_COL) {
                    sb.append(flat);
                } else {
                    sb.append("\n");
                    int lines = (flat.length() / SkiffC.MAX_COL) + 1;
                    for (int i = 0; i < lines; i++) {
                        sb.append(context.getIndent()).append(flat, i * SkiffC.MAX_COL, Math.min((i + 1) * SkiffC.MAX_COL, flat.length())).append("\n");
                    }
                    sb.append(context.getIndent());
                }
                sb.append(" */\n");
            }
            sb.append(context.getIndent());
            sb.append(s.getCompiledText());
            sb.append(s.isRequiresSemicolon() ? ";" : "");
            if (context.isDebug() && !(stmt instanceof AST.BlockStatement)) {
                sb.append(" /* End ").append(stmt.getClass().getSimpleName()).append(" */");
            }
            sb.append("\n");
        };
    }

    static void cleanupScope(StringBuilder sb, CompileContext context, boolean indent) {
        if (context.isDebug()) {
            if (indent) {
                sb.append(context.getIndent());
            }
            sb.append("// Cleanup scope\n");
        }

        if (indent || context.isDebug()) {
            sb.append(context.getIndent());
        }
        sb.append("skfree_ref_stack(").append(context.getRefStackSize()).append(");\n");
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

    static String compileStruct(String name, List<StructEntry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("struct ").append(name).append(" \n{\n");
        for (StructEntry entry : entries) {
            sb.append(CompileContext.INDENT).append(entry.toString()).append(";\n");
        }
        sb.append("};\n");
        return sb.toString();
    }

    public static String underscoreJoin(String... name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length; i++) {
            if (name[i].isEmpty()) {
                continue;
            }
            char[] n = name[i].toCharArray();
            for (int j = 0; j < n.length; j++) {
                if (Character.isUpperCase(n[j]) && j != 0) {
                    sb.append("_");
                }
                sb.append(Character.toLowerCase(n[j]));
            }
            if (i < name.length - 1) {
                sb.append("_");
            }
        }
        return sb.toString();
    }

    static CompiledCode compileFlowKeyword(String name, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        VisitorUtils.cleanupScope(sb, context, true);
        sb.append(context.getIndent()).append(name).append(";");
        return new CompiledCode()
                .withText(sb.toString())
                .withType(BuiltinTypes.VOID)
                .withSemicolon(false);
    }
}
