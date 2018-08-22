Skiff
==
Skiff is a simple programming language designed to run in an interpreter. The project is mainly designed as an expironment into building a programming language and the many problems that form along the way.

## Core:
The language is designed with the hope that it will allow full compilation into C as well as native interpretation. The former means that any built in functions that preform special operations have to be written in C and imported if needed. The latter means that the translator must actually understand what the code is meant to do as well as maintain an environment for the code to be executed in.

## Syntax:
The language supports a wide range of syntactical styles:
- [x] Define a variable: `name: Type`
- [x] Assign a variable: `name = value`
- [x] Define and assign a varaible: `name: Type = value`
- [x] Call a function: `functionName(parameter 1, parameter 2)`
- [x] Define a function: `def functionName(parameter: Type, parameter: Type): ReturnType {}`
- [x] Define a class: `class ClassName {}`
- [x] Define a data class: `struct ClassName {}`
- [x] Create an instance: `new ClassName()`
- [x] Return a value from a function: `return value`
- [x] Create a string: `"String"`
- [x] Create a char sequence: `'Sequence'`
- [ ] Regex litterals: `r/regex/flags`
- [x] Boolean values:
    - True: `true`
    - Flase: `false`
- [x] Bitwise operations:
    - AND: `&`
    - OR: `|`
    - XOR: `^`
    - Shift left `<<`
    - Shift right `>>`
    - Not `~`
- [x] Boolean Operations:
    - AND `&&`
    - OR `||`
- [x] Comparison:
    - Equals `==`
    - Less Than `<`
    - Greater Than `>`
    - Less Than EQUAL to `<=`
    - Greater Than EQUAL to `>=`
    - Not `!`
- Math operations:
    - [x] Add: `+`
    - [x] Substract: `-`
    - [x] Multiply: `*`
    - [x] Exponent: `**`
    - [x] Divide: `/`
    - [x] Mod: `%`
    - [ ] Assignment with math: `+=`, `-=`, `*=`, `/=`, `%=`
    - [x] Pre/Post increment `++`
    - [x] Pre/Post decriment `--`
- Generic types:
    - Class:
        - [x] Declare: `class ClassName<T> {}` or `class ClassName<T, U> {}`
        - [x] Instanciate: `new ClassName<T>()`
        - [x] Required extension: `class ClassName<T : OtherClass> {}`
    - Struct
        - [x] Declare: `struct ClassName<T> {}` or `struct ClassName<T, U> {}`
        - [x] Instanciate: `new ClassName<T>()`
        - [x] Required extension: `struct ClassName<T : MyClass> {}`
    - Function:
        - [x] Declare: `def functionName<T>(parameter: Type): ReturnType {}`
        - [x] Call: `functionName<T>(param1, param2)`
        - [x] Required extension: `def functionName<T : OtherClass> {}`
- [x] Inheritance: `class Child : Parent`
- [x] List access: `list[index]`
- [x] Include other source `import <system file>` or `import "localfile"` 
- Function modifiers:
    - [ ] Static: `static def functionName() {}`
    - [ ] Private: `private def functionName() {}`
- Instance variable modifiers:
    - [ ] Static: `static var: Type`
    - [ ] Private: `private var: Type`
- [ ] Anonymous functions:
    - Creation: `(parameter: Type): ReturnType => {}`
    - As a type: `myFunction: (Type) => ReturnType`
- [ ] Anonymous Class: `new MyClass() {}`
- [ ] Quick initiallization: `new MyClass() {{}}`
- [x] If: `if(condition) {}`
- Else: 
    - [x] Catch all: `{} else {}`
    - [x] Conditional `{} else if() {}`
- Looping:
    - [x] While: `while(condition) {}`
    - [x] Basic for: `for(myVal: Int = value; myVal < myMax; myVal++) {}`
    - [x] Itterator: `for(myVal: Int : intList) {}`
- [x] Loop control:
    - Next itteration: `next;`
    - Break itteration: `break;`
- Switch statement: 
    - [x] heading: `switch(var) {}`
    - [x] case: `case val => {}`
    - [x] default: `case _ => {}`
- Match statement:
    - [x] heading: `match(var) {}`
    - [x] standard class case: `case val: MyClass => {}`
    - [x] structure case: `case val: MyStruct(val1, val2) => {}` or `case val: MyStruct(_, val2) => {}`
    - [x] default: `case _ => {}`
- Exceptions:
    - [x] throw: `throw new MyException()`
    - [x] try/catch: `try {} catch(e: MyException) {}`
    - [x] try/finally: `try {} finally {}`
    - [x] Define a function to throw an exception: `def myFunction(arg1: Type): ReturnType throws MyException {}`
- [x] Annotations:
    - On a function: 
    ```
    @MyAnnotation(param1, param2)
    def myFunction(arg: Type): ReturnType {}
    ```
    - On a class variable:
    ```
    @MyAnnotation(param1, param2)
    var: Type;
    ```
    - definition: `annotation MyAnnotation {}`
- Enum: 
    - [x] Basic: `enum MyEnum {}`
    - [x] Class: `enum class MyClass {}`
    - [x] Struct: `enum struct MyDataClass {}`

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

Structs are very similar to their C/C++ partners, they contain only data without methods, all valiables are public and a constructor is generated from the order the variables are declared in. Example:
```
struct SampleDataClass {
    var1: Int;
    var2: String;
}
```

To construct this class use: `new SampleDataClass(myVar1, myVar2)`

In practice, structs are simply classes that extend the special class `skiff.lang.Struct`. This class is special because it makes any class that extends it pass by value instead of refrence. All the basic builtin types extend this class while most standard classes will not. The class `skiff.lang.Struct` itself also extends `skiff.lang.Any` as does every other class that can be constructed. The `Any` class forms the top of the skiff class hierarchy while the `None` class forms the bottom. In this way, a function can accept any type of object, and an instance of the `None` class can be returned in place of any other type. Skiff does not (as of yet) have the concept of a null pointer and instead thinks of these as instances of the `None` class.

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

Functions are object, but so are methods. When a method is converted to a function object, the `this` refrence is baked in implicitly:

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
Switch functions attempt to match the value of the object given by `switch(object)` to one of the cases listed. This is done through the `==` method which can be overriden on a class by class basis. A wildcard entry, denoted `case _` will match any value.

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

Methods can override the operation that will be preformed when the access `[]` is called on the class as well as the access and assignement `[] =` is called on the class. Through this methodology, the way array access and modification is used can vary based on class.

## Interpreter Implementation
The interpreter packages with Skiff is not well optimized and still lacks many features, however some things it should be able to do include collect garbage, allow staticly linked in C libraries, and use simple JIT compilation to improve performance. Currently is cannot do any of these things.
### Garbage collection
The skiff interpreter should be able to track and collect garbage as it is allocated and used throughout the system. Ideally this would work similarly to the JVM's garbage collection mechanism in which there are several statges of memory so that the majority of short term memory does not slow down collection of the more difficult to collect long term memory.
### Linked C Libraries
The interpreter should be able to have other C libraries linked in at compile time to allow for new modules and classes to be added without touching the internals of the interpreter. My rough design for this uses an additional folder marked "modules" which could contain a series of `.module` files along with sets of C files attached to them. The module file would explain what Skiff class the methods should attach to and the chosen C names for each function. Each C function should take several arguments: the environment, the parameters, and if this is a method, the object the method is being invoked on. From these basic inputs, the function must return a single object which will be considered the return value of the function. This way when compiling Skiff, these modules can be parsed in and attached to the appropriate locations in their containing Skiff classes.
### JIT
Skiff should be able to find certain sections of code that are run often and compile them to native code so they can be run faster in future. The hope is to make basic functions run faster and require less memory. Complex functions most likely will not be able to be compiled JIT even if they are used often.

## C compilation
Compiling to C is a long term goal for Skiff to make it natively compiling on many more systems than it would normally be able to compile on. One of the biggest problems with this is the use of classes and the use of closures on functions and classes. Ideally these issues should be circumvented by using a global state map which allows versions of closures to be executed by refering to their global scope parameters. Currently transpilation is not being worked on, however I hope it will be added once the execution behavior of the language is better pinned down.