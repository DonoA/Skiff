package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.CompileContext;
import io.dallen.compiler.CompiledCode;

import java.util.List;

class ConditionBlockCompiler {

    private static StringBuilder compileGenericLoop(String blockName, List<AST.Statement> body, AST.Statement condition,
                                                    AST.Statement tick, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(blockName);
        sb.append("(");
        sb.append(condition.compile(context).getCompiledText());
        sb.append(")\n");
        sb.append(context.getIndent());
        sb.append("{\n");
        CompileContext innerContext = new CompileContext(context).addIndent();
        body.forEach(VisitorUtils.compileToStringBuilder(sb, innerContext));
        VisitorUtils.cleanupScope(sb, innerContext, true);

        if (tick != null) {
            sb.append(innerContext.getIndent());
            CompiledCode tickCode = tick.compile(context);
            sb.append(tickCode.getCompiledText());
            sb.append(";\n");
        }

        sb.append(context.getIndent());
        sb.append("}");
        return sb;
    }

    static CompiledCode compileFor(AST.ForBlock stmt, CompileContext context) {
        CompiledCode start = stmt.start.compile(context);

        String text = start.getCompiledText() + "\n" + context.getIndent() +
                compileGenericLoop("while", stmt.body, stmt.condition, stmt.step, context);

        return new CompiledCode()
                .withText(text)
                .withSemicolon(false);
    }

    static CompiledCode compileWhile(AST.WhileBlock stmt, CompileContext context) {
        StringBuilder text = compileGenericLoop("while", stmt.body, stmt.condition, null, context);

        return new CompiledCode()
                .withText(text.toString())
                .withSemicolon(false);
    }

    static CompiledCode compileIfBlock(AST.IfBlock stmt, CompileContext context) {
        StringBuilder text = compileGenericLoop("if", stmt.body, stmt.condition, null, context);

        if (stmt.elseBlock.isPresent()) {
            text.append("\n");
            text.append(context.getIndent());
            text.append(stmt.elseBlock.get().compile(context).getCompiledText());
        }

        return new CompiledCode()
                .withText(text.toString())
                .withSemicolon(false);
    }

}
