# Java Generics: Invariance and Wildcards (`?`, `? extends T`, `? super T`)

> Your video summary lost its angle brackets when you pasted it (it said "a `List` is not a subclass of `List`"). I understood it as: **`Dog` is a subclass of `Animal`, but `List<Dog>` is NOT a subclass of `List<Animal>`.**
> This guide explains that idea, and the three kinds of wildcards that solve the problem, **from first principles**: for every rule we start with **the problem**, work out **what we need**, and then **derive** the rule ourselves. By the end, you should be able to rebuild the rules on a blank page without memorizing them.

---

## Table of Contents

1. [How to Read This Guide (First-Principles Method)](#1-how-to-read-this-guide-first-principles-method)
2. [Quick Recap: Why Generics Exist](#2-quick-recap-why-generics-exist)
3. [The Problem: Inheritance Does Not Carry Over to Generics](#3-the-problem-inheritance-does-not-carry-over-to-generics)
4. [Proving Why `List<Dog>` Cannot Be a `List<Animal>` (Invariance)](#4-proving-why-listdog-cannot-be-a-listanimal-invariance)
5. [Why Arrays Are Different (and Dangerous)](#5-why-arrays-are-different-and-dangerous)
6. [The Key Insight: Reading Is Safe, Writing Is Dangerous](#6-the-key-insight-reading-is-safe-writing-is-dangerous)
7. [Wildcards: The Big Picture](#7-wildcards-the-big-picture)
8. [Unbounded Wildcard `?`](#8-unbounded-wildcard-)
9. [Upper Bounded Wildcard `? extends T`](#9-upper-bounded-wildcard--extends-t)
10. [Lower Bounded Wildcard `? super T`](#10-lower-bounded-wildcard--super-t)
11. [PECS: Producer Extends, Consumer Super](#11-pecs-producer-extends-consumer-super)
12. [Type Erasure: Why the Compiler Does All the Work](#12-type-erasure-why-the-compiler-does-all-the-work)
13. [Wildcard Capture: What the Compiler Does Internally](#13-wildcard-capture-what-the-compiler-does-internally)
14. [Wildcards vs Generic Methods (Type Parameters)](#14-wildcards-vs-generic-methods-type-parameters)
15. [Real JDK Signatures Explained](#15-real-jdk-signatures-explained)
16. [Advanced Notes and Rules](#16-advanced-notes-and-rules)
17. [Version-by-Version History](#17-version-by-version-history)
18. [Practical Programs](#18-practical-programs)
19. [Common Mistakes](#19-common-mistakes)
20. [Cheat Sheet, Summary and Quiz](#20-cheat-sheet-summary-and-quiz)

---

## 1. How to Read This Guide (First-Principles Method)

**First-principles thinking** means: don't memorize rules, **rebuild** them from basic facts.

For each topic we ask three questions:

```
 ① THE PROBLEM   →   What breaks, or what is painful, without this?
 ② THE NEED      →   What would a solution have to guarantee?
 ③ THE SOLUTION  →   Oh, that's exactly the rule Java chose!
```

### The learning path

```
 STEP 1   Subtyping works for classes: Dog is an Animal. Does it work for List<Dog>?
    │
 STEP 2   NO. We PROVE why (the cat-in-the-dog-list argument). This is called INVARIANCE.
    │
 STEP 3   But now useful code (like printing a list of any animals) is blocked. Pain!
    │
 STEP 4   Insight: the danger comes only from WRITING. Reading is safe.
    │
 STEP 5   So invent "reference types that promise to only read" and "only write":
            ? extends T    →  read only (producer)
            ? super T      →  write only (consumer)
            ?              →  I don't care about the type
    │
 STEP 6   Combine them into one rule: PECS
    │
 STEP 7   Understand why it's the COMPILER's job: type erasure
```

---

## 2. Quick Recap: Why Generics Exist

### ① The problem (before Java 5)

```java
List list = new ArrayList();          // a list of "anything" (Objects)
list.add("hello");
list.add(42);                         // oops: the compiler allows mixing

String s = (String) list.get(1);      // compiles, but ClassCastException at RUNTIME
```

The compiler couldn't help. Bugs showed up **while the program ran**, often in production.

### ② The need

Tell the compiler **what the list is supposed to contain**, so it can **reject mistakes at compile time** and **remove the casts**.

### ③ The solution: generics (Java 5, 2004)

```java
List<String> list = new ArrayList<>();
list.add("hello");
list.add(42);                         // ✗ COMPILE ERROR: int cannot be converted to String

String s = list.get(0);               // no cast needed
```

### Generic basics in 60 seconds

```java
// a generic CLASS: T is a placeholder (type parameter)
class Box<T> {
    private T item;
    void set(T item) { this.item = item; }
    T get() { return item; }
}

Box<String> b = new Box<>();          // T = String
b.set("hi");
String s = b.get();

// a generic METHOD
static <T> T firstOf(List<T> list) { return list.get(0); }

// a BOUNDED type parameter: T must be a Number (or subclass)
static <T extends Number> double doubleOf(T n) { return n.doubleValue() * 2; }
```

| Letter | Convention |
|--------|-----------|
| `T` | Type |
| `E` | Element (collections) |
| `K`, `V` | Key, Value |
| `N` | Number |
| `R` | Return type |
| `?` | **Not a type parameter**: it's a **wildcard** (the topic of this guide) |

### The classes we will use in all examples

```java
class Animal { String name; Animal(String name) { this.name = name; } }
class Dog extends Animal { Dog(String name) { super(name); } }
class Cat extends Animal { Cat(String name) { super(name); } }
class Puppy extends Dog  { Puppy(String name) { super(name); } }
```

```
               Object
                 ▲
              Animal
             ▲      ▲
           Dog      Cat
            ▲
          Puppy
```

---

## 3. The Problem: Inheritance Does Not Carry Over to Generics

### 3.1 Inheritance works for plain objects

```java
Animal a = new Dog("Rex");            // ✓ a Dog IS an Animal
```

### 3.2 So we expect it to work for lists... but it doesn't

```java
List<Dog> dogs = new ArrayList<>();
List<Animal> animals = dogs;          // ✗ COMPILE ERROR
// incompatible types: List<Dog> cannot be converted to List<Animal>
```

Even though `Dog extends Animal`:

```
   Dog  ──is-a──►  Animal                ✓ (inheritance)

   List<Dog>  ──is-a──►  List<Animal>    ✗ NOT TRUE in Java
```

> **`List<Dog>` and `List<Animal>` are two completely unrelated types.** Neither is a subtype of the other. This property is called **invariance**.

### 3.3 It's about the container, not about what goes inside

A common confusion: you **can** put a `Dog` into a `List<Animal>`:

```java
List<Animal> animals = new ArrayList<>();
animals.add(new Dog("Rex"));          // ✓ fine: a Dog is an Animal
animals.add(new Cat("Tom"));          // ✓ fine
```

The rule says nothing against that. Invariance is only about **assigning one list type to another list type**.

### 3.4 The pain this causes

```java
static void introduceAll(List<Animal> animals) {
    for (Animal a : animals) {
        System.out.println("Meet " + a.name);
    }
}

List<Dog> dogs = List.of(new Dog("Rex"), new Dog("Fido"));
introduceAll(dogs);                   // ✗ COMPILE ERROR! A method for "animals" can't accept dogs
```

That method only **reads**. It would be perfectly safe. But the compiler refuses. That is the pain we want to fix. But first, why does Java refuse? Is it just being strict?

---

## 4. Proving Why `List<Dog>` Cannot Be a `List<Animal>` (Invariance)

Let's **pretend** Java allowed it (this idea is called **covariance**) and see what breaks.

### 4.1 The proof by contradiction

```java
List<Dog> dogs = new ArrayList<>();
List<Animal> animals = dogs;          // ← PRETEND this compiled

animals.add(new Cat("Tom"));          // legal for List<Animal>: a Cat IS an Animal

Dog d = dogs.get(0);                  // dogs promises "only Dogs", but there's a Cat inside!
                                      // → ClassCastException: Cat cannot be cast to Dog
```

```
   ONE object in the heap, TWO names:

   dogs    ──►  ┌─────────────────────┐  ◄──  animals
   (promises    │  ArrayList          │       (accepts ANY Animal)
    "only       │   [ Cat("Tom") ]    │
    Dogs")      └─────────────────────┘

   The "dogs" view promised only Dogs. The "animals" view put a Cat in.
   The promise is BROKEN. This must be stopped.
```

Both views point to the **same list**. One view says "only dogs inside", the other view lets anyone add. The two views contradict each other.

### 4.2 The reverse direction fails too

What about treating a `List<Animal>` as a `List<Dog>` (this idea is called **contravariance**)?

```java
List<Animal> animals = new ArrayList<>();
animals.add(new Cat("Tom"));

List<Dog> dogs = animals;             // ← PRETEND this compiled
Dog d = dogs.get(0);                  // reads a Cat as a Dog → ClassCastException
```

So **neither direction** is safe for a list we can both **read from** and **write to**.

### 4.3 Conclusion: invariance

The only safe rule for a read-and-write container is: **the type argument must match exactly.**

| Term | Meaning | Example | Java for generics |
|------|---------|---------|:-----------------:|
| **Covariant** | Subtype relation goes the **same way** | `List<Dog>` is a `List<Animal>` | ✗ (not by default) |
| **Contravariant** | Subtype relation goes the **opposite way** | `List<Animal>` is a `List<Dog>` | ✗ (not by default) |
| **Invariant** | **No** relation | `List<Dog>` and `List<Animal>` are unrelated | **✓ default** |

### 4.4 Why this matters in the real world

The compiler is protecting you from a bug that would otherwise appear **far from where it was caused**: the `Cat` is added in one place, and the crash happens when some other code reads the list, maybe much later.

---

## 5. Why Arrays Are Different (and Dangerous)

### 5.1 Arrays ARE covariant

```java
Animal[] animals = new Dog[2];        // ✓ COMPILES: Dog[] is an Animal[]
animals[0] = new Dog("Rex");          // ✓
animals[1] = new Cat("Tom");          // ✓ COMPILES! (a Cat is an Animal)
                                      // ✗ but throws ArrayStoreException at RUNTIME
```

So arrays chose the **"pretend it's allowed"** path, which we just showed is unsafe. How do arrays survive?

### 5.2 Because arrays remember their real type (they are "reified")

An array object **knows at runtime** what it really holds. A `new Dog[2]` knows it's a `Dog[]`. When you store a `Cat`, the JVM **checks** and throws **`ArrayStoreException`**:

```
 Animal[] animals = new Dog[2];
                     └── the real object is a Dog[]  (the JVM remembers this)

 animals[1] = new Cat("Tom");   → JVM: "this array is Dog[], a Cat doesn't fit" → ArrayStoreException
```

The bug is caught, but **only at runtime**, which is exactly what generics wanted to avoid.

### 5.3 Why were arrays made covariant? (history)

Java 1.0 had **no generics**. Methods like `Arrays.sort(Object[] a)` needed to accept **any** array (`String[]`, `Integer[]`...). The only way was to make `String[]` a subtype of `Object[]`. It was a practical trick, and the **runtime check** was added to keep it safe.

When generics arrived (Java 5), the designers **learned from the mistake**: generic types are **invariant by default**, and flexibility is added **safely** through wildcards.

### 5.4 Arrays vs generics: the comparison

| | Arrays | Generics |
|---|--------|----------|
| Subtyping | **Covariant**: `Dog[]` is an `Animal[]` | **Invariant**: `List<Dog>` is not a `List<Animal>` |
| Knows its element type at runtime? | **Yes** (reified) | **No** (erased, section 12) |
| Where is the mistake caught? | **Runtime** (`ArrayStoreException`) | **Compile time** |
| Why this design? | Needed before generics existed | Compile-time safety is the whole point |

> **Deep link:** because generic types are **erased at runtime**, the JVM **cannot** do the check that arrays do. So generics **must** be invariant: the compiler is the only guard. (We'll see this in section 12.)

---

## 6. The Key Insight: Reading Is Safe, Writing Is Dangerous

Let's look at our "proof" again and ask: **which operation caused the disaster?**

```java
List<Animal> animals = dogs;          // (pretend)
animals.add(new Cat("Tom"));          // ← THE WRITE caused the disaster
```

What if the `animals` view only allowed **reading**?

```java
List<Dog> dogs = ...;
READ-ONLY-VIEW<Animal> view = dogs;   // pretend: a view that can only read
Animal a = view.get(0);               // ✓ perfectly safe: every Dog IS an Animal
```

**Reading through a supertype view is totally safe**, because whatever comes out is a `Dog`, and a `Dog` can always be treated as an `Animal`.

Now the opposite: what if a view only allowed **writing**?

```java
List<Animal> animals = ...;
WRITE-ONLY-VIEW<Dog> sink = animals;  // pretend: a view that can only write
sink.add(new Dog("Rex"));             // ✓ perfectly safe: a Dog fits in a list of Animals
```

**Writing a Dog into an Animal-list is totally safe**, because the list accepts any Animal, and a Dog is one.

### The derivation

| Direction | Danger comes from | If we forbid it, is it safe? |
|-----------|-------------------|------------------------------|
| Treat `List<Dog>` as a list of `Animal` (covariance) | **writing** (can add a Cat) | **Yes**, if the view is **read-only** |
| Treat `List<Animal>` as a list of `Dog` (contravariance) | **reading** (can read a Cat as a Dog) | **Yes**, if the view is **write-only** |

### ② The need

A way to tell the compiler: **"through this reference I will only read"** (then allow covariance) or **"through this reference I will only write"** (then allow contravariance), and have the compiler **enforce** that promise by **blocking the dangerous operation**.

### ③ The solution: wildcards

```
   "I will only READ"   →   List<? extends Animal>    (covariance, writing blocked)
   "I will only WRITE"  →   List<? super Dog>         (contravariance, reading limited)
   "I don't care"       →   List<?>                   (any list, almost nothing allowed)
```

That's the whole idea of this guide. Everything below is the details.

---

## 7. Wildcards: The Big Picture

### 7.1 What is a wildcard?

A **wildcard** is the **question mark `?`**. It stands for **an unknown type**. Read `List<?>` as "a list of **some** type, I don't know which."

### 7.2 The three flavors

| Syntax | Name | Meaning | Allows |
|--------|------|---------|--------|
| `List<?>` | **Unbounded** wildcard | A list of **some unknown** type | Any list |
| `List<? extends T>` | **Upper-bounded** wildcard | A list of `T` **or any subclass of T** | `List<T>`, `List<Sub1>`, `List<Sub2>`... |
| `List<? super T>` | **Lower-bounded** wildcard | A list of `T` **or any superclass of T** | `List<T>`, `List<Super1>`, `Object` list... |

### 7.3 Picture: where the bounds point on the hierarchy

```
               Object
                 ▲                      ? super Dog      → Dog, Animal, Object   (go UP)
              Animal
             ▲      ▲                   ? extends Dog    → Dog, Puppy            (go DOWN)
           Dog      Cat
            ▲                           ?                → everything
          Puppy
```

```
                      ┌─────────────────────────────────────────────┐
   ? super Dog        │  Object  ◄─ Animal  ◄─ Dog                  │
                      └─────────────────────────────────────────────┘
                                                   ┌────────────────────────┐
   ? extends Dog                                   │   Dog ─► Puppy         │
                                                   └────────────────────────┘
                                         (both include Dog itself: the bound is inclusive)
```

### 7.4 The subtype picture (this is how wildcards fix invariance)

```
                          List<?>      (same as List<? extends Object>)
                              ▲
                  List<? extends Animal>
           ┌──────────────────┼──────────────────┐
           ▲                  ▲                  ▲
     List<Animal>         List<Dog>          List<Cat>       ← still unrelated to each other
```

Even though `List<Dog>` is not a `List<Animal>`, **both are** a `List<? extends Animal>`. That's the bridge we needed.

```java
List<Dog> dogs = new ArrayList<>();
List<Animal> animals = new ArrayList<>();

List<? extends Animal> view1 = dogs;         // ✓
List<? extends Animal> view2 = animals;      // ✓
```

### 7.5 What can you do through each reference? (memorize this table)

| Reference type | **Read** (get) returns | **Write** (add) accepts |
|----------------|------------------------|-------------------------|
| `List<T>` (exact) | `T` | `T` (and subtypes) |
| `List<?>` | `Object` | **only `null`** |
| `List<? extends T>` | `T` | **only `null`** |
| `List<? super T>` | `Object` | `T` (and subtypes of T) |

Below we derive each row ourselves.

---

## 8. Unbounded Wildcard `?`

(This is section II of your video outline.)

### 8.1 ① The problem

You want a method that works with **any** list, and **doesn't care** about the element type: printing it, counting its elements, checking if it's empty.

```java
static void printAll(List<Object> list) { ... }       // ✗ accepts ONLY List<Object>, not List<String>!

List<String> names = List.of("a", "b");
printAll(names);                                      // ✗ compile error (invariance!)
```

`List<Object>` is **not** a super-type of `List<String>` (invariance). So that signature is useless.

### 8.2 ② The need

A parameter type that accepts a list of **anything**.

### 8.3 ③ The solution: `List<?>`

```java
static void printAll(List<?> list) {
    for (Object o : list) {                           // elements are read as Object
        System.out.print(o + " ");
    }
    System.out.println();
}

printAll(List.of("a", "b"));                          // ✓ List<String>
printAll(List.of(1, 2, 3));                           // ✓ List<Integer>
printAll(List.of(new Dog("Rex")));                    // ✓ List<Dog>
```

### 8.4 What you can and can't do with `List<?>`

```java
List<?> list = new ArrayList<String>(List.of("a", "b"));

Object o = list.get(0);        // ✓ reading: you get Object (the only type guaranteed for ANY list)
String s = list.get(0);        // ✗ ERROR: Object cannot be converted to String
int n = list.size();           // ✓ methods that don't involve the element type are fine
list.clear();                  // ✓ fine
list.contains("a");            // ✓ fine (contains takes Object)

list.add("x");                 // ✗ COMPILE ERROR
list.add(new Object());        // ✗ COMPILE ERROR
list.add(null);                // ✓ the ONLY thing you can add
```

### 8.5 Derive it: why can you read only `Object` and write only `null`?

`List<?>` means "a list of *some unknown type*, call it **X**." The actual list could be `List<String>`, `List<Integer>`, `List<Dog>`...

**Reading:** what is the one type that every possible `X` is guaranteed to be? **`Object`**. So `get` returns `Object`.

**Writing:** to add an item, it must fit the **unknown** `X`. Let's test some values:

| Value to add | Fits `List<String>`? | Fits `List<Integer>`? | Fits **every** possible list? |
|--------------|:--------------------:|:---------------------:|:-----------------------------:|
| `"hello"` | ✓ | ✗ | ✗ |
| `42` | ✗ | ✓ | ✗ |
| `new Object()` | ✗ | ✗ | ✗ |
| **`null`** | ✓ | ✓ | **✓** (null fits every reference type) |

Since the compiler doesn't know which list it really is, it only allows what is safe for **all** of them: **`null`**.

```
   List<?>  ─ "unknown X" ─►   read:  safe as Object only
                               write: safe only for null
```

### 8.6 `List<?>` vs `List<Object>` vs raw `List`

These three look similar but are very different:

| | `List<Object>` | `List<?>` | `List` (raw) |
|---|----------------|-----------|--------------|
| Accepts a `List<String>` argument? | ✗ **No** (invariance) | ✓ **Yes** | ✓ yes (but unsafe) |
| Can you **add** items? | ✓ anything | ✗ only `null` | ✓ anything (unchecked warning) |
| Reads as | `Object` | `Object` | `Object` |
| Type-safe? | ✓ | ✓ | ✗ **No** (turns off checking) |
| Meaning | "a list that holds **anything**" | "a list of **one specific unknown** type" | legacy, avoid |

```java
List<Object> a = new ArrayList<String>();    // ✗ ERROR (invariance)
List<?> b = new ArrayList<String>();         // ✓
```

**Never use raw types** (`List` without `<>`) in new code. `List<?>` is the **safe** way to say "any list".

### 8.7 When to use `?`

- The method **doesn't depend on the element type**: `size()`, `isEmpty()`, `clear()`, printing, counting `null`s.
- You only need the elements **as `Object`**.
- `Class<?>`, `Map<String, ?>`, reflection code.

```java
static int countNulls(List<?> list) {
    int count = 0;
    for (Object o : list) if (o == null) count++;
    return count;
}
```

---

## 9. Upper Bounded Wildcard `? extends T`

(This is section III of your video outline.)

### 9.1 ① The problem (from section 3.4)

```java
static void introduceAll(List<Animal> animals) { ... }

introduceAll(dogsList);          // ✗ rejected, even though the method only READS
```

And a classic: summing numbers.

```java
static double sum(List<Number> nums) { ... }

List<Integer> ints = List.of(1, 2, 3);
sum(ints);                       // ✗ List<Integer> is not a List<Number>
```

### 9.2 ② The need

Accept a list of `Animal` **or any subtype** (`Dog`, `Cat`, `Puppy`), as long as I **only read** from it.

### 9.3 ③ The solution: `List<? extends Animal>`

```java
static void introduceAll(List<? extends Animal> animals) {
    for (Animal a : animals) {                        // ✓ each element is at least an Animal
        System.out.println("Meet " + a.name);
    }
}

introduceAll(List.of(new Dog("Rex"), new Dog("Fido")));      // ✓ List<Dog>
introduceAll(List.of(new Cat("Tom")));                       // ✓ List<Cat>
introduceAll(List.of(new Animal("Generic")));                // ✓ List<Animal>
introduceAll(List.of(new Puppy("Bit")));                     // ✓ List<Puppy>
```

```java
static double sum(List<? extends Number> nums) {
    double total = 0;
    for (Number n : nums) {
        total += n.doubleValue();
    }
    return total;
}

System.out.println(sum(List.of(1, 2, 3)));         // 6.0   (List<Integer>)
System.out.println(sum(List.of(1.5, 2.5)));        // 4.0   (List<Double>)
System.out.println(sum(List.of(10L, 20L)));        // 30.0  (List<Long>)
```

### 9.4 Read it in plain English

> **`List<? extends Animal>`** = "a list of **some unknown type that is `Animal` or a subclass of `Animal`**."

```
   ? extends Animal  accepts:   List<Animal>   List<Dog>   List<Cat>   List<Puppy>   ...
                     rejects:   List<Object>   List<String>   (not Animals)
```

The bound is **inclusive**: `? extends Animal` includes `Animal` itself.

### 9.5 Derive the READ rule: why `get()` returns `T`

The unknown type X is `Animal`, or `Dog`, or `Cat`, or `Puppy`... **Every** one of those **is an `Animal`**. So whatever `get()` hands back is **definitely an `Animal`**:

```java
List<? extends Animal> list = dogs;
Animal a = list.get(0);        // ✓ always safe
Dog d = list.get(0);           // ✗ ERROR: the list might really be a List<Cat>!
Object o = list.get(0);        // ✓ (an Animal is an Object)
```

So reading gives you the **upper bound `T`**.

### 9.6 Derive the WRITE rule: why you can add only `null`

```java
List<? extends Animal> list = new ArrayList<Dog>();   // we know it's a Dog list here, but...
list.add(new Dog("Rex"));      // ✗ COMPILE ERROR!
list.add(new Cat("Tom"));      // ✗ COMPILE ERROR
list.add(new Animal("X"));     // ✗ COMPILE ERROR
list.add(null);                // ✓ only null
```

Why is even `Dog` rejected? Because the compiler looks at the **declared** type `List<? extends Animal>`, **not** at what you assigned. As far as the compiler knows, the list could be **any** of these:

| If the real list is... | Adding a `Dog` is... | Adding a `Cat` is... | Adding an `Animal` is... |
|------------------------|:--------------------:|:--------------------:|:------------------------:|
| `List<Animal>` | ✓ ok | ✓ ok | ✓ ok |
| `List<Dog>` | ✓ ok | ✗ **breaks** | ✗ **breaks** |
| `List<Cat>` | ✗ **breaks** | ✓ ok | ✗ **breaks** |
| `List<Puppy>` | ✗ **breaks** | ✗ **breaks** | ✗ **breaks** |

An item is allowed only if it's safe for **every** row. **No** real value is safe in all four rows. Only **`null`** is.

```
   The compiler must protect the WORST case. Since it can't know which case it is,
   it blocks ALL writes. (Same logic as the unbounded wildcard.)
```

### 9.7 So what can you call on a `List<? extends T>`?

| Method | Allowed? | Why |
|--------|:--------:|-----|
| `get(i)` | ✓ returns `T` | Reading is safe |
| `size()`, `isEmpty()`, `iterator()`, `stream()` | ✓ | Don't write elements |
| `contains(Object)`, `indexOf(Object)`, `remove(Object)` | ✓ | Their parameter is `Object`, not `E` |
| `add(E)`, `set(int, E)`, `addAll(Collection<? extends E>)` | ✗ | Parameter involves the unknown type |
| `add(null)`, `clear()` | ✓ | `null` fits anything; `clear` takes nothing |

### 9.8 Read-only means "this method won't write", not "the list is immutable"

`List<? extends T>` is about **your reference**, not the list itself. Someone else holding a normal `List<Dog>` reference to the **same list** can still add to it. The wildcard only restricts **you** (the method) from writing, which is exactly the promise we wanted ("I will only read").

### 9.9 Use cases

| Use case | Example |
|----------|---------|
| Sum, average, max over numbers | `double sum(List<? extends Number>)` |
| Print or process a list of a base type | `void printAll(Collection<? extends Animal>)` |
| Copy **from** a source | `copy(dest, List<? extends T> src)` |
| Methods that **produce** values for you | `addAll(Collection<? extends E> c)` |

### 9.10 Works for interfaces too

`extends` in a wildcard means **"is a subtype of"**, so it's used for **both classes and interfaces**:

```java
static void printAll(List<? extends Comparable<?>> items) { ... }
static void run(List<? extends Runnable> tasks) { for (Runnable r : tasks) r.run(); }
```

---

## 10. Lower Bounded Wildcard `? super T`

(This is section IV of your video outline.)

### 10.1 ① The problem

Now the opposite need: a method that **adds** `Dog`s to a list. Which lists should it accept?

```java
static void addDogs(List<Dog> target) {
    target.add(new Dog("Rex"));
}

List<Animal> animals = new ArrayList<>();
addDogs(animals);                 // ✗ REJECTED (invariance)... but adding Dogs to a list of Animals is perfectly safe!

List<Object> objects = new ArrayList<>();
addDogs(objects);                 // ✗ REJECTED... also perfectly safe!
```

### 10.2 ② The need

Accept a list of `Dog` **or any supertype** (`Animal`, `Object`), as long as I **only write `Dog`s into it**.

### 10.3 ③ The solution: `List<? super Dog>`

```java
static void addDogs(List<? super Dog> target) {
    target.add(new Dog("Rex"));            // ✓
    target.add(new Puppy("Bit"));          // ✓ a Puppy is a Dog
}

List<Dog> dogs = new ArrayList<>();
List<Animal> animals = new ArrayList<>();
List<Object> objects = new ArrayList<>();

addDogs(dogs);                             // ✓ List<Dog>
addDogs(animals);                          // ✓ List<Animal>
addDogs(objects);                          // ✓ List<Object>
```

```java
static void fillWithInts(List<? super Integer> target, int n) {
    for (int i = 1; i <= n; i++) {
        target.add(i);                     // ✓ writing Integers
    }
}

List<Number> numbers = new ArrayList<>();
fillWithInts(numbers, 3);                  // ✓ List<Number>
System.out.println(numbers);               // [1, 2, 3]
```

### 10.4 Read it in plain English

> **`List<? super Dog>`** = "a list of **some unknown type that is `Dog` or a superclass of `Dog`**."

```
   ? super Dog   accepts:   List<Dog>   List<Animal>   List<Object>
                 rejects:   List<Puppy>   List<Cat>   List<String>
```

The bound is again **inclusive**: `? super Dog` includes `Dog`.

### 10.5 Derive the WRITE rule: why you can add `Dog` (and subtypes)

The unknown type X is `Dog`, `Animal`, or `Object`. Test which values are safe to add **in every case**:

| Value to add | Fits `List<Dog>`? | Fits `List<Animal>`? | Fits `List<Object>`? | Safe for **all**? |
|--------------|:-----------------:|:--------------------:|:--------------------:|:-----------------:|
| `new Dog()` | ✓ | ✓ | ✓ | **✓** |
| `new Puppy()` | ✓ | ✓ | ✓ | **✓** |
| `new Animal()` | ✗ | ✓ | ✓ | ✗ |
| `new Cat()` | ✗ | ✓ | ✓ | ✗ |
| `new Object()` | ✗ | ✗ | ✓ | ✗ |

A `Dog` (or any subtype of `Dog`) fits **every** possible list, because **every possible list holds `Dog` or something more general**. So writing **`T` and its subtypes** is safe.

```
   Every candidate list is "at least as general as Dog".
   A Dog can always go into a container of something more general.
```

### 10.6 Derive the READ rule: why you only get `Object`

```java
List<? super Dog> list = ...;
Object o = list.get(0);        // ✓
Animal a = list.get(0);        // ✗ ERROR
Dog d = list.get(0);           // ✗ ERROR
```

The real list might be `List<Dog>` (items are Dogs), `List<Animal>` (items could be Cats!), or `List<Object>` (items could be Strings!). The **only** type guaranteed for all of them is **`Object`**.

```
   what could come OUT?   Dog? Animal? Cat? String? ... → only "Object" is always true
```

### 10.7 So what can you call on a `List<? super T>`?

| Method | Allowed? | Notes |
|--------|:--------:|-------|
| `add(T)` and `add` of a subtype | ✓ | The point of this wildcard |
| `get(i)` | ✓ but returns **`Object`** | Rarely useful |
| `size()`, `clear()`, `isEmpty()` | ✓ | |
| `add(superTypeOfT)` e.g. `add(new Animal())` | ✗ | Might break `List<Dog>` |

### 10.8 Use cases

| Use case | Example |
|----------|---------|
| Adding / filling a **destination** list | `void fill(List<? super Integer> out)` |
| Collecting results | `Collection<? super E> sink` |
| Comparators for a type | `Comparator<? super T>` (a `Comparator<Animal>` can compare `Dog`s) |
| Copy **to** a destination | `copy(List<? super T> dest, ...)` |

### 10.9 A subtle rule: `super` only works on wildcards

You can write `? super T`, but **not** `<T super X>` when declaring a type parameter:

```java
static <T super Dog> void f() { }        // ✗ ILLEGAL: type parameters have only "extends" bounds
static void f(List<? super Dog> list) { }// ✓ legal: wildcards can use "super"
```

---

## 11. PECS: Producer Extends, Consumer Super

### 11.1 The rule

We derived two separate rules. They combine into one easy-to-remember principle (from *Effective Java*):

> **PECS = Producer Extends, Consumer Super**

| If the collection... | It is a... | Use | You can... |
|----------------------|------------|-----|------------|
| **gives you** values (you **read**/`get` from it) | **Producer** | **`? extends T`** | read as `T` |
| **takes** values from you (you **write**/`add` to it) | **Consumer** | **`? super T`** | write `T` |
| **both** gives and takes | both | **no wildcard**: `List<T>` | read and write `T` |
| neither (type doesn't matter) | n/a | **`?`** | use as `Object` |

```
        ┌───────────────┐   "extends"          ┌──────────┐
        │  SOURCE list  │ ───── produces ────► │  YOUR    │
        │ ? extends T   │       T values       │  CODE    │
        └───────────────┘                      └────┬─────┘
                                                    │ consumes T values
                                      "super"       ▼
                                             ┌───────────────┐
                                             │ DESTINATION   │
                                             │  ? super T    │
                                             └───────────────┘
```

### 11.2 The classic example: `copy`

```java
static <T> void copy(List<? super T> dest, List<? extends T> src) {
    for (T item : src) {          // src PRODUCES T      → ? extends T
        dest.add(item);           // dest CONSUMES T     → ? super T
    }
}
```

```java
List<Integer> ints = List.of(1, 2, 3);
List<Double> doubles = List.of(1.5, 2.5);
List<Object> objects = new ArrayList<>();

copy(objects, ints);               // ✓ T = Integer: List<Object> is "? super Integer", List<Integer> is "? extends Integer"
copy(objects, doubles);            // ✓ T = Double
System.out.println(objects);       // [1, 2, 3, 1.5, 2.5]
```

Without wildcards (`copy(List<T>, List<T>)`), you could only copy between lists of **exactly the same type**.

### 11.3 Designing your own class with PECS: a stack

```java
import java.util.*;

class MyStack<E> {
    private final ArrayList<E> items = new ArrayList<>();

    void push(E e)       { items.add(e); }
    E pop()              { return items.remove(items.size() - 1); }
    boolean isEmpty()    { return items.isEmpty(); }

    // src PRODUCES elements for us  → ? extends E
    void pushAll(Iterable<? extends E> src) {
        for (E e : src) push(e);
    }

    // dst CONSUMES elements from us → ? super E
    void popAll(Collection<? super E> dst) {
        while (!isEmpty()) dst.add(pop());
    }
}
```

```java
MyStack<Number> stack = new MyStack<>();
stack.pushAll(List.of(1, 2, 3));          // ✓ Iterable<Integer> accepted because of ? extends Number
stack.pushAll(List.of(4.5, 5.5));         // ✓ Iterable<Double> accepted too

List<Object> sink = new ArrayList<>();
stack.popAll(sink);                       // ✓ Collection<Object> accepted because of ? super Number
System.out.println(sink);                 // [5.5, 4.5, 3, 2, 1]
```

If we had written `pushAll(Iterable<E> src)` and `popAll(Collection<E> dst)`, **both calls above would fail to compile**: `List<Integer>` is not an `Iterable<Number>`, and `List<Object>` is not a `Collection<Number>`.

### 11.4 `Comparator<? super T>`: PECS in everyday code

```java
Comparator<Animal> byName = (a, b) -> a.name.compareTo(b.name);

List<Dog> dogs = new ArrayList<>(List.of(new Dog("Zed"), new Dog("Abe"), new Dog("Max")));
dogs.sort(byName);        // ✓ List<Dog>.sort expects Comparator<? super Dog>, and Comparator<Animal> qualifies
```

Why does this make sense? A comparator that knows how to compare **any `Animal`** can certainly compare **`Dog`s**. The comparator is a **consumer** of `Dog`s (it takes two Dogs in), so it is `? super Dog`.

### 11.5 Decision flowchart

```
 Inside my method, what do I do with this collection parameter?

   Only READ items from it ───────────────► List<? extends T>
   Only ADD items to it ──────────────────► List<? super T>
   Both read AND add (same type T) ───────► List<T>   (no wildcard; use a type parameter if needed)
   Only care about size/clear/null/Object ► List<?>
```

> **Simple memory trick:** *"If you **get** from it, use **extends**. If you **put** into it, use **super**. If you do **both**, use neither."* (This is called the **Get and Put Principle**.)

---

## 12. Type Erasure: Why the Compiler Does All the Work

### 12.1 ① The problem

Why can't Java just do what arrays do (check at runtime)? Why must it be so strict at compile time?

### 12.2 What is type erasure?

Generics were added in Java 5 **without changing the JVM**, so old code and new code could work together. The compiler **erases** (removes) generic type information after checking it:

```java
// What you write
List<String> list = new ArrayList<>();
list.add("hello");
String s = list.get(0);

// What the compiler effectively produces (conceptually)
List list = new ArrayList();
list.add("hello");
String s = (String) list.get(0);      // a cast is inserted for you
```

At **runtime**, `ArrayList<String>`, `ArrayList<Integer>` and `ArrayList<Dog>` are **all just `ArrayList`**:

```java
List<String> strings = new ArrayList<>();
List<Integer> ints = new ArrayList<>();
System.out.println(strings.getClass() == ints.getClass());    // true  (both are ArrayList)
```

### 12.3 ② The consequence: the JVM can't catch the mistake

```
 Arrays:     new Dog[2]            → the object KNOWS it's a Dog[] → JVM can throw ArrayStoreException
 Generics:   new ArrayList<Dog>()  → the object is just an ArrayList (the <Dog> is gone) → JVM can't check anything
```

Since the JVM can't protect us at runtime, the **compiler must prevent** the dangerous situation **in advance**. That is exactly **why generics are invariant**, and why wildcards block `add`: the compiler is the **only** guard.

### 12.4 Things erasure forbids (and why)

| Not allowed | Why |
|-------------|-----|
| `new T()` | At runtime `T` doesn't exist, so the JVM wouldn't know what to create |
| `new T[10]` | Same: and arrays need to know their element type |
| `new List<String>[10]` | **Generic array creation**: would reintroduce the unsafe covariance + erasure mix |
| `obj instanceof List<String>` | At runtime it's only a `List`; the `<String>` is gone. (`obj instanceof List<?>` is fine) |
| `static T field;` in `class Box<T>` | `T` belongs to each **instance**; static is shared by all |
| `void f(List<String>)` and `void f(List<Integer>)` in one class | Both erase to `f(List)`: "name clash: same erasure" |
| `catch (T e)` | Exceptions are checked by runtime type |
| `int` as a type argument: `List<int>` | Type arguments must be reference types (use `Integer`) |

What **is** allowed: `List<?>[] arr = new List<?>[10];` (unbounded wildcard arrays are safe because they promise nothing).

### 12.5 Heap pollution: breaking the rules with raw types

If you use a **raw type**, you turn the checking off, and erasure shows its teeth:

```java
List<String> strings = new ArrayList<>();
List raw = strings;                   // raw type: the compiler stops checking (warning)
raw.add(42);                          // compiles; NO error here: an Integer is now inside a List<String>!

String s = strings.get(0);            // ClassCastException HERE, far from the real mistake
```

This state, where a variable of a parameterized type points to an object that violates it, is called **heap pollution**. The crash happens at the **read**, not at the mistake, exactly like our "cat in the dog list" story.

### 12.6 Where the type information still exists

- In the **compiled class file** as signatures (so the compiler and reflection can read the declared generic types of fields, methods and class declarations).
- **Not** on individual **instances** at runtime.

### 12.7 Checking at runtime when you need it

If you need the type at runtime, pass a `Class<T>` token:

```java
static <T> T create(Class<T> type) throws Exception {
    return type.getDeclaredConstructor().newInstance();
}
String s = create(String.class);
```

---

## 13. Wildcard Capture: What the Compiler Does Internally

### 13.1 Capture

When the compiler sees `List<?>`, it quietly gives the unknown type a **temporary name**, called a **capture** (shown in errors as `CAP#1`). Each use of a wildcard gets its **own** fresh capture.

```java
List<?> list = new ArrayList<String>();
list.add("x");
// error: incompatible types: String cannot be converted to CAP#1
//        where CAP#1 is a fresh type-variable: CAP#1 extends Object from capture of ?
```

Read the error as: *"the list holds some unknown type CAP#1. A `String` is not known to be CAP#1."*

### 13.2 A famous problem: `swap` on `List<?>`

```java
static void swap(List<?> list, int i, int j) {
    Object tmp = list.get(i);
    list.set(i, list.get(j));          // ✗ ERROR: Object cannot be converted to CAP#1
    list.set(j, tmp);
}
```

We want a method that works for **any** list but writes back. The compiler refuses: it only knows items as `Object`.

### 13.3 The capture helper pattern

Delegate to a **generic helper method**. The helper's type parameter `T` **captures** the unknown type, giving it a name we can use:

```java
static void swap(List<?> list, int i, int j) {
    swapHelper(list, i, j);            // the compiler captures ? as T
}

private static <T> void swapHelper(List<T> list, int i, int j) {
    T tmp = list.get(i);
    list.set(i, list.get(j));          // ✓ now it's List<T>: both read and write the SAME T
    list.set(j, tmp);
}
```

```java
List<String> names = new ArrayList<>(List.of("a", "b", "c"));
swap(names, 0, 2);
System.out.println(names);             // [c, b, a]
```

Callers see a **clean wildcard signature**; internally a **named type parameter** makes the write legal. The JDK's `Collections.swap` is written exactly this way.

---

## 14. Wildcards vs Generic Methods (Type Parameters)

Both can express flexibility. When should you use which?

### 14.1 They overlap

```java
// wildcard version
static double sum(List<? extends Number> nums) { ... }

// type-parameter version
static <T extends Number> double sum(List<T> nums) { ... }
```

Both accept `List<Integer>`, `List<Double>`... Both work. The wildcard version is **simpler** and preferred, because `T` is never used again.

### 14.2 Rule: use a type parameter when the type is needed more than once

**Use a wildcard** when the type appears **once** (you don't need to name it).

**Use a type parameter** when you must **link** several places, such as two parameters, or a parameter and the return type:

```java
// Need to link the return type to the element type → type parameter
static <T> T first(List<T> list) { return list.get(0); }

// Need to link two parameters to the SAME type → type parameter
static <T> void swapElements(List<T> list, T a, T b) { ... }

// The element type appears once → wildcard is enough
static int count(List<?> list) { return list.size(); }
```

| Situation | Choose |
|-----------|--------|
| Type appears once, you just want flexibility | **Wildcard** |
| Type links parameters or return type | **Type parameter** `<T>` |
| You need to both read and write the same type | **Type parameter** `List<T>` |
| You want multiple bounds `T extends A & B` | **Type parameter** (wildcards allow only one bound) |
| You need the capture to write (section 13) | **Helper with type parameter** |

### 14.3 Multiple bounds only for type parameters

```java
static <T extends Number & Comparable<T>> T maxOf(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}

System.out.println(maxOf(3, 7));         // 7
System.out.println(maxOf(2.5, 1.5));     // 2.5
```

```java
List<? extends Number & Comparable<?>> x;   // ✗ ILLEGAL: a wildcard has ONE bound only
```

### 14.4 Combining both

```java
static <T> void copy(List<? super T> dest, List<? extends T> src)    // T links the two wildcards
```

### 14.5 Don't use wildcards in return types

```java
static List<? extends Animal> getAnimals() { ... }     // ✗ forces every caller to deal with wildcards
static List<Animal> getAnimals() { ... }                // ✓ clearer
```

Wildcards are for **parameters** (the flexibility you give to **callers**), not return values.

---

## 15. Real JDK Signatures Explained

Now you can read the JDK's own method signatures. Each one uses PECS.

### 15.1 `Collections.copy`

```java
public static <T> void copy(List<? super T> dest, List<? extends T> src)
```

`src` produces `T`s (extends); `dest` consumes `T`s (super). Exactly our `copy`.

### 15.2 `Collection.addAll`

```java
boolean addAll(Collection<? extends E> c)
```

A `Collection<Number>` can absorb a `List<Integer>`: the argument **produces** `E`s.

```java
List<Number> numbers = new ArrayList<>();
numbers.addAll(List.of(1, 2, 3));          // List<Integer> accepted
numbers.addAll(List.of(4.5, 5.5));         // List<Double> accepted
System.out.println(numbers);               // [1, 2, 3, 4.5, 5.5]
```

### 15.3 `Collections.addAll`

```java
public static <T> boolean addAll(Collection<? super T> c, T... elements)
```

The collection `c` **consumes** `T`s (super).

### 15.4 `List.sort` / `Collections.sort` with a comparator

```java
void sort(Comparator<? super E> c)
public static <T> void sort(List<T> list, Comparator<? super T> c)
```

A comparator is a **consumer** of elements, so `? super`. (Section 11.4.)

### 15.5 `Collections.max`

```java
public static <T> T max(Collection<? extends T> coll, Comparator<? super T> comp)
```

`coll` produces `T`s (extends), `comp` consumes `T`s (super).

### 15.6 The famous `Comparable<? super T>`

```java
public static <T extends Comparable<? super T>> void sort(List<T> list)
```

Read from the inside out: *"`T` must be comparable to **itself or one of its supertypes**."* Example: `Dog extends Animal`, and `Animal implements Comparable<Animal>`. Then `Dog` is **not** `Comparable<Dog>`, but it **is** `Comparable<Animal>`, and `Animal` is a supertype of `Dog`, so `Dog` qualifies. Sorting `List<Dog>` therefore works.

```java
class Animal implements Comparable<Animal> {
    String name;
    Animal(String name) { this.name = name; }
    public int compareTo(Animal o) { return name.compareTo(o.name); }
    public String toString() { return name; }
}
class Dog extends Animal { Dog(String n) { super(n); } }

List<Dog> dogs = new ArrayList<>(List.of(new Dog("Zed"), new Dog("Abe")));
Collections.sort(dogs);              // ✓ works thanks to Comparable<? super T>
System.out.println(dogs);            // [Abe, Zed]
```

With the stricter `<T extends Comparable<T>>`, this would **not** compile.

### 15.7 `Stream` and `Optional` (modern examples)

```java
<R> Stream<R> map(Function<? super T, ? extends R> mapper)
```

The function **consumes** a `T` (`? super T`: it may accept a wider type) and **produces** an `R` (`? extends R`: it may return a narrower type). PECS again.

| Signature | Meaning |
|-----------|---------|
| `Collection<? extends E>` | A source that produces `E` |
| `Collection<? super E>` | A destination that consumes `E` |
| `Comparator<? super T>` | A comparator that can compare `T` (or wider) |
| `Function<? super T, ? extends R>` | Accepts `T` (or wider), returns `R` (or narrower) |
| `Predicate<? super T>` | A test that can judge `T` (or wider) |
| `Consumer<? super T>` | Something that can take `T` (or wider) |
| `Supplier<? extends T>` | Something that gives `T` (or narrower) |

---

## 16. Advanced Notes and Rules

### 16.1 Wildcards only at "use sites"

Wildcards appear where you **use** a type (variables, parameters, return types, fields). You **cannot**:

```java
class Box<?> { }                              // ✗ can't declare a class with a wildcard
new ArrayList<?>();                           // ✗ can't instantiate with a bare wildcard
new ArrayList<? extends Number>();            // ✗ same
List<?> ok = new ArrayList<String>();         // ✓ instantiate a concrete type, refer through a wildcard
```

(Other languages like Kotlin and C# put variance at the **declaration** (`out T`, `in T`). Java puts it at the **use site** with wildcards. `? extends T` is like `out`, and `? super T` is like `in`.)

### 16.2 Nested generics need care

```java
List<List<Integer>> a = new ArrayList<>();

List<List<Number>> b = a;                         // ✗ List<Integer> is not List<Number>
List<List<? extends Number>> c = a;               // ✗ still invariant at the OUTER level
List<? extends List<? extends Number>> d = a;     // ✓ wildcard at BOTH levels
```

Rule: invariance applies at **every** level; add a wildcard where you need flexibility.

### 16.3 Wildcards in maps

```java
static double total(Map<String, ? extends Number> prices) {
    double t = 0;
    for (Number n : prices.values()) t += n.doubleValue();
    return t;
}

Map<String, Integer> m = Map.of("a", 1, "b", 2);
System.out.println(total(m));          // 3.0
```

### 16.4 Wildcards don't change `null`

`null` is accepted by `add` on any wildcard list. It's harmless but meaningless; don't rely on it.

### 16.5 Using `@SuppressWarnings` and `@SafeVarargs` sensibly

- **`@SuppressWarnings("unchecked")`**: silence an unchecked-cast warning **only** when you've proved the cast is safe.
- **`@SafeVarargs`**: on a generic varargs method you know is safe (`T... items`).

### 16.6 `var` and generics (Java 10+)

```java
var list = new ArrayList<String>();       // ArrayList<String>
var odd  = new ArrayList<>();             // ArrayList<Object> !  (nothing to infer from)
```

### 16.7 Bounded type parameters in classes

```java
class NumberBox<T extends Number> {
    private final T value;
    NumberBox(T value) { this.value = value; }
    double doubled() { return value.doubleValue() * 2; }     // we can call Number methods on T
}

new NumberBox<Integer>(5).doubled();      // 10.0
new NumberBox<String>("x");               // ✗ String is not a Number
```

### 16.8 Quick comparison of all reference types

```
  List<Animal>             exact: read Animal, write Animal. Accepts ONLY List<Animal>.
  List<? extends Animal>   producer: read Animal, write nothing. Accepts List<Animal>, List<Dog>, List<Cat>...
  List<? super Dog>        consumer: write Dog, read Object. Accepts List<Dog>, List<Animal>, List<Object>.
  List<?>                  unknown: read Object, write nothing. Accepts ANY list.
  List<Object>             exact: read/write Object. Accepts ONLY List<Object>.
```

---

## 17. Version-by-Version History

| Version | Year | Change |
|---------|------|--------|
| **Java 1.0 to 1.4** | 1996 to 2002 | No generics. Raw collections, casts everywhere, arrays are covariant |
| **Java 5** | 2004 | **Generics introduced** (JSR 14), with **wildcards** (`?`, `? extends`, `? super`), bounded types, generic methods, type erasure (for backward compatibility) |
| **Java 7** | 2011 | **Diamond operator** `<>`: `new ArrayList<>()` |
| **Java 8** | 2014 | Much better **type inference**; lambdas and `Function<? super T, ? extends R>`-style APIs make PECS signatures everywhere |
| **Java 9** | 2017 | Diamond allowed with **anonymous classes** |
| **Java 10** | 2018 | **`var`** for local variables (inference) |
| **Java 16 to 17** | 2021 | Pattern matching for `instanceof` works with generics when the cast is provably safe |
| **Future (Project Valhalla)** | in progress | Aims at improving the primitive/generic story (e.g. specialized generics). Not part of the current language |

---

## 18. Practical Programs

Save each as the named file, compile with `javac FileName.java`, run with `java ClassName`.

### 18.1 `InvarianceDemo`: arrays vs generics

```java
import java.util.ArrayList;
import java.util.List;

class Animal {
    String name;
    Animal(String name) { this.name = name; }
}
class Dog extends Animal { Dog(String name) { super(name); } }
class Cat extends Animal { Cat(String name) { super(name); } }

public class InvarianceDemo {
    public static void main(String[] args) {

        // ---- ARRAYS: covariant, caught only at RUNTIME ----
        Animal[] zoo = new Dog[2];             // compiles
        zoo[0] = new Dog("Rex");               // fine
        try {
            zoo[1] = new Cat("Tom");           // compiles!
        } catch (ArrayStoreException e) {
            System.out.println("Caught: " + e);
        }

        // ---- GENERICS: invariant, caught at COMPILE time ----
        List<Dog> dogs = new ArrayList<>();
        dogs.add(new Dog("Fido"));
        // List<Animal> animals = dogs;        // ✗ COMPILE ERROR (try removing the comment!)

        // the safe way to read dogs as animals:
        List<? extends Animal> readOnlyView = dogs;
        for (Animal a : readOnlyView) {
            System.out.println("Read: " + a.name);
        }
        // readOnlyView.add(new Cat("Tom"));   // ✗ COMPILE ERROR: cannot add through ? extends
    }
}
```

Output:
```
Caught: java.lang.ArrayStoreException: Cat
Read: Fido
```

### 18.2 `WildcardDemo`: all three wildcards and PECS

```java
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class Animal {
    String name;
    Animal(String name) { this.name = name; }
    @Override public String toString() { return getClass().getSimpleName() + "(" + name + ")"; }
}
class Dog extends Animal   { Dog(String name)   { super(name); } }
class Puppy extends Dog    { Puppy(String name) { super(name); } }

public class WildcardDemo {

    // 1) unbounded: any list, read as Object
    static void printAll(List<?> list) {
        for (Object o : list) System.out.print(o + " ");
        System.out.println();
    }

    static int countNulls(List<?> list) {
        int c = 0;
        for (Object o : list) if (o == null) c++;
        return c;
    }

    // 2) upper bounded: PRODUCER (we only read)
    static double sum(List<? extends Number> nums) {
        double total = 0;
        for (Number n : nums) total += n.doubleValue();
        return total;
    }

    static void introduceAll(List<? extends Animal> animals) {
        for (Animal a : animals) System.out.println("Meet " + a);
    }

    // 3) lower bounded: CONSUMER (we only write)
    static void addDogs(List<? super Dog> target) {
        target.add(new Dog("Rex"));
        target.add(new Puppy("Bit"));
    }

    static void fillWithInts(List<? super Integer> target, int n) {
        for (int i = 1; i <= n; i++) target.add(i);
    }

    // 4) PECS: copy from a producer to a consumer
    static <T> void copy(List<? super T> dest, List<? extends T> src) {
        for (T item : src) dest.add(item);
    }

    public static void main(String[] args) {
        List<Integer> ints = List.of(1, 2, 3);
        List<Double> doubles = List.of(1.5, 2.5);
        List<String> strs = Arrays.asList("a", null, "c");

        System.out.println("--- unbounded ---");
        printAll(ints);
        printAll(strs);
        System.out.println(countNulls(strs));

        System.out.println("--- extends (producer) ---");
        System.out.println(sum(ints));
        System.out.println(sum(doubles));
        introduceAll(List.of(new Dog("Rex"), new Puppy("Bit")));

        System.out.println("--- super (consumer) ---");
        List<Animal> animals = new ArrayList<>();
        List<Object> objects = new ArrayList<>();
        addDogs(animals);
        addDogs(objects);
        System.out.println(animals);
        System.out.println(objects);

        List<Number> numbers = new ArrayList<>();
        fillWithInts(numbers, 3);
        System.out.println(numbers);

        System.out.println("--- PECS copy ---");
        List<Object> mixed = new ArrayList<>();
        copy(mixed, ints);
        copy(mixed, doubles);
        System.out.println(mixed);

        System.out.println("--- Comparator<? super T> ---");
        Comparator<Animal> byName = (a, b) -> a.name.compareTo(b.name);
        List<Dog> dogs = new ArrayList<>(List.of(new Dog("Zed"), new Dog("Abe"), new Dog("Max")));
        dogs.sort(byName);                  // a Comparator<Animal> sorts a List<Dog>
        System.out.println(dogs);
    }
}
```

Output:
```
--- unbounded ---
1 2 3 
a null c 
1
--- extends (producer) ---
6.0
4.0
Meet Dog(Rex)
Meet Puppy(Bit)
--- super (consumer) ---
[Dog(Rex), Puppy(Bit)]
[Dog(Rex), Puppy(Bit)]
[1, 2, 3]
--- PECS copy ---
[1, 2, 3, 1.5, 2.5]
--- Comparator<? super T> ---
[Dog(Abe), Dog(Max), Dog(Zed)]
```

### 18.3 `StackDemo`: designing an API with PECS

```java
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class MyStack<E> {
    private final ArrayList<E> items = new ArrayList<>();

    void push(E e)    { items.add(e); }
    E pop()           { return items.remove(items.size() - 1); }
    boolean isEmpty() { return items.isEmpty(); }

    void pushAll(Iterable<? extends E> src) {        // src produces E
        for (E e : src) push(e);
    }

    void popAll(Collection<? super E> dst) {         // dst consumes E
        while (!isEmpty()) dst.add(pop());
    }
}

public class StackDemo {
    public static void main(String[] args) {
        MyStack<Number> stack = new MyStack<>();
        stack.pushAll(List.of(1, 2, 3));             // Iterable<Integer>
        stack.pushAll(List.of(4.5, 5.5));            // Iterable<Double>

        List<Object> sink = new ArrayList<>();
        stack.popAll(sink);                          // Collection<Object>
        System.out.println(sink);
    }
}
```

Output:
```
[5.5, 4.5, 3, 2, 1]
```

### 18.4 `CaptureDemo`: the capture helper

```java
import java.util.ArrayList;
import java.util.List;

public class CaptureDemo {

    static void swap(List<?> list, int i, int j) {
        swapHelper(list, i, j);                      // the compiler captures ? as T
    }

    private static <T> void swapHelper(List<T> list, int i, int j) {
        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

    public static void main(String[] args) {
        List<String> names = new ArrayList<>(List.of("a", "b", "c"));
        swap(names, 0, 2);
        System.out.println(names);                   // [c, b, a]

        List<Integer> nums = new ArrayList<>(List.of(10, 20, 30));
        swap(nums, 0, 1);
        System.out.println(nums);                    // [20, 10, 30]
    }
}
```

Output:
```
[c, b, a]
[20, 10, 30]
```

### 18.5 `ErasureDemo`: erasure and heap pollution

```java
import java.util.ArrayList;
import java.util.List;

public class ErasureDemo {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
        List<Integer> ints = new ArrayList<>();

        System.out.println(strings.getClass() == ints.getClass());
        System.out.println(strings.getClass().getName());

        List raw = strings;                          // raw type: checking is OFF
        raw.add(42);                                 // no error here...
        System.out.println("size = " + strings.size());

        try {
            String s = strings.get(0);               // ...the crash happens here
        } catch (ClassCastException e) {
            System.out.println("ClassCastException when READING (heap pollution)");
        }
    }
}
```

Output:
```
true
java.util.ArrayList
size = 1
ClassCastException when READING (heap pollution)
```

### 18.6 Try it yourself

1. Write `static double average(Collection<? extends Number> nums)` and test with `List<Integer>`, `Set<Double>` and `List<Long>`.
2. Write `static void addNumbers(List<? super Integer> out)` and call it with `List<Integer>`, `List<Number>` and `List<Object>`. Then try `List<Double>` and read the error.
3. Try to compile `List<Animal> a = new ArrayList<Dog>();` and read the message. Then fix it with a wildcard.
4. Inside a method taking `List<? extends Animal>`, try `list.add(new Dog("x"))` and read the error. Explain it using the table in section 9.6.
5. Write your own `MyBox<T>` with `copyFrom(MyBox<? extends T> other)` and `copyTo(MyBox<? super T> other)`.
6. Write `static <T extends Comparable<? super T>> T maxOf(List<? extends T> list)` and test it with `List<Dog>` where `Animal implements Comparable<Animal>`.
7. Reproduce the heap-pollution demo and explain why the exception appears at the read, not at the `add`.
8. Implement `swap` for `List<?>` with and without the helper, and compare the compiler messages.

---

## 19. Common Mistakes

| # | Mistake | What happens | Fix |
|---|---------|--------------|-----|
| 1 | Assuming `List<Dog>` is a `List<Animal>` | Compile error: incompatible types | Use `List<? extends Animal>` if you only read |
| 2 | Method parameter `List<Object>` hoping to accept any list | Accepts only `List<Object>` | Use `List<?>` |
| 3 | `List<? extends T>` and then calling `add(...)` | Compile error (only `null` allowed) | Use `List<? super T>` or `List<T>` |
| 4 | `List<? super T>` and then reading a `T` | You get `Object`, not `T` | Use `? extends T` for reading |
| 5 | Using a wildcard when you both read and write | Neither works well | Use a plain `List<T>` or a generic method `<T>` |
| 6 | Using raw types (`List`) to "avoid the problem" | Heap pollution, runtime `ClassCastException` | Use `List<?>` |
| 7 | `new T()` / `new T[n]` | Compile error (erasure) | Pass a `Class<T>` or `Supplier<T>`; use `(T[]) new Object[n]` carefully |
| 8 | `x instanceof List<String>` | Compile error | `x instanceof List<?>` |
| 9 | Two overloads differing only by generic argument | "same erasure" error | Rename one method |
| 10 | `<T super Dog>` | Illegal | Use the wildcard form `? super Dog` |
| 11 | A wildcard with two bounds | Illegal | Use a type parameter `<T extends A & B>` |
| 12 | Returning wildcard types (`List<? extends Animal>`) | Callers can't write or even use it well | Return a concrete `List<Animal>` |
| 13 | Suppressing unchecked warnings everywhere | Hides real bugs | Suppress only after proving safety |
| 14 | `Comparable<T>` bound instead of `Comparable<? super T>` | Can't sort subclasses of a comparable class | Use `<T extends Comparable<? super T>>` |
| 15 | `var x = new ArrayList<>();` | `ArrayList<Object>` | Write the type argument |
| 16 | Thinking invariance blocks adding a `Dog` to a `List<Animal>` | It doesn't | Invariance is about the container type, not its contents |
| 17 | Confusing `Object` with `?` | Different meanings | `List<Object>` = holds anything; `List<?>` = one unknown type |
| 18 | Generic array creation `new List<String>[10]` | Compile error | Use `List<?>[]`, or a `List<List<String>>` |

---

## 20. Cheat Sheet, Summary and Quiz

### 20.1 The one table to remember

| Reference | Accepts | Read as | Write |
|-----------|---------|---------|-------|
| `List<T>` | only `List<T>` | `T` | `T` |
| `List<?>` | any list | `Object` | only `null` |
| `List<? extends T>` | `List<T>` and `List<subtype of T>` | `T` | only `null` |
| `List<? super T>` | `List<T>` and `List<supertype of T>` | `Object` | `T` (and subtypes) |

### 20.2 PECS in one line

```
 Producer Extends, Consumer Super.
 Get from it → ? extends.   Put into it → ? super.   Both → no wildcard.   Don't care → ?
```

### 20.3 Syntax cheat sheet

```java
List<?> any;                                   // unbounded
List<? extends Number> readOnly;               // upper bound (producer)
List<? super Integer> writeOnly;               // lower bound (consumer)

static double sum(List<? extends Number> nums)         { ... }   // read
static void fill(List<? super Integer> out)            { ... }   // write
static <T> void copy(List<? super T> d, List<? extends T> s) { ... }   // PECS
static <T extends Comparable<? super T>> T max(List<? extends T> l) { ... }

// capture helper
static void swap(List<?> l, int i, int j) { swapHelper(l, i, j); }
private static <T> void swapHelper(List<T> l, int i, int j) { ... }
```

### 20.4 Summary in simple points

1. **Generics are invariant:** `Dog` is an `Animal`, but **`List<Dog>` is NOT a `List<Animal>`**. The two types are unrelated.
2. **Why (the proof):** if `List<Dog>` could be used as a `List<Animal>`, you could **add a `Cat`** through the `Animal` view and then **read it as a `Dog`**: a `ClassCastException`. The danger comes from **writing**.
3. **Arrays are covariant** (`Dog[]` is an `Animal[]`), and are protected by a **runtime check** (`ArrayStoreException`), because arrays know their element type. Generics are **erased** at runtime, so the **compiler** must be strict.
4. **Reading through a supertype view is safe; writing through a supertype view is unsafe** (and the reverse for subtype views). Wildcards let you promise "I only read" or "I only write", and the compiler enforces it.
5. **`?` (unbounded):** a list of some **unknown** type. Read as **`Object`**; add only **`null`**. Use it when the type doesn't matter.
6. **`? extends T` (upper bound):** `T` **or a subclass**. **Read as `T`**; **no writing** (except `null`). Use for **producers** (sources).
7. **`? super T` (lower bound):** `T` **or a superclass**. **Write `T`** (and subtypes); reading gives only **`Object`**. Use for **consumers** (destinations).
8. **PECS:** **P**roducer **E**xtends, **C**onsumer **S**uper. Use no wildcard when you both read and write.
9. **Type erasure** removes generic types at runtime, which is why `new T()`, `instanceof List<String>` and generic arrays are illegal, and why raw types can cause **heap pollution**.
10. **Capture:** the compiler names an unknown `?` as `CAP#1`. A **helper method with a type parameter** lets you write through a `List<?>`.
11. Use a **wildcard** when the type appears once; use a **type parameter** when you must link several places or use multiple bounds.
12. The JDK is full of PECS: `Collections.copy`, `addAll`, `Comparator<? super T>`, `Comparable<? super T>`, `Function<? super T, ? extends R>`.

### 20.5 Mini Quiz

1. Is `List<Dog>` a subtype of `List<Animal>`? Explain with the cat example.
2. Why are arrays allowed to be covariant but generics are not?
3. What can you add to a `List<?>`? What do you get from `get()`?
4. What do you get from `get()` on a `List<? extends Number>`? Why can't you `add(Integer)`?
5. What can you add to a `List<? super Integer>`? What do you get from `get()`?
6. Which signature lets a method sum a `List<Integer>` and a `List<Double>`?
7. Which signature lets a method add `Integer`s to a `List<Number>` and a `List<Object>`?
8. What does PECS stand for?
9. What is the difference between `List<?>` and `List<Object>`?
10. Why does `Comparator<? super T>` make sense for sorting?
11. Why is `new T()` illegal?
12. What is heap pollution and when does the exception actually occur?
13. Can you write `<T super Dog>`? Can you write `List<? super Dog>`?
14. When should you choose a type parameter over a wildcard?
15. Does this compile? `List<? extends Number> l = new ArrayList<Integer>(); l.add(5);`
16. Does this compile? `List<? super Integer> l = new ArrayList<Number>(); l.add(5);`
17. Why is `Collections.sort` declared with `<T extends Comparable<? super T>>`?
18. What does the compiler do when it sees `List<?>` in an error message like `CAP#1`?

<details>
<summary>Click for answers</summary>

1. **No.** If it were, you could add a `Cat` through the `List<Animal>` view and then read it as a `Dog` from the `List<Dog>` view, a `ClassCastException`. The two types are unrelated (**invariant**).
2. Arrays **know their element type at runtime** and throw `ArrayStoreException` on a bad store. Generics are **erased** at runtime, so the JVM can't check; the **compiler** must prevent the problem, so generics are invariant.
3. Only **`null`**. `get()` returns **`Object`**.
4. You get **`Number`**. You can't `add(5)` because the real list might be `List<Double>` (or another subtype), and an `Integer` doesn't fit every possibility. Only `null` is safe.
5. You can add **`Integer`** (and its subtypes). `get()` returns **`Object`**.
6. `static double sum(List<? extends Number> nums)`.
7. `static void add(List<? super Integer> out)`.
8. **Producer Extends, Consumer Super.**
9. `List<Object>` accepts only `List<Object>` and allows adding anything. `List<?>` accepts a list of **any** type, but you can add only `null`.
10. A `Comparator<Animal>` can compare any `Animal`, so it can also compare `Dog`s. It **consumes** the elements, so it's `? super T`.
11. Because of **type erasure**: at runtime `T` doesn't exist, so the JVM can't know which class to instantiate.
12. **Heap pollution** is when a variable of a parameterized type points to an object that doesn't match (for example, via a raw type, an `Integer` is placed in a `List<String>`). The exception happens later, when the wrong element is **read**.
13. **No** to `<T super Dog>` (type parameters allow only `extends`). **Yes** to `List<? super Dog>` (wildcards allow `super`).
14. When the type must be **linked** (several parameters, or a parameter and the return type), when you need to **read and write** the same type, or when you need **multiple bounds**.
15. **No.** You can't `add` to a `List<? extends Number>` (except `null`).
16. **Yes.** `? super Integer` allows adding an `Integer`.
17. So that a type like `Dog` (whose parent `Animal` implements `Comparable<Animal>`) can be sorted: `Dog` is comparable to a **supertype** of itself.
18. It gives the unknown type a temporary name (a **capture**) so it can check the code; each use of `?` gets a fresh capture, which is why you can't write through `List<?>` without a helper method.

</details>

---

*Practice tip: whenever you write a method that takes a collection, ask one question: "Do I read from it, write to it, or both?" Then pick the type from the table in section 20.1. After a few times, PECS becomes automatic.*
