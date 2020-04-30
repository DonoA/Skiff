package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.*;

public class SwitchMatchCompiler {

    public static CompiledCode compileMatchBlock(AST.MatchBlock stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        CompiledCode onCode = stmt.on.compile(context);
        String deref = (onCode.onStack() ? "*" : "");
        for (int i = 0; i < stmt.body.size(); i++) {
            if (!(stmt.body.get(i) instanceof AST.CaseMatchStatement)) {
                continue;
            }
            AST.CaseMatchStatement caseStatement = (AST.CaseMatchStatement) stmt.body.get(i);
            if (i != 0) {
                sb.append(context.getIndent()).append("else ");
            }

            CompileContext innerContext = new CompileContext(context).addIndent();
            sb.append("if(instance_of((skiff_any_ref_t *)").append(deref).append(onCode.getCompiledText()).append(", &");
            if (caseStatement.on instanceof AST.FunctionCall) {
                AST.FunctionCall func = (AST.FunctionCall) caseStatement.on;
                try {
                    sb.append(compileDeconstructCase(func, context, innerContext, deref, onCode));
                } catch(CompileException ex) {
                    context.throwError(ex.getMessage(), ex.getStatement());
                    return new CompiledCode();
                }
            } else if (caseStatement.on instanceof AST.Declare) {
                AST.Declare dec = (AST.Declare) caseStatement.on;
                CompiledType type = (CompiledType) dec.type.compile(context).getBinding();
                sb.append(type.getInterfaceName()).append("))\n")
                        .append(context.getIndent()).append("{\n")
                        .append(innerContext.getIndent()).append(dec.compile(innerContext).getCompiledText()).append(";\n")
                        .append(innerContext.getIndent()).append("*").append(dec.name).append(" = (")
                        .append(type.getCompiledName()).append(") *").append(onCode.getCompiledText()).append(";\n");
            } else {
                context.throwError("Invalid statement type", caseStatement.on);
            }
            for (int j = i + 1; j < stmt.body.size(); j++) {
                if (stmt.body.get(j) instanceof AST.BreakStatement) {
                    break;
                }
                if (stmt.body.get(j) instanceof AST.CaseMatchStatement) {
                    continue;
                }
                CompiledCode innerCode = stmt.body.get(j).compile(innerContext);
                sb.append(innerContext.getIndent()).append(innerCode.getCompiledText());
                if (innerCode.isRequiresSemicolon()) {
                    sb.append(";");
                }
                sb.append("\n");
            }
            VisitorUtils.cleanupScope(sb, innerContext, true);
            sb.append(context.getIndent()).append("}\n");
        }
        return new CompiledCode()
                .withText(sb.toString())
                .withSemicolon(false);
    }

    private static StringBuilder compileDeconstructCase(AST.FunctionCall func, CompileContext context, CompileContext innerContext,
                                               String deref, CompiledCode onCode) throws CompileException {
        CompiledObject intoObj = context.getObject(func.name);
        if (!(intoObj instanceof CompiledType)) {
            throw new CompileException("Match type is not a type", func);
        }
        CompiledType into = (CompiledType) intoObj;
        if (!into.isDataClass()) {
            throw new CompileException("Cannot deconstruct non data class", func);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(into.getInterfaceName()).append("))\n")
                .append(context.getIndent()).append("{\n");
        func.args.forEach(stmtArg -> {
            if (!(stmtArg instanceof AST.Variable)) {
                context.throwError("Args of type deconstruction must be Variables", stmtArg);
                return;
            }
            AST.Variable v = (AST.Variable) stmtArg;
            CompiledField field = into.getField(v.name);
            sb.append(innerContext.getIndent()).append(field.getType().getCompiledName());
            if (field.getType().isRef()) {
                sb.append("*");
            }
            sb.append(" ").append(v.name);
            if (field.getType().isRef()) {
                sb.append(" = skalloc_ref_stack()");
            }
            sb.append(";\n");
            sb.append(innerContext.getIndent());
            if (field.getType().isRef()) {
                sb.append("*");
            }
            sb.append(v.name).append(" = ((").append(into.getCompiledName()).append(") ").append(deref)
                    .append(onCode.getCompiledText()).append(")->").append(field.getName()).append(";\n");

            innerContext.declareObject(new CompiledVar(v.name, false, field.getType()));
            if (field.getType().isRef()) {
                innerContext.addRefStackSize(1);
            }
        });
        return sb;
    }

    public static CompiledCode compileSwitchBlock(AST.SwitchBlock stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        CompiledCode onCode = stmt.on.compile(context);
        for (int i = 0; i < stmt.body.size(); i++) {
            if (!(stmt.body.get(i) instanceof AST.CaseStatement)) {
                continue;
            }
            AST.CaseStatement caseStatement = (AST.CaseStatement) stmt.body.get(i);
            if (i != 0) {
                sb.append(context.getIndent()).append("else ");
            }
            CompiledCode equalCode = caseStatement.on.compile(context);
            sb.append("if(").append(onCode.getCompiledText()).append(" == ")
                    .append(equalCode.getCompiledText()).append(")\n").append(context.getIndent()).append("{\n");
            CompileContext innerContext = new CompileContext(context).addIndent();
            for (int j = i + 1; j < stmt.body.size(); j++) {
                if (stmt.body.get(j) instanceof AST.BreakStatement) {
                    break;
                }
                if (stmt.body.get(j) instanceof AST.CaseStatement) {
                    continue;
                }
                CompiledCode innerCode = stmt.body.get(j).compile(innerContext);
                sb.append(innerContext.getIndent()).append(innerCode.getCompiledText());
                if (innerCode.isRequiresSemicolon()) {
                    sb.append(";");
                }
                sb.append("\n");
            }
            sb.append(context.getIndent()).append("}\n");
        }
        return new CompiledCode()
                .withText(sb.toString())
                .withSemicolon(false);
    }
}
