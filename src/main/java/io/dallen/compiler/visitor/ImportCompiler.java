package io.dallen.compiler.visitor;

import io.dallen.SkiffC;
import io.dallen.ast.AST;
import io.dallen.ast.ASTEnums;
import io.dallen.compiler.CompileContext;
import io.dallen.compiler.CompiledCode;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ImportCompiler {
    static CompiledCode compileImportStatement(AST.ImportStatement stmt, CompileContext context) {
        String importText;

        if (stmt.type == ASTEnums.ImportType.NATIVE) {
            String location = new File(context.getFilename()).getParent() + "/" + stmt.value;
            StringBuilder importLocation = new StringBuilder();
            importLocation.append("#include \"");
            for (int i = 0; i < context.getDestFileName().length(); i++) {
                if (context.getDestFileName().charAt(i) == '/') {
                    importLocation.append("../");
                }
            }
            importLocation.append(location).append("\"\n");
            return new CompiledCode().withText(importLocation.toString());
        }

        String location;
        if (stmt.value.startsWith(".")) {
            location = new File(context.getFilename()).getParent() + "/" + stmt.value + ".skiff";
        } else {
            location = "lib/" + stmt.value + ".skiff";
        }

        try {
            importText = SkiffC.readFile(location);
        } catch (IOException e) {
            context.throwError("Cannot find import file", stmt);
            return new CompiledCode();
        }

        String currentFile = context.getFilename();
        context.setFilename(location);
        String currentText = context.getCode();
        context.setCode(importText);
        Optional<String> importCode = SkiffC.compile(importText, context);
        context.setFilename(currentFile);
        context.setCode(currentText);

        if (importCode.isEmpty()) {
            return new CompiledCode();
        }
        String text = "// Import " + stmt.value + "\n" + importCode.get() + "\n";
        return new CompiledCode()
                .withText(text);
    }

}
