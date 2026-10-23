# The Java `Object` Class: A First-Principles Deep Dive

> This guide follows the outline of the video you shared (intro, `toString()`, `equals()`, `hashCode()`, `getClass()`, `clone()`, deprecated and advanced methods, arrays, primitives and wrappers) and explains everything in **simple language**.
> You asked for the **"first thought principle"**. I'm reading that as **first-principles thinking**: for every method we start with **the problem**, work out **what is needed**, and **derive** the method as the natural answer. That way you understand *why* each method exists, not just what it does.

---

## Table of Contents

1. [How to Read This Guide (First-Principles Method)](#1-how-to-read-this-guide-first-principles-method)
2. [Introduction: The Root of Every Class](#2-introduction-the-root-of-every-class)
3. [Why Does a Universal Root Class Exist?](#3-why-does-a-universal-root-class-exist)
4. [The Methods of `Object` at a Glance](#4-the-methods-of-object-at-a-glance)
5. [`toString()`](#5-tostring)
6. [`equals()`](#6-equals)
7. [`hashCode()`](#7-hashcode)
8. [`equals()` and `hashCode()` Together](#8-equals-and-hashcode-together)
9. [`getClass()`](#9-getclass)
10. [`clone()`](#10-clone)
11. [Deprecated and Advanced Methods](#11-deprecated-and-advanced-methods)
12. [Arrays, Primitives and Wrapper Classes](#12-arrays-primitives-and-wrapper-classes)
13. [The `Objects` Utility Class (Different from `Object`)](#13-the-objects-utility-class-different-from-object)
14. [Practical Programs](#14-practical-programs)
15. [Common Mistakes](#15-common-mistakes)
16. [Cheat Sheet, Summary and Quiz](#16-cheat-sheet-summary-and-quiz)

---

## 1. How to Read This Guide (First-Principles Method)

**First-principles thinking** means: don't memorize, **rebuild the idea from the ground up**.

For each topic we ask three questions in order:

```
 ① THE PROBLEM   →   What is hard or impossible without this?
 ② THE NEED      →   What would we have to invent to solve it?
 ③ THE SOLUTION  →   Oh, that's exactly what Java's method does!
```

Example, to see the style: *"Why does `toString()` exist?"*

- ① **Problem:** a computer only has memory and numbers. To show an object to a human (or in a log), we need **text**.
- ② **Need:** a way for *every* object to describe itself as text.
- ③ **Solution:** a method every object has, `toString()`, which returns that text.

We will do this for every method below.

---

## 2. Introduction: The Root of Every Class

### 2.1 What is it?

`Object` is a class in the package **`java.lang`**. It is the **root (top) of the entire Java class hierarchy**: **every class in Java is a child (direct or indirect) of `Object`**.

| Fact | Detail |
|------|--------|
| **Full name** | `java.lang.Object` |
| **Auto-imported?** | **Yes.** Everything in `java.lang` is imported automatically, so you never write `import java.lang.Object;` |
| **Its parent** | **None.** It is the only class without a superclass |
| **Its children** | **Every other class**: `String`, `Integer`, `ArrayList`, `System`, your own classes... |
| **Does it have fields?** | **No** (no data of its own) |
| **Can you create one?** | Yes: `new Object()` (it is not abstract). It is mostly used as a **lock** (section 11.2) |

### 2.2 Every class extends `Object`: even if you don't write it

```java
public class Student { }
```

The compiler treats this exactly as:

```java
public class Student extends Object { }
```

Two cases:

| You write | What really happens |
|-----------|---------------------|
| `class A { }` (no `extends`) | Java adds **`extends Object`** automatically → **direct** child of `Object` |
| `class B extends A { }` | `B` → `A` → `Object` → **indirect** child of `Object` |

```
                       java.lang.Object
          ┌────────────────┬──┴──────────────┬────────────────────┐
        String          Number             Student          int[]  String[]  (arrays)
                  ┌───────┬──┴────┬───────┐     │
               Integer  Double   Long  ...  GradStudent
```

So **every** object you will ever create is an `Object`, and therefore **has all of `Object`'s methods**, whether or not its class mentions them.

### 2.3 The constructor chain ends here

Remember constructor chaining: every constructor calls `super(...)` (explicitly or implicitly). Follow the chain up and it always ends at **`Object()`**, a constructor that does nothing. That's the top of the chain for every object.

### 2.4 What about interfaces?

Interfaces **don't extend `Object`**, but you can still call `toString()`, `equals()` and `hashCode()` on an interface-typed variable. Java guarantees that any object behind an interface reference is an `Object`, so those methods are always available.

```java
List<String> list = new ArrayList<>();
System.out.println(list.toString());        // works: [] 
```

### 2.5 See the class yourself

```
javap java.lang.Object
```

prints all its methods (the exact modifiers vary a little between Java versions). We list them in section 4.

---

## 3. Why Does a Universal Root Class Exist?

### First-principles reasoning

**① Problem 1: Java needs one type that can hold anything.**
Suppose you write a method that should accept *any* object, or a box that stores *any* kind of thing. Without a common type, you'd need a different version for every class.

**② Need:** one **common ancestor type** that every class shares.

**③ Solution:** `Object`. A variable of type `Object` can refer to **any** object.

```java
Object o = "hello";            // a String
o = 42;                        // an Integer (autoboxed)
o = new int[]{1, 2, 3};        // an array
o = new Student();             // your own class
```

This is **polymorphism** in its simplest form: one reference type, many real types.

**① Problem 2: some behaviors are needed by every object, always.**
Think about what you can't avoid doing with *any* object:

| Everyone eventually needs to... | Method |
|---------------------------------|--------|
| Print / log it as text | `toString()` |
| Ask "is this the same as that?" | `equals()` |
| Put it in a hash-based collection (`HashMap`, `HashSet`) | `hashCode()` |
| Ask "what type are you, really?" | `getClass()` |
| Copy it | `clone()` |
| Use it as a lock for threads | `wait()`, `notify()`, `notifyAll()` |
| Clean up when it dies (old idea) | `finalize()` |

**② Need:** a guarantee that **every** object can answer these, so that library code (collections, `println`, locks) can rely on them without knowing the object's class.

**③ Solution:** put them all in `Object`, with **sensible defaults**, and let each class **override** the ones that need smarter behavior.

```
          ┌─────────────────────────────────────────────┐
          │  Object: the "contract" every object signs   │
          │   toString()  equals()  hashCode()  getClass()│
          └───────────────────┬─────────────────────────┘
                              │ inherited by everyone
        ┌─────────────────────┼──────────────────────┐
     String                Integer               Student
   (overrides all        (overrides all        (you override what
    three smartly)        three smartly)         you need)
```

### What `Object` gives us (summary)

- **Polymorphism:** one type for everything (`Object o`).
- **Guaranteed behaviors:** every object can print, compare, hash, report its type.
- **Consistency:** `println(x)`, `list.contains(x)`, `map.put(x, ...)` work for any `x`.

---

## 4. The Methods of `Object` at a Glance

`Object` has **11 methods** (counting the three `wait` versions separately) plus its constructor.

| # | Method | Signature (simplified) | Overridable? | Purpose |
|---|--------|------------------------|:------------:|---------|
| 1 | `toString()` | `public String toString()` | ✓ | Text description of the object |
| 2 | `equals()` | `public boolean equals(Object obj)` | ✓ | Are two objects "equal"? |
| 3 | `hashCode()` | `public int hashCode()` | ✓ | An `int` fingerprint for hash collections |
| 4 | `getClass()` | `public final Class<?> getClass()` | ✗ **final** | The object's real runtime class |
| 5 | `clone()` | `protected Object clone() throws CloneNotSupportedException` | ✓ | Make a copy |
| 6 | `finalize()` | `protected void finalize() throws Throwable` | ✓ | Old cleanup hook: **deprecated** |
| 7 | `notify()` | `public final void notify()` | ✗ **final** | Wake one waiting thread |
| 8 | `notifyAll()` | `public final void notifyAll()` | ✗ **final** | Wake all waiting threads |
| 9 | `wait()` | `public final void wait() throws InterruptedException` | ✗ **final** | Wait until notified |
| 10 | `wait(long ms)` | `public final void wait(long timeout)` | ✗ **final** | Wait with a time limit |
| 11 | `wait(long ms, int ns)` | `public final void wait(long timeout, int nanos)` | ✗ **final** | Wait with a finer time limit |

The ones you will override in everyday code: **`toString()`, `equals()`, `hashCode()`** (and rarely `clone()`).

Quick picture of what the **defaults** do:

| Method | Default behavior in `Object` |
|--------|------------------------------|
| `toString()` | `ClassName@hexNumber` |
| `equals()` | `this == obj` (same reference only) |
| `hashCode()` | A number tied to the **object's identity** |
| `clone()` | **Shallow** field-by-field copy (only if the class is `Cloneable`) |

---

## 5. `toString()`

### 5.1 First principles

**① Problem:** an object lives in memory as a bunch of fields. A human (or a log file) needs readable **text**.
**② Need:** every object must be able to describe itself as a `String`.
**③ Solution:** `public String toString()`.

### 5.2 The default behavior

If you don't override it, `Object.toString()` gives:

```
ClassName@hexadecimalCode
```

Exactly, the default is the same as writing:

```java
getClass().getName() + "@" + Integer.toHexString(hashCode())
```

```java
public class Student {
    String name = "Ali";
    int age = 20;
}

Student s = new Student();
System.out.println(s);        // Student@1b6d3586
```

What the pieces mean:

```
   Student      @       1b6d3586
 ┌─────────┐  ┌───┐  ┌────────────┐
  class name  separator  hash code of the object, in hexadecimal
```

It tells you the **type** and a **fingerprint**, but nothing about the **data**. That is rarely useful, so we override it.

> The hex number comes from `hashCode()`. By default that is an identity-based number (not guaranteed to be the memory address), which is why you'll see different numbers on different runs.

### 5.3 Overriding `toString()`

```java
public class Student {
    private String name;
    private int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Student[name=" + name + ", age=" + age + "]";
    }
}
```

```java
Student s = new Student("Ali", 20);
System.out.println(s);        // Student[name=Ali, age=20]
```

Always add **`@Override`**. If you mistype the name (`tostring`) or the signature, the compiler tells you.

### 5.4 When does Java call `toString()` automatically?

You rarely write `.toString()` yourself. Java calls it for you in these places:

| Situation | Example |
|-----------|---------|
| `System.out.println(obj)` / `print(obj)` | `System.out.println(s);` |
| String concatenation with `+` | `"Student: " + s` |
| `String.valueOf(obj)` | `String.valueOf(s)` |
| `printf` / `String.format` with `%s` | `String.format("%s", s)` |
| Inside collections' `toString()` | `list.toString()` calls it on every element |
| Debugger / logging frameworks | Shows your object as text |

```java
Student s = new Student("Ali", 20);
String text = "Hello, " + s;                     // calls s.toString()
System.out.println(text);                        // Hello, Student[name=Ali, age=20]

List<Student> list = List.of(s, new Student("Sara", 22));
System.out.println(list);                        // [Student[name=Ali, age=20], Student[name=Sara, age=22]]
```

### 5.5 What happens with `null`?

```java
Student s = null;
System.out.println(s);                  // null   (println uses String.valueOf, which handles null)
System.out.println("x" + s);            // xnull  (also safe)
s.toString();                           // NullPointerException (you called a method on nothing)
```

Safe ways: `String.valueOf(s)` or `Objects.toString(s)`.

### 5.6 Built-in classes already override it

| Object | `toString()` result |
|--------|---------------------|
| `String` | the text itself: `hello` |
| `Integer` | `42` |
| `Double` | `3.14` |
| `ArrayList` | `[1, 2, 3]` |
| `HashMap` | `{a=1, b=2}` |
| Enum constant | its name: `MONDAY` |
| `record` | `Point[x=3, y=4]` (generated automatically) |
| **Array** (`int[]`) | **not overridden** → `[I@1b6d3586` (use `Arrays.toString`) |

### 5.7 Best practices

1. **Include the important fields** so the output is useful for debugging.
2. Use a **consistent format**, like `ClassName[field=value, ...]`.
3. **Don't print secrets** (passwords, card numbers). `toString()` ends up in logs.
4. **Don't call methods that can fail or be slow.**
5. **Beware of loops:** if `A.toString()` prints `B` and `B.toString()` prints `A`, you get infinite recursion and a `StackOverflowError`.
6. Use **`StringBuilder`** or `String.format` for long output.
7. IDEs can generate it; **records** generate it for free.

---

## 6. `equals()`

### 6.1 First principles

**① Problem:** What does "same" mean? There are **two different meanings**:

| Meaning | Question | Real-life example |
|---------|----------|-------------------|
| **Identity** | Are these **literally the same object**? | "This is *my* phone" (this exact physical phone) |
| **Equality** (logical) | Do these **hold the same information**? | "Two phones of the same model and color" |

```java
Student a = new Student("Ali", 20);
Student b = new Student("Ali", 20);
Student c = a;
```

```
 STACK                        HEAP
 a ──────────────────────► [ Student: Ali, 20 ]  ◄──── c      (a and c: same object)
 b ──────────────────────► [ Student: Ali, 20 ]               (b: different object, same data)
```

- `a` and `c` are the **same object** (identity).
- `a` and `b` are **different objects with equal data** (logical equality).

**② Need:**
- A way to ask the **identity** question: that's the **`==`** operator (compares references/addresses).
- A way to ask the **equality** question, where **each class decides** what "equal" means (a `Student` is equal if name and age match; a `Rectangle` if width and height match).

**③ Solution:** the **`equals(Object)`** method, which classes override to define their own logical equality.

### 6.2 `==` vs `equals()`

| | `==` | `.equals()` |
|---|------|-------------|
| What it is | An **operator** | A **method** |
| For primitives | Compares **values** | Not available (primitives have no methods) |
| For objects | Compares **references** (same object?) | Compares **content**, *if overridden* |
| Can you change it? | **No** | **Yes**, by overriding |

### 6.3 The default `equals()`

`Object.equals()` is simply:

```java
public boolean equals(Object obj) {
    return this == obj;               // same as ==: identity only
}
```

So if you **don't** override it, equals behaves like `==`:

```java
Student a = new Student("Ali", 20);
Student b = new Student("Ali", 20);

System.out.println(a == b);           // false
System.out.println(a.equals(b));      // false  ← default equals: different objects
System.out.println(a.equals(a));      // true
```

Usually that's not what we want for a `Student`. We want `a.equals(b)` to be **true**.

### 6.4 How built-in classes handle it

`String`, `Integer`, `Double`, `ArrayList`, etc. **override `equals()`** to compare content.

```java
String s1 = new String("hello");
String s2 = new String("hello");

System.out.println(s1 == s2);          // false (two objects)
System.out.println(s1.equals(s2));     // true  (same characters)

Integer i1 = 1000, i2 = 1000;
System.out.println(i1 == i2);          // false (two objects)
System.out.println(i1.equals(i2));     // true
```

Lesson: **always use `.equals()` to compare objects' content** (Strings, wrappers, your own classes).

### 6.5 Overriding `equals()`: step by step

We'll derive the steps from first principles.

```java
public class Student {
    private final String name;
    private final int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public boolean equals(Object obj) {
        // STEP 1
        if (this == obj) return true;

        // STEP 2
        if (obj == null) return false;

        // STEP 3
        if (getClass() != obj.getClass()) return false;

        // STEP 4
        Student other = (Student) obj;

        // STEP 5
        return age == other.age && Objects.equals(name, other.name);
    }
}
```

Why each step is needed:

| Step | Code | The first-principles reason |
|------|------|-----------------------------|
| **1** | `if (this == obj) return true;` | An object is **always equal to itself**. This is a free, instant shortcut that skips all the work. |
| **2** | `if (obj == null) return false;` | `null` means "no object". Nothing real can equal it. Without this check, step 3 would crash with a `NullPointerException`. |
| **3** | `if (getClass() != obj.getClass()) return false;` | A `Student` can't be equal to a `String` or a `Dog`. If the types differ, the fields don't even correspond. |
| **4** | `Student other = (Student) obj;` | The parameter is declared as `Object`, so we have to **cast** it to `Student` before reading `Student` fields. Step 3 guarantees the cast is safe. |
| **5** | compare fields | The **real definition of equality**: all the fields that matter must match. |

### 6.6 How to compare each kind of field

| Field type | How to compare | Why |
|------------|----------------|-----|
| `int`, `long`, `char`, `boolean`, `byte`, `short` | `a == b` | Plain values |
| `float` | `Float.compare(a, b) == 0` | Handles `NaN` and `-0.0` correctly |
| `double` | `Double.compare(a, b) == 0` | Same reason |
| Object fields (`String`, etc.) | `Objects.equals(a, b)` | **Null-safe**: works even if `a` is `null` |
| Arrays | `Arrays.equals(a, b)` / `Arrays.deepEquals(a, b)` | Arrays don't override `equals` |

```java
return age == other.age
        && Double.compare(gpa, other.gpa) == 0
        && Objects.equals(name, other.name);
```

### 6.7 Why the parameter must be `Object` (the overloading trap)

```java
public boolean equals(Student other) { ... }    // ✗ this is OVERLOADING, not overriding!
```

`Object.equals` takes an `Object`. A method taking `Student` is a **different method**. Collections call `equals(Object)`, so your version would **never be used** there. Adding **`@Override`** catches this mistake at compile time.

```java
@Override
public boolean equals(Student other) { ... }   // compile error: method does not override or implement a method from a supertype
```

### 6.8 `getClass()` vs `instanceof` in `equals`

The video uses `getClass()`. There is another common style:

```java
if (!(obj instanceof Student)) return false;
```

| | `getClass() != obj.getClass()` | `obj instanceof Student` |
|---|-------------------------------|--------------------------|
| Subclass objects | **Not equal** to a parent object | **Can be equal** |
| Strictness | **Exact class** must match | The class **or a subclass** |
| `null` handling | Needs a separate `null` check first | `instanceof` returns `false` for `null`, so it covers null too |
| Risk | Can be too strict | Can **break symmetry** with subclasses |

**The symmetry problem with `instanceof`:**

```java
class Point {
    final int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }
}

class ColorPoint extends Point {
    final String color;
    ColorPoint(int x, int y, String color) { super(x, y); this.color = color; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColorPoint)) return false;
        return super.equals(o) && color.equals(((ColorPoint) o).color);
    }
}

Point p = new Point(1, 2);
ColorPoint cp = new ColorPoint(1, 2, "red");

System.out.println(p.equals(cp));     // true   (cp is a Point with the same x and y)
System.out.println(cp.equals(p));     // false  (p is not a ColorPoint)  ← SYMMETRY BROKEN
```

**Advice:** use **`getClass()`** (as the video does), or make the class **`final`** and use `instanceof`. Both are safe.

### 6.8b Modern form with pattern matching (Java 16+)

```java
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Student other)) return false;      // check and cast in one step
    return age == other.age && Objects.equals(name, other.name);
}
```

(Best used in a `final` class.)

### 6.9 The equals contract (the 5 rules)

A correct `equals` must be:

| Rule | Meaning | Example |
|------|---------|---------|
| **Reflexive** | `x.equals(x)` is always `true` | Step 1 |
| **Symmetric** | `x.equals(y)` ⇔ `y.equals(x)` | The `Point` trap above |
| **Transitive** | If `x.equals(y)` and `y.equals(z)` then `x.equals(z)` | |
| **Consistent** | Repeated calls give the same answer (if nothing changed) | Don't base it on random/time values |
| **Non-null** | `x.equals(null)` is always `false` | Step 2 |

### 6.10 Records get it for free

```java
record Point(int x, int y) { }

new Point(1, 2).equals(new Point(1, 2));    // true  (equals, hashCode, toString generated)
```

---

## 7. `hashCode()`

This is the trickiest of the three, so we will build it **very carefully from first principles**.

### 7.1 The problem: finding things fast

Suppose you store **1,000,000 students** and want to know: *"Is this student in the collection?"*

**Approach 1: a plain list.** Check every student one by one with `equals()`. That takes up to **1,000,000 comparisons** (O(n)). Too slow.

**Approach 2 (the idea): compute an address from the object itself.**
Imagine a big array with numbered slots (**buckets**). If we could turn each student into **a number**, we could use that number to jump **directly** to the right slot, with no searching. That's how a **hash table** works.

```
 Student ──► (some calculation) ──► number ──► slot (bucket) in an array
 ("Ali",20)                         2044439       → bucket #7
```

That "some calculation" is the **hash function**, and the number is the **hash code**.

### 7.2 The need: derive the rules

Let's think like an inventor.

**Question 1:** We put `Student("Ali", 20)` into a set. Later we search with a *different object* `Student("Ali", 20)`, equal by `equals()`. For the search to find it, we must jump to **the same bucket** as before.

→ **Equal objects must produce the same hash code.**

That is exactly the **Golden Rule** from the video:

> **If `a.equals(b)` is `true`, then `a.hashCode() == b.hashCode()` MUST be true.**

**Question 2:** There are only about 4 billion possible `int` values, but infinitely many possible objects. So **two different objects will sometimes get the same number** (a **collision**), by simple counting (the pigeonhole principle).

→ **The same hash code does NOT mean the objects are equal.** That's the second half of the rule:

> **If two objects have the same hash code, they are NOT necessarily equal.**

So how do we handle collisions? Each bucket holds a **small list** of objects. After jumping to the bucket by hash code, we use **`equals()`** to find the right one among the few in there.

```
 Hash table (array of buckets)

 index 0:  (empty)
 index 1:  → [Ali, 20]
 index 2:  (empty)
 index 3:  → [Sara, 22] → [John, 30]        ← collision: same bucket, told apart by equals()
 index 4:  (empty)
 index 5:  → [Mia, 19]
```

**How a lookup works (`set.contains(x)`):**

```
 Step 1:  h = x.hashCode()                 ← compute the number
 Step 2:  bucket = (some function of h)    ← pick the bucket (fast, no searching)
 Step 3:  for each item in that bucket:    ← only a few items
              if item.equals(x) → FOUND
 Step 4:  not found in the bucket → not in the set
```

Total work: **one hash calculation + a few comparisons**: effectively **constant time, O(1)**, instead of O(n).

### 7.3 So what is `hashCode()`?

> **`hashCode()` returns an `int` "fingerprint" of the object that hash-based collections (`HashMap`, `HashSet`, `Hashtable`, `LinkedHashMap`...) use to pick a bucket.**

### 7.4 The default implementation

`Object.hashCode()` returns a number based on the object's **identity** (typically produced by the JVM and stored in the object's header; **not guaranteed to be the memory address**). So:

- Different objects usually get different numbers.
- Two objects with **equal data** still get **different** hash codes, because they are different objects.

```java
Student a = new Student("Ali", 20);
Student b = new Student("Ali", 20);
System.out.println(a.hashCode() == b.hashCode());    // false (default: identity based)
```

You can always read the identity hash, even if `hashCode()` is overridden:

```java
System.out.println(System.identityHashCode(a));
```

### 7.5 The contract (the official rules)

| # | Rule |
|---|------|
| 1 | **Consistent:** during one run of the program, `hashCode()` must return the **same value** each time, as long as the fields used in `equals()` haven't changed. |
| 2 | **Equal ⇒ same hash:** if `a.equals(b)` then `a.hashCode() == b.hashCode()`. **(the Golden Rule)** |
| 3 | **Same hash ⇏ equal:** unequal objects **may** share a hash code (collisions are allowed). |

Handy logic table:

| `a.equals(b)` | `a.hashCode() == b.hashCode()` | Allowed? |
|:-------------:|:------------------------------:|:--------:|
| true | true | ✓ |
| true | **false** | ✗ **Forbidden (breaks the contract)** |
| false | true | ✓ (a collision) |
| false | false | ✓ |

**Useful consequence:** if two hash codes are **different**, the objects are **definitely not equal**, so `equals()` doesn't even need to be called.

### 7.6 The disaster: overriding `equals()` but not `hashCode()`

```java
public class Broken {
    private final String name;
    private final int age;

    Broken(String name, int age) { this.name = name; this.age = age; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Broken other = (Broken) obj;
        return age == other.age && Objects.equals(name, other.name);
    }
    // hashCode() NOT overridden!  → uses the identity-based default
}
```

```java
Set<Broken> set = new HashSet<>();
set.add(new Broken("Ali", 20));
set.add(new Broken("Ali", 20));                          // equal to the first one!

System.out.println(set.size());                          // 2  (almost certainly): duplicate NOT detected!
System.out.println(set.contains(new Broken("Ali", 20))); // false: can't find it!
```

Why? The two "equal" objects have **different identity hash codes**, so they land in **different buckets**. The set never even calls `equals()` because it never looks in the same bucket.

```
 add(Ali#1): hash 111 → bucket 3     [Ali#1]
 add(Ali#2): hash 987 → bucket 7     [Ali#2]    ← different bucket: set thinks it's a new element
 contains(Ali#3): hash 456 → bucket 1 → empty → false
```

### 7.7 The fix: override `hashCode()` using the same fields as `equals()`

**Rule:** use **exactly the fields** (or a subset of them) that `equals()` uses.

```java
@Override
public int hashCode() {
    return Objects.hash(name, age);
}
```

```java
Set<Student> set = new HashSet<>();
set.add(new Student("Ali", 20));
set.add(new Student("Ali", 20));

System.out.println(set.size());                          // 1  ✓
System.out.println(set.contains(new Student("Ali", 20)));// true ✓
```

### 7.8 How `Objects.hash(...)` works

`Objects.hash(a, b, c)` computes a number from all the fields. Internally it is:

```
 result = 1
 for each field f:
     result = 31 * result + (f == null ? 0 : f.hashCode())
```

**Worked example:** `Objects.hash("Ali", 20)`

```
 "Ali".hashCode() = 'A'*31² + 'l'*31 + 'i' = 65*961 + 108*31 + 105 = 62465 + 3348 + 105 = 65918
 Integer(20).hashCode() = 20

 result = 1
 result = 31 * 1      + 65918 = 65949
 result = 31 * 65949  + 20    = 2044419 + 20 = 2044439

 Objects.hash("Ali", 20)  =  2044439
```

**Why 31?** It's an **odd prime**: it mixes the bits well (fewer collisions), and the JVM can compute `31 * x` quickly as `(x << 5) - x`. Order matters: `Objects.hash("Ali", 20)` differs from `Objects.hash(20, "Ali")`.

**The manual version (what IDEs generate):**

```java
@Override
public int hashCode() {
    int result = 17;
    result = 31 * result + (name == null ? 0 : name.hashCode());
    result = 31 * result + age;
    return result;
}
```

### 7.9 What makes a GOOD hash code?

1. **Equal objects → equal hash codes** (mandatory).
2. **Spread out evenly:** different objects should land in different buckets.
3. **Fast to compute.**

A **legal but terrible** hash code:

```java
@Override
public int hashCode() {
    return 1;               // always the same: legal, but EVERYTHING collides
}
```

It obeys the contract, but all objects go into **one bucket**, turning the hash table into a plain list with O(n) search. (Modern `HashMap` converts crowded buckets into balanced trees to soften the damage, but it is still slow.)

### 7.10 `String.hashCode()`: collisions are real

`String` overrides `hashCode()` with the same 31-formula over its characters:

```
 s.hashCode() = s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
```

```java
System.out.println("Aa".hashCode());     // 2112   (65*31 + 97)
System.out.println("BB".hashCode());     // 2112   (66*31 + 66)
System.out.println("Aa".equals("BB"));   // false  ← same hash code, NOT equal (a collision!)
```

### 7.11 The mutable-key trap

If a field used in `hashCode()` **changes after** the object is stored in a hash collection, the object is "lost":

```java
Student s = new Student("Ali", 20);          // imagine age has a setter
Set<Student> set = new HashSet<>();
set.add(s);                                  // stored in the bucket for hash(Ali, 20)

s.setAge(21);                                // hashCode changes!

System.out.println(set.contains(s));         // false  ← it's sitting in the OLD bucket
```

**Rule:** use **immutable** objects (or immutable fields) as keys of hash maps and elements of hash sets. That is why `String` and wrapper classes are perfect keys.

### 7.12 Where hash codes are used

| Collection | How it uses `hashCode()` |
|------------|-------------------------|
| `HashMap` | Picks the bucket for each **key** |
| `HashSet` | Picks the bucket for each **element** (it's backed by a `HashMap`) |
| `Hashtable` | Same idea (older, synchronized) |
| `LinkedHashMap`, `LinkedHashSet` | Same, plus keeps insertion order |
| `TreeMap`, `TreeSet` | **Don't use hash codes**: they use `compareTo`/`Comparator` |
| `ArrayList` | **Doesn't use it** (`contains` uses `equals` with a linear scan) |

---

## 8. `equals()` and `hashCode()` Together

### 8.1 The golden pairing

> **Whenever you override `equals()`, override `hashCode()` too, using the same fields.**

```
   equals()                              hashCode()
 "what makes two objects equal?"     "a number that is the same for equal objects"
        │                                      │
        └──────── MUST agree ──────────────────┘
                (equal objects ⇒ equal hash codes)
```

### 8.2 The recipe (copy this pattern)

```java
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    MyClass other = (MyClass) obj;
    return field1 == other.field1 && Objects.equals(field2, other.field2);
}

@Override
public int hashCode() {
    return Objects.hash(field1, field2);        // same fields as equals
}
```

### 8.3 Comparison summary

| | `toString()` | `equals()` | `hashCode()` |
|---|-------------|------------|--------------|
| Question it answers | "Describe yourself as text" | "Are you equal to this?" | "Give me your fingerprint number" |
| Default | `ClassName@hex` | `this == obj` | Identity-based number |
| Override when | Almost always | Objects have logical equality | **Whenever you override equals** |
| Used by | `println`, `+`, logs | `contains`, `indexOf`, `remove`, etc. | `HashMap`, `HashSet` |
| Related contract | None strict | 5 rules (reflexive, symmetric...) | Equal ⇒ same hash |

---

## 9. `getClass()`

### 9.1 First principles

**① Problem:** a variable's **declared type** can differ from the object's **real type**:

```java
Object o = "hello";        // declared type: Object   real type: String
Animal a = new Dog();      // declared type: Animal   real type: Dog
```

At runtime, how do we find out what the object **really** is?

**② Need:** every object must **know and report its real class**, plus information about that class (its name, parent, methods...).

**③ Solution:** `getClass()` returns a **`Class` object**, a runtime description (metadata) of the object's class.

### 9.2 Basics

```java
public final Class<?> getClass()
```

| Property | Detail |
|----------|--------|
| **Returns** | A `Class<?>` object describing the **actual runtime class** |
| **`final`** | **Cannot be overridden.** The JVM must be able to trust it. If a class could lie about its type, `equals`, reflection and security would all break |
| **Native** | Implemented inside the JVM |

```java
Object o = "hello";

System.out.println(o.getClass());                  // class java.lang.String
System.out.println(o.getClass().getName());        // java.lang.String       (full name with package)
System.out.println(o.getClass().getSimpleName());  // String                 (just the name)
System.out.println(o.getClass().getSuperclass());  // class java.lang.Object
```

Note: it reports the **real** type (`String`), not the declared type (`Object`).

### 9.3 Useful `Class` methods

| Method | Result for a `String` |
|--------|----------------------|
| `getName()` | `java.lang.String` |
| `getSimpleName()` | `String` |
| `getPackageName()` | `java.lang` |
| `getSuperclass()` | `class java.lang.Object` |
| `getInterfaces()` | `Serializable, Comparable, CharSequence...` |
| `isInterface()`, `isArray()`, `isEnum()` | `false` |
| `isInstance(obj)` | like `instanceof`, but dynamic |
| `getDeclaredMethods()` / `getDeclaredFields()` | Lists methods/fields (**reflection**) |

### 9.4 One `Class` object per class

Java keeps **exactly one** `Class` object per class (per class loader). So you can compare classes with `==`:

```java
Student a = new Student("Ali", 20);
Student b = new Student("Sara", 22);

System.out.println(a.getClass() == b.getClass());   // true  (same class)
System.out.println(a.getClass() == Student.class);  // true  (Student.class is the "class literal")
```

`Student.class` (a class literal) and `a.getClass()` both give the same `Class` object. Primitives have class literals too: `int.class`.

### 9.5 `getClass()` in `equals()` and with inheritance

```java
Animal a = new Dog();

System.out.println(a instanceof Animal);                  // true   (a Dog IS an Animal)
System.out.println(a.getClass() == Animal.class);         // false  (the real class is Dog)
System.out.println(a.getClass() == Dog.class);            // true
```

| | `instanceof` | `getClass() ==` |
|---|--------------|-----------------|
| Meaning | "Is it this type **or a subtype**?" | "Is it **exactly** this class?" |

### 9.6 Arrays and `getClass()`

```java
int[] nums = {1, 2, 3};
System.out.println(nums.getClass().getName());         // [I
System.out.println(nums.getClass().getSimpleName());   // int[]
System.out.println(new String[0].getClass().getName());// [Ljava.lang.String;
System.out.println(new int[0][0].getClass().getName());// [[I
```

(That is where the strange `[I@1b6d3586` text from printing arrays comes from.)

### 9.7 Reflection: a first taste

`Class` objects let a program **inspect and use classes it didn't know at compile time**:

```java
Class<?> c = Class.forName("java.util.ArrayList");          // find a class by name (a String!)
Object list = c.getDeclaredConstructor().newInstance();     // create an object of it
System.out.println(list.getClass().getName());              // java.util.ArrayList
```

(This is how frameworks like Spring and Hibernate work. They throw checked exceptions, so wrap them in `try/catch` or `throws Exception`.)

---

## 10. `clone()`

### 10.1 First principles

**① Problem:** we want a **copy** of an object. But assignment doesn't copy:

```java
Person p1 = new Person("Ali");
Person p2 = p1;              // copies the REFERENCE only: still ONE object
```

```
 p1 ──┐
      ├──► [ Person "Ali" ]       one object, two names
 p2 ──┘
```

**② Need:** a way to create a **new, separate object** with the **same field values**.

**③ Solution:** `clone()`. It builds a new object and copies the fields over.

### 10.2 The signature and the three requirements

```java
protected Object clone() throws CloneNotSupportedException
```

| Part | Meaning |
|------|---------|
| **`protected`** | Not callable from outside by default. Cloning copies the state **without running a constructor**, which is dangerous (it could break singletons or immutables). So a class has to **opt in** by overriding it as `public`. |
| **returns `Object`** | You need to **cast** the result (or use a covariant return type in your override). |
| **`throws CloneNotSupportedException`** | A **checked** exception: thrown if the class didn't opt in. |

**To make a class cloneable you need all three steps:**

1. **Implement the `Cloneable` interface.**
2. **Override `clone()`** and make it `public`.
3. **Call `super.clone()`** inside it (which does the actual copying), and handle the checked exception.

### 10.3 `Cloneable`: a marker interface

```java
public interface Cloneable { }       // EMPTY: no methods
```

An interface with **no methods** is called a **marker interface**. It works like a **permission slip**: "this class allows its objects to be copied". `Object.clone()` checks for it at runtime:

```java
class NoPermission { }

new NoPermission().clone();     // not accessible (protected)... and if called via super.clone():
                                // throws CloneNotSupportedException
```

### 10.4 A simple clone

```java
public class Point implements Cloneable {       // step 1: opt in
    int x, y;

    Point(int x, int y) { this.x = x; this.y = y; }

    @Override
    public Point clone() {                      // step 2: public, covariant return type (Point)
        try {
            return (Point) super.clone();       // step 3: let Object.clone() copy the fields
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);        // can't happen: we implement Cloneable
        }
    }
}
```

```java
Point p1 = new Point(1, 2);
Point p2 = p1.clone();

p2.x = 99;
System.out.println(p1.x);            // 1   (p1 untouched)
System.out.println(p2.x);            // 99
System.out.println(p1 == p2);        // false  (a different object)
System.out.println(p1.getClass() == p2.getClass());   // true (same class)
```

### 10.5 What `Object.clone()` actually does

It allocates a **new object of the same runtime class** and **copies every field's value** one by one (bit for bit). It does **not** call any constructor.

```
   p1                                   p2 (clone)
 ┌───────────┐                       ┌───────────┐
 │ x = 1     │  ── field copy ─────► │ x = 1     │
 │ y = 2     │                       │ y = 2     │
 └───────────┘                       └───────────┘
```

### 10.6 Shallow copy vs deep copy (the big idea)

`Object.clone()` is a **shallow copy**: it copies each field's **value**. For a **primitive** field that's the actual value. For an **object** field, the value is a **reference**, so the copy and the original end up **sharing the same inner object**.

```java
class Address {
    String city;
    Address(String city) { this.city = city; }
}

class Person implements Cloneable {
    String name;
    Address address;                   // an OBJECT field

    // ... shallow clone via super.clone() ...
}
```

```
 SHALLOW COPY                                    DEEP COPY

 original ──► [Person: Ali, address ●]──┐        original ──► [Person: Ali, address ●]──► [Address: Paris]
                                         ├► [Address: Paris]
 copy ─────► [Person: Ali, address ●]──┘        copy ─────► [Person: Ali, address ●]──► [Address: Paris]
      (both share ONE Address!)                      (each has its OWN Address)
```

Changing the shared inner object through the copy changes the original too.

**Making a deep copy:** override `clone()` and **clone the object fields yourself**:

```java
@Override
public Person clone() {
    try {
        Person copy = (Person) super.clone();        // shallow copy first
        copy.address = this.address.clone();         // then clone each mutable object field
        return copy;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError(e);
    }
}
```

### 10.7 Full demo: shallow vs deep

**File: `CloneDemo.java`**

```java
public class CloneDemo {

    static class Address implements Cloneable {
        String city;
        Address(String city) { this.city = city; }

        @Override
        public Address clone() {
            try {
                return (Address) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
    }

    static class Person implements Cloneable {
        String name;
        Address address;

        Person(String name, Address address) {
            this.name = name;
            this.address = address;
        }

        // SHALLOW copy: only Object.clone()
        Person shallowCopy() {
            try {
                return (Person) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }

        // DEEP copy: also clone the Address
        @Override
        public Person clone() {
            try {
                Person copy = (Person) super.clone();
                copy.address = address.clone();
                return copy;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
    }

    public static void main(String[] args) {
        // --- shallow ---
        Person p1 = new Person("Ali", new Address("Paris"));
        Person shallow = p1.shallowCopy();
        shallow.address.city = "Rome";
        System.out.println("Shallow: original city = " + p1.address.city);     // Rome  (shared!)

        // --- deep ---
        Person p2 = new Person("Sara", new Address("Paris"));
        Person deep = p2.clone();
        deep.address.city = "Rome";
        System.out.println("Deep:    original city = " + p2.address.city);     // Paris (independent)

        System.out.println("Different objects? " + (p2 != deep));              // true
    }
}
```

Output:
```
Shallow: original city = Rome
Deep:    original city = Paris
Different objects? true
```

### 10.8 Arrays can be cloned directly

Arrays have a **public** `clone()` that returns the correct array type (shallow):

```java
int[] a = {1, 2, 3};
int[] b = a.clone();            // a new array with the same values
b[0] = 99;
System.out.println(a[0]);       // 1

String[] names = {"A", "B"};
String[] copy = names.clone();  // shallow: the String references are copied (fine: Strings are immutable)
```

For an array of **mutable objects**, a `clone()` is shallow (both arrays share the same elements).

### 10.9 Why `clone()` is considered awkward

| Problem | Detail |
|---------|--------|
| **Doesn't call constructors** | Bypasses your initialization and validation |
| **Weird design** | `Cloneable` has no `clone()` method; `Object.clone()` is `protected` |
| **Checked exception** | `CloneNotSupportedException` is annoying |
| **Trouble with `final` fields** | A `final` object field can't be re-assigned for a deep copy |
| **Easy to get wrong** | Forgetting the deep copy leaves hidden sharing |

### 10.10 Better alternatives

```java
// 1) Copy constructor
public Person(Person other) {
    this.name = other.name;
    this.address = new Address(other.address.city);      // deep copy by hand
}

// 2) Static copy factory
public static Person copyOf(Person other) { return new Person(other); }

// 3) Use immutable classes or records: no copying needed at all
// 4) Serialization-based deep copy (for complex graphs)
```

Most modern Java code uses **copy constructors** or immutability instead of `clone()`. You still need to understand `clone()` because it is part of `Object` (and it is asked in interviews).

---

## 11. Deprecated and Advanced Methods

### 11.1 `finalize()`: deprecated, don't use it

**① The old idea:** "When the garbage collector is about to destroy an object, run some cleanup code first (close a file, release a resource)."

```java
@Override
protected void finalize() throws Throwable {
    // cleanup...
}
```

**② Why it was a bad idea:**

| Problem | Explanation |
|---------|-------------|
| **Unpredictable timing** | You can't know **when** (or **if**) the GC will run. The program might end first, so cleanup might **never** happen. |
| **Not guaranteed** | The JVM doesn't promise to call it at all. |
| **Slow** | Finalizable objects make garbage collection slower and use extra memory. |
| **Unsafe** | A finalizer can "resurrect" the object, and exceptions inside it are silently ignored. It can also be a security risk. |
| **Wrong tool** | Resources like files and sockets should be freed **promptly**, not "sometime later". |

**③ Status:** deprecated since **Java 9**, and **deprecated for removal since Java 18**. Treat it as **gone**.

**④ What to use instead:**

```java
// Best: try-with-resources + AutoCloseable (cleanup runs exactly when the block ends)
try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
    System.out.println(br.readLine());
}   // br.close() is called automatically here

// For rare advanced cases: java.lang.ref.Cleaner (Java 9+)
```

You can also **request** garbage collection with `System.gc()`, but it's only a **hint**, not a command.

### 11.2 `wait()`, `notify()`, `notifyAll()`: thread communication

These are for **multithreading** (full detail comes in a later topic). Here is the first-principles idea.

**① Problem:** two threads must **coordinate**. For example, a *consumer* thread must **wait** until a *producer* thread has put something in a queue. Looping and checking constantly wastes CPU.

**② Need:** a way for a thread to say "**put me to sleep until something changes**", and for another thread to say "**wake up, it changed**".

**③ Solution:** `wait()` to sleep, `notify()` / `notifyAll()` to wake.

### Why are they in `Object` (not in `Thread`)?

Because **every object in Java has a built-in lock (monitor)**, and these methods work with **that object's lock**. Any object can be a meeting point between threads, so the methods must exist on every object, so they live in `Object`.

| Method | What it does |
|--------|--------------|
| `wait()` | Current thread **releases the lock** and sleeps until another thread calls `notify`/`notifyAll` on the **same object** |
| `wait(long ms)` | Same, but gives up waiting after the timeout |
| `notify()` | Wakes **one** thread waiting on this object |
| `notifyAll()` | Wakes **all** threads waiting on this object |

All of them are **`final`**.

### The rules

1. You must hold the object's lock: call them **inside `synchronized (obj) { ... }`** (or a `synchronized` method of that object). Otherwise: **`IllegalMonitorStateException`**.
2. `wait()` can throw **`InterruptedException`** (checked), so handle it.
3. **Always call `wait()` inside a `while` loop** that re-checks the condition. A thread can wake up **spuriously** (without being notified), or another thread may have changed the condition again.
4. `wait()` **releases the lock** while sleeping; `Thread.sleep()` does **not**.

### A tiny example

**File: `WaitNotifyDemo.java`**

```java
public class WaitNotifyDemo {

    private static final Object lock = new Object();     // a plain Object used as a lock
    private static boolean ready = false;

    public static void main(String[] args) throws InterruptedException {

        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                while (!ready) {                          // always wait in a loop
                    try {
                        lock.wait();                      // release the lock and sleep
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                System.out.println("Waiter: got the signal, continuing");
            }
        });

        waiter.start();
        Thread.sleep(500);                                // give the waiter time to start waiting

        synchronized (lock) {
            ready = true;                                 // change the condition
            lock.notify();                                // wake the waiter
            System.out.println("Main: signal sent");
        }

        waiter.join();                                    // wait for the waiter to finish
    }
}
```

Output:
```
Main: signal sent
Waiter: got the signal, continuing
```

The waiter can only continue after `main` leaves the `synchronized` block (releasing the lock), so "Main: signal sent" prints first.

(Newer code often uses higher-level tools such as `BlockingQueue`, `CountDownLatch` and `Lock`/`Condition`, but they are built on the same ideas.)

---

## 12. Arrays, Primitives and Wrapper Classes

### 12.1 Arrays are objects, so they extend `Object`

```java
int[] nums = {1, 2, 3};

Object o = nums;                            // allowed: an int[] IS an Object
System.out.println(nums instanceof Object); // true
System.out.println(nums.getClass().getSuperclass());   // class java.lang.Object
```

Because arrays are objects, they have **`Object`'s methods**, with these behaviors:

| Method on an array | What happens |
|--------------------|--------------|
| `toString()` | **Not overridden**: `[I@1b6d3586` (use `Arrays.toString(arr)`) |
| `equals()` | **Not overridden**: identity comparison (use `Arrays.equals(a, b)`) |
| `hashCode()` | Identity-based (use `Arrays.hashCode(arr)`) |
| `getClass()` | `[I` for `int[]`, `[Ljava.lang.String;` for `String[]` |
| `clone()` | **Public**, returns a shallow copy of the array |

Arrays also automatically implement `Cloneable` and `Serializable`.

```java
int[] a = {1, 2, 3};
int[] b = {1, 2, 3};

System.out.println(a.equals(b));                 // false (identity)
System.out.println(Arrays.equals(a, b));         // true
System.out.println(a);                           // [I@...
System.out.println(Arrays.toString(a));          // [1, 2, 3]
```

An `Object[]` can hold anything:

```java
Object[] box = {1, "two", 3.0, 'c', true, new int[]{9}};
for (Object item : box) {
    System.out.println(item.getClass().getSimpleName());
}
// Integer, String, Double, Character, Boolean, int[]
```

### 12.2 Primitives are NOT objects

`int`, `char`, `float`, `double`, `boolean`, `byte`, `short`, `long` are **not** classes, **do not** extend `Object`, and have **no methods**.

```java
int x = 5;
x.toString();                 // ERROR: int cannot be dereferenced
Object o = x;                 // works, BUT only because of autoboxing: x becomes an Integer first
```

**First principles: why does Java have non-object types at all?**
Objects cost extra memory (a header and a reference) and time (heap allocation, garbage collection). For simple numbers used billions of times in loops, that is too expensive. Primitives are **raw values**: small and fast.

### 12.3 Wrapper classes ARE objects

For each primitive there is a **wrapper class** that wraps the value inside an object. Wrappers **do extend `Object`**.

| Primitive | Wrapper | Direct parent |
|-----------|---------|---------------|
| `byte` | `Byte` | `Number` |
| `short` | `Short` | `Number` |
| `int` | `Integer` | `Number` |
| `long` | `Long` | `Number` |
| `float` | `Float` | `Number` |
| `double` | `Double` | `Number` |
| `char` | `Character` | `Object` |
| `boolean` | `Boolean` | `Object` |

```
                        Object
          ┌────────────┬──┴─────────┬─────────────┐
       Number      Character     Boolean        String ...
   ┌──┬──┬──┬──┬──┐
 Byte Short Integer Long Float Double      (also BigInteger, BigDecimal, AtomicInteger...)
```

`Number` is an **abstract class** (it adds methods like `intValue()` and `doubleValue()`), and it extends `Object`.

### 12.4 Wrappers override the `Object` methods properly

```java
Integer a = 100, b = 100;
System.out.println(a.toString());         // 100
System.out.println(a.equals(b));          // true   (compares the VALUE)
System.out.println(a.hashCode());         // 100    (the int value itself)

Boolean t = true;
System.out.println(t.hashCode());         // 1231   (Boolean uses 1231 for true, 1237 for false)

Long l = 100L;
System.out.println(l.equals(100));        // false! 100 is boxed as Integer, and a Long never equals an Integer
System.out.println(l.equals(100L));       // true
```

### 12.5 Autoboxing: how a primitive becomes an `Object`

```java
Object o = 42;                          // int 42 → autoboxed to Integer → stored as Object
System.out.println(o.getClass());       // class java.lang.Integer
System.out.println(o.toString());       // 42
System.out.println(o.equals(42));       // true

Integer boxed = 42;                     // boxing
int plain = boxed;                      // unboxing
```

This is why collections like `List<Integer>` work: they store **objects**, and `int` values get wrapped automatically.

### 12.6 `null` and `Object`

```java
Object o = null;
o.toString();            // NullPointerException
o.equals("x");           // NullPointerException
"x".equals(o);           // false (safe: call equals on the non-null side)
```

Tip: write `"literal".equals(variable)` so a null variable doesn't crash.

---

## 13. The `Objects` Utility Class (Different from `Object`)

Don't confuse the two:

| | `Object` | `Objects` |
|---|----------|-----------|
| Package | `java.lang` | `java.util` (needs `import java.util.Objects;`) |
| What it is | The **root class** every class extends | A **helper class** with static utility methods |
| Created with `new`? | Yes | No (all methods are `static`) |

Handy `Objects` methods (they make `equals`, `hashCode` and `toString` easier and null-safe):

| Method | Purpose | Example |
|--------|---------|---------|
| `Objects.equals(a, b)` | **Null-safe** equality | `Objects.equals(null, null)` → `true` |
| `Objects.hash(a, b, c...)` | Combined hash of several values | `Objects.hash(name, age)` |
| `Objects.hashCode(o)` | Hash of one object (`0` for null) | `Objects.hashCode(null)` → `0` |
| `Objects.toString(o)` | `"null"` for null, else `o.toString()` | `Objects.toString(null)` → `"null"` |
| `Objects.toString(o, "default")` | Custom text for null | `Objects.toString(null, "N/A")` → `"N/A"` |
| `Objects.requireNonNull(o, "msg")` | Throws `NullPointerException` if null | Validate constructor arguments |
| `Objects.isNull(o)` / `nonNull(o)` | Null checks (handy in lambdas) | `list.removeIf(Objects::isNull)` |

```java
public Student(String name, int age) {
    this.name = Objects.requireNonNull(name, "name must not be null");
    this.age = age;
}
```

---

## 14. Practical Programs

### 14.1 `Student`: `toString`, `equals`, `hashCode` working together

**File: `Student.java`**

```java
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Student {
    private final String name;
    private final int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Student[name=" + name + ", age=" + age + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;                           // 1. same object
        if (obj == null) return false;                          // 2. null
        if (getClass() != obj.getClass()) return false;         // 3. same class
        Student other = (Student) obj;                          // 4. cast
        return age == other.age && Objects.equals(name, other.name);   // 5. fields
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);                         // same fields as equals
    }

    public static void main(String[] args) {
        Student a = new Student("Ali", 20);
        Student b = new Student("Ali", 20);
        Student c = new Student("Sara", 22);

        System.out.println(a);                                    // Student[name=Ali, age=20]
        System.out.println("a == b          : " + (a == b));      // false
        System.out.println("a.equals(b)     : " + a.equals(b));   // true
        System.out.println("a.equals(c)     : " + a.equals(c));   // false
        System.out.println("a.equals(null)  : " + a.equals(null));// false
        System.out.println("same hashCode   : " + (a.hashCode() == b.hashCode()));  // true
        System.out.println("hashCode of a   : " + a.hashCode());  // 2044439 (hash of "Ali", 20)

        Set<Student> set = new HashSet<>(List.of(a, c));
        set.add(b);                                               // equal to a: not added again
        System.out.println("set size        : " + set.size());    // 2
        System.out.println("set contains    : " + set.contains(new Student("Sara", 22)));   // true

        Map<Student, String> grades = new HashMap<>();
        grades.put(a, "A");
        System.out.println("grade of b      : " + grades.get(b)); // A  (b is equal to a)

        System.out.println("class           : " + a.getClass().getSimpleName());   // Student
    }
}
```

Output:
```
Student[name=Ali, age=20]
a == b          : false
a.equals(b)     : true
a.equals(c)     : false
a.equals(null)  : false
same hashCode   : true
hashCode of a   : 2044439
set size        : 2
set contains    : true
grade of b      : A
class           : Student
```

### 14.2 A tour of the default `Object` behaviors

**File: `ObjectTour.java`**

```java
import java.util.Arrays;

public class ObjectTour {

    static class Plain { }          // overrides nothing

    public static void main(String[] args) {
        Plain p1 = new Plain();
        Plain p2 = new Plain();

        // toString: ClassName@hex
        System.out.println(p1.toString().startsWith("ObjectTour$Plain@"));     // true

        // equals: identity only
        System.out.println(p1.equals(p2));                                     // false
        System.out.println(p1.equals(p1));                                     // true

        // hashCode: identity based, stable for the same object
        System.out.println(p1.hashCode() == p1.hashCode());                    // true
        System.out.println(p1.hashCode() == System.identityHashCode(p1));      // true

        // getClass
        System.out.println(p1.getClass().getName());                           // ObjectTour$Plain
        System.out.println(p1.getClass() == p2.getClass());                    // true

        // arrays are objects, with identity-based equals
        int[] a = {1, 2, 3};
        int[] b = {1, 2, 3};
        System.out.println(a.equals(b));                                       // false
        System.out.println(Arrays.equals(a, b));                               // true
        System.out.println(a.getClass().getName());                            // [I

        // wrappers are objects with value-based equals
        Object o = 42;
        System.out.println(o.getClass().getName());                            // java.lang.Integer
        System.out.println(o.equals(42));                                      // true
    }
}
```

### 14.3 Try it yourself

1. Create a `Book` class (`title`, `author`, `year`). Override `toString`, `equals`, `hashCode`, then test it in a `HashSet` and as a `HashMap` key.
2. **Break it on purpose:** remove `hashCode()` from your `Book` and observe the `HashSet` accept duplicates. Add it back.
3. Add a `setYear` method, put a `Book` in a `HashSet`, change the year, and watch `contains` fail (the mutable-key trap).
4. Write a `Library` class with an `Address` field. Implement a **shallow** and a **deep** `clone()` and prove the difference.
5. Create two different objects whose hash codes **collide** (hint: `"Aa"` and `"BB"`), and put them both in a `HashSet` to see that `equals()` still keeps them apart.
6. Print `new Object()`, `new int[3]`, and `new String[2]` and explain each output.

---

## 15. Common Mistakes

| # | Mistake | What happens | Fix |
|---|---------|--------------|-----|
| 1 | Overriding `equals` but **not** `hashCode` | `HashSet`/`HashMap` can't find or de-duplicate equal objects | Always override both, using the same fields |
| 2 | Writing `equals(Student s)` instead of `equals(Object o)` | **Overloads** instead of overriding; collections ignore it | Use `Object` as the parameter and add `@Override` |
| 3 | Comparing objects/Strings with `==` | Compares references | Use `.equals()` |
| 4 | No `null` check in `equals` before `getClass()` | `NullPointerException` | `if (obj == null) return false;` |
| 5 | Using `==` to compare `double` fields in `equals` | Wrong for `NaN`/`-0.0` | `Double.compare(a, b) == 0` |
| 6 | Using **mutable** fields in `hashCode` and then mutating a key stored in a `HashSet`/`HashMap` | The object is "lost" | Use immutable keys |
| 7 | Returning a constant from `hashCode` | All objects collide, giving O(n) lookups | `Objects.hash(fields...)` |
| 8 | Printing an object and seeing `Student@1b6d3586` | `toString()` not overridden | Override `toString()` |
| 9 | Calling `toString()` on `null` | `NullPointerException` | `String.valueOf(o)` or `Objects.toString(o)` |
| 10 | `toString()` of two objects calling each other | `StackOverflowError` | Print only an ID/name of the other object |
| 11 | Printing/`equals` on arrays directly | Address text / identity comparison | `Arrays.toString`, `Arrays.equals` |
| 12 | Calling `clone()` on a class without `Cloneable` | `CloneNotSupportedException` | Implement `Cloneable` and override `clone()` as `public` |
| 13 | Assuming `clone()` makes a deep copy | Nested objects are shared | Clone nested objects too, or use a copy constructor |
| 14 | Relying on `finalize()` | Unpredictable, deprecated for removal | `try-with-resources` / `Cleaner` |
| 15 | Calling `wait()`/`notify()` outside `synchronized` | `IllegalMonitorStateException` | Call them inside `synchronized (obj)` on the **same** object |
| 16 | `if` instead of `while` around `wait()` | Spurious wake-ups break the logic | `while (!condition) obj.wait();` |
| 17 | `longObj.equals(100)` | `false` (Integer vs Long) | `longObj.equals(100L)` |
| 18 | Mixing `getClass()` and `instanceof` carelessly in `equals` | Broken symmetry | Pick `getClass()`, or `final` + `instanceof` |
| 19 | Using `Object` as a parameter type everywhere | Loses type safety, needs casts | Use generics |
| 20 | Using `equals` fields that **aren't** in `hashCode` or vice versa | Contract violation | Keep both based on the same fields |

---

## 16. Cheat Sheet, Summary and Quiz

### 16.1 Method cheat sheet

| Method | Default | Override? | Typical override |
|--------|---------|-----------|------------------|
| `toString()` | `ClassName@hex` | **Yes**, almost always | `"Student[name=" + name + "]"` |
| `equals(Object)` | `this == obj` | **Yes**, for logical equality | The 5-step pattern |
| `hashCode()` | Identity-based | **Yes**, **whenever you override `equals`** | `Objects.hash(fields...)` |
| `getClass()` | Real runtime class | **Can't** (`final`) | n/a |
| `clone()` | Shallow copy (needs `Cloneable`) | Rarely | `super.clone()` + deep-copy fields |
| `finalize()` | Does nothing | **No** (deprecated for removal) | Use `try-with-resources` |
| `wait()` / `notify()` / `notifyAll()` | Thread coordination | **Can't** (`final`) | Call inside `synchronized` |

### 16.2 The patterns to memorize

```java
// toString
@Override
public String toString() {
    return "ClassName[field1=" + field1 + ", field2=" + field2 + "]";
}

// equals
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ClassName other = (ClassName) obj;
    return field1 == other.field1 && Objects.equals(field2, other.field2);
}

// hashCode (same fields as equals)
@Override
public int hashCode() {
    return Objects.hash(field1, field2);
}
```

### 16.3 Summary in simple points

1. **`Object`** (`java.lang`, auto-imported) is the **root of every class**. If a class doesn't extend anything, Java makes it extend `Object`. Arrays and wrapper classes are objects too; **primitives are not**.
2. It exists so there is **one type for anything** (polymorphism) and so **every object has guaranteed basic behaviors**.
3. **`toString()`** gives text. Default: `ClassName@hex`. Override it to print useful field values. `println` and `+` call it automatically.
4. **`equals()`** answers "are these logically equal?". Default is `==` (same reference). Override with: same-reference check, null check, class check, cast, compare fields.
5. **`hashCode()`** gives an `int` used by hash collections to pick a bucket. **Golden Rule: equal objects must have equal hash codes**; equal hash codes don't guarantee equal objects (collisions). Build it with `Objects.hash(...)` from the **same fields** as `equals`.
6. **Override `equals` and `hashCode` together**, or `HashSet` and `HashMap` will misbehave.
7. **`getClass()`** returns the real runtime class as a `Class` object. It's `final` and is used in `equals` checks and reflection.
8. **`clone()`** makes a copy. It needs `Cloneable` (a **marker interface** with no methods), a **public override** that calls `super.clone()`, and it is a **shallow copy** by default. Deep copy needs manual cloning of nested objects. Often a copy constructor is better.
9. **`finalize()`** is **deprecated** (for removal): unpredictable and unsafe. Use `try-with-resources`.
10. **`wait()`, `notify()`, `notifyAll()`** are `final` thread-coordination methods that work with an object's built-in lock, so they must be called inside `synchronized`.
11. **`Objects`** (plural, `java.util`) is a **helper class** that makes `equals`, `hashCode` and `toString` null-safe and short.

### 16.4 Mini Quiz

1. In which package is `Object`, and do you need to import it?
2. If you write `class A { }`, what is `A`'s superclass?
3. What does the default `toString()` print, and where does the hex number come from?
4. What does the default `equals()` do?
5. What are the four checks/steps of a solid `equals()` override, as in the video?
6. State the golden rule of `hashCode()` and `equals()`.
7. If two objects have the same hash code, are they equal?
8. What happens in a `HashSet` if you override `equals` but not `hashCode`?
9. Why is `getClass()` `final`?
10. What is a marker interface? Which one does `clone()` require?
11. Is `Object.clone()` deep or shallow? What does that mean?
12. Why is `finalize()` deprecated? Name two reasons and one alternative.
13. Why do `wait()` and `notify()` belong to `Object` and not to `Thread`?
14. Are `int` and `Integer` objects? Does each extend `Object`?
15. What does `System.out.println(new int[]{1, 2})` print, and what should you use instead?
16. What does `Objects.hash("Ali", 20)` return (compute it)?

<details>
<summary>Click for answers</summary>

1. **`java.lang`**, and **no import** is needed (the whole package is imported automatically).
2. **`Object`**. The compiler adds `extends Object` automatically.
3. **`ClassName@hexadecimalCode`**. The hex number is the object's **hash code** (identity-based by default) in hexadecimal.
4. It compares **references**: it returns `true` only if both variables point to the **same object** (`this == obj`).
5. **(1)** `this == obj` → `true`; **(2)** `obj == null` → `false`; **(3)** compare classes with `getClass()`; **(4)** cast to the class and compare the fields. (Step 5 is the field comparison.)
6. **If `a.equals(b)` is `true`, then `a.hashCode()` must equal `b.hashCode()`.**
7. **Not necessarily.** Different objects can collide. `equals()` decides.
8. Equal objects get **different hash codes**, so they land in **different buckets**: duplicates aren't detected and `contains` fails to find equal objects.
9. So that the JVM and libraries can **trust** it to report an object's **true type**. If it could be overridden, objects could lie, breaking reflection, `equals` checks and security.
10. An interface with **no methods** that just "marks" a class with a capability or permission. `clone()` requires **`Cloneable`**.
11. **Shallow.** It copies field values, so object fields copy only the **reference**: the original and the copy **share** the inner objects.
12. Reasons: **unpredictable timing / may never run**, **slow**, **unsafe**, **can resurrect objects**. Alternative: **`try-with-resources`** (`AutoCloseable`) or `Cleaner`.
13. Because **every object has a built-in lock (monitor)** and these methods work on that lock. Any object can be a coordination point between threads.
14. **`int` is not an object** and doesn't extend `Object`. **`Integer` is an object** and extends `Number`, which extends `Object`.
15. Something like **`[I@1b6d3586`**. Use **`Arrays.toString(arr)`** to print `[1, 2]`.
16. **2044439**: start with 1; `31*1 + 65918 = 65949`; `31*65949 + 20 = 2044439`.

</details>

---

*Practice tip: whenever you create a new class, ask the three first-principles questions: "Does it need to print itself?", "What makes two of these equal?", and "Will it ever go into a hash collection?" The answers tell you exactly which `Object` methods to override.*
