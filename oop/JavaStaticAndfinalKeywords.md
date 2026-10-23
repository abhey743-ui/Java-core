# Java `static` and `final` Keywords Deep Dive
### Static Variables · Static Methods · Static Blocks · Final · `main()` · Static Final · `String[] args`

---

## Table of Contents

1. [The problem that `static` solves](#1-the-problem-that-static-solves)
2. [Static variables](#2-static-variables)
3. [Static methods and their rules](#3-static-methods-and-their-rules)
4. [Static blocks](#4-static-blocks)
5. [Order of execution: static block, instance block, constructor](#5-order-of-execution-static-block-instance-block-constructor)
6. [The `final` keyword](#6-the-final-keyword)
7. [`static final`: real constants](#7-static-final-real-constants)
8. [The `main()` method explained word by word](#8-the-main-method-explained-word-by-word)
9. [`String[] args` and command line arguments](#9-string-args-and-command-line-arguments)
10. [Bonus: static nested classes and static import](#10-bonus-static-nested-classes-and-static-import)
11. [Interview questions with answers](#11-interview-questions-with-answers)
12. [Cheat sheet and common mistakes](#12-cheat-sheet-and-common-mistakes)

---

## 1. The problem that `static` solves

Imagine a `Student` class where every student studies in the same school.

```java
class Student {
    String name;
    int rollNo;
    String schoolName = "Sunrise Public School";   // same for EVERY student
}
```

If you create 1000 students, the string reference `schoolName` is stored **1000 times**, once inside every object. If the school renames itself, you have to update 1000 objects. That is wasteful and error-prone.

```
 s1 [name, rollNo, schoolName]
 s2 [name, rollNo, schoolName]      <- same data copied in every object
 s3 [name, rollNo, schoolName]
```

What we want: **one copy of `schoolName` shared by all students.**

```
                 +-------------------+
                 | schoolName (ONE)  |   <- belongs to the CLASS
                 +-------------------+
                    ^      ^      ^
                    |      |      |
 s1 [name, rollNo]  |  s2 [name, rollNo]  s3 [name, rollNo]
```

That is exactly what the `static` keyword does: it makes a member belong to the **class** instead of to each **object**.

---

## 2. Static variables

### 2.1 Definition

A **static variable** (also called a **class variable**) is declared with `static`. There is **one copy per class**, shared by all objects, and it exists even if you never create an object.

```java
class Student {
    String name;                                        // instance variable (per object)
    int rollNo;                                         // instance variable (per object)
    static String schoolName = "Sunrise Public School"; // static variable (per class)

    Student(String name, int rollNo) {
        this.name = name;
        this.rollNo = rollNo;
    }
}

public class Main {
    public static void main(String[] args) {
        Student s1 = new Student("Rahul", 1);
        Student s2 = new Student("Priya", 2);

        System.out.println(s1.schoolName);   // Sunrise Public School
        System.out.println(s2.schoolName);   // Sunrise Public School

        Student.schoolName = "Moonlight Academy";   // change it ONCE via the class name

        System.out.println(s1.schoolName);   // Moonlight Academy
        System.out.println(s2.schoolName);   // Moonlight Academy
    }
}
```

### 2.2 Memory picture

```
 CLASS-LEVEL MEMORY                          HEAP
 (loaded once when class is first used)
+-------------------------------+     +----------------------+
| Student class                 |     | s1 object            |
|   static schoolName ----------+--+  |   name = "Rahul"     |
+-------------------------------+  |  |   rollNo = 1         |
                                   |  +----------------------+
 STACK                             |  +----------------------+
+----------+                       |  | s2 object            |
| s1 -----------------------------+|  |   name = "Priya"     |
| s2 ---------------------------+  ||  |   rollNo = 2         |
+----------+                       ||  +----------------------+
                                   ||
 static variable is stored ONCE, outside any object.
 Instance variables are stored INSIDE each object.
```

> Technical note: in modern JVMs (Java 8+) static fields are stored with the class's runtime representation (the `Class` object on the heap), while class metadata lives in **Metaspace**. For exams and interviews, the safe statement is: *"static variables are stored once per class, in class-level memory, not inside objects."*

### 2.3 How to access a static variable

```java
Student.schoolName      // BEST: through the class name
s1.schoolName           // works, but misleading (compiler may warn)
```

Always use `ClassName.staticMember`. Accessing through an object makes readers think it belongs to that object.

### 2.4 Classic example: object counter

```java
class Employee {
    static int count = 0;     // shared counter
    int id;                   // unique per object

    Employee() {
        count++;              // every new object increments the SAME counter
        id = count;
    }
}

public class Main {
    public static void main(String[] args) {
        new Employee();
        new Employee();
        Employee e3 = new Employee();

        System.out.println(Employee.count);  // 3
        System.out.println(e3.id);           // 3
    }
}
```

```
count (shared):  0 -> 1 -> 2 -> 3
e1.id = 1, e2.id = 2, e3.id = 3     (each object keeps its own id)
```

Without `static`, each object would have its own `count` starting from 0 and the result would always be 1.

### 2.5 When is a static variable created?

- When the class is **loaded** by the JVM (first use of the class: creating an object, calling a static method, or accessing a non-constant static field).
- It is initialized **once**, with default values first (`0`, `false`, `null`), then the declared initializer or static block runs.
- It lives until the class is unloaded (usually until the program ends).

### 2.6 Instance variable vs static variable

| | Instance variable | Static variable |
|---|---|---|
| Belongs to | Each object | The class |
| Copies | One per object | Exactly one per class |
| Created when | Object is created (`new`) | Class is loaded |
| Needs an object to access? | Yes | No, use `ClassName.var` |
| Memory | Inside the object (heap) | Class-level memory, shared |
| Typical use | Data specific to an object (name, age) | Shared data (counter, school name, config) |

### 2.7 Local variables cannot be static

```java
void test() {
    static int x = 5;   // COMPILE ERROR: illegal start of expression
}
```

`static` means "belongs to the class". A local variable belongs to one method call, so it makes no sense.

---

## 3. Static methods and their rules

### 3.1 Definition

A **static method** belongs to the class. You can call it **without creating an object**.

```java
class MathUtil {
    static int square(int n) {
        return n * n;
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println(MathUtil.square(5));   // 25, no object needed
    }
}
```

You have already used static methods: `Math.max()`, `Math.sqrt()`, `Integer.parseInt()`, `Arrays.sort()`, `String.valueOf()`.

### 3.2 Instance method vs static method

```java
class Demo {
    int value = 10;

    void instanceMethod() {          // needs an object
        System.out.println("instance " + value);
    }

    static void staticMethod() {     // does not need an object
        System.out.println("static");
    }
}

Demo d = new Demo();
d.instanceMethod();     // must use an object
Demo.staticMethod();    // use the class name
```

### 3.3 The rules (and WHY each rule exists)

The key idea behind all rules: **a static method can run when no object exists at all.** So it cannot assume that any object's data is available.

#### Rule 1: A static method can directly call only other static methods

```java
class Demo {
    void show() { System.out.println("show"); }
    static void hello() { System.out.println("hello"); }

    static void test() {
        hello();      // OK, static calls static
        show();       // ERROR: non-static method show() cannot be referenced from a static context
    }
}
```

Why? `show()` is an instance method. It needs an object to run on. Inside `test()` there is no object, so Java does not know *which* object's `show()` to call.

**How to call it anyway:** create an object.

```java
static void test() {
    Demo d = new Demo();
    d.show();     // OK, now we have an object
}
```

#### Rule 2: A static method cannot directly access instance variables

```java
class Demo {
    int x = 10;           // instance variable
    static int y = 20;    // static variable

    static void test() {
        System.out.println(y);    // OK
        System.out.println(x);    // ERROR: non-static variable x cannot be referenced from a static context
    }
}
```

Why? `x` lives inside objects. With 5 objects there are 5 different `x` values. A static method has no idea which object's `x` you mean.

Again, with an object it is fine:

```java
static void test() {
    Demo d = new Demo();
    System.out.println(d.x);   // OK
}
```

#### Rule 3: A static method cannot use `this` or `super`

`this` means "the current object". A static method has **no current object**.

```java
static void test() {
    System.out.println(this.x);   // ERROR: non-static variable this cannot be referenced from a static context
}
```

#### Rule 4: An instance method CAN use static members freely

```java
class Demo {
    static int count = 0;
    int id;

    void show() {
        System.out.println(count);   // OK: static data always exists
        helper();                    // OK
    }
    static void helper() { }
}
```

Why is this direction allowed? Whenever an object exists, the class already exists too, so static members are always available.

#### Summary diagram of what can access what

```
                       can access
 Instance method  -----------------> instance members   YES
 Instance method  -----------------> static members     YES

 Static method    -----------------> static members     YES
 Static method    -----------------> instance members   NO  (unless through an object)
 Static method    -----------------> this / super       NO
```

| From \ To | Static var/method | Instance var/method |
|---|---|---|
| **Static method** | Yes | No (needs an object) |
| **Instance method** | Yes | Yes |

#### Rule 5: Static methods are not overridden, they are hidden

```java
class A { static void show() { System.out.println("A"); } }
class B extends A { static void show() { System.out.println("B"); } }

A obj = new B();
obj.show();    // prints A  (decided by the reference TYPE at compile time)
```

Overriding (runtime polymorphism) works on **objects**. Static methods belong to classes, so the method is chosen by the reference type, not by the actual object. This is called **method hiding**.

#### Rule 6: A static method cannot be `abstract`

An abstract method has no body and must be overridden, but static methods can't be overridden. Contradiction, so it is not allowed.

### 3.4 When should you make a method static?

Make it static when the method **does not depend on any object's data**:

- Utility/helper methods (`Math.pow`, `Collections.sort`).
- Factory methods (`LocalDate.of(...)`, `List.of(...)`).
- Methods that only work with their parameters.
- The `main` method (see Section 8).

Do **not** make a method static if it needs to read or change instance fields.

---

## 4. Static blocks

### 4.1 Definition

A **static block** is a block of code marked `static { ... }` that runs **once, when the class is loaded**, before any object is created and before `main` starts (for the class that contains main).

```java
class Config {
    static int maxUsers;
    static String appName;

    static {
        System.out.println("Static block running...");
        maxUsers = 100;
        appName = "MyApp";
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("main started");
        System.out.println(Config.maxUsers);
        System.out.println(Config.appName);
    }
}
```

Output:

```
main started
Static block running...
100
MyApp
```

Notice that `Config`'s static block ran when `Config` was **first used** (at `Config.maxUsers`), not when the program started, because `Config` is a separate class that loads lazily.

### 4.2 Why do we need a static block?

A plain declaration (`static int x = 5;`) is fine for simple values. A static block is for **complex initialization** that needs multiple statements, loops, try/catch, or calculations:

```java
class Primes {
    static int[] firstFive = new int[5];

    static {
        int count = 0, num = 2;
        while (count < 5) {
            boolean isPrime = true;
            for (int i = 2; i * i <= num; i++) {
                if (num % i == 0) { isPrime = false; break; }
            }
            if (isPrime) firstFive[count++] = num;
            num++;
        }
    }
}
// Primes.firstFive = [2, 3, 5, 7, 11]
```

Other real uses: loading a database driver, reading configuration, filling a static `Map`.

```java
class Country {
    static Map<String, String> capitals = new HashMap<>();
    static {
        capitals.put("India", "Delhi");
        capitals.put("France", "Paris");
    }
}
```

### 4.3 Rules for static blocks

1. Runs **only once** per class (at class loading).
2. Runs **before** the constructor and before any static method call on that class.
3. You can write **multiple** static blocks. They run **top to bottom** in the order they appear.
4. It can access only **static** members directly (no `this`).
5. It cannot `return` a value and cannot be called manually.
6. If a static block throws an unchecked exception, the class fails to initialize (`ExceptionInInitializerError`).

```java
class Demo {
    static { System.out.println("Block 1"); }
    static int x = print("x initialized");
    static { System.out.println("Block 2"); }

    static int print(String s) { System.out.println(s); return 1; }
}
// Output order: Block 1 -> x initialized -> Block 2   (textual order)
```

> Old trick: in Java 6 and earlier a program with only a static block could print output without `main`. From Java 7 onward the JVM checks for `main` first, so this no longer works. Do not rely on it.

### 4.4 When does class loading happen?

A class is loaded and its static initialization runs when it is **first actively used**:

- creating an instance with `new`,
- calling a static method,
- reading/writing a non-constant static field.

It does **not** run just because you declare a variable `Config c;` (no object yet).

---

## 5. Order of execution: static block, instance block, constructor

Besides static blocks, Java also has **instance initializer blocks** (`{ ... }` without `static`) that run **every time an object is created, before the constructor body**.

```java
class Demo {
    static { System.out.println("1. static block"); }

    { System.out.println("2. instance block"); }

    Demo() { System.out.println("3. constructor"); }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("main start");
        new Demo();
        new Demo();
    }
}
```

Output:

```
main start
1. static block        <- once, when Demo is first loaded
2. instance block      <- for object 1
3. constructor
2. instance block      <- for object 2
3. constructor
```

```
Class load (once)         Each  new Demo()
+----------------+        +----------------+   +---------------+
| static blocks  |  --->  | instance block |-->| constructor   |
| static vars    |        +----------------+   +---------------+
+----------------+
```

### With inheritance

```java
class Parent {
    static { System.out.println("Parent static"); }
    { System.out.println("Parent instance"); }
    Parent() { System.out.println("Parent constructor"); }
}
class Child extends Parent {
    static { System.out.println("Child static"); }
    { System.out.println("Child instance"); }
    Child() { System.out.println("Child constructor"); }
}

new Child();
```

Output:

```
Parent static
Child static
Parent instance
Parent constructor
Child instance
Child constructor
```

Rule: **all static first (parent then child), then for the object: parent instance block + constructor, then child instance block + constructor.**

---

## 6. The `final` keyword

`final` means **"cannot be changed after it is set"**. It can be applied to variables, methods and classes, each with a different meaning.

| Applied to | Meaning |
|---|---|
| Variable | Value can be assigned only once (becomes a constant) |
| Method | Cannot be overridden in a subclass |
| Class | Cannot be extended (no subclass) |

### 6.1 final variables

```java
final int MAX = 100;
MAX = 200;      // COMPILE ERROR: cannot assign a value to final variable MAX
```

#### Three kinds of final variables

**(a) Final with initial value at declaration**

```java
final double PI = 3.14159;
```

**(b) Blank final (assigned later, exactly once)**

A final **instance** variable without an initial value must be assigned in every constructor (or an instance block):

```java
class Student {
    final int rollNo;           // blank final

    Student(int rollNo) {
        this.rollNo = rollNo;   // assigned once in the constructor
    }
}

Student s1 = new Student(1);
Student s2 = new Student(2);    // different final value for each object: allowed
s1.rollNo = 5;                  // ERROR: cannot be changed after the constructor
```

If you forget to assign it, the compiler says "variable rollNo might not have been initialized".

A final **local variable** can also be blank and assigned later, but only once:

```java
final int result;
if (flag) result = 1; else result = 2;   // OK, exactly one assignment on each path
```

**(c) Final parameter**

```java
void print(final int x) {
    x = 10;    // ERROR: the parameter cannot be reassigned
}
```

### 6.2 `final` with primitives vs references (important!)

For **primitives**, final means the **value** cannot change.

For **references**, final means the **reference (address) cannot change**, but the **object it points to can still be modified**.

```java
final StringBuilder sb = new StringBuilder("Hi");
sb.append(" there");          // OK: modifying the object
sb = new StringBuilder("X");  // ERROR: cannot point to another object
```

```
final StringBuilder sb

 sb ----------> [ "Hi there" ]     the arrow is locked, the object's contents are not
        X-----> [ "X" ]            re-pointing is NOT allowed
```

Same with arrays:

```java
final int[] arr = {1, 2, 3};
arr[0] = 99;              // OK
arr = new int[]{4, 5};    // ERROR
```

And with your own objects:

```java
final Student s = new Student("Rahul", 1);
s.name = "Priya";          // OK (field is not final)
s = new Student("A", 2);   // ERROR
```

> Final reference is **not** the same as an immutable object. To make the contents unchangeable you need an immutable class (like `String`) or an unmodifiable collection.

### 6.3 final methods

A final method **cannot be overridden** by child classes. Use it to protect critical logic.

```java
class Bank {
    final void verifyIdentity() {
        System.out.println("Identity verified by bank rules");
    }
}

class SpecialBank extends Bank {
    void verifyIdentity() { }   // ERROR: cannot override final method
}
```

Notes:
- A final method **can still be inherited and called** by children. It just cannot be changed.
- A `private` method is not inherited, so marking it final is redundant.
- A static method can be final (prevents hiding).
- Constructors cannot be final (they are not inherited anyway).

### 6.4 final classes

A final class **cannot be extended**.

```java
final class Vehicle { }
class Car extends Vehicle { }    // ERROR: cannot inherit from final Vehicle
```

Famous final classes: `String`, `Integer` and the other wrappers, `Math`, `System`. They are final for **security and immutability** (nobody can create a "fake" `String` subclass that changes behavior).

Methods inside a final class are effectively final too, but its fields are not automatically final.

### 6.5 Compile-time constants

```java
final int x = 10;           // value known at compile time -> compile-time constant
```

The compiler can replace uses of `x` directly with `10` (this is called inlining).

---

## 7. `static final`: real constants

### 7.1 Why combine them?

`final` alone gives each object its own constant copy:

```java
class Circle {
    final double PI = 3.14159;   // stored in EVERY Circle object (wasteful)
}
```

`static final` gives **one shared constant** for the whole class:

```java
class Circle {
    static final double PI = 3.14159;   // ONE copy, cannot change
}

double area = Circle.PI * r * r;
```

```
 static final  =  shared by all objects (static)  +  cannot change (final)
```

You already use these: `Math.PI`, `Math.E`, `Integer.MAX_VALUE`, `Integer.MIN_VALUE`, `Long.MAX_VALUE`.

### 7.2 Naming convention

Constants are written in **UPPER_SNAKE_CASE**:

```java
public static final int MAX_RETRY_COUNT = 3;
public static final String DEFAULT_CURRENCY = "INR";
public static final double TAX_RATE = 0.18;
```

### 7.3 How to initialize a static final variable

It **must** be assigned exactly once, either at declaration or in a **static block**:

```java
class A {
    static final int X = 10;          // at declaration
}

class B {
    static final int Y;               // blank static final
    static {
        Y = computeValue();           // in a static block
    }
    static int computeValue() { return 42; }
}

class C {
    static final int Z;               // never assigned
}   // COMPILE ERROR: variable Z not initialized in the default constructor / static initializer
```

(You cannot assign a static final in a constructor, because constructors run per object, but a static final belongs to the class.)

### 7.4 Putting everything together

```java
class Account {
    static final double INTEREST_RATE = 4.5;   // shared + unchangeable
    static int totalAccounts = 0;              // shared + changeable
    final int accountNo;                       // per object + unchangeable
    double balance;                            // per object + changeable

    Account(int accountNo) {
        this.accountNo = accountNo;
        totalAccounts++;
    }
}
```

| Member | Per object or shared? | Changeable? |
|---|---|---|
| `INTEREST_RATE` | Shared | No |
| `totalAccounts` | Shared | Yes |
| `accountNo` | Per object | No (after constructor) |
| `balance` | Per object | Yes |

### 7.5 Constants and class loading (small interview detail)

Accessing a **compile-time constant** like `static final int X = 10;` does **not** trigger the class's static block, because the compiler inlines the value into the caller. Accessing a non-constant like `static final int Y = new Random().nextInt();` does trigger class initialization.

---

## 8. The `main()` method explained word by word

```java
public static void main(String[] args) {
    System.out.println("Hello");
}
```

The JVM looks for **exactly this signature** to start your program. Each word has a reason:

```
   public     static     void      main      (String[] args)
     |          |          |         |              |
  JVM must   no object   returns   fixed name   command line
  access it  needed      nothing   JVM looks    arguments
  from        to call    to JVM    for
  outside
```

| Word | Why it is there |
|---|---|
| `public` | The JVM is **outside** your class/package. It needs unrestricted access to call `main`. |
| `static` | The JVM must call `main` **without creating an object** of your class. |
| `void` | The JVM does not expect a return value (use `System.exit(code)` to give an exit status). |
| `main` | The fixed name the JVM searches for as the entry point. |
| `String[] args` | Holds command line arguments passed when starting the program. |

### 8.1 Why must `main` be static? (the key question)

Think about the starting moment of a program:

1. You run `java Main`.
2. The JVM loads the class `Main`.
3. **No object of `Main` exists yet.** Nobody ran `new Main()`.
4. The JVM needs to call some method to start.

If `main` were non-static, the JVM would have to do `new Main()` first. But then:
- Which constructor should it call? What if the constructor needs parameters?
- What if the class is abstract or has a private constructor?
- Constructors might run code before `main`, which is confusing.

A **static** method belongs to the class, so the JVM can call it directly with `Main.main(args)` with **no object**. That is the simple and universal solution.

```
 java Main
    |
    v
 JVM loads class Main  -->  calls Main.main(args)    (no object, no constructor)
```

### 8.2 What happens if you remove a word?

| Change | Result |
|---|---|
| Remove `static` | Program compiles, but at run time: *"Error: Main method is not static in class Main"* (in older Java versions; see the note below on newer Java) |
| Remove `public` | Run time error: *"Main method not found / must be public"* |
| Change `void` to `int` | Run time error: *"Main method must return a value of type void"* |
| Rename to `Main2` | Run time error: *"Main method not found in class"* |
| Wrong parameter type | Run time error: main method not found |

### 8.3 Allowed variations

```java
public static void main(String[] args)      // standard
public static void main(String... args)     // varargs, same thing
public static void main(String args[])      // C-style array, works
static public void main(String[] args)      // modifier order doesn't matter
public static void main(final String[] args)// final allowed
public final static void main(String[] args)// final allowed
```

The parameter **name** (`args`) can be anything: `String[] a`, `String[] arguments`.

### 8.4 You can overload `main`

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Real main");
        main(5);                       // calling the overloaded one manually
    }

    public static void main(int n) {
        System.out.println("Overloaded main " + n);
    }
}
```

The JVM only calls the `String[]` version. Others are ordinary methods.

### 8.5 Newer Java versions

Recent Java versions (finalized in Java 25) added a simplified way to start programs: instance `main` methods and "compact source files" where `main` does not have to be `public static` or even inside an explicit class. This is meant to make learning easier. For your course, interviews and most codebases, the classic `public static void main(String[] args)` is still what you must know and write.

---

## 9. `String[] args` and command line arguments

### 9.1 What is it?

`args` is an array of `String` that holds the **values you type after the program name** when launching it.

```
java Main  Rahul  25  Delhi
           ^^^^^  ^^  ^^^^^
           args[0] args[1] args[2]
```

### 9.2 Example

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Number of arguments: " + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "] = " + args[i]);
        }
    }
}
```

Compile and run:

```
javac Main.java
java Main Rahul 25 Delhi
```

Output:

```
Number of arguments: 3
args[0] = Rahul
args[1] = 25
args[2] = Delhi
```

```
args  -->  [ ref | ref | ref ]
             |     |     |
             v     v     v
          "Rahul" "25"  "Delhi"
```

Points to remember:

- Every argument is a **String**, even if it looks like a number. `args[1]` is `"25"`, not `25`.
- Arguments are separated by **spaces**. To pass a value with spaces, wrap it in quotes: `java Main "Rahul Sharma" 25` gives 2 arguments.
- If you pass no arguments, `args` is an **empty array** (`args.length == 0`), **not** `null`.

### 9.3 Converting arguments to numbers

```java
public class Add {
    public static void main(String[] args) {
        int a = Integer.parseInt(args[0]);
        int b = Integer.parseInt(args[1]);
        System.out.println("Sum = " + (a + b));
    }
}
```

```
java Add 10 20
Sum = 30
```

### 9.4 Common errors

```
java Add                 -> ArrayIndexOutOfBoundsException (no args[0])
java Add 10 hello        -> NumberFormatException (cannot parse "hello")
```

Safe version:

```java
public static void main(String[] args) {
    if (args.length < 2) {
        System.out.println("Usage: java Add <num1> <num2>");
        return;
    }
    try {
        int a = Integer.parseInt(args[0]);
        int b = Integer.parseInt(args[1]);
        System.out.println("Sum = " + (a + b));
    } catch (NumberFormatException e) {
        System.out.println("Both arguments must be integers");
    }
}
```

### 9.5 How to pass arguments in an IDE

- **IntelliJ IDEA:** Run menu, *Edit Configurations*, then type values in the **Program arguments** box.
- **Eclipse:** Run Configurations, *Arguments* tab, **Program arguments**.
- **VS Code:** in `launch.json` add `"args": ["Rahul", "25"]`.

### 9.6 Why is `args` an array of Strings?

The command line is just text. The operating system hands the JVM raw text pieces, so a `String[]` is the most general container. You decide how to convert each piece (to int, double, boolean, file path...). It lets the same program behave differently without recompiling, for example `java Server 8080` vs `java Server 9090`.

### 9.7 `args` vs `Scanner`

| | `String[] args` | `Scanner` |
|---|---|---|
| When is data given? | **Before** the program starts | **While** the program is running |
| Interaction | None, fixed at launch | Interactive |
| Use for | Config, file names, ports, flags | User input during execution |

---

## 10. Bonus: static nested classes and static import

### 10.1 Static nested class

A class declared `static` **inside another class** is a **static nested class**. It does not need an outer object.

```java
class Outer {
    static int a = 10;
    int b = 20;

    static class Nested {
        void show() {
            System.out.println(a);      // OK: static member of Outer
            // System.out.println(b);   // ERROR: needs an Outer object
        }
    }
}

Outer.Nested n = new Outer.Nested();   // no Outer object needed
n.show();
```

Only **nested** classes can be static. A top-level class cannot be declared `static`. Common real use: the **Builder pattern** (`static class Builder`) and `Map.Entry`.

### 10.2 Static import

```java
import static java.lang.Math.*;

public class Main {
    public static void main(String[] args) {
        System.out.println(sqrt(16));   // no "Math." needed
        System.out.println(PI);
    }
}
```

Convenient, but use sparingly: too many static imports make it unclear where a name comes from.

---

## 11. Interview questions with answers

**Q1. What is the `static` keyword?**
It makes a member (variable, method, block, nested class) belong to the class instead of to individual objects. One copy is shared and it can be used without creating an object.

**Q2. Where are static variables stored and when are they created?**
They are stored once per class in class-level memory (not inside objects) and created when the class is loaded.

**Q3. Why can't a static method access instance variables directly?**
A static method can run without any object, so there is no object whose instance variables it could use. You must create or receive an object and access the variable through it.

**Q4. Can a static method use `this`?**
No. `this` refers to the current object, and a static method has none.

**Q5. Can we override a static method?**
No. Declaring the same static method in a child class **hides** the parent's method. The method called depends on the reference type, not the object.

**Q6. What is a static block and when does it run?**
A block marked `static { }` used for complex initialization of static variables. It runs once, when the class is loaded, before any object is created.

**Q7. Can there be multiple static blocks?**
Yes. They run in the order they appear in the class.

**Q8. What is the order: static block, instance block, constructor?**
Static block (once per class), then for each object: instance block, then constructor. With inheritance: parent static, child static, parent instance + constructor, child instance + constructor.

**Q9. What does `final` do on a variable, method and class?**
Variable: can be assigned only once. Method: cannot be overridden. Class: cannot be extended.

**Q10. If a reference variable is `final`, can I change the object's data?**
Yes. `final` locks only the reference (it cannot point to another object). The object's fields can still be changed unless the object is immutable.

**Q11. What is a blank final variable?**
A final variable declared without a value, which must then be assigned exactly once, in the constructor for instance variables or in a static block for static variables.

**Q12. Why do we write `static final` for constants?**
`static` gives a single shared copy instead of one per object, and `final` makes sure it cannot change. Example: `Math.PI`.

**Q13. Why is `main` static?**
So the JVM can call it without creating an object of the class (at startup no object exists).

**Q14. Why is `main` public?**
The JVM is outside the class and package, so the method must be accessible to it from anywhere.

**Q15. What happens if `main` is not static?**
In the classic setup, the code compiles but the JVM reports that the main method is not static, and the program will not start.

**Q16. Can we overload `main`? Can we override it?**
Overload: yes, but the JVM only starts with the `String[]` version. Override: static methods are hidden, not overridden.

**Q17. What is `String[] args`?**
An array holding command line arguments passed to the program at launch. Every element is a String. With no arguments it is an empty array, not `null`.

**Q18. Can a constructor be static or final?**
No. A constructor belongs to object creation and is not inherited, so neither modifier applies.

**Q19. Can a class be both `abstract` and `final`?**
No. Abstract needs to be extended; final forbids extending.

**Q20. Is `final`, `finally` and `finalize` the same?**
No. `final` is a keyword (constant/no override/no inherit), `finally` is a block in try-catch that always runs, and `finalize()` is an old method of `Object` called before garbage collection (deprecated, avoid it).

---

## 12. Cheat sheet and common mistakes

### One-page summary

```
STATIC  = belongs to the CLASS (one shared copy, no object needed)
   static variable : shared data (counter, school name)
   static method   : no object needed; can't use this / instance members directly
   static block    : runs once when the class loads (complex static init)

FINAL   = cannot change
   final variable  : assigned once (reference can't move; object may still change)
   final method    : cannot be overridden
   final class     : cannot be extended

STATIC FINAL = shared constant (UPPER_CASE), e.g. Math.PI

main()  = public static void main(String[] args)
   public : JVM calls from outside
   static : no object needed at startup
   void   : nothing returned to JVM
   args   : command line arguments (all Strings)
```

### Access rules

```
Static method   -> static members      YES
Static method   -> instance members    NO (use an object)
Static method   -> this / super        NO
Instance method -> everything          YES
```

### Execution order

```
1. Static variables / static blocks     (once, at class load, top to bottom)
2. Instance variables / instance blocks (every new object)
3. Constructor body                     (every new object)
```

### Common mistakes

| Mistake | Fix |
|---|---|
| Calling an instance method from `main` directly | Create an object first: `new Demo().show();` |
| Using `this` inside a static method | Pass an object as a parameter or create one |
| Thinking `final` makes an object immutable | It only locks the reference; use immutable classes for that |
| Using `static` for data that should differ per object | Static data is shared by **all** objects, so use an instance variable |
| Forgetting to initialize a blank `final` | Assign it in every constructor (or static block for static final) |
| Expecting `args[0]` to be an `int` | Everything is a String; use `Integer.parseInt(...)` |
| Not checking `args.length` | Check before indexing to avoid `ArrayIndexOutOfBoundsException` |
| Accessing static members through an object | Use `ClassName.member` for clarity |
| Writing constants in lowercase | Use `UPPER_SNAKE_CASE` for `static final` constants |
| Overusing static (global mutable state) | Static mutable state makes testing and multithreading harder, so prefer instance state unless sharing is truly needed |

### One-line takeaways

1. `static` = **class-level**, one shared copy, accessible without an object.
2. A static method can run with **no object**, so it cannot touch instance data or `this`.
3. A static block runs **once** when the class loads.
4. `final` = **assign once** (variable), **no override** (method), **no inherit** (class).
5. `static final` = **constant** shared by all.
6. `main` is `static` so the JVM can start the program **without creating an object**.
7. `String[] args` carries command line input, always as **Strings**.

---
*Practice tip: write one class that has a static variable, a static block, an instance block, a constructor and a static final constant. Print from each and run it. Seeing the order of output yourself makes it stick.*
