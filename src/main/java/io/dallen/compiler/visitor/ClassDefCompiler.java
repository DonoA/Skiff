package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.ast.ASTEnums;
import io.dallen.compiler.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class ClassDefCompiler {

    private static CompiledMethod createCompiledFunc(AST.FunctionDef dec, boolean isConstructor, boolean isStatic,
                                                     boolean isPrivate, CompileContext innerContext) {
        String compiledName = VisitorUtils.generateFuncName(isConstructor, isStatic, innerContext, dec.name);
        CompiledType returns = (CompiledType) dec.returns.compile(innerContext).getBinding();
        List<CompiledType> argTypes = dec.args.stream().map(arg -> arg.type.compile(innerContext).getBinding())
                .map(obj -> (CompiledType) obj).collect(Collectors.toList());
        return new CompiledMethod(
                new CompiledFunction(dec.name, compiledName, isConstructor, returns, argTypes),
                true, isPrivate);
    }

    private static CompiledField createCompiledField(AST.Declare dec, boolean isPrivate, CompileContext innerContext) {
        return new CompiledField(
                new CompiledVar(
                        dec.name,
                        false,
                        (CompiledType) dec.type.compile(innerContext).getBinding()),
                isPrivate
        );
    }

    private static void checkCollision(CompiledType cls, AST.Declare dec, CompileContext context) {
        boolean checkNameCollision = cls.getParent()
                .getAllFields()
                .stream()
                .map(CompiledObject::getName)
                .anyMatch(f -> f.equals(dec.name));

        if(checkNameCollision) {
            context.throwError("Cannot have var with same name in super", dec);
        }
    }

    private static void collectDecs(AST.Statement s, Map<String, CompiledField> declaredVars,
                                    Map<String, CompiledMethod> declaredMethods, CompiledType cls,
                                    CompileContext context, CompileContext innerContext) {

        if(s instanceof AST.Declare) {
            // TODO: These decs must be ordered if this is a data class, maybe refactor this methods to be a
            // different method for data classes
            AST.Declare dec = (AST.Declare) s;
            declaredVars.put(dec.name, createCompiledField(dec, false, innerContext));
            checkCollision(cls, dec, context);

        } else if(s instanceof AST.FunctionDef) {
            if(cls.isDataClass()) {
                context.throwError("Data classes have no methods", s);
            }
            AST.FunctionDef dec = (AST.FunctionDef) s;
            boolean isConstructor = dec.name.equals(cls.getName());
            CompiledMethod func = createCompiledFunc(dec, isConstructor, false, false, innerContext);
            if(isConstructor) {
                cls.addConstructor(func);
            } else {
                declaredMethods.put(dec.name, func);
            }
        } else if(s instanceof AST.FunctionDefModifier) {
            if(cls.isDataClass()) {
                context.throwError("Data classes have no methods", s);
            }
            AST.FunctionDefModifier mod = (AST.FunctionDefModifier) s;
            boolean isStatic = mod.type == ASTEnums.DecModType.STATIC;
            boolean isPrivate = mod.type == ASTEnums.DecModType.PRIVATE;

            CompiledMethod func = createCompiledFunc(mod.on, false, isStatic, isPrivate, innerContext);

            if(isStatic) {
                cls.addStaticMethod(func);
            } else {
                declaredMethods.put(mod.on.name, func);
            }
        } else if(s instanceof AST.FieldModifier) {
            if(cls.isDataClass()) {
                context.throwError("Data classes cannot have static or private fields", s);
            }
            AST.FieldModifier mod = (AST.FieldModifier) s;
            boolean isStatic = mod.type == ASTEnums.DecModType.STATIC;
            boolean isPrivate = mod.type == ASTEnums.DecModType.PRIVATE;

            CompiledField field = createCompiledField(mod.on, isPrivate, innerContext);
            if(isStatic) {
                cls.addStaticField(field);
            } else {
                declaredVars.put(mod.on.name, field);
                checkCollision(cls, mod.on, context);
            }
        } else {
            context.throwError("Class and Struct statements must be method or field declarations", s);
        }
    }

    private static String capNameFor(String name) {
        if(Character.isLowerCase(name.charAt(0))) {
            return String.valueOf(Character.toUpperCase(name.charAt(0))) +
                    name.substring(1);
        }
        return name;
    }

    private static void generateDataClassMethods(CompiledType cls) {
        List<CompiledType> args = cls.getAllFields().stream().map(CompiledVar::getType).collect(Collectors.toList());

        cls.addConstructor(new CompiledFunction(
                cls.getName(),
                VisitorUtils.underscoreJoin("skiff", cls.getName(), "new"),
                true,
                BuiltinTypes.VOID,
                args));

        cls.getAllFields().forEach(f -> {
            String fName = capNameFor(f.getName());
            cls.addMethod(new CompiledMethod(new CompiledFunction(
                "get" + fName,
                    VisitorUtils.underscoreJoin("skiff", cls.getName(), "get", f.getName()),
                    false,
                    f.getType(),
                    List.of()
            ), true, false));

            cls.addMethod(new CompiledMethod(new CompiledFunction(
                    "set" + fName,
                    VisitorUtils.underscoreJoin("skiff", cls.getName(), "set", f.getName()),
                    false,
                    BuiltinTypes.VOID,
                    List.of(f.getType())
            ), true, false));
        });
    }

    private static CompiledType compileClass(AST.ClassDef stmt, CompileContext context, CompileContext innerContext)
            throws CompileException {
        CompiledType cls = new CompiledType(stmt.name, true, stmt.isStruct);

        // Declare generic order
        stmt.genericTypes.forEach(generic -> {
            cls.addGeneric(generic.name);
            innerContext.declareObject(new CompiledType(generic.name, true, false)
                    .setCompiledName("void *")
                    .isGenericPlaceholder(true));
        });

        // Set parent class or default if none found
        stmt.extendClass.ifPresentOrElse(ext ->
                cls.setParent((CompiledType) ext.compile(context).getBinding()),
                () -> cls.setParent(BuiltinTypes.ANYREF)
        );

        context.declareObject(cls);

        Map<String, CompiledField> declaredVars = new HashMap<>();
        Map<String, CompiledMethod> declaredMethods = new HashMap<>();

        // Collect fields and methods declared in this class
        for(AST.Statement s : stmt.body) {
            collectDecs(s, declaredVars, declaredMethods, cls, context, innerContext);
        }

        // Copy in parent fields to ensure order is maintained
        cls.getParent().getAllFields().forEach(cls::addField);
        // Copy in declared fields
        declaredVars.values().forEach(cls::addField);

        // Copy in parent methods that are no overridden
        cls.getParent().getAllMethods()
                .stream()
                .filter(f -> !f.isConstructor())
                .forEach(f -> {
                    CompiledFunction override = declaredMethods.get(f.getName());
                    if(override != null) {
                        cls.addMethod(new CompiledMethod(override, true, f.isPrivate()));
                    } else {
                        cls.addMethod(new CompiledMethod(f, false, f.isPrivate()));
                    }
                });

        // Copy in declared methods if they were not already copied in above
        declaredMethods.values().forEach(v -> {
            if(cls.getMethod(v.getName()) == null) {
                cls.addMethod(new CompiledMethod(v, true, v.isPrivate()));
            }
        });

        // Declare special class keywords
        innerContext.declareObject(new CompiledVar("this", true, cls));
        cls.getParent().getConstructors().forEach(ctr ->
                innerContext.declareObject(new CompiledFunction("super", "super", ctr.getArgs()))
        );

        if(cls.isDataClass()) {
            generateDataClassMethods(cls);
        }

        return cls;
    }

    static CompiledCode compileClassDef(AST.ClassDef stmt, CompileContext context) {
        CompileContext innerContext = new CompileContext(context)
                .setScopePrefix(stmt.name);

        CompiledType cls = compileClass(stmt, context, innerContext);

        innerContext.setContainingClass(cls);

        String headerComment = "\n\n///////////////////// Start Class " + cls.getName() + " /////////////////////////\n\n";

        String typedef = "typedef struct " + VisitorUtils.underscoreJoin("skiff", cls.getName(), "struct") +
                " " + cls.getStructName() + ";\n";

        String functionForwardDecs = generateForwardDecs(cls) + "\n";

        String classStructName = VisitorUtils.underscoreJoin("skiff", cls.getName(), "class", "struct");

        String classStruct = generateClassStruct(cls, classStructName);

        String interfaceDec = "struct " + classStructName + " " + cls.getInterfaceName() + ";\n";

        String staticInitFunc = generateStaticInit(cls, cls.getInterfaceName());

        String dataStruct = generateDataStruct(cls, classStructName);

        String methodCode = generateMethodCode(stmt.body, innerContext);

        String dataClassCode = "";
        if(cls.isDataClass()) {
            dataClassCode = getDataClassCode(cls, context);
        }

        String footerComment = "\n\n///////////////////// End Class " + cls.getName() + " /////////////////////////\n\n";

        String text = headerComment +
                typedef +
                functionForwardDecs +
                classStruct +
                interfaceDec +
                staticInitFunc +
                dataStruct +
                methodCode +
                dataClassCode +
                footerComment;

        return new CompiledCode()
                .withText(text)
                .withBinding(cls)
                .withSemicolon(false);
    }

    private static String getDataClassCode(CompiledType cls, CompileContext context) {
        StringBuilder text = new StringBuilder();

        CompileContext ctrContext = new CompileContext(context).addIndent();
        CompiledFunction ctr = cls.getConstructors().get(0);
        text.append(cls.getCompiledName()).append(" ").append(ctr.getCompiledName())
                .append("(").append(cls.getCompiledName()).append(" this, int new_inst");

        cls.getAllFields()
                .stream()
                .map(arg -> arg.getType().getCompiledName() + " " + arg.getName())
                .forEach(arg -> text.append(", ").append(arg));

        text.append(")\n{\n");

        text.append(FunctionDefCompiler.initiateInstance(cls, ctrContext));

        cls.getAllFields()
                .stream()
                .map(arg -> "(this->" + arg.getName() + ") = " + arg.getName())
                .forEach(arg -> text.append(ctrContext.getIndent()).append(arg).append(";\n"));

        text.append(ctrContext.getIndent()).append("return this;\n");
        text.append("}\n");

        cls.getAllFields().forEach(f -> {
            String fName = capNameFor(f.getName());
            CompiledMethod getter = cls.getMethod("get" + fName);
            text.append(getter.getReturns().getCompiledName()).append(" ")
                    .append(getter.getCompiledName())
                    .append("(").append(cls.getCompiledName()).append("this)\n")
                    .append("{\n")
                    .append(CompileContext.INDENT).append("return (this)->").append(f.getName()).append(";\n")
                    .append("}\n");

            CompiledMethod setter = cls.getMethod("set" + fName);
            text.append("void").append(" ")
                    .append(setter.getCompiledName())
                    .append("(").append(cls.getCompiledName()).append("this, ")
                    .append(f.getType().getCompiledName()).append(" new_value)\n")
                    .append("{\n")
                    .append(CompileContext.INDENT).append("(this)->").append(f.getName()).append(" = new_value;\n")
                    .append("}\n");
        });
        return text.toString();
    }

    private static String generateMethodCode(List<AST.Statement> body, CompileContext innerContext) {
        String simple = body.stream()
                .filter(f -> f instanceof AST.FunctionDef)
                .map(f->f.compile(innerContext))
                .map(CompiledCode::getCompiledText)
                .collect(Collectors.joining("\n\n"));

        String modified = body.stream()
                .filter(f -> f instanceof AST.FunctionDefModifier)
                .map(f -> (AST.FunctionDefModifier) f)
                .map(f-> FunctionDefCompiler.compileFunctionDef(f.on, f.type == ASTEnums.DecModType.STATIC,
                        innerContext))
                .map(CompiledCode::getCompiledText)
                .collect(Collectors.joining("\n\n"));

        return simple + "\n\n" + modified;
    }

    private static String generateDataStruct(CompiledType cls, String classStructName) {
        List<VisitorUtils.StructEntry> entries = new ArrayList<>();
        entries.add(new VisitorUtils.StructEntry("struct " + classStructName,  "* class_ptr"));

        entries.addAll(cls.getAllFields().stream()
                .map(field -> new VisitorUtils.StructEntry(field.getType().getCompiledName(), field.getName()))
                .collect(Collectors.toList()));

        return VisitorUtils.compileStruct(
                VisitorUtils.underscoreJoin("skiff", cls.getName(), "struct"),
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

        cls.getAllStaticFields()
                .stream()
                .map(f -> new VisitorUtils.StructEntry(
                    f.getType().getCompiledName(), f.getName()
                )).forEach(structEntries::add);

        return VisitorUtils.compileStruct(classStructName, structEntries);
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
                .filter(CompiledMethod::isMine)
                .map(method -> sigFor(cls, method))
                .collect(Collectors.joining("\n"));
    }

    private static String sigFor(CompiledType cls, CompiledMethod method) {
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
