Skiff
==
Skiff is a refrence based Java like programming language designed for compilation into C. The project is mainly designed as an experiment into the inner working of a programming lanugage and compiler. The language is designed with the hope that it will allow full compilation into C as well as interpretation on a simple, jvm like virtual machine.

## Skiff to C transpiler:
Skiff is designed to be translated into C but support many features that C does not. Ideally Skiff will support classes, garbage collection, and closures all while transpiling into C. This will also allow the language implicit compatibility with C functions.

## Skiff on a VM
Compiling Skiff into a simple VM targetting assembly language will require a deeper understanding of how a machine operates and force Skiff to understand the meaning of the code more than a simple compiler will.

## Statements And Implementation:
| Name | Example | Parse | Compile | Test |
| ---- | ------- | ----- | ------- | ---- |
| Declare Variable | `name: Type` | ✓ | ✓ | ⅹ | 
| Assignment | `name = value` | ✓ | ✓ | ⅹ |
| Declare and assign | `name: Type = value` | ⅹ | ⅹ | ⅹ |
| Function Call | `functionName(p1, p2)` | ✓ | ✓ | ⅹ |
| Function Def | `def functionName(p1: T1, p2: T2): Returns {...}` | ✓ | ✓ | ⅹ |
| Class Def | `class ClassName {}` | ✓ | ✓ | ⅹ |
| Data Class | `struct ClassName {}` | ⅹ | ⅹ | ⅹ |
| Create instance | `new ClassName()` | ✓ | ✓ | ⅹ |
| Return from function | `return value` | ✓ | ✓ | ⅹ |
| String Literal | `"String"` | ✓ | ✓ | ⅹ |
| Char Sequence | `'Sequence'` | ⅹ | ⅹ | ⅹ | 
| Regex Literal | `r/regex/flags` | ⅹ | ⅹ | ⅹ |
| Bool Literal | `true false` | ⅹ | ⅹ | ⅹ |
| Bool Combos | <code>&& &#124;&#124;</code> | ⅹ | ⅹ | ⅹ |
| Compare | `== < <= > >= !=` | ✓ | ⅹ | ⅹ |
| Math | `+ - * / % **` | ✓ | ✓ | ⅹ |
| Math Assign| `+= -=` | ⅹ | ⅹ | ⅹ |
| Quick Inc/Dec | `++ --` | ⅹ | ⅹ | ⅹ |
| Generic Class Def | `class Cls<T, U> {}` | ⅹ | ⅹ | ⅹ |
| Generic Class Use | `new Cls<T>()` | ⅹ | ⅹ | ⅹ |
| Generic Extension | `class Cls<T : U> {}` | ⅹ | ⅹ | ⅹ |
| Generic Func Def | `def func<T>(p: T): Returns {}` | ⅹ | ⅹ | ⅹ |
| Inheritance | `class Child : Parent` | ⅹ | ⅹ | ⅹ |
| List Index | `list[index]` | ✓ | ✓ | ⅹ |
| Import Source | `import <file>` | ⅹ | ⅹ | ⅹ |
| Dec Mods | `static private` | ⅹ | ⅹ | ⅹ |
| Anon Func Def | `(p: T): Returns => {}` | ⅹ | ⅹ | ⅹ |
| Anon Func Type | `funcVar: (T) => Returns` | ⅹ | ⅹ | ⅹ |
| If | `if(cond) {}` | ⅹ | ⅹ | ⅹ |
| Else | `{} else if() {}` | ⅹ | ⅹ | ⅹ |
| While | `while(cond) {}` | ⅹ | ⅹ | ⅹ |
| Loop | `loop {}` | ⅹ | ⅹ | ⅹ |
| For | `for(i: Int = v; i < max; i++) {}` | ⅹ | ⅹ | ⅹ |
| For Iter | `for(v: Int in intList) {}` | ⅹ | ⅹ | ⅹ |
| Loop flow | `next break` | ⅹ | ⅹ | ⅹ |
| Switch | `switch(v){case val=>{}...}` | ⅹ | ⅹ | ⅹ |
| Match | `match(v) {case v:T=>{}}` | ⅹ | ⅹ | ⅹ |
| Deconstruction | `Struct(v1, v2) = myStruct` | ⅹ | ⅹ | ⅹ |
| Try/Catch | `try {} catch(e: MyException) {}` | ⅹ | ⅹ | ⅹ |
| Throw | `throw new MyException()` | ⅹ | ⅹ | ⅹ |

## Specifications:
### Namespaces/Packages
Skiff does not have the concept of a global scope so each file is executed on its own. By importing other files and modules, additional values can be added to this scope. Much like Python, everything is an object and can thus be built that way. Classes can be constructed an returned as the values of functions which means that all values within the global scope of a module are imported under that modules name. Packages may be aliased for ease of use, however often their names are quite verbose.

### Baked in types
There are several baked in types. `Char` `Short` `Int` `Long` `Float` and `Double` are the basic number classes, `String` and `Sequence` are the basic string classes, `Boolean` is the basic boolean class and `Array` is the basic array class. These classes are all contained under the package `skiff.lang` so without aliasing to access a method of the `Int` class, the full name `skiff.lang.Int.method()` would need to be used.

The basic number classes all work as expected, the only difference between the classes is the way their math operators are handled. A `String` is mutable meaning it can expand without needing to copy over its elements while `Sequence` is immutable. One side effect of this is that `String` supports assignment math (`+=`, `-=`, etc.) while `Sequence` does not.

### Classes and Structs

Classes are collections of functions with access to particular data about the object the represent. They function exactly as one would expect from C++ or Java. functions declared inside a class are methods and have access to the `this` object. Variables declared in outside any function within the class are considered instance variables. A constructor is denoted by a function within the class that shares its name. Example:
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

Structs are very similar to their C/C++ partners, they contain only data without methods, all variables are public and a constructor is generated from the order the variables are declared in. Example:
```
struct SampleDataClass {
    var1: Int;
    var2: String;
}
```

To construct this class use: `new SampleDataClass(myVar1, myVar2)`

In practice, structs are simply classes that extend the special class `skiff.lang.Struct`. This class is special because it makes any class that extends it pass by value instead of reference. All the basic builtin types extend this class while most standard classes will not. The class `skiff.lang.Struct` itself also extends `skiff.lang.Any` as does every other class that can be constructed. The `Any` class forms the top of the skiff class hierarchy while the `None` class forms the bottom. In this way, a function can accept any type of object, and an instance of the `None` class can be returned in place of any other type. Skiff does not (as of yet) have the concept of a null pointer and instead thinks of these as instances of the `None` class.

Much like functions, classes can be created and past and follow the same closure logic as functions. For example, I could build a function that generates classes for storing the given number of bytes statically.
```
def createNumberClass(bitWidth: Int): Class {
    class newClass {
        coreData: Array<Byte>(bitWidth)
    }
    return newClass;
}
```

Although this class is trivial to implement in other ways, this example shows how the logic of closures can be applied to classes for great effect.

### Functions
Functions declared outside a class can be thought of like any other object except they can be called. In addition, a functions type must be clearly defined. For example, the following would be equivilent:
```
def myFunction(arg: Int): String {
    return arg.to_string();
}
```
and
```
myFunction: (Int) => String = (arg: Int): String => { return arg.to_string() };
```

Functions are object, but so are methods. When a method is converted to a function object, the `this` reference is baked in implicitly:

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

## Interpreter Implementation
The interpreter packages with Skiff is not well optimized and still lacks many features, however some things it should be able to do include collect garbage, allow statically linked in C libraries, and use simple JIT compilation to improve performance. Currently is cannot do any of these things.
### Garbage collection
The skiff interpreter should be able to track and collect garbage as it is allocated and used throughout the system. Ideally this would work similarly to the JVM's garbage collection mechanism in which there are several stages of memory so that the majority of short term memory does not slow down collection of the more difficult to collect long term memory.
### Linked C Libraries
The interpreter should be able to have other C libraries linked in at compile time to allow for new modules and classes to be added without touching the internals of the interpreter. My rough design for this uses an additional folder marked "modules" which could contain a series of `.module` files along with sets of C files attached to them. The module file would explain what Skiff class the methods should attach to and the chosen C names for each function. Each C function should take several arguments: the environment, the parameters, and if this is a method, the object the method is being invoked on. From these basic inputs, the function must return a single object which will be considered the return value of the function. This way when compiling Skiff, these modules can be parsed in and attached to the appropriate locations in their containing Skiff classes.
### JIT
Skiff should be able to find certain sections of code that are run often and compile them to native code so they can be run faster in future. The hope is to make basic functions run faster and require less memory. Complex functions most likely will not be able to be compiled JIT even if they are used often.

## C compilation
Compiling to C is a long term goal for Skiff to make it natively compiling on many more systems than it would normally be able to compile on. One of the biggest problems with this is the use of classes and the use of closures on functions and classes. Ideally these issues should be circumvented by using a global state map which allows versions of closures to be executed by referring to their global scope parameters. Currently transpilation is not being worked on, however I hope it will be added once the execution behavior of the language is better pinned down.
