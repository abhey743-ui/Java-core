# Java Arrays: 1D, 2D, 3D and Jagged Arrays (Native Arrays Only)

> This guide is only about **Java's built-in (native) arrays**, the ones you write as `int[] arr = new int[5];`.
> We are **not** covering `ArrayList` or other collections here.
> You will learn how to **declare, create and initialize** arrays of every dimension, how they sit in **memory**, how to **loop** through them (with diagrams), and **where they are used** in real programs.

---

## Table of Contents

**Part 1: One-Dimensional Arrays**

1. [What Is an Array?](#1-what-is-an-array)
2. [How Arrays Live in Memory](#2-how-arrays-live-in-memory)
3. [Declaring, Creating and Initializing a 1D Array](#3-declaring-creating-and-initializing-a-1d-array)
4. [Default Values](#4-default-values)
5. [Accessing Elements, Index and length](#5-accessing-elements-index-and-length)
6. [Looping Through a 1D Array](#6-looping-through-a-1d-array)
7. [Copying Arrays](#7-copying-arrays)
8. [Passing Arrays to Methods, Returning Arrays, Varargs](#8-passing-arrays-to-methods-returning-arrays-varargs)
9. [The Helper Class `java.util.Arrays`](#9-the-helper-class-javautilarrays)
10. [Classic 1D Programs](#10-classic-1d-programs)

**Part 2: Two-Dimensional Arrays**

11. [2D Arrays: The Idea and the Memory Picture](#11-2d-arrays-the-idea-and-the-memory-picture)
12. [Declaring, Creating and Initializing 2D Arrays](#12-declaring-creating-and-initializing-2d-arrays)
13. [Looping Through 2D Arrays (with diagrams)](#13-looping-through-2d-arrays-with-diagrams)
14. [Classic 2D Programs](#14-classic-2d-programs)
15. [Jagged Arrays (Rows of Different Lengths)](#15-jagged-arrays-rows-of-different-lengths)

**Part 3: Three-Dimensional Arrays and Beyond**

16. [3D Arrays: The Idea and the Memory Picture](#16-3d-arrays-the-idea-and-the-memory-picture)
17. [Declaring, Creating and Initializing 3D Arrays](#17-declaring-creating-and-initializing-3d-arrays)
18. [Looping Through 3D Arrays](#18-looping-through-3d-arrays)
19. [4D and Higher: The Pattern](#19-4d-and-higher-the-pattern)

**Part 4: Extra Topics**

20. [Arrays of Strings, Objects and chars](#20-arrays-of-strings-objects-and-chars)
21. [Where Do We Use Arrays Exactly?](#21-where-do-we-use-arrays-exactly)
22. [Performance Tips: Loop Order and Flattening](#22-performance-tips-loop-order-and-flattening)
23. [Limitations of Arrays](#23-limitations-of-arrays)
24. [Common Mistakes and Exceptions](#24-common-mistakes-and-exceptions)
25. [Cheat Sheet, Summary and Quiz](#25-cheat-sheet-summary-and-quiz)

---

# PART 1: ONE-DIMENSIONAL ARRAYS

---

## 1. What Is an Array?

An **array** is a container that holds a **fixed number of values of the same type**, one after another, each reachable by a **number (index)**.

Imagine a row of numbered lockers, where every locker holds the same kind of thing:

```
 index:     0     1     2     3     4
         ┌─────┬─────┬─────┬─────┬─────┐
 marks   │ 85  │ 92  │ 78  │ 90  │ 66  │
         └─────┴─────┴─────┴─────┴─────┘
```

Without an array, to store 5 marks you would need 5 separate variables (`mark1`, `mark2`, ...). For 1000 marks that would be impossible. An array lets you store them all under **one name** and process them with a **loop**.

### The 6 key facts about Java arrays

| # | Fact | Meaning |
|---|------|---------|
| 1 | **Fixed size** | The length is decided when the array is created and **can never change** |
| 2 | **Same type** | All elements share one type (`int[]` holds only ints) |
| 3 | **Zero-based index** | The first element is at index `0`, the last at `length - 1` |
| 4 | **Arrays are objects** | Created with `new`, live on the **heap**, and are **reference types** |
| 5 | **Auto default values** | Elements start as `0`, `false`, `null`, etc. |
| 6 | **Bounds-checked** | Using a bad index throws `ArrayIndexOutOfBoundsException` (no memory corruption like C) |

An array can hold **primitives** (`int[]`, `double[]`, `char[]`, `boolean[]`...) or **objects** (`String[]`, `Student[]`...).

---

## 2. How Arrays Live in Memory

This is the most important picture to understand. Look at this code:

```java
int[] arr = new int[5];
```

It does **two** things:

1. `new int[5]` creates the **array object** on the **heap** (5 ints, all `0`).
2. `int[] arr` is a **reference variable** (on the stack, if it is a local variable) that **points to** that object.

```
        STACK                              HEAP
   ┌──────────────┐
   │  arr │   ●───┼──────────────►  ┌─────┬─────┬─────┬─────┬─────┐
   └──────────────┘                 │  0  │  0  │  0  │  0  │  0  │
                                    └─────┴─────┴─────┴─────┴─────┘
                                       [0]   [1]   [2]   [3]   [4]
                                       └── array object (also stores its length = 5) ──┘
```

### What this means

- The variable `arr` does **not** contain the numbers. It contains a **reference** (like an address).
- The array object also remembers its own **length**, which you read with `arr.length`.
- For primitive arrays, the values sit **side by side** in memory (this is why arrays are fast).
- For object arrays (`String[]`), each slot holds a **reference** to another object elsewhere on the heap.

```java
String[] names = new String[3];
names[0] = "Ali";
```

```
   STACK                  HEAP
  ┌────────┐          ┌──────┬──────┬──────┐
  │ names●─┼────────► │  ●   │ null │ null │
  └────────┘          └──┬───┴──────┴──────┘
                         │
                         ▼
                      ┌───────┐
                      │ "Ali" │
                      └───────┘
```

### `null` array vs empty array

```java
int[] a = null;            // no array at all (a points to nothing)
int[] b = new int[0];      // a real array with zero elements (legal!)
System.out.println(b.length);   // 0
System.out.println(a.length);   // NullPointerException
```

---

## 3. Declaring, Creating and Initializing a 1D Array

There are **three separate steps**. Beginners often mix them up, so let's name them.

| Step | Meaning | Example |
|------|---------|---------|
| **Declaration** | Tell Java the variable's name and that it is an array | `int[] arr;` |
| **Creation (allocation)** | Make the actual array object and set its size | `arr = new int[5];` |
| **Initialization** | Put the values in | `arr[0] = 10;` or `{10, 20, 30}` |

### 3.1 Declaration syntax

```java
int[] arr;        // recommended style: brackets next to the TYPE
int arr[];        // also legal (C-style): brackets next to the name
```

Both mean the same. The first style `int[] arr` is preferred because "array of int" is part of the **type**.

At this point `arr` has **no array yet**. It is just an empty reference variable.

### 3.2 Creation with `new` (size only)

```java
int[] arr = new int[5];     // declare + create in one line
```

```java
int[] arr;                  // declare first
arr = new int[5];           // create later
```

- The size can be any **non-negative `int`**: a literal, a variable or an expression.

```java
int n = 10;
int[] a = new int[n];          // OK, size from a variable
int[] b = new int[n * 2 + 1];  // OK, size from an expression
int[] c = new int[0];          // OK, empty array
int[] d = new int[-1];         // compiles, but throws NegativeArraySizeException at runtime
```

- **You must give a size** (or an initializer list). This is wrong:

```java
int[] arr = new int[];     // ERROR: array dimension missing
```

### 3.3 Creation with values: three ways

**Way 1: Initializer list at declaration (shortest)**

```java
int[] marks = {85, 92, 78, 90, 66};
```
Java counts the values and creates an array of length 5.

**Way 2: `new` with an initializer list (anonymous-array style)**

```java
int[] marks = new int[]{85, 92, 78, 90, 66};
```
Same result. You **must not** write a size here (`new int[5]{...}` is an error). This form also works **after** declaration, and when passing directly to a method:

```java
int[] marks;
marks = new int[]{85, 92, 78};     // OK
// marks = {85, 92, 78};           // ERROR: the short { } form works only at declaration

printAll(new int[]{1, 2, 3});      // passing an array without a variable
```

**Way 3: Create with size, then fill element by element**

```java
int[] marks = new int[5];
marks[0] = 85;
marks[1] = 92;
marks[2] = 78;
marks[3] = 90;
marks[4] = 66;
```

**Way 4 (bonus): Fill with a loop or `Arrays.fill`**

```java
int[] squares = new int[5];
for (int i = 0; i < squares.length; i++) {
    squares[i] = i * i;            // {0, 1, 4, 9, 16}
}

int[] allSeven = new int[5];
java.util.Arrays.fill(allSeven, 7);   // {7, 7, 7, 7, 7}
```

### 3.4 Arrays of every type

```java
int[]     a = {1, 2, 3};
double[]  d = {1.5, 2.5, 3.5};
char[]    c = {'J', 'a', 'v', 'a'};
boolean[] b = {true, false, true};
String[]  s = {"red", "green", "blue"};
long[]    l = new long[100];
byte[]    raw = new byte[1024];       // very common for files and network data
```

### 3.5 Multiple declarations on one line (careful!)

```java
int[] a, b;          // BOTH a and b are int arrays
int a[], b;          // a is an int ARRAY, b is a plain int (!!)
int[] a, b[];        // a is int[], b is int[][] (2D)
```

To avoid confusion, declare one array per line.

### 3.6 `final` arrays

```java
final int[] nums = {1, 2, 3};
nums[0] = 99;              // OK: the CONTENTS can change
nums = new int[5];         // ERROR: the REFERENCE cannot change
```

`final` locks the **variable**, not the **elements**.

### 3.7 `var` with arrays (Java 10+)

```java
var arr = new int[5];       // OK, type inferred as int[]
// var arr2 = {1, 2, 3};    // ERROR: needs an explicit type for the short initializer
```

---

## 4. Default Values

When you create an array with `new`, Java **automatically fills** it with the default value of the element type. You never get random garbage.

| Element type | Default value |
|--------------|---------------|
| `byte`, `short`, `int` | `0` |
| `long` | `0L` |
| `float` | `0.0f` |
| `double` | `0.0` |
| `char` | `'\u0000'` (the null character) |
| `boolean` | `false` |
| Any object type (`String`, `Integer`, your classes) | `null` |

```java
int[]     a = new int[3];        // [0, 0, 0]
double[]  d = new double[2];     // [0.0, 0.0]
boolean[] b = new boolean[2];    // [false, false]
String[]  s = new String[3];     // [null, null, null]
```

> `Integer[]` (wrapper type) defaults to `null`, but `int[]` defaults to `0`. Another reason to prefer `int[]`.

Remember that the **array elements** get defaults, even when the array is a local variable. But a **local variable that never received an array** gets nothing:

```java
int[] arr;
System.out.println(arr[0]);     // ERROR: variable arr might not have been initialized
```

---

## 5. Accessing Elements, Index and length

### 5.1 Reading and writing

```java
int[] arr = {10, 20, 30, 40, 50};

System.out.println(arr[0]);     // 10   (first)
System.out.println(arr[4]);     // 50   (last)
arr[2] = 99;                    // change the 3rd element
System.out.println(arr[2]);     // 99
```

```
 index:      0     1     2     3     4
          ┌─────┬─────┬─────┬─────┬─────┐
 arr      │ 10  │ 20  │ 99  │ 40  │ 50  │
          └─────┴─────┴─────┴─────┴─────┘
             ▲                       ▲
          arr[0]                  arr[4] = arr[arr.length - 1]
```

### 5.2 The index rules

- Valid indexes are **`0` to `length - 1`**.
- The index must be an **`int`** (or `byte`, `short`, `char`, which are promoted to `int`). A `long` index is an error.
- The last element is always `arr[arr.length - 1]`.

```java
int[] arr = new int[5];
arr[5] = 1;           // ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 5
arr[-1] = 1;          // ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 5
long i = 2;
arr[i] = 1;           // ERROR: possible lossy conversion from long to int
arr[(int) i] = 1;     // OK
```

### 5.3 The `length` property

```java
int[] arr = {10, 20, 30};
System.out.println(arr.length);    // 3
```

**No parentheses!** `length` is a **field** of arrays. Compare:

| Thing | Way to get its size |
|-------|---------------------|
| Array | `arr.length` (field, no brackets) |
| `String` | `s.length()` (method) |
| `ArrayList` | `list.size()` (method) |

```java
arr.length();    // ERROR: it's not a method
```

### 5.4 Printing an array (a very common surprise)

```java
int[] arr = {1, 2, 3};
System.out.println(arr);                       // [I@1b6d3586   (type code + hash, NOT the contents!)
System.out.println(java.util.Arrays.toString(arr));   // [1, 2, 3]
```

The strange text `[I@1b6d3586` means "an int array at hash code 1b6d3586". Prefixes you may see:

| Printed prefix | Meaning |
|----------------|---------|
| `[I@...` | `int[]` |
| `[D@...` | `double[]` |
| `[C@...` | `char[]` (but `println(char[])` is special: see section 20) |
| `[Ljava.lang.String;@...` | `String[]` |
| `[[I@...` | `int[][]` (2D int array) |

---

## 6. Looping Through a 1D Array

### 6.1 Forward loop with an index (the classic `for`)

```java
int[] arr = {10, 20, 30, 40, 50};

for (int i = 0; i < arr.length; i++) {
    System.out.println("arr[" + i + "] = " + arr[i]);
}
```

Output:
```
arr[0] = 10
arr[1] = 20
arr[2] = 30
arr[3] = 40
arr[4] = 50
```

```
 i=0    i=1    i=2    i=3    i=4
 ┌────┬─────┬─────┬─────┬─────┐
 │ 10 │ 20  │ 30  │ 40  │ 50  │
 └────┴─────┴─────┴─────┴─────┘
   ───────────────────────────►   forward, i++
```

Note the condition is **`i < arr.length`**, not `<=`. Using `<=` would hit index 5 and crash.

### 6.2 Reverse loop

```java
for (int i = arr.length - 1; i >= 0; i--) {
    System.out.print(arr[i] + " ");        // 50 40 30 20 10
}
```

```
   ◄───────────────────────────   reverse, i--
 ┌────┬─────┬─────┬─────┬─────┐
 │ 10 │ 20  │ 30  │ 40  │ 50  │
 └────┴─────┴─────┴─────┴─────┘
```

### 6.3 Enhanced `for` loop (for-each)

Use it when you only need to **read** every element and don't need the index.

```java
for (int value : arr) {
    System.out.print(value + " ");         // 10 20 30 40 50
}
```

Read it as: *"for each `value` in `arr`"*.

**Important limitations of for-each:**

```java
// 1) It does NOT change the array for primitives (value is just a copy)
for (int v : arr) {
    v = v * 2;               // changes only the local copy
}
// arr is still {10, 20, 30, 40, 50}

// 2) You don't get the index
// 3) You can't go backward or skip elements
```

To modify elements, use the indexed `for` loop:

```java
for (int i = 0; i < arr.length; i++) {
    arr[i] = arr[i] * 2;         // {20, 40, 60, 80, 100}
}
```

### 6.4 `while` loop

```java
int i = 0;
while (i < arr.length) {
    System.out.print(arr[i] + " ");
    i++;
}
```

### 6.5 Skipping, stepping and searching

```java
// Every second element (step of 2)
for (int i = 0; i < arr.length; i += 2) { ... }       // indexes 0, 2, 4

// Stop early when found
for (int i = 0; i < arr.length; i++) {
    if (arr[i] == 30) {
        System.out.println("Found at index " + i);
        break;
    }
}

// Two-pointer loop (compare from both ends)
int left = 0, right = arr.length - 1;
while (left < right) {
    // do something with arr[left] and arr[right]
    left++;
    right--;
}
```

### 6.6 Which loop should I use?

| Need | Best loop |
|------|-----------|
| Read every element, no index needed | **for-each** |
| Need the index, or need to modify elements | **indexed `for`** |
| Go backward or skip | **indexed `for`** |
| Loop until a condition (unknown count) | `while` |

---

## 7. Copying Arrays

### 7.1 The trap: `=` does NOT copy the array

```java
int[] a = {1, 2, 3};
int[] b = a;           // copies the REFERENCE, not the array
b[0] = 99;
System.out.println(a[0]);     // 99  ← a changed too!
```

```
  STACK              HEAP
 ┌───────┐
 │ a  ●──┼───┐      ┌────┬────┬────┐
 ├───────┤   ├────► │ 99 │ 2  │ 3  │     ← ONE array, TWO names (aliasing)
 │ b  ●──┼───┘      └────┴────┴────┘
 └───────┘
```

### 7.2 The real ways to copy

```java
import java.util.Arrays;

int[] src = {1, 2, 3, 4, 5};

// 1) clone()
int[] c1 = src.clone();

// 2) Arrays.copyOf(array, newLength)   (can also resize)
int[] c2 = Arrays.copyOf(src, src.length);        // {1, 2, 3, 4, 5}
int[] bigger = Arrays.copyOf(src, 8);             // {1, 2, 3, 4, 5, 0, 0, 0}  (padded with defaults)
int[] smaller = Arrays.copyOf(src, 3);            // {1, 2, 3}                 (cut off)

// 3) Arrays.copyOfRange(array, from, to)   → 'to' is EXCLUSIVE
int[] part = Arrays.copyOfRange(src, 1, 4);       // {2, 3, 4}

// 4) System.arraycopy(source, srcPos, dest, destPos, count)   (fastest, most flexible)
int[] dest = new int[5];
System.arraycopy(src, 0, dest, 0, 5);             // {1, 2, 3, 4, 5}

// 5) A manual loop
int[] c3 = new int[src.length];
for (int i = 0; i < src.length; i++) c3[i] = src[i];
```

### 7.3 Shallow copy warning (arrays of objects)

For arrays of **objects** (or arrays of arrays), all these methods copy only the **references**. Both arrays then point to the **same objects**. This is called a **shallow copy**. (More in the 2D section.)

### 7.4 Resizing = creating a new, bigger array

Arrays can't grow. To "grow" one, make a bigger array and copy:

```java
int[] data = {1, 2, 3};
data = Arrays.copyOf(data, data.length * 2);   // new array {1, 2, 3, 0, 0, 0}; the old one is garbage collected
```

---

## 8. Passing Arrays to Methods, Returning Arrays, Varargs

### 8.1 Passing an array to a method

Java always passes **by value**, but for arrays the "value" is the **reference**. So the method gets a **copy of the reference**, pointing to the **same array**.

Result: a method **can change the contents** of your array, but it **can't replace** your array variable.

```java
static void doubleAll(int[] arr) {
    for (int i = 0; i < arr.length; i++) {
        arr[i] *= 2;              // changes the caller's array
    }
}

static void tryToReplace(int[] arr) {
    arr = new int[]{9, 9, 9};     // only changes the local reference
}

public static void main(String[] args) {
    int[] nums = {1, 2, 3};
    doubleAll(nums);
    System.out.println(Arrays.toString(nums));   // [2, 4, 6]   ← changed
    tryToReplace(nums);
    System.out.println(Arrays.toString(nums));   // [2, 4, 6]   ← unchanged
}
```

### 8.2 Returning an array

```java
static int[] makeSquares(int n) {
    int[] result = new int[n];
    for (int i = 0; i < n; i++) result[i] = i * i;
    return result;
}

int[] sq = makeSquares(5);       // {0, 1, 4, 9, 16}
```

### 8.3 Varargs: `int... numbers` is really an `int[]`

```java
static int sum(int... numbers) {          // inside the method, numbers is an int[]
    int total = 0;
    for (int n : numbers) total += n;
    return total;
}

sum();               // 0
sum(5);              // 5
sum(1, 2, 3, 4);     // 10
sum(new int[]{7, 8});// 15  (you can also pass a real array)
```

### 8.4 `main(String[] args)`

The `args` in `public static void main(String[] args)` is a **String array** holding the command-line arguments:

```
java MyApp hello 42
```
```java
System.out.println(args.length);    // 2
System.out.println(args[0]);        // hello
System.out.println(args[1]);        // 42   (still a String; use Integer.parseInt to convert)
```

---

## 9. The Helper Class `java.util.Arrays`

Java's array has no methods of its own, so the utility class `java.util.Arrays` provides them as static methods. (`import java.util.Arrays;`)

| Method | What it does | Example |
|--------|--------------|---------|
| `Arrays.toString(a)` | Text form of a 1D array | `[1, 2, 3]` |
| `Arrays.deepToString(a)` | Text form of a 2D/3D array | `[[1, 2], [3, 4]]` |
| `Arrays.sort(a)` | Sorts ascending (in place) | `{3,1,2}` → `{1,2,3}` |
| `Arrays.sort(a, from, to)` | Sorts a range (`to` exclusive) | |
| `Arrays.fill(a, v)` | Sets every element to `v` | `Arrays.fill(a, -1)` |
| `Arrays.copyOf(a, n)` | New array of length `n` | |
| `Arrays.copyOfRange(a, i, j)` | Copy of `[i, j)` | |
| `Arrays.equals(a, b)` | Same length and same elements? | |
| `Arrays.deepEquals(a, b)` | Same, for nested arrays | |
| `Arrays.binarySearch(a, key)` | Find in a **sorted** array | returns index (or negative if missing) |
| `Arrays.hashCode(a)` | Hash based on contents | |
| `Arrays.stream(a).sum()` | Sum of an `int[]` (Java 8+) | |

```java
int[] a = {5, 3, 9, 1};
Arrays.sort(a);
System.out.println(Arrays.toString(a));              // [1, 3, 5, 9]
System.out.println(Arrays.binarySearch(a, 5));       // 2

int[] x = {1, 2, 3};
int[] y = {1, 2, 3};
System.out.println(x == y);                // false  (different objects)
System.out.println(x.equals(y));           // false  (arrays don't override equals!)
System.out.println(Arrays.equals(x, y));   // true   (compares the contents)
```

> **Rule:** never compare arrays with `==` or `.equals()` when you want to compare contents. Use `Arrays.equals` (1D) or `Arrays.deepEquals` (2D/3D).

---

## 10. Classic 1D Programs

### 10.1 Sum, average, max and min

```java
int[] marks = {85, 92, 78, 90, 66};

int sum = 0;
int max = marks[0];
int min = marks[0];

for (int m : marks) {
    sum += m;
    if (m > max) max = m;
    if (m < min) min = m;
}

double average = (double) sum / marks.length;

System.out.println("Sum     = " + sum);        // 411
System.out.println("Average = " + average);    // 82.2
System.out.println("Max     = " + max);        // 92
System.out.println("Min     = " + min);        // 66
```

### 10.2 Reverse an array in place

```java
int[] arr = {1, 2, 3, 4, 5};

for (int left = 0, right = arr.length - 1; left < right; left++, right--) {
    int temp = arr[left];
    arr[left] = arr[right];
    arr[right] = temp;
}
System.out.println(Arrays.toString(arr));     // [5, 4, 3, 2, 1]
```

```
 start:   [1] [2] [3] [4] [5]
           ↕               ↕        swap(left=0, right=4)
          [5] [2] [3] [4] [1]
               ↕       ↕            swap(left=1, right=3)
          [5] [4] [3] [2] [1]       left=2, right=2 → stop
```

### 10.3 Linear search

```java
static int indexOf(int[] arr, int target) {
    for (int i = 0; i < arr.length; i++) {
        if (arr[i] == target) return i;
    }
    return -1;      // not found
}
```

### 10.4 Counting frequencies (a classic array trick)

Count how many times each letter appears in a word:

```java
String word = "hello";
int[] freq = new int[26];                 // one slot per letter a-z

for (char c : word.toCharArray()) {
    freq[c - 'a']++;                      // 'a' → 0, 'b' → 1, ... 'z' → 25
}

for (int i = 0; i < 26; i++) {
    if (freq[i] > 0) {
        System.out.println((char) ('a' + i) + " = " + freq[i]);
    }
}
```

Output:
```
e = 1
h = 1
l = 2
o = 1
```

Here the **array index itself** is the data (the letter). This "index as key" idea is used everywhere.

### 10.5 Bubble sort

```java
int[] a = {5, 2, 9, 1, 7};

for (int pass = 0; pass < a.length - 1; pass++) {
    for (int i = 0; i < a.length - 1 - pass; i++) {
        if (a[i] > a[i + 1]) {
            int t = a[i];
            a[i] = a[i + 1];
            a[i + 1] = t;
        }
    }
}
System.out.println(Arrays.toString(a));     // [1, 2, 5, 7, 9]
```

---

# PART 2: TWO-DIMENSIONAL ARRAYS

---

## 11. 2D Arrays: The Idea and the Memory Picture

### 11.1 The idea: a table (rows and columns)

A **2D array** is like a **table**, a **grid** or a **matrix**: it has **rows** and **columns**. You need **two indexes** to reach one cell: the **row** and then the **column**.

```
                 col 0     col 1     col 2     col 3
              ┌─────────┬─────────┬─────────┬─────────┐
      row 0   │ [0][0]  │ [0][1]  │ [0][2]  │ [0][3]  │
              ├─────────┼─────────┼─────────┼─────────┤
      row 1   │ [1][0]  │ [1][1]  │ [1][2]  │ [1][3]  │
              ├─────────┼─────────┼─────────┼─────────┤
      row 2   │ [2][0]  │ [2][1]  │ [2][2]  │ [2][3]  │
              └─────────┴─────────┴─────────┴─────────┘

              grid[row][col]      grid has 3 rows and 4 columns
```

### 11.2 The truth: a 2D array is an ARRAY OF ARRAYS

In Java there is no real "table" in memory. A 2D array is **a 1D array whose elements are themselves 1D arrays** (the rows).

```java
int[][] grid = new int[3][4];
```

```
    STACK                    HEAP

 ┌──────────┐         ┌───────────┐
 │ grid  ●──┼───────► │ grid[0]  ●┼──────► [ 0 | 0 | 0 | 0 ]   row 0 (an int[4])
 └──────────┘         ├───────────┤
                      │ grid[1]  ●┼──────► [ 0 | 0 | 0 | 0 ]   row 1 (an int[4])
                      ├───────────┤
                      │ grid[2]  ●┼──────► [ 0 | 0 | 0 | 0 ]   row 2 (an int[4])
                      └───────────┘
                      the "outer" array: 3 references, one per row
```

So `new int[3][4]` creates **4 objects**: **1** outer array + **3** row arrays.

### 11.3 Why this matters

1. `grid[0]` is itself an `int[]` (the whole first row).
2. `grid.length` = **number of rows**.
3. `grid[0].length` = **number of columns** (in row 0).
4. Each row **can have a different length** (jagged arrays, section 15).
5. You can **swap whole rows** by swapping references.

```java
int[][] grid = new int[3][4];
System.out.println(grid.length);        // 3  (rows)
System.out.println(grid[0].length);     // 4  (columns in row 0)

int[] firstRow = grid[0];               // grabs the entire first row
```

---

## 12. Declaring, Creating and Initializing 2D Arrays

### 12.1 Declaration syntax

```java
int[][] grid;       // recommended
int grid[][];       // legal (C-style)
int[] grid[];       // legal but confusing: don't use
```

### 12.2 Creation with sizes: `new type[rows][columns]`

```java
int[][] grid = new int[3][4];       // 3 rows, 4 columns, all zeros
```

```
        c0  c1  c2  c3
 r0   [  0   0   0   0 ]
 r1   [  0   0   0   0 ]
 r2   [  0   0   0   0 ]
```

You can use variables for the sizes:

```java
int rows = 5, cols = 6;
double[][] table = new double[rows][cols];
```

**The first size is required; the second is optional** (see jagged arrays):

```java
int[][] a = new int[3][];       // OK: 3 rows, each row is null for now
int[][] b = new int[][4];       // ERROR: the row count must be given first
int[][] c = new int[][];        // ERROR
```

### 12.3 Creation with values (initializer list)

Each inner `{ }` is **one row**.

```java
int[][] grid = {
    {1, 2, 3, 4},       // row 0
    {5, 6, 7, 8},       // row 1
    {9, 10, 11, 12}     // row 2
};
```

```
         col 0  col 1  col 2  col 3
 row 0  [  1      2      3      4  ]
 row 1  [  5      6      7      8  ]
 row 2  [  9     10     11     12  ]
```

Also valid with `new`:

```java
int[][] grid = new int[][]{
    {1, 2, 3},
    {4, 5, 6}
};
```

### 12.4 Creation, then fill cell by cell

```java
int[][] grid = new int[3][4];
grid[0][0] = 1;
grid[0][1] = 2;
grid[2][3] = 99;
```

Or with nested loops (very common):

```java
int[][] table = new int[3][4];
int counter = 1;
for (int i = 0; i < table.length; i++) {
    for (int j = 0; j < table[i].length; j++) {
        table[i][j] = counter++;
    }
}
// table is now {{1,2,3,4},{5,6,7,8},{9,10,11,12}}
```

### 12.5 Creating a 2D array row by row (manual)

Because rows are separate arrays, you can build them one at a time:

```java
int[][] grid = new int[3][];       // outer array only
grid[0] = new int[4];
grid[1] = new int[4];
grid[2] = new int[]{7, 8, 9, 10};  // you can plug in a ready-made array
```

### 12.6 Accessing cells

```java
int[][] grid = {
    {1, 2, 3, 4},
    {5, 6, 7, 8},
    {9, 10, 11, 12}
};

System.out.println(grid[0][0]);     // 1   (row 0, col 0)
System.out.println(grid[1][2]);     // 7   (row 1, col 2)
System.out.println(grid[2][3]);     // 12  (last row, last col)
grid[1][2] = 70;                    // change a cell
```

```
 grid[1][2] means: go to row 1, then column 2

         c0   c1   c2   c3
 r0    │  1 │  2 │  3 │  4 │
 r1    │  5 │  6 │ [7]│  8 │     ◄── grid[1][2] = 7
 r2    │  9 │ 10 │ 11 │ 12 │
```

**Order matters: `[row][column]`**, and in image/graphics code it often becomes `[y][x]`.

### 12.7 Printing a 2D array

```java
System.out.println(grid);                          // [[I@6d06d69c   (useless)
System.out.println(Arrays.toString(grid));         // [[I@..., [I@..., [I@...]  (prints the row references!)
System.out.println(Arrays.deepToString(grid));     // [[1, 2, 3, 4], [5, 6, 7, 8], [9, 10, 11, 12]]   ✓
```

Use **`Arrays.deepToString`** for 2D and 3D arrays.

### 12.8 Default values and `fill` for 2D

```java
int[][] g = new int[2][3];          // all 0
boolean[][] seats = new boolean[5][10];   // all false
String[][] names = new String[2][2];      // all null

// Fill every cell with -1: Arrays.fill works on ONE row at a time
for (int[] row : g) {
    Arrays.fill(row, -1);
}
```

---

## 13. Looping Through 2D Arrays (with diagrams)

To visit **every cell** of a 2D array you need **two loops, one inside the other** (a **nested loop**):

- The **outer** loop picks the **row** (`i`).
- The **inner** loop walks across the **columns** (`j`) of that row.

### 13.1 Row-by-row traversal (the standard way, "row-major")

```java
int[][] grid = {
    {1,  2,  3,  4},
    {5,  6,  7,  8},
    {9, 10, 11, 12}
};

for (int i = 0; i < grid.length; i++) {            // rows
    for (int j = 0; j < grid[i].length; j++) {     // columns in row i
        System.out.print(grid[i][j] + " ");
    }
    System.out.println();                          // new line after each row
}
```

Output:
```
1 2 3 4 
5 6 7 8 
9 10 11 12 
```

The **order** in which the cells are visited (the numbers show the visiting order):

```
            j=0     j=1     j=2     j=3
          ┌───────┬───────┬───────┬───────┐
  i=0  ►  │   1   │   2   │   3   │   4   │   1st row, left → right
          ├───────┼───────┼───────┼───────┤
  i=1  ►  │   5   │   6   │   7   │   8   │   then the 2nd row
          ├───────┼───────┼───────┼───────┤
  i=2  ►  │   9   │  10   │  11   │  12   │   then the 3rd row
          └───────┴───────┴───────┴───────┘

  Visiting order: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
```

### 13.2 A trace of how the loops run

For a smaller `int[2][3]`:

| Step | `i` | `j` | Cell visited |
|------|-----|-----|--------------|
| 1 | 0 | 0 | `[0][0]` |
| 2 | 0 | 1 | `[0][1]` |
| 3 | 0 | 2 | `[0][2]` |
| (inner loop ends, `j` resets) | | | |
| 4 | 1 | 0 | `[1][0]` |
| 5 | 1 | 1 | `[1][1]` |
| 6 | 1 | 2 | `[1][2]` |

**Key insight:** for each value of the **outer** variable `i`, the **inner** variable `j` runs from its **start to its end**. The inner loop spins **fast**, the outer loop spins **slowly** (like the minute hand and the hour hand of a clock).

### 13.3 Column-by-column traversal ("column-major")

Swap the loops: put the **columns on the outside**.

```java
for (int j = 0; j < grid[0].length; j++) {         // columns on the OUTSIDE
    for (int i = 0; i < grid.length; i++) {        // rows on the INSIDE
        System.out.print(grid[i][j] + " ");
    }
    System.out.println();
}
```

Output:
```
1 5 9 
2 6 10 
3 7 11 
4 8 12 
```

The visiting order (numbers = order of visit):

```
            j=0     j=1     j=2     j=3
          ┌───────┬───────┬───────┬───────┐
  i=0     │   1   │   4   │   7   │  10   │
          │   ▼   │   ▼   │   ▼   │   ▼   │
          ├───────┼───────┼───────┼───────┤
  i=1     │   2   │   5   │   8   │  11   │
          │   ▼   │   ▼   │   ▼   │   ▼   │
          ├───────┼───────┼───────┼───────┤
  i=2     │   3   │   6   │   9   │  12   │
          └───────┴───────┴───────┴───────┘
   Go DOWN column 0 first, then column 1, and so on.
```

> This version assumes all rows have the same length (a rectangular array).

### 13.4 For-each with 2D arrays

Remember that each row is itself an `int[]`:

```java
for (int[] row : grid) {          // each 'row' is a whole int[]
    for (int value : row) {       // each 'value' is one cell
        System.out.print(value + " ");
    }
    System.out.println();
}
```

Nice and clean, but you lose the indexes `i` and `j`, and (for primitives) you can't change cells through `value`.

You **can** change cells if you need only the row index:

```java
for (int[] row : grid) {
    row[0] = 0;       // OK! 'row' is a reference to the real row, so this changes the grid
}
```

### 13.5 Walking only part of the grid

**Only one row:**
```java
for (int j = 0; j < grid[1].length; j++) {
    System.out.print(grid[1][j] + " ");        // row 1 only: 5 6 7 8
}
```

**Only one column:**
```java
for (int i = 0; i < grid.length; i++) {
    System.out.print(grid[i][2] + " ");        // column 2 only: 3 7 11
}
```

**The main diagonal (top-left to bottom-right): `i == j`** (square grids):
```java
int[][] m = {
    {1, 2, 3},
    {4, 5, 6},
    {7, 8, 9}
};
for (int i = 0; i < m.length; i++) {
    System.out.print(m[i][i] + " ");            // 1 5 9
}
```

**The anti-diagonal (top-right to bottom-left): `i + j == n - 1`:**
```java
int n = m.length;
for (int i = 0; i < n; i++) {
    System.out.print(m[i][n - 1 - i] + " ");    // 3 5 7
}
```

```
   Main diagonal          Anti-diagonal
   ┌───┬───┬───┐          ┌───┬───┬───┐
   │ 1 │ 2 │ 3 │          │ 1 │ 2 │[3]│
   ├───┼───┼───┤          ├───┼───┼───┤
   │ 4 │[5]│ 6 │          │ 4 │[5]│ 6 │
   ├───┼───┼───┤          ├───┼───┼───┤
   │ 7 │ 8 │[9]│          │[7]│ 8 │ 9 │
   └───┴───┴───┘          └───┴───┴───┘
   [1] [5] [9]            [3] [5] [7]
   (i == j)               (i + j == n-1)
```

**The border cells only:**
```java
for (int i = 0; i < rows; i++) {
    for (int j = 0; j < cols; j++) {
        if (i == 0 || i == rows - 1 || j == 0 || j == cols - 1) {
            // this cell is on the border
        }
    }
}
```

### 13.6 Pretty-printing a table

```java
for (int[] row : grid) {
    for (int v : row) {
        System.out.printf("%4d", v);       // each number takes 4 characters, right-aligned
    }
    System.out.println();
}
```
```
   1   2   3   4
   5   6   7   8
   9  10  11  12
```

---

## 14. Classic 2D Programs

### 14.1 Sum of each row and each column

```java
int[][] m = {
    {1, 2, 3},
    {4, 5, 6},
    {7, 8, 9}
};

// Row sums
for (int i = 0; i < m.length; i++) {
    int rowSum = 0;
    for (int j = 0; j < m[i].length; j++) {
        rowSum += m[i][j];
    }
    System.out.println("Row " + i + " sum = " + rowSum);
}
// Row 0 sum = 6,  Row 1 sum = 15,  Row 2 sum = 24

// Column sums
for (int j = 0; j < m[0].length; j++) {
    int colSum = 0;
    for (int i = 0; i < m.length; i++) {
        colSum += m[i][j];
    }
    System.out.println("Col " + j + " sum = " + colSum);
}
// Col 0 sum = 12,  Col 1 sum = 15,  Col 2 sum = 18
```

### 14.2 Transpose (rows become columns)

```java
int[][] a = {
    {1, 2, 3},
    {4, 5, 6}
};                                           // 2 rows × 3 columns

int[][] t = new int[a[0].length][a.length];  // 3 rows × 2 columns

for (int i = 0; i < a.length; i++) {
    for (int j = 0; j < a[0].length; j++) {
        t[j][i] = a[i][j];                   // swap the indexes
    }
}
System.out.println(Arrays.deepToString(t));  // [[1, 4], [2, 5], [3, 6]]
```

```
   a (2×3)                 t (3×2)
 ┌───┬───┬───┐           ┌───┬───┐
 │ 1 │ 2 │ 3 │           │ 1 │ 4 │
 ├───┼───┼───┤    ──►    ├───┼───┤
 │ 4 │ 5 │ 6 │           │ 2 │ 5 │
 └───┴───┴───┘           ├───┼───┤
                         │ 3 │ 6 │
                         └───┴───┘
```

### 14.3 Matrix addition

```java
int[][] A = {{1, 2}, {3, 4}};
int[][] B = {{5, 6}, {7, 8}};
int[][] C = new int[2][2];

for (int i = 0; i < 2; i++)
    for (int j = 0; j < 2; j++)
        C[i][j] = A[i][j] + B[i][j];

// C = {{6, 8}, {10, 12}}
```

### 14.4 Matrix multiplication (three nested loops)

```java
int[][] A = {{1, 2, 3},
             {4, 5, 6}};            // 2×3
int[][] B = {{7,  8},
             {9,  10},
             {11, 12}};             // 3×2

int[][] C = new int[2][2];          // result is 2×2

for (int i = 0; i < 2; i++) {            // row of A
    for (int j = 0; j < 2; j++) {        // column of B
        for (int k = 0; k < 3; k++) {    // walk along the row of A and the column of B
            C[i][j] += A[i][k] * B[k][j];
        }
    }
}
System.out.println(Arrays.deepToString(C));    // [[58, 64], [139, 154]]
```

Check: `C[0][0] = 1×7 + 2×9 + 3×11 = 7 + 18 + 33 = 58` ✓

### 14.5 A tic-tac-toe board

```java
char[][] board = new char[3][3];
for (char[] row : board) Arrays.fill(row, '.');

board[0][0] = 'X';
board[1][1] = 'O';
board[2][2] = 'X';

for (char[] row : board) {
    for (char c : row) System.out.print(c + " ");
    System.out.println();
}
```
Output:
```
X . . 
. O . 
. . X 
```

### 14.6 A multiplication table

```java
int[][] table = new int[5][5];
for (int i = 0; i < 5; i++)
    for (int j = 0; j < 5; j++)
        table[i][j] = (i + 1) * (j + 1);
```
```
  1   2   3   4   5
  2   4   6   8  10
  3   6   9  12  15
  4   8  12  16  20
  5  10  15  20  25
```

---

## 15. Jagged Arrays (Rows of Different Lengths)

### 15.1 What is a jagged array?

Because a 2D array is an **array of arrays**, each row can have **its own length**. Such an array is called **jagged** (or "ragged").

```java
int[][] jag = new int[3][];     // 3 rows, but row lengths not decided yet
jag[0] = new int[1];
jag[1] = new int[3];
jag[2] = new int[2];
```

```
   STACK            HEAP
 ┌───────┐
 │ jag ●─┼──► ┌────────┐
 └───────┘    │ jag[0]●┼──► [ 0 ]                    length 1
              ├────────┤
              │ jag[1]●┼──► [ 0 | 0 | 0 ]            length 3
              ├────────┤
              │ jag[2]●┼──► [ 0 | 0 ]                length 2
              └────────┘
```

Compare a **rectangular** array (every row the same length) with a **jagged** one:

```
 Rectangular int[3][4]          Jagged
 ┌───┬───┬───┬───┐              ┌───┐
 │   │   │   │   │              │   │
 ├───┼───┼───┼───┤              ├───┼───┬───┐
 │   │   │   │   │              │   │   │   │
 ├───┼───┼───┼───┤              ├───┼───┴───┘
 │   │   │   │   │              │   │   │
 └───┴───┴───┴───┘              └───┴───┘
```

### 15.2 Creating jagged arrays with values

```java
int[][] jag = {
    {1},
    {2, 3, 4},
    {5, 6}
};

System.out.println(jag.length);       // 3
System.out.println(jag[0].length);    // 1
System.out.println(jag[1].length);    // 3
System.out.println(jag[2].length);    // 2
```

### 15.3 Looping a jagged array

**Always use `arr[i].length` for the inner loop**, never a fixed number or `arr[0].length`:

```java
for (int i = 0; i < jag.length; i++) {
    for (int j = 0; j < jag[i].length; j++) {     // ← length of THIS row
        System.out.print(jag[i][j] + " ");
    }
    System.out.println();
}
```
Output:
```
1 
2 3 4 
5 6 
```

For-each works naturally with jagged arrays too (it doesn't care about the row lengths):

```java
for (int[] row : jag)
    for (int v : row)
        System.out.print(v + " ");
```

### 15.4 Example: Pascal's Triangle (a perfect jagged array)

```java
int rows = 5;
int[][] tri = new int[rows][];

for (int i = 0; i < rows; i++) {
    tri[i] = new int[i + 1];              // row i has i+1 elements
    tri[i][0] = 1;                        // first element of every row
    tri[i][i] = 1;                        // last element of every row
    for (int j = 1; j < i; j++) {
        tri[i][j] = tri[i - 1][j - 1] + tri[i - 1][j];   // sum of the two numbers above
    }
}

for (int[] row : tri) {
    for (int v : row) System.out.print(v + " ");
    System.out.println();
}
```
Output:
```
1 
1 1 
1 2 1 
1 3 3 1 
1 4 6 4 1 
```

```
 tri[0] ──►  [1]
 tri[1] ──►  [1, 1]
 tri[2] ──►  [1, 2, 1]
 tri[3] ──►  [1, 3, 3, 1]
 tri[4] ──►  [1, 4, 6, 4, 1]
```

### 15.5 Why use jagged arrays?

- **Save memory** when rows naturally have different sizes (triangles, adjacency lists for graphs, words of different lengths, a class where each student took a different number of tests).
- Model data like **"each day has a different number of events"**.

### 15.6 The `null` row trap

When you write `new int[3][]`, the rows are **`null`** until you create them:

```java
int[][] jag = new int[3][];
System.out.println(jag[0]);        // null
jag[0][0] = 5;                     // NullPointerException (row 0 doesn't exist yet!)
```

Always create each row (`jag[i] = new int[size];`) before using it.

### 15.7 The "shared row" bug

```java
int[] row = new int[3];
int[][] grid = new int[3][];
for (int i = 0; i < 3; i++) {
    grid[i] = row;               // BUG: every row is the SAME array
}
grid[0][0] = 9;
System.out.println(Arrays.deepToString(grid));    // [[9, 0, 0], [9, 0, 0], [9, 0, 0]]
```

The fix: create a **new** array for each row (`grid[i] = new int[3];`).

### 15.8 Copying a 2D array: shallow vs deep

```java
int[][] original = {{1, 2}, {3, 4}};

int[][] shallow = original.clone();     // copies the outer array only; rows are SHARED
shallow[0][0] = 99;
System.out.println(original[0][0]);     // 99  ← original changed too!

// Deep copy: copy each row too
int[][] deep = new int[original.length][];
for (int i = 0; i < original.length; i++) {
    deep[i] = original[i].clone();      // or Arrays.copyOf(original[i], original[i].length)
}
deep[0][0] = 5;
System.out.println(original[0][0]);     // 99  ← unaffected by the deep copy's change
```

```
 SHALLOW (clone)                          DEEP (copy each row too)

 original ─► [ ●  ● ]                     original ─► [ ●  ● ]
               │  │                                     │  │
               ▼  ▼                                     ▼  ▼
             [1,2] [3,4]  ◄─ the same rows            [1,2] [3,4]   (original rows)
               ▲  ▲        are shared!
               │  │                       deep ──────► [ ●  ● ]
 shallow ──► [ ●  ● ]                                    │  │
 (new outer array,                                       ▼  ▼
  but SAME rows)                                       [1,2] [3,4]   (brand-new rows)
```

---

# PART 3: THREE-DIMENSIONAL ARRAYS AND BEYOND

---

## 16. 3D Arrays: The Idea and the Memory Picture

### 16.1 The idea: a stack of tables

A **3D array** is a **stack of 2D tables**, like the **pages of a book**, where each page is a table of rows and columns. To find one value you need **three indexes**:

```
 [page][row][column]
```

```java
int[][][] cube = new int[2][3][4];     // 2 pages, each page has 3 rows and 4 columns
```

```
    page 0                                       page 1
  ┌───────┬───────┬───────┬───────┐       ┌───────┬───────┬───────┬───────┐
  │ 0,0,0 │ 0,0,1 │ 0,0,2 │ 0,0,3 │       │ 1,0,0 │ 1,0,1 │ 1,0,2 │ 1,0,3 │   ← row 0
  ├───────┼───────┼───────┼───────┤       ├───────┼───────┼───────┼───────┤
  │ 0,1,0 │ 0,1,1 │ 0,1,2 │ 0,1,3 │       │ 1,1,0 │ 1,1,1 │ 1,1,2 │ 1,1,3 │   ← row 1
  ├───────┼───────┼───────┼───────┤       ├───────┼───────┼───────┼───────┤
  │ 0,2,0 │ 0,2,1 │ 0,2,2 │ 0,2,3 │       │ 1,2,0 │ 1,2,1 │ 1,2,2 │ 1,2,3 │   ← row 2
  └───────┴───────┴───────┴───────┘       └───────┴───────┴───────┴───────┘
    cube[0][r][c]                    cube[1][r][c]
```

Another way to picture it, as a real **cube / stack of sheets**:

```
   cube = a stack of 2 pages (like sheets of paper)

   ┌─────────────────────┐
   │ cube[1]   page 1    │   3 rows × 4 columns
   └─────────────────────┘
   ┌─────────────────────┐
   │ cube[0]   page 0    │   3 rows × 4 columns
   └─────────────────────┘
   depth = page number (cube.length), then row, then column
```

### 16.2 The truth: an array of arrays of arrays

Again, Java stores it as nested arrays:

```
                          cube  (outer array of 2 references: the pages)
 STACK                    ┌─────────┐
 ┌────────┐               │ cube[0] ●────► page 0 (array of 3 row references)
 │ cube ●─┼──────────────►├─────────┤        ┌──────────┐
 └────────┘               │ cube[1] ●──┐     │ cube[0][0]●──► [ 0 | 0 | 0 | 0 ]
                          └─────────┘  │     ├──────────┤
                                       │     │ cube[0][1]●──► [ 0 | 0 | 0 | 0 ]
                                       │     ├──────────┤
                                       │     │ cube[0][2]●──► [ 0 | 0 | 0 | 0 ]
                                       │     └──────────┘
                                       │
                                       └────► page 1 (array of 3 row references)
                                                ┌──────────┐
                                                │ cube[1][0]●──► [ 0 | 0 | 0 | 0 ]
                                                ├──────────┤
                                                │ cube[1][1]●──► [ 0 | 0 | 0 | 0 ]
                                                ├──────────┤
                                                │ cube[1][2]●──► [ 0 | 0 | 0 | 0 ]
                                                └──────────┘
```

Number of objects created by `new int[2][3][4]`:

```
 1 outer array  +  2 page arrays  +  (2 × 3) row arrays  =  1 + 2 + 6  =  9 array objects
```

### 16.3 What each `.length` means

```java
int[][][] cube = new int[2][3][4];

cube.length            // 2  → number of pages
cube[0].length         // 3  → rows in page 0
cube[0][0].length      // 4  → columns in row 0 of page 0
```

---

## 17. Declaring, Creating and Initializing 3D Arrays

### 17.1 Declaration

```java
int[][][] cube;          // recommended
int cube[][][];          // legal (C-style)
```

### 17.2 Creation with sizes

```java
int[][][] cube = new int[2][3][4];        // 2 pages × 3 rows × 4 columns = 24 ints (all 0)
```

Total number of elements = **2 × 3 × 4 = 24**.

### 17.3 Creation with values (nested braces)

Three levels of braces: **outer = pages, middle = rows, inner = columns**.

```java
int[][][] data = {
    {                       // page 0
        {1, 2},             //   row 0
        {3, 4}              //   row 1
    },
    {                       // page 1
        {5, 6},             //   row 0
        {7, 8}              //   row 1
    }
};
```

```
   page 0          page 1
  ┌───┬───┐       ┌───┬───┐
  │ 1 │ 2 │       │ 5 │ 6 │
  ├───┼───┤       ├───┼───┤
  │ 3 │ 4 │       │ 7 │ 8 │
  └───┴───┘       └───┴───┘
```

Accessing:

```java
System.out.println(data[0][0][0]);   // 1
System.out.println(data[0][1][1]);   // 4
System.out.println(data[1][0][1]);   // 6   (page 1, row 0, col 1)
System.out.println(data[1][1][0]);   // 7
data[1][1][1] = 80;                  // change the last cell
```

### 17.4 Filling a 3D array with loops

```java
int[][][] cube = new int[2][3][4];
int counter = 1;

for (int p = 0; p < cube.length; p++) {
    for (int r = 0; r < cube[p].length; r++) {
        for (int c = 0; c < cube[p][r].length; c++) {
            cube[p][r][c] = counter++;
        }
    }
}
```

Result:

```
   page 0                        page 1
  ┌────┬────┬────┬────┐         ┌────┬────┬────┬────┐
  │  1 │  2 │  3 │  4 │         │ 13 │ 14 │ 15 │ 16 │
  ├────┼────┼────┼────┤         ├────┼────┼────┼────┤
  │  5 │  6 │  7 │  8 │         │ 17 │ 18 │ 19 │ 20 │
  ├────┼────┼────┼────┤         ├────┼────┼────┼────┤
  │  9 │ 10 │ 11 │ 12 │         │ 21 │ 22 │ 23 │ 24 │
  └────┴────┴────┴────┘         └────┴────┴────┴────┘
```

```java
System.out.println(cube[0][1][2]);   // 7
System.out.println(cube[1][2][3]);   // 24
```

### 17.5 Partially specified and jagged 3D arrays

```java
int[][][] a = new int[2][][];        // only pages created; rows are null
a[0] = new int[2][];                 // page 0 has 2 rows
a[0][0] = new int[3];                // row 0 of page 0 has 3 columns
a[0][1] = new int[5];                // row 1 of page 0 has 5 columns
a[1] = new int[1][2];                // page 1 has 1 row of 2 columns
```

Everything that applied to 2D (null rows, jagged lengths) applies here too, one level deeper.

### 17.6 Printing

```java
System.out.println(Arrays.deepToString(data));
// [[[1, 2], [3, 4]], [[5, 6], [7, 8]]]
```

---

## 18. Looping Through 3D Arrays

You need **three nested loops**: **pages → rows → columns**.

```java
for (int p = 0; p < cube.length; p++) {                    // 1) outer: which page
    System.out.println("Page " + p);
    for (int r = 0; r < cube[p].length; r++) {             // 2) middle: which row
        for (int c = 0; c < cube[p][r].length; c++) {      // 3) inner: which column
            System.out.printf("%4d", cube[p][r][c]);
        }
        System.out.println();
    }
    System.out.println();
}
```

Output (using the 2×3×4 array filled with 1..24):

```
Page 0
   1   2   3   4
   5   6   7   8
   9  10  11  12

Page 1
  13  14  15  16
  17  18  19  20
  21  22  23  24
```

### 18.1 The loop order (what spins fastest)

```
 p = 0
    r = 0:  c = 0, 1, 2, 3       ← innermost spins fastest
    r = 1:  c = 0, 1, 2, 3
    r = 2:  c = 0, 1, 2, 3
 p = 1
    r = 0:  c = 0, 1, 2, 3
    r = 1:  c = 0, 1, 2, 3
    r = 2:  c = 0, 1, 2, 3
```

```
 Visiting order (page 0, then page 1):

   page 0                         page 1
  ┌────┬────┬────┬────┐         ┌────┬────┬────┬────┐
  │ 1─►│ 2─►│ 3─►│ 4  │         │13─►│14─►│15─►│16  │
  ├────┼────┼────┼────┤         ├────┼────┼────┼────┤
  │ 5─►│ 6─►│ 7─►│ 8  │         │17─►│18─►│19─►│20  │
  ├────┼────┼────┼────┤         ├────┼────┼────┼────┤
  │ 9─►│10─►│11─►│12  │ ──────► │21─►│22─►│23─►│24  │
  └────┴────┴────┴────┘         └────┴────┴────┴────┘
   all of page 0 first,          then all of page 1
```

Think of the three loops as the **hour, minute and second hands** of a clock: the page changes slowest, the column changes fastest.

### 18.2 For-each version

```java
for (int[][] page : cube) {          // each page is an int[][]
    for (int[] row : page) {         // each row is an int[]
        for (int value : row) {      // each value is an int
            System.out.print(value + " ");
        }
        System.out.println();
    }
    System.out.println();
}
```

The pattern: **the type gets one `[]` shorter at each level** (`int[][]` → `int[]` → `int`).

### 18.3 Aggregating over a 3D array

**Sum of everything:**
```java
int total = 0;
for (int[][] page : cube)
    for (int[] row : page)
        for (int v : row)
            total += v;
// 1 + 2 + ... + 24 = 300
```

**Sum of each page:**
```java
for (int p = 0; p < cube.length; p++) {
    int pageSum = 0;
    for (int[] row : cube[p])
        for (int v : row)
            pageSum += v;
    System.out.println("Page " + p + " sum = " + pageSum);
}
// Page 0 sum = 78,  Page 1 sum = 222
```

(Check: 1+…+12 = 78 and 13+…+24 = 222; 78 + 222 = 300 ✓.)

### 18.4 A real example: marks of students

Three subjects, two semesters, four students → `marks[semester][student][subject]`:

```java
int[][][] marks = new int[2][4][3];     // 2 semesters, 4 students, 3 subjects

marks[0][2][1] = 88;    // semester 0, student 2, subject 1 scored 88

// Average of student 2 in semester 0
int sum = 0;
for (int s = 0; s < 3; s++) sum += marks[0][2][s];
double avg = sum / 3.0;
```

### 18.5 A real example: an RGB image

A color image is a **3D array**: `image[height][width][3]`, where the last index is the color channel (0 = red, 1 = green, 2 = blue).

```java
int height = 2, width = 3;
int[][][] image = new int[height][width][3];

image[0][1][0] = 255;   // pixel at row 0, column 1: red = 255
image[0][1][1] = 0;     // green = 0
image[0][1][2] = 0;     // blue  = 0   → a pure red pixel
```

### 18.6 A real example: cinema seats

`boolean[hall][row][seat]` says which seats are booked:

```java
boolean[][][] booked = new boolean[3][10][15];    // 3 halls, 10 rows, 15 seats per row
booked[1][4][7] = true;                           // hall 1, row 4, seat 7 is booked

int count = 0;
for (boolean[][] hall : booked)
    for (boolean[] row : hall)
        for (boolean seat : row)
            if (seat) count++;
```

---

## 19. 4D and Higher: The Pattern

Java lets you keep adding `[]`:

```java
int[][][][] a = new int[2][3][4][5];     // 4D: 2 × 3 × 4 × 5 = 120 elements
a[1][2][3][4] = 7;
```

- A **4D** array = an array of 3D arrays. A **5D** = an array of 4D arrays, and so on.
- The JVM allows up to **255 dimensions**.
- Each extra dimension needs **one more nested loop**.

### The pattern, side by side

| Dimension | Declaration | Meaning of the indexes | Nested loops | `length` of... |
|-----------|-------------|-------------------------|--------------|----------------|
| 1D | `int[] a = new int[5]` | `[i]` | 1 | `a.length` |
| 2D | `int[][] a = new int[3][4]` | `[row][col]` | 2 | `a.length`, `a[0].length` |
| 3D | `int[][][] a = new int[2][3][4]` | `[page][row][col]` | 3 | `a.length`, `a[0].length`, `a[0][0].length` |
| 4D | `int[][][][] a = new int[2][3][4][5]` | `[block][page][row][col]` | 4 | one more `[0]` each level |

### Mental shortcut

> **An N-dimensional array is just a 1D array whose elements are (N−1)-dimensional arrays.**

So to understand any dimension, peel off one `[]` at a time:

```
 int[][][] cube      is an array of   int[][]   (pages)
 int[][]   page      is an array of   int[]     (rows)
 int[]     row       is an array of   int       (numbers)
```

In practice, **1D, 2D and 3D cover almost everything**. Beyond 3D, programmers usually switch to objects, or to "flat" arrays (see section 22).

---

# PART 4: EXTRA TOPICS

---

## 20. Arrays of Strings, Objects and chars

### 20.1 `String[]`

```java
String[] days = {"Mon", "Tue", "Wed"};
for (String d : days) System.out.println(d);

String[] empty = new String[3];          // [null, null, null]
empty[0].length();                       // NullPointerException (element 0 is null!)
```

### 20.2 Arrays of your own objects

```java
class Student {
    String name;
    Student(String name) { this.name = name; }
}

Student[] students = new Student[3];     // 3 empty slots (all null): NO students exist yet!
students[0] = new Student("Ali");        // now slot 0 holds a real object
students[1] = new Student("Sara");
```

```
 students ──►  ┌──────┬──────┬──────┐
               │  ●   │  ●   │ null │
               └──┬───┴──┬───┴──────┘
                  ▼      ▼
              ┌──────┐ ┌──────┐
              │ Ali  │ │ Sara │
              └──────┘ └──────┘
```

`new Student[3]` creates the **array**, **not** the 3 students. You must create each object yourself.

### 20.3 `char[]` is special

```java
char[] letters = {'J', 'a', 'v', 'a'};
System.out.println(letters);                    // Java   (println has a special overload for char[])
System.out.println("Word: " + letters);         // Word: [C@15db9742  (string + array prints the address!)
System.out.println(String.valueOf(letters));    // Java
String s = new String(letters);                 // "Java"
char[] back = "Java".toCharArray();             // String → char[]
```

### 20.4 Array covariance and `ArrayStoreException`

Because `String` is an `Object`, Java lets you treat a `String[]` as an `Object[]`. But the array **remembers its real type** and checks at runtime:

```java
Object[] objs = new String[2];
objs[0] = "hello";         // OK
objs[1] = 42;              // compiles, but throws ArrayStoreException at runtime
```

### 20.5 Arrays of primitives vs wrappers

| | `int[]` | `Integer[]` |
|---|---------|-------------|
| Stores | the numbers directly | references to `Integer` objects |
| Default | `0` | `null` |
| Memory | small, compact | much bigger |
| Speed | faster | slower |
| Can sort with a custom comparator | No | Yes |

Prefer `int[]` unless you specifically need objects.

---

## 21. Where Do We Use Arrays Exactly?

### 21.1 By dimension

| Dimension | Think of it as | Real-world uses |
|-----------|----------------|-----------------|
| **1D** `int[]` | A **list** or a **row** of values | Marks of students, daily temperatures, prices, sensor readings, scores, counting/frequency tables (`int[26]` for letters), sorting and searching algorithms, `byte[]` buffers for reading files and network data, `String[] args` for command-line arguments, lookup tables, manual stacks/queues/heaps |
| **2D** `int[][]` | A **table**, **grid** or **matrix** | Game boards (tic-tac-toe, chess, Sudoku, Minesweeper, Battleship, Conway's Game of Life), grayscale images (pixel grid), spreadsheets and timetables, seating charts, maps and mazes for pathfinding, math matrices (graphics, physics, machine learning), dynamic-programming tables (LCS, knapsack), graph adjacency matrices, distance tables between cities |
| **3D** `int[][][]` | A **stack of tables**, a **cube** | Color images (`[height][width][RGB]`), video (`[frame][y][x]`), 3D worlds and voxel games (Minecraft-style chunks), medical scans (CT/MRI volumes), weather data (latitude × longitude × altitude), business data cubes (product × region × month), 3D tic-tac-toe, hotels (building × floor × room), neural-network tensors |
| **Jagged** | Rows of **different lengths** | Pascal's triangle, graph adjacency lists, each student with a different number of tests, text split into lines of different length, triangular matrices |

### 21.2 Arrays are hiding inside Java itself

You use arrays even when you don't write them:

- **`ArrayList`** stores its elements in an `Object[]` inside, and replaces it with a bigger array when it's full.
- **`HashMap`** keeps its buckets in an array.
- **`String`** keeps its characters in a byte array internally.
- **`StringBuilder`** is a resizable char/byte array.
- Method **varargs** (`String... names`) are arrays.
- **`main(String[] args)`**

Understanding arrays means understanding the foundation of most data structures.

### 21.3 When arrays are the right choice

Use an array when:

- You **know the size** in advance (or it rarely changes).
- You need **fast access by index** (O(1) time).
- You store **lots of primitives** (memory-efficient, no boxing).
- You are doing **numeric or grid work** (matrices, images, boards).
- You want **maximum performance**.

### 21.4 Choosing the dimension

```
 Is my data a simple list?                          → 1D   int[]
 Does it have rows AND columns (a table, a grid)?   → 2D   int[][]
 Is it a stack of tables / has three coordinates?   → 3D   int[][][]
 Do rows have different sizes?                      → jagged 2D
```

Rule of thumb: **the number of indexes you need to name one value = the number of dimensions.**

---

## 22. Performance Tips: Loop Order and Flattening

### 22.1 Loop in row-major order (rows outside, columns inside)

Each row is stored as a **contiguous block of memory**. When you walk **along a row**, the computer's CPU cache loads neighboring values together, so it is **fast**. Walking **down a column** jumps between different row arrays, so it is **slower** on big arrays.

```java
// FAST: walks along each row
for (int i = 0; i < rows; i++)
    for (int j = 0; j < cols; j++)
        sum += grid[i][j];

// SLOWER on large arrays: jumps from row to row on every step
for (int j = 0; j < cols; j++)
    for (int i = 0; i < rows; i++)
        sum += grid[i][j];
```

```
 FAST (row-major):   [row0: ► ► ► ► ]  [row1: ► ► ► ► ]  [row2: ► ► ► ► ]
 SLOW (col-major):   row0↓ → row1↓ → row2↓ , then back to row0, and so on (hopping around memory)
```

For small arrays you will never notice. For images or matrices with millions of cells, it can be several times faster.

### 22.2 Flattening a 2D array into a 1D array

A 2D array has extra objects (one per row) and extra pointer hops. High-performance code often stores a grid in **one 1D array** and calculates the index by hand:

```
 index = row * columns + column
```

```java
int rows = 3, cols = 4;
int[] flat = new int[rows * cols];

// write cell (row 1, col 2)
flat[1 * cols + 2] = 7;

// read cell (row 1, col 2)
int v = flat[1 * cols + 2];
```

```
 2D view:                       Flat 1D array (index = row*4 + col):
   c0 c1 c2 c3
 r0 0  1  2  3                  [0][1][2][3][4][5][6][7][8][9][10][11]
 r1 4  5  6  7                   └ row 0 ┘└ row 1 ┘└── row 2 ───┘
 r2 8  9 10 11
```

Going back from an index: `row = index / cols`, `col = index % cols`.

---

## 23. Limitations of Arrays

| Limitation | Details | Workaround |
|------------|---------|------------|
| **Fixed size** | Can't grow or shrink | Create a bigger array and copy, or use `ArrayList` |
| **Same type only** | `int[]` holds only ints | Use `Object[]` or a class |
| **No insert/delete in the middle** | You must shift elements yourself | Manual shifting or use collections |
| **No built-in methods** | Only `length` and `clone()` | Use `java.util.Arrays` |
| **`==` and `equals()` compare references** | Not the contents | `Arrays.equals` / `Arrays.deepEquals` |
| **Printing shows an address** | `println(arr)` is useless | `Arrays.toString` / `Arrays.deepToString` |
| **Max length** | About 2.1 billion (a bit less in practice) and limited by memory | Split into chunks or use files/databases |
| **Can't create generic arrays** | `new List<Integer>[5]` is not allowed | Use `ArrayList<List<Integer>>` |

---

## 24. Common Mistakes and Exceptions

### 24.1 Exceptions you will meet

| Exception | When it happens | Example |
|-----------|-----------------|---------|
| `ArrayIndexOutOfBoundsException` | Index < 0 or >= length | `arr[arr.length]` |
| `NegativeArraySizeException` | Creating an array with a negative size | `new int[-3]` |
| `NullPointerException` | Using an array (or a row/element) that is `null` | `int[] a = null; a[0]` |
| `ArrayStoreException` | Storing the wrong type into an array of a different real type | `Object[] o = new String[1]; o[0] = 1;` |
| `OutOfMemoryError` | Array too big for the heap | `new long[Integer.MAX_VALUE]` |

### 24.2 Top mistakes

| # | Mistake | Fix |
|---|---------|-----|
| 1 | Loop `i <= arr.length` (off by one) | Use `i < arr.length` |
| 2 | `arr.length()` | It's `arr.length` (a field) |
| 3 | `int[] arr = new int[];` | Give a size: `new int[5]` |
| 4 | `arr = {1, 2, 3};` after declaration | `arr = new int[]{1, 2, 3};` |
| 5 | `int[] b = a;` thinking it copies | Use `a.clone()` or `Arrays.copyOf` |
| 6 | `System.out.println(arr)` | `Arrays.toString(arr)`; for 2D use `Arrays.deepToString` |
| 7 | `arr1 == arr2` or `arr1.equals(arr2)` to compare contents | `Arrays.equals` / `Arrays.deepEquals` |
| 8 | Changing a primitive in for-each and expecting the array to change | Use an indexed loop |
| 9 | Using `arr[0].length` for a jagged array's inner loop | Use `arr[i].length` |
| 10 | `new int[3][]` then using a row before creating it | `arr[i] = new int[size];` first |
| 11 | `grid[i] = sameRow` for every row | Create a new row each time |
| 12 | Swapping `[row][col]` as `[col][row]` | Decide on `[y][x]` / `[row][col]` once and stay consistent |
| 13 | `String[] a = new String[3];` then `a[0].length()` | The slots are `null`; assign strings first |
| 14 | `Student[] s = new Student[3];` thinking students exist | Create each `new Student(...)` |
| 15 | Using a `long` as an index | Cast: `arr[(int) i]` |
| 16 | `int a[], b;` thinking both are arrays | `b` is a plain `int`; use `int[] a, b;` |

---

## 25. Cheat Sheet, Summary and Quiz

### 25.1 Syntax cheat sheet

```java
// ---------- 1D ----------
int[] a;                                  // declare
a = new int[5];                           // create (all 0)
int[] b = new int[5];                     // declare + create
int[] c = {1, 2, 3};                      // declare + values
int[] d = new int[]{1, 2, 3};             // create + values (works anywhere)

// ---------- 2D ----------
int[][] g1 = new int[3][4];               // 3 rows × 4 cols
int[][] g2 = {{1, 2}, {3, 4}};            // with values
int[][] g3 = new int[3][];                // jagged (rows null for now)
g3[0] = new int[2];                       // create a row

// ---------- 3D ----------
int[][][] c1 = new int[2][3][4];          // 2 pages × 3 rows × 4 cols
int[][][] c2 = {{{1, 2}, {3, 4}}, {{5, 6}, {7, 8}}};

// ---------- access ----------
a[0]              g1[1][2]              c1[1][2][3]

// ---------- lengths ----------
a.length          g1.length (rows)      c1.length (pages)
                  g1[0].length (cols)   c1[0].length (rows)
                                        c1[0][0].length (cols)

// ---------- loops ----------
for (int i = 0; i < a.length; i++) { }
for (int i = 0; i < g.length; i++) for (int j = 0; j < g[i].length; j++) { }
for (int p = 0; p < c.length; p++) for (int r = 0; r < c[p].length; r++) for (int k = 0; k < c[p][r].length; k++) { }
for (int[] row : g) for (int v : row) { }

// ---------- helpers ----------
Arrays.toString(a)   Arrays.deepToString(g)   Arrays.sort(a)   Arrays.fill(a, v)
Arrays.copyOf(a, n)  a.clone()                Arrays.equals(a, b)   Arrays.deepEquals(g1, g2)
```

### 25.2 Summary in simple points

1. An **array** stores a **fixed number** of values of the **same type**, accessed by a **zero-based index**.
2. Arrays are **objects on the heap**; the variable holds a **reference**. `int[] b = a;` makes **two names for one array**.
3. **Declare** (`int[] a;`), **create** (`new int[5]`) and **initialize** (`{1,2,3}`) are three separate steps that can be combined.
4. New arrays are filled with **defaults**: `0`, `false`, `null`, etc.
5. Use **`arr.length`** (no brackets). Valid indexes are **`0` to `length - 1`**; anything else throws `ArrayIndexOutOfBoundsException`.
6. Loop with the **indexed `for`** (need index or modification) or **for-each** (just reading).
7. Copy with **`clone()`**, **`Arrays.copyOf`** or **`System.arraycopy`**, never with `=`. Compare with **`Arrays.equals`**. Print with **`Arrays.toString`**.
8. A **2D array is an array of arrays** (rows). `grid.length` = rows, `grid[0].length` = columns. Loop with **two nested loops**: rows outside, columns inside.
9. Rows can have **different lengths** (jagged arrays). Always loop with **`grid[i].length`**, and create each row before use.
10. A **3D array is a stack of 2D tables**: `[page][row][col]`, looped with **three nested loops**. Higher dimensions follow the same pattern (up to 255).
11. For **2D and 3D**, use **`Arrays.deepToString`** and **`Arrays.deepEquals`**, and remember `clone()` is **shallow**.
12. Use arrays for **lists, grids, matrices, images, game boards, buffers** and fast numeric work. Iterate in **row-major** order for speed.

### 25.3 Mini Quiz

1. What does `new int[3][4]` create, and how many array objects are made?
2. What is `arr.length` for `int[][] arr = new int[5][8];`? What is `arr[0].length`?
3. Is `int[] a = new int[];` valid? What about `int[] a = {1, 2, 3};`?
4. After `int[][] x = new int[3][0];`, what does `x.length` give, and what is `x[0].length`?
5. What are the default values of `new boolean[2]` and `new String[2]`?
6. After `int[] a = {1, 2, 3}; int[] b = a; b[0] = 99;`, what is `a[0]`?
7. How many nested loops do you need to visit every element of a 3D array?
8. Which method prints `[[1, 2], [3, 4]]` for a 2D array?
9. Why does `grid[1][5] = 1;` crash on `new int[3][4]`?
10. After `int[][] g = new int[3][];`, what is `g[0]`, and what happens if you write `g[0][0] = 1;`?
11. How many array objects does `new int[2][3][4]` create?
12. What is the index in a flat 1D array for row 2, column 3 of a grid with 5 columns?

<details>
<summary>Click for answers</summary>

1. A 2D array with **3 rows and 4 columns** (12 ints, all `0`). It creates **4 array objects**: 1 outer array + 3 row arrays.
2. `arr.length` = **5** (rows); `arr[0].length` = **8** (columns).
3. `new int[]` **without a size is invalid** (array dimension missing). `int[] a = {1, 2, 3};` **is valid**.
4. `x.length` is **3** (three rows) and `x[0].length` is **0** (each row is an empty array).
5. `new boolean[2]` → `[false, false]`; `new String[2]` → `[null, null]`.
6. **99**, because `b` and `a` refer to the **same array**.
7. **Three** (pages, rows, columns).
8. **`Arrays.deepToString(array)`**.
9. Valid column indexes are `0` to `3` (the length is 4), so index `5` throws **`ArrayIndexOutOfBoundsException`**.
10. `g[0]` is **`null`**. `g[0][0] = 1;` throws **`NullPointerException`**, because row 0 hasn't been created yet.
11. **9** array objects: 1 + 2 + (2 × 3) = 9.
12. `row * columns + column` = `2 * 5 + 3` = **13**.

</details>

---

*Practice tip: draw the grid on paper, write the `i` and `j` values beside it, and "run" the loops by hand before you run them on the computer. After a few examples, nested loops will feel natural.*
