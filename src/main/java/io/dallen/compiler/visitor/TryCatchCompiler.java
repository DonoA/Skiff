package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.CompileContext;
import io.dallen.compiler.CompiledCode;
import io.dallen.compiler.CompiledType;
import io.dallen.compiler.CompiledVar;

import java.util.List;

public class TryCatchCompiler {
    static CompiledCode compileTryBlock(AST.TryBlock stmt, CompileContext context) {
        AST.CatchBlock ctch = stmt.catchBlock;

        CompileContext catchContext = new CompileContext(context).setIndent(CompileContext.INDENT);
        CompiledCode exceptType = ctch.ex.type.compile(context);
        catchContext.declareObject(new CompiledVar(ctch.ex.name, true, (CompiledType) exceptType.getBinding()));
        int catchId = context.getGlobalCounter();

        String text = compileTry(stmt.body, catchId, exceptType, context);

        context.addDependentCode(compileCatch(ctch, catchId, catchContext));

        return new CompiledCode()
                .withText(text)
                .withSemicolon(false);
    }

    private static String compileTry(List<AST.Statement> body, int catchId, CompiledCode exceptType,
                                     CompileContext context) {
        StringBuilder sb = new StringBuilder();

        String exceptionClassName = ((CompiledType) exceptType.getBinding()).getInterfaceName();
        String catchFuncName = "skiff_catch_" + String.valueOf(catchId);
        sb.append("skiff_start_try(")
                .append(catchFuncName)
                .append(", &").append(exceptionClassName).append(", sp_ref);\n");
        sb.append(context.getIndent())
                .append("int skiff_continue_exec_")
                .append(catchId)
                .append(" = setjmp(catch_layer_tail->current_catch_state);\n");
        sb.append(context.getIndent())
                .append("if(skiff_continue_exec_").append(catchId).append(" == 0)\n")
                .append(context.getIndent()).append("{\n");

        CompileContext innerContext = new CompileContext(context).addIndent();

        body.forEach(VisitorUtils.compileToStringBuilder(sb, innerContext));

        sb.append(context.getIndent()).append("}\n");
        sb.append(context.getIndent()).append("skiff_end_try();\n");

        return sb.toString();
    }

    private static String compileCatch(AST.CatchBlock ctch, int catchId, CompileContext catchContext) {
        StringBuilder catchText = new StringBuilder();
        catchText.append("void skiff_catch_")
                .append(catchId)
                .append("(skiff_catch_layer_t * layer, skiff_exception_t * ")
                .append(ctch.ex.name)
                .append(")\n");
        catchText.append("{\n");
        ctch.body.forEach(VisitorUtils.compileToStringBuilder(catchText, catchContext));
        catchText.append(catchContext.getIndent())
                .append("skfree_set_ref_stack(layer->sp_ref_val);\n");
        catchText.append(catchContext.getIndent())
                .append("longjmp(layer->current_catch_state, 1);\n");
        catchText.append("}\n");
        return catchText.toString();
    }
}
