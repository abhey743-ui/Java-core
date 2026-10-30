# Java Interfaces: The Complete Deep Dive

> This guide follows the outline of the video you shared (introduction, fundamentals, variables, multiple inheritance and the diamond problem, default and static methods, private methods, interface vs abstract class, functional and marker interfaces, internal working) and explains everything in **easy language**.
> You said to add anything the video skipped, so I also added: **why interfaces exist (first principles)**, interface inheritance, generic interfaces, nested interfaces, **common JDK interfaces**, design patterns that use interfaces (loose coupling, strategy, testing), **sealed interfaces and records**, a **version-by-version history**, and many runnable programs.

---

## Table of Contents

1. [Roadmap](#1-roadmap)
2. [What Is an Interface?](#2-what-is-an-interface)
3. [Why Do Interfaces Exist? (First Principles)](#3-why-do-interfaces-exist-first-principles)
4. [Interface Fundamentals: Declaring and Implementing](#4-interface-fundamentals-declaring-and-implementing)
5. [Access Modifiers and Visibility Rules](#5-access-modifiers-and-visibility-rules)
6. [Dynamic Polymorphism with Interfaces](#6-dynamic-polymorphism-with-interfaces)
7. [Variables in Interfaces (Constants)](#7-variables-in-interfaces-constants)
8. [Interface Inheritance and Multiple Inheritance](#8-interface-inheritance-and-multiple-inheritance)
9. [The Diamond Problem and How Java Solves It](#9-the-diamond-problem-and-how-java-solves-it)
10. [Default Methods (Java 8)](#10-default-methods-java-8)
11. [Static Methods in Interfaces (Java 8)](#11-static-methods-in-interfaces-java-8)
12. [Private Methods in Interfaces (Java 9)](#12-private-methods-in-interfaces-java-9)
13. [All Kinds of Interface Members at a Glance](#13-all-kinds-of-interface-members-at-a-glance)
14. [Interfaces vs Abstract Classes](#14-interfaces-vs-abstract-classes)
15. [Functional Interfaces](#15-functional-interfaces)
16. [Marker Interfaces](#16-marker-interfaces)
17. [Generic Interfaces](#17-generic-interfaces)
18. [Nested Interfaces and Interfaces with Enums, Records and Anonymous Classes](#18-nested-interfaces-and-interfaces-with-enums-records-and-anonymous-classes)
19. [Important Interfaces in the JDK](#19-important-interfaces-in-the-jdk)
20. [Design with Interfaces: Loose Coupling, Strategy and Testing](#20-design-with-interfaces-loose-coupling-strategy-and-testing)
21. [Sealed Interfaces (Java 17+)](#21-sealed-interfaces-java-17)
22. [Internal Working: What the Compiler and JVM Do](#22-internal-working-what-the-compiler-and-jvm-do)
23. [Version-by-Version History](#23-version-by-version-history)
24. [Practical Programs](#24-practical-programs)
25. [Common Mistakes](#25-common-mistakes)
26. [Cheat Sheet, Summary and Quiz](#26-cheat-sheet-summary-and-quiz)

---

## 1. Roadmap

Learn interfaces in this order and every step stays small:

```
 STEP 1   What is a contract?          "What can this object do?" (not how)
    │
 STEP 2   Why we need it               unrelated classes + single inheritance + loose coupling
    │
 STEP 3   Declare and implement        interface / implements / @Override
    │
 STEP 4   Use it                       interface reference = polymorphism
    │
 STEP 5   Constants                    public static final
    │
 STEP 6   Inherit                      interface extends interface(s); class implements many
    │
 STEP 7   The diamond problem          how Java resolves conflicts
    │
 STEP 8   Modern members               default (8), static (8), private (9)
    │
 STEP 9   Special kinds                functional (lambdas), marker (tags)
    │
 STEP 10  Under the hood               ACC_INTERFACE, invokeinterface
```

### The sentence to remember

> **A class says WHAT AN OBJECT IS and HOW IT WORKS. An interface says WHAT AN OBJECT CAN DO, without saying how.**

---

## 2. What Is an Interface?

### 2.1 Definition

An **interface** is a **contract** (a blueprint of behavior). It lists **methods** that a class promises to provide, but it (traditionally) says **nothing about how** those methods work.

```java
public interface Drawable {
    void draw();                 // just the promise: "I can draw". No body.
}
```

Any class that **signs** this contract with `implements` **must** provide the method:

```java
public class Circle implements Drawable {
    @Override
    public void draw() {
        System.out.println("Drawing a circle");
    }
}
```

### 2.2 Real-life pictures of a contract

| Real-life thing | The "interface" (contract) | The "implementations" |
|-----------------|---------------------------|-----------------------|
| **Wall power socket** | The shape of the socket | Fridge, TV, phone charger: anything with the right plug works |
| **USB port** | The USB standard | Mouse, keyboard, pen drive, fan |
| **TV remote** | Buttons: power, volume, channel | Samsung, Sony, LG TVs each do it differently inside |
| **Driving license** | "Can drive a car" | Any car: Toyota, Honda, Tesla |
| **Job description** | The duties listed | Any person who can do those duties |

The **user** of the contract doesn't care **which** object is behind it. They only care that it keeps the promise.

```
            ┌────────────────────────┐
            │  interface Drawable    │   ← the CONTRACT
            │     void draw();       │
            └───────────┬────────────┘
        implements      │      implements
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
     Circle          Square          Triangle      ← each does it its OWN way
   draw(){...}     draw(){...}      draw(){...}
```

### 2.3 Interface vs class (the video's first comparison)

| | **Class** | **Interface** |
|---|-----------|---------------|
| What it is | A blueprint for **objects** (data + behavior) | A blueprint for **behavior** (a contract) |
| Keyword | `class` | `interface` |
| Can you create objects directly (`new`)? | **Yes** | **No**: `new Drawable()` is an error |
| Holds instance data (fields)? | Yes | **No** (only constants) |
| Has constructors? | Yes | **No** |
| Used with | `extends` | `implements` |

```java
Drawable d = new Drawable();         // ERROR: Drawable is abstract; cannot be instantiated
Drawable d = new Circle();           // ✓ an interface REFERENCE can hold an implementing object
```

> An interface can't be instantiated because it has **no complete implementation**: it's just a promise. You can only create objects of **classes that implement** it.

---

## 3. Why Do Interfaces Exist? (First Principles)

Let's invent interfaces ourselves by facing the problems they solve.

### Problem 1: Unrelated things share an ability

A `Bird`, an `Airplane` and `Superman` can all **fly**. But they have **nothing in common** otherwise. A bird is an animal, a plane is a machine, Superman is a person.

If we tried inheritance, we'd need a common parent called `FlyingThing`. That's wrong: a plane **is not** a kind of animal.

**Need:** a way to say *"this class can do X"* **without** forcing a parent-child family.
**Solution:** `interface Flyable { void fly(); }`

```java
interface Flyable { void fly(); }

class Bird implements Flyable     { public void fly() { System.out.println("Flapping wings"); } }
class Airplane implements Flyable { public void fly() { System.out.println("Using engines"); } }
class Superman implements Flyable { public void fly() { System.out.println("Using superpowers"); } }
```

### Problem 2: A class can extend only ONE class

A smartphone is a **phone**, a **camera** and a **music player**. Java classes allow **only one parent** (single inheritance), so you can't write `class Smartphone extends Phone, Camera, MusicPlayer`.

**Need:** a class that has **many roles**.
**Solution:** a class can **implement many interfaces**.

```java
class Smartphone implements Callable, Photographer, MusicPlayer { ... }
```

### Problem 3: Tight coupling (code stuck to one concrete class)

```java
class OrderService {
    private MySqlDatabase db = new MySqlDatabase();     // stuck with MySQL forever
}
```

Want PostgreSQL? Edit `OrderService`. Want a fake database for tests? Can't.

**Need:** depend on **what the thing can do**, not **which exact class** it is.
**Solution:** depend on an interface.

```java
class OrderService {
    private final Database db;                          // any Database works
    OrderService(Database db) { this.db = db; }
}
```

### Problem 4: Teams need to agree before the code exists

Team A builds the payment screen; Team B builds the payment engine. They agree on a contract first:

```java
interface PaymentProcessor { boolean charge(double amount); }
```

Team A codes against the interface; Team B implements it later. Both work **in parallel**.

### Problem 5: Testing

To test `OrderService` without a real database, plug in a **fake** that implements the same interface.

### Problem 6: Treat different objects uniformly (polymorphism)

A list of `Drawable`s can hold circles, squares and triangles; one loop calls `draw()` on all of them.

### Summary of what interfaces give us

| Benefit | Meaning |
|---------|---------|
| **Abstraction** | Show *what*, hide *how* |
| **Multiple roles** | A class can have many "can-do" abilities |
| **Loose coupling** | Code depends on a contract, not a concrete class |
| **Polymorphism** | One reference type, many real types |
| **Flexibility** | Swap implementations without changing users |
| **Testability** | Fakes and mocks are easy |
| **Parallel development** | Agree on the contract first |
| **Evolvable APIs** | Default methods let interfaces grow (Java 8) |

---

## 4. Interface Fundamentals: Declaring and Implementing

### 4.1 Declaring an interface

```
[public] interface InterfaceName [extends OtherInterface, ...] {
    // constants
    // abstract methods
    // default methods     (Java 8)
    // static methods      (Java 8)
    // private methods     (Java 9)
    // nested types
}
```

```java
public interface Shape {
    double area();             // abstract method: no body, ends with a semicolon
    double perimeter();
}
```

In an interface, a method without a body is **automatically `public abstract`**. These two lines mean exactly the same:

```java
void draw();
public abstract void draw();
```

### 4.2 Implementing an interface

A class uses **`implements`** and must **provide a body (override) for every abstract method**:

```java
public class Rectangle implements Shape {
    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() { return width * height; }

    @Override
    public double perimeter() { return 2 * (width + height); }
}
```

```java
Shape s = new Rectangle(3, 4);
System.out.println(s.area());         // 12.0
System.out.println(s.perimeter());    // 14.0
```

### 4.3 If you forget a method

```java
class Broken implements Shape {
    public double area() { return 0; }
    // perimeter() is missing!
}
// ERROR: Broken is not abstract and does not override abstract method perimeter() in Shape
```

**Exception to the rule:** an **abstract class** may implement only some methods, leaving the rest for its subclasses:

```java
abstract class PartialShape implements Shape {
    @Override
    public double perimeter() { return 0; }     // only one implemented
    // area() stays abstract: concrete subclasses must write it
}
```

### 4.4 Rules for declaring interfaces

| Rule | Detail |
|------|--------|
| **Cannot be instantiated** | `new Shape()` is an error |
| **No constructors** | Nothing to construct |
| **No instance variables** | Only constants (section 7) |
| **No instance initializer blocks** | Not allowed |
| **Top-level access** | `public` or package-private (no keyword). Nested interfaces can also be `private`/`protected` |
| **File name** | A `public` interface must be in a file with the same name: `Shape.java` |
| **Methods can't be** | `protected`, package-private, `final`, `synchronized` |
| **Can contain** | Constants, abstract / default / static / private methods, nested types |
| **Can have `main`** | A `public static void main` in an interface can run (fun fact) |

### 4.5 `@Override`: always use it

```java
@Override
public double area() { ... }
```

If you mistype the method name or parameters (`aera()`), `@Override` makes the compiler report an error. (Using `@Override` for interface methods is allowed since Java 6.)

### 4.6 Naming conventions

| Style | Examples | Meaning |
|-------|----------|---------|
| **Adjective ending in -able / -ible** | `Comparable`, `Runnable`, `Serializable`, `Iterable`, `Flyable` | "Can do / can be treated as" a capability |
| **Noun for a role** | `List`, `Map`, `Shape`, `Repository` | A role or type |
| **-er names** | `Comparator`, `Listener`, `Handler` | The doer |
| **No `I` prefix** | `Shape`, not `IShape` | The Java convention (the `I` prefix is a C#/COM habit) |
| Implementation names | `ArrayList`, `CircleShape` | Describe the specific way (avoid vague `ShapeImpl` when you can) |

### 4.7 A class can implement several interfaces

```java
class Smartphone implements Callable, Photographer, MusicPlayer {
    // must implement all abstract methods from all three
}
```

Syntax order when also extending a class:

```java
class Dog extends Animal implements Pet, Trainable { ... }
//         ▲ only ONE parent class   ▲ any number of interfaces
```

### 4.8 `extends` vs `implements` cheat sheet

| Relationship | Keyword | How many |
|--------------|---------|----------|
| class → class | `extends` | **one** |
| class → interface | `implements` | **many** |
| interface → interface | `extends` | **many** |
| interface → class | *impossible* | n/a |

---

## 5. Access Modifiers and Visibility Rules

### 5.1 Interface methods are implicitly `public`

The video's point: **all (abstract) methods in an interface are `public`**, even if you don't write it. They are a promise to **everyone**, so they must be visible to everyone.

```java
interface Animal {
    void sound();              // = public abstract void sound();
    protected void run();      // ERROR: modifier protected not allowed here
    private void hide();       // ERROR (abstract methods can't be private)
}
```

### 5.2 When you implement it, you can't reduce visibility

When a class overrides the method, it must also be **`public`**. Making it weaker is an error.

```java
class Dog implements Animal {
    void sound() { }                  // ERROR: attempting to assign weaker access privileges; was public
    protected void sound() { }        // ERROR (same reason)
    public void sound() { }           // ✓
}
```

**Why?** Anyone holding an `Animal` reference is **entitled** to call `sound()`. If `Dog` hid it, the contract would be broken.

```
 Visibility order (weakest → strongest):   private < package-private < protected < public

 Overriding rule:  the override must be AT LEAST as visible as the original.
 Original is public  ⇒  the override must be public.
```

### 5.3 Other overriding rules

| Rule | Detail |
|------|--------|
| **Same signature** | Same name and parameter types |
| **Return type** | Same, or a **subtype** (covariant return) |
| **Exceptions** | The implementation may throw **fewer or narrower** checked exceptions, but **not broader** ones than the interface method declares |
| **`static`** | An instance method can't be implemented by a `static` one |

```java
interface Reader {
    String read() throws java.io.IOException;
}

class FileReaderImpl implements Reader {
    public String read() throws java.io.FileNotFoundException { return "data"; }   // ✓ narrower
}

class BadReader implements Reader {
    public String read() throws Exception { return "data"; }                       // ✗ broader
}
```

---

## 6. Dynamic Polymorphism with Interfaces

### 6.1 The idea

An **interface reference variable** can hold **any object of any class that implements it**. At runtime, Java calls the **real object's** version of the method. That is **dynamic (runtime) polymorphism**.

```java
interface PaymentMethod {
    void pay(double amount);
}

class CreditCard implements PaymentMethod {
    public void pay(double amount) { System.out.println("Paid " + amount + " by credit card"); }
}

class PayPal implements PaymentMethod {
    public void pay(double amount) { System.out.println("Paid " + amount + " via PayPal"); }
}

class Wallet implements PaymentMethod {
    public void pay(double amount) { System.out.println("Paid " + amount + " from wallet"); }
}
```

```java
PaymentMethod m;                 // the reference type is the INTERFACE

m = new CreditCard();
m.pay(50);                       // Paid 50.0 by credit card

m = new PayPal();
m.pay(50);                       // Paid 50.0 via PayPal

m = new Wallet();
m.pay(50);                       // Paid 50.0 from wallet
```

```
   STACK                               HEAP
  ┌──────────────────────┐
  │ PaymentMethod m ●────┼──────►  [ CreditCard object ]     (first)
  └──────────────────────┘
        m = new PayPal();  ───────► [ PayPal object ]        (later: same variable, new target)

  Declared type (compile time):  PaymentMethod   → decides WHAT you may call (pay)
  Real type (run time):          CreditCard/...  → decides WHICH code runs
```

### 6.2 Interface type everywhere = flexible code

**As a parameter:**

```java
static void checkout(PaymentMethod method, double amount) {
    method.pay(amount);                       // works for ANY PaymentMethod, now or in the future
}

checkout(new CreditCard(), 20);
checkout(new Wallet(), 20);
```

**As a return type:**

```java
static PaymentMethod chooseMethod(boolean international) {
    return international ? new PayPal() : new CreditCard();
}
```

**In an array or collection:**

```java
PaymentMethod[] options = { new CreditCard(), new PayPal(), new Wallet() };
for (PaymentMethod p : options) {
    p.pay(10);                               // one loop, three behaviors
}

java.util.List<PaymentMethod> list = new java.util.ArrayList<>();
```

**As a field:**

```java
class Checkout {
    private PaymentMethod method;             // can be changed at runtime
    void setMethod(PaymentMethod method) { this.method = method; }
}
```

### 6.3 What you can call through an interface reference

You can call **only** what the **interface declares** (plus `Object` methods). Other methods of the real class are hidden:

```java
class CreditCard implements PaymentMethod {
    public void pay(double amount) { ... }
    public void checkLimit() { ... }              // NOT in the interface
}

PaymentMethod m = new CreditCard();
m.pay(10);                // ✓
m.checkLimit();           // ✗ ERROR: cannot find symbol (PaymentMethod has no checkLimit)
```

To reach it, **downcast** (after checking):

```java
if (m instanceof CreditCard) {
    CreditCard c = (CreditCard) m;
    c.checkLimit();
}

// Java 16+: check and cast in one step
if (m instanceof CreditCard c) {
    c.checkLimit();
}
```

### 6.4 Upcasting is automatic

```java
CreditCard card = new CreditCard();
PaymentMethod m = card;              // implicit upcast: a CreditCard IS a PaymentMethod
```

### 6.5 `instanceof` with interfaces

```java
Object o = new CreditCard();
System.out.println(o instanceof PaymentMethod);     // true
System.out.println(o instanceof Wallet);            // false
```

### 6.6 An interface reference can also call `Object` methods

```java
PaymentMethod m = new CreditCard();
m.toString();    m.equals(x);    m.hashCode();    m.getClass();    // all fine
```

---

## 7. Variables in Interfaces (Constants)

### 7.1 The rule

> **Every variable declared in an interface is automatically `public`, `static` and `final`.** It's a **constant**.

```java
public interface GameConfig {
    int MAX_PLAYERS = 4;
    String GAME_NAME = "Battle Arena";
    double GRAVITY = 9.8;
}
```

The compiler reads this as:

```java
public static final int MAX_PLAYERS = 4;
public static final String GAME_NAME = "Battle Arena";
public static final double GRAVITY = 9.8;
```

| Modifier | Meaning here |
|----------|--------------|
| `public` | Visible to everyone |
| `static` | Belongs to the **interface**, not to objects (interfaces have no objects) |
| `final` | Can't be changed after initialization |

### 7.2 Using them

```java
System.out.println(GameConfig.MAX_PLAYERS);     // 4   (access with the INTERFACE name)
GameConfig.MAX_PLAYERS = 10;                    // ERROR: cannot assign a value to final variable MAX_PLAYERS
```

### 7.3 Rules

| Rule | Example |
|------|---------|
| **Must be initialized at declaration** | `int x;` → error: `= expected` |
| **Can't be `private` or `protected`** | `private int x = 1;` → error |
| **Can't be changed** | Compile error on assignment |
| **A reference constant isn't deeply constant** | `List<String> NAMES = new ArrayList<>();` can't be reassigned, but `NAMES.add("x")` still works |
| **Written in UPPER_SNAKE_CASE** | `MAX_PLAYERS` |

### 7.4 Why are they static and final?

Interfaces have **no objects** and **no instance state**. Allowing changing, per-object variables would be meaningless. So the only variables allowed are **shared constants**.

### 7.5 Inheriting constants

An implementing class (or sub-interface) can use the constants by simple name:

```java
class Game implements GameConfig {
    void start() {
        System.out.println("Max players: " + MAX_PLAYERS);     // OK
    }
}
```

**Conflict:** if two interfaces have a constant with the **same name**, using the simple name is **ambiguous**:

```java
interface P { int LIMIT = 10; }
interface Q { int LIMIT = 20; }

class R implements P, Q {
    void show() {
        System.out.println(LIMIT);          // ERROR: reference to LIMIT is ambiguous
        System.out.println(P.LIMIT);        // ✓ qualify with the interface name
        System.out.println(Q.LIMIT);        // ✓
    }
}
```

### 7.6 The "constant interface" anti-pattern

Don't create an interface **only** to share constants and have classes `implements` it:

```java
interface Colors { int RED = 0; }
class Painter implements Colors { }        // ✗ Painter now "is a" Colors: pollutes its API
```

Better options: a `final` class with a private constructor, or an **`enum`**.

```java
public final class Colors {
    private Colors() { }
    public static final int RED = 0;
}
```

---

## 8. Interface Inheritance and Multiple Inheritance

### 8.1 An interface can extend interfaces (even several)

```java
interface Walkable { void walk(); }
interface Swimmable { void swim(); }

interface Amphibious extends Walkable, Swimmable {     // extends TWO interfaces
    void breatheUnderwater();
}
```

```
      Walkable     Swimmable
          ▲            ▲
          └─────┬──────┘
           Amphibious
```

A class implementing `Amphibious` must implement **all three methods**:

```java
class Frog implements Amphibious {
    public void walk()              { System.out.println("Hopping"); }
    public void swim()              { System.out.println("Swimming"); }
    public void breatheUnderwater() { System.out.println("Breathing through skin"); }
}
```

### 8.2 Why interfaces allow multiple inheritance but classes don't

| | Classes | Interfaces |
|---|---------|------------|
| Multiple parents? | **No** (one `extends`) | **Yes** (many `extends`/`implements`) |
| Why | Two parents could bring **two different implementations** of the same method, or two copies of the same fields → ambiguity | Traditionally interfaces have **only a promise** (no body, no state), so there's nothing to conflict. One method body in the class settles it |

> Java supports multiple inheritance of **type** (and, since Java 8, of **behavior** through default methods under strict rules) but **not** multiple inheritance of **state** (fields). That is what keeps it safe.

### 8.3 A class that extends one class AND implements many interfaces

```java
class Animal { void eat() { System.out.println("eating"); } }

interface Pet   { void play(); }
interface Guard { void protect(); }

class Dog extends Animal implements Pet, Guard {
    public void play()    { System.out.println("fetching"); }
    public void protect() { System.out.println("barking at strangers"); }
}

Dog d = new Dog();
d.eat();                  // inherited from Animal
d.play();                 // from Pet
Animal a = d;             // a Dog is an Animal
Pet p = d;                // ...and a Pet
Guard g = d;              // ...and a Guard
```

One `Dog` object has **three types** at once.

```
          Animal          Pet        Guard
             ▲             ▲           ▲
             └─────────────┼───────────┘
                           │
                          Dog
```

### 8.4 Inheritance tree rules

- A class **cannot** `extends` an interface and an interface **cannot** `implements` anything. (Use `implements` for classes and `extends` for interfaces.)
- An interface cannot extend a class.
- Circular inheritance (`A extends B`, `B extends A`) is an error.

---

## 9. The Diamond Problem and How Java Solves It

### 9.1 What is the diamond problem?

Imagine a class could extend **two** classes that both inherit from a common parent, and both override the same method:

```
            ┌────────────┐
            │  Animal    │
            │  sound()   │
            └─────┬──────┘
          ┌───────┴───────┐
   ┌──────▼─────┐   ┌─────▼──────┐
   │   Dog      │   │    Cat     │
   │ sound()    │   │  sound()   │   ← both override sound()
   │ "Woof"     │   │  "Meow"    │
   └──────┬─────┘   └─────┬──────┘
          └───────┬───────┘
           ┌──────▼─────┐
           │  DogCat    │   extends Dog, Cat   ← which sound()? Woof or Meow?
           └────────────┘
```

This shape is a **diamond**, hence the name. `DogCat` would inherit **two competing** versions of `sound()`. The compiler can't decide. Some languages (like C++) allow this and need special rules. **Java avoids it by forbidding a class from extending more than one class.**

### 9.2 Interfaces before Java 8: no problem

Before Java 8, interface methods had **no body**. If `DogCat` implemented two interfaces that both declared `sound()`, there was **nothing to conflict**: the class writes **one** body, and that's the one used.

```java
interface Dog { void sound(); }
interface Cat { void sound(); }

class DogCat implements Dog, Cat {
    public void sound() { System.out.println("Woof-Meow"); }     // one body satisfies both
}
```

### 9.3 Default methods bring the problem back (Java 8)

With **default methods** (interface methods **with a body**), two interfaces can now offer **two different bodies** for the same method. That is a real diamond. Java solves it with **clear rules**.

### 9.4 The three resolution rules

When a class inherits the same method signature from several places, Java decides like this:

```
 RULE 1: CLASS WINS
         A method from a superclass (concrete) beats any interface default.

 RULE 2: MORE SPECIFIC INTERFACE WINS
         If one interface extends another and overrides the default,
         the sub-interface's version wins.

 RULE 3: OTHERWISE, YOU MUST DECIDE
         If two unrelated interfaces conflict, the compiler gives an ERROR
         until your class overrides the method (and can pick one with
         InterfaceName.super.method()).
```

#### Rule 1: Class wins

```java
class Animal {
    public void sound() { System.out.println("Animal sound"); }
}

interface Loud {
    default void sound() { System.out.println("LOUD SOUND"); }
}

class Dog extends Animal implements Loud { }

new Dog().sound();           // Animal sound   ← the superclass method wins
```

#### Rule 2: The more specific interface wins

```java
interface A {
    default void hi() { System.out.println("Hi from A"); }
}

interface B extends A {                       // B is MORE SPECIFIC than A
    @Override
    default void hi() { System.out.println("Hi from B"); }
}

class C implements A, B { }

new C().hi();                // Hi from B
```

```
        A  (hi)
        ▲
        │ extends
        B  (hi overrides A.hi)       C implements A, B   → B wins (it's lower in the tree)
```

#### Rule 3: Unrelated interfaces conflict: you must resolve it

```java
interface Walker {
    default void move() { System.out.println("Walking"); }
}

interface Swimmer {
    default void move() { System.out.println("Swimming"); }
}

class Duck implements Walker, Swimmer { }
// ERROR: types Walker and Swimmer are incompatible;
//        class Duck inherits unrelated defaults for move() from types Walker and Swimmer
```

**The fix:** override `move()` in the class. Inside it, you can **choose** one of the inherited versions (or both) using **`InterfaceName.super.method()`**:

```java
class Duck implements Walker, Swimmer {
    @Override
    public void move() {
        Walker.super.move();                       // call Walker's version
        Swimmer.super.move();                      // call Swimmer's version
        System.out.println("Duck moves both ways");
    }
}

new Duck().move();
```

Output:
```
Walking
Swimming
Duck moves both ways
```

Or just pick one: `public void move() { Swimmer.super.move(); }`, or write your own completely.

### 9.5 Another conflict: abstract vs default

```java
interface X { void run(); }                         // abstract
interface Y { default void run() { } }              // default

class Z implements X, Y { }
// ERROR: types X and Y are incompatible; class Z inherits abstract and default for run() from types X and Y
```

**Fix:** override `run()` in `Z`.

### 9.6 Summary of the diamond solution

| Situation | What happens |
|-----------|--------------|
| Two **classes** with the same method | Not possible: a class can only extend one class |
| Superclass method + interface default | **Superclass wins** |
| Sub-interface overrides parent interface default | **Most specific wins** |
| Two unrelated interfaces with the same default | **Compile error**: you override and choose with `X.super.method()` |
| Interface abstract + other interface default | **Compile error**: override it |
| Same constant name in two interfaces | Ambiguous: use `Interface.NAME` |

Java's philosophy: **never guess silently**. If it can't be decided safely, it forces **you** to decide explicitly.

---

## 10. Default Methods (Java 8)

### 10.1 First principles: the problem they solve

Imagine you published this interface and **thousands of classes** (yours and other people's) implement it:

```java
interface Shape {
    double area();
}
```

Now you want to add a new method `perimeter()`. Before Java 8, adding an abstract method **breaks every existing implementing class**: they all stop compiling until they add it. For a library like the JDK, that's impossible.

**Need:** add methods to an interface **without breaking** existing implementations.
**Solution (Java 8):** a **default method**: an interface method **with a body** that implementing classes get **for free** (and may override).

### 10.2 Syntax

```java
interface Shape {
    double area();                                  // abstract

    default String describe() {                     // default method: has the `default` keyword and a body
        return "Shape with area " + area();
    }
}

class Circle implements Shape {
    private final double r;
    Circle(double r) { this.r = r; }
    public double area() { return Math.PI * r * r; }
    // describe() is NOT written here, but Circle has it anyway!
}

System.out.println(new Circle(1).describe());     // Shape with area 3.141592653589793
```

### 10.3 Rules for default methods

| Rule | Detail |
|------|--------|
| Keyword | `default` (only valid inside interfaces; not the same as the `switch` `default`) |
| Has a **body** | Yes |
| Access | Implicitly **`public`** |
| Inherited by implementing classes | **Yes** |
| Can be **overridden** by the class | **Yes** (optional) |
| Can be re-declared **abstract** in a sub-interface | Yes (forces implementors to write it) |
| Can call other methods of the interface (even abstract ones) | **Yes** (`area()` above) |
| `this` inside a default method | The **object** implementing the interface |
| Can be `final`, `synchronized`? | **No** |
| Can override `Object` methods (`toString`, `equals`, `hashCode`)? | **No** (compile error) |

```java
interface Bad {
    default String toString() { return "x"; }      // ERROR: default method toString in interface Bad overrides a member of java.lang.Object
}
```

### 10.4 Overriding a default method

```java
class Square implements Shape {
    private final double side;
    Square(double side) { this.side = side; }
    public double area() { return side * side; }

    @Override
    public String describe() {                      // custom version
        return "Square of side " + side;
    }
}
```

### 10.5 Default methods in the JDK (the reason they were invented)

When Java 8 added lambdas and streams, the JDK needed **new methods on old interfaces** like `List`, `Collection`, `Iterable`, `Map`:

| Interface | Default method added in Java 8 |
|-----------|-------------------------------|
| `Iterable` | `forEach(Consumer)` |
| `Collection` | `stream()`, `removeIf(Predicate)` |
| `List` | `sort(Comparator)`, `replaceAll(...)` |
| `Map` | `getOrDefault`, `forEach`, `computeIfAbsent`, `merge`... |
| `Comparator` | `reversed()`, `thenComparing(...)` |
| `Predicate` | `and()`, `or()`, `negate()` |

Without default methods, all the thousands of existing `List` implementations in the world would have broken overnight.

```java
List<String> names = new ArrayList<>(List.of("b", "a", "c"));
names.sort(null);                              // default method List.sort
names.forEach(System.out::println);            // default method Iterable.forEach
```

### 10.6 Interface evolution in action

**Version 1** of your library:

```java
interface Logger {
    void log(String message);
}
```

Many classes implement it. **Version 2** adds a feature without breaking anyone:

```java
interface Logger {
    void log(String message);

    default void logError(String message) {          // NEW: existing classes keep compiling
        log("ERROR: " + message);
    }
}
```

### 10.7 Don't overuse default methods

Interfaces still should mainly express **contracts**. Use defaults for:

- **Evolving** an API safely.
- **Convenience methods** built on the abstract ones (`describe()`, `and()`, `reversed()`).
- **Optional** behavior with a sensible fallback.

Don't use them to hide lots of logic that belongs in a class.

---

## 11. Static Methods in Interfaces (Java 8)

### 11.1 What are they?

An interface can contain **`static` methods with a body**. They belong to the **interface itself** and are called **through the interface name**.

```java
interface MathUtil {
    static int square(int x) {
        return x * x;
    }

    static boolean isEven(int x) {
        return x % 2 == 0;
    }
}

System.out.println(MathUtil.square(5));       // 25
System.out.println(MathUtil.isEven(4));       // true
```

### 11.2 Rules

| Rule | Detail |
|------|--------|
| Keyword | `static` + a body |
| Access | `public` by default (can be `private` since Java 9) |
| Called with | **`InterfaceName.method()` only** |
| **Inherited** by implementing classes or sub-interfaces? | **NO** |
| Can be overridden? | **No** (a class can define its own static method of the same name, but it's unrelated) |
| Can use `this`? | No (static) |
| Can call instance methods directly? | No (only static ones) |

```java
class Calc implements MathUtil { }

Calc.square(5);                       // ERROR: not inherited
new Calc().square(5);                 // ERROR
MathUtil.square(5);                   // ✓ only this
```

**Why not inherited?** A class can implement many interfaces. If static methods were inherited, two interfaces with the same static method would create exactly the ambiguity (diamond) problem again.

### 11.3 Why have them?

Before Java 8, helper methods for an interface went in a **separate utility class** (`Collection` + `Collections`, `Path` + `Paths`). Now they can live **right next to the interface**.

| JDK static interface method | Use |
|-----------------------------|-----|
| `List.of(...)`, `Set.of(...)`, `Map.of(...)` | Create immutable collections |
| `Comparator.comparing(...)`, `Comparator.naturalOrder()` | Build comparators |
| `Stream.of(...)` | Create streams |
| `Predicate.not(...)`, `Function.identity()` | Helpers |
| `Path.of(...)` | Create a path |

### 11.4 Typical uses

```java
interface Shape {
    double area();

    // a factory method living next to the interface
    static Shape circle(double radius) {
        return () -> Math.PI * radius * radius;      // Shape has one abstract method → lambda works
    }

    // a validation helper
    static boolean isValidSize(double size) {
        return size > 0;
    }
}

Shape c = Shape.circle(2);
System.out.printf("%.2f%n", c.area());               // 12.57
```

---

## 12. Private Methods in Interfaces (Java 9)

### 12.1 The problem

Suppose two default methods share the **same chunk of code**:

```java
interface Report {
    default void printHtml() {
        System.out.println("Start");              // duplicated
        System.out.println("<html>...</html>");
        System.out.println("End");                // duplicated
    }

    default void printText() {
        System.out.println("Start");              // duplicated
        System.out.println("plain text");
        System.out.println("End");                // duplicated
    }
}
```

We'd like a **helper method** to avoid repeating code. But in Java 8, any non-abstract interface method was **public**. A helper would become part of the public contract, which we don't want.

### 12.2 The solution: `private` methods (Java 9)

```java
interface Report {

    default void printHtml() {
        printWithFrame("<html>...</html>");
    }

    default void printText() {
        printWithFrame("plain text");
    }

    private void printWithFrame(String body) {         // hidden helper
        System.out.println("Start");
        System.out.println(body);
        System.out.println("End");
    }
}
```

The helper is **invisible** to implementing classes and users. This is **encapsulation** inside an interface.

### 12.3 Two kinds

```java
interface Util {
    // 1) private instance method: usable by default and other private instance methods
    private String clean(String s) { return s.trim().toLowerCase(); }

    // 2) private static method: usable by static methods (and default methods)
    private static boolean isBlank(String s) { return s == null || s.isEmpty(); }

    default String normalize(String s) {
        return isBlank(s) ? "" : clean(s);
    }

    static String safe(String s) {
        return isBlank(s) ? "N/A" : s;                // a static method can call a private STATIC method
    }
}
```

### 12.4 Rules

| Rule | Detail |
|------|--------|
| Keyword | `private` (with a body), optionally `static` |
| Can be **abstract**? | **No** (a private method must have a body) |
| Visible to | **Only code inside the same interface** |
| Not inherited, not overridable | Right |
| Private **instance** methods can be called from | default methods and other private instance methods |
| Private **static** methods can be called from | static, default and private methods |
| Static methods can call private **instance** methods? | **No** |
| Since | **Java 9** |

---

## 13. All Kinds of Interface Members at a Glance

| Member | Syntax | Has body? | Implicit modifiers | Inherited by implementers? | Must the class override? | Since |
|--------|--------|:---------:|--------------------|:--------------------------:|:------------------------:|:-----:|
| **Constant** | `int X = 5;` | n/a | `public static final` | Name is accessible | n/a | 1.0 |
| **Abstract method** | `void run();` | No | `public abstract` | Yes | **Yes** | 1.0 |
| **Default method** | `default void m() {...}` | Yes | `public` | Yes | No (optional) | 8 |
| **Static method** | `static void m() {...}` | Yes | `public` | **No** | n/a | 8 |
| **Private method** | `private void m() {...}` | Yes | `private` | No | n/a | 9 |
| **Private static method** | `private static void m() {...}` | Yes | `private` | No | n/a | 9 |
| **Nested type** | `class/interface/enum X {...}` | n/a | `public static` | Name accessible | n/a | 1.0 |

A complete example with every kind:

```java
public interface Vehicle {

    // 1) constant
    int MAX_SPEED = 200;

    // 2) abstract method: implementers MUST write it
    void drive();

    // 3) default method: implementers get it for free
    default void honk() {
        System.out.println(beep() + " " + beep());
    }

    // 4) static method: called as Vehicle.info()
    static String info() {
        return "Max speed " + MAX_SPEED;
    }

    // 5) private helper
    private String beep() {
        return "Beep!";
    }

    // 6) nested type
    enum Type { CAR, BIKE }
}
```

---

## 14. Interfaces vs Abstract Classes

### 14.1 The core intent

| | **Interface** | **Abstract class** |
|---|---------------|--------------------|
| **Expresses** | A **capability / role**: **"can-do"** | A **family** of similar things: **"is-a"** |
| **Examples** | `Comparable` (can be compared), `Runnable` (can run), `Flyable` (can fly) | `Animal`, `Vehicle`, `InputStream` |
| **Question it answers** | *"What can this thing do?"* | *"What kind of thing is this?"* |

```
 "is-a"  (abstract class)               "can-do" (interface)

         Animal                          Flyable     Swimmable     Runnable
        /      \                           ▲  ▲         ▲            ▲
      Dog      Bird        ──►            Bird Plane   Duck Fish    Thread Task
      (a Dog IS an Animal)                (Bird CAN fly, Plane CAN fly, Duck CAN swim...)
```

### 14.2 Detailed comparison

| Feature | **Interface** | **Abstract class** |
|---------|---------------|--------------------|
| Keywords | `interface` / `implements` | `abstract class` / `extends` |
| **How many** can a class use | **Many** | **One** |
| **Instance variables (state)** | ✗ (only constants) | ✓ |
| **Constructors** | ✗ | ✓ |
| **Concrete methods** | `default`, `static`, `private` only (Java 8+) | ✓ any |
| **Abstract methods** | ✓ (implicitly public) | ✓ (any access except private) |
| **Method access modifiers** | `public` (or `private` helpers) | `public`, `protected`, package, `private` |
| **Field access** | `public static final` only | any |
| **`final` methods** | ✗ | ✓ |
| **Can be instantiated** | ✗ | ✗ |
| **Can implement/extend** | extends **many interfaces** | extends **one class**, implements many interfaces |
| **Lambda target** | ✓ (functional interfaces) | ✗ |
| **Best for** | Defining a contract; unrelated classes; multiple roles | Sharing **code and state** among **closely related** classes |

### 14.3 Side by side code

**Interface (a capability):**

```java
interface Flyable {
    void fly();
}
```

**Abstract class (a family with shared state and code):**

```java
abstract class Animal {
    protected String name;                       // STATE: shared by all animals

    Animal(String name) { this.name = name; }    // CONSTRUCTOR

    void eat() {                                 // shared CODE
        System.out.println(name + " is eating");
    }

    abstract void makeSound();                   // each animal does its own
}

class Bird extends Animal implements Flyable {   // BOTH together
    Bird(String name) { super(name); }
    void makeSound() { System.out.println("Tweet"); }
    public void fly() { System.out.println(name + " flies"); }
}
```

`Bird` **is an** `Animal` (family) and **can** `Flyable` (ability). That's the natural combination.

### 14.4 When to choose which

```
 Do the classes share STATE (fields) or CONSTRUCTOR logic, and are they closely related?
     │ Yes → ABSTRACT CLASS
     │ No
     ▼
 Do UNRELATED classes need the same ability? Or does one class need MANY roles?
     │ Yes → INTERFACE
     │
 Not sure? → start with an INTERFACE (it's more flexible: classes can implement many)
```

**Rules of thumb:**

- Use an **interface** to define **what** (a type/contract). **Prefer interfaces** for public APIs.
- Use an **abstract class** to share **how** (common code/state) among related classes.
- Use **both**: an interface for the contract plus an abstract class giving a **skeletal implementation** that implementers can extend (the JDK does this: `List` interface + `AbstractList` class).

### 14.5 "But Java 8 gave interfaces method bodies: are they the same now?"

No. The big differences remain: **interfaces still have no instance state and no constructors**, and a class can implement **many** interfaces but extend only **one** class. Default methods exist mainly for **evolving** APIs.

### 14.6 The skeletal implementation pattern

```java
interface Stack {                                      // the contract
    void push(int x);
    int pop();
    boolean isEmpty();
    default int size() { return -1; }
}

abstract class AbstractStack implements Stack {        // shares common code/state
    protected int count;

    @Override
    public boolean isEmpty() { return count == 0; }
}

class ArrayStack extends AbstractStack {               // only the unique parts remain
    private final int[] data = new int[100];
    public void push(int x) { data[count++] = x; }
    public int pop() { return data[--count]; }
}
```

---

## 15. Functional Interfaces

### 15.1 Definition

> A **functional interface** is an interface with **exactly one abstract method**. (It may also have any number of default, static and private methods.)

It is the **target type for lambda expressions**, the doorway to functional programming in Java (Java 8).

```java
@FunctionalInterface
interface Greeter {
    void greet(String name);          // the ONE abstract method
}
```

### 15.2 Using a lambda instead of a class

**Old way (anonymous class):**

```java
Greeter g1 = new Greeter() {
    @Override
    public void greet(String name) {
        System.out.println("Hello, " + name);
    }
};
```

**New way (lambda):**

```java
Greeter g2 = name -> System.out.println("Hello, " + name);

g2.greet("Ali");              // Hello, Ali
```

Read the lambda as: *"given `name`, do this."* Java matches it to the **single abstract method** of the interface. That's why exactly one is required: otherwise it wouldn't know which method the lambda implements.

```
   Greeter g = name -> System.out.println("Hello, " + name);
                 │                  │
                 │                  └─ the BODY: becomes greet(String name){ ... }
                 └─ the PARAMETER of the one abstract method
```

### 15.3 The `@FunctionalInterface` annotation

```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}
```

- It is **optional**, but recommended. Any interface with one abstract method is functional anyway.
- It makes the **compiler check** your intent. If someone later adds a second abstract method, you get an error:

```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
    int other();          // ERROR: Unexpected @FunctionalInterface annotation: Calculator is not a functional interface (multiple non-overriding abstract methods found)
}
```

### 15.4 What does NOT count toward "exactly one"?

| Member | Counts as abstract method? |
|--------|:--------------------------:|
| `default` method | No |
| `static` method | No |
| `private` method | No |
| An abstract method that matches a **public method of `Object`** (e.g. `boolean equals(Object)`) | **No** |

That's why `Comparator` is still functional even though it declares `boolean equals(Object)`: only `compare(...)` counts.

```java
@FunctionalInterface
interface Validator {
    boolean validate(String s);                    // the ONE abstract method

    default Validator and(Validator other) {       // default: doesn't count
        return s -> validate(s) && other.validate(s);
    }

    static Validator notEmpty() {                  // static: doesn't count
        return s -> !s.isEmpty();
    }
}
```

### 15.5 Examples

```java
@FunctionalInterface
interface Calculator { int calculate(int a, int b); }

Calculator add      = (a, b) -> a + b;
Calculator multiply = (a, b) -> a * b;
Calculator maxOf    = (a, b) -> a > b ? a : b;

System.out.println(add.calculate(6, 3));        // 9
System.out.println(multiply.calculate(6, 3));   // 18
System.out.println(maxOf.calculate(6, 3));      // 6

static int run(Calculator c, int x, int y) {    // pass behavior as an argument
    return c.calculate(x, y);
}
System.out.println(run((a, b) -> a - b, 10, 4));   // 6
```

### 15.6 Built-in functional interfaces (`java.util.function` and others)

| Interface | Abstract method | Takes | Returns | Example lambda |
|-----------|----------------|-------|---------|----------------|
| `Runnable` | `void run()` | nothing | nothing | `() -> System.out.println("hi")` |
| `Callable<V>` | `V call()` | nothing | a value | `() -> 42` |
| `Comparator<T>` | `int compare(T a, T b)` | two | `int` | `(a, b) -> a.length() - b.length()` |
| `Function<T,R>` | `R apply(T t)` | one | a value | `s -> s.length()` |
| `BiFunction<T,U,R>` | `R apply(T t, U u)` | two | a value | `(a, b) -> a + b` |
| `Predicate<T>` | `boolean test(T t)` | one | `boolean` | `n -> n % 2 == 0` |
| `Consumer<T>` | `void accept(T t)` | one | nothing | `s -> System.out.println(s)` |
| `Supplier<T>` | `T get()` | nothing | a value | `() -> "hello"` |
| `UnaryOperator<T>` | `T apply(T t)` | one | same type | `s -> s.trim()` |
| `BinaryOperator<T>` | `T apply(T a, T b)` | two | same type | `(a, b) -> a * b` |

```java
import java.util.function.*;

Function<Integer, Integer> square = x -> x * x;
Predicate<Integer> isEven        = n -> n % 2 == 0;
Supplier<String> greeting        = () -> "Hello!";
Consumer<String> printer         = s -> System.out.println(s);
BinaryOperator<Integer> sum      = (a, b) -> a + b;

System.out.println(square.apply(7));                 // 49
System.out.println(isEven.test(10));                 // true
System.out.println(greeting.get());                  // Hello!
printer.accept("Hi");                                // Hi
System.out.println(sum.apply(3, 4));                 // 7
```

### 15.7 Method references (a shorter lambda)

```java
Consumer<String> p1 = s -> System.out.println(s);
Consumer<String> p2 = System.out::println;           // same thing

Function<String, Integer> parse = Integer::parseInt;
```

### 15.8 Default methods make functional interfaces composable

```java
Predicate<Integer> isEven     = n -> n % 2 == 0;
Predicate<Integer> isPositive = n -> n > 0;

System.out.println(isEven.and(isPositive).test(4));    // true
System.out.println(isEven.and(isPositive).test(-4));   // false
System.out.println(isEven.negate().test(7));           // true

Function<Integer, Integer> square = x -> x * x;
System.out.println(square.andThen(x -> x + 1).apply(5));   // 26
```

### 15.9 Why functional interfaces matter

- They let you **pass behavior as data** (callbacks, strategies, comparators).
- They power **streams**, **`Optional`**, **`CompletableFuture`**, event handlers and more.
- Anonymous classes become **one-liners**.

(The next topic after interfaces is exactly this: **lambda expressions and functional programming**.)

---

## 16. Marker Interfaces

### 16.1 What are they?

A **marker interface** is an **empty interface** (no methods, no constants). It doesn't give the class any behavior. It just **tags (marks)** the class with a **piece of metadata** that other code (the JVM or libraries) can check.

```java
public interface Cloneable { }             // empty!
public interface Serializable { }          // empty!
public interface RandomAccess { }          // empty!
```

Think of it like a **sticker** on a box: **"FRAGILE"**. The sticker does nothing itself. The delivery people check it and handle the box differently.

### 16.2 The famous markers

| Interface | Package | The "sticker" says | Who checks it |
|-----------|---------|--------------------|---------------|
| **`Cloneable`** | `java.lang` | "This class allows `clone()`" | `Object.clone()` throws `CloneNotSupportedException` if missing |
| **`Serializable`** | `java.io` | "Objects of this class can be written to a stream/file" | `ObjectOutputStream` throws `NotSerializableException` if missing |
| **`RandomAccess`** | `java.util` | "This list supports fast index access" | `Collections.binarySearch` etc. choose a better algorithm |
| `EventListener` | `java.util` | "This is an event listener" | Frameworks |
| `Remote` | `java.rmi` | "Can be used remotely" | RMI |

### 16.3 How the check works

The JVM/library simply uses **`instanceof`**:

```java
if (obj instanceof Serializable) {
    // allowed to serialize
} else {
    throw new NotSerializableException(obj.getClass().getName());
}
```

### 16.4 Example: `Serializable`

```java
import java.io.*;

class Student implements Serializable {          // the marker: allowed to be saved
    String name = "Ali";
    int age = 20;
}

class Course {                                   // NO marker
    String title = "Java";
}

try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("data.bin"))) {
    out.writeObject(new Student());              // ✓ works
    out.writeObject(new Course());               // ✗ java.io.NotSerializableException: Course
}
```

### 16.5 Example: `RandomAccess`

```java
List<Integer> arrayList = new ArrayList<>();
List<Integer> linkedList = new LinkedList<>();

System.out.println(arrayList instanceof RandomAccess);     // true  (fast get(i))
System.out.println(linkedList instanceof RandomAccess);    // false (get(i) walks the chain)
```

### 16.6 Your own marker interface

```java
interface Auditable { }                       // marker: "log every save of this object"

class Order implements Auditable { }
class Product { }

class Repository {
    static void save(Object o) {
        if (o instanceof Auditable) {
            System.out.println("Saving " + o.getClass().getSimpleName() + " WITH audit log");
        } else {
            System.out.println("Saving " + o.getClass().getSimpleName());
        }
    }
}

Repository.save(new Order());      // Saving Order WITH audit log
Repository.save(new Product());    // Saving Product
```

### 16.7 Marker interface vs annotation

| | Marker interface | Annotation (e.g. `@Deprecated`, custom `@Auditable`) |
|---|------------------|------------------|
| Defines a **type** | **Yes**: you can write `void save(Auditable a)` | No |
| Checked at **compile time** via types | Yes | Not by types |
| Can mark **methods/fields** | No (classes only) | Yes |
| Carries **data** (parameters) | No | Yes |
| Modern preference | Used for JDK legacy markers | Preferred for **new** metadata |

Use a marker **interface** when you want the marker to be a **type**. Otherwise prefer an **annotation**.

---

## 17. Generic Interfaces

### 17.1 The idea

Interfaces can take **type parameters**, so one contract works for many types, with type safety.

```java
interface Repository<T, ID> {
    void save(T item);
    T findById(ID id);
    java.util.List<T> findAll();
}
```

### 17.2 Implementing with concrete types

```java
class Student {
    final int id; final String name;
    Student(int id, String name) { this.id = id; this.name = name; }
    @Override public String toString() { return name; }
}

class StudentRepository implements Repository<Student, Integer> {      // T=Student, ID=Integer
    private final java.util.Map<Integer, Student> store = new java.util.HashMap<>();

    @Override public void save(Student s)        { store.put(s.id, s); }
    @Override public Student findById(Integer id) { return store.get(id); }
    @Override public java.util.List<Student> findAll() { return new java.util.ArrayList<>(store.values()); }
}

Repository<Student, Integer> repo = new StudentRepository();
repo.save(new Student(1, "Ali"));
System.out.println(repo.findById(1));       // Ali
```

### 17.3 Famous generic interfaces

`Comparable<T>`, `Comparator<T>`, `Iterable<T>`, `Iterator<T>`, `List<E>`, `Map<K,V>`, `Function<T,R>`, `Supplier<T>`.

### 17.4 Generic methods in interfaces

```java
interface Converter {
    <T> String convert(T value);               // the METHOD has its own type parameter
}
```

---

## 18. Nested Interfaces and Interfaces with Enums, Records and Anonymous Classes

### 18.1 Interface inside a class

```java
class Button {
    interface ClickListener {                  // nested interface (implicitly static)
        void onClick();
    }

    private ClickListener listener;
    void setListener(ClickListener l) { this.listener = l; }
    void click() { if (listener != null) listener.onClick(); }
}

Button b = new Button();
b.setListener(() -> System.out.println("Clicked!"));      // a lambda (functional interface)
b.click();                                                // Clicked!
```

### 18.2 Class or enum inside an interface

Types declared inside an interface are implicitly **`public static`**:

```java
interface Vehicle {
    enum Type { CAR, BIKE }
    class Info { String label = "info"; }
}

Vehicle.Type t = Vehicle.Type.CAR;
```

### 18.3 Enums implement interfaces

```java
interface Discount { double apply(double price); }

enum Season implements Discount {
    SUMMER { public double apply(double p) { return p * 0.9; } },
    WINTER { public double apply(double p) { return p * 0.8; } }
}

System.out.println(Season.WINTER.apply(100));     // 80.0
```

### 18.4 Records implement interfaces (Java 16+)

```java
interface Shape { double area(); }

record Rectangle(double w, double h) implements Shape {
    public double area() { return w * h; }
}
record Circle(double r) implements Shape {
    public double area() { return Math.PI * r * r; }
}
```

### 18.5 Anonymous classes and lambdas

```java
Runnable r = new Runnable() {                      // an anonymous CLASS that implements Runnable
    @Override public void run() { System.out.println("run"); }
};

Runnable r2 = () -> System.out.println("run");     // a lambda (needs a functional interface)
```

`new Runnable() { ... }` doesn't instantiate the interface; it creates an **unnamed class that implements it**.

---

## 19. Important Interfaces in the JDK

### 19.1 The map of the standard ones

| Group | Interfaces |
|-------|-----------|
| **Collections** | `Iterable`, `Collection`, `List`, `Set`, `Queue`, `Deque`, `Map` |
| **Ordering** | `Comparable<T>`, `Comparator<T>` |
| **Iteration** | `Iterable<T>`, `Iterator<T>` |
| **Resources** | `AutoCloseable`, `Closeable` |
| **Concurrency** | `Runnable`, `Callable<V>`, `Executor`, `Future<V>`, `Lock` |
| **Text** | `CharSequence` (implemented by `String`, `StringBuilder`) |
| **Markers** | `Serializable`, `Cloneable`, `RandomAccess` |
| **Functional** | `Function`, `Predicate`, `Supplier`, `Consumer`... |

Programming to the interface is why we write `List<String> list = new ArrayList<>();` and not `ArrayList<String> list = ...`: we can swap `ArrayList` for `LinkedList` without touching the rest of the code.

### 19.2 `Comparable<T>`: "I can be ordered"

```java
class Student implements Comparable<Student> {
    final String name;
    final int marks;

    Student(String name, int marks) { this.name = name; this.marks = marks; }

    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.marks, other.marks);
    }

    @Override
    public String toString() { return name + "(" + marks + ")"; }
}

Student[] arr = { new Student("Ali", 85), new Student("Sara", 70), new Student("John", 92) };
java.util.Arrays.sort(arr);                                    // works because Student is Comparable
System.out.println(java.util.Arrays.toString(arr));            // [Sara(70), Ali(85), John(92)]
```

`Arrays.sort` can sort **any** array of `Comparable` objects. It only needs the contract `compareTo`.

### 19.3 `Iterable<T>`: "I can be used in a for-each loop"

```java
import java.util.Iterator;

class Range implements Iterable<Integer> {
    private final int start, end;

    Range(int start, int end) { this.start = start; this.end = end; }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            int current = start;
            public boolean hasNext() { return current < end; }
            public Integer next()    { return current++; }
        };
    }
}

for (int n : new Range(1, 5)) {
    System.out.print(n + " ");              // 1 2 3 4
}
```

Any class that implements `Iterable` works in a for-each loop.

### 19.4 `AutoCloseable`: "I can be used in try-with-resources"

```java
class Resource implements AutoCloseable {
    private final String name;
    Resource(String name) { this.name = name; System.out.println("Open " + name); }
    @Override public void close() { System.out.println("Close " + name); }
}

try (Resource a = new Resource("A"); Resource b = new Resource("B")) {
    System.out.println("Working");
}
```

Output:
```
Open A
Open B
Working
Close B
Close A
```

Resources close automatically, in **reverse order**.

### 19.5 `Runnable` and `Callable`

```java
Runnable task = () -> System.out.println("Running in a thread");
new Thread(task).start();
```

---

## 20. Design with Interfaces: Loose Coupling, Strategy and Testing

### 20.1 Program to an interface, not an implementation

```java
// TIGHT: stuck with the concrete class
class Notifier {
    private final EmailSender sender = new EmailSender();
}

// LOOSE: depends only on the contract
class Notifier {
    private final MessageSender sender;
    Notifier(MessageSender sender) { this.sender = sender; }
}
```

### 20.2 Full example: swap implementations freely

```java
interface MessageSender {
    void send(String to, String message);
}

class EmailSender implements MessageSender {
    public void send(String to, String message) { System.out.println("EMAIL to " + to + ": " + message); }
}

class SmsSender implements MessageSender {
    public void send(String to, String message) { System.out.println("SMS to " + to + ": " + message); }
}

class NotificationService {
    private final MessageSender sender;                         // the interface

    NotificationService(MessageSender sender) { this.sender = sender; }

    void notifyUser(String user, String message) {
        sender.send(user, "[Notice] " + message);
    }
}
```

```java
new NotificationService(new EmailSender()).notifyUser("ali@mail.com", "Server restarted");
new NotificationService(new SmsSender()).notifyUser("0300123", "Server restarted");
```

Output:
```
EMAIL to ali@mail.com: [Notice] Server restarted
SMS to 0300123: [Notice] Server restarted
```

`NotificationService` never changed. This idea is called **dependency injection**: the dependency (the sender) is **given** to the class instead of created inside it.

### 20.3 Testing with a fake (a lambda works too)

Because `MessageSender` has one abstract method, a **lambda** can act as a test double:

```java
java.util.List<String> log = new java.util.ArrayList<>();

NotificationService fake = new NotificationService((to, msg) -> log.add(to + " <- " + msg));
fake.notifyUser("test-user", "Hello");

System.out.println(log);          // [test-user <- [Notice] Hello]
```

No real email was sent, and we could check exactly what would have been sent.

### 20.4 Strategy pattern

Put each **algorithm** behind an interface and choose one at runtime:

```java
interface DiscountStrategy { double apply(double price); }

class NoDiscount implements DiscountStrategy { public double apply(double p) { return p; } }
class StudentDiscount implements DiscountStrategy { public double apply(double p) { return p * 0.9; } }

class Cart {
    private DiscountStrategy strategy = new NoDiscount();
    void setStrategy(DiscountStrategy s) { this.strategy = s; }
    double total(double price) { return strategy.apply(price); }
}

Cart cart = new Cart();
System.out.println(cart.total(200));                    // 200.0
cart.setStrategy(new StudentDiscount());
System.out.println(cart.total(200));                    // 180.0
cart.setStrategy(p -> p * 0.5);                         // a lambda as a strategy
System.out.println(cart.total(200));                    // 100.0
```

### 20.5 Callbacks / listeners (Observer idea)

```java
interface DownloadListener {
    void onComplete(String file);
}

static void download(String file, DownloadListener listener) {
    System.out.println("Downloading " + file + "...");
    listener.onComplete(file);                           // call back when done
}

download("data.zip", f -> System.out.println("Finished " + f));
```

---

## 21. Sealed Interfaces (Java 17+)

### 21.1 The problem

Normally **anyone** can implement your public interface. Sometimes you want a **closed, known set** of implementations (like an enum, but with different data per case).

### 21.2 The solution: `sealed ... permits`

```java
public sealed interface Shape permits Circle, Rectangle { }

public record Circle(double radius) implements Shape { }
public record Rectangle(double width, double height) implements Shape { }
```

Only `Circle` and `Rectangle` may implement `Shape`. Any other class gets a compile error. Each permitted class must be `final`, `sealed` or `non-sealed` (records are implicitly final).

### 21.3 The payoff: exhaustive `switch` (Java 21)

```java
static double area(Shape s) {
    return switch (s) {
        case Circle c    -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.width() * r.height();
    };                                                    // no default needed: the compiler knows all cases
}

System.out.println(area(new Rectangle(3, 4)));            // 12.0
System.out.printf("%.2f%n", area(new Circle(2)));         // 12.57
```

If you add a third shape later, every such `switch` becomes a **compile error** until you handle it. This is the same safety you get from enums (see the enums guide), now for **interfaces**.

---

## 22. Internal Working: What the Compiler and JVM Do

### 22.1 An interface is compiled to a `.class` file, just like a class

```java
public interface Drawable {
    int MAX = 10;
    void draw();
}
```

```
javac Drawable.java        →  Drawable.class
javap -v Drawable
```

The class file contains the same kind of structure as a class, **plus special flags**. A trimmed version of the output:

```
public interface Drawable
  flags: (0x0601) ACC_PUBLIC, ACC_INTERFACE, ACC_ABSTRACT
  this_class:  Drawable
  super_class: java/lang/Object
  interfaces: 0, fields: 1, methods: 1

  public static final int MAX;
    flags: (0x0019) ACC_PUBLIC, ACC_STATIC, ACC_FINAL
    ConstantValue: int 10

  public abstract void draw();
    flags: (0x0401) ACC_PUBLIC, ACC_ABSTRACT
```

### 22.2 The flags explained (the video's `ACC_INTERFACE`)

Each class file has an **access flags** number. The JVM reads the flags to know what kind of thing it is:

| Flag | Hex value | Meaning |
|------|-----------|---------|
| `ACC_PUBLIC` | `0x0001` | Public |
| `ACC_STATIC` | `0x0008` | Static |
| `ACC_FINAL` | `0x0010` | Final |
| **`ACC_INTERFACE`** | **`0x0200`** | **This is an interface** |
| `ACC_ABSTRACT` | `0x0400` | Abstract |
| `ACC_ANNOTATION` | `0x2000` | Annotation interface (`@interface`) |
| `ACC_ENUM` | `0x4000` | Enum |

```
 Interface flags:  0x0001 (PUBLIC) + 0x0200 (INTERFACE) + 0x0400 (ABSTRACT) = 0x0601
 Constant flags:   0x0001 (PUBLIC) + 0x0008 (STATIC)    + 0x0010 (FINAL)    = 0x0019
 Method flags:     0x0001 (PUBLIC) + 0x0400 (ABSTRACT)                       = 0x0401
```

So the compiler treats an interface **essentially like a class**, but it **tags** it with `ACC_INTERFACE` (and always `ACC_ABSTRACT`). That's how the JVM knows: *"don't instantiate this; check implementers; use interface rules."* The implicit modifiers (`public`, `static`, `final`, `abstract`) are **written explicitly into the class file** by the compiler.

### 22.3 What the implementing class's file contains

```java
class Circle implements Drawable { public void draw() { } }
```

The `Circle.class` file has an **`interfaces` table** listing `Drawable`:

```
class Circle
  super_class: java/lang/Object
  interfaces: 1       ← Drawable
```

At class-load time the JVM **verifies** that `Circle` provides every abstract method, otherwise you'd get `AbstractMethodError` at runtime (rare; it happens only when classes were compiled against different versions).

### 22.4 Which bytecode instruction calls an interface method?

| Instruction | Used for |
|-------------|----------|
| `invokevirtual` | Calling a method through a **class** reference |
| **`invokeinterface`** | Calling a method through an **interface** reference |
| `invokestatic` | Calling a **static** method (including interface static methods) |
| `invokespecial` | Constructors, private methods, and `super` calls (including `Interface.super.method()`) |
| `invokedynamic` | **Lambdas** and method references |

```java
Drawable d = new Circle();
d.draw();          // compiled to invokeinterface Drawable.draw
```

### 22.5 Why interface calls need a lookup

With a **class** reference, the JVM can use a fixed slot in the class's method table (vtable). With an **interface** reference, the object could be **any unrelated class** that implements it, so the method isn't at a fixed position in every class. The JVM uses a lookup structure (an *itable* in HotSpot) and caches the result. In practice, the **JIT compiler** optimizes this heavily (inline caches, inlining when it sees only one or two implementations), so interface calls are usually **as fast as class calls**.

```
 Drawable d  ──► [ Circle object ] ──► Circle's class info ──► method table ──► Circle.draw()
                                                                     ▲
                       JVM looks up "draw" for the interface Drawable (cached by the JIT)
```

### 22.6 Default and private methods in the class file

- A **default method** is stored as a normal **non-abstract** method of the interface. This is why class-file version **52** (Java 8) was needed.
- A **private interface method** needs version **53** (Java 9).
- A call `Walker.super.move()` compiles to an `invokespecial` on the interface method.

### 22.7 Lambdas and functional interfaces

```java
Greeter g = name -> System.out.println("Hello, " + name);
```

The compiler turns the lambda body into a **hidden method** and uses **`invokedynamic`** to create an object implementing `Greeter` at runtime. No extra `.class` file like `Outer$1.class` is generated (unlike an anonymous class).

### 22.8 Memory view

```
 Interfaces take NO object memory by themselves.

 Object in the heap:  [ header | fields of the CLASS ]   ← one object, no per-interface copy
 Class metadata:      [ methods | interfaces implemented: Drawable, Serializable ]

 Reference variable:  Drawable d  → just an address to the object (same size as any reference)
```

A class implementing 10 interfaces doesn't make its objects bigger. Interfaces only add **entries to the class's metadata**.

---

## 23. Version-by-Version History

| Version | Year | Interface-related change |
|---------|------|--------------------------|
| **Java 1.0** | 1996 | Interfaces exist: abstract methods and constants only. Multiple inheritance of type through interfaces |
| **Java 1.1** | 1997 | Anonymous inner classes (the old way to implement an interface inline) |
| **Java 5** | 2004 | **Generics** (generic interfaces like `Comparable<T>`), annotations, enums can implement interfaces |
| **Java 6** | 2006 | `@Override` allowed on methods that implement interface methods |
| **Java 8** | 2014 | **Default methods**, **static methods**, **lambdas**, `@FunctionalInterface`, `java.util.function`, streams |
| **Java 9** | 2017 | **Private** interface methods (and private static) |
| **Java 16** | 2021 | **Records** (can implement interfaces); pattern matching for `instanceof` |
| **Java 17** | 2021 | **Sealed** classes and interfaces (`sealed`, `permits`) |
| **Java 21** | 2023 | **Pattern-matching `switch`** with exhaustive checks over sealed interfaces |

```
 1996 ─────── 2004 ──────── 2014 ──────── 2017 ──────── 2021 ──────── 2023
  abstract     generics      default       private       sealed        pattern
  only         annotations   static        methods       records       switch
                             lambdas
```

---

## 24. Practical Programs

Save each as the named file, compile with `javac FileName.java`, run with `java ClassName`.

### 24.1 `PaymentDemo`: polymorphism through an interface

```java
interface PaymentMethod {
    void pay(double amount);
}

class CreditCard implements PaymentMethod {
    private final String holder;
    CreditCard(String holder) { this.holder = holder; }

    @Override
    public void pay(double amount) {
        System.out.println(holder + " paid " + amount + " by credit card");
    }
}

class PayPal implements PaymentMethod {
    private final String email;
    PayPal(String email) { this.email = email; }

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " via PayPal account " + email);
    }
}

class CryptoWallet implements PaymentMethod {
    private final String address;
    CryptoWallet(String address) { this.address = address; }

    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " from crypto wallet " + address);
    }
}

class Checkout {
    void process(PaymentMethod method, double amount) {
        method.pay(amount);                       // works for ANY PaymentMethod
    }
}

public class PaymentDemo {
    public static void main(String[] args) {
        Checkout checkout = new Checkout();

        PaymentMethod[] methods = {
            new CreditCard("Ali"),
            new PayPal("sara@mail.com"),
            new CryptoWallet("0xAB12")
        };

        for (PaymentMethod m : methods) {
            checkout.process(m, 49.99);
        }
    }
}
```

Output:
```
Ali paid 49.99 by credit card
Paid 49.99 via PayPal account sara@mail.com
Paid 49.99 from crypto wallet 0xAB12
```

### 24.2 `OfficeDemo`: multiple interfaces and a default method

```java
interface Printable {
    void print(String doc);
}

interface Scannable {
    String scan();
}

interface Faxable {
    default void fax(String doc) {
        System.out.println("Faxing: " + doc);
    }
}

class AllInOnePrinter implements Printable, Scannable, Faxable {
    @Override public void print(String doc) { System.out.println("Printing: " + doc); }
    @Override public String scan()          { return "scanned-image"; }
}

class SimplePrinter implements Printable {
    @Override public void print(String doc) { System.out.println("Simple printing: " + doc); }
}

public class OfficeDemo {
    public static void main(String[] args) {
        AllInOnePrinter all = new AllInOnePrinter();
        all.print("Report");
        System.out.println(all.scan());
        all.fax("Contract");

        Printable onlyPrint = all;                   // same object, seen through ONE role
        onlyPrint.print("Slides");

        Object simple = new SimplePrinter();
        System.out.println("SimplePrinter scannable? " + (simple instanceof Scannable));
        System.out.println("AllInOne scannable?      " + (all instanceof Scannable));
    }
}
```

Output:
```
Printing: Report
scanned-image
Faxing: Contract
Printing: Slides
SimplePrinter scannable? false
AllInOne scannable?      true
```

### 24.3 `DiamondDemo`: the three resolution rules

```java
// ---- Rule 1: class wins ----
class Animal {
    public void sound() { System.out.println("Animal sound"); }
}
interface Loud {
    default void sound() { System.out.println("LOUD SOUND"); }
}
class Dog extends Animal implements Loud { }

// ---- Rule 2: most specific interface wins ----
interface A {
    default void hi() { System.out.println("Hi from A"); }
}
interface B extends A {
    @Override
    default void hi() { System.out.println("Hi from B"); }
}
class C implements A, B { }

// ---- Rule 3: unrelated interfaces: you must choose ----
interface Walker {
    default void move() { System.out.println("Walking"); }
}
interface Swimmer {
    default void move() { System.out.println("Swimming"); }
}
class Duck implements Walker, Swimmer {
    @Override
    public void move() {
        Walker.super.move();
        Swimmer.super.move();
        System.out.println("Duck moves both ways");
    }
}

public class DiamondDemo {
    public static void main(String[] args) {
        System.out.println("--- Rule 1 ---");
        new Dog().sound();

        System.out.println("--- Rule 2 ---");
        new C().hi();

        System.out.println("--- Rule 3 ---");
        new Duck().move();
    }
}
```

Output:
```
--- Rule 1 ---
Animal sound
--- Rule 2 ---
Hi from B
--- Rule 3 ---
Walking
Swimming
Duck moves both ways
```

### 24.4 `ValidatorDemo`: abstract + default + static + private together

```java
@FunctionalInterface
interface Validator {

    boolean validate(String input);                          // the one abstract method

    default Validator and(Validator other) {                 // default: combine validators
        return s -> validate(s) && other.validate(s);
    }

    default String describe(String input) {                  // default using a private helper
        return format(input, validate(input));
    }

    static Validator notEmpty() {                            // static factories
        return s -> s != null && !s.isEmpty();
    }

    static Validator minLength(int n) {
        return s -> s != null && s.length() >= n;
    }

    private String format(String input, boolean ok) {       // private helper (Java 9)
        return "\"" + input + "\" is " + (ok ? "valid" : "invalid");
    }
}

public class ValidatorDemo {
    public static void main(String[] args) {
        Validator v = Validator.notEmpty().and(Validator.minLength(3));

        System.out.println(v.describe("hi"));
        System.out.println(v.describe("java"));
        System.out.println(v.describe(""));

        Validator custom = s -> s.startsWith("A");           // a lambda is a Validator too
        System.out.println(custom.describe("Ali"));
    }
}
```

Output:
```
"hi" is invalid
"java" is valid
"" is invalid
"Ali" is valid
```

### 24.5 `CapabilityDemo`: functional interfaces, markers and sealed interfaces

```java
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}

interface Auditable { }                                      // marker interface

class Order implements Auditable { }
class Product { }

sealed interface Shape permits Circle, Rectangle { }
record Circle(double radius) implements Shape { }
record Rectangle(double width, double height) implements Shape { }

public class CapabilityDemo {

    static int run(Calculator c, int x, int y) {
        return c.calculate(x, y);
    }

    static void save(Object o) {
        if (o instanceof Auditable) {
            System.out.println("Saving " + o.getClass().getSimpleName() + " WITH audit log");
        } else {
            System.out.println("Saving " + o.getClass().getSimpleName());
        }
    }

    static double area(Shape s) {
        return switch (s) {
            case Circle c    -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.width() * r.height();
        };
    }

    public static void main(String[] args) {
        // functional interface + lambdas
        Calculator add = (a, b) -> a + b;
        Calculator mul = (a, b) -> a * b;
        System.out.println(run(add, 6, 3));                  // 9
        System.out.println(run(mul, 6, 3));                  // 18
        System.out.println(run((a, b) -> a - b, 10, 4));     // 6

        Function<Integer, Integer> square = x -> x * x;
        Predicate<Integer> isEven = n -> n % 2 == 0;
        System.out.println(square.apply(7));                 // 49
        System.out.println(isEven.test(10));                 // true

        // marker interface
        save(new Order());
        save(new Product());

        // sealed interface + records (Java 21 for the pattern switch)
        System.out.println(area(new Rectangle(3, 4)));       // 12.0
        System.out.printf("%.2f%n", area(new Circle(2)));    // 12.57
    }
}
```

Output:
```
9
18
6
49
true
Saving Order WITH audit log
Saving Product
12.0
12.57
```

(This one needs **Java 21** because of the pattern-matching `switch`. On Java 17, replace the `switch` with `instanceof` checks.)

### 24.6 Try it yourself

1. Create `interface Animal { void sound(); }` with `Dog`, `Cat`, `Cow` implementations and loop over an `Animal[]`.
2. Add a `default void sleep()` method to `Animal` without touching `Dog`, `Cat` or `Cow`. Override it in one class only.
3. Create two interfaces with the same default method and a class that implements both. Read the compiler error, then fix it with `X.super.method()`.
4. Create `interface Stack<T>` and implement it with an array and with a `LinkedList`. Swap them behind the interface.
5. Write a `@FunctionalInterface TriFunction<A,B,C,R>` and use a lambda.
6. Make a marker interface `Archivable` and a method that treats objects differently based on `instanceof`.
7. Use `javap -v` on one of your interfaces and find `ACC_INTERFACE`.
8. Make an abstract class `Vehicle` (shared state) and interfaces `Electric` and `Autonomous`, then build `Tesla extends Vehicle implements Electric, Autonomous`.

---

## 25. Common Mistakes

| # | Mistake | What happens | Fix |
|---|---------|--------------|-----|
| 1 | `new Shape()` on an interface | "is abstract; cannot be instantiated" | Instantiate an implementing class (or use a lambda / anonymous class) |
| 2 | Implementing a method without `public` | "attempting to assign weaker access privileges; was public" | Add `public` |
| 3 | Not implementing all abstract methods | "is not abstract and does not override abstract method..." | Implement them, or make the class `abstract` |
| 4 | Using `extends` for a class → interface | Compile error | Use `implements` |
| 5 | Writing `protected`/`private` on an abstract interface method | Compile error | Interface methods are `public` |
| 6 | Trying to change an interface variable | "cannot assign a value to final variable" | They are constants |
| 7 | Declaring an interface variable without a value | "= expected" | Initialize it |
| 8 | Calling a static interface method through the class or an object | Compile error (not inherited) | `InterfaceName.method()` |
| 9 | Two interfaces with the same default method, class doesn't override | "inherits unrelated defaults" | Override and use `X.super.method()` |
| 10 | Default method overriding `toString`/`equals`/`hashCode` | Compile error | Not allowed; override in the class |
| 11 | Calling a class-specific method through an interface reference | "cannot find symbol" | Cast after `instanceof` |
| 12 | Adding a second abstract method to a `@FunctionalInterface` | Compile error | Make it `default`, or remove the annotation |
| 13 | Using a lambda for an interface with 2+ abstract methods | "not a functional interface" | Use a class or anonymous class |
| 14 | Same constant name in two interfaces, used unqualified | "reference to X is ambiguous" | `Interface.X` |
| 15 | A "constants interface" implemented by many classes | Pollutes class APIs | Use a class or `enum` |
| 16 | Forgetting the marker interface (`Serializable`) | `NotSerializableException` at runtime | Implement it (and its fields too) |
| 17 | Overusing default methods to hide complex logic | Interface becomes a half-class | Put logic in classes |
| 18 | Making an interface for everything (one implementation, no need) | Over-engineering | Add an interface when there is a real need for several implementations or decoupling |
| 19 | Private abstract method in an interface | Compile error | Private methods need a body |
| 20 | Static interface method calling an instance method | Compile error | Make the helper `static`/`private static` |
| 21 | Circular interface inheritance | Compile error | Fix the hierarchy |
| 22 | Throwing broader checked exceptions in the implementation | Compile error | Throw the same or narrower |

---

## 26. Cheat Sheet, Summary and Quiz

### 26.1 Syntax cheat sheet

```java
// ---------- declare ----------
public interface Vehicle extends Movable, Fuelable {      // extends MANY interfaces
    int MAX_SPEED = 200;                                  // public static final
    void drive();                                         // public abstract
    default void honk() { System.out.println("Beep"); }   // default (Java 8)
    static Vehicle of(String n) { ... }                   // static (Java 8)
    private void helper() { ... }                         // private (Java 9)
}

// ---------- implement ----------
class Car extends Base implements Vehicle, Serializable { // ONE class, MANY interfaces
    @Override public void drive() { ... }                 // must be public
}

// ---------- use ----------
Vehicle v = new Car();            // interface reference → polymorphism
v.drive();   v.honk();   Vehicle.of("x");   Vehicle.MAX_SPEED;
if (v instanceof Car c) { ... }   // downcast safely

// ---------- resolve a default-method conflict ----------
class Duck implements A, B {
    public void move() { A.super.move(); }
}

// ---------- functional interface + lambda ----------
@FunctionalInterface interface Calc { int apply(int a, int b); }
Calc add = (a, b) -> a + b;

// ---------- marker ----------
interface Auditable { }

// ---------- sealed (Java 17) ----------
sealed interface Shape permits Circle, Square { }
```

### 26.2 Interface vs class vs abstract class (quick)

| | Class | Abstract class | Interface |
|---|-------|----------------|-----------|
| Instantiate | ✓ | ✗ | ✗ |
| Instance fields | ✓ | ✓ | ✗ (constants only) |
| Constructors | ✓ | ✓ | ✗ |
| Abstract methods | ✗ | ✓ | ✓ |
| Concrete methods | ✓ | ✓ | `default`/`static`/`private` |
| Inherit from | 1 class | 1 class | **many** interfaces |
| Relationship | is-a | is-a | **can-do** |

### 26.3 Summary in simple points

1. An **interface** is a **contract/blueprint of behavior**: it says **what** an object can do, not **how**. It **can't be instantiated**.
2. A class uses **`implements`** and **must override every abstract method**. Interface methods are implicitly **`public`**, so the override must be **`public`** (you can't reduce visibility).
3. An interface reference can hold any implementing object. The real object's method runs: **dynamic polymorphism**. You can only call what the interface declares (cast to reach more).
4. Interfaces exist for **unrelated classes sharing abilities**, **multiple roles** (a class can implement many), **loose coupling**, **teamwork**, **testing** and **polymorphism**.
5. **Variables** in an interface are **implicitly `public static final`** constants and must be initialized.
6. **Java classes** can't extend multiple classes (to avoid the **diamond problem**), but a class can implement **many interfaces**, and an interface can **extend many interfaces**.
7. With **default methods**, conflicts are resolved by: **class wins**, **most specific interface wins**, otherwise **the compiler errors** and you override and choose with **`Interface.super.method()`**.
8. **Default methods (Java 8)** add methods to interfaces **without breaking** implementers. **Static methods (Java 8)** are utilities called with the **interface name** and are **not inherited**. **Private methods (Java 9)** share code inside the interface **without exposing it**.
9. **Interface = "can-do" (capability); abstract class = "is-a" (family with shared state and code)**. Interfaces have no state or constructors; a class can implement many but extend one.
10. A **functional interface** has **exactly one abstract method** and is the target for **lambdas** (`@FunctionalInterface` helps the compiler check it).
11. A **marker interface** is **empty**; it tags a class so the JVM or libraries can check it with `instanceof` (`Serializable`, `Cloneable`, `RandomAccess`).
12. Internally, an interface is compiled to a `.class` file with the **`ACC_INTERFACE`** (and `ACC_ABSTRACT`) flags. Calls through interface references use **`invokeinterface`**, and lambdas use **`invokedynamic`**.
13. **Sealed interfaces** (Java 17) restrict who may implement them and enable exhaustive `switch` (Java 21).

### 26.4 Mini Quiz

1. What is an interface in one sentence? Can you create an object of an interface type directly?
2. What keyword does a class use to adopt an interface? What must it do?
3. Why must an implementing method be `public`?
4. What modifiers are implicitly applied to a variable in an interface? To an abstract method?
5. Why can a class implement many interfaces but extend only one class?
6. What is the diamond problem?
7. A class extends `Animal` (with `sound()`) and implements `Loud` (default `sound()`). Which one runs and why?
8. Two unrelated interfaces have the same default method. What must the class do, and how can it call one of them?
9. Why were default methods introduced?
10. Can a class call a static interface method as `MyClass.method()`? Why or why not?
11. What problem do private interface methods solve, and from which Java version?
12. Give three differences between an interface and an abstract class.
13. What is a functional interface? What does `@FunctionalInterface` do?
14. Does `boolean equals(Object)` in an interface count as an abstract method for functional-interface purposes?
15. What is a marker interface? Name three.
16. What flag marks a class file as an interface?
17. Which bytecode instruction is used to call a method through an interface reference?
18. What does `sealed ... permits` do?
19. What does this print? `Object o = new ArrayList<String>(); System.out.println(o instanceof RandomAccess);`
20. When would you prefer an abstract class over an interface?

<details>
<summary>Click for answers</summary>

1. An interface is a **contract of behavior** listing what a class can do without (traditionally) saying how. **No**: you can't instantiate it. You create an object of a class that implements it (or use a lambda/anonymous class).
2. **`implements`**. It must **override all abstract methods** (unless the class is abstract).
3. Interface methods are implicitly `public`, and an override can't have **weaker** access, because anyone holding the interface reference is entitled to call the method.
4. Variable: **`public static final`**. Abstract method: **`public abstract`**.
5. Two parents' classes could bring **conflicting implementations/state** (diamond problem). Interfaces traditionally have **no implementation or state**, so there's nothing to conflict, and Java adds strict rules for default methods.
6. When a class inherits from two parents that both provide the same member (through a shared ancestor), so it's **ambiguous which version** to use.
7. **`Animal.sound()`**. **Class wins** over an interface default.
8. It **must override** the method. Inside, call a specific one with **`InterfaceName.super.method()`**.
9. To **add methods to existing interfaces without breaking** all implementing classes (needed for lambdas/streams on `List`, `Collection`, etc.).
10. **No.** Static interface methods are **not inherited**. Use `InterfaceName.method()`. (This avoids ambiguity when a class implements multiple interfaces.)
11. They let an interface **share code between its default/static methods without exposing the helper** publicly. **Java 9**.
12. Any three: interfaces have **no instance state / constructors**, a class can **implement many but extend one**, interface methods are **public**, interfaces express **can-do** vs **is-a**, interfaces are lambda targets, abstract classes can have **protected/private members and final methods**.
13. An interface with **exactly one abstract method**; it's the target type for lambdas. `@FunctionalInterface` makes the **compiler verify** that rule.
14. **No.** Abstract methods matching a public `Object` method don't count.
15. An **empty interface** that tags a class for the JVM/libraries. Examples: **`Serializable`, `Cloneable`, `RandomAccess`**.
16. **`ACC_INTERFACE`** (0x0200), together with `ACC_ABSTRACT`.
17. **`invokeinterface`**.
18. It **restricts which classes may implement/extend** the interface to a listed set, enabling exhaustive `switch`.
19. **`true`**, because `ArrayList` implements the `RandomAccess` marker interface.
20. When classes are **closely related and share state (fields), constructors or common code** (an "is-a" family), or you need **protected/final** members.

</details>

---

*Practice tip: when you design a program, start by writing the interfaces (the contracts) first. Ask, "What should this part be able to do?" Then write the classes that do it. After a few projects, thinking in contracts will feel natural.*
