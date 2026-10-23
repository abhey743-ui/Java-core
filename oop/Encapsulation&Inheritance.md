# Encapsulation & Inheritance in Java
### Packages · Access Modifiers · Encapsulation · Inheritance · Multiple Inheritance · `super` Keyword

---

## Table of Contents

1. [Java Packages](#1-java-packages)
2. [Access Modifiers (every one, in detail)](#2-access-modifiers-every-one-in-detail)
3. [Encapsulation](#3-encapsulation)
4. [Inheritance (basics)](#4-inheritance-basics)
5. [What is and is not inherited](#5-what-is-and-is-not-inherited)
6. [Types of inheritance](#6-types-of-inheritance)
7. [Why multiple inheritance does not work with classes](#7-why-multiple-inheritance-does-not-work-with-classes)
8. [Constructors in inheritance](#8-constructors-in-inheritance)
9. [The `super` keyword](#9-the-super-keyword)
10. [Method overriding rules](#10-method-overriding-rules)
11. [Field hiding, upcasting, downcasting, `instanceof`](#11-field-hiding-upcasting-downcasting-instanceof)
12. [`final` and inheritance](#12-final-and-inheritance)
13. [Inheritance vs Composition (IS-A vs HAS-A)](#13-inheritance-vs-composition-is-a-vs-has-a)
14. [Interview questions with answers](#14-interview-questions-with-answers)
15. [Cheat sheet and common mistakes](#15-cheat-sheet-and-common-mistakes)

---

## 1. Java Packages

### 1.1 What is a package?

A **package** is a **folder-like namespace** that groups related classes, interfaces and sub-packages together.

Real-life analogy: a big library. Books (classes) are arranged in sections (packages): `Science`, `History`, `Fiction`. Two sections may each have a book titled "Introduction", and there is no confusion because they sit in different sections.

```
com.myapp
 |-- model
 |     |-- Student.java
 |     '-- Teacher.java
 |-- service
 |     '-- StudentService.java
 '-- Main.java
```

### 1.2 Why do we use packages?

| Reason | Explanation |
|---|---|
| **Organization** | Thousands of classes in one folder is chaos. Packages group related code (model, service, utils). |
| **Avoid name conflicts** | `com.a.Date` and `com.b.Date` can coexist. The full name (package + class) is unique. |
| **Access control** | `protected` and default (package-private) access are based on packages (see Section 2). |
| **Reusability / maintenance** | Related code lives together and can be shipped as a JAR. |
| **Encapsulation at module level** | Hide internal classes by keeping them package-private. |

### 1.3 Declaring a package

The `package` statement must be the **very first statement** in the file (only comments can come before it). A file can have **at most one** package statement.

```java
// File: com/myapp/model/Student.java
package com.myapp.model;

public class Student {
    public String name;
}
```

**Rule:** the folder structure must match the package name.

```
project/
 '-- com/
      '-- myapp/
           '-- model/
                '-- Student.java     <- package com.myapp.model;
```

### 1.4 Naming convention

- All **lowercase**.
- Usually the company's **reversed domain name** to guarantee uniqueness: `google.com` becomes `com.google...`, `amazon.in` becomes `in.amazon...`.
- Example: `com.mycompany.project.module`.
- Never start with `java` or `javax` (reserved for the JDK).

### 1.5 Using classes from another package: `import`

```java
package com.myapp;

import com.myapp.model.Student;          // import one class

public class Main {
    public static void main(String[] args) {
        Student s = new Student();
    }
}
```

Three ways to use a class from another package:

```java
// 1. Import a single class
import java.util.ArrayList;

// 2. Import everything in a package (wildcard)
import java.util.*;

// 3. Fully qualified name (no import needed)
java.util.ArrayList<String> list = new java.util.ArrayList<>();
```

**Rules about imports:**

1. `import` goes **after** `package` and **before** class declarations.
2. `java.lang.*` (String, System, Math, Integer, Object...) is **imported automatically**. That is why you never import `String`.
3. `import java.util.*;` imports classes of `java.util` only, **not** its sub-packages. `java.util.concurrent.*` needs its own import.
4. Wildcard import has **no runtime cost**. It only tells the compiler where to look. (Many teams still prefer explicit imports for readability.)
5. Classes in the **same package** need no import.
6. If two packages have the same class name, import one and use the **fully qualified name** for the other:

```java
import java.util.Date;

java.sql.Date sqlDate = new java.sql.Date(0);   // fully qualified
Date utilDate = new Date();
```

7. Importing a package does not give you access to its non-public classes.

### 1.6 Static import (recap)

```java
import static java.lang.Math.*;
System.out.println(sqrt(25));   // no "Math." prefix
```

### 1.7 Compiling and running with packages

```
javac -d out com/myapp/Main.java        # -d puts compiled .class files in matching folders
java -cp out com.myapp.Main              # run using the FULLY QUALIFIED class name
```

### 1.8 Common built-in packages

| Package | Contains |
|---|---|
| `java.lang` | String, Math, System, Object, Integer, Thread (auto-imported) |
| `java.util` | Collections (List, Map, Set), Scanner, Random, Optional |
| `java.io` / `java.nio` | File and stream handling |
| `java.time` | LocalDate, LocalDateTime, Duration |
| `java.sql` | JDBC database classes |
| `java.net` | Networking |

### 1.9 The default package

If you write **no** `package` statement, the class goes into the unnamed **default package**. Fine for tiny practice programs, but:
- Classes in named packages **cannot import** classes from the default package.
- Not used in real projects.

### 1.10 Package-info: what a package does NOT do

Packages do not create "nested access". `com.myapp` and `com.myapp.model` are **different** packages. Package-private access does **not** extend to sub-packages.

---

## 2. Access Modifiers (every one, in detail)

### 2.1 What and why

**Access modifiers** decide **who can see and use** a class, field, method or constructor.

Why do we need them?
- **Security / safety:** stop other code from changing important data directly.
- **Encapsulation:** hide the internal working and expose only a clean interface.
- **Maintainability:** you can change hidden internals freely without breaking other people's code.
- **Clear design:** shows what is the public API and what is private detail.

Java has **four access levels**, but only **three keywords**:

| Level | Keyword |
|---|---|
| Most open | `public` |
| | `protected` |
| | *(nothing)*: **default** / **package-private** |
| Most restricted | `private` |

> "default" is **not a keyword** you write. It simply means "no modifier". Do not confuse it with the `default` keyword of `switch` and interface default methods.

### 2.2 Master visibility table

| Modifier | Same class | Same package | Subclass (different package) | Anywhere else |
|---|---|---|---|---|
| `public` | Yes | Yes | Yes | **Yes** |
| `protected` | Yes | Yes | Yes (through inheritance only, see 2.5) | No |
| default (none) | Yes | Yes | No | No |
| `private` | Yes | No | No | No |

```
 widest   public  ->  protected  ->  default  ->  private   narrowest
```

Picture it as circles:

```
+------------------------------------------------------------------+
| PUBLIC: everyone, everywhere                                     |
|  +------------------------------------------------------------+  |
|  | PROTECTED: same package + subclasses anywhere              |  |
|  |  +------------------------------------------------------+  |  |
|  |  | DEFAULT: same package only                           |  |  |
|  |  |  +------------------------------------------------+  |  |  |
|  |  |  | PRIVATE: same class only                       |  |  |  |
|  |  |  +------------------------------------------------+  |  |  |
|  |  +------------------------------------------------------+  |  |
|  +------------------------------------------------------------+  |
+------------------------------------------------------------------+
```

### 2.3 Where can each modifier be applied?

| Element | `public` | `protected` | default | `private` |
|---|---|---|---|---|
| **Top-level class / interface** | Yes | **No** | Yes | **No** |
| **Nested (inner) class** | Yes | Yes | Yes | Yes |
| **Fields** (instance and static) | Yes | Yes | Yes | Yes |
| **Methods** (instance and static) | Yes | Yes | Yes | Yes |
| **Constructors** | Yes | Yes | Yes | Yes |
| **Local variables** (inside methods/blocks) | **No** | **No** | n/a | **No** |
| **Method parameters** | **No** | **No** | n/a | **No** |
| **Interface methods / fields** | Yes (implicit) | No | No | `private` methods allowed since Java 9 |

Why can't a top-level class be `private` or `protected`?
- A top-level class is not "inside" anything. `private` means "only inside the enclosing class", but there is no enclosing class.
- `protected` means "package + subclasses", which only makes sense for a **member** of a class (a subclass inherits members, not other top-level classes).

Why can't local variables or parameters have access modifiers?
- They exist only inside one method call and are already invisible to everyone else. Their **scope** already limits them. Only `final` is allowed there.

```java
void test(final int x) {          // final OK
    final int y = 5;              // final OK
    // private int z = 1;         // ERROR: illegal start of expression
}
```

### 2.4 `public`

**Meaning:** accessible from **everywhere**: same class, same package, other packages, any class.

```java
package com.shop.model;

public class Product {
    public String name;
    public void display() { System.out.println(name); }
}
```

```java
package com.shop.app;
import com.shop.model.Product;

public class Main {
    public static void main(String[] args) {
        Product p = new Product();   // class must be public to be visible here
        p.name = "Pen";               // field is public
        p.display();                  // method is public
    }
}
```

**Rules and restrictions:**

1. A `.java` file can have **at most one public top-level class**, and the **file name must match that class name** (`Product.java` for `public class Product`).
2. To use a class from another package, the **class itself must be public** (and the member you use must also be public).
3. `public static void main` must be public so the JVM (outside your class) can call it.
4. Interface methods are **implicitly public**. When a class implements them, the implementing method **must be declared `public`** (it cannot weaken access).
5. Making fields `public` breaks encapsulation (anyone can set invalid values), so avoid it for data. Use `public` mainly for the **API** (methods, constants).

```java
public static final double PI = 3.14159;    // public constants are fine
```

### 2.5 `protected`

**Meaning:** accessible within the **same package**, plus in **subclasses even in other packages**, but subclasses in other packages only through **inheritance** (see the rule below).

Why does `protected` exist?
It is for members you want **child classes to use or override**, but you do not want the whole world to touch.

```java
package com.zoo;

public class Animal {
    protected String name;                       // visible to subclasses
    protected void eat() { System.out.println(name + " eats"); }
}
```

```java
package com.zoo.pets;                            // DIFFERENT package
import com.zoo.Animal;

public class Dog extends Animal {
    void test() {
        name = "Tommy";        // OK: inherited protected member
        eat();                 // OK
        this.eat();            // OK
    }
}
```

#### The tricky rule (very popular in interviews)

In a **different package**, `protected` is accessible **only through the subclass's own type** (`this`, `super`, or an object of the subclass type), **not** through a reference of the **parent type**.

```java
package com.zoo.pets;
import com.zoo.Animal;

public class Dog extends Animal {
    void test() {
        Animal a = new Animal();
        a.eat();                // ERROR: eat() has protected access in Animal

        Dog d = new Dog();
        d.eat();                // OK: via Dog reference

        Animal a2 = new Dog();
        a2.eat();               // ERROR: reference type is Animal
    }
}
```

And from an unrelated class in another package:

```java
package com.zoo.visitor;
import com.zoo.Animal;

public class Visitor {
    void test() {
        Animal a = new Animal();
        a.eat();                // ERROR: not a subclass, different package
    }
}
```

Why this rule? Otherwise a subclass could reach into **other** parents' private business (a `Cat` could peek at the protected data of a `Dog` via the shared `Animal` type). Protected is about **my inherited** members, not everyone's.

**Rules and restrictions:**

1. Cannot be applied to **top-level** classes (only members and nested classes).
2. Inside the **same package**, `protected` behaves like default plus "also subclasses".
3. A subclass can **override** a protected method and keep it `protected` or make it `public`, but **not** make it default/private.
4. A protected **constructor** can be called by subclasses via `super(...)` but not with `new` from a different package (unless it is a subclass calling `super`).

### 2.6 Default (package-private)

**Meaning:** when you write **no modifier**, the member is accessible only to classes in the **same package**.

```java
package com.shop.model;

class Helper {                 // package-private CLASS
    int count;                 // package-private field
    void log() { }             // package-private method
}
```

```java
package com.shop.app;
import com.shop.model.Helper;  // ERROR: Helper is not public in com.shop.model
```

**Why use it?** For **internal helper classes** of a package. Other packages shouldn't even know they exist. It is the best way to keep implementation details hidden at package level.

**Rules and restrictions:**

1. Not visible in other packages, **even to subclasses** (key difference from `protected`).
2. Not visible in **sub-packages** either (`com.shop` and `com.shop.model` are different).
3. If you extend a class from another package, its default members are **not inherited** into your class (they are not accessible).
4. A top-level class with no modifier is package-private. A file can have **many** such classes.
5. Default members in an **interface** do not exist: interface members are implicitly `public`.

### 2.7 `private`

**Meaning:** accessible **only inside the same class** (including its nested classes).

```java
class BankAccount {
    private double balance;                 // hidden from the world

    private void audit() { }                // internal helper

    public void deposit(double amt) {
        if (amt > 0) balance += amt;        // OK, inside the class
        audit();
    }
}

BankAccount acc = new BankAccount();
acc.balance = 1_000_000;    // ERROR: balance has private access
acc.audit();                // ERROR
```

**Rules and restrictions:**

1. Cannot be applied to top-level classes.
2. **Not inherited:** a child class does not get access to the parent's private members (they still exist inside the child object in memory, but the child's code cannot touch them).
3. A `private` method **cannot be overridden**. A child can declare a method with the same name, but it is a completely new unrelated method.
4. **Access is per class, not per object.** Two objects of the same class can read each other's private fields:

```java
class Point {
    private int x;
    boolean sameX(Point other) {
        return this.x == other.x;     // OK: other.x is private but same class
    }
}
```

5. An outer class can access the private members of its **nested** classes and vice versa (they share the same top-level class).
6. **Private constructor** is a powerful use:

```java
class Singleton {
    private static final Singleton INSTANCE = new Singleton();
    private Singleton() { }                       // nobody can call new Singleton()
    public static Singleton getInstance() { return INSTANCE; }
}

final class Utils {
    private Utils() { }                           // utility class: no objects allowed
    public static int add(int a, int b) { return a + b; }
}
```

Private constructors are used for the **Singleton pattern**, **utility classes**, and **factory methods**. A class with only private constructors **cannot be extended** by outside classes (the child's `super(...)` call would fail).

### 2.8 Modifiers on constructors

| Constructor modifier | Effect |
|---|---|
| `public` | Anyone can create objects |
| `protected` | Same package + subclasses (via `super`) |
| default | Only same package can create objects |
| `private` | Only the class itself can create objects (Singleton, factory) |

### 2.9 Access modifiers and inheritance (overriding rule)

> **An overriding method cannot have weaker (more restrictive) access than the overridden method.** It can have the same or **wider** access.

```
private < default < protected < public
```

```java
class Parent {
    protected void show() { }
}
class Child extends Parent {
    void show() { }               // ERROR: attempting to assign weaker access (default < protected)
    protected void show() { }     // OK
    public void show() { }        // OK (wider)
}
```

Why? **Substitutability.** If code holds a `Parent` reference and calls `show()` (allowed because it is protected/public in Parent), the call may land in a `Child`. If `Child` had hidden the method, the call would suddenly be illegal at runtime. Java forbids that.

### 2.10 Access modifiers and interfaces

```java
interface Shape {
    double PI = 3.14;            // implicitly public static final
    void draw();                 // implicitly public abstract
}

class Circle implements Shape {
    void draw() { }              // ERROR: weaker access (default) than public
    public void draw() { }       // OK
}
```

### 2.11 Quick "can I access it?" decision flow

```
Is it private?        -> only inside same class
Is it default?        -> same package only
Is it protected?      -> same package OR subclass (via inheritance) in another package
Is it public?         -> everywhere (but the CLASS must be visible too)
```

> Access needs **both**: the class must be visible AND the member must be visible. A `public` method inside a package-private class is still not reachable from another package.

### 2.12 Best practice

- Start with the **most restrictive** (`private`) and open up only when needed.
- Fields: `private` (almost always).
- Methods that form the API: `public`.
- Methods for children: `protected`.
- Helper stuff inside a package: default.

---

## 3. Encapsulation

### 3.1 Definition

**Encapsulation** = **wrapping data (fields) and the methods that work on that data into a single unit (class), and controlling access to the data.**

Simple version: **make fields `private`, give controlled access through `public` methods (getters/setters).**

Real-life analogy: a **medicine capsule**. The powder (data) is sealed inside the capsule (class). You swallow it as a whole; you don't touch the powder directly. Another one: an **ATM**. You cannot reach into the machine and take cash. You use the keypad (methods), and the machine checks your PIN and balance first.

```
        +--------------------------------------+
        |              CLASS                   |
        |  +--------------------------------+  |
 world -|->|  public methods (getters,      |  |
        |  |  setters, behaviour)           |  |
        |  +---------------+----------------+  |
        |                  |  controlled       |
        |  +---------------v----------------+  |
        |  |  private data (fields)         |  |
        |  +--------------------------------+  |
        +--------------------------------------+
        outside code can NOT touch data directly
```

### 3.2 Without encapsulation (the problem)

```java
class BankAccount {
    public double balance;
}

BankAccount acc = new BankAccount();
acc.balance = -50000;          // invalid, nothing stops it
acc.balance = 99999999;        // anyone can change it
```

### 3.3 With encapsulation

```java
class BankAccount {
    private double balance;               // 1. hide the data

    public BankAccount(double initial) {
        if (initial < 0) throw new IllegalArgumentException("Negative balance");
        this.balance = initial;
    }

    public double getBalance() {          // 2. controlled read
        return balance;
    }

    public void deposit(double amount) {  // 3. controlled write with validation
        if (amount <= 0) throw new IllegalArgumentException("Invalid amount");
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0 || amount > balance)
            throw new IllegalArgumentException("Invalid withdrawal");
        balance -= amount;
    }
}
```

Now nobody can set a negative balance. All rules live in **one** place.

### 3.4 Getters and setters

```java
class Student {
    private String name;
    private int age;

    public String getName() { return name; }                 // getter

    public void setName(String name) {                       // setter
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name required");
        this.name = name;
    }

    public int getAge() { return age; }

    public void setAge(int age) {
        if (age < 0 || age > 120)
            throw new IllegalArgumentException("Invalid age");
        this.age = age;
    }
}
```

**Naming convention (JavaBean style):**
- `getFieldName()` and `setFieldName(value)`.
- For `boolean`: `isActive()` and `setActive(boolean)`.

### 3.5 Read-only and write-only fields

| Want | Do this |
|---|---|
| **Read-only** | Provide only a getter (no setter). Example: `id`, `createdDate`. |
| **Write-only** | Provide only a setter (rare, e.g. a password you only set). |
| **Immutable** | Make fields `private final`, set only in the constructor, no setters. |

### 3.6 Benefits of encapsulation

1. **Data protection / validation:** invalid values are blocked.
2. **Flexibility:** you can change internal storage without breaking users. Example: store age as `birthYear` internally, still return `getAge()`.
3. **Maintainability:** logic for a field is in one place.
4. **Reusability and testability:** clear public API.
5. **Control:** make fields read-only, add logging, trigger events on change.
6. **Loose coupling:** other classes depend on the interface, not the internals.

### 3.7 A subtle trap: leaking mutable objects

Making a field `private` is **not enough** if you return a reference to a **mutable** object.

```java
class Team {
    private List<String> members = new ArrayList<>();
    public List<String> getMembers() { return members; }   // leaks the real list!
}

team.getMembers().clear();    // outside code just wiped the private data
```

Fix with a **defensive copy** or an unmodifiable view:

```java
public List<String> getMembers() {
    return Collections.unmodifiableList(members);   // or new ArrayList<>(members)
}
```

Same for arrays and `Date`-like mutable objects (copy them in the constructor/setter and on return).

### 3.8 Encapsulation vs Data hiding vs Abstraction

| Term | Idea |
|---|---|
| **Data hiding** | Hiding the data itself (`private` fields). |
| **Encapsulation** | Bundling data + methods and controlling access. Data hiding is **part** of it. |
| **Abstraction** | Showing only **what** an object does, hiding **how** (interfaces, abstract classes). Focus on design level. |

### 3.9 Tight encapsulation

A class is **tightly encapsulated** when **all** its fields are private (and any parent classes are also tightly encapsulated). One public field breaks it.

---

## 4. Inheritance (basics)

### 4.1 Definition

**Inheritance** lets one class **acquire the fields and methods of another class**, so you can **reuse** code and build "is-a" relationships.

- **Parent / Super / Base class:** the class being inherited from.
- **Child / Sub / Derived class:** the class that inherits.
- Keyword: **`extends`**.

```
        Animal           <- parent / superclass
          ^
          |  extends  (IS-A)
          |
         Dog             <- child / subclass
```

### 4.2 Why do we use inheritance?

1. **Code reuse:** write common code once in the parent.
2. **Extensibility:** add new features by creating child classes without touching old code.
3. **Polymorphism:** a parent reference can hold any child object (`Animal a = new Dog();`), enabling flexible code.
4. **Logical hierarchy:** models real-world "is-a" relations (Dog is an Animal).
5. **Method overriding:** children can customize parent behavior.

### 4.3 Example

```java
class Animal {
    String name;

    void eat() {
        System.out.println(name + " is eating");
    }
}

class Dog extends Animal {
    void bark() {
        System.out.println(name + " is barking");
    }
}

public class Main {
    public static void main(String[] args) {
        Dog d = new Dog();
        d.name = "Tommy";    // inherited field
        d.eat();             // inherited method
        d.bark();            // Dog's own method
    }
}
```

```
Dog object in memory
+--------------------------+
|  [from Animal]  name     |
|  [from Dog]     (own)    |
+--------------------------+
Methods available: eat() (inherited) + bark() (own)
```

A `Dog` **object contains an `Animal` part inside it**. When you create a `Dog`, the `Animal` part is created too (that is why the parent constructor runs first, see Section 8).

### 4.4 The root: `java.lang.Object`

Every class in Java **implicitly extends `Object`** (unless it extends another class, in which case `Object` is the ancestor higher up). So every object has `toString()`, `equals()`, `hashCode()`, `getClass()`, etc.

```
Object
  ^
Animal
  ^
Dog
```

### 4.5 Rules of `extends`

1. A class can extend **only one** class (single inheritance for classes).
2. **Cyclic inheritance** is not allowed (`A extends B` and `B extends A`).
3. A `final` class cannot be extended.
4. You cannot extend a class whose constructors are all `private` (outside that class).
5. A class can **implement many interfaces** (that is how Java gets multiple-inheritance-like behavior).
6. An `interface` can `extends` **multiple interfaces**.

---

## 5. What is and is not inherited

| Member | Inherited by the child? |
|---|---|
| `public` fields/methods | Yes |
| `protected` fields/methods | Yes (accessible in child) |
| default (package-private) | **Only if the child is in the same package** |
| `private` | **No** (exists in the object, but not accessible by name) |
| **Constructors** | **No.** Constructors are never inherited (but the child calls the parent's via `super`). |
| `static` members | Accessible through the child's name, but they belong to the class (hidden, not overridden) |
| Instance/static **blocks** | Not inherited, but they **run** as part of object/class initialization |

Example of the private case:

```java
class Parent {
    private int secret = 42;
    public int getSecret() { return secret; }
}
class Child extends Parent {
    void test() {
        // System.out.println(secret);        // ERROR: private in Parent
        System.out.println(getSecret());      // OK: via public method
    }
}
```

Memory-wise the `secret` **does exist** inside every `Child` object. Child code just cannot name it directly.

---

## 6. Types of inheritance

### 6.1 Single inheritance

One child, one parent.

```
 A
 ^
 |
 B
```

```java
class A { void a() { } }
class B extends A { void b() { } }
```

### 6.2 Multilevel inheritance

A chain: grandparent, parent, child.

```
 A
 ^
 |
 B
 ^
 |
 C
```

```java
class Vehicle { void move() { System.out.println("moving"); } }
class Car extends Vehicle { void fuel() { System.out.println("petrol"); } }
class SportsCar extends Car { void turbo() { System.out.println("turbo on"); } }

SportsCar s = new SportsCar();
s.move();    // from Vehicle
s.fuel();    // from Car
s.turbo();   // own
```

`SportsCar` has everything from `Car` **and** from `Vehicle`.

### 6.3 Hierarchical inheritance

**One parent, many children.**

```
        Animal
       /   |   \
     Dog  Cat  Bird
```

```java
class Animal { void eat() { } }
class Dog extends Animal { }
class Cat extends Animal { }
class Bird extends Animal { }
```

### 6.4 Multiple inheritance (a class with two parents)

```
  A     B
   \   /
     C        class C extends A, B   <-- NOT ALLOWED in Java (for classes)
```

Java **does not support** this with classes. It is supported with **interfaces**. Section 7 explains why in detail.

### 6.5 Hybrid inheritance

A mix of the above (for example hierarchical + multiple).

```
      A
     / \
    B   C
     \ /
      D
```

With **classes** this includes multiple inheritance, so Java does not support it with classes. With **interfaces** it works.

### 6.6 Summary table

| Type | With classes | With interfaces |
|---|---|---|
| Single | Yes | Yes |
| Multilevel | Yes | Yes |
| Hierarchical | Yes | Yes |
| **Multiple** | **No** | **Yes** |
| **Hybrid** | **No** (if it includes multiple) | **Yes** |

---

## 7. Why multiple inheritance does not work with classes

(So yes, your multiple inheritance attempt fails, and it is **by design**, not a mistake of yours.)

### 7.1 What you tried

```java
class A { void show() { System.out.println("A"); } }
class B { void show() { System.out.println("B"); } }

class C extends A, B { }    // COMPILE ERROR: '{' expected
```

### 7.2 The reason: ambiguity (the Diamond Problem)

Suppose Java allowed it:

```java
C obj = new C();
obj.show();       // Which show()? A's or B's?
```

The compiler and JVM cannot decide. This is the root of the problem.

The **Diamond Problem** is the famous version:

```
          Person
          /    \
     Student  Employee
          \    /
         Intern
```

If `Student` and `Employee` both inherit `Person` and both override `work()`, then which `work()` should `Intern` inherit? And how many copies of `Person`'s fields does `Intern` have: one or two?

```
 Person.name
    |          |
 Student    Employee         <- both override work()
    \          /
      Intern  -> inherits work() from ??? 
```

### 7.3 More problems multiple inheritance would cause

1. **Ambiguous methods** (same signature in two parents).
2. **Ambiguous fields** (same field name in two parents).
3. **Constructor chaining confusion:** every constructor must call exactly one `super(...)` first. With two parents, which runs first? What if they both initialize a shared grandparent? Would the grandparent constructor run twice?
4. **Complexity:** deep, tangled hierarchies are hard to read and debug.
5. **Tooling:** harder for compilers, JVM and IDEs.

Java's designers chose **simplicity and safety**. (C++ allows it and needs extra tools like virtual inheritance to handle the diamond.)

### 7.4 How Java achieves the same goal: interfaces

A class can **implement multiple interfaces**.

```java
interface Flyable { void fly(); }
interface Swimmable { void swim(); }

class Duck implements Flyable, Swimmable {
    public void fly()  { System.out.println("Duck flies"); }
    public void swim() { System.out.println("Duck swims"); }
}
```

Why no ambiguity here? Interface methods (traditionally) have **no body**. If two interfaces declare `void show();`, the class writes **one** implementation, and it satisfies both. There is no conflict because there is no inherited code, only a contract.

```
 Flyable    Swimmable
     \        /
       Duck          one class, two contracts, one implementation each
```

### 7.5 Since Java 8: default methods and the new conflict

Interfaces can now have **default methods** (with bodies). So the ambiguity can come back:

```java
interface A { default void show() { System.out.println("A"); } }
interface B { default void show() { System.out.println("B"); } }

class C implements A, B { }    // COMPILE ERROR: C inherits unrelated defaults for show()
```

Java does not guess. **You must resolve it** by overriding, and you can choose which parent to call using `InterfaceName.super.method()`:

```java
class C implements A, B {
    @Override
    public void show() {
        A.super.show();        // choose A's version
        B.super.show();        // or call both, or write your own logic
    }
}
```

Resolution rules for default methods:
1. **Class wins:** a method from a class (or superclass) beats any interface default.
2. **More specific interface wins:** if `B extends A` and both define the default, `B`'s version is used.
3. Otherwise, **you must override** and decide explicitly.

Interfaces also cannot have instance fields (only `public static final` constants) and have no constructors, so the field-ambiguity and constructor-chain problems do not appear.

### 7.6 Multiple inheritance of **type** vs **implementation**

| | Classes | Interfaces |
|---|---|---|
| Inherit **state** (fields)? | One parent only | No instance state |
| Inherit **behavior** (code)? | One parent only | Only through default methods, with forced conflict resolution |
| Multiple parents? | **No** | **Yes** |

### 7.7 Combine class + interfaces

```java
class Animal { void eat() { } }
interface Flyable { void fly(); }
interface Swimmable { void swim(); }

class Duck extends Animal implements Flyable, Swimmable {
    public void fly() { }
    public void swim() { }
}
```

`extends` comes **first**, then `implements`.

### 7.8 Multilevel is NOT multiple

`C extends B`, `B extends A` is **multilevel** and totally allowed. Multiple means one class has **two direct parent classes**.

---

## 8. Constructors in inheritance

### 8.1 Constructors are not inherited, but they ARE chained

When you create a child object, **the parent constructor runs first**, then the child's.

```java
class Parent {
    Parent() { System.out.println("Parent constructor"); }
}
class Child extends Parent {
    Child() { System.out.println("Child constructor"); }
}

new Child();
```

Output:

```
Parent constructor
Child constructor
```

Why? The parent part of the object must be fully built **before** the child part can rely on it.

### 8.2 The hidden `super()` call

If you do not write one, the compiler inserts `super();` as the **first line** of every constructor:

```java
Child() {
    super();                 // added automatically by the compiler
    System.out.println("Child constructor");
}
```

### 8.3 The classic compile error

If the parent has **only a parameterized constructor** (no no-arg constructor), the hidden `super()` finds nothing:

```java
class Parent {
    Parent(int x) { }
}
class Child extends Parent {
    Child() { }       // ERROR: constructor Parent in class Parent cannot be applied to given types
}
```

**Fix:** call the right parent constructor explicitly:

```java
class Child extends Parent {
    Child() {
        super(10);    // explicit
    }
}
```

(Or give the parent a no-arg constructor.)

Remember: if you write **any** constructor in a class, the compiler no longer provides the default no-arg one.

### 8.4 Rules

1. `super(...)` / `this(...)` must be the **first statement** of a constructor. (Very recent Java versions relax this slightly by allowing some statements that do not use `this` before the call, but treat "first statement" as the rule for learning and interviews.)
2. You can use **either** `this(...)` **or** `super(...)` in one constructor, **not both** (both need to be first).
3. You cannot call `super(...)` or `this(...)` from a normal method.
4. A constructor **cannot call itself** recursively with `this(...)`.
5. Constructors cannot be `static`, `final`, `abstract` or inherited.
6. The whole chain always ends at `Object()`.

### 8.5 Full object creation order (with static and instance blocks)

```java
class Parent {
    static { System.out.println("1. Parent static"); }
    { System.out.println("3. Parent instance block"); }
    Parent() { System.out.println("4. Parent constructor"); }
}
class Child extends Parent {
    static { System.out.println("2. Child static"); }
    { System.out.println("5. Child instance block"); }
    Child() { System.out.println("6. Child constructor"); }
}

new Child();
```

Order: **Parent static → Child static → Parent instance block → Parent constructor → Child instance block → Child constructor.**

```
Class loading (once):    Parent static  ->  Child static
Object creation:         Parent {instance block + constructor}
                          then
                         Child  {instance block + constructor}
```

### 8.6 Constructor chaining with `this()`

```java
class Student {
    String name; int age;

    Student() {
        this("Unknown", 0);          // calls the 2-arg constructor
    }
    Student(String name) {
        this(name, 0);
    }
    Student(String name, int age) {  // the "master" constructor
        this.name = name;
        this.age = age;
    }
}
```

Good practice: let one constructor do the real work and others delegate to it.

### 8.7 Pitfall: calling an overridable method from a constructor

```java
class Parent {
    Parent() { show(); }             // calls overridable method
    void show() { System.out.println("Parent show"); }
}
class Child extends Parent {
    int x = 10;
    Child() { super(); }
    @Override void show() { System.out.println("Child show x=" + x); }
}

new Child();    // prints: Child show x=0   (!!)
```

The parent constructor runs **before** the child's fields are initialized, so `x` is still `0`. Avoid calling overridable methods inside constructors.

---

## 9. The `super` keyword

`super` is a **reference to the parent class part** of the current object. It has three uses.

### 9.1 Use 1: `super.variable` (access hidden parent field)

```java
class Parent { String name = "Parent"; }

class Child extends Parent {
    String name = "Child";

    void print() {
        String name = "Local";
        System.out.println(name);          // Local   (local variable)
        System.out.println(this.name);     // Child   (this class's field)
        System.out.println(super.name);    // Parent  (parent's field)
    }
}
```

Lookup order for a plain `name`: local variable, then this class's field, then the parent's field.

### 9.2 Use 2: `super.method()` (call the parent's version)

Used when the child overrides a method but still wants the parent's behavior too.

```java
class Animal {
    void sound() { System.out.println("Some sound"); }
}
class Dog extends Animal {
    @Override
    void sound() {
        super.sound();                    // run Animal's version first
        System.out.println("Bark");       // then add own behavior
    }
}

new Dog().sound();
// Some sound
// Bark
```

Without `super.`, calling `sound()` inside `sound()` would be infinite recursion.

### 9.3 Use 3: `super(...)` (call the parent's constructor)

```java
class Person {
    String name;
    Person(String name) { this.name = name; }
}

class Employee extends Person {
    int salary;
    Employee(String name, int salary) {
        super(name);              // parent initializes name
        this.salary = salary;
    }
}
```

### 9.4 `this` vs `super`

| | `this` | `super` |
|---|---|---|
| Refers to | Current class's object | Parent class part of the object |
| Field access | `this.x` (own field) | `super.x` (parent's field) |
| Method call | `this.m()` | `super.m()` (parent's version) |
| Constructor call | `this(...)` (same class) | `super(...)` (parent) |
| Must be first in constructor? | Yes | Yes |
| Both in one constructor? | No | No |
| In static context? | **No** | **No** |

Both are **object-level** keywords, so they cannot be used inside `static` methods or static blocks.

### 9.5 Important notes

- `super` is **not** a separate object. It is the same object viewed as its parent.
- `super.super.method()` is **not allowed** (you cannot skip two levels).
- `super` cannot reach a **private** parent member.
- In an interface default-method conflict, the special form is `InterfaceName.super.method()`.

---

## 10. Method overriding rules

**Overriding** = child provides its own version of a parent's method (same name, same parameters). It is decided at **runtime** by the **actual object** (runtime polymorphism).

```java
class Animal {
    void sound() { System.out.println("Animal sound"); }
}
class Dog extends Animal {
    @Override
    void sound() { System.out.println("Bark"); }
}
class Cat extends Animal {
    @Override
    void sound() { System.out.println("Meow"); }
}

Animal a1 = new Dog();
Animal a2 = new Cat();
a1.sound();    // Bark
a2.sound();    // Meow
```

### Rules

| Rule | Detail |
|---|---|
| Same **name and parameter list** | Otherwise it is overloading, not overriding |
| **Return type** | Same, or a **subtype** for reference types (covariant return) |
| **Access** | Same or **wider**, never weaker (see 2.9) |
| **Checked exceptions** | Cannot throw **broader or new** checked exceptions than the parent method |
| `final` method | **Cannot** be overridden |
| `static` method | Cannot be overridden (it is **hidden**) |
| `private` method | Not inherited, so not overridden |
| Constructors | Cannot be overridden |
| `@Override` | Optional but **strongly recommended**: the compiler checks you really are overriding |

Overriding vs overloading:

| | Overloading | Overriding |
|---|---|---|
| Where | Same class (or inherited) | Parent and child |
| Signature | **Different** parameters | **Same** parameters |
| Resolved | Compile time | Runtime |
| Return type | Can differ | Same or covariant |

---

## 11. Field hiding, upcasting, downcasting, `instanceof`

### 11.1 Fields are NOT polymorphic

```java
class Parent { String name = "Parent"; }
class Child extends Parent { String name = "Child"; }

Parent p = new Child();
System.out.println(p.name);            // Parent  (decided by REFERENCE type)
System.out.println(((Child) p).name);  // Child
```

Methods are chosen by the **object**, fields by the **reference type**. This is called **field hiding**.

### 11.2 Upcasting (child to parent): automatic and safe

```java
Animal a = new Dog();       // implicit upcast
a.eat();                    // OK (Animal method)
// a.bark();                // ERROR: Animal reference doesn't know bark()
```

The reference type limits **what you can call**; the object type decides **which version runs** (for overridden methods).

### 11.3 Downcasting (parent to child): manual and risky

```java
Animal a = new Dog();
Dog d = (Dog) a;            // OK, the object really is a Dog
d.bark();

Animal a2 = new Animal();
Dog d2 = (Dog) a2;          // ClassCastException at runtime
```

### 11.4 Safe downcasting with `instanceof`

```java
if (a instanceof Dog) {
    Dog d = (Dog) a;
    d.bark();
}

// Java 16+ pattern matching:
if (a instanceof Dog d) {
    d.bark();
}
```

```
        Animal
        /    \
      Dog    Cat

 Animal a = new Dog();   // allowed (upcast)
 Dog    d = (Dog) a;     // allowed, object is a Dog
 Cat    c = (Cat) a;     // compiles, but ClassCastException at runtime
```

---

## 12. `final` and inheritance

| Use | Effect |
|---|---|
| `final class` | Cannot be extended (`String`, `Integer`, `Math`) |
| `final` method | Can be inherited and called, but **not overridden** |
| `final` field | Cannot be reassigned (child can't change it either) |

```java
final class Vehicle { }
class Car extends Vehicle { }        // ERROR

class Bank { final void verify() { } }
class MyBank extends Bank {
    void verify() { }                // ERROR
}
```

Also related: `abstract` classes **must** be extended (the opposite of `final`), so `abstract final` together is illegal.

---

## 13. Inheritance vs Composition (IS-A vs HAS-A)

| | Inheritance (IS-A) | Composition (HAS-A) |
|---|---|---|
| Relationship | Dog **is an** Animal | Car **has an** Engine |
| Keyword | `extends` | A field holding another object |
| Coupling | Tight (child depends on parent internals) | Loose |
| Flexibility | Fixed at compile time | Can swap parts at runtime |

```java
class Engine { void start() { System.out.println("Engine started"); } }

class Car {
    private Engine engine = new Engine();     // HAS-A

    void drive() {
        engine.start();
        System.out.println("Car moving");
    }
}
```

**Rule of thumb:** use inheritance only for a **true IS-A** relationship where the child can be used anywhere the parent is expected. Otherwise prefer composition ("favor composition over inheritance"). Do not extend a class just to reuse a few methods (a `Stack` that extends `Vector` is a well-known design mistake in the JDK).

---

## 14. Interview questions with answers

**Q1. What is a package and why do we use it?**
A namespace that groups related classes. It organizes code, avoids name conflicts and supports access control (protected/default).

**Q2. Is `java.lang` imported automatically?**
Yes. That is why `String`, `System`, `Math` need no import.

**Q3. Does `import java.util.*` import `java.util.concurrent`?**
No. Wildcard imports do not include sub-packages.

**Q4. Name the access modifiers in order of visibility.**
`public` > `protected` > default (package-private) > `private`.

**Q5. Can a top-level class be `private` or `protected`?**
No. Only `public` or default. Nested classes can use all four.

**Q6. Difference between `protected` and default?**
Both allow same-package access. `protected` additionally allows subclasses in other packages (through inheritance).

**Q7. Can access modifiers be used on local variables?**
No. Locals already have limited scope. Only `final` is allowed.

**Q8. Can a child class reduce the access of an overridden method?**
No. It can keep the same access or widen it (substitutability).

**Q9. What is encapsulation? How do you achieve it?**
Wrapping data and methods in a class and restricting direct access: private fields with public getters/setters (with validation).

**Q10. Is making a field private enough for encapsulation?**
Not always. If a getter returns a reference to a mutable object (list, array, date), outsiders can modify it. Return a copy or an unmodifiable view.

**Q11. Why does Java not support multiple inheritance with classes?**
To avoid ambiguity such as the Diamond Problem (which parent's method or field to inherit), plus constructor chaining complexity.

**Q12. How does Java achieve multiple inheritance then?**
By implementing multiple interfaces. Conflicts between default methods must be resolved explicitly with `InterfaceName.super.method()`.

**Q13. Are constructors inherited?**
No, but the parent constructor is always called through `super(...)`, explicit or implicit.

**Q14. What if the parent has no no-arg constructor?**
The child's constructors must call `super(args)` explicitly, otherwise a compile error occurs.

**Q15. In what order are constructors executed?**
Parent first, then child (all the way from `Object` down).

**Q16. Difference between `this` and `super`?**
`this` refers to the current class's members; `super` refers to the parent's. Both can call constructors (`this(...)`, `super(...)`) and must be first in the constructor, and only one of them can be used.

**Q17. Can we use `super` in a static method?**
No. `super` (like `this`) needs an object.

**Q18. Can private members be inherited?**
They exist inside the child object but are not accessible from the child's code. Access them through public/protected methods.

**Q19. Can we override a `static` or `private` method?**
No. Static methods are hidden; private methods are not visible to the child.

**Q20. What is the output type of `Animal a = new Dog(); a.name` when both define `name`?**
Animal's `name`. Fields are resolved by the reference type, methods by the actual object.

**Q21. What is the root class of all classes?**
`java.lang.Object`.

**Q22. Inheritance or composition?**
Use inheritance for a true IS-A relationship. Otherwise use composition (HAS-A) for flexibility and lower coupling.

---

## 15. Cheat sheet and common mistakes

### Access modifier cheat sheet

```
private    -> same class only                       (hide data)
default    -> same package                           (package helpers)
protected  -> same package + subclasses (via inheritance)   (for children)
public     -> everywhere                             (the API)

Top-level class: public or default only
Locals/params : no access modifiers (only final)
Override      : access can stay or widen, never shrink
```

### Inheritance cheat sheet

```
class B extends A              single inheritance (classes)
class C extends B              multilevel
class D extends A; class E extends A   hierarchical
class F extends A implements I1, I2    class + multiple interfaces  (the "multiple inheritance" way)
class G extends A, B           NOT ALLOWED
```

### Object creation flow

```
new Child()
   -> Object()  ->  Parent()  ->  Child()
   (each level: instance initializers, then constructor body)
```

### `super` cheat sheet

```
super.field      -> parent's field
super.method()   -> parent's version of an overridden method
super(args)      -> parent's constructor (first line of constructor)
```

### Common mistakes

| Mistake | Fix |
|---|---|
| Making fields `public` | Make them `private`, expose with getters/setters |
| Writing `extends A, B` | Only one class; use interfaces for the second |
| Parent has only a parameterized constructor, child has none matching | Call `super(args)` explicitly in the child's constructor |
| Weakening access while overriding | Keep same or wider access |
| Assuming `protected` works via any parent reference in another package | It works only via the subclass type |
| Expecting default members to be visible in sub-packages | Packages are flat; sub-packages are different packages |
| Class in a package but folder structure doesn't match | Folder path must equal package name |
| Running `java Main` for a class in a package | Use the fully qualified name: `java com.myapp.Main` |
| Returning internal list/array directly from a getter | Return a copy or unmodifiable view |
| Calling overridable methods in a constructor | Avoid; child fields aren't initialized yet |
| Using inheritance just to reuse code | Prefer composition unless it's a real IS-A |
| Using `this` / `super` in a static method | Not allowed; they need an object |
| Public top-level class name different from file name | Rename file to match the public class |
| Confusing overloading with overriding | Overloading = different params, compile time. Overriding = same params, runtime |

### One-line takeaways

1. **Package** = folder-like namespace; `package` first, then `import`; `java.lang` is automatic.
2. **Access:** `private` < default < `protected` < `public`; top-level classes can only be public or default.
3. **Encapsulation** = private data + public controlled access (validate, protect, stay flexible).
4. **Inheritance** = `extends`, IS-A, code reuse, polymorphism; constructors not inherited but always chained (parent first).
5. **Multiple inheritance** with classes is banned (ambiguity / diamond); use **interfaces** instead.
6. **`super`** = parent's field, method or constructor; **`this`** = current class's.
7. Override: same signature, same or wider access, never override `final`, `static`, `private`.

---
*Practice tip: build three packages (`model`, `service`, `app`), put a class with all four access levels in `model`, and try to access each member from `service` (same project, different package) and from a subclass. Seeing which lines fail to compile is the fastest way to remember the table.*
