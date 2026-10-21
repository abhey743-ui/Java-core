# Java Constructors: The Complete Step-by-Step Guide

> This guide explains **everything about constructors**: what they are, how to define them, the **default constructor**, the **`this` keyword**, **constructor overloading**, **constructor chaining** (`this(...)` and `super(...)`), initialization order, copy constructors, `final` fields, private constructors and more.
> Every example uses correct syntax and shows its output, so you can copy, run and change it.

---

## Table of Contents

1. [What Is a Constructor?](#1-what-is-a-constructor)
2. [Constructor Syntax and Rules](#2-constructor-syntax-and-rules)
3. [Your First Constructor, Step by Step](#3-your-first-constructor-step-by-step)
4. [What Happens When `new` Runs](#4-what-happens-when-new-runs)
5. [The Default Constructor](#5-the-default-constructor)
6. [Parameterized and No-Argument Constructors](#6-parameterized-and-no-argument-constructors)
7. [The `this` Keyword](#7-the-this-keyword)
8. [Constructor Overloading](#8-constructor-overloading)
9. [Constructor Chaining with `this(...)`](#9-constructor-chaining-with-this)
10. [Constructor Chaining with `super(...)`](#10-constructor-chaining-with-super)
11. [Rules and Restrictions for Chaining](#11-rules-and-restrictions-for-chaining)
12. [Order of Initialization](#12-order-of-initialization)
13. [Copy Constructors](#13-copy-constructors)
14. [`final` Fields and Constructors](#14-final-fields-and-constructors)
15. [Validation and Exceptions in Constructors](#15-validation-and-exceptions-in-constructors)
16. [Private Constructors](#16-private-constructors)
17. [Constructors in Enums, Records, Abstract Classes and Interfaces](#17-constructors-in-enums-records-abstract-classes-and-interfaces)
18. [Pitfall: Calling Overridable Methods in a Constructor](#18-pitfall-calling-overridable-methods-in-a-constructor)
19. [Constructor vs Method](#19-constructor-vs-method)
20. [Practical Programs](#20-practical-programs)
21. [Common Mistakes](#21-common-mistakes)
22. [Cheat Sheet, Summary and Quiz](#22-cheat-sheet-summary-and-quiz)

---

## 1. What Is a Constructor?

A **constructor** is a **special block of code** inside a class that runs **automatically when you create an object** with `new`. Its job is to **set up the new object**: give its fields their starting values and prepare it for use.

Think of building a car in a factory:

```
   Blueprint (class)  ──►  Factory assembly line (constructor)  ──►  Finished car (object)
                              • fits the engine
                              • paints the body
                              • installs the seats
```

The constructor is the **assembly line**. Every time you ask for a new car, it runs and hands you a **ready-to-use** one.

### Why do we need constructors?

Without constructors, you would create an empty object and then fill every field by hand:

```java
Student s = new Student();
s.name = "Ali";
s.age = 20;
s.gpa = 3.6;               // easy to forget one!
```

With a constructor, an object is **born complete**:

```java
Student s = new Student("Ali", 20, 3.6);     // one line, nothing forgotten
```

| Purpose | Meaning |
|---------|---------|
| **Initialize fields** | Give the object valid starting values |
| **Guarantee a valid state** | An object can't exist half-built (you can force required values) |
| **Validate input** | Reject bad data right at creation time |
| **Run setup code** | Open a connection, create internal arrays, register something, and so on |

---

## 2. Constructor Syntax and Rules

### 2.1 The syntax

```
[access modifier] ClassName ( [parameters] ) [throws exceptions] {
    // initialization code
}
```

```java
public class Car {
    String brand;
    int year;

    public Car(String brand, int year) {     // ← the constructor
        this.brand = brand;
        this.year = year;
    }
}
```

Anatomy:

```
   public    Car      ( String brand, int year )     {  this.brand = brand;  ... }
 ┌────────┐ ┌─────┐   └────────────┬────────────┘     └───────────┬───────────┘
 access     same name              parameter list                 body
 modifier   as the class           (can be empty)
 (optional)
```

### 2.2 The rules

| # | Rule |
|---|------|
| 1 | The constructor's **name must be exactly the class name** (same spelling and capitalization) |
| 2 | It has **no return type**, not even `void` |
| 3 | It **runs automatically** when you use `new`. You never call it like a normal method |
| 4 | It can have **any number of parameters** (including none) |
| 5 | A class can have **many constructors** (overloading, section 8) |
| 6 | It **can use access modifiers**: `public`, `protected`, package-private (none), `private` |
| 7 | It **cannot** be `static`, `final`, `abstract`, `synchronized` or `native` |
| 8 | It is **not inherited** by subclasses, and it **can't be overridden** |
| 9 | It can **throw exceptions** (`throws`) |
| 10 | It may contain a bare `return;` to exit early, but **cannot return a value** |

### 2.3 The classic trap: adding a return type

```java
public class Student {
    public void Student() {            // NOT a constructor! It's a normal method named Student
        System.out.println("Hello");
    }
}

Student s = new Student();             // runs the (invisible) default constructor
                                       // "Hello" is NOT printed
```

Because `void` was written, Java treats it as an ordinary method that happens to share the class name. **Remove the return type** to make it a constructor.

### 2.4 Access modifiers on constructors

| Modifier | Who can call `new ClassName(...)` |
|----------|-----------------------------------|
| `public` | Any class anywhere |
| `protected` | Same package and subclasses |
| *(none)* | Only classes in the same package |
| `private` | Only code **inside the same class** (section 16) |

---

## 3. Your First Constructor, Step by Step

**Goal:** a `Car` class where every car is created with a brand and a year.

**Step 1: Write the class and its fields.**

```java
public class Car {
    String brand;
    int year;
}
```

**Step 2: Add a constructor with the same name as the class and a parameter for each value you need.**

```java
public class Car {
    String brand;
    int year;

    public Car(String brand, int year) {
    }
}
```

**Step 3: Inside the constructor, copy each parameter into its field using `this.`**

```java
public class Car {
    String brand;
    int year;

    public Car(String brand, int year) {
        this.brand = brand;        // field = parameter
        this.year = year;
    }
}
```

**Step 4: Add a method to use the data.**

```java
    void show() {
        System.out.println(brand + " (" + year + ")");
    }
```

**Step 5: Create objects with `new`, passing arguments that match the constructor.**

```java
public class Main {
    public static void main(String[] args) {
        Car c1 = new Car("Toyota", 2024);
        Car c2 = new Car("Honda", 2022);
        c1.show();                   // Toyota (2024)
        c2.show();                   // Honda (2022)
    }
}
```

**Step 6: Remember the arguments must match the constructor.**

```java
Car c3 = new Car();                  // ERROR: constructor Car in class Car cannot be applied to given types
Car c4 = new Car("Kia");             // ERROR: needs 2 arguments
Car c5 = new Car(2024, "Kia");       // ERROR: wrong order/types
```

---

## 4. What Happens When `new` Runs

Take this line:

```java
Car c = new Car("Toyota", 2024);
```

### 4.1 The steps, in order

```
 1. The JVM loads the class Car (only the first time it is needed)
 2. Memory for ONE Car object is reserved in the HEAP
 3. Every field gets its DEFAULT value (brand = null, year = 0)
 4. The constructor is called:
       • the arguments are copied into the parameters
       • `this` is bound to the new object
 5. FIRST, the parent constructor runs (an invisible super() if you wrote none)
 6. Then the field initializers and instance initializer blocks run (if any)
 7. Then the rest of the constructor body runs   (this.brand = ..., this.year = ...)
 8. `new` produces a REFERENCE to the finished object
 9. That reference is stored in the variable c
```

### 4.2 The memory picture while the constructor runs

```
        STACK                                         HEAP
  ┌──────────────────────────┐
  │ Car(String,int)  frame   │                 ┌──────────────────────────────┐
  │   this   ●───────────────┼───────────────► │ Car object (being built)     │
  │   brand  ●──► "Toyota"   │                 │   brand : null  → "Toyota"   │
  │   year   2024            │                 │   year  : 0     → 2024       │
  ├──────────────────────────┤                 └──────────────────────────────┘
  │ main                     │
  │   c   (not assigned yet) │
  └──────────────────────────┘
```

```
 When the constructor finishes:

        STACK                                         HEAP
  ┌──────────────────────────┐                 ┌──────────────────────────────┐
  │ main                     │                 │ Car object                   │
  │   c  ●───────────────────┼───────────────► │   brand ●──► "Toyota"        │
  └──────────────────────────┘                 │   year  2024                 │
   (the constructor frame is gone)             └──────────────────────────────┘
```

Key points:

- **`this` is a hidden reference** to the object being built.
- The constructor's frame disappears when it ends, but the **object stays in the heap**, kept alive by the reference `c`.
- A constructor **does not "return" the object**. It's the `new` expression that gives you the reference.

---

## 5. The Default Constructor

### 5.1 What is it?

If you write **no constructor at all**, the Java compiler **adds one for you**. It is called the **default constructor**.

```java
class Dog {
    String name;
}
```

The compiler treats this class as if you had written:

```java
class Dog {
    String name;

    Dog() {              // added automatically by the compiler
        super();         // calls the parent constructor (Object)
    }
}
```

So this works even though you wrote no constructor:

```java
Dog d = new Dog();        // OK
System.out.println(d.name);   // null
```

### 5.2 What the default constructor does

| Property | Detail |
|----------|--------|
| **Parameters** | None (it is a no-argument constructor) |
| **Body** | Only calls `super()` (the parent's no-argument constructor) |
| **Access modifier** | The **same as the class**: `public class` gets `public` default constructor; package-private class gets package-private |
| **Field values** | Fields keep their **default values**: `0`, `0.0`, `false`, `'\u0000'`, `null` |

```java
class Demo {
    int count;
    double price;
    boolean active;
    String text;
}

Demo d = new Demo();
System.out.println(d.count);    // 0
System.out.println(d.price);    // 0.0
System.out.println(d.active);   // false
System.out.println(d.text);     // null
```

### 5.3 When does Java add it? (the most important rule)

> **The default constructor is added ONLY if the class has NO constructor at all.**
> **The moment you write ANY constructor, the default one is no longer added.**

```java
class Student {
    String name;

    Student(String name) {          // you wrote a constructor
        this.name = name;
    }
}

Student a = new Student("Ali");     // OK
Student b = new Student();          // ERROR: constructor Student in class Student cannot be applied to given types;
                                    //        required: String   found: no arguments
```

If you still want `new Student()` to work, **write the no-argument constructor yourself**:

```java
class Student {
    String name;

    Student() {                     // you write it explicitly now
        this.name = "Unknown";
    }

    Student(String name) {
        this.name = name;
    }
}
```

### 5.4 "Default constructor" vs "no-argument constructor"

These two terms are often mixed up.

| Term | Meaning |
|------|---------|
| **Default constructor** | The one the **compiler generates** when you write no constructors |
| **No-argument (no-arg) constructor** | **Any** constructor with an empty parameter list, whether the compiler made it or **you wrote it** |

So a default constructor is always a no-arg constructor, but a no-arg constructor is not always the default one.

### 5.5 See it yourself with `javap`

```
javac Dog.java
javap Dog
```

Output:
```
class Dog {
  java.lang.String name;
  Dog();
}
```

The `Dog();` line is the constructor the compiler added for you.

### 5.6 Why a no-arg constructor is sometimes required

Many frameworks and libraries (JavaBeans, JSON converters, database mappers, serialization) create your objects by calling a **no-argument constructor** and then setting fields. If your class has only parameterized constructors, they may fail. When in doubt, provide one.

---

## 6. Parameterized and No-Argument Constructors

### 6.1 No-argument constructor (sets defaults)

```java
class Student {
    String name;
    int age;

    Student() {
        name = "Unknown";
        age = 18;
    }
}

Student s = new Student();         // Unknown, 18
```

### 6.2 Parameterized constructor (you supply the values)

```java
class Student {
    String name;
    int age;

    Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

Student s = new Student("Ali", 20);      // Ali, 20
```

### 6.3 Using both

```java
class Student {
    String name;
    int age;

    Student() {                                   // no-argument
        this.name = "Unknown";
        this.age = 18;
    }

    Student(String name, int age) {               // parameterized
        this.name = name;
        this.age = age;
    }
}
```

When a class has several constructors, they are **overloaded** (next sections).

### 6.4 Parameters named differently than fields

You don't have to give parameters the same name as fields:

```java
Student(String n, int a) {
    name = n;          // no `this.` needed because there is no name clash
    age = a;
}
```

But using the **same name** with `this.` is the standard style, because it keeps the constructor readable.

---

## 7. The `this` Keyword

`this` is a **reference to the current object**: the object whose constructor or method is running right now.

### 7.1 Use 1: separate a field from a parameter with the same name

```java
class Student {
    String name;

    Student(String name) {
        this.name = name;      // this.name → the FIELD      name → the PARAMETER
    }
}
```

Without `this`, the parameter **hides** the field, and nothing is stored:

```java
Student(String name) {
    name = name;               // BUG: assigns the parameter to itself; the field stays null
}
```

### 7.2 Use 2: call another constructor of the same class: `this(...)`

```java
Student() {
    this("Unknown", 18);       // calls Student(String, int)
}
```

(Full detail in section 9.)

### 7.3 Use 3: pass the current object to another method or object

```java
class Button {
    Button(Window w) {
        w.register(this);      // "here I am", sends THIS Button object to the window
    }
}
```

### 7.4 Use 4: return the current object (used for method chaining)

```java
class Counter {
    int value;

    Counter add(int n) {
        value += n;
        return this;           // returns the same object, so calls can be chained
    }
}

new Counter().add(5).add(10);
```

### 7.5 Use 5: call another instance method or access a field explicitly

```java
void show() {
    this.print();              // same as print()
    System.out.println(this.name);   // same as name
}
```

### 7.6 What `this` really is in memory

```
 Student a = new Student("Ali");
 Student b = new Student("Sara");

 a.introduce();     inside introduce(), `this` → a's object
 b.introduce();     inside introduce(), `this` → b's object

   STACK                         HEAP
 ┌──────────────┐           ┌──────────────┐   ┌──────────────┐
 │ a  ●─────────┼─────────► │ name "Ali"   │   │ name "Sara"  │ ◄──────┐
 │ b  ●─────────┼───────────┼──────────────┼───┘              │        │
 └──────────────┘           └──────────────┘   └──────────────┘        │
 inside b.introduce():   this ●─────────────────────────────────────────┘
```

The **same method code** is used for every object, but `this` points to a **different object** each time. That is how one method gives different results for different objects.

### 7.7 Where `this` can NOT be used

`this` does not exist in **`static`** code (static methods, static blocks), because static code runs without any object.

```java
static void test() {
    System.out.println(this.name);     // ERROR: non-static variable this cannot be referenced from a static context
}
```

---

## 8. Constructor Overloading

### 8.1 What is it?

**Constructor overloading** means a class has **several constructors**, all with the class name but with **different parameter lists**. Each one gives a different way to create the object.

```java
public class Student {
    private String name;
    private int age;
    private String course;

    // 1) no parameters
    public Student() {
        this.name = "Unknown";
        this.age = 18;
        this.course = "Undeclared";
    }

    // 2) one parameter
    public Student(String name) {
        this.name = name;
        this.age = 18;
        this.course = "Undeclared";
    }

    // 3) two parameters
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
        this.course = "Undeclared";
    }

    // 4) three parameters
    public Student(String name, int age, String course) {
        this.name = name;
        this.age = age;
        this.course = course;
    }

    @Override
    public String toString() {
        return name + ", " + age + ", " + course;
    }
}
```

```java
System.out.println(new Student());                       // Unknown, 18, Undeclared
System.out.println(new Student("Ali"));                  // Ali, 18, Undeclared
System.out.println(new Student("Sara", 22));             // Sara, 22, Undeclared
System.out.println(new Student("John", 20, "Physics"));  // John, 20, Physics
```

Java picks the constructor by **matching the arguments** you pass.

### 8.2 What makes constructors different enough?

The same rules as method overloading. The parameter lists must differ in:

| Difference | Allowed? | Example |
|------------|:--------:|---------|
| **Number** of parameters | ✓ | `Box()` and `Box(int)` |
| **Type** of parameters | ✓ | `Box(int)` and `Box(String)` |
| **Order** of different types | ✓ | `Box(int, String)` and `Box(String, int)` |
| Only the **parameter names** | ✗ | `Box(int a)` and `Box(int b)` is a duplicate |
| Only the **access modifier** | ✗ | `public Box(int a)` and `private Box(int a)` is a duplicate |
| Return type | n/a | Constructors have no return type |

```java
class Box {
    Box(int a) { }
    Box(int b) { }        // ERROR: constructor Box(int) is already defined
}
```

### 8.3 Which constructor does Java pick?

Java follows the same three phases as for method overloading and stops at the first phase that finds a match:

```
 Phase 1: exact match or widening (int → long → float → double)
 Phase 2: allow boxing / unboxing (int ↔ Integer)
 Phase 3: allow varargs
```

```java
class Num {
    Num(long x)    { System.out.println("long"); }
    Num(double x)  { System.out.println("double"); }
    Num(Integer x) { System.out.println("Integer"); }
}

new Num(5);        // long      (int widens to long; widening beats boxing)
new Num(5L);       // long      (exact)
new Num(5.5f);     // double    (float widens to double)
new Num(5.5);      // double    (exact)
new Num(Integer.valueOf(5));   // Integer (exact)
```

### 8.4 Ambiguity errors

```java
class Box {
    Box(int w, double h) { }
    Box(double w, int h) { }
}

new Box(1, 1.5);      // OK → Box(int, double)
new Box(1.5, 1);      // OK → Box(double, int)
new Box(1, 1);        // ERROR: reference to Box is ambiguous
```

### 8.5 The problem with overloading: repeated code

Look again at section 8.1. The four constructors **repeat the same assignments**. If you add a new field or change a rule, you must edit **every** constructor and risk missing one.

The solution is **constructor chaining** (next section).

---

## 9. Constructor Chaining with `this(...)`

### 9.1 What is it?

**Constructor chaining** means one constructor **calls another constructor**, so the real work is written **once**.

Inside the **same class**, you use **`this(...)`**:

```java
public class Student {
    private String name;
    private int age;
    private String course;

    public Student() {
        this("Unknown");                         // calls Student(String)
    }

    public Student(String name) {
        this(name, 18);                          // calls Student(String, int)
    }

    public Student(String name, int age) {
        this(name, age, "Undeclared");           // calls Student(String, int, String)
    }

    public Student(String name, int age, String course) {   // the "master" constructor
        this.name = name;
        this.age = age;
        this.course = course;
    }

    @Override
    public String toString() {
        return name + ", " + age + ", " + course;
    }
}
```

```
  new Student()                 ─► this("Unknown")             ─┐
  new Student("Ali")            ─► this("Ali", 18)             ─┼─► ... ─┐
  new Student("Sara", 22)       ─► this("Sara", 22, "Undeclared") ───────┼─► Student(String,int,String)
  new Student("John",20,"Phys") ──────────────────────────────────────────┘     (the ONE place
                                                                                   fields are set)
```

Now there is **one master constructor** that assigns the fields. All the others just **forward** to it with default values. Adding a new field or a validation rule means editing **one place**.

### 9.2 The syntax rules

```java
this(arguments);        // calls another constructor of the SAME class
```

- It must be the **first statement** in the constructor.
- Java matches the arguments to a constructor using the normal overloading rules.

### 9.3 In what order do the bodies run?

When a constructor calls `this(...)`, the **called constructor runs completely first**, then the rest of the calling constructor's body continues.

```java
public class Student {
    private String name;
    private int age;
    private String course;

    public Student() {
        this("Unknown");
        System.out.println("Student()");
    }

    public Student(String name) {
        this(name, 18);
        System.out.println("Student(String)");
    }

    public Student(String name, int age) {
        this(name, age, "Undeclared");
        System.out.println("Student(String, int)");
    }

    public Student(String name, int age, String course) {
        this.name = name;
        this.age = age;
        this.course = course;
        System.out.println("Student(String, int, String)");
    }

    public static void main(String[] args) {
        new Student();
    }
}
```

Output:

```
Student(String, int, String)
Student(String, int)
Student(String)
Student()
```

The call stack shows why (each constructor waits for the next one to finish):

```
 DOWN: the calls (frames pile up)                  UP: the bodies finish (frames removed)

 ┌─────────────────────────────────┐
 │ Student(String,int,String)  ①   │ ◄ runs body FIRST, prints "Student(String, int, String)"
 ├─────────────────────────────────┤
 │ Student(String,int)         ②   │ ◄ then finishes, prints "Student(String, int)"
 ├─────────────────────────────────┤
 │ Student(String)             ③   │ ◄ then finishes, prints "Student(String)"
 ├─────────────────────────────────┤
 │ Student()                   ④   │ ◄ then finishes, prints "Student()"
 ├─────────────────────────────────┤
 │ main                            │
 └─────────────────────────────────┘
```

**Rule of thumb: the last constructor in the chain runs its body first; the constructor you originally called runs its body last.**

### 9.4 Adding validation in the master constructor

Because every path goes through the master constructor, **validation is written once**:

```java
public Student(String name, int age, String course) {
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("name is required");
    }
    if (age < 0) {
        throw new IllegalArgumentException("age cannot be negative");
    }
    this.name = name;
    this.age = age;
    this.course = course;
}
```

Now `new Student("", 20)`, `new Student("Ali", -5)` and every other constructor are all protected by the same checks.

### 9.5 The "wrong" way: constructors can't call each other like methods

```java
public Student() {
    Student("Unknown", 18, "Undeclared");      // ERROR: it's not a method call
}

public Student() {
    new Student("Unknown", 18, "Undeclared");  // compiles, but creates a SEPARATE object and throws it away!
}
```

Only **`this(...)`** chains constructors **on the same object**.

---

## 10. Constructor Chaining with `super(...)`

### 10.1 Parent constructors always run first

When a class **extends** another class (inheritance), creating a child object must also set up the **parent part** of the object. So a child constructor **always calls a parent constructor first**. You do it with **`super(...)`**:

```java
class Person {
    protected String name;

    Person(String name) {
        this.name = name;
        System.out.println("Person constructor: " + name);
    }
}

class Employee extends Person {
    private double salary;

    Employee(String name, double salary) {
        super(name);                 // call the parent's constructor FIRST
        this.salary = salary;
        System.out.println("Employee constructor: " + salary);
    }
}
```

```java
new Employee("Ali", 5000);
```

Output:
```
Person constructor: Ali
Employee constructor: 5000.0
```

```
  Employee object in the heap:
 ┌──────────────────────────────┐
 │  PARENT PART (from Person)   │ ◄── built first by super(name)
 │     name = "Ali"             │
 ├──────────────────────────────┤
 │  CHILD PART (from Employee)  │ ◄── built second
 │     salary = 5000.0          │
 └──────────────────────────────┘
```

### 10.2 The invisible `super()`

If you do **not** write `this(...)` or `super(...)` as the first line, the compiler **automatically inserts `super();`** (a call to the parent's **no-argument** constructor).

```java
class Parent {
    Parent() { System.out.println("Parent()"); }
}

class Child extends Parent {
    Child() {
        // the compiler secretly adds:  super();
        System.out.println("Child()");
    }
}

new Child();
```

Output:
```
Parent()
Child()
```

This is true for the **default constructor** too. Even a class with no code gets `Child() { super(); }`.

### 10.3 The most common error with inheritance

If the parent has **only a parameterized constructor**, there is **no no-argument constructor** for the invisible `super()` to call:

```java
class Parent {
    Parent(String name) { }          // only this constructor exists
}

class Child extends Parent {
    Child() {                        // ERROR: constructor Parent in class Parent cannot be applied to given types
    }                                //        (the compiler tries super(), but Parent() doesn't exist)
}

class Child2 extends Parent { }      // ERROR too: the default constructor tries super() as well
```

**Fix:** call the parent constructor explicitly with the right arguments:

```java
class Child extends Parent {
    Child() {
        super("default name");       // OK
    }
}
```

(Or add a no-argument constructor to `Parent`.)

### 10.4 A three-level chain

```java
class Vehicle {
    protected String brand;

    Vehicle(String brand) {
        this.brand = brand;
        System.out.println("Vehicle(" + brand + ")");
    }
}

class Car extends Vehicle {
    protected int doors;

    Car(String brand) {
        this(brand, 4);                                    // chain within Car
        System.out.println("Car(" + brand + ")");
    }

    Car(String brand, int doors) {
        super(brand);                                      // chain to Vehicle
        this.doors = doors;
        System.out.println("Car(" + brand + ", " + doors + ")");
    }
}

class ElectricCar extends Car {
    private int rangeKm;

    ElectricCar(String brand, int rangeKm) {
        super(brand);                                      // chain to Car(String)
        this.rangeKm = rangeKm;
        System.out.println("ElectricCar(" + brand + ", " + rangeKm + ")");
    }
}
```

```java
new ElectricCar("Tesla", 500);
```

Output:
```
Vehicle(Tesla)
Car(Tesla, 4)
Car(Tesla)
ElectricCar(Tesla, 500)
```

The path the program takes:

```
 new ElectricCar("Tesla", 500)
        │
        ▼
 ElectricCar(String,int)  ── super(brand) ──►  Car(String)
                                                  │  this(brand, 4)
                                                  ▼
                                              Car(String,int)  ── super(brand) ──►  Vehicle(String)
                                                                                         │ (implicit super() to Object)
                                                                                         ▼
                                                                                    Vehicle body runs  → prints "Vehicle(Tesla)"
                                              Car(String,int) body → prints "Car(Tesla, 4)"
                                          Car(String) body → prints "Car(Tesla)"
 ElectricCar body → prints "ElectricCar(Tesla, 500)"
```

**Rule of thumb: constructor bodies finish from the top-most parent (`Object`) down to the class you created.**

### 10.5 `this(...)` vs `super(...)`

| | `this(...)` | `super(...)` |
|---|-------------|--------------|
| Calls a constructor of | The **same** class | The **parent** class |
| Purpose | Avoid repeating code between your own constructors | Build the inherited part of the object |
| Must be first statement? | Yes | Yes |
| Can both appear in one constructor? | **No** (each needs to be first) | **No** |
| If neither is written | Compiler adds `super();` | |

---

## 11. Rules and Restrictions for Chaining

### Rule 1: `this(...)` / `super(...)` must be the first statement

```java
Student() {
    System.out.println("hi");
    this("Unknown");              // ERROR: call to this must be first statement in constructor
}
```

(Very recent Java versions loosen this slightly, but writing the call first is always correct and is what all existing code does.)

### Rule 2: You can't use both in the same constructor

```java
Student() {
    this("Unknown");
    super();                      // ERROR: both can't be first
}
```

If you use `this(...)`, the **other** constructor is the one that calls `super(...)` (or the implicit one).

### Rule 3: No circular chains

```java
class Loop {
    Loop()        { this(5); }
    Loop(int x)   { this(); }     // ERROR: recursive constructor invocation
}
```

A chain must **end** at a constructor that does **not** call `this(...)`.

### Rule 4: Arguments can't use the object being built

Inside `this(...)` or `super(...)`, the object **doesn't exist yet**, so you can't use `this`, instance fields or instance methods. You **can** use parameters, constants and **static** methods.

```java
class Demo {
    int base = 10;

    Demo() {
        this(base);               // ERROR: cannot reference base before supertype constructor has been called
    }

    Demo(int x) { }
}
```

```java
class Demo {
    static final int DEFAULT = 10;

    static int computeDefault() { return 7; }

    Demo()          { this(DEFAULT); }            // OK: a constant
    Demo(String s)  { this(computeDefault()); }   // OK: a static method
    Demo(int x)     { }
}
```

### Rule 5: Constructors are not inherited

A child class does **not** get its parent's constructors. If `Parent` has `Parent(String)`, then `new Child("x")` only works if `Child` defines `Child(String)` itself.

### Rule 6: Every chain ends in `Object()`

All classes eventually extend `java.lang.Object`, so the very last constructor in any chain is `Object()`, which does nothing.

---

## 12. Order of Initialization

A class can set values in **three places**:

1. **Field initializers**: `int count = 5;`
2. **Instance initializer blocks**: `{ ... }` (a bare block inside the class)
3. **Constructors**

### 12.1 The order inside ONE class

```
 1. Fields get their default values (0, null, false)
 2. Field initializers and instance blocks run, in the order they appear in the source
 3. The constructor body runs
```

```java
class Demo {
    int a = 10;                                     // field initializer

    {                                               // instance initializer block
        System.out.println("block, a = " + a);
        a = 20;
    }

    int b = 30;

    Demo() {
        System.out.println("constructor, a = " + a + ", b = " + b);
    }
}

new Demo();
```

Output:
```
block, a = 10
constructor, a = 20, b = 30
```

### 12.2 The order with inheritance

```java
class Parent {
    int a = init("Parent field a");

    {
        System.out.println("Parent instance block");
    }

    Parent() {
        System.out.println("Parent constructor");
    }

    static int init(String text) {
        System.out.println(text);
        return 1;
    }
}

class Child extends Parent {
    int b = init("Child field b");

    {
        System.out.println("Child instance block");
    }

    Child() {
        System.out.println("Child constructor");
    }
}

new Child();
```

Output:
```
Parent field a
Parent instance block
Parent constructor
Child field b
Child instance block
Child constructor
```

```
  new Child()
     │
     ▼
  Child constructor starts → implicit super()
     │
     ▼
  PARENT:  field initializers  →  instance blocks  →  constructor body
     │
     ▼  (back in Child)
  CHILD:   field initializers  →  instance blocks  →  rest of constructor body
```

### 12.3 Where static initialization fits

`static` fields and `static { }` blocks run **once**, when the class is first loaded, **before** any object is created:

```
 (once, at class load)  static fields / static blocks  (parent first, then child)
 (every new)            parent instance init + constructor  →  child instance init + constructor
```

### 12.4 Using an instance block to share code between constructors

```java
class Ticket {
    private final int id;
    private static int nextId = 1;

    {                                   // runs before EVERY constructor body
        id = nextId++;
    }

    Ticket()            { }
    Ticket(String note) { System.out.println("note: " + note); }

    int getId() { return id; }
}

System.out.println(new Ticket().getId());          // 1
System.out.println(new Ticket("hi").getId());      // note: hi   then   2
```

(Most code uses constructor chaining instead, because it is easier to follow.)

---

## 13. Copy Constructors

A **copy constructor** takes an **object of the same class** and builds a **new, separate object** with the same values.

Java doesn't give you one automatically. You write it:

```java
public class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Person(Person other) {                  // copy constructor
        this(other.name, other.age);               // reuse the main constructor
    }

    public void setAge(int age) { this.age = age; }

    @Override
    public String toString() { return name + ", " + age; }
}
```

```java
Person p1 = new Person("Ali", 20);
Person p2 = new Person(p1);          // a separate copy
Person p3 = p1;                      // NOT a copy: just a second name for the same object

p2.setAge(99);
System.out.println(p1);              // Ali, 20    (p1 untouched)
System.out.println(p2);              // Ali, 99

p3.setAge(50);
System.out.println(p1);              // Ali, 50    (p1 changed: p3 is the same object)
```

```
 p1 ──► [ Person: Ali, 50 ]  ◄── p3          (same object)
 p2 ──► [ Person: Ali, 99 ]                  (different object)
```

### Shallow vs deep copy

If a field is itself an **object or an array**, copying the reference only copies the **address**, so both objects **share** the same inner object. This is a **shallow copy**.

```java
class Team {
    private String[] members;

    Team(String[] members) { this.members = members; }

    // SHALLOW copy: both Teams share the SAME array
    Team(Team other) { this.members = other.members; }
}
```

For a **deep copy**, copy the inner object too:

```java
import java.util.Arrays;

class Team {
    private String[] members;

    Team(String[] members) { this.members = members; }

    // DEEP copy: a brand-new array with the same contents
    Team(Team other) {
        this.members = Arrays.copyOf(other.members, other.members.length);
    }
}
```

```
 SHALLOW copy                          DEEP copy
 t1 ─► [Team] ─► members ─┐            t1 ─► [Team] ─► members ─► [ "A","B" ]
 t2 ─► [Team] ─► members ─┴─► [ "A","B" ]   t2 ─► [Team] ─► members ─► [ "A","B" ]  (separate array)
       (one shared array!)
```

---

## 14. `final` Fields and Constructors

A **`final`** field can be assigned **only once**. A common place to assign it is the **constructor**.

```java
public class Account {
    private final String id;            // can never change after construction
    private double balance;

    public Account(String id, double balance) {
        this.id = id;                   // the one allowed assignment
        this.balance = balance;
    }

    public void setId(String newId) {
        this.id = newId;                // ERROR: cannot assign a value to final variable id
    }
}
```

### Rule: every constructor must assign every `final` field (exactly once)

```java
class Bad {
    private final int x;

    Bad() { }                           // ERROR: variable x might not have been initialized
}
```

When constructors are **chained**, the chain only needs to reach **one** constructor that assigns the field:

```java
class Point {
    private final int x;
    private final int y;

    Point()             { this(0, 0); }          // OK: assignment happens in Point(int,int)
    Point(int x, int y) { this.x = x; this.y = y; }
}
```

But you can't assign a final field **twice** in a chain:

```java
Point() {
    this(0, 0);
    this.x = 5;                         // ERROR: variable x might already have been assigned
}
```

### Immutable classes

A class whose objects **never change** after creation is **immutable** (like `String`). Recipe: all fields `private final`, no setters, set everything in the constructor.

```java
public final class Money {
    private final long cents;
    private final String currency;

    public Money(long cents, String currency) {
        this.cents = cents;
        this.currency = currency;
    }

    public Money plus(Money other) {
        return new Money(this.cents + other.cents, this.currency);   // returns a NEW object
    }
}
```

---

## 15. Validation and Exceptions in Constructors

### 15.1 Reject bad input at creation

```java
public class Rectangle {
    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        this.width = width;
        this.height = height;
    }
}
```

```java
try {
    Rectangle r = new Rectangle(-5, 10);
} catch (IllegalArgumentException e) {
    System.out.println("Error: " + e.getMessage());     // Error: Width and height must be positive
}
```

### 15.2 What happens when a constructor throws

- The `new` expression **does not produce a reference**.
- The half-built object is **abandoned** (the garbage collector removes it).
- The variable you were assigning to is **never assigned**.

```java
Rectangle r;
try {
    r = new Rectangle(-5, 10);        // throws
} catch (IllegalArgumentException e) { }
// r was never assigned
```

This is the big benefit: **an invalid object can never exist.**

### 15.3 Checked exceptions with `throws`

```java
import java.io.*;

public class FileReaderHelper {
    private final BufferedReader reader;

    public FileReaderHelper(String path) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(path));
    }
}

try {
    FileReaderHelper h = new FileReaderHelper("data.txt");
} catch (FileNotFoundException e) {
    System.out.println("File not found");
}
```

### 15.4 Keep constructors light

A constructor should **set up the object and check the input**. Avoid heavy work there (slow network calls, long loops). If the work is big or can fail in complicated ways, use a **static factory method** instead (section 16.4).

---

## 16. Private Constructors

A **`private`** constructor can only be called from **inside the same class**. So other classes **cannot** use `new` on it. That sounds useless, but it is a powerful tool.

### 16.1 Use 1: Utility classes (stop anyone from creating objects)

A class that only has `static` helper methods should never be instantiated:

```java
public final class MathUtils {

    private MathUtils() {                      // nobody can write new MathUtils()
        throw new AssertionError("No instances");
    }

    public static int square(int x) {
        return x * x;
    }
}

int r = MathUtils.square(5);                   // 25
MathUtils m = new MathUtils();                 // ERROR: MathUtils() has private access in MathUtils
```

(Java's own `Math` class does exactly this.)

### 16.2 Use 2: Singleton (exactly one object)

```java
public class AppConfig {

    private static final AppConfig INSTANCE = new AppConfig();   // the only object, created once

    private AppConfig() { }                                       // nobody else can create one

    public static AppConfig getInstance() {
        return INSTANCE;
    }
}

AppConfig a = AppConfig.getInstance();
AppConfig b = AppConfig.getInstance();
System.out.println(a == b);        // true   (the same single object)
```

### 16.3 Use 3: Force creation through a Builder

```java
public class Pizza {
    private final String size;
    private final boolean cheese;

    private Pizza(Builder b) {                  // only the Builder can call this
        this.size = b.size;
        this.cheese = b.cheese;
    }

    public static class Builder {
        private String size = "medium";
        private boolean cheese = true;

        public Builder size(String size)     { this.size = size;     return this; }
        public Builder cheese(boolean cheese){ this.cheese = cheese; return this; }
        public Pizza build()                 { return new Pizza(this); }
    }
}

Pizza p = new Pizza.Builder().size("large").cheese(false).build();
```

### 16.4 Use 4: Static factory methods (named "constructors")

Constructors can't have descriptive names, and two constructors can't share the same parameter types. **Static factory methods** solve both:

```java
public final class Temperature {
    private final double celsius;

    private Temperature(double celsius) {            // the only real constructor
        this.celsius = celsius;
    }

    public static Temperature ofCelsius(double c) {          // a named creator
        return new Temperature(c);
    }

    public static Temperature ofFahrenheit(double f) {       // same parameter type (double)!
        return new Temperature((f - 32) * 5 / 9);
    }

    public double getCelsius()    { return celsius; }
    public double toFahrenheit()  { return celsius * 9 / 5 + 32; }
}
```

```java
Temperature t1 = Temperature.ofCelsius(100);
Temperature t2 = Temperature.ofFahrenheit(32);

System.out.println(t1.toFahrenheit());     // 212.0
System.out.println(t2.getCelsius());       // 0.0
```

You could **not** write two constructors `Temperature(double celsius)` and `Temperature(double fahrenheit)`: they have the same signature. Factory methods with different names have no such problem.

---

## 17. Constructors in Enums, Records, Abstract Classes and Interfaces

### 17.1 Enums: constructors are always private

```java
enum Size {
    SMALL(1), MEDIUM(2), LARGE(3);

    private final int units;

    Size(int units) {                 // implicitly private
        this.units = units;
    }

    int getUnits() { return units; }
}

System.out.println(Size.LARGE.getUnits());    // 3
```

### 17.2 Records (Java 16+): compact and canonical constructors

A `record` generates a constructor for all its components. You can add validation with a **compact constructor** (no parameter list):

```java
record Point(int x, int y) {

    Point {                                        // compact constructor: runs before fields are assigned
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Coordinates must not be negative");
        }
    }

    Point() {                                      // an extra constructor must delegate with this(...)
        this(0, 0);
    }
}

Point p = new Point(3, 4);
Point origin = new Point();
System.out.println(p);          // Point[x=3, y=4]
System.out.println(origin);     // Point[x=0, y=0]
```

### 17.3 Abstract classes: constructors are allowed

You can't do `new` on an abstract class, but it **can** have constructors, which run via `super(...)` when a subclass object is built.

```java
abstract class Shape {
    protected final String name;

    Shape(String name) {
        this.name = name;
    }

    abstract double area();
}

class Circle extends Shape {
    private final double radius;

    Circle(double radius) {
        super("Circle");               // runs Shape's constructor
        this.radius = radius;
    }

    double area() { return Math.PI * radius * radius; }
}
```

### 17.4 Interfaces and anonymous classes

| Type | Constructors? |
|------|---------------|
| **Interface** | **None** (it has no state to initialize) |
| **Anonymous class** | **Can't declare one** (it has no name). Use an instance initializer block instead |
| **Enum** | Yes, always `private` |
| **Record** | Canonical / compact, and extra ones that delegate |
| **Abstract class** | Yes, called by subclasses with `super(...)` |

---

## 18. Pitfall: Calling Overridable Methods in a Constructor

Remember the order: the **parent constructor runs before the child's fields are initialized**. If a parent constructor calls a method that the **child overrides**, that method runs on a **half-built** object.

```java
class Parent {
    Parent() {
        show();                         // calls the CHILD's version!
    }

    void show() {
        System.out.println("Parent show");
    }
}

class Child extends Parent {
    private String msg = "hello";       // not initialized yet when Parent() runs

    @Override
    void show() {
        System.out.println("msg = " + msg);
    }
}

new Child();
```

Output:
```
msg = null
```

Why: `Parent()` runs first, calls `show()`. Because the object is really a `Child`, the **Child's** `show()` runs, but `Child`'s field `msg` has not yet received `"hello"` (still `null`).

**Rule:** inside a constructor, call only methods that are `private`, `final` or `static`, never methods that a subclass might override.

---

## 19. Constructor vs Method

| | **Constructor** | **Method** |
|---|-----------------|------------|
| Purpose | **Initialize** a new object | **Perform an action** on an object |
| Name | **Must equal the class name** | Any valid name (usually a verb) |
| Return type | **None** (not even `void`) | **Required** (`void` or a type) |
| Called | **Automatically** by `new` | **Explicitly** by you: `obj.method()` |
| Called how many times | Once per object | As many times as you want |
| Inherited | **No** | Yes (unless private/static rules apply) |
| Overridden | **No** | Yes |
| Overloaded | Yes | Yes |
| Can be `static`/`final`/`abstract` | **No** | Yes |
| If you write none | The compiler adds a **default constructor** | Nothing is added |
| Uses `this(...)` / `super(...)` | Yes (first line) | No |

---

## 20. Practical Programs

Save each as the named file, compile with `javac FileName.java`, and run with `java ClassName`.

### 20.1 `Person`: overloading, chaining, validation and a copy constructor

**File: `Person.java`**

```java
public class Person {
    private final String name;
    private final int age;
    private final String city;

    // ----- overloaded constructors, all chained to the master constructor -----
    public Person() {
        this("Unknown");
    }

    public Person(String name) {
        this(name, 0);
    }

    public Person(String name, int age) {
        this(name, age, "N/A");
    }

    // ----- the master constructor (the only place fields are assigned) -----
    public Person(String name, int age, String city) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (age < 0) {
            throw new IllegalArgumentException("age cannot be negative");
        }
        this.name = name;
        this.age = age;
        this.city = city;
    }

    // ----- copy constructor -----
    public Person(Person other) {
        this(other.name, other.age, other.city);
    }

    @Override
    public String toString() {
        return "Person[name=" + name + ", age=" + age + ", city=" + city + "]";
    }

    public static void main(String[] args) {
        Person p1 = new Person();
        Person p2 = new Person("Ali");
        Person p3 = new Person("Sara", 22);
        Person p4 = new Person("John", 30, "Paris");
        Person p5 = new Person(p4);                 // copy of p4

        System.out.println(p1);
        System.out.println(p2);
        System.out.println(p3);
        System.out.println(p4);
        System.out.println(p5);
        System.out.println("p4 == p5 ? " + (p4 == p5));

        try {
            new Person("Bob", -5);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
```

Output:
```
Person[name=Unknown, age=0, city=N/A]
Person[name=Ali, age=0, city=N/A]
Person[name=Sara, age=22, city=N/A]
Person[name=John, age=30, city=Paris]
Person[name=John, age=30, city=Paris]
p4 == p5 ? false
Error: age cannot be negative
```

**What it shows:** four overloaded constructors chained with `this(...)` into one master constructor, validation written once, `final` fields assigned once, and a copy constructor that makes a separate object.

### 20.2 `ChainDemo`: the call order through inheritance

**File: `ChainDemo.java`**

```java
class Vehicle {
    protected String brand;

    Vehicle(String brand) {
        this.brand = brand;
        System.out.println("Vehicle(" + brand + ")");
    }
}

class Car extends Vehicle {
    protected int doors;

    Car(String brand) {
        this(brand, 4);
        System.out.println("Car(" + brand + ")");
    }

    Car(String brand, int doors) {
        super(brand);
        this.doors = doors;
        System.out.println("Car(" + brand + ", " + doors + ")");
    }
}

class ElectricCar extends Car {
    private int rangeKm;

    ElectricCar(String brand, int rangeKm) {
        super(brand);
        this.rangeKm = rangeKm;
        System.out.println("ElectricCar(" + brand + ", " + rangeKm + ")");
    }
}

public class ChainDemo {
    public static void main(String[] args) {
        System.out.println("--- Creating an ElectricCar ---");
        new ElectricCar("Tesla", 500);

        System.out.println("--- Creating a Car with default doors ---");
        new Car("Honda");
    }
}
```

Output:
```
--- Creating an ElectricCar ---
Vehicle(Tesla)
Car(Tesla, 4)
Car(Tesla)
ElectricCar(Tesla, 500)
--- Creating a Car with default doors ---
Vehicle(Honda)
Car(Honda, 4)
Car(Honda)
```

**What it shows:** `super(...)` chaining up the parent classes and `this(...)` chaining inside one class. Constructor bodies finish from the top-most parent down.

### 20.3 `Temperature`: a private constructor with static factory methods

**File: `Temperature.java`**

```java
public final class Temperature {
    private final double celsius;

    private Temperature(double celsius) {
        if (celsius < -273.15) {
            throw new IllegalArgumentException("Below absolute zero");
        }
        this.celsius = celsius;
    }

    public static Temperature ofCelsius(double c) {
        return new Temperature(c);
    }

    public static Temperature ofFahrenheit(double f) {
        return new Temperature((f - 32) * 5 / 9);
    }

    public double getCelsius()   { return celsius; }
    public double toFahrenheit() { return celsius * 9 / 5 + 32; }

    public static void main(String[] args) {
        Temperature boiling = Temperature.ofCelsius(100);
        Temperature freezing = Temperature.ofFahrenheit(32);

        System.out.println(boiling.toFahrenheit());     // 212.0
        System.out.println(freezing.getCelsius());      // 0.0

        try {
            Temperature.ofCelsius(-300);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
```

Output:
```
212.0
0.0
Error: Below absolute zero
```

**What it shows:** a `private` constructor, two named factory methods with the **same parameter type** (impossible with two constructors), validation in one place.

### 20.4 Try it yourself

1. Create a `Rectangle` class with constructors: `Rectangle()` (1×1), `Rectangle(double side)` (a square) and `Rectangle(double w, double h)`. Chain them so only one assigns the fields.
2. Add a copy constructor to `Rectangle` and prove that changing the copy doesn't affect the original.
3. Create `Animal(String name)` and `Dog extends Animal`. Make `Dog()` call `super("Dog")`. Print messages to see the order.
4. Predict the output before you run: a class with a field initializer, an instance block and a constructor (section 12).
5. Write a `Singleton` class and verify with `==` that two calls to `getInstance()` return the same object.

---

## 21. Common Mistakes

| # | Mistake | What happens | Fix |
|---|---------|--------------|-----|
| 1 | `void Student() { }` (return type on a constructor) | It's a normal method, not a constructor | Remove `void` |
| 2 | Misspelling the constructor name (`Studnt`) | "invalid method declaration; return type required" | Match the class name exactly |
| 3 | Adding a parameterized constructor and then calling `new Student()` | Compile error (default constructor is gone) | Write a no-arg constructor yourself |
| 4 | `name = name;` instead of `this.name = name;` | Field never set (stays `null`) | Use `this.name = name;` |
| 5 | `this(...)` not on the first line | Compile error | Make it the first statement |
| 6 | Using `this(...)` and `super(...)` together | Compile error | Use only one; the other constructor handles `super` |
| 7 | Circular `this(...)` calls | "recursive constructor invocation" | Make the chain end at a master constructor |
| 8 | Parent has only a parameterized constructor, child has no `super(...)` | "constructor Parent cannot be applied to given types" | Call `super(args)` or add a no-arg parent constructor |
| 9 | Calling `Student("Ali", 20)` like a method inside another constructor | Compile error | Use `this("Ali", 20)` |
| 10 | `new Student(...)` inside a constructor expecting it to chain | Creates a separate throwaway object | Use `this(...)` |
| 11 | Forgetting to assign a `final` field in some constructor | "variable might not have been initialized" | Assign it in every constructor path |
| 12 | Using instance fields/`this` inside `this(...)`/`super(...)` arguments | "cannot reference before supertype constructor has been called" | Use parameters, constants or static methods |
| 13 | Calling overridable methods from a constructor | Child method runs before child fields are ready | Call only `private`/`final`/`static` methods |
| 14 | Two constructors that differ only by parameter names | "already defined" | Change the parameter **types** or **order**, or use factory methods |
| 15 | Assuming `Student b = a;` copies the object | Both variables share one object | Use a copy constructor |
| 16 | Shallow copy of an array/object field in a copy constructor | Copies still share inner data | Copy the inner object too |
| 17 | Doing heavy work (network, big loops) in a constructor | Slow, hard-to-handle failures | Use a factory method or an `init()` method |
| 18 | Making a utility class instantiable | Pointless objects | Add a `private` constructor |

---

## 22. Cheat Sheet, Summary and Quiz

### 22.1 Syntax cheat sheet

```java
public class Student {
    private final String name;
    private final int age;

    // default constructor: NOT written; the compiler adds it only if NO constructor exists

    // no-argument constructor (written by you)
    public Student() {
        this("Unknown", 18);                 // chaining: this(...)
    }

    // parameterized constructor + overloading
    public Student(String name) {
        this(name, 18);
    }

    // master constructor
    public Student(String name, int age) {
        this.name = name;                    // this.field = parameter
        this.age = age;
    }

    // copy constructor
    public Student(Student other) {
        this(other.name, other.age);
    }
}

class GradStudent extends Student {
    GradStudent(String name) {
        super(name, 24);                     // chaining to the parent: super(...)
    }
}

// private constructor + static factory
class Util {
    private Util() { }
    static Util create() { return new Util(); }
}
```

### 22.2 Quick reference

| You want to... | Write |
|----------------|-------|
| Create an object | `new ClassName(args)` |
| Separate field from parameter | `this.name = name;` |
| Call another constructor in the same class | `this(args);` (first line) |
| Call the parent constructor | `super(args);` (first line) |
| Make several ways to create an object | Overload the constructors |
| Avoid repeating code between constructors | Chain them to one master constructor |
| Copy an object | Copy constructor: `ClassName(ClassName other)` |
| Prevent creating objects | `private` constructor |
| Allow exactly one object | Singleton: `private` constructor + static `getInstance()` |
| Make a field that never changes | `private final` field assigned in the constructor |
| Reject invalid input | `throw new IllegalArgumentException(...)` in the constructor |

### 22.3 Summary in simple points

1. A **constructor** runs **automatically** when you use `new`, to **initialize** the new object.
2. Its name **equals the class name** and it has **no return type**. Adding `void` turns it into an ordinary method.
3. If you write **no constructor**, Java adds a **default constructor** (no parameters, calls `super()`, same access as the class). **If you write any constructor, the default is not added.**
4. A **no-arg constructor** is any constructor with no parameters; the **default constructor** is the one the compiler generates.
5. **`this`** is a reference to the **current object**. `this.name = name;` separates the field from the parameter. `this(...)` calls another constructor of the **same class**. `this` can't be used in static code.
6. **Constructor overloading** = several constructors with **different parameter lists** (number, type or order). Java chooses by the arguments (exact/widening, then boxing, then varargs).
7. **Constructor chaining** removes repeated code: `this(...)` chains **within a class**, `super(...)` chains **to the parent**. Both must be the **first statement**, and you **can't use both** in one constructor.
8. If you don't write `this(...)` or `super(...)`, the compiler inserts **`super();`**. If the parent has no no-arg constructor, you must call `super(args)` yourself.
9. In a chain, the **last constructor's body runs first**. With inheritance, bodies finish from **`Object` down to your class**.
10. Initialization order: **defaults, then field initializers and instance blocks (in source order), then the constructor body**. Parent first, then child.
11. **Copy constructors** create a separate object; remember **shallow vs deep** copy for object/array fields.
12. **`final` fields** must be assigned **exactly once** by the end of every constructor.
13. **Validate** input in the (master) constructor and throw an exception. An invalid object then can't exist.
14. **Private constructors** enable utility classes, singletons, builders and static factory methods.
15. Constructors are **not inherited** and **can't be overridden**. Don't call **overridable methods** from a constructor.

### 22.4 Mini Quiz

1. Can a constructor have a return type? What happens if you write `void` in front of it?
2. When does Java add a default constructor, and what does it contain?
3. A class has only `Student(String name)`. Does `new Student()` compile?
4. What is the difference between a default constructor and a no-argument constructor?
5. Where must `this(...)` and `super(...)` be placed in a constructor?
6. What does `name = name;` do in a constructor `Student(String name)`?
7. `Student()` calls `this("Unknown")`, which calls `this("Unknown", 18)`, and each prints its own name after the `this(...)` line. In which order do the print statements appear?
8. `class Parent { Parent(String s) { } }` and `class Child extends Parent { }`. Does this compile? Why or why not? How do you fix it?
9. Can two constructors differ only in their parameter names?
10. Can a constructor be `static` or `final`? Can it be inherited or overridden?
11. Why must every constructor assign a `final` field?
12. What is the output order for creating `new Child()` where both classes have a field initializer, an instance block and a constructor?
13. What is a `private` constructor used for? Name two uses.
14. Why is calling an overridable method from a constructor dangerous?

<details>
<summary>Click for answers</summary>

1. **No.** A constructor has no return type. If you write `void`, it becomes an **ordinary method** that happens to have the class's name, and it is not run by `new`.
2. **Only if the class has no constructors at all.** It has **no parameters**, its body only calls **`super()`**, and it has the **same access modifier as the class**.
3. **No.** Writing a constructor removes the default one. You get "constructor Student cannot be applied to given types". Write `Student() { }` yourself.
4. The **default constructor** is generated by the **compiler**. A **no-arg constructor** is **any** constructor with no parameters, including one you wrote.
5. As the **first statement**, and you can't use both in the same constructor.
6. It assigns the **parameter to itself**. The field stays at its default (`null`). Use `this.name = name;`.
7. **Innermost first:** the three-argument/two-argument constructor body, then `Student(String)`, then `Student()`. The one you called originally prints **last**.
8. **No.** `Child`'s default constructor tries `super()`, but `Parent` has no no-arg constructor. Fix: write `Child() { super("text"); }` or add a no-arg constructor to `Parent`.
9. **No.** Only the **types, number and order** of parameters matter, not their names. It would be a duplicate constructor.
10. **No** to `static`/`final`. Constructors are **not inherited** and **can't be overridden** (but they can be overloaded).
11. So that after construction **every `final` field has exactly one value**. The compiler checks every path (including chains) and rejects the class otherwise.
12. **Parent** field initializer, **Parent** instance block, **Parent** constructor body, then **Child** field initializer, **Child** instance block, **Child** constructor body.
13. To **block `new` from other classes**. Uses: utility classes (no objects), singletons (one object), builders, static factory methods.
14. The parent constructor runs **before the child's fields are initialized**, so the child's overriding method may run on a half-built object and see `null`/`0` values.

</details>

---

*Practice tip: add a `System.out.println` line to every constructor you write, create objects in different ways, and watch the order. Once you can predict the output of a constructor chain, you understand constructors.*
