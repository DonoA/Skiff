package io.dallen.compiler.visitor;

import io.dallen.ast.AST;
import io.dallen.compiler.CompileContext;
import io.dallen.compiler.CompiledCode;
import io.dallen.compiler.CompiledFunction;
import io.dallen.compiler.CompiledType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NewCompiler {
    static CompiledCode compileNew(AST.New stmt, CompileContext context) {
        // TODO: extract this
        CompiledType typeCode = (CompiledType) stmt.type.compile(context).getBinding();
        List<CompiledType> genericTypes = stmt.type.genericTypes
                .stream()
                .map(type -> {
                    CompiledType typ = (CompiledType) type.compile(context).getBinding();
                    if (typ == null) {

                    }
                    return typ;
                })
                .collect(Collectors.toList());
        List<CompiledCode> compiledArgs = stmt.argz.stream().map(arg -> arg.compile(context))
                .collect(Collectors.toList());

        CompiledFunction ctr = typeCode.getConstructor(compiledArgs.stream().map(CompiledCode::getType)
                .collect(Collectors.toList()));

        StringBuilder sb = new StringBuilder();
        sb.append(ctr.getCompiledName()).append("(");
        List<String> argz = new ArrayList<>();
        argz.add("0");
        for (int i = 0; i < stmt.argz.size(); i++) {
            String argType = "";
            CompiledCode argCode = compiledArgs.get(i);
            String argText = argCode.getCompiledText();
            if (argCode.onStack() && argCode.getType().isRef()) {
                argText = "*" + argText;
            }
            if (ctr.getArgs().get(i).getType().isGenericPlaceholder()) {
                argType = "(void *)";
            }
            argz.add(argType + argText);
        }

        sb.append(String.join(", ", argz))
                .append(")");

        CompiledType exactType = typeCode.fillGenericTypes(genericTypes, false);

        boolean requiresCopy = !genericTypes.stream().allMatch(CompiledType::isRef);

        if (requiresCopy) { // TODO: enable class duplication to support native types
//            CompileContext newClassContext = new CompileContext(context)
//                    .setContainingClass(exactType).setIndent("")
//                    .setScopePrefix(exactType.getName());
//
//            ClassDefCompiler.prepareContext(exactType, exactType.getOriginalDef(), newClassContext);
//
//            CompiledCode newClass = ClassDefCompiler.compileClassInstance(exactType, newClassContext);
//            context.addDependentCode(newClass.getCompiledText());
        }

        return new CompiledCode()
                .withType(exactType)
                .withText(sb.toString());
    }
}
