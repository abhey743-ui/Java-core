# Immutable Classes in Java
### Issue → Problem → First Principles → Solution (step by step, with diagrams)

> **How these notes flow:**
> 1. The **issue** (why anyone cares)
> 2. The **core idea** from first principles (what is "immutable", why it helps)
> 3. The **4 rules** and why each rule exists
> 4. The **hidden problem**: reference types and the shallow copy leak (with a worked example and memory diagrams)
> 5. The **solution**: defensive copies in the constructor and getters, step by step
> 6. The **best solution**: make the inner objects immutable too
> 7. Collections, arrays, dates, records, thread safety, pros and cons
> 8. Interview questions, quiz, checklist, cheat sheet

---

## Table of Contents

1. [The issue: why do we need immutability?](#1-the-issue-why-do-we-need-immutability)
2. [Core idea: what is an immutable object?](#2-core-idea-what-is-an-immutable-object)
3. [Why immutability helps (the advantages)](#3-why-immutability-helps-the-advantages)
4. [Immutable classes you already use](#4-immutable-classes-you-already-use)
5. [The 4 rules and the reason for each](#5-the-4-rules-and-the-reason-for-each)
6. [Building it step by step (version 1 to version 5)](#6-building-it-step-by-step-version-1-to-version-5)
7. [The hidden problem: shallow copy and leaking references](#7-the-hidden-problem-shallow-copy-and-leaking-references)
8. [The solution: defensive copying](#8-the-solution-defensive-copying)
9. [The best solution: immutable inner objects](#9-the-best-solution-immutable-inner-objects)
10. [Special cases: collections, arrays, dates](#10-special-cases-collections-arrays-dates)
11. [Thread safety explained](#11-thread-safety-explained)
12. [Modifying an immutable object: "with" methods and builders](#12-modifying-an-immutable-object-with-methods-and-builders)
13. [Records and immutability](#13-records-and-immutability)
14. [Disadvantages and trade-offs](#14-disadvantages-and-trade-offs)
15. [Extra points and gotchas](#15-extra-points-and-gotchas)
16. [Interview questions with answers](#16-interview-questions-with-answers)
17. [Predict the output (quiz)](#17-predict-the-output-quiz)
18. [Checklist and cheat sheet](#18-checklist-and-cheat-sheet)

---

## 1. The issue: why do we need immutability?

### 1.1 A story first

You write a `Student` object and hand it to another part of the program (or another thread, or another developer's library). A bit later you notice the student's data has **changed by itself**:

```java
Student s = new Student(1, "Rahul", "MIT");
report.print(s);                       // some other code receives s
// ... later ...
System.out.println(s.getCollege());    // "Hacked College"?! who changed it?
```

Nobody in *your* code changed it. Some **other code that had a reference to the same object** changed it.

### 1.2 The root cause

In Java, objects are shared by **reference** (an address). Passing an object around does not copy it; everyone holds an arrow to the **same** object.

```
 your code  ----\
 library    -----+---->  [ Student object ]   anyone with an arrow can change it
 thread 2   ----/
```

So **mutable (changeable) shared objects** cause real problems:

| Problem | What goes wrong |
|---|---|
| **Unexpected changes** | Some other code changes your object and you don't know who |
| **Race conditions** | Two threads change the same object at the same time, producing garbage |
| **Security holes** | Validated data is changed after the check (e.g. a file path, a user role) |
| **Hard debugging** | The bug appears far away from the code that caused it |
| **Broken collections** | A key changed inside a `HashMap` can no longer be found |

### 1.3 The idea in one line

> **If an object can never change after it is created, none of these problems can happen.**

That is the whole motivation for **immutable classes**.

---

## 2. Core idea: what is an immutable object?

### 2.1 Definition

> An **immutable object** is an object whose **state (its data) cannot be changed after it is created.**

Once built, it keeps the same values for its **entire life**. If you want different values, you create a **new object**.

### 2.2 Real-life analogies

- **A printed photograph or a printed exam paper:** once printed, the content is fixed. To change it you print a new one.
- **A coin or banknote value:** a 10 rupee note can't turn into a 50 rupee note. You swap the note for a different one.
- **A signed contract:** changes need a **new** contract (an amendment), the old one stays as it was.
- **A birth certificate:** details are fixed at issue time.

Contrast with **mutable**: a whiteboard that anyone can erase and rewrite.

```
 MUTABLE (whiteboard)                      IMMUTABLE (printed paper)
 [ Name: Rahul ]  --erase/write-->         [ Name: Rahul ]   fixed forever
 [ Name: Priya ]                            To "change": make a NEW paper
                                            [ Name: Priya ]   (new object)
```

### 2.3 What "state cannot change" means precisely

- No field of the object can be assigned a different value after construction.
- **And** nothing reachable through those fields can be changed either (this second part is the tricky one, Section 7).

### 2.4 The classic Java example: `String`

```java
String s = "Hello";
s.concat(" World");                  // returns a NEW string, s is unchanged
System.out.println(s);               // Hello

s = s.concat(" World");              // s now points to a NEW object
System.out.println(s);               // Hello World
```

```
 BEFORE:  s ---> [ "Hello" ]
 AFTER :  s ---> [ "Hello World" ]       and  [ "Hello" ] is untouched (maybe garbage soon)
```

The object `"Hello"` never changed. Only the reference `s` was re-pointed.

---

## 3. Why immutability helps (the advantages)

### 3.1 Thread safety (the biggest one)

Race conditions need **shared mutable state**. If nothing can change, there is nothing to race over. An immutable object can be shared by any number of threads **without locks or `synchronized`** (details in Section 11).

### 3.2 Security

Imagine a class that validates a file path and then opens the file:

```java
void open(MyPath path) {
    if (!isAllowed(path.get())) throw new SecurityException();   // check
    // ... if another thread changes the path HERE ...
    openFile(path.get());                                        // use
}
```

With a mutable path, an attacker (or buggy code) can change it **between the check and the use**. With an immutable path, the checked value **is** the used value. This is why `String` is immutable: file names, URLs, database connection strings, class names, usernames and passwords are all Strings.

### 3.3 Safe sharing and caching

Because nobody can change it, **one object can be shared everywhere**, instead of making defensive copies.

- The **String pool** reuses one `"Hello"` object for every `"Hello"` literal.
- The **Integer cache** (-128 to 127) reuses the same `Integer` objects.
- Immutable objects can be **cached** and reused freely.

```java
String a = "Java";
String b = "Java";      // same pooled object, safe because it can never change
```

### 3.4 Reliable hash keys

`HashMap` and `HashSet` place an object into a bucket using `hashCode()`. If a key's data changes **after** insertion, its hash changes and the map **cannot find it** any more.

```java
// mutable key (bad)
Point p = new Point(1, 2);
map.put(p, "A");
p.setX(99);                    // hash changes
map.get(p);                    // null! the entry is lost in the wrong bucket
```

Immutable keys never change, so lookups always work. (`String` and `Integer` are the most common keys for this reason.) Immutable objects can even **cache** their hash code (`String` does).

### 3.5 Simplicity and predictability

- An immutable object is in **one state forever**, so it is easy to reason about.
- No need to ask "who might have changed this?"
- Easy to test and debug.

### 3.6 Failure atomicity

If an operation fails, the object is **not left half-modified**, because it was never modified in the first place.

### 3.7 No copying needed for safe sharing

Mutable objects require defensive copies at every boundary. Immutable ones can simply be passed around.

### 3.8 Summary

```
 Immutable objects are:  thread-safe . secure . cacheable . reliable as map keys
                         simple . freely shareable . predictable
```

---

## 4. Immutable classes you already use

| Immutable (cannot change) | Mutable (can change) |
|---|---|
| `String` | `StringBuilder`, `StringBuffer` |
| Wrappers: `Integer`, `Long`, `Double`, `Boolean`, `Character`... | Arrays (`int[]`) |
| `BigInteger`, `BigDecimal` | `ArrayList`, `HashMap`, `HashSet` |
| `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `Duration` (`java.time`) | `java.util.Date`, `Calendar` |
| `UUID`, `Optional`, `Path` | `Random`, `Scanner` |
| `List.of(...)`, `Set.of(...)`, `Map.of(...)` (Java 9+) | Most normal classes with setters |
| `enum` constants (as objects; fields can still be made mutable, so keep them final) | |
| `record` (shallowly immutable, see Section 13) | |

Note how `java.time` (immutable) replaced `java.util.Date` (mutable, error-prone).

---

## 5. The 4 rules and the reason for each

These are the rules from your lecture. Here is **what each does** and **what breaks without it**.

```
 RULE 1  final class
 RULE 2  private final fields
 RULE 3  no setters
 RULE 4  initialize everything through the constructor
 (+ RULE 5 for reference fields: never leak mutable objects in or out  -> Sections 7 and 8)
```

### Rule 1: Make the class `final`

```java
public final class Student { ... }
```

**Why:** if the class is not final, someone can **extend** it and **override its methods** to add mutability.

```java
class Student {                          // NOT final
    private final String name;
    Student(String name) { this.name = name; }
    public String getName() { return name; }
}

class EvilStudent extends Student {
    private String secretlyChanged = "Hacked";
    EvilStudent(String n) { super(n); }

    @Override
    public String getName() { return secretlyChanged; }      // behaves differently now
    public void change(String x) { secretlyChanged = x; }     // adds a setter!
}

Student s = new EvilStudent("Rahul");     // looks like a Student, but is mutable
```

Code that receives a `Student` believes it is immutable, but it might actually be an `EvilStudent`. Making the class `final` closes this door.

(Alternative: make the constructor `private` and expose static factory methods. Then nobody outside can subclass either.)

### Rule 2: Make all fields `private` and `final`

```java
private final int id;
private final String name;
```

- **`private`**: outsiders can't read or write the field directly (`s.name = "x"` is a compile error). This is encapsulation.
- **`final`**: the field can be assigned **exactly once** (in the constructor). The compiler **guarantees** no later reassignment, even by your own methods.

```java
class A {
    private String name;                 // private only
    void oops() { name = "changed"; }    // compiles: even inside the class it can change
}

class B {
    private final String name;           // private + final
    void oops() { name = "changed"; }    // COMPILE ERROR: cannot assign a value to final variable
}
```

`private` stops outsiders; `final` stops **everyone, including the class itself.**

### Rule 3: No setters (and no mutator methods)

```java
// DO NOT write this
public void setName(String name) { this.name = name; }
```

A setter is a door that lets state change. No setters means no public way to change a field. (With `final` fields a setter wouldn't even compile, but the rule also covers any method that **changes internal state in any way**, such as `addMark()` that modifies an internal list.)

If you need a different value, return a **new object** (Section 12).

### Rule 4: Initialize all fields through the constructor

```java
public Student(int id, String name, College college) {
    this.id = id;
    this.name = name;
    this.college = college;
}
```

The constructor is the **only** time the fields get their values. After the constructor finishes, the object is complete and frozen. Every `final` field **must** be assigned by the end of the constructor, or the compiler complains.

### Rule 5 (the one people forget): do not leak mutable objects

If a field holds a **reference to a mutable object** (like `College`, `List`, `Date`, array), then `private final` is **not enough**. This is the main topic of this lecture, so it gets its own sections next.

---

## 6. Building it step by step (version 1 to version 5)

We will use the same example as your lecture: a `Student` that contains a `College`.

### Version 1: the mutable class (the starting point)

```java
class College {
    private String name;
    private String city;

    public College(String name, String city) {
        this.name = name;
        this.city = city;
    }
    public String getName() { return name; }
    public String getCity() { return city; }
    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
}

class Student {
    private int id;
    private String name;
    private College college;

    public Student(int id, String name, College college) {
        this.id = id; this.name = name; this.college = college;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public College getCollege() { return college; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCollege(College college) { this.college = college; }
}
```

**Problem:** every field has a setter, so anyone holding a `Student` can change it at any time.

```java
Student s = new Student(1, "Rahul", new College("MIT", "Boston"));
s.setName("Hacker");        // changed freely
```

### Version 2: apply the 4 rules

```java
final class Student {                                   // Rule 1
    private final int id;                                // Rule 2
    private final String name;
    private final College college;

    public Student(int id, String name, College college) {   // Rule 4
        this.id = id;
        this.name = name;
        this.college = college;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public College getCollege() { return college; }
    // Rule 3: no setters
}
```

Does it look immutable? `id` is a primitive, `name` is a `String` (immutable). Those two are safe. **But `college` is a reference to a mutable object.** Is the class really immutable?

**No.** It only *looks* immutable. This is the trap of the lecture. Let's prove it.

---

## 7. The hidden problem: shallow copy and leaking references

### 7.1 What does `final` really lock?

For reference fields, `final` locks **the arrow** (the reference), **not the object it points to**.

```
 private final College college;

 student.college ======> [ College object ]      the ARROW can never be re-pointed
                          name = "MIT"           but the OBJECT's own data can still be changed
                          city = "Boston"        by anyone who has an arrow to it
```

(This matches what you learned in the `final` notes: a final reference cannot point elsewhere, but the object can still be modified.)

### 7.2 Leak 1: through the **constructor** (the outside keeps an arrow)

```java
College c = new College("MIT", "Boston");
Student s = new Student(1, "Rahul", c);       // constructor stores the SAME reference

c.setName("Hacked College");                  // outside code still holds c

System.out.println(s.getCollege().getName()); // Hacked College   (!!)
```

What happened in memory:

```
 STACK                         HEAP
+----------+                  +-------------------------+
| c  ------+----------------->|  College object         |
+----------+          +------>|   name = "Hacked..."    |   <- changed through c
| s  ------+--+       |       |   city = "Boston"       |
+----------+  |       |       +-------------------------+
              v       |
        +--------------------+
        | Student object     |
        |  id = 1            |
        |  name = "Rahul"    |
        |  college ----------+  (same arrow!)
        +--------------------+

 Two arrows (c and s.college) point to ONE College object.
 This is a SHALLOW COPY: the reference was copied, not the object.
```

The constructor did `this.college = college;`. That copies the **address**, so the student **shares** its College with the caller. The caller can still modify it through `c`.

### 7.3 Leak 2: through the **getter** (the outside gets an arrow)

```java
Student s = new Student(1, "Rahul", new College("MIT", "Boston"));

College leaked = s.getCollege();              // getter returns the internal reference
leaked.setCity("Nowhere");                    // modifies the student's internal data

System.out.println(s.getCollege().getCity()); // Nowhere   (!!)
```

```
 s.getCollege()  hands out the INTERNAL arrow.

 outside code --leaked--> [ College ]  <--college-- [ Student (internal) ]
                          city changed here -> Student's data changed
```

### 7.4 Why this breaks immutability

```
 Immutable means: state never changes.
 Student's state includes: id, name, AND the data inside its College.
 If the College data can change, the Student's state can change.
 So the Student is NOT immutable, even though all 4 rules are followed.
```

### 7.5 The two doors

There are exactly **two doors** through which a reference to a mutable internal object can escape:

```
        outside world
            |     ^
   (door 1) |     | (door 2)
   constructor    getter
   parameter      return value
            v     |
        +----------------+
        |  Student       |
        |  college -----> [ College ]  <- the mutable thing inside
        +----------------+
```

- **Door 1 (coming in):** the outside keeps its own arrow to the object passed into the constructor.
- **Door 2 (going out):** the getter gives the outside a new arrow to the internal object.

To keep the object truly immutable, **close both doors.** That is what defensive copying does.

---

## 8. The solution: defensive copying

### 8.1 The idea (first principles)

> **Never share your internal mutable object with anyone. Keep your own private copy, and only ever hand out copies.**

A **defensive copy** is a fresh, independent copy of an object, made so that outside code can never touch your internal one. It is the **deep copy** idea from the shallow vs deep copy notes: copy the object itself, not just the reference.

### 8.2 Step 1: give `College` a way to be copied (copy constructor)

```java
class College {
    private String name;
    private String city;

    public College(String name, String city) { this.name = name; this.city = city; }

    public College(College other) {                 // copy constructor
        this.name = other.name;                      // String is immutable: safe to share
        this.city = other.city;
    }
    // getters/setters as before
}
```

(The fields of `College` are Strings, which are immutable, so copying the references is already safe. If `College` itself had mutable fields, the copy constructor would copy those too: deep copy.)

### 8.3 Step 2: close door 1: copy in the constructor

```java
public Student(int id, String name, College college) {
    this.id = id;
    this.name = name;
    this.college = new College(college);       // OWN copy, not the caller's object
}
```

```
 BEFORE (shared):                        AFTER (defensive copy):
 c ------> [College]                     c ------> [College @A]       (caller's, independent)
 s.college -^                            s.college --> [College @B]   (Student's own copy)
```

Now:

```java
College c = new College("MIT", "Boston");
Student s = new Student(1, "Rahul", c);
c.setName("Hacked College");                   // changes ONLY the caller's object @A
System.out.println(s.getCollege().getName());  // MIT   (Student's copy is safe)
```

### 8.4 Step 3: close door 2: copy in the getter

```java
public College getCollege() {
    return new College(this.college);          // hand out a COPY, never the internal object
}
```

```
 s.getCollege() returns a copy @C

 outside --leaked--> [College @C]            [Student.college @B]  (untouched)
```

Now:

```java
College leaked = s.getCollege();
leaked.setCity("Nowhere");                     // changes only the copy @C
System.out.println(s.getCollege().getCity());  // Boston   (internal data is safe)
```

### 8.5 The final immutable `Student`

```java
final class Student {                                         // 1. final class
    private final int id;                                      // 2. private final fields
    private final String name;
    private final College college;

    public Student(int id, String name, College college) {    // 4. constructor initialization
        this.id = id;
        this.name = name;
        this.college = new College(college);                   // defensive copy IN
    }

    public int getId() { return id; }
    public String getName() { return name; }                   // String: immutable, safe to return

    public College getCollege() {
        return new College(college);                           // defensive copy OUT
    }
    // 3. no setters
}
```

### 8.6 The two-door picture, closed

```
        outside world
            |     ^
   copy IN  |     | copy OUT
 (constructor)    (getter)
            v     |
        +--------------------+
        |  Student           |
        |  college -----> [ private College copy ]   nobody outside can ever reach this
        +--------------------+
```

### 8.7 Verify immutability with a test

```java
College c = new College("MIT", "Boston");
Student s = new Student(1, "Rahul", c);

c.setName("X");                         // attack via constructor argument
s.getCollege().setCity("Y");            // attack via getter (changes a throwaway copy)

System.out.println(s.getCollege().getName());   // MIT
System.out.println(s.getCollege().getCity());   // Boston
```

Both attacks fail. The student is now truly immutable.

### 8.8 Shallow copy vs deep copy: how it applies here

```
 SHALLOW (what we had):  this.college = college;          copies the ARROW
 DEEP (the fix):         this.college = new College(college);   copies the OBJECT
```

If `College` itself held another mutable object (say an `Address`), the **copy constructor of College would have to copy the Address too**, all the way down the chain. A copy is only safe if **every mutable level** is copied.

### 8.9 Order matters: copy first, then validate

If your constructor validates input, validate the **copy**, not the original. Otherwise another thread (or a malicious subclass object) could change the original between your check and your copy.

```java
public Period(Date start, Date end) {
    this.start = new Date(start.getTime());      // 1. copy first
    this.end   = new Date(end.getTime());
    if (this.start.after(this.end))              // 2. then validate the COPIES
        throw new IllegalArgumentException("start after end");
}
```

(Time-of-check to time-of-use problem: the "security" advantage of immutability from Section 3.2.)

### 8.10 Don't use `clone()` on parameters of non-final types

A subclass of `College` could override `clone()` to return something malicious. Using a copy constructor or a factory to build a **real `College`** is safer than `college.clone()`.

---

## 9. The best solution: immutable inner objects

Defensive copying works, but it costs a new object on **every** constructor call and **every** getter call. There is a better way.

> **If the inner object is itself immutable, you don't need to copy it at all.**

```java
final class College {                      // immutable too
    private final String name;
    private final String city;

    public College(String name, String city) {
        this.name = name;
        this.city = city;
    }
    public String getName() { return name; }
    public String getCity() { return city; }
    // no setters
}

final class Student {
    private final int id;
    private final String name;
    private final College college;         // immutable type

    public Student(int id, String name, College college) {
        this.id = id;
        this.name = name;
        this.college = college;            // safe to share directly: College can never change
    }
    public College getCollege() { return college; }    // safe to return directly
    // ...
}
```

```
 s1.college ----\
                 +--> [ College (immutable) ]    sharing is harmless
 s2.college ----/
```

**Rule of thumb:** build **immutable all the way down.** Make small value classes immutable, then composing them is trivial, with no copies and no leaks.

| Approach | Needs copies? | Cost |
|---|---|---|
| Mutable inner object + defensive copies | Yes, in constructor **and** getter | Extra objects, extra code |
| **Immutable inner object** | **No** | Zero copying, simplest and safest |

---

## 10. Special cases: collections, arrays, dates

The same two-door rule applies to **any mutable type**: lists, maps, sets, arrays, `Date`, `StringBuilder`, your own mutable classes.

### 10.1 Lists (and other collections)

```java
final class Team {
    private final List<String> members;

    public Team(List<String> members) {
        this.members = List.copyOf(members);              // copy IN: new immutable list
    }

    public List<String> getMembers() {
        return members;                                    // safe: it's already unmodifiable
    }
}
```

What each tool does:

| Tool | What it does | Notes |
|---|---|---|
| `new ArrayList<>(list)` | Makes an independent **mutable** copy | Copy in and also copy out if you return it |
| `List.copyOf(list)` (Java 10+) | Makes an independent **immutable** copy | Throws `NullPointerException` if the list has `null` elements |
| `Collections.unmodifiableList(list)` | A **read-only view** of the same list | **Not a copy!** If the original is changed, the view changes |
| `List.of(a, b, c)` (Java 9+) | A new immutable list | No nulls allowed |

The trap with `unmodifiableList`:

```java
List<String> original = new ArrayList<>(List.of("A", "B"));
List<String> view = Collections.unmodifiableList(original);

original.add("C");
System.out.println(view);          // [A, B, C]  the "read-only" view changed!
```

So for constructors: **copy first** (`List.copyOf` or `new ArrayList<>`), and for getters you can return an unmodifiable view or the immutable copy.

Remember: copying a list copies **the list**, not its **elements**. If the elements are mutable (like `College`), you must copy each element too. With immutable elements (`String`, `Integer`, immutable classes), the shallow list copy is enough.

```java
// elements are mutable: deep copy needed
this.colleges = new ArrayList<>();
for (College c : colleges) this.colleges.add(new College(c));
```

### 10.2 Arrays

Arrays are always mutable (you can't make elements read-only), so copy in and out:

```java
final class Scores {
    private final int[] marks;

    public Scores(int[] marks) {
        this.marks = marks.clone();                 // copy in (primitive array: clone is a full copy)
    }
    public int[] getMarks() {
        return marks.clone();                       // copy out
    }
}
```

For arrays of **objects**, `clone()` is shallow (it copies the references), so copy each element too if the elements are mutable.

### 10.3 `java.util.Date` (mutable) vs `java.time` (immutable)

```java
final class Event {
    private final Date date;

    public Event(Date date) { this.date = new Date(date.getTime()); }     // copy in
    public Date getDate()   { return new Date(date.getTime()); }          // copy out
}
```

Better: avoid the problem by using `LocalDate` / `Instant` (immutable) instead of `Date`.

```java
final class Event {
    private final LocalDate date;                    // immutable: no copies needed
    public Event(LocalDate date) { this.date = date; }
    public LocalDate getDate() { return date; }
}
```

### 10.4 `StringBuilder`, `StringBuffer`

Never store them directly. Store the `String` (`sb.toString()`).

### 10.5 Maps

```java
this.settings = Map.copyOf(settings);        // immutable copy
// or
this.settings = new HashMap<>(settings);     // mutable private copy
```

### 10.6 The general recipe for any mutable field

```
 CONSTRUCTOR : this.field = copyOf(parameter)     // copy IN
 GETTER      : return copyOf(this.field)           // copy OUT   (or return an immutable/unmodifiable version)
 BEST        : use an immutable type for the field so no copies are needed
```

---

## 11. Thread safety explained

### 11.1 The race condition

A **race condition** happens when two threads read and write shared data at the same time and the result depends on the unlucky timing.

```java
class MutableCounter {
    private int count = 0;
    public void increment() { count = count + 1; }       // read, add, write (3 steps)
    public int get() { return count; }
}
```

Two threads call `increment()` at the same time:

```
 count = 5
 Thread A: reads 5 ----------------> writes 6
 Thread B:        reads 5 ----------------> writes 6
 Result: 6   (should be 7: one update was lost)
```

To prevent it with mutable objects you need **locks** (`synchronized`, `Lock`), which add complexity, risk of deadlocks, and slow things down.

### 11.2 Why immutable objects don't race

A race needs a **write**. An immutable object has **no writes after construction**, so threads only ever **read**, and reading never conflicts.

```
 mutable   : Thread 1 writes   Thread 2 reads    -> conflict possible, needs locking
 immutable : Thread 1 reads    Thread 2 reads    -> no conflict, no locking needed
```

An "update" in an immutable design creates a **new object**, and each thread works with its own consistent snapshot.

```java
final class Money {
    private final long amount;
    Money(long amount) { this.amount = amount; }
    Money add(long extra) { return new Money(amount + extra); }    // new object, old one untouched
    long getAmount() { return amount; }
}
```

### 11.3 A Java Memory Model bonus: final fields are safely published

Java guarantees that once a constructor finishes, **other threads see the correct values of the object's `final` fields**, even if the object reference was shared through a data race. (With non-final fields another thread could theoretically see default values like `0` or `null`.) That is one more reason to make every field `final`, not just `private`.

One caveat: `this` must not escape during construction (don't pass `this` to other threads or register it in a global list from inside the constructor).

### 11.4 Immutability and state that changes over time

You can still model changing state: keep a **single mutable reference** to an immutable object, such as `AtomicReference<Money>` or a `volatile` field, and replace it with a new immutable object when it changes.

---

## 12. Modifying an immutable object: "with" methods and builders

### 12.1 Return a new object

```java
final class Student {
    private final int id;
    private final String name;

    public Student(int id, String name) { this.id = id; this.name = name; }

    public Student withName(String newName) {            // "wither"
        return new Student(this.id, newName);            // new object, same id
    }
    public String getName() { return name; }
}

Student s1 = new Student(1, "Rahul");
Student s2 = s1.withName("Priya");

System.out.println(s1.getName());    // Rahul   (unchanged)
System.out.println(s2.getName());    // Priya   (new object)
```

```
 s1 ---> [ Student 1, "Rahul" ]        unchanged forever
 s2 ---> [ Student 1, "Priya" ]        a new object
```

Examples in the JDK: `String.replace`, `BigDecimal.add`, `LocalDate.plusDays`, `String.toUpperCase` all return **new** objects.

### 12.2 Builder pattern for classes with many fields

When a class has many fields (some optional), a long constructor is hard to read. Use a **mutable builder** that creates an **immutable** result:

```java
final class User {
    private final String name;
    private final int age;
    private final String email;

    private User(Builder b) {
        this.name = b.name; this.age = b.age; this.email = b.email;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEmail() { return email; }

    public static class Builder {
        private String name;
        private int age;
        private String email;

        public Builder name(String name)   { this.name = name; return this; }
        public Builder age(int age)        { this.age = age; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public User build()                { return new User(this); }
    }
}

User u = new User.Builder().name("Rahul").age(20).email("r@x.com").build();
```

The builder is mutable and temporary; the `User` it produces is immutable. (`StringBuilder` → `String` follows the same idea.)

---

## 13. Records and immutability

Java 16+ has `record`, a compact way to write immutable data carriers:

```java
record Point(int x, int y) { }

Point p = new Point(1, 2);
p.x();        // 1   (accessor, not getX)
// p.x = 5;   // ERROR: fields are private final
```

The compiler generates: private final fields, a constructor, accessors, `equals`, `hashCode`, `toString`. Records are implicitly `final` and can't extend other classes.

### 13.1 But records are only **shallowly** immutable

```java
record Team(List<String> members) { }

List<String> list = new ArrayList<>(List.of("A", "B"));
Team t = new Team(list);
list.add("C");                       // the record's list changed too!
t.members().add("D");                // and callers can modify it through the accessor
```

Same shallow copy problem as before. Fix it with a **compact constructor**:

```java
record Team(List<String> members) {
    Team {                                       // compact canonical constructor
        members = List.copyOf(members);          // immutable defensive copy
    }
}
```

Now `members` is an unmodifiable independent copy, and the accessor can return it safely.

### 13.2 Summary

| | Hand-written immutable class | Record |
|---|---|---|
| Boilerplate | A lot | Almost none |
| Immutable fields | You write `private final` | Automatic |
| Deep immutability for mutable fields | You must write defensive copies | You must still write a compact constructor |

---

## 14. Disadvantages and trade-offs

| Disadvantage | Explanation | Mitigation |
|---|---|---|
| **A new object per change** | Every "modification" allocates a new object | Fine for small objects; the JVM allocates cheaply and GC handles short-lived objects well |
| **Performance in heavy loops** | `s = s + x` in a loop of a million iterations creates a million Strings | Use `StringBuilder` (mutable) for building, then convert to String |
| **Memory churn** | More garbage for the GC | Reuse/caching, builders, or use mutable objects locally inside a method |
| **Awkward for big objects** | Copying a large structure to change one field is expensive | Persistent data structures (structural sharing), or keep large state mutable but encapsulated |
| **More code** | Defensive copies, withers, builders | Records, Lombok `@Value`, immutable inner types |
| **Not always natural** | Some entities genuinely change over time (a bank balance, a game character) | Use mutable entities for identity-based things, immutable values for value-based things |

**When to choose which:**

```
 Value-like things  (Money, Point, Date, Name, Address, coordinates)  -> make immutable
 Identity-like things that evolve (Account, Order in progress, Game)  -> may be mutable, but guard them
```

A good default: **make classes immutable unless you have a reason not to.** (A well-known guideline in Effective Java.)

---

## 15. Extra points and gotchas

1. **`final` variable is not an immutable object.** `final List<String> list` can still be `list.add(...)`. Immutability is about the **object's state**, not the variable.

2. **Reflection can break immutability.** Code with enough permissions can use `setAccessible(true)` to modify `private final` fields. Immutability protects against normal code, not against hostile reflection (modules and the Security Manager era aside). It still defends against all ordinary bugs.

3. **Serialization and `clone()`** can create new instances without running your constructor, bypassing your checks. If an immutable class is `Serializable`, add a `readResolve` or a validating `readObject`, or avoid serialization. Don't implement `Cloneable` on an immutable class (it's pointless: just share the object).

4. **Don't call overridable methods from the constructor** (another reason for `final` class or `final` methods).

5. **`this` must not escape** during construction (see 11.3).

6. **All fields needn't be `final` for effective immutability** (for example `String` caches its hash in a non-final field), but that requires great care. For your own classes, make everything `final`.

7. **Equals/hashCode:** immutable value classes should override `equals`/`hashCode` based on their fields. This is safe because the fields never change.

8. **Partial immutability** is a trap: if some part can still change, the class can't be trusted as immutable. Check **every reachable object**.

9. **Immutable ≠ same value forever for every observer?** An immutable object's state is fixed. Whether the *thing it represents* changes is modeled by creating new objects.

10. **Unmodifiable vs immutable:** `Collections.unmodifiableList(x)` blocks changes through the wrapper only. A truly immutable list (`List.copyOf`) has no other path to change it.

---

## 16. Interview questions with answers

**Q1. What is an immutable class?**
A class whose objects cannot be changed after creation. Examples: `String`, wrappers, `LocalDate`.

**Q2. How do you create an immutable class in Java?**
Make the class `final`; make all fields `private final`; provide no setters or state-changing methods; initialize all fields in the constructor; and defensively copy any mutable objects on the way in and out.

**Q3. Why should the class be `final`?**
Otherwise a subclass can override methods or add setters/mutable state, breaking immutability for code that holds a reference of the parent type.

**Q4. Why `private final` and not just `private`?**
`private` blocks outside access; `final` blocks reassignment by anyone, including the class's own methods, and the compiler enforces it.

**Q5. If all fields are `private final`, is the class immutable?**
Not necessarily. If a field refers to a mutable object (list, array, `Date`, custom class), that object can still be changed through a leaked reference.

**Q6. What is a shallow copy problem in immutable classes?**
Assigning `this.college = college` copies only the reference, so the caller and the object share one mutable `College`. Changes by the caller show up inside the "immutable" object.

**Q7. What is a defensive copy?**
A private, independent copy of a mutable object made when receiving it (constructor) and when returning it (getter), so that outside code never holds a reference to the internal object.

**Q8. Where must defensive copies be made?**
In **constructors/setters-like entry points** (copy what you receive) and in **getters** (copy what you return).

**Q9. Is `Collections.unmodifiableList()` enough for immutability?**
No. It is a read-only **view**; changes to the original list are visible through it. Copy first (`List.copyOf` or `new ArrayList<>(...)`), then wrap or return that.

**Q10. How do you make an immutable class that has a `List` field?**
Copy the list in the constructor with `List.copyOf(...)` (or `Collections.unmodifiableList(new ArrayList<>(...))`), and return the unmodifiable list from the getter. If the elements are mutable, copy them too.

**Q11. Why is `String` immutable?**
String pool (sharing and memory saving), cached hash code, security (paths, URLs, connection strings can't be altered after validation), thread safety, and reliability as a `HashMap` key.

**Q12. What are the advantages of immutable objects?**
Thread safety without synchronization, security, safe sharing and caching, reliable hash keys, simpler reasoning and failure atomicity.

**Q13. What are the disadvantages?**
A new object for every change (memory/GC churn), possible performance cost in heavy loops, and extra code for copies and builders.

**Q14. How do you "change" an immutable object?**
Create a new object with the new value (a "wither" method like `withName`), or use a builder. Example: `s = s.concat("x")`.

**Q15. Is a `final` reference variable the same as an immutable object?**
No. `final` fixes the reference only; the object it points to can still change unless that object is immutable.

**Q16. Are records immutable?**
Their fields are `private final`, so shallowly yes. If a record component holds a mutable object (like a `List`), you must copy it in a compact constructor.

**Q17. Why are immutable objects good HashMap keys?**
Their `hashCode` can never change, so the entry stays in the correct bucket and `get` always finds it.

**Q18. Are immutable objects thread-safe? Why?**
Yes. Thread-unsafety comes from concurrent writes to shared state. With no writes after construction (and `final` fields safely published), threads only read.

**Q19. Can reflection break immutability?**
Yes, with `setAccessible(true)` code can modify `private final` fields. Immutability defends against normal code and bugs, not intentionally hostile reflection.

**Q20. How is `StringBuilder` related to the immutability idea?**
It is the mutable companion of `String`: used to build a value efficiently (avoiding many intermediate Strings), then `toString()` yields the immutable result. Builders for your own immutable classes follow the same pattern.

**Q21. Why copy first and validate afterwards?**
To avoid a time-of-check/time-of-use attack where the original is modified between the check and the copy. Validate the **copy**.

**Q22. Why not use `clone()` for defensive copies?**
`clone()` on a non-final type can be overridden by a subclass to return something malicious. Use copy constructors or factories that build a real object of the known type.

**Q23. Do you need defensive copies if the inner objects are immutable?**
No. Immutable objects can be shared freely. That's why immutable-all-the-way-down design is best.

**Q24. Immutable vs unmodifiable?**
Unmodifiable: you can't change it **through this reference**, but someone else may change the underlying data. Immutable: no one can change it at all.

**Q25. Can an immutable class have a method that returns a different value over time?**
Not if it depends on mutable state. Methods of an immutable object should depend only on its fixed fields (and parameters).

**Q26. Give three JDK immutable classes and three mutable ones.**
Immutable: `String`, `Integer`, `LocalDate`. Mutable: `ArrayList`, `StringBuilder`, `java.util.Date`.

**Q27. What is "effectively immutable"?**
An object that is not declared with all the immutability rules but is never modified after being safely published in practice. It works but relies on discipline; true immutability is enforced by the compiler.

---

## 17. Predict the output (quiz)

**Q1**
```java
final class Person {
    private final String name;
    private final List<String> hobbies;
    Person(String name, List<String> hobbies) { this.name = name; this.hobbies = hobbies; }
    List<String> getHobbies() { return hobbies; }
}

List<String> list = new ArrayList<>(List.of("Chess"));
Person p = new Person("Rahul", list);
list.add("Music");
System.out.println(p.getHobbies());
```
Answer: `[Chess, Music]`. The constructor stored the same list (shallow copy), so the "immutable" Person changed.

**Q2** Fix Q1. Answer: in the constructor `this.hobbies = List.copyOf(hobbies);` (and the getter can return it directly).

**Q3**
```java
List<String> original = new ArrayList<>(List.of("A"));
List<String> view = Collections.unmodifiableList(original);
original.add("B");
System.out.println(view);
```
Answer: `[A, B]`. The unmodifiable list is a view, not a copy.

**Q4**
```java
String s = "Hi";
s.concat(" there");
System.out.println(s);
```
Answer: `Hi`. The result of `concat` was discarded; `String` is immutable.

**Q5**
```java
final StringBuilder sb = new StringBuilder("A");
sb.append("B");
System.out.println(sb);
```
Answer: `AB`. `final` locks the reference only; `StringBuilder` is mutable.

**Q6**
```java
record Team(List<String> members) { }
List<String> l = new ArrayList<>(List.of("X"));
Team t = new Team(l);
t.members().add("Y");
System.out.println(l);
```
Answer: `[X, Y]`. Records are only shallowly immutable and the list is shared.

**Q7**
```java
int[] data = {1, 2, 3};
Scores sc = new Scores(data);       // constructor does marks.clone()
data[0] = 99;
System.out.println(sc.getMarks()[0]);
```
Answer: `1`. The constructor copied the array.

**Q8**
```java
final class Money {
    private final long amount;
    Money(long a) { amount = a; }
    Money add(long x) { return new Money(amount + x); }
    long get() { return amount; }
}
Money m1 = new Money(100);
Money m2 = m1.add(50);
System.out.println(m1.get() + " " + m2.get());
```
Answer: `100 150`. `add` returns a new object; `m1` is untouched.

---

## 18. Checklist and cheat sheet

### Immutable class checklist

```
[ ] class is final (or constructor is private with factory methods)
[ ] all fields are private final
[ ] no setters, no method that changes state
[ ] all fields set in the constructor
[ ] mutable field received in constructor?  -> copy it (copy first, then validate)
[ ] mutable field returned from a getter?   -> return a copy / unmodifiable / immutable version
[ ] better: use immutable types for fields (String, LocalDate, List.copyOf, your own immutable classes)
[ ] equals/hashCode/toString based on fields
[ ] no overridable method calls in the constructor; 'this' doesn't escape
```

### The whole lecture in one picture

```
  ISSUE     shared mutable objects change unexpectedly (bugs, races, security holes)
     |
  IDEA      objects that never change after creation = IMMUTABLE
     |
  RULES     final class . private final fields . no setters . init via constructor
     |
  PROBLEM   fields that point to MUTABLE objects still leak (shallow copy)
            door 1: constructor keeps caller's reference
            door 2: getter hands out the internal reference
     |
  SOLUTION  defensive (deep) copy IN the constructor and OUT in the getter
     |
  BEST      make the inner objects immutable too, so no copies are needed
```

### Quick reference table

| Field type | Safe to store and return directly? | What to do |
|---|---|---|
| Primitive (`int`, `double`) | Yes | Nothing |
| `String`, wrappers, `LocalDate`, `BigDecimal` | Yes (immutable) | Nothing |
| Your own **immutable** class | Yes | Nothing |
| Your own **mutable** class | **No** | Copy in constructor and getter |
| `List`, `Set`, `Map` | **No** | `List.copyOf` / `Set.copyOf` / `Map.copyOf` in constructor; return the immutable copy |
| Array | **No** | `clone()` in constructor and getter (deep copy elements if mutable) |
| `java.util.Date` | **No** | `new Date(d.getTime())`, or switch to `java.time` |
| `StringBuilder` | **No** | Store a `String` instead |

### Common mistakes

| Mistake | Fix |
|---|---|
| Thinking `private final` fields alone make a class immutable | Check every field's type: mutable references need copies |
| Copying only in the constructor, not in the getter (or vice versa) | Close **both** doors |
| Returning `Collections.unmodifiableList(internalList)` where `internalList` was shared with the caller | Copy first in the constructor |
| Leaving the class non-final | Make it `final` |
| Using `clone()` on parameters of non-final types | Use copy constructors |
| Validating before copying | Copy first, then validate the copy |
| Adding a "convenience" setter later | Return a new object with a `withX` method instead |
| Using `Date` or arrays as fields without copies | Copy, or use `java.time` / `List.copyOf` |
| Treating records as deeply immutable | Add a compact constructor with defensive copies |
| Mistaking `final` variable for immutable object | `final` fixes the reference; immutability concerns the object's state |

### One-line takeaways

1. **Immutable = state never changes after construction.** Change means **new object**.
2. Benefits: **thread-safe, secure, cacheable, reliable keys, simple**.
3. Four rules: **final class, private final fields, no setters, constructor initialization**.
4. **`final` locks the arrow, not the object**, so mutable fields still leak.
5. Two doors: **constructor in, getter out**. Close both with **defensive copies**.
6. **Best fix:** make inner types immutable, so nothing needs copying.
7. Prefer **`List.copyOf`, `java.time`, records with compact constructors, and builders**.

---
*Practice tip: write `Student` and `College` exactly as in this file, then write the two "attack" lines (`c.setName(...)` and `s.getCollege().setCity(...)`) before and after adding the defensive copies. Watching the attack succeed and then fail is the best way to feel why each copy is needed.*
