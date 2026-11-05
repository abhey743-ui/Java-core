# Java Collections Framework: `Set` and `Map` (Deep Dive)

> Easy-language notes with diagrams, examples, and interview points.

---

## Table of Contents

1. [Big Picture: Where Set and Map Live](#1-big-picture)
2. [Set Interface](#2-set-interface)
   - HashSet, LinkedHashSet, TreeSet
   - SortedSet and NavigableSet methods
3. [Map Interface Fundamentals](#3-map-interface-fundamentals)
   - Key operations, data views, `Map.Entry`, utility methods
4. [TreeMap](#4-treemap)
5. [Thread-Safe Maps: Hashtable vs ConcurrentHashMap](#5-thread-safe-maps)
6. [Specialized Maps](#6-specialized-maps)
   - Properties, WeakHashMap, IdentityHashMap, EnumMap
7. [Master Comparison Tables](#7-master-comparison-tables)
8. [Which One Should I Use?](#8-which-one-should-i-use)
9. [Common Mistakes](#9-common-mistakes)
10. [Interview Questions](#10-interview-questions)

---

## 1. Big Picture

Java's Collections Framework has **two separate families**:

- The **`Collection`** family (List, Set, Queue): stores **single elements**.
- The **`Map`** family: stores **key → value pairs**.

**Important:** `Map` does **NOT** extend `Collection`. It is its own tree.

```
                    Iterable
                       |
                  Collection                          Map  (separate tree!)
          ____________|____________                    |
         |            |            |          ________|__________________
        List         Set         Queue       |         |        |        |
                      |                  HashMap   SortedMap  Hashtable  ...
              ________|_________              |         |
             |        |         |       LinkedHashMap  NavigableMap
         HashSet  SortedSet  LinkedHashSet                |
                      |                               TreeMap
                NavigableSet
                      |
                   TreeSet
```

**Why is Map separate?**
A `Collection` is a group of *single items*. A `Map` is a group of *pairs*. Methods like `add(element)` don't make sense for pairs, so Java kept them apart.

---

## 2. Set Interface

### 2.1 What is a Set?

A **Set** is a collection that holds **unique elements only (no duplicates)**.

Think of it like a **bag of unique stickers**. If you try to add the same sticker twice, the bag ignores the second one.

### 2.2 Hierarchy

```
Iterable<E>
    |
Collection<E>
    |
  Set<E>   <-- no duplicates
    |
    +-- HashSet<E>          (no ordering)
    +-- LinkedHashSet<E>    (insertion order)
    +-- SortedSet<E>
            |
        NavigableSet<E>
            |
         TreeSet<E>         (sorted order)
```

- `Iterable` gives you the ability to use the **for-each loop**.
- `Collection` gives you `add()`, `remove()`, `contains()`, `size()`, etc.
- `Set` adds the rule: **no duplicates**.

### 2.3 The Three Main Implementations at a Glance

| Feature | `HashSet` | `LinkedHashSet` | `TreeSet` |
|---|---|---|---|
| Ordering | **None** (random-looking) | **Insertion order** | **Sorted order** |
| Internal structure | Hash table (backed by `HashMap`) | Hash table + doubly linked list | Self-balancing binary search tree (Red-Black tree) |
| `add` / `remove` / `contains` | O(1) average | O(1) average | O(log n) |
| Allows `null`? | Yes (one) | Yes (one) | No (with natural ordering, throws `NullPointerException`) |
| Best for | Fast uniqueness check | Unique + keep order | Unique + always sorted + range queries |

---

### 2.4 HashSet: "Fast, but no order"

**How it works (simple idea):**
It uses a hash table. Each element's `hashCode()` decides which **bucket** it goes into.

```
add("Apple")  -> hashCode -> bucket 3
add("Banana") -> hashCode -> bucket 7
add("Cherry") -> hashCode -> bucket 1

Buckets:
 [0]  
 [1]  Cherry
 [2]  
 [3]  Apple
 [4]  
 [5]  
 [6]  
 [7]  Banana

Iteration goes bucket by bucket: Cherry, Apple, Banana
(NOT the order you inserted them!)
```

**Behind the scenes:** `HashSet` is actually a `HashMap` where your element is the **key** and a dummy constant object is the value.

```java
// Simplified internals of HashSet
private transient HashMap<E, Object> map;
private static final Object PRESENT = new Object();

public boolean add(E e) {
    return map.put(e, PRESENT) == null;   // true if it was new
}
```

**Example:**

```java
import java.util.*;

public class HashSetDemo {
    public static void main(String[] args) {
        Set<String> fruits = new HashSet<>();

        System.out.println(fruits.add("Apple"));   // true  (new)
        System.out.println(fruits.add("Banana"));  // true
        System.out.println(fruits.add("Apple"));   // false (duplicate ignored)
        fruits.add("Cherry");
        fruits.add(null);                          // one null is allowed

        System.out.println(fruits);                // order is NOT guaranteed
        System.out.println(fruits.contains("Banana")); // true
        fruits.remove("Banana");
        System.out.println(fruits.size());
    }
}
```

> **Rule:** For custom objects in a `HashSet`, you must override **both** `equals()` and `hashCode()`. Otherwise duplicates will not be detected.

```java
class Student {
    int id; String name;
    Student(int id, String name) { this.id = id; this.name = name; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student s)) return false;
        return id == s.id && name.equals(s.name);
    }
    @Override public int hashCode() { return Objects.hash(id, name); }
}
```

---

### 2.5 LinkedHashSet: "Unique + remembers insertion order"

It is a `HashSet` **plus a doubly linked list** that connects elements in the order they were added.

```
Hash table (for fast lookup)         Linked list (for order)

 [1] Cherry  ----+
 [3] Apple   ----|---+              head -> Apple <-> Banana <-> Cherry <- tail
 [7] Banana  ----|---|--+                     (insertion order)
```

```java
Set<String> set = new LinkedHashSet<>();
set.add("Banana");
set.add("Apple");
set.add("Cherry");
set.add("Apple");   // ignored, and the original position stays

System.out.println(set);   // [Banana, Apple, Cherry]  <- insertion order kept
```

**Use it when:** you want no duplicates **and** predictable iteration order (e.g., "recently visited unique pages").

Cost: slightly more memory than `HashSet` because of the linked list pointers.

---

### 2.6 TreeSet: "Always sorted"

- Stores elements in **sorted order** (natural ordering, or a custom `Comparator`).
- Internally a **self-balancing binary search tree** (a Red-Black tree, via `TreeMap`).
- Implements **`SortedSet`** and **`NavigableSet`**.

```
Insert: 50, 30, 70, 20, 40, 60, 80

            50
          /    \
        30      70
       /  \    /  \
     20   40  60   80

In-order traversal (left, root, right) = 20 30 40 50 60 70 80  (sorted!)
```

"Self-balancing" means the tree **re-arranges itself** after inserts/deletes so it never becomes a long "stick". That keeps operations at **O(log n)**.

```
BAD (unbalanced):        GOOD (balanced):
 10                          30
   \                        /  \
    20                    10    50
      \
       30
         \
          40 ...  O(n)       O(log n)
```

**Basic example:**

```java
Set<Integer> nums = new TreeSet<>();
nums.add(50); nums.add(10); nums.add(40); nums.add(20);
System.out.println(nums);   // [10, 20, 40, 50]  <- auto sorted
```

**Custom ordering with a Comparator:**

```java
// Descending order
Set<Integer> desc = new TreeSet<>(Comparator.reverseOrder());
desc.addAll(List.of(5, 1, 9, 3));
System.out.println(desc);   // [9, 5, 3, 1]

// Sort strings by length
Set<String> byLen = new TreeSet<>(Comparator.comparingInt(String::length));
byLen.addAll(List.of("kiwi", "fig", "banana"));
System.out.println(byLen);  // [fig, kiwi, banana]
```

> **Gotcha:** `TreeSet` uses `compareTo()`/`compare()` to decide equality, **not** `equals()`. In the `byLen` example, adding another 3-letter word like `"pig"` would be treated as a duplicate of `"fig"` and ignored!

> **Gotcha:** Custom objects in a `TreeSet` must implement `Comparable` or you must pass a `Comparator`. Otherwise you get `ClassCastException`.

---

### 2.7 SortedSet and NavigableSet Methods

`TreeSet` gets a rich set of methods from two interfaces:

```
SortedSet    -> first(), last(), headSet(), tailSet(), subSet()
NavigableSet -> lower(), floor(), ceiling(), higher(),
                pollFirst(), pollLast(), descendingSet(), descendingIterator()
```

We will use this set for all the examples:

```java
TreeSet<Integer> ts = new TreeSet<>(List.of(10, 20, 30, 40, 50));
```

```
Number line:   10    20    30    40    50
```

#### (a) `first()` and `last()`

```java
ts.first();   // 10  (smallest)
ts.last();    // 50  (largest)
```

#### (b) `headSet(toElement)`: everything **before** a value

```java
ts.headSet(30);          // [10, 20]          (30 excluded by default)
ts.headSet(30, true);    // [10, 20, 30]      (30 included)
```

```
10   20  | 30   40   50
<-----   |
 headSet(30)
```

#### (c) `tailSet(fromElement)`: everything **from** a value onward

```java
ts.tailSet(30);          // [30, 40, 50]      (30 included by default)
ts.tailSet(30, false);   // [40, 50]          (30 excluded)
```

```
10   20   | 30   40   50
          |------------>
          tailSet(30)
```

> Easy to remember: **head** excludes the boundary, **tail** includes it (by default).

#### (d) `subSet(from, to)`: a range

```java
ts.subSet(20, 40);               // [20, 30]     (from inclusive, to exclusive)
ts.subSet(20, true, 40, true);   // [20, 30, 40] (both inclusive)
```

> `headSet`, `tailSet`, `subSet` return **views** (live windows), not copies. Changes to the view affect the original set.

#### (e) Range query methods: the "closest value" family

These are great for **"find the nearest value"** problems.

| Method | Meaning | Returns |
|---|---|---|
| `lower(x)` | Greatest element **strictly less than** x | `<` |
| `floor(x)` | Greatest element **less than or equal to** x | `<=` |
| `ceiling(x)` | Smallest element **greater than or equal to** x | `>=` |
| `higher(x)` | Smallest element **strictly greater than** x | `>` |

Each returns `null` if no such element exists.

```java
// ts = [10, 20, 30, 40, 50]

ts.lower(30);     // 20   (strictly less than 30)
ts.floor(30);     // 30   (<= 30, and 30 exists)
ts.ceiling(30);   // 30   (>= 30, and 30 exists)
ts.higher(30);    // 40   (strictly greater than 30)

ts.floor(35);     // 30   (35 not in set, so the greatest <= 35)
ts.ceiling(35);   // 40   (smallest >= 35)
ts.lower(10);     // null (nothing smaller)
ts.higher(50);    // null (nothing bigger)
```

```
Looking for 35 (not in set):

   10    20    30  [35]  40    50
                ^         ^
              floor(35) ceiling(35)
              lower(35) higher(35)
              (same here because 35 is absent)

Looking for 30 (in set):

   10    20  [30]  40    50
          ^    ^    ^
       lower floor/ higher
              ceiling
```

**Real-life example:** A bus schedule. "What's the next bus at or after 10:35?" That is `ceiling(10:35)`.

#### (f) Other handy `NavigableSet` methods

```java
ts.pollFirst();        // removes and returns 10
ts.pollLast();         // removes and returns 50
ts.descendingSet();    // view in reverse order: [40, 30, 20]

Iterator<Integer> it = ts.descendingIterator();
while (it.hasNext()) System.out.print(it.next() + " ");   // 40 30 20
```

---

## 3. Map Interface Fundamentals

### 3.1 What is a Map?

A **Map** stores **key → value** pairs.

- **Keys are unique.** Putting an existing key again **replaces** the old value.
- **Values can repeat.**

Think of a **dictionary** or **phone book**: name (key) → number (value).

```
        Map<String, Integer>
   +-----------+------------+
   |   KEY     |   VALUE    |
   +-----------+------------+
   |  "Alice"  |    25      |
   |  "Bob"    |    30      |
   |  "Carol"  |    25      |  <- value 25 repeated: OK
   +-----------+------------+
   Key "Alice" again -> replaces 25 with the new value
```

### 3.2 Key Operations

| Method | What it does |
|---|---|
| `put(key, value)` | Adds a pair. If key exists, **replaces** the value and returns the **old** value (else `null`). |
| `get(key)` | Returns the value for the key, or `null` if absent. |
| `containsKey(key)` | `true` if the key exists. |
| `containsValue(value)` | `true` if any key maps to this value (slow: scans everything, O(n)). |
| `size()` | Number of pairs. |
| `isEmpty()` | `true` if no pairs. |
| `remove(key)` | Removes the pair, returns the old value (or `null`). |

```java
Map<String, Integer> ages = new HashMap<>();

ages.put("Alice", 25);
ages.put("Bob", 30);
Integer old = ages.put("Alice", 26);   // replaces, returns 25

System.out.println(old);                    // 25
System.out.println(ages.get("Alice"));      // 26
System.out.println(ages.get("Zed"));        // null (not found)
System.out.println(ages.containsKey("Bob"));    // true
System.out.println(ages.containsValue(30));     // true
System.out.println(ages.size());            // 2
System.out.println(ages.isEmpty());         // false

ages.remove("Bob");
System.out.println(ages);                   // {Alice=26}
```

### 3.3 Data Views: `keySet()`, `values()`, `entrySet()`

A Map can't be looped directly. Instead you ask it for a **view** of its contents:

```
Map:  {Alice=25, Bob=30, Carol=28}
        |
        +-- keySet()   -> Set<String>               [Alice, Bob, Carol]
        +-- values()   -> Collection<Integer>       [25, 30, 28]
        +-- entrySet() -> Set<Map.Entry<String,Integer>>
                          [Alice=25, Bob=30, Carol=28]
```

- `keySet()` returns a **Set** (keys are unique).
- `values()` returns a **Collection** (values may repeat, so it can't be a Set).
- `entrySet()` returns a **Set of `Map.Entry`** objects (each one = one key + one value).

```java
Map<String, Integer> map = new HashMap<>();
map.put("Alice", 25); map.put("Bob", 30); map.put("Carol", 28);

// 1) Loop over keys
for (String k : map.keySet()) {
    System.out.println(k + " -> " + map.get(k));   // extra lookup each time
}

// 2) Loop over values only
for (int v : map.values()) {
    System.out.println(v);
}

// 3) Loop over entries (BEST way when you need both)
for (Map.Entry<String, Integer> e : map.entrySet()) {
    System.out.println(e.getKey() + " -> " + e.getValue());
}

// Java 8+ one-liner
map.forEach((k, v) -> System.out.println(k + " -> " + v));
```

> **Why is `entrySet()` best?** With `keySet()` you call `get(key)` for every key (extra lookup). `entrySet()` gives key and value together in one go.

> **Views are live:** removing from `keySet()` or `values()` removes from the original map. You can't `add` to `keySet()`/`values()`.

You can even modify values while iterating entries:

```java
for (Map.Entry<String, Integer> e : map.entrySet()) {
    e.setValue(e.getValue() + 1);   // bumps everyone's age by 1
}
```

### 3.4 Utility Methods (Production-Ready Helpers)

#### `getOrDefault(key, defaultValue)`

Avoids `null` checks.

```java
// Old, clumsy way
Integer count = map.get("Zed");
if (count == null) count = 0;

// Clean way
int count2 = map.getOrDefault("Zed", 0);   // 0 (map is NOT changed)
```

#### `putIfAbsent(key, value)`

Puts the value **only if the key is missing** (or mapped to `null`). Returns the existing value, or `null` if it inserted.

```java
map.putIfAbsent("Dave", 40);   // inserted (Dave was missing) -> returns null
map.putIfAbsent("Dave", 99);   // ignored (Dave exists)       -> returns 40
System.out.println(map.get("Dave"));   // 40
```

#### Real example: Word frequency counter

```java
String text = "to be or not to be";
Map<String, Integer> freq = new HashMap<>();

for (String word : text.split(" ")) {
    freq.put(word, freq.getOrDefault(word, 0) + 1);
}
System.out.println(freq);   // {not=1, be=2, or=1, to=2}  (order may vary)
```

```
Step by step:
 "to"  -> getOrDefault = 0 -> put("to", 1)
 "be"  -> 0 -> put("be", 1)
 "or"  -> 0 -> put("or", 1)
 "not" -> 0 -> put("not", 1)
 "to"  -> 1 -> put("to", 2)
 "be"  -> 1 -> put("be", 2)
```

Even shorter with `merge`:

```java
for (String w : text.split(" ")) freq.merge(w, 1, Integer::sum);
```

### 3.5 Quick Look: HashMap vs LinkedHashMap (the base Map implementations)

| | `HashMap` | `LinkedHashMap` |
|---|---|---|
| Order | None | Insertion order (or access order) |
| Speed | O(1) avg | O(1) avg (a bit more memory) |
| null | 1 null key, many null values | Same |

```java
Map<String,Integer> hm  = new HashMap<>();
Map<String,Integer> lhm = new LinkedHashMap<>();
// put "c", "a", "b" in both:
// hm  prints in some hash order, e.g. {a=.., b=.., c=..}
// lhm prints {c=.., a=.., b=..}  (insertion order)
```

---

## 4. TreeMap

**TreeMap = Map that keeps its keys sorted.**
It is the Map version of `TreeSet`: same Red-Black tree inside, same O(log n) cost. In fact, `TreeSet` is built on top of `TreeMap`.

```
TreeMap<Integer, String>:  put(30,"C") put(10,"A") put(20,"B")

          20=B
         /    \
      10=A    30=C

Printing: {10=A, 20=B, 30=C}   <- sorted by KEY
```

- Implements `SortedMap` and `NavigableMap`.
- Sorted by **key** (natural order or a `Comparator`).
- **No `null` keys** (natural ordering would throw `NullPointerException`). Null values are allowed.

### Navigation Methods

```java
TreeMap<Integer, String> tm = new TreeMap<>();
tm.put(10, "Ten");
tm.put(20, "Twenty");
tm.put(30, "Thirty");
tm.put(40, "Forty");
tm.put(50, "Fifty");
```

| Method | Result | Meaning |
|---|---|---|
| `firstKey()` | `10` | Smallest key |
| `lastKey()` | `50` | Largest key |
| `firstEntry()` | `10=Ten` | Entry with smallest key |
| `lastEntry()` | `50=Fifty` | Entry with largest key |
| `lowerKey(30)` | `20` | Greatest key `<` 30 |
| `floorKey(30)` | `30` | Greatest key `<=` 30 |
| `ceilingKey(35)` | `40` | Smallest key `>=` 35 |
| `higherKey(30)` | `40` | Smallest key `>` 30 |
| `headMap(30)` | `{10=Ten, 20=Twenty}` | Keys `< 30` |
| `tailMap(30)` | `{30=.., 40=.., 50=..}` | Keys `>= 30` |
| `subMap(20, 40)` | `{20=.., 30=..}` | `20 <= key < 40` |
| `pollFirstEntry()` | removes `10=Ten` | Remove and return smallest |
| `descendingMap()` | `{50=.., 40=.., ... 10=..}` | Reverse-ordered view |

```java
System.out.println(tm.firstKey());         // 10
System.out.println(tm.lastKey());          // 50
System.out.println(tm.descendingMap());    // {50=Fifty, 40=Forty, 30=Thirty, 20=Twenty, 10=Ten}
System.out.println(tm.floorKey(35));       // 30
System.out.println(tm.ceilingEntry(35));   // 40=Forty
```

**Real-life example: grading system**

```java
TreeMap<Integer, String> grades = new TreeMap<>();
grades.put(0,  "F");
grades.put(50, "D");
grades.put(65, "C");
grades.put(80, "B");
grades.put(90, "A");

int score = 72;
System.out.println(grades.floorEntry(score).getValue());   // "C"
// floorEntry(72) -> greatest key <= 72 -> 65 -> "C"
```

---

## 5. Thread-Safe Maps

### 5.1 The Problem

`HashMap` is **not thread-safe**. If two threads modify it at the same time, you can get lost updates, corrupted data, or (in old Java versions) infinite loops.

```
Thread A: put("x", 1) ---+
                          +--> HashMap  -> data can be corrupted
Thread B: put("y", 2) ---+
```

### 5.2 `Hashtable` (Legacy)

- Old class from Java 1.0 (before the Collections Framework). Considered **legacy**.
- Every method is `synchronized` on the **whole table**, so only **one thread at a time** can touch it (even for reads).
- **No `null` keys or values** (throws `NullPointerException`).

```
Hashtable: ONE big lock

 Thread A ---> [ LOCK ] --> whole table
 Thread B ---> waits...
 Thread C ---> waits...
```

Slow when many threads are involved.

### 5.3 `ConcurrentHashMap` (Modern, Efficient)

- Thread-safe **without** locking the whole map.
- Many threads can **read** at the same time, and **write** to different parts at the same time.
- Internally (Java 8+): uses **CAS (compare-and-swap)** operations plus locking only on the **single bucket** being modified. (Older Java 7 used "segment locks".)
- **No `null` keys or values.**
- Iterators are **weakly consistent**: they never throw `ConcurrentModificationException`.

```
ConcurrentHashMap: fine-grained locks (per bucket)

 Bucket 0   Bucket 1   Bucket 2   Bucket 3
   |          |          |          |
 Thread A   Thread B   Thread C    (free)
 (writes)   (writes)   (reads)
       All working at the same time!
```

```java
import java.util.concurrent.*;

ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<>();
chm.put("a", 1);
chm.putIfAbsent("b", 2);

// Atomic operations: safe for counters in multi-threaded code
chm.merge("a", 1, Integer::sum);          // a = 2 (atomic)
chm.compute("b", (k, v) -> v == null ? 1 : v + 10);   // b = 12
```

> **Tip:** Don't write `if (!map.containsKey(k)) map.put(k, v);` even on a `ConcurrentHashMap`. Two threads can both pass the check. Use the atomic `putIfAbsent`, `merge`, or `compute` instead.

### 5.4 Comparison

| Feature | `HashMap` | `Hashtable` | `ConcurrentHashMap` |
|---|---|---|---|
| Thread-safe | No | Yes | Yes |
| Locking | None | Whole table | Per bucket / CAS |
| Performance (multi-thread) | n/a (unsafe) | Slow | Fast |
| null key | 1 allowed | No | No |
| null value | Allowed | No | No |
| Iterator | Fail-fast | Fail-fast (Enumeration is not) | Weakly consistent |
| Use today? | Single-threaded | Avoid (legacy) | Yes, for concurrency |

(Another option: `Collections.synchronizedMap(map)` wraps any map with one lock, similar to `Hashtable`.)

---

## 6. Specialized Maps

### 6.1 `Properties`: for configuration data

- A special `Map` where **both keys and values are Strings**.
- Extends `Hashtable<Object,Object>` (so it is legacy-based, but still widely used).
- Built-in support for **reading/writing `.properties` files**.

Example file `app.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/mydb
db.user=admin
db.password=secret
```

```java
import java.io.*;
import java.util.Properties;

Properties props = new Properties();

// Load from file
try (FileInputStream in = new FileInputStream("app.properties")) {
    props.load(in);
}

String url  = props.getProperty("db.url");
String port = props.getProperty("db.port", "3306");   // default if missing

// Set and save
props.setProperty("db.user", "root");
try (FileOutputStream out = new FileOutputStream("app.properties")) {
    props.store(out, "Updated config");
}
```

```
 app.properties (file)  --load()-->  Properties (in memory)
                        <--store()--
```

Use `getProperty()` / `setProperty()` (String-safe) rather than `get()` / `put()`.

---

### 6.2 `WeakHashMap`: for cache-like behavior

**Idea:** Keys are held by **weak references**. If a key object is no longer used anywhere else in your program, the **garbage collector (GC) can remove it, and the map entry disappears automatically**.

```
Normal HashMap:
   map ---strong---> KeyObject     (KeyObject can NEVER be GC'd while in map)

WeakHashMap:
   map ---weak-----> KeyObject     (if nothing else points to KeyObject,
                                     GC removes it AND the entry vanishes)
```

```java
import java.util.*;

Map<Object, String> cache = new WeakHashMap<>();

Object key = new Object();
cache.put(key, "some cached data");
System.out.println(cache.size());   // 1

key = null;          // remove the only strong reference
System.gc();         // request GC (only a hint)

// After GC runs:
System.out.println(cache.size());   // likely 0: entry removed automatically
```

**Why useful?** Metadata/caches keyed by an object: when the object dies, its cached data is cleaned up automatically, so there is **no memory leak**.

> **Warning:** Do not use `String` literals or small cached `Integer`s as keys to test this. They are strongly held elsewhere (string pool, Integer cache) and will never be collected.
> Only the **key** is weak. The **value** is held strongly, so make sure the value doesn't reference its own key, or the entry will never be removed.

---

### 6.3 `IdentityHashMap`: compares with `==`, not `equals()`

A normal map treats two keys as the same if `key1.equals(key2)`.
`IdentityHashMap` treats them as the same **only if `key1 == key2`** (same object in memory).

```
 String a = new String("hi");
 String b = new String("hi");

 a.equals(b)  -> true    (same content)
 a == b       -> false   (different objects)

 HashMap:         a and b are the SAME key     -> 1 entry
 IdentityHashMap: a and b are DIFFERENT keys   -> 2 entries
```

```java
String a = new String("hi");
String b = new String("hi");

Map<String, Integer> hm = new HashMap<>();
hm.put(a, 1);
hm.put(b, 2);
System.out.println(hm.size());    // 1  (b replaced a's value)

Map<String, Integer> im = new IdentityHashMap<>();
im.put(a, 1);
im.put(b, 2);
System.out.println(im.size());    // 2  (different objects)
```

- Uses `System.identityHashCode()` instead of `hashCode()`.
- Allows `null` key.
- **Use cases:** object graph traversal (serialization, deep-copy tools: "have I already visited this exact object?"), proxies, special framework internals.
- It intentionally **violates the general `Map` contract**, so use it only when you really need reference equality.

---

### 6.4 `EnumMap`: super-fast map for Enum keys

A map made **only for `enum` keys**. Internally it is just an **array**, indexed by each enum constant's `ordinal()` (its position number).

```java
enum Day { MON, TUE, WED, THU, FRI }
//         0    1    2    3    4   <- ordinals
```

```
EnumMap<Day, String> internal array:

 index:   0       1       2       3       4
        +-------+-------+-------+-------+-------+
 value: | "Gym" | null  |"Study"| null  |"Party"|
        +-------+-------+-------+-------+-------+
          MON     TUE     WED     THU     FRI

 get(Day.WED) -> array[2]   (direct index: no hashing, no collisions!)
```

```java
import java.util.*;

EnumMap<Day, String> plan = new EnumMap<>(Day.class);   // must pass the enum class
plan.put(Day.FRI,  "Party");
plan.put(Day.MON,  "Gym");
plan.put(Day.WED,  "Study");

System.out.println(plan);   // {MON=Gym, WED=Study, FRI=Party}
// Iteration follows enum declaration order, not insertion order.
```

**Why it is great:**

| Benefit | Reason |
|---|---|
| Faster than `HashMap` | Direct array access, no hashing |
| Less memory | No bucket/node objects, just an array |
| Predictable order | Iterates in the **natural order of the enum** (ordinal order) |
| Type-safe | Only accepts keys from that one enum |

- `null` keys are **not** allowed; `null` values are allowed.
- Not thread-safe (wrap with `Collections.synchronizedMap` if needed).
- Sibling class: `EnumSet`, a super-fast Set for enums (implemented with bit vectors).

---

## 7. Master Comparison Tables

### 7.1 All Sets

| | HashSet | LinkedHashSet | TreeSet |
|---|---|---|---|
| Order | None | Insertion | Sorted |
| Time | O(1) | O(1) | O(log n) |
| Null | 1 | 1 | No |
| Structure | Hash table | Hash table + linked list | Red-Black tree |
| Extra API | none | none | `first`, `last`, `floor`, `ceiling`, `headSet`... |

### 7.2 All Maps

| Map | Order | Time | Null key | Thread-safe | Special feature |
|---|---|---|---|---|---|
| `HashMap` | None | O(1) | 1 | No | General purpose |
| `LinkedHashMap` | Insertion | O(1) | 1 | No | Keeps order (LRU cache possible) |
| `TreeMap` | Sorted by key | O(log n) | No | No | Navigation methods |
| `Hashtable` | None | O(1) | No | Yes (slow) | Legacy |
| `ConcurrentHashMap` | None | O(1) | No | Yes (fast) | Concurrency |
| `Properties` | None | O(1) | No | Yes (inherited) | String config, file I/O |
| `WeakHashMap` | None | O(1) | 1 | No | Keys can be GC'd |
| `IdentityHashMap` | None | O(1) | 1 | No | `==` comparison |
| `EnumMap` | Enum order | O(1) | No | No | Array-based, fastest for enums |

---

## 8. Which One Should I Use?

```
Need key -> value pairs?
 |
 +-- NO  (just unique items) -> SET
 |        |
 |        +-- Don't care about order ........ HashSet
 |        +-- Keep insertion order .......... LinkedHashSet
 |        +-- Need sorted / range queries ... TreeSet
 |
 +-- YES -> MAP
          |
          +-- Multiple threads? ........ ConcurrentHashMap
          +-- Keys are enums? .......... EnumMap
          +-- Need sorted keys? ........ TreeMap
          +-- Need insertion order? .... LinkedHashMap
          +-- Config (String/String)? .. Properties
          +-- Auto-clean cache by key? . WeakHashMap
          +-- Compare keys by reference? IdentityHashMap
          +-- Otherwise (default) ...... HashMap
```

---

## 9. Common Mistakes

1. **Forgetting `equals()` + `hashCode()`** on custom classes used in `HashSet` / `HashMap` keys.
2. **Mutating a key** after inserting it in a hash-based collection. Its hash changes and you can never find it again.
3. **Putting `null`** in `TreeSet`, `TreeMap`, `Hashtable`, `ConcurrentHashMap`. This throws `NullPointerException`.
4. **Expecting order from `HashSet`/`HashMap`.** There is no guarantee.
5. **Modifying a collection while looping over it** causes `ConcurrentModificationException`. Use `Iterator.remove()` or `removeIf()`.
6. **Non-comparable objects in `TreeSet`/`TreeMap`** causes `ClassCastException`.
7. **Check-then-act on a `ConcurrentHashMap`** (`containsKey` then `put`). Use `putIfAbsent`/`merge`.
8. **Confusing `Comparator` equality with `equals()`** in `TreeSet` (items that compare as 0 are treated as duplicates).

---

## 10. Interview Questions

**Q1. How is `HashSet` implemented internally?**
It is backed by a `HashMap`. Elements are keys; a shared dummy object `PRESENT` is the value.

**Q2. Difference between `HashSet`, `LinkedHashSet`, and `TreeSet`?**
Ordering (none / insertion / sorted), data structure (hash table / hash table + linked list / Red-Black tree), speed (O(1) / O(1) / O(log n)).

**Q3. `floor()` vs `lower()` vs `ceiling()` vs `higher()`?**
`lower` is `<`, `floor` is `<=`, `ceiling` is `>=`, `higher` is `>`.

**Q4. Why is `Map` not part of the `Collection` interface?**
`Collection` holds single elements; `Map` holds key-value pairs, so the contracts (`add(e)`, etc.) don't fit.

**Q5. `keySet()` vs `values()` vs `entrySet()` return types?**
`Set<K>`, `Collection<V>`, `Set<Map.Entry<K,V>>`.

**Q6. `HashMap` vs `Hashtable` vs `ConcurrentHashMap`?**
Not thread-safe vs whole-table lock vs fine-grained locking/CAS. `Hashtable` and `ConcurrentHashMap` reject nulls.

**Q7. Why is `ConcurrentHashMap` faster than `Hashtable`?**
It doesn't lock the entire map. It locks only the bucket being modified (and uses CAS), so many threads work in parallel.

**Q8. What is `WeakHashMap` used for?**
Caches/metadata keyed by objects, where entries should disappear automatically when the key is no longer referenced elsewhere.

**Q9. `IdentityHashMap` vs `HashMap`?**
`IdentityHashMap` compares keys with `==` (reference), `HashMap` uses `equals()` + `hashCode()`.

**Q10. Why is `EnumMap` faster than `HashMap`?**
It is an array indexed by the enum's ordinal. No hashing, no collisions, less memory, and it iterates in enum order.

**Q11. What is `Properties` and how does it differ from a `HashMap`?**
A `Hashtable` subclass for String key/value config data, with `load()`/`store()` for `.properties` files and `getProperty()` with default values.

**Q12. What does "self-balancing" mean in `TreeSet`?**
After inserts/deletes, the tree rotates/recolors nodes (Red-Black rules) so its height stays about log n, guaranteeing O(log n) operations.

---

## Quick Revision Cheat Sheet

```
SET   : no duplicates
  HashSet        -> fast, unordered          (HashMap inside)
  LinkedHashSet  -> fast, insertion order    (+ linked list)
  TreeSet        -> sorted, O(log n)         (Red-Black tree)
                    first/last/headSet/tailSet/subSet
                    lower(<) floor(<=) ceiling(>=) higher(>)

MAP   : key -> value, unique keys
  ops   : put get containsKey containsValue size isEmpty remove
  views : keySet()=Set  values()=Collection  entrySet()=Set<Entry>
  utils : getOrDefault, putIfAbsent (merge, compute)

  TreeMap            -> sorted keys, firstKey/lastKey/descendingMap
  Hashtable          -> legacy, whole-table lock, no nulls
  ConcurrentHashMap  -> modern thread-safe, bucket-level locking, no nulls
  Properties         -> String config, load/store files
  WeakHashMap        -> weak keys, GC auto-removes entries
  IdentityHashMap    -> == instead of equals()
  EnumMap            -> array by ordinal, fastest for enum keys
```
