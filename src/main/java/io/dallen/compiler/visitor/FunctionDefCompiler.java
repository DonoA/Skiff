package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.ast.ASTEnums;
import io.dallen.ast.ASTOptional;
import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class FunctionDefCompiler {

    private static String generateReturnType(CompiledFunction func, CompileContext context) {
        if (func instanceof CompiledMethod && ((CompiledMethod) func).isConstructor()) {
            return context.getContainingClass().getCompiledName();
        }

        return func.getReturns().getCompiledName();
    }

    static String generateFuncName(boolean isConstructor, boolean isStatic, String stmtName, CompileContext context) {
        if (isConstructor) {
            String ctrId = String.valueOf(context.getContainingClass().getConstructors().size());
            return VisitorUtils.underscoreJoin("skiff", stmtName, "new", ctrId);
        }

        if (isStatic) {
            return VisitorUtils.underscoreJoin("skiff", "static", context.getScopePrefix(), stmtName);
        }

        return VisitorUtils.underscoreJoin("skiff", context.getScopePrefix(), stmtName);
    }

    private static CompiledFunction createCompiledFunc(boolean isConstructor, AST.FunctionDef stmt, CompileContext context) {

        CompileContext innerContext = new CompileContext(context);

        stmt.genericTypes.forEach(generic -> {
            innerContext.declareObject(new CompiledType(generic.name, true, false)
                    .setCompiledName("void *")
                    .isGenericPlaceholder(true)
                    .isGeneric(true));
        });

        CompiledCode returns = stmt.returns.compile(innerContext);

        if (!returns.getBinding().equals(BuiltinTypes.VOID) && isConstructor) {
            innerContext.throwError("Constructor must return void", stmt);
        }

        boolean isStatic = stmt.modifiers.contains(ASTEnums.DecModType.STATIC);

        String compiledName = generateFuncName(isConstructor, isStatic, stmt.name, innerContext);

        List<CompiledVar> compiledArgs = stmt.args
                .stream()
                .map(e -> {
                    CompiledCode arg = e.compile(innerContext);
                    List<CompiledType> genericType = e.type.genericTypes
                            .stream()
                            .map(gt -> gt.compile(innerContext).getBinding())
                            .map(gt -> (CompiledType) gt)
                            .collect(Collectors.toList());

                    CompiledType filledType = arg.getType().fillGenericTypes(genericType, false);
                    return new CompiledVar(e.name, false, filledType);
                })
                .collect(Collectors.toList());

        return new CompiledFunction(stmt.name, compiledName, compiledArgs, (CompiledType) returns.getBinding());
    }

    private static String generateSig(List<ASTEnums.DecModType> modTypes, CompiledFunction func, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append(generateReturnType(func, context));
        sb.append(" ");
        sb.append(func.getCompiledName());
        sb.append("(");

        List<String> stringArgs = new ArrayList<>();

        boolean isStatic = modTypes.contains(ASTEnums.DecModType.STATIC);

        if (context.getContainingClass() != null && !isStatic) {
            stringArgs.add(context.getContainingClass().getCompiledName() + " this");
        }

        stringArgs.addAll(func.getArgs()
                .stream()
                .map(arg -> {
                    String prefix = "";
                    if (arg.getType().isRef()) {
                        prefix = "formal_";
                    }
                    return arg.getType().getCompiledName() + " " + prefix + arg.getName();
                })
                .collect(Collectors.toList()));

        sb.append(String.join(", ", stringArgs));

        sb.append(")");

        return sb.toString();
    }

    static CompiledCode compileFunctionDef(CompiledFunction func, AST.FunctionDef dec, CompileContext context) {
        CompileContext innerContext = new CompileContext(context, true)
                .addIndent();

        dec.genericTypes.forEach(generic -> {
            innerContext.declareObject(new CompiledType(generic.name, true, false)
                    .setCompiledName("void *")
                    .isGenericPlaceholder(true)
                    .isGeneric(true));
        });

        func.getArgs().forEach(innerContext::declareObject);

        StringBuilder functionCode = new StringBuilder();

        functionCode.append(generateSig(dec.modifiers, func, context));
        functionCode.append("\n")
                .append(context.getIndent()).append("{\n");

        if (func.getName().equals("main")) {
            injectSetup(functionCode, func, dec.returns, innerContext);
        }

        boolean isConstructor = func instanceof CompiledMethod && ((CompiledMethod) func).isConstructor();
        if (isConstructor) {
            functionCode.append(initiateInstance(context.getContainingClass(), innerContext));
        }

        injectFormalCopy(functionCode, func, innerContext);

        dec.body.forEach(VisitorUtils.compileToStringBuilder(functionCode, innerContext));

        Optional<AST.Return> returnOptional = Optional.empty();

        if (dec.body.size() > 0 && dec.body.get(dec.body.size() - 1) instanceof AST.Return) {
            returnOptional = Optional.of((AST.Return) dec.body.get(dec.body.size() - 1));
        }


        if (!func.getReturns().equals(BuiltinTypes.VOID)) {
            boolean hasReturn = checkReturns(dec.body);
            if (!hasReturn) {
                context.throwError("Function with non void return type must end with a return statement", dec);
            }
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

    private static boolean checkReturns(List<AST.Statement> body) {
        AST.Statement last;
        if (body.size() != 0) {
            last = body.get(body.size() - 1);
        } else {
            return false;
        }

        if (last instanceof AST.Return) {
            return true;
        }

        if (last instanceof AST.IfBlock) {
            AST.IfBlock blk = (AST.IfBlock) last;
            if (blk.body.size() == 0 || !(blk.body.get(blk.body.size() - 1) instanceof AST.Return)) {
                return false;
            }
            ASTOptional<AST.ElseBlock> currElse = blk.elseBlock;
            while (currElse.isPresent()) {
                if (currElse.get() instanceof AST.ElseIfBlock) {
                    AST.ElseIfBlock elseBlock = (AST.ElseIfBlock) currElse.get();
                    if (elseBlock.on.body.size() == 0 ||
                            !(elseBlock.on.body.get(elseBlock.on.body.size() - 1) instanceof AST.Return)) {
                        return false;
                    }
                    currElse = elseBlock.on.elseBlock;
                } else {
                    AST.ElseAlwaysBlock elseBlock = (AST.ElseAlwaysBlock) currElse.get();
                    if (elseBlock.body.size() == 0 ||
                            !(elseBlock.body.get(elseBlock.body.size() - 1) instanceof AST.Return)) {
                        return false;
                    }
                    currElse = ASTOptional.empty();
                }
            }
            return true;
        }

        return false;
    }

    static CompiledCode compileFunctionDef(AST.FunctionDef stmt, CompileContext context) {

        boolean isConstructor = context.getContainingClass() != null &&
                stmt.name.equals(context.getContainingClass().getName());

        CompiledFunction func = createCompiledFunc(isConstructor, stmt, context);

        if (context.getContainingClass() == null) {
            context.declareObject(func);
        }

        if (stmt.modifiers.contains(ASTEnums.DecModType.NATIVE)) {
            return new CompiledCode();
        }

        return compileFunctionDef(func, stmt, context);
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

    private static void injectSetup(StringBuilder functionCode, CompiledFunction func, AST.Statement returns,
                                    CompileContext context) {
        if (func.getReturns() != BuiltinTypes.INT) {
            context.throwError("Main must return int", returns);
        }

        context.getScope().getAllVars()
                .stream()
                .filter(o -> o instanceof CompiledType)
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
                + ";\n" +
                innerContext.getIndent() +
                "}\n";

    }

    private static String generateReturns(Optional<AST.Return> returns, boolean isConstructor, CompileContext context,
                                          CompileContext innerContext) {
        StringBuilder sb = new StringBuilder();
        if (returns.isEmpty()) {
            VisitorUtils.cleanupScope(sb, innerContext, true);
        }

        if (isConstructor) {
            returns.ifPresent(returnz -> context.throwError("Constructor cannot return!", returnz));

            sb.append(innerContext.getIndent())
                    .append("return this;\n");
        }

        sb.append(context.getIndent());
        sb.append("}");

        return sb.toString();
    }

}
