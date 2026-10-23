# Polymorphism Leftovers & What Really Happens in Memory
### `static` / `private` / `final` / Fields · `Parent p = new Child()` in the Heap · What Inheritance Actually Transfers

> **Order of this file (read in sequence):**
> 1. The 4 rules from your Demo3.java screenshot, with code and output
> 2. A tiny "memory map" of the JVM (so the diagrams make sense)
> 3. What exactly happens at `Parent p = new Child();` (step by step)
> 4. What the object contains and what the reference can see
> 5. How a method call finds the right code (the method table)
> 6. Why fields are not polymorphic (proved with memory)
> 7. What exactly is transferred in inheritance (member by member)
> 8. Full worked example, quiz, interview questions, cheat sheet

---

## Table of Contents

1. [The four rules from the screenshot](#1-the-four-rules-from-the-screenshot)
2. [JVM memory map in one picture](#2-jvm-memory-map-in-one-picture)
3. [What happens at `Parent p = new Child()`](#3-what-happens-at-parent-p--new-child)
4. [Inside the object: data and methods](#4-inside-the-object-data-and-methods)
5. [Reference type vs object type (the two "lenses")](#5-reference-type-vs-object-type-the-two-lenses)
6. [How a method call finds the right code](#6-how-a-method-call-finds-the-right-code)
7. [Why fields are not polymorphic](#7-why-fields-are-not-polymorphic)
8. [What exactly is transferred in inheritance](#8-what-exactly-is-transferred-in-inheritance)
9. [Full worked example](#9-full-worked-example)
10. [Predict the output (quiz)](#10-predict-the-output-quiz)
11. [Interview questions with answers](#11-interview-questions-with-answers)
12. [Cheat sheet and common mistakes](#12-cheat-sheet-and-common-mistakes)

---

## 1. The four rules from the screenshot

Your Demo3.java comments say:

```java
// Static  --> they belong to class and not to objects
// private methods can't be overridden
// final methods cannot be overridden
// Fields / Variables they cannot be polymorphic
```

Why these four? Because **runtime polymorphism (dynamic dispatch) works only when all three of these are true:**

```
 (1) the method belongs to an OBJECT      (not to the class)    -> breaks for static
 (2) the child can SEE the parent method  (it is inherited)     -> breaks for private
 (3) the child is ALLOWED to replace it   (not locked)          -> breaks for final
 and it must be a METHOD (code), not DATA                        -> breaks for fields
```

We will use the same class names as the screenshot (`A` as parent).

### 1.1 Rule 1: `static` belongs to the class, not to objects

```java
class A {
    static void fun() {
        System.out.println("A.fun");
    }
}

class B extends A {
    static void fun() {                // looks like overriding, but it is HIDING
        System.out.println("B.fun");
    }
}

public class Main {
    public static void main(String[] args) {
        A obj = new B();
        obj.fun();          // A.fun   (decided by the REFERENCE type A)
        B.fun();            // B.fun
        A.fun();            // A.fun
    }
}
```

**Why?** Dynamic dispatch means "look at the object and pick its method". A static method does not work **on an object**; it belongs to the **class**. The compiler therefore converts `obj.fun()` into `A.fun()` right at compile time, using the declared type `A`. The object is never even consulted.

```
 obj.fun()   --compiler rewrites-->   A.fun()
 (the object is not used at all)
```

Proof that no object is needed:

```java
A obj = null;
obj.fun();          // works! prints A.fun (no NullPointerException)
```

It works because the call is really `A.fun()`. (Do not write code like this; use `A.fun()`.)

**This is called method hiding**, not overriding. `@Override` on `B.fun()` is a compile error.

### 1.2 Rule 2: `private` methods cannot be overridden

```java
class A {
    private void fun2() {
        System.out.println("A.fun2");
    }
    void callFun2() {
        fun2();                        // inside A, so allowed
    }
}

class B extends A {
    void fun2() {                      // a NEW method, not an override
        System.out.println("B.fun2");
    }
}

B b = new B();
b.callFun2();      // A.fun2   (A's callFun2 calls A's private fun2)
b.fun2();          // B.fun2   (B's own, unrelated method)
```

**Why?** Overriding means "replace the **inherited** method". A private method is **not inherited** (B cannot even see it), so there is nothing to replace. `B.fun2()` is just a new method that happens to have the same name. Writing `@Override` on it is a compile error.

```
 A's world:   callFun2() -> fun2()  (private, belongs to A)       sealed inside A
 B's world:   fun2()                 (a different method)          no relation
```

### 1.3 Rule 3: `final` methods cannot be overridden

```java
class A {
    final void fun3() {
        System.out.println("A.fun3");
    }
}

class B extends A {
    void fun3() { }       // COMPILE ERROR: fun3() in B cannot override fun3() in A; overridden method is final
}
```

The method **is inherited** (B objects can call `fun3()`), but it is **locked**. Use it when a step must never change (security checks, a fixed step in a template).

```java
new B().fun3();    // A.fun3 works fine: inherited, just not replaceable
```

### 1.4 Rule 4: fields (variables) are not polymorphic

```java
class A { String name = "A-field"; }
class B extends A { String name = "B-field"; }

A obj = new B();
System.out.println(obj.name);          // A-field   (reference type decides)
System.out.println(((B) obj).name);    // B-field
```

Only **methods** get dynamic dispatch. Fields are looked up using the **reference type**. Section 7 shows why with a memory diagram.

### 1.5 The four rules in one table

| Member | Looked up using | Polymorphic? | If child declares same name |
|---|---|---|---|
| Instance method (normal) | **Actual object** (runtime) | **Yes** | Overrides |
| `static` method | Reference type (compile time) | No | **Hides** |
| `private` method | Not inherited | No | New unrelated method |
| `final` method | Locked | No | **Compile error** |
| Instance field | Reference type (compile time) | No | **Hides** (both exist in memory) |
| `static` field | Reference type (compile time) | No | **Hides** |

> Also: constructors are never overridden or inherited, and `super.method()` always calls the parent's version directly (static binding).

---

## 2. JVM memory map in one picture

To understand the heap diagrams, you need to know **where each thing lives**.

```
+--------------------------------------------------------------------------+
|                               JVM MEMORY                                 |
|                                                                          |
|  STACK (one per thread)          HEAP (shared)         METASPACE         |
|  +--------------------+     +--------------------+   +-----------------+ |
|  | local variables    |     | OBJECTS            |   | CLASS INFO      | |
|  | references (addr)  |---->|  header            |   |  per class:     | |
|  | method call frames |     |  instance fields   |   |  - name, parent | |
|  +--------------------+     |  (data only)       |   |  - method code  | |
|                             +--------------------+   |  - method table | |
|                             | Class objects      |   |    (vtable)     | |
|                             | (hold static fields|   |  - constant pool| |
|                             |  in modern JVMs)   |   +-----------------+ |
|                             +--------------------+                       |
+--------------------------------------------------------------------------+
```

The most important fact for this whole file:

> **An object on the heap stores only DATA (fields) plus a small header. It does NOT store method code.**
> Method code is stored **once per class** in class metadata (Metaspace). Every object just has a **pointer to its class**.

| Thing | Where it lives |
|---|---|
| Local variables and references (`p`) | **Stack** |
| Object (header + instance fields) | **Heap** |
| Method code (bytecode) | **Class metadata (Metaspace)**, one copy per class |
| Method table (vtable) | Class metadata, one per class |
| Static fields | Stored once per class (in the class's runtime representation, the `Class` object on the heap in modern JVMs) |
| Static method code | Class metadata |

---

## 3. What happens at `Parent p = new Child()`

We use this running example for the rest of the file:

```java
class Animal {
    String name = "Animal";          // instance field
    protected int age = 5;           // instance field
    private int secret = 42;         // instance field (private)
    static int count = 0;            // static field

    void eat()   { System.out.println("Animal eats"); }
    void sound() { System.out.println("Animal sound"); }
    private void hidden() { System.out.println("Animal hidden"); }
    static void info()    { System.out.println("Animal info"); }
    final void breathe()  { System.out.println("Animal breathes"); }
}

class Dog extends Animal {
    String name = "Dog";             // hides Animal's name
    String breed = "Labrador";       // new field

    @Override void sound() { System.out.println("Dog barks"); }   // override
    void fetch()           { System.out.println("Dog fetches"); } // new method
    static void info()     { System.out.println("Dog info"); }    // hides static
}
```

Now:

```java
Animal p = new Dog();
```

This one line is really **two separate things**:

```
 Animal p;          // (A) create a reference variable of type Animal  (STACK, compile-time type)
 p = new Dog();     // (B) create a Dog OBJECT                          (HEAP, runtime type)
```

### 3.1 Step-by-step timeline of `new Dog()`

```
 STEP 1  Class loading (only the first time)
         JVM loads Animal, then Dog -> class metadata goes to Metaspace
         (method code + method tables + static fields prepared)

 STEP 2  Allocate memory ONCE for the WHOLE object
         size = header + ALL instance fields of Dog AND its parents
         (Animal's fields + Dog's fields in ONE block)

 STEP 3  Zero everything (default values: 0, null, false)

 STEP 4  Set the object header (class pointer -> Dog's class metadata)

 STEP 5  Run constructors TOP-DOWN:  Object() -> Animal() -> Dog()
         (field initializers run as part of each constructor)

 STEP 6  Return the object's ADDRESS; store it in the stack variable p
```

**Very important misconception to kill:**

> There is **no separate "parent object"** created when you create a child. There is **ONE object** (a `Dog`) that contains the Animal part inside it. The parent constructor just initializes the Animal part of that same block.

```
 WRONG picture                          RIGHT picture
 [Animal object] <--- [Dog object]      [ Dog object                    ]
 (two objects)                          [   Animal part | Dog part      ]
                                        (ONE object, one address)
```

### 3.2 The memory picture right after `Animal p = new Dog();`

```
 STACK                        HEAP                                   METASPACE
+-----------+        +--------------------------------+       +---------------------------+
| p = 0x7A  |------->| Dog OBJECT @0x7A               |       | Dog class info            |
+-----------+        |--------------------------------|       |  parent: Animal           |
 (reference,         | HEADER                         |       |  vtable:                  |
  type Animal)       |   mark word (hash, GC, lock)   |       |   eat()    -> Animal.eat  |
                     |   class pointer ---------------+------>|   sound()  -> Dog.sound   |
                     |--------------------------------|       |   breathe()-> Animal.br.. |
                     | ANIMAL PART (inherited data)   |       |   fetch()  -> Dog.fetch   |
                     |   age     = 5                  |       +---------------------------+
                     |   secret  = 42   (private)     |       | Animal class info         |
                     |   name    = "Animal"           |       |  parent: Object           |
                     |--------------------------------|       |  vtable:                  |
                     | DOG PART (own data)            |       |   eat()    -> Animal.eat  |
                     |   breed   = "Labrador"         |       |   sound()  -> Animal.sound|
                     |   name    = "Dog"              |       +---------------------------+
                     +--------------------------------+
```

(Exact field order inside the object can vary by JVM; the grouping "parent's fields first, then child's fields" is the typical layout.)

The reference `p` on the stack holds **only an address**. The compiler remembers that `p` was declared as `Animal`, but at runtime the reference does not carry that type. The **object itself** knows its real class through the **class pointer in its header**.

---

## 4. Inside the object: data and methods

Now the exact question: *"what does it point to in the heap, what methods and data does it have?"*

### 4.1 DATA: what the heap object really contains

For `new Dog()` the heap object contains **every non-static field of every class in the chain**:

| Field | Declared in | Present in the Dog object? | Accessible from outside code? |
|---|---|---|---|
| `age` | Animal (protected) | **Yes** | Subclasses and same package |
| `secret` | Animal (private) | **Yes** (it is in memory!) | **No**, only Animal's own code |
| `name` (Animal's) | Animal | **Yes** | Via Animal-typed reference |
| `breed` | Dog | **Yes** | Via Dog-typed reference |
| `name` (Dog's) | Dog | **Yes** (second slot, hides the first) | Via Dog-typed reference |
| `count` | Animal (static) | **No**: lives with the class, not in objects | `Animal.count` |

Key points:

- **Private parent fields are physically inside the child object.** They are just not nameable from child code. (That is why `getSecret()` in the parent works on a Dog.)
- **Static fields are not inside any object.**
- **Shadowed fields (same name) both exist.** There are two `name` slots in a Dog object.

Memory size (using the rule from the object-size notes, typical 64-bit JVM, compressed pointers):

```
 header (12) + age (4) + secret (4) + Animal.name ref (4) + breed ref (4) + Dog.name ref (4)
 = 32 bytes  (already a multiple of 8, no padding needed)
```

(`Animal` alone would be 12 + 4 + 4 + 4 = 24 bytes. A `Dog` is bigger because it carries everything of Animal plus its own fields.)

### 4.2 METHODS: where they actually are

The object holds **no method code**. Its class pointer leads to the class metadata, whose **method table** says which code to run for each method name.

What methods can a `Dog` object perform?

| Method | Defined in | Dog's version | Available to a Dog object? |
|---|---|---|---|
| `eat()` | Animal | inherited as is (runs Animal's code) | Yes |
| `sound()` | Animal | **overridden** by Dog (runs Dog's code) | Yes |
| `breathe()` (final) | Animal | inherited, locked | Yes |
| `fetch()` | Dog | Dog's own | Yes |
| `hidden()` (private) | Animal | not inherited | Not callable from Dog code; Animal's own methods can still call it on this object |
| `info()` (static) | Animal / Dog | belongs to classes, not the object | `Animal.info()` / `Dog.info()` |
| `toString()`, `equals()`, `hashCode()`... | Object | inherited from `Object` | Yes |

Visually, "who owns which method" for the single Dog object:

```
 Dog OBJECT  (class pointer ----> Dog class)
                                    |
              +---------------------+-----------------------------+
              |                                                   |
        methods Dog adds / replaces                  methods inherited unchanged
        - sound()  (replaces Animal's)               - eat()      (Animal's code)
        - fetch()  (new)                             - breathe()  (Animal's, final)
                                                     - toString() etc. (Object's code)
```

### 4.3 Summary: what the heap object "has"

```
 DATA    : everything non-static from Animal + Dog (+ Object's header)
 METHODS : none inside the object; it points to Dog's class, whose table lists
           Dog's methods + inherited Animal methods + Object methods
 TYPE    : the real class (Dog) is recorded in the header class pointer
```

---

## 5. Reference type vs object type (the two "lenses")

```java
Animal p = new Dog();
```

- **Reference type** (`Animal`, left side): known at **compile time**. Decides **what you are ALLOWED to access**.
- **Object type** (`Dog`, right side): known at **runtime**. Decides **which overridden method RUNS** and what the object really is.

Think of the reference as a **window with a label**. It only lets you look at the parts that its label knows about. The object behind the window is the full Dog.

```
 Animal p  = new Dog();         Dog d = (Dog) p;           Object o = p;

 window sees only Animal's      window sees all of Dog     window sees only Object's
 members:                       (sound, fetch, breed ...)  members:
   name(Animal's), age,                                      toString, equals,
   eat(), sound(), breathe()                                 hashCode, getClass
        |                              |                           |
        +------------------------------+---------------------------+
                                       |
                              SAME ONE Dog OBJECT on the heap
```

### 5.1 What you can do through each reference

```java
Animal p = new Dog();

p.eat();          // OK  : Animal has eat()
p.sound();        // OK  : Animal has sound(); runs Dog's version at runtime
p.age;            // OK  (protected, same package here)
p.breed;          // ERROR: Animal has no field breed (window can't see it)
p.fetch();        // ERROR: Animal has no method fetch()

Dog d = (Dog) p;  // cast: same object, wider window
d.fetch();        // OK
d.breed;          // OK
```

### 5.2 A cast does NOT change the object

```java
Animal p = new Dog();
Dog d = (Dog) p;
System.out.println(p == d);            // true: same address, same object
System.out.println(p.getClass());      // class Dog  (the object never changed)
```

A cast only changes **which window (type) you use** to look at the object. It does not copy or convert the object. If the real object is not a `Dog`, the cast fails at runtime with `ClassCastException`.

```java
Animal a = new Animal();
Dog d = (Dog) a;      // ClassCastException: the object in the heap is only an Animal
```

### 5.3 The two checks, side by side

```
                 COMPILE TIME                         RUN TIME
                 (reference type)                     (object type)
 p.sound()  :    "Does Animal have sound()?" yes      "Object is Dog -> run Dog.sound()"
 p.fetch()  :    "Does Animal have fetch()?" NO ->    never gets to run
                 compile error
 p.name     :    "Animal.name" chosen NOW             (fixed already; no runtime lookup)
```

---

## 6. How a method call finds the right code

### 6.1 The method table (vtable)

Each class has a **method table**. A child's table is built like this:

1. **Copy** the parent's table.
2. **Replace** the entries of methods the child overrides.
3. **Append** the child's new methods.

```
 Animal vtable                      Dog vtable (built from Animal's)
 slot  method     -> code           slot  method      -> code
 [0]   toString   -> Object code    [0]   toString    -> Object code     (copied)
 [1]   equals     -> Object code    [1]   equals      -> Object code     (copied)
 [2]   eat        -> Animal.eat     [2]   eat         -> Animal.eat      (copied)
 [3]   sound      -> Animal.sound   [3]   sound       -> Dog.sound       (REPLACED = override)
 [4]   breathe    -> Animal.breathe [4]   breathe     -> Animal.breathe  (copied)
                                    [5]   fetch       -> Dog.fetch       (APPENDED = new)
```

(Simplified. Real tables also have hashCode, getClass-related slots, etc.)

**This is how "inheritance" of methods actually works: nothing is copied into the object, and the child's table simply points to the parent's code where the child did not change it.**

Note: a method keeps the **same slot number** in parent and child. That is the trick that makes dynamic dispatch fast, as the next section shows.

### 6.2 What happens at `p.sound()`

```java
Animal p = new Dog();
p.sound();
```

```
 COMPILE TIME
   compiler checks: type Animal has sound()  -> ok
   compiler notes:  "call virtual method sound, slot 3"

 RUN TIME
   1. follow p (address 0x7A)               -> reach the Dog object
   2. read class pointer in the header      -> Dog class metadata
   3. open Dog's vtable, go to slot 3       -> Dog.sound
   4. run Dog.sound()                       -> prints "Dog barks"
```

```
 p ----> [Dog object | class ptr] ----> Dog class ----> vtable[3] ----> Dog.sound() code
```

Compare `p.eat()`: slot 2 in Dog's vtable points to `Animal.eat`, so Animal's code runs. The object is a Dog but Dog did not override `eat()`, so the inherited code is used.

### 6.3 Which calls are NOT dynamic (no vtable lookup)

| Call | Resolved | Why |
|---|---|---|
| `Animal.info()` / `p.info()` (static) | Compile time | No object involved; uses reference type |
| `p.hidden()` inside Animal (private) | Compile time | Private methods can't be overridden |
| `p.breathe()` (final) | Effectively compile time | Can't be replaced, so no choice to make |
| `super.sound()` | Compile time | Explicitly asks for the parent's version |
| Constructors | Compile time | Not overridable |
| Overloaded method choice | Compile time | Chosen by argument types |
| **Field access `p.name`** | **Compile time** | Fields are not polymorphic |

(For the curious: at bytecode level normal instance calls use `invokevirtual`, interface calls `invokeinterface`, static calls `invokestatic`, and constructors, `super.` calls and private methods use `invokespecial` or, in newer Java, nest-based access. You don't need this for exams.)

### 6.4 The `static` and `private` examples on the heap diagram

```java
Animal p = new Dog();
p.info();       // Animal info   -> compiler turned it into Animal.info(); heap object not used
p.sound();      // Dog barks     -> goes through the heap object's class pointer and vtable
```

```
 p.info()   :  [compiler] -----------------------------> Animal.info() code
                          (never touches the heap object)

 p.sound()  :  p ---> [Dog object] ---> Dog class ---> vtable ---> Dog.sound() code
```

---

## 7. Why fields are not polymorphic

### 7.1 The proof from memory

A Dog object contains **both** `name` fields, each at a different position (offset):

```
 Dog object @0x7A
 +---------------------------------+
 | header                          |
 | age      = 5                    |   offset 12
 | secret   = 42                   |   offset 16
 | name     = "Animal"   <-- Animal.name   offset 20
 | breed    = "Labrador"           |   offset 24
 | name     = "Dog"      <-- Dog.name      offset 28
 +---------------------------------+
```

Now:

```java
Animal p = new Dog();
System.out.println(p.name);
```

The **compiler** sees that `p` is of type `Animal`, finds the field `name` declared in `Animal`, and **hard-codes its position (offset 20)** into the bytecode. At runtime it just reads whatever is at offset 20. There is no lookup that says "but the real object is a Dog, so use Dog's name". Hence `"Animal"`.

```java
System.out.println(((Dog) p).name);     // compiler uses Dog's name -> offset 28 -> "Dog"
```

```
                     p.name  --compile time-->  read offset 20  --> "Animal"
           ((Dog) p).name    --compile time-->  read offset 28  --> "Dog"
```

### 7.2 Why Java designed it this way

- Methods are **behavior**: a child naturally wants to change behavior. A vtable makes that cheap.
- Fields are **state/layout**: changing which slot a field reads at runtime would break the layout guarantees, slow down every field access, and make parent code (which expects its own field) unreliable.

Parent code must keep working on its **own** data. Example: `Animal`'s methods that use `name` always mean **Animal's** `name`, even when running on a Dog.

```java
class Animal {
    String name = "Animal";
    void print() { System.out.println(name); }       // always Animal's name field
}
class Dog extends Animal {
    String name = "Dog";
}
new Dog().print();      // Animal   (Animal's code reads Animal's field)
```

### 7.3 The good-practice answer

If you need polymorphic behavior for data, use **methods** (getters) and override those:

```java
class Animal {
    String getName() { return "Animal"; }
}
class Dog extends Animal {
    @Override String getName() { return "Dog"; }
}
Animal p = new Dog();
System.out.println(p.getName());     // Dog  (method -> polymorphic)
```

Another good practice: **don't reuse the same field name** in parent and child (it only causes confusion). Use `private` fields and getters.

---

## 8. What exactly is transferred in inheritance

First, the key word: **"transferred" does not mean "copied".**

> A child class does **not** get a physical copy of the parent's methods.
> - **Data (fields):** every object of the child **contains slots** for the parent's instance fields.
> - **Behavior (methods):** the child's **method table points** to the parent's code unless overridden.
> - **Visibility rules** (access modifiers) decide which of these the child's **code is allowed to use by name**.

So there are two different questions for every member:

```
 Q1. Does it EXIST in the child object / child class?   (memory view)
 Q2. Can the child's code USE it by name?               (access view)
```

### 8.1 Member by member

| Member of the parent | Exists in child object/class? | Child code can use it? | Notes |
|---|---|---|---|
| `public` instance field | Yes (slot in object) | **Yes** | |
| `protected` instance field | Yes | **Yes** (even in another package) | |
| default (package-private) field | Yes | **Only if child is in same package** | |
| `private` instance field | **Yes** (still occupies memory!) | **No** (use getter/setter) | Parent's own methods still use it |
| `static` field | Not in objects; one copy with the class | Yes, via `Child.x` or `x` (if accessible) | **Shared**, not copied; changing via child changes the parent's one copy |
| `public`/`protected` instance method | Yes (entry in child's vtable) | **Yes** | Can be overridden |
| default instance method | Yes if same package | Only same package | Override only possible in same package |
| `private` method | **No** (not inherited) | No | Parent's own methods can still call it |
| `final` method | Yes | Yes (call it) | **Cannot override** |
| `static` method | Belongs to class | Callable as `Child.m()` | **Hidden**, not overridden |
| `abstract` method | Yes (as a requirement) | n/a | Child must implement (or be abstract) |
| **Constructors** | **No** | No (call via `super(...)`) | Never inherited |
| Instance initializer block | Not inherited, but **runs** on every object creation | n/a | Runs right after `super()` returns, before the rest of that class's own constructor body |
| Static initializer block | Not inherited, but **runs once** when parent is loaded | n/a | Parent static block runs before child's |
| Methods of `Object` | Yes | Yes | `toString`, `equals`, `hashCode`, ... |
| Inner (member) classes | Yes if accessible | Yes | e.g. `Outer.Inner` usable via child |
| Interface `default` methods (from implemented interfaces) | Yes | Yes | Conflicts must be resolved |
| Interface constants | Yes | Yes | `public static final` |

### 8.2 Diagram: what a child gets from the parent

```
                     PARENT: Animal
   +------------------------------------------------+
   | public  String name        -----------------+   |
   | protected int age          -----------------+   |
   | private int secret         -----------------+   |  DATA SLOTS
   | static int count   (class-level, shared)    |   |  exist in every Dog object
   |                                             |   |  (except static)
   | void eat()                 -----------------+   |
   | void sound()               -----------------+   |  METHOD ENTRIES in Dog's table
   | private void hidden()   X (not inherited)       |
   | final void breathe()       -----------------+   |
   | static void info()   (class-level)              |
   | Animal() constructor    X (never inherited)     |
   +------------------------------------------------+
                          |   extends
                          v
                     CHILD: Dog  (can add its own + override)
```

### 8.3 What the child can and cannot touch (code view)

```java
class Dog extends Animal {
    void test() {
        System.out.println(name);        // OK  (public / package)
        System.out.println(age);         // OK  (protected)
        System.out.println(secret);      // ERROR: secret has private access in Animal
        eat();                           // OK
        breathe();                       // OK
        hidden();                        // ERROR: hidden() has private access in Animal
        System.out.println(count);       // OK  (static, shared)
        System.out.println(getSecret()); // OK  if Animal has a public getSecret()
    }
}
```

### 8.4 The private data is there, even if you can't see it

```java
class Animal {
    private int secret = 42;
    public int getSecret() { return secret; }
}
class Dog extends Animal { }

Dog d = new Dog();
System.out.println(d.getSecret());   // 42
```

```
 Dog object
 +----------------------+
 | secret = 42  (private to Animal's code) <-- exists in memory
 +----------------------+
 Dog's own code cannot name it, but Animal.getSecret() running on this object can.
```

### 8.5 Static members are shared, not copied

```java
class Animal { static int count = 0; }
class Dog extends Animal { }

Dog.count = 10;
System.out.println(Animal.count);     // 10  (ONE variable, two names to reach it)
```

```
 Animal class -----> [ count = 10 ]  <----- Dog.count (just another way to reach it)
```

### 8.6 Constructors: not transferred, but chained

```
 new Dog()
   |-- Dog() starts, first line is super()  (inserted by compiler if you didn't write it)
   |      |-- Animal() starts, first line is super()
   |      |      |-- Object() runs
   |      |      '-- Animal's field initializers + body run
   |      '-- Animal() done
   '-- Dog's field initializers + body run
```

Rule: **parent part is always initialized first**, because the child part may depend on it.

### 8.7 Access modifier effect on inheritance (cross-package)

| Parent member modifier | Same package child | Different package child |
|---|---|---|
| `public` | Usable | Usable |
| `protected` | Usable | Usable (through the child type) |
| default | Usable | **Not usable** (not inherited) |
| `private` | Not usable | Not usable |

### 8.8 One-sentence answer to "what is transferred?"

> **Every object of the child contains the parent's instance data, the child class's method table includes the parent's non-private methods (pointing to the parent's code unless overridden), static members stay shared with the parent class, and constructors/private methods are not inherited.**

---

## 9. Full worked example

Everything together, with output:

```java
class Animal {
    String name = "Animal";
    protected int age = 5;
    private int secret = 42;
    static int count = 0;

    Animal() { count++; System.out.println("Animal constructor"); }

    void eat()   { System.out.println("Animal eats"); }
    void sound() { System.out.println("Animal sound"); }
    private void hidden() { System.out.println("Animal hidden"); }
    void callHidden() { hidden(); }
    static void info()   { System.out.println("Animal info"); }
    final void breathe() { System.out.println("Animal breathes"); }
    int getSecret() { return secret; }
}

class Dog extends Animal {
    String name = "Dog";
    String breed = "Labrador";

    Dog() { System.out.println("Dog constructor"); }

    @Override void sound() { System.out.println("Dog barks"); }
    void hidden() { System.out.println("Dog hidden (unrelated method)"); }
    void fetch()  { System.out.println("Dog fetches"); }
    static void info() { System.out.println("Dog info"); }
}

public class Main {
    public static void main(String[] args) {
        Animal p = new Dog();     // Animal constructor, Dog constructor

        p.eat();                  // Animal eats      (inherited, not overridden)
        p.sound();                // Dog barks        (overridden, runtime)
        p.breathe();              // Animal breathes  (final, inherited)
        p.callHidden();           // Animal hidden    (Animal's private, not overridden)
        p.info();                 // Animal info      (static, reference type)
        System.out.println(p.name);              // Animal   (field, reference type)
        System.out.println(((Dog) p).name);      // Dog
        System.out.println(p.getSecret());       // 42       (private data exists)
        System.out.println(Animal.count);        // 1        (one object created)

        // p.fetch();             // compile error: Animal has no fetch()
        ((Dog) p).fetch();        // Dog fetches
    }
}
```

Output:

```
Animal constructor
Dog constructor
Animal eats
Dog barks
Animal breathes
Animal hidden
Animal info
Animal
Dog
42
1
Dog fetches
```

Note `Animal.count` is `1`: **only one object** was created even though two constructors ran.

### The final picture of this example

```
 STACK                    HEAP                                  METASPACE
+--------+        +---------------------------+        +--------------------------------+
| p ----------->  | Dog OBJECT  (one object)  |        | Dog class                      |
+--------+        | header: class ptr --------+------> |  vtable: eat->Animal.eat       |
                  | age=5, secret=42,         |        |          sound->Dog.sound      |
                  | Animal.name="Animal"      |        |          breathe->Animal.br.   |
                  | breed="Labrador"          |        |          callHidden->Animal... |
                  | Dog.name="Dog"            |        |          fetch->Dog.fetch      |
                  +---------------------------+        |          hidden()-> Dog's own  |
                                                       |  static: info()->Dog.info      |
                  CLASS-LEVEL (shared, no object)      | Animal class                   |
                  Animal.count = 1                     |  private hidden() (Animal only)|
                                                       |  static: info()->Animal.info   |
                                                       +--------------------------------+
```

---

## 10. Predict the output (quiz)

**Q1**

```java
class A { void show() { System.out.println("A"); } }
class B extends A { void show() { System.out.println("B"); } }
A obj = new B();
obj.show();
```
Answer: `B`. Normal instance method, overridden, runtime dispatch.

**Q2**

```java
class A { static void show() { System.out.println("A"); } }
class B extends A { static void show() { System.out.println("B"); } }
A obj = new B();
obj.show();
```
Answer: `A`. Static methods are hidden and chosen by reference type.

**Q3**

```java
class A { String x = "A"; }
class B extends A { String x = "B"; }
A obj = new B();
System.out.println(obj.x);
System.out.println(((B) obj).x);
```
Answer: `A` then `B`.

**Q4**

```java
class A {
    private void show() { System.out.println("A"); }
    void call() { show(); }
}
class B extends A {
    void show() { System.out.println("B"); }
}
new B().call();
```
Answer: `A`. `call()` runs inside A, and A's private `show()` is bound there.

**Q5**

```java
class A { final void show() { System.out.println("A"); } }
class B extends A { void show() { System.out.println("B"); } }
```
Answer: Compile error. Cannot override a final method.

**Q6**

```java
class A { void f() { System.out.println("A.f"); } }
class B extends A {
    void f() { System.out.println("B.f"); }
    void g() { System.out.println("B.g"); }
}
A obj = new B();
obj.g();
```
Answer: Compile error. The `A` reference has no `g()`. The object is a B, but the compiler checks the reference type.

**Q7**

```java
A obj = new A();
B b = (B) obj;
```
Answer: Compiles, then `ClassCastException` at runtime. The heap object is only an `A`.

**Q8**

```java
class A {
    A() { show(); }
    void show() { System.out.println("A"); }
}
class B extends A {
    int x = 10;
    void show() { System.out.println("B " + x); }
}
new B();
```
Answer: `B 0`. Dynamic dispatch picks `B.show()` even inside A's constructor, but B's field `x` is not initialized yet (still default 0).

---

## 11. Interview questions with answers

**Q1. When you write `Animal p = new Dog();`, what is stored in the stack and what in the heap?**
Stack: reference `p` (an address). Heap: one `Dog` object with a header (class pointer) and the instance data of both Animal and Dog parts. Method code is in class metadata, not in the object.

**Q2. Does a parent object get created when a child object is created?**
No. Only one object (the child) is created; it contains the parent's data part. The parent constructor merely initializes that part.

**Q3. Does the child object contain the parent's private fields?**
Yes, they occupy memory in the object, but child code can't access them by name. The parent's own methods can still use them.

**Q4. Where is the method code stored? Does each object have its own copy?**
Method code is stored once per class in class metadata. Objects don't copy it; they hold a class pointer.

**Q5. What decides which method runs in `p.sound()`?**
The actual object (via the class pointer and the method table). The reference type only decides if the call is allowed at compile time.

**Q6. Why can't we override static methods?**
They belong to the class, not to an object. Dynamic dispatch needs an object, so the call is bound at compile time using the reference type (method hiding).

**Q7. Why can't we override private methods?**
They aren't inherited, so the child can't see them. A same-named method in the child is a brand-new method.

**Q8. Why can't we override final methods?**
`final` explicitly locks the method. Overriding gives a compile error.

**Q9. Why are fields not polymorphic?**
The compiler resolves a field access to a fixed position in the object using the reference type. There is no runtime lookup. Both parent and child fields with the same name exist in the object.

**Q10. What does a cast actually do?**
It changes the type through which you view the object. It does not change or copy the object, and fails with `ClassCastException` if the real object isn't of that type.

**Q11. What exactly does a child inherit?**
Non-private members (fields and methods) accessible to it, `Object` methods, and interface defaults. It does not inherit constructors or private methods; private fields exist in memory but are inaccessible. Static members are shared with the parent class.

**Q12. What is a vtable?**
A per-class table of method pointers. The child's table copies the parent's, replaces overridden entries and adds new methods. Virtual calls go through it.

**Q13. Can a parent reference call a child-only method?**
No, not without a cast. The compiler only allows what the reference type declares.

**Q14. What is the output when a parent constructor calls an overridden method?**
The child's version runs (dynamic dispatch works during construction) but child fields are not yet initialized, so you see default values.

**Q15. Are static fields stored inside objects?**
No. They exist once per class and are shared by all objects.

---

## 12. Cheat sheet and common mistakes

### The whole file in 12 lines

```
1. Object on heap = header (class pointer) + instance DATA of the whole class chain. No method code.
2. Method code + method tables live once per class in class metadata.
3. Parent reference to child object = ONE child object; reference type is only a compile-time "window".
4. Reference type decides what you can ACCESS. Object type decides which OVERRIDDEN method RUNS.
5. Method call: p -> object -> class pointer -> vtable slot -> code.
6. Overridden method = vtable entry replaced. Inherited unchanged = entry points to parent's code.
7. static  : class-level, hidden, bound at compile time by reference type.
8. private : not inherited, nothing to override.
9. final   : inherited but locked.
10. Fields : resolved at compile time by reference type; both parent and child fields exist in memory.
11. Inheritance transfers: non-private data slots + non-private method entries; NOT constructors,
    NOT private methods (private fields exist but are inaccessible); statics are shared.
12. Cast = change the window, not the object.
```

### Dynamic or not?

```
 DYNAMIC (object decides)   : normal instance methods that are overridden
 NOT DYNAMIC (reference/compile time):
     static methods, private methods, final methods, super.method(),
     constructors, overload selection, ALL fields (static and instance)
```

### Common mistakes

| Mistake | Fix |
|---|---|
| Thinking a parent object is created along with the child | Only one object exists: the child, containing the parent part |
| Thinking methods are copied into each object | Code is stored once per class; objects only hold a class pointer |
| Thinking private parent fields don't exist in the child object | They exist in memory but are not accessible by name from child code |
| Expecting `@Override` to work on `static`/`private` methods | It's a compile error; they hide/are unrelated |
| Expecting `p.name` to use the child's field | Fields use the reference type; use getters to be polymorphic |
| Calling a child-only method through a parent reference | Cast to the child type (check with `instanceof`) |
| Believing a cast converts the object | It only changes the viewing type |
| Thinking static members are copied to the child | They're shared; one variable, reachable via both class names |
| Reusing the same field name in parent and child | Avoid it; use `private` + getters |
| Calling overridable methods from constructors | Child fields aren't initialized yet; avoid |

---
*Revision trick: draw the Dog object from memory: header with class pointer, Animal's fields first, Dog's fields next. Then draw the arrow from the class pointer to the vtable and mark which entries point to Animal code and which to Dog code. If you can draw that, you understand inheritance and polymorphism at the memory level.*
