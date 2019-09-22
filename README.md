Skiff
==
Skiff is a refrence based Java like programming language designed for compilation into C. The project is mainly designed as an experiment into the inner working of a programming lanugage and compiler. The language is designed with the hope that it will allow full compilation into C as well as interpretation on a simple, python like virtual machine.

There are some good examples of what Skiff is currently capable of in the [integration tests](https://github.com/DonoA/Skiff/tree/master/src/test/resources).

## Skiff to C transpiler:
Skiff is designed to be translated into C but support many features that C does not. Skiff supports classes, garbage collection, and closures all while transpiling into portable and (mostly) readable C code. This will also allow the language implicit compatibility with C functions.

## Statements And Implementation:
| Name | Example | Parse | Compile | Test |
| ---- | ------- | ----- | ------- | ---- |
| Declare Variable | `name: Type` | ✓ | ✓ | ✓ | 
| Assignment | `name = value` | ✓ | ✓ | ✓ |
| Declare and assign | `name: Type = value` | ✓ | ✓ | ✓ |
| Function Call | `functionName(p1, p2)` | ✓ | ✓ | ✓ |
| Function Def | `def functionName(p1: T1, p2: T2): Returns {...}` | ✓ | ✓ | ✓ |
| Class Def | `class ClassName {}` | ✓ | ✓ | ✓ |
| Data Class | `struct ClassName {}` | ✓ | ✓ | ✓ |
| Create instance | `new ClassName()` | ✓ | ✓ | ✓ |
| Return from function | `return value` | ✓ | ✓ | ✓ |
| String Literal | `"String"` | ✓ | ✓ | ✓ |
| Char Sequence | `'Sequence'` | ✓ | ⅹ | ⅹ | 
| Regex Literal | `r/regex/flags` | ✓ | ⅹ | ⅹ |
| Bool Literal | `true false` | ✓ | ✓ | ✓ |
| Bool Combos | <code>&& &#124;&#124;</code> | ✓ | ✓ | ✓ |
| Compare | `== < <= > >= !=` | ✓ | ✓ | ✓ |
| Math | `+ - * / % **` | ✓ | ✓ | ✓ |
| Math Assign| `+= -=` | ✓ | ✓ | ✓ |
| Quick Inc/Dec | `++ --` | ✓ | ✓ | ⅹ |
| Generic Class Def | `class Cls<T, U> {}` | ✓ | ✓ | ✓ |
| Generic Class Use | `new Cls<T>()` | ✓ | ✓ | ✓ |
| Generic Extension | `class Cls<T : U> {}` | ✓ | ⅹ | ⅹ |
| Generic Func Def | `def func<T>(p: T): Returns {}` | ✓ | ✓ | ✓ |
| Inheritance | `class Child : Parent` | ✓ | ✓ | ✓ |
| List Index | `list[index]` | ✓ | ✓ | ✓ |
| Import Source | `import <file>` | ✓ | ✓ | ✓ |
| Dec Mods | `static private` | ✓ | ✓ | ✓ |
| Anon Func Def | `(p: T): Returns => {}` | ✓ | ⅹ | ⅹ |
| Anon Func Type | `funcVar: (T) => Returns` | ✓ | ⅹ | ⅹ |
| If | `if(cond) {}` | ✓ | ✓ | ✓ |
| Else | `{} else if() {}` | ✓ | ✓ | ✓ |
| While | `while(cond) {}` | ✓ | ✓ | ✓ |
| Loop | `loop {}` | ✓ | ✓ | ✓ |
| For | `for(i: Int = v; i < max; i++) {}` | ✓ | ✓ | ✓ |
| For Iter | `for(v: Int in intList) {}` | ⅹ | ⅹ | ⅹ |
| Loop flow | `next break` | ✓ | ✓ | ✓ |
| Switch | `switch(v){case val => ...}` | ✓ | ✓ | ✓ |
| Match | `match(v) {case v:T => ` | ✓ | ✓ | ✓ |
| Deconstruction | `Struct(v1, v2) = myStruct` | ✓ | ✓ | ✓ |
| Try/Catch | `try {} catch(e: MyException) {}` | ✓ | ✓ | ✓ |
| Throw | `throw new MyException()` | ✓ | ✓ | ✓ |

## Specifications:
### Classes and Structs

Classes are collections of functions with access to particular data about the object they represent. They function exactly as one would expect from C++ or Java. Functions declared inside a class are methods and have access to the `this` object. Variables declared in outside any function within the class are considered instance variables. A constructor is denoted by a function within the class that shares its name and has no return type. Example:
```
class SampleClass {

    instanceVariable: Int;

    def SampleClass(var: Int) {
        this.instanceVariable = var;
    }

    def getInstanceVariable(): Int {
        return instanceVariable;
    }
}
```

Structs are very similar to their C/C++ partners but are more fully featured. Structs can only contain field declarations but generate getters and setter. In addition, a toString and simple all args constructor is generated for the class. The main advantage of structs is that they can be deconstructed easily to allow the use of their internal data. Example:
```
struct SampleDataClass {
    var1: Int;
    var2: String;
}

myData: SampleDataClass = new SampleDataClass(10, "StringData");
SampleDataClass(myInt, myString) = myData;
```

### Functions
Functions declared outside a class can be thought of like any other object except they can be called. In addition, a functions type must be clearly defined. For example, the following would be equivilent:
```
def myFunction(arg: Int): String {
    return arg.toString();
}
```
and
```
myFunction: (Int) => String = (arg: Int): String => { return arg.toString() };
```

Functions are objects, but so are methods. When a method is converted to a function object, the `this` reference is baked in implicitly:

```
class MyClass {
    var: Int;
    def MyClass(var: Int) {
        this.var = var;
    }
    def getVar(): Int {
        return var;
    }
}

myInst: MyClass = new MyClass(10);

myInstGetVar: () => Int = myInst.getVar;
```

A call to `print(myInstGetVar())` would thus print `10`. This is because assigning `myInst.getVar` to another variable essentially creates a closue with `myInst` in the place of `this`.

### Operator annotation
Any method that returns can be bound to the basic operators: `+, -, *, /, %, +=, -=, *=, /=, %=, ==, >, <, []`. To bind a method to a basic operator, the Operator annotation is used:
```
class MyClass {
    @Operator(Operators.PLUS)
    def plus(other: MyClass): MyClass {
        ...
    }
    ...
}
```

Much like Scala, these operators are simply methods on the object to the left of them, however unlike Scala, the number of different operator symbols that can be used it limited to widely known and understood ones to prevent more confusing operators from being used.

### Switch and Match:
Switch functions attempt to match the value of the object given by `switch(object)` to one of the cases listed. This is done through the `==` method which can be overridden on a class by class basis. A wildcard entry, denoted `case _` will match any value.

Match functions work in much the same way except the class of the object is searched on as opposed to the value. Data classes can have their data extracted implicitly using the `case val: MyStruct(val) => {}` syntax. When using a match, the object being searched on will be cast to the matching class when that cases is called. Suppose we have the following:
```
class MyClass {
    val: Int = 5;
}

struct MyStruct {
    val1: String = "Hello";
    val2: String = "World";
}

def getType(val: Any): String {
    match(val){
        case v: MyClass => {
            return "MyClass, val = " + v.val.to_string();
        }
        case v: MyStruct(v1, _) => {
            return "MyStruct, val1 = " + v1 + ", val2 = " + v.val2;
        }
        case _ => {
            return "Unknown";
        }
    }
}
```
### Truthiness
The only values which evaluate to false are `None`, `false`, and `0`. Any other value will evaluate to `true`. In addition, the evaluation of booleans is a fail fast operation. Thus `if(!value.isNone() && value.call_function())` is valid regardless of if value is `None`.

Methods can override the operation that will be performed when the access `[]` is called on the class as well as the access and assignment `[] =` is called on the class. Through this methodology, the way array access and modification is used can vary based on class.

### Namespaces/Packages
Skiff does not have the concept of a global scope so each file is executed on its own. By importing other files and modules, additional values can be added to this scope. Much like Python, everything is an object and can thus be built that way. Classes can be constructed an returned as the values of functions which means that all values within the global scope of a module are imported under that modules name. Packages may be aliased for ease of use, however often their names are quite verbose.

### Baked in types
There are several baked in types. `Char` `Short` `Int` `Long` `Float` and `Double` are the basic number classes, `String` and `Sequence` are the basic string classes, `Boolean` is the basic boolean class and `Array` is the basic array class. These classes are all contained under the package `skiff.lang` so without aliasing to access a method of the `Int` class, the full name `skiff.lang.Int.method()` would need to be used.

The basic number classes all work as expected, the only difference between the classes is the way their math operators are handled. A `String` is mutable meaning it can expand without needing to copy over its elements while `Sequence` is immutable. One side effect of this is that `String` supports assignment math (`+=`, `-=`, etc.) while `Sequence` does not.

## Implementation
### Garbage collection
Skiff implements a generational garbage collector. When the eden heap if filled up, the garbage collector is automatically triggered to locate some space for new variables. The skiff gc is a simple mark a sweep garbage collector. It currently runs the same execution thread as the rest of the skiff code, meaning that it stops to world for marking, sweeping, and memory compaction.
### Linked C Libraries
Skiff easily allows other C libraries to be linked in at compile time to allow for new modules and classes to be added without touching the internals of the compiler. Skiff classes and functions that are prefixed with the keywork `native` are defined to the compiler but their implementation does not need to be specified. When compiling the outputted C code, simply include the C files with the implementations for the functions that were declared native and the code will pick up on them. 