package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.*;

import java.util.stream.Collectors;

class DottedCompiler {

    static CompiledCode compileDotted(AST.Dotted stmt, CompileContext context) {
        CompiledCode lhs = stmt.left.compile(context);
        if(stmt.right instanceof AST.FunctionCall) {
            return compileFunctionDot(lhs, (AST.FunctionCall) stmt.right, context);
        }
        if(stmt.right instanceof AST.Variable) {
            return compileVarDot(lhs, (AST.Variable) stmt.right, context);
        }
        context.throwError("Dotted on invalid type", stmt);
        return new CompiledCode()
                .withText("")
                .withType(CompiledType.VOID);
    }

    private static CompiledCode compileFunctionDot(CompiledCode lhs, AST.FunctionCall call, CompileContext context) {
        CompiledType.CompiledMethod func;
        boolean isStatic = lhs.getBinding() instanceof CompiledType;
        if(isStatic) {
            CompiledType clazz = (CompiledType) lhs.getBinding();
            func = clazz.getStaticMethod(call.name);
        } else {
            func = lhs.getType().getMethod(call.name);
        }
        if(func == null) {
            context.throwError("Method not found", call);
            return new CompiledCode()
                    .withText("")
                    .withType(CompiledType.VOID);
        }
        if(func.isPrivate() && (context.getParentClass() == null || !context.getParentClass().equals(lhs.getType()))) {
            context.throwError("Cannot access private field", call);
        }
        StringBuilder sb = new StringBuilder();
        if(isStatic) {
            sb.append(func.getCompiledName()).append("(");
        } else {
            String deref = (lhs.onStack() ? "*" : "");
            sb.append("(").append(deref).append(lhs.getCompiledText()).append(")->class_ptr->").append(func.getName())
                    .append("(").append(deref).append(lhs.getCompiledText());
            if(!call.args.isEmpty()) {
                sb.append(", ");
            }
        }

        sb.append(call.args.stream()
                .map(e -> e.compile(context))
                .map(arg -> {
                    if(arg.onStack()) {
                        return  "*(" + arg.getCompiledText() + ")";
                    }
                    return arg.getCompiledText();
                })
                .collect(Collectors.joining(", ")));
        sb.append(")");

        return new CompiledCode()
                .withText(sb.toString())
                .withType(func.getReturns());
    }

    private static CompiledCode compileVarDot(CompiledCode lhs, AST.Variable v, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        CompiledType.CompiledField obj;
        boolean isStatic = lhs.getBinding() instanceof CompiledType;
        if(isStatic) {
            obj = ((CompiledType) lhs.getBinding()).getStaticField(v.name);
        } else {
            obj = lhs.getType().getField(v.name);
        }
        if(obj == null) {
            context.throwError("No such field", v);
            return new CompiledCode()
                    .withText("")
                    .withType(CompiledType.VOID);
        }
        if(obj.isPrivate() && (context.getParentClass() == null || !context.getParentClass().equals(lhs.getType()))) {
            context.throwError("Cannot access private field", v);
        }
        if(isStatic) {
            CompiledType lhsType = ((CompiledType) lhs.getBinding());
            sb.append(lhsType.getInterfaceName()).append(".").append(v.name);
        } else {
            sb.append(lhs.onStack() ? "(*" : "(").append(lhs.getCompiledText()).append(")->").append(v.name);
        }
        return new CompiledCode()
                .withText(sb.toString())
                .withType(obj.getType());
    }

}
