package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.CompileContext;
import io.dallen.compiler.CompiledCode;
import io.dallen.compiler.CompiledType;

import java.util.List;

public class ConditionBlockCompiler {

    private static StringBuilder compileGenericLoop(String blockName, List<AST.Statement> body, AST.Statement condition, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(blockName);
        sb.append("(");
        sb.append(condition.compile(context).getCompiledText());
        sb.append(")\n");
        sb.append(context.getIndent());
        sb.append("{\n");
        CompileContext innerContext = new CompileContext(context).addIndent();
        body.forEach(VisitorUtils.compileToStringBuilder(sb, innerContext));
        VisitorUtils.cleanupScope(sb, innerContext);
        sb.append(context.getIndent());
        sb.append("}");
        return sb;
    }

    static CompiledCode compileWhile(AST.WhileBlock stmt, CompileContext context) {
        StringBuilder text = compileGenericLoop("while", stmt.body, stmt.condition, context);

        return new CompiledCode()
                .withText(text.toString())
                .withType(CompiledType.VOID)
                .withSemicolon(false);
    }

    static CompiledCode compileIfBlock(AST.IfBlock stmt, CompileContext context) {
        StringBuilder text = compileGenericLoop("if", stmt.body, stmt.condition, context);

        if (stmt.elseBlock.isPresent()) {
            text.append("\n");
            text.append(context.getIndent());
            text.append(stmt.elseBlock.get().compile(context).getCompiledText());
        }

        return new CompiledCode()
                .withText(text.toString())
                .withType(CompiledType.VOID)
                .withSemicolon(false);
    }

}
