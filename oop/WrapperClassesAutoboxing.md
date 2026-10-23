# Java File Rules · Wrapper Classes · Autoboxing · Integer Cache · Abstract Class Q&A · POJO
### Deep explanation with diagrams, examples, and extra interview questions

> **Order of this file:**
> 1. Java file and class structure (why one public class, why the name must match)
> 2. Wrapper classes (what, why, all 8, how they work)
> 3. Autoboxing and unboxing (what the compiler really does, NPE, performance, traps)
> 4. `==` vs `.equals()` with wrappers
> 5. Integer class internals and the Integer cache (with memory diagrams)
> 6. Abstract class interview questions (with reasons, plus many extra)
> 7. POJO, JavaBean, DTO, Entity, Record; anemic vs rich domain model
> 8. Predict-the-output quiz, big interview Q&A, cheat sheet

---

## Table of Contents

1. [Java file and class structure](#1-java-file-and-class-structure)
2. [Wrapper classes](#2-wrapper-classes)
3. [Autoboxing and unboxing](#3-autoboxing-and-unboxing)
4. [Equality: `==` vs `.equals()` with wrappers](#4-equality--vs-equals-with-wrappers)
5. [Integer class internals and the Integer cache](#5-integer-class-internals-and-the-integer-cache)
6. [Abstract class interview questions](#6-abstract-class-interview-questions)
7. [POJO classes](#7-pojo-classes)
8. [Predict the output (quiz)](#8-predict-the-output-quiz)
9. [More interview questions with answers](#9-more-interview-questions-with-answers)
10. [Cheat sheet and common mistakes](#10-cheat-sheet-and-common-mistakes)

---

## 1. Java file and class structure

### 1.1 The rules

A `.java` file is called a **compilation unit**. These are the rules for what it can contain:

| Rule | Detail |
|---|---|
| **At most one `public` top-level class** (or interface/enum/record) per file | You may have zero or one, never two |
| If there **is** a public top-level class, the **file name must match it exactly** (case-sensitive) | `public class Student` must be in `Student.java` |
| You can have **any number of non-public (package-private) classes** in the same file | They get no `public` keyword |
| If there is **no** public class, the file can have **any name** | e.g. `Demo.java` containing `class A`, `class B` |
| `package` statement first, then `import`s, then the classes | Order matters |
| Nested classes don't count | A `public static class Inner` inside a class is fine |

```java
// File: Student.java
public class Student { }        // OK: public, name matches file
class Helper { }                // OK: not public
class Util { }                  // OK: not public

public class Teacher { }        // ERROR: class Teacher is public, should be declared in a file named Teacher.java
```

```java
// File: Demo.java (no public class)
class A { }
class B { }
// OK: no public class, so the filename can be anything
```

### 1.2 What the compiler produces

**One `.class` file per class**, named after the **class**, not after the source file.

```
 Student.java                      Compiler (javac)           Output
+---------------------------+                               +------------------+
| public class Student {}   |  ------------------------->   | Student.class    |
| class Helper {}           |                               | Helper.class     |
| class Util {}             |                               | Util.class       |
+---------------------------+                               +------------------+
```

Nested classes become `Outer$Inner.class`.

### 1.3 Why does Java have this rule?

1. **Finding classes fast and unambiguously.** When your code says `Student s = new Student();`, the compiler and the class loader must locate the `Student` class. By convention it maps the **class name to a file path**: class `com.app.Student` is found at `com/app/Student.java` (or `Student.class`). If a public class could hide inside any file, the tools would have to open and scan **every file** to find it.

```
 Need class: com.app.Student
        |
        v
 look for  com/app/Student.java     <-- one direct lookup, no scanning
```

2. **Readability and organization.** You always know where to look: the class `Student` is in `Student.java`. Teams and IDEs depend on this.
3. **Public means "visible to the world".** A public class is the **face** of the file. One public class per file makes the file's purpose clear.
4. **Avoids name clashes** inside a package (two public `Student` classes would conflict).
5. **Entry point clarity:** the JVM is started with a class name (`java Student`) and finds `main` in that class.

### 1.4 What if the file has several classes and a `main`?

```java
// File: Demo.java
class A { }
class B {
    public static void main(String[] args) {
        System.out.println("Hi");
    }
}
```

```
javac Demo.java     # produces A.class and B.class
java B              # run the class that has main (NOT "java Demo")
```

Common beginner confusion: you run the **class name**, not the file name.

(Since Java 11 you can also run a single source file directly with `java Demo.java`, which compiles in memory and launches the first class found. This is for quick scripts, not normal projects.)

### 1.5 Best practice

- **One top-level class per file**, even non-public ones (easier to find and maintain).
- File name = class name always.
- Put classes in **packages** that mirror the folder structure.

---

## 2. Wrapper classes

### 2.1 The problem they solve

Java has **8 primitive types** (`int`, `double`...). They are fast and small, but they are **not objects**. That causes problems:

```java
List<int> list = new ArrayList<>();      // ERROR: generics need objects, not primitives
int x = null;                             // ERROR: a primitive can't be null
x.toString();                             // ERROR: primitives have no methods
```

A **wrapper class** is an **object that wraps one primitive value**, so the primitive can be used wherever an object is required.

```
  primitive            wrapper object (on the heap)
 +--------+            +--------------------+
 | int 5  |   wrap     | Integer            |
 +--------+  ------->  |   value = 5        |
                       |   + methods        |
                       +--------------------+
```

### 2.2 All eight wrappers

| Primitive | Wrapper class | Size of primitive |
|---|---|---|
| `byte` | `Byte` | 1 byte |
| `short` | `Short` | 2 |
| `int` | `Integer` | 4 |
| `long` | `Long` | 8 |
| `float` | `Float` | 4 |
| `double` | `Double` | 8 |
| `char` | `Character` | 2 |
| `boolean` | `Boolean` | 1 (bit) |

Names are the same except `int → Integer` and `char → Character`.

### 2.3 Where they sit in the class hierarchy

```
                    Object
                   /      \
               Number   Character   Boolean
              (abstract)
     /     /     |     \      \      \
  Byte  Short Integer  Long  Float  Double
```

- `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double` extend the abstract class **`Number`** (so they have methods like `intValue()`, `doubleValue()`).
- `Character` and `Boolean` extend `Object` directly.
- All wrapper classes are **`final`** (cannot be extended) and **immutable**.

### 2.4 Why do we need wrapper classes? (the reasons)

1. **Collections and generics work only with objects.** `List<Integer>`, `Map<String, Double>`.
2. **`null` support:** "no value" can be represented (a database column that is empty, an optional field).
3. **Utility methods:** parsing, conversion, comparison, constants.
4. **Frameworks and APIs** (Spring, JPA, JSON libraries) work with objects and need `null` to mean "missing".
5. **Synchronization/threads:** objects can be locked and shared; `AtomicInteger` etc.
6. **Polymorphism:** a wrapper can be passed where `Object` or `Number` is expected.
7. **Object-oriented consistency.**

### 2.5 Creating wrapper objects

```java
Integer a = 10;                       // autoboxing (recommended, uses valueOf)
Integer b = Integer.valueOf(10);      // factory method (can reuse cached objects)
Integer c = Integer.valueOf("10");    // from a String
Integer d = new Integer(10);          // DEPRECATED (marked for removal): don't use
```

Why `new Integer(10)` is discouraged: it **always creates a new object**, wasting memory and bypassing the cache (Section 5). Use `valueOf` or autoboxing.

### 2.6 Useful methods (the toolbox)

```java
// Parsing: String to primitive
int n = Integer.parseInt("123");              // returns int
double d = Double.parseDouble("3.14");
boolean flag = Boolean.parseBoolean("true");

// String to wrapper
Integer w = Integer.valueOf("123");           // returns Integer

// Wrapper to primitive
int p = w.intValue();
double dv = w.doubleValue();                  // Number methods

// Conversion to String
String s1 = Integer.toString(255);
String s2 = Integer.toBinaryString(10);       // "1010"
String s3 = Integer.toHexString(255);         // "ff"

// Comparison
int cmp = Integer.compare(5, 8);              // -1
boolean eq = w.equals(123);                   // true

// Constants
Integer.MAX_VALUE;   // 2147483647
Integer.MIN_VALUE;   // -2147483648
Double.MAX_VALUE;
Long.MAX_VALUE;

// Character helpers
Character.isDigit('7');       // true
Character.isLetter('a');      // true
Character.toUpperCase('a');   // 'A'

// Utility
Integer.sum(3, 4);  Integer.max(3, 4);  Integer.bitCount(7);
```

`parseInt("abc")` throws `NumberFormatException`.

### 2.7 Memory cost of wrappers

(From the object-size notes.) An `Integer` object is **16 bytes** (12 header + 4 value), and the variable holding it adds a 4 or 8 byte reference.

```
 int x = 5;           ->  4 bytes
 Integer y = 5;       ->  reference (4) + object (16) = 20 bytes (cached objects are shared)

 int[1_000_000]       ->  about 4 MB
 Integer[1_000_000]   ->  about 4 MB of references + up to 16 MB of objects
```

That is why performance-critical code prefers primitives.

### 2.8 Wrappers are immutable

```java
Integer a = 5;
a++;                      // looks like it changes a, but...
```

What really happens:

```
 BEFORE:  a ---> [Integer 5]
 a++  means:  a = Integer.valueOf(a.intValue() + 1);
 AFTER:   a ---> [Integer 6]       (a NEW object; [Integer 5] is untouched)
```

The same is true for `String`. This is also why passing a wrapper to a method and "changing" it there does not change the caller's variable.

```java
static void inc(Integer x) { x++; }      // only changes the local reference
Integer n = 5;
inc(n);
System.out.println(n);                    // 5
```

---

## 3. Autoboxing and unboxing

### 3.1 Definitions

- **Autoboxing:** automatic conversion **primitive → wrapper**.
- **Unboxing:** automatic conversion **wrapper → primitive**.

```java
Integer a = 10;          // autoboxing   (int -> Integer)
int b = a;               // unboxing     (Integer -> int)
```

Added in **Java 5** so you don't have to write conversion code by hand.

### 3.2 What the compiler really writes

The compiler rewrites your code (this is "syntactic sugar"):

```java
Integer a = 10;
int b = a;
a = a + 1;
```

becomes

```java
Integer a = Integer.valueOf(10);                    // boxing uses valueOf
int b = a.intValue();                               // unboxing uses xxxValue()
a = Integer.valueOf(a.intValue() + 1);              // unbox, add, box again
```

```
 boxing   :  int  --Integer.valueOf(int)-->  Integer
 unboxing :  Integer --intValue()-->         int
```

Same pattern for every wrapper: `Double.valueOf`, `doubleValue()`, `Boolean.valueOf`, `booleanValue()`, ...

### 3.3 Where autoboxing/unboxing happens automatically

```java
// 1. Assignments
Integer a = 5;               int b = a;

// 2. Method arguments and returns
static Integer twice(Integer n) { return n * 2; }     // unbox, multiply, box
int r = twice(4);

// 3. Collections
List<Integer> list = new ArrayList<>();
list.add(10);                // boxing
int first = list.get(0);     // unboxing

// 4. Arithmetic and comparisons (<, >, +, -, ...)
Integer x = 5, y = 3;
int sum = x + y;             // both unboxed
boolean bigger = x > y;      // both unboxed
```

### 3.4 Trap 1: `NullPointerException` while unboxing

```java
Integer x = null;
int y = x;               // NullPointerException (null.intValue())
```

```java
Map<String, Integer> map = new HashMap<>();
int count = map.get("missing");     // NullPointerException (get returns null, then unboxed)
```

Safe versions:

```java
int count = map.getOrDefault("missing", 0);

Integer v = map.get("missing");
int safe = (v != null) ? v : 0;
```

A related ternary trap:

```java
Integer a = null;
int r = flag ? a : 0;      // when flag is true: a is unboxed -> NullPointerException
```

When one operand is `int` and the other is `Integer`, the result type is `int`, so the `Integer` is unboxed.

### 3.5 Trap 2: performance (hidden object creation)

```java
Long sum = 0L;                       // wrapper
for (int i = 0; i < 1_000_000; i++) {
    sum += i;                        // unbox sum, add, box into a NEW Long every time
}
```

That can create about **a million temporary `Long` objects** (garbage collector work). Use the primitive:

```java
long sum = 0L;                       // primitive: no objects created
```

Rule: **use primitives for local calculations and loops; use wrappers only when an object is needed** (collections, null, generics).

### 3.6 Trap 3: overload resolution with boxing

Java tries these in order: **exact match → widening → boxing → varargs**.

```java
static void show(long x)    { System.out.println("long"); }
static void show(Integer x) { System.out.println("Integer"); }

show(5);      // prints "long"   (widening int->long beats boxing int->Integer)
```

```java
static void show(Object o)  { System.out.println("Object"); }
static void show(int... x)  { System.out.println("varargs"); }

show(5);      // prints "Object" (boxing beats varargs)
```

### 3.7 Trap 4: `List.remove(int)` vs `remove(Object)`

```java
List<Integer> list = new ArrayList<>(List.of(10, 20, 30, 1));

list.remove(1);                      // removes the element AT INDEX 1  -> [10, 30, 1]
list.remove(Integer.valueOf(1));     // removes the VALUE 1             -> [10, 30]
```

`remove(1)` with a primitive literal picks `remove(int index)` (exact match, no boxing needed).

### 3.8 Trap 5: no cross-type boxing

```java
Long l = 100;          // ERROR: int cannot be converted to Long
Long l2 = 100L;        // OK
Double d = 5;          // ERROR
Double d2 = 5.0;       // OK
long big = 100;        // OK (widening of primitives)
```

Boxing only converts to the **matching** wrapper. It can't widen and box in one step.

### 3.9 Trap 6: comparing different wrappers

```java
Long l = 127L;
System.out.println(l.equals(127));     // false (the argument is boxed to Integer, not Long)
System.out.println(l.equals(127L));    // true
```

`equals` first checks that the other object is the **same type**.

### 3.10 Summary

| Item | Remember |
|---|---|
| Boxing | `Integer.valueOf(int)` (can reuse cached objects) |
| Unboxing | `intValue()` etc. (`null` leads to `NullPointerException`) |
| Cost | Boxing may allocate objects; avoid in hot loops |
| Safety | Check for `null` before unboxing |
| Overloading | Widening before boxing before varargs |

---

## 4. Equality: `==` vs `.equals()` with wrappers

### 4.1 Core idea

- `==` on **objects** compares **references (addresses)**: "Are these the same object?"
- `.equals()` compares **content**: "Do they hold the same value?"
- `==` between a **wrapper and a primitive** unboxes the wrapper and compares **values**.

```
 Integer a = 1000;         a ---> [Integer 1000 @0x10]
 Integer b = 1000;         b ---> [Integer 1000 @0x20]     different objects

 a == b        compares 0x10 with 0x20  -> false
 a.equals(b)   compares 1000 with 1000  -> true
```

### 4.2 Examples

```java
Integer a = 1000, b = 1000;
System.out.println(a == b);          // false  (two different objects)
System.out.println(a.equals(b));     // true

int c = 1000;
System.out.println(a == c);          // true   (a is unboxed, primitives compared)

Integer x = 127, y = 127;
System.out.println(x == y);          // true   (!!! cached object, see Section 5)
```

The last result surprises many people, and it is the Integer cache.

### 4.3 Rules to remember

1. **Always use `.equals()`** to compare wrapper objects (and Strings).
2. `==` is safe only when at least one side is a **primitive**.
3. Use `compareTo` / `Integer.compare` for ordering. (Never subtract values to compare: it can overflow.)
4. `<`, `>`, `<=`, `>=` **do unbox**, so they compare values (and throw NPE on null). Only `==` and `!=` compare references when both sides are wrappers.

### 4.4 Special case: `Double` and `NaN`

```java
double n = Double.NaN;
System.out.println(n == n);                       // false (primitive rule: NaN is never equal to itself)

Double d = Double.NaN;
System.out.println(d.equals(Double.NaN));         // true  (wrapper equals treats NaN as equal)
```

Also `0.0 == -0.0` is `true`, but `Double.valueOf(0.0).equals(-0.0)` is `false`.

---

## 5. Integer class internals and the Integer cache

### 5.1 How the `Integer` class is designed

Simplified view:

```java
public final class Integer extends Number implements Comparable<Integer> {

    private final int value;                     // the one wrapped value (immutable)

    public static final int MAX_VALUE = 0x7fffffff;
    public static final int MIN_VALUE = 0x80000000;

    public Integer(int value) { this.value = value; }   // deprecated

    public int intValue() { return value; }

    @Override public int hashCode()  { return value; }
    @Override public boolean equals(Object obj) {
        if (obj instanceof Integer) {
            return value == ((Integer) obj).intValue();
        }
        return false;
    }
    @Override public String toString() { return String.valueOf(value); }

    public static Integer valueOf(int i) { /* uses the cache, see below */ }
    public static int parseInt(String s) { /* ... */ }
    // plus many static helpers
}
```

Design points:

- **`final` class**: nobody can subclass it and change behavior.
- **`private final int value`**: makes it **immutable** (thread-safe, safe to share and cache).
- **`equals`** compares type and value; **`hashCode`** returns the value itself.
- Memory per object: 12 (header) + 4 (value) = **16 bytes**.

### 5.2 Why a cache?

Small integers (like loop counters, ages, ids, `0`, `1`, `100`) are used **constantly**. Creating a new `Integer` object each time would flood memory and the garbage collector. Since wrappers are **immutable**, it is totally safe to **share one object** among all users of the same small value.

### 5.3 How `Integer.valueOf(int)` works

```
 valueOf(i):
    if i is between -128 and 127:
         return the PRE-CREATED object from the cache array
    else:
         return a NEW Integer(i)
```

Pseudo-code:

```java
public static Integer valueOf(int i) {
    if (i >= -128 && i <= 127)
        return IntegerCache.cache[i + 128];      // shared, pre-created
    return new Integer(i);                        // new object
}
```

The cache is an **array of 256 Integer objects** created once when the `Integer` class is first used:

```
 IntegerCache.cache  (on the heap)
 index:   0      1      2    ...   128    ...   254    255
 value: -128   -127   -126  ...    0     ...   126    127
         |       |      |           |             |      |
       [Int]   [Int]  [Int]       [Int]         [Int]  [Int]    one object per value
```

Since autoboxing uses `valueOf`, it uses this cache too.

### 5.4 The famous result, explained with memory

```java
Integer a = 127, b = 127;
System.out.println(a == b);      // true
```

```
 a ----\
        +--> [Integer 127]  (the ONE cached object, index 255)
 b ----/
 same address -> == is true
```

```java
Integer c = 128, d = 128;
System.out.println(c == d);      // false
```

```
 c ---> [Integer 128 @0x30]     valueOf(128) is outside the cache range
 d ---> [Integer 128 @0x40]     so each boxing creates a NEW object
 different addresses -> == is false
```

And `equals` is always `true` for both pairs.

```java
Integer e = Integer.valueOf(100);
Integer f = Integer.valueOf(100);
System.out.println(e == f);                      // true  (cache)

Integer g = new Integer(100);                    // (deprecated) always a new object
Integer h = new Integer(100);
System.out.println(g == h);                      // false
System.out.println(e == g);                      // false (cached vs new)
```

### 5.5 Which wrappers have caches?

| Wrapper | Cached range | Notes |
|---|---|---|
| `Byte` | -128 to 127 (all values) | |
| `Short` | -128 to 127 | |
| `Integer` | -128 to 127 | **Upper bound configurable** with JVM option `-XX:AutoBoxCacheMax=<n>` |
| `Long` | -128 to 127 | |
| `Character` | 0 to 127 | |
| `Boolean` | `TRUE` and `FALSE` | Only two objects exist |
| `Float` | **No cache** | |
| `Double` | **No cache** | |

```java
Double x = 1.0, y = 1.0;
System.out.println(x == y);       // false (no cache for Double)

Boolean t1 = true, t2 = true;
System.out.println(t1 == t2);     // true  (both are Boolean.TRUE)
```

### 5.6 Does the cache change correct programs?

If you always use `.equals()` for comparisons, **no**. The cache is a performance optimization only. It becomes a bug only when someone uses `==` on wrappers. That is why the rule is: **never compare wrapper objects with `==`.**

### 5.7 Benefits and costs of the cache

**Benefits:** less memory, fewer allocations, less GC work, faster boxing of small values.
**Costs:** a fixed block of 256 objects always lives in memory; unpredictable `==` results across the range boundary; programmers may rely on it accidentally.

### 5.8 Other things inside `Integer` worth knowing

```java
Integer.MAX_VALUE + 1            // overflows to Integer.MIN_VALUE (primitive arithmetic wraps)
Integer.parseInt("12a")          // NumberFormatException
Integer.toBinaryString(5)        // "101"
Integer.compare(3, 7)            // negative number
Integer.valueOf(5).hashCode()    // 5
```

---

## 6. Abstract class interview questions

(Short answers first, then the reasoning, then examples.)

### 6.1 The questions from the video

| # | Question | Answer |
|---|---|---|
| 1 | Can an abstract class have a **constructor**? | **Yes** |
| 2 | Can an abstract class be **final**? | **No** |
| 3 | Can an abstract class have **static methods**? | **Yes** |
| 4 | Can an abstract class have **private** or **final** methods? | **Yes** (but those methods can't be abstract) |
| 5 | Must an abstract class have **abstract methods**? | **No** |

### 6.2 Explanations

**Q1. Constructor: yes.**
You can't create an object of the abstract class directly, but when a child object is created, the parent constructor runs (via `super(...)`) to initialize the parent's fields.

```java
abstract class Shape {
    String color;
    Shape(String color) {                    // allowed
        this.color = color;
        System.out.println("Shape constructor");
    }
    abstract double area();
}
class Circle extends Shape {
    double r;
    Circle(String color, double r) { super(color); this.r = r; }
    double area() { return Math.PI * r * r; }
}
new Circle("red", 2);       // prints: Shape constructor
```

**Q2. Final: no.**
`abstract` means "must be extended to be useful"; `final` means "can't be extended". Together they contradict each other. `abstract final class A {}` is a compile error.

**Q3. Static methods: yes.**
Static methods belong to the class and have a body, so they work fine in an abstract class and can be called as `Shape.helper()` without any object. (A static method cannot itself be abstract.)

```java
abstract class Utils {
    static int square(int n) { return n * n; }
}
Utils.square(4);            // 16, no object needed
```

**Q4. Private and final methods: yes, but they can't be abstract.**
An abstract class can have normal methods with bodies, including `private` helpers and `final` methods. The restriction is on **abstract methods**: an abstract method can't be `private` (child couldn't see it), `final` (can't override), or `static` (can't override).

```java
abstract class Report {
    private void log()  { }                  // OK (concrete private method)
    final void print()  { }                  // OK (concrete final method)
    abstract void generate();                // abstract: not private/final/static
}
```

**Q5. Need abstract methods: no.**
An abstract class can have zero abstract methods. It's still abstract, so you can't instantiate it. This is used to **force people to subclass** (or as a base class that shouldn't exist alone). The reverse is the rule: a class with an abstract method **must** be abstract.

### 6.3 More abstract class questions (extra practice)

**Q6. Can we create an object of an abstract class?**
No (`new Shape()` is a compile error). But we can create a **reference** of it, and we can use an **anonymous subclass** that gives bodies to all abstract methods:

```java
Shape s = new Shape("red") {
    @Override double area() { return 10; }
};
```

**Q7. If a child doesn't implement all abstract methods, what happens?**
The child must also be declared `abstract`; otherwise compile error.

**Q8. Can an abstract class have instance variables?**
Yes: any fields (instance, static, final), with any access modifier. (This is a key difference from interfaces.)

**Q9. Can an abstract class implement an interface without implementing its methods?**
Yes, because it is abstract. The concrete child must implement the remaining methods.

```java
interface Flyable { void fly(); void land(); }
abstract class Bird implements Flyable {
    public void land() { }                   // implemented
    // fly() left abstract
}
class Eagle extends Bird { public void fly() { } }
```

**Q10. Can an abstract class extend another (concrete or abstract) class?**
Yes. It can even **re-abstract** an inherited concrete method:

```java
class A { void show() { System.out.println("A"); } }
abstract class B extends A { abstract void show(); }    // allowed: turns it abstract again
```

**Q11. Can an abstract class have a `main` method?**
Yes. `main` is static, so you can run an abstract class with `java ClassName`.

**Q12. Can an abstract class have a private constructor?**
Yes, allowed. Then only classes nested inside it (or inside the same enclosing class) can extend it, because private access stops at the top-level class body. Used in sealed-like designs.

**Q13. Can the constructor of an abstract class be `abstract`?**
No. Constructors can never be abstract, static, or final.

**Q14. Can an abstract method have a body? Can a normal class have an abstract method?**
Abstract method: no body. A non-abstract class containing an abstract method is a compile error.

**Q15. Can abstract methods be `protected` or default?**
Yes (`public`, `protected`, default). Not `private`.

**Q16. Can we call an abstract method from the abstract class's constructor?**
Yes, it compiles, and runs the child's implementation. But the child's fields aren't initialized yet, so the result may surprise you (common bug).

```java
abstract class Base {
    Base() { init(); }
    abstract void init();
}
class Child extends Base {
    int x = 5;
    void init() { System.out.println(x); }   // prints 0, not 5
}
new Child();
```

**Q17. Does the static block of an abstract class run?**
Yes, when the class is loaded (for example when a child class is first used).

**Q18. Can an abstract class be a nested (inner) class?** Yes, including `static abstract class`.

**Q19. Can we have an array of an abstract type?**
Yes: `Shape[] arr = new Shape[3];` creates 3 null slots; you then store child objects. (The array itself is not an instance of the abstract class.)

**Q20. Is multiple inheritance possible with abstract classes?**
No. A class can extend only one class (abstract or not). Use interfaces for multiple contracts.

**Q21. Abstract class vs interface: one-line difference?**
Abstract class = IS-A base with shared state and code (single inheritance). Interface = CAN-DO contract (multiple inheritance of type).

**Q22. Why does an abstract class need a constructor if it can't be instantiated?**
To initialize the fields it declares. Every child object contains the parent's part, and that part must be set up.

**Q23. Can an abstract class be `sealed`? Can a record be abstract?**
Abstract + `sealed` is allowed (Java 17+) to restrict which classes can extend it. A `record` cannot be abstract (records are implicitly final).

**Q24. Can an enum have abstract methods?**
Yes, each constant must then provide the body in its own constant-specific class body.

### 6.4 Summary table: what an abstract class can contain

| Element | Allowed? |
|---|---|
| Abstract methods | Yes (no body; not private/final/static) |
| Concrete methods | Yes |
| `private` / `final` / `static` concrete methods | Yes |
| Constructors | Yes (not abstract/static/final) |
| Instance and static fields | Yes |
| Static and instance blocks | Yes |
| `main` method | Yes |
| `final` on the class | **No** |
| Direct instantiation with `new` | **No** |
| Anonymous subclass | Yes |

---

## 7. POJO classes

### 7.1 What is a POJO?

**POJO = Plain Old Java Object.**

> A simple Java class that is **not tied to any special framework**. It doesn't have to extend a framework's class, implement a framework's interface, or depend on a particular technology. It is "just a Java object".

The term became popular because frameworks used to force heavy rules (like old EJB), and developers wanted to say: "we use ordinary, simple objects".

```java
public class Student {
    private int id;
    private String name;
    private int age;

    public Student() { }                                  // no-arg constructor

    public Student(int id, String name, int age) {
        this.id = id; this.name = name; this.age = age;
    }

    public int getId()          { return id; }
    public void setId(int id)   { this.id = id; }
    public String getName()     { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge()         { return age; }
    public void setAge(int age) { this.age = age; }

    @Override public String toString() { return "Student{id=" + id + ", name=" + name + ", age=" + age + "}"; }
}
```

### 7.2 What a POJO should (and should not) do

**Typically:**
- Private fields with public getters/setters (encapsulation).
- A constructor (often a no-arg one plus an all-args one).
- Overridden `toString()`, `equals()`, `hashCode()` where useful.

**Should NOT be forced to:**
- Extend a framework-specific base class.
- Implement a framework-specific interface.
- Depend on a framework-specific annotation to function (annotations like JPA's `@Entity` are widely tolerated in practice, but a strict POJO has none).

### 7.3 Why use POJOs?

1. **Simple and readable:** just fields and methods.
2. **Framework independent:** easy to move between technologies.
3. **Easy to test:** no container needed; just `new Student()`.
4. **Reusable** across layers (controller, service, repository).
5. **Works with many tools:** Jackson/Gson (JSON), Hibernate/JPA (database), Spring (beans).
6. **Models real-world things** (Student, Order, Product).

### 7.4 POJO vs JavaBean vs DTO vs Entity vs Record

| Term | Meaning | Rules |
|---|---|---|
| **POJO** | Any plain Java class | No framework restriction (loose) |
| **JavaBean** | A POJO following a stricter convention | `public` **no-arg constructor**, `private` fields with **getters/setters** (`getX`, `setX`, `isX`), usually `Serializable` |
| **DTO** (Data Transfer Object) | A POJO whose only job is to **carry data** between layers/systems | Usually no logic; often immutable |
| **Entity** | A POJO **mapped to a database table** | Has annotations like `@Entity`, `@Id` (JPA) |
| **Record** (Java 16+) | A compact, immutable data carrier | `record Student(int id, String name) { }` auto-generates constructor, getters, `equals`, `hashCode`, `toString` |

So: **every JavaBean is a POJO, but not every POJO is a JavaBean.**

```java
// Record: modern way to write an immutable data POJO
public record StudentDto(int id, String name, int age) { }

StudentDto s = new StudentDto(1, "Rahul", 20);
s.name();            // accessor method (no "get" prefix)
```

### 7.5 Common tools to reduce boilerplate

- **Lombok:** annotations such as `@Data`, `@Getter`, `@Setter`, `@Builder` generate getters/setters/constructors at compile time.
- **Records:** built into the language (immutable).
- **IDE generators:** generate getters/setters/equals/hashCode.

### 7.6 Anemic domain model vs Rich domain model

This is a **design** question about **where the business logic lives**.

#### Anemic domain model

The object is **only data** (fields + getters/setters). All business logic lives in separate **service classes**.

```java
// POJO with NO behavior
class BankAccount {
    private double balance;
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}

// Logic lives somewhere else
class BankAccountService {
    void withdraw(BankAccount acc, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Invalid amount");
        if (amount > acc.getBalance()) throw new IllegalStateException("Insufficient funds");
        acc.setBalance(acc.getBalance() - amount);
    }
}
```

```
 +---------------+        +--------------------------+
 | BankAccount   |<-------| BankAccountService       |
 |  (data only)  |  reads | (all rules and logic)    |
 +---------------+  writes+--------------------------+
```

#### Rich domain model

The object contains **data AND the business rules** that protect that data.

```java
class BankAccount {
    private double balance;

    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Invalid amount");
        if (amount > balance) throw new IllegalStateException("Insufficient funds");
        balance -= amount;
    }
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Invalid amount");
        balance += amount;
    }
    public double getBalance() { return balance; }     // no public setBalance!
}
```

```
 +-----------------------------+
 | BankAccount                 |
 |  data: balance              |
 |  behavior: withdraw(),      |
 |            deposit()        |   rules live WITH the data
 +-----------------------------+
```

#### Comparison

| | Anemic model | Rich domain model |
|---|---|---|
| Object holds | Data only (getters/setters) | Data + business rules |
| Where logic lives | Service classes | Inside the domain objects |
| Object-oriented? | Looks like procedural code with data bags | True OOP (encapsulation, behavior with state) |
| Invariants (always-valid state) | Easy to break (anyone can call `setBalance(-100)`) | Protected by the object itself |
| Setters | Public setters everywhere | Few or none; changes only through meaningful methods |
| Testing | Test services | Test objects directly (simple unit tests) |
| Simplicity | Simple, fast to write | More design effort |
| Typical use | DTOs, simple CRUD apps, data transfer | Complex business domains (banking, insurance, orders) |
| Criticism | Called an **anti-pattern** by some (Martin Fowler) when used for core business objects | Can grow large; needs careful design |

#### The balanced view

- Anemic objects are **perfectly fine for DTOs**, API request/response objects, and simple CRUD.
- For **core business rules**, a rich model keeps rules next to the data they protect, which means fewer bugs and clearer encapsulation.
- Many real projects mix both: rich entities for core rules plus anemic DTOs at the boundaries.

#### Connection to what you learned: encapsulation

A rich model is **encapsulation done fully**: the data is private and can change **only through methods that enforce the rules**. A POJO with public getters and setters for everything is only the *minimum* level of encapsulation.

### 7.7 Writing a good POJO (checklist)

```java
public class Employee {
    private final int id;              // final if it should never change
    private String name;

    public Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // getters (and setters only where changing makes sense)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee other = (Employee) o;
        return id == other.id && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() { return Objects.hash(id, name); }

    @Override
    public String toString() { return "Employee{id=" + id + ", name='" + name + "'}"; }
}
```

Remember: if you override `equals`, **also override `hashCode`** (needed for `HashMap`/`HashSet`).

---

## 8. Predict the output (quiz)

**Q1**
```java
Integer a = 100, b = 100;
Integer c = 200, d = 200;
System.out.println(a == b);
System.out.println(c == d);
System.out.println(c.equals(d));
```
Answer: `true`, `false`, `true`. (100 is cached; 200 is not; `equals` compares values.)

**Q2**
```java
Integer a = 1000;
int b = 1000;
System.out.println(a == b);
```
Answer: `true`. `a` is unboxed, then two primitives are compared.

**Q3**
```java
Integer x = null;
int y = x;
```
Answer: `NullPointerException` (unboxing `null`).

**Q4**
```java
Long l = 127L;
System.out.println(l.equals(127));
```
Answer: `false`. `127` is boxed to `Integer`, and `Long.equals` requires a `Long`.

**Q5**
```java
List<Integer> list = new ArrayList<>(List.of(5, 6, 7));
list.remove(1);
System.out.println(list);
```
Answer: `[5, 7]`. `remove(1)` removes the element at **index 1**.

**Q6**
```java
Double a = 1.0, b = 1.0;
System.out.println(a == b);
System.out.println(a.equals(b));
```
Answer: `false`, `true`. (`Double` has no cache.)

**Q7**
```java
static void f(long x)    { System.out.println("long"); }
static void f(Integer x) { System.out.println("Integer"); }
f(10);
```
Answer: `long` (widening beats boxing).

**Q8**
```java
Integer a = 5;
Integer b = a;
a++;
System.out.println(a + " " + b);
```
Answer: `6 5`. `a++` creates a new Integer; `b` still points to the old one.

**Q9**
```java
Boolean b1 = true, b2 = true;
Boolean b3 = new Boolean(true);
System.out.println(b1 == b2);
System.out.println(b1 == b3);
```
Answer: `true`, `false`. (`Boolean.TRUE` is shared; `new Boolean` creates a new object, and is deprecated.)

**Q10**
```java
Long sum = 0L;
for (int i = 0; i < 3; i++) sum += i;
System.out.println(sum);
```
Answer: `3` (works, but boxing each step: inefficient for big loops).

**Q11**
```java
abstract class A { A() { System.out.println("A"); } }
class B extends A { B() { System.out.println("B"); } }
new B();
```
Answer: `A` then `B`. (The abstract class's constructor runs first via `super()`.)

**Q12**
```java
Character c1 = 'a', c2 = 'a';
Character c3 = '\u1234', c4 = '\u1234';
System.out.println(c1 == c2);
System.out.println(c3 == c4);
```
Answer: `true`, `false`. (Character cache covers 0 to 127 only.)

---

## 9. More interview questions with answers

### File and class structure

**Q1. Can a Java file contain more than one class?**
Yes, many. But only one can be `public`, and if there is a public class the file must be named after it.

**Q2. Why must the public class name match the file name?**
So compilers, class loaders and IDEs can find a class directly from its name (class name maps to file name) without scanning all files.

**Q3. What if two public classes are in one file?**
Compile error: the second public class should be in its own file.

**Q4. A file named `Test.java` contains only `class Demo { public static void main... }`. Does it compile? How do you run it?**
Yes (no public class, so any file name is allowed). It compiles to `Demo.class`. Run with `java Demo`.

**Q5. How many `.class` files does a file with 3 classes produce?**
Three, one per class (plus extra for each nested/anonymous class).

### Wrapper classes

**Q6. What is a wrapper class? Why do we need it?**
An object that wraps a primitive. Needed for collections/generics, `null`, utility methods, and frameworks.

**Q7. Are wrapper classes mutable?**
No, they are immutable and `final`.

**Q8. Difference between `Integer.parseInt()` and `Integer.valueOf()`?**
`parseInt` returns a primitive `int`. `valueOf` returns an `Integer` object (and may reuse a cached one).

**Q9. Why is `new Integer(5)` discouraged?**
It always creates a new object, ignoring the cache, wasting memory. It is deprecated; use `Integer.valueOf(5)` or autoboxing.

**Q10. Which classes are parents of the numeric wrappers?**
`Number` (abstract) for Byte, Short, Integer, Long, Float, Double. `Character` and `Boolean` extend `Object`.

**Q11. Can we use primitives in generics, like `List<int>`?**
No. Use `List<Integer>`; autoboxing handles the conversion.

**Q12. Is `int` or `Integer` faster/lighter?**
`int`: no object, no header, no indirection, no GC pressure. Prefer primitives unless you need an object.

**Q13. What does `Integer.MAX_VALUE + 1` give?**
`Integer.MIN_VALUE` (overflow wraps around).

### Autoboxing and unboxing

**Q14. What is autoboxing and unboxing?**
Automatic conversion between a primitive and its wrapper (`int` ↔ `Integer`), done by the compiler with `valueOf` and `xxxValue()`.

**Q15. When does autoboxing cause a `NullPointerException`?**
When unboxing a `null` wrapper (`int x = nullInteger;`, `int c = map.get("missing");`, arithmetic on a null wrapper).

**Q16. Can autoboxing hurt performance?**
Yes: in loops, `Long sum += i` creates many temporary objects. Use primitives for calculations.

**Q17. Why does `Long l = 5;` fail but `long l = 5;` works?**
Boxing converts only to the matching wrapper (`int` to `Integer`), not `int` to `Long`. Primitive widening (`int` to `long`) is allowed, but widening followed by boxing is not.

**Q18. Which gets chosen for `f(5)`: `f(long)` or `f(Integer)`?**
`f(long)`. Order: exact, widening, boxing, varargs.

### Equality and cache

**Q19. Why is `Integer a = 127, b = 127; a == b` true but 128 false?**
`valueOf` returns a shared cached object for -128 to 127. Outside that range, each boxing creates a new object, so references differ.

**Q20. How can you change the Integer cache range?**
Only the upper limit, with the JVM flag `-XX:AutoBoxCacheMax=<size>`. The lower bound stays -128.

**Q21. Which wrappers have no cache?**
`Float` and `Double`.

**Q22. How should wrapper objects be compared?**
With `.equals()` (or `compareTo`), never `==`.

**Q23. Is the Integer cache the reason for immutability?**
Immutability is what makes caching safe: sharing one object is harmless because nobody can change it.

**Q24. `new Integer(10) == new Integer(10)`?**
`false`: two distinct objects.

**Q25. Why does `Long.equals(Integer)` return false even for the same number?**
`equals` checks the other object's type first, and a `Long` is never equal to an `Integer`.

### Abstract classes (more)

**Q26. Can an abstract class have a constructor if it can't be instantiated?**
Yes, it initializes the parent part of child objects.

**Q27. Can an abstract class be final?**
No (contradiction: abstract needs extending).

**Q28. Can an abstract class have no abstract methods?**
Yes.

**Q29. Can an abstract method be private/static/final?**
No, they all conflict with the need to override.

**Q30. Can an abstract class have static and private methods?**
Yes, if they have bodies.

**Q31. Can an abstract class implement an interface partially?**
Yes.

**Q32. Can we instantiate an abstract class through an anonymous class?**
That creates an anonymous **subclass** that implements the abstract methods; the abstract class itself is still not instantiated.

### POJO

**Q33. What is a POJO?**
A Plain Old Java Object: a simple class not bound to any framework, usually with private fields, constructors and getters/setters.

**Q34. POJO vs JavaBean?**
A JavaBean is a POJO that follows stricter rules: public no-arg constructor, private fields with getters/setters, usually `Serializable`.

**Q35. POJO vs DTO vs Entity?**
POJO is the general idea. DTO is a POJO used to carry data between layers. Entity is a POJO mapped to a database table.

**Q36. What is the anemic domain model? Why is it criticized?**
Domain objects with only data and getters/setters, while all logic is in services. It is criticized because it breaks encapsulation and leaves objects without behavior (procedural style), making invariants easy to violate.

**Q37. What is a rich domain model?**
Objects that hold both data and the business rules that protect it, exposing meaningful methods (`withdraw`, `deposit`) instead of raw setters.

**Q38. When is an anemic model acceptable?**
For DTOs, API request/response objects and simple CRUD applications.

**Q39. Why override `hashCode` when you override `equals` in a POJO?**
Hash-based collections (`HashMap`, `HashSet`) use `hashCode` first. Equal objects must have equal hash codes, otherwise lookups fail.

**Q40. How does a `record` relate to POJOs?**
A record is a compact way to declare an immutable data carrier; the compiler generates the constructor, accessors, `equals`, `hashCode` and `toString`.

---

## 10. Cheat sheet and common mistakes

### One-page summary

```
FILE RULES
  1 public top-level class per file; file name = that class name
  any number of non-public classes; one .class file per class

WRAPPERS
  8 pairs: byte/Byte short/Short int/Integer long/Long float/Float double/Double char/Character boolean/Boolean
  immutable, final; Number is parent of the six numeric ones
  needed for: collections/generics, null, utility methods, frameworks

AUTOBOXING / UNBOXING
  boxing   = Integer.valueOf(int)      unboxing = intValue()
  null unboxing -> NullPointerException
  hot loops -> use primitives, not wrappers
  overload order: exact -> widening -> boxing -> varargs
  Long l = 5;  is an ERROR  (use 5L)

EQUALITY
  == on two wrappers = same object?  .equals() = same value?
  Integer cache: -128..127 shared (Byte/Short/Long same; Character 0..127; Boolean 2 objects; Float/Double none)
  ALWAYS use .equals() for wrappers

ABSTRACT CLASS
  constructor YES | final NO | static methods YES | private/final (concrete) methods YES
  abstract methods required NO | instantiate NO | anonymous subclass YES

POJO
  plain class, framework-free; JavaBean = POJO + no-arg ctor + getters/setters
  DTO carries data; Entity maps to a table; record = immutable data carrier
  anemic = data only + logic in services;  rich = data + behavior in the object
```

### Common mistakes

| Mistake | Fix |
|---|---|
| Two public classes in one file | One public class per file, name it after the file |
| Running `java Demo` when `main` is in class `B` inside `Demo.java` | Run the **class** name that contains `main` (`java B`) |
| Using `==` to compare `Integer`/`Long`/`Double` objects | Use `.equals()` |
| Assuming `==` works for 127 so it works for 1000 | Cache is only -128..127 |
| Unboxing a `null` (`map.get(key)` into an `int`) | Use `getOrDefault` or check for `null` |
| `Long l = 5;` or `Double d = 5;` | `5L`, `5.0` |
| Using wrappers for loop counters and sums | Use primitives |
| `list.remove(1)` expecting to remove the value 1 | Use `list.remove(Integer.valueOf(1))` |
| Using `new Integer(x)` | `Integer.valueOf(x)` or autoboxing |
| Subtracting wrapper values to compare | Use `Integer.compare(a, b)` |
| Declaring `abstract final` | Contradiction; remove one |
| Making abstract methods `private`/`static`/`final` | Not allowed |
| Overriding `equals` without `hashCode` | Always override both |
| Putting every business rule in services and exposing setters everywhere | Consider a rich model for core rules; keep anemic style for DTOs |

### One-line takeaways

1. **One public class per file**, and the file name equals its name, so tools can find classes without scanning.
2. **Wrappers** turn primitives into immutable objects for collections, generics and `null`.
3. **Autoboxing** is `valueOf`, **unboxing** is `xxxValue()`; unboxing `null` throws NPE.
4. **Integer cache** shares objects for -128 to 127, so `==` works there by accident; use `.equals()`.
5. **Abstract class:** can have constructors, static methods and private/final concrete methods; cannot be final or instantiated; abstract methods are optional.
6. **POJO** = simple framework-free class; **JavaBean** adds strict conventions.
7. **Anemic** = data only; **rich** = data + behavior. Choose rich for core rules, anemic for DTOs.

---
*Practice tip: write a tiny program that prints `a == b` and `a.equals(b)` for Integer values 126, 127, 128, 129 and for Long, Double and Character. Seeing where the results flip makes the cache rules stick.*
