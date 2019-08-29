
package io.dallen;

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
        public final List<Token> tokens;
        public Statement(List<Token> tokens) {
            
            this.tokens = tokens;
        }

        public String toString() {
            return "Statement(tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Statement(tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileStatement(this, context);
        }
    }
    
    public static class Expression extends Statement {

        public Expression(List<Token> tokens) {
            super(tokens);

        }

        public String toString() {
            return "Expression(tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Expression(tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileExpression(this, context);
        }
    }
    
    public static class Type  {
        public final Statement name;
        public final List<Type> genericTypes;
        public Type(Statement name, List<Type> genericTypes) {
            
            this.name = name;
            this.genericTypes = genericTypes;
        }

        public String toString() {
            return "Type(name = " + this.name.toString() + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Type(name = " + this.name.toFlatString() + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileType(this, context);
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
            return "GenericType(name = " + this.name.toString() + ", " + 
                "reqExtend = " + "[" + this.reqExtend.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "GenericType(name = " + this.name.toString() + ", " + 
                "reqExtend = " + "[" + this.reqExtend.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileGenericType(this, context);
        }
    }
    
    public static class BlockStatement extends Statement {
        public final List<Statement> body;
        public BlockStatement(List<Statement> body, List<Token> tokens) {
            super(tokens);
            this.body = body;
        }

        public String toString() {
            return "BlockStatement(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "BlockStatement(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileBlockStatement(this, context);
        }
    }
    
    public static class FunctionDef extends BlockStatement {
        public final List<GenericType> genericTypes;
        public final Type returns;
        public final String name;
        public final List<FunctionParam> args;
        public FunctionDef(List<GenericType> genericTypes, Type returns, String name, List<FunctionParam> args, List<Statement> body, List<Token> tokens) {
            super(body, tokens);
            this.genericTypes = genericTypes;
            this.returns = returns;
            this.name = name;
            this.args = args;
        }

        public String toString() {
            return "FunctionDef(genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "returns = " + this.returns.toString() + ", " + 
                "name = " + this.name.toString() + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "FunctionDef(genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "returns = " + this.returns.toFlatString() + ", " + 
                "name = " + this.name.toString() + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileFunctionDef(this, context);
        }
    }
    
    public static class AnonFunctionDef extends BlockStatement {
        public final Type returns;
        public final List<FunctionParam> args;
        public AnonFunctionDef(Type returns, List<FunctionParam> args, List<Statement> body, List<Token> tokens) {
            super(body, tokens);
            this.returns = returns;
            this.args = args;
        }

        public String toString() {
            return "AnonFunctionDef(returns = " + this.returns.toString() + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "AnonFunctionDef(returns = " + this.returns.toFlatString() + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileAnonFunctionDef(this, context);
        }
    }
    
    public static class ClassDef extends BlockStatement {
        public final String name;
        public final List<GenericType> genericTypes;
        public final Optional<Type> extendClass;
        public ClassDef(String name, List<GenericType> genericTypes, Optional<Type> extendClass, List<Statement> body, List<Token> tokens) {
            super(body, tokens);
            this.name = name;
            this.genericTypes = genericTypes;
            this.extendClass = extendClass;
        }

        public String toString() {
            return "ClassDef(name = " + this.name.toString() + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "extendClass = " + this.extendClass.toString() + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "ClassDef(name = " + this.name.toString() + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "extendClass = " + this.extendClass.toString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileClassDef(this, context);
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
            return "FunctionParam(type = " + this.type.toString() + ", " + 
                "name = " + this.name.toString() + ")";
        }

        public String toFlatString() {
            return "FunctionParam(type = " + this.type.toFlatString() + ", " + 
                "name = " + this.name.toString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileFunctionParam(this, context);
        }
    }
    
    public static class IfBlock extends BlockStatement {
        public final Statement condition;
        public  ElseBlock elseBlock;
        public  Boolean validElseBlock;
        public IfBlock(Statement condition, List<Statement> body, List<Token> tokens) {
            super(body, tokens);
            this.condition = condition;
            this.elseBlock = new ElseBlock(List.of());
            this.validElseBlock = false;
        }

        public String toString() {
            return "IfBlock(condition = " + this.condition.toString() + ", " + 
                "elseBlock = " + this.elseBlock.toString() + ", " + 
                "validElseBlock = " + this.validElseBlock.toString() + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "IfBlock(condition = " + this.condition.toFlatString() + ", " + 
                "elseBlock = " + this.elseBlock.toFlatString() + ", " + 
                "validElseBlock = " + this.validElseBlock.toString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileIfBlock(this, context);
        }
    }
    
    public static class ElseBlock extends Statement {

        public ElseBlock(List<Token> tokens) {
            super(tokens);

        }

        public String toString() {
            return "ElseBlock(tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "ElseBlock(tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileElseBlock(this, context);
        }
    }
    
    public static class ElseIfBlock extends ElseBlock {
        public final IfBlock on;
        public  ElseBlock elseBlock;
        public  Boolean validElseBlock;
        public ElseIfBlock(IfBlock on, List<Token> tokens) {
            super(tokens);
            this.on = on;
            this.elseBlock = new ElseBlock(List.of());
            this.validElseBlock = false;
        }

        public String toString() {
            return "ElseIfBlock(on = " + this.on.toString() + ", " + 
                "elseBlock = " + this.elseBlock.toString() + ", " + 
                "validElseBlock = " + this.validElseBlock.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "ElseIfBlock(on = " + this.on.toFlatString() + ", " + 
                "elseBlock = " + this.elseBlock.toFlatString() + ", " + 
                "validElseBlock = " + this.validElseBlock.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileElseIfBlock(this, context);
        }
    }
    
    public static class ElseAlwaysBlock extends ElseBlock {
        public final List<Statement> body;
        public ElseAlwaysBlock(List<Statement> body, List<Token> tokens) {
            super(tokens);
            this.body = body;
        }

        public String toString() {
            return "ElseAlwaysBlock(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "ElseAlwaysBlock(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileElseAlwaysBlock(this, context);
        }
    }
    
    public static class WhileBlock extends BlockStatement {
        public final Statement condition;
        public WhileBlock(Statement condition, List<Statement> body, List<Token> tokens) {
            super(body, tokens);
            this.condition = condition;
        }

        public String toString() {
            return "WhileBlock(condition = " + this.condition.toString() + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "WhileBlock(condition = " + this.condition.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileWhileBlock(this, context);
        }
    }
    
    public static class LoopBlock extends BlockStatement {

        public LoopBlock(List<Statement> body, List<Token> tokens) {
            super(body, tokens);

        }

        public String toString() {
            return "LoopBlock(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "LoopBlock(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileLoopBlock(this, context);
        }
    }
    
    public static class ForBlock extends BlockStatement {
        public final Statement start;
        public final Statement condition;
        public final Statement step;
        public ForBlock(Statement start, Statement condition, Statement step, List<Statement> body, List<Token> tokens) {
            super(body, tokens);
            this.start = start;
            this.condition = condition;
            this.step = step;
        }

        public String toString() {
            return "ForBlock(start = " + this.start.toString() + ", " + 
                "condition = " + this.condition.toString() + ", " + 
                "step = " + this.step.toString() + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "ForBlock(start = " + this.start.toFlatString() + ", " + 
                "condition = " + this.condition.toFlatString() + ", " + 
                "step = " + this.step.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileForBlock(this, context);
        }
    }
    
    public static class ForIterBlock extends BlockStatement {
        public final Statement item;
        public final Statement list;
        public ForIterBlock(Statement item, Statement list, List<Statement> body, List<Token> tokens) {
            super(body, tokens);
            this.item = item;
            this.list = list;
        }

        public String toString() {
            return "ForIterBlock(item = " + this.item.toString() + ", " + 
                "list = " + this.list.toString() + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "ForIterBlock(item = " + this.item.toFlatString() + ", " + 
                "list = " + this.list.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileForIterBlock(this, context);
        }
    }
    
    public static class MatchBlock extends BlockStatement {
        public final Statement on;
        public MatchBlock(Statement on, List<Statement> body, List<Token> tokens) {
            super(body, tokens);
            this.on = on;
        }

        public String toString() {
            return "MatchBlock(on = " + this.on.toString() + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "MatchBlock(on = " + this.on.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileMatchBlock(this, context);
        }
    }
    
    public static class SwitchBlock extends BlockStatement {
        public final Statement on;
        public SwitchBlock(Statement on, List<Statement> body, List<Token> tokens) {
            super(body, tokens);
            this.on = on;
        }

        public String toString() {
            return "SwitchBlock(on = " + this.on.toString() + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "SwitchBlock(on = " + this.on.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileSwitchBlock(this, context);
        }
    }
    
    public static class CaseStatement extends Expression {
        public final Statement on;
        public CaseStatement(Statement on, List<Token> tokens) {
            super(tokens);
            this.on = on;
        }

        public String toString() {
            return "CaseStatement(on = " + this.on.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "CaseStatement(on = " + this.on.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileCaseStatement(this, context);
        }
    }
    
    public static class CaseMatchStatement extends Expression {
        public final Statement on;
        public CaseMatchStatement(Statement on, List<Token> tokens) {
            super(tokens);
            this.on = on;
        }

        public String toString() {
            return "CaseMatchStatement(on = " + this.on.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "CaseMatchStatement(on = " + this.on.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileCaseMatchStatement(this, context);
        }
    }
    
    public static class BreakStatement extends Expression {

        public BreakStatement(List<Token> tokens) {
            super(tokens);

        }

        public String toString() {
            return "BreakStatement(tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "BreakStatement(tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileBreakStatement(this, context);
        }
    }
    
    public static class ContinueStatement extends Expression {

        public ContinueStatement(List<Token> tokens) {
            super(tokens);

        }

        public String toString() {
            return "ContinueStatement(tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "ContinueStatement(tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileContinueStatement(this, context);
        }
    }
    
    public static class TryBlock extends BlockStatement {
        public  CatchBlock catchBlock;
        public TryBlock(List<Statement> body, List<Token> tokens) {
            super(body, tokens);

        }

        public String toString() {
            return "TryBlock(catchBlock = " + this.catchBlock.toString() + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "TryBlock(catchBlock = " + this.catchBlock.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileTryBlock(this, context);
        }
    }
    
    public static class CatchBlock extends BlockStatement {
        public final Statement ex;
        public CatchBlock(Statement ex, List<Statement> body, List<Token> tokens) {
            super(body, tokens);
            this.ex = ex;
        }

        public String toString() {
            return "CatchBlock(ex = " + this.ex.toString() + ", " + 
                "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "CatchBlock(ex = " + this.ex.toFlatString() + ", " + 
                "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileCatchBlock(this, context);
        }
    }
    
    public static class FinallyBlock extends BlockStatement {

        public FinallyBlock(List<Statement> body, List<Token> tokens) {
            super(body, tokens);

        }

        public String toString() {
            return "FinallyBlock(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "FinallyBlock(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileFinallyBlock(this, context);
        }
    }
    
    public static class FunctionCall extends Expression {
        public final String name;
        public final List<Statement> args;
        public final List<Type> genericTypes;
        public FunctionCall(String name, List<Statement> args, List<Type> genericTypes, List<Token> tokens) {
            super(tokens);
            this.name = name;
            this.args = args;
            this.genericTypes = genericTypes;
        }

        public String toString() {
            return "FunctionCall(name = " + this.name.toString() + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "FunctionCall(name = " + this.name.toString() + ", " + 
                "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileFunctionCall(this, context);
        }
    }
    
    public static class Parened extends Expression {
        public final Statement sub;
        public Parened(Statement sub, List<Token> tokens) {
            super(tokens);
            this.sub = sub;
        }

        public String toString() {
            return "Parened(sub = " + this.sub.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Parened(sub = " + this.sub.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileParened(this, context);
        }
    }
    
    public static class Dotted extends Expression {
        public final Statement left;
        public final Statement right;
        public Dotted(Statement left, Statement right, List<Token> tokens) {
            super(tokens);
            this.left = left;
            this.right = right;
        }

        public String toString() {
            return "Dotted(left = " + this.left.toString() + ", " + 
                "right = " + this.right.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Dotted(left = " + this.left.toFlatString() + ", " + 
                "right = " + this.right.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileDotted(this, context);
        }
    }
    
    public static class Arrowed extends Expression {
        public final Statement left;
        public final Statement right;
        public Arrowed(Statement left, Statement right, List<Token> tokens) {
            super(tokens);
            this.left = left;
            this.right = right;
        }

        public String toString() {
            return "Arrowed(left = " + this.left.toString() + ", " + 
                "right = " + this.right.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Arrowed(left = " + this.left.toFlatString() + ", " + 
                "right = " + this.right.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileArrowed(this, context);
        }
    }
    
    public static class Return extends Expression {
        public final Statement value;
        public Return(Statement value, List<Token> tokens) {
            super(tokens);
            this.value = value;
        }

        public String toString() {
            return "Return(value = " + this.value.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Return(value = " + this.value.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileReturn(this, context);
        }
    }
    
    public static class New extends Expression {
        public final Type type;
        public final List<Statement> argz;
        public New(Type type, List<Statement> argz, List<Token> tokens) {
            super(tokens);
            this.type = type;
            this.argz = argz;
        }

        public String toString() {
            return "New(type = " + this.type.toString() + ", " + 
                "argz = " + "[" + this.argz.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "New(type = " + this.type.toFlatString() + ", " + 
                "argz = " + "[" + this.argz.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileNew(this, context);
        }
    }
    
    public static class ThrowStatement extends Expression {
        public final Statement value;
        public ThrowStatement(Statement value, List<Token> tokens) {
            super(tokens);
            this.value = value;
        }

        public String toString() {
            return "ThrowStatement(value = " + this.value.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "ThrowStatement(value = " + this.value.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileThrowStatement(this, context);
        }
    }
    
    public static class ImportStatement extends Expression {
        public final ASTEnums.ImportType type;
        public final String value;
        public ImportStatement(ASTEnums.ImportType type, String value, List<Token> tokens) {
            super(tokens);
            this.type = type;
            this.value = value;
        }

        public String toString() {
            return "ImportStatement(type = " + this.type.toString() + ", " + 
                "value = " + this.value.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "ImportStatement(type = " + this.type.toString() + ", " + 
                "value = " + this.value.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileImportStatement(this, context);
        }
    }
    
    public static class MathStatement extends Expression {
        public final Statement left;
        public final ASTEnums.MathOp op;
        public final Statement right;
        public MathStatement(Statement left, ASTEnums.MathOp op, Statement right, List<Token> tokens) {
            super(tokens);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "MathStatement(left = " + this.left.toString() + ", " + 
                "op = " + this.op.toString() + ", " + 
                "right = " + this.right.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "MathStatement(left = " + this.left.toFlatString() + ", " + 
                "op = " + this.op.toString() + ", " + 
                "right = " + this.right.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileMathStatement(this, context);
        }
    }
    
    public static class MathAssign extends Expression {
        public final Statement left;
        public final ASTEnums.MathOp op;
        public final Statement right;
        public MathAssign(Statement left, ASTEnums.MathOp op, Statement right, List<Token> tokens) {
            super(tokens);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "MathAssign(left = " + this.left.toString() + ", " + 
                "op = " + this.op.toString() + ", " + 
                "right = " + this.right.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "MathAssign(left = " + this.left.toFlatString() + ", " + 
                "op = " + this.op.toString() + ", " + 
                "right = " + this.right.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileMathAssign(this, context);
        }
    }
    
    public static class MathSelfMod extends Expression {
        public final Statement left;
        public final ASTEnums.MathOp op;
        public final ASTEnums.SelfModTime time;
        public MathSelfMod(Statement left, ASTEnums.MathOp op, ASTEnums.SelfModTime time, List<Token> tokens) {
            super(tokens);
            this.left = left;
            this.op = op;
            this.time = time;
        }

        public String toString() {
            return "MathSelfMod(left = " + this.left.toString() + ", " + 
                "op = " + this.op.toString() + ", " + 
                "time = " + this.time.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "MathSelfMod(left = " + this.left.toFlatString() + ", " + 
                "op = " + this.op.toString() + ", " + 
                "time = " + this.time.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileMathSelfMod(this, context);
        }
    }
    
    public static class Subscript extends Expression {
        public final Statement left;
        public final Statement sub;
        public Subscript(Statement left, Statement sub, List<Token> tokens) {
            super(tokens);
            this.left = left;
            this.sub = sub;
        }

        public String toString() {
            return "Subscript(left = " + this.left.toString() + ", " + 
                "sub = " + this.sub.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Subscript(left = " + this.left.toFlatString() + ", " + 
                "sub = " + this.sub.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileSubscript(this, context);
        }
    }
    
    public static class Compare extends Expression {
        public final Statement left;
        public final ASTEnums.CompareOp op;
        public final Statement right;
        public Compare(Statement left, ASTEnums.CompareOp op, Statement right, List<Token> tokens) {
            super(tokens);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "Compare(left = " + this.left.toString() + ", " + 
                "op = " + this.op.toString() + ", " + 
                "right = " + this.right.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Compare(left = " + this.left.toFlatString() + ", " + 
                "op = " + this.op.toString() + ", " + 
                "right = " + this.right.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileCompare(this, context);
        }
    }
    
    public static class BoolCombine extends Expression {
        public final Statement left;
        public final ASTEnums.BoolOp op;
        public final Statement right;
        public BoolCombine(Statement left, ASTEnums.BoolOp op, Statement right, List<Token> tokens) {
            super(tokens);
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "BoolCombine(left = " + this.left.toString() + ", " + 
                "op = " + this.op.toString() + ", " + 
                "right = " + this.right.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "BoolCombine(left = " + this.left.toFlatString() + ", " + 
                "op = " + this.op.toString() + ", " + 
                "right = " + this.right.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileBoolCombine(this, context);
        }
    }
    
    public static class Assign extends Expression {
        public final Statement name;
        public final Statement value;
        public Assign(Statement name, Statement value, List<Token> tokens) {
            super(tokens);
            this.name = name;
            this.value = value;
        }

        public String toString() {
            return "Assign(name = " + this.name.toString() + ", " + 
                "value = " + this.value.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Assign(name = " + this.name.toFlatString() + ", " + 
                "value = " + this.value.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileAssign(this, context);
        }
    }
    
    public static class Declare extends Statement {
        public final Type type;
        public final String name;
        public Declare(Type type, String name, List<Token> tokens) {
            super(tokens);
            this.type = type;
            this.name = name;
        }

        public String toString() {
            return "Declare(type = " + this.type.toString() + ", " + 
                "name = " + this.name.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Declare(type = " + this.type.toFlatString() + ", " + 
                "name = " + this.name.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileDeclare(this, context);
        }
    }
    
    public static class DeclareAssign extends Statement {
        public final Type type;
        public final String name;
        public final Statement value;
        public DeclareAssign(Type type, String name, Statement value, List<Token> tokens) {
            super(tokens);
            this.type = type;
            this.name = name;
            this.value = value;
        }

        public String toString() {
            return "DeclareAssign(type = " + this.type.toString() + ", " + 
                "name = " + this.name.toString() + ", " + 
                "value = " + this.value.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "DeclareAssign(type = " + this.type.toFlatString() + ", " + 
                "name = " + this.name.toString() + ", " + 
                "value = " + this.value.toFlatString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileDeclareAssign(this, context);
        }
    }
    
    public static class NumberLiteral extends Expression {
        public final Double value;
        public NumberLiteral(Double value, List<Token> tokens) {
            super(tokens);
            this.value = value;
        }

        public String toString() {
            return "NumberLiteral(value = " + this.value.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "NumberLiteral(value = " + this.value.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileNumberLiteral(this, context);
        }
    }
    
    public static class StringLiteral extends Expression {
        public final String value;
        public StringLiteral(String value, List<Token> tokens) {
            super(tokens);
            this.value = value;
        }

        public String toString() {
            return "StringLiteral(value = " + "\"" + this.value.toString() + "\"" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "StringLiteral(value = " + "\"" + this.value.toString() + "\"" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileStringLiteral(this, context);
        }
    }
    
    public static class SequenceLiteral extends Expression {
        public final String value;
        public SequenceLiteral(String value, List<Token> tokens) {
            super(tokens);
            this.value = value;
        }

        public String toString() {
            return "SequenceLiteral(value = " + "\"" + this.value.toString() + "\"" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "SequenceLiteral(value = " + "\"" + this.value.toString() + "\"" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileSequenceLiteral(this, context);
        }
    }
    
    public static class BooleanLiteral extends Expression {
        public final Boolean value;
        public BooleanLiteral(Boolean value, List<Token> tokens) {
            super(tokens);
            this.value = value;
        }

        public String toString() {
            return "BooleanLiteral(value = " + "\"" + this.value.toString() + "\"" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "BooleanLiteral(value = " + "\"" + this.value.toString() + "\"" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileBooleanLiteral(this, context);
        }
    }
    
    public static class RegexLiteral extends Expression {
        public final String pattern;
        public final String flags;
        public RegexLiteral(String pattern, String flags, List<Token> tokens) {
            super(tokens);
            this.pattern = pattern;
            this.flags = flags;
        }

        public String toString() {
            return "RegexLiteral(pattern = " + "\"" + this.pattern.toString() + "\"" + ", " + 
                "flags = " + "\"" + this.flags.toString() + "\"" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "RegexLiteral(pattern = " + "\"" + this.pattern.toString() + "\"" + ", " + 
                "flags = " + "\"" + this.flags.toString() + "\"" + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileRegexLiteral(this, context);
        }
    }
    
    public static class Variable extends Expression {
        public final String name;
        public Variable(String name, List<Token> tokens) {
            super(tokens);
            this.name = name;
        }

        public String toString() {
            return "Variable(name = " + this.name.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Variable(name = " + this.name.toString() + ", " + 
                "tokens = " + "[" + this.tokens.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileVariable(this, context);
        }
    }
    
}
