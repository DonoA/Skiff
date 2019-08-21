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
                " ** this = (" +
                context.getParentClass().getCompiledName() +
                " **) skalloc_heap(1, sizeof(" +
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

    private static String generateReturnDecl(CompiledCode returnType, CompileContext innerContext) {
        StringBuilder sb = new StringBuilder();
        sb.append(innerContext.getIndent()).append(returnType.getCompiledText()).append("* rtn = ");
        if(returnType.getType().isRef()) {
            sb.append("skalloc_ref_stack();\n");
            return sb.toString();
        }

        int size = ((CompiledType) returnType.getBinding()).getSize();
        sb.append("skalloc_data_stack(").append(size).append(");\n");
        return sb.toString();
    }

    private static String copyFormals(CompileContext innerContext, List<AST.FunctionParam> args) {
        StringBuilder sb = new StringBuilder();
        sb.append(innerContext.getIndent())
                .append("// Copy formals\n");
        args.forEach(arg -> {
            CompiledCode code = arg.type.compile(innerContext);
            sb.append(innerContext.getIndent()).append(code.getCompiledText()).append("* ").append(arg.name);
            sb.append(" = ");
            CompiledType type = ((CompiledType) code.getBinding());
            innerContext.trackObjCreation(type);
            if(type.isRef()) {
                sb.append("skalloc_ref_stack();\n");
            } else {
                sb.append("skalloc_data_stack(").append(type.getSize()).append(");\n");
            }
            sb.append(innerContext.getIndent()).append("*").append(arg.name).append(" = ");
            sb.append("*formal_").append(arg.name).append(";\n");
        });
        sb.append("\n");
        return sb.toString();
    }
}
