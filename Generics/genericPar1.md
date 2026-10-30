# Java Generics and Bounded Types

> Why generics exist, how they give compile-time type safety, how to write generic classes and methods, and how bounded types (`extends`, multiple bounds) let you safely call methods on generic objects.

---

## Table of Contents

1. [Type Safety: the core concept](#1-type-safety-the-core-concept)
2. [Upcasting vs Downcasting](#2-upcasting-vs-downcasting)
3. [Why we need generics (the problem)](#3-why-we-need-generics-the-problem)
4. [Generic classes](#4-generic-classes)
5. [Generic methods](#5-generic-methods)
6. [Type inference and the diamond operator](#6-type-inference-and-the-diamond-operator)
7. [Bounded types (upper bounds)](#7-bounded-types-upper-bounds)
8. [Multiple bounds](#8-multiple-bounds)
9. [Behind the scenes: type erasure](#9-behind-the-scenes-type-erasure)
10. [Important rules and limitations](#10-important-rules-and-limitations)
11. [A quick note on wildcards](#11-a-quick-note-on-wildcards)
12. [Interview questions and answers](#12-interview-questions-and-answers)
13. [Cheat sheet](#13-cheat-sheet)

---

## 1. Type Safety: the core concept

Java is a **strictly typed** language. Every variable has a type, and the compiler enforces rules about how types may be used.

```java
Integer a = 10;
String  s = "hello";

Integer b = s;   // COMPILE ERROR: cannot treat a String as an Integer
```

**Type safety** = the language prevents invalid operations on data (like treating a `String` as an `Integer`).

### Compile-time vs runtime errors

```
 Write code --> COMPILE TIME --> RUNTIME
                (javac)          (JVM running)

 Error caught here  ^            Error caught here
 = GOOD (cheap,                  = BAD (crash in production,
   fix before shipping)            found by users)
```

**The primary goal of generics: move type errors from runtime to compile time**, which makes code far more robust.

---

## 2. Upcasting vs Downcasting

Assume this hierarchy:

```
        Animal
        /    \
      Dog    Cat
```

### Upcasting (specific -> general)

```java
Dog d = new Dog();
Animal a = d;        // upcast: Dog IS-A Animal
```

- **Safe** (every Dog is an Animal).
- **Automatic**: no cast syntax needed.

### Downcasting (general -> specific)

```java
Animal a = new Dog();
Dog d = (Dog) a;     // downcast: manual cast required
```

- **Risky**: the compiler can't be sure what the object really is.
- Requires **manual** cast.
- If the real object doesn't match, you get a **`ClassCastException` at runtime**.

```java
Animal a = new Cat();
Dog d = (Dog) a;     // compiles fine, CRASHES at runtime: ClassCastException
```

```
            UPCAST (safe, automatic)
   Dog  ------------------------------>  Animal
   Dog  <------------------------------  Animal
            DOWNCAST (risky, manual cast,
            ClassCastException possible)
```

The same thing happens with `Object`, the parent of every class:

```java
Object o = "hello";
Integer i = (Integer) o;   // compiles, but ClassCastException at runtime
```

> Remember this: **before generics, using `Object` forced you into dangerous downcasts.** That is exactly the problem generics solve.

---

## 3. Why we need generics (the problem)

Imagine you want a reusable container class, a `Box`, that can hold *anything*.

### The pre-generics solution: use `Object`

```java
class Box {
    private Object value;

    public void set(Object value) { this.value = value; }
    public Object get()           { return value; }
}
```

Usage:

```java
Box box = new Box();
box.set("Hello");            // OK, a String is an Object

String s = (String) box.get();   // must cast manually
```

### Problems this causes

```
+---------------------------------------------------------------+
| 1. Loss of specific type information                          |
|    box.get() returns Object, not String.                      |
|                                                               |
| 2. Mandatory, dangerous casting on every retrieval            |
|    String s = (String) box.get();                             |
|                                                               |
| 3. Runtime errors if the cast is wrong                        |
+---------------------------------------------------------------+
```

The bug in action:

```java
Box box = new Box();
box.set(123);                    // someone stores an Integer

String s = (String) box.get();   // compiles perfectly...
                                 // ClassCastException at RUNTIME
```

The compiler **cannot warn you**, because `Box` accepts any `Object`.

```
box.set(123)  --->  [ Box: Object value ]  --->  (String) box.get()
   Integer              Integer inside              cast to String
                                                         |
                                                         v
                                              ClassCastException (runtime)
```

### The other bad option

Create `StringBox`, `IntegerBox`, `DogBox`, ... a separate class for every type. That means massive code duplication.

**Generics give us one class + full type safety.**

---

## 4. Generic classes

A generic class declares a **type parameter** in angle brackets.

```java
class Box<T> {                 // T = "Type parameter" (placeholder)
    private T value;

    public void set(T value) { this.value = value; }
    public T get()           { return value; }
}
```

### Using it

```java
Box<String> sbox = new Box<>();
sbox.set("Hello");
String s = sbox.get();          // NO cast needed

Box<Integer> ibox = new Box<>();
ibox.set(123);
int n = ibox.get();             // NO cast needed
```

### The compiler now protects you

```java
Box<String> sbox = new Box<>();
sbox.set(123);   // COMPILE ERROR: incompatible types: int cannot be converted to String
```

The bug that used to crash at runtime is now caught **at compile time**.

```
        Box<T>  (one class definition)
          |
   +------+-------+----------+
   v              v          v
Box<String>   Box<Integer>  Box<Dog>
 T = String    T = Integer   T = Dog
 set(String)   set(Integer)  set(Dog)
 String get()  Integer get() Dog get()
```

### Naming conventions for type parameters

| Letter | Meaning |
|---|---|
| `T` | Type |
| `E` | Element (collections) |
| `K` | Key |
| `V` | Value |
| `N` | Number |
| `R` | Return type |

### Multiple type parameters

```java
class Pair<K, V> {
    private K key;
    private V value;

    Pair(K key, V value) { this.key = key; this.value = value; }
    K getKey()   { return key; }
    V getValue() { return value; }
}

Pair<String, Integer> p = new Pair<>("age", 25);
String k = p.getKey();     // String, no cast
Integer v = p.getValue();  // Integer, no cast
```

---

## 5. Generic methods

A **method** can have its own type parameter, independent of the class. The type parameter is declared **before the return type**.

```java
public static <T> void printArray(T[] arr) {
    for (T item : arr) {
        System.out.println(item);
    }
}
```

Usage:

```java
Integer[] nums  = {1, 2, 3};
String[]  words = {"a", "b", "c"};

printArray(nums);     // T = Integer
printArray(words);    // T = String
```

### Anatomy

```
   public static  <T>   void   printArray ( T[] arr )
                   |     |                   |
      declares the type  return type    uses T as parameter type
      parameter (before
      the return type)
```

### Generic method returning T

```java
public static <T> T firstElement(T[] arr) {
    return arr[0];
}

String s  = firstElement(new String[]{"x", "y"});    // returns String
Integer i = firstElement(new Integer[]{1, 2});       // returns Integer
```

Note: a generic method can live in a **non-generic** class, and a generic class can also have generic methods.

---

## 6. Type inference and the diamond operator

Modern Java (Java 7+ for the diamond, improved in 8+) can **infer** type arguments from context so you don't repeat yourself.

```java
// Old, verbose
Box<String> b1 = new Box<String>();

// Diamond operator <>: compiler infers String from the left side
Box<String> b2 = new Box<>();
```

For methods:

```java
printArray(new Integer[]{1, 2, 3});   // compiler infers T = Integer
// same as: Main.<Integer>printArray(...)  (explicit, rarely needed)
```

The compiler looks at the arguments (or the assignment target) and works out `T` for you, making the code cleaner without losing type safety.

---

## 7. Bounded types (upper bounds)

### The problem

With plain `T`, the compiler knows nothing about it except that it's an `Object`:

```java
class Calculator<T> {
    T value;

    double doubled() {
        return value.doubleValue() * 2;   // COMPILE ERROR
                                          // T could be anything; does it have doubleValue()?
    }
}
```

### The solution: restrict T with `extends`

```java
class Calculator<T extends Number> {
    T value;

    Calculator(T value) { this.value = value; }

    double doubled() {
        return value.doubleValue() * 2;   // OK: T is guaranteed to be a Number
    }
}
```

`T extends Number` means: **T must be `Number` or any subclass of `Number`.**

```
                  Number
          +---------+---------+----------+
          v         v         v          v
       Integer    Double     Long       Float  ...
       
  Calculator<Integer>   OK
  Calculator<Double>    OK
  Calculator<String>    COMPILE ERROR (String is not a Number)
```

```java
Calculator<Integer> c1 = new Calculator<>(10);       // OK
Calculator<Double>  c2 = new Calculator<>(2.5);      // OK
Calculator<String>  c3 = new Calculator<>("hi");     // COMPILE ERROR
```

### Note on the keyword

In a bound, `extends` is used for **both** classes and interfaces:

```java
<T extends Number>        // T is a subclass of Number
<T extends Comparable<T>> // T implements Comparable (interface, still "extends")
```

### Bounded generic method

```java
public static <T extends Number> double sum(T[] arr) {
    double total = 0;
    for (T n : arr) {
        total += n.doubleValue();    // allowed because of the bound
    }
    return total;
}

sum(new Integer[]{1, 2, 3});        // 6.0
sum(new Double[]{1.5, 2.5});        // 4.0
sum(new String[]{"a"});             // COMPILE ERROR
```

### Another classic: `Comparable`

```java
public static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;    // compareTo is guaranteed to exist
}

max(3, 7);              // 7
max("apple", "pear");   // pear
```

### Why bounds matter

```
Without bound:   T  ==>  compiler only knows Object methods
                         (toString, equals, hashCode ...)

With bound:      T extends Number  ==>  compiler knows Number methods too
                                        (intValue, doubleValue ...)
                 So you can call them SAFELY, with no cast.
```

---

## 8. Multiple bounds

A type parameter can have **more than one** constraint, joined with `&`:

```java
interface Swimmable { void swim(); }

class Animal { void eat() { System.out.println("eating"); } }

class Dog extends Animal implements Swimmable {
    public void swim() { System.out.println("dog swimming"); }
}

class Pool<T extends Animal & Swimmable> {
    T animal;

    Pool(T animal) { this.animal = animal; }

    void use() {
        animal.eat();     // from Animal
        animal.swim();    // from Swimmable
    }
}
```

`T extends Animal & Swimmable` means: **T must extend `Animal` AND implement `Swimmable`.**

```
         Animal          Swimmable
            \             /
             \           /
              v         v
        T must satisfy BOTH

  Pool<Dog>      OK  (Dog extends Animal, implements Swimmable)
  Pool<Cat>      ERROR if Cat is not Swimmable
  Pool<Fish>     ERROR if Fish does not extend Animal
```

### Rules for multiple bounds

1. **If a class is among the bounds, it must come first.**
   ```java
   <T extends Animal & Swimmable>     // OK
   <T extends Swimmable & Animal>     // COMPILE ERROR (class not first)
   ```
2. **At most one class**, because Java has single inheritance for classes. All the rest must be **interfaces**.
   ```java
   <T extends Animal & Swimmable & Runnable & Comparable<T>>   // OK
   <T extends Animal & Cat>                                    // ERROR (two classes)
   ```
3. You can list many interfaces with `&`.

### Benefit

Because the compiler **guarantees** the object satisfies every bound, you can call methods from **all** of them without casting or `instanceof` checks.

---

## 9. Behind the scenes: type erasure

Generics exist **only at compile time**. The compiler checks types, then **erases** them, and the bytecode contains no `T`. This is called **type erasure**.

```
SOURCE                            AFTER ERASURE (bytecode view)
------                            -----------------------------
class Box<T> {                    class Box {
    T value;                          Object value;
    T get() {...}                     Object get() {...}
}                                 }

Box<String> b;                    Box b;
String s = b.get();               String s = (String) b.get();   // compiler inserts the cast
```

With a bound, `T` is erased to **its bound** instead of `Object`:

```
class Calculator<T extends Number>   ->   field type becomes Number
class Pool<T extends Animal & Swimmable> -> erased to the FIRST bound (Animal);
                                           the compiler inserts casts for Swimmable
```

Key insight: the compiler inserts the casts **for you, safely**, only after proving at compile time that they can't fail. That is why generics are both flexible and safe.

---

## 10. Important rules and limitations

| Rule | Example |
|---|---|
| **No primitives** as type arguments | `Box<int>` is an error. Use `Box<Integer>` (autoboxing handles conversion). |
| **Can't instantiate `T`** | `new T()` is an error (type is erased). |
| **Can't create generic arrays** | `new T[10]` is an error. |
| **Can't use `instanceof` with parameterized type** | `obj instanceof Box<String>` is an error. |
| **Static members can't use the class's `T`** | `static T value;` is an error. |
| **Generics are invariant** | `List<Dog>` is **not** a `List<Animal>`, even though `Dog` is an `Animal`. |

### Invariance (why `List<Dog>` is not `List<Animal>`)

```java
List<Dog> dogs = new ArrayList<>();
List<Animal> animals = dogs;     // COMPILE ERROR
```

If it were allowed:

```java
animals.add(new Cat());   // would put a Cat into a list of Dogs!
Dog d = dogs.get(0);      // ClassCastException, defeats the whole purpose
```

So the compiler forbids it, keeping type safety intact.

---

## 11. A quick note on wildcards

(Not in the video summary, but you'll meet them right after bounded types.)

The `?` wildcard means "some unknown type" and has its own bounds:

| Syntax | Name | Meaning | Typical use |
|---|---|---|---|
| `List<?>` | unbounded | list of anything | read-only, don't care about type |
| `List<? extends Number>` | upper-bounded | list of Number or subclasses | **reading** (producer) |
| `List<? super Integer>` | lower-bounded | list of Integer or its supertypes | **writing** (consumer) |

```java
static double sumAll(List<? extends Number> list) {
    double t = 0;
    for (Number n : list) t += n.doubleValue();
    return t;
}

sumAll(List.of(1, 2, 3));        // List<Integer> works
sumAll(List.of(1.5, 2.5));       // List<Double> works
```

Memory aid, **PECS**: *Producer Extends, Consumer Super*.

Note: a **type parameter** (`<T ...>`) can have an upper bound (`extends`) but **not** a lower bound. Lower bounds (`super`) exist only on wildcards.

---

## 12. Interview questions and answers

**Q1. What are generics and why were they introduced?**
A way to parameterize classes, interfaces and methods by type. They give compile-time type safety and remove manual casting, so errors that used to be `ClassCastException`s at runtime are caught by the compiler.

**Q2. What is the difference between upcasting and downcasting?**
Upcasting goes from a specific type to a general one (safe, automatic). Downcasting goes from general to specific (needs an explicit cast and can throw `ClassCastException`).

**Q3. What problem did using `Object` containers cause?**
Loss of type information, forced casts on retrieval, and runtime `ClassCastException` if the cast was wrong.

**Q4. What is a generic method? How is it declared?**
A method with its own type parameter, declared before the return type: `public static <T> void print(T[] a)`.

**Q5. What is a bounded type parameter?**
A type parameter restricted with `extends`, e.g. `T extends Number`, so only that type or its subtypes are accepted and its methods can be called safely.

**Q6. How do you specify multiple bounds? Any rules?**
`T extends Animal & Swimmable`. Only one class is allowed, and it must come first; the rest must be interfaces.

**Q7. Can you use `extends` for interfaces in a bound?**
Yes. In generic bounds, `extends` covers both classes and interfaces.

**Q8. What is type erasure?**
The compiler removes generic type information after checking; `T` becomes `Object` (or its first bound) in bytecode, and casts are inserted automatically.

**Q9. Why can't we use primitives like `List<int>`?**
Type arguments must be reference types because of erasure to `Object`. Use wrappers like `List<Integer>`.

**Q10. Is `List<Dog>` a subtype of `List<Animal>`?**
No. Generics are invariant. Use wildcards (`List<? extends Animal>`) for flexibility.

**Q11. What is the diamond operator?**
`<>` lets the compiler infer type arguments: `Box<String> b = new Box<>();`.

**Q12. Can a generic type parameter have a lower bound?**
No. Lower bounds (`super`) apply only to wildcards, not to type parameters.

---

## 13. Cheat sheet

```
PROBLEM          Object containers -> casts -> runtime ClassCastException
SOLUTION         Generics -> compiler checks types -> errors at COMPILE time

GENERIC CLASS    class Box<T> { T value; }
                 Box<String> b = new Box<>();

GENERIC METHOD   public static <T> void print(T[] arr) { ... }
                 (type parameter goes BEFORE the return type)

TYPE INFERENCE   new Box<>()       compiler infers the type
                 print(array)      compiler infers T

UPPER BOUND      <T extends Number>              T is Number or a subclass
                 <T extends Comparable<T>>       T implements Comparable

MULTIPLE BOUNDS  <T extends Animal & Swimmable>
                 - class first (at most one), then interfaces
                 - T must satisfy ALL bounds

BENEFIT          Compiler guarantees the hierarchy, so you can call
                 Number / Animal / Swimmable methods with NO cast.

UNDER THE HOOD   Type erasure: T -> Object (or first bound), casts auto-inserted

LIMITS           no primitives (use Integer), no new T(), no new T[],
                 List<Dog> is NOT List<Animal>

WILDCARDS        ? extends X (read/producer)   ? super X (write/consumer)
```

---

*End of notes.*
