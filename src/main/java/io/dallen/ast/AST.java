
package io.dallen.ast;

import io.dallen.compiler.visitor.ASTVisitor;
import io.dallen.compiler.CompileContext;
import io.dallen.compiler.CompiledCode;
import io.dallen.tokenizer.Token;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"Duplicates", "WeakerAccess", "Convert2MethodRef", "RedundantStringOperation", "OptionalUsedAsFieldOrParameterType"})
public class AST {

    public static class Statement  {
        public final int token_start;
        public final int token_end;
        public Statement(int token_start, int token_end) {
            
            this.token_start = token_start;
            this.token_end = token_end;
        }

        public String toString() {
            return "Statement(token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Statement(token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileStatement(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class Expression extends Statement {

        public Expression(int token_start, int token_end) {
            super(token_start, token_end);

        }

        public String toString() {
            return "Expression(token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Expression(token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileExpression(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class Program extends Statement {
        public final List<Statement> body;
        public final List<Token> tokens;
        public Program(List<Statement> body, List<Token> tokens, int token_start, int token_end) {
            super(token_start, token_end);
            this.body = body;
            this.tokens = tokens;
        }

        public String toString() {
            return "Program(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Program(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileProgram(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), this);
            }
            return new CompiledCode();
        }
    }
    
    public static class Type extends Statement {
        public final Statement name;
        public final List<Type> genericTypes;
        public Type(Statement name, List<Type> genericTypes, int token_start, int token_end) {
            super(token_start, token_end);
            this.name = name;
            this.genericTypes = genericTypes;
        }

        public String toString() {
            return "Type(name = " + String.valueOf(this.name) + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Type(name = " + this.name.toFlatString() + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileType(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class GenericType  {
        public final String name;
        public final List<Type> reqExtend;
        public GenericType(String name, List<Type> reqExtend) {
            
            this.name = name;
            this.reqExtend = reqExtend;
        }

        public String toString() {
            return "GenericType(name = " + String.valueOf(this.name) + ", " + 
                "reqExtend = " + "[" + this.reqExtend.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "GenericType(name = " + String.valueOf(this.name) + ", " + 
                "reqExtend = " + "[" + this.reqExtend.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileGenericType(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class BlockStatement extends Statement {
        public final List<Statement> body;
        public BlockStatement(List<Statement> body, int token_start, int token_end) {
            super(token_start, token_end);
            this.body = body;
        }

        public String toString() {
            return "BlockStatement(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "BlockStatement(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileBlockStatement(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class FunctionDef extends BlockStatement {
        public final List<GenericType> genericTypes;
        public final Type returns;
        public final String name;
        public final List<ASTEnums.DecModType> modifiers;
        public final List<FunctionParam> args;
        public FunctionDef(List<GenericType> genericTypes, Type returns, String name, List<ASTEnums.DecModType> modifiers, List<FunctionParam> args, List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);
            this.genericTypes = genericTypes;
            this.returns = returns;
            this.name = name;
            this.modifiers = modifiers;
            this.args = args;
        }

        public String toString() {
            return "FunctionDef(genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "returns = " + String.valueOf(this.returns) + ", " + 
                "name = " + String.valueOf(this.name) + ", " + 
                "modifiers = " + "[" + this.modifiers.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "FunctionDef(genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "returns = " + this.returns.toFlatString() + ", " + 
                "name = " + String.valueOf(this.name) + ", " + 
                "modifiers = " + "[" + this.modifiers.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileFunctionDef(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class AnonFunctionDef extends BlockStatement {
        public final Type returns;
        public final List<FunctionParam> args;
        public AnonFunctionDef(Type returns, List<FunctionParam> args, List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);
            this.returns = returns;
            this.args = args;
        }

        public String toString() {
            return "AnonFunctionDef(returns = " + String.valueOf(this.returns) + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "AnonFunctionDef(returns = " + this.returns.toFlatString() + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileAnonFunctionDef(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class ClassDef extends BlockStatement {
        public final String name;
        public final List<GenericType> genericTypes;
        public final boolean isStruct;
        public final List<ASTEnums.DecModType> modifiers;
        public final Optional<Type> extendClass;
        public ClassDef(String name, List<GenericType> genericTypes, boolean isStruct, List<ASTEnums.DecModType> modifiers, Optional<Type> extendClass, List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);
            this.name = name;
            this.genericTypes = genericTypes;
            this.isStruct = isStruct;
            this.modifiers = modifiers;
            this.extendClass = extendClass;
        }

        public String toString() {
            return "ClassDef(name = " + String.valueOf(this.name) + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "isStruct = " + String.valueOf(this.isStruct) + ", " + 
                "modifiers = " + "[" + this.modifiers.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "extendClass = " + String.valueOf(this.extendClass) + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "ClassDef(name = " + String.valueOf(this.name) + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "isStruct = " + String.valueOf(this.isStruct) + ", " + 
                "modifiers = " + "[" + this.modifiers.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "extendClass = " + String.valueOf(this.extendClass) + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileClassDef(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class FunctionParam  {
        public final Type type;
        public final String name;
        public FunctionParam(Type type, String name) {
            
            this.type = type;
            this.name = name;
        }

        public String toString() {
            return "FunctionParam(type = " + String.valueOf(this.type) + ", " + 
                "name = " + String.valueOf(this.name) + ")";
        }

        public String toFlatString() {
            return "FunctionParam(type = " + this.type.toFlatString() + ", " + 
                "name = " + String.valueOf(this.name) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileFunctionParam(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class IfBlock extends BlockStatement {
        public final Statement condition;
        public final ASTOptional<ElseBlock> elseBlock;
        public IfBlock(Statement condition, ASTOptional<ElseBlock> elseBlock, List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);
            this.condition = condition;
            this.elseBlock = elseBlock;
        }

        public String toString() {
            return "IfBlock(condition = " + String.valueOf(this.condition) + ", " + 
                "elseBlock = " + String.valueOf(this.elseBlock) + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "IfBlock(condition = " + this.condition.toFlatString() + ", " + 
                "elseBlock = " + this.elseBlock.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileIfBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class ElseBlock extends Statement {

        public ElseBlock(int token_start, int token_end) {
            super(token_start, token_end);

        }

        public String toString() {
            return "ElseBlock(token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "ElseBlock(token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileElseBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class ElseIfBlock extends ElseBlock {
        public final IfBlock on;
        public final ASTOptional<ElseBlock> elseBlock;
        public ElseIfBlock(IfBlock on, ASTOptional<ElseBlock> elseBlock, int token_start, int token_end) {
            super(token_start, token_end);
            this.on = on;
            this.elseBlock = elseBlock;
        }

        public String toString() {
            return "ElseIfBlock(on = " + String.valueOf(this.on) + ", " + 
                "elseBlock = " + String.valueOf(this.elseBlock) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "ElseIfBlock(on = " + this.on.toFlatString() + ", " + 
                "elseBlock = " + this.elseBlock.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileElseIfBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class ElseAlwaysBlock extends ElseBlock {
        public final List<Statement> body;
        public ElseAlwaysBlock(List<Statement> body, int token_start, int token_end) {
            super(token_start, token_end);
            this.body = body;
        }

        public String toString() {
            return "ElseAlwaysBlock(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "ElseAlwaysBlock(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileElseAlwaysBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class WhileBlock extends BlockStatement {
        public final Statement condition;
        public WhileBlock(Statement condition, List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);
            this.condition = condition;
        }

        public String toString() {
            return "WhileBlock(condition = " + String.valueOf(this.condition) + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "WhileBlock(condition = " + this.condition.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileWhileBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class LoopBlock extends BlockStatement {

        public LoopBlock(List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);

        }

        public String toString() {
            return "LoopBlock(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "LoopBlock(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileLoopBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class ForBlock extends BlockStatement {
        public final Statement start;
        public final Statement condition;
        public final Statement step;
        public ForBlock(Statement start, Statement condition, Statement step, List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);
            this.start = start;
            this.condition = condition;
            this.step = step;
        }

        public String toString() {
            return "ForBlock(start = " + String.valueOf(this.start) + ", " + 
                "condition = " + String.valueOf(this.condition) + ", " + 
                "step = " + String.valueOf(this.step) + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "ForBlock(start = " + this.start.toFlatString() + ", " + 
                "condition = " + this.condition.toFlatString() + ", " + 
                "step = " + this.step.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileForBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class ForIterBlock extends BlockStatement {
        public final Statement item;
        public final Statement list;
        public ForIterBlock(Statement item, Statement list, List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);
            this.item = item;
            this.list = list;
        }

        public String toString() {
            return "ForIterBlock(item = " + String.valueOf(this.item) + ", " + 
                "list = " + String.valueOf(this.list) + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "ForIterBlock(item = " + this.item.toFlatString() + ", " + 
                "list = " + this.list.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileForIterBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class MatchBlock extends BlockStatement {
        public final Statement on;
        public MatchBlock(Statement on, List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);
            this.on = on;
        }

        public String toString() {
            return "MatchBlock(on = " + String.valueOf(this.on) + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "MatchBlock(on = " + this.on.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileMatchBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class SwitchBlock extends BlockStatement {
        public final Statement on;
        public SwitchBlock(Statement on, List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);
            this.on = on;
        }

        public String toString() {
            return "SwitchBlock(on = " + String.valueOf(this.on) + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "SwitchBlock(on = " + this.on.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileSwitchBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class CaseStatement extends Expression {
        public final Statement on;
        public CaseStatement(Statement on, int token_start, int token_end) {
            super(token_start, token_end);
            this.on = on;
        }

        public String toString() {
            return "CaseStatement(on = " + String.valueOf(this.on) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "CaseStatement(on = " + this.on.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileCaseStatement(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class CaseMatchStatement extends Expression {
        public final Statement on;
        public CaseMatchStatement(Statement on, int token_start, int token_end) {
            super(token_start, token_end);
            this.on = on;
        }

        public String toString() {
            return "CaseMatchStatement(on = " + String.valueOf(this.on) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "CaseMatchStatement(on = " + this.on.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileCaseMatchStatement(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class BreakStatement extends Expression {

        public BreakStatement(int token_start, int token_end) {
            super(token_start, token_end);

        }

        public String toString() {
            return "BreakStatement(token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "BreakStatement(token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileBreakStatement(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class ContinueStatement extends Expression {

        public ContinueStatement(int token_start, int token_end) {
            super(token_start, token_end);

        }

        public String toString() {
            return "ContinueStatement(token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "ContinueStatement(token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileContinueStatement(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class TryBlock extends BlockStatement {
        public final CatchBlock catchBlock;
        public TryBlock(CatchBlock catchBlock, List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);
            this.catchBlock = catchBlock;
        }

        public String toString() {
            return "TryBlock(catchBlock = " + String.valueOf(this.catchBlock) + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "TryBlock(catchBlock = " + this.catchBlock.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileTryBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class CatchBlock extends BlockStatement {
        public final FunctionParam ex;
        public CatchBlock(FunctionParam ex, List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);
            this.ex = ex;
        }

        public String toString() {
            return "CatchBlock(ex = " + String.valueOf(this.ex) + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "CatchBlock(ex = " + this.ex.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileCatchBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class FinallyBlock extends BlockStatement {

        public FinallyBlock(List<Statement> body, int token_start, int token_end) {
            super(body, token_start, token_end);

        }

        public String toString() {
            return "FinallyBlock(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "FinallyBlock(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileFinallyBlock(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class FunctionCall extends Expression {
        public final String name;
        public final List<Statement> args;
        public final List<Type> genericTypes;
        public FunctionCall(String name, List<Statement> args, List<Type> genericTypes, int token_start, int token_end) {
            super(token_start, token_end);
            this.name = name;
            this.args = args;
            this.genericTypes = genericTypes;
        }

        public String toString() {
            return "FunctionCall(name = " + String.valueOf(this.name) + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "FunctionCall(name = " + String.valueOf(this.name) + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileFunctionCall(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class Parened extends Expression {
        public final Statement sub;
        public Parened(Statement sub, int token_start, int token_end) {
            super(token_start, token_end);
            this.sub = sub;
        }

        public String toString() {
            return "Parened(sub = " + String.valueOf(this.sub) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Parened(sub = " + this.sub.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileParened(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class Dotted extends Expression {
        public final Statement left;
        public final Statement right;
        public Dotted(Statement left, Statement right, int token_start, int token_end) {
            super(token_start, token_end);
            this.left = left;
            this.right = right;
        }

        public String toString() {
            return "Dotted(left = " + String.valueOf(this.left) + ", " + 
                "right = " + String.valueOf(this.right) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Dotted(left = " + this.left.toFlatString() + ", " + 
                "right = " + this.right.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileDotted(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class Return extends Expression {
        public final ASTOptional<Statement> value;
        public Return(ASTOptional<Statement> value, int token_start, int token_end) {
            super(token_start, token_end);
            this.value = value;
        }

        public String toString() {
            return "Return(value = " + String.valueOf(this.value) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Return(value = " + this.value.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileReturn(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class New extends Expression {
        public final Type type;
        public final List<Statement> argz;
        public New(Type type, List<Statement> argz, int token_start, int token_end) {
            super(token_start, token_end);
            this.type = type;
            this.argz = argz;
        }

        public String toString() {
            return "New(type = " + String.valueOf(this.type) + ", " + 
                "argz = " + "[" + this.argz.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "New(type = " + this.type.toFlatString() + ", " + 
                "argz = " + "[" + this.argz.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileNew(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class ThrowStatement extends Expression {
        public final Statement value;
        public ThrowStatement(Statement value, int token_start, int token_end) {
            super(token_start, token_end);
            this.value = value;
        }

        public String toString() {
            return "ThrowStatement(value = " + String.valueOf(this.value) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "ThrowStatement(value = " + this.value.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileThrowStatement(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class ImportStatement extends Expression {
        public final ASTEnums.ImportType type;
        public final String value;
        public ImportStatement(ASTEnums.ImportType type, String value, int token_start, int token_end) {
            super(token_start, token_end);
            this.type = type;
            this.value = value;
        }

        public String toString() {
            return "ImportStatement(type = " + String.valueOf(this.type) + ", " + 
                "value = " + String.valueOf(this.value) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "ImportStatement(type = " + String.valueOf(this.type) + ", " + 
                "value = " + String.valueOf(this.value) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileImportStatement(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class MathStatement extends Expression {
        public final Statement left;
        public final ASTEnums.MathOp op;
        public final Statement right;
        public MathStatement(Statement left, ASTEnums.MathOp op, Statement right, int token_start, int token_end) {
            super(token_start, token_end);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "MathStatement(left = " + String.valueOf(this.left) + ", " + 
                "op = " + String.valueOf(this.op) + ", " + 
                "right = " + String.valueOf(this.right) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "MathStatement(left = " + this.left.toFlatString() + ", " + 
                "op = " + String.valueOf(this.op) + ", " + 
                "right = " + this.right.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileMathStatement(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class MathAssign extends Expression {
        public final Statement left;
        public final ASTEnums.MathOp op;
        public final Statement right;
        public MathAssign(Statement left, ASTEnums.MathOp op, Statement right, int token_start, int token_end) {
            super(token_start, token_end);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "MathAssign(left = " + String.valueOf(this.left) + ", " + 
                "op = " + String.valueOf(this.op) + ", " + 
                "right = " + String.valueOf(this.right) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "MathAssign(left = " + this.left.toFlatString() + ", " + 
                "op = " + String.valueOf(this.op) + ", " + 
                "right = " + this.right.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileMathAssign(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class MathSelfMod extends Expression {
        public final Statement left;
        public final ASTEnums.MathOp op;
        public final ASTEnums.SelfModTime time;
        public MathSelfMod(Statement left, ASTEnums.MathOp op, ASTEnums.SelfModTime time, int token_start, int token_end) {
            super(token_start, token_end);
            this.left = left;
            this.op = op;
            this.time = time;
        }

        public String toString() {
            return "MathSelfMod(left = " + String.valueOf(this.left) + ", " + 
                "op = " + String.valueOf(this.op) + ", " + 
                "time = " + String.valueOf(this.time) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "MathSelfMod(left = " + this.left.toFlatString() + ", " + 
                "op = " + String.valueOf(this.op) + ", " + 
                "time = " + String.valueOf(this.time) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileMathSelfMod(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class Subscript extends Expression {
        public final Statement left;
        public final Statement sub;
        public Subscript(Statement left, Statement sub, int token_start, int token_end) {
            super(token_start, token_end);
            this.left = left;
            this.sub = sub;
        }

        public String toString() {
            return "Subscript(left = " + String.valueOf(this.left) + ", " + 
                "sub = " + String.valueOf(this.sub) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Subscript(left = " + this.left.toFlatString() + ", " + 
                "sub = " + this.sub.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileSubscript(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class Compare extends Expression {
        public final Statement left;
        public final ASTEnums.CompareOp op;
        public final Statement right;
        public Compare(Statement left, ASTEnums.CompareOp op, Statement right, int token_start, int token_end) {
            super(token_start, token_end);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "Compare(left = " + String.valueOf(this.left) + ", " + 
                "op = " + String.valueOf(this.op) + ", " + 
                "right = " + String.valueOf(this.right) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Compare(left = " + this.left.toFlatString() + ", " + 
                "op = " + String.valueOf(this.op) + ", " + 
                "right = " + this.right.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileCompare(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class BoolCombine extends Expression {
        public final Statement left;
        public final ASTEnums.BoolOp op;
        public final Statement right;
        public BoolCombine(Statement left, ASTEnums.BoolOp op, Statement right, int token_start, int token_end) {
            super(token_start, token_end);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "BoolCombine(left = " + String.valueOf(this.left) + ", " + 
                "op = " + String.valueOf(this.op) + ", " + 
                "right = " + String.valueOf(this.right) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "BoolCombine(left = " + this.left.toFlatString() + ", " + 
                "op = " + String.valueOf(this.op) + ", " + 
                "right = " + this.right.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileBoolCombine(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class Assign extends Expression {
        public final Statement name;
        public final Statement value;
        public Assign(Statement name, Statement value, int token_start, int token_end) {
            super(token_start, token_end);
            this.name = name;
            this.value = value;
        }

        public String toString() {
            return "Assign(name = " + String.valueOf(this.name) + ", " + 
                "value = " + String.valueOf(this.value) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Assign(name = " + this.name.toFlatString() + ", " + 
                "value = " + this.value.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileAssign(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class Declare extends Statement {
        public final Type type;
        public final String name;
        public final List<ASTEnums.DecModType> modifiers;
        public Declare(Type type, String name, List<ASTEnums.DecModType> modifiers, int token_start, int token_end) {
            super(token_start, token_end);
            this.type = type;
            this.name = name;
            this.modifiers = modifiers;
        }

        public String toString() {
            return "Declare(type = " + String.valueOf(this.type) + ", " + 
                "name = " + String.valueOf(this.name) + ", " + 
                "modifiers = " + "[" + this.modifiers.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Declare(type = " + this.type.toFlatString() + ", " + 
                "name = " + String.valueOf(this.name) + ", " + 
                "modifiers = " + "[" + this.modifiers.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileDeclare(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class DeclareAssign extends Declare {
        public final Statement value;
        public DeclareAssign(Statement value, Type type, String name, List<ASTEnums.DecModType> modifiers, int token_start, int token_end) {
            super(type, name, modifiers, token_start, token_end);
            this.value = value;
        }

        public String toString() {
            return "DeclareAssign(value = " + String.valueOf(this.value) + ", " + 
                "type = " + String.valueOf(this.type) + ", " + 
                "name = " + String.valueOf(this.name) + ", " + 
                "modifiers = " + "[" + this.modifiers.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "DeclareAssign(value = " + this.value.toFlatString() + ", " + 
                "type = " + this.type.toFlatString() + ", " + 
                "name = " + String.valueOf(this.name) + ", " + 
                "modifiers = " + "[" + this.modifiers.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileDeclareAssign(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class DeconstructAssign extends Statement {
        public final Type type;
        public final List<Statement> args;
        public final Statement value;
        public DeconstructAssign(Type type, List<Statement> args, Statement value, int token_start, int token_end) {
            super(token_start, token_end);
            this.type = type;
            this.args = args;
            this.value = value;
        }

        public String toString() {
            return "DeconstructAssign(type = " + String.valueOf(this.type) + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "value = " + String.valueOf(this.value) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "DeconstructAssign(type = " + this.type.toFlatString() + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "value = " + this.value.toFlatString() + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileDeconstructAssign(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class NumberLiteral extends Expression {
        public final double value;
        public NumberLiteral(double value, int token_start, int token_end) {
            super(token_start, token_end);
            this.value = value;
        }

        public String toString() {
            return "NumberLiteral(value = " + String.valueOf(this.value) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "NumberLiteral(value = " + String.valueOf(this.value) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileNumberLiteral(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class StringLiteral extends Expression {
        public final String value;
        public StringLiteral(String value, int token_start, int token_end) {
            super(token_start, token_end);
            this.value = value;
        }

        public String toString() {
            return "StringLiteral(value = " + "\"" + String.valueOf(this.value) + "\"" + ", " + 
                "token_start = " + "\"" + String.valueOf(this.token_start) + "\"" + ", " + 
                "token_end = " + "\"" + String.valueOf(this.token_end) + "\"" + ")";
        }

        public String toFlatString() {
            return "StringLiteral(value = " + "\"" + String.valueOf(this.value) + "\"" + ", " + 
                "token_start = " + "\"" + String.valueOf(this.token_start) + "\"" + ", " + 
                "token_end = " + "\"" + String.valueOf(this.token_end) + "\"" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileStringLiteral(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class SequenceLiteral extends Expression {
        public final String value;
        public SequenceLiteral(String value, int token_start, int token_end) {
            super(token_start, token_end);
            this.value = value;
        }

        public String toString() {
            return "SequenceLiteral(value = " + "\"" + String.valueOf(this.value) + "\"" + ", " + 
                "token_start = " + "\"" + String.valueOf(this.token_start) + "\"" + ", " + 
                "token_end = " + "\"" + String.valueOf(this.token_end) + "\"" + ")";
        }

        public String toFlatString() {
            return "SequenceLiteral(value = " + "\"" + String.valueOf(this.value) + "\"" + ", " + 
                "token_start = " + "\"" + String.valueOf(this.token_start) + "\"" + ", " + 
                "token_end = " + "\"" + String.valueOf(this.token_end) + "\"" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileSequenceLiteral(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class BooleanLiteral extends Expression {
        public final boolean value;
        public BooleanLiteral(boolean value, int token_start, int token_end) {
            super(token_start, token_end);
            this.value = value;
        }

        public String toString() {
            return "BooleanLiteral(value = " + "\"" + String.valueOf(this.value) + "\"" + ", " + 
                "token_start = " + "\"" + String.valueOf(this.token_start) + "\"" + ", " + 
                "token_end = " + "\"" + String.valueOf(this.token_end) + "\"" + ")";
        }

        public String toFlatString() {
            return "BooleanLiteral(value = " + "\"" + String.valueOf(this.value) + "\"" + ", " + 
                "token_start = " + "\"" + String.valueOf(this.token_start) + "\"" + ", " + 
                "token_end = " + "\"" + String.valueOf(this.token_end) + "\"" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileBooleanLiteral(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class RegexLiteral extends Expression {
        public final String pattern;
        public final String flags;
        public RegexLiteral(String pattern, String flags, int token_start, int token_end) {
            super(token_start, token_end);
            this.pattern = pattern;
            this.flags = flags;
        }

        public String toString() {
            return "RegexLiteral(pattern = " + "\"" + String.valueOf(this.pattern) + "\"" + ", " + 
                "flags = " + "\"" + String.valueOf(this.flags) + "\"" + ", " + 
                "token_start = " + "\"" + String.valueOf(this.token_start) + "\"" + ", " + 
                "token_end = " + "\"" + String.valueOf(this.token_end) + "\"" + ")";
        }

        public String toFlatString() {
            return "RegexLiteral(pattern = " + "\"" + String.valueOf(this.pattern) + "\"" + ", " + 
                "flags = " + "\"" + String.valueOf(this.flags) + "\"" + ", " + 
                "token_start = " + "\"" + String.valueOf(this.token_start) + "\"" + ", " + 
                "token_end = " + "\"" + String.valueOf(this.token_end) + "\"" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileRegexLiteral(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
    public static class Variable extends Expression {
        public final String name;
        public Variable(String name, int token_start, int token_end) {
            super(token_start, token_end);
            this.name = name;
        }

        public String toString() {
            return "Variable(name = " + String.valueOf(this.name) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public String toFlatString() {
            return "Variable(name = " + String.valueOf(this.name) + ", " + 
                "token_start = " + String.valueOf(this.token_start) + ", " + 
                "token_end = " + String.valueOf(this.token_end) + ")";
        }

        public CompiledCode compile(CompileContext context) {
            try {
                return ASTVisitor.instance.compileVariable(this, context);
            } catch(Exception ex) {
                ex.printStackTrace();
                context.throwError(ex.toString(), null);
            }
            return new CompiledCode();
        }
    }
    
}
