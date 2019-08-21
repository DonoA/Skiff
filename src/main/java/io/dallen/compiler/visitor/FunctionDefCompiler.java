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
        String compiledName = context.getParentClass().getCompiledName();
        String className = context.getParentClass().getName();

        return innerContext.getIndent() +
                CompileUtilities.underscoreJoin("skiff", className, "static") +
                "();\n" +
                innerContext.getIndent() +
                compiledName +
                " * this = (" +
                compiledName +
                " *) skalloc(1, sizeof(" +
                compiledName +
                "));\n" +
                innerContext.getIndent() +
                "this->class_ptr = &" +
                CompileUtilities.underscoreJoin("skiff", className, "interface")
                +";\n";
    }

    private static String generateReturns(boolean hasReturn, boolean isConstructor, CompileContext context,
                                          CompileContext innerContext) {
        StringBuilder sb = new StringBuilder();
        if(!hasReturn) {
            sb.append(innerContext.getIndent());
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
