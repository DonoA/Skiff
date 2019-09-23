package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.ast.ASTEnums;
import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.List;
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

        List<CompiledVar> compiledArgs = stmt.args
                .stream()
                .map(e -> {
                    CompiledCode arg = e.compile(context);
                    List<CompiledType> genericType = e.type.genericTypes
                            .stream()
                            .map(gt -> gt.compile(context).getBinding())
                            .map(gt -> (CompiledType) gt)
                            .collect(Collectors.toList());

                    CompiledType filledType = arg.getType().fillGenericTypes(genericType);
                    return new CompiledVar(e.name, false, filledType);
                })
                .collect(Collectors.toList());


        compiledArgs.forEach(innerContext::declareObject);

        return new CompiledFunction(stmt.name, compiledName, isConstructor, (CompiledType) returns.getBinding(),
                compiledArgs);
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

        if(context.getContainingClass() == null) {
            context.declareObject(func);
        }

        if(stmt.modifiers.contains(ASTEnums.DecModType.NATIVE)) {
            return new CompiledCode();
        }

        functionCode.append(generateSig(stmt, func, context));
        functionCode.append("\n")
                .append(context.getIndent()).append("{\n");

        if(func.getName().equals("main")) {
            injectSetup(functionCode, stmt, func, innerContext);
        }

        if(isConstructor) {
            functionCode.append(initiateInstance(context.getContainingClass(), innerContext));
        }

        injectFormalCopy(functionCode, func, innerContext);

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

    private static void injectFormalCopy(StringBuilder functionCode, CompiledFunction func, CompileContext innerContext) {
        func.getArgs().stream()
                .filter(arg -> arg.getType().isRef())
                .forEach(arg -> {
                    innerContext.addRefStackSize(1);
                    functionCode.append(innerContext.getIndent()).append(arg.getType().getCompiledName()).append("* ")
                            .append(arg.getName()).append(" = skalloc_ref_stack();\n");
                    functionCode.append(innerContext.getIndent()).append("*").append(arg.getName()).append(" = formal_")
                            .append(arg.getName()).append(";\n");
        });
    }

    private static void injectSetup(StringBuilder functionCode, AST.FunctionDef stmt, CompiledFunction func,
                                   CompileContext context) {
        if(func.getReturns() != BuiltinTypes.INT) {
            context.throwError("Main must return int", stmt.returns);
        }

        context.getScope().getAllVars()
                .stream()
                .filter(o->o instanceof CompiledType)
                .map(o -> (CompiledType) o)
                .filter(CompiledType::isRef)
                .forEach(t -> functionCode.append(context.getIndent()).append(t.getStaticInitName()).append("();\n"));
    }

    static String initiateInstance(CompiledType cls, CompileContext innerContext) {
        String className = cls.getName();
        String innerIndent = innerContext.getIndent() + CompileContext.INDENT;

        return innerContext.getIndent() +
                VisitorUtils.underscoreJoin("skiff", className, "static") +
                "();\n" +
                innerContext.getIndent() +
                "if(this == 0) \n" +
                 innerContext.getIndent() +
                "{ \n" +
                innerIndent +
                "this = skalloc(1, sizeof(" +
                cls.getStructName() +
                "));\n" +
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
