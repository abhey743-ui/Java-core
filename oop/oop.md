# Java OOP Basics: What Is OOP, Classes, Objects and the Heap

> This guide covers **only the foundation**: what OOP is, why we use it, its pros and cons, how to **write a class**, how to **name** it, and what happens in the **heap** when you create an object and access its data.
> Inheritance, polymorphism and the other big OOP topics are **not** covered here. They come later.

---

## Table of Contents

1. [What Is OOP?](#1-what-is-oop)
2. [The Problem OOP Solves (Procedural vs OOP)](#2-the-problem-oop-solves-procedural-vs-oop)
3. [The Four Pillars (Names Only)](#3-the-four-pillars-names-only)
4. [Benefits of OOP](#4-benefits-of-oop)
5. [Disadvantages of OOP](#5-disadvantages-of-oop)
6. [Class vs Object](#6-class-vs-object)
7. [How to Define a Class in Java](#7-how-to-define-a-class-in-java)
8. [Class Naming Conventions and Rules](#8-class-naming-conventions-and-rules)
9. [Creating Objects with `new`](#9-creating-objects-with-new)
10. [What Happens in Memory: The Object in the Heap](#10-what-happens-in-memory-the-object-in-the-heap)
11. [How We Access Data: The Dot Operator](#11-how-we-access-data-the-dot-operator)
12. [Controlling Access: private, getters and setters](#12-controlling-access-private-getters-and-setters)
13. [Instance Data vs Static (Class) Data](#13-instance-data-vs-static-class-data)
14. [Full Practical Example](#14-full-practical-example)
15. [Common Mistakes](#15-common-mistakes)
16. [Summary and Quiz](#16-summary-and-quiz)

---

## 1. What Is OOP?

**OOP = Object-Oriented Programming.**

It is a way of writing programs where you build the program from **objects**. An **object** bundles together:

- **Data** (what it *has*), called **state**, stored in **fields** (variables).
- **Actions** (what it *can do*), called **behavior**, written as **methods** (functions).

### Real-life example: a Car

```
        CAR (object)
 ┌─────────────────────────┐
 │ DATA (state)            │
 │   color  = "red"        │
 │   speed  = 60           │
 │   fuel   = 40           │
 ├─────────────────────────┤
 │ ACTIONS (behavior)      │
 │   accelerate()          │
 │   brake()               │
 │   refuel()              │
 └─────────────────────────┘
```

A car **has** a color, speed and fuel, and it **can** accelerate, brake and refuel. In OOP, you model things from the real world (a student, a bank account, a book) **exactly like this**: data and actions together in one unit.

### The one-sentence definition

> **OOP is a style of programming where you model your program as a collection of objects, each holding its own data and the methods that work on that data.**

Java is an **object-oriented language**: almost everything you write lives inside a **class**.

> Small honest note: Java is *mostly* object-oriented, not 100% pure. The 8 primitive types (`int`, `double`, etc.) are not objects, and `static` members belong to classes rather than objects.

---

## 2. The Problem OOP Solves (Procedural vs OOP)

### 2.1 The old way: procedural programming

In **procedural** programming, you keep **data** in one place and **functions** in another. The functions take the data as inputs.

Imagine storing 3 students:

```java
String[] names = {"Ali", "Sara", "John"};
int[]    ages  = {20, 22, 21};
double[] gpas  = {3.6, 3.2, 3.9};

static void printStudent(String name, int age, double gpa) {
    System.out.println(name + ", " + age + ", " + gpa);
}

printStudent(names[0], ages[0], gpas[0]);
```

Problems:

- The data of **one student** is scattered over three arrays. Nothing says they belong together.
- If you add a new property (like `course`), you add a new array and change every function.
- If one array is sorted and the others are not, the data is **silently corrupted**.
- Any function can change any data. It is hard to protect it.

### 2.2 The OOP way: bundle them

```java
class Student {
    String name;
    int age;
    double gpa;

    void print() {
        System.out.println(name + ", " + age + ", " + gpa);
    }
}
```

Now **one student = one object**. The data and the actions that use it **travel together**.

```
 PROCEDURAL                              OOP
 ┌────────────────────┐                  ┌───────────────┐ ┌───────────────┐
 │ names[]  ages[]    │                  │ Student       │ │ Student       │
 │ gpas[]             │  ◄── separate    │  name="Ali"   │ │  name="Sara"  │
 └────────────────────┘      from        │  age=20       │ │  age=22       │
 ┌────────────────────┐                  │  gpa=3.6      │ │  gpa=3.2      │
 │ printStudent()     │  ◄── functions   │  print()      │ │  print()      │
 │ calculateGrade()   │                  └───────────────┘ └───────────────┘
 └────────────────────┘                  data + actions together, one per object
```

---

## 3. The Four Pillars (Names Only)

OOP rests on four main ideas. We are **not** diving into them here; this is only so you recognize the names.

| Pillar | One-line meaning |
|--------|------------------|
| **Encapsulation** | Keep data **inside** the object and control how it is accessed |
| **Abstraction** | Show **what** an object does, hide **how** it does it |
| **Inheritance** | A new class can **reuse and extend** an existing class |
| **Polymorphism** | The **same method name** can behave differently for different objects |

(Section 12 gives a tiny taste of encapsulation, since it directly affects how you access data.)

---

## 4. Benefits of OOP

| # | Benefit | Meaning |
|---|---------|---------|
| 1 | **Models the real world** | Students, cars, accounts become classes. The code mirrors how we think. |
| 2 | **Modularity** | The program is split into **separate classes**, each with one job. Easy to understand piece by piece. |
| 3 | **Reusability** | Write a class once, create **many objects** from it, and reuse it in other programs. |
| 4 | **Easy maintenance** | A bug or change is usually **inside one class**, so you fix it in one place. |
| 5 | **Data protection (security)** | Data can be **hidden** (`private`) so outside code can't corrupt it. |
| 6 | **Scalability** | Large programs are built by **combining objects**. Big teams can work on different classes at once. |
| 7 | **Easier testing and debugging** | Each class can be **tested separately**. |
| 8 | **Flexibility and extension** | New features can be added by **adding classes** rather than rewriting old code. |
| 9 | **Better teamwork** | Clear boundaries: one person writes `Account`, another writes `Customer`. |

---

## 5. Disadvantages of OOP

OOP is powerful, but it is **not free**.

| # | Disadvantage | Meaning |
|---|--------------|---------|
| 1 | **Steeper learning curve** | Classes, objects, references, access rules: a lot to learn before your first useful program. |
| 2 | **More code ("boilerplate")** | Even a simple task may need a class, fields, a constructor and getters. |
| 3 | **More memory use** | Every object has **extra overhead** (header, references). Many small objects cost more than raw primitives or arrays. |
| 4 | **Slightly slower** | Following references and creating/cleaning up objects costs time compared to plain data. |
| 5 | **Over-engineering** | It is easy to create **too many classes** and layers for a simple problem. |
| 6 | **Complex designs** | Poorly designed class structures become tangled and hard to understand. |
| 7 | **Needs upfront planning** | You must think about which classes exist and how they relate **before** coding. |
| 8 | **Not ideal for every problem** | Small scripts, simple calculations, or heavy numeric/data processing are often easier without lots of classes. |
| 9 | **Harder to trace the flow** | Program logic jumps between many objects and files. |

> **Balance:** for medium and large programs, the benefits usually win by a wide margin. For tiny programs, keep it simple.

---

## 6. Class vs Object

This is the **core idea** of the whole topic.

| | **Class** | **Object** |
|---|-----------|------------|
| What it is | A **blueprint / template / design** | A **real thing** built from the blueprint |
| Analogy | The architect's **house plan** | The actual **house** built from the plan |
| Exists as | Code you write | **Memory** in the heap while the program runs |
| How many | **One** class definition | **Many** objects from one class |
| Holds actual values? | No, it only describes the *kind* of data | **Yes**, each object has its **own** values |
| Created with | The `class` keyword | The `new` keyword |

```
              CLASS Student  (the blueprint)
              ┌──────────────────────────┐
              │ fields:  name, age, gpa  │
              │ methods: introduce()     │
              └─────────────┬────────────┘
                            │  new Student(...)  ← builds objects from the blueprint
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
   │ Object s1   │   │ Object s2   │   │ Object s3   │
   │ name="Ali"  │   │ name="Sara" │   │ name="John" │
   │ age=20      │   │ age=22      │   │ age=21      │
   │ gpa=3.6     │   │ gpa=3.2     │   │ gpa=3.9     │
   └─────────────┘   └─────────────┘   └─────────────┘
   Same structure, but each object has its OWN values
```

An object is also called an **instance** of the class. "Creating an object" = "instantiating the class".

---

## 7. How to Define a Class in Java

### 7.1 Basic syntax

```
[access modifier] class ClassName {

    // 1. Fields (variables that hold the data)

    // 2. Constructors (code that runs when an object is created)

    // 3. Methods (the actions)
}
```

Standard order inside a class: **fields first, then constructors, then methods**.

### 7.2 A complete class

```java
public class Student {

    // ---------- 1. FIELDS (state / data) ----------
    String name;
    int age;
    double gpa;

    // ---------- 2. CONSTRUCTOR ----------
    Student(String name, int age, double gpa) {
        this.name = name;
        this.age = age;
        this.gpa = gpa;
    }

    // ---------- 3. METHODS (behavior) ----------
    void introduce() {
        System.out.println("Hi, I'm " + name + ", " + age + " years old, GPA " + gpa);
    }

    boolean isHonorStudent() {
        return gpa >= 3.5;
    }
}
```

### 7.3 The three parts explained

| Part | What it is | Notes |
|------|-----------|-------|
| **Fields** (instance variables) | Variables declared in the class, **outside any method** | Each object gets **its own copy**. They get **default values** automatically (`0`, `false`, `null`). |
| **Constructor** | A special block that **runs when you create an object** with `new` | Same name as the class, **no return type**. Used to set the starting values. |
| **Methods** | Functions inside the class | They can use the object's fields directly. |

### 7.4 Constructor rules

```java
Student(String name, int age, double gpa) { ... }
```

1. The name is **exactly the class name**.
2. It has **no return type** (not even `void`).
3. It runs **automatically** when you write `new Student(...)`.
4. If you write **no constructor**, Java gives you an empty **default constructor** (no parameters). The moment you write **any** constructor, the default one disappears.
5. You can write **several** constructors with different parameters.

```java
class Student {
    String name;
    int age;

    Student() {                        // no-argument constructor
        name = "Unknown";
        age = 0;
    }

    Student(String name, int age) {    // another constructor
        this.name = name;
        this.age = age;
    }
}

Student a = new Student();                // Unknown, 0
Student b = new Student("Ali", 20);       // Ali, 20
```

### 7.5 What is `this`?

`this` means **"the current object"**: the one whose constructor or method is running.

```java
Student(String name, int age) {
    this.name = name;     // this.name = the FIELD of the object
                          //      name = the PARAMETER (the value passed in)
    this.age = age;
}
```

Without `this`, a parameter with the same name **hides** the field:

```java
Student(String name) {
    name = name;          // BUG: assigns the parameter to itself; the field stays null
}
```

### 7.6 Where does the class go? File rules

| Rule | Detail |
|------|--------|
| File extension | `.java` |
| **Public class = file name** | `public class Student` must be in **`Student.java`** (same spelling and capitalization) |
| One public class per file | You can't have two `public` top-level classes in one file |
| Non-public classes | Allowed in the same file, but one class per file is the clean habit |
| Top-level class access | Only **`public`** or **default (no keyword)**. `private`/`protected` are not allowed for top-level classes |
| `main` method | Lives in a class too; that class is what you run |

A typical small project:

```
 MyProject/
 ├── Student.java      ← public class Student { ... }
 └── Main.java         ← public class Main { public static void main(String[] args) { ... } }
```

Compile and run:

```
javac Student.java Main.java
java Main
```

### 7.7 Access modifier for the class

```java
public class Student { }     // visible to every other class
class Student { }            // default: visible only inside the same package
```

For now, use **`public class`** for your main classes.

---

## 8. Class Naming Conventions and Rules

### 8.1 The hard rules (the compiler enforces these)

A class name is an **identifier**, so:

1. Only **letters, digits, `_` and `$`**.
2. **Cannot start with a digit.**
3. **No spaces** or other symbols (`-`, `@`, `#`...).
4. Cannot be a **reserved word** (`class`, `int`, `new`...).
5. It is **case-sensitive**: `Student` and `student` are different names.
6. A public class's name **must match its file name**.

### 8.2 The conventions (every Java programmer follows these)

| Convention | Meaning | Example |
|------------|---------|---------|
| **Start with a capital letter** | First letter uppercase | `Student` ✓   `student` ✗ |
| **PascalCase (UpperCamelCase)** | Every new word starts with a capital, no underscores | `BankAccount` ✓   `Bank_Account` ✗   `bankAccount` ✗ |
| **Use nouns** | A class represents a *thing* | `Customer`, `Invoice`, `Book` |
| **Singular, not plural** | One class = one thing | `Car` ✓   `Cars` ✗ |
| **Meaningful, descriptive** | The name explains the purpose | `OrderProcessor` ✓   `OP` ✗   `Thing1` ✗ |
| **No abbreviations (usually)** | Spell it out | `CustomerAddress` ✓   `CustAddr` ✗ |
| **Treat acronyms like words** | Modern style | `HtmlParser` ✓ (also seen: `HTMLParser`) |
| **Don't reuse common Java names** | Avoid clashing with `String`, `Object`, `List`, `Math`, `Date`, `Thread` | `Student` ✓   `String` ✗ |

### 8.3 Good vs bad names

| Name | Verdict | Why |
|------|---------|-----|
| `Student` | ✓ Good | Capitalized noun, singular |
| `BankAccount` | ✓ Good | PascalCase |
| `ShoppingCart` | ✓ Good | Clear and descriptive |
| `student` | ✗ | Starts lowercase |
| `bank_account` | ✗ | Underscore, lowercase start |
| `Bank_Account` | ✗ | Underscore |
| `BANKACCOUNT` | ✗ | All caps (that style is for constants) |
| `Cars` | ✗ | Plural |
| `1Student` | ✗ **Won't compile** | Starts with a digit |
| `My Class` | ✗ **Won't compile** | Space |
| `class` | ✗ **Won't compile** | Reserved word |
| `Data` / `Manager` / `Stuff` | ✗ | Too vague |

### 8.4 Common naming patterns

| Pattern | Used for | Example |
|---------|----------|---------|
| `...Exception` | Exception classes | `InvalidAgeException` |
| `...Utils` / `...Helper` | Classes full of helper methods | `StringUtils` |
| `...Service`, `...Repository`, `...Controller` | Roles in larger applications | `PaymentService` |
| `...Test` | Test classes | `StudentTest` |
| `...Builder` | Builder classes | `PersonBuilder` |

### 8.5 Naming other things (quick reference)

| What | Style | Example |
|------|-------|---------|
| **Class / interface / enum / record** | `PascalCase` | `Student`, `Runnable` |
| **Fields, variables, methods** | `camelCase` | `studentName`, `calculateGpa()` |
| **Constants** (`static final`) | `UPPER_SNAKE_CASE` | `MAX_SIZE` |
| **Packages** | all lowercase | `com.example.school` |

Only the **class** is capitalized at the start. Everything else starts lowercase (except constants).

---

## 9. Creating Objects with `new`

### 9.1 Syntax

```
ClassName variableName = new ClassName(arguments);
```

```java
Student s1 = new Student("Ali", 20, 3.6);
Student s2 = new Student("Sara", 22, 3.2);
```

Break it into pieces:

```
   Student      s1       =      new       Student("Ali", 20, 3.6) ;
 ┌─────────┐ ┌──────┐         ┌─────┐  ┌──────────────────────────┐
  the type    the              create     call the constructor
  of the      reference        an object  with these arguments
  variable    variable         in the heap
```

### 9.2 What `new` does (4 steps)

```
 1. Finds free space in the HEAP for a new Student object
 2. Sets every field to its default value (null, 0, false...)
 3. Runs the constructor, which fills in the real values
 4. Returns a REFERENCE (address) to the new object
    → that reference is stored in the variable s1
```

### 9.3 Declaring and creating separately

```java
Student s;                         // 1. declare a reference variable (no object yet!)
s = new Student("Ali", 20, 3.6);   // 2. create the object, store its reference in s
```

After step 1 alone, `s` points to **nothing**. Using it would be an error.

### 9.4 Many objects from one class

```java
Student s1 = new Student("Ali", 20, 3.6);
Student s2 = new Student("Sara", 22, 3.2);
Student s3 = new Student("John", 21, 3.9);
```

Three separate objects, each with **its own** `name`, `age` and `gpa`. Changing one does not affect the others.

---

## 10. What Happens in Memory: The Object in the Heap

This is the key picture for understanding objects.

### 10.1 Two different places: stack and heap

| Place | What lives there |
|-------|------------------|
| **Stack** | Local variables of running methods, **including reference variables** (like `s1`) |
| **Heap** | **The objects themselves** (created by `new`) |

```java
Student s1 = new Student("Ali", 20, 3.6);
```

```
        STACK                                  HEAP
  ┌────────────────┐
  │ main()         │
  │                │                    ┌───────────────────────────┐
  │  s1 │   ●──────┼──────────────────► │ Student object            │
  │                │    (reference /    │  ┌────────────────────┐   │
  └────────────────┘     address)       │  │ object header      │   │
                                        │  ├────────────────────┤   │
                                        │  │ name  ●────────────┼───┼──► "Ali"  (a String object)
                                        │  │ age   20           │   │
                                        │  │ gpa   3.6          │   │
                                        │  └────────────────────┘   │
                                        └───────────────────────────┘
```

**The variable `s1` does not contain the object.** It only contains a **reference** (like an address or a remote control) to the object in the heap.

> This is only the mental model. The JVM can optimize some short-lived objects, but this picture is how you should think about it.

### 10.2 What is inside an object in the heap

| Part | Meaning |
|------|---------|
| **Object header** | A small fixed amount of bookkeeping data (roughly 12 to 16 bytes): which class it belongs to, lock info, GC info |
| **Fields** | The actual instance data. **Primitive** fields hold the values **directly** (`age = 20`). **Object** fields hold a **reference** to another object (`name` points to a String object) |
| **Padding** | Extra bytes so the object size is a multiple of 8 |

### 10.3 Where do the methods live?

**Methods are not copied into every object.** The code of `introduce()` is stored **once**, with the **class information** (in the JVM's class/metadata area). All objects of the class **share** that one copy.

```
   CLASS INFO (stored once)                 HEAP (one block per object)
  ┌────────────────────────────┐          ┌──────────────┐ ┌──────────────┐
  │ class Student              │ ◄─────── │ s1 object    │ │ s2 object    │
  │   field list: name,age,gpa │  each    │ name, age,   │ │ name, age,   │
  │   method code: introduce() │  object  │ gpa  (values)│ │ gpa (values) │
  │                isHonor...()│  knows   └──────────────┘ └──────────────┘
  └────────────────────────────┘  its class
```

So: **each object stores only its own data; the methods are shared.** This keeps objects small.

### 10.4 Two objects, two blocks

```java
Student s1 = new Student("Ali", 20, 3.6);
Student s2 = new Student("Sara", 22, 3.2);
```

```
        STACK                              HEAP
  ┌───────────────┐
  │ s1 │    ●─────┼──────────────► ┌──────────────────────────┐
  ├───────────────┤                │ name ●──► "Ali"          │
  │ s2 │    ●─────┼───────┐        │ age  20                  │
  └───────────────┘       │        │ gpa  3.6                 │
                          │        └──────────────────────────┘
                          │
                          └──────► ┌──────────────────────────┐
                                   │ name ●──► "Sara"         │
                                   │ age  22                  │
                                   │ gpa  3.2                 │
                                   └──────────────────────────┘
```

### 10.5 Copying a reference does NOT copy the object

```java
Student s1 = new Student("Ali", 20, 3.6);
Student s3 = s1;          // copies the REFERENCE, not the object
s3.name = "Ahmed";
System.out.println(s1.name);     // Ahmed  ← s1 sees the change
```

```
  STACK                         HEAP
  ┌──────────┐
  │ s1  ●────┼──┐        ┌──────────────────────┐
  ├──────────┤  ├──────► │ name ●──► "Ahmed"    │   ONE object,
  │ s3  ●────┼──┘        │ age  20              │   TWO references (two names)
  └──────────┘           │ gpa  3.6             │
                         └──────────────────────┘
```

To create a truly separate object, use `new` again (or write a copy method).

### 10.6 `null`: a reference pointing to nothing

```java
Student s = null;          // reference exists, but there is no object
s.introduce();             // NullPointerException! (nothing to follow)
```

```
  STACK                 HEAP
  ┌──────────┐
  │ s  null  │    (no arrow, no object)
  └──────────┘
```

### 10.7 What happens to objects nobody uses? Garbage collection

If **no reference** points to an object anymore, it is **unreachable** and the **Garbage Collector (GC)** frees its memory automatically.

```java
Student s = new Student("Ali", 20, 3.6);   // object created
s = new Student("Sara", 22, 3.2);          // s now points to the new one
                                           // the "Ali" object has no references → eligible for GC
```

```
  BEFORE                          AFTER s = new Student("Sara"...)
  s ──► [Ali object]              s ──► [Sara object]
                                        [Ali object]  ← no arrows pointing here: garbage
```

You never free objects yourself in Java (no `delete`), unlike C++.

### 10.8 Default values of fields

When `new` creates the object, fields get defaults **before** the constructor runs:

| Field type | Default |
|------------|---------|
| `byte`, `short`, `int`, `long` | `0` |
| `float`, `double` | `0.0` |
| `char` | `'\u0000'` |
| `boolean` | `false` |
| Any object type (`String`, arrays, other classes) | `null` |

```java
class Demo {
    int count;
    String text;
    boolean flag;
}

Demo d = new Demo();
System.out.println(d.count);   // 0
System.out.println(d.text);    // null
System.out.println(d.flag);    // false
```

(This applies to **fields**. A **local variable** inside a method gets **no** default value and must be assigned before use.)

---

## 11. How We Access Data: The Dot Operator

### 11.1 The dot `.`

To reach **inside** an object, use the **dot operator**:

```
 reference.fieldName          → read or write a field
 reference.methodName(...)    → call a method
```

```java
Student s1 = new Student("Ali", 20, 3.6);

// READ a field
System.out.println(s1.name);       // Ali
System.out.println(s1.age);        // 20

// WRITE a field
s1.age = 21;
s1.gpa = 3.8;

// CALL a method
s1.introduce();                    // Hi, I'm Ali, 21 years old, GPA 3.8
boolean honor = s1.isHonorStudent();   // true
```

Read `s1.age` as: **"go to the object that `s1` refers to, and get its `age`."**

### 11.2 What really happens on `s1.age`

```java
int a = s1.age;
```

```
 STEP 1: Look at the variable s1 on the stack → it holds a reference (address)

 STEP 2: Follow the reference to the object in the heap

 STEP 3: Inside the object, jump to where `age` is stored and read it

   STACK                      HEAP
  ┌────────┐         ┌─────────────────────────┐
  │ s1  ●──┼───①───► │ header                  │
  └────────┘         │ name ●──► "Ali"         │
                     │ age  20   ◄──── ③ read  │
                     │ gpa  3.6                │
                     └─────────────────────────┘
                              ② the reference leads here
```

The position of each field inside the object is **fixed** (decided when the class is compiled/loaded). So finding a field is just **"object address + fixed offset"**, a single quick step (the same idea as array indexing being O(1)).

### 11.3 Accessing an object inside an object (chained dots)

`name` is itself a reference to a `String` object. So `s1.name.length()` means **two hops**:

```java
int len = s1.name.length();    // 3 ("Ali")
```

```
   s1 ──► [Student object] ──name──► [String "Ali"] ──► .length() → 3
          hop 1                       hop 2
```

If any hop in the chain is `null`, you get a **`NullPointerException`**:

```java
Student s = new Student(null, 20, 3.6);
s.name.length();      // NullPointerException: name is null
```

### 11.4 Inside the class: access without the dot

Inside the class's own methods and constructors, you can use the fields **directly by name** (Java adds `this.` for you):

```java
void introduce() {
    System.out.println("Hi, I'm " + name);       // same as this.name
}
```

`this` is the reference to **the object the method was called on**:

```java
s1.introduce();    // inside the method, `this` refers to s1's object
s2.introduce();    // inside the method, `this` refers to s2's object
```

So the **same method code** gives different output for different objects, because `this` points to a different object each time.

### 11.5 Each object has its own data

```java
Student s1 = new Student("Ali", 20, 3.6);
Student s2 = new Student("Sara", 22, 3.2);

s1.age = 30;
System.out.println(s1.age);     // 30
System.out.println(s2.age);     // 22   (unaffected)
```

### 11.6 Passing an object to a method

Java passes a **copy of the reference**. The method and the caller then point to the **same object**, so the method can change the object's fields.

```java
static void birthday(Student s) {
    s.age = s.age + 1;          // follows the copied reference to the SAME object
}

Student s1 = new Student("Ali", 20, 3.6);
birthday(s1);
System.out.println(s1.age);     // 21
```

```
 main: s1 ──┐
            ├──► [ Student: age 20 → 21 ]      same object
 birthday: s ┘
```

### 11.7 Printing an object

```java
System.out.println(s1);       // Student@1b6d3586   (class name + hash, not useful!)
```

To control what is printed, write a `toString()` method:

```java
@Override
public String toString() {
    return "Student[name=" + name + ", age=" + age + ", gpa=" + gpa + "]";
}

System.out.println(s1);       // Student[name=Ali, age=20, gpa=3.6]
```

### 11.8 Comparing objects

```java
Student a = new Student("Ali", 20, 3.6);
Student b = new Student("Ali", 20, 3.6);

System.out.println(a == b);   // false  (== compares references: two different objects)
System.out.println(a == a);   // true
```

`==` asks "are these the **same object**?", not "do they hold the same data?". (Comparing contents needs an `equals` method, which is a later topic.)

---

## 12. Controlling Access: private, getters and setters

So far all fields were accessible from anywhere (`s1.age = -500;` would be allowed!). Java lets you **restrict** access with **access modifiers**.

### 12.1 Access modifiers for fields and methods

| Modifier | Who can access |
|----------|----------------|
| `public` | **Everyone** |
| `private` | **Only inside the same class** |
| *(none)* | Same package |
| `protected` | Same package + subclasses |

### 12.2 `private` fields + public getters/setters

The standard practice: make fields **`private`**, then provide **public methods** to read (getter) and change (setter) them in a controlled way.

```java
public class Student {

    private String name;       // hidden from outside
    private int age;

    public Student(String name, int age) {
        this.name = name;
        setAge(age);                          // reuse the validation
    }

    public String getName() {                 // getter
        return name;
    }

    public int getAge() {                     // getter
        return age;
    }

    public void setAge(int age) {             // setter with a rule
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Invalid age: " + age);
        }
        this.age = age;
    }
}
```

```java
Student s = new Student("Ali", 20);

System.out.println(s.getAge());     // 20        ✓ allowed (through the getter)
s.setAge(21);                       //           ✓ allowed (through the setter)

s.age = 25;                         // ERROR: age has private access in Student
s.setAge(-5);                       // IllegalArgumentException (the rule protects the data)
```

```
   OUTSIDE CODE                  INSIDE THE CLASS
  ┌────────────┐               ┌──────────────────────────────┐
  │ s.age = 25 │ ──✗ blocked── │ private int age              │
  └────────────┘               │                              │
  ┌────────────┐               │ public setAge(...) { check } │
  │ s.setAge(25)│ ──✓ allowed─►│ public getAge()              │
  └────────────┘               └──────────────────────────────┘
```

**Why?**

- Outside code **can't put wrong values** in (like a negative age).
- You can change the inside of the class later **without breaking** other code.
- This is the first taste of **encapsulation** (data hiding), one of the four pillars.

### 12.3 Naming getters and setters

| Field | Getter | Setter |
|-------|--------|--------|
| `name` | `getName()` | `setName(String name)` |
| `age` | `getAge()` | `setAge(int age)` |
| `boolean active` | `isActive()` | `setActive(boolean active)` |

---

## 13. Instance Data vs Static (Class) Data

### 13.1 The difference

| | **Instance** field/method | **Static** field/method |
|---|---------------------------|--------------------------|
| Declared | Without `static` | With `static` |
| Belongs to | **Each object** | The **class itself** |
| Copies | One **per object** | Exactly **one**, shared by all |
| Stored | In each **heap object** | **Once**, with the class |
| Accessed with | `objectRef.field` | `ClassName.field` |
| Needs an object? | **Yes** | **No** |

### 13.2 Example: counting how many objects were created

```java
public class Student {
    static int count = 0;           // ONE shared counter (belongs to the class)
    String name;                    // each object has its own name

    Student(String name) {
        this.name = name;
        count++;                    // every new object increases the shared counter
    }
}
```

```java
Student a = new Student("Ali");
Student b = new Student("Sara");
Student c = new Student("John");

System.out.println(Student.count);   // 3   (access static data with the CLASS name)
System.out.println(a.name);          // Ali (access instance data with an OBJECT reference)
```

```
   CLASS (one copy)               HEAP OBJECTS (one block each)
  ┌──────────────────┐          ┌────────────┐ ┌────────────┐ ┌────────────┐
  │ static count = 3 │          │ name "Ali" │ │ name "Sara"│ │ name "John"│
  └──────────────────┘          └────────────┘ └────────────┘ └────────────┘
   shared by everyone             each object has its own name
```

### 13.3 The rule to remember

- **Instance members** need an **object**: `s1.name`
- **Static members** use the **class name**: `Student.count`

(You can technically write `a.count` for a static field, but it's misleading. Use `Student.count`.)

---

## 14. Full Practical Example

Two files: `Book.java` and `Main.java`.

### `Book.java`

```java
public class Book {

    // ---------- fields (private: hidden from outside) ----------
    private String title;
    private String author;
    private int totalPages;
    private int currentPage;

    // ---------- shared (static) data ----------
    private static int bookCount = 0;

    // ---------- constructor ----------
    public Book(String title, String author, int totalPages) {
        this.title = title;
        this.author = author;
        this.totalPages = totalPages;
        this.currentPage = 0;               // starts at the beginning
        bookCount++;
    }

    // ---------- methods (behavior) ----------
    public void read(int pages) {
        if (pages <= 0) {
            System.out.println("Pages must be positive.");
            return;
        }
        currentPage = Math.min(currentPage + pages, totalPages);
    }

    public int getProgressPercent() {
        return currentPage * 100 / totalPages;
    }

    public boolean isFinished() {
        return currentPage == totalPages;
    }

    // ---------- getters ----------
    public String getTitle()      { return title; }
    public String getAuthor()     { return author; }
    public int getCurrentPage()   { return currentPage; }

    public static int getBookCount() {      // static method for static data
        return bookCount;
    }

    @Override
    public String toString() {
        return title + " by " + author + " (" + currentPage + "/" + totalPages + ")";
    }
}
```

### `Main.java`

```java
public class Main {
    public static void main(String[] args) {

        // create objects (each goes to the heap)
        Book b1 = new Book("Clean Code", "Robert Martin", 400);
        Book b2 = new Book("Effective Java", "Joshua Bloch", 300);

        // call methods (access behavior)
        b1.read(100);
        b1.read(50);
        b2.read(300);

        // read data through getters and methods
        System.out.println(b1);                              // uses toString()
        System.out.println("Progress: " + b1.getProgressPercent() + "%");
        System.out.println(b2);
        System.out.println("b2 finished? " + b2.isFinished());

        // a second reference to the SAME object
        Book alias = b1;
        alias.read(50);
        System.out.println(b1.getCurrentPage());             // b1 sees the change

        // static data through the class name
        System.out.println("Books created: " + Book.getBookCount());

        // private access is blocked
        // b1.currentPage = 999;     // ERROR: currentPage has private access in Book
    }
}
```

### Output

```
Clean Code by Robert Martin (150/400)
Progress: 37%
Effective Java by Joshua Bloch (300/300)
b2 finished? true
200
Books created: 2
```

(Check: 150 × 100 / 400 = 37 with integer division. After `alias.read(50)`, `b1` is at page 200.)

### The memory picture of this program

```
        STACK (main)                          HEAP
  ┌──────────────────┐
  │ b1    ●──────────┼──┐            ┌─────────────────────────────────┐
  │ alias ●──────────┼──┼──────────► │ Book                            │
  │ b2    ●──────────┼─┐│            │  title  ●──► "Clean Code"       │
  └──────────────────┘ ││            │  author ●──► "Robert Martin"    │
                       ││            │  totalPages 400                 │
                       ││            │  currentPage 200                │
                       ││            └─────────────────────────────────┘
                       ││
                       │└─(b1 and alias point to the SAME object)
                       │
                       │             ┌─────────────────────────────────┐
                       └───────────► │ Book                            │
                                     │  title  ●──► "Effective Java"   │
                                     │  author ●──► "Joshua Bloch"     │
                                     │  totalPages 300                 │
                                     │  currentPage 300                │
                                     └─────────────────────────────────┘

   CLASS Book (stored once):   static bookCount = 2     method code: read(), getProgressPercent() ...
```

### Try it yourself

1. Create a `Car` class with `brand`, `speed` fields, and methods `accelerate(int amount)` and `brake(int amount)`. Make `speed` never go below 0.
2. Make a `Rectangle` class with `width`, `height` and methods `area()` and `perimeter()`.
3. Add a static counter to `Car` that counts how many cars were created.
4. Draw the stack/heap picture for three `Car` objects before running the program.

---

## 15. Common Mistakes

| # | Mistake | What happens | Fix |
|---|---------|--------------|-----|
| 1 | Forgetting `new`: `Student s;` then `s.name = "Ali";` | `NullPointerException` (or "variable might not have been initialized") | `Student s = new Student(...)` |
| 2 | Public class name doesn't match the file name | Compile error | `public class Student` goes in `Student.java` |
| 3 | `student` or `Bank_Account` as class names | Compiles, but breaks the convention | `Student`, `BankAccount` |
| 4 | `name = name;` in a constructor | The field is never set | `this.name = name;` |
| 5 | Using a reference that is `null` | `NullPointerException` | Make sure the object was created; check for `null` |
| 6 | Thinking `Student b = a;` makes a copy | Both names point to **one** object | Use `new` to make a separate object |
| 7 | Comparing objects with `==` to check the same data | `false` for different objects | `==` checks the same **object**; compare fields or use `equals` |
| 8 | Accessing a `private` field from another class | Compile error | Use a getter/setter |
| 9 | Calling an instance method without an object from `static main` | "non-static method cannot be referenced from a static context" | Create an object first: `obj.method()` |
| 10 | Adding a constructor and losing the default one | `new Student()` stops compiling | Write the no-argument constructor yourself |
| 11 | Writing a return type on a constructor: `void Student() {}` | It becomes a normal method, not a constructor | Remove the return type |
| 12 | Making everything `public` | No data protection | Fields `private`, expose through methods |
| 13 | Printing an object and getting `Student@1b6d3586` | `toString()` not written | Override `toString()` |
| 14 | Using `Student.name` for an instance field | Error: non-static field | `objectRef.name` |

---

## 16. Summary and Quiz

### 16.1 Summary in simple points

1. **OOP** models a program as **objects**, each bundling **data (fields)** and **actions (methods)**.
2. It solves the problem of data and functions being **scattered**: one object = one thing, with its data and behavior together.
3. The four pillars are **encapsulation, abstraction, inheritance and polymorphism** (names only for now).
4. **Benefits:** real-world modeling, modularity, reusability, easier maintenance, data protection, scalability, easier testing, teamwork.
5. **Disadvantages:** steeper learning curve, more code, more memory, slightly slower, risk of over-engineering, needs planning.
6. A **class** is a **blueprint**; an **object** is a **real instance** built from it with **`new`**. One class, many objects.
7. A class has **fields** (data), **constructors** (set up new objects) and **methods** (behavior).
8. **Class names:** `PascalCase`, a **singular noun**, meaningful, start with a capital, no spaces/underscores/digits at the start. A `public` class must match its **file name**.
9. `new` creates the object in the **heap**, sets default values, runs the **constructor**, and returns a **reference**.
10. The variable (like `s1`) lives on the **stack** and holds a **reference**; the **object** lives in the **heap**. Copying a reference does **not** copy the object. `null` means "no object".
11. Each object stores **only its own field values**; **method code is shared** and stored once with the class.
12. Use the **dot operator** to access data: `object.field`, `object.method()`. It means "follow the reference to the heap object, then go to that field". Inside the class, `this` refers to the current object.
13. Use **`private` fields + getters/setters** to protect data. **Static** members belong to the class and are accessed with the **class name**.
14. Objects with no references left are cleaned up by the **Garbage Collector**.

### 16.2 Mini Quiz

1. What two things does an object bundle together?
2. What is the difference between a class and an object?
3. Is `bank_account` a good class name? Why or why not?
4. A public class named `Student` must be saved in which file?
5. After `Student s1 = new Student("Ali");` what is stored on the stack and what is stored in the heap?
6. What does this print?
   ```java
   Student a = new Student("Ali");
   Student b = a;
   b.name = "Sara";
   System.out.println(a.name);
   ```
7. What happens when you run `Student s = null; s.introduce();`?
8. Where is the code of a method like `introduce()` stored: inside every object, or once?
9. How do you access a static field `count` of class `Student`?
10. Why make fields `private` and add getters/setters?
11. When does Java provide a default constructor for you, and when does it not?
12. Name two benefits and two disadvantages of OOP.

<details>
<summary>Click for answers</summary>

1. **Data (fields/state)** and **actions (methods/behavior)**.
2. A **class** is the blueprint (code you write). An **object** is a real instance created from it with `new`, living in the heap, with its own values.
3. **No.** Class names should be **PascalCase** (`BankAccount`): start with a capital and avoid underscores.
4. **`Student.java`** (the file name must match the public class name exactly).
5. **Stack:** the reference variable `s1` (holding an address). **Heap:** the `Student` object with its fields.
6. **Sara.** `a` and `b` are two references to the **same** object.
7. **`NullPointerException`**, because `s` points to no object.
8. **Once**, with the class information. Objects share it; each object stores only its own field values.
9. With the class name: **`Student.count`**.
10. To **protect the data** from invalid values and control how it is changed, and to be free to change the inside of the class later without breaking other code.
11. Java provides an empty default constructor **only if you write no constructors**. As soon as you write any constructor, it no longer does.
12. Benefits: **modularity/reusability/easier maintenance/data protection**. Disadvantages: **steeper learning curve/more code/more memory/over-engineering** (any two of each).

</details>

---

*Practice tip: always draw the stack and the heap on paper for the objects you create. Once you can "see" the references and objects, the rest of OOP becomes much easier.*
