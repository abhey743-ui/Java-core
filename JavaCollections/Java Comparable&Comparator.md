# Java Comparable & Comparator: "What Should Be Compared?"

> The follow-up to Set/Map and Queue notes. `TreeSet`, `TreeMap`, `PriorityQueue`, `sort()` all need to know **how to order your objects**. This file shows every way to tell Java **what to compare**: a field, many fields, map keys, map values, or any custom rule.

---

## Table of Contents

1. [The Problem: Java Doesn't Know How to Compare Your Objects](#1-the-problem)
2. [The Two Tools: Comparable vs Comparator](#2-the-two-tools)
3. [Comparable: Natural Ordering (built into the class)](#3-comparable)
4. [Comparator: External Ordering (passed from outside)](#4-comparator)
5. [Five Ways to Pass a Comparator](#5-five-ways-to-pass-a-comparator)
6. [Choosing WHAT to Compare in a Custom Class](#6-choosing-what-to-compare)
7. [Comparing Multiple Fields (`thenComparing`)](#7-multiple-fields)
8. [Reverse, Null Handling, Case-Insensitive](#8-reverse-nulls-case)
9. [Using Comparators With Collections](#9-using-comparators-with-collections)
10. [Comparing Maps: Keys and Values](#10-comparing-maps)
11. [Comparing for Equality vs Ordering (`equals` vs `compareTo`)](#11-equals-vs-compareto)
12. [Common Mistakes](#12-common-mistakes)
13. [Interview Questions](#13-interview-questions)

---

## 1. The Problem

Numbers and Strings have an obvious order. Your own classes do not.

```java
class Student {
    int id; String name; double marks;
    // constructor, getters...
}

TreeSet<Student> set = new TreeSet<>();
set.add(new Student(1, "Ravi", 88.5));
// ClassCastException: Student cannot be cast to class java.lang.Comparable
```

```
Java sees two Students:      Student A (id 1, "Ravi", 88.5)
                             Student B (id 2, "Amit", 92.0)

Which one is "smaller"?   By id? By name? By marks? 

Java can't guess. YOU must define the rule.
```

Everything that needs ordering depends on this rule:

```
 TreeSet / TreeMap ---+
 PriorityQueue -------+--->  needs a "compare" rule
 Collections.sort() --+
 Arrays.sort() -------+
 stream.sorted() -----+
```

---

## 2. The Two Tools

| | `Comparable<T>` | `Comparator<T>` |
|---|---|---|
| Package | `java.lang` | `java.util` |
| Method | `int compareTo(T other)` | `int compare(T a, T b)` |
| Where the rule lives | **Inside** the class itself | **Outside** (separate object/lambda) |
| How many rules per class | **One** (the "natural" order) | **Unlimited** (make as many as you like) |
| Can you change the class? | Required (you must edit the class) | Not required (works on classes you can't edit) |
| Passed how? | Automatic (class implements it) | You pass it to the constructor/method |

```
 Comparable:   "I know how to compare MYSELF with another object."
                a.compareTo(b)

 Comparator:   "A referee who compares TWO objects for you."
                referee.compare(a, b)
```

### The Return Value Rule (same for both)

| Returns | Meaning |
|---|---|
| **negative** (< 0) | first is **smaller** (comes first) |
| **zero** (0) | **equal** in ordering |
| **positive** (> 0) | first is **bigger** (comes later) |

```
 compare(3, 5)  -> negative   (3 comes before 5)
 compare(5, 5)  -> 0
 compare(7, 5)  -> positive   (7 comes after 5)
```

---

## 3. Comparable

Make your class implement `Comparable<YourClass>` and define the **natural order**.

```java
class Student implements Comparable<Student> {
    int id;
    String name;
    double marks;

    Student(int id, String name, double marks) {
        this.id = id; this.name = name; this.marks = marks;
    }

    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.id, other.id);   // natural order = by id
    }

    @Override
    public String toString() { return id + ":" + name + ":" + marks; }
}
```

Now everything just works with **no extra arguments**:

```java
List<Student> list = new ArrayList<>(List.of(
        new Student(3, "Ravi", 88.5),
        new Student(1, "Amit", 92.0),
        new Student(2, "Neha", 75.0)));

Collections.sort(list);          // uses compareTo
System.out.println(list);        // [1:Amit:92.0, 2:Neha:75.0, 3:Ravi:88.5]

TreeSet<Student> ts = new TreeSet<>(list);   // sorted by id automatically
PriorityQueue<Student> pq = new PriorityQueue<>(list);  // smallest id first
```

### How `compareTo` is read

```
 this.compareTo(other)

   this.id = 1,  other.id = 3   ->  Integer.compare(1, 3) = -1  -> this comes FIRST
   this.id = 3,  other.id = 1   ->  Integer.compare(3, 1) = +1  -> this comes LATER
   this.id = 2,  other.id = 2   ->  0                           -> equal
```

### Which fields to put in `compareTo`: examples

```java
// ascending by marks
return Double.compare(this.marks, other.marks);

// descending by marks  (swap the two sides)
return Double.compare(other.marks, this.marks);

// by name (Strings already have compareTo)
return this.name.compareTo(other.name);
```

> **Use `Integer.compare(a, b)` / `Double.compare(a, b)`, not `a - b`.**
> Subtraction can **overflow**: `Integer.MAX_VALUE - (-1)` becomes a negative number and gives the wrong answer.

---

## 4. Comparator

A **Comparator** is a separate rule you write **outside** the class. Use it when:

- you want a **different** ordering than the natural one,
- you want **several** orderings (by name, by marks, by age...),
- you **can't edit the class** (e.g., `String`, or a library class),
- the class doesn't implement `Comparable` at all.

```java
class Student {          // NOTE: does NOT implement Comparable
    int id; String name; double marks;
    // constructor, getters (getId, getName, getMarks), toString
}

class ByMarks implements Comparator<Student> {
    @Override
    public int compare(Student a, Student b) {
        return Double.compare(a.marks, b.marks);
    }
}

TreeSet<Student> ts = new TreeSet<>(new ByMarks());   // pass the rule in
```

```
 You decide the rule at the moment you create the collection:

   new TreeSet<>(byName)       -> sorted by name
   new TreeSet<>(byMarks)      -> sorted by marks
   new TreeSet<>(byIdDesc)     -> sorted by id, high to low

 Same class, different orderings. 
```

---

## 5. Five Ways to Pass a Comparator

All five do the same job (sort students by marks, ascending).

### 5.1 Separate named class

```java
class ByMarks implements Comparator<Student> {
    public int compare(Student a, Student b) {
        return Double.compare(a.getMarks(), b.getMarks());
    }
}
list.sort(new ByMarks());
```

### 5.2 Anonymous class

```java
list.sort(new Comparator<Student>() {
    @Override
    public int compare(Student a, Student b) {
        return Double.compare(a.getMarks(), b.getMarks());
    }
});
```

### 5.3 Lambda expression (Java 8+)

```java
list.sort((a, b) -> Double.compare(a.getMarks(), b.getMarks()));
```

### 5.4 `Comparator.comparing` with a method reference (cleanest)

```java
list.sort(Comparator.comparing(Student::getMarks));
```

### 5.5 Stored in a variable (reuse anywhere)

```java
Comparator<Student> byMarks = Comparator.comparingDouble(Student::getMarks);

list.sort(byMarks);
TreeSet<Student> ts = new TreeSet<>(byMarks);
PriorityQueue<Student> pq = new PriorityQueue<>(byMarks);
```

```
 Evolution (same logic, less code):

 named class  ->  anonymous class  ->  lambda  ->  Comparator.comparing(Student::getMarks)
   (long)                                              (short & readable)
```

### Where can you pass it?

| Place | Syntax |
|---|---|
| `List` | `list.sort(cmp)` or `Collections.sort(list, cmp)` |
| Array | `Arrays.sort(arr, cmp)` (object arrays only, not `int[]`) |
| `TreeSet` | `new TreeSet<>(cmp)` |
| `TreeMap` | `new TreeMap<>(cmp)` (compares **keys**) |
| `PriorityQueue` | `new PriorityQueue<>(cmp)` |
| Stream | `stream.sorted(cmp)` |
| Min/Max | `Collections.max(list, cmp)`, `stream.max(cmp)` |

---

## 6. Choosing WHAT to Compare

`Comparator.comparing(...)` takes a **"key extractor"**: a function that says *which piece of the object to look at*.

```
 Comparator.comparing( Student::getName )
                       \_______________/
                       "compare students by THIS value"
```

### 6.1 Compare by a single field

```java
Comparator.comparing(Student::getName)             // String field
Comparator.comparingInt(Student::getId)            // int field (avoids boxing)
Comparator.comparingDouble(Student::getMarks)      // double field
Comparator.comparingLong(Student::getPhone)        // long field
```

### 6.2 Compare by a nested object's field

```java
class Address { String city; /* getCity() */ }
class Employee { String name; Address address; /* getAddress() */ }

Comparator<Employee> byCity =
        Comparator.comparing(e -> e.getAddress().getCity());
```

### 6.3 Compare by a calculated value

```java
// By length of the name
Comparator<Student> byNameLength =
        Comparator.comparingInt(s -> s.getName().length());

// By how far marks are from a target of 80
Comparator<Student> closestTo80 =
        Comparator.comparingDouble(s -> Math.abs(s.getMarks() - 80));
```

### 6.4 Compare by a field with a custom rule for that field

```java
// Names compared case-insensitively
Comparator<Student> byNameIgnoreCase =
        Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER);

// Marks compared in descending order
Comparator<Student> byMarksDesc =
        Comparator.comparing(Student::getMarks, Comparator.reverseOrder());
```

The **second argument** is a Comparator for the extracted value.

### 6.5 Compare any object type (not just your own class)

```java
List<String> words = List.of("kiwi", "fig", "banana", "apple");

words.sort(Comparator.comparingInt(String::length));
// [fig, kiwi, apple, banana]   (by length)

List<int[]> pairs = new ArrayList<>();
pairs.add(new int[]{3, 9});
pairs.add(new int[]{1, 5});
pairs.sort((a, b) -> Integer.compare(a[0], b[0]));   // compare by first element
// [[1,5], [3,9]]
```

### Quick map: "I want to compare by..."

| I want to compare by... | Use |
|---|---|
| A String field | `Comparator.comparing(X::getField)` |
| An `int` field | `Comparator.comparingInt(X::getField)` |
| A `double` field | `Comparator.comparingDouble(X::getField)` |
| A nested field | `Comparator.comparing(x -> x.getA().getB())` |
| A computed value | `Comparator.comparingInt(x -> compute(x))` |
| Field + custom rule | `Comparator.comparing(X::getField, ruleComparator)` |
| Totally custom logic | `(a, b) -> { ... return int; }` |

---

## 7. Multiple Fields

Real data has ties. Two students with the same marks, who goes first? Chain rules with **`thenComparing`**.

```
 Sort by marks (high to low).  If marks are equal, sort by name (A to Z).

   Before:                          After:
   Ravi  90                         Amit  92
   Amit  92                         Neha  90   <- tie with Ravi at 90,
   Neha  90                         Ravi  90      so name decides
```

```java
Comparator<Student> rule =
        Comparator.comparing(Student::getMarks, Comparator.reverseOrder())  // 1st rule
                  .thenComparing(Student::getName);                         // tie-breaker

list.sort(rule);
```

Chain as many as you need:

```java
Comparator<Student> rule =
        Comparator.comparing(Student::getGrade)            // 1st
                  .thenComparing(Student::getMarks)        // 2nd
                  .thenComparingInt(Student::getId);       // 3rd
```

How it works:

```
 compare(a, b):
    result = compare by 1st rule
    if result != 0  -> done, return it
    else            -> try 2nd rule
                       if result != 0 -> done
                       else           -> try 3rd rule ...
```

### Manual way (inside `compareTo` or a lambda)

```java
@Override
public int compareTo(Student o) {
    int c = Double.compare(o.marks, this.marks);   // marks DESC
    if (c != 0) return c;                          // different marks -> done
    return this.name.compareTo(o.name);            // tie -> name ASC
}
```

> **Always add a final tie-breaker that is unique (like `id`)** when using `TreeSet`/`TreeMap`. Otherwise two different objects that compare as `0` are treated as duplicates and one is silently dropped (see section 11).

---

## 8. Reverse, Nulls, Case

### 8.1 Reverse any comparator

```java
Comparator<Student> byMarksAsc  = Comparator.comparingDouble(Student::getMarks);
Comparator<Student> byMarksDesc = byMarksAsc.reversed();
```

Built-in helpers for natural order:

```java
Comparator.naturalOrder()     // a.compareTo(b)
Comparator.reverseOrder()     // b.compareTo(a)
```

> **Gotcha with `.reversed()` and lambdas:** `Comparator.comparing(s -> s.getMarks()).reversed()` can fail to compile because Java can't infer the type. Use a method reference (`Student::getMarks`) or write `Comparator.comparing(Student::getMarks, Comparator.reverseOrder())`.

### 8.2 Handle `null` safely

Normal comparators throw `NullPointerException` if a value is `null`.

```java
// Students whose name is null go LAST
Comparator<Student> safe =
        Comparator.comparing(Student::getName,
                Comparator.nullsLast(Comparator.naturalOrder()));

// Or nulls FIRST
Comparator.nullsFirst(Comparator.naturalOrder());
```

### 8.3 Case-insensitive Strings

```java
words.sort(String.CASE_INSENSITIVE_ORDER);
// or
words.sort(String::compareToIgnoreCase);
```

---

## 9. Using Comparators With Collections

Let's use one Student list everywhere:

```java
record Student(int id, String name, double marks) {}

List<Student> students = List.of(
        new Student(1, "Ravi", 88.5),
        new Student(2, "Amit", 92.0),
        new Student(3, "Neha", 88.5),
        new Student(4, "Zoya", 70.0));
```

### 9.1 `List`

```java
List<Student> copy = new ArrayList<>(students);
copy.sort(Comparator.comparingDouble(Student::marks).reversed());
```

### 9.2 `TreeSet` (sorted set of objects)

```java
TreeSet<Student> byName = new TreeSet<>(Comparator.comparing(Student::name));
byName.addAll(students);
System.out.println(byName.first());     // Amit
```

NavigableSet methods work with your rule:

```java
// Who is the lowest scorer with marks >= 80?  (needs a probe object)
Student probe = new Student(0, "", 80);
```

### 9.3 `PriorityQueue` (top-K / scheduling)

```java
PriorityQueue<Student> pq =
        new PriorityQueue<>(Comparator.comparingDouble(Student::marks).reversed());
pq.addAll(students);

System.out.println(pq.poll().name());   // Amit  (highest marks first)
System.out.println(pq.poll().name());   // Ravi or Neha (tie at 88.5)
```

### 9.4 Streams

```java
students.stream()
        .sorted(Comparator.comparing(Student::name))
        .forEach(System.out::println);

Student best = students.stream()
        .max(Comparator.comparingDouble(Student::marks))
        .get();
```

### 9.5 Arrays

```java
Student[] arr = students.toArray(new Student[0]);
Arrays.sort(arr, Comparator.comparing(Student::name));

// For primitive int[] there is no comparator version.
// Use Integer[] (boxed) if you need a custom order:
Integer[] nums = {5, 1, 9, 3};
Arrays.sort(nums, Comparator.reverseOrder());   // [9, 5, 3, 1]
```

---

## 10. Comparing Maps

A Map has **two parts**: **key** and **value**. You can sort/compare on either, but they work differently.

```
   Map<String, Integer>
   +----------+-------+
   |   KEY    | VALUE |
   +----------+-------+
   | "banana" |   3   |
   | "apple"  |   7   |
   | "cherry" |   5   |
   +----------+-------+
```

### 10.1 Compare by KEY: easy, use `TreeMap`

```java
Map<String, Integer> map = new TreeMap<>();          // natural key order
map.put("banana", 3); map.put("apple", 7); map.put("cherry", 5);
System.out.println(map);   // {apple=7, banana=3, cherry=5}

// Custom key rule (reverse, or by length, etc.)
Map<String, Integer> rev = new TreeMap<>(Comparator.reverseOrder());
Map<String, Integer> byLen = new TreeMap<>(Comparator.comparingInt(String::length));
```

> The `TreeMap` comparator is **always applied to keys**. Keys are compared to find/put/get.

### 10.2 Compare by VALUE: a `TreeMap` can NOT do this

A `TreeMap` sorts by **key only**. There's no way to say "sort by value" in the constructor, because `get(key)` needs to search by key.

```
 TreeMap<String,Integer>(compare by value)   <- WRONG idea.
   get("apple") would try to find a key by comparing VALUES. Broken.
```

**Solution:** pull the entries out, sort **the entries**, and put them in a structure that keeps order.

```
  Map  --entrySet()-->  List of Entry  --sort by value-->  sorted list
                                                              |
                                              (optional) LinkedHashMap keeps that order
```

### 10.3 Sort map entries by VALUE (list approach)

```java
Map<String, Integer> map = new HashMap<>();
map.put("banana", 3); map.put("apple", 7); map.put("cherry", 5);

List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());

// Value ascending
entries.sort(Map.Entry.comparingByValue());

// Value descending
entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

for (Map.Entry<String, Integer> e : entries) {
    System.out.println(e.getKey() + " = " + e.getValue());
}
// apple = 7
// cherry = 5
// banana = 3
```

Built-in helpers on `Map.Entry`:

| Helper | Compares |
|---|---|
| `Map.Entry.comparingByKey()` | by key (natural) |
| `Map.Entry.comparingByKey(cmp)` | by key with your rule |
| `Map.Entry.comparingByValue()` | by value (natural) |
| `Map.Entry.comparingByValue(cmp)` | by value with your rule |

### 10.4 Keep the sorted order in a Map (`LinkedHashMap`)

A `HashMap` forgets order. A `LinkedHashMap` **remembers insertion order**, so insert in sorted order.

```java
Map<String, Integer> sortedByValueDesc = map.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,              // merge function (needed for the 4-arg version)
                LinkedHashMap::new));     // keeps the sorted order

System.out.println(sortedByValueDesc);   // {apple=7, cherry=5, banana=3}
```

```
 HashMap  --stream--> sorted entries --collect--> LinkedHashMap
 (no order)           (by value)                  (remembers that order)
```

> If you use `Collectors.toMap(k, v)` without `LinkedHashMap::new`, you get a plain `HashMap` and **the sorted order is lost**.

### 10.5 Sort by value, tie-break by key

```java
entries.sort(
    Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
             .thenComparing(Map.Entry.comparingByKey()));
```

### 10.6 Map whose values are custom objects

```java
Map<Integer, Student> byId = new HashMap<>();
students.forEach(s -> byId.put(s.id(), s));

// Entries sorted by the student's MARKS (a field inside the value)
List<Map.Entry<Integer, Student>> list = new ArrayList<>(byId.entrySet());
list.sort(Comparator.comparingDouble(e -> e.getValue().marks()));

// Just the values sorted by name
List<Student> vals = new ArrayList<>(byId.values());
vals.sort(Comparator.comparing(Student::name));
```

### 10.7 Map where the KEY is a custom object

Keys in a `TreeMap` need a rule (`Comparable` or a `Comparator`). Keys in a `HashMap` need `equals()` + `hashCode()` (not a compare rule).

```java
Map<Student, String> remarks =
        new TreeMap<>(Comparator.comparing(Student::name));   // keys sorted by name
remarks.put(new Student(1, "Ravi", 88.5), "Good");
remarks.put(new Student(2, "Amit", 92.0), "Excellent");
System.out.println(remarks.keySet());   // Amit first, then Ravi
```

### 10.8 Find the max/min entry by value (no full sort)

```java
Map.Entry<String, Integer> top =
        Collections.max(map.entrySet(), Map.Entry.comparingByValue());
System.out.println(top.getKey());    // apple

// Streams version
String topKey = map.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
```

This is **O(n)**, faster than sorting (O(n log n)) when you only need the top one.

### 10.9 Top-K by value with a `PriorityQueue`

```java
int k = 2;
PriorityQueue<Map.Entry<String, Integer>> minHeap =
        new PriorityQueue<>(Map.Entry.comparingByValue());   // smallest value on top

for (Map.Entry<String, Integer> e : map.entrySet()) {
    minHeap.offer(e);
    if (minHeap.size() > k) minHeap.poll();   // kick out the smallest
}
// minHeap now holds the k largest-value entries
```

### Summary: Map comparisons

| Goal | Tool |
|---|---|
| Keep keys sorted all the time | `TreeMap` (+ optional key Comparator) |
| Sort by **values** | Copy `entrySet()` to a List, sort with `Map.Entry.comparingByValue(...)` |
| Keep a sorted-by-value result as a Map | Collect into `LinkedHashMap` |
| Sort by a field **inside** a value object | `Comparator.comparing(e -> e.getValue().field())` |
| Only the max/min entry | `Collections.max/min` or `stream.max/min` |
| Top K entries | `PriorityQueue` of size K |

---

## 11. equals() vs compareTo()

These answer **different questions** and different collections use different ones:

| | Question | Used by |
|---|---|---|
| `equals()` + `hashCode()` | "Are these the **same**?" | `HashSet`, `HashMap`, `LinkedHashSet`, `contains`, `indexOf` |
| `compareTo()` / `compare()` | "Which comes **first**? (0 = same position)" | `TreeSet`, `TreeMap`, `PriorityQueue` ordering, sorting |

### The big gotcha: `TreeSet` treats "compare = 0" as duplicate

```java
TreeSet<Student> set = new TreeSet<>(Comparator.comparingDouble(Student::marks));

set.add(new Student(1, "Ravi", 88.5));
set.add(new Student(3, "Neha", 88.5));   // marks equal -> compare = 0 -> "duplicate"!

System.out.println(set.size());   // 1   (Neha silently dropped!)
```

```
 Ravi (88.5)  vs  Neha (88.5)   compare() = 0
 TreeSet: "0 means same -> I already have it -> ignore."
 
 FIX: add a tie-breaker so that different students never compare as 0.
```

```java
TreeSet<Student> set = new TreeSet<>(
        Comparator.comparingDouble(Student::marks)
                  .thenComparingInt(Student::id));   // unique tie-breaker
```

### "Consistent with equals"

Best practice: if `a.compareTo(b) == 0` then `a.equals(b)` should be `true` (and vice versa). If you break this, `TreeSet` and `HashSet` containing the same objects can behave differently.

(Example from the JDK itself: `BigDecimal("1.0")` and `BigDecimal("1.00")` are `compareTo == 0` but **not** `equals`.)

---

## 12. Common Mistakes

1. **No rule given** for a custom class in `TreeSet`/`TreeMap`/`PriorityQueue` leads to `ClassCastException`.
2. **Using `a - b`** instead of `Integer.compare(a, b)` can overflow.
3. **Compare result of 0 in `TreeSet`/`TreeMap`** silently drops entries. Add a unique tie-breaker.
4. **Expecting a `TreeMap` to sort by value.** It only sorts keys.
5. **`Collectors.toMap` without `LinkedHashMap::new`** loses the sorted order.
6. **Wrong direction.** Swapping `(a, b)` to `(b, a)` flips ascending/descending. Test with a tiny example.
7. **Comparing `Integer` or `String` objects with `==`** instead of `compareTo`/`equals`.
8. **`null` fields** cause `NullPointerException`. Use `Comparator.nullsFirst/nullsLast`.
9. **Mutating a field used in the comparator** after inserting into a `TreeSet`/`TreeMap` breaks the tree's ordering. Avoid it.
10. **Inconsistent comparators** (e.g., `compare(a,b)` and `compare(b,a)` not opposite) can throw `IllegalArgumentException: Comparison method violates its general contract!`.
11. **`Arrays.sort(int[], comparator)` does not exist.** Use `Integer[]`.
12. **`.reversed()` after a lambda** can break type inference. Use a method reference.

---

## 13. Interview Questions

**Q1. Comparable vs Comparator?**
`Comparable` is implemented **by the class** (`compareTo`) and gives **one natural order**. `Comparator` is a **separate object** (`compare`) and gives **many custom orders** without changing the class.

**Q2. When do you use each?**
`Comparable` when there is one obvious default order (id, date, name). `Comparator` when you need multiple orders, a different order, or can't modify the class.

**Q3. What does the return value of `compare` mean?**
Negative: first before second. Zero: equal. Positive: first after second.

**Q4. How do you sort by multiple fields?**
`Comparator.comparing(A::f1).thenComparing(A::f2)`, or compare field by field manually and return the first non-zero result.

**Q5. How do you sort in descending order?**
`.reversed()`, `Comparator.reverseOrder()`, or swap the arguments (`compare(b, a)`).

**Q6. Can a `TreeMap` be sorted by values?**
No. It sorts by **keys** only. To sort by values, put the entries into a `List`, sort with `Map.Entry.comparingByValue()`, and collect into a `LinkedHashMap` if you need a Map back.

**Q7. Why is `a - b` a bad comparator?**
Integer overflow can flip the sign. Use `Integer.compare(a, b)`.

**Q8. Why did my `TreeSet` lose elements?**
Your comparator returned `0` for two different objects, so `TreeSet` treated them as duplicates. Add a unique tie-breaker.

**Q9. Does `HashSet` use `compareTo`?**
No. `HashSet`/`HashMap` use `hashCode()` + `equals()`. Only tree-based and sorting structures use comparison.

**Q10. How do you handle `null` while comparing?**
`Comparator.nullsFirst(...)` or `Comparator.nullsLast(...)`.

**Q11. What is the difference between `comparing`, `comparingInt`, `comparingDouble`?**
They do the same job. The `Int`/`Double`/`Long` versions avoid autoboxing, so they are slightly faster for primitive fields.

**Q12. How do you find the entry with the max value in a Map?**
`Collections.max(map.entrySet(), Map.Entry.comparingByValue())`. That is O(n), no sorting needed.

**Q13. What is a "key extractor"?**
The function you pass to `Comparator.comparing(...)` (e.g., `Student::getName`) that tells it which part of the object to compare.

---

## Quick Revision Cheat Sheet

```
RULE of compare(a, b):   negative = a first | 0 = tie | positive = a later

COMPARABLE  (inside class)    class X implements Comparable<X> { int compareTo(X o) }
COMPARATOR  (outside)         (a, b) -> ...   or   Comparator.comparing(X::getField)

WHAT TO COMPARE:
  1 field         Comparator.comparing(X::getName)
  int/double      comparingInt / comparingDouble
  nested field    comparing(x -> x.getA().getB())
  many fields     .thenComparing(...)  (end with a unique tie-breaker)
  reverse         .reversed()  or  Comparator.reverseOrder()
  nulls           Comparator.nullsFirst / nullsLast

WHERE TO PASS:
  list.sort(cmp)   new TreeSet<>(cmp)   new TreeMap<>(cmp)   [keys only!]
  new PriorityQueue<>(cmp)   stream.sorted(cmp)   Arrays.sort(arr, cmp)

MAPS:
  by key    -> TreeMap
  by value  -> new ArrayList<>(map.entrySet()) + Map.Entry.comparingByValue()
  keep order-> collect into LinkedHashMap
  max entry -> Collections.max(entrySet, Map.Entry.comparingByValue())

REMEMBER:  Hash* uses equals+hashCode   |   Tree* & sort use compare
           compare == 0  in TreeSet/TreeMap  ==>  treated as duplicate!
```
