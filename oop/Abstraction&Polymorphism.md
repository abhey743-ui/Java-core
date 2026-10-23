# Abstraction & Polymorphism in Java
### First the core idea, then how Java implements it, then why, advantages and disadvantages

> **How to read these notes:** each big topic goes in the same order:
> **(1) Core idea in real life → (2) Why we need it → (3) How Java does it → (4) Advantages → (5) Disadvantages / traps.**
> If you forget something later, re-read only the "Core idea" boxes and you will rebuild everything.

---

## Table of Contents

**PART A: ABSTRACTION**
1. [Abstraction: the core idea](#1-abstraction-the-core-idea)
2. [Two levels of abstraction in Java](#2-two-levels-of-abstraction-in-java)
3. [Abstract classes](#3-abstract-classes)
4. [Interfaces](#4-interfaces)
5. [Abstract class vs Interface](#5-abstract-class-vs-interface)
6. [Abstraction vs Encapsulation](#6-abstraction-vs-encapsulation)
7. [Advantages and disadvantages of abstraction](#7-advantages-and-disadvantages-of-abstraction)

**PART B: POLYMORPHISM**
8. [Polymorphism: the core idea](#8-polymorphism-the-core-idea)
9. [The big link: how inheritance gives polymorphism](#9-the-big-link-how-inheritance-gives-polymorphism)
10. [Static polymorphism (method overloading)](#10-static-polymorphism-method-overloading)
11. [Dynamic polymorphism (method overriding)](#11-dynamic-polymorphism-method-overriding)
12. [How the JVM picks the method (binding)](#12-how-the-jvm-picks-the-method-binding)
13. [static, final, private and polymorphism](#13-static-final-private-and-polymorphism)
14. [Polymorphism with abstract classes and interfaces](#14-polymorphism-with-abstract-classes-and-interfaces)
15. [Overloading vs Overriding](#15-overloading-vs-overriding)
16. [Advantages and disadvantages of polymorphism](#16-advantages-and-disadvantages-of-polymorphism)

**PART C: REVISION**
17. [How all OOP pillars connect](#17-how-all-oop-pillars-connect)
18. [Interview questions with answers](#18-interview-questions-with-answers)
19. [Cheat sheet and common mistakes](#19-cheat-sheet-and-common-mistakes)

---

# PART A: ABSTRACTION

## 1. Abstraction: the core idea

### 1.1 First-principles idea

> **CORE IDEA:** Show only **what** something does. Hide **how** it does it.

The world is too complex to understand fully. Our brain survives by **ignoring unnecessary details** and keeping only the details that matter for the task at hand. That act of ignoring details is **abstraction**.

### 1.2 Real-life examples

**Driving a car.**
You use the steering wheel, accelerator and brake. You do **not** need to know how fuel injection, gear ratios or the ignition system work.

```
 WHAT YOU SEE (the interface)          WHAT IS HIDDEN (the implementation)
+----------------------------+        +----------------------------------+
|  start()                   |        |  fuel pump, spark plugs, pistons |
|  accelerate()              | -----> |  gearbox, ECU software, sensors  |
|  brake()                   |        |  hundreds of internal steps      |
+----------------------------+        +----------------------------------+
```

**An ATM:** you press "Withdraw". Authentication, balance check, ledger update, cash dispensing are hidden.

**A TV remote:** you press "Volume +". Infrared signal, circuits, amplifier are hidden.

**Google Maps:** you type a destination. Graph algorithms, traffic data, satellites are hidden.

### 1.3 Why does the world need abstraction?

1. **Reduces complexity:** a human can only hold a few things in mind at once.
2. **Lets you change the inside without affecting users:** replace the engine, the driver still uses the same pedals.
3. **Lets many people work in parallel:** one team builds the engine, another builds the dashboard, using only the agreed controls.
4. **Safety:** users cannot break what they cannot reach.

### 1.4 The same idea in software

In programming, abstraction means designing a class or module so that:

- Users see a **small, clean list of operations** (the **contract**).
- The messy details (algorithms, database calls, network handling) are **hidden** behind it.

You already use abstraction every day:

```java
List<Integer> list = new ArrayList<>();
list.add(5);          // you don't know how ArrayList resizes its internal array
Collections.sort(list); // you don't know which sorting algorithm runs inside
```

---

## 2. Two levels of abstraction in Java

Your lecture splits abstraction into two levels. Think of them as **"small scale"** and **"big scale"**.

```
 LOW-LEVEL abstraction    : hide HOW a method works          (classes + methods + access modifiers)
 HIGH-LEVEL abstraction   : hide WHICH implementation is used (abstract classes + interfaces)
```

### 2.1 Low-level abstraction (classes and methods)

A normal class with `private` internals and `public` methods already hides the "how".

```java
class Car {
    private void injectFuel()     { /* complex steps */ }
    private void igniteEngine()   { /* complex steps */ }
    private void engageGears()    { /* complex steps */ }

    public void start() {         // user only knows this
        injectFuel();
        igniteEngine();
    }

    public void accelerate() {
        engageGears();
        injectFuel();
    }
}

Car c = new Car();
c.start();         // easy
c.accelerate();    // easy
```

The user calls `start()` and `accelerate()` and never learns the mechanics. Here the abstraction is **per class**: one specific implementation is hidden.

### 2.2 High-level abstraction (abstract classes and interfaces)

Now suppose there are many kinds of vehicles (Car, Bike, Truck), and every vehicle must support `start()`. We want to say:

> "Whatever a vehicle is, it **must** be able to `start()`. How it starts is each vehicle's own business."

That is the separation of the **"what" (contract)** from the **"how" (implementation)**. Java gives two tools for it:

| Tool | Use it when |
|---|---|
| **Abstract class** | Related classes share some **common code and state**, but some behavior must be filled in by each child |
| **Interface** | You want a pure **contract** (capabilities) that unrelated classes can promise to follow |

```
                 Vehicle (contract: start())
                 /          |          \
              Car          Bike        Truck
        (key + engine) (kick/button) (air brake + diesel)
        each one implements start() in its OWN way
```

The code using a `Vehicle` does not care which one it holds. That connects directly to polymorphism (Part B).

---

## 3. Abstract classes

### 3.1 Core idea

A class that is **incomplete on purpose**. It describes a **general idea** ("a Shape", "a Vehicle") that is too vague to exist as a real object, so you are **not allowed to create objects of it**. It may leave some methods **without a body**, forcing children to provide them.

Real-life: "Animal" is an idea. You never meet "just an animal". You meet a dog, a cat, a sparrow. "Animal" is abstract; Dog and Cat are concrete.

### 3.2 Syntax

```java
abstract class Shape {                     // abstract class
    String color;                          // can have fields

    Shape(String color) {                  // can have constructors
        this.color = color;
    }

    abstract double area();                // abstract method: NO body

    void printColor() {                    // normal (concrete) method
        System.out.println("Color: " + color);
    }
}

class Circle extends Shape {
    double r;
    Circle(String color, double r) { super(color); this.r = r; }

    @Override
    double area() { return Math.PI * r * r; }     // must implement
}

class Rectangle extends Shape {
    double w, h;
    Rectangle(String color, double w, double h) { super(color); this.w = w; this.h = h; }

    @Override
    double area() { return w * h; }
}

public class Main {
    public static void main(String[] args) {
        // Shape s = new Shape("red");      // ERROR: Shape is abstract; cannot be instantiated
        Shape s1 = new Circle("red", 3);
        Shape s2 = new Rectangle("blue", 4, 5);
        System.out.println(s1.area());      // circle area
        System.out.println(s2.area());      // rectangle area
        s1.printColor();                    // shared code from the abstract class
    }
}
```

### 3.3 Rules of abstract classes

1. Declared with the `abstract` keyword.
2. **Cannot be instantiated** (`new Shape()` is an error). But you can create a **reference** of its type (`Shape s = new Circle(...)`).
3. Can contain **abstract methods** (no body) **and concrete methods** (with body).
4. Can have **fields, constructors, static members, blocks, final methods**.
5. Its **constructor runs** when a child object is created (via `super(...)`). That is how its fields get initialized.
6. A class that has **even one** abstract method **must** be declared abstract.
7. A class can be abstract **without any abstract method** (to simply forbid instantiation).
8. A **child class must implement all abstract methods**, otherwise the child must also be declared `abstract`.
9. Abstract classes support **single inheritance only** (one `extends`).

### 3.4 Rules of abstract methods

```java
abstract void draw();       // ends with ; no { }
```

| Illegal combination | Why |
|---|---|
| `abstract` + `private` | Abstract needs the child to override it; private is invisible to the child |
| `abstract` + `final` | `final` forbids overriding; abstract demands overriding |
| `abstract` + `static` | Static methods can't be overridden |
| `abstract` + body `{ }` | Abstract means "no body" |
| `abstract final class` | Abstract needs to be extended; final forbids it |

Allowed access for abstract methods: `public`, `protected`, default.

### 3.5 If the child forgets to implement

```java
class Triangle extends Shape {
    Triangle(String c) { super(c); }
    // forgot area()
}
// ERROR: Triangle is not abstract and does not override abstract method area() in Shape
```

Fix: implement `area()` or mark `Triangle` as `abstract`.

### 3.6 Template Method pattern (a real use of abstract classes)

The abstract class fixes the **steps**, children fill in the **details**.

```java
abstract class DataProcessor {
    public final void process() {         // fixed algorithm (final: can't be changed)
        readData();
        transform();
        saveData();
    }
    abstract void readData();             // children decide how
    abstract void transform();
    void saveData() { System.out.println("Saved to default DB"); }
}
```

### 3.7 Advantages and disadvantages (quick)

**Advantages:** code reuse (shared fields and methods), can enforce rules on children, can have state and constructors, can add new concrete methods later without breaking children.

**Disadvantages:** child can extend **only one** abstract class; tight coupling (child depends on parent's design); not suitable for unrelated classes.

---

## 4. Interfaces

### 4.1 Core idea

An interface is a **pure contract**: a list of abilities a class **promises** to provide. It says **what** must be done and says nothing about how.

Real-life: a **power socket standard**. Any device with the right plug works in any socket. The socket does not care if it is a phone charger or a laptop. Another one: **USB**. Any USB device follows the same contract.

Another view: an interface is a **job description**. "Must be able to: code(), test(), deploy()". Anyone who can do these can take the job.

### 4.2 Syntax

```java
interface Vehicle {
    void start();             // implicitly public abstract
    void stop();
}

class Car implements Vehicle {
    @Override
    public void start() { System.out.println("Car starts with a key"); }   // must be public
    @Override
    public void stop()  { System.out.println("Car stops"); }
}

class Bike implements Vehicle {
    @Override
    public void start() { System.out.println("Bike starts with a kick"); }
    @Override
    public void stop()  { System.out.println("Bike stops"); }
}

Vehicle v = new Car();
v.start();
v = new Bike();
v.start();
```

### 4.3 What an interface can contain

| Member | Rules |
|---|---|
| **Abstract methods** | `public abstract` automatically (no need to write it) |
| **Constants (fields)** | Always `public static final`; must be initialized |
| **`default` methods** (Java 8) | Methods with body; implementing classes inherit them and may override |
| **`static` methods** (Java 8) | Called with `InterfaceName.method()`; not inherited by implementing classes |
| **`private` methods** (Java 9) | Helpers to share code between default methods |
| **Nested types** | Allowed |
| **Constructors** | **Not allowed** |
| **Instance fields (state)** | **Not allowed** |

```java
interface Payment {
    double FEE_RATE = 0.02;                       // public static final

    void pay(double amount);                      // abstract

    default void printReceipt(double amount) {    // default method
        System.out.println("Paid " + amount + " (fee " + fee(amount) + ")");
    }

    private double fee(double amount) {           // private helper (Java 9+)
        return amount * FEE_RATE;
    }

    static boolean isValid(double amount) {       // static method
        return amount > 0;
    }
}
```

**Why default methods were added (Java 8):** to add new methods to an old interface (like `List.forEach`) **without breaking** every class that already implements it.

### 4.4 Rules of interfaces

1. A class uses **`implements`**; it must implement **all** abstract methods (or be abstract itself).
2. A class can implement **many** interfaces (this is Java's multiple inheritance).
3. An interface can **`extends` many interfaces**.
4. Implemented methods must be **`public`** (cannot reduce access from public).
5. You **cannot create an object** of an interface (`new Vehicle()` is an error), but you can use it as a **reference type**. (You can use an anonymous class or lambda.)
6. Interface fields are constants: `Vehicle.MAX_SPEED`.
7. If two interfaces give the same default method, the class **must override** it and pick (`A.super.show()`).
8. An interface with exactly one abstract method is a **functional interface** (can use lambdas), e.g. `Runnable`, `Comparator`.
9. An empty interface is a **marker interface** (e.g. `Serializable`).

### 4.5 Multiple interfaces

```java
interface Flyable { void fly(); }
interface Swimmable { void swim(); }

class Duck implements Flyable, Swimmable {
    public void fly()  { System.out.println("Duck flies"); }
    public void swim() { System.out.println("Duck swims"); }
}
```

### 4.6 Advantages and disadvantages (quick)

**Advantages:** 100% separation of contract and implementation; multiple inheritance of type; unrelated classes can share a capability; loose coupling; easy to swap implementations and to test with fake versions.

**Disadvantages:** no instance state or constructors; changing an interface (adding abstract methods) breaks all implementers (default methods soften this); too many tiny interfaces make code harder to navigate.

---

## 5. Abstract class vs Interface

### 5.1 The idea difference (this is what interviewers want)

```
 Abstract class  =  "IS-A, with shared body"       (a partially built thing)
 Interface       =  "CAN-DO, a capability"          (a promise / contract)
```

- `Dog` **is an** `Animal` → abstract class (shared state like `name`, shared code like `eat()`).
- `Dog` **can** `Swim`, **can** `Run` → interfaces.
- `Bird` and `Aeroplane` are unrelated, but both **can** `Fly` → interface.

### 5.2 Detailed comparison

| Feature | Abstract class | Interface |
|---|---|---|
| Keyword | `abstract class` / `extends` | `interface` / `implements` |
| Design intent | Common base for **related** classes (IS-A) | Capability/contract for **any** classes (CAN-DO) |
| Multiple inheritance | **No** (one parent) | **Yes** (many interfaces) |
| Methods | Abstract + concrete | Abstract + default + static + private |
| Fields | Any kind (instance, static, final) | Only `public static final` constants |
| Constructors | **Yes** | **No** |
| Instance state | **Yes** | **No** |
| Access modifiers on members | public, protected, default, private | Methods public (private for helpers); fields public |
| Instantiate? | No | No |
| Can extend/implement | Extends one class, implements interfaces | Extends many interfaces |
| Abstraction level | Partial (0 to 100%) | Traditionally 100% (contract only) |
| Speed of evolution | Add concrete methods freely | Add default methods carefully |
| Typical examples | `AbstractList`, `HttpServlet` | `List`, `Runnable`, `Comparable` |

### 5.3 When to use which

**Use an abstract class when:**
- Related classes share **fields** or **common code**.
- You need **constructors** or **non-public members**.
- You want to give a **template** and let children fill in the gaps.

**Use an interface when:**
- You want to define a **role/capability** that unrelated classes can take.
- You need **multiple inheritance** of type.
- You want maximum **flexibility** and **loose coupling** (program to an interface).

**Both together (very common):**

```java
interface Vehicle { void start(); }

abstract class BaseVehicle implements Vehicle {   // shared code + partial contract
    protected String model;
    BaseVehicle(String model) { this.model = model; }
    public void stop() { System.out.println(model + " stopped"); }
}

class Car extends BaseVehicle {
    Car(String m) { super(m); }
    public void start() { System.out.println(model + " starts"); }
}
```

This is exactly how the Java collections are built: `List` (interface) → `AbstractList` (abstract class) → `ArrayList` (concrete class).

---

## 6. Abstraction vs Encapsulation

These two are confused all the time. Use this memory trick:

```
 ABSTRACTION   = design level  = "WHAT do I show?"        (hide complexity, show essentials)
 ENCAPSULATION = code level    = "HOW do I protect data?"  (private fields + public methods)
```

| | Abstraction | Encapsulation |
|---|---|---|
| Question it answers | What should the user see/use? | How do I protect and bundle the data? |
| Focus | Hiding **complexity / implementation** | Hiding and protecting **data** |
| Level | Design level (what to expose) | Implementation level (how to guard) |
| Achieved by | Abstract classes, interfaces, well-designed public methods | Access modifiers (`private` fields, getters/setters) |
| Real example | Pressing "start" in a car | The engine is sealed under the hood, you can't touch it |
| Result | Simpler usage | Safer data |

**Analogy: a mobile phone.**
- *Abstraction:* you see icons and tap. You don't see code and circuits.
- *Encapsulation:* the phone body keeps the battery and circuits sealed so you can't damage them by accident.

> They work **together**: abstraction decides **which** things are exposed; encapsulation makes sure everything else is **locked away** and that exposed data is accessed safely. Data hiding (via `private`) is a tool of encapsulation that **supports** abstraction.

---

## 7. Advantages and disadvantages of abstraction

### Advantages

1. **Reduces complexity:** users learn a small interface, not the whole system.
2. **Easier maintenance:** change the internals, nobody outside notices.
3. **Loose coupling:** code depends on `Vehicle`, not on `Car`.
4. **Reusability:** common behavior written once in the abstract class.
5. **Security:** internal details are not exposed.
6. **Parallel teamwork:** teams agree on the contract first.
7. **Enables polymorphism and easy testing** (swap real DB for a fake one).

### Disadvantages / trade-offs

1. **More classes and files** (interfaces + abstract classes + implementations), which means a heavier design.
2. **Over-abstraction:** layers on layers make code hard to follow ("where is the real code?").
3. **Slight indirection cost** (calls go through an interface; the JVM usually optimizes it well).
4. **Wrong abstraction is expensive:** changing a widely used interface breaks every implementer.
5. **Learning curve** for beginners.
6. **Debugging** can be harder because the real code is a few hops away.

**Rule of thumb:** abstract only what is likely to **vary** or what users do not need to know. Do not abstract "just in case".

---

# PART B: POLYMORPHISM

## 8. Polymorphism: the core idea

### 8.1 First-principles idea

> **CORE IDEA:** **One name, many forms.** The *same command* can produce *different behavior* depending on **who receives it** (or **what you give it**).

The word comes from Greek: *poly* = many, *morph* = forms.

### 8.2 Real-life examples

**One command "speak" to different animals:**

```
 speak() -->  Dog  : "Woof"
 speak() -->  Cat  : "Meow"
 speak() -->  Cow  : "Moo"
```

You don't need a different command for each. The command is the same; **each receiver does it in its own way**.

**A person behaves differently by role.** The same person is a son at home, an employee at work, a customer at a shop. Same person, different forms.

**The "+" symbol:** `2 + 3 = 5` (addition) but `"Hi" + " there" = "Hi there"` (joining). Same symbol, different behavior depending on what it is given.

**A "Play" button:** same button, plays music in a music app, plays a video in a video app.

**"Pay" at a shop:** you say "pay", and it works as cash, card or UPI. Same action, different internal behavior.

### 8.3 Why does the world need it?

Without polymorphism you would need a **separate command for everything**: `speakDog()`, `speakCat()`, `speakCow()`, and every time a new animal appears you must change the code that gives the commands. With polymorphism you write the commander **once**, and new receivers just plug in.

### 8.4 Two flavors (the lecture's main split)

```
                    POLYMORPHISM
                   /            \
     Compile-time               Run-time
     (Static)                   (Dynamic)
     "which method?"            "which method?"
     decided by the             decided by the
     COMPILER                   JVM at EXECUTION
     using the PARAMETERS       using the ACTUAL OBJECT
         |                          |
   Method OVERLOADING          Method OVERRIDING
```

---

## 9. The big link: how inheritance gives polymorphism

This is the part people forget, so here it is as a chain of simple facts.

### 9.1 The chain

```
 STEP 1: Inheritance      -> Child IS-A Parent. (Dog is an Animal)
 STEP 2: Overriding       -> Child gives its own version of a parent's method.
 STEP 3: Upcasting        -> A PARENT reference can point to a CHILD object.
                              Animal a = new Dog();
 STEP 4: Dynamic dispatch -> When you call a.speak(), the JVM looks at the
                              ACTUAL OBJECT (Dog), not the reference type (Animal).
 RESULT: Polymorphism     -> The same call a.speak() behaves differently for
                              different objects.
```

### 9.2 Walk through it

**Step 1: Inheritance** gives the *relationship*.

```java
class Animal {
    void speak() { System.out.println("Some sound"); }
}
class Dog extends Animal { }
class Cat extends Animal { }
```

Because `Dog` IS-A `Animal`, a Dog can be used wherever an Animal is expected. That is what makes **Step 3** legal.

**Step 2: Overriding** gives the *different behavior*.

```java
class Dog extends Animal {
    @Override void speak() { System.out.println("Woof"); }
}
class Cat extends Animal {
    @Override void speak() { System.out.println("Meow"); }
}
```

**Step 3: Upcasting** gives the *common handle*.

```java
Animal a1 = new Dog();    // parent reference, child object
Animal a2 = new Cat();
```

**Step 4: Dynamic dispatch** gives the *magic*.

```java
a1.speak();   // Woof   (object is Dog)
a2.speak();   // Meow   (object is Cat)
```

```
   REFERENCE (compiler checks)          OBJECT (JVM uses at runtime)
   +--------------------+              +----------------------+
   | Animal a1  --------+------------> | Dog object           |
   +--------------------+              |   speak() = "Woof"   |
                                       +----------------------+
   Compiler asks: "Does Animal have speak()?"  -> yes, allowed.
   JVM asks:      "What is the real object?"   -> Dog, run Dog's speak().
```

### 9.3 Remove any link, polymorphism breaks

| If you remove... | What happens |
|---|---|
| Inheritance / interface | A parent reference can't hold the child. No common handle. |
| Overriding | All children behave the same. Nothing "many form" about it. |
| Upcasting (use `Dog d = new Dog()`) | Works, but you are not using polymorphism; you are tied to one type. |

### 9.4 The payoff: write once, works for all

```java
static void makeItSpeak(Animal a) {      // knows only about Animal
    a.speak();
}

makeItSpeak(new Dog());    // Woof
makeItSpeak(new Cat());    // Meow
makeItSpeak(new Cow());    // add a new class later: makeItSpeak needs NO change
```

```java
Animal[] zoo = { new Dog(), new Cat(), new Cow() };
for (Animal a : zoo) {
    a.speak();               // each one behaves in its own way
}
```

This is the **Open/Closed Principle**: open for extension (add new animals), closed for modification (no change to the loop).

### 9.5 Without polymorphism (the ugly version)

```java
if (animal instanceof Dog)       { /* dog speak */ }
else if (animal instanceof Cat)  { /* cat speak */ }
else if (animal instanceof Cow)  { /* cow speak */ }
// every new animal = edit this chain everywhere
```

Polymorphism replaces long `if / else` or `switch` chains by letting each object carry its own behavior.

---

## 10. Static polymorphism (method overloading)

### 10.1 Core idea

**Same method name, different parameters.** The compiler looks at **what you pass** and picks the matching version **at compile time**. Also called **compile-time polymorphism** or **early binding**.

Real-life: the verb "**print**". "Print a photo", "print a document", "print a ticket": same word, the action adapts to what you give it.

### 10.2 Example

```java
class Calculator {
    int add(int a, int b)            { return a + b; }
    int add(int a, int b, int c)     { return a + b + c; }       // different count
    double add(double a, double b)   { return a + b; }           // different type
    String add(String a, String b)   { return a + b; }           // different type
}

Calculator c = new Calculator();
c.add(2, 3);          // 5          -> int, int
c.add(2, 3, 4);       // 9          -> three ints
c.add(2.5, 3.5);      // 6.0        -> doubles
c.add("Hi ", "Java"); // Hi Java    -> Strings
```

### 10.3 What makes a valid overload?

Methods must differ in the **parameter list**: number, **type**, or **order** of types.

```java
void show(int a, String b) { }
void show(String a, int b) { }     // OK: different ORDER
```

**Return type alone is NOT enough:**

```java
int get() { return 1; }
double get() { return 1.0; }       // ERROR: method already defined (same parameters)
```

Why? When you write `get();`, the compiler decides using the **call**, and the call does not say which return type you want. So return type cannot be used to choose.

Also not enough: different parameter **names**, different access modifier, different `throws`.

### 10.4 How the compiler chooses (resolution order)

When several overloads could match, the compiler tries, in order:

1. **Exact match**
2. **Widening** (`int` to `long`, `float`, `double`)
3. **Boxing / unboxing** (`int` to `Integer`)
4. **Varargs** (`int...`)

```java
class Demo {
    void show(long x)    { System.out.println("long"); }
    void show(Integer x) { System.out.println("Integer"); }
    void show(int... x)  { System.out.println("varargs"); }
}
new Demo().show(5);      // prints: long   (widening beats boxing beats varargs)
```

### 10.5 Ambiguity trap

```java
void test(String s)  { }
void test(Integer i) { }

test(null);     // ERROR: ambiguous (null fits both)
```

### 10.6 Important: overloading uses the **reference type**, not the object

```java
class Printer {
    void print(Animal a) { System.out.println("Animal"); }
    void print(Dog d)    { System.out.println("Dog"); }
}

Animal a = new Dog();
new Printer().print(a);    // prints: Animal   (!!)
```

The compiler only sees `a` as `Animal` (declared type) and picks `print(Animal)` **at compile time**. The real object (Dog) is ignored. This is the key reason overloading is called **static** polymorphism.

### 10.7 Other points

- Overloading works in the **same class** or between a **parent and child** (child can add an overload).
- **Constructors** can be overloaded too (`Student()`, `Student(String)`, `Student(String, int)`).
- `main` can be overloaded, but the JVM starts only with `String[]`.
- `static` methods can be overloaded.
- Operator overloading is **not** supported in Java for user classes. The only built-in case is `+` working for numbers and Strings.

### 10.8 Advantages and disadvantages

**Advantages:** readable (one name for one idea, like `println` for every type), flexible API, no need to invent names like `addInt`, `addDouble`, resolved at compile time so no runtime cost.

**Disadvantages:** too many overloads confuse readers; implicit conversion and boxing can pick a method you did not expect; behaves by **declared type**, which surprises people (10.6); ambiguity errors.

---

## 11. Dynamic polymorphism (method overriding)

### 11.1 Core idea

**Same method (same signature) in parent and child. The child's version replaces the parent's for child objects.** Which version runs is decided **at runtime** by the **actual object**. Also called **runtime polymorphism**, **late binding** or **dynamic method dispatch**.

### 11.2 Example

```java
class Payment {
    void pay(double amount) {
        System.out.println("Paying " + amount + " by generic method");
    }
}
class CardPayment extends Payment {
    @Override
    void pay(double amount) {
        System.out.println("Paying " + amount + " by Card");
    }
}
class UpiPayment extends Payment {
    @Override
    void pay(double amount) {
        System.out.println("Paying " + amount + " by UPI");
    }
}

public class Main {
    static void checkout(Payment p, double amt) {
        p.pay(amt);                 // decided at runtime
    }

    public static void main(String[] args) {
        checkout(new CardPayment(), 500);   // Card
        checkout(new UpiPayment(), 700);    // UPI
    }
}
```

### 11.3 Rules of overriding (quick recap)

| Rule | Detail |
|---|---|
| Same name and parameters | Different parameters means overloading |
| Return type | Same, or a subtype (covariant) |
| Access | Same or wider, never narrower |
| Checked exceptions | Not broader than the parent's |
| `final`, `static`, `private` methods | Cannot be overridden (see Section 13) |
| Needs inheritance | Child must extend (or implement) the parent |
| Use `@Override` | Compiler catches typos like `speek()` |

### 11.4 `super` inside overriding

A child can reuse the parent's version, then add its own:

```java
@Override
void pay(double amount) {
    super.pay(amount);                 // parent's behavior first
    System.out.println("plus card cashback");
}
```

### 11.5 Covariant return type

```java
class Animal { Animal create() { return new Animal(); } }
class Dog extends Animal {
    @Override
    Dog create() { return new Dog(); }     // OK: Dog is a subtype of Animal
}
```

### 11.6 Fields are NOT overridden

Only **methods** are polymorphic. Fields are chosen by the **reference type**:

```java
class A { String name = "A"; }
class B extends A { String name = "B"; }

A obj = new B();
System.out.println(obj.name);    // A
```

### 11.7 Advantages and disadvantages

**Advantages:** flexible, extensible design (add new types without changing old code); removes `if/else` chains; core of frameworks (Spring, JDBC drivers, collections); easy to plug in new behavior.

**Disadvantages:** slightly harder to trace (you must know the real object); small runtime lookup cost (usually optimized away by the JIT); bad inheritance design leads to fragile code; overriding without `@Override` can silently fail (typo makes a new method); cannot be used for `static`/`final`/`private` methods.

---

## 12. How the JVM picks the method (binding)

**Binding** = connecting a method **call** to the method **code**.

| | Static (early) binding | Dynamic (late) binding |
|---|---|---|
| When | Compile time | Runtime |
| Decided by | **Reference type** and parameters | **Actual object** |
| Used for | Overloaded methods, `static`, `private`, `final` methods, constructors, fields | Overridden instance methods |
| Speed | Fastest | Tiny lookup |

### 12.1 Simple mental model: the method table (vtable)

Each class keeps a table of its methods. A child's table starts as a copy of the parent's, then overridden entries are replaced with the child's own code.

```
 Animal's table                 Dog's table (copy + override)
 +------------------+           +------------------+
 | speak() -> Animal|           | speak() -> Dog   |   <- replaced
 | eat()   -> Animal|           | eat()   -> Animal|   <- inherited as is
 +------------------+           | fetch() -> Dog   |   <- new
                                +------------------+

 a.speak() at runtime:
   1. follow the reference to the real object (Dog)
   2. look up speak() in Dog's table
   3. run that code
```

### 12.2 Compile time vs runtime check (two steps)

```java
Animal a = new Dog();
a.speak();       // legal?  COMPILER checks: does type Animal have speak()?   yes
                 // which?  JVM decides: object is Dog -> Dog.speak()
a.fetch();       // ERROR at compile time: Animal has no fetch()
```

The **reference type decides what you are allowed to call**. The **object type decides which version runs**.

---

## 13. static, final, private and polymorphism

Overriding needs three conditions: the method must be **inherited**, **visible** and **replaceable**. These three keywords break one of the conditions.

### 13.1 `static` methods: hidden, not overridden

```java
class Animal {
    static void info() { System.out.println("Animal info"); }
    void speak()       { System.out.println("Animal speak"); }
}
class Dog extends Animal {
    static void info() { System.out.println("Dog info"); }      // HIDES, not overrides
    @Override void speak() { System.out.println("Dog speak"); }
}

Animal a = new Dog();
a.speak();    // Dog speak     (dynamic)
a.info();     // Animal info   (static: decided by reference type Animal)
```

Why: static methods belong to the **class**, not to an object. Dynamic dispatch needs an **object**. No object involved means no runtime choice. This is **method hiding**, and it uses **static binding**.

Adding `@Override` on a static method gives a compile error.

### 13.2 `final` methods: overriding forbidden

```java
class Bank {
    final void verify() { System.out.println("Bank verify"); }
}
class MyBank extends Bank {
    void verify() { }       // ERROR: cannot override final method
}
```

The method is inherited and callable, but **locked**. Use it when a step must never change (security checks, template steps). Because it can't be overridden, the compiler can bind it early.

### 13.3 `private` methods: not even visible

```java
class Parent {
    private void secret() { System.out.println("Parent secret"); }
    void callSecret() { secret(); }
}
class Child extends Parent {
    void secret() { System.out.println("Child secret"); }   // NEW method, not an override
}

new Child().callSecret();   // prints: Parent secret
```

A private method is not inherited, so there is nothing to override. The child's `secret()` is an unrelated method that happens to share the name. `@Override` there would be a compile error.

### 13.4 Constructors

Never overridden and never inherited. They can only be overloaded.

### 13.5 Summary table

| Method kind | Overridable? | Binding | What happens in child with same signature |
|---|---|---|---|
| Normal instance | **Yes** | Dynamic (runtime) | Overrides |
| `static` | No | Static (compile time) | **Hides** the parent method |
| `final` | No | Static | **Compile error** |
| `private` | No | Static | A brand-new unrelated method |
| Constructor | No | n/a | Not inherited |
| `abstract` | **Must** be overridden | Dynamic | Child must implement |
| Fields (variables) | n/a | Static (by reference type) | **Hides** the parent field |

---

## 14. Polymorphism with abstract classes and interfaces

Everything in Section 9 works the same way when the parent is an **abstract class** or an **interface**. This is where polymorphism is most useful in real projects.

```java
interface Notifier { void send(String msg); }

class EmailNotifier implements Notifier {
    public void send(String msg) { System.out.println("Email: " + msg); }
}
class SmsNotifier implements Notifier {
    public void send(String msg) { System.out.println("SMS: " + msg); }
}
class PushNotifier implements Notifier {
    public void send(String msg) { System.out.println("Push: " + msg); }
}

class OrderService {
    private final Notifier notifier;                 // depends on the CONTRACT
    OrderService(Notifier notifier) { this.notifier = notifier; }

    void placeOrder() {
        // ... order logic ...
        notifier.send("Order placed!");              // polymorphic call
    }
}

new OrderService(new EmailNotifier()).placeOrder();  // Email: Order placed!
new OrderService(new SmsNotifier()).placeOrder();    // SMS: Order placed!
```

```
                    OrderService
                         |
                         v
                 Notifier (interface)
               /         |          \
        Email         Sms          Push        <- swap freely, OrderService never changes
```

**Principle:** *program to an interface, not to an implementation.* Spring's dependency injection, JDBC (`Connection`, `Statement`), and the Collections framework (`List<String> list = new ArrayList<>()`, later switch to `LinkedList`) all rely on this.

```java
List<String> list = new ArrayList<>();   // change to new LinkedList<>() later, rest of code unchanged
```

### Polymorphic collections

```java
List<Shape> shapes = List.of(new Circle("red", 2), new Rectangle("blue", 3, 4));
double total = 0;
for (Shape s : shapes) total += s.area();    // each shape computes area its own way
```

---

## 15. Overloading vs Overriding

| | Overloading | Overriding |
|---|---|---|
| Also called | Static / compile-time polymorphism | Dynamic / runtime polymorphism |
| Where | Same class (or parent + child) | Parent and child (needs inheritance) |
| Method name | Same | Same |
| Parameters | **Must differ** | **Must be same** |
| Return type | Can differ (not enough alone) | Same or covariant |
| Access modifier | Any | Same or wider |
| Exceptions | Any | Not broader (checked) |
| Decided by | Compiler, using **parameters and reference type** | JVM, using the **actual object** |
| `static` methods | Can be overloaded | Cannot be overridden (hidden) |
| `private` / `final` | Can be overloaded | Cannot be overridden |
| Needs `@Override`? | No | Recommended |
| Purpose | Convenience / readability | Specialized behavior of children |

---

## 16. Advantages and disadvantages of polymorphism

### Advantages

1. **Code reuse and less duplication:** one method handles all subtypes.
2. **Extensibility (Open/Closed):** add new types without modifying existing code.
3. **Removes long `if/else` / `switch` chains:** behavior lives in the objects.
4. **Loose coupling:** code depends on abstractions, so parts are replaceable.
5. **Easier testing:** pass a fake implementation in tests.
6. **Cleaner, readable APIs:** one name per idea (`print`, `add`, `speak`).
7. **Foundation of frameworks and design patterns:** Strategy, Factory, Observer, Template Method.

### Disadvantages

1. **Harder to trace:** you must know the real object to know what runs.
2. **Small runtime overhead** for virtual calls (usually minimal because of JIT optimization).
3. **Overloading surprises:** choice depends on declared type, and boxing/widening rules can pick an unexpected method.
4. **Silent override mistakes:** a typo or wrong signature creates a new method instead of overriding (use `@Override`).
5. **Fragile base class problem:** a change in the parent can break children.
6. **Overuse of inheritance:** deep hierarchies are hard to manage (prefer composition when it is not a true IS-A).
7. **Cannot use for `static`, `final`, `private`** (limits flexibility by design).
8. **Fields are not polymorphic**, which confuses beginners.

---

# PART C: REVISION

## 17. How all OOP pillars connect

```
 ENCAPSULATION : protect and bundle data     (private fields + public methods)
        |
        v
 ABSTRACTION   : expose only the contract    (interfaces, abstract classes)
        |
        v
 INHERITANCE   : reuse and form IS-A         (extends / implements)
        |
        v
 POLYMORPHISM  : same call, different behavior (overriding + upcasting + dynamic dispatch)
```

One story to remember:

1. **Encapsulation** keeps each object's data safe and its inside hidden.
2. **Abstraction** defines a clean contract (`Vehicle.start()`) that hides which implementation is used.
3. **Inheritance** (or implementing an interface) lets many classes follow that contract.
4. **Polymorphism** lets one piece of code use the contract, while each object responds in its own way.

| Pillar | One-line meaning | Java tools |
|---|---|---|
| Encapsulation | Protect data | `private`, getters/setters, packages |
| Abstraction | Hide the "how", show the "what" | `abstract class`, `interface` |
| Inheritance | Reuse, IS-A | `extends`, `implements` |
| Polymorphism | One name, many forms | overloading, overriding, upcasting |

---

## 18. Interview questions with answers

**Q1. What is abstraction? Give a real-life example.**
Hiding implementation details and showing only essential features. Example: you use a car's steering and pedals without knowing how the engine works.

**Q2. How do you achieve abstraction in Java?**
Through classes with private internals and public methods (low level), and through abstract classes and interfaces (high level).

**Q3. Can we create an object of an abstract class?**
No. But we can create a reference of it and point it at a child object.

**Q4. Can an abstract class have a constructor?**
Yes. It runs when a child object is created (through `super()`).

**Q5. Can an abstract class have no abstract methods?**
Yes. It simply prevents direct instantiation.

**Q6. Can an interface have a constructor? Fields?**
No constructor. Fields are allowed only as `public static final` constants.

**Q7. Difference between abstract class and interface?**
Abstract class: IS-A base for related classes, can have state, constructors, any methods, single inheritance. Interface: CAN-DO contract, no state or constructors, multiple inheritance of type, methods public (default/static/private allowed since Java 8/9).

**Q8. Which should you choose, abstract class or interface?**
Interface for capabilities and loose coupling. Abstract class when related classes share state or code. Often both: an interface plus an abstract base class implementing it.

**Q9. Difference between abstraction and encapsulation?**
Abstraction decides what to show (hide complexity, design level). Encapsulation protects data with access modifiers (code level). They work together.

**Q10. What is polymorphism?**
One name, many forms: the same call behaves differently depending on the receiving object (runtime) or the arguments (compile time).

**Q11. How does inheritance lead to polymorphism?**
Inheritance gives an IS-A relation, so a parent reference can hold a child object (upcasting). If the child overrides a method, the call on the parent reference runs the child's version at runtime (dynamic dispatch).

**Q12. Types of polymorphism in Java?**
Compile-time (method overloading) and run-time (method overriding).

**Q13. Can we overload by changing only the return type?**
No. The compiler picks overloads from the parameters only.

**Q14. Can we override static methods?**
No. They are hidden, and the call is decided by the reference type.

**Q15. Can we override private or final methods?**
Private methods are not inherited, so no. Final methods are explicitly locked, so overriding is a compile error.

**Q16. What is dynamic method dispatch?**
The JVM's runtime process of choosing the overridden method based on the actual object type, not the reference type.

**Q17. What is the output?**
```java
class A { void show() { System.out.println("A"); } }
class B extends A { void show() { System.out.println("B"); } }
A obj = new B();
obj.show();
```
`B` (overridden instance method, decided at runtime by the object).

**Q18. What is the output?**
```java
class A { String n = "A"; static void f() { System.out.println("A.f"); } }
class B extends A { String n = "B"; static void f() { System.out.println("B.f"); } }
A obj = new B();
System.out.println(obj.n);
obj.f();
```
`A` and `A.f`. Fields and static methods use the reference type.

**Q19. Which overload runs here?**
```java
void p(Object o) { System.out.println("Object"); }
void p(String s) { System.out.println("String"); }
p(null);
```
`String` (most specific type wins). If the candidates were `String` and `Integer`, it would be ambiguous.

**Q20. Why is it called static polymorphism?**
The method is chosen by the compiler at compile time, using the declared types of the arguments.

**Q21. What is covariant return type?**
An overriding method may return a subtype of the parent method's return type.

**Q22. What is the benefit of "program to an interface"?**
Loose coupling: implementations can be swapped (ArrayList to LinkedList, email to SMS) without changing the code that uses them.

**Q23. Is polymorphism possible without inheritance?**
Overloading yes (compile-time). Runtime polymorphism needs inheritance or interface implementation.

**Q24. Can an interface method be private or protected?**
Abstract methods are always public. Since Java 9 an interface can have `private` helper methods (with body). `protected` is not allowed.

---

## 19. Cheat sheet and common mistakes

### One-page summary

```
ABSTRACTION  = show WHAT, hide HOW
   low level  : class + private internals + public methods
   high level : abstract class (partial) / interface (contract)

ABSTRACT CLASS : abstract keyword, can't new it, may have abstract + concrete methods,
                 fields, constructors; child must implement abstract methods; single inheritance
INTERFACE      : contract; public abstract methods (+ default/static/private), constants only;
                 no constructors/state; class can implement many

ABSTRACTION vs ENCAPSULATION : design (what to show)  vs  code (protect data)

POLYMORPHISM = one name, many forms
   compile-time : OVERLOADING  (same name, different params, compiler decides)
   run-time     : OVERRIDING   (same signature in child, JVM decides by actual object)

Inheritance -> Overriding -> Upcasting -> Dynamic dispatch -> POLYMORPHISM

static / final / private  => NOT overridable (static hides, final errors, private unrelated)
Fields                    => NOT polymorphic (reference type decides)
```

### The golden sentence for polymorphism

> **Reference type decides what you CAN call. Object type decides which overridden version RUNS.**

### Common mistakes

| Mistake | Fix |
|---|---|
| `new Shape()` on an abstract class or interface | Create a concrete child and use the parent as the reference type |
| Forgetting `public` when implementing an interface method | Interface methods are public; implementations must be `public` |
| Child doesn't implement all abstract methods | Implement all, or make the child abstract |
| Trying `abstract` with `private` / `final` / `static` | These contradict "must be overridden" |
| Expecting return type alone to differentiate overloads | Change the parameters |
| Expecting overloading to follow the object's real type | Overloading uses the declared (reference) type |
| Expecting a `static` method to behave polymorphically | Static methods are hidden; use instance methods |
| Expecting fields to be overridden | Fields use the reference type; use methods (getters) |
| Typo in overriding method name creates a new method | Always add `@Override` |
| Weakening access while overriding | Same or wider access only |
| Using inheritance only to share code | Prefer composition unless it's a true IS-A |
| Over-abstracting everything | Abstract only what varies or what users don't need to know |
| Mixing up abstraction and encapsulation | Abstraction = what to show; Encapsulation = how to protect |

### One-line takeaways

1. **Abstraction:** show *what*, hide *how*. In Java: abstract classes and interfaces.
2. **Abstract class** = partial blueprint (IS-A, shared code). **Interface** = pure contract (CAN-DO).
3. **Abstraction vs encapsulation:** design-level hiding of complexity vs code-level protection of data.
4. **Polymorphism:** one name, many forms. **Overloading** = compiler decides (parameters). **Overriding** = JVM decides (actual object).
5. **Inheritance gives polymorphism** through overriding + upcasting + dynamic dispatch.
6. `static`, `final`, `private` methods and fields are **not** dynamically dispatched.
7. Program to an interface; add new types without touching old code.

---
*Revision trick: close this file and explain out loud, using the Animal example, how `Animal a = new Dog(); a.speak();` ends up running Dog's code. If you can tell the four-step chain (inheritance, overriding, upcasting, dynamic dispatch), you understand polymorphism.*
