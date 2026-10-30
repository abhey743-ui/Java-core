# Java Enums: The Complete Deep Dive (With a Full Postmortem)

> You said enums feel tough because there are many concepts. This guide fixes that by building them **one idea at a time**, starting from **the problem**, then **the solution**, then **what the compiler really does** (the postmortem), then **every feature, every operation and every use case**, then **how enums changed from Java version to Java version**.
> It follows your video outline (constants problem, introduction, architecture, state and behavior, built-in methods, best practices) and goes much deeper.
> Every concept has code, a diagram, and the output so you can check your understanding.

---

## Table of Contents

1. [Roadmap: How to Learn Enums](#1-roadmap-how-to-learn-enums)
2. [The Problem: Life Without Enums](#2-the-problem-life-without-enums)
3. [What Is an Enum?](#3-what-is-an-enum)
4. [Why Not Just Use Other Data Types or OOP?](#4-why-not-just-use-other-data-types-or-oop)
5. [Enum Architecture: What an Enum Really Is](#5-enum-architecture-what-an-enum-really-is)
6. [The Postmortem: What the Compiler Does to Your Enum](#6-the-postmortem-what-the-compiler-does-to-your-enum)
7. [Adding State and Behavior: Fields, Constructors, Methods](#7-adding-state-and-behavior-fields-constructors-methods)
8. [Constant-Specific Behavior: Abstract Methods and Polymorphism](#8-constant-specific-behavior-abstract-methods-and-polymorphism)
9. [Enums and Interfaces (and Why Enums Can't Extend)](#9-enums-and-interfaces-and-why-enums-cant-extend)
10. [Built-in and Compiler-Generated Methods](#10-built-in-and-compiler-generated-methods)
11. [Enums and `switch`](#11-enums-and-switch)
12. [`EnumMap` and `EnumSet`](#12-enummap-and-enumset)
13. [Where Can You Declare an Enum? (and Modifier Rules)](#13-where-can-you-declare-an-enum-and-modifier-rules)
14. [The Enum Singleton, Serialization and Reflection](#14-the-enum-singleton-serialization-and-reflection)
15. [Enums You Already Use in the JDK](#15-enums-you-already-use-in-the-jdk)
16. [Use Cases: Where We Use Enums in Real Software](#16-use-cases-where-we-use-enums-in-real-software)
17. [Version-by-Version Improvements](#17-version-by-version-improvements)
18. [Best Practices and When NOT to Use Enums](#18-best-practices-and-when-not-to-use-enums)
19. [Common Mistakes and Pitfalls](#19-common-mistakes-and-pitfalls)
20. [Practical Programs](#20-practical-programs)
21. [Cheat Sheet, Summary and Quiz](#21-cheat-sheet-summary-and-quiz)

---

## 1. Roadmap: How to Learn Enums

Enums look complicated only if you meet all features at once. Learn them in this order and each step will feel small:

```
 STEP 1  The problem          Why were plain constants not good enough?
    │
 STEP 2  The basic enum       A named, fixed list of values:  enum Day { MON, TUE }
    │
 STEP 3  Enum = a class       Each constant is an OBJECT of the enum type
    │
 STEP 4  Fields + constructor Attach data to each constant
    │
 STEP 5  Methods + abstract   Attach behavior to each constant
    │
 STEP 6  Built-in methods     values(), valueOf(), name(), ordinal(), compareTo()
    │
 STEP 7  Partners             switch, EnumMap, EnumSet, interfaces
    │
 STEP 8  Real use cases       status, roles, state machines, strategies, singletons
```

### The one sentence to remember

> **An enum is a special class that has a fixed, named set of instances (the constants), created once, and nobody else can create more.**

If you remember only that, everything else becomes a consequence of it.

---

## 2. The Problem: Life Without Enums

Before Java 5 (2004) there were no enums. Programmers represented "a fixed set of choices" with plain constants. To understand why enums exist, you must feel the pain of the old way.

### 2.1 Approach 1: `int` constants

```java
public class OrderStatus {
    public static final int NEW      = 0;
    public static final int PAID     = 1;
    public static final int SHIPPED  = 2;
    public static final int CANCELLED = 3;
}
```

```java
static void process(int status) {
    if (status == OrderStatus.PAID) {
        System.out.println("Preparing shipment");
    }
}
```

It looks fine. Now watch what the compiler **allows**:

```java
process(OrderStatus.PAID);        // intended use
process(99);                      // COMPILES! 99 is not a status at all
process(-5);                      // COMPILES!
process(Day.MONDAY);              // COMPILES! (a constant from a totally different group)
int weird = OrderStatus.PAID + OrderStatus.SHIPPED;   // COMPILES: meaningless arithmetic
System.out.println(OrderStatus.PAID);                 // prints 1. What is "1"?
```

#### The 8 problems with `int` constants

| # | Problem | Explanation |
|---|---------|-------------|
| 1 | **No type safety** | The parameter is just an `int`. The compiler accepts **any** integer, not only the intended status codes. (This is the main point from the video.) |
| 2 | **Mixing unrelated groups** | `Day.MONDAY` (0) and `OrderStatus.NEW` (0) are the same number. The compiler can't tell them apart. |
| 3 | **Meaningless printing** | Logs show `1`, not `PAID`. Debugging becomes guesswork. |
| 4 | **No namespace** | All constants live as plain names. To avoid clashes you invent prefixes: `ORDER_NEW`, `TICKET_NEW`. |
| 5 | **Can't list or count them** | There is no way to say "give me all statuses" or "how many are there?" |
| 6 | **Meaningless operations** | `PAID + SHIPPED`, `PAID * 7`, `PAID < SHIPPED` all compile. |
| 7 | **Brittle (compile-time inlining)** | `static final int` constants are **copied into** every class that uses them at compile time. If you change a value and recompile only one class, other classes still contain the **old** number. |
| 8 | **No data or behavior** | You can't attach a description, a code or a method to a number. |

### 2.2 Approach 2: `String` constants

Developers tried strings to get readable values:

```java
public class Status {
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
}
```

```java
static void handle(String status) {
    if (status.equals(Status.SUCCESS)) {
        System.out.println("OK");
    }
}
```

Again, the compiler allows nonsense, and new bug types appear:

```java
handle("SUCCESS");        // works
handle("success");        // compiles, but equals() is FALSE  → case sensitivity bug
handle("SUCESS");         // compiles with a typo: silently does nothing
handle("banana");         // compiles: not a status at all
handle(null);             // compiles, then NullPointerException at status.equals(...)

if (status == "SUCCESS")  // compiles, but compares REFERENCES: may be false even for equal text
```

#### The problems with `String` constants

| # | Problem | Explanation |
|---|---------|-------------|
| 1 | **No type safety** | Any `String` is accepted. |
| 2 | **Typos aren't caught** | `"SUCESS"` compiles. You find the bug at runtime, if at all. |
| 3 | **Case sensitivity** | `"Success"` ≠ `"SUCCESS"`. |
| 4 | **`==` vs `equals`** | Using `==` on strings is a classic bug (reference comparison). |
| 5 | **Slower** | Comparing strings means checking length and characters (`equals` is O(length)), while comparing two `int`s or two references is a **single instruction**. (The video points this out.) |
| 6 | **More memory** | A String is an object with its own characters; an `int` is 4 bytes. |
| 7 | **No list of all values, no behavior** | Same limits as `int` constants. |

### 2.3 Approach 3: boolean flags

```java
boolean isNew, isPaid, isShipped, isCancelled;
```

With `n` choices you have `n` booleans, so **2ⁿ possible combinations**, but only `n` are valid. Nothing stops `isNew == true && isShipped == true`. This is worse than the others.

### 2.4 Approach 4: constants in an `interface` (an anti-pattern)

```java
interface Colors { int RED = 0; int GREEN = 1; }
class Painter implements Colors { }     // now Painter "inherits" the constants: pollutes the API
```

It has all the `int` problems, plus it misuses inheritance.

### 2.5 Approach 5: the "type-safe enum pattern" (the clever pre-Java-5 fix)

Smart developers (popularized by Joshua Bloch in *Effective Java*, 2001) said: *"Make each constant an **object** of a special class, and **forbid anyone else from creating objects** of that class."*

```java
public final class Color {

    public static final Color RED   = new Color("RED");
    public static final Color GREEN = new Color("GREEN");
    public static final Color BLUE  = new Color("BLUE");

    private final String name;

    private Color(String name) {            // PRIVATE constructor: nobody else can create a Color
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
```

```java
static void paint(Color c) { ... }

paint(Color.RED);        // ✓
paint(5);                // ✗ compile error: int is not a Color
paint("RED");            // ✗ compile error
```

This **fixes type safety**: only three `Color` objects exist. But it has big drawbacks:

| Missing or painful | Detail |
|--------------------|--------|
| **Lots of boilerplate** | You write the class, private constructor, `toString`... for every group |
| **No `values()`** | You must write the "list all colors" method yourself |
| **No `valueOf("RED")`** | You must write the lookup yourself |
| **No ordering/`ordinal`** | You must invent it |
| **Doesn't work in `switch`** | `switch` can't use these objects |
| **Serialization breaks it** | Deserializing creates a **new** `Color` object, so `==` stops working unless you add `readResolve()` |
| **No `EnumMap`/`EnumSet` support** | You can't use the fast special collections |
| **Easy to get wrong** | Forget `final` or the private constructor and the guarantee is gone |

### 2.6 What we really need (the requirements list)

From the failed attempts, we can write down exactly what a perfect solution must give:

| # | Requirement |
|---|-------------|
| 1 | A **closed set**: only the listed values exist |
| 2 | **Type safety**: the compiler rejects anything else |
| 3 | **Readable**: prints as `PAID`, not `1` |
| 4 | **Iterable and countable**: "all values" is one call |
| 5 | Can carry **data** (code, description...) |
| 6 | Can carry **behavior** (methods) |
| 7 | Works in **`switch`**, **collections**, **serialization** |
| 8 | **Almost no boilerplate** |

**The solution is the `enum`:** it is exactly the type-safe enum pattern, but **built into the language**, with the compiler generating all the boilerplate for you.

---

## 3. What Is an Enum?

### 3.1 Definition

An **enum** (short for *enumeration*) is a special Java type that represents a **fixed set of named constants**.

### 3.2 Your first enum

```java
public enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}
```

Read it as: *"There is a type `Day`, and it has exactly these 7 possible values."*

### 3.3 Using it

```java
Day today = Day.FRIDAY;                 // pick a constant using  EnumName.CONSTANT

System.out.println(today);              // FRIDAY      (readable!)

today = Day.SUNDAY;                     // ✓ fine
today = 5;                              // ✗ ERROR: int cannot be converted to Day
today = "FRIDAY";                       // ✗ ERROR: String cannot be converted to Day
today = Day.FUNDAY;                     // ✗ ERROR: cannot find symbol (no such constant)
```

**This solves the video's main problem:** an `int` variable accepts any integer, but a `Day` variable accepts **only the seven `Day` constants** (or `null`).

### 3.4 Comparing and looping

```java
if (today == Day.FRIDAY) {                       // compare enums with ==
    System.out.println("Weekend is close!");
}

for (Day d : Day.values()) {                     // loop over ALL constants
    System.out.println(d);
}
```

### 3.5 Syntax rules (basic)

```
 [access modifier]  enum  EnumName  {
        CONSTANT_ONE, CONSTANT_TWO, CONSTANT_THREE      // comma-separated, usually UPPER_SNAKE_CASE
        ;                                               // semicolon: required only if members follow
        // optional: fields, constructors, methods
 }
```

| Rule | Detail |
|------|--------|
| Keyword | `enum` |
| Enum name | `PascalCase`, a singular noun: `Day`, `OrderStatus`, `Direction` |
| Constant names | `UPPER_SNAKE_CASE`: `MONDAY`, `NOT_FOUND` |
| Constants come first | Before any field or method |
| Separator | Commas between constants |
| Semicolon | **Required** after the last constant **only if** you add fields/methods/constructors |
| Trailing comma | Allowed: `A, B, C,` |
| Empty enum | Allowed: `enum Nothing { }` |
| Files | A `public` enum must be in a file with the same name: `Day.java` |

```java
public enum Level { LOW, MEDIUM, HIGH }                  // no semicolon needed

public enum Size {
    SMALL, MEDIUM, LARGE;                                // semicolon because a method follows

    public boolean isBig() { return this == LARGE; }
}
```

### 3.5b Don't confuse `enum` with `Enumeration`

Java also has an old interface `java.util.Enumeration` (used by `Vector` and `Hashtable` to loop over elements). It has **nothing to do** with the `enum` keyword.

---

## 4. Why Not Just Use Other Data Types or OOP?

This is the question you asked: *"Why does enum exist if we can make it using other data types or OOP concepts?"* The honest answer has two parts.

### 4.1 You CAN: but it's worse

| Approach | Type-safe | Prints nicely | List all values | Data + behavior | Works in `switch` | Boilerplate |
|----------|:---------:|:-------------:|:---------------:|:---------------:|:-----------------:|:-----------:|
| `int` constants | ✗ | ✗ | ✗ | ✗ | ✓ | low |
| `String` constants | ✗ | ✓ | ✗ | ✗ | ✓ | low |
| `boolean` flags | ✗ | ✗ | ✗ | ✗ | n/a | grows fast |
| Constants in an interface | ✗ | ✗ | ✗ | ✗ | ✓ | low |
| **Hand-written type-safe class** (OOP) | ✓ | ✓ (if you write it) | only if you write it | ✓ | ✗ | **high** |
| **`enum`** | **✓** | **✓** | **✓** (`values()`) | **✓** | **✓** | **very low** |

### 4.2 The deeper answer: enum IS the OOP solution

An enum is **not** a competitor to OOP. It is **OOP made easy** for one specific situation:

> *"I have a **small, fixed, known** set of **instances** of a concept, and I want the compiler to guarantee that no other instances exist."*

Using plain `int`/`String` here is **"stringly/numerically typed" programming**: you store a *concept* in a type that **knows nothing** about the concept. The compiler can't help you. With an enum the **type itself** carries the meaning:

```
   int status = 1;          →  "some number"                (the compiler knows nothing)
   OrderStatus s = PAID;    →  "one of exactly 5 order states" (the compiler enforces it)
```

### 4.3 What the compiler and runtime give you for free

| Free benefit | What it means |
|--------------|---------------|
| **Compile-time safety** | Wrong values, typos and wrong groups are compile errors |
| **Exactly one instance per constant** | `==` is always correct; no duplicates |
| **`values()` / `valueOf()`** | Generated for you |
| **`name()`, `ordinal()`, `compareTo()`** | Inherited from `java.lang.Enum` |
| **`switch` support** with **exhaustiveness checking** (in switch expressions) | Add a constant later and the compiler tells you every `switch` to update |
| **Safe serialization** | Deserializing never creates a second `RED` |
| **Reflection-proof** | You can't create extra instances even with reflection |
| **Fast special collections** | `EnumMap` and `EnumSet` |
| **Readable `toString()`** | Prints the name |
| **Can carry data and behavior** | Fields, constructors, methods, abstract methods, interfaces |

### 4.4 Decision guide: do I need an enum?

```
 Is the set of possible values SMALL, FIXED and KNOWN AT COMPILE TIME?
     │ No (changes at runtime, comes from a database/config) → use a class/table, not an enum
     │ Yes
     ▼
 Do I want the compiler to reject any other value?
     │ Yes → ENUM
```

Typical answers where it's a **yes**: days of week, months, card suits, order/payment status, user roles, log levels, directions, HTTP method types, traffic-light states, file open modes.

---

## 5. Enum Architecture: What an Enum Really Is

This covers section III of your video outline.

### 5.1 An enum is a class

> **An `enum` is not just a list of names. The compiler turns it into a CLASS.**

That class automatically **extends `java.lang.Enum`**.

```java
public enum Day { MONDAY, TUESDAY, WEDNESDAY }
```

```
          java.lang.Object
                 ▲
                 │ extends
          java.lang.Enum<E>        ← the built-in base class for ALL enums
          │ fields:  name, ordinal
          │ methods: name(), ordinal(), toString(), compareTo(), equals(), hashCode(), getDeclaringClass() ...
          │ implements: Comparable<E>, Serializable (and Constable)
                 ▲
                 │ extends   (the compiler writes this for you)
               Day              ← YOUR enum
```

> Why `Enum<E>`, e.g. `Enum<Day>`? The type parameter is the enum itself, so `compareTo(E other)` accepts **only the same enum type**: you can't compare a `Day` with a `Color`.

### 5.2 Every constant is an object

> **Each constant you list is a `public static final` instance (an object) of the enum type.**

`MONDAY` is not a number and not a string. It is **an object of type `Day`**.

```java
Day d = Day.MONDAY;       // d refers to a real object of class Day
```

```
 HEAP (memory)

   class Day (loaded once)
   ┌──────────────────────────────────────────────────────────────────────┐
   │ static final Day MONDAY    ─────►  ┌─────────────────────────┐        │
   │ static final Day TUESDAY   ──┐     │ Day object              │        │
   │ static final Day WEDNESDAY ─┐│     │  name    = "MONDAY"     │        │
   │ static final Day[] $VALUES ┐││     │  ordinal = 0            │        │
   └────────────────────────────┼┼┼─────└─────────────────────────┘        │
                                │││     ┌─────────────────────────┐        │
                                ││└───► │ Day object: TUESDAY, 1  │        │
                                │└────► │ Day object: WEDNESDAY, 2│        │
                                │       └─────────────────────────┘        │
                                └─► [ ●, ●, ● ]  (array holding the 3 references)

   Your variable:   Day d = Day.MONDAY;     d ──► the SAME MONDAY object (no copy is made)
```

Key facts:

- **Exactly one object per constant**, created once, shared everywhere.
- Each object stores its **`name`** and **`ordinal`** (its position, starting at 0).
- Because there is only **one** `MONDAY` object, **`==` is the perfect way to compare** enums.

### 5.3 Why "marker"? (the video's term)

The constants work as **markers** (tags) for specific states: `PAID` is a unique, readable marker meaning "this order is paid". Their **identity** is their meaning. And because the type is closed, you **cannot assign anything except the defined constants** to an enum variable:

```java
OrderStatus s = OrderStatus.PAID;     // ✓
s = 7;                                // ✗ compile error
s = new OrderStatus();                // ✗ compile error: enum types may not be instantiated
s = null;                             // ✓ compiles (null is allowed: be careful)
```

### 5.4 The fixed-set guarantee in one picture

```
   Normal class                          Enum
   ────────────                          ────
   Student s1 = new Student();           OrderStatus s1 = OrderStatus.PAID;
   Student s2 = new Student();           OrderStatus s2 = OrderStatus.PAID;
   ... infinite instances possible       ... s1 == s2  (the SAME object; only 5 exist in total)
```

---

## 6. The Postmortem: What the Compiler Does to Your Enum

Here we open the enum up and look inside, like a post-mortem examination: *"What exactly did the compiler build from my 3-line enum?"*

### 6.1 What you write vs what the compiler produces

**You write:**

```java
public enum Day { MON, TUE, WED }
```

**The compiler produces (conceptually, in plain Java):**

```java
public final class Day extends java.lang.Enum<Day> {

    // 1) each constant becomes a public static final object
    public static final Day MON = new Day("MON", 0);
    public static final Day TUE = new Day("TUE", 1);
    public static final Day WED = new Day("WED", 2);

    // 2) a hidden array that remembers all constants
    private static final Day[] $VALUES = new Day[] { MON, TUE, WED };

    // 3) a PRIVATE constructor with two hidden extra parameters (name, ordinal)
    private Day(String name, int ordinal) {
        super(name, ordinal);                 // passes them up to java.lang.Enum
    }

    // 4) generated static methods
    public static Day[] values() {
        return $VALUES.clone();               // a COPY, so nobody can modify the internal array
    }

    public static Day valueOf(String name) {
        return Enum.valueOf(Day.class, name); // look the name up
    }
}
```

> You can't write this yourself. `new Day(...)` is "enum types may not be instantiated", and `class X extends Enum` is "classes cannot directly extend java.lang.Enum". Only the compiler is allowed to build it.

### 6.2 The 8 things the compiler did

| # | What the compiler did | Why it matters |
|---|-----------------------|----------------|
| 1 | Made the enum a **`final` class** extending `java.lang.Enum<Day>` | Nobody can subclass it, so the set stays fixed |
| 2 | Turned each constant into a **`public static final` field** holding an object | `Day.MON` is a normal static field access |
| 3 | Created the objects **in order**, passing each its **name** and **ordinal** | That's where `name()` and `ordinal()` come from |
| 4 | Made the **constructor `private`** (and added the 2 hidden parameters) | Nobody else can create instances |
| 5 | Generated **`values()`** that returns a **clone** of a hidden array | Safe iteration; the internal array can't be modified |
| 6 | Generated **`valueOf(String)`** | Converts a name to its constant |
| 7 | Marked the class with the **`ENUM` flag** in the `.class` file | Reflection and `switch` recognize it as an enum |
| 8 | Made `equals`, `hashCode`, `compareTo`, `name`, `clone`, `finalize` **`final`** in `Enum` | The identity rules can't be broken |

### 6.3 See it yourself with `javap`

Save `Day.java`:

```java
public enum Day { MON, TUE, WED }
```

```
javac Day.java
javap Day
```

Typical output (exact lines vary a little by Java version):

```
Compiled from "Day.java"
public final class Day extends java.lang.Enum<Day> {
  public static final Day MON;
  public static final Day TUE;
  public static final Day WED;
  public static Day[] values();
  public static Day valueOf(java.lang.String);
  static {};
}
```

Add `-p` to also see the private members (`$VALUES`, the private constructor, a tiny private `$values()` helper used by newer compilers):

```
javap -p Day
```

### 6.4 Why `values()` returns a clone (the video's "prevent direct modification")

Arrays are mutable. If `values()` returned the **internal** array, anyone could damage your enum:

```java
Day[] days = Day.values();
days[0] = null;              // if this changed the internal array, Day would be corrupted!
```

So `values()` returns `$VALUES.clone()`, a **fresh copy**, each time:

```java
System.out.println(Day.values() == Day.values());          // false (two different arrays)
System.out.println(Day.values()[0] == Day.values()[0]);    // true  (the elements are the same objects)
```

**Performance note:** each call allocates a new array. In a hot loop, call it once and keep it:

```java
private static final Day[] ALL = Day.values();      // cache it
```

### 6.5 How `valueOf` really works

`Day.valueOf("TUE")` calls `Enum.valueOf(Day.class, "TUE")`, which uses a **name → constant map** that the JVM builds **once** (lazily) from the constants. So lookup is a fast hash lookup, not a loop.

```
   "TUE" ──► [ map: "MON"→MON, "TUE"→TUE, "WED"→WED ] ──► Day.TUE
   "xyz" ──► not in the map ──► IllegalArgumentException: No enum constant Day.xyz
```

### 6.6 When are the constants created? (the timing demo)

Enum constants are created **when the enum class is first used** (class initialization), **all at once**, exactly one time. The JVM guarantees this is **thread-safe**.

```java
public class InitDemo {

    enum Level {
        LOW(1), HIGH(3);

        Level(int v) {
            System.out.println("Creating " + name() + " (ordinal " + ordinal() + ")");
        }
    }

    public static void main(String[] args) {
        System.out.println("Before first use of Level");
        Level l = Level.LOW;
        System.out.println("After: " + l);
    }
}
```

Output:
```
Before first use of Level
Creating LOW (ordinal 0)
Creating HIGH (ordinal 1)
After: LOW
```

Notice: touching `Level.LOW` created **both** constants, in declaration order, once. Using `Level` again later creates nothing new.

### 6.7 The static-access restriction (a famous compile error)

Constants are created **before** the enum's other `static` fields are initialized. So a constructor can't use a normal static field:

```java
enum Color {
    RED, GREEN;

    private static int count = 0;

    Color() {
        count++;              // ERROR: illegal reference to static field from initializer
    }
}
```

**Why:** when `RED` is being constructed, `count` has not been set up yet. **Fix:** do that work **after** the constants exist, in a static block:

```java
enum Color {
    RED, GREEN;

    private static int count;

    static {                   // runs AFTER all constants are created
        count = values().length;
    }
}
```

(Compile-time constants like `static final int MAX = 10;` are allowed.)

### 6.8 Constant bodies become hidden subclasses

If a constant has its **own `{ ... }` body** (section 8), the compiler creates a **hidden subclass** for it:

```java
enum Operation {
    PLUS  { int apply(int a, int b) { return a + b; } },
    MINUS { int apply(int a, int b) { return a - b; } };
    abstract int apply(int a, int b);
}
```

```
                 java.lang.Enum<Operation>
                          ▲
                      Operation              (abstract; NOT final any more)
                  ┌───────┴────────┐
             Operation$1       Operation$2   (hidden anonymous subclasses)
              (PLUS)            (MINUS)
```

Consequences:

- `Operation.PLUS.getClass()` is **`Operation$1`**, not `Operation`.
- Use **`getDeclaringClass()`** to get `Operation`.
- The enum class is **not final** in this case. Since **Java 17** the language treats such an enum as **implicitly sealed** (its permitted subclasses are exactly those hidden classes).

### 6.9 How `switch` on an enum is compiled

```java
switch (day) {
    case MON: ...
    case TUE: ...
}
```

The compiler doesn't hard-code ordinals. For a classic switch, `javac` creates a small **synthetic helper class** (like `Main$1`) containing a table `$SwitchMap$Day[]` that maps each constant's **ordinal → case number**, built at runtime using the constants' **names**.

```
   day.ordinal()  ──►  $SwitchMap$Day[ordinal]  ──►  1, 2, 3...  ──►  jump to the matching case
```

Why the indirection? If someone later **reorders** the constants in `Day` and recompiles only `Day`, the old `switch` code still works, because the mapping is rebuilt from names. (Pattern-matching switches in Java 21 use a different mechanism, but the idea of "not trusting ordinals" is the same.)

### 6.10 What the postmortem tells us: the "can't" list

| You can't... | Because... |
|--------------|------------|
| `new Day()` | The constructor is private and the compiler forbids it |
| `class X extends Day` | The enum is `final` (or sealed) |
| `enum E extends SomeClass` | It already extends `Enum`, and Java has single inheritance |
| `day.clone()` | `Enum.clone()` is `final` and always throws `CloneNotSupportedException` |
| Create a new constant by **reflection** | `Constructor.newInstance` refuses: "Cannot reflectively create enum objects" |
| Create a duplicate by **deserialization** | Enums are serialized by name and resolved to the existing constant |
| Override `equals`/`hashCode`/`compareTo`/`name` | They're `final` in `Enum` |

### 6.11 Inspect an enum at runtime

See program 20.5 for a runnable inspector that prints the superclass, `isEnum()`, the hidden subclass names and more.

---

## 7. Adding State and Behavior: Fields, Constructors, Methods

This covers section IV (first half) of your outline. Since an enum is a class, it can have **fields, a constructor and methods**.

### 7.1 Step-by-step: attach data to each constant

**Goal:** a direction enum where each direction knows its compass angle.

**Step 1: List the constants and pass values in parentheses.**

```java
public enum Direction {
    NORTH(0), EAST(90), SOUTH(180), WEST(270);      // each constant calls the constructor
```

**Step 2: End the constants with a semicolon** (required because members follow).

**Step 3: Add a field** (make it `private final`).

```java
    private final int degrees;
```

**Step 4: Add the constructor** that receives the value.

```java
    Direction(int degrees) {            // implicitly PRIVATE: you can't call it yourself
        this.degrees = degrees;
    }
```

**Step 5: Add a getter and any methods.**

```java
    public int getDegrees() {
        return degrees;
    }
}
```

**Complete enum:**

```java
public enum Direction {
    NORTH(0), EAST(90), SOUTH(180), WEST(270);

    private final int degrees;

    Direction(int degrees) {
        this.degrees = degrees;
    }

    public int getDegrees() {
        return degrees;
    }

    public Direction turnRight() {
        return values()[(ordinal() + 1) % values().length];
    }

    public Direction turnLeft() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public Direction opposite() {
        return values()[(ordinal() + 2) % values().length];
    }
}
```

```java
Direction d = Direction.NORTH;
System.out.println(d.getDegrees());            // 0
System.out.println(Direction.WEST.getDegrees());// 270
System.out.println(d.turnRight());             // EAST
System.out.println(Direction.WEST.turnRight());// NORTH   (wraps around)
System.out.println(d.turnLeft());              // WEST
System.out.println(Direction.EAST.opposite()); // WEST
```

(Here `ordinal()` is fine because the **declared order is the meaning** of the enum. For data that must stay stable, use an explicit field instead; see section 10.4.)

### 7.2 What happens when this enum loads

```
 NORTH(0)  → calls Direction("NORTH", 0, 0)  → name="NORTH", ordinal=0, degrees=0
 EAST(90)  → calls Direction("EAST", 1, 90)  → name="EAST",  ordinal=1, degrees=90
 SOUTH(180)→ ...
 WEST(270) → ...

 HEAP:  [NORTH: degrees 0]  [EAST: degrees 90]  [SOUTH: degrees 180]  [WEST: degrees 270]
```

Each constant is an **object with its own field values**.

### 7.3 Rules for fields, constructors and methods

| Topic | Rule |
|-------|------|
| **Constructor** | **Implicitly `private`.** Writing `public` or `protected` is a compile error. You never call it; the constants do. |
| **Constructor arguments** | Written in parentheses after each constant: `EAST(90)` |
| **Overloaded constructors** | Allowed; each constant picks one by its arguments |
| **Fields** | Any kind. **Make them `private final`** (immutable) in almost every case |
| **Methods** | Instance and `static` methods are allowed |
| **`this`** | Refers to the current constant |
| **Constants first** | Constants must be listed **before** any member |
| **Semicolon** | Required after the last constant if anything follows |
| **Static fields in the constructor** | Not allowed (section 6.7) |

### 7.4 More examples

**Several fields:**

```java
public enum HttpStatus {
    OK(200, "OK"),
    NOT_FOUND(404, "Not Found"),
    SERVER_ERROR(500, "Internal Server Error");

    private final int code;
    private final String reason;

    HttpStatus(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode()      { return code; }
    public String getReason() { return reason; }
    public boolean isError()  { return code >= 400; }
}
```

```java
HttpStatus s = HttpStatus.NOT_FOUND;
System.out.println(s.getCode() + " " + s.getReason());   // 404 Not Found
System.out.println(s.isError());                         // true
System.out.println(HttpStatus.OK.isError());             // false
```

**Coins and money:**

```java
public enum Coin {
    PENNY(1), NICKEL(5), DIME(10), QUARTER(25);

    private final int cents;

    Coin(int cents) { this.cents = cents; }

    public int getCents() { return cents; }
}

int total = 0;
for (Coin c : Coin.values()) {
    total += c.getCents();
}
System.out.println(total);      // 41
```

**Unit conversion:**

```java
public enum LengthUnit {
    METER(1.0), KILOMETER(1000.0);

    private final double inMeters;

    LengthUnit(double inMeters) { this.inMeters = inMeters; }

    public double convert(double value, LengthUnit to) {
        return value * this.inMeters / to.inMeters;
    }
}

System.out.println(LengthUnit.KILOMETER.convert(2, LengthUnit.METER));   // 2000.0
System.out.println(LengthUnit.METER.convert(2500, LengthUnit.KILOMETER)); // 2.5
```

### 7.5 Reverse lookup: from a code back to the constant

A very common need: you receive `404` from a database or network and want the constant.

```java
import java.util.HashMap;
import java.util.Map;

public enum HttpStatus {
    OK(200), NOT_FOUND(404), SERVER_ERROR(500);

    private final int code;
    private static final Map<Integer, HttpStatus> BY_CODE = new HashMap<>();

    static {                                   // runs AFTER the constants exist
        for (HttpStatus s : values()) {
            BY_CODE.put(s.code, s);
        }
    }

    HttpStatus(int code) { this.code = code; }

    public int getCode() { return code; }

    public static HttpStatus fromCode(int code) {
        HttpStatus s = BY_CODE.get(code);
        if (s == null) {
            throw new IllegalArgumentException("Unknown status code: " + code);
        }
        return s;
    }
}

System.out.println(HttpStatus.fromCode(404));     // NOT_FOUND
HttpStatus.fromCode(999);                         // IllegalArgumentException: Unknown status code: 999
```

(The map is filled in a **static block**, because a constructor can't touch static fields.)

### 7.6 A warning about mutable state

Enum constants are **shared singletons**. If a field is **mutable**, the change is **visible to the whole program**:

```java
enum Counter {
    INSTANCE;
    private int count;                 // mutable!
    public int next() { return ++count; }
}

Counter.INSTANCE.next();               // 1
Counter.INSTANCE.next();               // 2  (global state, shared by everyone, not thread-safe)
```

Prefer **`private final`** fields. Use mutable state only deliberately (like a singleton), and then protect it for multithreading.

---

## 8. Constant-Specific Behavior: Abstract Methods and Polymorphism

This covers section IV (second half) of your outline: *"define abstract methods inside an enum and force each constant to provide its own implementation."*

### 8.1 The idea

Sometimes each constant should **behave differently**. Instead of one big `switch`, let **each constant carry its own code**.

### 8.2 Step-by-step example: a calculator

**Step 1: Declare an abstract method in the enum.**

```java
public enum Operation {
    // constants go here...

    public abstract double apply(double a, double b);       // every constant MUST implement it
}
```

**Step 2: Give each constant a body `{ ... }` implementing the method.**

```java
public enum Operation {

    ADD("+") {
        @Override
        public double apply(double a, double b) { return a + b; }
    },
    SUBTRACT("-") {
        @Override
        public double apply(double a, double b) { return a - b; }
    },
    MULTIPLY("*") {
        @Override
        public double apply(double a, double b) { return a * b; }
    },
    DIVIDE("/") {
        @Override
        public double apply(double a, double b) {
            if (b == 0) throw new ArithmeticException("Division by zero");
            return a / b;
        }
    };                                                      // semicolon: members follow

    private final String symbol;

    Operation(String symbol) { this.symbol = symbol; }

    public String getSymbol() { return symbol; }

    public abstract double apply(double a, double b);
}
```

**Step 3: Use it.**

```java
for (Operation op : Operation.values()) {
    System.out.println("12 " + op.getSymbol() + " 4 = " + op.apply(12, 4));
}
```

Output:
```
12 + 4 = 16.0
12 - 4 = 8.0
12 * 4 = 48.0
12 / 4 = 3.0
```

**If you forget a body for one constant**, it's a compile error: *"enum constant must implement abstract method"*. The compiler **forces** every constant to provide its behavior.

### 8.3 Why is this polymorphism?

Each constant is an object of its own hidden subclass (section 6.8). When you call `op.apply(12, 4)`, Java looks at the **real object** and runs **that constant's** version. Same call, different behavior: that's **runtime polymorphism**.

```
   Operation op = ...;           op.apply(12, 4)
                                      │
            ┌─────────────────────────┼──────────────────────────┐
         op is ADD               op is MULTIPLY              op is DIVIDE
      runs a + b                 runs a * b                  runs a / b (with a zero check)
```

### 8.4 Non-abstract method with optional overriding

Not every method must be abstract. You can give a **default** and let only some constants **override** it:

```java
public enum Role {
    GUEST,
    EDITOR,
    ADMIN {
        @Override
        public boolean canDelete() { return true; }       // only ADMIN overrides
    };

    public boolean canDelete() { return false; }          // default for everyone else
}

System.out.println(Role.GUEST.canDelete());     // false
System.out.println(Role.ADMIN.canDelete());     // true
```

### 8.5 Alternative: behavior as a lambda field (Java 8+)

When each constant needs only **one small function**, a lambda is shorter:

```java
import java.util.function.DoubleBinaryOperator;

public enum Op {
    ADD("+",      (a, b) -> a + b),
    SUBTRACT("-", (a, b) -> a - b),
    MULTIPLY("*", (a, b) -> a * b),
    DIVIDE("/",   (a, b) -> a / b);

    private final String symbol;
    private final DoubleBinaryOperator function;

    Op(String symbol, DoubleBinaryOperator function) {
        this.symbol = symbol;
        this.function = function;
    }

    public double apply(double a, double b) {
        return function.applyAsDouble(a, b);
    }

    public String getSymbol() { return symbol; }
}

System.out.println(Op.MULTIPLY.apply(6, 7));      // 42.0
```

No hidden subclasses are created here: every constant is a plain `Op` object holding a function.

### 8.6 Another example: a state machine (traffic light)

```java
public enum TrafficLight {
    RED(30)    { @Override public TrafficLight next() { return GREEN; } },
    GREEN(25)  { @Override public TrafficLight next() { return YELLOW; } },
    YELLOW(5)  { @Override public TrafficLight next() { return RED; } };

    private final int seconds;

    TrafficLight(int seconds) { this.seconds = seconds; }

    public int getSeconds() { return seconds; }

    public abstract TrafficLight next();         // each light knows what comes after it
}
```

```java
TrafficLight light = TrafficLight.RED;
for (int i = 0; i < 5; i++) {
    System.out.println(light + " for " + light.getSeconds() + "s");
    light = light.next();
}
```

Output:
```
RED for 30s
GREEN for 25s
YELLOW for 5s
RED for 30s
GREEN for 25s
```

```
        ┌─────────────────────────────┐
        ▼                             │
      RED ──────► GREEN ──────► YELLOW
     (30s)        (25s)         (5s)
```

### 8.7 Abstract methods vs `switch (this)`

You can also put the logic in one method using `switch (this)`:

```java
public enum TrafficLight {
    RED, GREEN, YELLOW;

    public TrafficLight next() {
        return switch (this) {
            case RED    -> GREEN;
            case GREEN  -> YELLOW;
            case YELLOW -> RED;
        };
    }
}
```

| | Abstract method per constant | `switch (this)` |
|---|------------------------------|-----------------|
| Forgot a new constant | **Compile error** (must implement) | Switch **expression**: compile error too. Old switch statement with `default`: **silently** wrong |
| Where the logic lives | Next to each constant | In one place |
| Best when | Each constant has **substantial** unique logic | The logic is **short and uniform** |
| Hidden classes | One per constant | None |

---

## 9. Enums and Interfaces (and Why Enums Can't Extend)

### 9.1 An enum can implement interfaces

> **An enum cannot extend another class, but it CAN implement any number of interfaces.**

```java
interface Payable {
    double finalAmount(double price);
}

public enum PaymentMethod implements Payable {
    CASH {
        @Override
        public double finalAmount(double price) { return price; }
    },
    CARD {
        @Override
        public double finalAmount(double price) { return price + 1.5; }     // flat card fee
    };
}

Payable p = PaymentMethod.CARD;
System.out.println(p.finalAmount(100));                    // 101.5
System.out.println(PaymentMethod.CASH.finalAmount(100));   // 100.0
```

Multiple interfaces:

```java
interface A { void a(); }
interface B { void b(); }

enum Both implements A, B {          // several interfaces: allowed
    ONE;

    @Override public void a() { System.out.println("a"); }
    @Override public void b() { System.out.println("b"); }
}
```

(Every enum already implements `Comparable` and `Serializable` through `java.lang.Enum`, so you never need to declare those.)

### 9.2 Why can't an enum extend a class?

```java
enum Color extends Base { RED }       // ERROR
```

| Reason | Explanation |
|--------|-------------|
| **Already has a parent** | Every enum already `extends java.lang.Enum`, and Java allows **only one** superclass |
| **Design** | The enum's members (`name`, `ordinal`, `compareTo`...) come from `Enum`. A different parent would break them |

### 9.3 Why can't you extend an enum?

```java
enum Color { RED, GREEN }
class Fancy extends Color { }         // ERROR: cannot inherit from final Color
```

If anyone could add subclasses, the set would no longer be **fixed**. That would destroy the whole point.

### 9.4 "Extending" an enum with an interface (the workaround)

If you need an **extensible** set of options, define an **interface** and let several enums implement it:

```java
interface Operator {
    int apply(int a, int b);
}

enum BasicOp implements Operator {
    ADD { public int apply(int a, int b) { return a + b; } },
    SUB { public int apply(int a, int b) { return a - b; } }
}

enum ExtraOp implements Operator {
    POW { public int apply(int a, int b) { return (int) Math.pow(a, b); } },
    MOD { public int apply(int a, int b) { return a % b; } }
}

static void run(Operator op, int a, int b) {
    System.out.println(op + ": " + op.apply(a, b));
}

run(BasicOp.ADD, 7, 3);     // ADD: 10
run(ExtraOp.POW, 2, 5);     // POW: 32
run(ExtraOp.MOD, 7, 3);     // MOD: 1
```

The method accepts **any** `Operator`, whether it comes from `BasicOp`, `ExtraOp`, or an enum you add in a future library.

---

## 10. Built-in and Compiler-Generated Methods

This covers section V of your outline.

### 10.1 The full list

| Method | From | What it does |
|--------|------|--------------|
| `static E[] values()` | **Compiler-generated** | Array of all constants, in declaration order |
| `static E valueOf(String)` | **Compiler-generated** | Name → constant |
| `String name()` | `Enum` (final) | The constant's exact declared name |
| `int ordinal()` | `Enum` (final) | Position, starting at 0 |
| `String toString()` | `Enum` (overridable) | Default: the same as `name()` |
| `int compareTo(E other)` | `Enum` (final) | Compares by ordinal |
| `boolean equals(Object)` | `Enum` (final) | `this == obj` |
| `int hashCode()` | `Enum` (final) | Identity-based |
| `Class<E> getDeclaringClass()` | `Enum` (final) | The enum class (even for constants with bodies) |
| `static T valueOf(Class<T>, String)` | `Enum` | Generic version of `valueOf` |
| `Optional<...> describeConstable()` | `Enum` (Java 12+) | Supports the constants API |

We'll use this enum for the examples:

```java
public enum Day { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }
```

### 10.2 `values()`: loop over everything

```java
Day[] all = Day.values();
System.out.println(all.length);          // 7
System.out.println(all[0]);              // MONDAY

for (Day d : Day.values()) {
    System.out.println(d);
}
```

Other ways to use it:

```java
import java.util.Arrays;
import java.util.stream.Stream;

System.out.println(Arrays.toString(Day.values()));
Stream.of(Day.values()).filter(d -> d.name().startsWith("T")).forEach(System.out::println);   // TUESDAY, THURSDAY
```

(It returns a **new array each time**; see section 6.4.)

### 10.3 `valueOf(String)`: text → constant

```java
Day d = Day.valueOf("FRIDAY");           // Day.FRIDAY
```

It is **strict**:

| Input | Result |
|-------|--------|
| `"FRIDAY"` | ✓ `Day.FRIDAY` |
| `"friday"` | ✗ `IllegalArgumentException: No enum constant Day.friday` (**case-sensitive**) |
| `" FRIDAY"` (with space) | ✗ `IllegalArgumentException` |
| `"FUNDAY"` | ✗ `IllegalArgumentException` |
| `null` | ✗ `NullPointerException: Name is null` |

**Safe parsing patterns:**

```java
// 1) Loop (also lets you ignore case)
static Day parseDay(String text) {
    for (Day d : Day.values()) {
        if (d.name().equalsIgnoreCase(text)) {
            return d;
        }
    }
    return null;                                 // or throw, or return Optional
}

// 2) A generic Optional-returning helper that works for ANY enum
static <E extends Enum<E>> java.util.Optional<E> tryParse(Class<E> type, String text) {
    try {
        return java.util.Optional.of(Enum.valueOf(type, text.trim().toUpperCase()));
    } catch (IllegalArgumentException | NullPointerException e) {
        return java.util.Optional.empty();
    }
}

System.out.println(tryParse(Day.class, " friday "));    // Optional[FRIDAY]
System.out.println(tryParse(Day.class, "funday"));      // Optional.empty
System.out.println(tryParse(Day.class, null));          // Optional.empty
```

### 10.4 `name()` and `ordinal()`

```java
Day d = Day.WEDNESDAY;
System.out.println(d.name());       // WEDNESDAY
System.out.println(d.ordinal());    // 2      (MONDAY=0, TUESDAY=1, WEDNESDAY=2 ...)
```

**Two important warnings:**

**Warning 1: `ordinal()` is fragile. Don't store it or use it as the meaning.** It changes if someone reorders or inserts constants.

```java
// Version 1:  NEW(0), PAID(1), SHIPPED(2)         → you saved "1" in a database meaning PAID

// Version 2: someone adds a constant in the middle:
//            NEW(0), PENDING(1), PAID(2), SHIPPED(3)
//            → your saved "1" now means PENDING!  DATA CORRUPTED SILENTLY
```

Instead, give each constant an **explicit code** field (section 7.5) or store the **`name()`**.

**Warning 2: `name()` is also a contract.** If you rename a constant, saved names and `valueOf` calls break. Pick names carefully.

Official guidance: `ordinal()` is designed for the infrastructure (like `EnumSet` and `EnumMap`); most programmers should not use it directly.

### 10.5 `toString()` vs `name()`

`toString()` returns `name()` by default, but you may **override `toString()`** for pretty display. `name()` can't be overridden (`final`), and `valueOf` always uses `name()`.

```java
public enum Planet {
    EARTH, MARS;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();    // "Earth"
    }
}

System.out.println(Planet.EARTH);              // Earth        (toString)
System.out.println(Planet.EARTH.name());       // EARTH        (name)
System.out.println(Planet.valueOf("EARTH"));   // Earth        (works: valueOf uses the name)
Planet.valueOf("Earth");                       // IllegalArgumentException (name is EARTH, not Earth)
```

**Rule:** use `name()` for **stable identifiers** (storage, parsing); use `toString()` (or a separate `getDisplayName()` field) for **human display**.

### 10.6 `compareTo()` and natural order

Enums are `Comparable`; the natural order is the **declaration order** (ordinal order):

```java
System.out.println(Day.MONDAY.compareTo(Day.FRIDAY));     // -4   (0 - 4)
System.out.println(Day.FRIDAY.compareTo(Day.MONDAY));     //  4
System.out.println(Day.FRIDAY.compareTo(Day.FRIDAY));     //  0

Day[] days = { Day.SUNDAY, Day.MONDAY, Day.FRIDAY };
Arrays.sort(days);
System.out.println(Arrays.toString(days));                // [MONDAY, FRIDAY, SUNDAY]
```

Comparing two **different** enum types doesn't compile. A `TreeMap<Day, ...>` or `TreeSet<Day>` is sorted in declaration order.

For a custom order, use a `Comparator`:

```java
days = Day.values();
Arrays.sort(days, Comparator.comparing(Day::name));     // alphabetical by name
```

### 10.7 `==` vs `equals()`

Both work, because `equals()` is just `==`. **Prefer `==`:**

| Reason | Example |
|--------|---------|
| **Null-safe** | `day == Day.MONDAY` never throws. `day.equals(Day.MONDAY)` throws if `day` is `null` |
| **Compile-time type check** | `Day.MONDAY == Color.RED` is a **compile error**, while `.equals` happily returns `false` |
| **Same or better speed** | A reference comparison |

```java
Day day = null;
System.out.println(day == Day.MONDAY);        // false
System.out.println(day.equals(Day.MONDAY));   // NullPointerException
```

### 10.8 `getDeclaringClass()`

```java
System.out.println(Day.MONDAY.getDeclaringClass());       // class Day
```

It's mainly useful for constants **with bodies**, whose `getClass()` is a hidden subclass (section 6.8).

### 10.9 The `Class` helpers

```java
Day.class.isEnum();                          // true
Day.class.getEnumConstants();                // Day[] (same constants; reflection-friendly)
Enum.valueOf(Day.class, "FRIDAY");           // Day.FRIDAY
```

---

## 11. Enums and `switch`

### 11.1 The classic `switch` statement

```java
Day day = Day.SATURDAY;

switch (day) {
    case SATURDAY:
    case SUNDAY:
        System.out.println("Weekend!");
        break;
    case FRIDAY:
        System.out.println("Almost there");
        break;
    default:
        System.out.println("Weekday");
}
```

Rules for the **classic** form (before Java 21):

- Case labels use the **unqualified** name: `case SATURDAY:` (not `Day.SATURDAY`).
- Don't forget **`break`**, or execution falls through to the next case.
- A `switch` on a `null` enum throws **`NullPointerException`**.

### 11.2 The `switch` expression with arrows (Java 14+)

It is shorter, has **no fall-through**, and **returns a value**:

```java
String type = switch (day) {
    case SATURDAY, SUNDAY -> "Weekend";
    case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
};
```

**Exhaustiveness:** a switch **expression** must cover **every constant** (or have `default`). If you list them all, you need **no `default`**. And if you later **add a constant** to the enum, every such switch becomes a **compile error** until you handle it. That is a huge safety net.

```java
enum Day { MONDAY, TUESDAY, ..., SUNDAY, HOLIDAY }   // new constant

String type = switch (day) { ... };                   // ERROR: the switch expression does not cover all possible input values
```

> **Trade-off:** adding `default ->` makes the switch compile again, but **hides** this protection. For enums you control, prefer listing all constants and **no default**.

### 11.3 Multi-line arms with `yield`

```java
int hours = switch (day) {
    case SATURDAY, SUNDAY -> 0;
    case FRIDAY -> {
        int base = 6;
        yield base;                    // 'yield' returns the value of a block arm
    }
    default -> 8;
};
```

### 11.4 Java 21: qualified names, `case null`, patterns

```java
// qualified constant names are allowed in case labels (Java 21+)
switch (day) {
    case Day.SATURDAY, Day.SUNDAY -> System.out.println("Weekend");
    default -> System.out.println("Weekday");
}

// null can be handled in the switch itself (Java 21+)
String s = switch (day) {
    case null -> "no day given";
    case SATURDAY, SUNDAY -> "weekend";
    default -> "weekday";
};
```

### 11.5 `switch (this)` inside the enum

```java
public enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    public boolean isWeekend() {
        return switch (this) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }
}
```

---

## 12. `EnumMap` and `EnumSet`

### 12.1 First principles

**Problem:** you often need a **map keyed by enum** (`Day → plan`) or a **set of enum values** (`{SATURDAY, SUNDAY}`). A `HashMap`/`HashSet` works, but it hashes objects and wastes memory.

**Idea:** enum constants already have a **small, fixed, consecutive number** (`ordinal`: 0, 1, 2...). We can use it as an **array index** or a **bit position**. That's both **faster** and **smaller**.

```
 HashMap<Day,String>:   hash the key → find bucket → compare with equals
 EnumMap<Day,String>:   index = day.ordinal() → array[index]       (no hashing at all)
```

### 12.2 `EnumMap`

```java
import java.util.EnumMap;
import java.util.Map;

Map<Day, String> plan = new EnumMap<>(Day.class);     // you pass the enum's Class object

plan.put(Day.FRIDAY, "Party");
plan.put(Day.MONDAY, "Work");
plan.put(Day.WEDNESDAY, "Gym");

System.out.println(plan);               // {MONDAY=Work, WEDNESDAY=Gym, FRIDAY=Party}
System.out.println(plan.get(Day.FRIDAY));   // Party
System.out.println(plan.containsKey(Day.SUNDAY));   // false
```

Notice: iteration order is **declaration order of the enum** (by ordinal), **not** insertion order.

**Internally:**

```
 EnumMap<Day,String>  =  an array with one slot per constant

  index:   0          1         2           3        4         5          6
        ┌────────┬─────────┬─────────┬──────────┬─────────┬─────────┬──────────┐
        │ "Work" │  null   │  "Gym"  │   null   │ "Party" │  null   │   null   │
        └────────┴─────────┴─────────┴──────────┴─────────┴─────────┴──────────┘
          MON       TUE       WED        THU       FRI        SAT       SUN
```

**Counting example:**

```java
Map<Day, Integer> counts = new EnumMap<>(Day.class);
Day[] events = { Day.MONDAY, Day.FRIDAY, Day.MONDAY, Day.SUNDAY, Day.MONDAY };
for (Day d : events) {
    counts.merge(d, 1, Integer::sum);
}
System.out.println(counts);       // {MONDAY=3, FRIDAY=1, SUNDAY=1}
```

Rules: keys must be from **one enum type**; `null` keys are **not allowed**; it's **not thread-safe**.

### 12.3 `EnumSet`

An `EnumSet` stores membership as **bits** (a `long` for enums with up to 64 constants).

```java
import java.util.EnumSet;

EnumSet<Day> weekend  = EnumSet.of(Day.SATURDAY, Day.SUNDAY);
EnumSet<Day> weekdays = EnumSet.complementOf(weekend);
EnumSet<Day> midweek  = EnumSet.range(Day.TUESDAY, Day.THURSDAY);
EnumSet<Day> all      = EnumSet.allOf(Day.class);
EnumSet<Day> none     = EnumSet.noneOf(Day.class);

System.out.println(weekend);     // [SATURDAY, SUNDAY]
System.out.println(weekdays);    // [MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY]
System.out.println(midweek);     // [TUESDAY, WEDNESDAY, THURSDAY]
System.out.println(all.size());  // 7
System.out.println(none);        // []

weekend.contains(Day.SUNDAY);    // true
none.add(Day.MONDAY);            // now [MONDAY]
```

```
 weekend = {SATURDAY, SUNDAY}     as bits (one bit per constant, in ordinal order)

   ordinal:   6  5  4  3  2  1  0
   constant: SUN SAT FRI THU WED TUE MON
   bits:      1  1  0  0  0  0  0    =  0b1100000  =  96     (stored in ONE long)
```

| Factory method | Meaning |
|----------------|---------|
| `EnumSet.of(a, b, ...)` | Set with the given constants |
| `EnumSet.noneOf(X.class)` | Empty set |
| `EnumSet.allOf(X.class)` | Every constant |
| `EnumSet.range(from, to)` | Constants from `from` to `to`, inclusive |
| `EnumSet.complementOf(set)` | Everything **not** in `set` |
| `EnumSet.copyOf(collection)` | Copy of another collection |

### 12.4 `EnumSet` replaces bit flags

Old style (error-prone):

```java
final int READ = 1, WRITE = 2, EXECUTE = 4;
int perms = READ | WRITE;                    // plain int: no type safety
```

New style (type-safe, readable, same speed):

```java
enum Permission { READ, WRITE, EXECUTE, DELETE }

EnumSet<Permission> perms = EnumSet.of(Permission.READ, Permission.WRITE);
perms.add(Permission.DELETE);
perms.remove(Permission.WRITE);
System.out.println(perms.contains(Permission.READ));   // true
System.out.println(perms);                              // [READ, DELETE]
```

### 12.5 When to use which

| You need | Use |
|----------|-----|
| Map with enum keys | `EnumMap` |
| Set of enum values / flags | `EnumSet` |
| Keys are **not** enums | `HashMap` / `HashSet` |

---

## 13. Where Can You Declare an Enum? (and Modifier Rules)

### 13.1 Four places

**1) Top-level, in its own file:**

```java
// Day.java
public enum Day { MONDAY, TUESDAY }
```

**2) Nested inside a class (implicitly `static`):**

```java
public class Order {
    public enum Status { NEW, PAID, SHIPPED }       // used as Order.Status

    private Status status = Status.NEW;
}

Order.Status s = Order.Status.PAID;
```

A nested enum is **always static**: you never need an `Order` object to use it. (Writing `static` is allowed but redundant.)

**3) Inside an interface (implicitly `public static`):**

```java
interface Vehicle {
    enum Type { CAR, BIKE }
}
```

**4) Local enum, inside a method (Java 16+):**

```java
void report() {
    enum Mode { FAST, SLOW }                // visible only inside this method
    for (Mode m : Mode.values()) {
        System.out.println(m);
    }
}
```

A local enum is implicitly static, so it can't use the method's local variables.

### 13.2 Modifier rules

| Modifier | Allowed? |
|----------|----------|
| `public` / package-private (top-level) | ✓ |
| `public`, `protected`, `private`, package-private (nested) | ✓ |
| `static` (nested) | ✓ (redundant) |
| `strictfp` | ✓ |
| `abstract` | ✗ (the compiler decides if the enum is abstract) |
| `final` | ✗ (the compiler decides if the enum is final) |
| `sealed` / `non-sealed` | ✗ |

```java
public abstract enum Bad { A }      // ERROR: illegal combination/modifier
public final enum AlsoBad { A }     // ERROR
```

---

## 14. The Enum Singleton, Serialization and Reflection

### 14.1 Singleton with one constant

A **singleton** is a class with **exactly one instance**. An enum with a single constant is the **simplest, safest** way to build one (recommended in *Effective Java*).

```java
public enum AppConfig {
    INSTANCE;                                    // the one and only instance

    private String appName = "MyApp";

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
}
```

```java
AppConfig.INSTANCE.setAppName("Shop");
System.out.println(AppConfig.INSTANCE.getAppName());                // Shop
System.out.println(AppConfig.INSTANCE == AppConfig.INSTANCE);       // true
```

### 14.2 Why it's safe (the postmortem explains it)

| Threat | How a normal singleton fails | How an enum singleton survives |
|--------|------------------------------|--------------------------------|
| **Multiple threads** race to create it | Needs `synchronized` or double-checked locking | The **JVM** creates constants during class initialization, which is thread-safe |
| **Serialization** | Deserializing creates a **second** instance (unless you add `readResolve`) | Enums are serialized **by name only**, and deserialization returns the **existing** constant |
| **Reflection** | `setAccessible(true)` lets you call the private constructor again | `Constructor.newInstance` **refuses** to create enum objects (`IllegalArgumentException: Cannot reflectively create enum objects`) |
| **Cloning** | `clone()` can make a copy | `Enum.clone()` is `final` and throws `CloneNotSupportedException` |

### 14.3 Limits of the enum singleton

- It can't **extend another class** (it already extends `Enum`), though it can implement interfaces.
- The state is global and shared. Make it thread-safe if it's mutable.

### 14.4 Serialization in one picture

```
 Serialize:   Day.MONDAY ──► writes only the NAME "MONDAY" (not the fields, not the object graph)
 Deserialize: "MONDAY"   ──► Day.valueOf("MONDAY") ──► the SAME existing Day.MONDAY object
```

So `==` stays correct after sending an enum across the network or saving it to disk. (And it explains why renaming a constant breaks old saved data.)

---

## 15. Enums You Already Use in the JDK

Enums are everywhere in the standard library. Recognizing them helps you see what they are for.

| Enum | Package | Meaning |
|------|---------|---------|
| `DayOfWeek` | `java.time` | MONDAY ... SUNDAY |
| `Month` | `java.time` | JANUARY ... DECEMBER |
| `ChronoUnit` | `java.time.temporal` | SECONDS, MINUTES, DAYS... |
| `TimeUnit` | `java.util.concurrent` | NANOSECONDS ... DAYS, with conversions |
| `Thread.State` | `java.lang` | NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED |
| `RoundingMode` | `java.math` | HALF_UP, HALF_EVEN, FLOOR, CEILING... |
| `StandardOpenOption` | `java.nio.file` | READ, WRITE, APPEND, CREATE... |
| `RetentionPolicy`, `ElementType` | `java.lang.annotation` | Annotation settings |
| `TextStyle`, `FormatStyle` | `java.time.format` | Formatting options |

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.concurrent.TimeUnit;

DayOfWeek d = DayOfWeek.MONDAY;
System.out.println(d.plus(3));                          // THURSDAY
System.out.println(Month.FEBRUARY.length(false));        // 28   (non-leap year)
System.out.println(TimeUnit.SECONDS.toMillis(5));        // 5000
System.out.println(TimeUnit.HOURS.toMinutes(2));         // 120
System.out.println(Thread.State.valueOf("RUNNABLE"));    // RUNNABLE

BigDecimal x = new BigDecimal("2.5");
System.out.println(x.setScale(0, RoundingMode.HALF_UP));    // 3
System.out.println(x.setScale(0, RoundingMode.HALF_EVEN));  // 2
```

Notice how **`TimeUnit`** and **`DayOfWeek`** have **methods** (`toMillis`, `plus`): they are enums with data and behavior, exactly as in sections 7 and 8.

---

## 16. Use Cases: Where We Use Enums in Real Software

### 16.1 The big map of use cases

| Category | Examples |
|----------|----------|
| **Status / state** | Order status, payment status, ticket status, job state, connection state |
| **Types / categories** | Account type, vehicle type, product category, file type, message type |
| **Roles and permissions** | USER / ADMIN / GUEST; READ / WRITE / DELETE |
| **Modes and options** | Environment (DEV / TEST / PROD), log level, sort order (ASC / DESC), theme (LIGHT / DARK) |
| **Fixed domain data** | Days, months, card suits and ranks, planets, compass directions, currencies |
| **State machines** | Traffic light, vending machine, workflow with allowed transitions |
| **Strategies / commands** | Each constant has its own algorithm (sorting strategy, discount rule, math operation) |
| **Units and conversions** | Length, weight, time units, currency |
| **Result and error codes** | HTTP status, API error codes, validation results |
| **Flags** | `EnumSet<Permission>` instead of bit masks |
| **Singletons** | One-instance services |
| **Lookup tables** | Code → meaning mappings |
| **Switch-based logic** | Branching on a fixed set of cases with compiler checking |
| **Configuration** | Feature toggles, plan tiers (FREE / PRO / ENTERPRISE) |
| **Database and JSON mapping** | Store the enum's name or code in a column or JSON field |

### 16.2 Example: status with allowed transitions (state machine)

```java
import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    NEW, PAID, SHIPPED, DELIVERED, CANCELLED;

    public Set<OrderStatus> next() {
        return switch (this) {
            case NEW        -> EnumSet.of(PAID, CANCELLED);
            case PAID       -> EnumSet.of(SHIPPED, CANCELLED);
            case SHIPPED    -> EnumSet.of(DELIVERED);
            case DELIVERED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    public boolean canMoveTo(OrderStatus target) {
        return next().contains(target);
    }
}
```

```
   NEW ───► PAID ───► SHIPPED ───► DELIVERED
    │         │
    └────┬────┘
         ▼
     CANCELLED
```

```java
OrderStatus s = OrderStatus.NEW;
System.out.println(s.canMoveTo(OrderStatus.PAID));        // true
System.out.println(s.canMoveTo(OrderStatus.SHIPPED));     // false  (can't skip payment)
System.out.println(OrderStatus.PAID.next());              // [SHIPPED, CANCELLED]
```

### 16.3 Example: roles and permissions

```java
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

enum Permission { READ, WRITE, DELETE, MANAGE_USERS }

enum Role {
    GUEST(EnumSet.of(Permission.READ)),
    EDITOR(EnumSet.of(Permission.READ, Permission.WRITE)),
    ADMIN(EnumSet.allOf(Permission.class));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    public boolean can(Permission p) {
        return permissions.contains(p);
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}
```

```java
System.out.println(Role.EDITOR.can(Permission.WRITE));     // true
System.out.println(Role.GUEST.can(Permission.DELETE));      // false
System.out.println(Role.ADMIN.getPermissions());            // [READ, WRITE, DELETE, MANAGE_USERS]
```

### 16.4 Example: strategy pattern (discount rules)

```java
import java.util.function.DoubleUnaryOperator;

public enum Discount {
    NONE(price -> price),
    STUDENT(price -> price * 0.90),
    SENIOR(price -> price * 0.80),
    BLACK_FRIDAY(price -> price * 0.50);

    private final DoubleUnaryOperator rule;

    Discount(DoubleUnaryOperator rule) { this.rule = rule; }

    public double apply(double price) { return rule.applyAsDouble(price); }
}

System.out.println(Discount.BLACK_FRIDAY.apply(200));    // 100.0
System.out.println(Discount.NONE.apply(200));            // 200.0
```

A new discount is **one new constant**. No `if/else` chain to edit.

### 16.5 Example: log levels with comparison

```java
public enum LogLevel {
    DEBUG, INFO, WARN, ERROR;

    public boolean isAtLeast(LogLevel other) {
        return this.compareTo(other) >= 0;          // uses the declaration order
    }
}

LogLevel threshold = LogLevel.WARN;
for (LogLevel l : LogLevel.values()) {
    System.out.println(l + " printed? " + l.isAtLeast(threshold));
}
```

Output:
```
DEBUG printed? false
INFO printed? false
WARN printed? true
ERROR printed? true
```

### 16.6 Example: a deck of cards (two enums combine)

```java
enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

enum Rank {
    TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
}

record Card(Rank rank, Suit suit) { }

java.util.List<Card> deck = new java.util.ArrayList<>();
for (Suit s : Suit.values()) {
    for (Rank r : Rank.values()) {
        deck.add(new Card(r, s));
    }
}
System.out.println(deck.size());        // 52
System.out.println(deck.get(0));        // Card[rank=TWO, suit=CLUBS]
```

### 16.7 Example: configuration by environment

```java
public enum Environment {
    DEV("http://localhost:8080", true),
    TEST("https://test.example.com", true),
    PROD("https://example.com", false);

    private final String baseUrl;
    private final boolean debug;

    Environment(String baseUrl, boolean debug) {
        this.baseUrl = baseUrl;
        this.debug = debug;
    }

    public String getBaseUrl() { return baseUrl; }
    public boolean isDebug()   { return debug; }
}

Environment env = Environment.valueOf(System.getProperty("env", "DEV"));
System.out.println(env.getBaseUrl());
```

### 16.8 Storing enums in databases and JSON

| Storage choice | Pros | Cons |
|----------------|------|------|
| **Name** (`"PAID"`) | Readable, safe against reordering | Renaming a constant breaks old data; slightly bigger |
| **Explicit code** (`2`) | Compact, you control it, safe to rename | Needs a lookup method (section 7.5) |
| **`ordinal()`** | Compact | **Dangerous**: reordering or inserting breaks all stored data |

Many frameworks default to ordinal in some configurations (for example, JPA's `@Enumerated(EnumType.ORDINAL)`). **Choose name or code instead.**

---

## 17. Version-by-Version Improvements

### 17.1 Timeline

```
 Before 2004      2004        2014        2019        2020        2021         2023        2024+
 ──────────────────┬───────────┬───────────┬───────────┬───────────┬────────────┬───────────┬──────►
  int constants     Java 5     Java 8      Java 12     Java 14     Java 16/17   Java 21     Java 22+
  type-safe class   ENUMS      lambdas     Constable   switch      local enums  pattern     unnamed
                    born       java.time               expressions sealed       switch      variables
```

### 17.2 Detailed table

| Version | Year | What changed for enums |
|---------|------|------------------------|
| **Java 1.4 and older** | before 2004 | **No enums.** Developers used `int`/`String` constants or the hand-written **type-safe enum pattern** (section 2.5). |
| **Java 5** | 2004 | **Enums are introduced** (JSR 201), together with generics, annotations and autoboxing. Added: the `enum` keyword, the `java.lang.Enum` base class, `values()` and `valueOf()`, fields/constructors/methods, **constant-specific class bodies**, **`switch` on enums**, `EnumMap` and `EnumSet`. |
| **Java 6 and 7** | 2006, 2011 | No major enum language changes. (Java 7 added `switch` on `String`, which reduced one reason to use string constants, but enums remain safer.) |
| **Java 8** | 2014 | **Lambdas and functional interfaces** make enums with behavior fields concise (section 8.5). **Streams** make `Stream.of(Enum.values())` pipelines easy. The new **`java.time`** API is built on enums (`DayOfWeek`, `Month`, `ChronoUnit`). |
| **Java 9 to 11** | 2017 to 2018 | No enum-specific language changes. |
| **Java 12** | 2019 | `Enum` implements **`Constable`** and gains **`describeConstable()`** (constants API, JEP 334). **Switch expressions** arrive as a preview. |
| **Java 14** | 2020 | **Switch expressions are standard** (arrow `->`, `yield`). Switching over an enum without `default` is checked for **exhaustiveness**: forgetting a constant is a compile error. |
| **Java 16** | 2021 | **Local enums** (and local interfaces and records) can be declared inside methods. **Records** arrive (a perfect partner for enums, e.g. `record Card(Rank, Suit)`). |
| **Java 17 (LTS)** | 2021 | **Sealed classes** become standard. The language specification treats an enum whose constants have bodies as **implicitly sealed** (otherwise it's implicitly final). |
| **Java 21 (LTS)** | 2023 | **Pattern matching for `switch`** is final: you may use **qualified enum names** in `case` labels (`case Day.SATURDAY`), handle **`case null`**, and combine enum constants with patterns and guards. Exhaustiveness checking extends to sealed hierarchies. |
| **Java 22 and later** | 2024+ | **Unnamed variables** (`_`) are final: `for (Day _ : Day.values()) count++;` when you don't need the loop variable. Further switch/pattern refinements continue. |

### 17.3 Code that shows the evolution

**The same task ("is it a weekend?") across versions:**

```java
// Before Java 5: int constants
public static final int SATURDAY = 5;
public static final int SUNDAY = 6;
static boolean isWeekend(int day) {
    return day == SATURDAY || day == SUNDAY;      // isWeekend(42) compiles!
}

// Java 5: enum with classic switch
static boolean isWeekend(Day day) {
    switch (day) {
        case SATURDAY:
        case SUNDAY:
            return true;
        default:
            return false;
    }
}

// Java 8: stream with an enum predicate
long weekendDays = java.util.Arrays.stream(Day.values())
                                   .filter(d -> d == Day.SATURDAY || d == Day.SUNDAY)
                                   .count();

// Java 14: switch expression, exhaustive, no break, no default
static boolean isWeekend(Day day) {
    return switch (day) {
        case SATURDAY, SUNDAY -> true;
        case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> false;
    };
}

// Java 21: pattern-matching switch with null handling
static String describe(Day day) {
    return switch (day) {
        case null -> "no day";
        case SATURDAY, SUNDAY -> "weekend";
        default -> "weekday";
    };
}
```

---

## 18. Best Practices and When NOT to Use Enums

This covers section VI of your outline and adds more.

### 18.1 Best practices

1. **Use enums for fixed, limited sets of related constants** (status, days, log levels, roles).
2. **Treat enums as classes.** Put data and logic related to each state **inside** the enum (fields, methods, abstract methods).
3. **Name the type in `PascalCase`, singular** (`OrderStatus`), and the constants in **`UPPER_SNAKE_CASE`** (`NOT_FOUND`).
4. **Make fields `private final`.** Immutable constants are safe to share.
5. **Compare with `==`.**
6. **Never persist `ordinal()`.** Store the `name()` or an explicit code.
7. **Don't depend on declaration order** unless the order is the meaning (and say so in a comment).
8. **Prefer switch expressions without `default`** so the compiler warns you when a constant is added.
9. **Use `EnumMap` / `EnumSet`** instead of `HashMap` / bit flags for enum keys.
10. **Implement an interface** when different enums must be used interchangeably, since an enum can't extend a class.
11. **Provide a safe lookup** (`fromCode`) instead of calling `valueOf` on user input.
12. **Keep enums focused.** If an enum grows huge or complex, consider a class hierarchy.
13. **Cache `values()`** in hot paths (it clones the array).
14. **Be careful with `null`.** An enum variable can still be `null`. Validate parameters.
15. **Separate display text from the identifier:** use a `displayName` field, not a changed `toString()` that you also parse.

### 18.2 When NOT to use an enum

| Situation | Better choice |
|-----------|---------------|
| The values **change at runtime** or come from a **database/config** (e.g. product categories added by admins) | A normal class + a table |
| The set is **very large** (hundreds of values) | A class or lookup data |
| You need **many instances with different data** | A normal class |
| You need to **add values from other modules** without changing the enum | An **interface** with several implementations |
| Just a **boolean** choice | `boolean` |
| A single numeric constant (a limit, a size) | `static final` constant |

---

## 19. Common Mistakes and Pitfalls

| # | Mistake | What happens | Fix |
|---|---------|--------------|-----|
| 1 | Forgetting the **semicolon** after the constants when adding members | Compile error | Add `;` after the last constant |
| 2 | Writing members **before** the constants | Compile error | Constants first |
| 3 | Marking the enum constructor `public` | Compile error | Leave it package-private/`private` |
| 4 | `new Day()` | "enum types may not be instantiated" | Use the constants |
| 5 | `extends` on an enum, or extending an enum | Compile error | Use an interface |
| 6 | Constructor touching a non-constant `static` field | "illegal reference to static field from initializer" | Use a `static { }` block after the constants |
| 7 | `Day.valueOf("friday")` | `IllegalArgumentException` | Normalize case or use a safe parser |
| 8 | `Day.valueOf(null)` | `NullPointerException` | Check for `null` |
| 9 | Storing `ordinal()` in a database | Silent corruption after reordering | Store `name()` or an explicit code |
| 10 | `day.equals(Day.MONDAY)` when `day` may be `null` | `NullPointerException` | Use `day == Day.MONDAY` |
| 11 | Classic `switch` with `Day.MONDAY` in a case label (before Java 21) | Compile error | Use the unqualified `MONDAY` |
| 12 | Forgetting `break` in a classic `switch` | Fall-through bugs | Use arrow syntax |
| 13 | `default ->` hiding new constants | New constant silently handled wrongly | Omit `default` in switch expressions |
| 14 | Calling `values()` repeatedly in a hot loop | Allocates a new array each time | Cache it in a `static final` field |
| 15 | Mutable fields in an enum | Shared global state | Use `private final` |
| 16 | Using `getClass()` on a constant with a body | Gets `Op$1`, not `Op` | Use `getDeclaringClass()` |
| 17 | Overriding `toString()` and then parsing it with `valueOf` | `IllegalArgumentException` | `valueOf` uses `name()`; add a separate parser |
| 18 | Renaming a constant after data was saved | Old data fails to load | Treat names as a public contract |
| 19 | Putting each constant's complex logic in a giant `switch` elsewhere | Scattered logic | Move it into the enum |
| 20 | Using `==` on `Integer` codes inside the enum | Wrapper identity bug | Use primitives or `equals` |
| 21 | Expecting `EnumMap` to accept `null` keys | `NullPointerException` | Don't use `null` keys |
| 22 | Enum with 2 constants meaning true/false | Over-engineering | Use `boolean` |

---

## 20. Practical Programs

Save each program under the shown file name, compile with `javac FileName.java`, run with `java ClassName`.

### 20.1 `DayDemo`: constants, switch, loop, compare

```java
public class DayDemo {

    enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

        boolean isWeekend() {
            return this == SATURDAY || this == SUNDAY;
        }
    }

    public static void main(String[] args) {
        for (Day d : Day.values()) {
            String kind = switch (d) {
                case SATURDAY, SUNDAY -> "weekend";
                default -> "weekday";
            };
            System.out.println(d.ordinal() + " " + d + " -> " + kind);
        }

        System.out.println(Day.valueOf("FRIDAY").isWeekend());          // false
        System.out.println(Day.MONDAY.compareTo(Day.FRIDAY));           // -4
        System.out.println(Day.SUNDAY.isWeekend());                     // true
    }
}
```

Output:
```
0 MONDAY -> weekday
1 TUESDAY -> weekday
2 WEDNESDAY -> weekday
3 THURSDAY -> weekday
4 FRIDAY -> weekday
5 SATURDAY -> weekend
6 SUNDAY -> weekend
false
-4
true
```

### 20.2 `DirectionDemo`: state in an enum

```java
public class DirectionDemo {

    enum Direction {
        NORTH(0), EAST(90), SOUTH(180), WEST(270);

        private final int degrees;

        Direction(int degrees) { this.degrees = degrees; }

        int getDegrees() { return degrees; }

        Direction turnRight() { return values()[(ordinal() + 1) % values().length]; }

        Direction turnLeft()  { return values()[(ordinal() + values().length - 1) % values().length]; }

        Direction opposite()  { return values()[(ordinal() + 2) % values().length]; }
    }

    public static void main(String[] args) {
        Direction d = Direction.NORTH;
        System.out.println(d + " = " + d.getDegrees() + " degrees");     // NORTH = 0 degrees

        for (int i = 0; i < 5; i++) {
            d = d.turnRight();
            System.out.println("turn right -> " + d + " (" + d.getDegrees() + ")");
        }

        System.out.println("opposite of EAST: " + Direction.EAST.opposite());
        System.out.println("left of NORTH: " + Direction.NORTH.turnLeft());
    }
}
```

Output:
```
NORTH = 0 degrees
turn right -> EAST (90)
turn right -> SOUTH (180)
turn right -> WEST (270)
turn right -> NORTH (0)
turn right -> EAST (90)
opposite of EAST: WEST
left of NORTH: WEST
```

### 20.3 `OrderDemo`: codes, reverse lookup, transitions, `EnumMap`

```java
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OrderDemo {

    enum OrderStatus {
        NEW(1), PAID(2), SHIPPED(3), DELIVERED(4), CANCELLED(5);

        private final int code;
        private static final Map<Integer, OrderStatus> BY_CODE = new HashMap<>();

        static {
            for (OrderStatus s : values()) {
                BY_CODE.put(s.code, s);
            }
        }

        OrderStatus(int code) { this.code = code; }

        int getCode() { return code; }

        static OrderStatus fromCode(int code) {
            OrderStatus s = BY_CODE.get(code);
            if (s == null) throw new IllegalArgumentException("Unknown code: " + code);
            return s;
        }

        Set<OrderStatus> next() {
            return switch (this) {
                case NEW        -> EnumSet.of(PAID, CANCELLED);
                case PAID       -> EnumSet.of(SHIPPED, CANCELLED);
                case SHIPPED    -> EnumSet.of(DELIVERED);
                case DELIVERED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
            };
        }

        boolean canMoveTo(OrderStatus target) { return next().contains(target); }
    }

    public static void main(String[] args) {
        OrderStatus s = OrderStatus.NEW;
        System.out.println(s + " code=" + s.getCode());                       // NEW code=1
        System.out.println(s.canMoveTo(OrderStatus.PAID));                    // true
        System.out.println(s.canMoveTo(OrderStatus.SHIPPED));                 // false
        System.out.println(OrderStatus.fromCode(3));                          // SHIPPED
        System.out.println(OrderStatus.PAID.next());                          // [SHIPPED, CANCELLED]

        Map<OrderStatus, Integer> counts = new EnumMap<>(OrderStatus.class);
        OrderStatus[] orders = {
            OrderStatus.NEW, OrderStatus.PAID, OrderStatus.NEW,
            OrderStatus.SHIPPED, OrderStatus.NEW, OrderStatus.PAID
        };
        for (OrderStatus o : orders) {
            counts.merge(o, 1, Integer::sum);
        }
        System.out.println(counts);                                           // {NEW=3, PAID=2, SHIPPED=1}

        try {
            OrderStatus.fromCode(99);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());                   // Error: Unknown code: 99
        }
    }
}
```

Output:
```
NEW code=1
true
false
SHIPPED
[SHIPPED, CANCELLED]
{NEW=3, PAID=2, SHIPPED=1}
Error: Unknown code: 99
```

### 20.4 `CalculatorDemo`: abstract methods per constant + a state machine

```java
public class CalculatorDemo {

    enum Operation {
        ADD("+") {
            @Override public double apply(double a, double b) { return a + b; }
        },
        SUBTRACT("-") {
            @Override public double apply(double a, double b) { return a - b; }
        },
        MULTIPLY("*") {
            @Override public double apply(double a, double b) { return a * b; }
        },
        DIVIDE("/") {
            @Override public double apply(double a, double b) {
                if (b == 0) throw new ArithmeticException("Division by zero");
                return a / b;
            }
        };

        private final String symbol;

        Operation(String symbol) { this.symbol = symbol; }

        String getSymbol() { return symbol; }

        public abstract double apply(double a, double b);
    }

    enum TrafficLight {
        RED(30)   { @Override TrafficLight next() { return GREEN; } },
        GREEN(25) { @Override TrafficLight next() { return YELLOW; } },
        YELLOW(5) { @Override TrafficLight next() { return RED; } };

        private final int seconds;

        TrafficLight(int seconds) { this.seconds = seconds; }

        int getSeconds() { return seconds; }

        abstract TrafficLight next();
    }

    public static void main(String[] args) {
        for (Operation op : Operation.values()) {
            System.out.println("12 " + op.getSymbol() + " 4 = " + op.apply(12, 4));
        }

        try {
            Operation.DIVIDE.apply(1, 0);
        } catch (ArithmeticException e) {
            System.out.println("Error: " + e.getMessage());
        }

        TrafficLight light = TrafficLight.RED;
        for (int i = 0; i < 5; i++) {
            System.out.println(light + " for " + light.getSeconds() + "s");
            light = light.next();
        }
    }
}
```

Output:
```
12 + 4 = 16.0
12 - 4 = 8.0
12 * 4 = 48.0
12 / 4 = 3.0
Error: Division by zero
RED for 30s
GREEN for 25s
YELLOW for 5s
RED for 30s
GREEN for 25s
```

### 20.5 `PostmortemDemo`: inspect an enum at runtime

This program prints facts that prove what section 6 described.

```java
import java.lang.reflect.Modifier;
import java.util.Arrays;

enum Color { RED, GREEN }

enum Operation {
    PLUS { int apply(int a, int b) { return a + b; } };
    abstract int apply(int a, int b);
}

public class PostmortemDemo {
    public static void main(String[] args) {
        Color c = Color.RED;
        System.out.println("getClass()          : " + c.getClass());
        System.out.println("superclass          : " + c.getClass().getSuperclass());
        System.out.println("isEnum()            : " + c.getClass().isEnum());
        System.out.println("is final?           : " + Modifier.isFinal(Color.class.getModifiers()));
        System.out.println("enum constants      : " + Arrays.toString(Color.class.getEnumConstants()));
        System.out.println("name/ordinal        : " + c.name() + "/" + c.ordinal());
        System.out.println("declaring class     : " + c.getDeclaringClass());

        Operation p = Operation.PLUS;
        System.out.println("PLUS.getClass()     : " + p.getClass());
        System.out.println("PLUS superclass     : " + p.getClass().getSuperclass());
        System.out.println("PLUS class isEnum   : " + p.getClass().isEnum());
        System.out.println("PLUS declaring class: " + p.getDeclaringClass());
        System.out.println("Operation isEnum    : " + Operation.class.isEnum());
        System.out.println("Operation final?    : " + Modifier.isFinal(Operation.class.getModifiers()));

        System.out.println("values() same array?: " + (Color.values() == Color.values()));
        System.out.println("same constant?      : " + (Color.values()[0] == Color.values()[0]));
    }
}
```

Output:
```
getClass()          : class Color
superclass          : class java.lang.Enum
isEnum()            : true
is final?           : true
enum constants      : [RED, GREEN]
name/ordinal        : RED/0
declaring class     : class Color
PLUS.getClass()     : class Operation$1
PLUS superclass     : class Operation
PLUS class isEnum   : false
PLUS declaring class: class Operation
Operation isEnum    : true
Operation final?    : false
values() same array?: false
same constant?      : true
```

What it proves:

- A plain enum **extends `java.lang.Enum`** and is **final**.
- A constant with a **body** lives in a **hidden subclass** (`Operation$1`) that is **not** an enum class itself, so `getDeclaringClass()` is the right way to get the enum.
- An enum with such constants is **not final**.
- `values()` returns **a new array** every time, but the **objects inside are the same**.

### 20.6 `RoleDemo`: roles, permissions and `EnumSet`

```java
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class RoleDemo {

    enum Permission { READ, WRITE, DELETE, MANAGE_USERS }

    enum Role {
        GUEST(EnumSet.of(Permission.READ)),
        EDITOR(EnumSet.of(Permission.READ, Permission.WRITE)),
        ADMIN(EnumSet.allOf(Permission.class));

        private final Set<Permission> permissions;

        Role(Set<Permission> permissions) {
            this.permissions = Collections.unmodifiableSet(permissions);
        }

        boolean can(Permission p) { return permissions.contains(p); }

        Set<Permission> getPermissions() { return permissions; }
    }

    public static void main(String[] args) {
        for (Role r : Role.values()) {
            System.out.println(r + " -> " + r.getPermissions());
        }
        System.out.println("EDITOR can WRITE : " + Role.EDITOR.can(Permission.WRITE));
        System.out.println("GUEST can DELETE : " + Role.GUEST.can(Permission.DELETE));

        EnumSet<Permission> extra = EnumSet.complementOf(EnumSet.copyOf(Role.EDITOR.getPermissions()));
        System.out.println("EDITOR lacks     : " + extra);
    }
}
```

Output:
```
GUEST -> [READ]
EDITOR -> [READ, WRITE]
ADMIN -> [READ, WRITE, DELETE, MANAGE_USERS]
EDITOR can WRITE : true
GUEST can DELETE : false
EDITOR lacks     : [DELETE, MANAGE_USERS]
```

### 20.7 Try it yourself

1. Create `Season { SPRING, SUMMER, AUTUMN, WINTER }` with a `getAverageTemp()` field and a `next()` method that wraps around.
2. Write `PizzaSize { SMALL, MEDIUM, LARGE }` with a `diameterCm` field and a `price()` method that is **abstract** and implemented per constant.
3. Reorder the constants of your `OrderStatus` and observe how `ordinal()` values change but `getCode()` stays stable.
4. Add a new constant to an enum used in a **switch expression without default**, and read the compiler error.
5. Write `static <E extends Enum<E>> E parse(Class<E> type, String text)` that ignores case, and test it with three different enums.
6. Replace a set of `boolean` flags in one of your programs with an `EnumSet`.
7. Build an enum singleton `Logger` that stores messages in a list, and prove `Logger.INSTANCE == Logger.INSTANCE`.
8. Run `javap -p` on one of your enums and match each line to section 6.

---

## 21. Cheat Sheet, Summary and Quiz

### 21.1 Syntax cheat sheet

```java
// ---------- basic ----------
enum Level { LOW, MEDIUM, HIGH }

// ---------- with data ----------
enum Direction {
    NORTH(0), EAST(90), SOUTH(180), WEST(270);      // ; required because members follow

    private final int degrees;
    Direction(int degrees) { this.degrees = degrees; }     // implicitly private
    int getDegrees() { return degrees; }
}

// ---------- with abstract behavior ----------
enum Op {
    ADD { double apply(double a, double b) { return a + b; } },
    SUB { double apply(double a, double b) { return a - b; } };
    abstract double apply(double a, double b);
}

// ---------- implementing an interface ----------
enum Mode implements Runnable {
    ON  { public void run() { System.out.println("on"); } },
    OFF { public void run() { System.out.println("off"); } }
}

// ---------- using ----------
Level l = Level.HIGH;
l == Level.HIGH                  // compare with ==
l.name()   l.ordinal()   l.toString()   l.compareTo(Level.LOW)
Level.values()                   // all constants (a fresh array)
Level.valueOf("LOW")             // exact name or IllegalArgumentException
switch (l) { case LOW -> ...; case MEDIUM -> ...; case HIGH -> ...; }

// ---------- collections ----------
EnumMap<Level, String> map = new EnumMap<>(Level.class);
EnumSet<Level> set = EnumSet.of(Level.LOW, Level.HIGH);
EnumSet.allOf(Level.class)   EnumSet.noneOf(Level.class)   EnumSet.range(a, b)   EnumSet.complementOf(set)
```

### 21.2 Rules at a glance

| Rule | Detail |
|------|--------|
| An enum is | A **class** that extends `java.lang.Enum` |
| Constants are | `public static final` **objects** of the enum type |
| Constructor | Always **private**; can't call `new` |
| Can extend a class? | **No** |
| Can implement interfaces? | **Yes**, several |
| Can be extended? | **No** (final, or implicitly sealed with constant bodies) |
| Compare with | `==` |
| `values()` | New array each call |
| `valueOf()` | Exact, case-sensitive name; throws on mismatch |
| `ordinal()` | Position from 0; **don't persist it** |
| `compareTo()` | Declaration order |
| Switch expression over an enum | Exhaustive: no `default` needed if all constants are covered |

### 21.3 Summary in simple points

1. Old constants (`int`, `String`) have **no type safety**, print meaninglessly, allow nonsense values and typos, and carry no data or behavior. `String` comparison is also slower and error-prone.
2. An **`enum`** is a special type for a **fixed set of named constants**. The compiler turns it into a **`final` class extending `java.lang.Enum`**.
3. Each constant is a **`public static final` object** of the enum, created **once** when the class loads. The **constructor is private**, so no other instances can exist. That's why only the defined constants can be assigned to an enum variable.
4. Because an enum is a class, it can have **fields, a constructor and methods**. Pass data to a constant with `EAST(90)`; the semicolon is required after the constants when members follow.
5. You can declare an **abstract method** and make **each constant implement it**, which gives per-constant behavior (polymorphism) and forces the compiler to check you didn't forget one. Those constants become **hidden subclasses**.
6. An enum **can implement interfaces but can't extend classes**, and **can't be extended**. Use interfaces for an extensible set.
7. **`values()`** returns a **clone** of an internal array (so it can't be modified), **`valueOf(String)`** converts a name to a constant (exact match, or an exception), **`name()`** gives the name, **`ordinal()`** gives the index from 0, and **`compareTo()`** uses declaration order.
8. Compare enums with **`==`**. Don't store **`ordinal()`**. Prefer an **explicit code** or the **name**.
9. Enums work in **`switch`** (switch expressions are exhaustive and flag missing constants), and have fast dedicated collections: **`EnumMap`** (array by ordinal) and **`EnumSet`** (bit vector).
10. An enum with a single constant is the **safest singleton** (thread-safe, serialization-safe, reflection-safe).
11. Use enums for **status, types, roles, modes, fixed domain data, state machines, strategies, units, flags and configuration**. Don't use them when the set **changes at runtime**.
12. **Version history:** none before Java 5 → **enums arrive in Java 5** → lambdas and `java.time` in **Java 8** → switch expressions in **Java 14** → local enums and records in **Java 16** → sealed in **Java 17** → pattern-matching switch in **Java 21**.

### 21.4 Mini Quiz

1. Name three problems with using `int` constants for statuses.
2. Why are `String` constants risky? Give two reasons.
3. What does the compiler turn an `enum` into? What class does it extend?
4. What is each enum constant, in terms of Java objects?
5. Can you write `new Day()`? Why or why not?
6. Why does an enum with fields need a semicolon after the last constant?
7. Why is an enum constructor always private?
8. How do you force every constant to have its own behavior?
9. Why can't an enum extend a class? What can it do instead?
10. What does `values()` return, and why is it a clone?
11. What happens with `Day.valueOf("monday")`? With `Day.valueOf(null)`?
12. Why shouldn't you store `ordinal()` in a database?
13. Why is `day == Day.MONDAY` better than `day.equals(Day.MONDAY)`?
14. What's the benefit of a switch expression without `default` over an enum?
15. What are `EnumMap` and `EnumSet` and why are they fast?
16. Why is an enum a good singleton?
17. What is `Operation.PLUS.getClass()` if `PLUS` has a body, and how do you get `Operation` itself?
18. In which Java version were enums introduced? Which version made switch expressions standard?
19. Why does `private static int count; Color() { count++; }` not compile in an enum?
20. Give four real-world uses of enums.

<details>
<summary>Click for answers</summary>

1. Any three: **no type safety** (any `int` accepted), **meaningless printing** (`1` instead of `PAID`), **mixing unrelated groups**, **no list of all values**, **meaningless arithmetic**, **compile-time inlining makes it brittle**, **no data/behavior**.
2. **Typos and case differences** compile fine and fail at runtime; **`==` vs `equals`** confusion; also **slower** comparison and **no type safety**.
3. Into a **class** (final, with the ENUM flag). It extends **`java.lang.Enum<E>`**.
4. A **`public static final` instance (object)** of the enum type, created once.
5. **No.** Enum types may not be instantiated; the constructor is private and only the compiler creates the constants.
6. Because the constants list ends with `;` to tell the compiler that **fields, constructors or methods follow**.
7. To **forbid creating more instances**, so the set of constants stays fixed.
8. Declare an **`abstract` method** in the enum and give **each constant a body** that implements it. The compiler errors if one is missing.
9. It **already extends `java.lang.Enum`** and Java has single inheritance. It can **implement any number of interfaces**.
10. An array of **all constants** in declaration order. It returns a **clone** so callers can't modify the enum's internal array.
11. `valueOf("monday")` throws **`IllegalArgumentException`** (case-sensitive). `valueOf(null)` throws **`NullPointerException`**.
12. It changes when constants are **reordered or inserted**, silently corrupting saved data. Store the **name** or an **explicit code**.
13. `==` is **null-safe** and gives a **compile-time type check**, and it is the same as `equals` for enums.
14. If you add a new constant, the **compiler reports an error** in every such switch until you handle it.
15. **`EnumMap`** is an **array indexed by ordinal**; **`EnumSet`** is a **bit vector**. Both avoid hashing, so they are fast and compact.
16. The JVM creates the constant **once, thread-safely**; **serialization** returns the same instance; **reflection** can't create another; **`clone()`** is blocked.
17. A hidden subclass such as **`Operation$1`**. Use **`getDeclaringClass()`** to get `Operation`.
18. Enums arrived in **Java 5 (2004)**. Switch expressions became standard in **Java 14**.
19. The constants are created **before** the other static fields are initialized, so a constructor can't reference a non-constant static field. Use a **`static { }` block** after the constants.
20. Any four: **order/payment status**, **user roles**, **log levels**, **days/months**, **card suits**, **directions**, **state machines**, **strategies**, **environments**, **units**, **HTTP codes**, **permissions with `EnumSet`**.

</details>

---

*Practice tip: every time you catch yourself writing `int` or `String` constants, or a `String` parameter that can only be one of a few values, stop and ask: "Is this a fixed set?" If yes, make it an enum, then add the data and behavior that belongs with each constant. After a few times, enums will feel like the most natural way to write that code.*
