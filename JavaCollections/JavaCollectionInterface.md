# Java Collection Interface

Study notes based on a video walkthrough of the `Collection` interface.

---

## 1. Overview

- `Collection<E>` extends `Iterable<E>`. It is the root interface for `List`, `Set` and `Queue`.
- It defines the core operations that make sense for *any* group of elements. Code written against `Collection` works with every concrete class (`ArrayList`, `HashSet`, `ArrayDeque`, ...) through polymorphism.
- `Map` is **not** part of the `Collection` hierarchy.

```
Iterable
   └── Collection
         ├── List   (ArrayList, LinkedList)
         ├── Set    (HashSet, TreeSet, LinkedHashSet)
         └── Queue  (PriorityQueue, ArrayDeque)
```

### Collection vs Collections

| | `Collection` | `Collections` |
|---|---|---|
| Kind | Interface (`java.util`) | Utility class (`java.util`) |
| Purpose | Defines what a collection can do | Static helpers: `sort`, `reverse`, `unmodifiableList`, `max`, ... |

```java
Collection<String> names = new ArrayList<>();   // interface
Collections.sort((List<String>) names);         // utility class
```

---

## 2. Basic Operations

| Method | Description | Notes |
|---|---|---|
| `int size()` | Number of elements | |
| `boolean isEmpty()` | `true` if no elements | Prefer over `size() == 0` for readability. Some concurrent collections implement it more cheaply |
| `boolean contains(Object o)` | Checks for existence using `equals()` | `ArrayList` O(n), `HashSet` O(1) average, `TreeSet` O(log n) |
| `boolean add(E e)` | Adds an element | Returns `false` if the collection did not change (e.g. duplicate in a `Set`) |
| `boolean remove(Object o)` | Removes a single matching instance | Returns `true` if something was removed |

```java
Collection<String> list = new ArrayList<>();
Collection<String> set  = new HashSet<>();

list.add("A");   // true
list.add("A");   // true  (duplicates allowed)

set.add("A");    // true
set.add("A");    // false (duplicate rejected)

list.contains("A");  // true, uses equals()
list.remove("A");    // removes the first "A" only
```

> Optional operations (`add`, `remove`, ...) may throw `UnsupportedOperationException` on immutable collections such as `List.of(...)`.

---

## 3. Bulk Operations

| Method | Meaning |
|---|---|
| `addAll(Collection<? extends E> c)` | Union: add everything from `c` |
| `containsAll(Collection<?> c)` | Subset check: does this contain every element of `c`? |
| `removeAll(Collection<?> c)` | Difference: remove every element that is in `c` |
| `retainAll(Collection<?> c)` | **Intersection**: keep only elements that are also in `c` |
| `clear()` | Remove all elements |

```java
Collection<Integer> a = new ArrayList<>(List.of(1, 2, 3, 4));
Collection<Integer> b = List.of(3, 4, 5);

a.retainAll(b);          // a = [3, 4]   (intersection)

a = new ArrayList<>(List.of(1, 2, 3, 4));
a.removeAll(b);          // a = [1, 2]   (difference)

a.addAll(b);             // a = [1, 2, 3, 4, 5]
a.containsAll(b);        // true
a.clear();               // a = []
```

All of these return `boolean` (except `clear`), indicating whether the collection was modified.

---

## 4. Conversion: `toArray`

```java
Collection<String> c = List.of("x", "y", "z");

Object[] objs = c.toArray();                 // loses type info

String[] strs = c.toArray(new String[0]);    // type-safe, preferred
String[] strs2 = c.toArray(String[]::new);   // Java 11+
```

- `toArray()` always returns `Object[]`.
- `toArray(T[] a)` returns an array of the requested type. If `a` is big enough it is filled and returned, otherwise a new array is allocated. Passing `new String[0]` is the idiomatic form.

---

## 5. equals() and hashCode()

- `Collection` **re-declares** `equals()` and `hashCode()` in the interface purely to document the contract. The actual implementation lives in the concrete classes (via `AbstractList`, `AbstractSet`, ...).
- Contract by type:
  - `List.equals`: same elements in the same order.
  - `Set.equals`: same elements, order ignored.
  - A `List` is never equal to a `Set`, even with identical elements.
- Elements stored in collections must have a proper `equals()`/`hashCode()` pair, or `contains`, `remove` and hash-based collections will misbehave.

```java
record Point(int x, int y) {}   // equals/hashCode generated automatically

class User {
    String id;
    @Override public boolean equals(Object o) {
        return o instanceof User u && Objects.equals(id, u.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
```

Rule: **if you override one, override the other.** Equal objects must have equal hash codes.

---

## 6. Modern Default Methods (Java 8+)

| Method | Purpose |
|---|---|
| `removeIf(Predicate<? super E>)` | Remove all elements matching a condition |
| `stream()` | Sequential `Stream<E>` over the elements |
| `parallelStream()` | Possibly parallel stream |
| `spliterator()` | Underlying traversal/splitting mechanism used by streams |
| `forEach(Consumer)` | Inherited from `Iterable` |

```java
Collection<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));

nums.removeIf(n -> n % 2 == 0);              // [1, 3, 5]

int sum = nums.stream().mapToInt(i -> i).sum();   // 9

nums.forEach(System.out::println);
```

> Prefer `removeIf` over removing inside a for-each loop, which throws `ConcurrentModificationException`.

---

## Quick Recap

1. `Collection` is the common parent of `List`, `Set`, `Queue`. `Collections` is a separate utility class.
2. Core methods: `size`, `isEmpty`, `contains`, `add`, `remove`.
3. Bulk methods: `addAll`, `containsAll`, `removeAll`, `retainAll`, `clear`.
4. Use `toArray(new T[0])` for typed arrays.
5. Always pair `equals()` with `hashCode()`.
6. Use `removeIf` and `stream()` for modern, lambda-based work.
