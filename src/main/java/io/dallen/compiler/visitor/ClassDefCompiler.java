package io.dallen.compiler.visitor;

import io.dallen.AST;
import io.dallen.compiler.*;

import java.util.List;
import java.util.stream.Collectors;

class ClassDefCompiler {

    static CompiledCode compileClassDef(AST.ClassDef stmt, CompileContext context) {

        CompiledType cls = new CompiledType(stmt.name, -1)
                .setParent(CompiledType.ANYREF);

        context.declareObject(cls);

        CompileContext innerContext = new CompileContext(context)
                .setScopePrefix(stmt.name)
                .setParentClass(cls);

        innerContext.declareObject(new CompiledVar("this", cls));

        StringBuilder methods = new StringBuilder();

        List<CompiledCode> fields = compileFields(stmt.body, innerContext, cls);

        List<VisitorUtils.FunctionSig> compiledMethods = stmt.body
                .stream()
                .filter(e -> e instanceof AST.FunctionDef)
                .map(line -> extractMethod(cls, (AST.FunctionDef) line, innerContext))
                .collect(Collectors.toList());

        compiledMethods.forEach(method -> cls.addClassObject(method.getFunction()));

        stmt.body.stream()
                .filter(e  -> e instanceof AST.FunctionDef)
                .map(line -> line.compile(innerContext).getCompiledText())
                .forEach(text -> methods.append(text).append("\n\n"));

        String text = generateStruct(cls, fields) +
                generateForewardDecs(compiledMethods) +
                methods;

        return new CompiledCode()
                .withType(CompiledType.VOID)
                .withText(text)
                .withBinding(cls)
                .withSemicolon(false);
    }

    private static List<CompiledCode> compileFields(List<AST.Statement> lines, CompileContext innerContext,
                                                    CompiledType cls) {

        return lines.stream()
                .filter(line -> line instanceof AST.Declare || line instanceof AST.DeclareAssign)
                .map(line -> {
                    if(line instanceof AST.Declare){
                        CompileContext anonContext = new CompileContext(innerContext)
                                .setOnStack(false);
                        CompiledCode code = line.compile(anonContext);
                        cls.addClassObject(code.getBinding());
                        return code;
                    } else {
                        throw new CompileError("Declare assign not supported yet");
                    }
                })
                .collect(Collectors.toList());
    }

    private static String generateStruct(CompiledType cls, List<CompiledCode> fields) {
        StringBuilder text = new StringBuilder();
        text.append("typedef struct ")
                .append(CompileUtilities.underscoreJoin("skiff", cls.getName(), "struct"))
                .append(" ")
                .append(cls.getCompiledName())
                .append(";\n");

        text.append("struct ")
                .append(CompileUtilities.underscoreJoin("skiff", cls.getName(), "struct"))
                .append("\n{\n");

        fields.forEach(code -> {
            text.append("    ");
            text.append(code.getCompiledText());
            text.append(";\n");
        });

        text.append("};\n\n");

        return text.toString();
    }

    private static String generateForewardDecs(List<VisitorUtils.FunctionSig> compiledMethods) {
        StringBuilder sb = new StringBuilder();
        compiledMethods.forEach(method -> {
            sb.append(method.getText())
                    .append(";\n");
        });
        return sb.append("\n").toString();
    }

    private static VisitorUtils.FunctionSig extractMethod(CompiledType cls, AST.FunctionDef stmt, CompileContext context) {

        CompileContext innerContext = new CompileContext(context);

        boolean isConstructor = stmt.name.equals(cls.getName());

        CompiledCode returnType = stmt.returns.compile(context);

        return VisitorUtils.generateSig(isConstructor, context, returnType, stmt, innerContext);
    }
}
