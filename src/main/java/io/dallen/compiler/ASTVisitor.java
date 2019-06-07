package io.dallen.compiler;

import io.dallen.AST.*;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class ASTVisitor {

    public CompiledCode compileStatement(Statement stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Statement");
    }

    public CompiledCode compileExpression(Expression stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Expression");
    }

    public CompiledCode compileType(Type stmt, CompileContext context) {
        CompiledCode typeName = stmt.name.compile(this, context);
        String name = nativeTypeFor(typeName.getCompiledText());
        return new CompiledCode(name, typeName.getBinding(), CompiledType.CLASS);
    }

    private static String nativeTypeFor(String name) {
        switch(name) {
            case "Int":
                return "int32_t";
            default:
                return name;
        }
    }

    public CompiledCode compileBlockStatement(BlockStatement stmt, CompileContext context) {
        throw new UnsupportedOperationException("Cannot compile statement type Block");
    }

    public CompiledCode compileFunctionDef(FunctionDef stmt, CompileContext context) {
        StringBuilder sb = new StringBuilder();
        CompiledCode returns = stmt.returns.compile(this, context);

        CompileContext innerContext = new CompileContext(context);
        innerContext.addIndent("    ");

        sb.append(returns.getCompiledText());
        sb.append(" ");
        sb.append(stmt.name);
        sb.append("(");

        List<CompiledCode> compiledArgs = stmt.args
                .stream()
                .map(e->e.compile(this, context))
                .collect(Collectors.toList());

        ListIterator<FunctionParam> paramItr = stmt.args.listIterator();

        for (CompiledCode arg : compiledArgs) {
            innerContext.delcareObject(new CompiledVar(paramItr.next().name, arg.getType()));
        }

        List<String> stringArgs = compiledArgs
                .stream()
                .map(CompiledCode::getCompiledText)
                .collect(Collectors.toList());

        sb.append(String.join(", ", stringArgs));

        sb.append(")");

        context.delcareObject(new CompiledVar(stmt.name, CompiledType.FUNCTION));

        sb.append("\n{\n");

        stmt.body
                .stream()
                .map(e-> e.compile(this, innerContext))
                .forEach(e-> {
            sb.append(innerContext.getIndent());
            sb.append(e.getCompiledText());
            sb.append(";\n");
        });

        sb.append("}\n");

        return new CompiledCode(sb.toString(), CompiledType.NONE);
    }

    public CompiledCode compileFunctionParam(FunctionParam stmt, CompileContext context) {
        CompiledCode type = stmt.type.compile(this, context);
        StringBuilder sb = new StringBuilder();
        sb.append(type.getCompiledText());
        sb.append(" ");
        sb.append(stmt.name);
        return new CompiledCode(sb.toString(), type.getType());
    }

    public CompiledCode compileIfBlock(IfBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileWhileBlock(WhileBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileForBlock(ForBlock stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileFunctionCall(FunctionCall stmt, CompileContext context) {
//        CompiledObject nameVar = context.getVar(stmt.name);
//        if(!(nameVar instanceof CompiledFunction)) {
//            throw new CompileError("Variable not function " + stmt.name);
//        }
//
//        CompiledFunction func = (CompiledFunction) nameVar;
//
//        List<CompiledCode> compArgs = stmt.args.stream().map(e -> e.compile(this, context))
//                .collect(Collectors.toList());
//
//        if(func.getArgs().size() != compArgs.size()) {
//            throw new CompileError("Differing param count " + stmt.name);
//        }
//
//        ListIterator<CompiledType> expected = func.getArgs().listIterator();
//        ListIterator<CompiledCode> found = compArgs.listIterator();
//
//        while(expected.hasNext()) {
//            CompiledType typ1 = expected.next();
//            CompiledType typ2 = found.next().getReturnType();
//            if(!typ1.equals(typ2)) {
//                throw new CompileError("Differing param type " + stmt.name);
//            }
//        }
        return null;
    }

    public CompiledCode compileParened(Parened stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileDotted(Dotted stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileArrowed(Arrowed stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileReturn(Return stmt, CompileContext context) {
        String rtnVar = stmt.value.compile(this, context).getCompiledText();
        return new CompiledCode("return " + rtnVar, CompiledType.VOID);
    }

    public CompiledCode compileMathStatement(MathStatement stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileMathAssign(MathAssign stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileSubscript(Subscript stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileCompare(Compare stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileBoolCombine(BoolCombine stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileAssign(Assign stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileDeclare(Declare stmt, CompileContext context) {
        CompiledCode type = stmt.type.compile(this, context);
        context.delcareObject(new CompiledVar(stmt.name, type.getType()));

        StringBuilder sb = new StringBuilder();
        sb.append(type.getCompiledText());
        sb.append(stmt.name);
        return new CompiledCode(sb.toString(), CompiledType.VOID);
    }

    public CompiledCode compileDeclareAssign(DeclareAssign stmt, CompileContext context) {
        return null;
    }

    public CompiledCode compileNumberLiteral(NumberLiteral stmt, CompileContext context) {
        return new CompiledCode(stmt.value.toString(), context.getType("Int"));
    }

    public CompiledCode compileStringLiteral(StringLiteral stmt, CompileContext context) {
        return new CompiledCode("\"" + stmt.value + "\"", context.getType("String"));
    }

    public CompiledCode compileVariable(Variable stmt, CompileContext context) {
        CompiledObject compiledObject = context.getObject(stmt.name);
        CompiledType objType = CompiledType.CLASS;
        if(compiledObject instanceof CompiledVar) {
            objType = ((CompiledVar) compiledObject).getType();
        }
        return new CompiledCode(stmt.name, compiledObject, objType);
    }

}
