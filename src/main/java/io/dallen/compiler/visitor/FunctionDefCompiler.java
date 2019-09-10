package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.ast.ASTEnums;
import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

class FunctionDefCompiler {

    private static String generateReturnType(boolean isConstructor, CompileContext context, CompiledType returnType) {
        if(isConstructor) {
            return context.getContainingClass().getCompiledName();
        }

        return returnType.getCompiledName();
    }

    static String generateFuncName(boolean isConstructor, boolean isStatic, String stmtName, CompileContext context) {
        if(isConstructor) {
            return VisitorUtils.underscoreJoin("skiff", stmtName, "new");
        }

        if(isStatic) {
            return VisitorUtils.underscoreJoin("skiff", "static", stmtName);
        }

        return VisitorUtils.underscoreJoin("skiff", context.getScopePrefix(), stmtName);
    }

    private static CompiledFunction createCompiledFunc(boolean isConstructor, AST.FunctionDef stmt, CompileContext context,
                                                       CompileContext innerContext) {

        CompiledCode returns = stmt.returns.compile(context);

        if(!returns.getBinding().equals(BuiltinTypes.VOID) && isConstructor) {
            context.throwError("Constructor must return void", stmt);
        }

        boolean isStatic = stmt.modifiers.contains(ASTEnums.DecModType.STATIC);

        String compiledName = generateFuncName(isConstructor, isStatic, stmt.name, context);

        List<CompiledCode> compiledArgs = stmt.args
                .stream()
                .map(e -> e.compile(context))
                .collect(Collectors.toList());

        ListIterator<AST.FunctionParam> paramItr = stmt.args.listIterator();

        compiledArgs.forEach(arg -> {
            CompiledVar argVar = new CompiledVar(paramItr.next().name, true, arg.getType());
            innerContext.declareObject(argVar);
        });

        return new CompiledFunction(stmt.name, compiledName, isConstructor, (CompiledType) returns.getBinding(),
                compiledArgs.stream().map(CompiledCode::getType).collect(Collectors.toList()));
    }

    private static String generateSig(AST.FunctionDef stmt, CompiledFunction func, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(generateReturnType(func.isConstructor(), context, func.getReturns()));
        sb.append(" ");
        sb.append(func.getCompiledName());
        sb.append("(");

        List<String> stringArgs = new ArrayList<>();

        boolean isStatic = stmt.modifiers.contains(ASTEnums.DecModType.STATIC);

        if(context.getContainingClass() != null && !isStatic) {
            stringArgs.add(context.getContainingClass().getCompiledName() + " this");
        }

        if(func.isConstructor()) {
            stringArgs.add("int new_inst");
        }

        stringArgs.addAll(stmt.args
                .stream()
                .map(e -> e.compile(context))
                .map(CompiledCode::getCompiledText)
                .collect(Collectors.toList()));

        sb.append(String.join(", ", stringArgs));

        sb.append(")");

        return sb.toString();
    }

    static CompiledCode compileFunctionDef(AST.FunctionDef stmt, CompileContext context) {
        StringBuilder functionCode = new StringBuilder();

        boolean isConstructor = context.getContainingClass() != null &&
                stmt.name.equals(context.getContainingClass().getName());

        CompileContext innerContext = new CompileContext(context, true)
                .addIndent();

        CompiledFunction func = createCompiledFunc(isConstructor, stmt, context, innerContext);

        if(!isConstructor) {
            context.declareObject(func);
        }

        if(stmt.modifiers.contains(ASTEnums.DecModType.NATIVE)) {
            return new CompiledCode();
        }

        functionCode.append(generateSig(stmt, func, context));
        functionCode.append("\n")
                .append(context.getIndent()).append("{\n");

        if(isConstructor) {
            functionCode.append(initiateInstance(context.getContainingClass(), innerContext));
        }

        stmt.body.forEach(VisitorUtils.compileToStringBuilder(functionCode, innerContext));

        Optional<AST.Return> returnOptional = Optional.empty();

        if(stmt.body.size() > 0 && stmt.body.get(stmt.body.size() - 1) instanceof AST.Return) {
            returnOptional = Optional.of((AST.Return) stmt.body.get(stmt.body.size() - 1));
        }

        if(returnOptional.isEmpty() && !func.getReturns().equals(BuiltinTypes.VOID)) {
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
                .withBinding(func)
                .withSemicolon(false);
    }

    static String initiateInstance(CompiledType cls, CompileContext innerContext) {
        String className = cls.getName();

        return innerContext.getIndent() +
                VisitorUtils.underscoreJoin("skiff", className, "static") +
                "();\n" +
                innerContext.getIndent() +
                "if(new_inst) { \n" +
                innerContext.getIndent() + CompileContext.INDENT +
                "this->class_ptr = &" +
                cls.getInterfaceName()
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
