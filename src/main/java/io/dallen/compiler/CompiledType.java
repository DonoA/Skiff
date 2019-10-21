package io.dallen.compiler;

import io.dallen.compiler.visitor.VisitorUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CompiledType extends CompiledObject {

    private final boolean isRef;
    // order is vital for both declared vars and declared functions
    private final List<CompiledField> declaredVars = new ArrayList<>();
    private final List<CompiledField> declaredVarStructOrder = new ArrayList<>();
    private final List<CompiledMethod> declaredMethods = new ArrayList<>();
    private final List<CompiledFunction> constructors = new ArrayList<>();

    private final CompileScope declaredScope = new CompileScope(null);
    private CompileScope staticScope = new CompileScope(null);

    private List<String> genericOrder = new ArrayList<>();
    private CompiledType parent = null;
    private String structName;
    private String compiledName;
    private final String interfaceName;
    private final String interfaceStruct;
    private final String staticInitName;
    private boolean genericPlaceholder = false;
    private boolean generic = false;
    private final boolean dataClass;

    public CompiledType(String className, boolean ref, boolean dataClass) {
        super(className);
        this.structName = VisitorUtils.underscoreJoin("skiff", className, "t");
        this.interfaceName = VisitorUtils.underscoreJoin("skiff", className, "interface");
        this.interfaceStruct = VisitorUtils.underscoreJoin("skiff", className, "class", "struct");
        this.staticInitName = VisitorUtils.underscoreJoin("skiff", className, "static");
        this.compiledName = this.structName + (ref ? " *" : "");
        this.isRef = ref;
        this.dataClass = dataClass;
    }

    public String getStructName() {
        return this.structName;
    }

    public CompiledType addField(CompiledField obj) {
        declaredVars.add(obj);
        declaredScope.declareObject(obj);
        return this;
    }

    public boolean hasField(String name) {
        try {
            declaredScope.getObject(name);
        } catch(NoSuchElementException ex) {
            return false;
        }
        return true;
    }

    public CompiledField getField(String name) {
        return (CompiledField) declaredScope.getObject(name);
    }

    public CompiledType addMethod(CompiledMethod obj) {
        declaredMethods.add(obj);
        declaredScope.declareFunction(obj);
        return this;
    }

    public CompiledMethod getMethod(String name, List<CompiledType> args) {
        return (CompiledMethod) declaredScope.getFunction(name, args);
    }

    public boolean hasMethod(String name, List<CompiledType> args) {
        try {
            declaredScope.getFunction(name, args);
        } catch(NoSuchElementException ex) {
            return false;
        }
        return true;
    }

    public CompiledType addStaticMethod(CompiledMethod obj) {
        staticScope.declareFunction(obj);
        return this;
    }

    public CompiledMethod getStaticMethod(String name, List<CompiledType> args) {
        return (CompiledMethod) staticScope.getFunction(name, args);
    }

    public boolean hasStaticMethod(String name, List<CompiledType> args) {
        try {
            staticScope.getFunction(name, args);
        } catch(NoSuchElementException ex) {
            return false;
        }
        return true;
    }

    public List<CompiledMethod> getAllStaticMethods() {
        return (List) staticScope.getAllFuncs();
    }

    public CompiledType addStaticField(CompiledField obj) {
        staticScope.declareObject(obj);
        return this;
    }

    public CompiledField getStaticField(String name) {
        return (CompiledField) staticScope.getObject(name);
    }

    public CompiledType addConstructor(CompiledFunction func) {
        constructors.add(func);
        return this;
    }

    public List<CompiledFunction> getConstructors() {
        return constructors;
    }

    public List<CompiledField> getAllFields() {
        return this.declaredVars;
    }

    public List<CompiledField> getAllStaticFields() {
        return (List) staticScope.getAllVars();
    }

    public List<CompiledMethod> getAllMethods() {
        return this.declaredMethods;
    }

    public boolean isRef() {
        return isRef;
    }

    public CompiledType getParent() {
        return parent;
    }

    public CompiledType setParent(CompiledType parent) {
        this.parent = parent;
        return this;
    }

    public String getCompiledName() {
        return compiledName;
    }

    public CompiledType setCompiledName(String compiledName) {
        this.compiledName = compiledName;
        this.structName = compiledName;
        return this;
    }

    public CompiledType addGeneric(String name) {
        genericOrder.add(name);
        return this;
    }

    public CompiledType isGenericPlaceholder(boolean v) {
        this.genericPlaceholder = v;
        return this;
    }

    public boolean isGenericPlaceholder() {
        return this.genericPlaceholder;
    }

    public CompiledType isGeneric(boolean v) {
        this.generic = v;
        return this;
    }

    public boolean isGeneric() {
        return this.generic;
    }

    public CompiledType fillGenericTypes(List<CompiledType> genericList, boolean modifiyName) {
        Map<String, CompiledType> generics = new HashMap<>();

        ListIterator<String> genericNameItr = genericOrder.listIterator();

        genericList.forEach(g -> {
            generics.put(genericNameItr.next(), g);
        });

        String name = getName();
        if(modifiyName) {
            String appends = genericList.stream().map(CompiledObject::getName).collect(Collectors.joining(""));
            name = name + appends;
        }

        CompiledType filledType = new CompiledType(name, isRef, dataClass)
                .isGeneric(true)
                .setParent(parent);

        filledType.compiledName = compiledName;
        filledType.structName = structName;
        filledType.genericOrder = genericOrder;

        this.declaredVars.forEach(f -> {
            CompiledField post = f;
            if(f.getType().genericPlaceholder) {
                CompiledType comp = generics.get(f.getType().getName());
                post = new CompiledField(new CompiledVar(f.getName(), f.isParam(), comp), f.isPrivate(), f.isMine());
            }
            filledType.addField(post);
        });

        this.declaredVarStructOrder.forEach(f -> {
            filledType.declaredVarStructOrder.add(this.getField(f.getName()));
        });

        this.declaredMethods.forEach(m -> {
            CompiledFunction modFunc = fillFunction(generics, m);
            filledType.addMethod(new CompiledMethod(modFunc, m.isMine(), m.isPrivate()));
        });

        this.constructors.forEach(ctor -> {
            filledType.addConstructor(fillFunction(generics, ctor));
        });

        return filledType;
    }

    private CompiledFunction fillFunction(Map<String, CompiledType> generics, CompiledFunction func) {
        boolean newTypeNeeded = false;
        CompiledType returns = func.getReturns();
        if(func.getReturns().genericPlaceholder) {
            returns = generics.get(func.getReturns().getName());
            newTypeNeeded = true;
            returns.isGeneric(true);
        }
        List<CompiledVar> argTypes = func.getArgs();
        if(func.getArgs().stream().anyMatch(arg -> arg.getType().genericPlaceholder)) {
            newTypeNeeded = true;
            argTypes = func.getArgs().stream().map(arg -> {
                if(arg.getType().genericPlaceholder) {
                    return new CompiledVar(arg.getName(), false, generics.get(arg.getType().getName()).isGeneric(true));
                } else {
                    return arg;
                }
            }).collect(Collectors.toList());
        }

        if(newTypeNeeded) {
            return new CompiledFunction(func.getName(), func.getCompiledName(), false, returns, argTypes);
        } else {
            return func;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompiledType that = (CompiledType) o;
        return Objects.equals(structName, that.structName) &&
                Objects.equals(compiledName, that.compiledName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(structName, compiledName);
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public boolean isDataClass() {
        return dataClass;
    }

    public String getInterfaceStruct() {
        return interfaceStruct;
    }

    public String getStaticInitName() {
        return staticInitName;
    }

    public List<CompiledField> getDeclaredVarStructOrder() {
        return declaredVarStructOrder;
    }

    public void addToDeclaredVarStructOrder(CompiledField f) {
        this.declaredVarStructOrder.add(f);
    }
}
