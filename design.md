SuperCC design doc
=
Possible better names:
- Skifft
- Scaft
## Core:
The language needs to allow full compilation into C as well as native interpretation. The former means that any built in functions that preform special operations have to be written in C and imported if needed. The latter means that the translator must actually understand what the code is meant to do as well as maintain an environment for the code to be executed in.

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
    - And: `&`
    - Or: `|`
    - Xor: `^`
    - Shift left `<<`
    - Shift right `>>`
    - Not `~`
- [x] Boolean Operations:
    - And `&&`
    - Or `||`
- [x] Comparison:
    - Equals `==`
    - Less Than `<`
    - Greater Than `>`
    - Less Than Equal to `<=`
    - Greater Than Equal to `>=`
    - Not `!`
- Math operations:
    - [x] Add: `+`
    - [x] Substract: `-`
    - [x] Multiply: `*`
    - [ ] Exponent: `**`
    - [x] Divide: `/`
    - [x] Mod: `%`
    - [x] Assignment with math: `+=`, `-=`, `*=`, `/=`, `%=`
    - [x] Pre/Post increment `++`
    - [x] Pre/Post decriment `--`
- Generic types:
    - Class:
        - [ ] Declare: `class ClassName<T> {}` or `class ClassName<T, U> {}`
        - [ ] Instanciate: `new ClassName<T>()`
        - [ ] Required extension: `class ClassName<T : OtherClass> {}`
    - Struct
        - [ ] Declare: `struct ClassName<T> {}` or `struct ClassName<T, U> {}`
        - [ ] Instanciate: `new ClassName<T>()`
        - [ ] Required extension: `struct ClassName<T : MyClass> {}`
    - Function:
        - [ ] Declare: `def functionName<T>(parameter: Type): ReturnType {}`
        - [ ] Call: `functionName<T>(param1, param2)`
        - [ ] Required extension: `def functionName<T : OtherClass> {}`
- [ ] Inheritance: `class Child : Parent`
- [ ] List access: `list[index]`
- [ ] Include other source `import <system file>` or `import "localfile"` 
- Function modifiers:
    - [x] Static: `static def functionName() {}`
    - [x] Private: `private def functionName() {}`
- Instance variable modifiers:
    - [x] Static: `static var: Type`
    - [x] Private: `private var: Type`
- [ ] Anonymous functions: 
    - Creation: `(parameter: Type): ReturnType => {}`
    - As a type: `myFunction: (Type) => ReturnType`
- [ ] Anonymous Class: `new MyClass() {}`
- [ ] Quick initiallization: `new MyClass() {{}}`
- [x] If: `if(condition) {}`
- Else: 
    - [ ] Catch all: `{} else {}`
    - [ ] Conditional `{} else if() {}`
- Looping:
    - [ ] While: `while(condition) {}`
    - [ ] Basic for: `for(myVal: Int = value; myVal < myMax; myVal++) {}`
    - [ ] Itterator: `for(myVal: Int : intList) {}`
- [ ] Loop control:
    - Next itteration: `next;`
    - Break itteration: `break;`
- Switch statement: 
    - [ ] heading: `switch(var) {}`
    - [ ] case: `case val => {}`
    - [ ]default: `case _ => {}`
- Match statement:
    - heading: `match(var) {}`
    - standard class case: `case val: MyClass => {}`
    - structure case: `case val: MyStruct(val1, val2) => {}` or `case val: MyStruct(_, val2) => {}`
    - default: `case _ => {}`
- Exceptions:
    - [x] throw: `throw new MyException()`
    - [ ] try/catch: `try {} catch(e: MyException) {}`
    - [ ] try/finally: `try {} finally {}`
    - [ ] Define a function to throw an exception: `def myFunction(arg1: Type): ReturnType throws MyException {}`
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
    - [ ] Class: `enum class MyClass {}`
    - [ ] Struct: `enum struct MyDataClass {}`

## Specifications:
### Baked in types
There are several baked in types. `Char` `Short` `Int` `Long` `Float` and `Double` are the basic number classes, `String` and `Sequence` are the basic string classes, `Boolean` is the basic boolean class and `Array` is the basic array class.

The basic number classes all work as expected, the only difference being the way their math operators are handled. A `String` is mutable meaning it can expand without needing to copy over its elements while `Sequence` is immutable. This means `String` supports assignment math (`+=`, `-=`, etc.)  while `Sequence` does not. 

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