# Java Methods (Functions): The Complete Deep Dive

> You wrote "junctions", and I'm taking that to mean **functions**. In Java, functions are called **methods**.
> This guide covers everything about them: how to write them, **passing variables**, **overloading**, **chaining**, recursion, lambdas, and much more, with a lot of practical code.
> Every example is meant to be copied, run and changed.

---

## Table of Contents

1. [What Is a Method (Function)?](#1-what-is-a-method-function)
2. [Anatomy of a Method](#2-anatomy-of-a-method)
3. [Declaring and Calling Methods](#3-declaring-and-calling-methods)
4. [Return Types and the `return` Statement](#4-return-types-and-the-return-statement)
5. [Parameters and Passing Variables (Pass-by-Value)](#5-parameters-and-passing-variables-pass-by-value)
6. [Varargs: A Variable Number of Arguments](#6-varargs-a-variable-number-of-arguments)
7. [Method Overloading](#7-method-overloading)
8. [Method Chaining](#8-method-chaining)
9. [Static vs Instance Methods](#9-static-vs-instance-methods)
10. [Access Modifiers and Other Method Modifiers](#10-access-modifiers-and-other-method-modifiers)
11. [Scope, Lifetime and the Call Stack](#11-scope-lifetime-and-the-call-stack)
12. [Recursion](#12-recursion)
13. [Method Overriding (Overloading's Cousin)](#13-method-overriding-overloadings-cousin)
14. [Constructors and Constructor Chaining](#14-constructors-and-constructor-chaining)
15. [Generic Methods](#15-generic-methods)
16. [Methods and Exceptions](#16-methods-and-exceptions)
17. [Methods as Values: Lambdas and Method References](#17-methods-as-values-lambdas-and-method-references)
18. [The `main` Method Explained](#18-the-main-method-explained)
19. [Best Practices for Writing Methods](#19-best-practices-for-writing-methods)
20. [Practical Programs](#20-practical-programs)
21. [Common Mistakes](#21-common-mistakes)
22. [Cheat Sheet, Summary and Quiz](#22-cheat-sheet-summary-and-quiz)

---

## 1. What Is a Method (Function)?

A **method** is a **named, reusable block of code** that does one job. You write it once, and then **call** it as many times as you want.

Think of a method as a **machine**:

```
           ┌───────────────────┐
 inputs ──►│      METHOD       │──► output
(arguments)│  (does some work) │   (return value)
           └───────────────────┘
```

Real-life example: a **coffee machine**. You give it inputs (water, beans), it does the work, and it gives you an output (coffee). You don't rebuild the machine each time.

### Why use methods?

| Benefit | Meaning |
|---------|---------|
| **Reuse (DRY)** | Write once, use many times. DRY = "Don't Repeat Yourself" |
| **Organization** | Break a big problem into small, named pieces |
| **Readability** | `calculateTax(price)` is clearer than 10 lines of math |
| **Easy fixing** | A bug is fixed in one place, not ten |
| **Easy testing** | Small methods are easy to test separately |
| **Abstraction** | Use a method without knowing *how* it works inside |

### "Method" vs "function": the terminology

| Word | Meaning in Java |
|------|-----------------|
| **Method** | The official Java word. Every function in Java lives **inside a class**, so it is called a method. |
| **Function** | A general programming word. Java has **no standalone functions**, but people say "function" casually. |

### Vocabulary you must know

| Term | Meaning | Example |
|------|---------|---------|
| **Method declaration (definition)** | Writing the method | `static int add(int a, int b) { ... }` |
| **Method call (invocation)** | Using the method | `add(3, 4)` |
| **Parameter (formal parameter)** | A variable in the method's declaration | `int a, int b` |
| **Argument (actual parameter)** | The real value you pass in the call | `3, 4` |
| **Return value** | The result sent back | `7` |
| **Caller** | The code that calls the method | `main` |
| **Callee** | The method being called | `add` |

### Your first method

```java
public class Hello {

    // declaration: a method that prints a greeting
    static void greet(String name) {
        System.out.println("Hello, " + name + "!");
    }

    public static void main(String[] args) {
        greet("Ali");       // call 1
        greet("Sara");      // call 2
    }
}
```

Output:
```
Hello, Ali!
Hello, Sara!
```

---

## 2. Anatomy of a Method

```
  modifiers   return type   name     parameter list      throws clause      body
 ┌─────────┐  ┌─────────┐ ┌─────┐ ┌───────────────┐ ┌────────────────┐ ┌─────────┐
  public static    int      add    ( int a, int b )   throws SomeError  { return a + b; }
 └─────────────────────────────────────────────────────────────────────┘
                           method header (declaration line)
```

| Part | Required? | What it does |
|------|-----------|--------------|
| **Modifiers** (`public`, `static`, `final`...) | Optional | Control access and behavior |
| **Return type** | **Required** | The type of value sent back, or `void` if nothing |
| **Method name** | **Required** | How you call it (`camelCase`, usually a verb) |
| **Parameter list** `( )` | **Required** (can be empty) | The inputs, each with a type and a name |
| **`throws` clause** | Optional | Lists checked exceptions the method may throw |
| **Body** `{ }` | **Required** (except abstract methods) | The code that runs |

### The method signature

The **signature** of a method is its **name + the types of its parameters (in order)**.

```java
static int add(int a, int b)       // signature: add(int, int)
static double add(double a, double b)  // signature: add(double, double)
```

Important: the **return type is NOT part of the signature**. This matters for overloading (section 7). Two methods in the same class cannot have the same signature.

### Naming conventions

- Use **camelCase**: `calculateTotal`, `isEmpty`, `getName`.
- Start with a **verb**: `print`, `compute`, `find`, `save`, `validate`.
- Methods returning `boolean` usually start with `is`, `has`, `can`: `isValid()`, `hasNext()`.
- Getters/setters: `getAge()`, `setAge(int age)`.

---

## 3. Declaring and Calling Methods

### 3.1 Rules for declaring

1. A method must be written **inside a class** (never outside).
2. You **cannot nest** a method inside another method. (Lambdas and local classes exist, but not nested methods.)
3. The **order** of methods in a class doesn't matter. You can call a method that is written lower in the file.
4. The name follows identifier rules (letters, digits, `_`, `$`; can't start with a digit; can't be a keyword).

```java
public class Demo {
    public static void main(String[] args) {
        sayHi();            // OK even though sayHi is written below
    }

    static void sayHi() {
        System.out.println("Hi");
    }
}
```

### 3.2 Four common shapes of methods

```java
// 1) No parameters, no return value
static void sayHello() {
    System.out.println("Hello");
}

// 2) Parameters, no return value
static void greet(String name) {
    System.out.println("Hello " + name);
}

// 3) No parameters, with return value
static int getFive() {
    return 5;
}

// 4) Parameters and return value
static int add(int a, int b) {
    return a + b;
}
```

### 3.3 Calling methods

```java
sayHello();                  // call as a statement
greet("Ali");
int x = getFive();           // use the returned value
int sum = add(3, 4);         // sum = 7
System.out.println(add(10, 20));          // call inside another call: prints 30
int total = add(add(1, 2), add(3, 4));    // nested calls: add(3, 7) = 10
```

- A call to a method that **returns a value** is an **expression**. You can store it, print it, or pass it on.
- You are allowed to **ignore** the returned value: `add(3, 4);` compiles (but is pointless).
- Calling a **`static`** method: just `name(...)` inside the same class, or `ClassName.name(...)` from another class.
- Calling an **instance** method: you need an object first: `object.name(...)` (see section 9).

### 3.4 What really happens when you call a method

```
 1) Java evaluates the arguments, left to right
 2) A new "stack frame" is created for the method (in the Call Stack)
 3) The argument values are COPIED into the parameters
 4) The method body runs
 5) On `return` (or reaching the end), the frame is removed (popped)
 6) Execution continues in the caller, with the return value in place of the call
```

Example: `int result = add(3, 4);`

```
 BEFORE the call                   DURING the call                   AFTER the call

 ┌────────────────┐                ┌────────────────┐                ┌────────────────┐
 │ main           │                │ add   a=3 b=4  │ ◄── running    │ main           │
 │  (waiting)     │                ├────────────────┤                │  result = 7    │
 └────────────────┘                │ main           │                └────────────────┘
                                   │  (waiting)     │
                                   └────────────────┘
```

---

## 4. Return Types and the `return` Statement

### 4.1 `void`: returns nothing

```java
static void printLine() {
    System.out.println("-----");
}
```

A `void` method can use a bare `return;` to **exit early**:

```java
static void printPositive(int n) {
    if (n <= 0) {
        return;                    // stop here, nothing printed
    }
    System.out.println(n);
}
```

You can't use a `void` call as a value: `int x = printLine();` is an error.

### 4.2 Returning a value

The `return` statement **ends the method** and sends a value back. The value must be **compatible with the return type**.

```java
static int square(int n) {
    return n * n;
}

static boolean isEven(int n) {
    return n % 2 == 0;
}

static String fullName(String first, String last) {
    return first + " " + last;
}

static double average(int a, int b) {
    return (a + b) / 2.0;
}
```

### 4.3 Rules for `return`

**Rule 1: Every possible path must return (if the type isn't `void`).**

```java
static int sign(int n) {
    if (n > 0) {
        return 1;
    } else if (n < 0) {
        return -1;
    }
    // ERROR: missing return statement (what if n == 0?)
}
```

Fix:

```java
static int sign(int n) {
    if (n > 0) return 1;
    if (n < 0) return -1;
    return 0;
}
```

**Rule 2: Code after `return` is unreachable (compile error).**

```java
static int f() {
    return 5;
    System.out.println("never runs");   // ERROR: unreachable statement
}
```

**Rule 3: The value can be widened automatically, but not narrowed.**

```java
static double f() { return 5; }        // OK: int → double
static int g()    { return 5.5; }      // ERROR: possible lossy conversion
static int h()    { return (int) 5.5; }// OK with a cast
```

**Rule 4: A method can have many `return` statements.**

```java
static String grade(int marks) {
    if (marks >= 90) return "A";
    if (marks >= 80) return "B";
    if (marks >= 70) return "C";
    return "F";
}
```

This "early return" style (called **guard clauses**) avoids deep nesting.

### 4.4 Returning different kinds of things

```java
static int[] makeSquares(int n) {              // returns an array
    int[] r = new int[n];
    for (int i = 0; i < n; i++) r[i] = i * i;
    return r;
}

static String describe(int n) {                // returns an object (String)
    return n > 0 ? "positive" : "non-positive";
}

static Person findPerson(String name) {        // may return null
    return null;                               // allowed for object return types
}
```

> **Tip:** returning `null` for "nothing found" can cause `NullPointerException`. For collections, return an **empty** list instead of `null`. For single values, consider `Optional<T>`.

### 4.5 Returning more than one value

A method can return **only one thing**, but that one thing can hold several values.

**Option 1: An array**

```java
static int[] minAndMax(int[] a) {
    int min = a[0], max = a[0];
    for (int v : a) {
        if (v < min) min = v;
        if (v > max) max = v;
    }
    return new int[]{min, max};
}

int[] r = minAndMax(new int[]{5, 2, 9});
System.out.println(r[0] + " " + r[1]);     // 2 9
```

**Option 2: A `record` (Java 16+), the cleanest**

```java
record MinMax(int min, int max) {}

static MinMax minAndMax(int[] a) {
    int min = a[0], max = a[0];
    for (int v : a) {
        if (v < min) min = v;
        if (v > max) max = v;
    }
    return new MinMax(min, max);
}

MinMax r = minAndMax(new int[]{5, 2, 9});
System.out.println(r.min() + " " + r.max());   // 2 9
System.out.println(r);                         // MinMax[min=2, max=9]
```

**Option 3: A small class of your own**, or a `List`/`Map`.

### 4.6 Methods that return `boolean` (checkers)

```java
static boolean isAdult(int age) {
    return age >= 18;                 // no need for if/else that returns true/false
}

if (isAdult(20)) { ... }
```

---

## 5. Parameters and Passing Variables (Pass-by-Value)

This is the most important and most misunderstood topic about methods.

### 5.1 Parameters vs arguments

```java
static int add(int a, int b) {     // a and b are PARAMETERS
    return a + b;
}

int x = 5, y = 7;
add(x, y);                         // x and y are ARGUMENTS
add(3, 4);                         // 3 and 4 are ARGUMENTS
```

Parameters are **local variables** of the method. They are created when the method is called and disappear when it ends.

### 5.2 Matching arguments to parameters

The arguments must match the parameters in:

1. **Number** (how many)
2. **Order** (which comes first)
3. **Type** (compatible; widening is automatic, narrowing needs a cast)

```java
static void show(int a, double b, String c) { }

show(1, 2.5, "x");        // OK
show(1, 2, "x");          // OK: int 2 is widened to double
show(2.5, 1, "x");        // ERROR: double can't go into int
show("x", 1, 2.5);        // ERROR: wrong order/types
show(1, 2.5);             // ERROR: missing an argument
```

### 5.3 The golden rule: Java is ALWAYS pass-by-value

> **Java always passes a COPY of the argument's value to the method. Always. No exceptions.**

- For a **primitive**, the copy is the **number itself**.
- For an **object or array**, the copy is a copy of the **reference** (the address). Both the original and the copy point to the **same object**.

This one sentence explains every behavior below.

### 5.4 Passing primitives: the method can't change your variable

```java
static void change(int x) {
    x = 100;                       // changes only the local copy
    System.out.println("inside: " + x);
}

public static void main(String[] args) {
    int a = 5;
    change(a);
    System.out.println("outside: " + a);
}
```

Output:
```
inside: 100
outside: 5
```

```
 main                      change
 ┌───────────┐             ┌───────────┐
 │ a = 5     │ ──copy────► │ x = 5     │
 └───────────┘             │ x = 100   │   ← only this copy is changed
                           └───────────┘
 a is still 5
```

### 5.5 Passing objects: the reference is copied

```java
class Person {
    String name;
    Person(String name) { this.name = name; }
}
```

**Case A: Changing the object's contents is visible to the caller**

```java
static void rename(Person p) {
    p.name = "Changed";            // follows the reference to the SAME object
}

Person a = new Person("Ali");
rename(a);
System.out.println(a.name);        // Changed
```

**Case B: Reassigning the parameter is NOT visible to the caller**

```java
static void replace(Person p) {
    p = new Person("New");         // p now points to a different object; only the copy moved
}

Person a = new Person("Ali");
replace(a);
System.out.println(a.name);        // Ali   (unchanged)
```

```
 CASE A: p.name = "Changed"            CASE B: p = new Person("New")

 caller: a ──┐                          caller: a ──────────► [ Person "Ali" ]
             ├──► [ Person "Changed" ]
 method: p ──┘                          method: p ──────────► [ Person "New" ]
 (same object, so the change is seen)   (p was re-pointed; the caller's a is untouched)
```

> **Memory aid:** you give the method a **copy of your house key**. The method can go into your house and rearrange the furniture (change the object). But if the method throws its copy away and picks up a different key (reassigns the parameter), your own key is unaffected.

### 5.6 Passing arrays (same idea)

```java
static void doubleAll(int[] arr) {
    for (int i = 0; i < arr.length; i++) arr[i] *= 2;   // changes the caller's array
}

static void replaceArray(int[] arr) {
    arr = new int[]{9, 9, 9};                            // only the local reference changes
}

int[] nums = {1, 2, 3};
doubleAll(nums);
System.out.println(Arrays.toString(nums));    // [2, 4, 6]
replaceArray(nums);
System.out.println(Arrays.toString(nums));    // [2, 4, 6]   (unchanged by the reassignment)
```

### 5.7 Passing Strings and wrappers: they can't be changed

`String`, `Integer`, `Double` and other wrappers are **immutable**: their content can never change. So a method can't modify them; it can only create new ones.

```java
static void addWorld(String s) {
    s = s + " World";                  // creates a NEW String; only the local reference moves
}

String msg = "Hello";
addWorld(msg);
System.out.println(msg);               // Hello   (unchanged)
```

A mutable object like `StringBuilder` **can** be changed through the method:

```java
static void addWorld(StringBuilder sb) {
    sb.append(" World");               // changes the shared object
}

StringBuilder sb = new StringBuilder("Hello");
addWorld(sb);
System.out.println(sb);                // Hello World
```

If you want a changed String back, **return it**:

```java
static String addWorld(String s) {
    return s + " World";
}
msg = addWorld(msg);                   // msg = "Hello World"
```

### 5.8 The famous `swap` puzzle

```java
static void swap(int x, int y) {
    int temp = x;
    x = y;
    y = temp;
}

int a = 1, b = 2;
swap(a, b);
System.out.println(a + " " + b);       // 1 2   (NOT swapped!)
```

It fails because `x` and `y` are copies. Ways that work:

```java
// 1) Swap elements inside an array (arrays are shared)
static void swap(int[] arr, int i, int j) {
    int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
}

// 2) Return the new values
static int[] swap(int x, int y) {
    return new int[]{y, x};
}

// 3) Just swap inline, where the variables live
int temp = a; a = b; b = temp;
```

### 5.9 `final` parameters

```java
static void f(final int x) {
    x = 5;                    // ERROR: cannot assign a value to final parameter x
}
```

`final` stops the method from reassigning the parameter. (For an object, `final` still allows changing the object's contents.)

### 5.10 Java has no default parameter values and no named parameters

Some languages allow `connect(host, port = 80)`. Java doesn't. Use **overloading** instead (very common pattern):

```java
static void connect(String host) {
    connect(host, 80);                          // delegates to the full version
}

static void connect(String host, int port) {
    System.out.println("Connecting to " + host + ":" + port);
}

connect("example.com");          // Connecting to example.com:80
connect("example.com", 8080);    // Connecting to example.com:8080
```

When a method has **many parameters**, use a **builder** (section 8.5) or pass a **parameter object**.

### 5.11 Evaluation order of arguments

Arguments are evaluated **left to right**:

```java
static void show(int a, int b) { System.out.println(a + " " + b); }

int i = 1;
show(i++, i++);          // prints "1 2"   (first i++ gives 1, then i becomes 2, second gives 2)
```

### 5.12 Simulating "output parameters"

Java has no `ref` or `out` parameters. Instead:

1. **Return** the value (best).
2. Return an **object/record/array** holding several values.
3. Pass a **mutable holder** (array of size 1, `AtomicInteger`, or your own object) and let the method fill it.

```java
static void compute(int n, int[] resultHolder) {
    resultHolder[0] = n * n;
}
int[] out = new int[1];
compute(7, out);
System.out.println(out[0]);       // 49
```

---

## 6. Varargs: A Variable Number of Arguments

**Varargs** lets a method accept **any number** of arguments of one type. Write `type... name`.

```java
static int sum(int... numbers) {
    int total = 0;
    for (int n : numbers) {
        total += n;
    }
    return total;
}

sum();                       // 0
sum(5);                      // 5
sum(1, 2, 3);                // 6
sum(10, 20, 30, 40, 50);     // 150
sum(new int[]{7, 8});        // 15  (you may pass a real array too)
```

Inside the method, `numbers` is **just an `int[]`** (`numbers.length`, `numbers[0]`, for-each all work).

### Rules

1. **Only one** varargs parameter per method.
2. It must be the **last** parameter.
3. Calling with **zero** arguments gives an **empty array** (never `null`).

```java
static void log(String level, String... messages) { }     // OK: varargs last
static void bad(String... a, int b) { }                   // ERROR: varargs must be last
static void bad2(int... a, String... b) { }               // ERROR: only one varargs
```

### Real examples from the Java library

```java
String.format("%s is %d years old", "Ali", 25);     // format(String, Object...)
System.out.printf("%d + %d = %d%n", 1, 2, 3);       // printf(String, Object...)
Arrays.asList(1, 2, 3);                             // asList(T...)
```

### Things to know

- Each call creates a **new array** behind the scenes (small cost).
- Avoid **overloading** two varargs methods against each other; calls can become ambiguous.
- `main(String... args)` is legal, equal to `main(String[] args)`.

---

## 7. Method Overloading

### 7.1 What is overloading?

**Overloading** means having **several methods with the same name** in the same class, but with **different parameter lists**.

```java
static int add(int a, int b)             { return a + b; }
static int add(int a, int b, int c)      { return a + b + c; }
static double add(double a, double b)    { return a + b; }
static String add(String a, String b)   { return a + b; }
```

```java
add(2, 3);            // 5        → add(int, int)
add(2, 3, 4);         // 9        → add(int, int, int)
add(2.5, 3.5);        // 6.0      → add(double, double)
add("Hi", "!");       // "Hi!"    → add(String, String)
```

Java picks the right version **automatically**, based on the arguments you give. This is also called **compile-time polymorphism** or **static binding**, because the choice is made **by the compiler**, before the program runs.

### 7.2 What counts as "different"?

Overloaded methods must differ in their **parameter list**:

| Difference | Overload? | Example |
|------------|-----------|---------|
| **Number** of parameters | Yes | `f(int)` and `f(int, int)` |
| **Type** of parameters | Yes | `f(int)` and `f(double)` |
| **Order** of different types | Yes | `f(int, String)` and `f(String, int)` |
| **Only the return type** | **NO** | `int f(int)` and `double f(int)` → error |
| **Only modifiers** (`static`, `public`...) | **NO** | |
| **Only parameter names** | **NO** | `f(int a)` and `f(int b)` → same signature |
| **Only the `throws` clause** | **NO** | |

```java
static int f(int a)    { return a; }
static double f(int a) { return a; }      // ERROR: method f(int) is already defined
```

Why can't the return type distinguish them? Because when you write `f(5);` Java must know which one to call *from the call alone*, and a call doesn't say which return type you want.

### 7.3 Why overload?

1. **One concept, one name:** `print`, `max`, `add` instead of `printInt`, `printDouble`, `printString`.
2. **Convenience:** offer shorter versions that use default values (section 5.10).
3. **Flexibility:** accept several kinds of input.

### 7.4 Real Java library examples

You already use overloading all the time:

| Method | Overloads |
|--------|-----------|
| `System.out.println(...)` | `boolean`, `char`, `int`, `long`, `float`, `double`, `char[]`, `String`, `Object` |
| `Math.max(a, b)` | `int`, `long`, `float`, `double` |
| `Math.abs(x)` | `int`, `long`, `float`, `double` |
| `String.valueOf(x)` | `int`, `double`, `char`, `boolean`, `Object`, `char[]`... |
| `StringBuilder.append(x)` | almost every type |
| `Arrays.sort(x)` | `int[]`, `double[]`, `char[]`, `Object[]`... |
| `List.remove(...)` | `remove(int index)` and `remove(Object o)` |

### 7.5 How Java chooses the right overload

When you call an overloaded method, Java goes through **three phases** and stops at the **first phase that finds a match**:

```
 Phase 1:  exact match or WIDENING only         (int → long → float → double)
              │ none found?
              ▼
 Phase 2:  allow BOXING / UNBOXING too          (int ↔ Integer)
              │ none found?
              ▼
 Phase 3:  allow VARARGS too                    (int...)
              │ none found?
              ▼
          COMPILE ERROR
```

If several methods match in the same phase, Java picks the **most specific** one.

**Full example: one class with many overloads**

```java
public class OverloadDemo {
    static void show(int x)      { System.out.println("int"); }
    static void show(long x)     { System.out.println("long"); }
    static void show(double x)   { System.out.println("double"); }
    static void show(Integer x)  { System.out.println("Integer"); }
    static void show(Object x)   { System.out.println("Object"); }
    static void show(int... x)   { System.out.println("varargs"); }

    public static void main(String[] args) {
        show(5);                    // int         (exact match)
        show(5L);                   // long        (exact match)
        show(5.0f);                 // double      (float widens to double)
        show('a');                  // int         (char widens to int, the closest)
        byte b = 1;
        show(b);                    // int         (byte widens to int, the closest)
        show(Integer.valueOf(5));   // Integer     (exact match)
        show("hi");                 // Object      (String is an Object)
        show();                     // varargs     (only option with zero arguments)
        show(1, 2, 3);              // varargs
    }
}
```

**What if some overloads are removed?**

| If you delete... | `show(5)` now calls... | Why |
|------------------|------------------------|-----|
| nothing | `show(int)` | Exact match |
| `show(int)` | `show(long)` | Next closest widening |
| `show(int)`, `show(long)`, `show(double)` | `show(Integer)` | Boxing (phase 2). `Integer` is more specific than `Object` |
| all of the above and `show(Integer)` | `show(Object)` | Boxing then widening to `Object` |

**Widening beats boxing:**

```java
static void test(long x)    { System.out.println("long"); }
static void test(Integer x) { System.out.println("Integer"); }

test(5);        // long   (phase 1 finds long before phase 2 considers boxing)
```

### 7.6 Ambiguity errors

If Java cannot decide, you get a **compile error**:

```java
static void m(int a, double b) { }
static void m(double a, int b) { }

m(1, 1);        // ERROR: reference to m is ambiguous (both are equally good)
m(1, 1.5);      // OK → m(int, double)
m(1.5, 1);      // OK → m(double, int)
```

### 7.7 Passing `null`

```java
static void f(Object o) { System.out.println("Object"); }
static void f(String s) { System.out.println("String"); }

f(null);        // String   (the most specific type wins)
```

But if the candidates are unrelated, it's ambiguous:

```java
static void g(String s)        { }
static void g(StringBuilder s) { }

g(null);        // ERROR: ambiguous (cast it: g((String) null))
```

### 7.8 Overloads are chosen by the DECLARED type, not the real object

Overloading is decided at **compile time**, by the **type of the variable**, not by what object it currently holds.

```java
static void who(Object o) { System.out.println("Object"); }
static void who(String s) { System.out.println("String"); }

Object x = "hello";       // declared type: Object, real object: a String
who(x);                   // Object   ← chosen by the DECLARED type

String y = "hello";
who(y);                   // String
```

(Choosing by the *real* object at runtime is **overriding**, section 13.)

### 7.9 Overloads can delegate to each other

A very common clean pattern: short versions call the full version.

```java
static double area(double radius) {                 // circle
    return Math.PI * radius * radius;
}
static double area(double width, double height) {   // rectangle
    return width * height;
}
static double area(double base, double height, boolean triangle) {
    return triangle ? 0.5 * base * height : area(base, height);
}
```

### 7.10 A famous overloading trap: `List.remove`

```java
List<Integer> list = new ArrayList<>(List.of(10, 20, 30, 1));

list.remove(1);                       // remove(int index)  → removes the element AT INDEX 1
System.out.println(list);             // [10, 30, 1]

list.remove(Integer.valueOf(1));      // remove(Object o)   → removes the VALUE 1
System.out.println(list);             // [10, 30]
```

`list.remove(1)` picks `remove(int)` because an exact `int` match is found in phase 1, before boxing is considered.

### 7.11 More gotchas with `println` overloads

```java
System.out.println('a');             // a      (char version)
System.out.println('a' + 1);         // 98     (char + int = int, so the int version)
System.out.println((char) ('a' + 1));// b
char[] letters = {'J', 'a', 'v', 'a'};
System.out.println(letters);         // Java   (special char[] overload prints the characters)
```

### 7.12 Overloading constructors

Constructors can be overloaded too (shown in section 14).

---

## 8. Method Chaining

### 8.1 What is method chaining?

**Method chaining** means calling several methods **in a row on one line**, where each method is called on the **result of the previous one**.

```java
String result = "  Hello World  ".trim().toLowerCase().replace(" ", "-");
System.out.println(result);        // hello-world
```

How it runs, step by step (left to right):

```
 "  Hello World  "
      │  .trim()                 → "Hello World"
      ▼
 "Hello World"
      │  .toLowerCase()          → "hello world"
      ▼
 "hello world"
      │  .replace(" ", "-")      → "hello-world"
      ▼
 "hello-world"
```

**The rule that makes chaining work:**

> Each method in the chain must **return an object** that has the **next method** you want to call.

### 8.2 Chaining on methods that return NEW objects (immutable style)

`String` methods never change the original. They return a **new** `String`.

```java
String s = "hello";
s.toUpperCase();                   // result thrown away! s is still "hello"
s = s.toUpperCase();               // now s = "HELLO"
```

### 8.3 Chaining with `StringBuilder`

`StringBuilder` methods return **the same object** (`this`), so chaining is natural:

```java
String result = new StringBuilder()
        .append("a")
        .append(1)
        .append('c')
        .reverse()
        .toString();
System.out.println(result);        // c1a
```

### 8.4 Writing your own chainable class: `return this`

To make **your own** methods chainable, make each one **return `this`** (the current object):

```java
class Calculator {
    private double value;

    Calculator(double start) { this.value = start; }

    Calculator add(double n)      { value += n; return this; }
    Calculator subtract(double n) { value -= n; return this; }
    Calculator multiply(double n) { value *= n; return this; }
    Calculator divide(double n) {
        if (n == 0) throw new ArithmeticException("Cannot divide by zero");
        value /= n;
        return this;
    }

    double result() { return value; }      // the end of the chain returns the final value
}
```

```java
double r = new Calculator(10)
        .add(5)            // 15
        .multiply(2)       // 30
        .subtract(4)       // 26
        .divide(2)         // 13
        .result();

System.out.println(r);     // 13.0
```

```
 new Calculator(10) ─► .add(5) ─► .multiply(2) ─► .subtract(4) ─► .divide(2) ─► .result()
        value=10        15          30              26              13           returns 13.0
                      (each step returns `this`, the SAME Calculator)
```

Without chaining you would write:

```java
Calculator c = new Calculator(10);
c.add(5);
c.multiply(2);
c.subtract(4);
c.divide(2);
double r = c.result();
```

Chaining is shorter and often easier to read. Such an API is called a **fluent interface**.

### 8.5 The Builder pattern (chaining for objects with many options)

When an object has many optional settings, chaining builds it clearly (instead of a constructor with 8 parameters):

```java
class Person {
    private final String name;
    private final int age;
    private final String city;

    private Person(Builder b) {                 // private: only the Builder can create it
        this.name = b.name;
        this.age = b.age;
        this.city = b.city;
    }

    @Override
    public String toString() {
        return "Person{name=" + name + ", age=" + age + ", city=" + city + "}";
    }

    static class Builder {
        private String name = "Unknown";
        private int age = 0;
        private String city = "N/A";

        Builder name(String name) { this.name = name; return this; }
        Builder age(int age)      { this.age = age;   return this; }
        Builder city(String city) { this.city = city; return this; }
        Person build()            { return new Person(this); }
    }
}
```

```java
Person p = new Person.Builder()
        .name("Ali")
        .age(25)
        .city("Paris")
        .build();

System.out.println(p);     // Person{name=Ali, age=25, city=Paris}
```

You can skip any setting, and the call reads like a sentence. This is also how Java's own `StringBuilder`, `HttpRequest.newBuilder()` and many libraries work.

### 8.6 Chaining in streams (Java 8+)

```java
import java.util.*;
import java.util.stream.Collectors;

List<String> names = List.of("ali", "sara", "john", "amy", "bob");

List<String> result = names.stream()
        .filter(n -> n.length() > 3)       // keep names longer than 3 letters
        .map(String::toUpperCase)          // make them uppercase
        .sorted()                          // sort alphabetically
        .collect(Collectors.toList());     // gather into a list

System.out.println(result);                // [JOHN, SARA]
```

```
 [ali, sara, john, amy, bob]
        │ filter(length > 3)
        ▼
 [sara, john]
        │ map(toUpperCase)
        ▼
 [SARA, JOHN]
        │ sorted()
        ▼
 [JOHN, SARA]
```

### 8.7 Dangers of chaining

**1) `NullPointerException`:** if **any** link returns `null`, the next call crashes.

```java
String city = user.getAddress().getCity().toUpperCase();
// If getAddress() returns null → NullPointerException
```

Use null checks or `Optional` when links can be `null`.

**2) Hard-to-debug long chains:** if it crashes in a 10-call chain, which one failed? Keep chains reasonably short and put **one call per line**:

```java
String result = text
        .trim()
        .toLowerCase()
        .replace(" ", "-");
```

**3) Forgetting `return this`:** if a chained method returns `void`, the chain stops:

```java
void add(double n) { value += n; }
calc.add(5).multiply(2);      // ERROR: you can't call .multiply on void
```

### 8.8 Chaining vs nesting

```java
// Nested: read from the inside out (harder to read)
String r = replace(toLowerCase(trim(text)), " ", "-");

// Chained: read left to right, in the order things happen (easier)
String r = text.trim().toLowerCase().replace(" ", "-");
```

---

## 9. Static vs Instance Methods

### 9.1 The difference

| | **Static method** | **Instance method** |
|---|-------------------|---------------------|
| Keyword | has `static` | no `static` |
| Belongs to | The **class** | A specific **object** |
| Called with | `ClassName.method()` | `object.method()` |
| Needs an object? | **No** | **Yes** |
| Can use `this`? | **No** | **Yes** |
| Can use instance variables directly? | **No** | **Yes** |
| Can use static variables? | Yes | Yes |
| Typical use | Utility/helper methods (`Math.max`) | Behavior of an object (`account.deposit`) |

### 9.2 Example

```java
class Counter {
    static int totalCount = 0;      // ONE copy shared by every Counter
    int count = 0;                  // each Counter object has its OWN copy

    void increment() {              // instance method: works on THIS object
        count++;
        totalCount++;
    }

    static void resetTotal() {      // static method: works on the class
        totalCount = 0;
    }
}
```

```java
Counter a = new Counter();
Counter b = new Counter();

a.increment();
a.increment();
b.increment();

System.out.println(a.count);           // 2
System.out.println(b.count);           // 1
System.out.println(Counter.totalCount);// 3   (shared)

Counter.resetTotal();                  // call a static method with the class name
```

```
  Class Counter (static area)               Objects (heap)
  ┌────────────────────┐                    ┌───────────┐   ┌───────────┐
  │ totalCount = 3     │ ◄── shared by ──── │ a         │   │ b         │
  │ resetTotal()       │     everyone       │ count = 2 │   │ count = 1 │
  └────────────────────┘                    └───────────┘   └───────────┘
```

### 9.3 The classic compile error

```java
public class Demo {
    int value = 10;
    void show() { System.out.println(value); }

    public static void main(String[] args) {
        show();             // ERROR: non-static method show() cannot be referenced from a static context
        System.out.println(value);   // ERROR: same problem for a non-static variable
    }
}
```

`main` is `static`: it runs **without any object**. So it can't directly use things that belong to an object. **Fix:** create an object first:

```java
Demo d = new Demo();
d.show();               // OK
```

### 9.4 When to make a method `static`

Make it `static` when it **doesn't use any object's data**, so the result depends only on its parameters:

```java
static int max(int a, int b) { return a > b ? a : b; }        // utility → static
static boolean isPrime(int n) { ... }                          // utility → static
```

Make it an instance method when it **uses or changes the object's fields**:

```java
void deposit(double amount) { balance += amount; }             // uses balance → instance
```

### 9.5 `this` inside an instance method

`this` means "the object this method was called on".

```java
class Student {
    private String name;

    Student(String name) {
        this.name = name;      // this.name = the field; name = the parameter
    }

    void printName() {
        System.out.println(this.name);    // "this." is optional here
    }
}
```

---

## 10. Access Modifiers and Other Method Modifiers

### 10.1 Access modifiers (who can call the method)

| Modifier | Same class | Same package | Subclass (other package) | Anywhere |
|----------|:----------:|:------------:|:------------------------:|:--------:|
| `private` | ✓ | ✗ | ✗ | ✗ |
| *(none)* "package-private" | ✓ | ✓ | ✗ | ✗ |
| `protected` | ✓ | ✓ | ✓ | ✗ |
| `public` | ✓ | ✓ | ✓ | ✓ |

```java
public class Account {
    private double balance;                     // hidden data

    public void deposit(double amount) {        // public: the "official" way in
        validate(amount);
        balance += amount;
    }

    private void validate(double amount) {      // private helper: internal only
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
    }
}
```

**Good practice:** make methods as **private as possible**. Only expose what other code truly needs (this is called **encapsulation**).

### 10.2 Other modifiers

| Modifier | Meaning |
|----------|---------|
| `static` | Belongs to the class, not an object |
| `final` | Can **not be overridden** by a subclass |
| `abstract` | Has **no body**; a subclass must implement it (only in abstract classes/interfaces) |
| `synchronized` | Only one thread can run it at a time on the same object |
| `native` | Implemented in another language (C/C++) via JNI |
| `default` | In an interface: a method with a body (Java 8+) |

```java
abstract class Shape {
    abstract double area();             // no body: subclasses must write it
    final void describe() {             // can't be overridden
        System.out.println("Area = " + area());
    }
}
```

### 10.3 Kinds of methods you will meet

| Kind | Example |
|------|---------|
| Instance method | `account.deposit(50)` |
| Static method | `Math.sqrt(16)` |
| Getter / setter | `getName()`, `setName("x")` |
| Constructor (special) | `new Person("Ali")` |
| Abstract method | `abstract double area();` |
| Overloaded methods | `println(int)`, `println(String)` |
| Overridden method | `toString()` in your own class |
| Recursive method | A method that calls itself |
| Varargs method | `sum(int... nums)` |
| Generic method | `<T> void print(T x)` |
| Lambda / method reference | `x -> x * 2`, `String::length` |
| Default / static interface method | `default void log() { }` |

---

## 11. Scope, Lifetime and the Call Stack

### 11.1 Local variables and parameters are born and die with the method

```java
static int calc(int a) {          // 'a' is created
    int result = a * 2;           // 'result' is created
    return result;
}                                 // both are destroyed here

// 'a' and 'result' do NOT exist out here
```

### 11.2 Block scope

A variable exists only inside the `{ }` where it was declared.

```java
for (int i = 0; i < 3; i++) {
    int square = i * i;
}
System.out.println(i);        // ERROR: i doesn't exist here
System.out.println(square);   // ERROR: square doesn't exist here
```

### 11.3 Shadowing

A local variable or parameter can have the **same name as a field**; the local one **hides** the field.

```java
class Box {
    int size = 10;                  // field

    void test(int size) {           // parameter hides the field
        System.out.println(size);        // the parameter
        System.out.println(this.size);   // the field: 10
    }
}
```

But two locals in the **same scope** can't share a name:

```java
static void f(int x) {
    int x = 5;                      // ERROR: variable x is already defined
}
```

### 11.4 The Call Stack

Every method call gets its own **stack frame** holding its parameters and local variables. Frames are stacked; the **top frame** is the running method.

```java
static int square(int x)              { return x * x; }
static int sumOfSquares(int a, int b) { return square(a) + square(b); }

public static void main(String[] args) {
    int r = sumOfSquares(3, 4);
    System.out.println(r);            // 25
}
```

How the stack changes over time:

```
 (1) main starts      (2) main calls          (3) sumOfSquares        (4) square returns 9,
                          sumOfSquares            calls square(3)         then square(4) runs
 ┌────────────┐       ┌────────────────┐      ┌────────────────┐      ┌────────────────┐
 │ main       │       │ sumOfSquares   │      │ square  x=3    │      │ square  x=4    │
 │            │       │   a=3  b=4     │      ├────────────────┤      ├────────────────┤
 └────────────┘       ├────────────────┤      │ sumOfSquares   │      │ sumOfSquares   │
                      │ main           │      │   a=3  b=4     │      │   a=3  b=4     │
                      └────────────────┘      ├────────────────┤      ├────────────────┤
                                              │ main           │      │ main           │
                                              └────────────────┘      └────────────────┘
 (5) square returns 16, so sumOfSquares returns 9 + 16 = 25 → main receives 25
```

The stack is **limited in size**. If you nest too many calls (usually by endless recursion), you get **`StackOverflowError`**.

> The JVM's JIT compiler can **inline** small methods (paste their body into the caller), so using many small methods costs almost nothing at runtime. Write clear methods; don't worry about call overhead.

---

## 12. Recursion

### 12.1 What is recursion?

A **recursive method** is a method that **calls itself**, solving a problem by solving a **smaller version** of the same problem.

Every recursive method needs two parts:

| Part | Meaning |
|------|---------|
| **Base case** | The simplest case, answered directly. It **stops** the recursion. |
| **Recursive case** | Calls itself with a **smaller/simpler** input, moving toward the base case. |

> **Without a base case, the method calls itself forever, and you get `StackOverflowError`.**

### 12.2 Example: factorial

`n! = n × (n−1) × ... × 1`, and `0! = 1`.

```java
static long factorial(int n) {
    if (n <= 1) {                       // base case
        return 1;
    }
    return n * factorial(n - 1);        // recursive case
}

System.out.println(factorial(5));       // 120
```

How `factorial(4)` unfolds:

```
 factorial(4)
  = 4 × factorial(3)
  = 4 × (3 × factorial(2))
  = 4 × (3 × (2 × factorial(1)))
  = 4 × (3 × (2 × 1))          ← base case reached: factorial(1) = 1
  = 4 × (3 × 2)
  = 4 × 6
  = 24
```

The call stack for `factorial(3)`:

```
 CALLING DOWN (frames pile up)            RETURNING UP (frames are removed)

 ┌──────────────────────┐                 ┌──────────────────────┐
 │ factorial(1)  → 1    │ base case       │ factorial(1) returns 1│
 ├──────────────────────┤                 ├──────────────────────┤
 │ factorial(2)  waits  │                 │ factorial(2) returns 2×1 = 2 │
 ├──────────────────────┤                 ├──────────────────────┤
 │ factorial(3)  waits  │                 │ factorial(3) returns 3×2 = 6 │
 ├──────────────────────┤                 └──────────────────────┘
 │ main                 │
 └──────────────────────┘
```

> Watch out: `factorial` overflows `long` after `20!`. `21!` doesn't fit in a `long` (use `BigInteger` for bigger values).

### 12.3 Example: sum of digits

```java
static int sumDigits(int n) {
    if (n < 10) return n;                         // base case: a single digit
    return (n % 10) + sumDigits(n / 10);          // last digit + sum of the rest
}

System.out.println(sumDigits(1234));              // 10   (4 + 3 + 2 + 1)
```

### 12.4 Example: power

```java
static long power(int base, int exp) {
    if (exp == 0) return 1;                       // base case
    return base * power(base, exp - 1);
}

System.out.println(power(2, 10));                 // 1024
```

### 12.5 Example: Fibonacci (and why naive recursion is slow)

Fibonacci: `0, 1, 1, 2, 3, 5, 8, 13, ...` Each number is the sum of the previous two.

```java
static int fib(int n) {
    if (n <= 1) return n;                         // base cases: fib(0)=0, fib(1)=1
    return fib(n - 1) + fib(n - 2);
}

System.out.println(fib(6));                       // 8
```

Problem: it **repeats the same work** many times.

```
                      fib(5)
                     /      \
                fib(4)       fib(3)
               /     \       /    \
          fib(3)   fib(2) fib(2) fib(1)
          /   \
      fib(2) fib(1)       ← fib(3), fib(2) are computed again and again
```

`fib(50)` would take forever this way. Fix it with **memoization** (remember answers already computed):

```java
static long fib(int n, long[] memo) {
    if (n <= 1) return n;
    if (memo[n] != 0) return memo[n];                         // already computed
    return memo[n] = fib(n - 1, memo) + fib(n - 2, memo);     // compute once, store it
}

System.out.println(fib(50, new long[51]));        // 12586269025   (instant)
```

### 12.6 Example: palindrome check with recursion

```java
static boolean isPalindrome(String s) {
    if (s.length() <= 1) return true;                        // base case
    if (s.charAt(0) != s.charAt(s.length() - 1)) return false;
    return isPalindrome(s.substring(1, s.length() - 1));     // check the inside part
}

isPalindrome("racecar");     // true
isPalindrome("hello");       // false
```

### 12.7 StackOverflowError

```java
static void forever(int n) {
    forever(n + 1);                 // no base case!
}
forever(0);                         // Exception: java.lang.StackOverflowError
```

Even correct recursion can overflow if it goes **very deep** (for example, depth 100,000). The default stack usually allows roughly **10,000 to 20,000** levels, depending on the frame size.

### 12.8 Recursion vs loops

| | Recursion | Loop |
|---|-----------|------|
| Code | Often shorter and clearer for tree-like/self-similar problems | Often more direct for simple repetition |
| Memory | Uses a stack frame **per call** | Constant memory |
| Speed | Slightly slower (call overhead) | Faster |
| Risk | `StackOverflowError` | Infinite loop |
| Good for | Trees, folders, divide-and-conquer, backtracking, Fibonacci-style definitions | Counting, summing, scanning arrays |

> Java does **not** optimize "tail recursion", so a deep recursion that could be a loop should be written as a loop.

---

## 13. Method Overriding (Overloading's Cousin)

**Overriding** means a **subclass provides its own version** of a method that its **parent class** already has, with the **same signature**.

```java
class Animal {
    void speak() {
        System.out.println("Some sound");
    }
}

class Dog extends Animal {
    @Override
    void speak() {                              // same name and parameters
        System.out.println("Woof");
    }
}

class Cat extends Animal {
    @Override
    void speak() {
        System.out.println("Meow");
    }
}
```

```java
Animal a1 = new Dog();
Animal a2 = new Cat();
a1.speak();      // Woof   ← chosen at RUNTIME by the real object (Dog)
a2.speak();      // Meow
```

This is **runtime polymorphism** (dynamic binding): the version that runs is decided by the **actual object**, not the variable's type.

### Overloading vs Overriding

| | **Overloading** | **Overriding** |
|---|-----------------|----------------|
| Where | Same class (or a subclass adding new versions) | Subclass vs superclass |
| Method name | Same | Same |
| Parameters | **Must differ** | **Must be the same** |
| Return type | Can differ | Same (or a subtype, "covariant") |
| Decided | **Compile time** (by declared types) | **Runtime** (by the real object) |
| Annotation | None | `@Override` (recommended) |
| `static` methods | Can be overloaded | Cannot be overridden (only hidden) |
| `private` / `final` methods | Can be overloaded | Cannot be overridden |

### Rules for overriding

1. Same **name and parameter list**.
2. Return type must be **the same or a subtype**.
3. Access can't be **more restrictive** (a `public` method can't be overridden as `private`).
4. Can't throw **broader checked exceptions**.
5. Use **`@Override`**: the compiler then catches typos like `speek()`.
6. Call the parent's version with **`super.speak()`**.

```java
class Puppy extends Dog {
    @Override
    void speak() {
        super.speak();                          // runs Dog's speak() first
        System.out.println("(squeaky)");
    }
}
```

---

## 14. Constructors and Constructor Chaining

### 14.1 Constructors are special methods

A **constructor** has the **same name as the class**, **no return type**, and runs when you create an object with `new`.

```java
class Student {
    String name;
    int age;

    Student(String name, int age) {      // constructor
        this.name = name;
        this.age = age;
    }
}

Student s = new Student("Ali", 20);
```

If you write **no** constructor, Java adds an empty **default constructor** for you. As soon as you write any constructor, that default disappears.

### 14.2 Constructor overloading

```java
class Student {
    String name;
    int age;
    String course;

    Student() {                                       // no arguments
        this.name = "Unknown";
        this.age = 0;
        this.course = "None";
    }

    Student(String name) {
        this.name = name;
        this.age = 0;
        this.course = "None";
    }

    Student(String name, int age, String course) {
        this.name = name;
        this.age = age;
        this.course = course;
    }
}
```

### 14.3 Constructor chaining with `this(...)`

The code above repeats itself. **`this(...)`** lets one constructor **call another** in the same class. This is called **constructor chaining**.

```java
class Student {
    String name;
    int age;
    String course;

    Student() {
        this("Unknown", 0, "None");               // calls the 3-argument constructor
    }

    Student(String name) {
        this(name, 0, "None");                    // calls the 3-argument constructor
    }

    Student(String name, int age, String course) {   // the "main" constructor
        this.name = name;
        this.age = age;
        this.course = course;
    }
}
```

```
  new Student()          ─► this("Unknown", 0, "None") ─┐
  new Student("Ali")     ─► this("Ali", 0, "None")     ─┼─► Student(String, int, String)  ← real work done once
  new Student("Ali",20,"CS") ──────────────────────────┘
```

**Rule:** `this(...)` must be the **first statement** in the constructor.

### 14.4 `super(...)`: chaining to the parent's constructor

```java
class Person {
    String name;
    Person(String name) { this.name = name; }
}

class Employee extends Person {
    double salary;
    Employee(String name, double salary) {
        super(name);                 // call the parent constructor first
        this.salary = salary;
    }
}
```

---

## 15. Generic Methods

A **generic method** works with **any type** while staying type-safe. The type parameter goes before the return type: `<T>`.

```java
static <T> void printArray(T[] items) {
    for (T item : items) {
        System.out.print(item + " ");
    }
    System.out.println();
}

printArray(new Integer[]{1, 2, 3});        // 1 2 3
printArray(new String[]{"a", "b", "c"});   // a b c
```

With a **bound** (the type must be comparable):

```java
static <T extends Comparable<T>> T maxOf(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}

System.out.println(maxOf(3, 7));                  // 7
System.out.println(maxOf("apple", "banana"));     // banana
System.out.println(maxOf(2.5, 1.5));              // 2.5
```

One method replaces many overloads (`maxOf(int,int)`, `maxOf(String,String)`, ...). Java infers `T` from the arguments. (Generics work with **objects**, not primitives; `3` is auto-boxed to `Integer`.)

---

## 16. Methods and Exceptions

### 16.1 Throwing an exception from a method

When a method receives bad input, it can **throw** an exception instead of returning nonsense.

```java
static int divide(int a, int b) {
    if (b == 0) {
        throw new IllegalArgumentException("b must not be zero");
    }
    return a / b;
}
```

### 16.2 Catching it in the caller

```java
try {
    System.out.println(divide(10, 2));     // 5
    System.out.println(divide(10, 0));     // throws
    System.out.println("not reached");
} catch (IllegalArgumentException e) {
    System.out.println("Error: " + e.getMessage());
}
```

Output:
```
5
Error: b must not be zero
```

### 16.3 Checked exceptions and `throws`

**Checked** exceptions (like `IOException`) must be either **caught** or **declared** with `throws` in the method header:

```java
import java.io.*;

static String readFirstLine(String path) throws IOException {      // declares it
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine();
    }
}

public static void main(String[] args) {
    try {
        System.out.println(readFirstLine("data.txt"));
    } catch (IOException e) {
        System.out.println("Could not read file: " + e.getMessage());
    }
}
```

| Type | Examples | Must handle? |
|------|----------|--------------|
| **Unchecked** (RuntimeException) | `IllegalArgumentException`, `NullPointerException`, `ArithmeticException` | No |
| **Checked** | `IOException`, `SQLException` | **Yes**: `try/catch` or `throws` |

### 16.4 What happens when an exception is thrown

The exception **travels back up the call stack**, frame by frame, until someone catches it:

```
 main  ────────► calls process() ──► calls divide(10, 0)
                                          │ throw!
                                          ▼
                              divide has no catch → frame removed
                                          ▼
                              process has no catch → frame removed
                                          ▼
                              main has catch → handles it ✓
```

If nobody catches it, the program stops and prints a stack trace.

### 16.5 Validate parameters at the top

```java
static double areaOfRectangle(double width, double height) {
    if (width <= 0 || height <= 0) {
        throw new IllegalArgumentException("Width and height must be positive");
    }
    return width * height;
}
```

---

## 17. Methods as Values: Lambdas and Method References

Since **Java 8**, you can treat a piece of behavior as a **value**: store it in a variable, pass it to a method, or return it from a method.

### 17.1 Lambda expressions

A **lambda** is a short, nameless method:

```
 (parameters) -> expression
 (parameters) -> { statements; }
```

```java
// Regular method:
static int square(int x) { return x * x; }

// The same thing as a lambda:
Function<Integer, Integer> square = x -> x * x;
System.out.println(square.apply(5));          // 25
```

### 17.2 Functional interfaces: the "types" of lambdas

A lambda needs a **functional interface** (an interface with exactly one abstract method). Java provides common ones in `java.util.function`:

| Interface | Method | Takes | Returns | Example |
|-----------|--------|-------|---------|---------|
| `Function<T,R>` | `apply(T)` | one value | a value | `x -> x * 2` |
| `BiFunction<T,U,R>` | `apply(T,U)` | two values | a value | `(a, b) -> a + b` |
| `Predicate<T>` | `test(T)` | one value | `boolean` | `n -> n % 2 == 0` |
| `Consumer<T>` | `accept(T)` | one value | nothing | `s -> System.out.println(s)` |
| `Supplier<T>` | `get()` | nothing | a value | `() -> "hello"` |
| `UnaryOperator<T>` | `apply(T)` | one value | same type | `s -> s.trim()` |
| `BinaryOperator<T>` | `apply(T,T)` | two values | same type | `(a, b) -> a * b` |
| `Runnable` | `run()` | nothing | nothing | `() -> System.out.println("run")` |

```java
import java.util.function.*;

Function<Integer, Integer> doubler = x -> x * 2;
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
Predicate<Integer> isEven = n -> n % 2 == 0;
Supplier<String> greeting = () -> "Hello!";
Consumer<String> printer = s -> System.out.println(s);
Runnable task = () -> System.out.println("Running...");

System.out.println(doubler.apply(21));        // 42
System.out.println(add.apply(3, 4));          // 7
System.out.println(isEven.test(10));          // true
System.out.println(greeting.get());           // Hello!
printer.accept("Hi");                         // Hi
task.run();                                   // Running...
```

### 17.3 Passing a method (behavior) as an argument

```java
static int applyTwice(Function<Integer, Integer> f, int x) {
    return f.apply(f.apply(x));
}

System.out.println(applyTwice(x -> x * x, 3));       // 81   (3 → 9 → 81)
System.out.println(applyTwice(x -> x + 10, 5));      // 25   (5 → 15 → 25)
```

The same method `applyTwice` does different things depending on the behavior you pass in.

### 17.4 Returning a method (behavior) from a method

```java
static Function<Integer, Integer> multiplier(int n) {
    return x -> x * n;                       // the lambda "remembers" n
}

Function<Integer, Integer> triple = multiplier(3);
System.out.println(triple.apply(4));         // 12
System.out.println(multiplier(10).apply(7)); // 70
```

A lambda may use local variables from the enclosing method, but they must be **effectively final** (never reassigned).

### 17.5 Chaining functions

```java
Function<Integer, Integer> square = x -> x * x;

System.out.println(square.andThen(x -> x + 1).apply(5));    // 26   (square first, then +1)
System.out.println(square.compose((Integer x) -> x + 1).apply(5));   // 36   (+1 first, then square)

Predicate<Integer> isEven = n -> n % 2 == 0;
Predicate<Integer> isBig  = n -> n > 10;
System.out.println(isEven.and(isBig).test(12));     // true
System.out.println(isEven.and(isBig).test(8));      // false
System.out.println(isEven.negate().test(7));        // true
```

### 17.6 Method references `::`

When a lambda only **calls one existing method**, you can write it shorter:

| Kind | Lambda | Method reference |
|------|--------|------------------|
| Static method | `s -> Integer.parseInt(s)` | `Integer::parseInt` |
| Instance method of a type | `s -> s.toUpperCase()` | `String::toUpperCase` |
| Instance method of an object | `x -> System.out.println(x)` | `System.out::println` |
| Constructor | `() -> new ArrayList<>()` | `ArrayList::new` |

```java
Function<String, Integer> parse = Integer::parseInt;
Function<String, String> upper = String::toUpperCase;
Consumer<String> print = System.out::println;

System.out.println(parse.apply("42") + 1);   // 43
System.out.println(upper.apply("java"));     // JAVA
print.accept("done");                        // done
```

### 17.7 Why this matters

Lambdas power **streams**, **sorting with comparators**, **event handlers** and **concurrency** (`Runnable`):

```java
List<String> words = new ArrayList<>(List.of("pear", "fig", "banana"));
words.sort((a, b) -> a.length() - b.length());       // sort by length
System.out.println(words);                           // [fig, pear, banana]

words.forEach(System.out::println);                  // method reference as an argument
```

---

## 18. The `main` Method Explained

```java
public static void main(String[] args) {
    // program starts here
}
```

| Word | Why |
|------|-----|
| `public` | The JVM must be able to call it from outside the class |
| `static` | The JVM calls it **without creating an object** |
| `void` | It returns nothing to the JVM |
| `main` | The special name the JVM looks for |
| `String[] args` | The command-line arguments (an array of Strings) |

```
 java MyApp hello 42
          ↓
 args[0] = "hello"
 args[1] = "42"        (still a String; use Integer.parseInt to convert)
```

`main` is just a **normal method**: it can call other methods, and other code can even call `main(...)`. It is the entry point because the JVM chooses to call it first. (Recent Java versions also allow simpler forms of `main`, but the classic form above works everywhere.)

---

## 19. Best Practices for Writing Methods

1. **One method, one job** (Single Responsibility). If you describe it with "and", split it.
2. **Use clear verb names:** `calculateTax`, `isValid`, `findUserById`.
3. **Keep methods short**, usually less than about 20 to 30 lines.
4. **Few parameters:** ideally 0 to 3. If you need many, use a parameter object or a builder.
5. **Avoid boolean "flag" parameters:** `print(text, true)` is unclear. Make two methods: `printBold(text)`, `printNormal(text)`.
6. **Validate inputs** at the top (guard clauses) and fail fast with clear exceptions.
7. **Prefer returning a value** over changing the arguments.
8. **Avoid side effects** when possible. A **pure method** always gives the same output for the same input and changes nothing else:

   ```java
   // Pure: easy to test and trust
   static int add(int a, int b) { return a + b; }

   // Impure: depends on and changes outside state
   static int total = 0;
   static int addToTotal(int a) { total += a; return total; }
   ```
9. **Never return `null` for a collection:** return an empty list.
10. **Don't repeat yourself.** If you copy-paste code, make it a method.
11. **Make helpers `private`.** Expose only what's needed.
12. **Document with Javadoc** for public methods:

    ```java
    /**
     * Calculates the area of a rectangle.
     *
     * @param width  the width (must be positive)
     * @param height the height (must be positive)
     * @return the area
     * @throws IllegalArgumentException if width or height is not positive
     */
    static double area(double width, double height) { ... }
    ```
13. **Don't worry about call overhead.** The JIT compiler inlines small methods.

---

## 20. Practical Programs

Each program is complete. Copy it, save it as `ClassName.java`, run with `javac ClassName.java` then `java ClassName`.

### 20.1 `MathUtils`: overloading, varargs and recursion

```java
public class MathUtils {

    // ---------- overloaded max ----------
    static int max(int a, int b) {
        return a > b ? a : b;
    }

    static double max(double a, double b) {
        return a > b ? a : b;
    }

    static int max(int... nums) {                 // varargs version
        int m = nums[0];
        for (int n : nums) {
            if (n > m) m = n;
        }
        return m;
    }

    // ---------- varargs average ----------
    static double average(int... nums) {
        if (nums.length == 0) return 0;
        int sum = 0;
        for (int n : nums) sum += n;
        return (double) sum / nums.length;
    }

    // ---------- boolean checker ----------
    static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; (long) i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    // ---------- recursion ----------
    static long factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        if (n <= 1) return 1;
        return n * factorial(n - 1);
    }

    static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    public static void main(String[] args) {
        System.out.println(max(3, 7));             // 7      → max(int, int)
        System.out.println(max(3, 7.5));           // 7.5    → max(double, double)
        System.out.println(max(4, 9, 2, 6));       // 9      → max(int...)
        System.out.println(average(80, 90, 100));  // 90.0
        System.out.println(isPrime(29));           // true
        System.out.println(isPrime(30));           // false
        System.out.println(factorial(10));         // 3628800
        System.out.println(gcd(48, 18));           // 6
    }
}
```

Output:
```
7
7.5
9
90.0
true
false
3628800
6
```

**What it shows:** three overloads of `max` (Java picks by arguments), a varargs method, a `boolean` method, and two recursive methods.

### 20.2 `BankAccount`: chaining, overloading, exceptions

```java
public class BankAccount {
    private final String owner;
    private double balance;

    // constructor chaining
    BankAccount(String owner) {
        this(owner, 0);
    }

    BankAccount(String owner, double initialBalance) {
        this.owner = owner;
        this.balance = initialBalance;
    }

    // overloaded deposit methods; each returns `this` so calls can be chained
    BankAccount deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit must be positive");
        }
        balance += amount;
        return this;
    }

    BankAccount deposit(double amount, String note) {
        System.out.println("Note: " + note);
        return deposit(amount);                    // delegates to the first version
    }

    BankAccount withdraw(double amount) {
        if (amount > balance) {
            throw new IllegalStateException("Insufficient funds");
        }
        balance -= amount;
        return this;
    }

    double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "BankAccount[owner=" + owner + ", balance=" + balance + "]";
    }

    public static void main(String[] args) {
        BankAccount acc = new BankAccount("Ali", 100);

        acc.deposit(50)                       // 150
           .deposit(25, "salary bonus")       // 175
           .withdraw(30);                     // 145

        System.out.println(acc);

        try {
            acc.withdraw(1000);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println(acc);              // balance unchanged
    }
}
```

Output:
```
Note: salary bonus
BankAccount[owner=Ali, balance=145.0]
Error: Insufficient funds
BankAccount[owner=Ali, balance=145.0]
```

**What it shows:** constructor chaining (`this(...)`), overloading (`deposit` with and without a note), **method chaining** (`return this`), throwing and catching exceptions, `private` fields with `public` methods.

### 20.3 `TextUtils`: string methods and helper methods

```java
public class TextUtils {

    static boolean isPalindrome(String s) {
        String clean = s.toLowerCase().replaceAll("[^a-z0-9]", "");        // chaining
        String reversed = new StringBuilder(clean).reverse().toString();   // chaining
        return clean.equals(reversed);
    }

    static int countVowels(String s) {
        int count = 0;
        for (char ch : s.toLowerCase().toCharArray()) {
            if ("aeiou".indexOf(ch) >= 0) {
                count++;
            }
        }
        return count;
    }

    static String reverseWords(String s) {
        String[] words = s.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = words.length - 1; i >= 0; i--) {
            sb.append(words[i]);
            if (i > 0) sb.append(" ");
        }
        return sb.toString();
    }

    static String capitalize(String word) {
        if (word == null || word.isEmpty()) return word;
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }

    public static void main(String[] args) {
        System.out.println(isPalindrome("A man, a plan, a canal: Panama"));   // true
        System.out.println(isPalindrome("Hello"));                            // false
        System.out.println(countVowels("Hello World"));                       // 3
        System.out.println(reverseWords("Java is fun"));                      // fun is Java
        System.out.println(capitalize("jAVA"));                               // Java
    }
}
```

Output:
```
true
false
3
fun is Java
Java
```

### 20.4 `StudentReport`: returning several values with a record

```java
import java.util.Arrays;

public class StudentReport {

    record Stats(int min, int max, double average) {}       // Java 16+

    static Stats analyze(int[] marks) {
        int min = marks[0];
        int max = marks[0];
        int sum = 0;
        for (int m : marks) {
            if (m < min) min = m;
            if (m > max) max = m;
            sum += m;
        }
        return new Stats(min, max, (double) sum / marks.length);
    }

    static char grade(double average) {
        if (average >= 90) return 'A';
        if (average >= 80) return 'B';
        if (average >= 70) return 'C';
        if (average >= 60) return 'D';
        return 'F';
    }

    static void printReport(String name, int[] marks) {
        Stats s = analyze(marks);
        System.out.println("Student: " + name);
        System.out.println("Marks  : " + Arrays.toString(marks));
        System.out.println(s);
        System.out.println("Grade  : " + grade(s.average()));
    }

    public static void main(String[] args) {
        printReport("Ali", new int[]{85, 92, 78, 90, 66});
    }
}
```

Output:
```
Student: Ali
Marks  : [85, 92, 78, 90, 66]
Stats[min=66, max=92, average=82.2]
Grade  : B
```

**What it shows:** a method that **returns several values** (a record), an array passed as an argument, small single-purpose methods that call each other (`printReport` → `analyze` and `grade`).

### 20.5 Try it yourself (practice ideas)

1. Write overloaded `area` methods for a circle, rectangle and triangle.
2. Write `static boolean isLeapYear(int year)` and use it in `daysInMonth(int month, int year)`.
3. Make a chainable `StringBuilder`-like class `Text` with `append`, `upper`, `trim` methods that return `this`.
4. Write a recursive `binarySearch(int[] a, int target, int low, int high)`.
5. Write `static <T> void swap(T[] arr, int i, int j)` (a generic method).
6. Write a method `applyToAll(int[] arr, Function<Integer,Integer> f)` that returns a new array with `f` applied to every element.

---

## 21. Common Mistakes

| # | Mistake | Why it's wrong | Fix |
|---|---------|----------------|-----|
| 1 | Expecting a method to change a primitive argument | Java passes a **copy** | Return the new value |
| 2 | `swap(a, b)` that doesn't swap | Parameters are copies | Swap array elements or inline |
| 3 | `s.toUpperCase();` and ignoring the result | Strings are immutable | `s = s.toUpperCase();` |
| 4 | Missing `return` on some path | "missing return statement" | Return on every path |
| 5 | Code after `return` | "unreachable statement" | Remove it |
| 6 | Overloading by return type only | Same signature | Change the parameters or the name |
| 7 | Ambiguous overload call (`m(1, 1)`) | Two equally good matches | Cast an argument or rename |
| 8 | Calling an instance method from `static main` | No object exists | Create an object first |
| 9 | Recursion with no base case | Infinite calls | Add a base case that is always reachable |
| 10 | Forgetting `return this` in a chainable method | Chain breaks (`void`) | Return `this` |
| 11 | `a.getB().getC()` when `getB()` can be `null` | `NullPointerException` | Check for `null` or use `Optional` |
| 12 | `static` method that needs object data | Can't access instance fields | Make it an instance method |
| 13 | Varargs not last | Compile error | Put varargs last |
| 14 | Thinking `Object x = "hi"; who(x)` calls `who(String)` | Overloads use the **declared** type | Cast: `who((String) x)` |
| 15 | `list.remove(1)` meaning "remove value 1" | Picks `remove(int index)` | `list.remove(Integer.valueOf(1))` |
| 16 | Forgetting `@Override` and mistyping the name | Silently creates a new method | Always add `@Override` |
| 17 | Doing integer division inside a method then returning `double` | `5 / 2` is already `2` | `5 / 2.0` or cast first |
| 18 | Huge recursion depth | `StackOverflowError` | Use a loop |
| 19 | Using `==` for strings inside a method | Compares references | `.equals()` |
| 20 | Method does too many things | Hard to read/test | Split into smaller methods |

---

## 22. Cheat Sheet, Summary and Quiz

### 22.1 Syntax cheat sheet

```java
// ---------- declaration ----------
[modifiers] returnType name([parameters]) [throws X] { body }

static void greet(String name)           { ... }       // no return value
static int add(int a, int b)             { return a + b; }
public double area(double w, double h)   { return w * h; }
static int sum(int... nums)              { ... }       // varargs
static <T> T first(List<T> list)         { ... }       // generic
abstract void draw();                                  // abstract: no body

// ---------- calling ----------
greet("Ali");                       // static, same class
MathUtils.max(3, 7);                // static, other class
account.deposit(50);                // instance method
account.deposit(50).withdraw(20);   // chaining

// ---------- overloading ----------
int add(int a, int b)       double add(double a, double b)       int add(int a, int b, int c)

// ---------- chaining ----------
Builder name(String n) { this.name = n; return this; }

// ---------- lambda ----------
Function<Integer,Integer> sq = x -> x * x;       sq.apply(5);
Predicate<String> empty = String::isEmpty;       empty.test("");

// ---------- constructor chaining ----------
Student() { this("Unknown", 0); }
```

### 22.2 Quick reference tables

**Overload resolution order**

| Phase | Allows |
|-------|--------|
| 1 | Exact match, widening |
| 2 | + boxing / unboxing |
| 3 | + varargs |

**What passes how**

| You pass | The method gets | Can change the caller's original? |
|----------|-----------------|-----------------------------------|
| `int`, `double`, `char`... | A copy of the value | **No** |
| `String`, `Integer`... (immutable) | A copy of the reference | **No** (can't mutate them) |
| Array / object (mutable) | A copy of the reference | **Contents: Yes. Reassigning: No** |

### 22.3 Summary in simple points

1. A **method** is a named, reusable block of code. In Java every function lives inside a class.
2. Anatomy: **modifiers, return type, name, parameters, body**. The **signature** is the name plus the parameter types (not the return type).
3. `return` sends a value back and ends the method. **Every path must return** (unless `void`).
4. **Java is always pass-by-value.** Primitives: a copy of the value. Objects/arrays: a copy of the reference. So a method can **mutate** a shared object, but can **not re-point** your variable.
5. **Varargs** (`int... nums`) accepts any number of arguments; it must be the **last** and **only** varargs parameter.
6. **Overloading** = same name, **different parameters** (number, type or order). **Not** the return type. The compiler picks using: **widening, then boxing, then varargs**, choosing the most specific. Chosen by the **declared type** at compile time.
7. **Method chaining** = calling methods in a row. For your own classes, **`return this`**. Builders, `StringBuilder` and streams all use it. Beware of `null` in the middle of a chain.
8. **Static** methods belong to the class (no object needed); **instance** methods belong to an object and can use `this` and its fields.
9. **Access modifiers** control who can call a method; keep helpers `private`.
10. Each call gets a **stack frame**; too many nested calls cause `StackOverflowError`.
11. **Recursion** needs a **base case** and a smaller recursive case. Use memoization or loops for efficiency.
12. **Overriding** (subclass replaces a parent's method) is decided at **runtime**; use `@Override`.
13. **Constructors** can be overloaded and chained with `this(...)` and `super(...)`.
14. **Generic methods** (`<T>`) work with many types; **lambdas** and **method references** let you pass behavior around as values.
15. Write **small, focused, well-named** methods, with validated inputs and few parameters.

### 22.4 Mini Quiz

1. Is the return type part of a method's signature?
2. What does this print?
   ```java
   static void change(int x) { x = 100; }
   public static void main(String[] args) { int a = 5; change(a); System.out.println(a); }
   ```
3. With `test(long x)` and `test(Integer x)` defined, what does `test(5)` call?
4. What do you write inside a method to make it chainable, and what must its return type be?
5. What does this print?
   ```java
   static void who(Object o) { System.out.println("Object"); }
   static void who(String s) { System.out.println("String"); }
   Object x = "hello";
   who(x);
   ```
6. Can you overload a method by changing only its return type?
7. Does Java support default parameter values? What do you use instead?
8. Where are a method's parameters and local variables stored?
9. What error occurs when a recursive method has no base case?
10. With `f(Object o)` and `f(String s)`, what does `f(null)` call?
11. What are the two rules for a varargs parameter?
12. What does `applyTwice(x -> x * 2, 5)` return if `applyTwice(Function<Integer,Integer> f, int x)` returns `f.apply(f.apply(x))`?
13. What is the difference between overloading and overriding, in one line each?
14. Why does `String s = "hi"; s.toUpperCase(); System.out.println(s);` print `hi`?

<details>
<summary>Click for answers</summary>

1. **No.** The signature is the name plus the parameter types.
2. **5.** The method changes only its own copy of `x`.
3. **`test(long)`**: widening (phase 1) is preferred over boxing (phase 2).
4. **`return this;`**, and the return type must be the **class itself** (for example `Calculator`).
5. **Object.** Overloads are chosen by the **declared** type (`Object`) at compile time.
6. **No.** It would be the same signature, causing a compile error.
7. **No.** Use **overloading** (a shorter version that calls the full version with default values) or a builder.
8. In the method's **stack frame** on the **call stack**.
9. **`StackOverflowError`.**
10. **`f(String)`**, because `String` is the most specific type.
11. It must be the **last** parameter, and there can be **only one** varargs parameter.
12. **20** (5 → 10 → 20).
13. **Overloading:** same name, different parameters, chosen at compile time. **Overriding:** a subclass replaces a parent method with the same signature, chosen at runtime.
14. Strings are **immutable**; `toUpperCase()` returns a **new** String, and the result was thrown away. Use `s = s.toUpperCase();`.

</details>

---

*Practice tip: take each example in this guide, change it, and predict the output before running it. Methods are the building blocks of every Java program, so time spent here pays off everywhere.*
