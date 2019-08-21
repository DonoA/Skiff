package io.dallen.compiler.visitor;

import io.dallen.AST;
import io.dallen.compiler.*;

import java.util.List;

class FunctionDefCompiler {

    static CompiledCode compileFunctionDef(AST.FunctionDef stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        CompiledCode returns = stmt.returns.compile(context);

        boolean isConstructor = context.getParentClass() != null &&
                stmt.name.equals(context.getParentClass().getName());

        CompileContext innerContext = new CompileContext(context)
                .addIndent();

        if(!returns.getBinding().equals(CompiledType.VOID) && isConstructor) {
            throw new CompileError("Constructor must return void");
        }

        VisitorUtils.FunctionSig sig = VisitorUtils.generateSig(isConstructor, context, returns, stmt, innerContext);
        context.declareObject(sig.getFunction());

        sb.append(sig.getText());
        sb.append("\n")
                .append(context.getIndent()).append("{\n");

        if(isConstructor) {
            sb.append(allocateNewInstance(context, innerContext));
        }

//        if(!returns.getBinding().equals(CompiledType.VOID)){
//            sb.append(generateReturnDecl(returns, innerContext));
//        }

//        sb.append(copyFormals(innerContext, stmt.args));

        stmt.body.forEach(VisitorUtils.compileToStringBuilder(sb, innerContext));

        boolean hasReturn = stmt.body.get(stmt.body.size() - 1) instanceof AST.Return;

        if(!hasReturn && !returns.getBinding().equals(CompiledType.VOID)) {
            throw new CompileError("Function with non void return type must end with a return statement");
        }

        sb.append(generateReturns(hasReturn, isConstructor, context, innerContext));

        return new CompiledCode()
                .withText(sb.toString())
                .withType(CompiledType.VOID)
                .withBinding(sig.getFunction())
                .withSemicolon(false);
    }

    private static String allocateNewInstance(CompileContext context, CompileContext innerContext) {
        String text = innerContext.getIndent() +
                context.getParentClass().getCompiledName() +
                " * this = (" +
                context.getParentClass().getCompiledName() +
                " *) calloc(1, sizeof(" +
                context.getParentClass().getCompiledName() +
                "));\n";
        return text;
    }

    private static String generateReturns(boolean hasReturn, boolean isConstructor, CompileContext context,
                                          CompileContext innerContext) {
        StringBuilder sb = new StringBuilder();
        if(!hasReturn) {
            VisitorUtils.cleanupScope(sb, innerContext);
        }

        if(isConstructor) {
            if(hasReturn) {
                throw new CompileError("Constructor cannot return!");
            }

            sb.append(innerContext.getIndent())
                    .append("return this;\n");
        }

        sb.append(context.getIndent());
        sb.append("}");

        return sb.toString();
    }

}
