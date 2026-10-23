# Java Nested Classes: The Complete Deep Dive

> A class written **inside another class**. Java has **four kinds**: **static nested classes**, **inner classes**, **local classes** and **anonymous classes**.
> This guide follows the outline of the video you shared (why nest, static nested, inner, local, anonymous) and expands every topic with diagrams, rules, error messages and runnable examples.
> The video summary you pasted was cut off at "largely being replaced by L...". I'm assuming that means **lambdas**, and section 8 covers it in detail.

---

## Table of Contents

1. [What Are Nested Classes?](#1-what-are-nested-classes)
2. [Why Nest Classes? (Benefits and When Not To)](#2-why-nest-classes-benefits-and-when-not-to)
3. [The Four Kinds at a Glance](#3-the-four-kinds-at-a-glance)
4. [Static Nested Classes](#4-static-nested-classes)
5. [Inner Classes (Non-Static Member Classes)](#5-inner-classes-non-static-member-classes)
6. [Local Classes](#6-local-classes)
7. [Anonymous Classes](#7-anonymous-classes)
8. [Anonymous Classes vs Lambdas](#8-anonymous-classes-vs-lambdas)
9. [Access Rules Summary](#9-access-rules-summary)
10. [Access Modifiers and Private Sharing](#10-access-modifiers-and-private-sharing)
11. [Memory and Compiled Output](#11-memory-and-compiled-output)
12. [Practical Programs](#12-practical-programs)
13. [Best Practices: Which Kind Should I Use?](#13-best-practices-which-kind-should-i-use)
14. [Common Mistakes](#14-common-mistakes)
15. [Cheat Sheet, Summary and Quiz](#15-cheat-sheet-summary-and-quiz)

---

## 1. What Are Nested Classes?

Normally every class sits in its own file at the top level. Java also lets you **write a class inside another class**.

```java
public class Outer {              // the OUTER (enclosing) class

    class Inner {                 // a class nested inside it
        void hello() {
            System.out.println("Hello from the nested class");
        }
    }
}
```

| Term | Meaning |
|------|---------|
| **Outer class** (enclosing class) | The class that **contains** the other class |
| **Nested class** | A class declared **inside** another class |
| **Top-level class** | A class that is **not** nested inside another class |

### A real-life picture

A **Car** has an **Engine**. The engine is only meaningful as *part of a car*. Instead of making `Engine` a fully independent class that anyone can use, you can nest it inside `Car`, so the code shows that they belong together.

```
   ┌──────────────────── class Car ─────────────────────┐
   │   fields: brand, speed ...                          │
   │   methods: drive(), stop() ...                      │
   │                                                     │
   │   ┌──────────── class Engine ───────────────┐       │
   │   │  (only makes sense as part of a Car)    │       │
   │   └─────────────────────────────────────────┘       │
   └─────────────────────────────────────────────────────┘
```

### The terminology tree

```
 Nested classes (any class inside another class)
 │
 ├── 1. Static nested class        →  declared with the `static` keyword
 │
 └── Inner classes (NOT static)
        ├── 2. Member inner class  →  a plain non-static class inside the outer class
        ├── 3. Local class         →  declared inside a method / block
        └── 4. Anonymous class     →  has no name, declared and created in one expression
```

> **Terminology note:** many books and the video say "nested class" for the static kind and "inner class" for the non-static kind. Oracle's tutorial calls both "nested classes", where **inner classes are the non-static ones**. Local and anonymous classes are special cases of inner classes. This guide uses the same wording as the video: **static nested** and **inner**.

---

## 2. Why Nest Classes? (Benefits and When Not To)

### 2.1 Benefits

| # | Benefit | Meaning |
|---|---------|---------|
| 1 | **Logical grouping** | If a class is useful **only to one other class**, put it right there. The relationship is obvious. (This is the main reason, as the video says.) |
| 2 | **Encapsulation** | The nested class can be `private`, so the **outside world doesn't even know it exists**. And a nested class can use the outer class's `private` members. |
| 3 | **More readable code** | The helper class sits **close to where it is used**, so you don't jump between files. |
| 4 | **Less clutter** | Fewer tiny top-level classes and files in your package. |
| 5 | **Namespacing** | `Computer.Builder` reads clearly and avoids name clashes with other `Builder` classes. |

### 2.2 Examples from the Java library you already use

| Nested class | Where |
|--------------|-------|
| `Map.Entry` | Nested **interface** inside `Map` |
| `ArrayList`'s iterator (`Itr`) | A private **inner class** inside `ArrayList` |
| `Node` of a `LinkedList` | A private **static nested class** |
| `Thread.State` | Nested **enum** inside `Thread` |
| `AbstractMap.SimpleEntry` | Static nested class |
| `new Comparator<>() { ... }` | **Anonymous class** (very common before lambdas) |

### 2.3 When NOT to nest

- If the class is **useful on its own** or used by many unrelated classes, make it a normal **top-level** class.
- If the nested class becomes **large** (hundreds of lines), move it to its own file. Deeply nested code is hard to read.
- Don't nest just to "save a file".

---

## 3. The Four Kinds at a Glance

```java
public class Outer {

    static class StaticNested { }              // 1) static nested class

    class Inner { }                            // 2) inner class (member)

    void method() {
        class Local { }                        // 3) local class (inside a method)

        Object o = new Object() { };           // 4) anonymous class (no name)
    }
}
```

| | **Static nested** | **Inner (member)** | **Local** | **Anonymous** |
|---|-------------------|--------------------|-----------|---------------|
| **Where declared** | Class body | Class body | Inside a block (method, constructor, loop, `if`) | Inside an expression |
| **`static` keyword?** | **Yes** | No | No (not allowed) | No |
| **Has a name?** | Yes | Yes | Yes | **No** |
| **Needs an outer object?** | **No** | **Yes** | Yes (if in an instance context) | Yes (if in an instance context) |
| **Can use outer *instance* members?** | **No** (only via an object reference) | **Yes** | Yes | Yes |
| **Can use outer *static* members?** | Yes | Yes | Yes | Yes |
| **Can use local variables?** | n/a | n/a | Yes (**effectively final** only) | Yes (**effectively final** only) |
| **Access modifiers** (`public`, `private`...) | Allowed | Allowed | **Not allowed** | Not allowed |
| **Can have constructors?** | Yes | Yes | Yes | **No** |
| **Scope** | Wherever the modifier allows | Wherever the modifier allows | **Only inside its block** | **Only at that one expression** |
| **How many instances?** | Any number | Any number | Any number (inside the block) | Exactly **one** (where it's written) |
| **Compiled file name** | `Outer$Nested.class` | `Outer$Inner.class` | `Outer$1Local.class` | `Outer$1.class` |
| **Typical use** | Builder, helper, `Node` | Iterators, parts tied to one outer object | One-off helper inside a method | One-time override or implementation |

---

## 4. Static Nested Classes

### 4.1 What is it?

A **static nested class** is a class declared with the **`static`** keyword inside another class. It behaves like a **normal top-level class** that is just **scoped inside** the outer class (for naming and access).

> **The key idea:** it belongs to the outer **class**, not to any outer **object**. So you can create it **without creating an outer object**.

### 4.2 Syntax

```java
public class Outer {

    private static int staticValue = 100;
    private int instanceValue = 5;

    static class StaticNested {               // note the `static` keyword
        void show() {
            System.out.println("staticValue = " + staticValue);   // OK: outer's static member
        }
    }
}
```

### 4.3 Creating an instance

**From outside the outer class:** use `Outer.StaticNested`.

```java
Outer.StaticNested n = new Outer.StaticNested();    // NO Outer object needed
n.show();                                           // staticValue = 100
```

**From inside the outer class:** the simple name works.

```java
public class Outer {
    static class StaticNested { }

    void test() {
        StaticNested n = new StaticNested();        // fine
    }
}
```

**Import it:**

```java
import com.example.Outer.StaticNested;
...
StaticNested n = new StaticNested();
```

### 4.4 What it can and can't access

| Outer member | Accessible directly? |
|--------------|:--------------------:|
| `static` fields and methods (even `private` ones) | **Yes** |
| **Non-static** (instance) fields and methods | **No** (compile error) |

```java
public class Outer {
    private int instanceValue = 5;
    private static int staticValue = 100;

    static class StaticNested {
        void test() {
            System.out.println(staticValue);          // OK
            System.out.println(instanceValue);        // ERROR: non-static variable instanceValue
                                                      //        cannot be referenced from a static context
        }
    }
}
```

**Why?** A static nested object has **no hidden link to any `Outer` object**. There is no "which Outer's `instanceValue`?" to follow.

**How to reach instance members anyway:** give it an `Outer` object, then use the dot (even `private` fields are allowed, because both are in the same outer class):

```java
static class StaticNested {
    void show(Outer o) {
        System.out.println(o.instanceValue);          // OK: via an explicit object reference
    }
}
```

### 4.5 What a static nested class CAN do

A static nested class is a **full-featured class**:

| Feature | Allowed? |
|---------|:--------:|
| Instance fields and methods | ✓ |
| `static` fields and methods | ✓ |
| Constructors (several, overloaded) | ✓ |
| `extends` another class | ✓ |
| `implements` interfaces | ✓ |
| Be `abstract` or `final` | ✓ |
| Have its own nested classes | ✓ |
| Use `public`, `protected`, `private`, or package-private | ✓ |

```java
public class Outer {

    static class Animal {
        protected String name;
        Animal(String name) { this.name = name; }
        void speak() { System.out.println(name + " makes a sound"); }
    }

    static class Dog extends Animal {                // a nested class extending another nested class
        static int dogCount = 0;                     // its own static member

        Dog(String name) {                           // its own constructor
            super(name);
            dogCount++;
        }

        @Override
        void speak() { System.out.println(name + " says Woof"); }
    }
}
```

```java
Outer.Dog d = new Outer.Dog("Rex");
d.speak();                                // Rex says Woof
System.out.println(Outer.Dog.dogCount);   // 1
```

### 4.6 `private` static nested class: the perfect helper

If you mark a static nested class **`private`**, nothing outside the outer class can even see it. It becomes an **internal implementation detail**.

```java
public class Library {

    private static class Book {                   // hidden from the world
        final String title;
        final int year;
        Book(String title, int year) { this.title = title; this.year = year; }
    }

    private final java.util.List<Book> books = new java.util.ArrayList<>();

    public void add(String title, int year) {
        books.add(new Book(title, year));
    }

    public void printAll() {
        for (Book b : books) {
            System.out.println(b.title + " (" + b.year + ")");
        }
    }
}
```

```java
Library lib = new Library();
lib.add("Clean Code", 2008);
lib.printAll();                                   // Clean Code (2008)

Library.Book b = new Library.Book("X", 1);        // ERROR: Library.Book has private access in Library
```

Users of `Library` can't depend on `Book`, so you can **change `Book` freely later** without breaking anyone. This is **encapsulation**.

### 4.7 Use case 1: the Builder pattern

Static nested classes are the classic home of the **Builder pattern** (the video mentions this). Why static? Because you must be able to create the **builder first**, **before** the object it builds exists.

```java
public class Computer {
    private final String cpu;
    private final int ramGb;
    private final int storageGb;
    private final boolean gpu;

    private Computer(Builder b) {                       // private: only the Builder can create one
        this.cpu = b.cpu;
        this.ramGb = b.ramGb;
        this.storageGb = b.storageGb;
        this.gpu = b.gpu;
    }

    public static class Builder {                       // static nested class
        private final String cpu;                       // required
        private int ramGb = 8;                          // optional, with defaults
        private int storageGb = 256;
        private boolean gpu = false;

        public Builder(String cpu)            { this.cpu = cpu; }
        public Builder ramGb(int ramGb)       { this.ramGb = ramGb;         return this; }
        public Builder storageGb(int gb)      { this.storageGb = gb;        return this; }
        public Builder gpu(boolean gpu)       { this.gpu = gpu;             return this; }
        public Computer build()               { return new Computer(this); }
    }

    @Override
    public String toString() {
        return "Computer[cpu=" + cpu + ", ram=" + ramGb + "GB, storage=" + storageGb + "GB, gpu=" + gpu + "]";
    }

    public static void main(String[] args) {
        Computer pc = new Computer.Builder("Ryzen 7")
                .ramGb(32)
                .gpu(true)
                .build();
        System.out.println(pc);
    }
}
```

Output:
```
Computer[cpu=Ryzen 7, ram=32GB, storage=256GB, gpu=true]
```

Notes: `Builder` reads `Computer`'s **private constructor** and `Computer` reads `Builder`'s **private fields**, which is allowed because they are in the same outer class (section 10).

### 4.8 Use case 2: helper/data holder classes like `Node`

```java
public class MyList {

    private static class Node {                // does not need MyList's instance data → static
        int value;
        Node next;
        Node(int value) { this.value = value; }
    }

    private Node head;

    public void add(int value) {
        Node n = new Node(value);
        if (head == null) {
            head = n;
        } else {
            Node cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = n;
        }
    }

    public void print() {
        StringBuilder sb = new StringBuilder("[");
        for (Node cur = head; cur != null; cur = cur.next) {
            sb.append(cur.value);
            if (cur.next != null) sb.append(", ");
        }
        System.out.println(sb.append("]"));
    }

    public static void main(String[] args) {
        MyList list = new MyList();
        list.add(10);
        list.add(20);
        list.add(30);
        list.print();                           // [10, 20, 30]
    }
}
```

`Node` is `static` because a node doesn't need to know which `MyList` it belongs to. Making it non-static would waste memory on a hidden outer reference in **every node** (section 5.9).

### 4.9 Nested interfaces, enums and records are implicitly static

```java
public class Shop {

    interface Discount { double apply(double price); }       // implicitly static

    enum Category { FOOD, TOYS, BOOKS }                      // implicitly static

    record Item(String name, double price) { }               // implicitly static (Java 16+)
}

Shop.Item item = new Shop.Item("Pen", 1.5);
Shop.Category c = Shop.Category.BOOKS;
```

A class nested inside an **interface** is implicitly `public static`.

### 4.10 Memory picture

```
          HEAP
 ┌──────────────────────┐        ┌──────────────────────┐
 │ Outer object (maybe) │        │ StaticNested object  │
 │  instanceValue = 5   │        │  its own fields      │
 └──────────────────────┘        └──────────────────────┘
        NO link between them: the static nested object is independent
```

It exists **on its own**. Creating it doesn't need, create or keep alive an `Outer` object.

### 4.11 Summary of static nested classes

- Declared with `static`, created with `new Outer.Nested()` (**no outer object**).
- Sees outer **static** members only (even private). Instance members need an object reference.
- Can have everything a normal class has; can be `private` to hide it.
- Best for: **Builders**, **helper classes**, **data nodes**, anything logically grouped but independent of an outer instance.

---

## 5. Inner Classes (Non-Static Member Classes)

### 5.1 What is it?

An **inner class** is a **non-static** class declared in the body of another class. Each inner object is **tied to one specific outer object**.

> **The key idea:** an inner object **always lives inside an outer object**. It holds a **hidden reference** to that outer object, so it can use all of the outer object's data.

### 5.2 Syntax

```java
public class Outer {

    private int value = 42;
    private static int counter = 7;

    class Inner {                                  // NO `static` keyword
        void show() {
            System.out.println(value);             // outer's instance field: OK
            System.out.println(counter);           // outer's static field: OK
        }
    }
}
```

### 5.3 Creating an instance: you need an outer object

**From outside:** create the outer object first, then use **`outerObject.new Inner()`**.

```java
Outer outer = new Outer();
Outer.Inner inner = outer.new Inner();         // syntax: outerReference.new InnerName()
inner.show();                                  // 42  then  7
```

One line (works, but harder to read):

```java
Outer.Inner inner = new Outer().new Inner();
```

**From inside an instance method of the outer class:** just use `new Inner()` (Java uses `this.new Inner()` for you).

```java
public class Outer {
    class Inner { }

    void createInner() {
        Inner i = new Inner();                 // same as this.new Inner()
    }
}
```

### 5.4 The three common errors

```java
Outer.Inner i1 = new Outer.Inner();
// ERROR: an enclosing instance that contains Outer.Inner is required
```
You tried to build an inner object **without an outer object**.

```java
public class Outer {
    class Inner { }

    public static void main(String[] args) {
        Inner i = new Inner();
        // ERROR: non-static variable this cannot be referenced from a static context
    }
}
```
`main` is `static`, so there is **no `this`** (no outer object). Create one first:

```java
public static void main(String[] args) {
    Outer o = new Outer();
    Inner i = o.new Inner();         // OK
}
```

```java
Outer outer = new Outer();
Outer.Inner i = outer.new Inner();   // ✓
```

### 5.5 Access: an inner class can use EVERYTHING in the outer class

| Outer member | Accessible from inner? |
|--------------|:----------------------:|
| `public` / `protected` / package members | ✓ |
| **`private`** members | ✓ |
| **Instance** fields and methods | ✓ (through the hidden outer reference) |
| **Static** fields and methods | ✓ |

```java
public class Counter {
    private int count = 0;

    class Incrementer {
        void increment() {
            count++;                                  // changes the OUTER object's private field
        }
    }

    int getCount() { return count; }

    public static void main(String[] args) {
        Counter c = new Counter();
        Counter.Incrementer inc = c.new Incrementer();
        inc.increment();
        inc.increment();
        System.out.println(c.getCount());             // 2
    }
}
```

### 5.6 Which outer object? Each inner object is bound to ONE outer object

```java
Counter c1 = new Counter();
Counter c2 = new Counter();

Counter.Incrementer a = c1.new Incrementer();     // bound to c1
Counter.Incrementer b = c2.new Incrementer();     // bound to c2

a.increment(); a.increment(); b.increment();

System.out.println(c1.getCount());                // 2
System.out.println(c2.getCount());                // 1
```

```
   c1 ──► [ Counter: count=2 ] ◄── (hidden link) ── a
   c2 ──► [ Counter: count=1 ] ◄── (hidden link) ── b
```

### 5.7 `this`, `Outer.this` and shadowing

If the inner class has a variable with the **same name** as an outer one, the inner one **hides (shadows)** it. Reach the right one with:

- `this.x`: the **inner** object's field
- `Outer.this.x`: the **outer** object's field

```java
public class ShadowTest {
    public int x = 0;                                        // outer field

    class FirstLevel {
        public int x = 1;                                    // inner field (shadows the outer one)

        void methodInFirstLevel(int x) {                     // parameter (shadows both)
            System.out.println("x = " + x);                              // the parameter
            System.out.println("this.x = " + this.x);                    // the inner field
            System.out.println("ShadowTest.this.x = " + ShadowTest.this.x); // the outer field
        }
    }

    public static void main(String... args) {
        ShadowTest st = new ShadowTest();
        ShadowTest.FirstLevel fl = st.new FirstLevel();
        fl.methodInFirstLevel(23);
    }
}
```

Output:
```
x = 23
this.x = 1
ShadowTest.this.x = 0
```

```
 Which x?   plain  x        → closest scope: the PARAMETER (23)
            this.x          → the field of the INNER object (1)
            ShadowTest.this.x → the field of the OUTER object (0)
```

`Outer.this` is **the hidden reference** to the enclosing object. You can pass it around too:

```java
class Inner {
    void register() {
        Registry.add(Outer.this);          // hands the outer object to someone else
    }
}
```

### 5.8 Static members in inner classes: the old restriction

The video notes a **historical restriction**:

| Java version | Static members inside an inner class |
|--------------|--------------------------------------|
| **Java 15 and older** | **Not allowed**, except **constants** (`static final` primitives/Strings fixed at compile time). The reasoning: an inner class is tied to an instance, so "static" (one per class) made no sense. |
| **Java 16 and newer** | **Allowed.** The restriction was lifted (when records were added). |

```java
public class Outer {
    class Inner {
        static final int MAX = 10;        // always allowed (a constant)
        static int counter = 0;           // ERROR in Java 15 and older; OK in Java 16+
        static void helper() { }          // ERROR in Java 15 and older; OK in Java 16+
    }
}
```

If your project runs on an older Java, keep this in mind.

### 5.9 Memory picture: the hidden outer reference

When Java compiles an inner class, it **secretly adds a field** (named something like `this$0`) that points to the outer object, and the inner constructor secretly receives the outer object as an extra argument.

```
                HEAP
  ┌───────────────────────────┐          ┌──────────────────────────────┐
  │ Outer object              │ ◄─────── │ Inner object                 │
  │   value = 42              │  hidden  │   this$0  ●  (hidden field)  │
  │   ...                     │  link    │   its own fields             │
  └───────────────────────────┘          └──────────────────────────────┘

  STACK
  outer ──────► (Outer object)
  inner ──────► (Inner object)
```

**Consequences:**

1. **Extra memory:** every inner object carries one more reference.
2. **Keeps the outer object alive.** As long as the inner object is reachable, the garbage collector **can't free the outer object**, even if you no longer use it. This is a classic **memory-leak** source:

   ```java
   // A tiny inner object stored in a long-lived list keeps a HUGE outer object in memory
   longLivedList.add(bigOuter.new Inner());
   ```
3. **If you don't need the outer object, use a static nested class instead.** (This is why the Java library uses a `static` `Node` in `LinkedList`.)

### 5.10 Inner class inside inner class

```java
public class A {
    class B {
        class C {
            void hi() { System.out.println("deep"); }
        }
    }
}

A a = new A();
A.B b = a.new B();
A.B.C c = b.new C();
c.hi();                                // deep
```

Deep nesting gets confusing fast. Avoid going beyond two levels.

### 5.11 Inner classes can extend and implement

```java
public class Outer {
    interface Greeter { void greet(); }

    class Friendly implements Greeter {
        @Override public void greet() { System.out.println("Hello!"); }
    }
}
```

### 5.12 Use cases for inner classes

| Use | Why an inner class fits |
|-----|-------------------------|
| **Iterators** (e.g. inside `ArrayList`) | The iterator needs the list's private data and belongs to **one specific list** |
| **Parts of an object** (`Car` → `Engine`) | The part lives and dies with one whole |
| **Event handlers / listeners** | Needs to call methods of the object that owns it |
| **Records of actions** (e.g. a `Transaction` of one `BankAccount`) | Naturally bound to the outer object |

### 5.13 Summary of inner classes

- Non-static; **created through an outer object**: `outer.new Inner()`.
- Has a **hidden reference** to that outer object, so it can use **all** outer members (even private).
- Use `Outer.this` to name the outer object explicitly (especially to resolve shadowing).
- Static members: not allowed (except constants) **before Java 16**, allowed **from Java 16**.
- Costs a hidden reference and can keep the outer object alive. **If you don't need the outer instance, make it `static`.**

---

## 6. Local Classes

### 6.1 What is it?

A **local class** is a class **declared inside a block of code**: a method, a constructor, an `if`, a loop, or an initializer block. It exists **only inside that block**, like a local variable.

### 6.2 Syntax

```java
public class Demo {

    void process() {
        class Helper {                                // local class: declared inside a method
            void work() {
                System.out.println("Working...");
            }
        }

        Helper h = new Helper();                      // use it only inside this method
        h.work();
    }
}
```

Outside `process()`, the name `Helper` **doesn't exist**:

```java
Helper h = new Helper();     // ERROR: cannot find symbol (outside the method)
```

### 6.3 Where can local classes be declared?

```java
void example(boolean flag) {

    class A { }                          // directly in a method body

    if (flag) {
        class B { }                      // inside an if-block: visible only in this block
        new B();
    }

    for (int i = 0; i < 3; i++) {
        class C { }                      // inside a loop body: visible only in this body
        new C();
    }
}
```

Also allowed in constructors, instance initializer blocks and static initializer blocks.

### 6.4 Rules for local classes

| Rule | Detail |
|------|--------|
| **Scope** | Visible **only from its declaration to the end of the block** |
| **Access modifiers** | **Not allowed** (`public`, `private`, `protected`, `static`). It is local, so it has no access level |
| **`final` / `abstract`** | Allowed |
| **Constructors, fields, methods** | All allowed |
| **`extends` / `implements`** | Allowed |
| **Outer instance members** | Accessible **if** the block is in an instance method/constructor |
| **In a `static` method** | **No** access to the outer's instance members (there is no outer object), only static ones |
| **Local variables of the method** | Accessible only if **effectively final** (section 6.5) |
| **Several instances** | Create as many as you like inside the block |

```java
public class Outer {
    private int field = 1;
    private static int sfield = 2;

    void instanceMethod() {
        class L { void show() { System.out.println(field + " " + sfield); } }   // both OK
        new L().show();
    }

    static void staticMethod() {
        class L { void show() { System.out.println(sfield); } }                 // only static OK
        // System.out.println(field);                                           // ERROR here
        new L().show();
    }
}
```

### 6.5 The "effectively final" rule

A local class (and an anonymous class, and a lambda) may use the **local variables and parameters of the enclosing method**, but only if they are **final or effectively final**.

| Term | Meaning |
|------|---------|
| **`final`** | Declared with the `final` keyword |
| **Effectively final** | Not declared `final`, but **never changed after its first assignment** |

```java
void ok(String name) {
    int base = 10;                              // assigned once and never changed → effectively final
    class Greeter {
        void hello() { System.out.println(name + " " + base); }   // ✓ allowed
    }
    new Greeter().hello();
}
```

```java
void bad() {
    int count = 0;

    class Printer {
        void show() { System.out.println(count); }   // ERROR
    }
    // error: local variables referenced from an inner class must be final or effectively final

    count++;                                          // this change makes `count` NOT effectively final
}
```

**Just one change anywhere in the method breaks the rule**, even *after* the class:

```java
int x = 5;
class L { int get() { return x; } }     // ERROR because of the next line
x = 6;
```

### 6.6 WHY does this rule exist?

The video explains it as avoiding ambiguity when the method finishes. Here is the full picture.

1. Local variables live on the **stack**, in the method's frame. When the method **returns**, the frame is **destroyed** and the variables vanish.
2. But a local class object lives on the **heap** and **can outlive the method** (you can return it or store it).
3. So the local class object **cannot point to the method's variable** (it will be gone). Instead, Java **copies the variable's value** into hidden fields of the local class object.
4. If the original variable could **change** later, the **copy** and the **original** would disagree, and nobody would know which one the code means.
5. The rule **guarantees the value never changes**, so the copy is always correct and there is **no ambiguity**.

```
   METHOD FRAME (stack)                    HEAP
  ┌─────────────────────┐              ┌───────────────────────────┐
  │ count = 5           │  ── copy ──► │ Printer object            │
  │ (will vanish when   │              │   val$count = 5 (a copy)  │
  │  the method ends)   │              │   (still alive later)     │
  └─────────────────────┘              └───────────────────────────┘
       If `count` could change to 6, the copy (5) and the original (6)
       would DISAGREE. So Java forbids changing it.
```

### 6.7 Workarounds when you need a changing value

**1) Use a one-element array (the array reference is effectively final; its contents can change):**

```java
void run() {
    int[] counter = {0};
    class Inc { void go() { counter[0]++; } }
    Inc inc = new Inc();
    inc.go();
    inc.go();
    System.out.println(counter[0]);        // 2
}
```

**2) Use `AtomicInteger` (or another mutable holder):**

```java
java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger();
class Inc { void go() { count.incrementAndGet(); } }
```

**3) Use a field of the outer class** (fields aren't subject to the rule), or give the local class its **own field**.

**4) Copy it into a new effectively final variable:**

```java
int count = 0;
count++;
final int snapshot = count;                // a new variable that never changes
class Printer { void show() { System.out.println(snapshot); } }
```

### 6.8 Example: a local class that outlives the method

```java
static Runnable makePrinter(String label, int times) {

    class Printer implements Runnable {            // local class
        @Override
        public void run() {
            for (int i = 1; i <= times; i++) {
                System.out.println(label + " " + i);
            }
        }
    }

    return new Printer();                          // returned as its supertype, Runnable
}

public static void main(String[] args) {
    Runnable r = makePrinter("Round", 3);          // makePrinter has finished and its frame is gone...
    r.run();                                       // ...but the object still remembers label and times
}
```

Output:
```
Round 1
Round 2
Round 3
```

The object kept **its own copies** of `label` and `times`, exactly as section 6.6 explained. Also notice you return it as `Runnable`, because the name `Printer` isn't visible outside the method.

### 6.9 Local records, enums and interfaces (Java 16+)

Since Java 16 you can also declare **local records, enums and interfaces**. They are implicitly **static**, so they **cannot capture** local variables or outer instance members.

```java
static void report(java.util.List<String> names) {
    record NameLength(String name, int length) { }           // a local record

    for (String n : names) {
        NameLength nl = new NameLength(n, n.length());
        System.out.println(nl);
    }
}
```

### 6.10 Local class vs a normal class

| Use a local class when... | Use a normal/nested class when... |
|---------------------------|-----------------------------------|
| The helper is needed **only inside one method** | It is used in several methods |
| It needs that method's **parameters/local variables** | It doesn't need them |
| You need a **named** type with several methods/state | A lambda or a simple expression would do |

Local classes are **rare in practice** (lambdas and private methods cover most needs), but you should understand them: the same capturing rules apply to anonymous classes and lambdas.

### 6.11 Summary of local classes

- Declared **inside a block**, visible **only in that block**; no access modifiers; no `static`.
- Can use outer members and the method's variables, **only if those variables are effectively final**, because Java **copies** them and must make sure the copy never goes out of date.
- The object **can outlive the method**.

---

## 7. Anonymous Classes

### 7.1 What is it?

An **anonymous class** is a class **with no name**, declared and instantiated **in a single expression**. You use it to **extend a class or implement an interface for one specific, one-time use**.

### 7.2 Syntax

```
new  SuperTypeOrInterface( constructorArguments )  {
        // class body: fields, methods, @Override methods
}
```

You write `new Something()` and, **instead of a semicolon**, you open a `{ }` body right away.

### 7.3 Form 1: implementing an interface

```java
interface Greeter {
    void greet(String name);
}

Greeter g = new Greeter() {                       // "new Greeter" + a body = anonymous class
    @Override
    public void greet(String name) {
        System.out.println("Hello, " + name);
    }
};

g.greet("Ali");                                   // Hello, Ali
```

Read it as: *"Create an object of a new, unnamed class that implements `Greeter` like this."*

Note the **semicolon after the closing brace**: the whole thing is an **expression** (assignment statement).

### 7.4 Form 2: extending an abstract or normal class

```java
abstract class Greeting {
    abstract String text();
    void print() { System.out.println(text()); }
}

Greeting g = new Greeting() {
    @Override
    String text() { return "Hello, anonymous!"; }
};
g.print();                                        // Hello, anonymous!
```

You can even use the object immediately without storing it:

```java
new Greeting() {
    @Override String text() { return "One-shot!"; }
}.print();                                        // One-shot!
```

**Overriding a normal (concrete) class for a single object:**

```java
class Printer {
    void print() { System.out.println("Normal printing"); }
}

Printer special = new Printer() {
    @Override
    void print() { System.out.println("Special printing"); }     // only THIS object behaves differently
};

new Printer().print();          // Normal printing
special.print();                // Special printing
```

### 7.5 Passing arguments to the superclass constructor

An anonymous class **can't declare a constructor** (no name to give it!), but the arguments you write in `new Parent(args)` are **passed to the parent's constructor**.

```java
abstract class Animal {
    protected final String name;
    Animal(String name) { this.name = name; }
    abstract void sound();
}

Animal cat = new Animal("Tom") {                  // "Tom" goes to Animal(String)
    @Override
    void sound() { System.out.println(name + " says Meow"); }
};
cat.sound();                                      // Tom says Meow
```

(For an **interface** there is no constructor, so you write empty parentheses: `new Greeter() { ... }`.)

### 7.6 Rules and limitations

| Rule | Detail |
|------|--------|
| **No name** | You can't refer to its type by name, and can't create a second instance of the same anonymous class |
| **No constructors** | It has no name to give a constructor. Use **superclass arguments** or an **instance initializer block** `{ ... }` |
| **One supertype only** | It either **extends one class** OR **implements one interface**, never both, and never several interfaces |
| **Exactly one instance** | Declared and created at the same time |
| **Can have** | Fields, extra methods, instance initializer blocks, overridden methods |
| **Can access** | Outer members (if in an instance context) and **effectively final** local variables (section 6.5) |
| **Access modifiers/`static`** | Not allowed on the class itself |
| **Compiled name** | `Outer$1.class`, `Outer$2.class`, ... |

### 7.7 Fields and state in an anonymous class

```java
Runnable counter = new Runnable() {
    private int count = 0;                         // its own state

    @Override
    public void run() {
        count++;
        System.out.println("Run #" + count);
    }
};

counter.run();      // Run #1
counter.run();      // Run #2
```

### 7.8 Instance initializer block as the "constructor substitute"

```java
Animal dog = new Animal("Rex") {
    private final int legs;

    {                                              // instance initializer block: runs at creation
        legs = 4;
        System.out.println("Anonymous Animal created with " + legs + " legs");
    }

    @Override
    void sound() { System.out.println(name + " says Woof"); }
};
```

### 7.9 Extra methods can't be called through the supertype

```java
Greeter g = new Greeter() {
    @Override public void greet(String n) { System.out.println("Hi " + n); }
    public void extra() { System.out.println("extra"); }
};

g.extra();                 // ERROR: cannot find symbol (g's type is Greeter, which has no extra())
```

(A `var` declaration keeps the anonymous type, so this works with Java 10+:)

```java
var obj = new Object() { int x = 5; void hi() { System.out.println("hi"); } };
System.out.println(obj.x);     // 5
obj.hi();                      // hi
```

### 7.10 Capturing variables and the outer object

```java
public class Demo {
    private String owner = "Demo object";

    void run(int times) {                          // `times` is effectively final
        String label = "Task";                     // effectively final

        Runnable r = new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= times; i++) {
                    System.out.println(label + " " + i + " (owner: " + owner + ")");
                }
            }
        };
        r.run();
    }
}
```

```java
new Demo().run(2);
```
Output:
```
Task 1 (owner: Demo object)
Task 2 (owner: Demo object)
```

The anonymous class uses **`times` and `label`** (copied, so effectively final) and **`owner`** (through the hidden outer reference).

### 7.11 Typical uses (mostly before Java 8)

```java
// 1) Sorting with a Comparator
Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String a, String b) { return a.length() - b.length(); }
});

// 2) Starting a thread
new Thread(new Runnable() {
    @Override
    public void run() { System.out.println("Running in a thread"); }
}).start();

// 3) Event listeners (GUI code)
button.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) { System.out.println("Clicked!"); }
});

// 4) Overriding one method of an object for a single use (section 7.4)
```

### 7.12 Drawbacks of anonymous classes

1. **Verbose:** a lot of boilerplate for a few lines of logic.
2. **Hard to read** when long or nested.
3. **Can't be reused** (no name).
4. **Can't be unit-tested separately.**
5. Hold a **hidden reference to the outer object** when created in an instance context (potential memory leak).
6. Each one produces its **own `.class` file**.

### 7.13 The "double-brace initialization" trick (avoid it)

```java
List<String> list = new ArrayList<String>() {{
    add("a");
    add("b");
}};
```

The **outer** `{` creates an anonymous **subclass of `ArrayList`**; the **inner** `{` is an instance initializer block. It looks neat but creates an extra class, an extra hidden reference, and breaks `equals`/serialization expectations. Prefer `List.of("a", "b")` or `new ArrayList<>(List.of("a", "b"))`.

---

## 8. Anonymous Classes vs Lambdas

As the video says, anonymous classes are **now largely replaced by lambdas** (Java 8+).

### 8.1 The conversion

A **lambda** is a short way to write an anonymous class **when the interface has exactly one abstract method** (a *functional interface*).

**Anonymous class (old):**
```java
Comparator<String> byLength = new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.length() - b.length();
    }
};
```

**Lambda (new):**
```java
Comparator<String> byLength = (a, b) -> a.length() - b.length();
```

**Even shorter with a library helper / method reference:**
```java
Comparator<String> byLength = Comparator.comparingInt(String::length);
```

**Runnable:**
```java
// anonymous class
Runnable r1 = new Runnable() {
    @Override public void run() { System.out.println("Hi"); }
};

// lambda
Runnable r2 = () -> System.out.println("Hi");
```

### 8.2 Side-by-side differences

| | **Anonymous class** | **Lambda** |
|---|---------------------|------------|
| Works with | **Any** interface, abstract class or normal class | **Only** interfaces with **exactly one abstract method** |
| Can have **state** (fields) | **Yes** | No |
| Can have **several methods** | **Yes** | No |
| Can **extend a class** | **Yes** | No |
| Meaning of **`this`** | The **anonymous object itself** | The **enclosing** (outer) object |
| Shadowing local variables | Can declare its own variables that shadow outer ones | **Can't** redeclare a local variable of the enclosing method |
| Creates a separate `.class` file | **Yes** (`Outer$1.class`) | No (uses `invokedynamic`) |
| Verbosity | High | Low |
| Captured variables | Effectively final | Effectively final |

### 8.3 The `this` difference (a favorite interview question)

```java
public class Demo {
    String name = "Demo object";

    void test() {
        Runnable anon = new Runnable() {
            String name = "anonymous object";
            @Override
            public void run() {
                System.out.println(this.name);          // `this` = the anonymous object
            }
        };

        Runnable lambda = () -> System.out.println(this.name);   // `this` = the Demo object

        anon.run();         // anonymous object
        lambda.run();       // Demo object
    }

    public static void main(String[] args) {
        new Demo().test();
    }
}
```

### 8.4 When you STILL need an anonymous class

1. The interface has **more than one abstract method**:

   ```java
   interface Greeter { void greet(String n); void bye(String n); }

   Greeter g = new Greeter() {                // a lambda can't implement two methods
       public void greet(String n) { System.out.println("Hi " + n); }
       public void bye(String n)   { System.out.println("Bye " + n); }
   };
   ```
2. You need to **extend an abstract/normal class** (section 7.4, 7.5).
3. You need **fields / state** inside the object (section 7.7).
4. You need an **instance initializer** or want a **distinct `this`**.

### 8.5 Rule of thumb

> **Functional interface (one abstract method) → use a lambda.**
> **Otherwise → anonymous class (or a named class, if it's big or reused).**

---

## 9. Access Rules Summary

### 9.1 What can each kind access?

| | Outer **static** members | Outer **instance** members | **Local variables** of the enclosing method |
|---|:---:|:---:|:---:|
| **Static nested** | ✓ | ✗ (only through an object reference) | n/a |
| **Inner (member)** | ✓ | ✓ | n/a |
| **Local** (in an instance method) | ✓ | ✓ | ✓ if effectively final |
| **Local** (in a static method) | ✓ | ✗ | ✓ if effectively final |
| **Anonymous** (in an instance context) | ✓ | ✓ | ✓ if effectively final |
| **Anonymous** (in a static context) | ✓ | ✗ | ✓ if effectively final |

### 9.2 What does the OUTER class see inside the nested class?

The outer class can access **all members of its nested classes, including `private` ones** (through a reference to a nested object):

```java
public class Outer {

    static class Nested {
        private int secret = 99;
    }

    void peek() {
        Nested n = new Nested();
        System.out.println(n.secret);          // OK: private, but we're in the same outer class
    }
}
```

### 9.3 Creating each kind (quick syntax table)

| Kind | Creating one |
|------|--------------|
| **Static nested** | `Outer.Nested n = new Outer.Nested();` |
| **Inner** | `Outer o = new Outer();`  then  `Outer.Inner i = o.new Inner();` |
| **Local** | `new LocalName();` (inside the block where it's declared) |
| **Anonymous** | `new SuperType(args) { ... }` |

---

## 10. Access Modifiers and Private Sharing

### 10.1 Top-level vs nested class modifiers

| | Allowed access modifiers |
|---|--------------------------|
| **Top-level class** | `public` or package-private only |
| **Member nested class** (static nested or inner) | `public`, `protected`, **`private`**, or package-private |
| **Local class / anonymous class** | **None** |

A **`private`** nested class is only visible inside the outer class. A **`protected`** nested class is visible in the package and to subclasses.

```java
public class Outer {
    private static class Secret { }
    public static class Open { }
}

new Outer.Open();       // ✓ from anywhere
new Outer.Secret();     // ✗ error: Outer.Secret has private access in Outer
```

### 10.2 Outer and nested classes share their `private` members

Everything inside the same **top-level class body** (the outer class and all of its nested classes) can use each other's `private` members. This is intentional: they are considered one logical unit.

```java
public class Bank {
    private int vault = 1000;                         // private in Bank

    class Teller {
        private String name = "Sam";                  // private in Teller

        void check() {
            System.out.println("Vault: " + vault);    // Teller reads Bank's private field ✓
        }
    }

    void meet() {
        Teller t = new Teller();
        System.out.println(t.name);                   // Bank reads Teller's private field ✓
    }
}
```

> **Behind the scenes:** older compilers generated tiny synthetic "accessor" methods to make this work. Since **Java 11**, the JVM supports *nestmates* so nested classes can access each other's private members directly.

### 10.3 Nested interfaces, enums, records (reminder)

| Declared inside | Implicitly |
|-----------------|-----------|
| A class → `interface`, `enum`, `record` | `static` |
| An interface → any member class | `public static` |

---

## 11. Memory and Compiled Output

### 11.1 Compiled class files

Each class, nested or not, becomes **its own `.class` file**, using `$` to show nesting.

```java
public class Outer {
    static class StaticNested { }
    class Inner { }

    void method() {
        class Local { }
        Object o = new Object() { };
    }
}
```

```
javac Outer.java
```

Produces:

```
Outer.class
Outer$StaticNested.class
Outer$Inner.class
Outer$1Local.class
Outer$1.class
```

| File | Kind |
|------|------|
| `Outer$StaticNested.class` | Static nested class |
| `Outer$Inner.class` | Inner class |
| `Outer$1Local.class` | Local class (`1` = the first local class named `Local`) |
| `Outer$1.class` | Anonymous class (numbered in order of appearance) |

You can inspect the hidden members with `javap -p 'Outer$Inner'`: you'll see the synthetic field `this$0` for inner classes, and `val$name` fields for captured local variables.

### 11.2 What each kind carries in the heap

```
 STATIC NESTED object        INNER object                 LOCAL / ANONYMOUS object
 ┌─────────────────┐     ┌────────────────────┐        ┌──────────────────────────┐
 │ own fields      │     │ this$0 ──► Outer   │        │ this$0 ──► Outer (if in  │
 │                 │     │ own fields         │        │            an instance   │
 │ (no outer link) │     └────────────────────┘        │            context)      │
 └─────────────────┘                                   │ val$x = copy of local x  │
                                                       │ own fields               │
                                                       └──────────────────────────┘
```

| Kind | Hidden outer reference? | Captured variable copies? |
|------|:----------------------:|:-------------------------:|
| Static nested | **No** | No |
| Inner | **Yes** | No |
| Local | Yes (instance context) | **Yes** |
| Anonymous | Yes (instance context) | **Yes** |

### 11.3 Why this matters

- **Static nested** objects are the **lightest** and can't cause an outer-object leak.
- **Inner, local and anonymous** objects can **keep the outer object alive** as long as they are alive.
- Captured variable copies explain the **effectively final** rule.

---

## 12. Practical Programs

### 12.1 All four kinds in one program

**File: `Outer.java`**

```java
public class Outer {
    private int instanceValue = 10;
    private static int staticValue = 20;

    // 1) static nested class
    static class StaticNested {
        void show() {
            System.out.println("StaticNested sees staticValue = " + staticValue);
        }
    }

    // 2) inner class
    class Inner {
        void show() {
            System.out.println("Inner sees instanceValue = " + instanceValue
                    + ", staticValue = " + staticValue);
        }
    }

    void demo(int param) {
        int local = 5;

        // 3) local class
        class Local {
            void show() {
                System.out.println("Local sees instanceValue = " + instanceValue
                        + ", local = " + local + ", param = " + param);
            }
        }
        new Local().show();

        // 4) anonymous class
        Runnable anon = new Runnable() {
            @Override
            public void run() {
                System.out.println("Anonymous sees instanceValue = " + instanceValue
                        + ", local = " + local);
            }
        };
        anon.run();
    }

    public static void main(String[] args) {
        new Outer.StaticNested().show();         // no outer object needed

        Outer o = new Outer();
        Outer.Inner in = o.new Inner();          // outer object needed
        in.show();

        o.demo(7);
    }
}
```

Output:
```
StaticNested sees staticValue = 20
Inner sees instanceValue = 10, staticValue = 20
Local sees instanceValue = 10, local = 5, param = 7
Anonymous sees instanceValue = 10, local = 5
```

### 12.2 `BankAccount` with an inner `Transaction` class

An inner class is perfect here: each `Transaction` belongs to **one specific account** and reads that account's `owner` and `balance`.

**File: `BankAccount.java`**

```java
import java.util.ArrayList;
import java.util.List;

public class BankAccount {
    private final String owner;
    private double balance;
    private final List<Transaction> history = new ArrayList<>();

    public BankAccount(String owner) {
        this.owner = owner;
    }

    // inner class: every Transaction belongs to ONE BankAccount
    class Transaction {
        private final String type;
        private final double amount;
        private final double balanceAfter;

        Transaction(String type, double amount) {
            this.type = type;
            this.amount = amount;
            this.balanceAfter = balance;           // reads the OUTER object's field
        }

        @Override
        public String toString() {
            return owner + " | " + type + " " + amount + " | balance after: " + balanceAfter;
        }
    }

    public void deposit(double amount) {
        balance += amount;
        history.add(new Transaction("DEPOSIT", amount));        // new Inner() inside an instance method
    }

    public void withdraw(double amount) {
        balance -= amount;
        history.add(new Transaction("WITHDRAW", amount));
    }

    public void printHistory() {
        for (Transaction t : history) {
            System.out.println(t);
        }
    }

    public static void main(String[] args) {
        BankAccount acc = new BankAccount("Ali");
        acc.deposit(100);
        acc.withdraw(30);
        acc.deposit(50);
        acc.printHistory();

        // creating an inner object from outside: outerObject.new Inner(...)
        BankAccount.Transaction manual = acc.new Transaction("MANUAL", 5);
        System.out.println(manual);
    }
}
```

Output:
```
Ali | DEPOSIT 100.0 | balance after: 100.0
Ali | WITHDRAW 30.0 | balance after: 70.0
Ali | DEPOSIT 50.0 | balance after: 120.0
Ali | MANUAL 5.0 | balance after: 120.0
```

### 12.3 Anonymous classes, lambdas and method references together

**File: `SortDemo.java`**

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortDemo {

    abstract static class Greeting {
        abstract String text();
        void print() { System.out.println(text()); }
    }

    public static void main(String[] args) {
        List<String> names = new ArrayList<>(List.of("banana", "fig", "apple", "kiwi"));

        // 1) anonymous class
        Collections.sort(names, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.length() - b.length();
            }
        });
        System.out.println("Anonymous class : " + names);

        // 2) lambda (reverse order so we can see a change)
        names.sort((a, b) -> b.length() - a.length());
        System.out.println("Lambda          : " + names);

        // 3) method reference with a library comparator
        names.sort(Comparator.comparingInt(String::length));
        System.out.println("Method reference: " + names);

        // 4) anonymous class extending an abstract class (a lambda can't do this)
        new Greeting() {
            @Override
            String text() { return "Hello from an anonymous subclass!"; }
        }.print();

        // 5) anonymous class with its own state (a lambda can't have fields)
        Runnable counter = new Runnable() {
            private int count = 0;
            @Override
            public void run() {
                count++;
                System.out.println("Run #" + count);
            }
        };
        counter.run();
        counter.run();
    }
}
```

Output:
```
Anonymous class : [fig, kiwi, apple, banana]
Lambda          : [banana, apple, kiwi, fig]
Method reference: [fig, kiwi, apple, banana]
Hello from an anonymous subclass!
Run #1
Run #2
```

### 12.4 Effectively final in action

**File: `CaptureDemo.java`**

```java
import java.util.ArrayList;
import java.util.List;

public class CaptureDemo {

    static List<Runnable> makeTasks() {
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            final int id = i;                          // `i` changes, so copy it into an effectively final variable
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Task " + id);
                }
            });
        }
        return tasks;                                  // the loop and the method are finished...
    }

    public static void main(String[] args) {
        for (Runnable r : makeTasks()) {
            r.run();                                   // ...but each task still remembers its own id
        }

        // a counter that CHANGES: use a one-element array
        int[] total = {0};
        Runnable adder = new Runnable() {
            @Override public void run() { total[0] += 10; }
        };
        adder.run();
        adder.run();
        System.out.println("Total = " + total[0]);
    }
}
```

Output:
```
Task 1
Task 2
Task 3
Total = 20
```

(Using `i` directly inside the anonymous class would **not compile**, because `i++` changes it. The copy `id` is a new variable in every loop pass and never changes.)

### 12.5 Try it yourself

1. Make a `School` class with a private **static nested** class `Subject` (name, credits) and print a list of subjects.
2. Make a `Playlist` class with an **inner** class `Song` that prints `Playlist name: song title`. Create songs with `playlist.new Song(...)`.
3. Write a method with a **local class** that uses a parameter, then return an object of it as an interface type.
4. Replace every anonymous `Comparator` in your old code with a lambda or `Comparator.comparing(...)`.
5. Write an interface with **two** abstract methods and implement it with an anonymous class. Then try a lambda and read the compiler error.
6. Predict (then check) what happens: change the `ShadowTest` field names and use `this.x` and `ShadowTest.this.x` in different places.

---

## 13. Best Practices: Which Kind Should I Use?

### 13.1 Decision guide

```
 Do you need a class used ONLY by one other class (or one method)?
   │ No → make it a normal top-level class
   │ Yes
   ▼
 Is it needed in just ONE place, once, to implement an interface/override a method?
   │ Yes → Is the interface a FUNCTIONAL interface (one abstract method)?
   │         ├─ Yes → use a LAMBDA (or method reference)
   │         └─ No  → use an ANONYMOUS class
   │ No
   ▼
 Is it needed only INSIDE ONE METHOD and uses that method's variables?
   │ Yes → LOCAL class (rare; often a lambda or private method is simpler)
   │ No
   ▼
 Does each nested object need a link to ONE specific outer object (uses outer's instance data)?
   │ Yes → INNER class
   │ No  → STATIC NESTED class   ← the default choice
```

### 13.2 Practical rules

1. **Default to `static` nested.** Only drop `static` when the nested class truly needs the outer **instance**. (Avoids the hidden reference and memory leaks.)
2. **Make helper nested classes `private`** to hide implementation details.
3. **Use a lambda instead of an anonymous class** whenever the target is a functional interface.
4. **Keep nested classes short.** Long ones belong in their own file.
5. **Don't nest more than two levels.**
6. **Name them well:** `Builder`, `Node`, `Entry`, `Iterator`: names that make sense next to the outer name (`Computer.Builder`).
7. **Beware of memory leaks:** don't store inner/anonymous objects in long-lived structures unless you want to keep the outer object alive too.
8. **Don't use double-brace initialization.**
9. **Use `@Override`** in anonymous classes.
10. For the **Builder pattern**: static nested `Builder`, **private** constructor on the outer class, methods that `return this`, and a `build()` method.

---

## 14. Common Mistakes

| # | Mistake | Error or result | Fix |
|---|---------|-----------------|-----|
| 1 | `new Outer.Inner()` for an inner (non-static) class | "an enclosing instance that contains Outer.Inner is required" | `outer.new Inner()` |
| 2 | `new Inner()` inside `static main` | "non-static variable this cannot be referenced from a static context" | Create an `Outer` object first |
| 3 | Static nested class using an outer **instance** field | "non-static variable ... cannot be referenced from a static context" | Pass an outer object, or make the field static |
| 4 | Modifying a local variable used by a local/anonymous class | "local variables referenced from an inner class must be final or effectively final" | Copy to a final variable, or use an array/`AtomicInteger` |
| 5 | Using a local class outside its block | "cannot find symbol" | Declare it where it's needed, or move it out |
| 6 | Writing `private class Local` inside a method | Illegal modifier | Remove the modifier (local classes have none) |
| 7 | Trying to write a **constructor** in an anonymous class | Not possible | Pass args to the superclass constructor or use an initializer block |
| 8 | Anonymous class implementing **two** interfaces | Not possible | Use a named class |
| 9 | Using a lambda for an interface with **2+ abstract methods** | "not a functional interface" | Use an anonymous class |
| 10 | Calling an extra method of an anonymous class through its supertype variable | "cannot find symbol" | Add it to the supertype, or use `var` |
| 11 | Mixing up `this` and `Outer.this` | Wrong field used (shadowing) | `this.x` for inner, `Outer.this.x` for outer |
| 12 | Using a **non-static inner class** just because it's the default | Wasted memory; possible outer-object leak | Add `static` if you don't need the outer instance |
| 13 | Accessing a `private` nested class from another class | "has private access" | Make it package-private/public, or give a public method |
| 14 | Static member in an inner class on **Java 15 or older** | Compile error | Upgrade, or move the member to the outer class |
| 15 | Forgetting the **semicolon** after an anonymous class expression | Compile error | `Runnable r = new Runnable() { ... };` |
| 16 | Expecting `this` in a lambda to mean the lambda | It means the **enclosing** object | Use an anonymous class if you need its own `this` |
| 17 | Deeply nesting classes | Hard to read | Flatten; use top-level classes |
| 18 | Capturing the loop variable `i` of a `for` loop directly | Not effectively final | `final int id = i;` inside the loop |

---

## 15. Cheat Sheet, Summary and Quiz

### 15.1 Syntax cheat sheet

```java
public class Outer {

    // 1) STATIC NESTED CLASS
    static class StaticNested { }
    //   create:  Outer.StaticNested s = new Outer.StaticNested();

    // 2) INNER CLASS
    class Inner { }
    //   create:  Outer o = new Outer();
    //            Outer.Inner i = o.new Inner();
    //   outer reference inside Inner:  Outer.this

    void method(int param) {

        // 3) LOCAL CLASS  (only inside this method; no access modifiers)
        class Local { }
        Local l = new Local();

        // 4) ANONYMOUS CLASS  (no name; one use)
        Runnable r1 = new Runnable() {
            @Override public void run() { }
        };

        // lambda replacement for a functional interface
        Runnable r2 = () -> { };
    }
}
```

### 15.2 Quick comparison

| | Static nested | Inner | Local | Anonymous |
|---|---|---|---|---|
| Keyword | `static class` | `class` | `class` in a method | `new X() { }` |
| Outer object needed | No | **Yes** | If instance context | If instance context |
| Sees outer instance data | Via object only | **Yes** | Yes | Yes |
| Sees local variables | n/a | n/a | Effectively final | Effectively final |
| Name | Yes | Yes | Yes | **No** |
| Constructor | Yes | Yes | Yes | **No** |
| Best for | Builder, Node, helpers | Iterators, parts of an object | One-off helper in a method | One-off override/implementation |

### 15.3 Summary in simple points

1. A **nested class** is a class declared inside another class. The reason is **logical grouping**: keep a class next to the only class that uses it.
2. There are **four kinds**: **static nested**, **inner (member)**, **local** and **anonymous**.
3. A **static nested class** belongs to the **outer class**, not an object. Create it with `new Outer.Nested()`. It can't use outer **instance** members directly. It can be `private` (a hidden helper) and is the home of the **Builder pattern**.
4. An **inner class** is tied to **one outer object** via a hidden reference. Create it with **`outer.new Inner()`**. It can use **all** outer members, even private ones, and `Outer.this` names the outer object. Static members in inner classes were **not allowed before Java 16**.
5. A **local class** is declared **inside a block** and exists only there. It can use the method's variables only if they're **effectively final**, because Java **copies** them and must prevent the copy from becoming out of date.
6. An **anonymous class** has **no name**, is created with `new Type() { ... }`, **can't have constructors**, extends **one** class **or** implements **one** interface, and is for **one-off** use.
7. **Lambdas** have largely **replaced anonymous classes** for functional interfaces. They are shorter, and `this` means the enclosing object. Anonymous classes are still needed for **multiple methods, state, or extending classes**.
8. Inner, local and anonymous objects can **keep the outer object alive**. **Prefer `static` nested** when you don't need the outer instance.
9. Nested classes and their outer class **share access to each other's `private` members**.
10. Each nested class compiles to its own file: `Outer$Nested.class`, `Outer$1Local.class`, `Outer$1.class`.

### 15.4 Mini Quiz

1. Name the four kinds of nested classes.
2. How do you create an instance of a **static** nested class `Nested` of `Outer` from another class?
3. How do you create an instance of an **inner** class `Inner` of `Outer`?
4. Can a static nested class directly use the outer class's non-static fields? Why or why not?
5. What does `Outer.this` mean?
6. What does "effectively final" mean, and why does Java require it for local and anonymous classes?
7. Can an anonymous class have a constructor? How can you still pass values to its superclass?
8. Were static members allowed in inner classes before Java 16?
9. What does this print?
   ```java
   public class T {
       int x = 0;
       class In {
           int x = 1;
           void f(int x) {
               System.out.println(x + " " + this.x + " " + T.this.x);
           }
       }
       public static void main(String[] a) { new T().new In().f(23); }
   }
   ```
10. Which kind of nested class does **not** hold a hidden reference to the outer object, and why does that matter?
11. When can you replace an anonymous class with a lambda, and when can't you?
12. Can a local class be declared `private` or `static`?
13. What is different about `this` inside a lambda vs inside an anonymous class?
14. What file names does the compiler generate for a static nested class `StaticNested`, an inner class `Inner`, the first local class `Local` in a method, and the first anonymous class?

<details>
<summary>Click for answers</summary>

1. **Static nested**, **inner (member)**, **local**, **anonymous**.
2. `Outer.Nested n = new Outer.Nested();` (no outer object needed).
3. `Outer o = new Outer();` then `Outer.Inner i = o.new Inner();`.
4. **No.** A static nested object has no hidden reference to any outer object, so Java can't know **which** outer object's field you mean. You can use them only through an explicit outer object reference.
5. The **hidden reference to the enclosing outer object** of an inner class. Used to access outer members, especially when names are shadowed (`Outer.this.x`).
6. **Effectively final** means the variable is never changed after its first assignment (even without the `final` keyword). Java copies the variable's value into the class object, because the object may outlive the method. If the original could change, the copy and the original would disagree, so Java forbids it.
7. **No**, because it has no name. You pass arguments in `new Parent(args) { ... }` and they go to the **superclass constructor**. For setup code, use an **instance initializer block**.
8. **No** (except `static final` constants). **From Java 16**, static members are allowed in inner classes.
9. **`23 1 0`**: the parameter, the inner field, then the outer field.
10. A **static nested class**. It matters because inner objects can **keep the outer object from being garbage collected** and use extra memory, while static nested objects are independent.
11. You **can** when the type is a **functional interface** (one abstract method). You **can't** when you need several methods, fields/state, to extend a class, or a different `this`.
12. **No** to both. Local classes can't have access modifiers or be `static`. (They **can** be `final` or `abstract`.)
13. In a **lambda**, `this` refers to the **enclosing** object. In an **anonymous class**, `this` refers to the **anonymous object itself**.
14. `Outer$StaticNested.class`, `Outer$Inner.class`, `Outer$1Local.class`, `Outer$1.class`.

</details>

---

*Practice tip: for each kind, write a tiny program, compile it, and look at the generated `.class` files with `javap -p`. Seeing the hidden `this$0` and `val$...` fields makes the rules (outer instance needed, effectively final) click.*
