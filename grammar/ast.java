
public static class Statement  {
    
    public Statement() {
        
        
    }

    public String toString() {
        return "Statement()";
    }

    public String toFlatString() {
        return "Statement()";
    }

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileStatement(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileExpression(this, context);
    }
}
    
public static class Type  {
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileType(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileBlockStatement(this, context);
    }
}
    
public static class FunctionDef extends BlockStatement {
    public final Type returns;
    public final String name;
    public final List<FunctionParam> args;
    public FunctionDef(Type returns, String name, List<FunctionParam> args, List<Statement> body) {
        super(body);
        this.returns = returns;
        this.name = name;
        this.args = args;
    }

    public String toString() {
        return "FunctionDef(returns = " + this.returns.toString() + ", " + 
            "name = " + this.name.toString() + ", " + 
            "args = " + "[" + this.args.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
            "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
    }

    public String toFlatString() {
        return "FunctionDef(returns = " + this.returns.toFlatString() + ", " + 
            "name = " + this.name.toString() + ", " + 
            "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
            "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
    }

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileFunctionDef(this, context);
    }
}
    
public static class ClassDef extends BlockStatement {
    public final String name;
    public final List<Statement> extendClasses;
    public ClassDef(String name, List<Statement> extendClasses, List<Statement> body) {
        super(body);
        this.name = name;
        this.extendClasses = extendClasses;
    }

    public String toString() {
        return "ClassDef(name = " + this.name.toString() + ", " + 
            "extendClasses = " + "[" + this.extendClasses.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
            "body = " + "[\n" + this.body.stream().map(e -> e.toString()).collect(Collectors.joining(", \n")) + " \n]" + ")";
    }

    public String toFlatString() {
        return "ClassDef(name = " + this.name.toString() + ", " + 
            "extendClasses = " + "[" + this.extendClasses.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ", " + 
            "body = " + "[" + this.body.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
    }

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileClassDef(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileFunctionParam(this, context);
    }
}
    
public static class IfBlock extends BlockStatement {
    public final Statement condition;
    public ElseBlock elseBlock;
    public IfBlock(Statement condition, List<Statement> body) {
        super(body);
        this.condition = condition;
        this.elseBlock = null;
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileIfBlock(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileElseBlock(this, context);
    }
}
    
public static class ElseIfBlock extends ElseBlock {
    public final IfBlock on;
    public ElseBlock elseBlock;
    public ElseIfBlock(IfBlock on) {
        super();
        this.on = on;
        this.elseBlock = null;
    }

    public String toString() {
        return "ElseIfBlock(on = " + this.on.toString() + ", " + 
            "elseBlock = " + this.elseBlock.toString() + ")";
    }

    public String toFlatString() {
        return "ElseIfBlock(on = " + this.on.toFlatString() + ", " + 
            "elseBlock = " + this.elseBlock.toFlatString() + ")";
    }

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileElseIfBlock(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileElseAlwaysBlock(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileWhileBlock(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileForBlock(this, context);
    }
}
    
public static class FunctionCall extends Expression {
    public final String name;
    public final List<Statement> args;
    public FunctionCall(String name, List<Statement> args) {
        super();
        this.name = name;
        this.args = args;
    }

    public String toString() {
        return "FunctionCall(name = " + this.name.toString() + ", " + 
            "args = " + "[" + this.args.stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + " ]" + ")";
    }

    public String toFlatString() {
        return "FunctionCall(name = " + this.name.toString() + ", " + 
            "args = " + "[" + this.args.stream().map(e -> e.toFlatString()).collect(Collectors.joining(", ")) + " ]" + ")";
    }

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileFunctionCall(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileParened(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileDotted(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileArrowed(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileReturn(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileNew(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileMathStatement(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileMathAssign(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileSubscript(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileCompare(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileBoolCombine(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileAssign(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileDeclare(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileDeclareAssign(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileNumberLiteral(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileStringLiteral(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileVariable(this, context);
    }
}
    