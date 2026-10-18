# How Java Stores Negative Numbers and Decimal Numbers

> Computer memory only has **0s and 1s**. There is no minus sign and no decimal point inside it.
> So how does Java store `-5`? And how does it store `3.14`?
> This guide answers both questions step by step, in simple language.

---

## Table of Contents

**Part A: Negative numbers (integers)**

1. [The Core Idea: Bits Have No Meaning Until a Type Gives Them One](#1-the-core-idea-bits-have-no-meaning-until-a-type-gives-them-one)
2. [Why Negative Numbers Are a Problem](#2-why-negative-numbers-are-a-problem)
3. [Three Ways People Tried to Store Negatives](#3-three-ways-people-tried-to-store-negatives)
4. [Two's Complement: The Method Java Uses](#4-twos-complement-the-method-java-uses)
5. [Encoding and Decoding, Step by Step](#5-encoding-and-decoding-step-by-step)
6. [Why the Range Is Uneven (−128 to 127)](#6-why-the-range-is-uneven-128-to-127)
7. [Overflow: The Odometer Effect](#7-overflow-the-odometer-effect)
8. [Sign Extension and Casting](#8-sign-extension-and-casting)
9. [Bitwise Operators and Negative Numbers](#9-bitwise-operators-and-negative-numbers)
10. [Division and Remainder With Negatives](#10-division-and-remainder-with-negatives)
11. [Unsigned Numbers in Java](#11-unsigned-numbers-in-java)
12. [See It Yourself: Java Code for Integers](#12-see-it-yourself-java-code-for-integers)

**Part B: Decimal numbers (floating-point)**

13. [The Problem: Fractions in Binary](#13-the-problem-fractions-in-binary)
14. [The Big Idea: Binary Scientific Notation](#14-the-big-idea-binary-scientific-notation)
15. [The IEEE 754 Layout (float and double)](#15-the-ieee-754-layout-float-and-double)
16. [Worked Example: Storing 5.75 as a float](#16-worked-example-storing-575-as-a-float)
17. [Negative Decimal Numbers](#17-negative-decimal-numbers)
18. [Why 0.1 Cannot Be Stored Exactly](#18-why-01-cannot-be-stored-exactly)
19. [Special Values: Zero, Infinity, NaN, Subnormals](#19-special-values-zero-infinity-nan-subnormals)
20. [Range and Precision: Where the Numbers Come From](#20-range-and-precision-where-the-numbers-come-from)
21. [Gaps Between Numbers and Rounding](#21-gaps-between-numbers-and-rounding)
22. [Casting Between Decimals and Integers](#22-casting-between-decimals-and-integers)
23. [See It Yourself: Java Code for Floating-Point](#23-see-it-yourself-java-code-for-floating-point)
24. [Final Comparison, Summary and Quiz](#24-final-comparison-summary-and-quiz)

---

# PART A: NEGATIVE NUMBERS (INTEGERS)

---

## 1. The Core Idea: Bits Have No Meaning Until a Type Gives Them One

Memory holds only patterns of bits. The **data type** decides how to read them.

```
Same 8 bits:   1 1 1 1 1 1 1 1

  read as unsigned 8-bit number  →  255
  read as Java byte (signed)     →  -1
  read as part of a character    →  some symbol
```

So the question "how is -5 stored?" really means: **which bit pattern did we agree means -5?**

Java's answer for `byte`, `short`, `int` and `long` is **two's complement**.

---

## 2. Why Negative Numbers Are a Problem

Positive numbers are easy. `5` is just `00000101`. But we have no room for a `-` symbol, so we need a **rule** that turns some bit patterns into negative numbers.

A good rule should satisfy these goals:

1. Every number has **exactly one** pattern (only one zero).
2. **Addition and subtraction** should work with the **same simple hardware**, without special cases.
3. It should be **easy to tell** whether a number is negative.

Let's look at the options people tried.

---

## 3. Three Ways People Tried to Store Negatives

We will use **4 bits** to keep the tables small. The ideas are the same for 8, 16, 32 or 64 bits.

### 3.1 Sign-Magnitude (the "obvious" idea)

Use the first bit as the sign (0 = plus, 1 = minus). The remaining bits hold the size.

```
+5 =  0 101
-5 =  1 101
```

Problems:

- **Two zeros:** `0000` (+0) and `1000` (−0). Wasteful and confusing.
- Adding needs extra logic: you must compare signs and sizes first, then decide whether to add or subtract.

### 3.2 One's Complement

To make a negative number, **flip every bit** of the positive number.

```
+5 = 0101
-5 = 1010
```

Problems:

- **Still two zeros:** `0000` and `1111`.
- Addition needs a special "end-around carry" fix.

### 3.3 Two's Complement (the winner)

To make a negative number, **flip every bit and then add 1**.

```
+5 = 0101
flip → 1010
add 1 → 1011   ← this is -5
```

Advantages:

- **Only one zero.**
- **Addition works with no special cases.** The same adder circuit handles positive and negative numbers.
- Subtraction is just addition of the negative.
- The sign is simply the **leftmost bit**.

That is why **virtually every modern computer and language (including Java) uses two's complement.**

### 3.4 Comparison in one table (4-bit)

| Pattern | Sign-magnitude | One's complement | Two's complement |
|---------|---------------|------------------|------------------|
| `0000` | +0 | +0 | **0** |
| `0001` | 1 | 1 | **1** |
| `0111` | 7 | 7 | **7** |
| `1000` | −0 | −7 | **−8** |
| `1001` | −1 | −6 | **−7** |
| `1010` | −2 | −5 | **−6** |
| `1011` | −3 | −4 | **−5** |
| `1100` | −4 | −3 | **−4** |
| `1101` | −5 | −2 | **−3** |
| `1110` | −6 | −1 | **−2** |
| `1111` | −7 | −0 | **−1** |

Notice how the two other systems waste one pattern on a second zero, while two's complement uses it for one extra number (−8).

---

## 4. Two's Complement: The Method Java Uses

There are **two equivalent ways** to understand it. Learn both; each makes a different thing clear.

### 4.1 View 1: The leftmost bit has a NEGATIVE weight

In an ordinary unsigned 8-bit number, the place values are:

```
128   64   32   16    8    4    2    1
```

In **two's complement**, only the **leftmost** place value changes sign:

```
-128  +64  +32  +16   +8   +4   +2   +1
```

To read any pattern, add up the place values wherever there is a `1`.

**Examples (8 bits):**

| Binary | Calculation | Value |
|--------|-------------|-------|
| `0000 0101` | 4 + 1 | **5** |
| `0111 1111` | 64+32+16+8+4+2+1 | **127** |
| `1000 0000` | −128 | **−128** |
| `1000 0001` | −128 + 1 | **−127** |
| `1111 1011` | −128 + 64+32+16+8 + 2+1 = −128 + 123 | **−5** |
| `1111 1111` | −128 + 127 | **−1** |

**Quick rules from this view:**

- Leftmost bit **0** → number is **zero or positive** (0 to 127).
- Leftmost bit **1** → number is **negative** (−128 to −1).
- The leftmost bit is called the **sign bit**.

### 4.2 View 2: A circle, like a clock

Because computer numbers have a fixed number of bits, counting **wraps around**. Picture all 16 patterns of 4 bits around a circle:

```
                    0000 (0)
              1111 (-1)    0001 (1)
          1110 (-2)            0010 (2)
        1101 (-3)                0011 (3)
        1100 (-4)                0100 (4)
        1011 (-5)                0101 (5)
          1010 (-6)            0110 (6)
              1001 (-7)    0111 (7)
                    1000 (-8)
```

Going one step **clockwise** adds 1. Going one step **counter-clockwise** subtracts 1. Just before 0 (counter-clockwise) comes **−1**, and just before that **−2**, and so on. Negative numbers are simply "the numbers behind zero on the circle".

This is why `0000 − 1` becomes `1111` (which means −1). The circle has no edge; it just wraps.

### 4.3 The formula

For **N bits**, the negative of a number `x` is stored as the pattern for:

```
2^N − x
```

Example, `-5` in 8 bits: `2^8 − 5 = 256 − 5 = 251`, and 251 in binary is `1111 1011`. The same pattern we got earlier.

That is where the name comes from: in N bits, a number and its negative **complement each other to 2^N**.

---

## 5. Encoding and Decoding, Step by Step

### 5.1 Encoding: write a negative number in binary

**Recipe: write the positive number, flip every bit, add 1.**

**Example: store `-5` in a `byte` (8 bits)**

```
Step 1: write +5          →  0000 0101
Step 2: flip all bits     →  1111 1010
Step 3: add 1             →  1111 1011      ← this is -5
```

**Example: store `-100` in a `byte`**

```
+100         →  0110 0100
flip         →  1001 1011
add 1        →  1001 1100       ← this is -100
```

Check using View 1: −128 + 16 + 8 + 4 = **−100** ✓

**Example: `-1` in a 32-bit `int`**

```
+1           →  0000 0000 0000 0000 0000 0000 0000 0001
flip         →  1111 1111 1111 1111 1111 1111 1111 1110
add 1        →  1111 1111 1111 1111 1111 1111 1111 1111
```

So **-1 is always "all ones"** in any size. In hex: `0xFF` (byte), `0xFFFF` (short), `0xFFFFFFFF` (int).

### 5.2 Decoding: read a pattern whose leftmost bit is 1

**Method A:** use the weights (View 1). Leftmost bit counts as −2^(N−1).

**Method B:** flip and add 1 again (it reverses itself), then put a minus sign.

Pattern `1111 0110`:

```
flip       →  0000 1001
add 1      →  0000 1010   = 10
so the number is -10
```

Check with Method A, writing the place values under the bits:

```
Bits:          1     1    1    1    0    1    1    0
Place value: -128   +64  +32  +16   +8   +4   +2   +1

-128 + 64 + 32 + 16 + 0 + 4 + 2 + 0  =  -128 + 118  =  -10  ✓
```

> Tip: always write out the place values under the bits so you don't miscount. Only add the place values where the bit is 1.

### 5.3 Why "flip + 1" works (the intuition)

If you add a number and its **flipped** version, every position has one 0 and one 1, so the result is **all ones**:

```
  0000 0101   (5)
+ 1111 1010   (flipped 5)
-----------
  1111 1111   (all ones = -1)
```

So `5 + flipped(5) = -1`. Add 1 to both sides: `5 + (flipped(5) + 1) = 0`. Therefore `flipped(5) + 1` is `-5`. 

### 5.4 Adding with two's complement just works

Calculate `5 + (−3)` in 8 bits:

```
   0000 0101     (5)
 + 1111 1101     (-3)
 ------------
 1 0000 0010     → the 9th bit falls off the end, so 0000 0010 = 2   ✓
```

And `5 + (−5)`:

```
   0000 0101
 + 1111 1011
 ------------
 1 0000 0000     → drop the extra bit → 0 ✓
```

The CPU used the **same adder** with no special handling for negatives. That is the whole magic of two's complement.

---

## 6. Why the Range Is Uneven (−128 to 127)

For 8 bits, there are 256 patterns:

```
Leftmost bit 0 → 128 patterns → 0 to 127       (zero uses one of them)
Leftmost bit 1 → 128 patterns → -128 to -1
```

- The positive side loses one slot to **zero**, so it stops at **127**.
- The negative side has no zero, so it goes one further, to **−128**.

**−128 has no positive twin.** There is no `+128` in a signed byte.

### The general formula (N bits)

```
smallest = −2^(N−1)
largest  =  2^(N−1) − 1
```

| Type | Bits | Smallest | Largest |
|------|------|----------|---------|
| `byte` | 8 | −128 | 127 |
| `short` | 16 | −32,768 | 32,767 |
| `int` | 32 | −2,147,483,648 | 2,147,483,647 |
| `long` | 64 | −9,223,372,036,854,775,808 | 9,223,372,036,854,775,807 |

### A famous side effect

The smallest number cannot be negated, because its positive version doesn't fit:

```java
int min = Integer.MIN_VALUE;          // -2147483648
System.out.println(-min);             // -2147483648  (still negative!)
System.out.println(Math.abs(min));    // -2147483648  (abs fails too!)
```

Let's see why with a byte: `-(-128)` → flip `1000 0000` to `0111 1111`, add 1 → `1000 0000`. We get back **−128**. No room for +128.

---

## 7. Overflow: The Odometer Effect

When a result goes past the largest or smallest value, it **wraps around** (like a car odometer rolling from 999999 to 000000). Java does **not** throw an error for this.

```java
byte b = 127;
b++;
System.out.println(b);              // -128

int max = Integer.MAX_VALUE;
System.out.println(max + 1);        // -2147483648

int min = Integer.MIN_VALUE;
System.out.println(min - 1);        //  2147483647
```

Why it happens, in bits (byte):

```
  0111 1111   (127)
+ 0000 0001   (1)
-----------
  1000 0000   (-128)    ← sign bit flipped on
```

On the circle diagram, you stepped off the end of the positive side right into the negative side.

### How to catch overflow

```java
int result = Math.addExact(Integer.MAX_VALUE, 1);   // throws ArithmeticException
long safe = (long) a + b;                           // do the math in a wider type
```

Methods available: `addExact`, `subtractExact`, `multiplyExact`, `negateExact`, `toIntExact`.

---

## 8. Sign Extension and Casting

### 8.1 Widening: small → big (sign extension)

When Java converts a smaller signed type into a larger one, it **copies the sign bit** into all the new left-hand bits. This keeps the *value* the same.

```java
byte b = -5;      // 1111 1011
int i = b;        // 1111 1111 1111 1111 1111 1111 1111 1011  (still -5)
```

```
byte  -5 :                                   1111 1011
int   -5 :  1111 1111 1111 1111 1111 1111  1111 1011
            └──────── copies of sign bit ──────┘
```

A positive number gets zeros on the left, a negative number gets ones.

### 8.2 Narrowing: big → small (chops off the left bits)

Casting to a smaller type just **keeps the lowest bits** and throws away the rest. The value can change completely.

```java
int x = 130;           // 0000 ... 1000 0010
byte b = (byte) x;     // keep last 8 bits: 1000 0010  →  -126
```

Check: `1000 0010` = −128 + 2 = **−126** ✓

```java
int y = 200;
byte c = (byte) y;     // 1100 1000 → -56
```

### 8.3 Getting the unsigned value of a byte: `& 0xFF`

When a negative byte is widened to `int`, it keeps its negative value. If you want the "0 to 255" view, mask off the extra bits:

```java
byte b = (byte) 200;        // -56
int signed = b;             // -56   (sign extended)
int unsigned = b & 0xFF;    // 200   (keep only the lowest 8 bits)
```

```
  1111 1111 1111 1111 1111 1111 1100 1000   (-56 as int)
& 0000 0000 0000 0000 0000 0000 1111 1111   (0xFF)
-----------------------------------------
  0000 0000 0000 0000 0000 0000 1100 1000   (200)
```

### 8.4 `char` is different: it is unsigned

`char` is 16 bits and **never negative**. Casting a negative number to `char` gives a big positive number:

```java
char c = (char) -1;
System.out.println((int) c);    // 65535   (1111 1111 1111 1111)
```

And widening `char` → `int` uses **zero extension** (fills with zeros), not sign extension.

---

## 9. Bitwise Operators and Negative Numbers

### 9.1 Bitwise NOT `~` (flip every bit)

```java
~5       // -6
~0       // -1
~(-1)    // 0
```

Since "negate" is "flip + 1", we get this beautiful identity:

```
~x  =  -x - 1
```

Example: `~5 = -5 - 1 = -6` ✓. In 4 bits: 5 is `0101`, flipped is `1010` = −8 + 2 = −6 ✓.

### 9.2 Shift operators

| Operator | Name | What it does |
|----------|------|--------------|
| `<<` | Left shift | Moves bits left, fills with 0. Same as multiply by 2 each step. |
| `>>` | **Signed (arithmetic)** right shift | Moves bits right, **fills with the sign bit**. Keeps the number's sign. Same as divide by 2 (rounds toward negative infinity). |
| `>>>` | **Unsigned (logical)** right shift | Moves bits right, **always fills with 0**. Result can turn a negative number into a big positive one. |

```java
int a = -16;                 // 1111 ... 1111 0000

System.out.println(a >> 2);  // -4     (sign bit copied in, still negative)
System.out.println(a >>> 28);// 15     (zeros come in: 0000 ... 1111)
System.out.println(-1 >>> 28);// 15
System.out.println(-8 >> 1); // -4
System.out.println(-1 >> 5); // -1     (all ones stay all ones)
```

Visual of `-16 >> 2` (using 8 bits to keep it short):

```
 1111 0000   (-16)
 >> 2
 1111 1100   (-4)    ← two copies of the sign bit (1) were shifted in on the left
```

Visual of `-16 >>> 2` on 8 bits (conceptually):

```
 1111 0000
 >>> 2
 0011 1100   ← zeros shifted in on the left
```

(In real Java, `byte` is promoted to `int` before shifting, so you get the 32-bit result shown in the code above.)

### 9.3 AND, OR, XOR with negatives

They work bit by bit on the full two's complement pattern. The classic use is masking:

```java
int n = -5;                  // ...1111 1011
System.out.println(n & 1);   // 1   (lowest bit is 1 → odd number)
System.out.println(n & 0xF); // 11  (1011 → lowest 4 bits)
```

Fun trick: `(n & 1) == 1` correctly detects odd numbers even for negatives, while `n % 2 == 1` fails for negatives (see the next section).

---

## 10. Division and Remainder With Negatives

Java's integer division **truncates toward zero** (it chops off the fraction; it does not round down).

```java
System.out.println(7 / 2);     //  3
System.out.println(-7 / 2);    // -3    (not -4!)
System.out.println(7 / -2);    // -3
System.out.println(-7 / -2);   //  3
```

The remainder `%` gets the **sign of the left operand** (the dividend). The rule that always holds is:

```
(a / b) * b + (a % b) == a
```

```java
System.out.println(7 % 3);     //  1
System.out.println(-7 % 3);    // -1
System.out.println(7 % -3);    //  1
System.out.println(-7 % -3);   // -1
```

That is why `n % 2 == 1` is wrong for negative odd numbers: `-3 % 2` is **-1**, not 1. Use `n % 2 != 0`, or `(n & 1) == 1`.

If you want math-style results (always non-negative remainder for positive divisor), use:

```java
Math.floorDiv(-7, 2);   // -4
Math.floorMod(-7, 2);   //  1
```

(Handy for things like wrapping array indexes: `Math.floorMod(index, length)`.)

---

## 11. Unsigned Numbers in Java

Java has **no unsigned `int`, `long`, `short` or `byte` types** (only `char` is unsigned). But since Java 8, helper methods let you treat signed values **as if** they were unsigned:

```java
int x = -1;                                    // 0xFFFFFFFF
System.out.println(Integer.toUnsignedString(x));        // 4294967295
System.out.println(Integer.toUnsignedLong(x));          // 4294967295
System.out.println(Integer.parseUnsignedInt("4294967295")); // -1 (same bits)
System.out.println(Integer.compareUnsigned(-1, 1));     // positive: -1 is "bigger" as unsigned
System.out.println(Integer.divideUnsigned(-1, 2));      // 2147483647

byte b = (byte) 200;
System.out.println(Byte.toUnsignedInt(b));              // 200
```

The **bits stay the same**; only the way you *interpret* them changes.

---

## 12. See It Yourself: Java Code for Integers

Copy this into a file named `NegativeDemo.java` and run it.

```java
public class NegativeDemo {

    // Print a number as full 32-bit binary with leading zeros
    static String bits32(int n) {
        String s = Integer.toBinaryString(n);
        return String.format("%32s", s).replace(' ', '0');
    }

    // Print a byte as 8 bits
    static String bits8(byte n) {
        return String.format("%8s", Integer.toBinaryString(n & 0xFF)).replace(' ', '0');
    }

    public static void main(String[] args) {
        System.out.println("5   as int : " + bits32(5));
        System.out.println("-5  as int : " + bits32(-5));
        System.out.println("-1  as int : " + bits32(-1));
        System.out.println("MIN as int : " + bits32(Integer.MIN_VALUE));
        System.out.println("MAX as int : " + bits32(Integer.MAX_VALUE));

        System.out.println();
        System.out.println("byte  5 : " + bits8((byte) 5));
        System.out.println("byte -5 : " + bits8((byte) -5));
        System.out.println("byte 127: " + bits8((byte) 127));
        System.out.println("byte -128: " + bits8((byte) -128));

        System.out.println();
        System.out.println("Hex of -1 : " + Integer.toHexString(-1));       // ffffffff
        System.out.println("Hex of -5 : " + Integer.toHexString(-5));       // fffffffb

        System.out.println();
        System.out.println("~5   = " + (~5));                               // -6
        System.out.println("-16 >>  2 = " + (-16 >> 2));                   // -4
        System.out.println("-16 >>> 28 = " + (-16 >>> 28));                // 15

        System.out.println();
        byte b = 127;
        b++;
        System.out.println("127 + 1 as byte = " + b);                      // -128
        System.out.println("MAX + 1 as int  = " + (Integer.MAX_VALUE + 1));// -2147483648
        System.out.println("abs(MIN)        = " + Math.abs(Integer.MIN_VALUE));

        System.out.println();
        System.out.println("-7 / 2 = " + (-7 / 2) + ",  -7 % 2 = " + (-7 % 2));
        System.out.println("floorDiv = " + Math.floorDiv(-7, 2) + ",  floorMod = " + Math.floorMod(-7, 2));
    }
}
```

Run it:

```
javac NegativeDemo.java
java NegativeDemo
```

---

# PART B: DECIMAL NUMBERS (FLOATING-POINT)

---

## 13. The Problem: Fractions in Binary

Integers have place values that are powers of 2 going **left**: 1, 2, 4, 8, 16...

Fractions simply continue the pattern going **right** of the point, using **negative powers of 2**:

```
 Place:   8    4    2    1  .  1/2   1/4   1/8   1/16
 Value:   8    4    2    1  .  0.5   0.25  0.125 0.0625
```

So a binary number with a point works like this:

```
101.11 (binary)  =  4 + 0 + 1 + 0.5 + 0.25  =  5.75 (decimal)
```

### 13.1 Converting a decimal fraction to binary

**Integer part:** divide by 2 repeatedly (as in the earlier guide).
**Fraction part:** **multiply by 2 repeatedly**; the digit before the point each time is the next binary digit.

Convert `0.75`:

```
0.75 × 2 = 1.5   → digit 1   (keep 0.5)
0.5  × 2 = 1.0   → digit 1   (keep 0.0, done)

0.75 = 0.11 (binary)
```

So `5.75` = `101` + `.` + `11` = `101.11` ✓.

### 13.2 Why not just keep a fixed number of bits after the point?

That idea is called **fixed-point**. Suppose we give 16 bits to the left and 16 to the right. Then:

- The biggest number is only about 65,536 (too small for science).
- The smallest step is about 0.000015 (too coarse for tiny values).

We cannot store both **huge** (like the number of atoms in the universe) **and tiny** (like the mass of an electron) with one fixed split. We need something smarter.

---

## 14. The Big Idea: Binary Scientific Notation

In normal life we write huge/tiny numbers like this:

```
602,200,000,000,000,000,000,000  =  6.022 × 10^23
0.00000000012                    =  1.2   × 10^-10
```

We store **just the important digits** (6.022) and **how far the point moves** (23). The point "floats". That is the meaning of **floating-point**.

Computers do exactly the same in **binary**:

```
number  =  ± 1.xxxxx (binary)  ×  2^exponent
```

Example: `101.11` (binary) = `1.0111 × 2^2` (we moved the point 2 places left, so the exponent is 2).

So three things must be stored:

1. **Sign**: positive or negative.
2. **Exponent**: how far the point moved.
3. **Mantissa (also called significand or fraction)**: the digits after the leading `1.`

---

## 15. The IEEE 754 Layout (float and double)

Java uses the world standard **IEEE 754** for `float` and `double`.

```
float  (32 bits):
┌───┬──────────────┬───────────────────────────────┐
│ S │  Exponent    │          Mantissa             │
│1  │   8 bits     │          23 bits              │
└───┴──────────────┴───────────────────────────────┘
 bit31  bits 30–23         bits 22–0

double (64 bits):
┌───┬─────────────────┬────────────────────────────────────────────┐
│ S │    Exponent     │                 Mantissa                   │
│1  │    11 bits      │                 52 bits                    │
└───┴─────────────────┴────────────────────────────────────────────┘
 bit63  bits 62–52             bits 51–0
```

| Part | float | double | Job |
|------|-------|--------|-----|
| **Sign (S)** | 1 bit | 1 bit | `0` = positive, `1` = negative |
| **Exponent (E)** | 8 bits | 11 bits | The power of 2 (stored with a "bias") |
| **Mantissa (M)** | 23 bits | 52 bits | The digits after the leading 1 |
| **Bias** | 127 | 1023 | Added to the real exponent before storing |

### 15.1 The formula

For a normal number:

```
value = (−1)^S  ×  1.M  ×  2^(E − bias)
```

### 15.2 Two clever tricks

**Trick 1: The hidden leading 1.**
Every normalized binary number starts with `1.`, always. So there's no point storing that `1`. It is **implied** (hidden). This gives one **free extra bit of precision**:

- `float` stores 23 mantissa bits but has **24 bits of precision**.
- `double` stores 52 mantissa bits but has **53 bits of precision**.

**Trick 2: The biased exponent.**
The exponent must be able to be negative (for tiny numbers like 2^−10). But instead of using two's complement for the exponent, IEEE 754 stores **real exponent + bias** as a plain positive number.

```
float : stored E = real exponent + 127     (E from 1 to 254 for normal numbers)
double: stored E = real exponent + 1023    (E from 1 to 2046)
```

Why? It makes comparing two positive floats as easy as comparing them as integers, and it keeps all exponent values positive.

---

## 16. Worked Example: Storing 5.75 as a float

**Goal:** find the 32 bits of `5.75f`.

**Step 1: Convert to binary.**
```
5    = 101
0.75 = 0.11
5.75 = 101.11
```

**Step 2: Normalize** (move the point so there is a single `1` before it).
```
101.11  =  1.0111 × 2^2
```
The point moved 2 places to the left, so **real exponent = 2**.

**Step 3: Sign bit.**
The number is positive → **S = 0**.

**Step 4: Biased exponent.**
```
stored E = 2 + 127 = 129 = 1000 0001 (binary)
```

**Step 5: Mantissa.**
Take the bits after the leading `1.`: `0111`. Pad with zeros to 23 bits:
```
M = 0111 0000 0000 0000 0000 000
```

**Step 6: Put it together.**

```
 S   Exponent     Mantissa
 0   10000001     01110000000000000000000

Full 32 bits: 0 10000001 01110000000000000000000
Grouped by 4: 0100 0000 1011 1000 0000 0000 0000 0000
Hex:            4    0    B    8    0    0    0    0   →  0x40B80000
```

**Check in Java:**

```java
System.out.println(Integer.toHexString(Float.floatToIntBits(5.75f)));  // 40b80000
```

### Decoding the other way: what number is 0x41200000?

```
0x41200000 = 0100 0001 0010 0000 0000 0000 0000 0000

S = 0
E = 1000 0010 = 130  →  real exponent = 130 − 127 = 3
M = 010 0000 ...     →  1.M = 1.01 (binary) = 1.25

value = 1.25 × 2^3 = 1.25 × 8 = 10.0
```

So `0x41200000` is the float `10.0` ✓.

---

## 17. Negative Decimal Numbers

This is the key link between Part A and Part B:

> **Negative decimals do NOT use two's complement.**
> For `float` and `double`, the sign is **its own separate bit**. This is the **sign-magnitude** idea we rejected for integers!

To store `-5.75f`, take the bits of `+5.75f` and **flip only the sign bit**:

```
+5.75f :  0 10000001 01110000000000000000000   = 0x40B80000
-5.75f :  1 10000001 01110000000000000000000   = 0xC0B80000
          ↑ only this bit changed
```

Why can floating-point afford sign-magnitude when integers couldn't?

- The extra "negative zero" is actually **useful** in floating-point (it records which direction a tiny result came from).
- Floating-point hardware is built differently from the integer adder anyway.
- It makes negation trivial: flip one bit.

```java
System.out.println(Integer.toHexString(Float.floatToIntBits(-5.75f)));  // c0b80000
```

### Two zeros exist

```java
System.out.println(0.0 == -0.0);                      // true  (they compare equal)
System.out.println(Double.valueOf(0.0).equals(-0.0)); // false (different bit patterns)
System.out.println(1 / 0.0);                          //  Infinity
System.out.println(1 / -0.0);                         // -Infinity
```

---

## 18. Why 0.1 Cannot Be Stored Exactly

Let's convert `0.1` to binary with the multiply-by-2 method:

```
0.1 × 2 = 0.2   → 0
0.2 × 2 = 0.4   → 0
0.4 × 2 = 0.8   → 0
0.8 × 2 = 1.6   → 1   (keep 0.6)
0.6 × 2 = 1.2   → 1   (keep 0.2)
0.2 × 2 = 0.4   → 0   ← repeats from here!
0.4 × 2 = 0.8   → 0
0.8 × 2 = 1.6   → 1
0.6 × 2 = 1.2   → 1
... forever
```

So:

```
0.1 (decimal) = 0.0001 1001 1001 1001 1001 ... (binary, repeating "0011" forever)
```

Just like **1/3 = 0.33333...** can't be written exactly in decimal, **1/10 can't be written exactly in binary**. We only have 23 (float) or 52 (double) mantissa bits, so the repeating pattern gets **cut off and rounded**.

```java
System.out.println(Integer.toHexString(Float.floatToIntBits(0.1f)));        // 3dcccccd
System.out.println(Long.toHexString(Double.doubleToLongBits(0.1)));         // 3fb999999999999a
```

Notice the repeating `cccc` and `9999`, which is the repeating pattern, with the last digit rounded up (`d` and `a`).

### What is actually stored for 0.1?

```java
System.out.println(new java.math.BigDecimal(0.1));
// 0.1000000000000000055511151231257827021181583404541015625
```

It's very close to 0.1, but not exactly 0.1.

### Why 0.1 + 0.2 ≠ 0.3

```
0.1 (stored) ≈ 0.1000000000000000055511
0.2 (stored) ≈ 0.2000000000000000111022
sum          ≈ 0.3000000000000000166533   → rounded to the nearest double

0.3 (stored) ≈ 0.2999999999999999888978
```

The rounded sum lands on a different double than the one stored for `0.3`:

```java
System.out.println(0.1 + 0.2);          // 0.30000000000000004
System.out.println(0.1 + 0.2 == 0.3);   // false
```

### Which decimals ARE exact?

Numbers that are a **sum of powers of 2** (halves, quarters, eighths...) are exact:

| Exact in binary | Not exact in binary |
|-----------------|---------------------|
| 0.5, 0.25, 0.75, 0.125, 0.375 | 0.1, 0.2, 0.3, 0.7, 3.14 |
| Any integer (up to the precision limit) | Most "nice" decimal fractions |

### The correct ways to handle this

```java
// 1) Compare with a tolerance
double a = 0.1 + 0.2, b = 0.3;
boolean same = Math.abs(a - b) < 1e-9;       // true

// 2) For money and exact decimals, use BigDecimal (build from Strings!)
BigDecimal x = new BigDecimal("0.1");
BigDecimal y = new BigDecimal("0.2");
System.out.println(x.add(y));                // 0.3

// 3) Or store whole cents in a long
long cents = 1999;   // $19.99
```

---

## 19. Special Values: Zero, Infinity, NaN, Subnormals

The exponent field has two "reserved" patterns that give special meaning.

| Exponent bits | Mantissa bits | Meaning |
|---------------|---------------|---------|
| All **0** | All 0 | **Zero** (+0.0 or −0.0, depending on sign) |
| All **0** | Not all 0 | **Subnormal** (tiny numbers closer to zero than the smallest normal number) |
| 1 to max−1 | anything | **Normal number** (uses the hidden leading 1) |
| All **1** | All 0 | **Infinity** (+∞ or −∞) |
| All **1** | Not all 0 | **NaN** (Not a Number) |

For `float`: exponent all 0 = `0000 0000`, all 1 = `1111 1111` (255).
For `double`: exponent all 0 = 11 zeros, all 1 = 11 ones (2047).

```
+Infinity (float):  0 11111111 00000000000000000000000   = 0x7F800000
-Infinity (float):  1 11111111 00000000000000000000000   = 0xFF800000
NaN       (float):  0 11111111 10000000000000000000000   = 0x7FC00000
+0.0      (float):  0 00000000 00000000000000000000000   = 0x00000000
-0.0      (float):  1 00000000 00000000000000000000000   = 0x80000000
```

### Where they come from

```java
System.out.println(1.0 / 0);          // Infinity
System.out.println(-1.0 / 0);         // -Infinity
System.out.println(0.0 / 0);          // NaN
System.out.println(Math.sqrt(-1));    // NaN
System.out.println(Double.MAX_VALUE * 2);   // Infinity (overflow)
```

Unlike integers, floating-point division by zero does **not** throw an exception.

### NaN is weird

```java
double n = Double.NaN;
System.out.println(n == n);               // false  (NaN is not equal to anything, even itself)
System.out.println(Double.isNaN(n));      // true   ← correct way to check
```

### Subnormal numbers (gradual underflow)

Without them there would be a big empty gap between 0 and the smallest normal number. Subnormals (exponent all zeros, **no hidden 1**) fill that gap so numbers fade smoothly toward zero. They have **less precision** as they get smaller.

---

## 20. Range and Precision: Where the Numbers Come From

You learned `float` ≈ ±3.4×10³⁸ and `double` ≈ ±1.8×10³⁰⁸. Now let's see **why**.

### 20.1 Range comes from the exponent bits

**float:**
- Largest normal exponent stored = 254 → real exponent = 254 − 127 = **127**.
- Largest mantissa = all ones → `1.111...1` (almost 2).
- Max value ≈ (2 − 2⁻²³) × 2¹²⁷ ≈ **3.4028235 × 10³⁸**.
- Smallest **normal** positive = 2⁻¹²⁶ ≈ **1.17549435 × 10⁻³⁸**.
- Smallest **subnormal** positive = 2⁻¹⁴⁹ ≈ **1.4 × 10⁻⁴⁵**.

**double:**
- Real exponent up to 1023 → max ≈ **1.7976931348623157 × 10³⁰⁸**.
- Smallest **normal** positive = 2⁻¹⁰²² ≈ **2.2250738585072014 × 10⁻³⁰⁸**.
- Smallest **subnormal** positive = 2⁻¹⁰⁷⁴ ≈ **4.9 × 10⁻³²⁴**.

### 20.2 Precision comes from the mantissa bits

```
float : 24 bits of precision → 24 × log10(2) ≈ 7.2 decimal digits  → "6 to 7 digits"
double: 53 bits of precision → 53 × log10(2) ≈ 15.95 decimal digits → "15 to 16 digits"
```

### 20.3 Summary table

| Property | float | double |
|----------|-------|--------|
| Total bits | 32 | 64 |
| Sign / Exponent / Mantissa | 1 / 8 / 23 | 1 / 11 / 52 |
| Exponent bias | 127 | 1023 |
| Precision (bits) | 24 | 53 |
| Decimal digits | about 7 | about 15 to 16 |
| Max value | 3.4028235E38 | 1.7976931348623157E308 |
| Smallest positive normal | 1.17549435E-38 | 2.2250738585072014E-308 |
| Smallest positive (subnormal) | 1.4E-45 | 4.9E-324 |

### 20.4 A famous trap: `MIN_VALUE`

For floating-point wrappers, `MIN_VALUE` is **NOT the most negative number**. It's the **smallest positive** number!

```java
System.out.println(Double.MIN_VALUE);     // 4.9E-324   (tiny positive!)
System.out.println(-Double.MAX_VALUE);    // -1.7976931348623157E308   (the real "most negative")
System.out.println(Integer.MIN_VALUE);    // -2147483648 (for integers MIN_VALUE IS the most negative)
```

---

## 21. Gaps Between Numbers and Rounding

Because the mantissa has a **fixed number of bits**, floating-point numbers are **not evenly spaced**. They are packed **densely near zero** and **spread out far from zero**.

```
 near 0:    | | | | | | | | |                       (very small gaps)
 near 1:    |   |   |   |   |                       (bigger gaps)
 near 1e9:  |        |        |        |            (much bigger gaps)
```

The gap between a number and the next representable one is called the **ULP** (Unit in the Last Place). Java gives it to you:

```java
System.out.println(Math.ulp(1.0));        // 2.220446049250313E-16
System.out.println(Math.ulp(1_000_000.0));// 1.1641532182693481E-10
System.out.println(Math.ulp(1e16));       // 2.0
System.out.println(Math.ulp(1.0f));       // 1.1920929E-7
```

### Real consequences

**A float can't count past 16,777,216 (2²⁴) by 1s:**

```java
float f = 16_777_216f;
System.out.println(f + 1);    // 1.6777216E7  (the +1 vanished: the gap here is 2)
System.out.println(f + 2);    // 1.6777218E7
```

**Adding a tiny number to a huge one loses the tiny one:**

```java
double big = 1e16;
System.out.println(big + 1 == big);   // true!
```

### Rounding rule

When a result isn't exactly representable, IEEE 754 rounds to the **nearest** representable value; if it's exactly halfway, it picks the one whose last bit is **even** ("round half to even"). That's why errors are tiny but not zero.

### Practical advice

- Don't compare with `==`. Use a tolerance (`Math.abs(a - b) < epsilon`).
- When adding many numbers of very different sizes, add the small ones first (or use `Math.fma`, Kahan summation, or `BigDecimal`).
- Never use `float`/`double` as loop counters or for money.

---

## 22. Casting Between Decimals and Integers

### 22.1 Decimal → integer: truncation toward zero

```java
(int) 3.99     //  3
(int) -3.99    // -3     (not -4: it cuts the fraction off)
Math.round(3.5);   // 4   (use this to round)
Math.floor(-3.5);  // -4.0
Math.ceil(-3.5);   // -3.0
```

### 22.2 Out-of-range and NaN

Java **saturates** instead of wrapping when converting a floating-point value to an integer:

```java
(int) 1e20           // 2147483647   (Integer.MAX_VALUE)
(int) -1e20          // -2147483648  (Integer.MIN_VALUE)
(int) Double.NaN     // 0
(long) Double.POSITIVE_INFINITY   // Long.MAX_VALUE
```

(Compare with integer narrowing, which wraps around.)

### 22.3 Integer → decimal: may lose precision

```java
int big = 123_456_789;
float f = big;
System.out.println((int) f);     // 123456792   (float has only 24 bits of precision)

long l = 9_007_199_254_740_993L; // 2^53 + 1
double d = l;
System.out.println((long) d);    // 9007199254740992   (double can't hold the +1)
```

So `int` fits fully into a `double` (32 < 53 bits), but `long` doesn't always fit into a `double`, and neither does `int` always fit into a `float`.

---

## 23. See It Yourself: Java Code for Floating-Point

Copy this into `FloatDemo.java`:

```java
import java.math.BigDecimal;

public class FloatDemo {

    // Show the three parts of a float
    static void showFloat(float x) {
        int bits = Float.floatToIntBits(x);
        int sign = bits >>> 31;
        int exp  = (bits >>> 23) & 0xFF;
        int mant = bits & 0x7FFFFF;

        String expBits  = String.format("%8s",  Integer.toBinaryString(exp)).replace(' ', '0');
        String mantBits = String.format("%23s", Integer.toBinaryString(mant)).replace(' ', '0');

        System.out.println(x + "f");
        System.out.println("  sign     = " + sign);
        System.out.println("  exponent = " + expBits + "  (stored " + exp + ", real " + (exp - 127) + ")");
        System.out.println("  mantissa = " + mantBits);
        System.out.println("  hex      = 0x" + Integer.toHexString(bits));
        System.out.println();
    }

    // Show the three parts of a double
    static void showDouble(double x) {
        long bits = Double.doubleToLongBits(x);
        int sign  = (int) (bits >>> 63);
        int exp   = (int) ((bits >>> 52) & 0x7FF);
        long mant = bits & 0xFFFFFFFFFFFFFL;

        String expBits  = String.format("%11s", Integer.toBinaryString(exp)).replace(' ', '0');
        String mantBits = String.format("%52s", Long.toBinaryString(mant)).replace(' ', '0');

        System.out.println(x);
        System.out.println("  sign     = " + sign);
        System.out.println("  exponent = " + expBits + "  (stored " + exp + ", real " + (exp - 1023) + ")");
        System.out.println("  mantissa = " + mantBits);
        System.out.println("  hex      = 0x" + Long.toHexString(bits));
        System.out.println();
    }

    public static void main(String[] args) {
        showFloat(5.75f);
        showFloat(-5.75f);
        showFloat(10.0f);
        showFloat(0.1f);
        showDouble(0.1);
        showDouble(1.0);

        System.out.println("0.1 + 0.2        = " + (0.1 + 0.2));
        System.out.println("0.1 + 0.2 == 0.3 = " + (0.1 + 0.2 == 0.3));
        System.out.println("exact 0.1        = " + new BigDecimal(0.1));
        System.out.println("BigDecimal sum   = " + new BigDecimal("0.1").add(new BigDecimal("0.2")));

        System.out.println();
        System.out.println("1.0 / 0   = " + (1.0 / 0));
        System.out.println("0.0 / 0   = " + (0.0 / 0));
        System.out.println("MIN_VALUE = " + Double.MIN_VALUE);
        System.out.println("float 16777216 + 1 = " + (16_777_216f + 1));
        System.out.println("(int) -3.99 = " + (int) -3.99);
        System.out.println("(int) 1e20  = " + (int) 1e20);
    }
}
```

Expected output for the first item (`5.75f`):

```
5.75f
  sign     = 0
  exponent = 10000001  (stored 129, real 2)
  mantissa = 01110000000000000000000
  hex      = 0x40b80000
```

---

## 24. Final Comparison, Summary and Quiz

### 24.1 Integers vs floating-point storage

| Feature | Integers (`byte`, `short`, `int`, `long`) | Floating-point (`float`, `double`) |
|---------|------------------------------------------|-------------------------------------|
| Standard | **Two's complement** | **IEEE 754** |
| Negative numbers | Two's complement (flip bits, add 1) | Separate **sign bit** (sign-magnitude) |
| Layout | One block of bits with a negative leftmost weight | Sign + exponent + mantissa |
| Exact? | **Exact** (within range) | **Approximate** (most decimals) |
| Zero | Only one | Two (+0.0 and −0.0) |
| Overflow | **Wraps around silently** | Becomes **Infinity** |
| Division by zero | `ArithmeticException` | `Infinity` or `NaN` |
| Spacing of numbers | Even (every integer) | Uneven (dense near 0, sparse far away) |
| Range (32-bit) | about ±2.1 billion | about ±3.4×10³⁸ (float) |
| Special values | None | Infinity, NaN, subnormals |

### 24.2 Summary in simple points

**Negative integers**

1. Memory holds only bits; the type decides the meaning.
2. Java integers use **two's complement**: the leftmost bit has weight **−2^(N−1)**; all other bits are positive.
3. To negate: **flip all bits, then add 1** (or compute 2^N − x).
4. There is **one zero**, and addition/subtraction use the **same hardware** for all signs.
5. Range is **−2^(N−1) to 2^(N−1) − 1**, so there is one more negative number than positive. `-MIN_VALUE` and `Math.abs(MIN_VALUE)` stay negative.
6. Overflow **wraps around** silently (127 + 1 = −128 for a byte).
7. Widening copies the sign bit (**sign extension**); narrowing chops the left bits. `b & 0xFF` gets the unsigned view.
8. `>>` keeps the sign, `>>>` fills with zeros. Division **truncates toward zero** and `%` takes the sign of the left operand.

**Decimal numbers**

1. They use **IEEE 754**: **sign bit + exponent + mantissa**, like binary scientific notation.
2. `value = (−1)^S × 1.M × 2^(E − bias)`; bias is **127** (float) and **1023** (double).
3. There is a **hidden leading 1**, giving 24 and 53 bits of precision.
4. **Negative decimals just flip the sign bit**; they do **not** use two's complement.
5. Many decimals (like **0.1**) are **repeating fractions in binary**, so they're stored **approximately**. That is why `0.1 + 0.2 != 0.3`.
6. Special values: **±0, ±Infinity, NaN, subnormals**.
7. Numbers are **unevenly spaced**; large values have large gaps (`ULP`).
8. For exact decimal math (money), use **`BigDecimal`** or whole cents in a `long`.

### 24.3 Mini Quiz

1. What is the 8-bit two's complement pattern for −5? Show the steps.
2. What does `1111 1111` mean in a signed byte? In an unsigned byte?
3. Why can a signed byte hold −128 but not +128?
4. What is `Math.abs(Integer.MIN_VALUE)` and why?
5. What is the difference between `>>` and `>>>` on a negative number?
6. What are `-7 / 2` and `-7 % 2` in Java?
7. Which bit pattern field stores the sign in a `double`, and how many bits are in the exponent and the mantissa?
8. Do negative `double` values use two's complement? How is −5.75 different from 5.75 in bits?
9. Why can't 0.1 be stored exactly in a `double`?
10. What is `Double.MIN_VALUE`, and what is the most negative double?

<details>
<summary>Click for answers</summary>

1. `+5 = 0000 0101` → flip = `1111 1010` → add 1 = **`1111 1011`**.
2. Signed byte: **−1**. Unsigned byte: **255**.
3. There are 256 patterns: 128 have the sign bit set (−128 to −1), and 128 have it clear (0 to 127, where zero uses up one slot). So the positive side stops at 127.
4. It returns **−2147483648** (still negative), because +2147483648 doesn't fit in an `int`, so the negation wraps back to the same pattern.
5. `>>` copies the sign bit into the left side (stays negative). `>>>` fills with zeros (the result becomes a large positive number).
6. `-7 / 2 = -3` (truncates toward zero) and `-7 % 2 = -1` (sign of the left operand).
7. The **leftmost bit** (bit 63). The exponent has **11** bits and the mantissa has **52** bits.
8. **No.** They use a separate sign bit. `-5.75` has exactly the same bits as `5.75` except the sign bit is `1` instead of `0`.
9. In binary, 0.1 is the repeating fraction `0.000110011001100...`. The mantissa has only 52 bits, so it's cut and rounded, leaving a tiny error.
10. `Double.MIN_VALUE` is the **smallest positive** double (about 4.9E-324). The most negative double is **`-Double.MAX_VALUE`**.

</details>

---

*Tip: the best way to make this stick is to run the two demo programs, change the numbers, and check your hand calculations against what Java prints.*
