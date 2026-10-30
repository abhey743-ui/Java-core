# Data Structures and the Java Collection Framework

> Why plain arrays are not enough, how the main data structures work internally (dynamic array, linked list, set, stack, queue, map, BST), and how Java organizes them under one Collection Framework using interfaces and polymorphism.

---

## Table of Contents

1. [Why do we need data structures?](#1-why-do-we-need-data-structures)
2. [Arrays and their limits](#2-arrays-and-their-limits)
3. [Big-O in 60 seconds](#3-big-o-in-60-seconds)
4. [Dynamic arrays (ArrayList internals)](#4-dynamic-arrays-arraylist-internals)
5. [Linked lists](#5-linked-lists)
6. [Sets and hashing](#6-sets-and-hashing)
7. [Stacks and queues](#7-stacks-and-queues)
8. [Maps (key-value storage)](#8-maps-key-value-storage)
9. [Trees and Binary Search Trees](#9-trees-and-binary-search-trees)
10. [The Java Collection Framework](#10-the-java-collection-framework)
11. [Polymorphism in the framework](#11-polymorphism-in-the-framework)
12. [Why Map is separate from Collection](#12-why-map-is-separate-from-collection)
13. [Complexity comparison table](#13-complexity-comparison-table)
14. [Which one should I use?](#14-which-one-should-i-use)
15. [Interview questions and answers](#15-interview-questions-and-answers)
16. [Cheat sheet](#16-cheat-sheet)

---

## 1. Why do we need data structures?

A **data structure** is a way of **organizing and storing data** so that the operations you care about (add, remove, search, sort, find min/max, check "does it exist?") are **efficient**.

```
 Raw data  ----->  [ Data structure ]  ----->  fast operations
 (millions of      (arrangement of data         (insert, delete,
  records)          + rules to access it)        search, iterate)
```

Real applications constantly ask questions like:

- Is this username already taken?
- Give me the user with ID 4821.
- What was the last action (undo)?
- Who is next in line (print queue)?
- Show everything in sorted order.

The **right structure** makes each of these fast. The **wrong one** makes your app crawl as data grows.

---

## 2. Arrays and their limits

An array is a **contiguous block of memory** holding elements of the same type.

```java
int[] a = new int[5];
```

```
 index:     0     1     2     3     4
          +-----+-----+-----+-----+-----+
 a ----->  |  10 |  20 |  30 |  40 |  50 |      contiguous memory
          +-----+-----+-----+-----+-----+
 address:  100   104   108   112   116          (int = 4 bytes)

 address of a[i] = base + i * elementSize     -> instant jump: O(1)
```

### What arrays are good at

| Operation | Cost | Why |
|---|---|---|
| Access `a[i]` | **O(1)** | address is computed directly |
| Insert at the end (if there is space) | **O(1)** | write to the next free slot |

### Where arrays struggle

| Problem | Explanation |
|---|---|
| **Search / query** | To find a value (not an index), you may check every element: **O(n)** |
| **Fixed size** | Size is set at creation and cannot grow |
| **Insert/delete in the middle** | Must shift elements: **O(n)** |
| **Wasted or insufficient space** | Allocating too much wastes memory; too little crashes the plan |

```
Searching for 40 in an unsorted array:

 [10][20][30][40][50]
   ^   ^   ^   ^
   1   2   3   4   comparisons -> found.   Worst case: check all n elements -> O(n)
```

```
Inserting 25 at index 2 (shifting needed):

 before:  [10][20][30][40][50][  ]
                    \   \   \
                     v   v   v   shift right
 after:   [10][20][25][30][40][50]       -> O(n)
```

So: **insertion at the end is fast, but searching and querying are slow** for large data. This gap is the reason data structures exist.

---

## 3. Big-O in 60 seconds

Big-O describes how the **work grows as the input size `n` grows**.

| Notation | Name | Example | 1,000,000 items |
|---|---|---|---|
| O(1) | constant | array access, hash lookup | 1 step |
| O(log n) | logarithmic | binary search, balanced tree | ~20 steps |
| O(n) | linear | scan an unsorted array | 1,000,000 steps |
| O(n log n) | linearithmic | good sorting | ~20,000,000 |
| O(n^2) | quadratic | nested loops | 1,000,000,000,000 |

```
work
 ^                                       O(n^2)
 |                                    /
 |                                 /      O(n)
 |                             /      /
 |                         /     /
 |                    /     /       ____----  O(log n)
 |              /  /    __---
 |  ------- O(1) --------------------------
 +-------------------------------------------> n
```

Also remember **average vs worst case**: hash lookups are O(1) *on average* but can degrade; **amortized** O(1) means "occasionally expensive, cheap on average" (like dynamic array growth).

---

## 4. Dynamic arrays (ArrayList internals)

A **dynamic array** hides a normal array and **resizes automatically**.

### 4.1 The idea

When the internal array is **full**:

1. Create a **new, larger** array.
2. **Copy** all existing elements into it.
3. Add the new element.
4. Discard the old array (garbage collected).

```
Capacity 4, full:                    add(50)  -> no room!

 [10][20][30][40]                    STEP 1: allocate bigger array
                                     [  ][  ][  ][  ][  ][  ]   (capacity 6)
                                     STEP 2: copy old elements
                                     [10][20][30][40][  ][  ]
                                     STEP 3: place new element
                                     [10][20][30][40][50][  ]
                                     STEP 4: old array -> garbage
```

### 4.2 What Java's `ArrayList` really does

```java
List<Integer> list = new ArrayList<>();
```

- Internally: `Object[] elementData` and an `int size`.
- A no-arg `ArrayList` starts with an **empty array**; the first `add` allocates capacity **10**.
- When full, new capacity = **old + old/2** (about **1.5x** growth).

```
capacity:  10  ->  15  ->  22  ->  33  ->  49 ...
          (each step: old + (old >> 1))
```

```
 STACK            HEAP
 +------+      +--------------------------+
 | list |----->| ArrayList object         |
 +------+      |   size = 3               |
               |   elementData ---+       |
               +------------------|-------+
                                  v
               +------------------------------------------+
               | Object[10]                               |
               | [ref][ref][ref][ ][ ][ ][ ][ ][ ][ ]     |
               |  \____used____/ \______spare_______/     |
               +------------------------------------------+
```

(Elements of an `ArrayList<Integer>` are references to `Integer` objects, not raw ints.)

### 4.3 Why adding is still cheap: amortized O(1)

Most `add` calls just write into a free slot (O(1)). Once in a while a resize copies everything (O(n)). Because capacity grows **geometrically**, resizes become rarer and rarer, so the average cost per add is **O(1) amortized**.

### 4.4 Costs

| Operation | ArrayList |
|---|---|
| `get(i)` | O(1) |
| `add(e)` at end | O(1) amortized |
| `add(i, e)` / `remove(i)` in middle | **O(n)** (shifting) |
| `contains(x)` / `indexOf(x)` | **O(n)** |

Tip: if you know the size, use `new ArrayList<>(expectedSize)` to avoid repeated resizing.

---

## 5. Linked lists

### 5.1 The idea

A linked list stores each element in a separate **node**. Each node holds the **data** and **reference(s) to neighbours**. Nodes can live anywhere in memory.

```
Singly linked list:

 head
  |
  v
 +------+---+     +------+---+     +------+------+
 |  10  | o-+---->|  20  | o-+---->|  30  | null |
 +------+---+     +------+---+     +------+------+
  data   next       data   next      data   next
```

Java's `LinkedList` is a **doubly** linked list (each node knows `prev` and `next`):

```
 null <---+------+------+     +------+------+     +------+------+---> null
          | prev | 10   |<--->| prev | 20   |<--->| prev | 30   |
          +------+------+     +------+------+     +------+------+
          ^                                                      ^
         first                                                  last
```

### 5.2 Insertion/deletion without shifting

Compare inserting **25 between 20 and 30**:

```
ARRAY (shift required):          LINKED LIST (just re-link):

[10][20][30][40]                  10 -> 20 -> 30 -> 40
        |  shift                           \
        v                                   25   (new node)
[10][20][25][30][40]
                                  20 -> 25 -> 30   only two pointers change
```

```
 before:   20 ----------> 30
 step 1:   25.next = 30
 step 2:   20.next = 25
 after:    20 ---> 25 ---> 30      O(1) once you are AT the position
```

### 5.3 The catch

To get to the middle you must **walk from the head/tail**, node by node.

| Operation | ArrayList | LinkedList |
|---|---|---|
| `get(i)` | **O(1)** | O(n) (walk) |
| add/remove at **ends** | O(1) end / O(n) front | **O(1)** both ends |
| add/remove **at a known node** (via iterator) | O(n) | **O(1)** |
| add/remove by **index** in middle | O(n) | O(n) (finding it) |
| Memory per element | low | higher (two extra references + object header per node) |

So linked lists are "ideal for frequent insertions/deletions" **when you already hold the position** (iterator, ends). In real-world Java, `ArrayList` is usually faster even for many middle operations because contiguous memory is CPU-cache friendly. Default to `ArrayList` unless you specifically need queue/deque behavior or constant-time inserts at a held position.

`LinkedList` implements both `List` and `Deque`.

---

## 6. Sets and hashing

### 6.1 What is a Set?

A **Set** stores **unique elements** (no duplicates). Its main question: *"Is X in here?"*

```java
Set<String> s = new HashSet<>();
s.add("apple");   // true  (added)
s.add("apple");   // false (already present, ignored)
s.contains("apple");   // true, in O(1) on average
```

### 6.2 How hashing gives O(1) lookup

A **hash function** converts an element into an integer (`hashCode()`), which is mapped to a **bucket index** in an internal array.

```
element "apple"
    |
    v  hashCode()
 93029210
    |
    v  index = hash & (tableSize - 1)       (spread + mask)
   index 6
    |
    v
 +---+---+---+---+---+---+---------+---+---+---+
 | 0 | 1 | 2 | 3 | 4 | 5 | "apple" | 7 | 8 | 9 |   table of buckets
 +---+---+---+---+---+---+---------+---+---+---+
```

- **Add / contains / remove** = compute hash, jump to bucket, check. **No scanning of everything**, hence **O(1) average**.

### 6.3 Collisions

Two different elements can land in the **same bucket**. The bucket then holds a **chain** of entries, compared using `equals()`.

```
 index 3:  [ "cat" ] -> [ "act" ] -> [ "tac" ]       (collision chain)

 contains("act"): go to bucket 3, walk chain, equals() each -> found
```

In `HashMap`/`HashSet`, long chains (8 or more entries in a bucket, with a large enough table) are converted into a **balanced tree** to keep worst-case lookups at O(log n).

### 6.4 The `equals()` / `hashCode()` contract

For your own classes used in hash-based collections:

```
if a.equals(b)  then  a.hashCode() == b.hashCode()   (MUST hold)
```

Break it and you get duplicates in a Set or failed lookups.

### 6.5 Set implementations

| Class | Order | Lookup | Backed by |
|---|---|---|---|
| `HashSet` | none (unpredictable) | O(1) avg | HashMap |
| `LinkedHashSet` | insertion order | O(1) avg | hash table + linked list |
| `TreeSet` | **sorted** | O(log n) | Red-Black tree |

`HashSet` is internally a `HashMap` where your element is the key and a dummy constant is the value.

---

## 7. Stacks and queues

### 7.1 Stack: LIFO (Last-In, First-Out)

The most recently added item leaves first, like a stack of plates.

```
   push(10)   push(20)   push(30)      pop() -> 30
                                       
                         |  30  | <- top
              |  20  |   |  20  |      after pop:   |  20  | <- top
   |  10  |   |  10  |   |  10  |                   |  10  |
   +------+   +------+   +------+                   +------+
```

Operations (all O(1)): `push`, `pop`, `peek` (look at the top without removing).

Real-world uses:
- **Undo/redo** in editors
- **Browser back button**
- **Method call stack** (JVM)
- Expression evaluation, matching brackets `({[]})`
- DFS (depth-first search)

Java note: the legacy `Stack` class extends `Vector` (synchronized, outdated). **Prefer `Deque<Integer> stack = new ArrayDeque<>();`** with `push/pop/peek`.

### 7.2 Queue: FIFO (First-In, First-Out)

The oldest item leaves first, like a line at a ticket counter.

```
   enqueue (offer)                                dequeue (poll)
        |                                              ^
        v                                              |
     +-----+-----+-----+-----+                    
 --> |  40 |  30 |  20 |  10 | -->     rear <-------- front
     +-----+-----+-----+-----+
      rear                front
```

Operations (all O(1) for good implementations): `offer/add` (enqueue at rear), `poll/remove` (dequeue from front), `peek/element` (look at the front).

| Purpose | Throws exception if it fails | Returns special value |
|---|---|---|
| Insert | `add(e)` | `offer(e)` (false if full) |
| Remove | `remove()` | `poll()` (null if empty) |
| Examine | `element()` | `peek()` (null if empty) |

Real-world uses:
- **Print queue**, task scheduling, request handling
- Message queues
- **BFS** (breadth-first search)
- Buffers (streaming, I/O)

### 7.3 Queue variants in Java

| Class | Behavior |
|---|---|
| `LinkedList` | queue (and list, deque) |
| `ArrayDeque` | fast resizable-array deque (stack or queue) |
| `PriorityQueue` | **not FIFO**: always serves the smallest (or by comparator) element first (heap-based; `poll` is O(log n)) |
| `Deque` | double-ended: add/remove at **both** ends |

```
Stack (LIFO)        Queue (FIFO)           Deque (both ends)
 push/pop           offer -> poll          addFirst/addLast
 same end           opposite ends          pollFirst/pollLast
```

---

## 8. Maps (key-value storage)

A **Map** stores **pairs**: a unique **key** mapped to a **value**. You look up by key.

```java
Map<String, Integer> ages = new HashMap<>();
ages.put("Asha", 25);
ages.put("Ravi", 30);
ages.get("Asha");           // 25   (O(1) average)
ages.containsKey("Ravi");   // true
ages.put("Asha", 26);       // key exists -> value REPLACED (keys stay unique)
```

```
  KEY        VALUE
 +--------+---------+
 | "Asha" |   26    |
 | "Ravi" |   30    |
 +--------+---------+
 keys unique, values may repeat
```

### 8.1 How `HashMap` finds a value in constant time

```
 get("Asha")
    |
    1) hash = hashCode("Asha") (then spread bits: h ^ (h >>> 16))
    2) index = hash & (capacity - 1)
    3) go to bucket[index]
    4) compare keys with equals() along the chain
    5) found -> return its VALUE
```

```
 table (capacity 16)
 +----+
 | 0  | -> null
 | 1  | -> null
 | 2  | -> [ "Ravi"=30 ]
 | 3  | -> null
 | .. |
 | 9  | -> [ "Asha"=26 ] -> [ "Meera"=22 ]    (collision chain)
 | .. |
 +----+
```

Defaults worth knowing:
- Initial capacity **16**, **load factor 0.75**.
- When `size > capacity * 0.75`, the table **doubles** and entries are **rehashed**.
- A bucket with 8+ entries becomes a tree (if table is large enough); this protects against bad hash distributions.
- `HashMap` permits **one `null` key** and many `null` values; `Hashtable` and `ConcurrentHashMap` permit none.

### 8.2 Map implementations

| Class | Order | Operations | Notes |
|---|---|---|---|
| `HashMap` | none | O(1) avg | the default choice |
| `LinkedHashMap` | insertion (or access) order | O(1) avg | good for LRU caches |
| `TreeMap` | **sorted by key** | O(log n) | Red-Black tree |
| `Hashtable` | none | O(1) | legacy, synchronized |
| `ConcurrentHashMap` | none | O(1) avg | thread-safe, scalable |

### 8.3 Maps vs Sets

A `HashSet` is a `HashMap` that only cares about keys. If you understand Maps, you understand Sets.

---

## 9. Trees and Binary Search Trees

### 9.1 Trees: hierarchical data

A **tree** is made of **nodes** connected by edges: one **root**, each node may have **children**, nodes without children are **leaves**.

```
            [ root ]
            /      \
        [child]   [child]
         /   \         \
     [leaf] [leaf]    [leaf]
```

Real examples: file systems, HTML DOM, organization charts, database indexes.

### 9.2 Binary Search Tree (BST)

A **binary tree** gives each node at most **two** children (left and right). A **BST** adds an ordering rule:

```
For every node:   all values in LEFT subtree  <  node  <  all values in RIGHT subtree
```

Insert 50, 30, 70, 20, 40, 60, 80:

```
              50
            /    \
          30      70
         /  \    /  \
       20   40  60   80
```

### 9.3 Searching (halve the problem each step)

Search for 60:

```
 at 50: 60 > 50  -> go RIGHT
 at 70: 60 < 70  -> go LEFT
 at 60: found!      (3 steps instead of scanning 7 elements)

              50  <-- start
                \
                 70
                /
              60  <-- found
```

Each comparison discards about half the remaining tree, so a **balanced** BST gives **O(log n)** search, insert, and delete.

### 9.4 Insert example

Insert 65:

```
 65 > 50 -> right
 65 < 70 -> left
 65 > 60 -> right of 60 (empty) -> place here

              50
            /    \
          30      70
         /  \    /  \
       20   40  60   80
                  \
                   65
```

### 9.5 Traversals

| Traversal | Order | On the tree above |
|---|---|---|
| **In-order** | left, node, right | 20 30 40 50 60 70 80 (**sorted!**) |
| Pre-order | node, left, right | 50 30 20 40 70 60 80 |
| Post-order | left, right, node | 20 40 30 60 80 70 50 |
| Level-order (BFS) | level by level | 50 30 70 20 40 60 80 |

### 9.6 The worst case: an unbalanced (skewed) BST

Inserting already-sorted data 10, 20, 30, 40:

```
 10
   \
    20
      \
       30
         \
          40        -> behaves like a linked list: O(n) search
```

**Self-balancing trees** (Red-Black, AVL) rotate nodes automatically to stay balanced and guarantee **O(log n)**.

Java uses a **Red-Black tree** inside `TreeMap` and `TreeSet` (and for treeified `HashMap` buckets).

```java
TreeSet<Integer> t = new TreeSet<>(List.of(50, 30, 70, 20));
System.out.println(t);       // [20, 30, 50, 70]  always sorted
t.first();                   // 20
t.ceiling(40);               // 50  (smallest element >= 40)
```

---

## 10. The Java Collection Framework

### 10.1 The idea

Without a framework, every structure would be an independent class with its own method names (`insert()` here, `addElement()` there, `push()` elsewhere). Java instead organizes them under **one unified architecture** in `java.util`:

1. **Interfaces**: the contracts (`List`, `Set`, `Queue`, `Map`...)
2. **Implementations**: concrete classes (`ArrayList`, `HashSet`, `LinkedList`...)
3. **Algorithms**: utility methods in the `Collections` class (`sort`, `reverse`, `shuffle`, `max`, `binarySearch`...)

### 10.2 The hierarchy

```
                          Iterable<E>              (java.lang: enables for-each loops)
                              |
                         Collection<E>
          +-------------------+---------------------+
          |                   |                     |
        List<E>             Set<E>               Queue<E>
          |                   |                     |
   +------+------+     +------+------+         +----+--------+
   |      |      |     |      |      |         |             |
ArrayList Linked- Vector HashSet  SortedSet   Linked-     Deque<E>
          List    |        |         |        List         |     \
                  Stack  Linked-  NavigableSet  PriorityQueue   ArrayDeque
                         HashSet     |
                                  TreeSet


   Map<K,V>      <-- SEPARATE hierarchy (does NOT extend Collection)
      |
   +--+------------+-------------+
   |               |             |
 HashMap        SortedMap     Hashtable
   |               |
 LinkedHashMap  NavigableMap      ConcurrentHashMap (implements ConcurrentMap)
                   |
                TreeMap
```

(Interfaces: `Iterable`, `Collection`, `List`, `Set`, `Queue`, `Deque`, `Map`, `SortedSet`, `NavigableSet`, `SortedMap`, `NavigableMap`. The rest are classes.)

### 10.3 What each interface promises

| Interface | Meaning | Key traits |
|---|---|---|
| `Collection` | a group of objects | `add`, `remove`, `contains`, `size`, `isEmpty`, `iterator`, `clear` |
| `List` | **ordered**, indexed sequence | duplicates allowed, `get(i)`, `set(i, e)`, `add(i, e)` |
| `Set` | **unique** elements | no duplicates; ordering depends on the implementation |
| `Queue` | elements **waiting for processing** | typically FIFO: `offer`, `poll`, `peek` |
| `Deque` | double-ended queue | stack **and** queue operations |
| `Map` | **key to value** pairs | unique keys, `put`, `get`, `containsKey` |

### 10.4 Iterating any collection

Because every `Collection` is `Iterable`:

```java
for (String s : collection) { ... }           // for-each

Iterator<String> it = collection.iterator();  // explicit iterator
while (it.hasNext()) {
    String s = it.next();
    if (s.isEmpty()) it.remove();             // safe removal during iteration
}
```

Removing inside a for-each loop with `collection.remove(...)` throws `ConcurrentModificationException`; use the iterator's `remove()` or `removeIf(...)`.

### 10.5 `Collection` vs `Collections`

| | `Collection` | `Collections` |
|---|---|---|
| Kind | **interface** (root of the hierarchy) | **utility class** (static helper methods) |
| Example | `Collection<String> c` | `Collections.sort(list)` |

### 10.6 Generics everywhere

All collections are generic (`List<E>`, `Map<K,V>`), giving compile-time type safety (see the Generics notes): `List<String>` can't accidentally hold an `Integer`, and `get()` needs no cast. Collections hold **objects**, so primitives are auto-boxed (`List<Integer>`, not `List<int>`).

---

## 11. Polymorphism in the framework

The big payoff of this hierarchy: **program to the interface, not the implementation.**

### 11.1 Swapping implementations freely

```java
List<String> names = new ArrayList<>();     // declared as the INTERFACE type

names.add("Asha");
names.add("Ravi");
```

Need different behavior later? Change **one word**:

```java
List<String> names = new LinkedList<>();    // everything else compiles unchanged
```

```
        List<String> names
               |
        points to ANY object that IS-A List
         /          |          \
   ArrayList    LinkedList    Vector       (each with different internals,
                                            same List methods)
```

### 11.2 Writing reusable methods

One method serves every implementation:

```java
static void printAll(Collection<String> items) {
    for (String s : items) System.out.println(s);
}

printAll(new ArrayList<>(List.of("a", "b")));      // List
printAll(new HashSet<>(Set.of("x", "y")));         // Set
printAll(new ArrayDeque<>(List.of("p", "q")));     // Queue/Deque
```

```
                 printAll(Collection<String>)
                 /         |            \
            ArrayList   HashSet      ArrayDeque     all ARE Collections
```

Because of **runtime polymorphism**, the call `items.add(x)` runs the **actual object's** implementation (ArrayList's version, HashSet's version, ...).

### 11.3 Compare with the pre-framework world

```
WITHOUT a framework:               WITH the Collection Framework:
 ArrayX.insert(e)                   c.add(e)       <- same on every collection
 ListY.addElement(e)                c.remove(e)
 SetZ.put(e)                        c.contains(e)
 (every class different)            c.size()
```

Benefits:
- **Consistency**: learn once, use everywhere.
- **Reusability**: algorithms like `Collections.sort(List<T>)` work for any list.
- **Flexibility**: change implementation without touching callers.
- **Interoperability**: collections convert easily: `new ArrayList<>(someSet)`, `new HashSet<>(someList)` (a classic way to **remove duplicates**).

### 11.4 Rule of thumb for declarations

```java
List<String>        a = new ArrayList<>();     // good
Map<String,Integer> m = new HashMap<>();       // good
ArrayList<String>   b = new ArrayList<>();     // avoid unless you need ArrayList-only methods
```

---

## 12. Why Map is separate from Collection

`Map` is part of the **Collections Framework** (same package, same docs, same chapter), but it does **not extend `Collection`**.

Reasons:

1. **Different shape of data.** A `Collection` holds **single elements**; a `Map` holds **key-value pairs**.
2. **The methods don't fit.** `Collection.add(E e)` takes one element. A map needs `put(K key, V value)`, which takes two.
3. **Different contract.** Map's identity is the **key** (unique), and `get(key)` is its main operation, which has no equivalent in `Collection`.

```
 Collection<E>:  add(E)                    Map<K,V>:  put(K, V)
                 [ e1 ][ e2 ][ e3 ]                   [k1 -> v1][k2 -> v2]
```

### The bridge: collection views

A Map gives you **Collection views** of its contents so you can still use all the collection tools:

```java
Map<String, Integer> m = new HashMap<>();
m.put("a", 1); m.put("b", 2);

Set<String>              keys    = m.keySet();     // all keys (a Set, since keys are unique)
Collection<Integer>      values  = m.values();     // all values (may contain duplicates)
Set<Map.Entry<String,Integer>> entries = m.entrySet();   // all key-value pairs

for (Map.Entry<String, Integer> e : m.entrySet()) {
    System.out.println(e.getKey() + " = " + e.getValue());
}
```

These are **views** (live windows into the map), not copies.

---

## 13. Complexity comparison table

(n = number of elements; "avg" = average case)

| Structure | Access by index | Search by value | Insert | Delete | Ordered? |
|---|---|---|---|---|---|
| **Array** | O(1) | O(n) | O(n) (middle) | O(n) | by index |
| **ArrayList** | O(1) | O(n) | O(1) amort. end / O(n) middle | O(n) | insertion |
| **LinkedList** | O(n) | O(n) | O(1) at ends / at held node | O(1) at ends / held node | insertion |
| **HashSet** | n/a | **O(1) avg** | O(1) avg | O(1) avg | no |
| **LinkedHashSet** | n/a | O(1) avg | O(1) avg | O(1) avg | insertion |
| **TreeSet** | n/a | O(log n) | O(log n) | O(log n) | **sorted** |
| **HashMap** | n/a | **O(1) avg** (by key) | O(1) avg | O(1) avg | no |
| **TreeMap** | n/a | O(log n) | O(log n) | O(log n) | **sorted by key** |
| **ArrayDeque (stack/queue)** | n/a | O(n) | O(1) at ends | O(1) at ends | insertion |
| **PriorityQueue** | n/a | O(n) | O(log n) | O(log n) (poll) | heap order (min first) |

---

## 14. Which one should I use?

```
 Need key -> value lookup?
      YES -> Map
              |- fastest, order irrelevant ........... HashMap
              |- keep insertion order ................ LinkedHashMap
              |- sorted by key ....................... TreeMap
              '- multi-threaded ...................... ConcurrentHashMap

 NO -> Need uniqueness (no duplicates)?
      YES -> Set
              |- fastest ............................. HashSet
              |- keep insertion order ................ LinkedHashSet
              '- sorted ............................... TreeSet

 NO -> Need processing order?
      |- LIFO (undo, backtracking) ................... ArrayDeque (as stack)
      |- FIFO (waiting line) ......................... ArrayDeque / LinkedList (as queue)
      |- by priority ................................. PriorityQueue
      '- ordered list with index access .............. ArrayList  (default)
                                                       LinkedList (only if you need deque/list inserts at held position)
```

---

## 15. Interview questions and answers

**Q1. Why do we need data structures when we have arrays?**
Arrays give O(1) index access and fast end insertion, but have fixed size and O(n) search/insert/delete. Other structures optimize different operations (hash lookup, ordering, uniqueness, LIFO/FIFO).

**Q2. How does a dynamic array grow?**
When full, it allocates a larger array, copies the old elements, and adds the new one. `ArrayList` grows by about 1.5x, giving amortized O(1) appends.

**Q3. What is the default capacity of `ArrayList`?**
10, allocated lazily on the first `add` (a new empty `ArrayList` starts with an empty shared array).

**Q4. ArrayList vs LinkedList?**
ArrayList: O(1) random access, contiguous memory, O(n) middle insertion. LinkedList: O(n) access, O(1) insertion/removal at ends or a held position, more memory per element. ArrayList is the default choice.

**Q5. How does a HashSet guarantee uniqueness?**
It uses `hashCode()` to find the bucket and `equals()` to compare within it; if an equal element exists, the add is ignored. It is backed by a `HashMap`.

**Q6. What happens if two keys have the same hash?**
A collision: both go in the same bucket (a chain, converted to a tree when it gets long) and are told apart with `equals()`.

**Q7. State the `equals`/`hashCode` contract.**
Equal objects must have equal hash codes. Unequal objects may share a hash code, but good distribution keeps lookups fast. Override both together.

**Q8. Stack vs Queue?**
Stack is LIFO (`push`/`pop`/`peek`), queue is FIFO (`offer`/`poll`/`peek`). In Java prefer `ArrayDeque` over the legacy `Stack` class.

**Q9. What is a BST and what is its complexity?**
A binary tree where left < node < right. Balanced: O(log n) search/insert/delete. Degenerate (sorted input, skewed): O(n). Java's `TreeMap`/`TreeSet` use self-balancing Red-Black trees.

**Q10. What does in-order traversal of a BST give?**
The elements in sorted order.

**Q11. Draw the Collection hierarchy.**
`Iterable` -> `Collection` -> `List`, `Set`, `Queue` (with `Deque` under `Queue`, `SortedSet`/`NavigableSet` under `Set`). `Map` is a separate hierarchy.

**Q12. Why is `Map` not a `Collection`?**
It stores key-value pairs, so `add(E)` doesn't fit its `put(K, V)` contract. It exposes `keySet()`, `values()`, `entrySet()` as Collection views.

**Q13. What is `Collection` vs `Collections`?**
`Collection` is the root interface; `Collections` is a utility class with static methods like `sort`, `reverse`, `shuffle`, `unmodifiableList`.

**Q14. Why declare `List<String> x = new ArrayList<>()` rather than `ArrayList<String> x`?**
Programming to the interface lets you swap implementations without changing the rest of the code and lets methods accept any `List`/`Collection` (polymorphism).

**Q15. How do you remove duplicates from a list?**
`new ArrayList<>(new LinkedHashSet<>(list))` (keeps order) or `new HashSet<>(list)` (no order).

**Q16. HashMap vs TreeMap?**
HashMap: unordered, O(1) average. TreeMap: sorted by key, O(log n), supports navigation methods (`firstKey`, `floorKey`, `headMap`...).

**Q17. Why does removing inside a for-each loop throw `ConcurrentModificationException`?**
The iterator is fail-fast: structural modification outside the iterator changes `modCount`, which the iterator detects. Use `Iterator.remove()` or `removeIf`.

---

## 16. Cheat sheet

```
WHY DS?       Array: O(1) access, O(1) end insert, but O(n) search, fixed size

DYNAMIC ARRAY Full -> allocate bigger -> copy -> add.   ArrayList: cap 10, grows x1.5
LINKED LIST   nodes + pointers (Java: doubly). Insert at held node O(1), get(i) O(n)
SET           unique elements. HashSet O(1) avg | LinkedHashSet order | TreeSet sorted
HASHING       hashCode() -> index -> bucket -> equals(). Collisions: chain -> tree
STACK         LIFO  push/pop/peek     (use ArrayDeque)
QUEUE         FIFO  offer/poll/peek   (ArrayDeque, LinkedList, PriorityQueue by priority)
MAP           key -> value. HashMap O(1) | LinkedHashMap order | TreeMap sorted
              default capacity 16, load factor 0.75, doubles when exceeded
BST           left < node < right. balanced O(log n), skewed O(n). in-order = sorted
              Java: Red-Black trees in TreeMap/TreeSet

FRAMEWORK     Iterable > Collection > List | Set | Queue(> Deque)
              Map = separate hierarchy (key-value; not a Collection)
              Collections = utility class

POLYMORPHISM  List<String> x = new ArrayList<>();   swap impl freely
              void f(Collection<?> c)               accepts any collection

MAP VIEWS     keySet() | values() | entrySet()

DEFAULT PICKS ArrayList, HashMap, HashSet, ArrayDeque
```

---

*End of notes.*
