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

        innerContext.declareObject(new CompiledVar("this", true, cls));

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

        String text =
                generateForwardDecs(cls, compiledMethods) +
                generateStatics(cls, fields, compiledMethods, context) +
                generateStruct(cls, fields) +
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

    private static String generateStatics(CompiledType cls, List<CompiledCode> fields,
                                          List<VisitorUtils.FunctionSig> compiledMethods,
                                          CompileContext context) {
        StringBuilder text = new StringBuilder();

        //struct skiff_person_class_struct
        //{
        //    skiff_string_t * (*get_name)(skiff_person_t *);
        //    void (*inc_age)(skiff_person_t *);
        //}

        String classStructName = VisitorUtils.underscoreJoin("skiff", cls.getName(), "class", "struct");

        List<VisitorUtils.StructEntry> structEntries = compiledMethods
                .stream()
                .filter(method -> !method.getFunction().isConstructor())
                .map(VisitorUtils.FunctionSig::getFunction)
                .map(method -> new VisitorUtils.StructEntry(
                    method.getReturns().getCompiledName() + (method.getReturns().isRef() ? "*" : ""),
                    "(*" + method.getName() + ")()"
                ))
                .collect(Collectors.toList());

        text.append(VisitorUtils.compileStruct(classStructName, "", CompileContext.INDENT, structEntries));

        //struct skiff_person_class_struct skiff_person_interface;
        String interfaceName = VisitorUtils.underscoreJoin("skiff", cls.getName(), "interface");
        text.append("struct ")
                .append(classStructName)
                .append(" ")
                .append(interfaceName)
                .append(";\n");

        //void skiff_person_static()
        //{
        //    skiff_person_interface.get_name = skiff_person_get_name;
        //    skiff_person_interface.inc_age = skiff_person_inc_age;
        //}
        text.append("void ").append(VisitorUtils.underscoreJoin("skiff", cls.getName(), "static"))
                .append("()").append("\n{\n");

        compiledMethods
                .stream()
                .filter(method -> !method.getFunction().isConstructor())
                .map(VisitorUtils.FunctionSig::getFunction)
                .forEach(method -> {
                    text.append(CompileContext.INDENT).append(interfaceName).append(".").append(method.getName())
                            .append(" = ").append(method.getCompiledName()).append(";\n");
                });

        text.append("}\n");

        return text.toString();
    }

    private static String generateStruct(CompiledType cls, List<CompiledCode> fields) {
        StringBuilder text = new StringBuilder();

        text.append("struct ")
                .append(VisitorUtils.underscoreJoin("skiff", cls.getName(), "struct"))
                .append("\n{\n");

        // struct skiff_person_class_struct * class_ptr
        String classStructName = VisitorUtils.underscoreJoin("skiff", cls.getName(), "class", "struct");
        text.append(CompileContext.INDENT).append("struct ").append(classStructName).append(" * ").append("class_ptr;\n");

        fields.forEach(code -> {
            text.append(CompileContext.INDENT);
            text.append(code.getCompiledText());
            text.append(";\n");
        });

        text.append("};\n\n");

        return text.toString();
    }

    private static String generateForwardDecs(CompiledType cls, List<VisitorUtils.FunctionSig> compiledMethods) {
        StringBuilder sb = new StringBuilder();
        sb.append("typedef struct ")
                .append(VisitorUtils.underscoreJoin("skiff", cls.getName(), "struct"))
                .append(" ")
                .append(VisitorUtils.underscoreJoin("skiff", cls.getName(), "t"))
                .append(";\n");

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
