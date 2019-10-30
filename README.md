Skiff
==
Skiff is a refrence based Java like programming language designed for compilation into C. The project is mainly designed as an experiment into the inner working of a programming lanugage and compiler. The language is designed with the hope that it will allow full compilation into C as well as interpretation on a simple, python like virtual machine.

There are some good examples of what Skiff is currently capable of in the [integration tests](https://github.com/DonoA/Skiff/tree/master/src/test/resources).

## Skiff to C transpiler:
Skiff is designed to be translated into C but support many features that C does not. Skiff supports classes, garbage collection, and closures all while transpiling into portable and (mostly) readable C code. This will also allow the language implicit compatibility with C functions.

## Statements And Implementation:
| Name | Example | Parse | Compile | Test |
| ---- | ------- | ----- | ------- | ---- |
| Declare Variable | `name: Type` | ✓ | ✓ | [Classes.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Classes/Classes.skiff) | 
| Assignment | `name = value` | ✓ | ✓ | [Math.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Math/Math.skiff) |
| Declare and assign | `name: Type = value` | ✓ | ✓ | [Math.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Math/Math.skiff) |
| Function Call | `functionName(p1, p2)` | ✓ | ✓ | [HelloWorld.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/HelloWorld/HelloWorld.skiff) |
| Function Def | `def functionName(p1: T1, p2: T2): Returns {...}` | ✓ | ✓ | [HelloWorld.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/HelloWorld/HelloWorld.skiff) |
| Class Def | `class ClassName {}` | ✓ | ✓ | [Classes.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Classes/Classes.skiff) |
| Data Class | `struct ClassName {}` | ✓ | ✓ | [Deconstruction.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Deconstruction/Deconstruction.skiff) |
| Create instance | `new ClassName()` | ✓ | ✓ | [Classes.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Classes/Classes.skiff) |
| Return from function | `return value` | ✓ | ✓ | [HelloWorld.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/HelloWorld/HelloWorld.skiff) |
| String Literal | `"String"` | ✓ | ✓ | [HelloWorld.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/HelloWorld/HelloWorld.skiff) |
| Char Sequence | `'Sequence'` | ✓ | ⅹ | ⅹ | 
| Regex Literal | `r/regex/flags` | ✓ | ⅹ | ⅹ |
| Bool Literal | `true false` | ✓ | ✓ | [Loops.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Loops/Loops.skiff) |
| Bool Combos | <code>&& &#124;&#124;</code> | ✓ | ✓ | [Loops.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Loops/Loops.skiff) |
| Compare | `== < <= > >= !=` | ✓ | ✓ | [Loops.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Loops/Loops.skiff) |
| Math | `+ - * / % **` | ✓ | ✓ | [Math.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Math/Math.skiff) |
| Math Assign| `+= -=` | ✓ | ✓ | [Math.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Math/Math.skiff) |
| Quick Inc/Dec | `++ --` | ✓ | ✓ | [Math.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Math/Math.skiff) |
| Generic Class Def | `class Cls<T, U> {}` | ✓ | ✓ | [Generics.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Generics/Generics.skiff) |
| Generic Class Use | `new Cls<T>()` | ✓ | ✓ | [Generics.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Generics/Generics.skiff) |
| Generic Extension | `class Cls<T : U> {}` | ✓ | ⅹ | ⅹ |
| Generic Func Def | `def func<T>(p: T): Returns {}` | ✓ | ✓ | [Optional.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Optional/Optional.skiff) |
| Inheritance | `class Child : Parent` | ✓ | ✓ | [Classes.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Classes/Classes.skiff) |
| List Index | `list[index]` | ✓ | ✓ | [Subscript.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Subscript/Subscript.skiff) |
| Import Source | `import <file>` | ✓ | ✓ | [Import.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Import/Import.skiff) |
| Dec Mods | `static private` | ✓ | ✓ | [Optional.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Optional/Optional.skiff) |
| Anon Func Def | `(p: T): Returns => {}` | ✓ | ⅹ | ⅹ |
| Anon Func Type | `funcVar: (T) => Returns` | ✓ | ⅹ | ⅹ |
| If | `if(cond) {}` | ✓ | ✓ | [FlowControl.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/FlowControl/FlowControl.skiff) |
| Else | `{} else if() {}` | ✓ | ✓ | [FlowControl.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/FlowControl/FlowControl.skiff) |
| While | `while(cond) {}` | ✓ | ✓ | [Loops.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Loops/Loops.skiff) |
| Loop | `loop {}` | ✓ | ✓ | [Loops.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Loops/Loops.skiff) |
| For | `for(i: Int = v; i < max; i++) {}` | ✓ | ✓ | [Loops.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Loops/Loops.skiff) |
| For Iter | `for(v: Int in intList) {}` | ⅹ | ⅹ | ⅹ |
| Loop flow | `next break` | ✓ | ✓ | [FlowControl.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/FlowControl/FlowControl.skiff) |
| Switch | `switch(v){case val => ...}` | ✓ | ✓ | [FlowControl.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/FlowControl/FlowControl.skiff) |
| Match | `match(v) {case v:T => ` | ✓ | ✓ | [Deconstruction.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Deconstruction/Deconstruction.skiff) |
| Deconstruction | `Struct(v1, v2) = myStruct` | ✓ | ✓ | [Deconstruction.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/Deconstruction/Deconstruction.skiff) |
| Try/Catch | `try {} catch(e: MyException) {}` | ✓ | ✓ | [TryCatch.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/TryCatch/TryCatch.skiff) |
| Throw | `throw new MyException()` | ✓ | ✓ | [TryCatch.skiff](https://github.com/DonoA/Skiff/tree/master/src/test/resources/TryCatch/TryCatch.skiff) |

## Language Structure:
### Classes and Structs
Classes are collections of functions with access to particular data about the object they represent. They function exactly as one would expect from C++ or Java. Functions declared inside a class are methods and have access to the `this` object. Variables declared outside any function within the class are considered instance variables. A constructor is denoted by a function within the class that shares its name and has no return type. Example:
```scala
class SampleClass {
    // Some internal class data, only accessable from methods
    private instanceVariable: Int;

    // A static counter of the number of instances of this class
    static sampleClassCount: Int = 0;

    // Constructor for the class
    def SampleClass(var: Int) {
        this.instanceVariable = var;
        SampleClass.sampleClassCounter++;
    }

    // A static builder method
    static def createDefault(): SampleClass {
        return new SampleClass(0);
    }

    // A getter method for private data
    def getInstanceVariable(): Int {
        return instanceVariable;
    }
}

// A class extending the function of SampleClass, this does not provide it access 
// to private fields in SampleClass, however it does contain that data
class SampleExtendingClass : SampleClass {
    // A class var for the extending class
    v2: Int;
    
    def SampleExtendingClass(var1: Int, var2: Int) {
        super(var1); // super must be first line of constructor
        this.v2 = var2;
    }
}

```

Structs form the basis for Skiff data classes and provide some handy features that classes do not. Structs can only contain field declarations but generate getters and setter. In addition, a toString and simple all args constructor is generated for the class. The main advantage of structs is that they can be deconstructed easily to allow the use of their internal data. Example:
```scala
// A simple data class with two instance variables
struct SampleDataClass {
    var1: Int;
    var2: String;
}

// To create a new instace, use the generated constructor
myData: SampleDataClass = new SampleDataClass(10, "StringData");

// Access data using a generated getter
println(myData.getVar2());

// Set data using a generated setter
myData.setVar1(5);

// Structs can be deconstructed into the data they contain for easy access.
SampleDataClass(myInt, myString) = myData;
```
### Functions
Functions declared outside a class can be thought of like any other object except they can be called. 
```scala
// the following would be equivilent:
def myFunction(arg: Int): String {
    return arg.toString();
}

myFunction: (Int) => String = (arg: Int): String => { return arg.toString() };
```

Functions are objects, but so are methods. When a method is converted to a function object, the `this` reference is baked in implicitly:

```scala
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

### Switch and Match:
Switch functions attempt to match the value of the object given by `switch(object)` to one of the cases listed. This is done through the `==` method which can be overridden on a class by class basis. A wildcard entry, denoted `case _ =>` will match any value.

Match functions work in much the same way except the class of the object is searched on as opposed to the value. Data classes can have their data extracted implicitly using the `case val: MyStruct(val) => {}` syntax. When using a match, the object being searched on will be cast to the matching class when that cases is called. Suppose we have the following:
```scala
class MyClass {
    val: Int = 5;
}

struct MyStruct {
    val1: String = "Hello";
    val2: String = "World";
}

def getType(val: Any): String {
    match(val){
        // When val is an instance of MyClass, this block is activated and v is defined 
        // as val cast to MyClass
        case v: MyClass => {
            return "MyClass, val = " + v.val.to_string();
        }
        // When val is an instance of MyStruct, this block will deconstruct the fields in 
        // val and allow access to the object. Here, v is defined as val cast to MyStruct 
        // and v1 is the value of MyStruct's first field, or val1.
        case v: MyStruct(v1, _) => {
            return "MyStruct, val1 = " + v1 + ", val2 = " + v.val2;
        }
        // The default case is handled as well
        case _ => {
            return "Unknown";
        }
    }
}
```
### Operator annotation
Much like Scala, these operators are simply methods on the object to the left of them, however unlike Scala, the number of different operator symbols that can be used it limited to widely known and understood ones to prevent more confusing operators from being used.

Any method that returns can be bound to the basic operators: `+, -, *, /, %, +=, -=, *=, /=, %=, ==, >, <, []`. To bind a method to a basic operator, the Operator annotation is used:
```scala
class MyClass {
    @Operator(Operators.PLUS)
    def plus(other: MyClass): MyClass {
        ...
    }
    ...
}
```
### Truthiness
The only values which evaluate to false are `None`, `false`, and `0`. Any other value will evaluate to `true`. In addition, the evaluation of booleans is a fail fast operation. Thus `if(!value.isNone() && value.callMethod())` is valid regardless of if value is `None`.

Methods can override the operation that will be performed when the access `[]` is called on the class as well as the access and assignment `[] =` is called on the class. Through this methodology, the way array access and modification is used can vary based on class.

## Implementation
### Storage Spaces
Skiff stores data in several different locations. In order to remain portable, Skiff does not attempt to tamper with the internal C stack and instead creates its own. This stack is globally declared as `skiff_any_ref_t * skiff_ref_stack` and contains pointers to all objects currently reachable from executing code. When an object is declared, a location on the stack is reserved for it. When the object goes out of scope, the location is freed. Beyond the stack, Skiff maintains a heap of all objects currently allocated. This heap is periodically cleaned by the garbage collector and contains several regions: `eden_space`, `survivor_space`, and `old_gen_space`. When an object is allocated to the heap using the `new` keyword, it is placed in eden space. When the garbage collector runs, it will promote any surviving objects to survivor space. Old gen is used for objects that survive many gc passes in survivor space.

### Types of data
Skiff has two main types of data, objects and primitives. Objects are instances of classes. They are refrences which are automatically deleted when they are not longer reachable. They live in one of the levels of the Skiff heap. Primitives are basic, small data types that are stored in the native C stack. They do not require tracking, and they are not refrences dispite the ability to call some methods on them.

### Classes and Structs
#### Backing struct layout:
When Skiff classes are compiled into C, they are translated into structs containing the data stored in the class and some important metadata. To be exact, the first two fields of the backing C struct are always `struct skiff_<classname>_class_struct * class_ptr` and `uint8_t mark`. This metadata is used to allow polymorphism and garbage collecting. The `mark` field is used for the mark and sweap garbage collector that Skiff runs to track which objects have been marked. The `class_ptr` field points to a static instance of a C struct containing information about the objects class. Beyond the first two fields, the entries in the struct are setup so that refrence fields are declared before primitive fields. This is used by the garbage collector to follow refrences and mark child objects. The only violation of this field layout is for inheritance. Backing structs for classes that extend existing Skiff classes first declare all fields from their parent class before declaring their own fields. This allows fields in the parent's backing C struct to be accessed through instances of the children. Consider the following:

Skiff code:
```scala
class Thing {
    id: Int;
    name: String;
}
class Person : Thing {
    age: Int;
    lastName: String;
}
```
Backing Struct:
```c
struct skiff_thing_struct 
{
    // Metadata
    struct skiff_thing_class_struct * class_ptr;
    uint8_t mark;
    // Fields (notice that name is declared before id)
    skiff_string_t * name;
    int32_t id;
};

struct skiff_person_struct 
{
    struct skiff_person_class_struct * class_ptr;
    uint8_t mark;
    // Parent Fields
    skiff_string_t * name;
    int32_t id;
    // Local fields (refrence classes are again declared before primitives)
    skiff_string_t * lastName;
    int32_t age;
};
```
#### Interface structs
Each class has a special C struct that contains important static information. This struct is a singleton which is globally visible under the name `skiff_<class name>_interface`. This interface struct is attached to each instance of the class allowing for quick access to important class metadata. The interface object contains 4 important metadata fields: `int32_t class_refs`, `int32_t struct_size`, `void * parent`, `char * simple_name`. `class_refs` and `struct_size` are used mainly my the garbage collector. `class_refs` stores the number of refrence fields within the backing C struct for the class so they can be easily scanned. `struct_size` simply contains the value of `sizeof(skiff_<class>_t)`. The parent field points to the interface singleton of the parent class to this class (or null if there is none), and `simple_name` is a string with the name of the class as it was defined in the Skiff code. Beyond the 4 basic metadata fields, the interface struct contains refrences to all the methods contained by the class. This allows overridden methods to be called correctly even when the exact type of the object is not known at compile time.

To ensure the interface struct has the correct information, every constructor calls a function to setup the interface struct for its class. This setup function can also be used to initialize other static fields, values, and invoke static setup functions. (Currently the main function also calls the setup function for all known types, however it is not clear if that is needed).

#### Constructors
Constructor methods are used to initialize an area for use as an object with a given type. Constructor functions work like normal methods except they can accept `null` in place of the `this` object. When `null` is passed in the place of `this`, a new area is allocated for use with that object. This means special classes that may need to allocate additional space for their backing struct can do so in a unified way. Constructors also return a pointer to the newly created object making it easy to use the result of a constructor for a variable or as a parameter. Constructors include a number after their name to indicate which constructor they represent. This allows each class to have several constructors with differing signatures.

```c
// A simple constructor
skiff_person_t * skiff_person_new_0(skiff_person_t * this)
{
    skiff_person_static();
    if(this == 0) 
    { 
        this = skalloc(1, sizeof(skiff_person_t));
        this->class_ptr = &skiff_person_interface;
    }
    skfree_ref_stack(0);
    return this;
}
```

#### Calling methods
Calling methods on Skiff objects is slightly more complex than simply invoking a function. To allow for effective method overriding, Skiff methods are invoked from pointers in the objects class interface. For example, to call the method `getName` on this simple class, the C code needs to determine which version of `getName` should be called and where it was defined:
```scala
class Thing {
    name: String;

    def Thing(name: String) {
        this.name = name;
    }

    def getName(): String {
        return name;
    }
}

thing: Thing = new Thing("Dave");
thing.getName();
```

```c
// First we define thing on the stack for gc
skiff_thing_t ** thing = skalloc_ref_stack();
// Then we assign a new value to it
*(thing) = (skiff_thing_t *)(skiff_thing_new_0(0, skiff_string_new_0(0, "Dave"));
// Finally we call the method
(*thing)->class_ptr->getName(*thing);
```
### Try/Catch
Try/catch in Skiff function much like Java or Javascript. When an error is thrown, the execution is carried up the call stack to the last catch clause that matches the thrown exception. When this clause is located, the catch function for the matching clause is executed, and the application resumes after the try/catch block. For tracking catch clauses, the global objects `catch_layer_head` and `catch_layer_tail` are used. These variables make up the head and tail of a linked list of catch layers that can be searched to locate the current catch context. When a try/catch block starts, the catch method and current C state are captured using the `skiff_start_try` function. When the block ends, `skiff_end_try()` can be used to notify the catch layers list that the bottom most layer has completed use. 

For user thrown errors, the `void skiff_throw(skiff_exception_t * ex)` is invoked. This will search up the `catch_layer` chain to find a layer that matches the thrown type. It will then call the catch layer's `void (*current_catch)(skiff_catch_layer_t *, skiff_exception_t *)` so the error can be handled. For native errors such as segmentation faults or math errors, the C error is wrapped in a Skiff error and the `skiff_throw` method is used to handle the exception. 

### Garbage collection
Skiff implements a generational garbage collector. When the eden heap is filled up, the garbage collector is automatically triggered to locate additional space for new variables. The skiff gc is a simple mark and sweep garbage collector. It currently runs the same execution thread as the rest of the skiff code, meaning that it stops to world for marking, sweeping, and memory compaction.

### Linked C Libraries
Skiff easily allows other C libraries to be linked in at compile time to allow for new modules and classes to be added without touching the internals of the compiler. Skiff classes and functions that are prefixed with the keywork `native` are defined to the compiler but their implementation does not need to be specified. The compiler will add them to the given scope with the expected c versions of the defined skiff objects. To include the implementation for these native functions, the `native` keyword can be used before an import statement to `#include` the file during C compilation.