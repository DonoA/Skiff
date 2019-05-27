
public static class Statement  {
    
    public Statement() {
        
        
    }
    public String toString() {
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
            "genericTypes = " + "[" + this.genericTypes.stream().map(Objects::toString).collect(Collectors.joining(", ")) + " ]" + ")";
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
        return "BlockStatement(body = " + "[\n" + this.body.stream().map(Objects::toString).collect(Collectors.joining(", \n")) + " \n]" + ")";
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
            "args = " + "[" + this.args.stream().map(Objects::toString).collect(Collectors.joining(", ")) + " ]" + ", " + 
            "body = " + "[\n" + this.body.stream().map(Objects::toString).collect(Collectors.joining(", \n")) + " \n]" + ")";
    }

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileFunctionDef(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileFunctionParam(this, context);
    }
}
    
public static class IfBlock extends BlockStatement {
    public final Statement condition;
    public IfBlock(Statement condition, List<Statement> body) {
        super(body);
        this.condition = condition;
    }
    public String toString() {
        return "IfBlock(condition = " + this.condition.toString() + ", " + 
            "body = " + "[\n" + this.body.stream().map(Objects::toString).collect(Collectors.joining(", \n")) + " \n]" + ")";
    }

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileIfBlock(this, context);
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
            "body = " + "[\n" + this.body.stream().map(Objects::toString).collect(Collectors.joining(", \n")) + " \n]" + ")";
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
            "body = " + "[\n" + this.body.stream().map(Objects::toString).collect(Collectors.joining(", \n")) + " \n]" + ")";
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
            "args = " + "[" + this.args.stream().map(Objects::toString).collect(Collectors.joining(", ")) + " ]" + ")";
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileReturn(this, context);
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

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {
        return visitor.compileVariable(this, context);
    }
}
    