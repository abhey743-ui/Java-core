# Java Loops — `while`, `do-while`, `for`, `break`, `continue` and Labels

> Easy-language notes, explained in sequence.
> Covers: how each loop works, every legal way to write a `for` loop (even with empty parts), multiple variables in a `for`, `break`, `continue`, and **labeled** `break` / `continue`.

---

## Table of Contents

1. [What is a loop?](#1-what-is-a-loop)
2. [The `while` loop](#2-the-while-loop)
3. [The `do-while` loop](#3-the-do-while-loop)
4. [The `for` loop](#4-the-for-loop)
5. [`for` loop variations (empty parts are allowed!)](#5-for-loop-variations-empty-parts-are-allowed)
6. [Multiple variables in a `for` loop](#6-multiple-variables-in-a-for-loop)
7. [Scope of the loop variable](#7-scope-of-the-loop-variable)
8. [The enhanced `for` loop (for-each)](#8-the-enhanced-for-loop-for-each)
9. [Infinite loops](#9-infinite-loops)
10. [`break`](#10-break)
11. [`continue`](#11-continue)
12. [Labeled loops: `break label` and `continue label`](#12-labeled-loops-break-label-and-continue-label)
13. [Nested loops](#13-nested-loops)
14. [`while` vs `do-while` vs `for`](#14-while-vs-do-while-vs-for)
15. [Common mistakes](#15-common-mistakes)
16. [Practice questions](#16-practice-questions)
17. [Quick cheat sheet](#17-quick-cheat-sheet)

---

## 1. What is a loop?

A **loop** repeats a block of code again and again **as long as a condition is true**.

Without a loop, printing 1 to 5 looks like this:

```java
System.out.println(1);
System.out.println(2);
System.out.println(3);
System.out.println(4);
System.out.println(5);
```

With a loop:

```java
for (int i = 1; i <= 5; i++) {
    System.out.println(i);
}
```

Every loop has **four ideas** behind it:

| Idea | Meaning |
|------|---------|
| **Initialization** | Where the loop starts (e.g., `i = 1`) |
| **Condition** | Keep going while this is `true` (e.g., `i <= 5`) |
| **Body** | The code that repeats |
| **Update** | Change something so the loop eventually ends (e.g., `i++`) |

If you forget the update, the condition may never become false → **infinite loop**.

Java has three loop statements: `while`, `do-while`, `for` (plus the for-each form of `for`).

---

## 2. The `while` loop

Checks the condition **first**, then runs the body. If the condition is false at the start, the body runs **zero times**.

### Syntax

```java
initialization;
while (condition) {
    // body
    update;
}
```

### Flow

```
        ┌──────────────┐
   ┌───►│  condition?  │
   │    └──────┬───────┘
   │      true │      false
   │           ▼          │
   │      run body        │
   │           │          ▼
   └───────────┘     exit the loop
```

### Example: print 1 to 5

```java
int i = 1;                    // initialization
while (i <= 5) {              // condition
    System.out.println(i);    // body
    i++;                      // update
}
```

Output:

```
1
2
3
4
5
```

### Dry run (follow the values)

| Check | `i` | `i <= 5` | Action |
|-------|-----|----------|--------|
| 1st | 1 | true | print 1, `i` becomes 2 |
| 2nd | 2 | true | print 2, `i` becomes 3 |
| 3rd | 3 | true | print 3, `i` becomes 4 |
| 4th | 4 | true | print 4, `i` becomes 5 |
| 5th | 5 | true | print 5, `i` becomes 6 |
| 6th | 6 | **false** | loop ends |

### Example: sum of digits

```java
int n = 1234, sum = 0;
while (n > 0) {
    sum += n % 10;   // add last digit
    n /= 10;         // remove last digit
}
System.out.println(sum);   // 10
```

### When to use `while`

Use it when you **don't know in advance how many times** the loop will run (read until input ends, repeat until a value becomes 0, etc.).

### Body runs zero times if the condition starts false

```java
int x = 10;
while (x < 5) {
    System.out.println("Never printed");
}
```

---

## 3. The `do-while` loop

Runs the body **first**, then checks the condition. So the body runs **at least once**, no matter what.

### Syntax

```java
do {
    // body
} while (condition);     // <-- semicolon is REQUIRED here
```

### Flow

```
   ┌────────────►  run body
   │                  │
   │                  ▼
   │            ┌───────────┐
   │            │ condition?│
   │            └─────┬─────┘
   │          true    │    false
   └──────────────────┘        │
                               ▼
                         exit the loop
```

### Example

```java
int i = 1;
do {
    System.out.println(i);
    i++;
} while (i <= 5);
```

### The key difference from `while`

```java
int x = 10;

while (x < 5) {
    System.out.println("while body");     // NOT printed
}

do {
    System.out.println("do-while body");  // printed ONCE
} while (x < 5);
```

Output:

```
do-while body
```

### When to use `do-while`

Use it when the code **must run at least once**, for example **menus** and **input validation**:

```java
Scanner sc = new Scanner(System.in);
int choice;

do {
    System.out.println("1. Start");
    System.out.println("2. Settings");
    System.out.println("0. Exit");
    System.out.print("Enter choice: ");
    choice = sc.nextInt();
} while (choice != 0);
```

The menu must be shown once before you can even check the user's choice, which is exactly what `do-while` is made for.

---

## 4. The `for` loop

The `for` loop puts **initialization, condition and update on one line**. It is the most used loop when you know how many times to repeat.

### Syntax

```java
for (initialization; condition; update) {
    // body
}
```

### Example

```java
for (int i = 1; i <= 5; i++) {
    System.out.println(i);
}
```

### The exact order of execution (very important)

```
1. initialization     → runs ONCE, at the very start
2. condition          → checked
3. body               → runs if condition is true
4. update             → runs after the body
5. go back to step 2  → condition checked again
   (when the condition is false, the loop ends)
```

Visual:

```
for ( init ; condition ; update ) { body }
       │        │          │         │
       1        2,5        4         3
```

So the order is: `init → cond → body → update → cond → body → update → ... → cond (false) → exit`.

### Dry run of `for (int i = 1; i <= 3; i++)`

| Step | What happens |
|------|--------------|
| 1 | `i = 1` (init, once) |
| 2 | `1 <= 3` true → body runs |
| 3 | `i++` → `i = 2` |
| 4 | `2 <= 3` true → body runs |
| 5 | `i++` → `i = 3` |
| 6 | `3 <= 3` true → body runs |
| 7 | `i++` → `i = 4` |
| 8 | `4 <= 3` false → **loop ends** |

### More examples

**Sum of 1 to n**

```java
int sum = 0;
for (int i = 1; i <= 10; i++) {
    sum += i;
}
System.out.println(sum);   // 55
```

**Counting down**

```java
for (int i = 5; i >= 1; i--) {
    System.out.print(i + " ");
}
// 5 4 3 2 1
```

**Skipping by 2**

```java
for (int i = 0; i <= 10; i += 2) {
    System.out.print(i + " ");
}
// 0 2 4 6 8 10
```

**Multiplication table**

```java
int n = 5;
for (int i = 1; i <= 10; i++) {
    System.out.println(n + " x " + i + " = " + (n * i));
}
```

**Factorial**

```java
int fact = 1;
for (int i = 1; i <= 5; i++) {
    fact *= i;
}
System.out.println(fact);   // 120
```

---

## 5. `for` loop variations (empty parts are allowed!)

Here is the interesting part: **all three parts of a `for` loop are optional.** The two semicolons `;` must always stay, but what is between them can be empty.

```java
for ( [init] ; [condition] ; [update] ) { }
```

### 5.1 Rule for each part

| Part | If you leave it empty |
|------|-----------------------|
| Initialization | Nothing is initialized (the variable must already exist) |
| Condition | Treated as **`true`** → the loop runs forever unless you `break` |
| Update | Nothing is updated automatically (you update inside the body) |

### 5.2 Without initialization

Declare the variable **before** the loop:

```java
int i = 1;
for ( ; i <= 5; i++) {
    System.out.println(i);
}
```
Valid, and `i` is still available **after** the loop, because it was declared outside.

### 5.3 Without update

Do the update inside the body:

```java
for (int i = 1; i <= 5; ) {
    System.out.println(i);
    i++;                     // update moved into the body
}
```

### 5.4 Without initialization and update (it's just a `while`)

```java
int i = 1;
for ( ; i <= 5; ) {
    System.out.println(i);
    i++;
}
```
This behaves exactly like `while (i <= 5)`.

### 5.5 Initialization only: `for (int x = 0; ; )`

You mentioned `for(x,;;)`. The real valid form is to put a proper initialization and leave the other two parts empty:

```java
for (int x = 0; ; ) {
    System.out.println(x);
    x++;
    if (x == 3) break;       // condition is empty (= true), so we must break
}
```
Output: `0 1 2`.

> Note: the first part must be a **declaration** (`int x = 0`) or a **statement** (`x = 0`, `x++`, a method call). Writing only `x` is a compile error ("not a statement"), and a lone trailing comma like `for (x,;;)` is not valid either.

### 5.6 Condition only: `for ( ; i < 5; )`

```java
int i = 0;
for ( ; i < 5; ) {
    i++;
}
```

### 5.7 Everything empty: `for (;;)`

```java
for (;;) {
    System.out.println("Running forever");
}
```
This is an **infinite loop**, the same as `while (true)`. To stop it you must use `break` (or `return`, or an exception).

```java
int count = 0;
for (;;) {
    count++;
    if (count == 5) {
        break;
    }
}
System.out.println(count);   // 5
```

### 5.8 Initialization or update can be any statement, even a method call

```java
int i = 0;
for (System.out.println("Starting"); i < 3; System.out.println("Done " + i), i++) {
    System.out.println("Body " + i);
}
```
Output:

```
Starting
Body 0
Done 0
Body 1
Done 1
Body 2
Done 2
```
You rarely write code like this, but it shows that init and update can be **any expression statement**.

### 5.9 Summary table of valid forms

| Code | Meaning |
|------|---------|
| `for (int i = 0; i < 5; i++)` | Normal loop |
| `for ( ; i < 5; i++)` | No initialization |
| `for (int i = 0; i < 5; )` | No update |
| `for ( ; i < 5; )` | Only a condition (like `while`) |
| `for (int i = 0; ; i++)` | No condition → runs forever unless `break` |
| `for (;;)` | Infinite loop |

---

## 6. Multiple variables in a `for` loop

You can declare and update **more than one variable**. Separate them with a **comma**.

### 6.1 Multiple initializations and updates

```java
for (int i = 0, j = 5; i < j; i++, j--) {
    System.out.println("i = " + i + ", j = " + j);
}
```

Output:

```
i = 0, j = 5
i = 1, j = 4
i = 2, j = 3
```

Then `i = 3, j = 2` → `3 < 2` is false → loop ends.

This "two pointers moving toward each other" style is common in problems like **reversing an array** or **checking a palindrome**:

```java
int[] arr = {1, 2, 3, 4, 5};
for (int left = 0, right = arr.length - 1; left < right; left++, right--) {
    int temp = arr[left];
    arr[left] = arr[right];
    arr[right] = temp;
}
// arr = {5, 4, 3, 2, 1}
```

### 6.2 Rules for multiple variables

**Rule 1:** In the init part, all declared variables must have the **same type**.

```java
for (int i = 0, j = 10; i < j; i++, j--) { }       // OK, both int
for (int i = 0, double d = 1.5; ; ) { }           // ERROR, different types
```
(If you need different types, declare them **before** the loop.)

**Rule 2:** The **condition** is a single boolean expression. You cannot use a comma there. Combine conditions with `&&` or `||`:

```java
for (int i = 0, j = 10; i < 5 && j > 6; i++, j--) { }
```

**Rule 3:** The **update** part can have several expressions separated by commas, and they can be any statement:

```java
int count = 0;
for (int i = 0; i < 5; i++, count += 2) { }
```

**Rule 4:** You can also reuse variables that already exist:

```java
int i, j;
for (i = 0, j = 10; i < j; i++, j--) { }
```

---

## 7. Scope of the loop variable

A variable declared **inside** the `for` header exists **only inside the loop**.

```java
for (int i = 0; i < 3; i++) {
    System.out.println(i);
}
System.out.println(i);    // ERROR: i is out of scope
```

To use it after the loop, declare it **before**:

```java
int i;
for (i = 0; i < 3; i++) { }
System.out.println(i);    // 3  (works)
```

A variable declared **inside the body** is created fresh on every iteration and is gone at the end of each round:

```java
for (int i = 0; i < 3; i++) {
    int square = i * i;     // new variable each time
}
// 'square' does not exist here
```

---

## 8. The enhanced `for` loop (for-each)

A simpler `for` used to go through **every element** of an array or collection.

### Syntax

```java
for (type variable : arrayOrCollection) {
    // use variable
}
```

### Example

```java
int[] marks = {70, 85, 90};

for (int m : marks) {
    System.out.println(m);
}
```

Read it as: *"for each `m` in `marks`"*.

### Limits of for-each

| Point | Detail |
|-------|--------|
| No index | You don't know the position (`i`) |
| Can't go backwards | Always first to last |
| Can't skip by 2 | Always one by one |
| Changing the variable doesn't change the array | `m = 100;` only changes a copy of the value (for primitive arrays) |

If you need the index, or want to change elements, use the normal `for` loop.

---

## 9. Infinite loops

A loop that never ends. Sometimes it's a **bug**, sometimes it's **intentional**.

### Intentional (stopped with `break`)

```java
while (true) {
    // read input, process...
    if (finished) break;
}
```

```java
for (;;) { ... if (done) break; }
```

### Accidental causes

**1. Forgetting the update**

```java
int i = 0;
while (i < 5) {
    System.out.println(i);   // i never changes -> infinite
}
```

**2. Wrong direction**

```java
for (int i = 10; i > 0; i++) { }   // i grows, never <= 0
```

**3. Float counters**

```java
for (double d = 0.0; d != 1.0; d += 0.1) { }   // d never exactly equals 1.0
```
Use `<` or `<=` with decimals, never `!=`.

**4. Small data types that wrap around**

```java
for (byte b = 0; b < 200; b++) { }   // byte max is 127, wraps to -128 -> infinite
```

### Unreachable code

Code written right after an infinite loop with **no way out** gives a compile error:

```java
while (true) {
    System.out.println("Hi");
}
System.out.println("After");   // ERROR: unreachable statement
```
Similarly, `while (false) { ... }` gives an error because the body can never run.

---

## 10. `break`

`break` **immediately stops the loop** and jumps to the first statement **after** the loop.

### Example: stop when a number is found

```java
for (int i = 1; i <= 10; i++) {
    if (i == 5) {
        break;
    }
    System.out.print(i + " ");
}
System.out.println("Loop ended");
```

Output:

```
1 2 3 4 Loop ended
```

### Real use: check if a number is prime

```java
int n = 29;
boolean isPrime = n > 1;

for (int i = 2; i * i <= n; i++) {
    if (n % i == 0) {
        isPrime = false;
        break;               // no need to check further
    }
}
System.out.println(isPrime);   // true
```

### Real use: linear search

```java
int[] arr = {4, 8, 15, 16, 23};
int target = 15, index = -1;

for (int i = 0; i < arr.length; i++) {
    if (arr[i] == target) {
        index = i;
        break;               // found, stop searching
    }
}
System.out.println(index);   // 2
```

### Important: `break` only exits the **innermost** loop

```java
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        if (j == 2) break;           // only stops the inner loop
        System.out.println(i + " " + j);
    }
}
```

Output:

```
1 1
2 1
3 1
```

The outer loop keeps going. (To break the outer loop, use a **label**, see section 12.)

### Trap: `break` inside a `switch` inside a loop

```java
for (int i = 1; i <= 3; i++) {
    switch (i) {
        case 2:
            break;                 // breaks the SWITCH, not the loop
    }
    System.out.println(i);
}
// prints 1, 2, 3
```
A `break` inside a `switch` only leaves the `switch`.

---

## 11. `continue`

`continue` **skips the rest of the current round** and moves on to the **next round** of the loop. The loop itself does not stop.

### Example: skip number 3

```java
for (int i = 1; i <= 5; i++) {
    if (i == 3) {
        continue;
    }
    System.out.print(i + " ");
}
```

Output:

```
1 2 4 5
```

### `break` vs `continue`

| | `break` | `continue` |
|---|---------|-----------|
| Effect | **Ends** the whole loop | **Skips** only the current round |
| After it | Code after the loop runs | Next round starts |

### Where does `continue` jump to?

| Loop | `continue` jumps to |
|------|---------------------|
| `for` | The **update** part (`i++`), then the condition |
| `while` | The **condition** check |
| `do-while` | The **condition** check |

### Danger in `while`: `continue` can skip your update

```java
int i = 0;
while (i < 5) {
    if (i == 2) {
        continue;       // i++ below is skipped -> i stays 2 forever -> infinite loop!
    }
    System.out.println(i);
    i++;
}
```
Fix: update **before** `continue`:

```java
int i = 0;
while (i < 5) {
    if (i == 2) {
        i++;
        continue;
    }
    System.out.println(i);
    i++;
}
```
In a `for` loop this problem doesn't happen, because `continue` still runs the update.

### Example: print only odd numbers

```java
for (int i = 1; i <= 10; i++) {
    if (i % 2 == 0) continue;     // skip even numbers
    System.out.print(i + " ");
}
// 1 3 5 7 9
```

---

## 12. Labeled loops: `break label` and `continue label`

### The problem

`break` and `continue` only affect the **innermost** loop. What if you are inside **nested loops** and want to stop or skip the **outer** loop?

Java solves this with a **label**: a name you put before a loop.

### Syntax

```java
labelName:
for (...) {
    for (...) {
        break labelName;       // exits the loop that has this label
        continue labelName;    // jumps to the next round of the labeled loop
    }
}
```

- A label is any valid identifier followed by a colon (`outer:`).
- By convention, labels are short descriptive names like `outer`, `rows`, `search`.

### Example setup

We'll use this loop three different ways and compare the outputs:

```java
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        if (i == 2 && j == 2) {
            // ??? (we change this line)
        }
        System.out.println(i + " " + j);
    }
    System.out.println("-- row " + i + " done --");
}
```

### 12.1 Plain `break` (stops only the inner loop)

```java
if (i == 2 && j == 2) break;
```

Output:

```
1 1
1 2
1 3
-- row 1 done --
2 1
-- row 2 done --
3 1
3 2
3 3
-- row 3 done --
```
When `i == 2, j == 2`, the **inner** loop stops. The "row 2 done" line **still prints**, and the outer loop continues with `i = 3`.

### 12.2 `break outer` (stops BOTH loops)

```java
outer:
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        if (i == 2 && j == 2) break outer;
        System.out.println(i + " " + j);
    }
    System.out.println("-- row " + i + " done --");
}
```

Output:

```
1 1
1 2
1 3
-- row 1 done --
2 1
```
Everything stops right away. Not even "row 2 done" is printed.

### 12.3 `continue outer` (skips to the next round of the OUTER loop)

```java
outer:
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        if (i == 2 && j == 2) continue outer;
        System.out.println(i + " " + j);
    }
    System.out.println("-- row " + i + " done --");
}
```

Output:

```
1 1
1 2
1 3
-- row 1 done --
2 1
3 1
3 2
3 3
-- row 3 done --
```
At `i == 2, j == 2`, the rest of the inner loop **and** the "row done" line are skipped, and the outer loop jumps to `i = 3`.

### 12.4 Quick comparison

| Statement | What it does |
|-----------|--------------|
| `break` | Exit the **inner** loop only |
| `break outer` | Exit the **labeled (outer)** loop, so everything stops |
| `continue` | Next round of the **inner** loop |
| `continue outer` | Next round of the **labeled (outer)** loop |

### 12.5 Real use: searching a 2D array

```java
int[][] grid = {
    {1, 2, 3},
    {4, 5, 6},
    {7, 8, 9}
};
int target = 5;
int foundRow = -1, foundCol = -1;

search:
for (int r = 0; r < grid.length; r++) {
    for (int c = 0; c < grid[r].length; c++) {
        if (grid[r][c] == target) {
            foundRow = r;
            foundCol = c;
            break search;       // stop both loops as soon as it's found
        }
    }
}
System.out.println(foundRow + ", " + foundCol);   // 1, 1
```
Without a label, you would need an extra `boolean found` flag and a check in both loops.

### 12.6 Rules for labels

- `break label` works with **any labeled statement** (loops, and even labeled blocks).
- `continue label` works **only with loops**.
- The label must be on a loop that **encloses** the `break` / `continue` statement.
- Java has **no `goto`**. Labeled `break` / `continue` are the safe, limited replacement.
- Use labels **sparingly**. They are great for nested-loop exits, but too many make code hard to read.

---

## 13. Nested loops

A loop inside another loop. For each round of the **outer** loop, the **inner** loop runs **completely**.

```java
for (int i = 1; i <= 3; i++) {          // outer: 3 rounds
    for (int j = 1; j <= 2; j++) {      // inner: 2 rounds per outer round
        System.out.println("i=" + i + " j=" + j);
    }
}
```

Total inner executions = outer rounds × inner rounds = 3 × 2 = **6**.

Output:

```
i=1 j=1
i=1 j=2
i=2 j=1
i=2 j=2
i=3 j=1
i=3 j=2
```

### Pattern example: right triangle of stars

```java
int rows = 4;
for (int i = 1; i <= rows; i++) {
    for (int j = 1; j <= i; j++) {
        System.out.print("* ");
    }
    System.out.println();           // new line after each row
}
```

Output:

```
* 
* * 
* * * 
* * * * 
```

Rule of thumb: the **outer** loop controls **rows**, the **inner** loop controls **columns**.

---

## 14. `while` vs `do-while` vs `for`

| Feature | `while` | `do-while` | `for` |
|---------|---------|-----------|-------|
| Condition checked | **Before** body | **After** body | **Before** body |
| Minimum runs | 0 | **1** | 0 |
| Initialization | Written outside | Written outside | In the header (or outside) |
| Update | Inside the body | Inside the body | In the header (or inside) |
| Semicolon at end | No | **Yes** (`while (cond);`) | No |
| Best when | Number of rounds **unknown** | Must run **at least once** | Number of rounds **known** |
| Typical use | Read until done | Menus, input validation | Counting, arrays |

All three can do the same job; pick the one that makes the code the clearest.

---

## 15. Common mistakes

| Mistake | Problem | Fix |
|---------|---------|-----|
| Forgetting the update | Infinite loop | Make sure the loop variable changes |
| Semicolon after `for (...)` / `while (...)` | Empty loop body, real body runs once after it | Remove the `;` |
| `<=` instead of `<` on arrays | `ArrayIndexOutOfBoundsException` | Use `i < arr.length` |
| Missing `;` after `do-while` | Compile error | `} while (cond);` |
| `continue` before the update in `while` | Infinite loop | Update before `continue` |
| Using `!=` with decimals | Never equals exactly | Use `<` or `<=` |
| `break` inside `switch` in a loop | Only exits the switch | Use a label or a flag |
| Using the loop variable outside the loop | Compile error | Declare it before the loop |
| Different types in `for (int i = 0, double d = 0;...)` | Compile error | Declare them separately |
| Changing the loop variable inside the body carelessly | Skips or repeats rounds | Avoid it unless intended |

**The semicolon trap in detail:**

```java
for (int i = 0; i < 3; i++);      // <-- stray semicolon
{
    System.out.println("Hello");   // runs only ONCE, after the loop finishes
}
```

---

## 16. Practice questions

**Q1.** What is the output?

```java
for (int i = 0; i < 3; i++) {
    System.out.print(i + " ");
}
```
**Answer:** `0 1 2`

---

**Q2.** What is the output?

```java
int i = 5;
do {
    System.out.print(i + " ");
    i++;
} while (i < 5);
```
**Answer:** `5`. The body runs once before the condition is checked.

---

**Q3.** What is the output?

```java
for (int i = 1; i <= 5; i++) {
    if (i == 2) continue;
    if (i == 4) break;
    System.out.print(i + " ");
}
```
**Answer:** `1 3`. (2 skipped by `continue`, loop ends at 4 by `break`.)

---

**Q4.** What does `for (;;)` do?
**Answer:** It is an infinite loop (empty condition means `true`). It stops only with `break`, `return` or an exception.

---

**Q5.** What is the output?

```java
for (int i = 0, j = 4; i < j; i++, j--) {
    System.out.print(i + "" + j + " ");
}
```
**Answer:** `04 13`. (Next: `i=2, j=2` → `2 < 2` false → stop.)

---

**Q6.** Is this valid?

```java
int i = 0;
for ( ; i < 3; ) {
    i++;
}
```
**Answer:** Yes. It works like `while (i < 3)`.

---

**Q7.** How many times does "Hi" print?

```java
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 4; j++) {
        System.out.println("Hi");
    }
}
```
**Answer:** `3 × 4 = 12` times.

---

**Q8.** What is the output?

```java
outer:
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        if (j == 2) continue outer;
        System.out.print(i + "" + j + " ");
    }
}
```
**Answer:** `11 21 31`. At `j == 2`, `continue outer` jumps to the next `i`.

---

**Q9.** Same code, but with `break outer` instead.
**Answer:** `11`. Both loops stop at the first `j == 2`.

---

**Q10.** Will this compile?

```java
while (true) {
    System.out.println("Hi");
}
System.out.println("Bye");
```
**Answer:** No. "Unreachable statement". The loop never ends, so the last line can never run.

---

## 17. Quick cheat sheet

### Syntax

```java
// while
while (condition) { ... }

// do-while  (note the semicolon)
do { ... } while (condition);

// for
for (init; condition; update) { ... }

// for-each
for (type item : collection) { ... }

// infinite loops
while (true) { ... }
for (;;) { ... }

// multiple variables
for (int i = 0, j = 9; i < j; i++, j--) { ... }

// labeled
outer:
for (...) {
    for (...) {
        break outer;
        continue outer;
    }
}
```

### Key points to remember

1. **`while`**: checks first, may run **0** times.
2. **`do-while`**: runs the body first, runs **at least once**, needs `;` at the end.
3. **`for`** order: **init (once) → condition → body → update → condition → ...**
4. In a `for`, **every part is optional**, but both `;` stay. An empty condition means `true`, so `for (;;)` is an infinite loop.
5. You can use **several variables** with commas: `int i = 0, j = 5` and `i++, j--`. They must be the **same type** in the init, and the condition is a single boolean expression.
6. A variable declared in the `for` header **dies after the loop**.
7. **`break`** exits the loop. **`continue`** skips to the next round.
8. They only affect the **innermost** loop, so use a **label** (`break outer;`, `continue outer;`) for outer loops.
9. In `while`, make sure `continue` doesn't skip your update. In `for`, `continue` still runs the update.
10. A `break` inside a `switch` leaves only the `switch`, not the loop.

---

*End of notes. Practice: write the same loop using `while`, `do-while` and `for`, then solve one problem using a labeled `break`.*
