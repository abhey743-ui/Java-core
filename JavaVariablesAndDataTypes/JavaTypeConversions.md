# Java Type Conversions, Type Promotion and Casting

> Java is **strongly typed**: every value has a type, and Java is strict about mixing types.
> But real programs constantly need to move values between types: an `int` into a `double`, a `double` into an `int`, a `char` into a number, a `String` into an `int`.
> This guide explains **every kind of conversion**, **what Java does automatically (promotion)** and **what you must do yourself (casting)**, with lots of examples.

---

## Table of Contents

1. [What Is Type Conversion and Why Do We Need It?](#1-what-is-type-conversion-and-why-do-we-need-it)
2. [The Big Picture: Implicit vs Explicit](#2-the-big-picture-implicit-vs-explicit)
3. [Widening Conversion (Automatic, Safe)](#3-widening-conversion-automatic-safe)
4. [Narrowing Conversion and Casting (Manual, Risky)](#4-narrowing-conversion-and-casting-manual-risky)
5. [What Happens Inside a Narrowing Cast (Bit Level)](#5-what-happens-inside-a-narrowing-cast-bit-level)
6. [Assignment Rules: The Constant Exception](#6-assignment-rules-the-constant-exception)
7. [Type Promotion in Expressions](#7-type-promotion-in-expressions)
8. [Compound Assignment Operators Hide a Cast](#8-compound-assignment-operators-hide-a-cast)
9. [Promotion in Method Calls and Overloading](#9-promotion-in-method-calls-and-overloading)
10. [Conversions Involving char](#10-conversions-involving-char)
11. [boolean Cannot Be Converted](#11-boolean-cannot-be-converted)
12. [Converting To and From String](#12-converting-to-and-from-string)
13. [Boxing and Unboxing (Primitive ↔ Wrapper)](#13-boxing-and-unboxing-primitive--wrapper)
14. [Reference Type Conversion: Upcasting and Downcasting](#14-reference-type-conversion-upcasting-and-downcasting)
15. [Common Mistakes and Gotchas](#15-common-mistakes-and-gotchas)
16. [Cheat Sheet, Summary and Quiz](#16-cheat-sheet-summary-and-quiz)

---

## 1. What Is Type Conversion and Why Do We Need It?

**Type conversion** means changing a value from one data type to another.

```java
int apples = 5;
double price = apples;        // int → double (conversion happened here)
```

You need it all the time:

| Situation | Example |
|-----------|---------|
| Mixing numbers in math | `int + double` |
| Storing a big number into a smaller box | `double` → `int` |
| Using a character as a number | `'A'` → `65` |
| Reading user input (text) as a number | `"42"` → `42` |
| Putting numbers into collections | `int` → `Integer` |
| Treating a Dog as an Animal | reference conversion |

Remember from the earlier guides: each type has a **size and a way of reading bits**. Converting is not just "relabeling"; the bits often have to be **rearranged** (for example, an `int` and a `float` store the same number with completely different bit patterns).

---

## 2. The Big Picture: Implicit vs Explicit

```
                         Type Conversion in Java
                                  │
        ┌─────────────────────────┴──────────────────────────┐
        ▼                                                    ▼
  IMPLICIT (automatic)                              EXPLICIT (manual, "casting")
  Java does it for you                              YOU write (type) in front
        │                                                    │
  Widening: small → big                             Narrowing: big → small
  (int → long, int → double)                        (double → int, long → byte)
  No data loss (usually)                            Data may be lost!
```

| | Implicit (Widening) | Explicit (Narrowing / Casting) |
|---|---|---|
| Who does it | Compiler, automatically | Programmer, with `(type)` |
| Direction | Smaller range → larger range | Larger range → smaller range |
| Risk | Safe (rare precision loss) | **Can lose data or change the value** |
| Syntax | `long x = intValue;` | `int x = (int) longValue;` |

**Casting** is simply the name for the explicit form:

```
(targetType) value
```

```java
double d = 9.78;
int i = (int) d;       // casting: i becomes 9
```

---

## 3. Widening Conversion (Automatic, Safe)

### 3.1 The rule

Java automatically converts a value to a "bigger" type when it is going into a variable of that bigger type. The value is **preserved**, so no cast is needed.

### 3.2 The widening chart

```
 byte ──► short ──► int ──► long ──► float ──► double
                     ▲
          char ──────┘
```

Read it left to right: any type can be converted to any type **to its right**.

| From | Can be widened to |
|------|-------------------|
| `byte` | `short`, `int`, `long`, `float`, `double` |
| `short` | `int`, `long`, `float`, `double` |
| `char` | `int`, `long`, `float`, `double` |
| `int` | `long`, `float`, `double` |
| `long` | `float`, `double` |
| `float` | `double` |

Notice the two things that are **not** on the chart:

- **`byte` → `char`** is **not** a widening conversion (a `byte` can be negative, `char` cannot).
- **`short` ↔ `char`** are not widening to each other either (same reason: signed vs unsigned).
- **`boolean`** is not in the chart at all.

### 3.3 Examples

```java
byte  b = 10;
short s = b;          // byte → short
int   i = s;          // short → int
long  l = i;          // int → long
float f = l;          // long → float  (yes, allowed!)
double d = f;         // float → double

char c = 'A';
int code = c;         // char → int  → 65
System.out.println(code);   // 65
```

### 3.4 "Widening" is about RANGE, not number of bits

`long` has 64 bits but `float` has only 32. Yet `long → float` is a **widening** conversion, because a `float` can reach **much larger** values (up to about 3.4×10³⁸) than a `long` (up to about 9.2×10¹⁸).

The price: a `float` has only 24 bits of precision, so the value may be **rounded**.

### 3.5 Widening can still lose precision (in 3 cases)

The **magnitude** is always kept, but the **exact digits** may not be:

| Conversion | Why precision can be lost |
|------------|---------------------------|
| `int` → `float` | `float` has only 24 bits of precision |
| `long` → `float` | `float` has only 24 bits of precision |
| `long` → `double` | `double` has only 53 bits of precision |

```java
int big = 123_456_789;
float f = big;                 // allowed automatically
System.out.println(f);         // 1.23456792E8
System.out.println((int) f);   // 123456792   ← not the same as 123456789!

long huge = 9_007_199_254_740_993L;   // 2^53 + 1
double d = huge;
System.out.println((long) d);         // 9007199254740992   ← the last +1 vanished
```

All other widening conversions (like `int → long` and `int → double`) are **exact**.

### 3.6 Widening a negative number (sign extension)

From the earlier guide: when a signed value is widened, Java copies the **sign bit**, so negative values stay negative.

```java
byte b = -5;       // 1111 1011
int i = b;         // 1111 1111 1111 1111 1111 1111 1111 1011  → still -5
```

### 3.7 Float → double does NOT restore lost digits

```java
float f = 0.1f;
double d = f;
System.out.println(d);    // 0.10000000149011612
```

The `float` was already an approximation of 0.1. Widening it to `double` faithfully keeps that approximation (it doesn't magically go back to the true 0.1).

---

## 4. Narrowing Conversion and Casting (Manual, Risky)

### 4.1 The rule

Going from a **bigger** type to a **smaller** one is **never automatic**, because you might lose data. Java makes you say "yes, I know" by writing a **cast**.

```java
double d = 9.99;
int i = d;              // ERROR: incompatible types: possible lossy conversion from double to int
int j = (int) d;        // OK: j = 9
```

### 4.2 Narrowing chart (the reverse of widening)

```
 double ──► float ──► long ──► int ──► short ──► byte
                                 └────► char ◄────┘  (and byte/short/char between each other)
```

Any move **to the left** needs a cast. Also needing a cast: `byte → char`, `short → char`, `char → byte`, `char → short`.

### 4.3 Examples and results

```java
// double → int : fractional part is CUT OFF (truncated toward zero), not rounded
System.out.println((int) 9.99);      //  9
System.out.println((int) -9.99);     // -9      (not -10)

// int → byte : only the lowest 8 bits are kept
System.out.println((byte) 130);      // -126
System.out.println((byte) 200);      // -56
System.out.println((byte) 256);      // 0
System.out.println((byte) -129);     // 127

// int → short
System.out.println((short) 40_000);  // -25536

// long → int
System.out.println((int) 3_000_000_000L);   // -1294967296

// int → char
System.out.println((char) 65);       // A
System.out.println((int) (char) -1); // 65535   (char is unsigned)

// double → float : rounded to float precision, or becomes Infinity if too large
System.out.println((float) 0.1);     // 0.1
System.out.println((float) 1e40);    // Infinity
```

### 4.4 Casting does NOT round. Use `Math.round()` for that

```java
double price = 9.99;
System.out.println((int) price);          // 9    (truncated)
System.out.println(Math.round(price));    // 10   (rounded; returns a long for a double)
System.out.println((int) Math.round(price)); // 10 (cast the long back to int)
```

> `Math.round(double)` returns a **`long`**. `Math.round(float)` returns an **`int`**.

### 4.5 Floating-point → integer rules (very important)

When casting a `float` or `double` to an integer type, Java follows these rules:

1. **NaN** becomes **0**.
2. A value too big becomes the **largest** value of the target type (**saturates**, doesn't wrap).
3. A value too small becomes the **smallest** value of the target type.
4. Otherwise the fraction is **truncated toward zero**.

```java
System.out.println((int) Double.NaN);       // 0
System.out.println((int) 1e20);             // 2147483647   (Integer.MAX_VALUE)
System.out.println((int) -1e20);            // -2147483648  (Integer.MIN_VALUE)
System.out.println((long) 1e30);            // 9223372036854775807 (Long.MAX_VALUE)
System.out.println((int) Double.POSITIVE_INFINITY);  // 2147483647
```

Compare this with **integer → smaller integer**, which **wraps around** (section 5). Two different behaviours!

### 4.6 Float → byte, short or char goes in two steps

A `double`/`float` cannot jump directly into `byte`, `short` or `char`. Java first converts to `int` (with the saturating rules above), **then** narrows from `int`:

```java
System.out.println((byte) 300.7);    // 300.7 → int 300 → byte 44
System.out.println((byte) 1e10);     // 1e10 → int 2147483647 (0x7FFFFFFF) → byte -1 (lowest 8 bits are all 1s)
```

### 4.7 Cast operator precedence

The cast applies to the **thing immediately after it**, so use parentheses when needed.

```java
System.out.println((int) 5.5 + 2.5);      // 5 + 2.5 = 7.5   (cast only affects 5.5)
System.out.println((int) (5.5 + 2.5));    // (int) 8.0 = 8   (cast affects the whole sum)
```

---

## 5. What Happens Inside a Narrowing Cast (Bit Level)

### 5.1 Integer → smaller integer: **keep the low bits, drop the rest**

Java simply **chops off the left-hand bits**. The remaining bits are then **re-read as a signed number** of the smaller size.

**Example: `(byte) 130`**

```
130 as int :  0000 0000 0000 0000 0000 0000 1000 0010
keep last 8 bits:                             1000 0010
read as signed byte:   -128 + 2 = -126
```

**Example: `(byte) 200`**

```
200 as int :  ... 0000 0000 1100 1000
keep last 8 :              1100 1000   → -128 + 64 + 8 = -56
```

**Example: `(short) 40000`**

```
40000 as int :  0000 0000 0000 0000 1001 1100 0100 0000
keep last 16 :                      1001 1100 0100 0000   → -32768 + ... = -25536
```

### 5.2 A handy shortcut

For narrowing to an N-bit signed type, you can think of it as the "circle" from the earlier guide:

```
byte :  keep (value mod 256), then shift into the range -128 … 127
130  →  130 − 256 = −126
200  →  200 − 256 = −56
300  →  300 − 256 = 44
```

### 5.3 In bytecode

You can see casts in the compiled bytecode (use `javap -c`). Each conversion has its own instruction:

| Java | Bytecode |
|------|----------|
| `(byte) x` | `i2b` |
| `(short) x` | `i2s` |
| `(char) x` | `i2c` |
| `(long) x` (from int) | `i2l` |
| `(float) x` (from int) | `i2f` |
| `(double) x` (from int) | `i2d` |
| `(int) x` (from long) | `l2i` |
| `(int) x` (from double) | `d2i` |

### 5.4 byte → char: widening AND narrowing combined

`byte → char` first **sign-extends to int**, then **narrows to char**:

```java
byte b = -1;                       // 1111 1111
char c = (char) b;                 // byte → int (-1) → char (lowest 16 bits all ones)
System.out.println((int) c);       // 65535
```

---

## 6. Assignment Rules: The Constant Exception

### 6.1 The surprising rule

Normally you can't put an `int` into a `byte` without a cast. But Java makes one **exception**:

> If the right side is a **constant expression** of type `byte`, `short`, `char` or `int`, **and the value fits** in the target (`byte`, `short` or `char`), no cast is needed.

```java
byte b = 100;        // OK: 100 is an int constant, but it fits in a byte
byte c = 200;        // ERROR: 200 doesn't fit (max 127)
short s = 30000;     // OK
char ch = 65;        // OK → 'A'
char ch2 = 'a' + 1;  // OK → 'b' (constant expression that fits)
byte d = 10 + 20;    // OK: 10 + 20 is a constant expression = 30
byte e = 127 + 1;    // ERROR: 128 doesn't fit
```

### 6.2 `final` variables count as constants

```java
final int LIMIT = 50;
byte b = LIMIT;         // OK: LIMIT is a compile-time constant that fits

int limit = 50;         // not final
byte c = limit;         // ERROR: not a constant, so Java can't be sure it fits
```

### 6.3 What does NOT get the exception

- It does **not** apply to `long`, `float` or `double` constants:
  ```java
  int x = 10L;       // ERROR (10L is a long)
  float f = 3.14;    // ERROR (3.14 is a double)
  long big = 3000000000;   // ERROR: integer number too large (the literal is an int; add L)
  ```
- It does **not** apply to ordinary (non-constant) variables or method results.

---

## 7. Type Promotion in Expressions

**Type promotion** means Java automatically **widens operands** inside an expression so the operation can be carried out. You don't write anything; it just happens.

### 7.1 Binary numeric promotion: the 4 rules

For an operator with **two** numeric operands (`+ - * / %`, comparisons, etc.), Java applies these rules **in order**:

| Rule | If... | Then both operands become... |
|------|-------|------------------------------|
| 1 | either operand is `double` | **`double`** |
| 2 | else, either is `float` | **`float`** |
| 3 | else, either is `long` | **`long`** |
| 4 | otherwise (everything else: `int`, `short`, `byte`, `char`) | **`int`** |

> The result of the operation has the **promoted type**.

```
 byte, short, char  →  int  →  long  →  float  →  double
 ──────────────────  (the result is at least int, or the biggest operand type)
```

### 7.2 Examples

```java
int    + int     →  int        5 + 3          = 8
int    + long    →  long       5 + 3L         = 8L
int    + float   →  float      5 + 3.0f       = 8.0f
int    + double  →  double     5 + 3.0        = 8.0
long   + float   →  float      5L + 3.0f      = 8.0f
float  + double  →  double     1.5f + 2.5     = 4.0
```

### 7.3 The most important consequence: byte, short and char become int

Even when **both** operands are small, the result is `int`:

```java
byte a = 10, b = 20;
byte c = a + b;          // ERROR: a + b is an int, can't go into a byte
byte d = (byte) (a + b); // OK
int  e = a + b;          // OK

short s1 = 1000, s2 = 2000;
short s3 = s1 * s2;      // ERROR (int result)
int s4 = s1 * s2;        // OK = 2,000,000

char c1 = 'A';
char c2 = c1 + 1;        // ERROR (c1 + 1 is an int)
char c3 = (char) (c1 + 1);   // OK → 'B'
```

> **Why?** The JVM's arithmetic instructions work on `int`, `long`, `float` and `double` only. There is no "add two bytes" instruction. So the values are first promoted to `int`. (This links to the JVM guide: the execution engine works with 32-bit slots.)

### 7.4 Promotion step by step

```java
byte b = 42;
char c = 'a';          // 97
short s = 1024;
int i = 50000;
float f = 5.67f;
double d = .1234;

double result = (f * b) + (i / c) - (d * s);
```

How Java evaluates it:

```
f * b      →  float * byte    → byte promoted to float          → float
i / c      →  int / char      → char promoted to int            → int
d * s      →  double * short  → short promoted to double        → double
(f*b) + (i/c)  →  float + int → int promoted to float           → float
float - double →  float promoted to double                      → double
Final: double
```

### 7.5 Unary numeric promotion

Operators with **one** operand also promote small types to `int`:

```java
byte b = 5;
var x = -b;          // int
var y = ~b;          // int
var z = +b;          // int
```

Exceptions: `++` and `--` keep the original type (`b++` stays a `byte`).

### 7.6 Shift operators promote each side separately

For `<<`, `>>`, `>>>`, there is **no** "both sides to the same type" rule. The result type is the promoted type of the **left** operand only:

```java
byte b = 8;
int r1 = b << 2;          // int (byte promoted to int)
long r2 = 1L << 40;       // long (left operand is long)
long r3 = 1 << 40;        // WRONG! left operand is int: 1 << (40 % 32) = 1 << 8 = 256
```

### 7.7 Integer division happens BEFORE any conversion

This is the #1 beginner mistake:

```java
int a = 5, b = 2;

double wrong = a / b;          // 2.0   (5/2 computed as int = 2, THEN converted to 2.0)
double right1 = (double) a / b;   // 2.5   (a becomes double, so b is promoted to double)
double right2 = a / 2.0;       // 2.5
double right3 = a * 1.0 / b;   // 2.5
```

```
double wrong = a / b;
               └─┬─┘
         int / int = int (2)   ← the fraction is already gone
                   │
                   └─► widened to double → 2.0
```

Notice the cast `(double) a / b`: the cast has **higher precedence** than `/`, so it only converts `a`, and then promotion converts `b`.

### 7.8 Overflow happens BEFORE widening, too

```java
long wrong = Integer.MAX_VALUE + 1;        // computed as int → -2147483648, THEN widened
long right = Integer.MAX_VALUE + 1L;       // 2147483648

long ms1 = 24 * 60 * 60 * 1000 * 365;      // all ints → overflows → 1471228928  (wrong!)
long ms2 = 24L * 60 * 60 * 1000 * 365;     // 31536000000  (correct)
```

**Rule:** if the result might be big, make **one operand `long`** *before* the calculation starts.

### 7.9 Promotion with `+` and Strings

If **either** operand of `+` is a `String`, the other is converted to a string and they are **concatenated** (this is *string conversion*, not numeric promotion).

```java
System.out.println(1 + 2 + "3");        // "33"   (1+2 = 3 first, then "3" + "3")
System.out.println("1" + 2 + 3);        // "123"  (left to right: "1" + 2 = "12", then "12" + 3)
System.out.println(1 + 2 + "3" + 4 + 5);// "3345"
System.out.println('a' + 'b');          // 195    (both chars → int, then added)
System.out.println('a' + 'b' + "c");    // "195c"
System.out.println("c" + 'a' + 'b');    // "cab"
```

### 7.10 The ternary operator `? :` also promotes

```java
System.out.println(true ? 1 : 2.0);        // 1.0  (int promoted to double)
```

A classic trap with wrappers and `null`:

```java
Integer a = null;
int x = true ? a : 0;      // NullPointerException! (a is unboxed because the other side is int)
```

And a famous `char` puzzle:

```java
char c = 'a';
int i = 0;
System.out.println(true ? c : 0);   // a      (0 is a constant that fits in char → type char)
System.out.println(true ? c : i);   // 97     (i is a non-constant int → type int)
```

---

## 8. Compound Assignment Operators Hide a Cast

The operators `+=`, `-=`, `*=`, `/=`, `%=`, `<<=` etc. include an **automatic cast** back to the variable's type:

```
a += b      means      a = (TypeOfA) (a + b)
```

So these work even though `b + 5` would normally be an `int`:

```java
byte b = 10;
b += 5;               // OK → 15    (hidden cast to byte)
b = b + 5;            // ERROR      (b + 5 is an int)

short s = 5;
s *= 2;               // OK
s = s * 2;            // ERROR
s++;                  // OK (++ keeps the type)

char ch = 'a';
ch += 1;              // OK → 'b'
ch++;                 // OK → 'c'
```

### The danger: silent data loss

```java
byte b = 10;
b += 300;
System.out.println(b);        // 54   (10 + 300 = 310, narrowed to a byte: 310 - 256 = 54)

int x = 10;
x += 3.7;                     // x = (int) (10 + 3.7) = (int) 13.7 = 13
System.out.println(x);        // 13

int y = 10;
y *= 1.5;                     // y = (int) (10 * 1.5) = 15
```

So `+=` can quietly truncate or wrap. It's convenient, but be aware of it.

---

## 9. Promotion in Method Calls and Overloading

When you call a method, Java decides which version (overload) to use. Conversions are tried in a **specific order of preference**:

| Phase | Allowed conversions |
|-------|---------------------|
| **1. Strict** | Identity and **widening primitive** (and widening reference). No boxing. |
| **2. Loose** | Phase 1 **plus boxing/unboxing**. |
| **3. Varargs** | Variable-arity methods (`int...`). |

Java picks the **most specific** method from the first phase that finds a match.

### 9.1 Primitive widening in overloads

```java
static void show(int x)    { System.out.println("int"); }
static void show(long x)   { System.out.println("long"); }
static void show(double x) { System.out.println("double"); }

public static void main(String[] args) {
    byte b = 5;
    show(b);        // int     (byte → int is the closest widening)
    show('a');      // int     (char → int)
    show(5);        // int     (exact match)
    show(5L);       // long    (exact match)
    show(5f);       // double  (float → double is the only fit)
    show(5.0);      // double
}
```

If you remove `show(int)`, then `show(5)` calls `show(long)` (the next-closest widening).

### 9.2 Widening beats boxing

```java
static void test(long x)    { System.out.println("long"); }
static void test(Integer x) { System.out.println("Integer"); }

test(5);     // long   (widening in phase 1 wins over boxing in phase 2)
```

### 9.3 Narrowing is never automatic in a method call

```java
static void takesInt(int x) { }

takesInt(5L);        // ERROR: long can't be passed to an int parameter
takesInt((int) 5L);  // OK
```

Also, the "constant fits" exception from section 6 does **not** apply to method arguments:

```java
static void takesByte(byte b) { }

takesByte(10);          // ERROR (even though 10 fits in a byte)
takesByte((byte) 10);   // OK
```

### 9.4 Array indexes

An array index must be `int` (or something that promotes to `int`):

```java
int[] arr = new int[10];
byte b = 3;
char c = 2;
long idx = 4;

arr[b] = 1;           // OK (byte → int)
arr[c] = 1;           // OK (char → int)
arr[idx] = 1;         // ERROR: possible lossy conversion from long to int
arr[(int) idx] = 1;   // OK
```

---

## 10. Conversions Involving char

`char` is a **16-bit unsigned number** that represents a Unicode code. So it converts naturally to and from numbers.

### 10.1 char → number (widening, automatic)

```java
char c = 'A';
int code = c;                     // 65
long l = c;                       // 65L
double d = c;                     // 65.0
System.out.println((int) 'a');    // 97
```

### 10.2 number → char (needs a cast, unless it's a fitting constant)

```java
char c1 = 66;                // OK constant that fits → 'B'
int n = 67;
char c2 = (char) n;          // 'C' (cast required, n is a variable)
```

### 10.3 Arithmetic with char gives int

```java
char c = 'A';
System.out.println(c + 1);            // 66        (int)
System.out.println((char) (c + 1));   // B
c++;                                  // c is now 'B' (++ keeps the type)
c += 2;                               // c is now 'D' (hidden cast)
```

### 10.4 Looping through letters

```java
for (char ch = 'a'; ch <= 'e'; ch++) {
    System.out.print(ch + " ");       // a b c d e
}
```

### 10.5 Digit character ↔ number (very common!)

The characters `'0'` to `'9'` have consecutive codes (48 to 57), so subtracting `'0'` gives the digit:

```java
char digitChar = '7';
int digit = digitChar - '0';          // 55 - 48 = 7
System.out.println(digit);            // 7

int number = 5;
char back = (char) (number + '0');    // '5'
System.out.println(back);             // 5
```

Or use the library: `Character.getNumericValue('7')` returns `7`, and `Character.forDigit(5, 10)` returns `'5'`.

### 10.6 Upper/lower case trick

```java
char lower = 'c';
char upper = (char) (lower - 32);      // 'C'  (lowercase is 32 higher than uppercase in ASCII)
// Better: Character.toUpperCase(lower);
```

### 10.7 char ↔ byte / short

These need casts in **both** directions (signed vs unsigned mismatch):

```java
char c = 'A';
byte b = (byte) c;      // OK with cast
short s = (short) c;    // OK with cast
char d = (char) b;      // OK with cast
```

---

## 11. boolean Cannot Be Converted

`boolean` has **no conversion at all** to or from any other primitive type (no widening, no casting):

```java
boolean flag = true;
int x = flag;          // ERROR
int y = (int) flag;    // ERROR
boolean b = 1;         // ERROR
boolean c = (boolean) 1;   // ERROR
```

If you need it, write the logic yourself:

```java
int asInt = flag ? 1 : 0;
boolean asBool = (x != 0);
```

---

## 12. Converting To and From String

A `String` is an **object**, not a primitive, so you **cannot cast** to or from it:

```java
int n = (int) "42";        // ERROR
String s = (String) 42;    // ERROR
```

Use these tools instead.

### 12.1 Number → String

```java
int n = 42;
String s1 = String.valueOf(n);       // "42"   (recommended)
String s2 = Integer.toString(n);     // "42"
String s3 = "" + n;                  // "42"   (quick trick, string concatenation)
String s4 = String.format("%d", n);  // "42"

double d = 3.14;
String s5 = String.valueOf(d);       // "3.14"

char c = 'A';
String s6 = String.valueOf(c);       // "A"
String s7 = Character.toString(c);   // "A"
```

### 12.2 String → Number (parsing)

```java
int n = Integer.parseInt("42");              // 42
long l = Long.parseLong("9000000000");       // 9000000000
double d = Double.parseDouble("3.14");       // 3.14
boolean b = Boolean.parseBoolean("true");    // true

Integer w = Integer.valueOf("42");           // returns an Integer object
```

If the text isn't valid, you get an exception:

```java
Integer.parseInt("12.5");     // NumberFormatException
Integer.parseInt("abc");      // NumberFormatException
Integer.parseInt(" 42");      // NumberFormatException (space!)
Integer.parseInt("1_000");    // NumberFormatException (underscores only work in source code)
Integer.parseInt("");         // NumberFormatException
```

### 12.3 String → char

```java
String s = "Hello";
char first = s.charAt(0);           // 'H'
char[] letters = s.toCharArray();   // {'H','e','l','l','o'}
```

### 12.4 Other bases

```java
Integer.parseInt("ff", 16);           // 255
Integer.toString(255, 2);             // "11111111"
```

---

## 13. Boxing and Unboxing (Primitive ↔ Wrapper)

- **Boxing**: primitive → wrapper object (`int` → `Integer`)
- **Unboxing**: wrapper → primitive (`Integer` → `int`)

Java does both **automatically** (autoboxing).

```java
Integer boxed = 5;       // boxing   → Integer.valueOf(5)
int plain = boxed;       // unboxing → boxed.intValue()
```

### 13.1 You can combine unboxing with widening

```java
Integer i = 10;
long l = i;              // unbox (Integer → int), then widen (int → long)   ✓
double d = i;            // unbox, then widen                                ✓
```

### 13.2 But you CANNOT combine widening with boxing

```java
Long l = 5;              // ERROR! int would need to widen to long AND be boxed
Long l2 = 5L;            // OK
Double d = 5;            // ERROR
Double d2 = 5.0;         // OK
Double d3 = (double) 5;  // OK (cast first, then box)

Object o = 5;            // OK: box to Integer, then widen the reference to Object
```

**Rule:** the wrapper must match the primitive **exactly** (`int` ↔ `Integer`, `long` ↔ `Long`, ...).

### 13.3 Casting between wrappers is not allowed

```java
Integer i = 5;
Long l = (Long) i;            // ERROR: Integer can't be cast to Long
Long l2 = (long) i;           // OK: unbox → widen → box
Long l3 = i.longValue();      // OK: clearer
```

### 13.4 Wrapper methods to convert between numeric types

```java
Integer i = 65;
i.byteValue();       // 65
i.longValue();       // 65L
i.doubleValue();     // 65.0

Double d = 9.99;
d.intValue();        // 9  (truncates, like a cast)
```

### 13.5 The `equals` trap with mixed types

```java
Long a = 5L;
System.out.println(a.equals(5));    // false!  5 is boxed as Integer, and a Long never equals an Integer
System.out.println(a.equals(5L));   // true
```

### 13.6 `==` on wrappers vs primitives

```java
Integer x = 127, y = 127;
System.out.println(x == y);        // true  (cached objects, -128 to 127)
Integer p = 1000, q = 1000;
System.out.println(p == q);        // false (different objects) → use equals()
System.out.println(p == 1000);     // true  (p is unboxed because one side is a primitive)
```

### 13.7 Unboxing `null` crashes

```java
Integer count = null;
int n = count;       // NullPointerException
```

---

## 14. Reference Type Conversion: Upcasting and Downcasting

Casting isn't only for numbers. It also applies to **objects**. Here, casting **does not change the object**; it only changes **how the compiler views** the reference.

### 14.1 The family tree used in the examples

```java
class Animal {
    void eat() { System.out.println("eating"); }
}
class Dog extends Animal {
    void bark() { System.out.println("woof"); }
}
class Cat extends Animal {
    void meow() { System.out.println("meow"); }
}
```

```
        Animal
        /    \
      Dog    Cat
```

### 14.2 Upcasting (child → parent): automatic and always safe

```java
Dog dog = new Dog();
Animal a = dog;          // implicit upcast (a Dog IS-A Animal)
a.eat();                 // OK
a.bark();                // ERROR: the Animal "view" doesn't know bark()
```

The object is still a `Dog` in memory; you just can't see its `Dog`-only methods through an `Animal` reference.

### 14.3 Downcasting (parent → child): explicit and risky

```java
Animal a = new Dog();        // a really holds a Dog
Dog d = (Dog) a;             // explicit downcast: OK at runtime
d.bark();                    // works

Animal b = new Cat();
Dog bad = (Dog) b;           // compiles fine, but CRASHES at runtime:
                             // ClassCastException: Cat cannot be cast to Dog
```

Two checks happen:

| When | Check |
|------|-------|
| **Compile time** | Are the two types even **related** (parent/child or interface)? If not → compile error. |
| **Runtime** | Is the **actual object** really that type? If not → `ClassCastException`. |

```java
Dog d = (Dog) "hello";       // compile error: String and Dog are unrelated
```

### 14.4 Protect yourself with `instanceof`

```java
Animal a = getAnimal();

if (a instanceof Dog) {
    Dog d = (Dog) a;
    d.bark();
}

// Modern way (Java 16+): pattern matching, check and cast in one step
if (a instanceof Dog d) {
    d.bark();
}
```

### 14.5 Casting with `Object`

```java
Object o = "hello";
String s = (String) o;          // OK
Integer n = (Integer) o;        // ClassCastException at runtime

Object o2 = 42;                 // boxes to Integer
int x = (Integer) o2;           // OK (cast to Integer, then auto-unbox)
int y = (int) o2;               // OK in modern Java (cast Object → int)
```

### 14.6 Interfaces

```java
Runnable r = new Thread();               // upcast to an interface
Thread t = (Thread) r;                   // downcast back
```

### 14.7 Quick summary: primitive casting vs reference casting

| | Primitive cast | Reference cast |
|---|----------------|----------------|
| Changes the actual data? | **Yes** (new value/bits) | **No** (same object, different "view") |
| Failure mode | Silent data loss/wrap | `ClassCastException` at runtime |
| Related to | Size and range | Inheritance relationships |

---

## 15. Common Mistakes and Gotchas

| # | Mistake | Why it happens | Fix |
|---|---------|----------------|-----|
| 1 | `float f = 3.14;` | `3.14` is a `double` | `3.14f` or `(float) 3.14` |
| 2 | `long big = 3000000000;` | The literal is an `int` and is too big | `3000000000L` |
| 3 | `byte c = a + b;` | `byte + byte` is an `int` | `(byte) (a + b)` |
| 4 | `double avg = sum / count;` (both `int`) | Integer division first | `(double) sum / count` |
| 5 | `long x = a * b;` (both `int`, big values) | Overflow before widening | `(long) a * b` |
| 6 | `int pct = (int) (a / b * 100);` (ints) | `a / b` becomes 0 | `a * 100 / b` or use doubles |
| 7 | `(int) 2.9` expected `3` | Casts truncate | `Math.round(2.9)` |
| 8 | `System.out.println('a' + 1);` prints `98` | `char + int` is `int` | `(char) ('a' + 1)` |
| 9 | `Long l = 5;` | Can't widen and box together | `Long l = 5L;` |
| 10 | `byte b = 130;` | Doesn't fit | `(byte) 130` (gives −126) or use `short` |
| 11 | `arr[someLong]` | Index must be `int` | `arr[(int) someLong]` |
| 12 | `(Dog) animal` crashes | Object isn't really a `Dog` | Check with `instanceof` |
| 13 | `s = s + 1;` with `short s` | Result is `int` | `s += 1;` or `s++` |
| 14 | `Integer a = 1000, b = 1000; a == b` | Compares references | `a.equals(b)` |
| 15 | `1 << 40` expecting a large `long` | Shift is done in `int` | `1L << 40` |
| 16 | Money in `double` | Floating-point rounding errors | `BigDecimal` or `long` cents |

### General safety tips

1. **Prefer widening** whenever you can. It's automatic and safe.
2. **Think twice before every narrowing cast.** Ask: "Could the value be out of range?"
3. Use **`Math.toIntExact(long)`** to narrow `long → int` and throw an error instead of silently wrapping:
   ```java
   int safe = Math.toIntExact(5_000_000_000L);   // ArithmeticException: integer overflow
   ```
4. Use **`Math.round`** when you want rounding, not a cast.
5. Put the **wider type first** in math involving big numbers: `1L * a * b`.
6. For references, **use `instanceof`** before downcasting.

---

## 16. Cheat Sheet, Summary and Quiz

### 16.1 Conversion summary table

| Conversion | Automatic? | Needs cast? | Data loss? | Example |
|------------|-----------|-------------|-----------|---------|
| Widening primitive | Yes | No | Usually no (maybe precision) | `long l = intValue;` |
| Narrowing primitive | No | **Yes** | Possible | `int i = (int) doubleValue;` |
| Constant that fits | Yes | No | No | `byte b = 100;` |
| Numeric promotion (expressions) | Yes (by Java) | No | No | `byte + byte → int` |
| Compound assignment (`+=`) | Yes (hidden cast) | No | Possible | `b += 5;` |
| Boxing / unboxing | Yes | No | No (null → NPE) | `Integer i = 5;` |
| Upcasting (reference) | Yes | No | No | `Animal a = new Dog();` |
| Downcasting (reference) | No | **Yes** | Might throw | `Dog d = (Dog) a;` |
| String ↔ number | No | **Not a cast!** | Might throw | `Integer.parseInt("5")` |
| boolean ↔ number | **Never** | **Never** | n/a | not possible |

### 16.2 Binary numeric promotion in one glance

```
Is any operand double?  → everything becomes double
else is any float?      → everything becomes float
else is any long?       → everything becomes long
else                    → everything becomes int   (byte, short, char, int)
```

### 16.3 Widening and narrowing at a glance

```
WIDENING (automatic):
 byte ─► short ─► int ─► long ─► float ─► double
         char ───────┘

NARROWING (needs a cast):  go in the opposite direction
 double ─► float ─► long ─► int ─► short ─► byte
                          int ─► char
```

### 16.4 Summary in simple points

1. **Type conversion** changes a value from one type to another.
2. **Widening** (small → big) is **automatic**: `byte → short → int → long → float → double`, and `char → int`. It is about **range**, not bit count (`long → float` is widening).
3. Widening is exact, except `int → float`, `long → float` and `long → double`, which may lose precision.
4. **Narrowing** (big → small) needs an explicit **cast** `(type) value`.
5. Casting **integers** to a smaller integer **keeps the low bits** (wraps around). Casting a **floating-point** value to an integer **truncates** the fraction and **saturates** at the limits (NaN becomes 0).
6. A **constant** that fits can be assigned to `byte`, `short` or `char` without a cast (`byte b = 100;`); `final` constants count too.
7. **Type promotion** in expressions: `byte`, `short`, `char` always become **`int`**, then the larger of the two operand types wins (`int → long → float → double`).
8. Integer division and overflow happen **before** the result is widened. Make an operand `long` or `double` **first**.
9. Compound assignments (`+=`, `*=`...) include a **hidden cast**.
10. In overloaded method calls, Java tries **widening first**, then **boxing**, then **varargs**.
11. `char` is an unsigned number; `char + int` is an `int`. `'7' - '0'` gives `7`.
12. **`boolean` never converts** to or from numbers. **Strings** are converted with `parse`/`valueOf`, not casts.
13. **Boxing** needs the exact wrapper (`Long l = 5L`); you can unbox then widen (`long l = integerObj`), not widen then box.
14. **Reference casting**: upcasting is automatic and safe; downcasting is explicit and may throw `ClassCastException`; use `instanceof` to be safe.

### 16.5 Mini Quiz

1. Does this compile? Why or why not? How would you fix it?
   ```java
   byte a = 10, b = 20;
   byte c = a + b;
   ```
2. What does `System.out.println('a' + 1);` print?
3. What is the value of `x` after `int x = 10; x += 5.5;`?
4. What does `System.out.println(1 + 2 + "3");` print?
5. What is `(byte) 200`?
6. What is the value of `d` in `double d = 7 / 2;`?
7. Given `test(long x)` and `test(Integer x)`, which one runs for `test(5)`?
8. Is `Long l = 5;` valid?
9. What is `(int) -3.99`?
10. Does `short s = 10; s = s * 2;` compile? What about `s *= 2;`?
11. What is the type of the result of `5 + 3.0f`?
12. What does `(int) 1e20` give?

<details>
<summary>Click for answers</summary>

1. **No, it doesn't compile.** `a + b` is promoted to `int`, and you can't put an `int` into a `byte` without a cast. Fix: `byte c = (byte) (a + b);` or use `int c = a + b;`.
2. **98**: `'a'` (97) is promoted to `int`, then 1 is added.
3. **15**: `x += 5.5` means `x = (int) (10 + 5.5) = (int) 15.5 = 15`.
4. **`33`**: `1 + 2 = 3` first, then `3 + "3"` gives `"33"`.
5. **−56** (200 − 256).
6. **3.0**: `7 / 2` is integer division (3), then widened to `3.0`.
7. **`test(long)`**: widening (phase 1) is preferred over boxing (phase 2).
8. **No.** You can't widen `int` to `long` and box it in one step. Use `Long l = 5L;`.
9. **−3**: the fraction is truncated toward zero.
10. `s = s * 2;` **doesn't compile** (`s * 2` is an `int`). `s *= 2;` **compiles** (hidden cast).
11. **`float`**: the `int` is promoted to `float`.
12. **2147483647** (`Integer.MAX_VALUE`): floating-point to integer casts saturate.

</details>

---

*Practice tip: write small programs that print the result of each example, change the numbers, and predict the output before you run them. That is the fastest way to master conversions.*
