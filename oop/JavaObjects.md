# Java Objects Deep Dive
### Object Size · Call by Value vs Reference · Shallow vs Deep Copy
*(Java Full Course #14, explained in sequence, in easy language)*

---

## Table of Contents

1. [Class vs Object (quick recap)](#1-class-vs-object-quick-recap)
2. [What really happens when you write `new`](#2-what-really-happens-when-you-write-new)
3. [Stack vs Heap (where things live)](#3-stack-vs-heap-where-things-live)
4. [Reference variables: many arrows, one object](#4-reference-variables-many-arrows-one-object)
5. [Size of an object / class (the big topic)](#5-size-of-an-object--class-the-big-topic)
6. [Call by Value vs Call by Reference](#6-call-by-value-vs-call-by-reference)
7. [Copying objects: reference copy, shallow copy, deep copy](#7-copying-objects-reference-copy-shallow-copy-deep-copy)
8. [Immutability and why it makes copying easy](#8-immutability-and-why-it-makes-copying-easy)
9. [Interview questions with answers](#9-interview-questions-with-answers)
10. [Cheat sheet and common mistakes](#10-cheat-sheet-and-common-mistakes)

---

## 1. Class vs Object (quick recap)

- **Class** = a blueprint / template. It describes *what data* (fields) and *what behavior* (methods) something has. It does not hold real data by itself.
- **Object** = a real thing built from that blueprint. It holds actual values in memory.

```java
class Student {
    String name;
    int age;

    void study() {
        System.out.println(name + " is studying");
    }
}

public class Main {
    public static void main(String[] args) {
        Student s1 = new Student();   // object 1
        Student s2 = new Student();   // object 2
        s1.name = "Rahul";
        s2.name = "Priya";
    }
}
```

Real-life analogy:

```
   CLASS (Blueprint of a house)         OBJECTS (actual houses built)
   +-------------------------+          +-----------+   +-----------+
   |  rooms, doors, windows  |  ---->   |  House #1 |   |  House #2 |
   +-------------------------+          |  (red)    |   |  (blue)   |
                                        +-----------+   +-----------+
```

One blueprint, many houses. Each house has its **own** values (own paint colour), but they all follow the same design.

---

## 2. What really happens when you write `new`

```java
Student s1 = new Student();
```

This one line is actually **two things**:

```
Student s1;            // (A) declare a reference variable (a remote control)
s1 = new Student();    // (B) create the object and connect the remote to it
```

### Step by step

1. **Class loading**: the JVM loads `Student.class` (only the first time it is needed).
2. **Memory allocation**: the JVM reserves space on the **heap** for one `Student` object (size depends on its fields, see Section 5).
3. **Default values**: every field is set to a default (`0`, `false`, `null`).
4. **Constructor runs**: your constructor code fills in the real values.
5. **Address returned**: `new` gives back the object's address (a reference), which is stored in `s1`.

```
        STACK                              HEAP
   +--------------+                 +---------------------------+
   | s1 | 0x1A2B  |  ------------>  | Student object @0x1A2B    |
   +--------------+                 |   name = null             |
                                    |   age  = 0                |
                                    +---------------------------+
```

**Key idea:** `s1` is **not** the object. `s1` is a *variable that stores the address* of the object. Think of it as a TV remote: the remote is not the TV, but it controls the TV.

### Default values table

| Field type | Default |
|---|---|
| `byte`, `short`, `int`, `long` | `0` |
| `float`, `double` | `0.0` |
| `char` | `'\u0000'` |
| `boolean` | `false` |
| any reference (String, arrays, objects) | `null` |

> Note: **local variables** (inside methods) get **no** default value. You must assign them before use. Only **fields** get defaults.

---

## 3. Stack vs Heap (where things live)

| | Stack | Heap |
|---|---|---|
| Stores | Local variables, method call frames, references | All objects and arrays |
| Per thread? | Yes, each thread has its own stack | No, shared by all threads |
| Cleanup | Automatic when method ends | Garbage Collector (GC) |
| Speed | Very fast | Slower |
| Size | Small | Large |

```java
public static void main(String[] args) {
    int x = 10;                    // x is a primitive local -> lives on stack
    Student s = new Student();     // s (reference) on stack, object on heap
}
```

```
   STACK (main's frame)                 HEAP
   +----------------+              +--------------------+
   | x = 10         |              |                    |
   | s = 0x1A2B ----+------------> | Student object     |
   +----------------+              +--------------------+
```

Primitives **declared as local variables** hold the value directly. References hold only an address. (A primitive that is a *field* of an object lives inside that object, on the heap.)

---

## 4. Reference variables: many arrows, one object

```java
Student s1 = new Student();
s1.name = "Rahul";

Student s2 = s1;       // copies the ADDRESS, not the object
s2.name = "Priya";

System.out.println(s1.name);   // Priya  (!!)
```

```
   STACK                         HEAP
   +-----------+            +--------------------+
   | s1 | 0x1A |--+         | Student @0x1A      |
   +-----------+  +-------> |   name = "Priya"   |
   | s2 | 0x1A |--+         +--------------------+
   +-----------+
        Two remotes, ONE TV.
```

There is **only one object**. `s2 = s1` did not create a new object. Changing it through `s2` changes what `s1` sees too.

### `null` and garbage collection

```java
Student s1 = new Student();
s1 = null;     // remote disconnected. Object has no remote now.
```

When **no reference** points to an object, it becomes **unreachable** and the Garbage Collector can free its memory.

```java
Student a = new Student();   // Object A
Student b = new Student();   // Object B
a = b;                       // 'a' now points to B. Object A is orphaned -> GC eligible
```

```
BEFORE a = b                       AFTER a = b
a --> [Object A]                   a --\
b --> [Object B]                   b ---+--> [Object B]
                                        [Object A]  <- nobody points here: garbage
```

### `==` vs `.equals()`

- `==` on references compares **addresses** (are they the same object?).
- `.equals()` compares **contents** (only if the class overrides it, like `String` does).

```java
String a = new String("hi");
String b = new String("hi");
System.out.println(a == b);        // false  (two different objects)
System.out.println(a.equals(b));   // true   (same content)
```

---

## 5. Size of an object / class (the big topic)

### 5.1 First, a clarification

People say "size of a class", but there are really three different things:

| Term | Meaning |
|---|---|
| **Size of the class** | The `.class` file / metadata in the Metaspace. Stored **once**, not per object. Rarely asked about. |
| **Size of one object (instance size)** | Memory one object takes on the heap. **This is what we calculate.** |
| **Retained size** | Object's own size **plus** everything only it keeps alive (its child objects). |

Java has **no `sizeof()`** operator like C/C++. The size depends on the JVM implementation, but on a typical **64-bit HotSpot JVM** the rules below hold. We will learn them and also see how to measure them.

### 5.2 What is inside an object?

```
+-----------------------------------------------+
|  OBJECT HEADER                                |
|    +---------------------------------------+  |
|    | Mark Word          (8 bytes)          |  |  <- hash code, GC age, lock info
|    +---------------------------------------+  |
|    | Class Pointer (4 bytes compressed,    |  |  <- points to class metadata
|    |                8 bytes uncompressed)  |  |
|    +---------------------------------------+  |
+-----------------------------------------------+
|  INSTANCE FIELDS  (only NON-static fields)    |
|    int a, double b, reference c, ...          |
+-----------------------------------------------+
|  PADDING (to make total a multiple of 8)      |
+-----------------------------------------------+
```

1. **Header = 12 bytes** (8 mark word + 4 class pointer) with the default *compressed class pointers*. It is 16 bytes if compression is off.
2. **Fields** = sum of the sizes of the instance fields.
3. **Padding** = extra empty bytes so the **total size is a multiple of 8** (the JVM aligns objects to 8-byte boundaries because it makes memory access faster).

> **What is NOT counted in object size:**
> - **Methods** (code is shared, stored once in the class metadata)
> - **Static fields** (belong to the class, stored once)
> - Local variables (those are on the stack)

### 5.3 Size of each field type

| Type | Size (bytes) |
|---|---|
| `boolean` | 1 |
| `byte` | 1 |
| `char` | 2 |
| `short` | 2 |
| `int` | 4 |
| `float` | 4 |
| `long` | 8 |
| `double` | 8 |
| **reference** (any object/array/String) | **4** (compressed oops, default when heap < 32 GB) or **8** |

Important: a reference field always costs only 4 or 8 bytes **no matter how big the object it points to is**. The pointed-to object is counted separately.

### 5.4 Worked examples (assume 64-bit JVM, compressed oops)

#### Example 1: empty class

```java
class Empty { }
```

```
Header 12 bytes + fields 0 = 12  -> pad to multiple of 8 -> 16 bytes
```

| Part | Bytes |
|---|---|
| Header | 12 |
| Fields | 0 |
| Padding | 4 |
| **Total** | **16** |

So even an empty object costs **16 bytes**.

#### Example 2: one int

```java
class A { int x; }
```

```
12 (header) + 4 (int) = 16  -> already multiple of 8 -> 16 bytes
```

#### Example 3: int + long

```java
class B { int x; long y; }
```

```
12 (header) + 4 (int x) = 16
16 + 8 (long y)          = 24   -> multiple of 8 -> 24 bytes
```

The JVM is clever here: it puts the 4-byte `int` right after the 12-byte header (filling the gap) so that the 8-byte `long` starts at an 8-aligned offset (16). No wasted bytes.

#### Example 4: mixed fields

```java
class C {
    int a;
    double b;
    boolean c;
}
```

```
Offset  0..11  : header (12)
Offset 12..15  : int a    (4)
Offset 16..23  : double b (8)
Offset 24      : boolean c (1)
Total so far   : 25 bytes -> pad to 32
```

| Part | Bytes |
|---|---|
| Header | 12 |
| `int a` | 4 |
| `double b` | 8 |
| `boolean c` | 1 |
| Padding | 7 |
| **Total** | **32** |

#### Example 5: object with a reference field

```java
class Student {
    int age;        // 4
    String name;    // 4 (reference, compressed)
}
```

```
12 + 4 + 4 = 20 -> pad to 24 bytes
```

This is the size of the `Student` **object only**. The `String` it points to lives separately (see retained size below).

#### Example 6: static fields don't count

```java
class D {
    static int count;   // NOT in object size
    int id;             // 4
}
```

```
12 + 4 = 16 bytes   (count is stored once with the class, not in every object)
```

### 5.5 Field reordering (why declaration order does not matter much)

The JVM is **free to reorder fields** to pack them tightly. HotSpot usually places larger types first (long/double), then int, short, char, byte/boolean, then references, filling gaps. So this:

```java
class X { byte a; long b; byte c; }
```

does **not** waste space the way you might expect from C/C++ struct padding rules. The JVM reorders for compactness. (Exact layout can vary between JVM versions, so treat the numbers as "typical", not guaranteed.)

### 5.6 Sizes of arrays

Arrays are objects too, with an extra **length** field in the header:

```
Array header = 12 (mark+class) + 4 (length) = 16 bytes
Array size   = 16 + (length x element size), rounded up to multiple of 8
```

| Array | Calculation | Size |
|---|---|---|
| `int[0]` | 16 | 16 |
| `int[10]` | 16 + 40 = 56 | 56 |
| `byte[5]` | 16 + 5 = 21 -> pad | 24 |
| `long[3]` | 16 + 24 = 40 | 40 |
| `Student[5]` (refs) | 16 + 5x4 = 36 -> pad | 40 (**only the reference slots**, not the Student objects) |

### 5.7 Wrapper objects vs primitives

```java
int a = 5;          // 4 bytes (on stack, or 4 inside an object)
Integer b = 5;      // reference (4) + Integer object (16) on the heap
```

`Integer` object = 12 header + 4 int value = **16 bytes**. That is 4x the memory of a raw `int` (plus the reference). This is why `int[1_000_000]` (~4 MB) is far lighter than `List<Integer>` with a million elements (~16 MB of Integer objects + ~4 MB of references + list overhead).

### 5.8 Shallow size vs Retained size (very important)

```java
class Address {
    String city;       // ref 4
    int pin;           // 4
}                      // 12 + 4 + 4 = 20 -> 24 bytes

class Student {
    int age;           // 4
    String name;       // ref 4
    Address address;   // ref 4
}                      // 12 + 4 + 4 + 4 = 24 bytes
```

```
 Student object (24 B)
   |-- name ----> String object (24 B) --> byte[] (24 B for "Rahul")
   '-- address -> Address object (24 B) --> String "Delhi" (24 B) --> byte[] (24 B)
```

- **Shallow size of Student** = **24 bytes** (just itself).
- **Retained size of Student** = 24 + (name: 24 + 24) + (address: 24 + city: 24 + 24) = **144 bytes** (if nothing else references those children).

Memory tools (VisualVM, Eclipse MAT, IntelliJ profiler) show both numbers. The shallow size is what the field-sum rule gives you; the retained size is what you would free if this object were garbage collected.

#### Size of a `String` (a favorite)

In modern Java (9+), `String` uses compact strings:

```
String object : 12 header + 4 (hash) + 4 (value ref) + 1 (coder) + 1 (hashIsZero) = 22 -> 24 bytes
byte[] "hello": 16 + 5 = 21 -> 24 bytes
Total for "hello" = 48 bytes
```

### 5.9 How to actually measure object size

**Option A: JOL (Java Object Layout)** by OpenJDK, the best learning tool.

```xml
<!-- Maven -->
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.17</version>
</dependency>
```

```java
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;

class C { int a; double b; boolean c; }

public class SizeDemo {
    public static void main(String[] args) {
        // Layout (shallow) of one object
        System.out.println(ClassLayout.parseClass(C.class).toPrintable());

        // Total (deep / retained) size of an object graph
        Student s = new Student();
        System.out.println(GraphLayout.parseInstance(s).totalSize());
    }
}
```

Sample output (typical):

```
C object internals:
OFF  SZ      TYPE DESCRIPTION               VALUE
  0   8           (object header: mark)
  8   4           (object header: class)
 12   4       int C.a
 16   8    double C.b
 24   1   boolean C.c
 25   7           (object alignment gap)
Instance size: 32 bytes
```

**Option B: `Instrumentation.getObjectSize()`** (needs a Java agent; gives shallow size).

**Option C: Rough runtime estimate** (not accurate, only a demonstration):

```java
Runtime rt = Runtime.getRuntime();
long before = rt.totalMemory() - rt.freeMemory();
Object[] arr = new Object[100_000];
for (int i = 0; i < arr.length; i++) arr[i] = new C();
long after = rt.totalMemory() - rt.freeMemory();
System.out.println((after - before) / 100_000.0 + " bytes per object (approx)");
```

> `sizeof` does not exist in Java. Anyone who asks "size of an object in Java" in an interview expects the **header + fields + padding** explanation.

### 5.10 Quick formula

```
Object size = round_up_to_8( header(12 or 16) + sum(instance field sizes) )
Array size  = round_up_to_8( 16 + length x element size )
```

---

## 6. Call by Value vs Call by Reference

### 6.1 The golden rule

> **Java is ALWAYS pass-by-value. There is no pass-by-reference in Java.**

What gets copied depends on the type:

| What you pass | What is copied |
|---|---|
| Primitive (`int`, `double`...) | The **value** itself |
| Object / array / String | The **value of the reference** (the address). The object is **not** copied. |

So for objects it is "pass the reference *by value*". The method gets its **own copy of the remote control**, pointing at the **same TV**.

### 6.2 Passing primitives

```java
public class PrimitiveDemo {
    static void change(int x) {
        x = 100;
        System.out.println("inside : " + x);   // 100
    }

    public static void main(String[] args) {
        int a = 10;
        change(a);
        System.out.println("outside: " + a);   // 10
    }
}
```

```
main() frame            change() frame
+---------+             +---------+
| a = 10  |  --copy-->  | x = 10  |   then x = 100
+---------+             +---------+
a stays 10 (only x changed; they are separate boxes)
```

### 6.3 Passing objects: mutating the object (change is visible)

```java
class Student { String name; Student(String n) { name = n; } }

public class RefDemo {
    static void rename(Student s) {
        s.name = "Priya";          // follows the address and changes the REAL object
    }

    public static void main(String[] args) {
        Student st = new Student("Rahul");
        rename(st);
        System.out.println(st.name);   // Priya
    }
}
```

```
main() frame              rename() frame            HEAP
+---------------+         +---------------+     +------------------+
| st = 0x1A ----+-------> |               |     |                  |
+---------------+    \    | s  = 0x1A ----+---> | Student @0x1A    |
                      \-------------------+---> |  name = "Priya"  |
                                              +------------------+
Both variables hold the same address (the address was copied by value),
so both reach the SAME object.
```

### 6.4 Passing objects: reassigning the reference (change is NOT visible)

```java
static void replace(Student s) {
    s = new Student("Amit");       // s now points to a NEW object
    System.out.println("inside : " + s.name);   // Amit
}

public static void main(String[] args) {
    Student st = new Student("Rahul");
    replace(st);
    System.out.println("outside: " + st.name);  // Rahul  (unchanged!)
}
```

```
BEFORE s = new Student("Amit")         AFTER s = new Student("Amit")

st --> [Rahul]                         st --> [Rahul]
s  --> [Rahul]  (same object)          s  --> [Amit]   (new object, only s moved)
```

The method's `s` is a **photocopy of the address**. Pointing the photocopy somewhere else does not move the original `st`.

### 6.5 The classic swap test (proves Java is pass-by-value)

```java
static void swap(Student a, Student b) {
    Student temp = a;
    a = b;
    b = temp;
}

public static void main(String[] args) {
    Student s1 = new Student("Rahul");
    Student s2 = new Student("Priya");
    swap(s1, s2);
    System.out.println(s1.name + " " + s2.name);   // Rahul Priya  (NOT swapped)
}
```

If Java were truly pass-by-reference (like C++ `&`), the swap would work. It does not, because only the copies swapped.

**How to make a swap work:** swap the *contents*, not the references:

```java
static void swapNames(Student a, Student b) {
    String temp = a.name;
    a.name = b.name;
    b.name = temp;
}
```

### 6.6 Arrays behave the same way

```java
static void modify(int[] arr) {
    arr[0] = 99;              // changes the real array
}
static void reassign(int[] arr) {
    arr = new int[]{7, 7, 7}; // only changes the local copy of the reference
}

public static void main(String[] args) {
    int[] data = {1, 2, 3};
    modify(data);
    System.out.println(data[0]);   // 99
    reassign(data);
    System.out.println(data[0]);   // still 99
}
```

### 6.7 Strings and wrappers: why changes "don't stick"

```java
static void change(String s) {
    s = s + " World";     // creates a NEW String object; s points to it
}

public static void main(String[] args) {
    String str = "Hello";
    change(str);
    System.out.println(str);   // Hello
}
```

Strings are **immutable**: `s + " World"` can never modify the original, it always builds a new String. Same for `Integer`, `Double`, etc. If you want a change to be visible, use a mutable type such as `StringBuilder`:

```java
static void change(StringBuilder sb) { sb.append(" World"); }
// caller's StringBuilder now contains "Hello World"
```

### 6.8 Summary table

| Scenario | Visible to caller? |
|---|---|
| Change a primitive parameter | No |
| Change a **field/element** of an object/array parameter | **Yes** |
| Reassign the parameter (`s = new ...`) | No |
| Modify a `String` / wrapper parameter | No (immutable) |
| Modify a `StringBuilder` / `List` / array parameter | **Yes** |

> **Why people think "Java is call by reference":** because modifying an object's contents inside a method is visible outside. But the *reason* is that the **address was copied**, not that the variable itself was passed by reference.

---

## 7. Copying objects: reference copy, shallow copy, deep copy

Three different levels. Understand them as a ladder.

```
Level 1: Reference copy  -> NO new object, just another remote
Level 2: Shallow copy    -> NEW outer object, but inner objects are SHARED
Level 3: Deep copy       -> NEW outer object AND NEW copies of every inner object
```

We will use this example throughout:

```java
class Address {
    String city;
    Address(String city) { this.city = city; }
}

class Student {
    String name;
    int age;
    Address address;      // a nested object
    Student(String name, int age, Address address) {
        this.name = name; this.age = age; this.address = address;
    }
}
```

Original object:

```java
Address addr = new Address("Delhi");
Student original = new Student("Rahul", 20, addr);
```

```
   original --> [ Student @A ]
                  name    -> "Rahul"
                  age     =  20
                  address -> [ Address @X : city "Delhi" ]
```

### 7.1 Reference copy (not a copy at all)

```java
Student copy = original;
```

```
   original ---\
                +--> [ Student @A ] --> [ Address @X ]
   copy -------/
```

One object, two names. Any change through one is seen via the other.

### 7.2 Shallow copy

A shallow copy creates a **new outer object** and copies the **field values** one by one. For primitive fields that means real independent copies. For reference fields it copies the **address**, so the inner object is **shared**.

#### Way 1: Copy constructor (shallow)

```java
Student(Student other) {
    this.name = other.name;         // String ref copied (safe: String immutable)
    this.age  = other.age;          // primitive copied
    this.address = other.address;   // SAME Address object shared!  <-- shallow
}

Student copy = new Student(original);
```

#### Way 2: `clone()` (default is shallow)

```java
class Student implements Cloneable {
    String name;
    int age;
    Address address;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();       // field-by-field copy = SHALLOW
    }
}

Student copy = (Student) original.clone();
```

Notes about `clone()`:
- The class must implement the marker interface `Cloneable`, otherwise `CloneNotSupportedException` is thrown.
- `Object.clone()` is `protected`, so you override it (and usually make it `public`).
- It does **not call the constructor**.
- Many Java experts consider `clone()` awkward and prefer copy constructors or factory methods.

#### The shallow copy diagram

```
   original --> [ Student @A ]               copy --> [ Student @B ]
                  name    ----+                          name    ----+
                  age = 20    |                          age = 20    |
                  address ----+---+                      address ----+---+
                              |   |                                  |   |
                              v   |                                  v   |
                        "Rahul"   +------------> [ Address @X ] <----+   |
                          (shared string)         city = "Delhi"         |
                                                  ^----------------------+
                                       BOTH students point to the SAME Address
```

#### The danger

```java
copy.age = 25;                    // independent (primitive)
System.out.println(original.age); // 20  OK

copy.address.city = "Mumbai";     // modifies the SHARED Address
System.out.println(original.address.city);   // Mumbai   (!!) original changed too
```

That is the classic shallow copy bug: **primitive fields are safe, nested mutable objects are shared.**

> Why `name` (a `String`) is "safe" even in a shallow copy: the String is shared, but since Strings are immutable, nobody can change it in place. `copy.name = "Priya"` just points `copy.name` to a new String and leaves `original.name` alone.

### 7.3 Deep copy

A deep copy creates a new outer object **and recursively new copies of every nested mutable object**. The two object graphs become completely independent.

#### Way 1: Manual copy constructor (most common, recommended)

```java
class Address {
    String city;
    Address(String city) { this.city = city; }

    Address(Address other) {                 // copy constructor for Address
        this.city = other.city;
    }
}

class Student {
    String name;
    int age;
    Address address;

    Student(Student other) {                 // DEEP copy constructor
        this.name = other.name;                       // immutable, fine to share
        this.age  = other.age;
        this.address = new Address(other.address);    // NEW Address object
    }
}

Student deep = new Student(original);
```

#### Way 2: Override `clone()` to deep copy

```java
class Address implements Cloneable {
    String city;
    Address(String city) { this.city = city; }

    @Override
    public Address clone() throws CloneNotSupportedException {
        return (Address) super.clone();      // fine: city is an immutable String
    }
}

class Student implements Cloneable {
    String name;
    int age;
    Address address;

    @Override
    public Student clone() throws CloneNotSupportedException {
        Student copy = (Student) super.clone();   // shallow first
        copy.address = this.address.clone();      // then replace shared parts with real copies
        return copy;
    }
}
```

#### Way 3: Serialization (copy through bytes)

Works for large graphs with many levels. All classes must implement `Serializable`.

```java
import java.io.*;

static <T extends Serializable> T deepCopy(T obj) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
        out.writeObject(obj);
    }
    try (ObjectInputStream in = new ObjectInputStream(
            new ByteArrayInputStream(bos.toByteArray()))) {
        return (T) in.readObject();
    }
}
```

Downsides: slow, `transient` fields are skipped, and classes need `Serializable`.

#### Way 4: Libraries / JSON round trip

Convert to JSON with Jackson or Gson and back, or use a mapper library. Simple for big graphs, but slower and needs extra dependencies.

#### The deep copy diagram

```
   original --> [ Student @A ]               deep --> [ Student @B ]
                  name ---> "Rahul" <------------------ name (shared immutable String OK)
                  age = 20                              age = 20
                  address --> [ Address @X ]            address --> [ Address @Y ]
                               city "Delhi"                          city "Delhi"
                          (independent from @Y)             (own copy)
```

```java
deep.address.city = "Mumbai";
System.out.println(original.address.city);   // Delhi  (unchanged, completely independent)
```

### 7.4 Side-by-side comparison

| | Reference copy | Shallow copy | Deep copy |
|---|---|---|---|
| New outer object? | No | Yes | Yes |
| New inner objects? | No | No (shared) | Yes (all nested) |
| Primitive fields | Same object | Independent | Independent |
| Nested object changes affect original? | Yes | **Yes** | No |
| Speed / memory | Fastest | Fast | Slowest |
| Typical tool | `b = a` | copy constructor, default `clone()` | custom copy constructor, overridden `clone()`, serialization |

### 7.5 Copying arrays (very common trap)

```java
int[] a = {1, 2, 3};

int[] r = a;                       // reference copy (same array)
int[] c1 = a.clone();              // new array (fine for primitives)
int[] c2 = Arrays.copyOf(a, a.length);
int[] c3 = new int[a.length];
System.arraycopy(a, 0, c3, 0, a.length);
```

For **primitive** arrays, `clone()` / `copyOf` / `arraycopy` give a true independent copy. But for **object arrays** they are **shallow**:

```java
Address[] arr  = { new Address("Delhi"), new Address("Pune") };
Address[] copy = arr.clone();      // new array, SAME Address objects inside

copy[0].city = "Goa";
System.out.println(arr[0].city);   // Goa  (shared element objects!)
```

```
arr  --> [ ref | ref ]               copy --> [ ref | ref ]
           |     |                              |     |
           v     v                              v     v
        [Delhi] [Pune]  <-----------------  same two objects
```

**2D arrays** are arrays of arrays, so `clone()` on the outer array is shallow too:

```java
int[][] m = {{1, 2}, {3, 4}};
int[][] shallow = m.clone();       // inner rows shared!
shallow[0][0] = 99;
System.out.println(m[0][0]);       // 99

// Deep copy of a 2D array:
int[][] deep = new int[m.length][];
for (int i = 0; i < m.length; i++) {
    deep[i] = m[i].clone();
}
```

### 7.6 Collections too

```java
List<Address> list = new ArrayList<>();
list.add(new Address("Delhi"));

List<Address> shallow = new ArrayList<>(list);   // new list, SAME Address objects

// Deep copy:
List<Address> deep = new ArrayList<>();
for (Address a : list) {
    deep.add(new Address(a));                    // copy each element
}
```

### 7.7 Decision guide

```
Do the object's fields hold only primitives and immutable types (String, Integer, LocalDate)?
        |
        +-- YES --> shallow copy is already safe (effectively a full copy)
        |
        +-- NO (has mutable nested objects / arrays / collections)
                |
                +-- Do you need full independence? --> DEEP copy
                +-- Is sharing intentional (e.g., shared config)? --> shallow is OK
```

---

## 8. Immutability and why it makes copying easy

An **immutable** object cannot change after creation. Examples: `String`, `Integer`, `LocalDate`, records with immutable components.

```java
final class Address {
    private final String city;
    Address(String city) { this.city = city; }
    String getCity() { return city; }
    // no setters, so nobody can change it
}
```

If every nested object is immutable, **sharing them is harmless**, so a shallow copy behaves like a deep copy and you save memory too. This is why `Student(Student other)` can safely do `this.name = other.name;` for a `String`.

Rules to make a class immutable:
1. Declare the class `final`.
2. Make all fields `private final`.
3. No setters.
4. For mutable fields (like arrays/lists), copy them in the constructor and return copies from getters (defensive copying).

```java
final class Team {
    private final List<String> members;

    Team(List<String> members) {
        this.members = new ArrayList<>(members);           // defensive copy in
    }
    List<String> getMembers() {
        return new ArrayList<>(members);                   // defensive copy out
    }
}
```

---

## 9. Interview questions with answers

**Q1. Is Java pass-by-value or pass-by-reference?**
Always pass-by-value. For objects, the value that is copied is the reference (address), so the method gets a copy of the address pointing to the same object.

**Q2. Then why can a method modify my object?**
Because the copied reference still points to the same object on the heap. Changing the object's fields through it is visible to the caller. But reassigning the parameter (`s = new ...`) only changes the local copy.

**Q3. Can you write a swap method for two objects in Java?**
Not by swapping the parameter references, since that only affects the local copies. You can swap the contents (fields), or wrap them in an array/holder object and swap inside it.

**Q4. What is the size of an empty object in Java?**
16 bytes on a typical 64-bit JVM with compressed class pointers (12-byte header + 4 bytes padding).

**Q5. Does the number of methods increase object size?**
No. Methods are stored once with the class metadata. Static fields also don't count. Only instance fields do.

**Q6. Does a reference field's size depend on the size of the object it points to?**
No. It is always 4 bytes (compressed) or 8 bytes. The target object is a separate allocation.

**Q7. Why is padding added?**
The JVM aligns objects to 8-byte boundaries so the CPU can access memory faster and addresses can be compressed.

**Q8. Difference between shallow and deep copy?**
Shallow copy duplicates the outer object but shares nested objects. Deep copy duplicates the whole graph so the copies are fully independent.

**Q9. Is `Object.clone()` deep or shallow?**
Shallow. You must override it and copy mutable fields yourself to get a deep copy.

**Q10. Why are copy constructors preferred over `clone()`?**
`clone()` needs `Cloneable` (a marker with no methods), bypasses constructors, throws a checked exception, and is hard to get right with `final` fields. Copy constructors are explicit, type-safe and simple.

**Q11. What does `==` compare for objects?**
Reference equality (same address). Use `.equals()` for logical equality.

**Q12. When does an object become eligible for garbage collection?**
When it is no longer reachable from any live reference (for example, after `ref = null` or the reference's scope ends and nothing else points to it).

**Q13. Is `arr.clone()` a deep copy of an `Address[]`?**
No. It creates a new array but the elements still point to the same `Address` objects (shallow).

**Q14. Why does `String` behave "differently" when passed to a method?**
Because it is immutable. Any "change" creates a new String, and the method's local reference points to it while the caller's reference is unaffected.

---

## 10. Cheat sheet and common mistakes

### Memory in one picture

```
   STACK                              HEAP
   +------------------+          +-----------------------------+
   | primitive locals |          | objects (header+fields+pad) |
   | references  -----+--------> | arrays (16B header + data)  |
   +------------------+          +-----------------------------+
                                  Garbage Collector cleans unreachable objects
```

### Size formulas

```
Object = round_up_to_8( 12 header + instance fields )          // 16 with no compression
Array  = round_up_to_8( 16 + length x element size )
Empty object = 16 B | Integer = 16 B | int[10] = 56 B | "hello" String ~ 48 B total
```

### Pass-by-value rules

```
Primitive  -> value copied, caller never affected
Object     -> address copied, object shared
             modify inside object  -> caller sees it
             reassign parameter    -> caller does NOT see it
```

### Copy rules

```
b = a              -> same object (reference copy)
copy constructor   -> shallow unless you copy nested objects yourself
clone()            -> shallow by default
deep copy          -> new copy of every nested MUTABLE object
```

### Common mistakes

| Mistake | Fix |
|---|---|
| Thinking `b = a` copies the object | It copies only the address. Use a copy constructor / clone. |
| Expecting `swap(a, b)` to swap caller's variables | Swap contents, or return values / use an array holder. |
| Believing `clone()` gives a deep copy | Override it and clone nested objects manually. |
| Using `arr.clone()` on object arrays or 2D arrays and assuming independence | Copy each element / row yourself. |
| Counting static fields or methods in object size | Only instance fields (+ header + padding) count. |
| Forgetting that reference fields cost only 4/8 bytes | The referenced object is counted separately (retained size). |
| Comparing objects with `==` | Use `.equals()` (and override `equals`/`hashCode`). |
| Assuming sizes are guaranteed by the language | They depend on the JVM, flags (compressed oops) and version. Use JOL to verify. |

### One-line takeaways

1. A reference variable is a **remote control**, the object is the **TV**.
2. Object size = **header + instance fields + padding** (multiple of 8). Empty object = **16 bytes**.
3. Java is **always pass-by-value**; for objects the value is the **address**.
4. **Shallow copy** shares inner objects; **deep copy** duplicates them all.
5. **Immutable** nested objects make shallow copies safe.

---
*End of notes. Practice tip: add JOL to a small project and print `ClassLayout.parseClass(...)` for your own classes, then compare the output with the formulas above.*
