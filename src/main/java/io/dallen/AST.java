package io.dallen;

import io.dallen.compiler.visitor.ASTVisitor;
import io.dallen.compiler.CompileContext;
import io.dallen.compiler.CompiledCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class AST {

    public interface HasRaw {
        String getRawOp();
    }

    public enum MathOp implements HasRaw {
        PLUS("add"), MINUS("sub"), MUL("mul"), DIV("div"), XOR("^");

        private final String rawOp;

        MathOp(String rawOp) {
            this.rawOp = rawOp;
        }

        public String getRawOp() {
            return rawOp;
        }
    }

    public enum CompareOp implements HasRaw {
        LT("<"), GT(">"), LE("<="), GE(">="), EQ("=="), NE("!=");

        private final String rawOp;

        CompareOp(String rawOp) {
            this.rawOp = rawOp;
        }

        public String getRawOp() {
            return rawOp;
        }
    }

    public enum BoolOp implements HasRaw {
        AND("&&"), OR("||");

        private final String rawOp;

        BoolOp(String rawOp) {
            this.rawOp = rawOp;
        }

        public String getRawOp() {
            return rawOp;
        }
    }

    public enum ImportType implements HasRaw {
        LOCAL("local"), SYSTEM("system");

        private final String rawOp;

        ImportType(String rawOp) {
            this.rawOp = rawOp;
        }

        public String getRawOp() {
            return rawOp;
        }
    }

    // Begin Generated AST classes

    public static class Statement  {

        public Statement() {


        }

        public String toString() {
            return "Statement()";
        }

        public String toFlatString() {
            return "Statement()";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileStatement(this, context);
        }
    }

    public static class Expression extends Statement {

        public Expression() {
            super();

        }

        public String toString() {
            return "Expression()";
        }

        public String toFlatString() {
            return "Expression()";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileExpression(this, context);
        }
    }

    public static class Type  {
        public final static Type VOID = new Type(new Variable("Void"), 0, new ArrayList<>());

        public final Statement name;
        public final Integer arraySize;
        public final List<Type> genericTypes;
        public Type(Statement name, Integer arraySize, List<Type> genericTypes) {

            this.name = name;
            this.arraySize = arraySize;
            this.genericTypes = genericTypes;
        }

        public String toString() {
            return "Type(name = " + this.name.toString() + ", " +
                    "arraySize = " + this.arraySize.toString() + ", " +
                    "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "Type(name = " + this.name.toFlatString() + ", " +
                    "arraySize = " + this.arraySize.toString() + ", " +
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
        public BlockStatement(List<Statement> body) {
            super();
            this.body = body;
        }

        public String toString() {
            return "BlockStatement(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "BlockStatement(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
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
        public FunctionDef(List<GenericType> genericTypes, Type returns, String name, List<FunctionParam> args, List<Statement> body) {
            super(body);
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
                    "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "FunctionDef(genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " +
                    "returns = " + this.returns.toFlatString() + ", " +
                    "name = " + this.name.toString() + ", " +
                    "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " +
                    "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileFunctionDef(this, context);
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

    public static class AnonFunctionDef extends BlockStatement {
        public final Type returns;
        public final String name;
        public final List<FunctionParam> args;
        public AnonFunctionDef(Type returns, String name, List<FunctionParam> args, List<Statement> body) {
            super(body);
            this.returns = returns;
            this.name = name;
            this.args = args;
        }

        public String toString() {
            return "AnonFunctionDef(returns = " + this.returns.toString() + ", " +
                    "name = " + this.name.toString() + ", " +
                    "args = " + "[" + this.args.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " +
                    "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "AnonFunctionDef(returns = " + this.returns.toFlatString() + ", " +
                    "name = " + this.name.toString() + ", " +
                    "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " +
                    "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileAnonFunctionDef(this, context);
        }
    }

    public static class ClassDef extends BlockStatement {
        public final String name;
        public final List<GenericType> genericTypes;
        public final List<Type> extendClasses;
        public ClassDef(String name, List<GenericType> genericTypes, List<Type> extendClasses, List<Statement> body) {
            super(body);
            this.name = name;
            this.genericTypes = genericTypes;
            this.extendClasses = extendClasses;
        }

        public String toString() {
            return "ClassDef(name = " + this.name.toString() + ", " +
                    "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " +
                    "extendClasses = " + "[" + this.extendClasses.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " +
                    "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "ClassDef(name = " + this.name.toString() + ", " +
                    "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " +
                    "extendClasses = " + "[" + this.extendClasses.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " +
                    "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileClassDef(this, context);
        }
    }


    public static class IfBlock extends BlockStatement {
        public final Statement condition;
        public Statement elseBlock;
        public IfBlock(Statement condition, List<Statement> body) {
            super(body);
            this.condition = condition;
            this.elseBlock = new ElseBlock();
        }

        public String toString() {
            return "IfBlock(condition = " + this.condition.toString() + ", " +
                    "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ", " +
                    "elseBlock = " + this.elseBlock.toString() + ")";
        }

        public String toFlatString() {
            return "IfBlock(condition = " + this.condition.toFlatString() + ", " +
                    "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " +
                    "elseBlock = " + this.elseBlock.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileIfBlock(this, context);
        }
    }

    public static class ElseBlock extends Statement {

        public ElseBlock() {
            super();

        }

        public String toString() {
            return "ElseBlock()";
        }

        public String toFlatString() {
            return "ElseBlock()";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileElseBlock(this, context);
        }
    }

    public static class ElseIfBlock extends ElseBlock {
        public final IfBlock on;
        public ElseBlock elseBlock;
        public ElseIfBlock(IfBlock on) {
            super();
            this.on = on;
            this.elseBlock = new ElseBlock();
        }

        public String toString() {
            return "ElseIfBlock(on = " + this.on.toString() + ", " +
                    "elseBlock = " + this.elseBlock.toString() + ")";
        }

        public String toFlatString() {
            return "ElseIfBlock(on = " + this.on.toFlatString() + ", " +
                    "elseBlock = " + this.elseBlock.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileElseIfBlock(this, context);
        }
    }

    public static class ElseAlwaysBlock extends ElseBlock {
        public final List<Statement> body;
        public ElseAlwaysBlock(List<Statement> body) {
            super();
            this.body = body;
        }

        public String toString() {
            return "ElseAlwaysBlock(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "ElseAlwaysBlock(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileElseAlwaysBlock(this, context);
        }
    }

    public static class WhileBlock extends BlockStatement {
        public final Statement condition;
        public WhileBlock(Statement condition, List<Statement> body) {
            super(body);
            this.condition = condition;
        }

        public String toString() {
            return "WhileBlock(condition = " + this.condition.toString() + ", " +
                    "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "WhileBlock(condition = " + this.condition.toFlatString() + ", " +
                    "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileWhileBlock(this, context);
        }
    }

    public static class LoopBlock extends BlockStatement {

        public LoopBlock(List<Statement> body) {
            super(body);

        }

        public String toString() {
            return "LoopBlock(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "LoopBlock(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileLoopBlock(this, context);
        }
    }

    public static class ForBlock extends BlockStatement {
        public final Statement start;
        public final Statement condition;
        public final Statement step;
        public ForBlock(Statement start, Statement condition, Statement step, List<Statement> body) {
            super(body);
            this.start = start;
            this.condition = condition;
            this.step = step;
        }

        public String toString() {
            return "ForBlock(start = " + this.start.toString() + ", " +
                    "condition = " + this.condition.toString() + ", " +
                    "step = " + this.step.toString() + ", " +
                    "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "ForBlock(start = " + this.start.toFlatString() + ", " +
                    "condition = " + this.condition.toFlatString() + ", " +
                    "step = " + this.step.toFlatString() + ", " +
                    "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileForBlock(this, context);
        }
    }

    public static class ForIterBlock extends BlockStatement {
        public final Statement item;
        public final Statement list;
        public ForIterBlock(Statement item, Statement list, List<Statement> body) {
            super(body);
            this.item = item;
            this.list = list;
        }

        public String toString() {
            return "ForIterBlock(item = " + this.item.toString() + ", " +
                    "list = " + this.list.toString() + ", " +
                    "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "ForIterBlock(item = " + this.item.toFlatString() + ", " +
                    "list = " + this.list.toFlatString() + ", " +
                    "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileForIterBlock(this, context);
        }
    }

    public static class MatchBlock extends BlockStatement {
        public final Statement on;
        public MatchBlock(Statement on, List<Statement> body) {
            super(body);
            this.on = on;
        }

        public String toString() {
            return "MatchBlock(on = " + this.on.toString() + ", " +
                    "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "MatchBlock(on = " + this.on.toFlatString() + ", " +
                    "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileMatchBlock(this, context);
        }
    }

    public static class SwitchBlock extends BlockStatement {
        public final Statement on;
        public SwitchBlock(Statement on, List<Statement> body) {
            super(body);
            this.on = on;
        }

        public String toString() {
            return "SwitchBlock(on = " + this.on.toString() + ", " +
                    "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "SwitchBlock(on = " + this.on.toFlatString() + ", " +
                    "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileSwitchBlock(this, context);
        }
    }

    public static class CaseStatement extends Expression {
        public final Statement on;
        public CaseStatement(Statement on) {
            super();
            this.on = on;
        }

        public String toString() {
            return "CaseStatement(on = " + this.on.toString() + ")";
        }

        public String toFlatString() {
            return "CaseStatement(on = " + this.on.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileCaseStatement(this, context);
        }
    }

    public static class TryBlock extends BlockStatement {

        public TryBlock(List<Statement> body) {
            super(body);

        }

        public String toString() {
            return "TryBlock(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "TryBlock(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileTryBlock(this, context);
        }
    }

    public static class CatchBlock extends BlockStatement {
        public final Statement ex;
        public CatchBlock(Statement ex, List<Statement> body) {
            super(body);
            this.ex = ex;
        }

        public String toString() {
            return "CatchBlock(ex = " + this.ex.toString() + ", " +
                    "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "CatchBlock(ex = " + this.ex.toFlatString() + ", " +
                    "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileCatchBlock(this, context);
        }
    }

    public static class FinallyBlock extends BlockStatement {

        public FinallyBlock(List<Statement> body) {
            super(body);

        }

        public String toString() {
            return "FinallyBlock(body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
        }

        public String toFlatString() {
            return "FinallyBlock(body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileFinallyBlock(this, context);
        }
    }

    public static class FunctionCall extends Expression {
        public final String name;
        public final List<Statement> args;
        public final List<Type> genericTypes;
        public FunctionCall(String name, List<Statement> args, List<Type> genericTypes) {
            super();
            this.name = name;
            this.args = args;
            this.genericTypes = genericTypes;
        }

        public String toString() {
            return "FunctionCall(name = " + this.name.toString() + ", " +
                    "args = " + "[" + this.args.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " +
                    "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "FunctionCall(name = " + this.name.toString() + ", " +
                    "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " +
                    "genericTypes = " + "[" + this.genericTypes.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileFunctionCall(this, context);
        }
    }

    public static class Parened extends Expression {
        public final Statement sub;
        public Parened(Statement sub) {
            super();
            this.sub = sub;
        }

        public String toString() {
            return "Parened(sub = " + this.sub.toString() + ")";
        }

        public String toFlatString() {
            return "Parened(sub = " + this.sub.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileParened(this, context);
        }
    }

    public static class Dotted extends Expression {
        public final Statement left;
        public final Statement right;
        public Dotted(Statement left, Statement right) {
            super();
            this.left = left;
            this.right = right;
        }

        public String toString() {
            return "Dotted(left = " + this.left.toString() + ", " +
                    "right = " + this.right.toString() + ")";
        }

        public String toFlatString() {
            return "Dotted(left = " + this.left.toFlatString() + ", " +
                    "right = " + this.right.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileDotted(this, context);
        }
    }

    public static class Arrowed extends Expression {
        public final Statement left;
        public final Statement right;
        public Arrowed(Statement left, Statement right) {
            super();
            this.left = left;
            this.right = right;
        }

        public String toString() {
            return "Arrowed(left = " + this.left.toString() + ", " +
                    "right = " + this.right.toString() + ")";
        }

        public String toFlatString() {
            return "Arrowed(left = " + this.left.toFlatString() + ", " +
                    "right = " + this.right.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileArrowed(this, context);
        }
    }

    public static class Return extends Expression {
        public final Statement value;
        public Return(Statement value) {
            super();
            this.value = value;
        }

        public String toString() {
            return "Return(value = " + this.value.toString() + ")";
        }

        public String toFlatString() {
            return "Return(value = " + this.value.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileReturn(this, context);
        }
    }

    public static class New extends Expression {
        public final Statement type;
        public final List<Statement> argz;
        public New(Statement type, List<Statement> argz) {
            super();
            this.type = type;
            this.argz = argz;
        }

        public String toString() {
            return "New(type = " + this.type.toString() + ", " +
                "argz = " + "[" + this.argz.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public String toFlatString() {
            return "New(type = " + this.type.toFlatString() + ", " +
                "argz = " + "[" + this.argz.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileNew(this, context);
        }
    }

    public static class ThrowStatement extends Expression {
        public final Statement value;
        public ThrowStatement(Statement value) {
            super();
            this.value = value;
        }

        public String toString() {
            return "ThrowStatement(value = " + this.value.toString() + ")";
        }

        public String toFlatString() {
            return "ThrowStatement(value = " + this.value.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileThrowStatement(this, context);
        }
    }

    public static class ImportStatement extends Expression {
        public final ImportType type;
        public final String value;
        public ImportStatement(ImportType type, String value) {
            super();
            this.type = type;
            this.value = value;
        }

        public String toString() {
            return "ImportStatement(type = " + this.type.toString() + ", " +
                    "value = " + this.value.toString() + ")";
        }

        public String toFlatString() {
            return "ImportStatement(type = " + this.type.toString() + ", " +
                    "value = " + this.value.toString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileImportStatement(this, context);
        }
    }

    public static class MathStatement extends Expression {
        public final Statement left;
        public final MathOp op;
        public final Statement right;
        public MathStatement(Statement left, MathOp op, Statement right) {
            super();
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "MathStatement(left = " + this.left.toString() + ", " +
                    "op = " + this.op.toString() + ", " +
                    "right = " + this.right.toString() + ")";
        }

        public String toFlatString() {
            return "MathStatement(left = " + this.left.toFlatString() + ", " +
                    "op = " + this.op.toString() + ", " +
                    "right = " + this.right.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileMathStatement(this, context);
        }
    }

    public static class MathAssign extends Expression {
        public final Statement left;
        public final MathOp op;
        public final Statement right;
        public MathAssign(Statement left, MathOp op, Statement right) {
            super();
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "MathAssign(left = " + this.left.toString() + ", " +
                    "op = " + this.op.toString() + ", " +
                    "right = " + this.right.toString() + ")";
        }

        public String toFlatString() {
            return "MathAssign(left = " + this.left.toFlatString() + ", " +
                    "op = " + this.op.toString() + ", " +
                    "right = " + this.right.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileMathAssign(this, context);
        }
    }

    public static class MathSelfMod extends Expression {
        public final Statement left;
        public final MathOp op;
        public MathSelfMod(Statement left, MathOp op) {
            super();
            this.left = left;
            this.op = op;
        }

        public String toString() {
            return "MathSelfMod(left = " + this.left.toString() + ", " +
                    "op = " + this.op.toString() + ")";
        }

        public String toFlatString() {
            return "MathSelfMod(left = " + this.left.toFlatString() + ", " +
                    "op = " + this.op.toString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileMathSelfMod(this, context);
        }
    }

    public static class Subscript extends Expression {
        public final Statement left;
        public final Statement sub;
        public Subscript(Statement left, Statement sub) {
            super();
            this.left = left;
            this.sub = sub;
        }

        public String toString() {
            return "Subscript(left = " + this.left.toString() + ", " +
                    "sub = " + this.sub.toString() + ")";
        }

        public String toFlatString() {
            return "Subscript(left = " + this.left.toFlatString() + ", " +
                    "sub = " + this.sub.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileSubscript(this, context);
        }
    }

    public static class Compare extends Expression {
        public final Statement left;
        public final CompareOp op;
        public final Statement right;
        public Compare(Statement left, CompareOp op, Statement right) {
            super();
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "Compare(left = " + this.left.toString() + ", " +
                    "op = " + this.op.toString() + ", " +
                    "right = " + this.right.toString() + ")";
        }

        public String toFlatString() {
            return "Compare(left = " + this.left.toFlatString() + ", " +
                    "op = " + this.op.toString() + ", " +
                    "right = " + this.right.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileCompare(this, context);
        }
    }

    public static class BoolCombine extends Expression {
        public final Statement left;
        public final BoolOp op;
        public final Statement right;
        public BoolCombine(Statement left, BoolOp op, Statement right) {
            super();
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public String toString() {
            return "BoolCombine(left = " + this.left.toString() + ", " +
                    "op = " + this.op.toString() + ", " +
                    "right = " + this.right.toString() + ")";
        }

        public String toFlatString() {
            return "BoolCombine(left = " + this.left.toFlatString() + ", " +
                    "op = " + this.op.toString() + ", " +
                    "right = " + this.right.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileBoolCombine(this, context);
        }
    }

    public static class Assign extends Expression {
        public final Statement name;
        public final Statement value;
        public Assign(Statement name, Statement value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String toString() {
            return "Assign(name = " + this.name.toString() + ", " +
                    "value = " + this.value.toString() + ")";
        }

        public String toFlatString() {
            return "Assign(name = " + this.name.toFlatString() + ", " +
                    "value = " + this.value.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileAssign(this, context);
        }
    }

    public static class Declare extends Statement {
        public final Type type;
        public final String name;
        public Declare(Type type, String name) {
            super();
            this.type = type;
            this.name = name;
        }

        public String toString() {
            return "Declare(type = " + this.type.toString() + ", " +
                    "name = " + this.name.toString() + ")";
        }

        public String toFlatString() {
            return "Declare(type = " + this.type.toFlatString() + ", " +
                    "name = " + this.name.toString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileDeclare(this, context);
        }
    }

    public static class DeclareAssign extends Statement {
        public final Type type;
        public final String name;
        public final Statement value;
        public DeclareAssign(Type type, String name, Statement value) {
            super();
            this.type = type;
            this.name = name;
            this.value = value;
        }

        public String toString() {
            return "DeclareAssign(type = " + this.type.toString() + ", " +
                    "name = " + this.name.toString() + ", " +
                    "value = " + this.value.toString() + ")";
        }

        public String toFlatString() {
            return "DeclareAssign(type = " + this.type.toFlatString() + ", " +
                    "name = " + this.name.toString() + ", " +
                    "value = " + this.value.toFlatString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileDeclareAssign(this, context);
        }
    }

    public static class NumberLiteral extends Expression {
        public final Double value;
        public NumberLiteral(Double value) {
            super();
            this.value = value;
        }

        public String toString() {
            return "NumberLiteral(value = " + this.value.toString() + ")";
        }

        public String toFlatString() {
            return "NumberLiteral(value = " + this.value.toString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileNumberLiteral(this, context);
        }
    }

    public static class StringLiteral extends Expression {
        public final String value;
        public StringLiteral(String value) {
            super();
            this.value = value;
        }

        public String toString() {
            return "StringLiteral(value = " + "\"" + this.value.toString() + "\"" + ")";
        }

        public String toFlatString() {
            return "StringLiteral(value = " + "\"" + this.value.toString() + "\"" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileStringLiteral(this, context);
        }
    }

    public static class SequenceLiteral extends Expression {
        public final String value;
        public SequenceLiteral(String value) {
            super();
            this.value = value;
        }

        public String toString() {
            return "SequenceLiteral(value = " + "\"" + this.value.toString() + "\"" + ")";
        }

        public String toFlatString() {
            return "SequenceLiteral(value = " + "\"" + this.value.toString() + "\"" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileSequenceLiteral(this, context);
        }
    }

    public static class BooleanLiteral extends Expression {
        public final Boolean value;
        public BooleanLiteral(Boolean value) {
            super();
            this.value = value;
        }

        public String toString() {
            return "BooleanLiteral(value = " + "\"" + this.value.toString() + "\"" + ")";
        }

        public String toFlatString() {
            return "BooleanLiteral(value = " + "\"" + this.value.toString() + "\"" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileBooleanLiteral(this, context);
        }
    }

    public static class RegexLiteral extends Expression {
        public final String pattern;
        public final String flags;
        public RegexLiteral(String pattern, String flags) {
            super();
            this.pattern = pattern;
            this.flags = flags;
        }

        public String toString() {
            return "RegexLiteral(pattern = " + "\"" + this.pattern.toString() + "\"" + ", " +
                    "flags = " + "\"" + this.flags.toString() + "\"" + ")";
        }

        public String toFlatString() {
            return "RegexLiteral(pattern = " + "\"" + this.pattern.toString() + "\"" + ", " +
                    "flags = " + "\"" + this.flags.toString() + "\"" + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileRegexLiteral(this, context);
        }
    }

    public static class Variable extends Expression {
        public final String name;
        public Variable(String name) {
            super();
            this.name = name;
        }

        public String toString() {
            return "Variable(name = " + this.name.toString() + ")";
        }

        public String toFlatString() {
            return "Variable(name = " + this.name.toString() + ")";
        }

        public CompiledCode compile(CompileContext context) {
            return ASTVisitor.instance.compileVariable(this, context);
        }
    }

    // End generated AST
}
