package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.*;

import java.util.Optional;

class FunctionDefCompiler {

    static CompiledCode compileFunctionDef(AST.FunctionDef stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        CompiledCode returns = stmt.returns.compile(context);

        boolean isConstructor = context.getParentClass() != null &&
                stmt.name.equals(context.getParentClass().getName());

        CompileContext innerContext = new CompileContext(context)
                .addIndent();

        if(!returns.getBinding().equals(CompiledType.VOID) && isConstructor) {
            context.throwError("Constructor must return void", stmt);
        }

        VisitorUtils.FunctionSig sig = VisitorUtils.generateSig(isConstructor, context, returns, stmt, innerContext);
        context.declareObject(sig.getFunction());

        sb.append(sig.getText());
        sb.append("\n")
                .append(context.getIndent()).append("{\n");

        if(isConstructor) {
            sb.append(initiateInstance(context, innerContext));
        }

        stmt.body.forEach(VisitorUtils.compileToStringBuilder(sb, innerContext));

        Optional<AST.Return> returnOptional = Optional.empty();

        if(stmt.body.get(stmt.body.size() - 1) instanceof AST.Return) {
            returnOptional = Optional.of((AST.Return) stmt.body.get(stmt.body.size() - 1));
        }

        if(returnOptional.isEmpty() && !returns.getBinding().equals(CompiledType.VOID)) {
            context.throwError("Function with non void return type must end with a return statement", stmt);
        }

        sb.append(generateReturns(returnOptional, isConstructor, context, innerContext));

        return new CompiledCode()
                .withText(sb.toString())
                .withType(CompiledType.VOID)
                .withBinding(sig.getFunction())
                .withSemicolon(false);
    }

    private static String initiateInstance(CompileContext context, CompileContext innerContext) {
        String className = context.getParentClass().getName();

        return innerContext.getIndent() +
                VisitorUtils.underscoreJoin("skiff", className, "static") +
                "();\n" +
                innerContext.getIndent() +
                "if(new_inst) { \n" +
                innerContext.getIndent() + CompileContext.INDENT +
                "this->class_ptr = &" +
                VisitorUtils.underscoreJoin("skiff", className, "interface")
                +";\n" +
                innerContext.getIndent() +
                "}\n";

    }

    private static String generateReturns(Optional<AST.Return> returns, boolean isConstructor, CompileContext context,
                                          CompileContext innerContext) {
        StringBuilder sb = new StringBuilder();
        if(returns.isEmpty()) {
            sb.append(innerContext.getIndent());
            VisitorUtils.cleanupScope(sb, innerContext);
        }

        if(isConstructor) {
            returns.ifPresent(returnz -> context.throwError("Constructor cannot return!", returnz));

            sb.append(innerContext.getIndent())
                    .append("return this;\n");
        }

        sb.append(context.getIndent());
        sb.append("}");

        return sb.toString();
    }

}
