package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.*;

import java.util.List;

public class AssignmentCompiler {
    static CompiledCode compileAssign(AST.Assign stmt, CompileContext context) {
        // Vars with different names must have different stack locations
        // Each stack location should represent exactly one named var
        CompiledCode value = stmt.value.compile(context);

        if(stmt.name instanceof AST.FunctionCall) {
            AST.FunctionCall func = (AST.FunctionCall) stmt.name;
            return compileDeconstructionAssign(value, func, context);
        } else if(stmt.name instanceof AST.Subscript) {
            AST.Subscript sub = (AST.Subscript) stmt.name;
            return compileSubscript(value, sub, context);
        } else {
            return compileSimpleAssign(value, stmt, context);
        }
    }

    private static CompiledCode compileDeconstructionAssign(CompiledCode value, AST.FunctionCall func,
                                                            CompileContext context) {
        CompiledType intoType = (CompiledType) context.getObject(func.name);

        StringBuilder sb = new StringBuilder();

        String deref = (value.onStack() ? "*" : "");

        for (int i = 0; i < func.args.size(); i++) {
            if(!(func.args.get(i) instanceof AST.Variable)) {
                context.throwError("Args of type deconstruction must be Variables", func.args.get(i));
                continue;
            }
            AST.Variable v = (AST.Variable) func.args.get(i);
            CompiledField field = intoType.getAllFields().get(i);
            sb.append(context.getIndent()).append(field.getType().getCompiledName());
            if(field.getType().isRef()) {
                sb.append("*");
            }
            sb.append(" ").append(v.name);
            if(field.getType().isRef()) {
                sb.append(" = skalloc_ref_stack()");
            }
            sb.append(";\n");
            sb.append(context.getIndent());
            if(field.getType().isRef()) {
                sb.append("*");
            }
            sb.append(v.name).append(" = ((").append(intoType.getCompiledName()).append(") ").append(deref)
                    .append(value.getCompiledText()).append(")->").append(field.getName()).append(";\n");

            context.declareObject(new CompiledVar(v.name, false, field.getType()));
            if(field.getType().isRef()) {
                context.addRefStackSize(1);
            }
        }

        return new CompiledCode()
                .withText(sb.toString())
                .withSemicolon(false);
    }

    private static CompiledCode compileSubscript(CompiledCode value, AST.Subscript stmt, CompileContext context) {
        CompiledCode left = stmt.left.compile(context);

        CompiledFunction subscrCall = left.getType().getMethod("assignSub",
                List.of(value.getType(), BuiltinTypes.INT));
        CompiledCode sub = stmt.sub.compile(context);
        String name = (left.onStack() ? "(*" : "(") + left.getCompiledText() + ")";

        boolean rhsDeRef = value.onStack() && value.getType().isRef();

        String text = name + "->class_ptr->" + subscrCall.getName() + "(" + name + ", " +
                (rhsDeRef ? "*(" : "(") + value.getCompiledText() + "), " +
                sub.getCompiledText() +")";

        return new CompiledCode()
                .withText(text)
                .withBinding(value.getBinding())
                .withType(value.getType());
    }

    private static CompiledCode compileSimpleAssign(CompiledCode value, AST.Assign stmt, CompileContext context) {
        CompiledCode name = stmt.name.compile(context);

        boolean lhsDeRef = name.onStack();
        if(name.getBinding() instanceof CompiledVar) {
            lhsDeRef = lhsDeRef && ((CompiledVar) name.getBinding()).getType().isRef();
        }

        boolean rhsDeRef = value.onStack() && value.getType().isRef();

        String cast = "";
        if(!name.getType().getName().equals(value.getType().getName())) {
            cast = "(" + name.getType().getCompiledName() + ")";
        }

        String text = (lhsDeRef ? "*" : "") + "(" + name.getCompiledText() + ") = " + cast +
                (rhsDeRef ? "*" : "") + "(" + value.getCompiledText() + ")";
        return new CompiledCode()
                .withText(text)
                .withBinding(name.getBinding())
                .withType(name.getType());
    }
}
