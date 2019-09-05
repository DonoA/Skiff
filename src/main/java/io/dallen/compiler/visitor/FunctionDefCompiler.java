package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.*;

import java.util.Optional;

class FunctionDefCompiler {

    static CompiledCode compileFunctionDef(AST.FunctionDef stmt, boolean isStatic, CompileContext context) {
        StringBuilder functionCode = new StringBuilder();
        CompiledCode returns = stmt.returns.compile(context);

        boolean isConstructor = context.getContainingClass() != null && !isStatic &&
                stmt.name.equals(context.getContainingClass().getName());

        CompileContext innerContext = new CompileContext(context, true)
                .addIndent();

        if(!returns.getBinding().equals(BuiltinTypes.VOID) && isConstructor) {
            context.throwError("Constructor must return void", stmt);
        }

        VisitorUtils.FunctionSig sig = VisitorUtils.generateSig(isConstructor, isStatic, context, returns, stmt,
                innerContext);

        if(!isConstructor) {
            context.declareObject(sig.getFunction());
        }

        functionCode.append(sig.getText());
        functionCode.append("\n")
                .append(context.getIndent()).append("{\n");

        if(isConstructor) {
            functionCode.append(initiateInstance(context, innerContext));
        }

        stmt.body.forEach(VisitorUtils.compileToStringBuilder(functionCode, innerContext));

        Optional<AST.Return> returnOptional = Optional.empty();

        if(stmt.body.size() > 0 && stmt.body.get(stmt.body.size() - 1) instanceof AST.Return) {
            returnOptional = Optional.of((AST.Return) stmt.body.get(stmt.body.size() - 1));
        }

        if(returnOptional.isEmpty() && !returns.getBinding().equals(BuiltinTypes.VOID)) {
            // TODO: Branch checking to ensure that there is a way nothing can be returned
//            context.throwError("Function with non void return type must end with a return statement", stmt);
        }

        functionCode.append(generateReturns(returnOptional, isConstructor, context, innerContext));

        StringBuilder text = new StringBuilder();

        innerContext.getDependentCode().forEach(s -> text.append(s).append("\n"));
        text.append(functionCode.toString());

        return new CompiledCode()
                .withText(text.toString())
                .withType(BuiltinTypes.VOID)
                .withBinding(sig.getFunction())
                .withSemicolon(false);
    }

    private static String initiateInstance(CompileContext context, CompileContext innerContext) {
        String className = context.getContainingClass().getName();

        return innerContext.getIndent() +
                VisitorUtils.underscoreJoin("skiff", className, "static") +
                "();\n" +
                innerContext.getIndent() +
                "if(new_inst) { \n" +
                innerContext.getIndent() + CompileContext.INDENT +
                "this->class_ptr = &" +
                context.getContainingClass().getInterfaceName()
                +";\n" +
                innerContext.getIndent() +
                "}\n";

    }

    private static String generateReturns(Optional<AST.Return> returns, boolean isConstructor, CompileContext context,
                                          CompileContext innerContext) {
        StringBuilder sb = new StringBuilder();
        if(returns.isEmpty()) {
            VisitorUtils.cleanupScope(sb, innerContext, true);
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
