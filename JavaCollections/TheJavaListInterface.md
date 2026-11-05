# Java `Set` and `Map` Internals: HashMap, HashSet, LinkedHashMap, TreeMap and TreeSet

> This guide follows the lecture outline you shared (Set and Map basics, buckets and hashing, collision chaining, node structure, load factor and rehashing, treeification, `LinkedHashMap`/`LinkedHashSet`, `TreeMap`/`TreeSet`) and explains the **internal architecture the way a senior engineer would**: not "what the method does", but **why the data structure is built this way and what it costs**.
> Every big idea starts from **first principles**: *what is the problem, what must a solution guarantee, and which design falls out of that?* Where I give numbers (bucket indexes, hash codes, resize points), I worked them out by hand so you can check them yourself.

---

## Table of Contents

1. [Roadmap and How to Read This Guide](#1-roadmap-and-how-to-read-this-guide)
2. [Set and Map Basics](#2-set-and-map-basics)
3. [Deriving a Hash Table From First Principles](#3-deriving-a-hash-table-from-first-principles)
4. [`HashMap` Architecture: The Real Structure](#4-hashmap-architecture-the-real-structure)
5. [The Journey of a Key: Hash, Spread, Index](#5-the-journey-of-a-key-hash-spread-index)
6. [`put`, `get` and `remove`: Step by Step](#6-put-get-and-remove-step-by-step)
7. [Collisions and Chaining](#7-collisions-and-chaining)
8. [Load Factor, Capacity and Rehashing](#8-load-factor-capacity-and-rehashing)
9. [Treeification: Chains Become Red-Black Trees (Java 8+)](#9-treeification-chains-become-red-black-trees-java-8)
10. [`HashSet`: A `HashMap` in Disguise](#10-hashset-a-hashmap-in-disguise)
11. [Iteration Order, `null`s and the `equals`/`hashCode` Contract](#11-iteration-order-nulls-and-the-equalshashcode-contract)
12. [`LinkedHashMap` and `LinkedHashSet`](#12-linkedhashmap-and-linkedhashset)
13. [From Hashing to Trees: Why Red-Black Trees?](#13-from-hashing-to-trees-why-red-black-trees)
14. [`TreeMap` and `TreeSet` Internals](#14-treemap-and-treeset-internals)
15. [Choosing the Right Implementation](#15-choosing-the-right-implementation)
16. [The `Map` API in Depth](#16-the-map-api-in-depth)
17. [Thread Safety and Other Maps](#17-thread-safety-and-other-maps)
18. [A Senior Engineer's Checklist](#18-a-senior-engineers-checklist)
19. [Version-by-Version History](#19-version-by-version-history)
20. [Practical Programs](#20-practical-programs)
21. [Common Mistakes](#21-common-mistakes)
22. [Cheat Sheet, Summary and Quiz](#22-cheat-sheet-summary-and-quiz)

---

## 1. Roadmap and How to Read This Guide

```
 PART A  The idea
   Set and Map contracts            "no duplicates" and "key → value"
   Hash table from scratch          array + hash function + collision handling + resizing

 PART B  Hash-based implementations
   HashMap / HashSet                buckets, nodes, chaining, load factor, rehashing, treeification
   LinkedHashMap / LinkedHashSet    hash table + a linked list for order (LRU caches)

 PART C  Tree-based implementations
   Red-Black trees                  self-balancing binary search trees
   TreeMap / TreeSet                sorted keys, range queries, O(log n)

 PART D  Engineering
   Choosing, sizing, pitfalls, concurrency, version history, programs, quiz
```

### The question trio we use throughout

```
 ① THE PROBLEM   →   What is hard or slow without this?
 ② THE NEED      →   What must a solution guarantee?
 ③ THE SOLUTION  →   The design that follows (and its price)
```

---

## 2. Set and Map Basics

(This is section 1 of the lecture outline.)

### 2.1 The `Set` interface

A **`Set<E>`** is a collection of **distinct** elements: **no duplicates**.

```java
Set<String> users = new HashSet<>();
System.out.println(users.add("ali"));     // true   (added)
System.out.println(users.add("sara"));    // true
System.out.println(users.add("ali"));     // false  (already there: nothing changes)
System.out.println(users.size());         // 2
System.out.println(users.contains("ali"));// true
```

| Promise | Meaning |
|---------|---------|
| **No duplicates** | Two elements `a` and `b` with `a.equals(b)` can't both be in the set |
| **Fast membership test** | `contains` is **O(1) on average** for `HashSet`, **O(log n)** for `TreeSet` |
| **No positional access** | There is **no `get(i)`**: a set has no index |

Typical uses: **tracking unique users**, **removing duplicates**, **membership checks** ("is this ID already processed?"), **set math** (union, intersection).

### 2.2 The `Map` interface

A **`Map<K, V>`** stores **key → value pairs**. **Keys are unique**; values may repeat.

```java
Map<String, Integer> ages = new HashMap<>();
ages.put("ali", 20);
ages.put("sara", 22);
ages.put("ali", 21);                      // same key: the value is REPLACED (old value 20 returned)
System.out.println(ages.get("ali"));      // 21
System.out.println(ages.get("zed"));      // null  (no such key)
```

| Promise | Meaning |
|---------|---------|
| **Unique keys** | One value per key |
| **Fast lookup by key** | `get(key)` is **O(1) on average** for `HashMap` |
| **Not a `Collection`** | `Map` does **not** extend `Collection`; it exposes **views** (`keySet`, `values`, `entrySet`) |

Typical uses: **caches**, **indexes** (id → object), **counting** (word → frequency), **grouping**, **lookup tables**.

### 2.3 The trade-off: no positions

Unlike an `ArrayList`, hash-based structures give up **positional access**. The element's location is decided by its **hash**, not by the order you added it:

```
 ArrayList:   "give me the 3rd"      → index 2           (position is meaningful)
 HashSet:     "is 'ali' in there?"   → hash('ali') → bucket  (identity is meaningful, position is not)
```

You trade **order and index** for **speed of search**.

### 2.4 The family tree (with Java 21 additions)

```
                        Iterable
                           ▲
                       Collection
                           ▲
                          Set
           ┌───────────────┼──────────────────┐
        HashSet        SequencedSet        SortedSet                (SortedSet extends SequencedSet in Java 21)
           ▲          (Java 21)               ▲
     LinkedHashSet ───────┘               NavigableSet
     (insertion order)                         ▲
                                            TreeSet
                                         (sorted order)

                          Map                      ← NOT a Collection
           ┌───────────────┼──────────────────┬───────────────┐
        HashMap       SequencedMap        SortedMap       ConcurrentMap
           ▲          (Java 21)               ▲                ▲
     LinkedHashMap ───────┘               NavigableMap   ConcurrentHashMap
                                              ▲
                                           TreeMap
```

| Need | Set | Map |
|------|-----|-----|
| Fastest, no order | `HashSet` | `HashMap` |
| Keep insertion order | `LinkedHashSet` | `LinkedHashMap` |
| Keep sorted order | `TreeSet` | `TreeMap` |

### 2.5 Set algebra with `Set`

| Math operation | Java |
|----------------|------|
| Union `A ∪ B` | `a.addAll(b)` |
| Intersection `A ∩ B` | `a.retainAll(b)` |
| Difference `A − B` | `a.removeAll(b)` |
| Subset `B ⊆ A` | `a.containsAll(b)` |

---

## 3. Deriving a Hash Table From First Principles

Let's *invent* the hash table ourselves. If you understand this section, the rest of the guide becomes obvious.

### 3.1 ① The problem

You store 1,000,000 user names. The question: **"Is `ali` already stored?"**

| Approach | Cost |
|----------|------|
| Unsorted list: scan everything | O(n): up to 1,000,000 comparisons |
| Sorted array + binary search | O(log n): about 20 comparisons (but inserting is O(n)) |
| **Direct access by number** | O(1): **if only we had a number for each name** |

### 3.2 ② The big idea: turn the key into an array index

From the array guide: reading `arr[i]` is O(1) (it's just `base + i × size`). So **if we could convert any key into an array index, lookup would be one step.**

```
 key ──► (some function) ──► number ──► array index ──► the slot where we store/find it
```

That function is a **hash function**; the number is the **hash code**; the array slots are **buckets**.

### 3.3 The three problems this idea creates

**Problem 1: the number is huge.** `hashCode()` returns any `int` (±2 billion), but our array has maybe 16 slots.
→ **Fix:** shrink the number into the array's range (modulo, or a bit mask).

**Problem 2: collisions are unavoidable.** Infinitely many possible keys, only about 4 billion `int` values, and only a few slots. So **different keys will land in the same slot** (the *pigeonhole principle*).
→ **Fix:** a **collision strategy**.

**Problem 3: the array fills up.** With 1,000,000 keys in 16 slots, every slot holds a huge pile.
→ **Fix:** **grow the array** as items are added, keeping piles short.

### 3.4 Collision strategies: chaining vs open addressing

| | **Separate chaining** (Java) | **Open addressing** (e.g. Python's dict) |
|---|------------------------------|------------------------------------------|
| Idea | Each slot holds a **list (chain)** of entries that hashed there | If the slot is taken, **probe** other slots in the same array until a free one |
| Delete | Easy: unlink the node | Hard: needs **tombstones** (markers) so searches don't stop early |
| Load factor | Works fine above 1.0 (just longer chains) | **Must stay below 1.0**; performance collapses near full |
| Memory | One node object per entry (pointer overhead) | Entries stored in the array itself (cache-friendly, compact) |
| Clustering | None | Entries bunch up (primary/secondary clustering) |
| Worst case | Can be improved with **trees** (Java 8 did exactly this) | Hard to improve |

**Why Java chose chaining** (the lecture's point): it is **simpler and more predictable**, deletion is trivial, performance degrades **gracefully**, and a long chain can be **upgraded to a balanced tree** (section 9), something open addressing can't do.

### 3.5 What we have invented

```
   table (array of buckets)
  ┌─────┐
  │  0  │──► null
  ├─────┤
  │  1  │──► [ali] ──► [zed] ──► null          ← a collision: two keys in bucket 1, chained
  ├─────┤
  │  2  │──► null
  ├─────┤
  │  3  │──► [sara] ──► null
  └─────┘
```

- **Hash function:** key → number
- **Index function:** number → bucket
- **Chaining:** a linked list per bucket
- **Load factor + resizing:** keep the chains short

That is precisely `java.util.HashMap`. Everything else is engineering polish.

### 3.6 Why it's fast: the expected chain length

With a good hash function, keys spread **evenly**. If we keep **(number of entries) / (number of buckets) ≤ 0.75**, the **average chain length is below 1**, so a lookup does: one hash, one array access, about one comparison. That is **O(1) on average** (the lecture's "constant time").

---

## 4. `HashMap` Architecture: The Real Structure

### 4.1 The fields (simplified from the JDK source)

```java
public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V> ... {

    transient Node<K, V>[] table;     // the bucket array (allocated lazily on the first put)
    transient int size;               // number of key-value pairs
    transient int modCount;           // structural-modification counter (for fail-fast iterators)
    int threshold;                    // resize when size exceeds this: capacity × loadFactor
    final float loadFactor;           // default 0.75

    static final int DEFAULT_INITIAL_CAPACITY = 16;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final int TREEIFY_THRESHOLD = 8;
    static final int UNTREEIFY_THRESHOLD = 6;
    static final int MIN_TREEIFY_CAPACITY = 64;
    static final int MAXIMUM_CAPACITY = 1 << 30;
}
```

### 4.2 The node (the lecture's "hash, key, value, next")

Every entry is stored in a small **`Node`** object:

```java
static class Node<K, V> implements Map.Entry<K, V> {
    final int hash;       // the (spread) hash of the key, stored so we never recompute it
    final K key;          // the key (never changes)
    V value;              // the value (can be replaced)
    Node<K, V> next;      // the next node in the same bucket (the chain)
}
```

```
   one Node in the heap
  ┌───────────────────────────┐
  │ header                    │
  │ hash   = 65919            │   cached: makes comparisons fast and resizing cheap
  │ key    ─────────► "Ali"   │
  │ value  ─────────► 42      │
  │ next   ─────────► (next node in this bucket, or null)
  └───────────────────────────┘
```

Why store `hash`? (1) comparing two ints is far cheaper than calling `equals`, so most non-matching nodes are rejected instantly; (2) during a **resize** we don't need to call `hashCode()` again.

### 4.3 The whole picture

```
   HashMap object
  ┌───────────────────────────┐
  │ table ──────────────────┐ │          the bucket array (length is ALWAYS a power of two)
  │ size = 5                │ │
  │ threshold = 12          │ │      index
  │ loadFactor = 0.75       │ │     ┌──────┐
  └─────────────────────────┼─┘     │  0   │──► null
                            └─────► │  1   │──► [Node: Ali=1] ──► [Node: Zed=7] ──► null
                                    │  2   │──► null
                                    │  3   │──► [Node: Sara=2] ──► null
                                    │ ...  │
                                    │  15  │──► [Node: Bob=5] ──► null
                                    └──────┘
                    (a bucket with 9+ nodes may be converted to a tree: section 9)
```

### 4.4 Capacity is always a power of two

You can request any capacity, but `HashMap` **rounds up to the next power of two** (`tableSizeFor`):

| You ask for | You get |
|-------------|---------|
| `new HashMap<>()` | 16 (on first `put`) |
| `new HashMap<>(10)` | 16 |
| `new HashMap<>(17)` | 32 |
| `new HashMap<>(100)` | 128 |
| `new HashMap<>(1000)` | 1024 |

**Why a power of two?** Because then `hash % capacity` can be replaced by the much faster bit mask **`hash & (capacity - 1)`**, and resizing becomes a clever "stay or move by `oldCapacity`" split (section 8.4).

### 4.5 Lazy allocation

`new HashMap<>()` allocates **no table**. The 16-bucket array is created on the **first `put`**. (So an empty `HashMap` is cheap.)

---

## 5. The Journey of a Key: Hash, Spread, Index

(This is the "buckets and hashing with modulo arithmetic" part of the lecture.)

### 5.1 The pipeline

```
   key ──► hashCode() ──► spread ──► mask ──► bucket index
          (your class)    (h ^ h>>>16)  (& (n-1))

   "Ali" ──► 65918 ──────► 65919 ─────► 15
```

### 5.2 Step 1: `hashCode()`

Every object has one (from `Object`, or your override, or `String`'s formula). `String.hashCode()` is `s[0]×31^(n-1) + ... + s[n-1]`. `Integer.hashCode()` is the value itself.

### 5.3 Step 2: the spread function

```java
static int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

It XORs the **upper 16 bits into the lower 16 bits**.

**① Problem:** the bucket index only uses the **lowest bits** of the hash (for 16 buckets, only 4 bits!). If two hashes differ only in their **high** bits, they'd all collide.

```
 hash A:  0000 0000 0001 0000 | 0000 0000 0000 0000
 hash B:  0000 0000 0010 0000 | 0000 0000 0000 0000        (differ only in the HIGH half)
 low 4 bits of both = 0000 → same bucket, always!
```

**② Need:** make the high bits influence the low bits.

**③ Solution:** `h ^ (h >>> 16)` mixes the two halves. It is cheap (one shift and one XOR) and good enough.

### 5.4 Step 3: from hash to bucket index

```java
int index = (table.length - 1) & hash;      // equivalent to hash % length, when length is a power of two
```

For a table of 16: `n - 1 = 15 = 0b1111`. The mask **keeps only the lowest 4 bits**.

```
   hash        =  ... 0001 0000 0001 0111 1111     (65919)
   n - 1 (15)  =  ... 0000 0000 0000 0000 1111
   AND         =  ... 0000 0000 0000 0000 1111  =  15   → bucket 15
```

**Why `&` instead of `%`?** It's faster, and it handles **negative** hash codes correctly (`-7 % 16` is `-7` in Java, which is not a valid index; `-7 & 15` is `9`).

### 5.5 A fully worked example: the key `"Ali"`

```
 "Ali".hashCode() = 'A'×31² + 'l'×31 + 'i' = 65×961 + 108×31 + 105 = 65918

 in hex:     65918 = 0x1017E
 h >>> 16  = 0x00001              (the upper 16 bits, moved down)
 spread    = 0x1017E ^ 0x00001 = 0x1017F = 65919

 index (n = 16) = 65919 & 15 = 0xF = 15       → bucket 15
```

More keys (computed the same way, n = 16):

| Key | `hashCode()` | After spread | Bucket |
|-----|--------------|--------------|:------:|
| `"Ali"` | 65918 | 65919 | **15** |
| `"Sara"` | 2569501 | 2569530 | **10** |
| `"John"` | 2314539 | 2314504 | **8** |
| `"Aa"` | 2112 | 2112 | **0** |
| `"BB"` | 2112 | 2112 | **0** |
| `"C#"` | 2112 | 2112 | **0** |

Notice the last three: **different strings with the same hash code**, which means the same bucket. That's a real collision (program 20.3 prints this table).

### 5.6 Small integers: the index is just the low bits

For `Integer` keys below 65536, the spread changes nothing (the high half is 0), so the bucket is simply `value & (n - 1)`. With 16 buckets: `5 → 5`, `17 → 1`, `33 → 1`, `100 → 4`. This makes small-integer examples easy to trace by hand, and we use them below.

---

## 6. `put`, `get` and `remove`: Step by Step

### 6.1 `put(key, value)` flow

```
 put(key, value)
    │
    ├─ table is null or empty?  ───────────────► resize()   (allocate 16 buckets, threshold 12)
    │
    ├─ hash = spread(key.hashCode());   i = (n - 1) & hash
    │
    ├─ bucket i is EMPTY?  ── yes ─────────────► place a new Node there
    │          │ no
    │          ▼
    │   the FIRST node has the same key?  ─ yes ► replace its value, return the OLD value
    │          │ no                                (size doesn't change)
    │          ▼
    │   the bin is a TREE?  ── yes ────────────► putTreeVal(...)   (insert into the red-black tree)
    │          │ no
    │          ▼
    │   walk the chain:
    │      • find a node with the same key ─────► replace its value, return the old value
    │      • reach the end ─────────────────────► append the new Node at the TAIL
    │             └─ chain length now > 8?  ───► treeifyBin():
    │                                              table < 64 buckets?  → resize() instead
    │                                              otherwise            → convert the chain to a tree
    │
    └─ ++size > threshold?  ───────────────────► resize()   (double the table)
```

**"Same key" test** (used everywhere): a node matches when

```java
node.hash == hash  &&  ( node.key == key  ||  key.equals(node.key) )
```

The cheap **hash comparison first**, then the identity check `==`, and only then the (possibly expensive) `equals`.

### 6.2 `get(key)` flow

```
 get(key)
    │
    ├─ table empty?  ──────────────────────────► return null
    ├─ i = (n - 1) & hash(key)
    ├─ bucket i empty?  ───────────────────────► return null
    ├─ first node matches?  ───────────────────► return its value
    ├─ bin is a tree?  ────────────────────────► search the tree: O(log n)
    └─ else walk the chain comparing (hash, then key) ─► value, or null if not found
```

Cost: one hash, one array read, then a **short** chain walk. That's the **O(1) average**.

### 6.3 `remove(key)`

Find the node the same way, then **unlink** it from the chain (`prev.next = node.next`), `size--`. No shifting, no tombstones (a benefit of chaining). If a tree bin becomes small, it converts back to a chain (section 9.5).

### 6.4 `put` returns the previous value

```java
Map<String, Integer> m = new HashMap<>();
System.out.println(m.put("a", 1));      // null  (no previous mapping)
System.out.println(m.put("a", 2));      // 1     (the replaced value)
```

Note that `null` is ambiguous (no mapping **or** a mapping to `null`). Use `containsKey` to tell them apart.

### 6.5 Java 7 vs Java 8: what changed in `put`

| | Java 7 | Java 8+ |
|---|--------|---------|
| Insert into a chain | at the **head** | at the **tail** |
| Long chains | stay linked lists (O(n)) | **treeify** (O(log n)) |
| Resize relink | recomputes each node's index; **reverses** chain order | **splits** lo/hi, preserves order |
| Concurrent resize | could create a **cycle** → infinite loop | still unsafe, but no cycle from that bug |
| Hash function | several shifts/XORs | one `h ^ (h >>> 16)` |

---

## 7. Collisions and Chaining

(This is the "collision handling" part of the lecture.)

### 7.1 What a collision looks like

Take a small table of capacity 4 (index = `value & 3`) and the integer keys `1, 5, 2, 9`:

```
   1 & 3 = 1        5 & 3 = 1        2 & 3 = 2        9 & 3 = 1

   table
  ┌───┐
  │ 0 │──► null
  ├───┤
  │ 1 │──► [hash=1, key=1, "a"] ──► [hash=5, key=5, "b"] ──► [hash=9, key=9, "d"] ──► null
  ├───┤          ▲                           ▲                         ▲
  │ 2 │──► [2,"c"]   three keys, ONE bucket: they are chained in insertion order (tail insertion)
  ├───┤
  │ 3 │──► null
  └───┘
```

To `get(9)`: hash → bucket 1 → compare with node 1 (`hash 1 ≠ 9`), node 5 (`5 ≠ 9`), node 9 (**match**). Three steps instead of one: that's the price of a collision.

### 7.2 How bad can it get?

The cost of an operation is **the length of the chain it must walk**.

| Situation | Chain length | `get` cost |
|-----------|--------------|------------|
| Good hash, load factor ≤ 0.75 | ~0 to 2 | **O(1)** |
| Poor `hashCode()` (e.g. always returns 42) | **all keys in ONE chain** | O(n) (O(log n) after treeification) |
| Malicious keys crafted to collide (hash-flooding attack) | huge chains | O(n) without trees; O(log n) with trees |

### 7.3 A terrible (but legal) `hashCode`

```java
@Override
public int hashCode() { return 42; }    // obeys the contract (equal objects → equal hashes), but ALL keys collide
```

The map still **works** (equals still distinguishes keys) but degrades into a single long chain. This is why **the quality of `hashCode()` matters** for performance, while its **correctness** depends only on being consistent with `equals`.

### 7.4 Why collisions are normal, not a bug

By the pigeonhole principle you can't avoid them. A well-built table doesn't try to prevent collisions; it just keeps chains **short on average** (load factor) and **bounded in the worst case** (treeification).

---

## 8. Load Factor, Capacity and Rehashing

(This is the "Load Factor & Rehashing" part of the lecture.)

### 8.1 ① The problem

If we keep adding entries to a fixed table, the average chain length grows without limit: `entries / buckets` → ∞. Lookups slow toward O(n).

### ② The need

A rule that says **"when it's getting crowded, make more room"**.

### ③ The solution: load factor and resize

```
 load factor = (number of entries) / (number of buckets)

 threshold = capacity × loadFactor            (default loadFactor = 0.75)
 When size > threshold → resize: DOUBLE the number of buckets.
```

| Capacity | Threshold (× 0.75) | The put that triggers the resize |
|---------:|-------------------:|----------------------------------|
| 16 | 12 | the **13th** entry |
| 32 | 24 | the 25th |
| 64 | 48 | the 49th |
| 128 | 96 | the 97th |
| 256 | 192 | the 193rd |

So **a default `HashMap` resizes when it becomes more than 75% full**, doubling from 16 → 32 → 64 and so on. (In the lecture: "when an array reaches 75% capacity, Java triggers a resize to double the size".)

### 8.2 Why 0.75? (the space/time trade-off)

| Load factor | Effect |
|-------------|--------|
| **Low** (e.g. 0.5) | Short chains, faster lookups, **more wasted memory** (many empty buckets), more resizes |
| **High** (e.g. 1.0) | Less memory, but **longer chains and slower** lookups |
| **0.75** | A well-tested compromise |

The JDK source comments back this with probability math: under random hashing with the resize policy of 0.75, the number of nodes in a bucket follows a Poisson distribution with a mean of about 0.5:

| Nodes in one bucket | Probability |
|--------------------:|------------:|
| 0 | 0.60653066 |
| 1 | 0.30326533 |
| 2 | 0.07581633 |
| 3 | 0.01263606 |
| 4 | 0.00157952 |
| 5 | 0.00015795 |
| 6 | 0.00001316 |
| 7 | 0.00000094 |
| 8 | 0.00000006 |

So a chain of **8** is astronomically rare with a good hash function. That's why the **treeify threshold is 8**: trees only appear when something is wrong (bad hashes or attacks).

### 8.3 What a resize does

```
 resize():
   1. Allocate a new array with DOUBLE the capacity.
   2. Move every existing node into the new array (rehashing).
   3. threshold = newCapacity × loadFactor.
```

A resize is **O(n)** (every node is touched), but it happens rarely (each time the table doubles), so the **amortized** cost per `put` is **O(1)**, the same argument as `ArrayList` growth.

### 8.4 The clever Java 8 resize: "stay or move by oldCapacity"

**① Problem:** After doubling, each node's bucket index may change. Recomputing everything is slow.

**② Observation:** the capacity is a power of two, so doubling adds **one more bit** to the mask:

```
 old capacity 16:  mask = 0b01111   (index uses the low 4 bits)
 new capacity 32:  mask = 0b11111   (index uses the low 5 bits)
                           ▲
                    the new bit: (hash & oldCapacity)
```

So the **only** thing that can change is that one extra bit of the hash:

- If `(hash & oldCapacity) == 0` → the new bit is 0 → the node **stays at the same index** `j`.
- If `(hash & oldCapacity) != 0` → the new bit is 1 → the node **moves to `j + oldCapacity`**.

**③ Solution:** walk each old chain once and split it into a **"lo" chain** (stays) and a **"hi" chain** (moves), **preserving the original order**. No `hashCode()` calls, no index recomputation.

Worked example (from the small table above): capacity 4 → 8, so `oldCap = 4` (binary `100`):

```
 old bucket 1:   1 ──► 5 ──► 9

   key 1:  1 & 4 = 0   → LO  (stays in bucket 1)
   key 5:  5 & 4 = 4   → HI  (moves to bucket 1 + 4 = 5)
   key 9:  9 & 4 = 0   → LO  (stays in bucket 1)

 new table (capacity 8):
   [1] ──► 1 ──► 9
   [5] ──► 5
```

```
   BEFORE (capacity 4)                      AFTER (capacity 8)
  ┌───┐                                    ┌───┐
  │ 0 │ -                                  │ 0 │ -
  │ 1 │ 1 → 5 → 9        ── split ──►      │ 1 │ 1 → 9
  │ 2 │ 2                                  │ 2 │ 2
  │ 3 │ -                                  │ 3 │ -
  └───┘                                    │ 4 │ -
                                           │ 5 │ 5        ← "5" moved to 1 + oldCap = 5
                                           │ 6 │ -
                                           │ 7 │ -
                                           └───┘
```

You can run this exact scenario in program 20.2 (a miniature `HashMap` that prints its table).

### 8.5 Why iteration order can change after a resize

Nodes move between buckets, and iteration walks buckets in index order, so the **iteration order of a `HashMap` can change when it resizes**. (Another reason never to depend on it.)

### 8.6 Pre-sizing (a senior-engineer habit)

If you **know** you'll store about `N` entries, avoid repeated resizes. Beware: **`new HashMap<>(N)` is not enough!**

```java
Map<String, Integer> m = new HashMap<>(1000);
// capacity rounds to 1024, threshold = 1024 × 0.75 = 768  →  it will STILL resize when you pass 768 entries
```

The capacity you need is `N / 0.75`:

```java
Map<String, Integer> m1 = new HashMap<>((int) (1000 / 0.75f) + 1);   // 1334 → rounds to 2048, threshold 1536 ✓
Map<String, Integer> m2 = HashMap.newHashMap(1000);                   // Java 19+: does the math for you ✓
```

### 8.7 Other facts about sizing

| Fact | Detail |
|------|--------|
| **Never shrinks** | Removing entries doesn't reduce capacity. `clear()` empties the table but keeps its size |
| **Max capacity** | 2³⁰ (about 1 billion buckets) |
| **Iteration cost** | **O(capacity + size)**: a mostly empty, huge table iterates slowly |
| **Custom load factor** | `new HashMap<>(16, 0.5f)` trades memory for speed. Rarely needed |

---

## 9. Treeification: Chains Become Red-Black Trees (Java 8+)

(This is the "Treeification" part of the lecture.)

### 9.1 ① The problem

If many keys collide (bad `hashCode()`, or an attacker choosing colliding keys), a bucket's chain grows long and `get` degrades to **O(n)**. An attacker could slow a server to a crawl using many keys with equal hashes (a **hash-flooding / HashDoS** attack).

### ② The need

A **worst-case guarantee**: even a crowded bucket should stay **fast**.

### ③ The solution

When a chain gets too long, convert it into a **balanced tree** (a **red-black tree**). Searching a balanced tree is **O(log n)** instead of O(n).

### 9.2 The three constants

| Constant | Value | Meaning |
|----------|:-----:|---------|
| `TREEIFY_THRESHOLD` | **8** | A chain that grows **beyond 8 nodes** is a candidate to become a tree |
| `MIN_TREEIFY_CAPACITY` | **64** | Treeify only if the **table has at least 64 buckets**; otherwise **resize** instead |
| `UNTREEIFY_THRESHOLD` | **6** | A tree that shrinks to **6 or fewer** nodes turns back into a chain |

The gap between 8 and 6 prevents a bucket from flip-flopping between chain and tree on every insert and remove.

### 9.3 Why "resize first when the table is small"?

A long chain in a **small** table usually just means **the table is too small** (everything is crowded), not that the hashes are bad. Doubling the table spreads the keys out for free. Only when the table is already big (64+) and a bucket is **still** crowded does Java conclude that the hashing is the problem and builds a tree.

```
 chain reaches 9 nodes in one bucket
        │
        ├─ table length < 64?  ──► resize() (double the table: cheap fix)
        │
        └─ table length ≥ 64?  ──► treeifyBin(): convert this bucket into a red-black tree
```

### 9.4 What the bucket looks like

```
   BEFORE (chain of 9, search = up to 9 steps)

   [i] ─► N1 ─► N2 ─► N3 ─► N4 ─► N5 ─► N6 ─► N7 ─► N8 ─► N9

   AFTER (red-black tree, search = about log₂(9) ≈ 3 steps)

   [i] ─► root
              ┌──── N5 (B) ────┐
           N3 (R)           N7 (R)
          /     \          /     \
        N2(B)  N4(B)     N6(B)  N8(B)
        /                          \
      N1(R)                       N9(R)
```

Details of a "tree bin":

- The nodes become **`TreeNode`** objects (extra fields: parent, left, right, previous, red/black flag), which are **bigger** than plain nodes (about twice the memory). This is acceptable because it's rare.
- The tree is ordered by **hash code first**. If two keys have equal hashes, it uses **`compareTo`** when the keys are `Comparable` of the same class; otherwise it uses a tie-breaker (class name, then identity hash) to keep a consistent order.
- The `TreeNode`s also keep their `next` links, so **iteration** and **untreeifying** still work.

### 9.5 Going back: untreeify

A tree is turned back into a plain chain when it becomes small (6 nodes or fewer), which is checked during a **resize split** or a **removal**.

### 9.6 The cost model after treeification

| Keys in the crowded bucket | Lookup cost |
|----------------------------|-------------|
| `Comparable` keys (like `String`, `Integer`) | **O(log n)** |
| Non-`Comparable` keys with identical hashes | The tree can't order them, so lookups may still search many nodes (**up to O(n)** in the pathological case) |

**Senior takeaway:** treeification is a **safety net**, not a performance feature. A good `hashCode()` still matters. If your keys can collide a lot, make them `Comparable`.

### 9.7 Summary of the Java 8 improvement

```
 Java 7:  worst case per bucket =  O(n)        (linked list only)
 Java 8:  worst case per bucket =  O(log n)    (red-black tree, when keys are Comparable)
```

---

## 10. `HashSet`: A `HashMap` in Disguise

### 10.1 ① The problem and ② the need

A `Set` needs the same thing as a `Map`'s keys: unique elements with fast lookup. Rewriting a whole hash table would be silly.

### ③ The solution: reuse `HashMap`

A `HashSet` **contains a `HashMap`** and uses the **elements as keys**. The values are a single shared dummy object:

```java
public class HashSet<E> extends AbstractSet<E> implements Set<E> ... {

    private transient HashMap<E, Object> map;
    private static final Object PRESENT = new Object();     // the same dummy value for every key

    public boolean add(E e)        { return map.put(e, PRESENT) == null; }
    public boolean contains(Object o) { return map.containsKey(o); }
    public boolean remove(Object o)   { return map.remove(o) == PRESENT; }
    public int size()              { return map.size(); }
    public Iterator<E> iterator()  { return map.keySet().iterator(); }
}
```

```
   HashSet<String>                           the HashMap inside
  ┌──────────────┐                       ┌─────────────────────────────┐
  │ map ─────────┼─────────────────────► │ "ali"   ──►  PRESENT        │
  └──────────────┘                       │ "sara"  ──►  PRESENT        │
                                         │ "zed"   ──►  PRESENT        │
                                         └─────────────────────────────┘
        one shared dummy Object, so a HashSet costs only the keys + the node overhead
```

### 10.2 How `add` detects duplicates

`map.put(e, PRESENT)` returns the **previous value**:

- first time: no previous mapping → returns `null` → `add` returns **`true`**
- already present: previous value is `PRESENT` (not null) → `add` returns **`false`** (and the existing element stays)

### 10.3 So everything you learned about `HashMap` applies

Same buckets, same collision handling, same load factor 0.75, same resizing, same treeification, and the same requirement: **elements need a correct `equals` + `hashCode`**.

### 10.4 Own-built `HashSet` in six lines

```java
class MyHashSet<E> {
    private static final Object PRESENT = new Object();
    private final java.util.HashMap<E, Object> map = new java.util.HashMap<>();

    boolean add(E e)          { return map.put(e, PRESENT) == null; }
    boolean contains(Object o){ return map.containsKey(o); }
    boolean remove(Object o)  { return map.remove(o) == PRESENT; }
    int size()                { return map.size(); }
}
```

### 10.5 The same trick everywhere

| Set | Backed by |
|-----|-----------|
| `HashSet` | `HashMap` |
| `LinkedHashSet` | `LinkedHashMap` |
| `TreeSet` | `TreeMap` (a `NavigableMap`) |

---

## 11. Iteration Order, `null`s and the `equals`/`hashCode` Contract

### 11.1 `HashMap` iteration order

The iteration walks **bucket 0, then bucket 1, ...**, and each bucket's chain in order. So the order depends on the **hash codes and the table size**, **not** on insertion order. It is **deterministic for a given JDK** but **not a guarantee**.

Example: a default `HashSet<Integer>` (16 buckets, index = `value & 15`):

```java
Set<Integer> set = new HashSet<>();
for (int n : new int[]{100, 5, 33, 17}) set.add(n);
System.out.println(set);                  // [33, 17, 100, 5]
```

```
   value:   100    5     33    17
   index:   4      5     1     1         (100 & 15 = 4,  5 & 15 = 5,  33 & 15 = 1,  17 & 15 = 1)

   table:  [1] ─► 33 ─► 17        ← a collision, chained in insertion order
           [4] ─► 100
           [5] ─► 5

   iteration (bucket order):  33, 17, 100, 5
```

The set "looks random" but is perfectly explained by the buckets. Never depend on it: a resize, a different JDK, or different keys changes it.

### 11.2 Null handling

| Collection | `null` key / element | `null` value |
|------------|:--------------------:|:------------:|
| `HashMap` | **One** null key allowed (hash 0 → bucket 0) | allowed |
| `HashSet` | **One** `null` allowed | n/a |
| `LinkedHashMap` / `LinkedHashSet` | allowed | allowed |
| `TreeMap` / `TreeSet` (natural ordering) | **Not allowed** (`NullPointerException`) | `TreeMap`: allowed |
| `Hashtable`, `ConcurrentHashMap` | **Not allowed** | **Not allowed** |
| `Map.of(...)`, `Set.of(...)` | **Not allowed** | **Not allowed** |

Why does `TreeMap` reject `null` keys? It must **compare** keys (`compareTo`), and you can't compare against `null`. (A `TreeMap` with a custom `Comparator` that handles `null` can allow it.)

### 11.3 The equals/hashCode contract (revisited, now you see why)

```
 HashMap uses BOTH:  hash first (to find the bucket and quickly reject), then equals (to confirm).
```

| Rule | Why `HashMap` needs it |
|------|------------------------|
| **Equal objects must have equal hash codes** | Otherwise an equal key would be searched for in a **different bucket** and never be found |
| `hashCode()` must be **stable** while the object is a key | If it changes after insertion, the node sits in the **wrong bucket** → "lost" entry |
| Different objects **may** share a hash | Collisions are handled by `equals` in the chain |

**Mutable keys are dangerous:**

```java
List<Integer> key = new ArrayList<>(List.of(1, 2));
Map<List<Integer>, String> map = new HashMap<>();
map.put(key, "found");

key.add(3);                                // the list's hashCode changed!
System.out.println(map.get(key));          // null   ← looked in the wrong bucket
System.out.println(map.containsKey(List.of(1, 2)));   // false ← the right content, but the node's cached hash is stale
```

Use **immutable** keys (`String`, `Integer`, records, enums, your own immutable classes).

### 11.4 Views and fail-fast iteration

Iterators check **`modCount`**. If the map is structurally changed while iterating (not through the iterator), you get **`ConcurrentModificationException`**.

---

## 12. `LinkedHashMap` and `LinkedHashSet`

(This is the "Linked implementations" part of the lecture.)

### 12.1 ① The problem

`HashMap` is fast but its iteration order looks random. Many programs need **predictable order**: print entries in the order they were added, or implement a cache that evicts the **least recently used** entry.

### ② The need

Keep the speed of hashing **and** remember an order.

### ③ The solution: a hash table plus a doubly linked list

`LinkedHashMap extends HashMap`. Every entry is a node of the hash table **and** also a node in a **doubly linked list** that records the order.

```java
static class Entry<K, V> extends HashMap.Node<K, V> {
    Entry<K, V> before;        // previous in the order list
    Entry<K, V> after;         // next in the order list
}
// LinkedHashMap also keeps:  Entry head;  Entry tail;  boolean accessOrder;
```

```
   hash table (for fast lookup)                       linked list (for order)

  ┌───┐
  │ 0 │ ─►  [C]                              head                            tail
  │ 1 │ ─►  [A] ─► [D]                         │                               │
  │ 2 │ -                                      ▼                               ▼
  │ 3 │ ─►  [B]                              [B] ⇄ [A] ⇄ [C] ⇄ [D]        (insertion order: B, A, C, D)
  └───┘
   Each box is the SAME node object: it sits in a bucket chain AND in the before/after list.
```

- **Lookup** (`get`, `containsKey`): through the hash table: **O(1)**, exactly like `HashMap`.
- **Iteration:** walks the **linked list** from `head` to `tail`: **O(size)** (independent of the table capacity!).

### 12.2 Costs

| | `HashMap` | `LinkedHashMap` |
|---|-----------|-----------------|
| `get` / `put` / `remove` | O(1) avg | O(1) avg (slightly slower: maintains the list) |
| Iteration | O(capacity + size) | **O(size)** |
| Memory per entry | node (~32 bytes) | node + 2 references (~40 bytes) |
| Order | none | **insertion order** (or access order) |

### 12.3 Insertion order (the default)

```java
Map<String, Integer> hash = new HashMap<>();
Map<String, Integer> linked = new LinkedHashMap<>();
for (String k : new String[]{"c", "a", "b"}) {
    hash.put(k, k.charAt(0) - 'a' + 1);
    linked.put(k, k.charAt(0) - 'a' + 1);
}
System.out.println(hash);     // {a=1, b=2, c=3}     (bucket order: "a"=97&15=1, "b"=98&15=2, "c"=99&15=3)
System.out.println(linked);   // {c=3, a=1, b=2}     (insertion order)
```

**Re-inserting an existing key does not change its position** in insertion-order mode.

### 12.4 Access order and the LRU cache

Construct it with `accessOrder = true` and every `get`/`put` of an existing key **moves that entry to the end** (most recently used). The **head** is then always the **least recently used** entry.

```java
Map<String, Integer> m = new LinkedHashMap<>(16, 0.75f, true);     // true = access order
```

A hook method, `removeEldestEntry`, is called after every insertion. Return `true` and the **head (eldest) entry is removed**. That gives a complete **LRU cache** in a few lines:

```java
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    LRUCache(int capacity) {
        super(16, 0.75f, true);                 // access order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;               // evict the least recently used when too big
    }
}
```

```
 capacity 3

 put A, B, C         order:  A → B → C          (head = A is the least recently used)
 get A               order:  B → C → A          (A moved to the tail: most recent)
 put D               order:  B → C → A → D      then size 4 > 3 → evict the head (B)
                     order:  C → A → D
```

### 12.5 How `LinkedHashMap` hooks into `HashMap`

`HashMap` has empty "hook" methods that `LinkedHashMap` overrides:

| Hook | Called | `LinkedHashMap` does |
|------|--------|----------------------|
| `newNode(...)` | when a node is created | creates a linked `Entry` and **appends it at the tail** |
| `afterNodeAccess(node)` | after `get`/`put` of an existing key | in access-order mode, **moves the node to the tail** |
| `afterNodeInsertion(evict)` | after a new entry is added | calls `removeEldestEntry(head)` and removes the head if it returns `true` |
| `afterNodeRemoval(node)` | after a node is removed | **unlinks** it from the order list |

This is a good example of the **Template Method** design pattern: `HashMap` defines the algorithm and leaves hooks for subclasses.

### 12.6 `LinkedHashSet`

```java
public class LinkedHashSet<E> extends HashSet<E> ... // internally uses a LinkedHashMap
```

Same trick as `HashSet`, but backed by `LinkedHashMap`: unique elements **in insertion order**.

```java
List<Integer> withDuplicates = List.of(3, 1, 3, 2, 1);
List<Integer> unique = new ArrayList<>(new LinkedHashSet<>(withDuplicates));
System.out.println(unique);                 // [3, 1, 2]    ← duplicates removed, first-seen order kept
```

### 12.7 Java 21: `SequencedMap` and `SequencedSet`

`LinkedHashMap` implements **`SequencedMap`**, adding `firstEntry()`, `lastEntry()`, `pollFirstEntry()`, `pollLastEntry()`, `putFirst(k, v)`, `putLast(k, v)` and `reversed()` (a reverse-order **view**). `LinkedHashSet` implements **`SequencedSet`** (`getFirst`, `getLast`, `reversed()`...).

---

## 13. From Hashing to Trees: Why Red-Black Trees?

### 13.1 ① The problem hashing can't solve

A `HashMap` is great at **"find this exact key"**, but it knows **nothing about order**. It can't answer:

- "Give me the keys **in sorted order**."
- "What is the **smallest** key? The **largest**?"
- "Which keys fall **between 10 and 50**?" (a **range query**)
- "What is the **closest key ≤ 37**?" (floor), or "≥ 37" (ceiling)

To answer those from a `HashMap` you must scan or sort everything: O(n) or O(n log n).

### ② The need

A structure that **keeps keys sorted** at all times, with fast insert, delete and search.

### ③ Candidate 1: the binary search tree (BST)

A **BST** stores each key in a node with the rule: **left subtree < node < right subtree**.

```
            50
          /    \
        30      70
       /  \    /  \
     20   40  60   80          search 60: 50 → right → 70 → left → 60   (3 steps)
```

Search/insert/delete cost **O(height)**. For a *balanced* tree with `n` nodes, height ≈ `log₂ n` (about 20 for a million nodes). Excellent.

### 13.2 The flaw: an unbalanced BST can degenerate

Insert keys in **sorted order** (very common in real data!) into a plain BST:

```
 insert 10, 20, 30, 40, 50 in this order:

   10
     \
      20
        \
         30             height = 5 = n   → search is O(n): it became a linked list!
           \
            40
              \
               50
```

### 13.3 ③ The solution: a self-balancing BST

After every insert or delete, the tree **repairs itself** so its height stays about `log n`. There are several kinds (AVL, red-black, B-trees, ...). Java's `TreeMap` uses a **red-black tree**.

| | AVL tree | **Red-black tree** (Java) |
|---|----------|----------------------------|
| Balance strictness | Stricter (heights differ by at most 1) | Looser (longest path ≤ 2× shortest) |
| Lookups | Slightly faster (shorter tree) | Slightly slower |
| Inserts/deletes | More rotations | **Fewer rotations** (≤ 2 for insert, ≤ 3 for delete) |
| Best when | Read-heavy | **Mixed workloads**: a good general default |

### 13.4 The five red-black rules

Each node is colored **red** or **black**, and the tree always satisfies:

| # | Rule |
|---|------|
| 1 | Every node is **red or black** |
| 2 | The **root is black** |
| 3 | Missing children (`null` leaves) count as **black** |
| 4 | **A red node never has a red child** (no two reds in a row) |
| 5 | Every path from a node down to its `null` leaves has the **same number of black nodes** |

**Why this guarantees balance:** rule 5 makes all root-to-leaf paths contain the same number of black nodes. Rule 4 says reds can only be sprinkled between blacks (at most one red per black). So the **longest path is at most twice the shortest**, giving a height of at most about **2 × log₂(n + 1)**. Hence **O(log n)** for everything.

### 13.5 Repairing the tree: recoloring and rotations

After inserting a new **red** node, if its parent is also red (violating rule 4), the tree is fixed using two tools:

**Tool 1: recolor** (flip colors of the parent, uncle and grandparent).

**Tool 2: rotation** (change the shape without breaking the BST order):

```
  LEFT ROTATION at x                       RIGHT ROTATION at y  (the mirror image)

        x                    y                 y                    x
       / \                  / \               / \                  / \
      a   y      ──►       x   c             x   c      ──►      a   y
         / \              / \               / \                      / \
        b   c            a   b             a   b                    b   c

   The in-order sequence a, x, b, y, c is unchanged (BST order preserved); only the shape changes.
```

**Decision after inserting a red node whose parent is red:**

```
   Look at the UNCLE (the parent's sibling):

   uncle is RED   →  RECOLOR: parent and uncle become black, grandparent becomes red,
                     then check the grandparent (the problem may move up the tree).

   uncle is BLACK →  ROTATE (once or twice) around the grandparent, then recolor
                     (parent black, grandparent red). The problem is solved right there.
```

### 13.6 A worked example: inserting 10, 20, 30, 40, 50

A plain BST gives a chain of height 5. Watch the red-black tree stay balanced.

```
 STEP 1: insert 10            STEP 2: insert 20 (red child)      STEP 3: insert 30
                                                                  red-red violation (20 and 30),
      10(B)                        10(B)                          uncle is null (black) → LEFT ROTATE at 10 + recolor
                                      \
                                      20(R)                             20(B)
                                                                       /     \
                                                                    10(R)   30(R)
```

```
 STEP 4: insert 40             red-red (30 and 40); uncle 10 is RED → RECOLOR
                               (30 and 10 become black, 20 would turn red, but it's the root → black)

                20(B)
               /     \
            10(B)   30(B)
                       \
                       40(R)

 STEP 5: insert 50             red-red (40 and 50); uncle (30's left) is null/black → LEFT ROTATE at 30 + recolor

                20(B)
               /     \
            10(B)   40(B)
                   /     \
                30(R)   50(R)

   Final height = 3 (a plain BST would be 5). All five rules hold.
```

### 13.7 The cost of keeping order

Compared with hashing, a tree is **slower for single lookups** (`O(log n)` vs `O(1)`) but gives you **sorted order and range queries**, which hashing cannot do at all.

---

## 14. `TreeMap` and `TreeSet` Internals

### 14.1 The structure

```java
public class TreeMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V> ... {

    private final Comparator<? super K> comparator;     // null → use the keys' natural order (Comparable)
    private transient Entry<K, V> root;                  // the root of the red-black tree
    private transient int size;
    private transient int modCount;

    static final class Entry<K, V> implements Map.Entry<K, V> {
        K key;
        V value;
        Entry<K, V> left;
        Entry<K, V> right;
        Entry<K, V> parent;
        boolean color = BLACK;                           // the red/black flag
    }
}
```

```
           TreeMap
          ┌──────────────┐
          │ root ────────┼──► [50 (B)]
          │ size = 7     │       ├─ left ──► [30 (R)] ── ...
          │ comparator   │       └─ right ─► [70 (R)] ── ...
          └──────────────┘
          No hash table at all. No hashCode() is ever called.
```

### 14.2 Keys are compared, not hashed

A `TreeMap` places keys using **`compareTo`** (natural order) or a **`Comparator`** you supply:

```java
Map<String, Integer> natural = new TreeMap<>();                           // String.compareTo
Map<String, Integer> byLength = new TreeMap<>(Comparator.comparing(String::length));
Map<Integer, String> reverse = new TreeMap<>(Comparator.reverseOrder());
```

### 14.3 Complexity

| Operation | Cost |
|-----------|------|
| `get`, `put`, `remove`, `containsKey` | **O(log n)** |
| `firstKey()`, `lastKey()`, `firstEntry()`, `pollFirstEntry()` | **O(log n)** (walk to the leftmost/rightmost node) |
| `floorKey`, `ceilingKey`, `lowerKey`, `higherKey` | **O(log n)** |
| Iterate all entries in sorted order | **O(n)** (in-order traversal) |
| `size()` | O(1) |

### 14.4 `NavigableMap` / `NavigableSet` API: the real superpower

Imagine the keys on a number line:

```
        keys:    10     20     30     40     50
                 │      │      │      │      │
 lowerKey(30)    ◄──────┘      │      │      │        = 20   (strictly smaller)
 floorKey(30)           ◄──────┘      │      │        = 30   (smaller OR equal)
 floorKey(25)           ◄──────┘      │      │        = 20
 ceilingKey(25)                       ──►    │        = 30   (larger OR equal)
 higherKey(30)                               ──►      = 40   (strictly larger)
```

| Method | Meaning |
|--------|---------|
| `firstKey()` / `lastKey()` | Smallest / largest key |
| `lowerKey(k)` / `lowerEntry(k)` | Greatest key **strictly less** than `k` |
| `floorKey(k)` / `floorEntry(k)` | Greatest key **≤ `k`** |
| `ceilingKey(k)` / `ceilingEntry(k)` | Smallest key **≥ `k`** |
| `higherKey(k)` / `higherEntry(k)` | Smallest key **strictly greater** than `k` |
| `headMap(to)` / `headMap(to, inclusive)` | Keys **< to** (a **view**) |
| `tailMap(from)` / `tailMap(from, inclusive)` | Keys **≥ from** (a **view**) |
| `subMap(from, to)` | Keys in **[from, to)** (a **view**) |
| `descendingMap()` / `descendingKeySet()` | Reverse-order **views** |
| `pollFirstEntry()` / `pollLastEntry()` | Remove and return the smallest / largest entry |

`TreeSet` offers the same on elements: `first`, `last`, `lower`, `floor`, `ceiling`, `higher`, `headSet`, `tailSet`, `subSet`, `descendingSet`, `pollFirst`, `pollLast`.

### 14.5 Classic uses

| Use case | How |
|----------|-----|
| **Grade boundaries / tax brackets / price tiers** | `floorEntry(score)` finds the bracket |
| **Leaderboards, "top K"** | Sorted by score; `lastEntry`, `descendingMap` |
| **Scheduling / time lines** | `ceilingKey(now)` = next event; `headMap` = past events |
| **Range queries** ("all orders between two dates") | `subMap(from, to)` |
| **Sorted unique values** | `TreeSet` |
| **Closest value search** | `floor` / `ceiling` |

```java
TreeMap<Integer, String> grades = new TreeMap<>();
grades.put(0, "F"); grades.put(60, "D"); grades.put(70, "C"); grades.put(80, "B"); grades.put(90, "A");

System.out.println(grades.floorEntry(85).getValue());     // B   (the greatest threshold ≤ 85 is 80)
System.out.println(grades.floorEntry(59).getValue());     // F
```

### 14.6 The big gotcha: a `TreeMap` uses `compareTo`, **not** `equals`

A tree decides "same key" when **`compare(a, b) == 0`**. If your ordering is **inconsistent with `equals`**, you get surprising behavior:

```java
Set<BigDecimal> hashSet = new HashSet<>();
Set<BigDecimal> treeSet = new TreeSet<>();
for (String s : new String[]{"1.0", "1.00"}) {
    hashSet.add(new BigDecimal(s));
    treeSet.add(new BigDecimal(s));
}
System.out.println(hashSet.size());     // 2   (equals: "1.0" ≠ "1.00" because the scale differs)
System.out.println(treeSet.size());     // 1   (compareTo: 1.0 and 1.00 compare as equal → a duplicate)
```

Similarly:

```java
TreeSet<String> ci = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
ci.addAll(List.of("banana", "Apple", "cherry", "apple"));
System.out.println(ci);                 // [Apple, banana, cherry]   ("apple" is a duplicate of "Apple" for this comparator)
```

**Rule:** make your `Comparator`/`compareTo` **consistent with `equals`**, or know exactly what you're doing.

### 14.7 `null` and type rules

- With natural ordering: `put(null, ...)` → **`NullPointerException`**.
- Keys must be **mutually comparable**: mixing `Integer` and `String` keys throws `ClassCastException`.
- Values may be `null`.
- A `Comparator` must give a **consistent** ordering (no changing results), or the tree is corrupted.

### 14.8 `TreeSet` is a `TreeMap` in disguise

Exactly like `HashSet`:

```java
public class TreeSet<E> ... {
    private transient NavigableMap<E, Object> m;          // a TreeMap
    private static final Object PRESENT = new Object();
    public boolean add(E e) { return m.put(e, PRESENT) == null; }
}
```

### 14.9 Memory and speed in practice

| | `HashMap` | `TreeMap` |
|---|-----------|-----------|
| `get`/`put` | O(1) average | **O(log n)** (about 20 comparisons for a million entries) |
| Memory per entry | ~32 bytes (node) + table slot | ~40 bytes (left, right, parent, color, key, value) |
| Order | none | **sorted** |
| Cache behavior | decent | poorer (pointer chasing through nodes) |
| Extra abilities | none | range queries, floor/ceiling, first/last |

Use a `TreeMap` **only when you need the ordering features**. Otherwise a `HashMap` is faster.

### 14.10 The concurrent sorted map

`ConcurrentSkipListMap` / `ConcurrentSkipListSet` are the thread-safe sorted equivalents (a skip list instead of a red-black tree).

---

## 15. Choosing the Right Implementation

### 15.1 Decision flow

```
 Do I need key → value pairs?     Yes → Map            No (just unique elements) → Set

 Then:
   Do I need SORTED order / range queries / floor-ceiling?
        Yes → TreeMap / TreeSet                       (O(log n))
        No
         │
   Do I need a predictable iteration order (insertion or access order)?
        Yes → LinkedHashMap / LinkedHashSet           (O(1), + order, + LRU cache)
        No
         │
   Otherwise → HashMap / HashSet                      (O(1): the default)

 Special cases:
   Keys are enum constants      → EnumMap / EnumSet   (array-based, fastest)
   Several threads              → ConcurrentHashMap  (never plain HashMap)
   Constants that never change  → Map.of / Set.of / copyOf
```

### 15.2 Complexity comparison

| Operation | `HashMap` | `LinkedHashMap` | `TreeMap` |
|-----------|:---------:|:---------------:|:---------:|
| `get` / `containsKey` | O(1)* | O(1)* | **O(log n)** |
| `put` | O(1)* | O(1)* | **O(log n)** |
| `remove` | O(1)* | O(1)* | **O(log n)** |
| Iteration | O(capacity + n) | **O(n)** | **O(n)**, sorted |
| Smallest / largest key | O(n) | O(1) head/tail (Java 21) | **O(log n)** |
| Range query | O(n) | O(n) | **O(log n + k)** |
| Ordered? | no | insertion/access | **sorted** |
| `null` key | 1 allowed | 1 allowed | no (natural order) |
| Worst case | O(log n)** | O(log n)** | O(log n) |

\* average, with a good hash function. \** with treeification and `Comparable` keys.

The same table applies to `HashSet`, `LinkedHashSet` and `TreeSet` for `add`, `contains` and `remove`.

### 15.3 Memory per entry (rough, 64-bit JVM with compressed pointers)

| Structure | Approximate bytes per entry |
|-----------|-----------------------------|
| `HashMap.Node` | ~32 (+ 4 for the table slot) |
| `LinkedHashMap.Entry` | ~40 |
| `TreeMap.Entry` | ~40 |
| `HashMap.TreeNode` (in a tree bin) | ~56 to 64 |

Plus the **boxed key and value objects** (`Integer` ≈ 16 bytes each). A `HashMap<Integer, Integer>` with a million entries uses on the order of **80 to 100 MB**; this is why high-volume code sometimes uses specialized primitive-collection libraries.

---

## 16. The `Map` API in Depth

### 16.1 Basic operations

| Method | What it does | Returns |
|--------|--------------|---------|
| `put(k, v)` | Insert or **replace** | the **previous** value, or `null` |
| `get(k)` | Look up | the value, or `null` |
| `getOrDefault(k, d)` | Look up with a fallback | the value or `d` |
| `containsKey(k)` / `containsValue(v)` | Presence test | `boolean` (`containsValue` is **O(n)**) |
| `remove(k)` | Remove | the removed value, or `null` |
| `size()` / `isEmpty()` / `clear()` | Housekeeping | |
| `putAll(map)` | Copy all entries in | |

### 16.2 The "compound" methods (Java 8+): the modern way

They do check-then-act in **one operation** (no double lookup):

| Method | Meaning |
|--------|---------|
| `putIfAbsent(k, v)` | Put only if the key is **absent** (or mapped to null) |
| `computeIfAbsent(k, fn)` | If absent, compute the value with `fn(k)` and store it; returns the value |
| `computeIfPresent(k, fn)` | If present, replace the value with `fn(k, oldValue)` (removes the entry if `fn` returns `null`) |
| `compute(k, fn)` | Compute the new value from `(k, oldValue-or-null)` |
| `merge(k, v, fn)` | If absent store `v`; else store `fn(oldValue, v)` |
| `replace(k, v)` / `replaceAll(fn)` | Replace existing mappings |
| `forEach((k, v) -> ...)` | Iterate |

```java
// word counting
Map<String, Integer> freq = new HashMap<>();
for (String w : text.split(" ")) {
    freq.merge(w, 1, Integer::sum);                       // absent → 1; present → old + 1
}

// grouping into lists
Map<Integer, List<String>> byLength = new HashMap<>();
for (String w : words) {
    byLength.computeIfAbsent(w.length(), k -> new ArrayList<>()).add(w);
}
```

The old way (`if (!map.containsKey(k)) map.put(k, ...)`) does **two or three lookups**; the compound methods do **one**.

### 16.3 The three views

```java
Set<K> keys = map.keySet();                      // view of the keys
Collection<V> values = map.values();             // view of the values
Set<Map.Entry<K, V>> entries = map.entrySet();   // view of the key-value pairs
```

They are **live views** (not copies): removing from the view removes from the map; **adding** through them is **not supported**.

```java
map.keySet().remove("a");                        // removes the "a" mapping from the map
map.values().removeIf(v -> v < 0);               // removes every entry with a negative value
map.entrySet().removeIf(e -> e.getKey().startsWith("tmp"));
```

### 16.4 Iteration idioms

| Idiom | Use |
|-------|-----|
| `for (Map.Entry<K, V> e : map.entrySet())` | **Best**: key and value together, one pass |
| `map.forEach((k, v) -> ...)` | Concise (Java 8) |
| `for (K k : map.keySet())` | Only keys |
| `for (V v : map.values())` | Only values |
| `for (K k : map.keySet()) { map.get(k) }` | ✗ **Avoid**: an extra lookup per key |

### 16.5 Entries

```java
for (Map.Entry<String, Integer> e : map.entrySet()) {
    String k = e.getKey();
    Integer v = e.getValue();
    e.setValue(v + 1);                          // writes through to the map
}

Map.Entry<String, Integer> fixed = Map.entry("a", 1);        // an immutable entry (Java 9)
```

### 16.6 Immutable maps and sets (Java 9+)

```java
Map<String, Integer> m = Map.of("a", 1, "b", 2);                  // up to 10 pairs
Map<String, Integer> m2 = Map.ofEntries(Map.entry("a", 1), Map.entry("b", 2));
Set<String> s = Set.of("x", "y", "z");
Map<String, Integer> copy = Map.copyOf(existingMap);              // Java 10
```

| Rule | Detail |
|------|--------|
| Unmodifiable | Any change throws `UnsupportedOperationException` |
| No `null` keys/values/elements | `NullPointerException` |
| **No duplicates** | `Set.of(1, 1)` and `Map.of("a", 1, "a", 2)` throw `IllegalArgumentException` |
| **Iteration order is randomized** | Differs between JVM runs (deliberately, so nobody depends on it) |

---

## 17. Thread Safety and Other Maps

### 17.1 `HashMap` is not thread-safe

Concurrent updates can **lose entries**, corrupt counts, or throw exceptions. (In Java 7 a concurrent resize could even create a **cycle** in a chain and spin forever.)

| Option | How it works | Notes |
|--------|--------------|-------|
| **`ConcurrentHashMap`** | Java 8+: lock-free reads, **CAS** for empty buckets, and `synchronized` on just the **head node of a bucket** for updates | The standard choice. **No `null`s**. Provides atomic `merge`, `compute`, `putIfAbsent` |
| `Collections.synchronizedMap(map)` | Wraps every method in one lock | Simple but contended; you must lock manually while iterating |
| `Hashtable` | Every method `synchronized` | Legacy: avoid |
| `ConcurrentSkipListMap` | Thread-safe sorted map | The concurrent `TreeMap` |

```java
ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
counts.merge("apple", 1, Integer::sum);          // atomic: safe from many threads
```

### 17.2 Other `Map` implementations worth knowing

| Class | What it's for |
|-------|---------------|
| `EnumMap` | Enum keys: an array indexed by ordinal (fastest) |
| `IdentityHashMap` | Compares keys with `==` instead of `equals` |
| `WeakHashMap` | Keys held by **weak references**: entries disappear when keys are garbage collected (caches) |
| `Properties` | `Hashtable<Object,Object>` for configuration (strings) |

---

## 18. A Senior Engineer's Checklist

**Key design**
1. Use **immutable** keys (`String`, `Integer`, enums, records). Never mutate a field used in `hashCode` while the object is in a hash collection.
2. Always override **`equals` and `hashCode` together**, from the same fields (see the Object class guide). Use `Objects.hash(...)` or a record.
3. If your keys might collide heavily, make them `Comparable` so treeified bins stay O(log n).
4. For a `TreeMap`/`TreeSet`, make the comparator **consistent with `equals`**.

**Sizing and performance**
5. If you know the size, **pre-size**: `HashMap.newHashMap(n)` (Java 19+) or `new HashMap<>((int)(n / 0.75f) + 1)`.
6. Remember `HashMap` **never shrinks**; for a map that grows large then empties, create a new one.
7. Iterating a `HashMap` costs **O(capacity)**; don't over-allocate.
8. Prefer **`entrySet()`** iteration over `keySet()` + `get()`.
9. Use **`merge`, `computeIfAbsent`, `getOrDefault`** instead of check-then-put (one lookup, and atomic in `ConcurrentHashMap`).
10. Boxing costs memory and time: for very large int→int maps consider specialized libraries.
11. Don't use a `TreeMap` unless you need sorted order or range queries.

**Correctness**
12. **Never rely on `HashMap`/`HashSet` iteration order**; use `LinkedHashMap`/`TreeMap` if order matters.
13. Don't modify a map while iterating it directly; use `removeIf`, the iterator's `remove`, or collect keys first.
14. `null` from `get` is ambiguous: use `containsKey` or `getOrDefault` when `null` values are allowed.
15. Expose maps defensively: `Map.copyOf` or `Collections.unmodifiableMap`.
16. Use `ConcurrentHashMap` (never plain `HashMap`) across threads.

**Design**
17. Declare variables by **interface** (`Map<K, V> m = new HashMap<>();`), so you can swap implementations.
18. For enum keys use `EnumMap`/`EnumSet`.
19. For an LRU cache, `LinkedHashMap` with `accessOrder = true` and `removeEldestEntry` (single-threaded), or a proven caching library for production concurrency.
20. Know the failure modes: hash flooding (treeification helps), mutable keys, poor `hashCode`.

---

## 19. Version-by-Version History

| Version | Year | Change |
|---------|------|--------|
| **Java 1.0** | 1996 | `Hashtable` (synchronized, no nulls) |
| **Java 1.2** | 1998 | **Collections Framework**: `HashMap`, `HashSet`, `TreeMap`, `TreeSet`, `Map`, `Set` |
| **Java 1.4** | 2002 | `LinkedHashMap`, `LinkedHashSet`, `IdentityHashMap` |
| **Java 5** | 2004 | Generics, `EnumMap`/`EnumSet`, `ConcurrentHashMap` |
| **Java 6** | 2006 | `NavigableMap`/`NavigableSet`, `ConcurrentSkipListMap` |
| **Java 7** | 2011 | Diamond operator |
| **Java 8** | 2014 | **HashMap overhaul**: treeification (red-black bins), tail insertion, lo/hi resize split, simpler spread function. New map methods: `computeIfAbsent`, `merge`, `forEach`, `getOrDefault`. `ConcurrentHashMap` rewritten (CAS + bin locks) |
| **Java 9** | 2017 | `Set.of`, `Map.of`, `Map.ofEntries`, `Map.entry` |
| **Java 10** | 2018 | `Set.copyOf`, `Map.copyOf` |
| **Java 19** | 2022 | `HashMap.newHashMap(n)`, `HashSet.newHashSet(n)`, `LinkedHashMap.newLinkedHashMap(n)` (correct pre-sizing) |
| **Java 21** | 2023 | **Sequenced collections**: `SequencedSet`, `SequencedMap` (first/last, `reversed()`) |

---

## 20. Practical Programs

Save each as the named file, compile with `javac FileName.java`, run with `java ClassName`.

### 20.1 `HashDemo`: `HashMap`, `HashSet` and set algebra

```java
import java.util.*;

public class HashDemo {
    public static void main(String[] args) {

        // ---------- HashMap basics ----------
        Map<String, Integer> stock = new HashMap<>();
        System.out.println(stock.put("apple", 3));         // null   (no previous value)
        System.out.println(stock.put("apple", 5));         // 3      (old value returned)
        stock.put("kiwi", 1);
        stock.put(null, 0);                                // one null key is allowed
        stock.put("fig", null);                            // null values are allowed
        System.out.println(stock.get(null));               // 0
        System.out.println(stock.get("fig") + " " + stock.containsKey("fig"));
        System.out.println(stock.getOrDefault("zzz", -1)); // -1
        stock.putIfAbsent("kiwi", 99);                     // already present: unchanged
        stock.merge("apple", 10, Integer::sum);            // 5 + 10
        stock.computeIfAbsent("pear", k -> k.length());    // pear = 4
        System.out.println(stock.get("kiwi") + " " + stock.get("apple") + " " + stock.get("pear"));
        System.out.println(stock.size());

        // ---------- word frequency ----------
        String text = "to be or not to be that is the question to be";
        Map<String, Integer> freq = new HashMap<>();
        for (String w : text.split(" ")) {
            freq.merge(w, 1, Integer::sum);
        }
        System.out.println(new TreeMap<>(freq));           // a sorted COPY, only for nice display

        // ---------- HashSet: no duplicates; order = bucket order ----------
        Set<Integer> set = new HashSet<>();
        for (int n : new int[]{100, 5, 33, 17, 5, 100}) {
            System.out.print(set.add(n) + " ");            // add returns false for a duplicate
        }
        System.out.println();
        System.out.println(set);

        // ---------- set algebra (TreeSet only to print in sorted order) ----------
        Set<Integer> a = new TreeSet<>(List.of(1, 2, 3, 4));
        Set<Integer> b = new TreeSet<>(List.of(3, 4, 5, 6));

        Set<Integer> union = new TreeSet<>(a);
        union.addAll(b);
        Set<Integer> inter = new TreeSet<>(a);
        inter.retainAll(b);
        Set<Integer> diff = new TreeSet<>(a);
        diff.removeAll(b);
        Set<Integer> sym = new TreeSet<>(union);
        sym.removeAll(inter);

        System.out.println(union + " " + inter + " " + diff + " " + sym);

        // ---------- deduplicate a list, keeping first-seen order ----------
        List<Integer> unique = new ArrayList<>(new LinkedHashSet<>(List.of(3, 1, 3, 2, 1)));
        System.out.println(unique);
    }
}
```

Output:
```
null
3
0
null true
-1
1 15 4
5
{be=3, is=1, not=1, or=1, question=1, that=1, the=1, to=3}
true true true true false false 
[33, 17, 100, 5]
[1, 2, 3, 4, 5, 6] [3, 4] [1, 2] [1, 2, 5, 6]
[3, 1, 2]
```

The `[33, 17, 100, 5]` order is exactly the bucket order explained in section 11.1.

### 20.2 `MyHashMapDemo`: build your own `HashMap` and watch it resize

A miniature version of the real algorithm: array of buckets, hash spreading, chaining with tail insertion, a load factor of 0.75, and the **lo/hi split** during resize.

```java
class MyHashMap<K, V> {

    private static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Node<K, V>[] table;
    private int size;
    private int threshold;
    private final float loadFactor = 0.75f;

    @SuppressWarnings("unchecked")
    MyHashMap(int initialCapacity) {                    // must be a power of two
        table = (Node<K, V>[]) new Node[initialCapacity];
        threshold = (int) (initialCapacity * loadFactor);
    }

    // the same spread function as java.util.HashMap
    private static int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    private static boolean sameKey(Node<?, ?> e, int hash, Object key) {
        return e.hash == hash && (e.key == key || (key != null && key.equals(e.key)));
    }

    public V put(K key, V value) {
        int h = hash(key);
        int i = (table.length - 1) & h;                 // bucket index

        for (Node<K, V> e = table[i]; e != null; e = e.next) {
            if (sameKey(e, h, key)) {                   // existing key: replace the value
                V old = e.value;
                e.value = value;
                return old;
            }
        }

        Node<K, V> node = new Node<>(h, key, value, null);
        if (table[i] == null) {
            table[i] = node;
        } else {
            Node<K, V> last = table[i];
            while (last.next != null) last = last.next; // append at the TAIL (Java 8 style)
            last.next = node;
        }

        if (++size > threshold) resize();
        return null;
    }

    public V get(Object key) {
        int h = hash(key);
        for (Node<K, V> e = table[(table.length - 1) & h]; e != null; e = e.next) {
            if (sameKey(e, h, key)) return e.value;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTab = table;
        int oldCap = oldTab.length;
        int newCap = oldCap << 1;                       // double
        System.out.println(">>> resize: " + oldCap + " -> " + newCap
                + " (threshold " + (int) (newCap * loadFactor) + ")");

        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        for (int j = 0; j < oldCap; j++) {
            Node<K, V> e = oldTab[j];
            if (e == null) continue;

            Node<K, V> loHead = null, loTail = null;    // nodes that STAY at index j
            Node<K, V> hiHead = null, hiTail = null;    // nodes that MOVE to index j + oldCap
            while (e != null) {
                Node<K, V> next = e.next;
                e.next = null;
                if ((e.hash & oldCap) == 0) {           // the one new bit is 0: stay
                    if (loTail == null) loHead = e; else loTail.next = e;
                    loTail = e;
                } else {                                // the one new bit is 1: move
                    if (hiTail == null) hiHead = e; else hiTail.next = e;
                    hiTail = e;
                }
                e = next;
            }
            newTab[j] = loHead;
            newTab[j + oldCap] = hiHead;
        }
        table = newTab;
        threshold = (int) (newCap * loadFactor);
    }

    public void print() {
        System.out.println("capacity=" + table.length + " size=" + size + " threshold=" + threshold);
        for (int i = 0; i < table.length; i++) {
            StringBuilder sb = new StringBuilder("[" + i + "] ");
            Node<K, V> e = table[i];
            if (e == null) sb.append("-");
            while (e != null) {
                sb.append(e.key).append("=").append(e.value);
                if (e.next != null) sb.append(" -> ");
                e = e.next;
            }
            System.out.println(sb);
        }
    }
}

public class MyHashMapDemo {
    public static void main(String[] args) {
        MyHashMap<Integer, String> map = new MyHashMap<>(4);

        map.put(1, "a");
        map.put(5, "b");                  // 5 & 3 = 1  → collides with key 1
        map.put(2, "c");
        map.print();

        System.out.println("--- put(9, \"d\"): 9 & 3 = 1, size becomes 4 > threshold 3 ---");
        map.put(9, "d");
        map.print();

        System.out.println(map.get(5));           // b
        System.out.println(map.get(7));           // null
        System.out.println(map.put(5, "B"));      // b   (old value returned)
        System.out.println(map.get(5));           // B
    }
}
```

Output:
```
capacity=4 size=3 threshold=3
[0] -
[1] 1=a -> 5=b
[2] 2=c
[3] -
--- put(9, "d"): 9 & 3 = 1, size becomes 4 > threshold 3 ---
>>> resize: 4 -> 8 (threshold 6)
capacity=8 size=4 threshold=6
[0] -
[1] 1=a -> 9=d
[2] 2=c
[3] -
[4] -
[5] 5=b
[6] -
[7] -
b
null
b
B
```

Study it: key `5` **moved** from bucket 1 to bucket 5 (`5 & 4 != 0`), keys `1` and `9` **stayed** in bucket 1, and the chain order was preserved. That is the real Java 8 resize algorithm in miniature.

### 20.3 `CollisionDemo`: the hash pipeline and real collisions

```java
import java.util.HashMap;
import java.util.Map;

public class CollisionDemo {

    // the spread function copied from java.util.HashMap
    static int spread(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    static int bucket(Object key, int capacity) {
        return (capacity - 1) & spread(key);
    }

    public static void main(String[] args) {
        String[] keys = {"Aa", "BB", "C#", "Ali", "Sara", "John"};

        for (String k : keys) {
            System.out.println(k + ": hashCode=" + k.hashCode()
                    + ", spread=" + spread(k)
                    + ", bucket(16)=" + bucket(k, 16));
        }

        Map<String, Integer> map = new HashMap<>();
        for (String k : keys) map.put(k, k.length());

        // "Aa", "BB" and "C#" share a hash code and a bucket, yet remain distinct keys
        System.out.println(map.get("Aa") + " " + map.get("BB") + " " + map.get("C#"));
        System.out.println("Aa".equals("BB"));
    }
}
```

Output:
```
Aa: hashCode=2112, spread=2112, bucket(16)=0
BB: hashCode=2112, spread=2112, bucket(16)=0
C#: hashCode=2112, spread=2112, bucket(16)=0
Ali: hashCode=65918, spread=65919, bucket(16)=15
Sara: hashCode=2569501, spread=2569530, bucket(16)=10
John: hashCode=2314539, spread=2314504, bucket(16)=8
2 2 2
false
```

### 20.4 `LinkedDemo`: insertion order, access order and an LRU cache

```java
import java.util.*;

class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    LRUCache(int capacity) {
        super(16, 0.75f, true);                  // true = access order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;                // evict the least recently used entry
    }
}

public class LinkedDemo {
    public static void main(String[] args) {
        Map<String, Integer> hash = new HashMap<>();
        Map<String, Integer> linked = new LinkedHashMap<>();
        for (String k : new String[]{"c", "a", "b"}) {
            int v = k.charAt(0) - 'a' + 1;
            hash.put(k, v);
            linked.put(k, v);
        }
        System.out.println("HashMap      : " + hash);
        System.out.println("LinkedHashMap: " + linked);

        linked.put("a", 100);                    // existing key: position does NOT change
        System.out.println("after put(a)  : " + linked);

        // ---- LRU cache ----
        LRUCache<String, Integer> cache = new LRUCache<>(3);
        cache.put("A", 1);
        cache.put("B", 2);
        cache.put("C", 3);
        System.out.println(cache);               // A, B, C

        cache.get("A");                          // A becomes the most recently used
        System.out.println(cache);               // B, C, A

        cache.put("D", 4);                       // size 4 > 3 → evict the eldest (B)
        System.out.println(cache);               // C, A, D

        // ---- LinkedHashSet removes duplicates and keeps first-seen order ----
        System.out.println(new ArrayList<>(new LinkedHashSet<>(List.of(3, 1, 3, 2, 1))));
    }
}
```

Output:
```
HashMap      : {a=1, b=2, c=3}
LinkedHashMap: {c=3, a=1, b=2}
after put(a)  : {c=3, a=100, b=2}
{A=1, B=2, C=3}
{B=2, C=3, A=1}
{C=3, A=1, D=4}
[3, 1, 2]
```

### 20.5 `TreeDemo`: sorted structures, navigation and the `equals` vs `compareTo` gotcha

```java
import java.math.BigDecimal;
import java.util.*;

public class TreeDemo {
    public static void main(String[] args) {

        // ---- floorEntry: finding the bracket ----
        TreeMap<Integer, String> grades = new TreeMap<>();
        grades.put(0, "F");
        grades.put(60, "D");
        grades.put(70, "C");
        grades.put(80, "B");
        grades.put(90, "A");
        for (int s : new int[]{95, 85, 72, 60, 59, 100}) {
            System.out.println(s + " -> " + grades.floorEntry(s).getValue());
        }

        // ---- TreeSet navigation ----
        TreeSet<Integer> nums = new TreeSet<>(List.of(10, 20, 30, 40, 50));
        System.out.println(nums.floor(25) + " " + nums.ceiling(25) + " "
                + nums.lower(30) + " " + nums.higher(30));
        System.out.println(nums.first() + " " + nums.last());
        System.out.println(nums.headSet(30) + " " + nums.headSet(30, true) + " "
                + nums.tailSet(30) + " " + nums.subSet(20, 40));
        System.out.println(nums.descendingSet());
        System.out.println(nums.pollFirst() + " " + nums);

        // ---- comparators decide "equal" ----
        TreeSet<String> ci = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        ci.addAll(List.of("banana", "Apple", "cherry", "apple"));
        System.out.println(ci);

        TreeSet<Integer> rev = new TreeSet<>(Comparator.reverseOrder());
        rev.addAll(List.of(3, 1, 2));
        System.out.println(rev);

        // ---- compareTo vs equals ----
        Set<BigDecimal> hs = new HashSet<>();
        Set<BigDecimal> ts = new TreeSet<>();
        for (String s : new String[]{"1.0", "1.00"}) {
            hs.add(new BigDecimal(s));
            ts.add(new BigDecimal(s));
        }
        System.out.println("HashSet size = " + hs.size() + ", TreeSet size = " + ts.size());

        // ---- null keys ----
        try {
            new TreeMap<String, Integer>().put(null, 1);
        } catch (NullPointerException e) {
            System.out.println("TreeMap rejects a null key");
        }
    }
}
```

Output:
```
95 -> A
85 -> B
72 -> C
60 -> D
59 -> F
100 -> A
20 30 20 40
10 50
[10, 20] [10, 20, 30] [30, 40, 50] [20, 30]
[50, 40, 30, 20, 10]
10 [20, 30, 40, 50]
[Apple, banana, cherry]
[3, 2, 1]
HashSet size = 2, TreeSet size = 1
TreeMap rejects a null key
```

### 20.6 `TreeifyExperiment`: feel the value of treeification (numbers vary)

All keys get the **same hash code** so they all land in **one bucket**. One key class is `Comparable` (the tree can order them), the other is not.

```java
import java.util.HashMap;
import java.util.Map;

public class TreeifyExperiment {

    // every key has hash 42: they ALL collide
    static class SlowKey {
        final int id;
        SlowKey(int id) { this.id = id; }
        @Override public int hashCode() { return 42; }
        @Override public boolean equals(Object o) { return o instanceof SlowKey k && k.id == id; }
    }

    // same collisions, but the keys are Comparable: a red-black bin can order them
    static class FastKey implements Comparable<FastKey> {
        final int id;
        FastKey(int id) { this.id = id; }
        @Override public int hashCode() { return 42; }
        @Override public boolean equals(Object o) { return o instanceof FastKey k && k.id == id; }
        @Override public int compareTo(FastKey o) { return Integer.compare(id, o.id); }
    }

    static long run(Map<Object, Integer> map, java.util.function.IntFunction<Object> keyFactory, int n) {
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) map.put(keyFactory.apply(i), i);
        long sum = 0;
        for (int i = 0; i < n; i++) sum += map.get(keyFactory.apply(i));
        return (System.nanoTime() - start) / 1_000_000;
    }

    public static void main(String[] args) {
        int n = 10_000;
        System.out.println("Non-Comparable colliding keys: " + run(new HashMap<>(), SlowKey::new, n) + " ms");
        System.out.println("Comparable colliding keys    : " + run(new HashMap<>(), FastKey::new, n) + " ms");
    }
}
```

**What you should see (the numbers differ per machine, the pattern doesn't):** the `Comparable` version is **dramatically faster** (a few milliseconds versus hundreds), because its single crowded bucket became a red-black tree with **O(log n)** searches, while the non-comparable bucket must still be searched node by node. Both are far slower than a well-hashed map, which is the whole point of having a good `hashCode()`.

### 20.7 Try it yourself

1. Extend `MyHashMapDemo` with a `remove(key)` method that unlinks a node, then test it on a chained bucket.
2. In `MyHashMap`, add `containsKey` and a `size()` getter. Print the load factor after each `put`.
3. Change `MyHashMap` to use **head insertion** (Java 7 style) and observe how the chain order differs after a resize.
4. Insert the keys `16, 32, 48, 64` (all multiples of 16) into a `MyHashMap` of capacity 16 and explain why they collide (look at their low 4 bits).
5. Write a `HashMap` word-frequency counter, then print the words sorted by **frequency** (hint: copy the entries into a list and sort).
6. Build an LRU cache of capacity 2 and trace its order after a series of `put` and `get` calls on paper before running it.
7. Use a `TreeMap` to implement a tax calculator with `floorEntry` over tax brackets.
8. Make a `TreeSet<String>` with a comparator that sorts by length, then by alphabet. Explain why two different words of the same length can still both be stored.
9. Run `TreeifyExperiment` with `n = 20_000` and compare the growth of both timings.
10. Draw (on paper) the red-black tree after inserting `5, 3, 8, 1, 4, 7, 9, 2` and check all five rules.

---

## 21. Common Mistakes

| # | Mistake | What happens | Fix |
|---|---------|--------------|-----|
| 1 | Overriding `equals` but not `hashCode` for a key class | Equal keys land in different buckets → duplicates, lookups fail | Override both, from the same fields |
| 2 | Using **mutable** objects as keys and then changing them | The entry is "lost" (stored in the old bucket) | Use immutable keys |
| 3 | Depending on `HashMap`/`HashSet` iteration order | Order changes across resizes/JDKs | `LinkedHashMap` / `TreeMap` for order |
| 4 | `new HashMap<>(1000)` thinking it holds 1000 without resizing | Threshold is 768; it resizes | `HashMap.newHashMap(1000)` or `(int)(1000/0.75f) + 1` |
| 5 | A constant or poor `hashCode()` | Everything collides → O(n) (O(log n) with trees) | Use `Objects.hash(...)` / a good formula |
| 6 | Modifying a map while iterating it | `ConcurrentModificationException` | `removeIf`, iterator `remove`, or collect then modify |
| 7 | Using a plain `HashMap` from several threads | Lost updates, corrupted state | `ConcurrentHashMap` |
| 8 | `map.get(k) == null` to test presence when `null` values are allowed | Ambiguous | `containsKey(k)` |
| 9 | Check-then-put (`if (!map.containsKey(k)) map.put(k, v)`) | Multiple lookups; not atomic | `putIfAbsent`, `computeIfAbsent`, `merge` |
| 10 | Iterating `keySet()` and calling `get(k)` for each | An extra lookup per key | Iterate `entrySet()` |
| 11 | `put(null, ...)` into a `TreeMap` with natural order | `NullPointerException` | Don't use `null` keys, or supply a null-safe comparator |
| 12 | A `TreeSet` comparator inconsistent with `equals` | "Duplicates" silently dropped | Compare on all identifying fields |
| 13 | Mixing key types in a `TreeMap` | `ClassCastException` | Keep one comparable key type |
| 14 | Expecting `HashSet` to keep insertion order | It won't | `LinkedHashSet` |
| 15 | `map.containsValue(v)` in a hot loop | O(n) every call | Maintain a reverse map or set |
| 16 | `Set.of(1, 1)` or `Map.of("a", 1, "a", 2)` | `IllegalArgumentException` (duplicates) | Remove duplicates |
| 17 | Putting `null` in `Map.of`, `Set.of`, `ConcurrentHashMap` | `NullPointerException` | Avoid `null` |
| 18 | Assuming `keySet().add(...)` works | `UnsupportedOperationException` | Views don't support add |
| 19 | Using a `TreeMap` when you don't need ordering | Slower (O(log n)) | `HashMap` |
| 20 | Huge map of boxed integers without thinking about memory | High memory use | Pre-size; consider primitive-specialized collections |
| 21 | Assuming `HashMap` shrinks after removals | It never shrinks | Create a new map if needed |
| 22 | Using `Stack`-like or index logic on a `Set` (`set.get(0)`) | Doesn't exist | Use a `List`, or `LinkedHashSet` + iterator/`getFirst` (Java 21) |

---

## 22. Cheat Sheet, Summary and Quiz

### 22.1 One-page internals cheat sheet

```
 HashMap:      Node<K,V>[] table  (length = power of 2, default 16, lazily allocated)
               Node = { int hash; K key; V value; Node next; }
               hash   = h ^ (h >>> 16)             spread
               index  = (n - 1) & hash             mask (≡ hash % n)
               same key:  hash ==  &&  (key == k || key.equals(k))
               chain  = linked list per bucket, new nodes appended at the TAIL
               resize when ++size > capacity × 0.75;  capacity doubles; lo/hi split by (hash & oldCap)
               treeify:   chain > 8 AND table ≥ 64  → red-black tree;   (table < 64 → resize instead)
               untreeify: tree ≤ 6 nodes → chain
 HashSet:      HashMap<E, PRESENT>
 LinkedHashMap: HashMap + doubly linked list (before/after); insertion or access order; removeEldestEntry → LRU
 TreeMap:      red-black tree (Entry: key, value, left, right, parent, color); compareTo/Comparator; O(log n); sorted
 TreeSet:      TreeMap<E, PRESENT>
```

### 22.2 Complexity at a glance

| | get / contains | put / add | remove | Ordered | Range / floor / ceiling |
|---|:---:|:---:|:---:|:---:|:---:|
| `HashMap` / `HashSet` | O(1) | O(1) | O(1) | ✗ | ✗ |
| `LinkedHashMap` / `LinkedHashSet` | O(1) | O(1) | O(1) | insertion/access | ✗ |
| `TreeMap` / `TreeSet` | O(log n) | O(log n) | O(log n) | **sorted** | **✓** |

### 22.3 Summary in simple points

1. **`Set`** = no duplicates with fast `contains`; **`Map`** = unique keys → values with fast `get`. Hash-based versions give up **positional access** to gain **speed**.
2. A **hash table** turns a key into an **array index** via `hashCode`: hash → spread (`h ^ h>>>16`) → mask (`& (n-1)`) → **bucket**. Collisions are unavoidable (pigeonhole).
3. Java handles collisions by **separate chaining** (a linked list per bucket) because it is simpler than open addressing, deletion is easy, and chains can be upgraded to trees.
4. Each entry is a **`Node`** with `hash`, `key`, `value` and `next`. The table length is always a **power of two**.
5. **Load factor 0.75**: when `size > capacity × 0.75`, the table **doubles** and entries are **rehashed** (a node either stays at index `j` or moves to `j + oldCapacity`, decided by `hash & oldCapacity`). The amortized cost per `put` is O(1).
6. **Treeification (Java 8+):** a chain longer than **8**, in a table of at least **64** buckets, becomes a **red-black tree** (O(log n)); it reverts when it shrinks to **6**. In a smaller table Java **resizes** instead.
7. **`HashSet`** is a `HashMap` with a dummy value; `LinkedHashSet` and `TreeSet` are the same trick over `LinkedHashMap` and `TreeMap`.
8. **`LinkedHashMap`** adds a **doubly linked list** to the hash table: insertion order (or **access order**, giving a built-in **LRU cache** with `removeEldestEntry`), and **O(size)** iteration.
9. **`TreeMap`/`TreeSet`** use a **red-black tree** (self-balancing BST): **sorted keys**, **range queries**, `floor`/`ceiling`, **O(log n)** operations, and **no `null` keys** (with natural ordering). They use **`compareTo`/`Comparator`, not `equals`/`hashCode`**.
10. The five red-black rules keep the longest path at most **twice** the shortest, so the height stays about **log n**, via **recoloring and rotations**.
11. Always override **`equals` + `hashCode`** together, use **immutable keys**, **pre-size** large maps, never rely on hash iteration order, and use **`ConcurrentHashMap`** across threads.

### 22.4 Mini Quiz

1. What two promises does a `Set` make? What does a `Map` promise about keys?
2. Why don't hash-based collections support `get(index)`?
3. What are the three steps that turn a key into a bucket index?
4. Why does `HashMap` XOR the high 16 bits into the low 16 bits?
5. Why is the table length always a power of two? What does `(n - 1) & hash` replace?
6. Which two constants decide when a chain is turned into a tree? What happens if the table is smaller than 64?
7. At what size does a default `HashMap` resize? What is the new capacity?
8. During a resize from 4 to 8 buckets, which of the keys `1, 5, 9` move, and where?
9. Why did Java choose chaining over open addressing?
10. What does `HashSet.add` return for a duplicate, and how does it detect it?
11. How does `LinkedHashMap` keep order, and what does `accessOrder = true` do?
12. How do you implement an LRU cache with `LinkedHashMap`?
13. Why can a plain BST be slow, and what do red-black trees guarantee?
14. State two of the five red-black rules.
15. Why does `TreeMap` reject `null` keys, and why can `TreeSet<BigDecimal>` treat `1.0` and `1.00` as duplicates while `HashSet` doesn't?
16. What does `floorEntry(85)` return for keys `0, 60, 70, 80, 90`?
17. What happens to a `HashMap` entry whose key is mutated after insertion?
18. Why is `new HashMap<>(1000)` not enough to hold 1000 entries without resizing?
19. Which map would you use for a thread-safe cache, for sorted iteration, and for enum keys?
20. In Java 7 vs 8, what changed about how nodes are added to a chain?

<details>
<summary>Click for answers</summary>

1. A `Set` promises **no duplicates** (by `equals`) and **fast membership tests**. A `Map` promises **unique keys** with one value per key.
2. Elements are placed by **hash**, not by insertion position, so there is no meaningful index.
3. **`hashCode()` → spread (`h ^ (h >>> 16)`) → mask (`& (n - 1)`)**.
4. The index uses only the **low bits**; XORing the high bits down lets **high bits influence the bucket**, reducing collisions.
5. A power of two lets the cheap mask `hash & (n - 1)` replace `hash % n` (and it works for negative hashes), and makes the lo/hi resize split possible.
6. **`TREEIFY_THRESHOLD = 8`** and **`MIN_TREEIFY_CAPACITY = 64`**. If the table is smaller than 64, Java **resizes** instead of treeifying. (`UNTREEIFY_THRESHOLD = 6` turns trees back.)
7. When **`size` exceeds 12** (the 13th entry): capacity goes **16 → 32**.
8. `5` moves to bucket **5** (`5 & 4 != 0`); `1` and `9` **stay** in bucket 1 (`& 4 == 0`).
9. Chaining is **simpler**, **deletion is trivial**, it **degrades gracefully** at high load, and chains can be **upgraded to balanced trees**.
10. It returns **`false`**. It calls `map.put(e, PRESENT)`, which returns the **previous value** (non-null for an existing element).
11. It adds a **doubly linked list** (`before`/`after`) through the entries. With `accessOrder = true`, each `get`/`put` of an existing key **moves the entry to the end**, so the head is the least recently used.
12. Extend `LinkedHashMap` with `super(16, 0.75f, true)` and override **`removeEldestEntry`** to return `size() > capacity`.
13. A plain BST can **degenerate into a linked list** with sorted inserts (O(n)). Red-black trees **guarantee height ≈ O(log n)**, so all operations are **O(log n)**.
14. Any two: every node is red or black; the root is black; null leaves are black; a red node has no red child; every root-to-leaf path has the same number of black nodes.
15. It must **compare** keys and you can't compare against `null`. `TreeSet` uses **`compareTo`** (where `1.0` and `1.00` compare as 0), while `HashSet` uses **`equals`/`hashCode`** (where they differ because of scale).
16. The entry for key **80** (`B`): the greatest key ≤ 85.
17. Its stored hash is stale, so lookups go to the **wrong bucket** and the entry is effectively **lost**.
18. 1000 rounds up to a capacity of **1024**, whose threshold is **768**, so it resizes after 768 entries. Use `HashMap.newHashMap(1000)` or `(int)(1000/0.75f) + 1`.
19. **`ConcurrentHashMap`** (thread-safe), **`TreeMap`** (sorted), **`EnumMap`** (enum keys).
20. Java 7 inserted new nodes at the **head** of a chain and could create a **cycle** during concurrent resize; Java 8 inserts at the **tail**, preserves order on resize, and adds **treeification**.

</details>

---

*Practice tip: the fastest way to truly understand a hash table is to build one. Re-type `MyHashMap` from memory, add `remove`, and print the table after every operation. Once you can predict the bucket layout before you run the code, you understand `HashMap` the way a senior engineer does.*
