# Java Strings, Part 2: Constructors, Methods, StringBuilder and StringBuffer

> Continuation of the Strings internals notes (String Pool, immutability, `byte[]` + `coder`, hash caching). This part covers: how Strings are created with constructors, the most important String methods, and the mutable classes `StringBuilder` and `StringBuffer`, including capacity, threading and `equals()`.

---

## Table of Contents

1. [Quick recap from Part 1](#1-quick-recap-from-part-1)
2. [String constructors and initialization](#2-string-constructors-and-initialization)
3. [Key String methods](#3-key-string-methods)
   - 3.1 [Inquiry: `length`, `isEmpty`, `isBlank`, `charAt`](#31-inquiry-methods)
   - 3.2 [Cleaning: `trim` vs `strip`](#32-cleaning-trim-vs-strip)
   - 3.3 [`repeat`](#33-repeat)
   - 3.4 [Modification: `replace` vs `replaceAll`](#34-modification-replace-vs-replaceall)
   - 3.5 [Parsing: `split`](#35-parsing-split)
   - 3.6 [Combining: `String.join`](#36-combining-stringjoin)
   - 3.7 [Formatting: `String.format`](#37-formatting-stringformat)
   - 3.8 [`intern()` revisited](#38-intern-revisited)
   - 3.9 [`getBytes()` and encoding](#39-getbytes-and-encoding)
4. [Why we need mutable string classes](#4-why-we-need-mutable-string-classes)
5. [StringBuilder and StringBuffer: internal architecture](#5-stringbuilder-and-stringbuffer-internal-architecture)
6. [Capacity management](#6-capacity-management)
7. [Core methods: `append`, `insert`, `delete`, `replace`, `reverse`](#7-core-methods)
8. [StringBuffer vs StringBuilder (threading)](#8-stringbuffer-vs-stringbuilder-threading)
9. [The `equals()` trap](#9-the-equals-trap)
10. [Performance comparison](#10-performance-comparison)
11. [Choosing the right class](#11-choosing-the-right-class)
12. [Predict the output (practice)](#12-predict-the-output-practice)
13. [Interview questions and answers](#13-interview-questions-and-answers)
14. [Cheat sheet](#14-cheat-sheet)

---

## 1. Quick recap from Part 1

```
String s = "hello";            -> reference on STACK, object in STRING POOL
String t = new String("hello");-> reference on STACK, object in normal HEAP

String object  = { byte[] value, byte coder, int hash }
coder 0 = LATIN1 (1 byte/char)     coder 1 = UTF16 (2 bytes/char)

Strings are IMMUTABLE: every "change" creates a NEW String object.
```

This part builds on that: first more ways to *create* strings, then useful methods, then the classes that let you *modify* text in place.

---

## 2. String constructors and initialization

### 2.1 The overview

```
                    HOW TO CREATE A STRING
                             |
      +----------+-----------+------------+-----------------+
      v          v           v            v                 v
  Literal      new         char[]       byte[]        StringBuilder /
 "hello"   String("..")   array         array          StringBuffer
    |          |            |             |                 |
   POOL       HEAP         HEAP          HEAP              HEAP
```

### 2.2 Literal (String Pool)

```java
String s1 = "hello";
String s2 = "hello";     // reuses the pooled object
System.out.println(s1 == s2);   // true
```

### 2.3 `new` operator (Heap)

```java
String s3 = new String("hello");   // always a new heap object
System.out.println(s1 == s3);      // false
```

### 2.4 Empty string

```java
String e1 = new String();   // empty string "" in heap, length 0
String e2 = "";             // empty string literal in pool
```

### 2.5 From a `char[]`

```java
char[] chars = {'J', 'a', 'v', 'a'};

String a = new String(chars);            // "Java"
String b = new String(chars, 1, 2);      // offset 1, count 2 -> "av"
```

```
char[] chars:   [ J ][ a ][ v ][ a ]
                  0    1    2    3

new String(chars)        -> copies ALL    -> "Java"
new String(chars, 1, 2)  -> from index 1, take 2 chars -> "av"
```

Important: the String **copies** the array contents. Changing `chars` later does **not** change the String (this protects immutability).

```java
char[] chars = {'H', 'i'};
String s = new String(chars);
chars[0] = 'X';
System.out.println(s);       // Hi  (unchanged, because of the copy)
```

```
Before:   chars -> [H][i]          s -> String -> byte[] {H, i}   (own copy)
After:    chars -> [X][i]          s -> String -> byte[] {H, i}   (still safe)
```

### 2.6 From a `byte[]`

```java
byte[] bytes = {72, 101, 108, 108, 111};   // ASCII codes

String s = new String(bytes);                       // "Hello" (default charset)
String t = new String(bytes, 1, 3);                 // "ell"
String u = new String(bytes, StandardCharsets.UTF_8); // explicit charset (best practice)
```

```
byte[]:  72   101   108   108   111
          H     e     l     l     o     <- decoded using a charset
```

The bytes are **decoded** into characters using a charset (UTF-8, ISO-8859-1, ...). Always pass the charset explicitly to avoid surprises on different machines.

### 2.7 From `StringBuilder` / `StringBuffer`

```java
StringBuilder sb = new StringBuilder("Hello");
String s1 = new String(sb);        // "Hello"

StringBuffer sbf = new StringBuffer("World");
String s2 = new String(sbf);       // "World"
```

(You usually use `sb.toString()`, which does the same job.)

### 2.8 Other useful ways

```java
String.valueOf(123);            // "123"
String.valueOf(true);           // "true"
String.valueOf(new char[]{'a','b'});   // "ab"
String.copyValueOf(chars);      // from char[]
```

### 2.9 Summary table

| Constructor / method | Source | Where result lives |
|---|---|---|
| `"text"` | literal | **String Pool** |
| `new String("text")` | String | Heap |
| `new String()` | nothing | Heap (empty) |
| `new String(char[])` / `(char[], offset, count)` | char array | Heap |
| `new String(byte[])` / `(byte[], Charset)` | byte array | Heap |
| `new String(StringBuilder)` | builder | Heap |
| `new String(StringBuffer)` | buffer | Heap |

---

## 3. Key String methods

Remember: **all of these return a new String (or a value); none modify the original.**

### 3.1 Inquiry methods

```java
String s = "Hello";

s.length();          // 5
s.isEmpty();         // false  (length() == 0)
"".isEmpty();        // true
"   ".isEmpty();     // false  (has 3 spaces)
"   ".isBlank();     // true   (Java 11: empty OR only whitespace)
s.charAt(1);         // 'e'
s.indexOf('l');      // 2
s.contains("ell");   // true
```

```
isEmpty()  ->  length == 0
isBlank()  ->  length == 0  OR  every char is whitespace

 ""       isEmpty: true    isBlank: true
 "   "    isEmpty: false   isBlank: true
 " a "    isEmpty: false   isBlank: false
```

`length()` internally uses `value.length >> coder` (see Part 1).

### 3.2 Cleaning: `trim` vs `strip`

```java
String s = "   hello   ";

s.trim();           // "hello"
s.strip();          // "hello"
s.stripLeading();   // "hello   "
s.stripTrailing();  // "   hello"
```

```
"   hello   "
 ^^^       ^^^
 trim()/strip() remove both ends

 stripLeading()  removes only the left spaces
 stripTrailing() removes only the right spaces
```

**The difference:**

| | `trim()` | `strip()` (Java 11+) |
|---|---|---|
| Removes | characters with code <= `U+0020` (space, tab, newline, control chars) | characters where `Character.isWhitespace()` is true (**Unicode-aware**) |
| Unicode spaces (e.g. `\u2003` em-space) | **Not removed** | **Removed** |
| Recommended today | legacy | **yes** |

```java
String s = "\u2003hello\u2003";    // em-spaces around
s.trim().length();     // 7  (not removed)
s.strip().length();    // 5  (removed)
```

Neither touches spaces **inside** the text, and since Strings are immutable, you must **assign the result**:

```java
s = s.trim();     // correct
s.trim();         // result thrown away
```

### 3.3 `repeat`

```java
"ab".repeat(3);     // "ababab"
"-".repeat(20);     // "--------------------"
"x".repeat(0);      // ""
```

Handy for separators and padding. (Java 11+; negative count throws `IllegalArgumentException`.)

### 3.4 Modification: `replace` vs `replaceAll`

Strings are immutable, so these return a **new** string.

```java
String s = "banana";

s.replace('a', 'o');          // "bonono"      (char -> char)
s.replace("an", "AN");        // "bANANa"      (text -> text, literal)
s.replaceAll("a", "o");       // "bonono"      (regex)
s.replaceFirst("a", "o");     // "bonana"      (only first match, regex)
```

**The key difference: `replace` is literal text, `replaceAll` uses a regular expression.**

| Method | Pattern is | Replaces |
|---|---|---|
| `replace(char, char)` | a char | all occurrences |
| `replace(CharSequence, CharSequence)` | **literal** text | all occurrences |
| `replaceAll(String regex, String repl)` | **regex** | all matches |
| `replaceFirst(String regex, String repl)` | **regex** | first match only |

Regex power:

```java
"a1b22c333".replaceAll("[0-9]+", "#");     // "a#b#c#"
"hello   world".replaceAll("\\s+", " ");   // "hello world"  (collapse spaces)
"Hello World".replaceAll("[aeiou]", "*");  // "H*ll* W*rld"
```

The classic trap:

```java
"a.b.c".replace(".", "-");       // "a-b-c"     literal dot
"a.b.c".replaceAll(".", "-");    // "-----"     regex: '.' means ANY character!
"a.b.c".replaceAll("\\.", "-");  // "a-b-c"     escaped dot
```

```
replaceAll(".", "-") on "a.b.c"
   '.' (regex) = any char

   a  .  b  .  c
   |  |  |  |  |
   -  -  -  -  -    -> "-----"
```

### 3.5 Parsing: `split`

`split(regex)` breaks a string into a `String[]` around matches of a **regex** delimiter.

```java
String csv = "red,green,blue";
String[] parts = csv.split(",");
// ["red", "green", "blue"]
```

```
"red,green,blue"
   |     |
   v     v
 split(",")
   |
   v
 [ "red" ][ "green" ][ "blue" ]
    [0]       [1]        [2]
```

More examples:

```java
"a b   c".split(" ");        // ["a", "b", "", "", "c"]   (empty strings between spaces)
"a b   c".split("\\s+");     // ["a", "b", "c"]           (one or more spaces)
"one-two-three".split("-", 2);  // ["one", "two-three"]   (limit = max parts)
```

**Special regex characters need escaping:**

```java
"a.b.c".split(".");       // []              ('.' is regex "any char")
"a.b.c".split("\\.");     // ["a", "b", "c"]
"a|b|c".split("|");       // ["a","|","b","|","c"]  ('|' is regex OR, matches empty)
"a|b|c".split("\\|");     // ["a", "b", "c"]
```

**Trailing empty strings are removed** (with the default limit of 0):

```java
"a,b,,".split(",");        // ["a", "b"]            (trailing empties dropped)
"a,b,,".split(",", -1);    // ["a", "b", "", ""]    (negative limit keeps them)
```

| `limit` | Behavior |
|---|---|
| `> 0` | at most `limit` parts; last part holds the rest |
| `0` (default) | all parts; trailing empty strings removed |
| `< 0` | all parts; trailing empty strings kept |

### 3.6 Combining: `String.join`

The opposite of `split`.

```java
String.join("-", "2026", "10", "26");              // "2026-10-26"
String.join(", ", "a", "b", "c");                  // "a, b, c"

List<String> list = List.of("x", "y", "z");
String.join("/", list);                            // "x/y/z"
```

```
split(",")                      join(",")
"a,b,c"  ------->  [a][b][c]  ------->  "a,b,c"
```

It is cleaner (and avoids trailing-delimiter bugs) than manual loops with `+`.

### 3.7 Formatting: `String.format`

Builds structured output using **format specifiers**, without messy concatenation.

```java
String name = "Asha";
int age = 25;
double score = 92.456;

String msg = String.format("Name: %s, Age: %d, Score: %.2f", name, age, score);
// Name: Asha, Age: 25, Score: 92.46
```

Versus concatenation:

```java
"Name: " + name + ", Age: " + age + ", Score: " + score   // harder to read, no control over decimals
```

Common specifiers:

| Specifier | Meaning | Example | Output |
|---|---|---|---|
| `%s` | String (any object) | `format("%s", "hi")` | `hi` |
| `%d` | integer | `format("%d", 42)` | `42` |
| `%f` | floating point (default 6 decimals) | `format("%f", 3.14)` | `3.140000` |
| `%.2f` | 2 decimals | `format("%.2f", 3.14159)` | `3.14` |
| `%c` | char | `format("%c", 'A')` | `A` |
| `%b` | boolean | `format("%b", true)` | `true` |
| `%x` | hex | `format("%x", 255)` | `ff` |
| `%n` | platform newline | | |
| `%%` | literal percent | | `%` |

Width, alignment, padding, grouping:

```java
String.format("|%10s|", "hi");     // |        hi|   (right-aligned, width 10)
String.format("|%-10s|", "hi");    // |hi        |   (left-aligned)
String.format("%05d", 42);         // 00042          (zero-padded)
String.format("%,d", 1234567);     // 1,234,567      (thousands separator)
```

```
%-10s   ->  hi________      (_ = space, left aligned)
%10s    ->  ________hi      (right aligned)
%05d    ->  00042
```

Java 15+ also has `"...".formatted(args)`, an instance-method version.

### 3.8 `intern()` revisited

`intern()` forces a string into the **String Pool** (or returns the pooled copy that already exists).

```java
String a = new String("java");     // heap
String b = a.intern();             // pool reference
String c = "java";                 // pool literal

System.out.println(a == c);        // false
System.out.println(b == c);        // true
```

```
BEFORE intern                       AFTER a.intern()

 a --> HEAP  "java"                  a --> HEAP "java"      (unchanged)
 c --> POOL  "java"                  b --> POOL "java" <-- c  (same pooled object)
```

Use case: many duplicate strings built at runtime (for example, values read from a file or database) can be interned so duplicates share one object and save memory. Don't overuse it; the pool lookup has a cost.

### 3.9 `getBytes()` and encoding

`getBytes()` shows the **underlying byte values** of the characters in a given encoding.

```java
"Hello".getBytes();       // [72, 101, 108, 108, 111]
```

```
 H    e     l     l     o
72   101   108   108   111      (ASCII / UTF-8, 1 byte each)
```

The result depends on the **charset**:

```java
String s = "A";
s.getBytes(StandardCharsets.UTF_8);      // [65]           1 byte
s.getBytes(StandardCharsets.UTF_16);     // [-2, -1, 0, 65] BOM (2 bytes) + 2 bytes

String h = "\u0915";                     // Devanagari 'ka'
h.getBytes(StandardCharsets.UTF_8);      // [-32, -92, -107]   3 bytes
```

```
"A"  in UTF-8   : 41                      (1 byte)
"A"  in UTF-16  : FE FF 00 41             (BOM + 2 bytes)
"ka" in UTF-8   : E0 A4 95                (3 bytes)
```

Negative numbers appear because Java `byte` is **signed** (-128 to 127); the bit pattern is the same.

Notes:
- Don't confuse this with the internal `byte[]` of the `String` object (Part 1). `getBytes()` **encodes into a new array** using the requested charset; the internal array uses Latin-1 or UTF-16 depending on `coder`.
- `getBytes()` with no argument uses the default charset (UTF-8 from Java 18, platform-dependent before). **Always pass a charset explicitly.**
- Round trip: `new String(bytes, charset)` decodes back. Using a different charset to decode gives garbled text.

---

## 4. Why we need mutable string classes

Recall the cost of `String +=` in a loop (Part 1): a new String and a full copy on every iteration, plus garbage.

```java
String s = "";
for (int i = 0; i < 5; i++) s += i;
```

```
iter 1:  "0"        new object
iter 2:  "01"       new object, "0" is garbage
iter 3:  "012"      new object, "01" is garbage
...
```

**`StringBuilder` and `StringBuffer` are *mutable***: they change their content **in place** inside one internal array, so no new object per edit. This saves memory and time.

```
String          : text -> NEW object each change   (immutable)
StringBuilder   : same object, content modified    (mutable)
StringBuffer    : same object, content modified    (mutable, synchronized)
```

Both classes extend `AbstractStringBuilder` and share the same API.

---

## 5. StringBuilder and StringBuffer: internal architecture

Both inherit these fields from `AbstractStringBuilder`:

```java
abstract class AbstractStringBuilder {
    byte[] value;     // internal buffer (NOT final, can be replaced by a bigger one)
    byte   coder;     // 0 = LATIN1, 1 = UTF16 (same compact trick as String)
    int    count;     // how many characters are actually used (= length())
}
```

Compare with `String`:

| | `String` | `StringBuilder` / `StringBuffer` |
|---|---|---|
| `value` | `private final byte[]` | `byte[]` (**not final**, grows) |
| Length tracking | `value.length >> coder` | separate `count` field |
| Extra space | none (exact fit) | **spare capacity** |
| Content can change | no | yes |
| Cached hash | yes | no |

### 5.1 Length vs capacity

- **length** = characters currently stored (`count`).
- **capacity** = size of the internal array (how many chars fit before growing).

```java
StringBuilder sb = new StringBuilder();   // default capacity 16
sb.append("Hello");
sb.length();     // 5
sb.capacity();   // 16
```

```
StringBuilder sb = new StringBuilder();  sb.append("Hello");

 STACK                  HEAP
 +----+              +------------------------------------+
 | sb |------------->| StringBuilder object               |
 +----+              |   coder = 0 (LATIN1)               |
                     |   count = 5     <-- length         |
                     |   value ---+                       |
                     +------------|-----------------------+
                                  v
                     +------------------------------------+
                     | byte[16]   <-- capacity            |
                     | [H][e][l][l][o][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ][ ] |
                     |  0  1  2  3  4  5 ...              15           |
                     |  \_____used_____/ \____spare______/             |
                     +------------------------------------+
```

### 5.2 Constructors and initial capacity

| Constructor | Initial capacity |
|---|---|
| `new StringBuilder()` | **16** |
| `new StringBuilder(int capacity)` | the given value |
| `new StringBuilder(String s)` | `s.length() + 16` |
| `new StringBuilder(CharSequence cs)` | `cs.length() + 16` |

```java
new StringBuilder().capacity();          // 16
new StringBuilder(50).capacity();        // 50
new StringBuilder("Hello").capacity();   // 21  (5 + 16)
```

(Identical for `StringBuffer`.)

---

## 6. Capacity management

### 6.1 Automatic growth

When you append beyond the current capacity, the builder allocates a **bigger array**, copies the old content into it, and drops the old array.

The new capacity is normally **`(oldCapacity * 2) + 2`** (the lecture describes this as "doubling"; precisely it is double plus 2). If that is still too small for what you are appending, it grows to exactly what is needed.

```
capacity 16  ->  34  ->  70  ->  142  -> ...
         (16*2+2)  (34*2+2)  (70*2+2)
```

Visual of one growth step:

```java
StringBuilder sb = new StringBuilder();          // capacity 16
sb.append("0123456789ABCDEF");                   // exactly 16 chars, still fits
sb.append("X");                                  // 17th char -> must grow
```

```
BEFORE (full):
 value -> byte[16] [0][1][2][3][4][5][6][7][8][9][A][B][C][D][E][F]

append("X") needs index 16 -> out of room!

STEP 1: allocate new byte[34]
STEP 2: copy 16 old bytes into it
STEP 3: put 'X' at index 16
STEP 4: value now points to the NEW array; old array becomes garbage

AFTER:
 value -> byte[34] [0]...[F][X][ ][ ]...[ ]      capacity 34, length 17
```

Why growth is geometric: copying is expensive (O(n)), so growing by a large factor means copies happen rarely, and appends average out to **O(1) amortized**.

### 6.2 `ensureCapacity(int minimumCapacity)`

Pre-grows the buffer if the current capacity is smaller than the minimum you need. Useful if you **know** you'll append a lot, avoiding many reallocations.

```java
StringBuilder sb = new StringBuilder();   // 16
sb.ensureCapacity(100);
sb.capacity();                            // at least 100 (max(100, 16*2+2)) -> 100
sb.ensureCapacity(10);                    // already enough, nothing happens
```

Rule: if `minimumCapacity > capacity`, new capacity = `max(minimumCapacity, oldCapacity * 2 + 2)`. Otherwise no change.

### 6.3 `trimToSize()`

Shrinks the internal array down to the current length, releasing spare memory.

```java
StringBuilder sb = new StringBuilder(100);
sb.append("Hi");
sb.capacity();       // 100  (98 slots unused)
sb.trimToSize();
sb.capacity();       // 2    (exact fit)
```

```
BEFORE:  count=2   byte[100]  [H][i][ ][ ][ ] ... (98 wasted)
AFTER :  count=2   byte[2]    [H][i]
```

Use when the builder is finished growing and will live a long time.

### 6.4 `setLength(int newLength)`

```java
StringBuilder sb = new StringBuilder("Hello");
sb.setLength(2);     // "He"      (truncates)
sb.setLength(0);     // ""        (clear, keeps capacity for reuse)
```

Handy to **reuse** a builder (`sb.setLength(0)`) without allocating a new one.

---

## 7. Core methods

All of these **modify the same object** and (except the query methods) **return `this`**, which enables **method chaining**.

```java
StringBuilder sb = new StringBuilder();
sb.append("Hello").append(' ').append("World").append(2026);   // chained
// "Hello World2026"
```

```
sb.append("Hello")  returns sb (same object) ->  .append(' ') returns sb -> ...
```

### 7.1 `append`

Adds to the **end**. Overloaded for every type: `String`, `char`, `int`, `long`, `double`, `boolean`, `char[]`, `Object`, `CharSequence`...

```java
StringBuilder sb = new StringBuilder("Hi");
sb.append(" there");       // "Hi there"
sb.append(1).append(true); // "Hi there1true"
```

```
Before: [H][i]
append(" there")
After : [H][i][ ][t][h][e][r][e]
```

### 7.2 `insert(int offset, X)`

Inserts at a position, shifting everything after it to the right.

```java
StringBuilder sb = new StringBuilder("Hello World");
sb.insert(5, ",");         // "Hello, World"
sb.insert(0, ">> ");       // ">> Hello, World"
```

```
"Hello World"        insert(5, ",")
 H e l l o _ W o r l d
          ^ offset 5
 H e l l o , _ W o r l d      <- tail shifted right by 1
```

Cost: O(n) because of shifting.

### 7.3 `delete(int start, int end)` and `deleteCharAt(int index)`

`start` is inclusive, `end` is **exclusive**.

```java
StringBuilder sb = new StringBuilder("Hello World");
sb.delete(5, 11);          // "Hello"        (removes indexes 5..10)
sb.deleteCharAt(0);        // "ello"
```

```
"Hello World"
 0123456789 10
      delete(5, 11)  -> removes index 5 up to 10
 "Hello"
```

### 7.4 `replace(int start, int end, String str)`

Replaces the range `[start, end)` with the given string (the replacement can be a different length).

```java
StringBuilder sb = new StringBuilder("Hello World");
sb.replace(6, 11, "Java");     // "Hello Java"
```

```
"Hello World"
        ^^^^^ indexes 6..10
replace(6, 11, "Java")
"Hello Java"
```

**Not the same as `String.replace(old, new)`**: the builder version replaces by **index range**, not by matching text.

### 7.5 `reverse()`

```java
new StringBuilder("Hello").reverse();     // "olleH"
```

```
H e l l o   ->   o l l e H
```

Classic use: palindrome check.

```java
static boolean isPalindrome(String s) {
    return s.equals(new StringBuilder(s).reverse().toString());
}
isPalindrome("madam");   // true
```

### 7.6 Other useful methods

```java
StringBuilder sb = new StringBuilder("Hello");
sb.charAt(1);            // 'e'
sb.setCharAt(0, 'J');    // "Jello"  (modify one char in place)
sb.indexOf("l");         // 2
sb.substring(1, 3);      // "el"   (returns a String; builder unchanged)
sb.length();             // 5
sb.toString();           // converts to an immutable String
```

### 7.7 Methods summary

| Method | Effect | Returns |
|---|---|---|
| `append(x)` | add at end | `this` |
| `insert(i, x)` | add at index | `this` |
| `delete(s, e)` | remove `[s, e)` | `this` |
| `deleteCharAt(i)` | remove one char | `this` |
| `replace(s, e, str)` | replace `[s, e)` | `this` |
| `reverse()` | reverse in place | `this` |
| `setCharAt(i, c)` | change one char | `void` |
| `setLength(n)` | truncate/pad | `void` |
| `ensureCapacity(n)` | pre-grow | `void` |
| `trimToSize()` | shrink to fit | `void` |
| `length()` / `capacity()` | sizes | `int` |
| `toString()` | make a String | `String` |

### 7.8 End-to-end walkthrough

```java
StringBuilder sb = new StringBuilder("Java");   // capacity 20, length 4
sb.append(" is fun");                            // "Java is fun"
sb.insert(5, "really ");                         // "Java really is fun"
sb.replace(0, 4, "Kotlin");                      // "Kotlin really is fun"
sb.delete(7, 14);                                // "Kotlin is fun"
sb.reverse();                                    // "nuf si niltoK"
```

```
"Java"
  append(" is fun")      -> J a v a _ i s _ f u n
  insert(5,"really ")    -> J a v a _ r e a l l y _ i s _ f u n
  replace(0,4,"Kotlin")  -> K o t l i n _ r e a l l y _ i s _ f u n
  delete(7,14)           -> K o t l i n _ i s _ f u n
  reverse()              -> n u f _ s i _ n i l t o K
ALL on the SAME object, one internal array (grown when needed).
```

---

## 8. StringBuffer vs StringBuilder (threading)

They have **the same methods and internal design**. The only real difference is **synchronization**.

### 8.1 `StringBuffer`: thread-safe

Its methods are `synchronized`, so only one thread can run them on a given object at a time.

```java
// simplified idea
public synchronized StringBuffer append(String str) { ... }
```

```
Thread A ---\
             +--> [ lock on StringBuffer ] --> append() runs one thread at a time
Thread B ---/
```

- Prevents **race conditions** in multi-threaded code.
- Cost: locking overhead, so it is **slower**.

### 8.2 `StringBuilder`: not thread-safe, but faster

No synchronization.

```
Thread A ---\
             +--> StringBuilder  (no lock; concurrent edits may corrupt content)
Thread B ---/
```

- **Faster**, preferred when only **one thread** uses the object (the vast majority of cases, e.g. a local variable inside a method).

### 8.3 What a race condition looks like

```java
StringBuilder sb = new StringBuilder();
// two threads each append "x" 1000 times
// expected length: 2000
// actual with StringBuilder: can be < 2000 or throw ArrayIndexOutOfBoundsException
// with StringBuffer: always 2000
```

Why: `append` is several steps (check capacity, maybe grow, copy chars, update `count`). Two threads can interleave these steps and overwrite each other's work.

```
Thread A: reads count = 5
Thread B: reads count = 5          <-- both think next free slot is index 5
Thread A: writes 'x' at 5, count = 6
Thread B: writes 'x' at 5 (overwrites!), count = 6     -> one 'x' lost
```

### 8.4 A caution about "thread-safe"

`StringBuffer` makes **each method call** atomic, not a **sequence** of calls:

```java
// NOT safe even with StringBuffer: check-then-act is two separate calls
if (sb.length() == 0) {
    sb.append("first");
}
```

For compound actions you still need your own `synchronized` block or another design. In practice, `StringBuffer` is rarely needed; build strings locally with `StringBuilder` and share the finished immutable `String`.

### 8.5 Comparison table

| Feature | `String` | `StringBuilder` | `StringBuffer` |
|---|---|---|---|
| Mutable | No | Yes | Yes |
| Thread-safe | Yes (immutable) | **No** | **Yes** (synchronized) |
| Speed for edits | Slowest | **Fastest** | Slower than builder |
| Since | 1.0 | 1.5 | 1.0 |
| Stored in | Pool or heap | Heap | Heap |
| `equals()` compares | **content** | **reference** | **reference** |
| `hashCode()` | content-based, cached | identity-based | identity-based |
| Typical use | fixed text | single-thread building | multi-thread shared building (rare) |

---

## 9. The `equals()` trap

**Neither `StringBuilder` nor `StringBuffer` overrides `equals()`** (nor `hashCode()`). They inherit `Object.equals()`, which compares **references**.

```java
StringBuilder a = new StringBuilder("hello");
StringBuilder b = new StringBuilder("hello");

System.out.println(a == b);        // false
System.out.println(a.equals(b));   // false  <-- surprise! same content, different objects
```

```
 a ----> [ StringBuilder "hello" ]     different objects
 b ----> [ StringBuilder "hello" ]
 
 a.equals(b)  -> Object.equals -> same reference? NO -> false
```

Contrast with String:

```java
new String("hello").equals(new String("hello"));   // true (String overrides equals)
```

### How to compare content correctly

```java
a.toString().equals(b.toString());     // true  (convert, then compare)
a.compareTo(b) == 0;                   // true  (compareTo exists since Java 11)
"hello".contentEquals(a);              // true  (String.contentEquals(CharSequence))
```

### Mixed comparison pitfalls

```java
String s = "hello";
StringBuilder sb = new StringBuilder("hello");

s.equals(sb);          // false (different types)
sb.equals(s);          // false
s.contentEquals(sb);   // true
```

### Another consequence: collections

Because `hashCode()` is identity-based, builders behave badly as `HashMap` keys or `HashSet` elements: two builders with the same text are treated as **different** keys. Use the final `String` as the key instead.

---

## 10. Performance comparison

```java
// SLOW: O(n^2) copying, many garbage objects
String s = "";
for (int i = 0; i < 100_000; i++) s += "x";

// FAST: amortized O(1) per append
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 100_000; i++) sb.append("x");
String result = sb.toString();
```

```
String +=     : each iteration copies ALL previous chars
                work ~ 1 + 2 + 3 + ... + n  =  n(n+1)/2  -> O(n^2)

StringBuilder : each iteration copies 1 char (plus rare growth copies)
                work ~ n  -> O(n)
```

Tips:
- Pre-size when you know the approximate size: `new StringBuilder(expectedLength)` or `ensureCapacity()` avoids repeated regrowth.
- A single expression like `"a" + b + "c"` is fine; the compiler optimizes it. The problem is concatenating **inside loops**.
- `StringBuilder` beats `StringBuffer` in single-threaded code because there is no lock overhead.

---

## 11. Choosing the right class

```
                 Is the text fixed / rarely changing?
                         /               \
                       YES                NO
                        |                  |
                     String        Many edits (loop, building)?
                                          |
                           Shared between multiple threads?
                                  /                \
                                NO                  YES
                                 |                   |
                          StringBuilder         StringBuffer
                          (default choice)      (or better: avoid
                                                 sharing; synchronize
                                                 explicitly if needed)
```

---

## 12. Predict the output (practice)

```java
// 1
String s = "hello";
s.toUpperCase();
System.out.println(s);                 // hello (result discarded)

// 2
StringBuilder sb = new StringBuilder("hello");
sb.append(" world");
System.out.println(sb);                // hello world (same object modified)

// 3
StringBuilder a = new StringBuilder("x");
StringBuilder b = new StringBuilder("x");
System.out.println(a.equals(b));       // false

// 4
System.out.println("a.b.c".split(".").length);     // 0
System.out.println("a.b.c".split("\\.").length);   // 3

// 5
System.out.println("a,b,,".split(",").length);     // 2

// 6
System.out.println(new StringBuilder("Hello").capacity());   // 21

// 7
StringBuilder sb2 = new StringBuilder("abcdef");
sb2.delete(1, 3);
System.out.println(sb2);               // adef  (removes index 1 and 2)

// 8
StringBuilder sb3 = new StringBuilder("abcdef");
sb3.insert(3, "-");
System.out.println(sb3);               // abc-def

// 9
System.out.println("   ".isBlank() + " " + "   ".isEmpty());   // true false

// 10
System.out.println("ab".repeat(2) + "|" + String.join("-", "x", "y"));   // abab|x-y

// 11
String x = new String("java");
System.out.println(x == x.intern());   // false (x is heap, intern returns pool ref)
System.out.println("java" == x.intern()); // true
```

---

## 13. Interview questions and answers

**Q1. Name different ways to create a String.**
Literal (pool), `new String("..")` (heap), from `char[]`, from `byte[]` (with charset), from `StringBuilder`/`StringBuffer`, and `String.valueOf(...)`.

**Q2. `trim()` vs `strip()`?**
`trim()` removes characters <= U+0020 only. `strip()` (Java 11) is Unicode-aware and removes all characters that `Character.isWhitespace` accepts. Prefer `strip()`.

**Q3. `replace()` vs `replaceAll()`?**
`replace` treats the target as literal text; `replaceAll` treats it as a regular expression (and the replacement string also has special characters `$` and `\`).

**Q4. Why does `"a.b".split(".")` return an empty array?**
`split` takes a regex, and `.` matches any character, so every character is a delimiter and the resulting empty strings are removed. Use `split("\\.")`.

**Q5. What does `intern()` do?**
Returns the canonical String Pool copy of the string, adding it to the pool if absent. Saves memory for duplicate runtime strings.

**Q6. What does `getBytes()` return and why does charset matter?**
The string encoded to bytes. Different charsets give different bytes (e.g. one Hindi character is 3 bytes in UTF-8). Always specify the charset.

**Q7. Why use `StringBuilder` instead of `String` concatenation in loops?**
`String` is immutable: every `+=` creates a new object and copies everything (O(n^2), lots of garbage). `StringBuilder` mutates one internal buffer.

**Q8. What is the default capacity of `StringBuilder`? How does it grow?**
16. When full it allocates a new array of size `oldCapacity * 2 + 2`, copies the data, and discards the old array.

**Q9. What do `ensureCapacity()` and `trimToSize()` do?**
`ensureCapacity(n)` pre-grows the buffer to at least `n`, avoiding repeated reallocation. `trimToSize()` shrinks the buffer to the current length to free unused memory.

**Q10. Difference between `StringBuffer` and `StringBuilder`?**
`StringBuffer` is synchronized (thread-safe, slower); `StringBuilder` is not (faster, for single-threaded use). Same API otherwise.

**Q11. Do `StringBuilder`/`StringBuffer` override `equals()`?**
No. They use `Object.equals()` (reference comparison), so two builders with identical text are not equal. Compare via `toString().equals(...)`, `compareTo`, or `contentEquals`.

**Q12. Is `StringBuffer` completely safe in multithreaded code?**
Each method is atomic, but sequences of calls (check-then-act) are not. You still need external synchronization for compound operations.

**Q13. `sb.delete(start, end)`: is `end` inclusive?**
No, `end` is exclusive. `start` is inclusive.

**Q14. What is the difference between `length()` and `capacity()`?**
`length()` is the number of characters stored; `capacity()` is the size of the internal array.

**Q15. How do you convert between String and StringBuilder?**
String to builder: `new StringBuilder(str)`. Builder to String: `sb.toString()` or `new String(sb)`.

---

## 14. Cheat sheet

```
CREATING STRINGS
  "x"                      pool            new String("x")        heap
  new String(char[])       heap            new String(byte[], cs) heap
  new String(sb / sbuf)    heap            String.valueOf(x)

METHODS (all return NEW values, original unchanged)
  length() isEmpty() isBlank() charAt(i) indexOf() contains()
  trim()  -> chars <= ' '        strip() -> Unicode whitespace (prefer)
  repeat(n)
  replace(a,b)      literal          replaceAll(regex, r)   regex
  split(regex)  (escape . | $ etc.)  default drops trailing empties
  String.join(delim, items...)
  String.format("%s %d %.2f %n", ...)    %-10s left   %05d zero-pad   %,d grouping
  intern()    -> pooled copy
  getBytes(charset) -> encoded byte[]

MUTABLE CLASSES (same API, same object modified, most return this)
  append  insert(i,x)  delete(s,e)  deleteCharAt(i)  replace(s,e,str)  reverse
  setCharAt  setLength  ensureCapacity  trimToSize  length  capacity  toString

CAPACITY
  default 16 | from String: len + 16 | grows to old*2 + 2 when full
  length = chars used, capacity = array size

THREADS
  StringBuffer  = synchronized, thread-safe, slower
  StringBuilder = no sync, fastest, default choice

GOTCHAS
  - assign results of String methods (immutability)
  - replaceAll / split take REGEX ("." matches everything)
  - StringBuilder/StringBuffer equals() compares REFERENCES
  - never use String += inside big loops
```

---

*End of notes.*
