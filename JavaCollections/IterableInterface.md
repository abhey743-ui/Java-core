# Java Iterable, Iterator and the Fail-Fast Mechanism

> Why traversal needs an abstraction, how `Iterable` and `Iterator` work, how an iterator is implemented inside a collection (inner class, cursor, `hasNext()`, `next()`), how fail-fast detection throws `ConcurrentModificationException`, and why the collection and the iterator are deliberately separate objects.

---

## Table of Contents

1. [The problem: every structure is traversed differently](#1-the-problem-every-structure-is-traversed-differently)
2. [The abstraction: `Iterable` and `Iterator`](#2-the-abstraction-iterable-and-iterator)
3. [The Iterator pattern: a pointer that holds traversal state](#3-the-iterator-pattern-a-pointer-that-holds-traversal-state)
4. [How the for-each loop really works](#4-how-the-for-each-loop-really-works)
5. [Internal implementation mechanics](#5-internal-implementation-mechanics)
6. [Building your own Iterable collection](#6-building-your-own-iterable-collection)
7. [The fail-fast mechanism](#7-the-fail-fast-mechanism)
8. [Safe ways to modify while iterating](#8-safe-ways-to-modify-while-iterating)
9. [Why Iterable and Iterator are separate (separation of concerns)](#9-why-iterable-and-iterator-are-separate-separation-of-concerns)
10. [Related topics: ListIterator, fail-safe iterators, Map](#10-related-topics-listiterator-fail-safe-iterators-map)
11. [Interview questions and answers](#11-interview-questions-and-answers)
12. [Cheat sheet](#12-cheat-sheet)

---

## 1. The problem: every structure is traversed differently

Different collections store data in completely different ways, so "go through every element" looks different for each one.

```
ArrayList  (array, indices)          LinkedList  (nodes, pointers)
+----+----+----+----+                head
| 10 | 20 | 30 | 40 |                  |
+----+----+----+----+                  v
  0    1    2    3                  [10|o]->[20|o]->[30|o]->[40|null]
```

Without an abstraction, the code that walks each one is different:

```java
// ArrayList: use indices
for (int i = 0; i < list.size(); i++) {
    System.out.println(list.get(i));
}

// LinkedList (hand-made): follow node references
Node cur = head;
while (cur != null) {
    System.out.println(cur.data);
    cur = cur.next;
}

// HashSet: no indices, no obvious "next" at all (buckets, chains, tree bins)
// TreeSet: needs an in-order tree walk
// Queue / PriorityQueue: heap order or circular array
```

Problems:

1. The caller must **know the internal structure** of every collection.
2. Code can't be **reused**: a method that prints a list can't print a set.
3. Switching `ArrayList` to `LinkedList` **breaks** the traversal code. (`list.get(i)` on a `LinkedList` even silently turns a loop into O(n^2).)

```
 Caller code  --needs to know-->  [ArrayList internals]
              --needs to know-->  [LinkedList internals]
              --needs to know-->  [HashSet internals]
              ...                  => tight coupling, no reuse
```

We want **one uniform way** to traverse **anything**.

---

## 2. The abstraction: `Iterable` and `Iterator`

Java solves this with two small interfaces (the **Iterator design pattern**).

### 2.1 `Iterable<T>` (in `java.lang`)

"I can give you something that walks over my elements."

```java
public interface Iterable<T> {
    Iterator<T> iterator();                         // the one abstract method

    default void forEach(Consumer<? super T> action) { ... }
    default Spliterator<T> spliterator() { ... }
}
```

### 2.2 `Iterator<E>` (in `java.util`)

"I walk, one element at a time."

```java
public interface Iterator<E> {
    boolean hasNext();                 // is there another element?
    E next();                          // give me the next one and move forward

    default void remove() { throw new UnsupportedOperationException(); }
    default void forEachRemaining(Consumer<? super E> action) { ... }
}
```

### 2.3 How they fit in the hierarchy

```
        Iterable<E>              <-- "can be traversed"   (iterator())
            |
       Collection<E>             <-- every Collection IS Iterable
       /     |     \
    List    Set    Queue
      |      |       |
  ArrayList HashSet  ArrayDeque ...   each provides its OWN iterator() implementation


     Iterator<E>    <-- the "walker" object returned by iterator()
```

Because `Collection extends Iterable`, **every** List, Set and Queue can be traversed the same way:

```java
Iterator<String> it = anyCollection.iterator();
while (it.hasNext()) {
    String s = it.next();
    System.out.println(s);
}
```

This code is **identical** for `ArrayList`, `LinkedList`, `HashSet`, `TreeSet`, `ArrayDeque`... The caller knows nothing about indices or nodes.

```
 Caller ---> Iterator interface ---> [ArrayList's iterator]
                                 ---> [LinkedList's iterator]
                                 ---> [HashSet's iterator]
 (depends only on hasNext()/next())   (each hides its own details)
```

---

## 3. The Iterator pattern: a pointer that holds traversal state

Calling `list.iterator()` hands you an **Iterator object**: think of it as a **bookmark / pointer** that remembers *where you are* in the traversal.

```java
List<String> list = new ArrayList<>(List.of("A", "B", "C"));
Iterator<String> it = list.iterator();
```

The cursor sits **between** elements. `next()` returns the element after the cursor and moves the cursor forward.

```
 list:       [  A  ][  B  ][  C  ]
 index:         0      1      2

 start:      ^                         cursor = 0   hasNext() = true
 next() -> A    ^                      cursor = 1
 next() -> B           ^               cursor = 2
 next() -> C                  ^        cursor = 3   hasNext() = false
 next()  -> NoSuchElementException
```

Rules:

- `hasNext()` first, then `next()`. Calling `next()` with nothing left throws **`NoSuchElementException`**.
- An iterator is **single-use and forward-only**: it cannot go back or reset. For another pass, ask the collection for a **new** iterator.
- The iterator holds **all the traversal state**; the collection holds only the data.

---

## 4. How the for-each loop really works

The enhanced `for` loop is **syntactic sugar** over `Iterator`. The compiler rewrites:

```java
for (String s : list) {
    System.out.println(s);
}
```

into:

```java
for (Iterator<String> it = list.iterator(); it.hasNext(); ) {
    String s = it.next();
    System.out.println(s);
}
```

This is why **any class that implements `Iterable` works in a for-each loop**, which is exactly what `Iterable` is for.

```
for (T x : thing)       requires:  thing is Iterable<T>   (or an array)
```

- **Arrays** are not `Iterable`, but for-each has a special compiler translation for them (index loop).
- **`Map` is not `Iterable`**. Iterate its views instead: `for (var e : map.entrySet())`, `map.keySet()`, `map.values()`.
- Since Java 8: `list.forEach(x -> System.out.println(x));` uses `Iterable.forEach`.

---

## 5. Internal implementation mechanics

### 5.1 The iterator is an inner class of the collection

The iterator needs to read the collection's **private** fields (the internal array, `size`, head node...). The natural solution: implement it as an **inner class**, which can access the outer class's private members directly.

```
+----------------------------------------------------------+
|  class MyArrayList<E> implements Iterable<E>             |
|                                                          |
|     private Object[] data;      <---------+              |
|     private int size;           <------+  |  (private,    |
|                                        |  |   but visible |
|     public Iterator<E> iterator() {    |  |   to inner)   |
|         return new Itr();              |  |               |
|     }                                  |  |               |
|                                        |  |               |
|     private class Itr implements Iterator<E> {            |
|         int position = 0;   // the cursor                 |
|         public boolean hasNext() { return position < size; }
|         public E next() { return (E) data[position++]; }  |
|     }                                                     |
+----------------------------------------------------------+
```

(JDK's `ArrayList` does exactly this with a private inner class named `Itr`.)

### 5.2 State management: the `position` cursor

The iterator stores its own **cursor variable** (`position`) pointing at the next element to return.

### 5.3 The two key methods

| Method | Logic |
|---|---|
| `hasNext()` | returns `true` if `position < size` (there is still an element at the cursor) |
| `next()` | returns the element at `position`, then **increments** `position` |

```java
public boolean hasNext() {
    return position < size;
}

public E next() {
    if (!hasNext()) throw new NoSuchElementException();
    return (E) data[position++];     // return current, then move forward
}
```

(The real `ArrayList.Itr` uses `cursor != size` instead of `<`, and also checks for concurrent modification; see section 7.)

### 5.4 Trace through memory

```java
MyArrayList<String> list = ...;   // contains A, B, C
Iterator<String> it = list.iterator();
```

```
 STACK                 HEAP
 +------+          +---------------------------+
 | list |--------->| MyArrayList               |
 +------+          |   data -> [A][B][C][ ][ ] |
 | it   |--+       |   size = 3                |
 +------+  |       +---------------------------+
           |            ^
           |            | (inner class keeps a hidden reference to the outer object)
           |       +----+----------------------+
           '------>| Itr (iterator object)     |
                   |   position = 0            |
                   |   this$0 -> the list      |
                   +---------------------------+

 it.next() -> reads data[0] ("A"), position becomes 1
 it.next() -> reads data[1] ("B"), position becomes 2
```

### 5.5 Each collection, its own iterator

The **interface is the same**; the **internals differ**:

```
 ArrayList iterator      position (int index)      next(): data[cursor++]
 LinkedList iterator     reference to a Node       next(): value = node.item; node = node.next
 HashMap/HashSet         bucket index + node       next(): walk chain, then next non-empty bucket
 TreeMap/TreeSet         path in the tree          next(): in-order successor
```

```
 LinkedList iterator
   head
    |
    v
 [10]-->[20]-->[30]-->null
          ^
          '--- iterator holds a reference to the "next" node
```

---

## 6. Building your own Iterable collection

Any class can plug into for-each by implementing `Iterable<T>`.

```java
import java.util.*;

class MyList<T> implements Iterable<T> {
    private Object[] data = new Object[4];
    private int size = 0;

    public void add(T item) {
        if (size == data.length) data = Arrays.copyOf(data, size * 2);
        data[size++] = item;
    }

    @Override
    public Iterator<T> iterator() {
        return new MyIterator();        // a NEW iterator every call
    }

    // inner (non-static) class: can see data and size
    private class MyIterator implements Iterator<T> {
        private int position = 0;       // per-iterator state

        @Override
        public boolean hasNext() {
            return position < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            return (T) data[position++];
        }
    }
}
```

Using it:

```java
MyList<String> names = new MyList<>();
names.add("Asha"); names.add("Ravi"); names.add("Meera");

for (String n : names) {                 // works because MyList is Iterable
    System.out.println(n);
}
```

### Iterable as a lambda

`Iterable` has a single abstract method, so you can write it as a lambda or method reference:

```java
Iterable<Integer> numbers = () -> List.of(1, 2, 3).iterator();
for (int n : numbers) System.out.println(n);
```

### A range example (no storage at all!)

An iterator doesn't need stored data; it can *compute* values:

```java
class Range implements Iterable<Integer> {
    private final int start, end;
    Range(int start, int end) { this.start = start; this.end = end; }

    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            int cur = start;
            public boolean hasNext() { return cur < end; }
            public Integer next()    { return cur++; }
        };
    }
}

for (int i : new Range(0, 5)) System.out.print(i + " ");   // 0 1 2 3 4
```

---

## 7. The fail-fast mechanism

### 7.1 The symptom

```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4));

for (Integer x : list) {
    if (x == 2) {
        list.remove(x);      // modifies the list during traversal
    }
}
```

Result: **`java.util.ConcurrentModificationException`**.

Note the name is misleading: this happens in **single-threaded** code too. "Concurrent" means "modified at the same time as being iterated", not "by multiple threads" (though multiple threads can also cause it).

### 7.2 Why it's needed

If the collection changes while an iterator is walking it, the iterator's cursor no longer matches reality:

```
 list = [1, 2, 3, 4]       iterator cursor at index 2 (after returning 2)
                                          ^
 list.remove(2)  ->  [1, 3, 4]            elements shifted left!
                                          ^
 cursor still says "next index = 2" -> returns 4, SKIPS 3 !
```

Possible bad outcomes: skipped elements, repeated elements, `ArrayIndexOutOfBounds`, or inconsistent state. Rather than risk silent wrong results, Java **fails immediately and loudly**: **fail-fast**.

### 7.3 How it works: `modCount`

Collections like `ArrayList`, `LinkedList`, `HashMap` keep an integer field **`modCount`** (modification count) that is incremented on every **structural modification** (add, remove, clear, resize... anything that changes the size or structure).

Each iterator **snapshots** the value when it is created:

```
 collection.modCount = 4          (changes whenever the structure changes)
 iterator.expectedModCount = 4    (copied at iterator creation)
```

Every `next()` first compares them:

```java
// simplified from ArrayList.Itr
int expectedModCount = modCount;          // captured at creation

public E next() {
    checkForComodification();             // <-- the fail-fast check
    ...
}

final void checkForComodification() {
    if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
}
```

### 7.4 Step by step

```
 STEP 1  it = list.iterator()
         list.modCount = 4, it.expectedModCount = 4        (equal, all good)

 STEP 2  it.next() -> check 4 == 4 OK -> returns element

 STEP 3  list.remove(x)       <-- NOT through the iterator
         list.modCount becomes 5
         it.expectedModCount is still 4                    (now different!)

 STEP 4  it.next() -> check 5 != 4 -> throws ConcurrentModificationException
```

```
 collection ----------------- modCount: 4 -> 5   (someone changed it)
                                        |
 iterator ---- expectedModCount: 4 -----+--> mismatch -> CME
```

### 7.5 What counts as structural modification?

| Action | Increments `modCount`? |
|---|---|
| `add`, `remove`, `clear`, `addAll`, `removeIf` (that removes) | **Yes** |
| `set(i, x)` on an ArrayList (replace in place) | No (structure unchanged) |
| Changing a field **inside** an element object | No |
| `HashMap.put` on an **existing** key (value replaced) | No |
| `HashMap.put` of a **new** key | **Yes** |

### 7.6 Fail-fast is "best effort", not a guarantee

It is a **debugging aid** to catch bugs, not something to rely on for program logic. Two caveats:

1. **Multi-threaded access** may not be detected reliably (`modCount` is not volatile or synchronized), so the exception isn't guaranteed.
2. **A famous quirk:** because `ArrayList.Itr.hasNext()` is `cursor != size`, removing the **second-to-last** element makes `hasNext()` return `false`, so the loop ends **without** throwing:

```java
List<String> list = new ArrayList<>(List.of("A", "B", "C"));
for (String s : list) {
    if (s.equals("B")) list.remove(s);   // "B" is second-to-last
}
// No exception! Loop silently ends, list = [A, C]
// Removing "A" or "C" instead WOULD throw (or behave differently)
```

```
 list [A,B,C]  iterating, just returned "B": cursor = 2
 remove("B") -> list [A,C], size = 2
 hasNext(): cursor(2) != size(2)?  false -> loop exits, "C" never visited, no check run
```

Moral: don't depend on this; never structurally modify during iteration outside the iterator.

### 7.7 Fail-fast vs fail-safe (weakly consistent)

| | Fail-fast | Fail-safe / weakly consistent |
|---|---|---|
| Behavior on concurrent change | throws `ConcurrentModificationException` | does **not** throw |
| How | checks `modCount` | works on a **snapshot copy** or tolerates changes |
| Examples | `ArrayList`, `LinkedList`, `HashMap`, `HashSet`, `TreeMap` | `CopyOnWriteArrayList`, `ConcurrentHashMap`, `ConcurrentLinkedQueue` |
| Sees new changes? | n/a (it aborts) | `CopyOnWriteArrayList`: **no** (snapshot). `ConcurrentHashMap`: **maybe** (weakly consistent) |
| Cost | cheap | snapshot copying or extra bookkeeping |

("Fail-safe" is an informal term; the official wording is *weakly consistent* iterators.)

---

## 8. Safe ways to modify while iterating

### 8.1 `Iterator.remove()`

The iterator's own `remove()` deletes the **last element returned by `next()`** and **updates `expectedModCount`**, so no exception occurs.

```java
Iterator<Integer> it = list.iterator();
while (it.hasNext()) {
    int x = it.next();
    if (x % 2 == 0) {
        it.remove();            // safe: removal goes THROUGH the iterator
    }
}
```

```
 it.remove() does:
   1) remove element from the collection (modCount++)
   2) expectedModCount = modCount        <-- keeps the iterator "in sync"
   3) fix the cursor position
```

Rules for `remove()`:
- Must be called **after** `next()`, and **at most once per `next()`**, otherwise `IllegalStateException`.
- Not every iterator supports it (immutable collections throw `UnsupportedOperationException`).

### 8.2 `removeIf` (Java 8+), the cleanest

```java
list.removeIf(x -> x % 2 == 0);
```

### 8.3 Other options

```java
// Collect, then remove after the loop
List<Integer> toRemove = new ArrayList<>();
for (int x : list) if (x % 2 == 0) toRemove.add(x);
list.removeAll(toRemove);

// Iterate over a COPY while modifying the original
for (int x : new ArrayList<>(list)) { if (x % 2 == 0) list.remove(Integer.valueOf(x)); }

// Use a concurrent collection if threads are involved
List<Integer> safe = new CopyOnWriteArrayList<>(list);
```

Gotcha: `list.remove(int index)` vs `list.remove(Object o)`. For `List<Integer>`, `list.remove(2)` removes **index** 2; use `list.remove(Integer.valueOf(2))` to remove the **value** 2.

---

## 9. Why Iterable and Iterator are separate (separation of concerns)

### 9.1 Two responsibilities, two objects

| Object | Responsibility |
|---|---|
| **Collection** (`Iterable`) | **store** the data (add, remove, contains...) |
| **Iterator** | **traverse** the data (where am I? what's next?) |

Mixing both into one class violates the **Single Responsibility Principle**. Let's see why the combined design breaks.

### 9.2 The tempting (bad) design: collection traverses itself

```java
class BadList<T> {
    private Object[] data = new Object[10];
    private int size;
    private int position = 0;          // traversal state INSIDE the collection

    boolean hasNext() { return position < size; }
    T next()          { return (T) data[position++]; }
    void reset()      { position = 0; }
}
```

```java
BadList<String> list = ...;
while (list.hasNext()) {
    System.out.println(list.next());
}
// to traverse again we MUST call list.reset()
```

### Problem 1: Resetting state is fragile

- After a traversal the single `position` sits at the end; the next traversal will see **nothing** unless someone remembers to call `reset()`.
- If the caller forgets or an exception interrupts the loop, the collection is left in a **dirty state**, and the **next, unrelated caller** gets wrong results.
- Every user must know and follow the protocol.

```
 traversal 1 ends:   position = 3 (end)
 traversal 2 starts: hasNext() -> false   (nothing printed!) unless reset() was called
```

### Problem 2: Nested loops are impossible

Only **one** `position` exists for the whole collection, so two loops **share** it:

```java
BadList<Integer> nums = ...;   // 1, 2, 3
while (nums.hasNext()) {                       // outer
    int a = nums.next();
    nums.reset();
    while (nums.hasNext()) {                   // inner
        int b = nums.next();
        System.out.println(a + "," + b);       // wants all pairs
    }
    // the inner loop exhausted position -> the outer loop is now finished too!
}
```

```
 outer starts: position = 0
 outer next() -> 1, position = 1
 inner loop runs position 1 -> 2 -> 3 (end)
 back in outer: hasNext() -> false  => outer loop dies after ONE round
 Expected 9 pairs, got 3
```

The same trap hits **any two simultaneous traversals** (two threads, a method that iterates while its caller is also iterating, comparing a list to itself, etc.).

### 9.3 The solution: every call to `iterator()` creates a NEW iterator

```java
for (int a : nums) {              // iterator #1 (its own position)
    for (int b : nums) {          // iterator #2 (its own position)
        System.out.println(a + "," + b);
    }
}
// 9 pairs, correct
```

```
 nums (collection holds only DATA)
   data: [1][2][3]
        ^        ^
        |        |
 iterator #1     iterator #2          independent cursors, independent state
 (outer loop)    (inner loop)
 position = 1    position = 2
```

Each iterator object has **its own `position`** (and its own `expectedModCount`), so you can have:

- **Nested loops** over the same collection
- **Multiple independent traversals** at the same time
- **Re-traversal** simply by asking for a fresh iterator: **no `reset()` method** needed
- Different traversal strategies (forward, backward via `ListIterator`, filtered, sorted) over the same data without touching the collection class

### 9.4 Comparison

```
 COMBINED (bad)                          SEPARATED (Iterable + Iterator)
 +-----------------------+               +-------------+      +-------------+
 | Collection            |               | Collection  |      | Iterator #1 |
 |  data                 |               |  data       |<-----|  position   |
 |  position  (1 only!)  |               +-------------+   |  +-------------+
 |  reset()              |                       ^         |  +-------------+
 +-----------------------+                       '---------+--| Iterator #2 |
  one cursor shared by all                          each has |  position   |
  callers -> conflicts                              its own  +-------------+
```

| Aspect | Combined | Separated |
|---|---|---|
| Nested loops | broken | works |
| Re-traversal | needs fragile `reset()` | new iterator, trivial |
| Concurrent readers | interfere | independent |
| Collection class complexity | storage **and** traversal | storage only |
| Add new traversal style | modify the collection | write a new iterator |

---

## 10. Related topics: ListIterator, fail-safe iterators, Map

### 10.1 `ListIterator` (for Lists only)

A richer iterator for `List`s: **bidirectional**, can **add** and **set**.

```java
ListIterator<String> it = list.listIterator();
while (it.hasNext()) {
    int idx = it.nextIndex();
    String s = it.next();
    if (s.equals("B")) it.set("B2");        // replace last returned
    if (s.equals("C")) it.add("C-after");   // insert at cursor
}
while (it.hasPrevious()) {                  // walk backwards
    System.out.println(it.previous());
}
```

| Feature | `Iterator` | `ListIterator` |
|---|---|---|
| Direction | forward only | **both** |
| Works on | any Collection | List only |
| `remove()` | yes | yes |
| `add()` / `set()` | no | **yes** |
| Index access | no | `nextIndex()` / `previousIndex()` |

### 10.2 Iterating a `Map`

`Map` does not implement `Iterable`; use its Collection views:

```java
for (Map.Entry<String, Integer> e : map.entrySet()) { ... }
for (String k : map.keySet())   { ... }
for (Integer v : map.values())  { ... }

map.forEach((k, v) -> System.out.println(k + "=" + v));
map.entrySet().removeIf(e -> e.getValue() < 10);     // safe removal
```

Modifying a `HashMap` during iteration (other than through the iterator) also throws `ConcurrentModificationException`.

### 10.3 `Spliterator` (just a mention)

`Iterable.spliterator()` powers parallel streams: an iterator that can **split** itself for parallel traversal.

---

## 11. Interview questions and answers

**Q1. What is the `Iterable` interface?**
A single-method interface (`Iterator<T> iterator()`) implemented by anything that can be traversed. `Collection` extends it, so all collections (except `Map`) support for-each.

**Q2. What is an `Iterator`?**
An object that traverses a collection one element at a time using `hasNext()` and `next()`, and optionally `remove()`. It holds the traversal state (cursor).

**Q3. Why do we need Iterator if we have `for (int i...) list.get(i)`?**
Index access only works for index-based structures. `Iterator` gives one uniform way to traverse any collection (sets, queues, linked lists), independent of its internals, and avoids O(n^2) `get(i)` on linked lists.

**Q4. How does the for-each loop work internally?**
The compiler turns it into `iterator()` + a `while(hasNext()) next()` loop. That's why only `Iterable` types (and arrays) can be used.

**Q5. How is an iterator typically implemented?**
As an inner class of the collection so it can access private fields. It keeps a `position`/cursor; `hasNext()` checks `position < size`; `next()` returns the element and advances the cursor.

**Q6. What is fail-fast?**
Detecting structural modification during iteration immediately and throwing `ConcurrentModificationException` instead of producing unpredictable results.

**Q7. How does the JDK detect it?**
Each collection has a `modCount` incremented on structural changes. The iterator copies it into `expectedModCount` on creation and compares them on every `next()`. A mismatch throws the exception.

**Q8. Does `ConcurrentModificationException` only happen with multiple threads?**
No. Modifying a list inside its own for-each loop in a single thread triggers it. Multi-threading can also trigger it.

**Q9. How do you safely remove elements while iterating?**
Use `Iterator.remove()`, `Collection.removeIf(...)`, collect-then-`removeAll`, or iterate over a copy. Use concurrent collections for multi-threaded cases.

**Q10. Is fail-fast guaranteed?**
No, it is best-effort. It is for catching bugs, not for program logic; unsynchronized multi-threaded changes may go undetected, and some single-thread cases (e.g. removing the second-to-last element of an `ArrayList`) don't throw.

**Q11. Fail-fast vs fail-safe iterators?**
Fail-fast (`ArrayList`, `HashMap`) throw on concurrent modification. Fail-safe/weakly consistent (`CopyOnWriteArrayList`, `ConcurrentHashMap`) don't throw; they iterate a snapshot or tolerate changes.

**Q12. Why are `Iterable` and `Iterator` separate interfaces?**
Separation of concerns: the collection stores data; each iterator holds its own traversal state. If one object did both, you'd need a fragile `reset()` and nested or simultaneous traversals would corrupt each other's state. With separation, every `iterator()` call returns a fresh, independent iterator.

**Q13. Can you traverse the same collection with nested loops?**
Yes, because each loop gets its own iterator with its own cursor.

**Q14. Why does calling `remove()` twice after one `next()` fail?**
`remove()` removes the last element returned by `next()`; calling it again without another `next()` throws `IllegalStateException`.

**Q15. `Iterator` vs `ListIterator`?**
`ListIterator` (Lists only) can move backward, report indices, and `add`/`set` elements; `Iterator` is forward-only on any collection.

**Q16. Is `Map` iterable?**
No. Iterate over `entrySet()`, `keySet()`, or `values()`, which are collections.

**Q17. Are arrays `Iterable`?**
No, but the for-each loop supports arrays through a separate compiler translation. Use `Arrays.asList(arr)` or `Arrays.stream(arr)` if you need an `Iterable`/stream.

---

## 12. Cheat sheet

```
PROBLEM      Each structure traverses differently (indices, nodes, buckets, trees)
SOLUTION     Iterable<T>.iterator() -> Iterator<T> { hasNext(), next(), remove() }

HIERARCHY    Iterable > Collection > List | Set | Queue        (Map is NOT Iterable)

FOR-EACH     for (T x : c)  ==  for (Iterator<T> it = c.iterator(); it.hasNext(); ) { T x = it.next(); }

ITERATOR     = pointer/bookmark holding traversal state (cursor)
             forward-only, single-use. New traversal = new iterator
             next() with nothing left -> NoSuchElementException

INTERNALS    iterator = INNER class of the collection (sees private fields)
             int position;  hasNext(): position < size
                            next():    return data[position++]
             (JDK ArrayList: cursor != size, plus modCount check)

FAIL-FAST    collection.modCount++  on structural change
             iterator.expectedModCount = modCount  (at creation)
             next(): if (modCount != expectedModCount) throw ConcurrentModificationException
             best-effort only; also happens in a single thread

SAFE REMOVE  it.remove()  |  removeIf(pred)  |  collect then removeAll  |  iterate a copy
             CopyOnWriteArrayList / ConcurrentHashMap for concurrent access (fail-safe)

WHY SEPARATE Collection = store data;  Iterator = traverse data
             - no fragile reset()
             - nested loops work (each iterator has its own position)
             - many simultaneous, independent traversals
             - collection class stays simple

GOTCHAS      remove(int index) vs remove(Object) on List<Integer>
             remove() after next(), once per next()
             Map: iterate entrySet()/keySet()/values()
```

---

*End of notes.*
