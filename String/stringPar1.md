# Java Strings: Complete Internals Guide

> Fundamentals, String Pool vs Heap, Immutability, the internal `byte[]` + `coder` layout, hash caching, and how everything looks in memory.

---

## Table of Contents

1. [What is a String?](#1-what-is-a-string)
2. [Why a String class and not a raw char array?](#2-why-a-string-class-and-not-a-raw-char-array)
3. [JVM memory map (the big picture)](#3-jvm-memory-map-the-big-picture)
4. [String Pool vs Heap](#4-string-pool-vs-heap)
5. [Literal vs `new String()`: step-by-step memory pictures](#5-literal-vs-new-string-step-by-step-memory-pictures)
6. [`==` vs `.equals()`](#6-vs-equals)
7. [Concatenation: compile-time vs runtime](#7-concatenation-compile-time-vs-runtime)
8. [`intern()`](#8-intern)
9. [Immutability](#9-immutability)
10. [Internal architecture of the `String` object](#10-internal-architecture-of-the-string-object)
11. [`byte[]` vs `char[]`: compact strings](#11-byte-vs-char-compact-strings)
12. [The `coder` field](#12-the-coder-field)
13. [Hash caching](#13-hash-caching)
14. [Full lifecycle: from source code to bytes in memory](#14-full-lifecycle-from-source-code-to-bytes-in-memory)
15. [Loops, garbage and StringBuilder](#15-loops-garbage-and-stringbuilder)
16. [Summary of optimizations](#16-summary-of-optimizations)
17. [Predict the output (practice)](#17-predict-the-output-practice)
18. [Interview questions and answers](#18-interview-questions-and-answers)
19. [Cheat sheet](#19-cheat-sheet)

---

## 1. What is a String?

A **String** is a **sequence of characters**. In Java it is an **object** (not a primitive) that wraps (abstracts) a character array and gives you ready-made operations on it.

```
Text you see:     H  e  l  l  o
                  |  |  |  |  |
Index:            0  1  2  3  4
```

- Class: `java.lang.String`
- Package: `java.lang`, which is **auto-imported**, so you never write `import java.lang.String;`
- It is a **final class** (nobody can extend it) and it is **immutable**.
- It implements `Serializable`, `Comparable<String>`, `CharSequence`.

```java
String s = "Hello";          // most common way (literal)
String t = new String("Hello"); // explicit object creation
```

---

## 2. Why a String class and not a raw char array?

In C, a string is just `char[]` ending with `'\0'`. Every operation is something *you* must code manually.

| Task | With raw `char[]` | With `String` |
|---|---|---|
| Compare two texts | write a loop comparing each char | `a.equals(b)` |
| Join two texts | allocate a bigger array, copy both | `a + b` or `a.concat(b)` |
| Get a part | allocate new array, copy a range | `s.substring(1, 4)` |
| Find a char | loop and compare | `s.indexOf('l')` |
| Upper-case | loop and convert | `s.toUpperCase()` |
| Length | track it yourself | `s.length()` |

So `String` = **a char array + a big toolbox + safety + JVM-level optimizations** (pool, compact storage, cached hash).

---

## 3. JVM memory map (the big picture)

```
+------------------------------------------------------------------+
|                           JVM PROCESS                            |
|                                                                  |
|  +-------------------+     +-----------------------------------+ |
|  |   STACK (per      |     |              HEAP                 | |
|  |   thread)         |     |                                   | |
|  |                   |     |  +-----------------------------+  | |
|  |  local variables  |     |  |  STRING POOL (String Table) |  | |
|  |  (references)     |---->|  |  special area for literals  |  | |
|  |                   |     |  +-----------------------------+  | |
|  |   s1  --------------+   |                                   | |
|  |   s2  --------------+-->|  +-----------------------------+  | |
|  |                   |     |  |  NORMAL HEAP (young / old)  |  | |
|  +-------------------+     |  |  objects created by `new`,  |  | |
|                            |  |  runtime concatenation etc. |  | |
|  +-------------------+     |  +-----------------------------+  | |
|  |  METASPACE        |     +-----------------------------------+ |
|  |  class metadata   |                                           |
|  +-------------------+                                           |
+------------------------------------------------------------------+
```

Key points:

- **Stack** holds *references* (like `s1`, `s2`), never the actual String text.
- **Heap** holds the actual `String` objects.
- **String Pool** is a *special region/table inside the heap* (since Java 7; before that it lived in PermGen).
- The pool is basically a **hash table** (the *String Table*) that maps text to a `String` object already living on the heap.

---

## 4. String Pool vs Heap

### 4.1 String Pool

- A special area designed for **reusability**.
- Holds **one single copy** of each distinct literal.
- Before creating a literal, Java checks: *"Does an identical string already exist in the pool?"*
  - **Yes** -> reuse it (just hand back the reference).
  - **No** -> create it in the pool, then return the reference.

### 4.2 Normal Heap

- Objects created with `new String(...)` live here.
- Results of **runtime** operations (e.g. `s1 + s2` with variables) live here.
- Duplicates are allowed. Two heap Strings can have identical text but be different objects.

### 4.3 Side by side

```
                       HEAP
  +-----------------------------------------------------+
  |                                                     |
  |   STRING POOL                    NORMAL HEAP AREA   |
  |  +---------------------+        +-----------------+ |
  |  | "hello"  <--------- |        | String@A1       | |
  |  | "world"             |        |   "hello"       | |
  |  | "java"              |        | String@B7       | |
  |  +---------------------+        |   "hello"       | |
  |   no duplicates                 +-----------------+ |
  |                                  duplicates allowed |
  +-----------------------------------------------------+
```

| Feature | String Pool | Normal Heap |
|---|---|---|
| Created by | literals (`"hello"`), `intern()` | `new String()`, runtime concat, `StringBuilder.toString()` |
| Duplicates | **No** (one per distinct text) | **Yes** |
| Garbage collected? | Yes, if no longer referenced (modern JVMs) | Yes |
| Purpose | Save memory and reuse | General object storage |

---

## 5. Literal vs `new String()`: step-by-step memory pictures

### 5.1 Two identical literals

```java
String s1 = "hello";
String s2 = "hello";
```

**Step 1:** `String s1 = "hello";` -> pool has no `"hello"`, so create it.

```
STACK                 HEAP
+------+              +-------------- STRING POOL --------------+
| s1   |------------->|  [ "hello" ]  (object created)         |
+------+              +----------------------------------------+
```

**Step 2:** `String s2 = "hello";` -> pool already has `"hello"`, reuse it. **No new object.**

```
STACK                 HEAP
+------+              +-------------- STRING POOL --------------+
| s1   |----+-------->|  [ "hello" ]                           |
+------+    |         +----------------------------------------+
| s2   |----+            s1 and s2 point to the SAME object
+------+
```

```java
s1 == s2        // true  (same reference)
s1.equals(s2)   // true
```

### 5.2 Literal + `new`

```java
String s1 = "hello";
String s3 = new String("hello");
```

`new` **always** creates a fresh object in the normal heap, bypassing the pool lookup for the *result*.

```
STACK                 HEAP
+------+              +-------------- STRING POOL --------------+
| s1   |------------->|  [ "hello" ]                           |
+------+              +----------------------------------------+
| s3   |-----+
+------+     |        +-------------- NORMAL HEAP -------------+
             +------->|  String object "hello"  (separate!)    |
                      +----------------------------------------+
```

```java
s1 == s3        // false (different objects)
s1.equals(s3)   // true  (same content)
```

### 5.3 How many objects does `new String("hello")` create?

- The literal `"hello"` is a compile-time constant, so it goes to the pool **if not already there** (1 object).
- `new` then creates **another** String object in the heap (1 object).

So: **up to 2 objects** (1 if `"hello"` was already in the pool). This is a very common interview question.

```
new String("hello")
        |
        |--> 1) literal "hello"  -> POOL   (only if absent)
        |
        '--> 2) new object      -> HEAP   (always)
```

*Detail:* the new heap object copies the same internal `byte[]` reference from the literal in modern JDKs (the array is shared since it is immutable), but the `String` object itself is distinct.

### 5.4 Full combined picture

```java
String a = "java";
String b = "java";
String c = new String("java");
String d = new String("java");
```

```
STACK              HEAP
+---+              +----------- STRING POOL ----------+
| a |--+---------->|   [ "java" ]                     |
+---+  |           +----------------------------------+
| b |--+
+---+              +----------- NORMAL HEAP ----------+
| c |------------->|   String@1  "java"               |
+---+              |                                  |
| d |------------->|   String@2  "java"               |
+---+              +----------------------------------+

a == b  -> true
a == c  -> false
c == d  -> false
All .equals() -> true
```

---

## 6. `==` vs `.equals()`

| | `==` | `.equals()` |
|---|---|---|
| Compares | **memory reference** (same object?) | **character content** |
| Question asked | "Are these the exact same object?" | "Do these hold the same text?" |
| Overridden in String? | n/a (operator) | Yes, String overrides it to compare content |

```
  s1 ----> [ "hello" ]  <---- s2          s1 == s2 -> true

  s1 ----> [ "hello" ]
  s3 ----> [ "hello" ]  (other object)    s1 == s3 -> false
                                          s1.equals(s3) -> true
```

**Rule:** to compare String *content*, **always use `.equals()`** (or `equalsIgnoreCase()`).

How `equals` works internally (simplified):

```java
public boolean equals(Object o) {
    if (this == o) return true;                 // quick: same reference
    if (o instanceof String other) {
        // compare coder first, then compare bytes
        return coder == other.coder && Arrays.equals(value, other.value);
    }
    return false;
}
```

---

## 7. Concatenation: compile-time vs runtime

### 7.1 Compile-time constants -> Pool

```java
String s1 = "hel" + "lo";   // both are literals
String s2 = "hello";
System.out.println(s1 == s2);  // true
```

The **compiler** folds `"hel" + "lo"` into `"hello"` while compiling. The class file already contains `"hello"`, so it is a pool literal.

```
Source:   "hel" + "lo"
             |
        (javac constant folding)
             v
Bytecode: ldc "hello"   ->  POOL
```

Same for `final` variables that are compile-time constants:

```java
final String a = "hel";
final String b = "lo";
String s3 = a + b;           // constant expression -> folded
System.out.println(s3 == "hello"); // true
```

### 7.2 Runtime concatenation -> Heap

```java
String x = "hel";        // NOT final
String y = "lo";
String s4 = x + y;       // value only known at RUNTIME
System.out.println(s4 == "hello");     // false
System.out.println(s4.equals("hello")); // true
```

The compiler cannot be sure what `x` and `y` hold when the program runs, so the joining happens at runtime and creates a **new object in the normal heap**.

```
STACK            HEAP
+----+           +------- POOL -------+
| x  |---------->| "hel"              |
| y  |---------->| "lo"               |
+----+           | "hello"  <-- (from the literal in the comparison) |
| s4 |--+        +--------------------+
+----+  |
        |        +------ NORMAL HEAP --+
        +------->| String "hello"      |   <- result of x + y
                 +---------------------+
```

> Under the hood: older JDKs used `new StringBuilder().append(x).append(y).toString()`. Since Java 9 the compiler emits an `invokedynamic` call to `StringConcatFactory`, which is faster, but the **result is still a new heap String**.

---

## 8. `intern()`

`intern()` lets you **manually push a heap string into the pool** (or fetch the pooled copy).

```java
String h = new String("hello");   // heap object
String p = h.intern();            // returns the POOL reference
System.out.println(h == p);       // false (h is heap, p is pool)
System.out.println(p == "hello"); // true
```

```
Before intern():              After h.intern():

 h ----> HEAP "hello"          h ----> HEAP "hello"      (unchanged)
                               p ----> POOL "hello"      (pool copy)
 POOL: "hello" (if literal      
 existed) or empty              If pool lacked "hello", the pool now
                                stores a reference to it.
```

Rules:
- If the pool already contains an equal string -> return the pool's reference.
- Otherwise -> add this string to the pool and return it.

Use sparingly (it can slow things down, and is mainly useful when you have many duplicate strings read from external input).

---

## 9. Immutability

### 9.1 Concept

Once a `String` object is created, **its content can never change**. Methods that look like they modify a string actually return a **new** String.

```java
String s = "hello";
s.concat(" world");      // result thrown away!
System.out.println(s);   // hello   (unchanged)

s = s.concat(" world");  // reassign the reference
System.out.println(s);   // hello world
```

```
BEFORE:   s ----> [ "hello" ]

s = s.concat(" world");

AFTER:    s ----> [ "hello world" ]   (NEW object)
                  [ "hello" ]         (old object, untouched,
                                       still in pool if literal;
                                       garbage if no references)
```

Note: **the variable `s` changed, the object did not.** Don't confuse the reference with the object.

### 9.2 How Java enforces it

```
public final class String {                 <- final class: cannot be subclassed
    private final byte[] value;             <- private + final reference
    private final byte coder;
    private int hash;
    ...                                     <- no setters, no method mutates value
}
```

- Class is `final` -> no subclass can break the rules.
- `value` is `private final` -> nobody outside can touch the array, and the reference never changes.
- No method exposes the internal array (methods like `toCharArray()` return a **copy**).

### 9.3 Why immutable? (the reasons)

1. **Safe sharing in the String Pool.** If `s1` and `s2` point to the same object and one could modify it, the other would be corrupted.

   ```
   s1 ---+
         +--> [ "hello" ]   if mutable, s1 changing it would silently change s2!
   s2 ---+
   ```

2. **Thread-safety.** Immutable objects can be shared between threads without locks.
3. **Hash caching.** The hash code can be computed once and stored, since content never changes (see section 13).
4. **Security.** Strings are used for file paths, URLs, DB credentials, class names. If someone could mutate a validated String after the check, it would be a vulnerability.
5. **Reliable as map keys.** A key whose hash changes would be lost inside a `HashMap`.

### 9.4 The efficiency issue

Because every "change" makes a new object, loops are costly:

```java
String s = "";
for (int i = 0; i < 5; i++) {
    s = s + i;     // new String every iteration
}
```

```
iteration 0:  s ----> "0"
iteration 1:  s ----> "01"        "0"     now garbage
iteration 2:  s ----> "012"       "01"    now garbage
iteration 3:  s ----> "0123"      "012"   now garbage
iteration 4:  s ----> "01234"     "0123"  now garbage
```

All the discarded intermediates sit in the heap until the **Garbage Collector** cleans them. With 100,000 iterations this is slow and memory hungry -> use `StringBuilder` (section 15).

---

## 10. Internal architecture of the `String` object

Simplified source from the JDK (Java 9+, field names as in OpenJDK):

```java
public final class String implements Serializable, Comparable<String>, CharSequence {

    private final byte[] value;   // the actual characters, stored as bytes
    private final byte   coder;   // 0 = LATIN1, 1 = UTF16
    private int          hash;    // cached hashCode (default 0)
    private boolean      hashIsZero; // (JDK 13+) marks that real hash is 0

    static final boolean COMPACT_STRINGS = true;
    static final byte LATIN1 = 0;
    static final byte UTF16  = 1;
}
```

### 10.1 What `String s = "Hi"` really looks like in memory

A String is **two objects**: the `String` object itself and a separate `byte[]` array object it points to.

```
 STACK                         HEAP (inside the String Pool region)
+-------+                   +-----------------------------------+
|   s   |------------------>|  String object                    |
+-------+                   |  +-----------------------------+  |
                            |  | object header   (~12 bytes) |  |
                            |  | value  : ref  ----------+   |  |
                            |  | coder  : 0 (LATIN1)     |   |  |
                            |  | hash   : 0 (not yet)    |   |  |
                            |  | hashIsZero : false      |   |  |
                            |  +-------------------------|---+  |
                            +----------------------------|------+
                                                         |
                                                         v
                            +-----------------------------------+
                            |  byte[] object                    |
                            |  +-----------------------------+  |
                            |  | array header (~12 bytes)    |  |
                            |  | length : 2                  |  |
                            |  | [0] = 0x48  ('H')           |  |
                            |  | [1] = 0x69  ('i')           |  |
                            |  +-----------------------------+  |
                            +-----------------------------------+
```

So a String reaches its characters through **two hops**:

```
variable s  --->  String object  --->  byte[] array  --->  bytes of characters
 (stack)           (heap/pool)          (heap)
```

### 10.2 Approximate size (64-bit JVM, compressed oops)

| Part | Approx. bytes |
|---|---|
| String object header | 12 |
| `value` reference | 4 |
| `coder` | 1 |
| `hash` | 4 |
| `hashIsZero` | 1 |
| padding to 8-byte multiple | 2 |
| **String object total** | **24** |
| `byte[]` header (12) + length (4) | 16 |
| data (e.g. 2 bytes for `"Hi"`) | 2 (+ padding to 8 -> 24 total array) |

So even a tiny `"Hi"` costs roughly 48 bytes. That is why avoiding duplicates (the pool) matters.

### 10.3 How the pool *finds* a string (the String Table)

The pool is a **hash table**. Each bucket holds references to String objects on the heap.

```
 String Table (hash table)
 +--------+
 | bucket0| -> null
 | bucket1| -> ref -> [String "java"]
 | bucket2| -> null
 | bucket3| -> ref -> [String "hello"] -> ref -> [String "world"]   (collision chain)
 | bucket4| -> null
 | ...     |
 +--------+

 Lookup of literal "hello":
   1) compute hash("hello")
   2) bucket index = hash % tableSize
   3) walk the chain, compare content with equals-like check
   4) found -> return that reference  |  not found -> create, insert, return
```

This is exactly why a **cached hash** matters: the pool lookup and `HashMap` both rely on hash codes constantly.

---

## 11. `byte[]` vs `char[]`: compact strings

### 11.1 Before Java 9

```java
private final char[] value;   // every char = 2 bytes (UTF-16)
```

`"Hello"` -> 5 chars x 2 bytes = **10 bytes**, even though every letter would fit in 1 byte.

```
Java 8:  value = char[5]
         +------+------+------+------+------+
         | 'H'  | 'e'  | 'l'  | 'l'  | 'o'  |
         | 2 B  | 2 B  | 2 B  | 2 B  | 2 B  |   = 10 bytes
         +------+------+------+------+------+
         00 48  00 65  00 6C  00 6C  00 6F
         (every first byte is wasted zeros)
```

### 11.2 Since Java 9: Compact Strings (JEP 254)

```java
private final byte[] value;   // 1 byte/char when possible
private final byte coder;     // tells how to interpret the bytes
```

Most text in real programs (English, digits, punctuation, identifiers, JSON keys) is within the **Latin-1** range (code points 0 to 255). Those need only **1 byte**.

```
Java 9+: value = byte[5], coder = 0 (LATIN1)
         +------+------+------+------+------+
         | 0x48 | 0x65 | 0x6C | 0x6C | 0x6F |
         | 1 B  | 1 B  | 1 B  | 1 B  | 1 B  |   = 5 bytes
         +------+------+------+------+------+
           'H'    'e'    'l'    'l'    'o'
```

**5 bytes instead of 10, a 50% saving for the character data.**

### 11.3 Visual comparison

```
"Hello"   Java 8  (char[]):  [00 48][00 65][00 6C][00 6C][00 6F]   10 bytes
"Hello"   Java 9+ (byte[]):  [48][65][6C][6C][6F]                   5 bytes
```

> The 50% figure applies to the **character data of Latin-1 text**. The fixed overhead of the String and array headers stays the same, and text with non-Latin characters does **not** shrink.

---

## 12. The `coder` field

`coder` tells the JVM **how to read** the `byte[]`.

| coder | Name | Bytes per char | Used when |
|---|---|---|---|
| `0` | **LATIN1** | 1 | every character is in range U+0000 to U+00FF |
| `1` | **UTF16** | 2 | at least one character is outside Latin-1 |

### 12.1 LATIN1 example: `"Java"`

```
String object                      byte[] value
+-----------------+               +------+------+------+------+
| value  ---------+-------------->| 0x4A | 0x61 | 0x76 | 0x61 |
| coder = 0       |               |  'J' |  'a' |  'v' |  'a' |
| hash  = 0       |               +------+------+------+------+
+-----------------+                length in chars = value.length >> 0 = 4
```

### 12.2 UTF16 example: `"Hi" + Hindi char` e.g. `"Hiक"`

The whole string switches to UTF-16 if **even one** character needs it. All characters then take 2 bytes.

```
String object                      byte[] value (6 bytes for 3 chars)
+-----------------+               +-------+-------+-------+-------+-------+-------+
| value  ---------+-------------->| 0x48  | 0x00  | 0x69  | 0x00  | 0x15  | 0x09  |
| coder = 1       |               +-------+-------+-------+-------+-------+-------+
| hash  = 0       |                 'H' (2 bytes)   'i' (2 bytes)   'क' U+0915
+-----------------+                length in chars = value.length >> 1 = 3
```

(Bytes are stored in the platform's native byte order, so the exact order of each pair can differ between machines. The point is: 2 bytes per char.)

### 12.3 How methods use `coder`

```java
public int length() {
    return value.length >> coder;      // coder 0 -> /1,  coder 1 -> /2
}

public char charAt(int index) {
    if (isLatin1()) {
        return (char)(value[index] & 0xff);     // 1 byte -> char
    }
    return StringUTF16.charAt(value, index);    // 2 bytes -> char
}
```

```
        charAt(i)
           |
     coder == 0 ?
      /          \
    yes           no
     |             |
 read 1 byte    read 2 bytes
 value[i]       value[2i], value[2i+1]
```

### 12.4 Decision flow when a String is created

```
        new String / literal "..."
                  |
       scan all characters
                  |
   all chars <= 0xFF (Latin-1)?
          /                \
        YES                 NO
         |                   |
   coder = 0             coder = 1
   byte[] 1 B/char       byte[] 2 B/char
```

---

## 13. Hash caching

### 13.1 How `hashCode()` is computed

```
hash = s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
```

Example `"abc"`:

```
hash = 'a'*31^2 + 'b'*31 + 'c'
     = 97*961   + 98*31   + 99
     = 93217    + 3038    + 99
     = 96354
```

This costs **O(n)**: it loops over every character. For a long string that is expensive if done repeatedly.

### 13.2 The cache

```java
private int hash;   // 0 by default

public int hashCode() {
    int h = hash;
    if (h == 0 && !hashIsZero) {      // not computed yet
        h = isLatin1() ? StringLatin1.hashCode(value)
                       : StringUTF16.hashCode(value);
        if (h == 0) hashIsZero = true;   // real hash is 0, remember that
        else        hash = h;            // store for next time
    }
    return h;
}
```

### 13.3 Visual: first call vs later calls

```
FIRST call  s.hashCode()
+-----------------+
| value -> bytes  |        compute over all chars  (slow, O(n))
| coder           | ----->  96354
| hash = 0        |            |
+-----------------+            v
                      store in `hash` field
+-----------------+
| hash = 96354    |   <- cached inside the object
+-----------------+

SECOND call s.hashCode()
+-----------------+
| hash = 96354    | ----->  return immediately  (fast, O(1))
+-----------------+
```

### 13.4 Why this is safe

Cache only works **because strings are immutable**: the content can never change, so the hash can never become stale. A mutable object could not do this safely.

### 13.5 Where it pays off

- `HashMap<String, V>`, `HashSet<String>`, `ConcurrentHashMap`: every `get`/`put` needs the key's hash.
- The String Pool's hash table lookups.
- Strings used repeatedly as keys (very common: field names, IDs, config keys).

---

## 14. Full lifecycle: from source code to bytes in memory

Putting everything together for:

```java
public class Demo {
    public static void main(String[] args) {
        String s1 = "Hello";
        String s2 = "Hello";
        String s3 = new String("Hello");
        String s4 = s1 + " World";
        String s5 = s4.intern();
    }
}
```

### Step A: Compile time

```
Demo.java --javac--> Demo.class

Constant Pool inside the .class file:
  #1 = String  "Hello"
  #2 = String  " World"
  (literals only live here as TEXT, no String objects yet)
```

### Step B: Class load and execution

When the JVM first executes `ldc "Hello"`, it **resolves** the constant: looks in the String Table, creates the String object if absent, and caches the link so later `ldc` of the same literal gives the same reference.

### Step C: Final memory state

```
STACK (main)                  HEAP
+------+                    +============= STRING POOL (String Table) ============+
| s1   |--------+---------->| String@P1                                          |
+------+        |           |   coder=0, hash=0                                  |
| s2   |--------+           |   value --> byte[5] { 48 65 6C 6C 6F }  "Hello"    |
+------+                    |                                                    |
| s5   |-----+              | String@P2   (added by intern())                    |
+------+     +------------->|   coder=0                                          |
                            |   value --> byte[11] { "Hello World" }             |
                            |   (this pooled ref IS the same object as s4 if     |
                            |    "Hello World" was not already in the pool)      |
                            |                                                    |
                            | String@P3   " World" literal                       |
                            +====================================================+

+------+                    +============= NORMAL HEAP ==========================+
| s3   |------------------->| String@H1                                          |
+------+                    |   coder=0, hash=0                                  |
| s4   |------------------->|   value --> byte[5] { "Hello" }  (new object)      |
+------+                    |                                                    |
                            | String@H2   "Hello World"  (runtime concat)        |
                            |   value --> byte[11]                               |
                            +====================================================+
```

Note: in the case of `s5 = s4.intern()`, if `"Hello World"` is not yet in the pool, the pool records the **same** heap object that `s4` references, so `s4 == s5` would be `true` on modern JDKs. If it was already in the pool, `s5` points to that pooled copy instead. Either way `s5` is the canonical pool reference.

### Step D: Results

```
s1 == s2       true    (both pool "Hello")
s1 == s3       false   (pool vs new heap object)
s1.equals(s3)  true
s4 == "Hello World"   false in general (runtime concat), true after intern
s5 == "Hello World"   true  (pool reference)
```

### Step E: "Fetching" a character

```java
char c = s1.charAt(1);
```

```
 s1 (stack ref)
   |
   v
 String object  --coder==0-->  value[1]  --> 0x65  --> (char) 'e'
   |                              ^
   '---- value ref ---------------'  (byte[] in heap)
```

Walk: stack reference -> String object -> check `coder` -> index into `byte[]` -> convert to `char`.

---

## 15. Loops, garbage and StringBuilder

### 15.1 The problem

```java
String result = "";
for (int i = 0; i < 100000; i++) {
    result += "x";     // creates a new String each time, copies all old chars
}
```

Total work grows as roughly **O(n^2)** (copying longer and longer strings), and ~100,000 garbage objects fill the heap.

### 15.2 The fix

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 100000; i++) {
    sb.append("x");    // mutates one internal buffer, grows when full
}
String result = sb.toString();   // one final String
```

```
String +=                              StringBuilder
----------                             -------------
iter1: [x]                             buffer: [x . . . . . . .]
iter2: [xx]   [x] = garbage            buffer: [x x . . . . . .]
iter3: [xxx]  [xx] = garbage           buffer: [x x x . . . . .]
 ...                                    ...(capacity doubles when full)
many objects, many copies              1 buffer, few copies
```

| Class | Mutable | Thread-safe | Speed | Use when |
|---|---|---|---|---|
| `String` | No | Yes (immutable) | slow for repeated edits | fixed text |
| `StringBuilder` | Yes | No | fastest | single-thread building |
| `StringBuffer` | Yes | Yes (synchronized) | slower | multi-thread building |

`StringBuilder` uses the same compact trick: internal `byte[]` + `coder`.

---

## 16. Summary of optimizations

| Optimization | What it does | Benefit |
|---|---|---|
| **Compact Strings (`byte[]` + `coder`)** | 1 byte per char for Latin-1 text instead of 2 | Up to ~50% less memory for character data of typical text, less GC pressure |
| **String Pool** | One shared object per distinct literal | No duplicate objects for identical literals |
| **Hash caching** | `hash` stored in the object after first computation | O(1) hash retrieval after first call; faster `HashMap`/`HashSet`/pool lookups |
| **Immutability** | Content never changes | Safe sharing, thread-safety, makes the two optimizations above possible |

```
          Immutability
         /     |      \
        v      v       v
  String Pool  Hash    Thread
  (sharing)   caching   safety
```

---

## 17. Predict the output (practice)

```java
String a = "java";
String b = "java";
String c = new String("java");
String d = "ja" + "va";
String e1 = "ja";
String e = e1 + "va";
final String f1 = "ja";
String f = f1 + "va";
```

| Expression | Result | Why |
|---|---|---|
| `a == b` | `true` | same pool literal |
| `a == c` | `false` | `c` is a new heap object |
| `a == d` | `true` | compile-time constant folding |
| `a == e` | `false` | `e1` is a normal variable, concat at runtime -> heap |
| `a == f` | `true` | `f1` is `final` constant, so folded at compile time |
| `a == e.intern()` | `true` | intern returns the pool reference |
| `a.equals(c)` | `true` | same content |
| `c.equals(e)` | `true` | same content |

Another one:

```java
String s = "abc";
s.toUpperCase();
System.out.println(s);        // abc  (result was discarded)
s = s.toUpperCase();
System.out.println(s);        // ABC
```

---

## 18. Interview questions and answers

**Q1. What is the String Pool?**
A special region in the heap, implemented as a hash table (String Table), that stores one copy of each distinct string literal so identical literals share a single object.

**Q2. `String s = "a"` vs `new String("a")`?**
The literal is looked up in or added to the pool and the pool reference is returned. `new` always creates a new heap object (and the literal `"a"` also lands in the pool if absent).

**Q3. How many objects does `new String("hello")` create?**
Up to two: the pooled literal (if not already present) and the new heap object.

**Q4. Why is String immutable?**
Safe pooling and sharing, thread-safety, hash caching, security, and reliability as hash map keys.

**Q5. Is the `String` variable immutable?**
No. The **object** is immutable; the **reference variable** can be reassigned to point to a different String.

**Q6. `==` vs `equals()`?**
`==` compares references; `equals()` compares content.

**Q7. Why does `s1 + s2` not go in the pool?**
Values of variables are only known at runtime, so the compiler cannot fold it into a constant; the result is a new heap object. Use `intern()` to pool it.

**Q8. What changed in Java 9 regarding String?**
Compact Strings: `char[]` replaced by `byte[]` plus a `coder` flag (LATIN1 or UTF16), saving memory for text that fits in Latin-1.

**Q9. What does the `coder` field do?**
It says whether bytes are 1-byte Latin-1 characters (0) or 2-byte UTF-16 characters (1), so `length()`, `charAt()` etc. interpret the array correctly.

**Q10. Does a string with one emoji or Hindi character stay compact?**
No. If any character is outside Latin-1, the **entire** string is stored as UTF-16 (2 bytes per char).

**Q11. Why is hash caching possible?**
Because the content can never change, the computed hash stays valid forever, so it is stored in the `hash` field.

**Q12. Why prefer `StringBuilder` in loops?**
`String +=` creates a new object and copies all characters every iteration (O(n^2), lots of garbage). `StringBuilder` appends into one growable buffer.

**Q13. Where is the String Pool located?**
In the heap (since Java 7). Before Java 7 it was in PermGen.

**Q14. Can pooled strings be garbage collected?**
Yes, on modern JVMs, if no references to them remain.

**Q15. Why is String a good HashMap key?**
Immutable (key never changes), cached hash (fast lookups), and well-defined `equals`/`hashCode`.

---

## 19. Cheat sheet

```
CREATION
  "text"              -> POOL (reused)
  new String("text")  -> HEAP (always new) (+ pool literal if absent)
  a + b (variables)   -> HEAP (runtime)
  "a" + "b" (literals)-> POOL (compile-time folded)
  s.intern()          -> returns POOL reference

COMPARISON
  ==        -> same object?
  equals()  -> same content?

INTERNALS (Java 9+)
  String { byte[] value; byte coder; int hash; boolean hashIsZero; }
  coder 0 = LATIN1 (1 byte/char)     coder 1 = UTF16 (2 bytes/char)
  length() = value.length >> coder

MEMORY PATH
  stack ref --> String object --> byte[] --> character bytes

WHY IT'S FAST / SAFE
  Immutable   -> pool sharing + thread-safe + cacheable hash
  Compact     -> ~50% less char data for Latin-1 text
  Hash cached -> O(1) hashCode after first call

LOOPS
  Many edits? Use StringBuilder (single thread) / StringBuffer (multi-thread)
```

---

*End of notes.*
