Java Conditional Statements# Java Conditional Statements — `if`, `if-else`, `else-if`, `switch`

> Easy-language notes for **Java Full Course #7**.
> Goal: understand how each statement works, then understand **`switch` so well you never forget it** — its rules, its limits, how it differs from `if-else`, and why it can be faster.

---

## Table of Contents

1. [What are conditional statements?](#1-what-are-conditional-statements)
2. [The `if` statement](#2-the-if-statement)
3. [The `if-else` statement](#3-the-if-else-statement)
4. [The `if-else-if` ladder](#4-the-if-else-if-ladder)
5. [Nested `if`](#5-nested-if)
6. [Common mistakes with `if`](#6-common-mistakes-with-if)
7. [The `switch` statement (classic)](#7-the-switch-statement-classic)
8. [How to never forget `switch`](#8-how-to-never-forget-switch)
9. [Modern `switch` (arrow `->` and switch expression)](#9-modern-switch-arrow---and-switch-expression)
10. [What you CAN and CANNOT use in `switch`](#10-what-you-can-and-cannot-use-in-switch)
11. [`if-else` vs `switch`](#11-if-else-vs-switch)
12. [Why `switch` can be faster internally](#12-why-switch-can-be-faster-internally)
13. [Common mistakes with `switch`](#13-common-mistakes-with-switch)
14. [Practice questions](#14-practice-questions)
15. [Quick cheat sheet](#15-quick-cheat-sheet)

---

## 1. What are conditional statements?

Normally Java runs code **line by line, top to bottom**.
Conditional statements let the program **make decisions**: run some code only when a condition is true.

| Statement | Use it when |
|-----------|-------------|
| `if` | Run code only if a condition is true |
| `if-else` | Choose between **two** paths |
| `if-else-if` ladder | Choose between **many** paths using conditions |
| `switch` | Choose between **many** paths by matching **one value** against fixed options |

---

## 2. The `if` statement

### Syntax

```java
if (condition) {
    // runs only when condition is true
}
```

### Rules

- The condition must be a **`boolean`** expression (`true` / `false`).
- If the condition is `false`, the block is skipped.

```java
int age = 20;

if (age >= 18) {
    System.out.println("You can vote");
}
// Output: You can vote
```

### Flow

```
        ┌───────────┐
        │ condition │
        └─────┬─────┘
        true  │  false
     ┌────────┴───────┐
     ▼                ▼
 run the block     skip it
     │                │
     └───────┬────────┘
             ▼
      continue program
```

> In Java, `if (1)` or `if (x)` where `x` is an `int` is a **compile error**. Only `boolean` is accepted (unlike C/C++).

---

## 3. The `if-else` statement

Gives **two paths**: one if true, one if false. Exactly **one** of them always runs.

### Syntax

```java
if (condition) {
    // runs when condition is true
} else {
    // runs when condition is false
}
```

### Example

```java
int number = 7;

if (number % 2 == 0) {
    System.out.println("Even");
} else {
    System.out.println("Odd");
}
// Output: Odd
```

---

## 4. The `if-else-if` ladder

Used when there are **more than two** possibilities. Java checks conditions **from top to bottom** and runs the **first** one that is true. After that, the rest of the ladder is skipped.

### Syntax

```java
if (condition1) {
    // ...
} else if (condition2) {
    // ...
} else if (condition3) {
    // ...
} else {
    // runs if none of the conditions above were true
}
```

### Example — grade calculator

```java
int marks = 82;

if (marks >= 90) {
    System.out.println("Grade A");
} else if (marks >= 75) {
    System.out.println("Grade B");
} else if (marks >= 50) {
    System.out.println("Grade C");
} else {
    System.out.println("Fail");
}
// Output: Grade B
```

### Important points

- Only **one** block runs (the first true one).
- The final `else` is optional. It acts as the "none matched" case.
- **Order matters.** If you wrote `marks >= 50` first, then 82 would print "Grade C", because that condition is also true and comes first.
- Put the **most likely** conditions first to save checks.

---

## 5. Nested `if`

An `if` inside another `if`. The inner one is checked only when the outer one is true.

```java
int age = 25;
boolean hasLicense = true;

if (age >= 18) {
    if (hasLicense) {
        System.out.println("You can drive");
    } else {
        System.out.println("Get a license first");
    }
} else {
    System.out.println("Too young to drive");
}
```

Often a nested `if` can be shortened with `&&`:

```java
if (age >= 18 && hasLicense) {
    System.out.println("You can drive");
}
```

---

## 6. Common mistakes with `if`

**1. Missing braces**

```java
if (x > 5)
    System.out.println("A");
    System.out.println("B");   // NOT part of the if! Always prints
```
Without `{ }`, only the **next single statement** belongs to the `if`. Always use braces.

**2. Using `=` instead of `==`**

```java
int x = 5;
if (x = 10) { }     // compile error: int cannot be a condition
```
`=` assigns, `==` compares. (With `boolean`, `if (flag = true)` compiles and silently assigns `true` — a nasty bug.)

**3. Semicolon right after the condition**

```java
if (x > 5);          // empty statement!
{
    System.out.println("Always runs");
}
```

**4. Dangling else** — an `else` belongs to the **nearest** `if`, no matter how it is indented.

```java
if (a > 0)
    if (b > 0)
        System.out.println("both positive");
else                       // belongs to "if (b > 0)", NOT "if (a > 0)"
    System.out.println("?");
```
Braces fix this.

---

## 7. The `switch` statement (classic)

`switch` takes **one value** and jumps straight to the **matching `case`**.

### Syntax

```java
switch (expression) {
    case value1:
        // code
        break;
    case value2:
        // code
        break;
    default:
        // code if nothing matched
}
```

### The parts

| Part | Meaning |
|------|---------|
| `switch (expression)` | The value being checked (called the **selector**) |
| `case value:` | One possible match. The value must be a **constant** |
| `break;` | Stops the switch and jumps out |
| `default:` | Runs when **no** case matches (optional) |

### Example

```java
int day = 3;

switch (day) {
    case 1:
        System.out.println("Monday");
        break;
    case 2:
        System.out.println("Tuesday");
        break;
    case 3:
        System.out.println("Wednesday");
        break;
    default:
        System.out.println("Invalid day");
}
// Output: Wednesday
```

### Step-by-step: how Java runs it

1. Evaluate the expression (`day` → `3`).
2. Find the `case` whose constant equals it (`case 3`).
3. Start running code **from that case onward**.
4. Stop when it hits `break` (or reaches the end of the switch).
5. If no case matches, jump to `default` (if present).

### 7.1 Fall-through (the most important switch behavior)

Without `break`, Java **keeps running into the next cases**, even if they don't match. This is called **fall-through**.

```java
int day = 2;

switch (day) {
    case 1:
        System.out.println("Monday");
    case 2:
        System.out.println("Tuesday");   // match starts here
    case 3:
        System.out.println("Wednesday"); // falls through
    default:
        System.out.println("Default");   // falls through
}
```

Output:

```
Tuesday
Wednesday
Default
```

The `case` label is only an **entry point**. Once inside, Java does **not** re-check anything. It runs downward until `break`.

### 7.2 Fall-through is useful: grouping cases

When several values should do the same thing, stack the cases:

```java
char ch = 'e';

switch (ch) {
    case 'a':
    case 'e':
    case 'i':
    case 'o':
    case 'u':
        System.out.println("Vowel");
        break;
    default:
        System.out.println("Consonant");
}
// Output: Vowel
```

### 7.3 The `default` case

- Optional, but good practice.
- Can be placed **anywhere** in the switch (usually last).
- If `default` is not last, it needs its own `break`, otherwise it falls through into the case below it.
- Runs only if **no case matched**, no matter where it is written.

### 7.4 `switch` with `String`

```java
String command = "stop";

switch (command) {
    case "start":
        System.out.println("Starting...");
        break;
    case "stop":
        System.out.println("Stopping...");
        break;
    default:
        System.out.println("Unknown command");
}
// Output: Stopping...
```
String matching is **case-sensitive** (`"Stop"` would not match `"stop"`). Use `command.toLowerCase()` if you want to ignore case.

### 7.5 `switch` with `enum`

```java
enum Level { LOW, MEDIUM, HIGH }

Level level = Level.MEDIUM;

switch (level) {
    case LOW:
        System.out.println("Low");
        break;
    case MEDIUM:
        System.out.println("Medium");
        break;
    case HIGH:
        System.out.println("High");
        break;
}
```
Inside a classic switch on an enum, write the constant name **without** the enum name (`LOW`, not `Level.LOW`).

### 7.6 Variables inside cases share one scope

```java
switch (x) {
    case 1:
        int a = 10;
        break;
    case 2:
        int a = 20;   // ERROR: 'a' is already defined (same switch block)
        break;
}
```
Fix: wrap each case body in its own `{ }`:

```java
case 1: { int a = 10; break; }
case 2: { int a = 20; break; }
```

---

## 8. How to never forget `switch`

### Picture it: the lift (elevator) buttons

- **`if-else` ladder** = knocking on every door of a building, one by one, asking *"Are you room 305?"* until someone says yes.
- **`switch`** = pressing the button for floor 3 and going **directly** there.

`switch` doesn't ask "is it this? is it that?" repeatedly. It **jumps** to the right place.

### The 6 rules in one line each

| # | Rule | Remember as |
|---|------|-------------|
| 1 | Checks **equality only** (`==`) | "switch = exact match" |
| 2 | Case values must be **constants** | "fixed labels, not variables" |
| 3 | No duplicate case values | "each label is unique" |
| 4 | `break` ends the case | "no break = keeps falling" |
| 5 | `default` = nothing matched | "the else of switch" |
| 6 | Works with `int`-like types, `String`, `enum` | "no long, float, double, boolean" |

### Memory sentence

> **"Switch matches ONE value against FIXED constants, jumps to the match, and falls down until `break`."**

If you remember that sentence, you remember almost everything.

---

## 9. Modern `switch` (arrow `->` and switch expression)

Since **Java 14**, `switch` has a cleaner form that removes most of the old problems.

### 9.1 Arrow syntax `->`

```java
int day = 3;

switch (day) {
    case 1 -> System.out.println("Monday");
    case 2 -> System.out.println("Tuesday");
    case 3 -> System.out.println("Wednesday");
    default -> System.out.println("Invalid day");
}
```

What is different from the classic style:

| Classic `:` | Arrow `->` |
|-------------|-----------|
| Needs `break` | **No `break` needed** |
| Fall-through happens | **No fall-through** |
| One value per `case` (stack to group) | **Many values: `case 1, 7 ->`** |

```java
switch (day) {
    case 1, 7 -> System.out.println("Weekend");
    case 2, 3, 4, 5, 6 -> System.out.println("Weekday");
    default -> System.out.println("Invalid");
}
```

For more than one statement, use a block:

```java
case 3 -> {
    System.out.println("Midweek");
    System.out.println("Keep going!");
}
```

> You cannot mix `:` and `->` in the same switch.

### 9.2 Switch expression (a switch that **returns a value**)

```java
int day = 6;

String type = switch (day) {
    case 1, 7 -> "Weekend";
    case 2, 3, 4, 5, 6 -> "Weekday";
    default -> "Invalid";
};

System.out.println(type);   // Weekday
```

Note the `;` after the closing `}` — it is an expression assigned to a variable.

### 9.3 `yield` — returning a value from a block

```java
int bonus = switch (grade) {
    case 'A' -> 1000;
    case 'B' -> {
        System.out.println("Good job");
        yield 500;              // value returned by this case
    }
    default -> 0;
};
```

### 9.4 Rules for switch expressions

- Every possible input must be handled → write a `default`, **or** cover all enum constants.
- Every case must produce a value (or throw an exception).
- No fall-through.

### 9.5 Pattern matching in `switch` (Java 21+, brief)

Newer Java lets `switch` match on **types** and add conditions with `when`:

```java
static String describe(Object obj) {
    return switch (obj) {
        case Integer i when i > 100 -> "Big number";
        case Integer i              -> "Number";
        case String s               -> "Text of length " + s.length();
        case null                   -> "Null value";
        default                     -> "Something else";
    };
}
```
This is an advanced feature; learn the classic and arrow forms first.

---

## 10. What you CAN and CANNOT use in `switch`

### 10.1 Allowed types for the `switch` expression (selector)

| Allowed ✅ | Not allowed ❌ |
|-----------|---------------|
| `byte` | `long` |
| `short` | `float` |
| `char` | `double` |
| `int` | `boolean` |
| `Byte`, `Short`, `Character`, `Integer` (wrappers) | |
| `String` (Java 7+) | |
| `enum` | |

```java
long big = 5L;
switch (big) { }          // ERROR: long not allowed

double price = 9.5;
switch (price) { }        // ERROR: double not allowed

boolean flag = true;
switch (flag) { }         // ERROR: boolean not allowed
```

**Why no `long`, `float`, `double`?** The switch jumps using an `int`-sized table or lookup (see section 12). Floating-point values also have precision problems, so exact matching isn't reliable. For `boolean`, a simple `if-else` is better since there are only two values.

(Pattern-matching `switch` in Java 21+ allows more reference types, but the primitive types `long`, `float`, `double` and `boolean` are still not usable in normal `switch`.)

### 10.2 Rules for `case` values

**1. Must be a compile-time constant** — a literal, or a `final` variable with a constant value.

```java
int a = 5;
final int b = 5;
final int c = getValue();        // not known at compile time

switch (n) {
    case 1:        break;        // OK: literal
    case b:        break;        // OK: final + constant
    case 2 + 3:    break;        // OK: constant expression
    case a:        break;        // ERROR: not final
    case c:        break;        // ERROR: not a compile-time constant
}
```

**2. Must be unique**

```java
case 1: ...
case 1: ...    // ERROR: duplicate case label
```

**3. Must match the selector's type**

```java
String s = "A";
switch (s) {
    case 1: break;    // ERROR: int vs String
}

byte b = 10;
switch (b) {
    case 200: break;  // ERROR: 200 doesn't fit in a byte
}
```

**4. Cannot be `null`** in a classic switch (`case null` only exists in Java 21+).

### 10.3 What kind of comparison can `switch` do?

> **Only equality** (is the value exactly equal to this constant?).

So all of these are **NOT possible** in a `case`:

```java
case marks > 80:        // ERROR: relational operators not allowed
case x > 5 && x < 10:   // ERROR: logical operators not allowed
case 1..5:              // ERROR: ranges do not exist in Java
case a:                 // ERROR: variable that is not constant
```

### 10.4 How to handle ranges then?

**Option A:** use `if-else-if` (best for real ranges like `marks >= 90`).

**Option B:** convert the range into discrete values:

```java
int marks = 87;

switch (marks / 10) {       // 87 / 10 = 8
    case 10:
    case 9:
        System.out.println("A");
        break;
    case 8:
        System.out.println("B");
        break;
    case 7:
        System.out.println("C");
        break;
    default:
        System.out.println("Below C");
}
// Output: B
```

**Option C:** pattern matching with `when` (Java 21+).

### 10.5 Other things to know

| Point | Detail |
|-------|--------|
| `null` selector | Throws `NullPointerException` for `String`, wrappers and `enum` (unless `case null` is used in Java 21+) |
| `default` | Optional in classic switch. Required (or full coverage) in switch **expressions** |
| Order of cases | Does not matter for correctness (unlike `if-else-if`), except fall-through behavior |
| Nested switch | Allowed — a switch inside a case |
| `break` outside a case | Not needed on the last case, but harmless |
| `continue` | Allowed inside a switch **only if** the switch is inside a loop |
| Empty switch | `switch (x) { }` is legal, but useless |

---

## 11. `if-else` vs `switch`

### 11.1 Side-by-side

| Feature | `if-else` / ladder | `switch` |
|---------|-------------------|----------|
| What it checks | **Any boolean condition** | **Equality with constants only** |
| Ranges (`x > 10`) | ✅ Yes | ❌ No (classic) |
| Multiple conditions (`&&`, `\|\|`) | ✅ Yes | ❌ No |
| Compare different variables | ✅ Yes | ❌ No — only one selector |
| Data types | Anything that gives a `boolean` (`long`, `double`, `boolean`, objects…) | `byte, short, char, int`, wrappers, `String`, `enum` |
| Values to compare | Can be variables, method calls | Must be constants |
| Checking order | **Top to bottom**, one by one | **Direct jump** to the matching case |
| Fall-through | None | Yes (classic) / none (arrow) |
| Readability for many fixed options | Gets long and messy | Clean and neat |
| Speed with many fixed options | Slower (checks one by one) | Usually faster |
| Default option | final `else` | `default` |

### 11.2 Same problem, both ways

**With `if-else`:**

```java
if (day == 1) {
    System.out.println("Monday");
} else if (day == 2) {
    System.out.println("Tuesday");
} else if (day == 3) {
    System.out.println("Wednesday");
} else {
    System.out.println("Invalid");
}
```

**With `switch`:**

```java
switch (day) {
    case 1 -> System.out.println("Monday");
    case 2 -> System.out.println("Tuesday");
    case 3 -> System.out.println("Wednesday");
    default -> System.out.println("Invalid");
}
```

### 11.3 When to use which

**Use `switch` when:**
- You compare **one variable** with **many fixed values** (menu choice, day number, command name, enum state).
- Values are `int`, `char`, `String` or `enum`.
- You want cleaner code.

**Use `if-else` when:**
- You need **ranges** (`age > 18`, `marks >= 90`).
- You need **combined conditions** (`a > 5 && b < 3`).
- You compare **`long`, `double`, `boolean`** or different variables.
- There are only 2–3 simple options.

---

## 12. Why `switch` can be faster internally

### 12.1 How `if-else` works internally

An `if-else-if` ladder is checked **one condition after another**:

```
x == 1 ? → no
x == 2 ? → no
x == 3 ? → no
...
x == 50 ? → yes!   (50 comparisons)
```

If the match is the last option (or no match at all), Java has made **n comparisons**. This is **O(n)** time.

### 12.2 How `switch` works internally

When the Java compiler (`javac`) compiles a `switch`, it does **not** turn it into a chain of `if` checks. It generates a special JVM instruction that **jumps** directly. There are two kinds:

#### `tableswitch` — used when case values are close together (dense)

Example cases: `1, 2, 3, 4, 5`.

The compiler builds a **jump table** (like an array of addresses):

```
index:   0     1     2     3     4
value:   1     2     3     4     5
target: L1    L2    L3    L4    L5
```

To run it, the JVM does only:

1. Check if the value is inside the range (`1` to `5`). If not → `default`.
2. Compute `value - 1` and use it as an **array index**.
3. **Jump** straight to that address.

That is **O(1)** — the same small amount of work whether there are 5 cases or 5000.

#### `lookupswitch` — used when case values are far apart (sparse)

Example cases: `1, 50, 1000, 99999`.

A jump table would waste memory (almost all empty slots), so the compiler stores a **sorted list** of `(value → target)` pairs. The JVM finds the value using **binary search**: **O(log n)**.

#### Which one does the compiler choose?

`javac` estimates the cost of both and picks the better one:
- Values close together → `tableswitch`
- Values spread out → `lookupswitch`

You can see it yourself:

```
javac Demo.java
javap -c Demo
```

Sample output you may see:

```
tableswitch   { // 1 to 3
          1: 28
          2: 34
          3: 40
    default: 46
}
```

### 12.3 What about `String` switch?

The JVM cannot jump on a `String` directly, so the compiler transforms it into **two steps**:

1. **Switch on the `hashCode()`** of the string (an `int`, so a fast `lookupswitch`/`tableswitch`).
2. Inside the matching hash, call **`equals()`** to confirm (because two different strings can share a hash).
3. Then a **second switch** on a small index number runs the real case code.

Simplified idea of what the compiler generates:

```java
int index = -1;
switch (command.hashCode()) {
    case /* hash of "start" */:
        if (command.equals("start")) index = 0;
        break;
    case /* hash of "stop" */:
        if (command.equals("stop")) index = 1;
        break;
}
switch (index) {
    case 0: /* start code */ break;
    case 1: /* stop code */  break;
}
```
So a `String` switch avoids comparing the string against every case — usually one hash computation (Java caches a string's hash) plus one `equals()`.

### 12.4 What about `enum` switch?

The compiler maps each enum constant to a small number (its **ordinal**) and then uses a `tableswitch` — again a direct jump.

### 12.5 Comparison of work done

| Number of cases | `if-else` (worst case) | `switch` |
|-----------------|------------------------|----------|
| 5 | up to 5 comparisons | 1 jump |
| 50 | up to 50 comparisons | 1 jump (`tableswitch`) or ~6 steps (`lookupswitch`) |
| 1000 | up to 1000 comparisons | 1 jump or ~10 steps |

### 12.6 Be honest about real-world speed

- With **just 2–4 cases**, the speed difference is tiny and you won't notice it.
- Modern JVMs (the JIT compiler) optimize `if-else` chains too, so the gap is not always large.
- `switch` shines when there are **many cases** with fixed constant values.
- The bigger benefit is often **cleaner, easier-to-read code**.

> Choose `switch` for **many fixed options** and readability. Choose `if-else` when you need **conditions** (ranges, `&&`, `||`). Don't pick one just to "save nanoseconds".

---

## 13. Common mistakes with `switch`

| Mistake | What happens | Fix |
|---------|--------------|-----|
| Forgetting `break` | Fall-through runs extra cases | Add `break`, or use arrow `->` |
| Using a variable as case | Compile error | Use literals or `final` constants |
| Duplicate case values | Compile error | Make each value unique |
| Using `long`, `double`, `boolean` as selector | Compile error | Use `if-else` or convert to `int` |
| Trying `case x > 5` | Compile error | Use `if-else` or `switch (x / 10)` trick |
| `null` String in switch | `NullPointerException` | Check `if (s != null)` first |
| Same variable name in two cases | Compile error (shared scope) | Add braces `{ }` per case |
| Expecting `"Stop"` to match `"stop"` | No match | Use `toLowerCase()` |
| Mixing `:` and `->` | Compile error | Use only one style |
| `default` in the middle without `break` | Falls into next case | Put `default` last or add `break` |

---

## 14. Practice questions

**Q1.** What is the output?

```java
int x = 2;
switch (x) {
    case 1: System.out.print("A ");
    case 2: System.out.print("B ");
    case 3: System.out.print("C ");
    default: System.out.print("D ");
}
```
**Answer:** `B C D` — starts at `case 2`, no `break`, so it falls through to the end.

---

**Q2.** What is the output?

```java
int x = 5;
switch (x) {
    default: System.out.print("D ");
    case 1: System.out.print("A ");
        break;
    case 2: System.out.print("B ");
}
```
**Answer:** `D A` — no case matches, so `default` runs, then falls into `case 1`, then `break`.

---

**Q3.** Will this compile?

```java
long n = 10;
switch (n) {
    case 10: System.out.println("Ten");
}
```
**Answer:** No. `long` is not allowed as a switch selector.

---

**Q4.** Will this compile?

```java
int a = 3;
switch (x) {
    case a: System.out.println("three");
}
```
**Answer:** No. `a` is not `final`, so it is not a compile-time constant.

---

**Q5.** What does this print?

```java
String s = "java";
switch (s) {
    case "Java": System.out.println("Capital"); break;
    case "java": System.out.println("Small"); break;
    default: System.out.println("None");
}
```
**Answer:** `Small` (matching is case-sensitive).

---

**Q6.** What does this print?

```java
int day = 3;
String r = switch (day) {
    case 1, 2 -> "Early";
    case 3, 4 -> "Mid";
    default -> "Late";
};
System.out.println(r);
```
**Answer:** `Mid`.

---

**Q7.** Why is `switch` often faster than a long `if-else` ladder?
**Answer:** The compiler turns `switch` into `tableswitch` (jump table, O(1)) or `lookupswitch` (binary search, O(log n)), while `if-else` checks conditions one by one (O(n)).

---

**Q8.** Can you write `case x > 10:` in a switch? Why or why not?
**Answer:** No. `case` accepts only constant values (equality match), not conditions.

---

**Q9.** Which types can be used in `switch`?
**Answer:** `byte, short, char, int`, their wrappers, `String`, `enum`.

---

**Q10.** What happens if two cases have the same value?
**Answer:** Compile error: duplicate case label.

---

## 15. Quick cheat sheet

### Statements

```java
// if
if (cond) { ... }

// if-else
if (cond) { ... } else { ... }

// ladder
if (c1) { ... } else if (c2) { ... } else { ... }

// classic switch
switch (value) {
    case 1: ...; break;
    case 2: ...; break;
    default: ...;
}

// modern switch
switch (value) {
    case 1, 2 -> ...;
    default   -> ...;
}

// switch expression
String r = switch (value) {
    case 1 -> "one";
    default -> "other";
};
```

### Key points to remember

1. `if` conditions must be **`boolean`**.
2. In an `if-else-if` ladder, the **first true** condition runs and the rest are skipped. **Order matters.**
3. `switch` = **one value vs fixed constants**, equality only.
4. Allowed selector types: `byte, short, char, int`, wrappers, `String`, `enum`. **Not** `long, float, double, boolean`.
5. `case` values must be **unique compile-time constants**.
6. **No `break` = fall-through** (classic). Arrow `->` has no fall-through.
7. `default` runs when nothing matches; it can be anywhere.
8. No ranges or `&&`/`||` in `case`. Use `if-else` for those.
9. Internally: `tableswitch` (O(1)), `lookupswitch` (O(log n)), `String` via `hashCode()` + `equals()`. `if-else` is O(n).
10. Use `switch` for **many fixed options**; use `if-else` for **conditions and ranges**.

---

*End of notes. Practice by rewriting one `if-else` ladder as a `switch` — and one `switch` that needs a range back as an `if-else`.*
