# Java Variables and Data Types: The Complete Beginner-Friendly Guide

> You gave me the topics in random order, so I arranged them into one logical learning path.
> Each section builds on the one before it. Read in order and nothing will feel confusing.

---

## Your Topics Checklist (everything you asked for, and where it is)

| # | What you asked for | Section |
|---|--------------------|---------|
| 1 | What a variable is, how to declare one | 1, 2 |
| 2 | Declaring vs defining (and initializing) | 2 |
| 3 | Rules for naming a variable | 3 |
| 4 | How many reserved words Java has, and the "reserved but never used" fun fact | 4 |
| 5 | Data types: primitive vs non-primitive | 5 |
| 6 | The 8-bit concept: 255 vs -128 to 127, how numbers are really stored | 6 |
| 7 | Why they are called "primitive", full list with ranges and syntax | 7 |
| 8 | Integer types: byte, short, int, long | 8 |
| 9 | Decimal (floating-point) types: float and double, why double in production | 9 |
| 10 | char, ASCII, then Unicode and bigger bit sizes | 10 |
| 11 | boolean | 11 |
| 12 | Hex, octal, binary: what they are and why we use them | 12 |
| 13 | Scientific notation with `e` | 13 |
| 14 | Underscores in numbers and their exact rules | 14 |
| 15 | Default values and kinds of variables | 15 |
| 16 | Type conversion, casting and overflow | 16 |
| 17 | Non-primitive (reference) types | 17 |
| 18 | Wrapper classes (Integer, Long...) and why they exist when int and long already exist | 18 |
| 19 | Summary, cheat sheet and quiz | 19 |

---

## Table of Contents

1. [What is a Variable?](#1-what-is-a-variable)
2. [Declaring, Defining, Initializing and Assigning](#2-declaring-defining-initializing-and-assigning)
3. [Rules for Naming Variables (Identifiers)](#3-rules-for-naming-variables-identifiers)
4. [Reserved Words in Java](#4-reserved-words-in-java)
5. [Data Types: The Big Picture](#5-data-types-the-big-picture)
6. [Bits, Bytes and How Numbers Are Really Stored](#6-bits-bytes-and-how-numbers-are-really-stored)
7. [The 8 Primitive Types](#7-the-8-primitive-types)
8. [Integer Types: byte, short, int, long](#8-integer-types-byte-short-int-long)
9. [Floating-Point Types: float and double](#9-floating-point-types-float-and-double)
10. [The char Type: ASCII and Unicode](#10-the-char-type-ascii-and-unicode)
11. [The boolean Type](#11-the-boolean-type)
12. [Number Systems: Decimal, Binary, Octal, Hexadecimal](#12-number-systems-decimal-binary-octal-hexadecimal)
13. [Scientific Notation (the `e` form)](#13-scientific-notation-the-e-form)
14. [Underscores in Numeric Literals](#14-underscores-in-numeric-literals)
15. [Default Values and Kinds of Variables](#15-default-values-and-kinds-of-variables)
16. [Type Conversion, Casting and Overflow](#16-type-conversion-casting-and-overflow)
17. [Non-Primitive (Reference) Types](#17-non-primitive-reference-types)
18. [Wrapper Classes: Integer, Long and Friends](#18-wrapper-classes-integer-long-and-friends)
19. [Final Summary, Cheat Sheet and Quiz](#19-final-summary-cheat-sheet-and-quiz)

---

## 1. What is a Variable?

A **variable** is a **named box in memory** that holds a value which can change while the program runs.

```
        Memory (RAM)
   ┌─────────────────────┐
   │  age   │     25     │   ← variable name: age, value: 25
   ├────────┼────────────┤
   │ price  │    99.5    │   ← variable name: price, value: 99.5
   ├────────┼────────────┤
   │ grade  │     'A'    │   ← variable name: grade, value: 'A'
   └─────────────────────┘
```

A variable has **three things**:

| Part | Meaning | Example |
|------|---------|---------|
| **Type** | What kind of data it can hold (and how much memory it uses) | `int` |
| **Name** | The label you use to access it | `age` |
| **Value** | The data stored right now | `25` |

### Java is "statically typed" and "strongly typed"

- **Statically typed**: the type of every variable is fixed **at compile time**. Once you say `int age`, it can never hold text.
- **Strongly typed**: Java is strict about mixing types. It won't silently treat a `String` as a number.

```java
int age = 25;
age = "hello";   // ERROR: a String cannot go into an int box
```

This strictness catches many mistakes *before* the program runs. That is a big reason Java is trusted for large systems.

---

## 2. Declaring, Defining, Initializing and Assigning

These four words are often mixed up. Let's separate them clearly.

### 2.1 Declaration
**Declaration** means telling Java the **type and name** of a variable.

```java
int age;        // declaration: "I need an int box called age"
```

### 2.2 Initialization
**Initialization** means giving the variable its **first value**.

```java
age = 25;       // first value given → initialization
```

### 2.3 Declaration + Initialization together (most common)

```java
int age = 25;   // declare and initialize in one line
```

### 2.4 Assignment
**Assignment** means putting a **new value** into a variable that already has one.

```java
age = 26;       // assignment (changing the value later)
```

### 2.5 So what is "defining"?

This is a place where Java differs from C/C++.

| Language | Declaration | Definition |
|----------|-------------|------------|
| **C / C++** | Only tells the compiler the name and type exists (`extern int x;`) | Actually **creates** the variable and reserves memory (`int x;`) |
| **Java** | `int x;` (name and type introduced) | Java has no separate "definition" step for variables |

In Java, when you write `int x;` inside a method or class, the variable is created **and** its memory is reserved. So Java people rarely say "define" for variables. In everyday talk:

- **"Declare"** = write `int x;`
- **"Define / initialize"** = give it a value, `x = 10;` or `int x = 10;`

> The word "definition" is used more strictly for **methods and classes**: a method that has a body is *defined*; an `abstract` method or an interface method with no body is only *declared*.

### 2.6 Basic syntax

```
dataType variableName;                  // declaration
dataType variableName = value;          // declaration + initialization
```

```java
int count;                    // declared only
int score = 100;              // declared and initialized
double price = 49.99;
char letter = 'J';
boolean isOpen = true;
String name = "Ali";          // String is a non-primitive type (see section 17)
```

### 2.7 Declaring several variables at once

```java
int a = 1, b = 2, c = 3;      // three ints, all initialized
int x, y, z;                  // three ints, none initialized
int p = 5, q;                 // p is initialized, q is NOT
```

> Be careful: in `int p, q = 5;`, only `q` gets the value 5. `p` stays uninitialized.

### 2.8 Constants with `final`

If you add `final`, the value can be set **once** and never changed.

```java
final double PI = 3.14159;
PI = 3.14;      // ERROR: cannot assign a value to a final variable
```

### 2.9 `var`: letting Java guess the type (Java 10+)

```java
var age = 25;            // Java infers: int
var name = "Ali";        // Java infers: String
```

Important: `var` is **still statically typed**. The type is decided at compile time and cannot change later. It works only for **local variables** with an initial value.

---

## 3. Rules for Naming Variables (Identifiers)

A name you give to a variable, method, class, etc. is called an **identifier**. Java has strict rules.

### 3.1 The hard rules (break them and the code won't compile)

| # | Rule | Valid | Invalid |
|---|------|-------|---------|
| 1 | Can contain **letters, digits, underscore `_` and dollar `$`** only | `total_1`, `$cost` | `my-var`, `my var`, `a#b` |
| 2 | **Cannot start with a digit** | `item1` | `1item` |
| 3 | **Cannot be a reserved word** (keyword) | `count` | `class`, `int`, `new` |
| 4 | **Cannot be `true`, `false` or `null`** (reserved literals) | `isTrue` | `true`, `null` |
| 5 | **No spaces** and no special symbols (`+ - * / . @ # % !`) | `firstName` | `first name`, `first-name` |
| 6 | **Case-sensitive** | `age` and `Age` are two different variables | |
| 7 | **No duplicate name in the same scope** | | `int a = 1; int a = 2;` in the same block |
| 8 | A single underscore `_` alone is **not allowed as a normal name** (since Java 9) | | `int _ = 5;` |

### 3.2 Other facts about names

- **Length:** there is no practical limit on how long a name can be.
- **Letters can be Unicode**, so `int π = 3;` or `int café = 1;` are legal. But avoid them in team projects; stick to English letters.
- **`$` and `_` are legal** but you should avoid them in normal names. `$` is meant for machine-generated code.
- **`var`, `record`, `yield`, `sealed`, `permits`** are *contextual* keywords. They are special only in certain places, so `int var = 5;` still compiles. But don't do it; it's confusing.
- **Words like `String`, `System`, `Integer`, `main` are NOT reserved.** They are just normal class/method names (you can technically name a variable `String`, but never do it).
- Since Java 22, a single `_` has a new meaning: an **unnamed variable** that you deliberately don't use.

### 3.3 Valid and invalid examples

```java
int age;            // valid
int _count;         // valid (but not recommended)
int $money;         // valid (but not recommended)
int student2;       // valid
int totalMarks;     // valid, and good style

int 2students;      // INVALID: starts with a digit
int total marks;    // INVALID: contains a space
int my-name;        // INVALID: hyphen is the minus operator
int class;          // INVALID: reserved word
int null;           // INVALID: reserved literal
int Age; int age;   // both valid and DIFFERENT (case-sensitive)
```

### 3.4 Naming conventions (not enforced by the compiler, but every professional follows them)

| What | Convention | Example |
|------|------------|---------|
| **Variables and methods** | `camelCase`: first word lowercase, next words start with capital | `studentAge`, `calculateTotal()` |
| **Classes and interfaces** | `PascalCase`: every word starts with capital | `StudentRecord`, `Runnable` |
| **Constants** (`static final`) | `UPPER_SNAKE_CASE` | `MAX_SIZE`, `PI` |
| **Packages** | all lowercase | `com.example.shop` |
| **Boolean variables** | start with `is`, `has`, `can` | `isActive`, `hasLicense` |

Good-naming tips:

- Use **meaningful names**: `totalPrice` is better than `tp` or `x`.
- Single letters are okay only for tiny loop counters like `i`, `j`.
- Don't use names that are too similar (`data1`, `data2`, `data3`...).

---

## 4. Reserved Words in Java

**Reserved words** are words Java keeps for itself. You cannot use them as variable names.

### 4.1 How many are there?

The classic number you will find in books and interviews is:

> **53 reserved words = 50 keywords + 3 reserved literals (`true`, `false`, `null`)**

And of those 50 keywords:

> **48 are actually used + 2 are reserved but NEVER used (`goto` and `const`)**

```
 53 reserved words
 ├── 50 keywords
 │     ├── 48 used keywords
 │     └──  2 unused keywords   → goto, const
 └──  3 reserved literals       → true, false, null
```

> Newer versions: Java 9 also made the single underscore `_` a keyword, so modern counts can say 51 keywords. The "53" figure is still the classic exam answer. Also, "contextual keywords" like `var`, `record`, `yield`, `sealed` and `permits` are special only in certain contexts, so they are not counted in the main list.

### 4.2 The full list by category

| Category | Keywords |
|----------|----------|
| **Primitive types (8)** | `byte` `short` `int` `long` `float` `double` `char` `boolean` |
| **Flow control (11)** | `if` `else` `switch` `case` `default` `for` `while` `do` `break` `continue` `return` |
| **Exception handling (5)** | `try` `catch` `finally` `throw` `throws` |
| **Modifiers (11)** | `public` `private` `protected` `static` `final` `abstract` `synchronized` `volatile` `transient` `native` `strictfp` |
| **Class and object related (11)** | `class` `interface` `extends` `implements` `new` `this` `super` `instanceof` `enum` `package` `import` |
| **Other (2)** | `void` `assert` |
| **Unused but reserved (2)** | `goto` `const` |
| **Reserved literals (3)** | `true` `false` `null` |

Count check: 8 + 11 + 5 + 11 + 11 + 2 + 2 = **50 keywords**, plus 3 literals = **53**.

### 4.3 Fun facts

**Fun fact 1: `goto` and `const` are reserved but do nothing.**
The Java designers reserved them on purpose:

- **`goto`**: famous "bad practice" that makes spaghetti code. Java deliberately doesn't allow it, but reserved the word so nobody could use it as a name and confuse C/C++ programmers. Java gives safer tools instead: **labeled `break` and `continue`**.
- **`const`**: Java uses **`final`** to make constants. The word `const` stays reserved in case it is ever needed.

```java
int goto = 5;     // ERROR even though goto does nothing
int const = 1;    // ERROR as well
```

**Fun fact 2:** `true`, `false` and `null` look like keywords but are technically **literals**. They are still reserved.

**Fun fact 3:** Java has **no `sizeof`** keyword (unlike C), no `unsigned` keyword, and no `string` keyword. `String` is a class, not a keyword.

**Fun fact 4:** `strictfp` still exists as a keyword, but since Java 17 it has no effect (all floating-point math is strict now).

---

## 5. Data Types: The Big Picture

A **data type** tells Java:

1. **What kind of values** the variable can hold (whole numbers, decimals, text, true/false).
2. **How much memory** to reserve for it.
3. **What operations** are allowed on it.

Java splits all data types into **two big families**:

```
                           Java Data Types
                                  │
              ┌───────────────────┴───────────────────┐
              ▼                                       ▼
       PRIMITIVE TYPES                       NON-PRIMITIVE (REFERENCE) TYPES
       (8 built-in basic types)              (everything else)
              │                                       │
   ┌──────────┼──────────┐              ┌─────────────┼──────────────┐
   ▼          ▼          ▼              ▼             ▼              ▼
 Numeric    char      boolean         String       Arrays      Classes, Interfaces,
   │       (character) (true/false)  (text)      (int[] ...)   Enums, Records,
   │                                                            Wrapper classes
   ├── Integral (whole numbers): byte, short, int, long (+ char)
   └── Floating-point (fractions): float, double
```

### Quick difference

| Point | Primitive | Non-primitive (Reference) |
|-------|-----------|---------------------------|
| **What it stores** | The **actual value** directly | A **reference** (address) pointing to an object |
| **Where defined** | Built into the language | Created by Java or by you (classes) |
| **Starts with** | Lowercase keyword (`int`) | Usually Capital letter (`String`, `Integer`) |
| **Can be `null`?** | **No** | **Yes** |
| **Has methods?** | No | Yes (`name.length()`) |
| **Number of types** | Exactly **8** | Unlimited (you can create your own) |
| **Size** | Fixed | Depends on the object |

We will explore each family in detail. First, we need to understand how a computer stores numbers, because that explains all the ranges.

---

## 6. Bits, Bytes and How Numbers Are Really Stored

This is the section that unlocks everything else. Take your time here.

### 6.1 What is a bit and a byte?

- A **bit** is the smallest unit of data. It is either **0** or **1**.
- A **byte** is a group of **8 bits**.

A computer's memory is just a huge sequence of 0s and 1s. There are no "numbers", "letters" or "decimals" inside. **The type decides how to read those bits.**

### 6.2 How many values can N bits represent?

With **N bits**, you get **2^N different patterns**.

| Bits | Patterns (2^N) |
|------|----------------|
| 1 | 2 (0, 1) |
| 2 | 4 (00, 01, 10, 11) |
| 3 | 8 |
| 8 | **256** |
| 16 | 65,536 |
| 32 | 4,294,967,296 |
| 64 | 18,446,744,073,709,551,616 |

### 6.3 The "255" idea (unsigned, positive only)

If we use all 8 bits only for **non-negative numbers** (0 and up), we get 256 patterns, so the numbers are **0 to 255**.

**Formula (unsigned):**

```
largest number = 2^N − 1
8 bits → 2^8 − 1 = 256 − 1 = 255
```

Why "minus 1"? Because counting **starts at 0**, not at 1. 256 patterns cover 0, 1, 2, ..., 255.

Proof, with place values:

```
Bit position:   7    6    5    4    3    2    1    0
Place value:   128   64   32   16    8    4    2    1
All ones:       1    1    1    1    1    1    1    1

128+64+32+16+8+4+2+1 = 255
```

So you are 100% right: **8 bits can represent 256 values, the largest being 255 if all are positive.**

### 6.4 So why is Java's `byte` -128 to 127?

Because Java's integer types are **signed**: they must store **negative numbers too**. We still have only **256 patterns**, so we must split them between negatives, zero and positives:

```
 256 patterns split as:
   128 negative numbers : -128 ... -1
     1 zero             :  0
   127 positive numbers :  1 ... 127
   ─────────────────────────────
   Total = 128 + 1 + 127 = 256 ✓
```

The count of values is the same (256). Only the **labels** are shifted: instead of `0 to 255`, it is `-128 to 127`.

**Formula (signed, N bits):**

```
smallest = −2^(N−1)
largest  =  2^(N−1) − 1

8 bits : −2^7 = −128     to   2^7 − 1 = 127
```

### 6.5 How are negative numbers actually stored? Two's Complement

Java (and almost every computer) uses **two's complement**. The trick is simple:

> The **leftmost bit** (the highest bit) has a **negative** place value. All the others stay positive.

For 8 bits the place values become:

```
Bit position:    7     6    5    4    3    2    1    0
Place value:   −128   +64  +32  +16   +8   +4   +2   +1
```

Let's decode some patterns:

| Binary | Calculation | Value |
|--------|-------------|-------|
| `00000000` | 0 | **0** |
| `00000001` | +1 | **1** |
| `00000101` | 4 + 1 | **5** |
| `01111111` | 64+32+16+8+4+2+1 | **127** (largest) |
| `10000000` | −128 | **−128** (smallest) |
| `10000001` | −128 + 1 | **−127** |
| `11111011` | −128 + 64+32+16+8 + 2+1 = −128 + 123 | **−5** |
| `11111111` | −128 + 127 | **−1** |

So:
- If the **leftmost bit is 0** → the number is **positive** (or zero): 0 to 127.
- If the **leftmost bit is 1** → the number is **negative**: −128 to −1.

That leftmost bit is called the **sign bit**. It is also why we only have **7 bits left for the "size" of a positive number**, so the largest positive is 127 (2^7 − 1), not 255.

### 6.6 How to turn a positive number into its negative (the easy recipe)

**Invert all bits, then add 1.**

Example: find the bits for **−5** in 8 bits.

```
 5  = 0000 0101
 invert → 1111 1010
 add 1  → 1111 1011   ← this is −5   (matches the table above ✓)
```

### 6.7 Why do computers use two's complement?

1. **Only one zero.** (No confusing "+0" and "−0".)
2. **Addition and subtraction use the same circuit.** `5 + (−5)` just works:
   ```
     0000 0101   (5)
   + 1111 1011   (−5)
   ─────────────
   1 0000 0000   → the extra 9th bit is dropped → 0000 0000 = 0 ✓
   ```
3. It is simple and fast in hardware.

### 6.8 Why is the range uneven? (−128 but only +127)

Zero takes one of the "non-negative" patterns, so the negative side has one extra number. **−128 has no positive twin**: there is no +128 in a signed byte.

### 6.9 The same bits can mean different things!

Look at the pattern `11111111`:

| If the type is... | It means |
|-------------------|----------|
| unsigned 8-bit | **255** |
| signed 8-bit (Java `byte`) | **−1** |

Memory doesn't know. **The data type tells how to interpret the bits.** That is exactly why data types exist.

### 6.10 Overflow: what happens at the edges

Numbers wrap around like a car's odometer:

```java
byte b = 127;
b++;                        // b becomes -128 !
System.out.println(b);      // -128
```

```
... 125, 126, 127, -128, -127, -126 ...
                ↑ wraps around
```

If you really need the unsigned view (0 to 255) of a byte:

```java
byte b = (byte) 200;                    // stored as 11001000, which is -56
int unsigned = b & 0xFF;                // 200
int alsoUnsigned = Byte.toUnsignedInt(b);   // 200
```

### 6.11 The general table (signed)

| Bits | Formula | Smallest | Largest |
|------|---------|----------|---------|
| 8 (byte) | −2^7 to 2^7−1 | −128 | 127 |
| 16 (short) | −2^15 to 2^15−1 | −32,768 | 32,767 |
| 32 (int) | −2^31 to 2^31−1 | −2,147,483,648 | 2,147,483,647 |
| 64 (long) | −2^63 to 2^63−1 | −9,223,372,036,854,775,808 | 9,223,372,036,854,775,807 |

The same formula works for every size. That is the "secret formula" you were wondering about.

---

## 7. The 8 Primitive Types

### 7.1 Why are they called "primitive"?

(You wrote "primary"; the correct word is **primitive**.)

In programming, *primitive* means **basic, fundamental, the simplest building block**, not "old-fashioned". They are called primitive because:

1. They are the **most basic data types**, built directly into the language.
2. They are **not objects** and are not made from other types. Everything else in Java is built *on top of* them.
3. They hold the **raw value directly** in memory (not a reference to something else).
4. They have **no methods** and no extra features. They just hold data.
5. They map almost directly to what the **CPU** understands (integers and floating-point numbers), so they are **very fast and memory-efficient**.

### 7.2 Master table

| Type | Category | Size | Range | Default | Example |
|------|----------|------|-------|---------|---------|
| `byte` | Integral | 8 bits (1 byte) | −128 to 127 | `0` | `byte b = 100;` |
| `short` | Integral | 16 bits (2 bytes) | −32,768 to 32,767 | `0` | `short s = 30000;` |
| `int` | Integral | 32 bits (4 bytes) | −2,147,483,648 to 2,147,483,647 | `0` | `int i = 100000;` |
| `long` | Integral | 64 bits (8 bytes) | −9,223,372,036,854,775,808 to 9,223,372,036,854,775,807 | `0L` | `long l = 5000000000L;` |
| `float` | Floating-point | 32 bits (4 bytes) | about ±3.4028235 × 10^38 (≈ 6 to 7 digits of precision) | `0.0f` | `float f = 3.14f;` |
| `double` | Floating-point | 64 bits (8 bytes) | about ±1.7976931348623157 × 10^308 (≈ 15 to 16 digits of precision) | `0.0d` | `double d = 3.14159;` |
| `char` | Integral (unsigned) | 16 bits (2 bytes) | 0 to 65,535 (`'\u0000'` to `'\uffff'`) | `'\u0000'` | `char c = 'A';` |
| `boolean` | Logical | not precisely defined (see section 11) | `true` or `false` | `false` | `boolean ok = true;` |

### 7.3 Professional classification

Here is how professionals (and the Java Language Specification) group them:

```
Primitive types
├── boolean type
└── numeric types
      ├── integral types            (exact whole numbers)
      │     ├── byte, short, int, long    (signed integers)
      │     └── char                      (unsigned 16-bit, holds a character code)
      └── floating-point types      (numbers with a fractional part, approximate)
            ├── float   (single precision, 32-bit, IEEE 754)
            └── double  (double precision, 64-bit, IEEE 754)
```

Plain-language mapping for your "decimal or not" question:

| Casual word | Professional term | Java types |
|-------------|-------------------|------------|
| "Whole numbers, no decimal point" | **Integral types** (integer types) | `byte`, `short`, `int`, `long` (and `char`) |
| "Numbers with decimal point" | **Floating-point types** | `float`, `double` |
| "Text character" | **Character type** | `char` |
| "True/false" | **Boolean type** | `boolean` |

> Careful with the word *decimal*: in professional language, "decimal" means **base-10**. Java's `float` and `double` are **binary floating-point** (IEEE 754), not true decimal types. For exact decimal math (like money) Java offers the class `BigDecimal`. More in section 9.

### 7.4 Syntax recap for declaring primitives

```java
byte    smallNumber = 100;
short   mediumNumber = 20000;
int     normalNumber = 1000000;
long    bigNumber = 9000000000L;     // note the L
float   price = 19.99f;              // note the f
double  pi = 3.141592653589793;
char    grade = 'A';                 // single quotes
boolean isJavaFun = true;
```

### 7.5 Handy constants so you never have to memorize ranges

```java
System.out.println(Byte.MIN_VALUE + " to " + Byte.MAX_VALUE);        // -128 to 127
System.out.println(Short.MIN_VALUE + " to " + Short.MAX_VALUE);      // -32768 to 32767
System.out.println(Integer.MIN_VALUE + " to " + Integer.MAX_VALUE);  // -2147483648 to 2147483647
System.out.println(Long.MIN_VALUE + " to " + Long.MAX_VALUE);
System.out.println(Double.MAX_VALUE);                                // 1.7976931348623157E308
```

---

## 8. Integer Types: byte, short, int, long

All four store **whole numbers** (no fractions), **exactly**, as **signed two's complement** numbers.

### 8.1 `byte`: 8 bits

- Range: **−128 to 127**
- Use for: raw binary data (files, network packets, image data), or large arrays where memory matters.

```java
byte level = 100;
byte tooBig = 128;       // ERROR: 128 doesn't fit in a byte
```

### 8.2 `short`: 16 bits

- Range: **−32,768 to 32,767**
- Rarely used. Handy for big arrays of small numbers or old file formats. (Formula: −2^15 to 2^15−1.)

```java
short temperature = -300;
```

### 8.3 `int`: 32 bits (the default whole-number type)

- Range: **−2,147,483,648 to 2,147,483,647** (about ±2.1 billion)
- This is the **default type of any whole-number literal** such as `42`.
- Use for: counters, ages, indexes, most everyday integers.

```java
int population = 1_400_000_000;     // fits
int tooLarge = 3_000_000_000;       // ERROR: integer number too large
```

### 8.4 `long`: 64 bits

- Range: **−9,223,372,036,854,775,808 to 9,223,372,036,854,775,807** (about ±9.2 quintillion)
- Use for: huge counts, timestamps (milliseconds since 1970), file sizes, IDs, factorials.
- A `long` literal **must end with `L`** if the number doesn't fit in an `int`. Use a capital `L` (lowercase `l` looks like the digit `1`).

```java
long worldPopulation = 8_100_000_000L;     // L needed: bigger than int
long smallButLong = 50L;                   // L optional here: 50 fits in int anyway
long timestamp = System.currentTimeMillis();
```

### 8.5 Integer arithmetic facts

```java
int a = 5 / 2;          // 2     (integer division drops the fraction)
double b = 5 / 2.0;     // 2.5   (one side is double)
int c = 5 % 2;          // 1     (remainder)
int d = 1 / 0;          // ArithmeticException: / by zero
```

**Surprise rule:** in Java, arithmetic on `byte`, `short` and `char` is done in **`int`**. So this fails:

```java
byte x = 10, y = 20;
byte z = x + y;          // ERROR: x + y is an int
byte z2 = (byte)(x + y); // OK: cast back to byte
```

**Overflow is silent for integers:**

```java
int max = Integer.MAX_VALUE;     // 2147483647
System.out.println(max + 1);     // -2147483648  (wraps around, no error!)

int safe = Math.addExact(max, 1);   // throws ArithmeticException instead
```

### 8.6 Quick choosing guide

| Need | Pick |
|------|------|
| Normal whole numbers | **`int`** |
| Numbers beyond ±2.1 billion | **`long`** |
| Raw bytes, tiny memory | `byte` |
| Rare, special cases | `short` |

---

## 9. Floating-Point Types: float and double

These store numbers **with a fractional part** (like 3.14), also very big or very tiny numbers.

### 9.1 How are they stored? (IEEE 754)

A floating-point number is stored like **scientific notation**, but in binary. It has three parts:

```
  float  (32 bits):  [ 1 sign bit ][ 8 exponent bits ][ 23 mantissa bits ]
  double (64 bits):  [ 1 sign bit ][ 11 exponent bits ][ 52 mantissa bits ]
```

- **Sign**: positive or negative.
- **Exponent**: how big/small (the "×10^something" idea, but in powers of 2).
- **Mantissa (significand)**: the actual digits (the precision).

That is why they can hold numbers as huge as 10^308 or as tiny as 10^−324 using only 64 bits. They trade **exactness** for **range**.

### 9.2 `float` vs `double`

| Feature | `float` | `double` |
|---------|---------|----------|
| Name | **Single precision** | **Double precision** |
| Size | 32 bits (4 bytes) | 64 bits (8 bytes) |
| Approx. max | ±3.4028235 × 10^38 | ±1.7976931348623157 × 10^308 |
| Smallest positive | about 1.4 × 10^−45 | about 4.9 × 10^−324 |
| Precision | about **6 to 7** significant digits | about **15 to 16** significant digits |
| Literal | needs **`f`** or **`F`**: `3.14f` | default for decimals: `3.14` (or `3.14d`) |
| Default value | `0.0f` | `0.0d` |

### 9.3 The default literal rule (very common error)

Every number with a decimal point is a **`double` by default**.

```java
float f1 = 3.14;      // ERROR: 3.14 is a double; possible lossy conversion
float f2 = 3.14f;     // OK
double d = 3.14;      // OK
```

### 9.4 Precision difference in action

```java
float  f = 1.123456789f;
double d = 1.123456789;

System.out.println(f);   // 1.1234568     (digits lost after ~7)
System.out.println(d);   // 1.123456789
```

Another float surprise:

```java
float big = 16777216f;
System.out.println(big + 1);   // 1.6777216E7 (the +1 is lost!)
```

### 9.5 Why professionals use `double` (and not `float`) in production

You were right to wonder. Here are the real reasons:

1. **It is the default.** `3.14` is already a `double`. And `Math.sqrt()`, `Math.pow()`, `Math.sin()` and most standard APIs take and return `double`.
2. **Much better precision.** 15 to 16 digits versus 6 to 7. Small errors in `float` pile up quickly in calculations and become visible.
3. **No speed penalty on modern computers.** 64-bit CPUs handle `double` as fast as `float` for normal code. The memory saving of `float` rarely matters.
4. **Fewer bugs.** No need to remember the `f` suffix or deal with lossy conversion errors.
5. **When is `float` used then?** Where memory or bandwidth is the real limit: **huge arrays** (millions of values), **graphics/GPU/game engines**, **machine learning**, **sensor data**. In those cases halving the size is worth the lost precision.

### 9.6 Floating-point is approximate (important!)

Computers store numbers in **binary**, and some simple decimals (like 0.1) **cannot be stored exactly in binary**, just like 1/3 can't be written exactly in decimal (0.3333...).

```java
System.out.println(0.1 + 0.2);        // 0.30000000000000004  (not exactly 0.3!)
System.out.println(0.1 + 0.2 == 0.3); // false
```

> **Never use `float` or `double` for money!** Use **`BigDecimal`**, or store the amount in whole cents using `long`.

```java
import java.math.BigDecimal;

BigDecimal a = new BigDecimal("0.1");
BigDecimal b = new BigDecimal("0.2");
System.out.println(a.add(b));         // 0.3  (exact)
```

(Always create `BigDecimal` from a **String**, `"0.1"`, not from the double `0.1`.)

### 9.7 Special floating-point values

```java
System.out.println(1.0 / 0);     // Infinity     (no exception for floating-point!)
System.out.println(-1.0 / 0);    // -Infinity
System.out.println(0.0 / 0);     // NaN          ("Not a Number")
```

- `NaN` is not equal to anything, **not even itself**: `Double.NaN == Double.NaN` is `false`. Use `Double.isNaN(x)`.
- Compare floating-point values with a small tolerance, not with `==`.

### 9.8 Syntax recap

```java
float  temperature = 36.6f;
double distance = 384_400.5;
double smallNumber = 0.000_001;
```

---

## 10. The char Type: ASCII and Unicode

### 10.1 What is `char`?

`char` stores **one single character**: a letter, digit, symbol, etc. Use **single quotes**:

```java
char letter = 'A';
char digit = '7';          // this is the character '7', not the number 7
char symbol = '$';
```

(Double quotes `"A"` make a **String**, a different type.)

### 10.2 Computers only know numbers, so what's a character?

A computer cannot store the letter "A". It stores a **number**, and everyone agrees that this number means "A". The agreement table is called a **character set / encoding**.

### 10.3 ASCII: the old standard

**ASCII** (American Standard Code for Information Interchange, 1960s):

- Uses **7 bits** → 2^7 = **128 characters** (codes 0 to 127).
- Contains English letters (upper and lower), digits, punctuation and some control codes.
- Extended versions used **8 bits** → 256 characters, but each country invented its own extra 128, which caused a mess.

Some useful codes:

| Character | ASCII code |
|-----------|------------|
| `'0'` to `'9'` | 48 to 57 |
| `'A'` to `'Z'` | 65 to 90 |
| `'a'` to `'z'` | 97 to 122 |
| space `' '` | 32 |
| newline `'\n'` | 10 |

Notice: `'a'` (97) is exactly `'A'` (65) + 32.

ASCII has a huge problem: **no room for other languages** (Hindi, Urdu, Arabic, Chinese, Japanese, emoji...).

### 10.4 Unicode: one table for every character in the world

**Unicode** is a universal standard that gives **every character in every language** its own unique number, called a **code point**, written like `U+0041` (that's `'A'`).

- The first 128 Unicode code points are **identical to ASCII**, so old ASCII text still works.
- Unicode defines over **1.1 million possible code points** (`U+0000` to `U+10FFFF`) and covers well over 100,000 characters today, including emoji.

### 10.5 Java chose Unicode from day one: that's why `char` is 16 bits

When Java was designed (early 1990s), Unicode fit in **16 bits** (65,536 characters). So Java's `char` became a **16-bit unsigned** number: **0 to 65,535**. Languages like C used 8-bit `char` (ASCII). **This is the "bit value increased" idea you mentioned: ASCII 7 bits → 8 bits → Java 16-bit Unicode.**

| Encoding | Bits per character | Characters possible |
|----------|--------------------|---------------------|
| ASCII | 7 | 128 |
| Extended ASCII | 8 | 256 |
| Java `char` (Unicode, UTF-16 unit) | 16 | 65,536 |

### 10.6 But later, Unicode grew beyond 65,536...

Unicode eventually needed more than 16 bits (for emoji and rare scripts). Java handled it by using **UTF-16 encoding**:

- Characters in the first 65,536 (the "Basic Multilingual Plane") = **one `char`**.
- Characters beyond that (like 😀) = **two `char`s** joined together, called a **surrogate pair**.

```java
String smile = "😀";
System.out.println(smile.length());                        // 2  (two chars!)
System.out.println(smile.codePointCount(0, smile.length())); // 1  (one real character)
```

So: a single Java `char` holds one **UTF-16 code unit**. For most text it equals one character, but not always for emoji.

### 10.7 Ways to write a char

```java
char a = 'A';          // the character itself
char b = 65;           // by number: 65 is 'A'
char c = '\u0041';     // Unicode escape: also 'A'
char hindi = 'अ';      // Unicode letters work
char tab = '\t';       // escape sequence
```

### 10.8 Escape sequences (special characters)

| Escape | Meaning |
|--------|---------|
| `\n` | New line |
| `\t` | Tab |
| `\r` | Carriage return |
| `\b` | Backspace |
| `\f` | Form feed |
| `\\` | A backslash |
| `\'` | Single quote |
| `\"` | Double quote |
| `\0` | Null character (octal escape) |
| `\uXXXX` | Unicode character by hex code |

### 10.9 char is really a number (unsigned)

```java
char c = 'A';
System.out.println(c + 1);          // 66        (char + int → int)
System.out.println((char)(c + 1));  // B
c++;                                // now 'B'
System.out.println((int) 'A');      // 65

System.out.println((int) Character.MAX_VALUE);   // 65535
```

`char` is the **only unsigned** integral type in Java, which is why its range is **0 to 65,535** (no negatives).

### 10.10 Useful `Character` methods

```java
Character.isDigit('5');       // true
Character.isLetter('x');      // true
Character.isUpperCase('A');   // true
Character.toLowerCase('A');   // 'a'
```

---

## 11. The boolean Type

- Holds only **`true`** or **`false`**.
- Used for decisions: `if`, `while`, comparisons.
- Default value: **`false`**.

```java
boolean isRaining = true;
boolean isAdult = age >= 18;        // result of a comparison is a boolean

if (isRaining) {
    System.out.println("Take an umbrella");
}
```

### Key rules

- **`true` and `false` are not numbers.** Unlike C, you cannot write `if (1)` or store `1` in a boolean.
  ```java
  boolean b = 1;          // ERROR
  int x = true;           // ERROR
  if (x = 5) { }          // ERROR: x = 5 is an int, not boolean (this C bug can't happen in Java)
  ```
- **Size:** the Java specification doesn't fix the exact size of a boolean. Conceptually it needs **1 bit**, but in practice the JVM usually uses **1 byte** (in arrays) or a whole **int** (for single local variables), because memory is addressed in bytes, not single bits.
- Logical operators: `&&` (and), `||` (or), `!` (not).

---

## 12. Number Systems: Decimal, Binary, Octal, Hexadecimal

A **number system (base)** is just a way of writing numbers using a certain number of digits.

### 12.1 The four systems

| System | Base | Digits used | Java prefix | Example (value 255) |
|--------|------|-------------|-------------|---------------------|
| **Decimal** | 10 | 0 to 9 | none | `255` |
| **Binary** | 2 | 0, 1 | `0b` or `0B` | `0b11111111` |
| **Octal** | 8 | 0 to 7 | `0` (leading zero) | `0377` |
| **Hexadecimal** | 16 | 0 to 9 and A to F | `0x` or `0X` | `0xFF` |

In hex, the letters mean: **A=10, B=11, C=12, D=13, E=14, F=15** (upper or lower case both work).

### 12.2 Writing them in Java

```java
int dec = 255;          // decimal
int bin = 0b11111111;   // binary    (Java 7+)
int oct = 0377;         // octal     (leading zero!)
int hex = 0xFF;         // hexadecimal

System.out.println(dec);   // 255
System.out.println(bin);   // 255
System.out.println(oct);   // 255
System.out.println(hex);   // 255   → all are the same number!
```

> **Beware the octal trap:** `int x = 010;` is **8**, not 10! A leading zero means octal. Never put a leading zero on a normal decimal number.

### 12.3 What does it mean? (place values)

Decimal 255 means: 2×100 + 5×10 + 5×1
Binary `1101` means: 1×8 + 1×4 + 0×2 + 1×1 = **13**
Hex `0x2F` means: 2×16 + 15×1 = **47**
Octal `0377` means: 3×64 + 7×8 + 7×1 = **255**

### 12.4 Why do we use binary, octal and hex?

Computers work in **binary**, but long binary strings are painful to read:

```
11111111 10101010 00001111   ← hard to read, easy to make a mistake
0xFFAA0F                     ← same value, short and clear
```

**Hexadecimal** is popular because **1 hex digit = exactly 4 bits** (a "nibble"), so **1 byte = exactly 2 hex digits**. Conversion is trivial:

```
Binary:   1111  1111  1010  1010
Hex:        F     F     A     A      →  0xFFAA
```

Real uses:

| System | Typical uses |
|--------|--------------|
| **Binary** | Bit flags, bit masks, learning how data is stored |
| **Octal** | Unix/Linux file permissions (`chmod 755`), because 1 octal digit = 3 bits (read/write/execute) |
| **Hex** | Colors (`#FF5733`), memory addresses, MAC addresses, byte values, hash values, debugging, low-level code |
| **Decimal** | Everyday human-friendly numbers |

### 12.5 Practical examples

```java
int red   = 0xFF0000;          // color red in RGB hex
int mask  = 0b0000_1111;       // keep only the lowest 4 bits
int perm  = 0755;              // Unix-style permission (octal)
byte raw  = (byte) 0xA5;       // byte value in hex (0xA5 = 165, so cast needed: byte max is 127)
long big  = 0xFFFF_FFFFL;      // hex long
```

### 12.6 Converting between systems with Java methods

```java
Integer.toBinaryString(10);       // "1010"
Integer.toOctalString(8);         // "10"
Integer.toHexString(255);         // "ff"
Integer.toString(255, 2);         // "11111111"  (any base from 2 to 36)

Integer.parseInt("ff", 16);       // 255
Integer.parseInt("1010", 2);      // 10
```

### 12.7 Negative numbers in hex

A hex literal like `0xFFFFFFFF` fits in 32 bits and is stored with the sign bit on, so it equals **−1** as an `int`:

```java
int x = 0xFFFFFFFF;
System.out.println(x);   // -1
```

### 12.8 Converting by hand (quick methods)

**Decimal → Binary:** divide by 2 repeatedly, read remainders from bottom to top.

```
13 ÷ 2 = 6 remainder 1
 6 ÷ 2 = 3 remainder 0
 3 ÷ 2 = 1 remainder 1
 1 ÷ 2 = 0 remainder 1
Read upward → 1101
```

**Binary → Hex:** group bits in 4s from the right.
`1011 0110` → `B` `6` → `0xB6`.

**Binary → Octal:** group bits in 3s from the right.
`011 111 111` → `3` `7` `7` → `0377`.

---

## 13. Scientific Notation (the `e` form)

Scientific notation writes a number as:

```
number × 10^exponent
```

In Java you write this with the letter **`e`** (or **`E`**) for "× 10 to the power of":

| Java literal | Means | Value |
|--------------|-------|-------|
| `1.5e3` | 1.5 × 10³ | 1500.0 |
| `2e2` | 2 × 10² | 200.0 |
| `3.0E-2` | 3.0 × 10⁻² | 0.03 |
| `6.022e23` | 6.022 × 10²³ | 602,200,000,000,000,000,000,000 |
| `1e-9` | 1 × 10⁻⁹ | 0.000000001 |

```java
double distance = 1.496e8;       // 149,600,000.0 (km from Earth to the Sun)
double atom = 1.2e-10;           // 0.00000000012
float small = 2.5e-3f;           // float with scientific notation
double avogadro = 6.022e23;
```

### Rules for scientific notation

1. **The result is always floating-point.** Even `1e3` is a **`double`** (value 1000.0), **not** an `int`.
   ```java
   int x = 1e3;        // ERROR: 1e3 is a double
   int y = (int) 1e3;  // OK: 1000
   double z = 1e3;     // OK
   ```
2. Use `e` or `E`; both work.
3. The exponent must be an **integer** and may have a sign: `e5`, `e+5`, `e-5`.
4. For `float`, add the suffix: `1.5e3f`.
5. It works with **decimal** literals only (hex floating-point uses `p`, a special advanced form like `0x1.0p3` = 8.0).
6. You need digits on both sides of `e`: `e5` alone or `1e` alone is invalid.

### How Java prints them

Java switches to scientific style automatically for very large or very small doubles:

```java
System.out.println(1234567.0);      // 1234567.0
System.out.println(10000000.0);     // 1.0E7      (10 million or more)
System.out.println(0.001);          // 0.001
System.out.println(0.0001);         // 1.0E-4     (smaller than 0.001)
```

To print in normal style, use `printf` or `String.format`:

```java
System.out.printf("%.2f%n", 1.5e7);   // 15000000.00
```

---

## 14. Underscores in Numeric Literals

Since **Java 7**, you can put underscores `_` inside numbers to make them **easier to read**. The compiler **ignores them completely**; they don't change the value.

```java
int million = 1_000_000;                 // same as 1000000
long cardNumber = 1234_5678_9012_3456L;
int binary = 0b1010_1010;
int hex = 0xFF_EC_DE_5E;
double pi = 3.141_592_653;
int weird = 12_34;                       // valid → 1234 (not pretty but legal)
```

### The one golden rule

> **An underscore must have a digit on BOTH sides of it.**

That single rule explains all the cases below. You can use **as many underscores as you like**, in **any group size**, and even several in a row (`1__000` is legal).

### 14.1 Where underscores are **allowed**

```java
int a = 1_000;                // between digits
int b = 1_0_0_0;              // any grouping
int c = 1__000;               // multiple underscores in a row
long d = 9_223_372_036_854_775_807L;
int e1 = 0b1010_0101;         // inside binary digits
int f = 0xCAFE_BABE;          // inside hex digits
int g = 0_7;                  // inside octal (the leading 0 counts as a digit)
double h = 3.14_15;           // after the decimal point, between digits
double i = 1_000.5_5;         // both sides of the decimal point, between digits
double j = 1e1_0;             // inside the exponent digits (e10 with an underscore)
double k = 6.022_140_76e23;   // big scientific number
```

### 14.2 Where underscores are **NOT allowed**

| Invalid literal | Why it fails |
|-----------------|--------------|
| `12_34_` | **At the end** of the number (nothing after it) |
| `_1234` | **At the beginning**: this is read as an identifier (a variable name), not a number |
| `3_.14` | **Right before the decimal point** |
| `3._14` | **Right after the decimal point** |
| `100_L` | **Right before the `L` suffix** |
| `3.14_f` | **Right before the `f` suffix** |
| `1_e3` | **Right before the `e`** (no digit after it) |
| `1e_3` | **Right after the `e`** (no digit before it) |
| `0x_FF` | **Right after the `0x` prefix** (the `x` is not a digit) |
| `0_x25` | **Inside** the `0x` prefix itself |
| `0b_101` | **Right after the `0b` prefix** |
| `1e3_` | **At the end** of the exponent |

```java
int bad1 = 12_34_;        // ERROR
int bad2 = _1234;         // ERROR: _1234 is treated as a variable name
double bad3 = 3_.14;      // ERROR
double bad4 = 3._14;      // ERROR
long bad5 = 100_L;        // ERROR
int bad6 = 0x_FF;         // ERROR
double bad7 = 1_e3;       // ERROR
double bad8 = 1e_3;       // ERROR
```

### 14.3 The scenario you asked about: underscores with `e` (exponent)

```java
double ok1 = 1_000e3;       // OK: underscore is between digits in the mantissa → 1,000,000.0
double ok2 = 1e1_0;         // OK: underscore is between digits in the exponent → 1e10
double ok3 = 1_0.5_0e0_1;   // OK: all underscores sit between digits
double no1 = 1_e3;          // ERROR: underscore touches the `e`
double no2 = 1e_3;          // ERROR: underscore touches the `e`
double no3 = 1e3_;          // ERROR: underscore at the very end
```

### 14.4 Important side note

Underscores exist **only in source code literals**. They don't work when parsing text at runtime:

```java
Integer.parseInt("1_000");   // NumberFormatException!
```

### 14.5 Good style

- Group decimals by **3 digits**: `1_000_000`
- Group binary and hex by **4 digits** (a nibble) or 8 (a byte): `0b1010_1100`, `0xDE_AD_BE_EF`
- Don't invent random grouping; use underscores to help readers, not confuse them.

---

## 15. Default Values and Kinds of Variables

### 15.1 Kinds of variables (by where you declare them)

| Kind | Where declared | Lifetime | Stored in | Default value? |
|------|----------------|----------|-----------|----------------|
| **Local variable** | Inside a method, constructor or block | Until the block ends | **Stack** | **NO**: must be assigned before use |
| **Parameter** | In the method's parentheses | During the method call | Stack | Gets the caller's value |
| **Instance variable** (field) | Inside a class, outside methods, **without** `static` | As long as the object lives | **Heap**, inside the object | **Yes** |
| **Static (class) variable** | Inside a class with `static` | As long as the class is loaded | Shared, **one copy per class** | **Yes** |

```java
public class Student {
    static int totalStudents;      // static variable: one copy shared by all students
    String name;                   // instance variable: each student object has its own
    int age;                       // instance variable

    void study(int hours) {        // hours is a parameter
        int result = hours * 2;    // result is a local variable
    }
}
```

### 15.2 Default values (for fields only)

| Type | Default |
|------|---------|
| `byte`, `short`, `int` | `0` |
| `long` | `0L` |
| `float` | `0.0f` |
| `double` | `0.0d` |
| `char` | `'\u0000'` (the null character) |
| `boolean` | `false` |
| Any reference type (`String`, arrays, objects) | `null` |

### 15.3 Local variables get NO default

```java
void test() {
    int x;
    System.out.println(x);    // ERROR: variable x might not have been initialized
}
```

Java forces you to initialize local variables. This stops bugs caused by random leftover garbage, a classic C problem.

---

## 16. Type Conversion, Casting and Overflow

Sometimes you must move a value from one type to another.

### 16.1 Widening (automatic, safe): small → big

```
byte → short → int → long → float → double
              ↑
            char
```

```java
int i = 100;
long l = i;        // OK, automatic
double d = l;      // OK, automatic
```

### 16.2 Narrowing (manual with a cast, risky): big → small

You must write the target type in parentheses. **Data can be lost.**

```java
double d = 9.99;
int i = (int) d;           // 9    (fraction is cut off, not rounded!)

int big = 130;
byte b = (byte) big;       // -126  (130 doesn't fit in a byte, so it wraps: 130 − 256)

long huge = 5_000_000_000L;
int broken = (int) huge;   // 705032704  (garbage: the high bits are chopped off)
```

### 16.3 Special rules

- A constant that fits is allowed without cast: `byte b = 10;` and `char c = 65;` are OK. `byte b = 200;` is an error.
- Compound operators add a hidden cast: `byte b = 10; b += 5;` is OK.
- `int` → `float` or `long` → `float/double` is allowed automatically but **can lose precision**.
- `boolean` can **never** be converted to or from a number.

### 16.4 A classic overflow bug

```java
long result = 2_000_000_000 + 2_000_000_000;   // wrong! the addition happens in int first
System.out.println(result);                    // -294967296

long correct = 2_000_000_000L + 2_000_000_000L;  // 4000000000
```

---

## 17. Non-Primitive (Reference) Types

### 17.1 What are they?

Every type that is **not one of the 8 primitives** is a **non-primitive**, also called a **reference type** or **object type**.

Main kinds:

| Kind | Examples |
|------|----------|
| **Classes** | `String`, `Scanner`, `ArrayList`, your own `Student` |
| **Arrays** | `int[]`, `String[]` |
| **Interfaces** | `List`, `Runnable` |
| **Enums** | `enum Day { MON, TUE }` |
| **Records** (Java 16+) | `record Point(int x, int y) {}` |
| **Wrapper classes** | `Integer`, `Double`, `Boolean`, ... (section 18) |

### 17.2 How do they work? (reference vs value)

A reference variable does **not** hold the object. It holds a **reference** (like an address) that points to the object on the **heap**.

```java
int age = 25;                      // primitive: value stored directly
String name = "Ali";               // reference: points to a String object
```

```
   STACK                         HEAP
 ┌─────────────┐
 │ age │  25   │               (value stored right here)
 ├─────────────┤               ┌──────────────┐
 │ name│  ●────┼─────────────► │  "Ali"       │
 └─────────────┘               └──────────────┘
```

### 17.3 What this means in practice

**1) Copying primitives copies the value:**

```java
int a = 10;
int b = a;
b = 20;
System.out.println(a);   // 10   (a is unaffected)
```

**2) Copying references copies the address, so both point to the same object:**

```java
int[] x = {1, 2, 3};
int[] y = x;           // y points to the SAME array
y[0] = 99;
System.out.println(x[0]);   // 99  (x sees the change!)
```

```
 x ●──────┐
          ▼
        [ 99, 2, 3 ]
          ▲
 y ●──────┘
```

**3) They can be `null`** (pointing to nothing). Using a null reference crashes:

```java
String s = null;
s.length();       // NullPointerException
```

**4) `==` compares references, `.equals()` compares content:**

```java
String a = new String("hi");
String b = new String("hi");
System.out.println(a == b);        // false  (different objects)
System.out.println(a.equals(b));   // true   (same content)
```

### 17.4 `String`: the most-used reference type

- It is a **class**, not a primitive (capital S).
- Written with **double quotes**: `"Hello"`.
- **Immutable**: once created, the text can never change. Operations create *new* strings.
- Java keeps a **String Pool** to reuse identical string literals and save memory.

```java
String greeting = "Hello";
String full = greeting + " World";    // a new String; greeting is unchanged
System.out.println(full.length());    // 11
System.out.println(full.toUpperCase());
```

### 17.5 Primitive vs Reference: full comparison

| Feature | Primitive | Reference |
|---------|-----------|-----------|
| Number of types | 8 fixed | Unlimited |
| Stores | The actual value | An address to an object |
| Memory location (as local variable) | Stack | Reference on stack, object on heap |
| Default (as field) | `0`, `false`, etc. | `null` |
| Can be `null` | No | Yes |
| Methods | No | Yes |
| Compared with `==` | Compares values | Compares addresses |
| Speed / memory | Faster, smaller | Slower, bigger |
| Allowed in generics (`List<...>`) | **No** | **Yes** |

---

## 18. Wrapper Classes: Integer, Long and Friends

### 18.1 What are wrapper classes?

For **each primitive type**, Java provides a matching **class** that **wraps** (contains) a primitive value inside an object.

| Primitive | Wrapper class |
|-----------|---------------|
| `byte` | `Byte` |
| `short` | `Short` |
| `int` | **`Integer`** |
| `long` | **`Long`** |
| `float` | `Float` |
| `double` | `Double` |
| `char` | **`Character`** |
| `boolean` | `Boolean` |

### 18.2 Are they primitive or non-primitive?

> **Wrapper classes are NON-PRIMITIVE (reference types).**
> `int` is primitive, but **`Integer` is an object**. It lives in the `java.lang` package, extends `Object`, is `final` and immutable (can't be changed after creation). The numeric wrappers all extend the class `Number`.

```
 int x = 5;          primitive → value 5 sits right in the variable
 Integer y = 5;      object    → variable points to an Integer object that holds 5

   STACK                       HEAP
  ┌────────┐
  │ x │ 5  │
  ├────────┤                 ┌───────────────┐
  │ y │ ●──┼───────────────► │ Integer: 5    │
  └────────┘                 └───────────────┘
```

### 18.3 Why do wrappers exist when we already have int, long, etc.?

This is a great question. There are **5 solid reasons**.

**Reason 1: Collections and generics only work with objects.**
`ArrayList`, `HashMap`, etc. can store only objects, never primitives.

```java
ArrayList<int> list = new ArrayList<>();          // ERROR: primitives not allowed
ArrayList<Integer> list = new ArrayList<>();      // OK
list.add(10);                                     // 10 is auto-converted to Integer
```

**Reason 2: A primitive can't be `null`, but sometimes "no value" is needed.**
For example, a database column that may be empty:

```java
int age = null;          // ERROR
Integer age = null;      // OK: means "age unknown"
```

**Reason 3: They provide useful methods and constants.**
Primitives have no methods. Wrappers do:

```java
Integer.parseInt("123");            // text → int: 123
Integer.valueOf("123");             // text → Integer
Integer.MAX_VALUE;                  // 2147483647
Integer.toBinaryString(10);         // "1010"
Integer.compare(5, 9);              // -1
Double.parseDouble("3.14");         // 3.14
Character.isDigit('7');             // true
Long.parseLong("9000000000");
Boolean.parseBoolean("true");
```

**Reason 4: Object-oriented features.**
Wrappers allow a number to be treated as an `Object`: passed to methods that take `Object`, used with polymorphism, `Optional<Integer>`, synchronization, reflection and many APIs that work only with objects.

**Reason 5: Java wanted speed AND object orientation.**
Java's designers kept primitives for **performance**, but the rest of the language is object-based. Wrappers are the **bridge** between the two worlds.

### 18.4 So why keep primitives at all? (Why not use only Integer?)

Because **primitives are much cheaper**:

| | `int` | `Integer` |
|---|-------|-----------|
| Memory | 4 bytes | roughly 16 bytes for the object **plus** a reference to it |
| Allocation | Nothing to allocate | Object created on the heap |
| Garbage collection | None | Must be cleaned up |
| Arrays | `int[]`: values side by side in memory (fast) | `Integer[]`: array of references scattered on the heap (slower) |
| Can be null | No | Yes (and may cause `NullPointerException`) |

**Rule of thumb: use primitives by default. Use wrappers only when you need an object** (collections, generics, nullability).

### 18.5 Autoboxing and unboxing

Since Java 5, Java converts automatically between primitive and wrapper.

- **Autoboxing**: primitive → wrapper (`int` → `Integer`)
- **Unboxing**: wrapper → primitive (`Integer` → `int`)

```java
Integer boxed = 5;          // autoboxing   (really: Integer.valueOf(5))
int plain = boxed;          // unboxing     (really: boxed.intValue())

List<Integer> nums = new ArrayList<>();
nums.add(7);                // autoboxing
int first = nums.get(0);    // unboxing
```

Convenient, but **hidden work** is happening behind the scenes.

### 18.6 Traps to know

**Trap 1: The Integer cache and `==`**

Java caches `Integer` objects for values **−128 to 127**, so boxing the same small number gives the same object:

```java
Integer a = 127, b = 127;
System.out.println(a == b);       // true   (same cached object)

Integer c = 128, d = 128;
System.out.println(c == d);       // false  (two different objects!)
System.out.println(c.equals(d));  // true   ← always use equals() for wrappers
```

**Trap 2: Unboxing `null` crashes**

```java
Integer count = null;
int n = count;            // NullPointerException
```

**Trap 3: Silent slowness in loops**

```java
Long sum = 0L;
for (int i = 0; i < 1_000_000; i++) {
    sum += i;      // unbox, add, box a new Long ... every single time!
}
// Use `long sum = 0L;` instead
```

**Trap 4: Don't use `new Integer(5)`**
The constructors are deprecated (marked for removal). Use `Integer.valueOf(5)` or just autoboxing.

### 18.7 Handy wrapper utilities

```java
Integer.MAX_VALUE        Integer.MIN_VALUE        Integer.SIZE   // 32 (bits)    Integer.BYTES // 4
Long.MAX_VALUE           Double.MAX_VALUE         Byte.SIZE      // 8
Integer.parseInt("42");                  // String → int
Integer.toString(42);                    // int → String
Integer.valueOf(42).doubleValue();       // 42.0
Double.compare(1.5, 2.5);                // -1
Character.getNumericValue('9');          // 9
```

---

## 19. Final Summary, Cheat Sheet and Quiz

### 19.1 Everything in a nutshell

1. A **variable** is a named memory box with a **type**, **name** and **value**. Java is **statically typed**.
2. **Declare**: `int x;` | **Initialize**: `x = 5;` | Both: `int x = 5;`. In Java, "define" usually just means declare and initialize together.
3. **Naming rules:** letters, digits, `_`, `$` only; can't start with a digit; no spaces or symbols; can't be a reserved word; case-sensitive. Convention: `camelCase`.
4. Java has **53 reserved words** (50 keywords + `true`, `false`, `null`). **`goto` and `const` are reserved but never used.**
5. Two families of types: **primitive** (8 built-in basic types that store the value directly) and **non-primitive/reference** (objects, store an address).
6. N bits give 2^N patterns. Unsigned 8 bits = 0 to 255 (2^8 − 1). Java uses **signed two's complement**, so a `byte` is **−128 to 127** (−2^7 to 2^7 − 1). Same 256 patterns, different labels.
7. The **8 primitives:** `byte`, `short`, `int`, `long` (integral), `float`, `double` (floating-point), `char` (character), `boolean` (true/false).
8. **`double`** is preferred over `float` in production: default, more precise (15 to 16 digits), no practical speed loss. Use **`BigDecimal`** for money.
9. **`char`** is a **16-bit unsigned Unicode (UTF-16)** unit (0 to 65,535). ASCII was 7 bits (128 chars); Unicode covers every language.
10. Write numbers in **binary** (`0b`), **octal** (leading `0`), **hex** (`0x`), **scientific** (`1.5e3`). Use **underscores** only **between two digits**.
11. **Wrapper classes** (`Integer`, `Long`, ...) are **non-primitive objects** that exist for collections, `null`, utility methods and OO features. Primitives remain for speed and low memory.

### 19.2 Master cheat sheet

| Type | Bits | Range | Literal example | Default |
|------|------|-------|-----------------|---------|
| `byte` | 8 | −128 to 127 | `byte b = 100;` | 0 |
| `short` | 16 | −32,768 to 32,767 | `short s = 20000;` | 0 |
| `int` | 32 | −2,147,483,648 to 2,147,483,647 | `int i = 1_000_000;` | 0 |
| `long` | 64 | −9.22×10¹⁸ to 9.22×10¹⁸ | `long l = 5_000_000_000L;` | 0L |
| `float` | 32 | ±3.4×10³⁸, 6 to 7 digits | `float f = 3.14f;` | 0.0f |
| `double` | 64 | ±1.8×10³⁰⁸, 15 to 16 digits | `double d = 3.14;` | 0.0 |
| `char` | 16 | 0 to 65,535 | `char c = 'A';` | `'\u0000'` |
| `boolean` | JVM-dependent | `true` / `false` | `boolean ok = true;` | false |

### 19.3 Number literal cheat sheet

| Form | Example | Value |
|------|---------|-------|
| Decimal | `255` | 255 |
| Binary | `0b1111_1111` | 255 |
| Octal | `0377` | 255 |
| Hex | `0xFF` | 255 |
| Long | `255L` | 255 (long) |
| Float | `255f` | 255.0 (float) |
| Double | `255.0` or `255d` | 255.0 (double) |
| Scientific | `2.55e2` | 255.0 (double) |
| With underscores | `1_000_000` | 1000000 |

### 19.4 Mini Quiz

1. What is the largest unsigned number 8 bits can hold, and what is the formula?
2. Why is a Java `byte` range −128 to 127 and not 0 to 255?
3. What is the default type of the literal `3.14`? Of `42`?
4. Why is `float f = 3.14;` an error?
5. How many reserved words does Java have, and which two are never used?
6. Is `Integer` primitive or non-primitive? Name two reasons it exists.
7. Is `12_34_` a valid literal? What about `1e1_0`?
8. What does `int x = 010;` store?
9. Why shouldn't you use `double` for money?
10. How many bits is a Java `char`, and why not 8?

<details>
<summary>Click for answers</summary>

1. **255**, using the formula **2^N − 1** → 2^8 − 1 = 255.
2. Java integers are **signed** (two's complement). The 256 patterns are split into 128 negative numbers, zero, and 127 positive numbers: −2^7 to 2^7 − 1.
3. `3.14` is a **`double`**; `42` is an **`int`**.
4. Because `3.14` is a double, and putting a double into a float is a "possible lossy conversion". Write `3.14f`.
5. **53** (50 keywords + `true`, `false`, `null`). **`goto`** and **`const`** are reserved but never used.
6. **Non-primitive** (a reference type/object). Reasons: collections and generics need objects; it can be `null`; it has utility methods like `parseInt`.
7. `12_34_` is **invalid** (underscore at the end). `1e1_0` is **valid** (underscore between two digits of the exponent).
8. **8**, because a leading zero means octal.
9. Floating-point is binary and approximate (`0.1 + 0.2` gives `0.30000000000000004`). Use `BigDecimal` or whole cents in a `long`.
10. **16 bits**, because Java uses Unicode (UTF-16), which needs more than the 8 bits of ASCII/extended ASCII to represent characters from all languages.

</details>

---

*Keep experimenting: the best way to learn data types is to type small programs and print the results. Happy coding!*
