package io.dallen.compiler.visitor;

import io.dallen.AST;
import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ClassDefCompiler {

    private static CompiledType compileClass(AST.ClassDef stmt, CompileContext context, CompileContext innerContext) {
        CompiledType cls = new CompiledType(stmt.name, true);

        stmt.genericTypes.forEach(generic -> {
            cls.addGeneric(generic.name);
            innerContext.declareObject(new CompiledType(generic.name, true).setCompiledName("void *").isGenericPlaceholder(true));
        });

        stmt.extendClass.ifPresentOrElse(ext -> {
            cls.setParent((CompiledType) ext.compile(context).getBinding());
        }, () -> cls.setParent(CompiledType.ANYREF));

        context.declareObject(cls);

        Map<String, CompiledVar> declaredVars = new HashMap<>();
        Map<String, CompiledFunction> declaredMethods = new HashMap<>();

        // Collect Vars declared in this class
        for(AST.Statement s : stmt.body) {
            if(s instanceof AST.Declare) {
                AST.Declare dec = (AST.Declare) s;
                declaredVars.put(dec.name, new CompiledVar(dec.name, false,
                        (CompiledType) dec.type.compile(innerContext).getBinding()));
            } else if(s instanceof AST.FunctionDef) {
                AST.FunctionDef dec = (AST.FunctionDef) s;
                boolean isConstructor = dec.name.equals(cls.getName());
                String compiledName = VisitorUtils.generateFuncName(isConstructor, innerContext, dec.name);
                CompiledType returns = (CompiledType) dec.returns.compile(innerContext).getBinding();
                List<CompiledType> argTypes = dec.args.stream().map(arg -> arg.type.compile(innerContext).getBinding())
                        .map(obj -> (CompiledType) obj).collect(Collectors.toList());
                CompiledFunction func = new CompiledFunction(dec.name, compiledName, isConstructor, returns, argTypes);
                if(isConstructor) {
                    cls.addConstructor(func);
                } else {
                    declaredMethods.put(dec.name, func);
                }
            } else {
                throw new CompileError("Class statements must be function defs or Declares");
            }
        }

        cls.getParent().getAllFields().forEach(f -> {
            if(declaredVars.containsKey(f.getName())) {
                throw new CompileError("Cannot have var with same name in super");
            }
            cls.addField(f);
        });
        declaredVars.values().forEach(cls::addField);

        cls.getParent().getAllMethods()
                .stream()
                .filter(f -> !f.isConstructor())
                .forEach(f -> {
                    CompiledFunction override = declaredMethods.get(f.getName());
                    if(override != null) {
                        cls.addMethod(new CompiledType.CompiledMethod(override, true));
                    } else {
                        cls.addMethod(new CompiledType.CompiledMethod(f, false));
                    }
                });

        declaredMethods.values().forEach(v -> {
            if(cls.getMethod(v.getName()) == null) {
                cls.addMethod(new CompiledType.CompiledMethod(v, true));
            }
        });

        innerContext.declareObject(new CompiledVar("this", true, cls));
        innerContext.declareObject(new CompiledFunction("super", "super", List.of()));

        return cls;
    }

    static CompiledCode compileClassDef(AST.ClassDef stmt, CompileContext context) {
        CompileContext innerContext = new CompileContext(context)
                .setScopePrefix(stmt.name);

        CompiledType cls = compileClass(stmt, context, innerContext);

        innerContext.setParentClass(cls);

        String headerComment = "\n\n///////////////////// Start Class " + cls.getName() + " /////////////////////////\n\n";

        String typedef = "typedef struct " + VisitorUtils.underscoreJoin("skiff", cls.getName(), "struct") +
                " " + cls.getStructName() + ";\n";

        String functionForwardDecs = generateForwardDecs(cls) + "\n";

        String classStructName = VisitorUtils.underscoreJoin("skiff", cls.getName(), "class", "struct");

        String classStruct = generateClassStruct(cls, classStructName);

        String interfaceName = VisitorUtils.underscoreJoin("skiff", cls.getName(), "interface");

        String interfaceDec = "struct " + classStructName + " " + interfaceName + ";\n";

        String staticInitFunc = generateStaticInit(cls, interfaceName);

        String dataStruct = generateDataStruct(cls, classStructName);

        String methodCode = generateMethodCode(stmt.body, innerContext);

        String footerComment = "\n\n///////////////////// End Class " + cls.getName() + " /////////////////////////\n\n";

        String text = headerComment +
                typedef +
                functionForwardDecs +
                classStruct +
                interfaceDec +
                staticInitFunc +
                dataStruct +
                methodCode +
                footerComment;

        return new CompiledCode()
                .withType(CompiledType.VOID)
                .withText(text)
                .withBinding(cls)
                .withSemicolon(false);
    }

    private static String generateMethodCode(List<AST.Statement> body, CompileContext innerContext) {
        return body.stream()
                .filter(f -> f instanceof AST.FunctionDef)
                .map(f->f.compile(innerContext))
                .map(CompiledCode::getCompiledText)
                .collect(Collectors.joining("\n\n"));
    }

    private static String generateDataStruct(CompiledType cls, String classStructName) {
        List<VisitorUtils.StructEntry> entries = new ArrayList<>();
        entries.add(new VisitorUtils.StructEntry("struct " + classStructName,  "* class_ptr"));

        entries.addAll(cls.getAllFields().stream()
                .map(field -> new VisitorUtils.StructEntry(field.getType().getCompiledName(), field.getName()))
                .collect(Collectors.toList()));

        return VisitorUtils.compileStruct(
                VisitorUtils.underscoreJoin("skiff", cls.getName(), "struct"),
                "", CompileContext.INDENT,
                entries
        );
    }

    private static String generateClassStruct(CompiledType cls, String classStructName) {
        List<VisitorUtils.StructEntry> structEntries = cls.getAllMethods()
                .stream()
                .map(method -> new VisitorUtils.StructEntry(
                        method.getReturns().getCompiledName(),
                        "(*" + method.getName() + ")()"
                ))
                .collect(Collectors.toList());

        return VisitorUtils.compileStruct(classStructName, "", CompileContext.INDENT, structEntries);
    }

    private static String generateStaticInit(CompiledType cls, String interfaceName) {
        StringBuilder text = new StringBuilder();

        text.append("void ").append(VisitorUtils.underscoreJoin("skiff", cls.getName(), "static"))
                .append("()").append("\n{\n");

        cls.getAllMethods()
                .forEach(method ->
                    text.append(CompileContext.INDENT).append(interfaceName).append(".").append(method.getName())
                            .append(" = ").append(method.getCompiledName()).append(";\n"));

        text.append("}\n");

        return text.toString();
    }

    private static String generateForwardDecs(CompiledType cls) {
        return cls.getAllMethods()
                .stream()
                .filter(CompiledType.CompiledMethod::isMine)
                .map(method -> sigFor(cls, method))
                .collect(Collectors.joining("\n"));
    }

    private static String sigFor(CompiledType cls, CompiledType.CompiledMethod method) {
        StringBuilder text = new StringBuilder();

        text.append(method.getReturns().getCompiledName()).append(" ").append(method.getCompiledName()).append("(");
        List<String> argList = new ArrayList<>();
        argList.add(cls.getCompiledName());
        argList.addAll(method.getArgs().stream().map(CompiledType::getCompiledName)
                .collect(Collectors.toList()));
        text.append(String.join(", ", argList)).append(");");

        return text.toString();
    }
}
