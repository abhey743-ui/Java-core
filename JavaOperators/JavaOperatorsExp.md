# Java Operators — The Complete, Deep-Dive Notes

> Companion notes for **Java Full Course #6 (Coder Army)**: Arithmetic, Unary, Assignment, Relational, Logical, Bitwise, Shift, Ternary, Precedence.
> Special focus: **Bitwise operators** — what they are, how they work internally, where they are used, and the tricks that show up again and again in **DSA and interviews**.

---

## Table of Contents

1. [What is an operator?](#1-what-is-an-operator)
2. [Arithmetic operators](#2-arithmetic-operators)
3. [Unary operators (incl. pre/post increment)](#3-unary-operators)
4. [Assignment operators](#4-assignment-operators)
5. [Relational operators](#5-relational-operators)
6. [Logical operators and short-circuiting](#6-logical-operators)
7. [Number systems and two's complement (needed for bitwise)](#7-number-systems-and-twos-complement)
8. [Bitwise operators (deep dive)](#8-bitwise-operators)
9. [Shift operators](#9-shift-operators)
10. [Bit manipulation tricks and patterns for DSA](#10-bit-manipulation-tricks-for-dsa)
11. [Real-world uses of bitwise operators](#11-real-world-uses-of-bitwise-operators)
12. [Ternary operator](#12-ternary-operator)
13. [Operator precedence and associativity](#13-operator-precedence-and-associativity)
14. [Interview traps and edge cases](#14-interview-traps-and-edge-cases)
15. [Practice questions with answers](#15-practice-questions-with-answers)
16. [Final cheat sheet](#16-final-cheat-sheet)

---

## 1. What is an operator?

An **operator** is a symbol that tells the compiler to perform a specific operation on one or more values.
The values it works on are called **operands**.

```java
int result = a + b;
//           ^   ^ ^
//        operand | operand
//             operator
```

### Classification by number of operands (arity)

| Type | Operands | Examples |
|------|----------|----------|
| **Unary** | 1 | `++`, `--`, `!`, `~`, unary `+`, unary `-` |
| **Binary** | 2 | `+`, `-`, `*`, `/`, `%`, `&&`, `&`, `<<`, `==` |
| **Ternary** | 3 | `condition ? a : b` (the only one in Java) |

### Classification by purpose

| Category | Operators |
|----------|-----------|
| Arithmetic | `+  -  *  /  %` |
| Unary | `+  -  ++  --  !  ~` |
| Assignment | `=  +=  -=  *=  /=  %=  &=  |=  ^=  <<=  >>=  >>>=` |
| Relational | `==  !=  >  <  >=  <=  instanceof` |
| Logical | `&&  ||  !` |
| Bitwise | `&  |  ^  ~` |
| Shift | `<<  >>  >>>` |
| Ternary | `?:` |

> **Why care?** Operators control expression evaluation, conditions, memory-level manipulation, and bit-level logic. Weak operator knowledge leads to subtle bugs (overflow, wrong precedence, wrong increment order) — and they are favourite **interview trap** topics.

---

## 2. Arithmetic operators

| Operator | Name | Example | Result |
|----------|------|---------|--------|
| `+` | Addition (also string concat) | `7 + 2` | `9` |
| `-` | Subtraction | `7 - 2` | `5` |
| `*` | Multiplication | `7 * 2` | `14` |
| `/` | Division | `7 / 2` | `3` (int!) |
| `%` | Modulus (remainder) | `7 % 2` | `1` |

### 2.1 Integer division vs floating-point division

If **both** operands are integers, the result is an integer and the fractional part is **truncated (dropped, not rounded)**.

```java
System.out.println(7 / 2);        // 3
System.out.println(7 / 2.0);      // 3.5   (one operand is double -> double division)
System.out.println((double) 7 / 2);   // 3.5  (cast happens first, then division)
System.out.println((double) (7 / 2)); // 3.0  (division happens first -> 3, then cast)
System.out.println(-7 / 2);       // -3   (truncates toward ZERO, not toward -infinity)
```

**Rule:** Integer division truncates toward zero. So `-7 / 2 = -3`, **not** `-4`.

If you want floor division (round toward negative infinity): `Math.floorDiv(-7, 2) == -4`.

### 2.2 Division by zero

| Expression | Result |
|------------|--------|
| `5 / 0` (int) | throws `ArithmeticException: / by zero` |
| `5 % 0` (int) | throws `ArithmeticException` |
| `5.0 / 0` (double) | `Infinity` |
| `-5.0 / 0` | `-Infinity` |
| `0.0 / 0` | `NaN` |
| `5.0 % 0` | `NaN` |

### 2.3 The modulus operator `%`

`a % b` gives the remainder, with the guaranteed relation:

```
a == (a / b) * b + (a % b)
```

**The sign of the result follows the dividend (left operand)** in Java:

```java
System.out.println( 7 %  3);   //  1
System.out.println(-7 %  3);   // -1   (sign of -7)
System.out.println( 7 % -3);   //  1   (sign of 7)
System.out.println(-7 % -3);   // -1
```

`%` also works on floating-point numbers: `5.5 % 2 == 1.5`.

**Getting a non-negative modulus (very common in DSA, circular arrays, hashing):**

```java
int mod = ((a % m) + m) % m;      // always in [0, m-1]
int mod2 = Math.floorMod(a, m);   // built-in, same idea
```

#### Where `%` and `/` are used

| Use case | Code |
|----------|------|
| Even / odd check | `n % 2 == 0` (or faster to read in bits: `(n & 1) == 0`) |
| Last digit | `n % 10` |
| Remove last digit | `n / 10` |
| Circular array index | `next = (i + 1) % n` |
| Prevent overflow in DSA answers | `ans % 1_000_000_007` |
| Clock arithmetic | `(hour + 5) % 12` |

**Digit extraction loop (reverse a number / digit sum / palindrome):**

```java
int n = 1234, rev = 0;
while (n > 0) {
    int digit = n % 10;     // last digit
    rev = rev * 10 + digit; // build reversed number
    n /= 10;                // drop last digit
}
// rev = 4321
```

### 2.4 Type promotion in arithmetic (important!)

Before arithmetic happens, Java promotes operands:

1. `byte`, `short`, `char` are **always promoted to `int`**.
2. If either operand is `long` → result is `long`.
3. Else if either is `float` → `float`.
4. Else if either is `double` → `double`.

```java
byte a = 10, b = 20;
// byte c = a + b;        // COMPILE ERROR: a + b is an int
byte c = (byte) (a + b);  // OK with explicit cast
int  d = a + b;           // OK

short s = 5;
s = (short) (s * 2);      // needs cast
```

Hierarchy (widening is automatic, narrowing needs a cast):

```
byte -> short -> int -> long -> float -> double
        char  -> int
```

### 2.5 Integer overflow (silent!)

Java does **not** throw on overflow for `int`/`long`; it wraps around (two's complement).

```java
int max = Integer.MAX_VALUE;     //  2147483647
System.out.println(max + 1);     // -2147483648  (wrapped to MIN_VALUE)

int big = 1_000_000 * 1_000_000;       // overflow! = -727379968
long ok = 1_000_000L * 1_000_000;      // 1000000000000 (one operand is long)
```

**Trap:** `long x = 1_000_000 * 1_000_000;` still overflows, because the multiplication is done in `int` **before** being assigned to `long`. Put `L` on an operand.

Safe alternatives that throw instead of wrapping:

```java
Math.addExact(a, b);
Math.multiplyExact(a, b);
Math.toIntExact(someLong);
```

**Classic DSA bug — binary search midpoint:**

```java
int mid = (low + high) / 2;          // can overflow when low+high > 2^31 - 1
int mid = low + (high - low) / 2;    // safe
int mid = (low + high) >>> 1;        // safe (unsigned shift) - used inside the JDK
```

### 2.5.1 Two special overflow cases

```java
Integer.MIN_VALUE / -1      // overflows -> stays MIN_VALUE (no exception for int in Java)
Math.abs(Integer.MIN_VALUE) // still negative! -2147483648  (no positive counterpart)
-Integer.MIN_VALUE          // also MIN_VALUE
```

### 2.6 The `+` operator with Strings (concatenation)

If either operand of `+` is a `String`, it becomes concatenation. Evaluation is **left to right**:

```java
System.out.println(1 + 2 + "a");     // "3a"   -> (1+2) first, then + "a"
System.out.println("a" + 1 + 2);     // "a12"  -> "a"+1 = "a1", then +2
System.out.println("a" + (1 + 2));   // "a3"
System.out.println('a' + 1);         // 98     (char promoted to int)
System.out.println((char)('a' + 1)); // b
System.out.println('a' + 'b');       // 195    (int addition!)
System.out.println("" + 'a' + 'b');  // "ab"   (string concat)
```

### 2.7 Floating point precision

```java
System.out.println(0.1 + 0.2);            // 0.30000000000000004
System.out.println(0.1 + 0.2 == 0.3);     // false
// Compare with tolerance:
Math.abs((0.1 + 0.2) - 0.3) < 1e-9        // true
```

Use `BigDecimal` for money.

---

## 3. Unary operators

Unary operators work on a **single** operand.

| Operator | Meaning |
|----------|---------|
| `+x` | unary plus (promotes `byte/short/char` to `int`, rarely used) |
| `-x` | negation |
| `++x` | pre-increment |
| `x++` | post-increment |
| `--x` | pre-decrement |
| `x--` | post-decrement |
| `!b` | logical NOT (boolean only) |
| `~x` | bitwise NOT / complement (integral types only) |

### 3.1 Unary minus and plus

```java
int a = 5;
int b = -a;     // -5
int c = -(-a);  //  5
byte x = 3;
// byte y = -x;   // ERROR: -x is int
```

### 3.2 Increment / decrement — Pre vs Post (the heart of many interview questions)

| Form | What happens | Value of the expression |
|------|--------------|-------------------------|
| `++x` (pre) | **increment first**, then use | the **new** value |
| `x++` (post) | **use first**, then increment | the **old** value |

```java
int a = 5;
int b = a++;   // b = 5, a = 6   (old value used, then incremented)
int c = ++a;   // a = 7, c = 7   (incremented, then used)
```

Memory model of `x++` (what the JVM conceptually does):

```
temp = x;       // save old value
x = x + 1;      // increment variable
result = temp;  // expression value is the OLD value
```

Memory model of `++x`:

```
x = x + 1;
result = x;
```

### 3.3 Tricky examples (walk through each step!)

```java
int i = 5;
i = i++;
System.out.println(i);   // 5 !!
```
Why? `i++` returns old value 5 and sets i=6, **then** the assignment overwrites i with 5.

```java
int i = 5;
int r = i++ + i++;       // 5 + 6 = 11, i = 7
```

```java
int i = 5;
int r = ++i + i++;       // 6 + 6 = 12, i = 7
```
(`++i` makes i=6 and yields 6; `i++` yields 6 then makes i=7.)

```java
int i = 10;
int r = i-- - --i;       // 10 - 8 = 2, i = 8
```
(`i--` yields 10, i becomes 9; `--i` makes i=8 and yields 8; 10 - 8 = 2.)

```java
int x = 5;
x = x++ * 2 + x;         // x++ -> 5 (x becomes 6); 5*2 = 10; + x (6) = 16 -> x = 16
```

**Rule of thumb:** Java evaluates operands **strictly left to right**, and the side effect of `x++` is applied immediately when that sub-expression is evaluated. (Unlike C/C++, such expressions have well-defined behaviour in Java.)

### 3.4 Increment on other types

```java
byte b = 127;
b++;                 // allowed (implicit cast built into ++) -> -128 (wraps)
char c = 'a';
c++;                 // 'b'
double d = 1.5;
d++;                 // 2.5
// b = b + 1;        // ERROR (int -> byte), but b++ and b += 1 are fine
```

`++` / `--` can only be applied to **variables**, not literals or expressions: `5++` or `(a+b)++` is a compile error. They also don't work on `boolean`.

### 3.5 Use in loops

```java
for (int i = 0; i < n; i++)   // i++ vs ++i makes no difference here (value is discarded)
```

For arrays: `arr[i++] = x;` uses index i, then increments. Common in two-pointer / merge code:
```java
result[k++] = left[i++];      // copy and advance both
```

### 3.6 Logical NOT `!`

```java
boolean flag = true;
System.out.println(!flag);   // false
```

### 3.7 Bitwise NOT `~`

Flips every bit. For two's complement: **`~x == -x - 1`**.

```java
~5   // -6
~0   // -1
~-1  //  0
```
(Detailed in the bitwise section.)

---

## 4. Assignment operators

### 4.1 Simple assignment `=`

```java
int a = 10;
int a, b, c;
a = b = c = 5;        // chain assignment: right-to-left (c=5, then b=c, then a=b)
```

Assignment is an **expression** that returns the assigned value, which is why `while ((line = reader.readLine()) != null)` works.

### 4.2 Compound assignment operators

`a op= b` is **equivalent** to `a = (T)(a op b)` where `T` is the type of `a`.

| Operator | Meaning |
|----------|---------|
| `+=` | `a = a + b` |
| `-=` | `a = a - b` |
| `*=` | `a = a * b` |
| `/=` | `a = a / b` |
| `%=` | `a = a % b` |
| `&=` | `a = a & b` |
| `\|=` | `a = a \| b` |
| `^=` | `a = a ^ b` |
| `<<=` | `a = a << b` |
| `>>=` | `a = a >> b` |
| `>>>=` | `a = a >>> b` |

### 4.3 The hidden implicit cast (famous interview question)

```java
byte b = 10;
// b = b + 1;      // COMPILE ERROR (b + 1 is int)
b += 1;            // OK! compiler inserts (byte) cast automatically
b++;               // OK as well

int x = 10;
x += 3.7;          // x = (int)(10 + 3.7) = (int) 13.7 = 13   -> silently truncates!
x *= 1.5;          // x = (int)(13 * 1.5) = (int) 19.5 = 19
```

This silent narrowing is a source of bugs: compound assignment will **not** warn you about precision loss.

### 4.4 Examples

```java
int n = 20;
n += 5;    // 25
n -= 3;    // 22
n *= 2;    // 44
n /= 4;    // 11
n %= 4;    // 3
n <<= 2;   // 12   (3 * 4)
n >>= 1;   // 6
n &= 4;    // 4    (110 & 100 = 100)
n |= 3;    // 7    (100 | 011 = 111)
n ^= 5;    // 2    (111 ^ 101 = 010)
```

### 4.5 Order of evaluation in compound assignment

For `a[i] += expr`, the array reference and index are evaluated **once**, and the left operand's value is saved **before** `expr` is evaluated:

```java
int x = 5;
x += x++;      // x = 5 + 5 = 10  (left value 5 saved, x++ returns 5, result 10)
```

---

## 5. Relational operators

They compare two values and always produce a **`boolean`**.

| Operator | Meaning |
|----------|---------|
| `==` | equal to |
| `!=` | not equal to |
| `>` | greater than |
| `<` | less than |
| `>=` | greater or equal |
| `<=` | less or equal |
| `instanceof` | is the object an instance of a type? |

### 5.1 `==` on primitives vs references

- **Primitives:** compares **values**.
- **References (objects):** compares **memory addresses** (are they the same object?).

```java
String s1 = "hello";
String s2 = "hello";
String s3 = new String("hello");

s1 == s2          // true  (both point to the same string-pool object)
s1 == s3          // false (s3 is a new heap object)
s1.equals(s3)     // true  (content comparison)
```

**Always use `.equals()` for object content comparison.**

### 5.2 Integer cache trap

```java
Integer a = 127, b = 127;
System.out.println(a == b);   // true   (cached range -128..127)

Integer c = 128, d = 128;
System.out.println(c == d);   // false  (different objects!)
System.out.println(c.equals(d)); // true
```

### 5.3 Floating-point and `NaN`

```java
double nan = 0.0 / 0;
nan == nan          // false  (NaN is not equal to anything, even itself)
nan != nan          // true
Double.isNaN(nan)   // true  (the correct check)
0.0 == -0.0         // true
```

### 5.4 Characters compare by Unicode value

```java
'a' < 'b'        // true
'A' < 'a'        // true  (65 < 97)
char c = 'x';
c >= 'a' && c <= 'z'   // is lowercase letter
```

### 5.5 Relational operators only work on numeric types

`<`, `>`, `<=`, `>=` only work with numbers/char. `boolean` supports only `==` and `!=`. Chaining like `1 < x < 10` is **not valid** in Java; write `x > 1 && x < 10`.

### 5.6 `instanceof`

```java
Object obj = "Java";
if (obj instanceof String) { ... }

// Java 16+ pattern matching:
if (obj instanceof String s && s.length() > 3) {
    System.out.println(s.toUpperCase());
}
null instanceof String   // false (always false for null)
```

---

## 6. Logical operators

Work with `boolean` operands.

| Operator | Name | Description |
|----------|------|-------------|
| `&&` | Logical AND (short-circuit) | true if **both** true |
| `\|\|` | Logical OR (short-circuit) | true if **at least one** true |
| `!` | Logical NOT | inverts |
| `&` | Non-short-circuit AND | evaluates **both** sides always |
| `\|` | Non-short-circuit OR | evaluates **both** sides always |
| `^` | XOR | true if operands **differ** |

### 6.1 Truth tables

| A | B | A && B | A \|\| B | A ^ B | !A |
|---|---|--------|----------|-------|----|
| T | T | T | T | F | F |
| T | F | F | T | T | F |
| F | T | F | T | T | T |
| F | F | F | F | F | T |

### 6.2 Short-circuit evaluation

- `A && B`: if `A` is **false**, `B` is **never evaluated** (result must be false).
- `A || B`: if `A` is **true**, `B` is **never evaluated** (result must be true).

```java
int a = 5;
if (a > 10 && ++a > 0) { }
System.out.println(a);     // 5   (++a never ran)

if (a > 10 & ++a > 0) { }  // non-short-circuit: BOTH sides run
System.out.println(a);     // 6

int b = 5;
if (b < 10 || ++b > 0) { }
System.out.println(b);     // 5   (right side skipped because left was true)
```

### 6.3 Why short-circuit matters in real code

**Null / bounds guards:**

```java
if (str != null && str.length() > 0) { ... }   // safe, no NullPointerException
if (i < arr.length && arr[i] == target) { ... } // safe, no ArrayIndexOutOfBounds
if (n != 0 && total / n > 10) { ... }           // avoids divide by zero
```

If you wrote `&` instead of `&&` in these, both sides would run and crash.

**Performance:** put the cheap / most-likely-to-decide condition first.

### 6.4 `&`, `|`, `^` on booleans

Use `&` / `|` only when you **want** both sides evaluated (e.g., both have side effects). `^` is "different from" and is handy: `isMale ^ isFemale`, or `(x > 0) ^ (y > 0)` to test "different signs".

### 6.5 De Morgan's laws (simplifying conditions)

```
!(A && B)  ==  !A || !B
!(A || B)  ==  !A && !B
```

---

## 7. Number systems and two's complement

You **must** understand this to understand bitwise and shift operators.

### 7.1 Number systems in Java literals

```java
int dec = 26;          // decimal
int bin = 0b11010;     // binary  (prefix 0b)   = 26
int oct = 032;         // octal   (prefix 0)    = 26
int hex = 0x1A;        // hexadecimal (0x)      = 26
int big = 1_000_000;   // underscores for readability
```

Useful helpers:

```java
Integer.toBinaryString(26);   // "11010"
Integer.toHexString(255);     // "ff"
Integer.toOctalString(8);     // "10"
Integer.parseInt("11010", 2); // 26
Integer.toBinaryString(-1);   // "11111111111111111111111111111111" (32 ones)
```

### 7.2 Sizes of integer types

| Type | Bits | Range |
|------|------|-------|
| `byte` | 8 | -128 to 127 |
| `short` | 16 | -32,768 to 32,767 |
| `char` | 16 | 0 to 65,535 (unsigned) |
| `int` | 32 | -2,147,483,648 to 2,147,483,647 |
| `long` | 64 | -9.22×10¹⁸ to 9.22×10¹⁸ |

For `n` bits signed: range is **−2^(n−1) to 2^(n−1) − 1**.

### 7.3 Two's complement (how Java stores negative numbers)

The leftmost bit is the **sign bit** (0 = positive, 1 = negative).
To get the binary of `-x`: **invert all bits of x, then add 1**.

Example with 8 bits, `5 = 00000101`:

```
 5  = 0000 0101
 invert: 1111 1010
 add 1 : 1111 1011   -> this is -5
```

Check: `-5` in 8-bit = `11111011`. Value = -128 + 64 + 32 + 16 + 8 + 2 + 1 = -5. ✔

Key facts:
- `-1` is **all ones** (`1111...1111`).
- `0` is all zeros.
- `MIN_VALUE` = `1000...0000`, `MAX_VALUE` = `0111...1111`.
- `~x = -x - 1`, hence `-x = ~x + 1`.
- Adding works the same for signed and unsigned — that's why CPUs use two's complement.

### 7.4 Reading a binary number quickly

Bit positions are counted **from the right, starting at 0**:

```
Number:   1   0   1   1   0
Position: 4   3   2   1   0
Value:   16   8   4   2   1   -> 16 + 4 + 2 = 22
```

---

## 8. Bitwise operators

They operate on the **individual bits** of integral operands (`byte`, `short`, `char`, `int`, `long`) — bit by bit. (Operands smaller than `int` are promoted to `int` first.)

| Operator | Name | Rule (per bit pair) |
|----------|------|----------------------|
| `&` | AND | 1 only if **both** are 1 |
| `\|` | OR | 1 if **at least one** is 1 |
| `^` | XOR | 1 if bits are **different** |
| `~` | NOT | flips the bit (unary) |

### 8.1 Truth tables

| a | b | a & b | a \| b | a ^ b |
|---|---|-------|--------|-------|
| 0 | 0 | 0 | 0 | 0 |
| 0 | 1 | 0 | 1 | 1 |
| 1 | 0 | 0 | 1 | 1 |
| 1 | 1 | 1 | 1 | 0 |

`~0 = 1`, `~1 = 0`.

### 8.2 Worked example (use 8 bits to keep it readable)

`a = 12 = 00001100`, `b = 10 = 00001010`

```
a      = 0000 1100
b      = 0000 1010
-----------------
a & b  = 0000 1000  = 8
a | b  = 0000 1110  = 14
a ^ b  = 0000 0110  = 6
~a     = 1111 0011  = -13   (since ~a = -a - 1 = -12 - 1)
```

### 8.3 Intuition for each operator

**AND (`&`) = "filter / mask / check / clear"**
Think of AND as a **stencil**: where the mask has 1, the original bit passes through; where the mask has 0, the result is forced to 0.

```
value 1011 0110
mask  0000 1111   (keep lower 4 bits)
----------------
res   0000 0110
```
Uses: extract bits, check if a bit is set, clear bits, test even/odd, `n % 2^k`.

**OR (`|`) = "set / combine / merge"**
Where the mask has 1, the result is forced to 1; where mask has 0, original passes through.

```
value 1000 0001
mask  0000 0110   (set bits 1 and 2)
----------------
res   1000 0111
```
Uses: turn bits on, combine flags, pack values into one integer.

**XOR (`^`) = "toggle / difference / cancel"**
Where mask has 1, the bit **flips**; where mask has 0, it's unchanged. Also "are these different?".

```
value 1010 1010
mask  1111 0000   (flip upper 4 bits)
----------------
res   0101 1010
```

**NOT (`~`) = "flip everything"**
`~x == -x - 1`. Often combined with `&` to build a "clear" mask: `n & ~(1 << i)`.

### 8.4 The superpower properties of XOR (memorize!)

```
a ^ 0 = a          // XOR with 0 does nothing
a ^ a = 0          // XOR with itself cancels to 0
a ^ b = b ^ a      // commutative
(a ^ b) ^ c = a ^ (b ^ c)   // associative
a ^ b ^ b = a      // XOR twice with the same value restores the original
```

These are the basis of: finding the unique element, swapping without temp, simple encryption, parity, checksums, RAID parity, and many DSA tricks.

### 8.5 Properties of AND and OR

```
a & 0 = 0        a | 0 = a
a & a = a        a | a = a
a & -1 = a       a | -1 = -1      (-1 is all ones)
a & ~a = 0       a | ~a = -1
```

### 8.6 Bitwise operators on `boolean`

`&`, `|`, `^` also work on booleans (as non-short-circuit logical operators). `~` does **not** work on boolean — use `!`.

### 8.7 Bitwise vs Logical (don't confuse them)

| | Logical `&&` `\|\|` | Bitwise `&` `\|` |
|---|---|---|
| Operands | `boolean` | integers (and boolean) |
| Short-circuit | Yes | No |
| Result | boolean | integer (same promoted type) |

---

## 9. Shift operators

Shift operators **move bits** left or right.

| Operator | Name | What it does |
|----------|------|--------------|
| `<<` | Left shift | shifts bits left, fills the right with `0` |
| `>>` | Signed (arithmetic) right shift | shifts right, fills the left with the **sign bit** |
| `>>>` | Unsigned (logical) right shift | shifts right, fills the left with `0` |

### 9.1 Left shift `<<`

`x << n` shifts x's bits left by n positions. **Equivalent to `x * 2^n`** (as long as no overflow).

```java
5 << 1   // 10   (0101 -> 1010)
5 << 2   // 20
5 << 3   // 40
1 << 4   // 16    (this is how you compute 2^4)
```

### 9.2 Signed right shift `>>`

`x >> n` is roughly **`floor(x / 2^n)`**; the sign bit is copied into vacated positions, so negatives stay negative.

```java
20 >> 1    // 10
20 >> 2    // 5
-20 >> 2   // -5
 -5 >> 1   // -3   (note: floor, not truncation!)
 -5 / 2    // -2   (division truncates toward 0)
```

Why `-5 >> 1 = -3`?

```
-5 = ...1111 1011
>>1  ...1111 1101  = -3   (sign bit 1 copied in)
```

So **`>>` is floor division by 2^n**, while `/` truncates toward zero. They differ for negative odd numbers.

### 9.3 Unsigned right shift `>>>`

Fills the left with **0**, regardless of sign. For non-negative numbers, `>>` and `>>>` are identical. For negatives, the result becomes a large positive number.

```java
-1 >>> 28     // 15   (1111...1111 -> 0000 1111)
-1 >>> 31     // 1
-1 >>> 1      // 2147483647  (Integer.MAX_VALUE)
-8 >>> 1      // 2147483644
```

Use it when you treat the int as raw **bits** (hashes, bit-reversal, binary-search midpoint, unsigned logic). Java has **no unsigned `int` type**, so `>>>` fills that role.

### 9.4 Shift distance is masked (huge trap)

- For `int`: only the **low 5 bits** of the shift distance are used (`n & 31`).
- For `long`: only the **low 6 bits** are used (`n & 63`).

```java
1 << 32      // 1   (32 & 31 = 0 -> shift by 0!)
1 << 33      // 2   (33 & 31 = 1)
1 << -1      // 1 << 31 = -2147483648   (-1 & 31 = 31)
1L << 32     // 4294967296  (works, it's a long)
```

### 9.5 `int` vs `long` shifting trap

```java
long mask = 1 << 40;    // WRONG: computed as int -> 1 << (40 & 31) = 1 << 8 = 256
long mask = 1L << 40;   // RIGHT: 1099511627776
```

**Always use `1L << i` when `i` can be ≥ 31 or you are working with `long`.**

### 9.6 Byte / short shift trap

`byte`/`short`/`char` are promoted to `int` before shifting:

```java
byte b = -16;                   // 1111 0000 as byte, but promoted to 0xFFFFFFF0
System.out.println(b >>> 2);    // 1073741820  (not what a beginner expects!)
b = (byte)(b >>> 2);            // 0x3FFFFFFC cast to byte -> 0xFC = -4

// To treat a byte as unsigned:
int ub = b & 0xFF;              // 240
```

### 9.7 Shift + multiply/divide performance — honest note

`x << 1` ≡ `x * 2` and `x >> 1` ≡ `x / 2` (for non-negative). On old compilers it was faster; **today the JIT already converts `x * 2` into a shift** automatically. So:
- Use shifts when you are doing **bit-level logic** (masks, bitsets, powers of two) — that's what they are *for*.
- Don't replace every `*2` with `<<1` for "speed"; readability matters more, and negative `/` vs `>>` differ.

---

## 10. Bit manipulation tricks for DSA

> Notation: `i` = bit position (0-indexed from the right), `n` = the number.

### 10.1 The five fundamental operations

```java
// 1) Get / check the i-th bit
boolean isSet = ((n >> i) & 1) == 1;     // or: (n & (1 << i)) != 0

// 2) Set the i-th bit to 1
n = n | (1 << i);

// 3) Clear the i-th bit to 0
n = n & ~(1 << i);

// 4) Toggle (flip) the i-th bit
n = n ^ (1 << i);

// 5) Update the i-th bit to value v (0 or 1)
n = (n & ~(1 << i)) | (v << i);
```

Why they work: `1 << i` creates a number with **only bit i** set — a *mask*.

```
i = 3 -> 1 << 3 = 0000 1000
```
- `n & mask` → keeps only bit i (non-zero if it was set)
- `n | mask` → forces bit i to 1
- `n & ~mask` → forces bit i to 0 (`~mask` has all ones except bit i)
- `n ^ mask` → flips bit i

### 10.2 Even / odd

```java
(n & 1) == 0   // even  (last bit is 0)
(n & 1) == 1   // odd
```
Works for negatives too (`-3 & 1 == 1`) whereas `n % 2 == 1` fails for negatives (`-3 % 2 == -1`). That's a real reason to prefer `& 1` or `n % 2 != 0`.

### 10.3 Multiply / divide by powers of two

```java
n << k      // n * 2^k
n >> k      // floor(n / 2^k)
n & (m - 1) // n % m, ONLY when m is a power of two (works for non-negative n)
```
Example: `n % 8` ≡ `n & 7`, `n % 16` ≡ `n & 15`.

### 10.4 Check power of two

A power of two has **exactly one** set bit: `1000`. Subtract 1 and all lower bits flip: `0111`. AND gives 0.

```java
boolean isPow2 = n > 0 && (n & (n - 1)) == 0;
```

```
n   = 8 = 1000
n-1 = 7 = 0111
n&(n-1) = 0000  -> power of two
n = 6 = 0110, n-1 = 0101, AND = 0100 != 0 -> not a power of two
```
(The `n > 0` check handles `0` and negative numbers.)

*Power of four (LeetCode 342):* `n > 0 && (n & (n-1)) == 0 && (n & 0x55555555) != 0`.

### 10.5 `n & (n - 1)` — clear the lowest set bit (Brian Kernighan's trick)

Subtracting 1 flips the lowest set bit to 0 and all the zeros to its right to 1. AND-ing wipes out exactly that lowest set bit.

```
n     = 1011 0100
n - 1 = 1011 0011
n&(n-1)=1011 0000   (lowest set bit removed)
```

**Count set bits (popcount) in O(number of set bits):**

```java
int count = 0;
while (n != 0) {
    n &= (n - 1);   // remove lowest set bit
    count++;
}
```
Much better than looping over all 32 bits when few bits are set. Built-in: `Integer.bitCount(n)` / `Long.bitCount(n)` (uses a CPU instruction when available).

### 10.6 `n & -n` — isolate the lowest set bit

Since `-n = ~n + 1`, only the lowest set bit survives in `n & -n`.

```
n  = 0110 1000
-n = 1001 1000
n&-n=0000 1000
```

Uses:
- **Fenwick Tree / Binary Indexed Tree**: `i += i & -i` (go to parent), `i -= i & -i` (go to previous range).
- Iterating over set bits: `while (n != 0) { int low = n & -n; ...; n -= low; }`.
- Finding position of lowest set bit: `Integer.numberOfTrailingZeros(n)`.

### 10.7 Count bits for all numbers 0..n (DP with bits)

```java
int[] bits = new int[n + 1];
for (int i = 1; i <= n; i++) {
    bits[i] = bits[i >> 1] + (i & 1);   // bits of i/2 + last bit
}
```
(LeetCode 338 — Counting Bits)

### 10.8 XOR tricks

#### a) Swap two numbers without a temp variable

```java
a = a ^ b;
b = a ^ b;   // (a^b)^b = a
a = a ^ b;   // (a^b)^a = b
```
**Caveat:** if both refer to the *same* memory location (e.g., `swap(arr[i], arr[i])`), the value becomes 0! Also, it's a curiosity — a normal temp variable is clearer and just as fast.

#### b) Find the single non-repeating element (others appear twice) — LeetCode 136

```java
int ans = 0;
for (int x : nums) ans ^= x;   // pairs cancel (x^x=0), the unique one remains
```
O(n) time, **O(1)** space.

#### c) Find the missing number in 0..n — LeetCode 268

```java
int xor = n;
for (int i = 0; i < n; i++) xor ^= i ^ nums[i];
// every present number appears twice (index + value) and cancels
```

#### d) Two unique numbers, everything else twice — LeetCode 260

```java
int xor = 0;
for (int x : nums) xor ^= x;          // xor = a ^ b  (a != b, so xor != 0)
int diff = xor & -xor;                // lowest bit where a and b differ
int a = 0, b = 0;
for (int x : nums) {
    if ((x & diff) == 0) a ^= x;      // group 1
    else                 b ^= x;      // group 2
}
// a and b are the two unique numbers
```

#### e) Single number when others appear **three** times — LeetCode 137

Count each bit position mod 3:
```java
int ones = 0, twos = 0;
for (int x : nums) {
    ones = (ones ^ x) & ~twos;
    twos = (twos ^ x) & ~ones;
}
return ones;
```

#### f) XOR of numbers from 1 to n in O(1)

```java
int xorUpTo(int n) {
    switch (n % 4) {
        case 0: return n;
        case 1: return 1;
        case 2: return n + 1;
        default: return 0;       // n % 4 == 3
    }
}
// XOR of range [l, r] = xorUpTo(r) ^ xorUpTo(l - 1)
```

#### g) Check whether two numbers have opposite signs

```java
boolean opposite = (x ^ y) < 0;   // sign bits differ -> result's sign bit is 1
```

#### h) Hamming distance (number of differing bits) — LeetCode 461

```java
int dist = Integer.bitCount(x ^ y);
```

#### i) Add two numbers without `+` — LeetCode 371

```java
while (b != 0) {
    int carry = (a & b) << 1;   // bits where both 1 produce a carry
    a = a ^ b;                  // sum without carry
    b = carry;
}
return a;
```

### 10.9 Subsets generation with bitmasks (very common!)

For a set of `n` elements there are `2^n` subsets. Each integer from `0` to `2^n - 1` **is** a subset: bit `j` set ⇒ include element `j`.

```java
int n = nums.length;
for (int mask = 0; mask < (1 << n); mask++) {
    List<Integer> subset = new ArrayList<>();
    for (int j = 0; j < n; j++) {
        if ((mask & (1 << j)) != 0) subset.add(nums[j]);
    }
    result.add(subset);
}
```
Example with `{a,b,c}`: mask `101` → `{a, c}`. (LeetCode 78 — Subsets)

**Iterate over all submasks of a mask** (used in DP):

```java
for (int sub = mask; sub > 0; sub = (sub - 1) & mask) { ... }
```

### 10.10 Bitmask DP (state compression)

When `n ≤ ~20`, a whole "set of visited items" fits in one `int`, making it a DP/memo key:

```java
// Travelling Salesman style
int FULL = (1 << n) - 1;
// visited |= (1 << city);           // mark visited
// (visited & (1 << city)) != 0      // is visited?
// visited == FULL                   // everything visited
```
Used in: TSP, assignment problems, "can we partition into k groups", "shortest path visiting all nodes", game states, SOS DP.

### 10.11 Character tricks (ASCII)

In ASCII, `'A' = 65 = 0100 0001` and `'a' = 97 = 0110 0001` — they differ only in **bit 5 (value 32)**.

```java
char lower  = (char)(c | ' ');    // to lowercase   (' ' = 32)
char upper  = (char)(c & '_');    // to uppercase   ('_' = 95 = 0101 1111)
char toggle = (char)(c ^ ' ');    // flip case
```
(Valid for letters A–Z/a–z only.)

**Bitmask for letters (anagram / "have we seen this letter" problems):**

```java
int seen = 0;
for (char c : s.toCharArray()) {
    int bit = 1 << (c - 'a');
    if ((seen & bit) != 0) { /* duplicate letter */ }
    seen |= bit;
}
```
A single `int` represents the set of lowercase letters — O(1) space instead of `boolean[26]`.

### 10.12 Other useful tricks

```java
// Absolute value without branching (32-bit)
int mask = n >> 31;               // 0 if n>=0, -1 if n<0
int abs  = (n ^ mask) - mask;

// Gray code (consecutive numbers differ by exactly one bit) — LeetCode 89
int gray = i ^ (i >> 1);

// Parity (is the number of set bits odd?)
boolean odd = (Integer.bitCount(n) & 1) == 1;

// Highest set bit position
int pos = 31 - Integer.numberOfLeadingZeros(n);    // floor(log2(n)) for n > 0

// Round up to next power of two
int p = Integer.highestOneBit(n - 1) << 1;         // for n > 1

// Reverse bits (LeetCode 190)
int rev = Integer.reverse(n);

// Next number with the same number of set bits, etc. (Gosper's hack)
int c = n & -n, r = n + c;
int next = (((r ^ n) >>> 2) / c) | r;
```

### 10.13 Helpful built-in methods (know them!)

| Method | Purpose |
|--------|---------|
| `Integer.bitCount(n)` | number of 1 bits |
| `Integer.toBinaryString(n)` | binary string |
| `Integer.highestOneBit(n)` | keep only the highest set bit |
| `Integer.lowestOneBit(n)` | same as `n & -n` |
| `Integer.numberOfLeadingZeros(n)` | zeros before the first 1 |
| `Integer.numberOfTrailingZeros(n)` | zeros after the last 1 |
| `Integer.reverse(n)` | reverse bit order |
| `Integer.rotateLeft(n, k)` / `rotateRight` | circular shift |
| `Integer.toUnsignedString(n)` | print as unsigned |
| `Integer.compareUnsigned(a, b)` | unsigned compare |
| `Integer.divideUnsigned(a, b)` | unsigned division |
| `Long.*` equivalents | same for `long` |
| `java.util.BitSet` | resizable bit array |

### 10.14 DSA problem list to practice

| Problem | Core idea |
|---------|-----------|
| 136 Single Number | XOR all |
| 137 Single Number II | bit counting mod 3 |
| 260 Single Number III | XOR + lowest differing bit |
| 191 Number of 1 Bits | `n & (n-1)` |
| 231 Power of Two | `n & (n-1) == 0` |
| 342 Power of Four | + mask `0x55555555` |
| 268 Missing Number | XOR index and value |
| 338 Counting Bits | `dp[i] = dp[i>>1] + (i&1)` |
| 190 Reverse Bits | shift + OR |
| 371 Sum of Two Integers | XOR + AND-carry |
| 461 Hamming Distance | `bitCount(x ^ y)` |
| 78 Subsets | bitmask enumeration |
| 89 Gray Code | `i ^ (i>>1)` |
| 421 Max XOR of Two Numbers | bitwise trie |
| Fenwick Tree (BIT) | `i & -i` |
| TSP / bitmask DP | visited mask |

---

## 11. Real-world uses of bitwise operators

### 11.1 Flags and permissions (store many booleans in one int)

```java
final int READ    = 1 << 0;   // 001
final int WRITE   = 1 << 1;   // 010
final int EXECUTE = 1 << 2;   // 100

int perms = READ | WRITE;                 // grant read + write  -> 011
boolean canWrite = (perms & WRITE) != 0;  // check
perms |= EXECUTE;                         // add permission
perms &= ~WRITE;                          // revoke permission
perms ^= READ;                            // toggle permission
```
This is how Unix file permissions (`rwx` = 4/2/1), Android `Intent` flags, window styles, game state flags, and database column flags work. Java's `EnumSet` is internally a bit vector as well.

### 11.2 Memory saving: `BitSet` and bit arrays

- One `boolean` in a `boolean[]` typically uses **1 byte**. One bit in a `long[]` uses **1/8 byte** → ~8× less memory (`BitSet` does this).
- **Sieve of Eratosthenes** for primes up to 10⁸ becomes feasible in memory using a bitset.
- Visited arrays in huge graphs, bloom filters, bitmap indexes (databases, search engines).

### 11.3 HashMap internals in Java

```java
// Spread higher bits into lower bits (reduces collisions)
static int hash(Object key) {
    int h = key.hashCode();
    return h ^ (h >>> 16);          // XOR + unsigned shift
}
// Bucket index: table length is always a power of 2, so:
int index = (n - 1) & hash;         // same as hash % n, but faster and non-negative
```
This is why HashMap capacity is always a **power of two**.

### 11.4 Packing multiple values into one integer (colors, coordinates)

```java
// Pack RGB into one int
int rgb = (r << 16) | (g << 8) | b;

// Unpack
int red   = (rgb >> 16) & 0xFF;
int green = (rgb >> 8)  & 0xFF;
int blue  =  rgb        & 0xFF;
```

Pack two ints into one long (e.g., as a HashMap key for a grid cell `(x, y)`):

```java
long key = ((long) x << 32) | (y & 0xFFFFFFFFL);
int x2 = (int) (key >> 32);
int y2 = (int) key;
```
(`& 0xFFFFFFFFL` prevents sign extension of a negative `y`.)

### 11.5 Networking: IP address and subnet masks

```java
int ip     = (192 << 24) | (168 << 16) | (1 << 8) | 10;  // 192.168.1.10
int mask   = 0xFFFFFF00;                                  // 255.255.255.0
int network = ip & mask;                                  // 192.168.1.0
```
AND with the subnet mask yields the network address.

### 11.6 Cryptography, checksums, hashing, compression

- **XOR cipher / stream ciphers:** `cipher = plain ^ key`, and `cipher ^ key = plain`.
- **Hash functions** (MurmurDB, FNV, SHA family) are built from shifts, rotates, XORs.
- **CRC / parity bits / RAID-5:** XOR parity lets a disk be rebuilt from the others.
- **Huffman / bit-level compression, Base64, UTF-8** encoding are all shift/mask operations.

### 11.7 Reading binary data

```java
// Bytes are signed in Java, so mask with 0xFF to read them as 0..255
int value = ((b0 & 0xFF) << 24) | ((b1 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b3 & 0xFF);
```
(Used in file formats, network protocols, serialization, endianness conversion.)

### 11.8 Games, graphics, embedded

Chess **bitboards** (a 64-bit `long` represents the board; moves are computed with shifts and ANDs), tile maps, collision masks, hardware register control, IoT/embedded flags.

---

## 12. Ternary operator

The only operator with **three** operands.

```
condition ? valueIfTrue : valueIfFalse
```

```java
int a = 10, b = 20;
int max = (a > b) ? a : b;                         // 20
String s = (n % 2 == 0) ? "even" : "odd";
```

### 12.1 Nesting (right-associative)

```java
String grade = (m >= 90) ? "A"
             : (m >= 75) ? "B"
             : (m >= 50) ? "C"
             : "F";
```
Readable if formatted one-per-line; avoid deep nesting.

### 12.2 It's an expression (has a value), `if` is a statement

You can use it inside `return`, method arguments, string concatenation (wrap in parentheses!):

```java
System.out.println("Result: " + (x > 0 ? "positive" : "non-positive"));
```

### 12.3 Type traps

```java
System.out.println(true ? 1 : 2.0);     // 1.0  (numeric promotion -> double!)

Integer x = null;
int y = true ? x : 0;                   // NullPointerException! (auto-unboxing)

System.out.println(true ? 'a' : 0);     // a   (0 is a constant that fits in char)
int i = 0;
System.out.println(true ? 'a' : i);     // 97  (i is an int variable -> promoted to int)
```

Both branches must be assignable to a common type; only **one** branch is evaluated (like short-circuit).

---

## 13. Operator precedence and associativity

**Precedence** = which operator binds tighter (evaluated first).
**Associativity** = for operators of the same precedence, the direction of grouping.

### 13.1 Precedence table (highest to lowest)

| Level | Operators | Associativity |
|-------|-----------|---------------|
| 1 | `()` `[]` `.` postfix `x++` `x--` | left to right |
| 2 | prefix `++x` `--x`, unary `+` `-`, `!` `~`, cast `(type)`, `new` | right to left |
| 3 | `*` `/` `%` | left to right |
| 4 | `+` `-` (and string `+`) | left to right |
| 5 | `<<` `>>` `>>>` | left to right |
| 6 | `<` `<=` `>` `>=` `instanceof` | left to right |
| 7 | `==` `!=` | left to right |
| 8 | `&` | left to right |
| 9 | `^` | left to right |
| 10 | `\|` | left to right |
| 11 | `&&` | left to right |
| 12 | `\|\|` | left to right |
| 13 | `?:` | right to left |
| 14 | `=` `+=` `-=` `*=` `/=` `%=` `&=` `^=` `\|=` `<<=` `>>=` `>>>=` | right to left |

**Memory trick for the bitwise region:** `shift > relational > equality > & > ^ > | > && > ||`.
Remember: the bitwise `& ^ |` rank **below** `==` — the source of the biggest trap in the next section.

### 13.2 Examples

```java
int r = 2 + 3 * 4;          // 14   (* before +)
int s = (2 + 3) * 4;        // 20
int t = 10 - 4 - 3;         // 3    (left to right: (10-4)-3)
int u = 2 + 3 << 1;         // 10   (+ before <<:  (2+3)<<1)
int v = 1 << 2 + 3;         // 32   (1 << 5)
boolean b = true || false && false;  // true  (&& before ||)
int x, y, z;
x = y = z = 7;              // right to left
```

### 13.3 Precedence vs order of evaluation (they are different!)

Precedence decides **grouping**; Java always evaluates **operands left to right**:

```java
int a = 1;
int r = a + (a = 5) * a;     // 1 + (5 * 5) = 26
```
Here `a` on the left is read first (value 1) before `a = 5` runs.

**Best practice:** when unsure, **use parentheses** — they cost nothing and make intent explicit.

---

## 14. Interview traps and edge cases

### Trap 1 — Bitwise precedence vs `==`

```java
if (n & 1 == 0) { }    // COMPILE ERROR!
```
`==` binds tighter: it's `n & (1 == 0)` → `int & boolean`. **Fix:** `if ((n & 1) == 0)`.
Always parenthesize bitwise expressions inside comparisons.

### Trap 2 — `i = i++`

Stays unchanged (see section 3.3).

### Trap 3 — `byte b = b + 1` vs `b += 1`

First fails to compile; second compiles (implicit cast).

### Trap 4 — Integer overflow in multiplication assigned to `long`

`long x = 1_000_000 * 1_000_000;` overflows before widening.

### Trap 5 — Integer division

`double avg = sum / count;` with ints truncates first. Use `(double) sum / count`.

### Trap 6 — `%` with negative numbers

`-7 % 3 == -1`. Use `Math.floorMod` for a non-negative result.

### Trap 7 — Short-circuit side effects

`if (x > 5 && y++ > 0)` — `y` doesn't increment when `x <= 5`.

### Trap 8 — `==` on objects / Integer cache

`Integer 128 == 128` is `false`; use `.equals`.

### Trap 9 — Shift distance masking

`1 << 32 == 1` (not 0). And `1 << 40` as int is `256`. Use `1L`.

### Trap 10 — `>>` vs `>>>` on negatives; `byte` promotions

`-1 >> 10 == -1`, `-1 >>> 10 == 4194303`.

### Trap 11 — `char` arithmetic

`'a' + 'b'` is `195`, but `"" + 'a' + 'b'` is `"ab"`.

### Trap 12 — `Math.abs(Integer.MIN_VALUE)` is negative

### Trap 13 — NaN comparisons

`NaN == NaN` is false; use `Double.isNaN`.

### Trap 14 — Ternary numeric promotion and null unboxing

`true ? 1 : 2.0` prints `1.0`; `Integer null` unboxing → NPE.

### Trap 15 — XOR swap with the same reference

`a ^= b; b ^= a; a ^= b;` zeroes the value if `a` and `b` are the same variable/array slot.

### Trap 16 — `~` on small types

```java
byte b = 5;
byte c = ~b;        // ERROR: ~b is int
byte c = (byte) ~b; // -6
```

### Trap 17 — Compound assignment silently narrows

`int x = 10; x += 3.9;` → `13`.

### Trap 18 — String concatenation order

`1 + 2 + "a"` → `"3a"`, `"a" + 1 + 2` → `"a12"`.

---

## 15. Practice questions with answers

Try each **before** reading the answer.

**Q1.** What does this print?
```java
int a = 5;
int b = a++ + ++a;
System.out.println(a + " " + b);
```
**A:** `a++` → 5 (a=6), `++a` → 7 (a=7). `b = 5 + 7 = 12`. Output: **`7 12`**.

**Q2.**
```java
int x = 10;
x = x-- - --x;
System.out.println(x);
```
**A:** `x--` → 10 (x=9); `--x` → 8 (x=8); 10 - 8 = 2; assigned to x → **`2`**.

**Q3.**
```java
System.out.println(10 + 20 + "Java" + 10 + 20);
```
**A:** `30Java1020`.

**Q4.**
```java
byte b = 127;
b++;
System.out.println(b);
```
**A:** **`-128`** (overflow wraps).

**Q5.**
```java
System.out.println(5 & 3);
System.out.println(5 | 3);
System.out.println(5 ^ 3);
System.out.println(~5);
```
**A:** `101 & 011 = 001 = 1`, `101 | 011 = 111 = 7`, `101 ^ 011 = 110 = 6`, `~5 = -6`. Output: **1, 7, 6, -6**.

**Q6.**
```java
System.out.println(-16 >> 2);
System.out.println(-16 >>> 28);
```
**A:** `-16 >> 2 = -4`. `-16 = 0xFFFFFFF0`; `>>> 28` → `0x0000000F` = **15**.

**Q7.**
```java
int a = 5;
if (a > 10 && a++ > 2) { }
System.out.println(a);
```
**A:** **`5`** (short-circuit).

**Q8.** Check if a number is a power of two in O(1).
**A:** `n > 0 && (n & (n - 1)) == 0`.

**Q9.** Count the set bits of 29 using Kernighan's algorithm.
**A:** `29 = 11101` → 4 iterations: `11101 → 11100 → 11000 → 10000 → 0`. Answer **4**.

**Q10.** Why is `n & 1 == 0` an error?
**A:** `==` has higher precedence than `&`, giving `n & (1 == 0)` → `int & boolean`. Use `(n & 1) == 0`.

**Q11.**
```java
System.out.println(1 << 33);
```
**A:** shift distance is `33 & 31 = 1` → **`2`**.

**Q12.**
```java
int a = 5, b = 9;
a ^= b; b ^= a; a ^= b;
System.out.println(a + " " + b);
```
**A:** XOR swap → **`9 5`**.

**Q13.**
```java
Integer a = 200, b = 200;
System.out.println(a == b);
System.out.println(a.equals(b));
```
**A:** **`false`**, **`true`** (200 is outside the −128..127 cache).

**Q14.** Find the element appearing once in `[4, 1, 2, 1, 2]`.
**A:** `4 ^ 1 ^ 2 ^ 1 ^ 2 = 4`.

**Q15.**
```java
int x = 7;
x += x++ + ++x;
System.out.println(x);
```
**A:** Left value 7 saved. `x++` → 7 (x=8); `++x` → 9 (x=9). `7 + (7 + 9) = 23`. Output **`23`**.

**Q16.** How do you toggle the 4th bit (index 3) of `n`?
**A:** `n ^ (1 << 3)`.

**Q17.** How do you clear the lowest set bit? How do you isolate it?
**A:** Clear: `n & (n - 1)`. Isolate: `n & -n`.

**Q18.** What does this print: `System.out.println(true ? 1 : 2.0);`
**A:** **`1.0`**.

---

## 16. Final cheat sheet

### All operators at a glance

| Category | Operators | Remember |
|----------|-----------|----------|
| Arithmetic | `+ - * / %` | int/int truncates; `%` sign follows left operand; overflow wraps silently |
| Unary | `+ - ++ -- ! ~` | `x++` = use then add; `++x` = add then use |
| Assignment | `= += -= ... <<= >>= >>>=` | compound ops include an implicit cast |
| Relational | `== != < > <= >= instanceof` | `==` on objects compares references |
| Logical | `&& \|\| !` | short-circuit; guard against null/bounds/zero |
| Bitwise | `& \| ^ ~` | AND = mask/check/clear, OR = set, XOR = toggle/cancel, NOT = flip |
| Shift | `<< >> >>>` | `<<` = ×2ⁿ, `>>` = floor ÷2ⁿ, `>>>` = zero-fill; distance masked to 5/6 bits |
| Ternary | `?:` | right-associative, watch type promotion & unboxing |

### The bit manipulation cheat sheet

| Task | Code |
|------|------|
| Check bit i | `(n >> i) & 1` |
| Set bit i | `n \| (1 << i)` |
| Clear bit i | `n & ~(1 << i)` |
| Toggle bit i | `n ^ (1 << i)` |
| Is odd | `(n & 1) == 1` |
| `n * 2^k` | `n << k` |
| `n / 2^k` (non-negative) | `n >> k` |
| `n % 2^k` (non-negative) | `n & ((1 << k) - 1)` |
| Is power of 2 | `n > 0 && (n & (n-1)) == 0` |
| Clear lowest set bit | `n & (n - 1)` |
| Isolate lowest set bit | `n & -n` |
| Count set bits | `Integer.bitCount(n)` |
| All ones (k bits) | `(1 << k) - 1` |
| Swap (XOR) | `a ^= b; b ^= a; a ^= b;` |
| Opposite signs | `(x ^ y) < 0` |
| Unsigned byte | `b & 0xFF` |
| Long mask | `1L << i` |
| Subset enumeration | `for (mask = 0; mask < (1 << n); mask++)` |

### The 10 rules to never forget

1. Integer division truncates toward zero; cast **before** dividing if you want decimals.
2. `byte/short/char` are promoted to `int` in arithmetic.
3. `x++` returns the **old** value, `++x` returns the **new** value.
4. Compound assignment (`+=`) silently casts back to the left type.
5. Use `&&`/`||` (short-circuit) for guards; `&`/`|` only when you want both sides evaluated.
6. **Always parenthesize** bitwise operations inside comparisons: `(n & 1) == 0`.
7. XOR: `a ^ a = 0`, `a ^ 0 = a` — the key to "find the unique element" problems.
8. `n & (n-1)` clears the lowest set bit; `n & -n` isolates it.
9. Use `1L << i` for 64-bit masks; shift distances are masked (`& 31` / `& 63`).
10. `>>` keeps the sign (floor division), `>>>` fills zeros (treat as unsigned).

---

*End of notes. Re-read sections 7–10 until bit manipulation feels natural, then solve the problem list in 10.14.*
