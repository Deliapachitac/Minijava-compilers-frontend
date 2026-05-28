# Compilers-Project2
Name: DELIA-MARIA          
Surname: PACHITAC           
AM: 1115202200125

## How to run the program 
- cd myproject2
- make compile (to compile the program)
- make run_tests (runs a all the tests from the tests folder )  
- make run FILE= "namefile"  (if you want to run a specific test)
- make clean 

## Implementation 

### Symbol Table 
A symbol table is a data structure used by a compiler to store information found in the source program.During the first pass over the syntax tree, the compiler completes the symbol table with declarations.I seperated the symbol table implementation in 4 files:

- **FieldInfo.java** 
This class stores all the information about a single field (variable,arguments,..) of a class.For every field we will the name, type  and its memory offset. The offset is calculated at the time the field is inserted in the symbol table and it follows this rules:
  - `int`=> 4 bytes
  - `boolean` => 1 byte
  - every pointer (class reference or`int[]`) => 8 bytes

- **MethodInfo.java** 
Here we store  all the information about a method. For every method we are going to keep the method's name,the return type, the parameter list (each parameter is has a  `[type, name]`) and the method's vtable offset. The vtable offset indicates the position of the method's function pointer in the virtual method table of its class. New methods receive a new slot (offset incremented by 8 bytes per method),but  overriding methods reuse the parent's slot. 

- **ClassInfo.java**
Stores all the information about a single class. It holds the class name and the parent ClassInfo (or `null` if there is no parent class).Also we created  a`LinkedHashMap` that keeps each field  of the class   and a `LinkedHashMap` that maps each method name to a `LinkedList<MethodInfo>` because overloaded methods share the same name. Lastly we have the field offset and method offset so that each new field or method is assigned the next available slot. 

- **AllClasses.java** 
This class is the most important one because it acts as the top-level symbol table that contains information about all the classes in the program. It stores a `LinkedHashMap classes` of all registered `ClassInfo` objects. We also created a variable `currentClass` (`ClassInfo`) that keeps track of the active class the visitor is currently walking through . Lastly the `LinkedHashMap localvar` is mapping the local variables andd method parametres to their types . Local variables are temporary and they exist only inside their method block. Every time the visitor enters a new method it calls clearLocals() on this map and  then it  fills up the  map with the new  local variables and parametres.These are some of the key functions that this implementation provides:
    - `isSubClass` : Checks if one class is a child of another by searching all the tree
    - `getTypeVariable`: Looks for the variable type (first inside the  local method then in the class fields and lastly in the parent classes if they exist)
    - `findMethod`: Finds a method in the class or the parent class
    - `getMethodOverride` :Checks if a method in a child class has the exact same name and parameters as a method in a parent class, confirming it is an override
    -`printOffset` :It loops through all variables and methods and prints their byte size offsets that we saved in the class

### Visitors 
In our project the JTB tool reads the given MiniJava grammar and automatically generates a bunch of simple Java classes. Each class represents a piece of the code structure (ex: MethodDeclaration, VarDeclaration,...). Instead of putting complicated compiler logic inside these  fileswe keep them clean and write our compiler logic in separate visitor class 

- **SymbolVisitorTable.java** 
This visitor is responsible for doing the first scan over the source code to build our  Symbol Table. Its main job is to collect the names and types of every class, field, method, and parameter it encounters.I wrote comments in detail explaining what each function does . This visitor also handles 2 important concepts: 
    - `Method overloading `: This happens when multiple methods within the same class (or across an inheritance chain) share the exact same name but have a different number of input parameters. It allows a class to perform similar actions with different types or amounts of incoming data.For example a class could define a method fun(int x, int y) to perform an operation of two integers and also define fun(int x, int y, int z) to  perform an operation for three integers. The compiler distinguishes between these methods at compile time by looking closely at the arguments passed during the method call.

    - ` Method overriding` : This occurs when a child class gives a new body to a method it inherited from its parent class. For this to work  the method in the child class has to be an exact copy of the parent's method (name ,return type and arguments). For example if we have a  class called Machine with a method sound() a subclass like Truck can override the general sound.We implemented this by letting the overridden method reuse the exact same slot in the Virtual Method Table that was already allocated by the parent class.

- **TypeChecking.java** 
This visitor is responsible for executing the second pass of the semantic analyzer. Once the SymbolTableVisitor has built theSymbol table then the TypeChecking pass walks through every  expression  to ensure that it  obeys  type safety and structural constraints.This are some of the conditions that we check 
    - Validate types in arithmetic operations (+, -, * ) => Enforces that both operands are ` int`
    - Validate type in comparisons operations(<) => Enforces that both operands are ` int` 
    - Validate type in logical operations(&&,!)=>Enforces that both operands are ` boolean` 
    - Validate loops and braches(if,while) => Enforces that both operands are ` boolean` 
    - Validate assignments (x=y) => Ensures that the type of the expression matches with the target declared type
    - Validate method return values (return expr) => Checks that the type of the returned expression is the same with the method's return type
    - Validate array operations(new int[size], arr[idx], arr.length) => Ensures that the array sizes and indexes are integers
    - Validate class allocation (new ClassName())  => Ensures that the target class has been successfully declared and exists within the symbol table
    - Validate Method calls  (obj.method(args))=> Confirms the method exists in its inheritance hierarchy and checks that the passed argument types are compatible with the expected parameter types
    

### Offset Rules
- Field Offset Rules: 
Fields are allocated in the order they appear based  on  their data types. If a class extends a parent class its field offsets start exactly where the parent class fields left off.
    * `boolean`: Allocates 1 byte 
    * `int`: Allocates 4 bytes
    * Pointers (`int[]`, `boolean[]`, or Class References): Allocate 8 bytes

- Virtual Method Table (VTable) Rules
Methods do not occupy data space inside a class  instead they are tracked via a shared Virtual Method Table pointer structure 
    * New Methods: Receive a new 8 byte slot allocated at the end of the current class's VTable 
    * Inherited Methods: Derived classes copy  the VTable set by their parents and continue to add 8 byte slots at the end 
    * Overridden Methods: When a subclass redefines an inherited parent method signature it reuses the exact same slot index previously allocated by the parent.

### Project architecture 
For this project I followed the structure of the example project given in the lectures:
https://cgi.di.uoa.gr/~compilers/24_25/tutorials-material/jtb-javacc-2025.zip

1. **Lexical and Syntactic Analysis (JavaCC)**: Converts the raw source text stream into tokens and verifies grammatical structural 
2. **Abstract Syntax Tree Generation (JTB)**: Transforms the token stream into an object-oriented syntax tree structure 
3. **Pass 1: Declaration Scanning (`SymbolTableVisitor`)**: Walks down the AST to save information inside the  symbol table database (`AllClasses`).
4. **Pass 2: Semantic Verification (`TypeChecking`)**: Re-traverses the AST nodes to enforce type-safety rules, validate conditions in loops, check variable assignments, and ensure that method calls match their expected arguments
5. **Output Generation**: Prints the  byte offsets of every field and method virtual table slot 