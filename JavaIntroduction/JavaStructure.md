# Java Behind the Scenes: JVM, JIT, JRE, JDK and Java Editions

> A friendly, detailed guide written in simple language.
> Read it top to bottom once, and Java's "magic" will stop feeling like magic.

---

## Table of Contents

1. [The Big Picture (start here)](#1-the-big-picture-start-here)
2. [What is the JVM?](#2-what-is-the-jvm)
3. [The Main Components of the JVM](#3-the-main-components-of-the-jvm)
   - 3.1 Class Loader Subsystem
   - 3.2 Runtime Data Areas (memory)
   - 3.3 Execution Engine (Interpreter, JIT, Garbage Collector)
   - 3.4 Native Interface and Native Libraries
4. [Portability: "Write Once, Run Anywhere"](#4-portability-write-once-run-anywhere)
5. [Security in the JVM](#5-security-in-the-jvm)
6. [The Java Compiler (javac)](#6-the-java-compiler-javac)
7. [The Interpreter](#7-the-interpreter)
8. [Why Was Java Interpreted by Design?](#8-why-was-java-interpreted-by-design)
9. [The JIT Compiler and Optimization](#9-the-jit-compiler-and-optimization)
10. [JRE: Java Runtime Environment](#10-jre-java-runtime-environment)
11. [JDK: Java Development Kit](#11-jdk-java-development-kit)
12. [JDK vs JRE vs JVM: Quick Comparison](#12-jdk-vs-jre-vs-jvm-quick-comparison)
13. [Java Editions: SE, EE (Jakarta EE), ME and more](#13-java-editions-se-ee-jakarta-ee-me-and-more)
14. [Final Summary](#14-final-summary)

---

## 1. The Big Picture (start here)

Before the details, let's see the whole journey of a Java program in one picture.

```
   You write                 javac                    JVM                    CPU
 ┌───────────┐          ┌────────────┐          ┌──────────────┐        ┌──────────┐
 │ Hello.java│ ───────► │ Hello.class│ ───────► │ Interpreter  │ ─────► │ Machine  │
 │ (source)  │ compile  │ (bytecode) │  run on  │ + JIT        │        │ code     │
 └───────────┘          └────────────┘          └──────────────┘        └──────────┘
   Human-readable        Not for humans,         Translates bytecode     What the CPU
                         not for the CPU         for the real machine    really understands
```

In one sentence:

> **You write Java source code → `javac` converts it into bytecode → the JVM runs that bytecode on any computer.**

Everything in this document is just a deeper explanation of one of these arrows or boxes.

### A simple real-life example

Imagine you wrote a book in English and you want to sell it all over the world.

- Translating the book separately for every country's language by hand = **traditional compiled languages** (like C: compile once per operating system/CPU).
- Writing it in one **universal language** (bytecode), and hiring a **local translator** (the JVM) in each country = **Java**.

The book (bytecode) stays the same. Only the translator (JVM) changes per country (per OS/CPU). That is the heart of Java.

---

## 2. What is the JVM?

**JVM = Java Virtual Machine.**

Let's break the name apart:

- **Java**: it runs Java programs (and today also Kotlin, Scala, Groovy, Clojure and any language that produces Java bytecode).
- **Virtual**: it is not a physical machine. It is a *software-made computer* that lives inside your real computer.
- **Machine**: it behaves like a small computer with its own rules, its own memory layout and its own "instruction set" (bytecode).

### So what does the JVM actually do?

1. **Loads** your compiled `.class` files.
2. **Checks** them for safety (verification).
3. **Runs** them (by interpreting and/or compiling to machine code).
4. **Manages memory** for you (allocation and garbage collection).
5. **Handles threads**, exceptions and many other runtime jobs.

### JVM is a *specification* and also an *implementation*

This confuses many beginners, so remember:

| Word | Meaning |
|------|---------|
| **JVM Specification** | A document (written by the Java people) describing *how a JVM must behave*. |
| **JVM Implementation** | Real software that follows that document. |

Different companies built different JVMs that follow the same rules:

- **HotSpot**: the most popular one, used by Oracle JDK and OpenJDK.
- **OpenJ9**: made by IBM (now Eclipse), known for lower memory use.
- **GraalVM**: a modern high-performance VM with an advanced compiler.

Same bytecode, different engines. All of them run the same `.class` files.

### What is bytecode?

Bytecode is the "universal language" in the middle. It is a compact set of simple instructions, each one byte long, like `iload`, `iadd`, `invokevirtual`.

Example Java code:

```java
int a = 5;
int b = 10;
int c = a + b;
```

Roughly becomes bytecode like:

```
iconst_5      // push 5
istore_1      // store it in variable a
bipush 10     // push 10
istore_2      // store it in variable b
iload_1       // load a
iload_2       // load b
iadd          // add them
istore_3      // store result in c
```

You can see real bytecode yourself with this command:

```
javap -c Hello
```

Bytecode is **not tied to Windows, Linux, Mac, Intel or ARM**. That's why it is portable.

---

## 3. The Main Components of the JVM

Here is the whole JVM architecture in one drawing:

```
┌──────────────────────────────────────────────────────────────────────┐
│                               J V M                                  │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                 CLASS LOADER SUBSYSTEM                         │  │
│  │     Loading  ──►  Linking (Verify, Prepare, Resolve)  ──► Init │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                  │                                   │
│                                  ▼                                   │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                   RUNTIME DATA AREAS                           │  │
│  │  ┌─────────────┐ ┌───────┐ ┌─────────┐ ┌──────────┐ ┌───────┐  │  │
│  │  │ Method Area │ │ Heap  │ │ Stacks  │ │ PC       │ │Native │  │  │
│  │  │ (Metaspace) │ │       │ │(per     │ │ Registers│ │Method │  │  │
│  │  │             │ │       │ │ thread) │ │          │ │Stacks │  │  │
│  │  └─────────────┘ └───────┘ └─────────┘ └──────────┘ └───────┘  │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                  │                                   │
│                                  ▼                                   │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                      EXECUTION ENGINE                          │  │
│  │      Interpreter     │     JIT Compiler     │  Garbage Collector│  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                  │                                   │
│                                  ▼                                   │
│  ┌──────────────────────────┐  ┌──────────────────────────────────┐  │
│  │ Java Native Interface    │  │ Native Method Libraries (C/C++)  │  │
│  │ (JNI)                    │  │                                  │  │
│  └──────────────────────────┘  └──────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

Let's go through each part slowly.

---

### 3.1 Class Loader Subsystem

**Job:** find `.class` files and bring them into memory, *only when needed*.

Java does **lazy loading**: a class is not loaded when the program starts, it is loaded the first time your code actually uses it. This saves memory and startup time.

The class loader works in **three phases**:

#### Phase 1: Loading
The loader finds the `.class` file (from disk, a JAR file, the network, etc.) and reads its bytes into memory.

Java has a **hierarchy** of class loaders:

| Loader | What it loads |
|--------|---------------|
| **Bootstrap Class Loader** | The core Java classes (`java.lang.String`, `java.lang.Object`, etc.). Written in native code. |
| **Platform Class Loader** (earlier called *Extension*) | Extra platform modules and libraries. |
| **Application (System) Class Loader** | *Your* code and libraries from the classpath. |

**Parent delegation model:** when asked to load a class, a loader first asks its **parent**. Only if the parent says "I can't find it" does the child try. This is a **security feature** too (see section 5): nobody can sneak in a fake `java.lang.String` because the Bootstrap loader always gets the first chance.

```
Application Loader ──asks──► Platform Loader ──asks──► Bootstrap Loader
        ▲                           ▲                          │
        └──────── "not found? then I'll try myself" ◄──────────┘
```

#### Phase 2: Linking
This has three small steps:

1. **Verification**: checks that the bytecode is valid and safe (more in the Security section).
2. **Preparation**: allocates memory for `static` variables and gives them **default values** (0, false, null).
3. **Resolution**: replaces symbolic names (like "call method `println`") with real memory references.

#### Phase 3: Initialization
Now the `static` variables get their **actual values** (the ones you wrote in code) and `static { }` blocks run.

```java
static int count = 10;   // Preparation: count = 0   |   Initialization: count = 10
```

---

### 3.2 Runtime Data Areas (JVM Memory)

When the JVM runs, it divides memory into areas. Each has a clear job.

| Area | Shared or per-thread? | What it stores |
|------|----------------------|----------------|
| **Method Area** (Metaspace in modern Java) | Shared by all threads | Class structure, method code, static variables, constant pool |
| **Heap** | Shared by all threads | All **objects** and arrays (`new ...`) |
| **Stack** (JVM Stack) | One per thread | Method calls, local variables, partial results |
| **PC Register** | One per thread | Address of the *current instruction* being executed |
| **Native Method Stack** | One per thread | Stack for native (C/C++) methods |

#### The Heap, in detail
Every time you write `new Something()`, the object lives on the heap. The heap is managed by the **Garbage Collector** (GC).

Most GCs split the heap into generations, based on a simple observation: **most objects die young**.

```
┌─────────────────────────── HEAP ───────────────────────────┐
│  Young Generation                    │   Old Generation    │
│  ┌──────┬──────────┬──────────┐      │                     │
│  │ Eden │Survivor 0│Survivor 1│      │  Long-living objects│
│  └──────┴──────────┴──────────┘      │                     │
│  New objects are born here           │                     │
└────────────────────────────────────────────────────────────┘
```

- New objects are created in **Eden**.
- Objects that survive a few cleanups move to **Survivor** spaces, then eventually to the **Old Generation**.
- Cleaning the young area is quick and frequent. Cleaning the old area is bigger and rarer.

#### The Stack, in detail
Each time a method is called, the JVM pushes a **stack frame** holding that method's local variables. When the method returns, the frame is popped.

```java
void a() { b(); }
void b() { c(); }
void c() { }
```

```
 Stack (top is current)
 ┌─────────┐
 │ frame c │ ◄── running now
 ├─────────┤
 │ frame b │
 ├─────────┤
 │ frame a │
 ├─────────┤
 │ main    │
 └─────────┘
```

If you call methods too deeply (like endless recursion), the stack fills up and you get **`StackOverflowError`**. If the heap fills up and GC cannot free space, you get **`OutOfMemoryError`**.

#### Quick memory rule
- **Local variables and method calls → Stack**
- **Objects (`new`) → Heap**
- **Class info and static data → Method Area / Metaspace**

---

### 3.3 Execution Engine

This is the **brain** of the JVM, the part that actually runs your bytecode. It has three main parts:

1. **Interpreter**: runs bytecode instruction by instruction.
2. **JIT Compiler**: converts frequently used bytecode into fast machine code.
3. **Garbage Collector**: automatically frees memory of objects no longer used.

We explain the Interpreter and JIT in detail in sections 7, 8 and 9. First, a quick look at the GC.

#### Garbage Collector (GC)
In languages like C/C++, the programmer must free memory manually (`free`, `delete`), and forgetting causes **memory leaks** or crashes. In Java the GC does it automatically.

How it thinks: *"Starting from the roots (running threads, static variables), which objects can still be reached? Everything else is garbage."*

```
 Roots ──► A ──► B           C ──► D      ← C and D can't be reached from roots
                              (garbage → collected)
```

Modern GCs you may hear about: **G1** (the default in modern Java), **ZGC** and **Shenandoah** (very low pause times), **Parallel GC** (high throughput), **Serial GC** (small and simple).

---

### 3.4 Native Interface (JNI) and Native Libraries

Sometimes Java must talk to the operating system or use code written in C/C++ (for example, file systems, graphics, hardware). The **Java Native Interface (JNI)** is the bridge, and the **Native Method Libraries** are the C/C++ libraries the JVM can call. Java's own classes use this a lot internally. (Newer Java versions also offer the *Foreign Function & Memory API* as a modern, safer alternative.)

---

## 4. Portability: "Write Once, Run Anywhere"

This was Java's famous slogan (**WORA**): *Write Once, Run Anywhere*.

### How does it work?

```
                          ┌──────────────────┐   ┌──────────────┐
                          │ JVM for Windows  │──►│ Windows PC   │
 Hello.java ─► Hello.class│──────────────────│   ├──────────────┤
 (compile once) (same     │ JVM for Linux    │──►│ Linux server │
                 bytecode)│──────────────────│   ├──────────────┤
                          │ JVM for macOS    │──►│ Mac          │
                          └──────────────────┘   └──────────────┘
```

- The **bytecode is the same** everywhere.
- The **JVM is different** for each OS/CPU. Each one knows how to translate bytecode for its own platform.

So the portability is in two places:

1. **Platform-independent bytecode.**
2. **Platform-dependent JVMs** that hide all the differences.

### Other things that help portability

- **Fixed size of primitive types.** In Java, `int` is *always* 32 bits and `long` is *always* 64 bits on every machine. In C, `int` can differ between systems.
- **Standard libraries.** The same classes (`ArrayList`, `File`, `Thread`) work everywhere. The library hides OS differences (like `\` vs `/` in file paths).
- **Defined behavior.** Java's language rules say exactly what should happen, leaving much less "undefined behavior" than C/C++.

> Honest note: "Write once, run anywhere" is very true for pure Java, but you can still get differences if you use native code, OS-specific features or GUI quirks. Developers joked it was "write once, test everywhere", but for server-side Java it works wonderfully.

---

## 5. Security in the JVM

Java was designed in the internet era, when code could be downloaded and run on your machine (remember **applets** in browsers). So the JVM was built to be **safe against untrusted or buggy code**. Security comes from many layers working together.

### Layer 1: The Language Itself Is Safe
- **No raw pointers.** You cannot do pointer arithmetic and read random memory like in C.
- **Array bounds checking.** Accessing `arr[100]` on a 10-element array throws `ArrayIndexOutOfBoundsException` instead of silently corrupting memory (no classic *buffer overflow*).
- **Automatic memory management.** No "use after free" or double free bugs.
- **Strong typing.** You can't treat an `Integer` as a `String` secretly. Illegal casts throw `ClassCastException`.
- **Access modifiers.** `private`, `protected` and `public` are enforced.

### Layer 2: The Bytecode Verifier
Remember the *Verification* step in linking? This is it. Before running a class, the JVM checks things like:

- Does the bytecode follow the format rules?
- Will the stack ever overflow or underflow in an illegal way?
- Are types used correctly (no passing an `int` where an object reference is expected)?
- Are there illegal jumps (jumping into the middle of an instruction)?
- Is it respecting `private`, `final`, etc.?

Why is this needed? Because someone could hand-craft a `.class` file that did **not** come from a normal Java compiler, with evil bytecode. The verifier catches it before it runs.

### Layer 3: Class Loaders and Namespaces
- The **parent-delegation** model protects core classes. Nobody can replace `java.lang.Object` with a malicious version.
- Each class loader has its own **namespace**, so classes loaded by different loaders are kept separate.

### Layer 4: The Module System (Java 9+)
Modules let a library say exactly which packages it exposes and which are hidden. This is called **strong encapsulation** and it stops code from reaching into the JDK's internals.

### Layer 5: Sandboxing and the Security Manager (historical)
Older Java used a **Security Manager** and permission policy files to restrict what code could do (read files, open network connections, etc.). That's how applets were sandboxed.

> Modern note: the Security Manager is **deprecated for removal** (starting Java 17) and has been disabled in newer versions, because it was complex and rarely used well. Today, security usually relies on the OS, containers, and the layers above.

### Layer 6: Cryptography and Secure APIs
Java ships with security libraries: encryption, digital signatures, certificates, TLS/SSL (`javax.crypto`, `java.security`, `javax.net.ssl`).

### Security summary

| Layer | Protects against |
|-------|------------------|
| Safe language design | Memory corruption, buffer overflows |
| Bytecode verifier | Malicious or corrupted `.class` files |
| Class loader hierarchy | Fake replacements of core classes |
| Modules | Misuse of internal APIs |
| Crypto/security APIs | Data theft and tampering over the network |

---

## 6. The Java Compiler (javac)

The **Java compiler** is called `javac`. It is part of the **JDK**, not the JVM.

### What it does
It converts your **`.java`** source code into **`.class`** bytecode.

```
javac Hello.java      →  creates Hello.class
java Hello            →  JVM runs Hello.class
```

### What javac checks along the way
1. **Syntax**: missing semicolons, wrong keywords.
2. **Types**: you can't put a `String` into an `int`.
3. **Names**: using a variable that doesn't exist.
4. **Checked exceptions**: you must handle or declare them.
5. **Other rules**: unreachable code, uninitialized variables, and so on.

If something is wrong, you get a **compile-time error** and no `.class` file is produced. That's good: many mistakes are caught *before* the program runs.

### Important: javac does NOT produce machine code
`javac` produces *bytecode*, not CPU instructions. The final translation to machine code happens **later, inside the JVM** (by the interpreter and the JIT compiler).

> So Java is both **compiled** *and* **interpreted**:
> - **Compiled** by `javac` into bytecode (the first step).
> - **Interpreted/JIT-compiled** by the JVM at runtime (the second step).

### Two compilers, don't mix them up!

| Compiler | Where | Input → Output | When |
|----------|-------|----------------|------|
| **javac** (front-end compiler) | JDK | `.java` → bytecode | Before running (build time) |
| **JIT compiler** | Inside the JVM | bytecode → machine code | While the program is running |

---

## 7. The Interpreter

### What is an interpreter?
An interpreter reads instructions **one at a time**, figures out what each means, and **executes it immediately**, without first producing a complete machine-code program.

The JVM interpreter takes bytecode like this:

```
iload_1
iload_2
iadd
istore_3
```

…and for each instruction, it does the matching action on the CPU, one by one, looking each instruction up, running it, then moving to the next.

### Advantages of interpreting
- **Starts instantly.** No waiting for a big compilation step.
- **Simple and small.** Needs little memory.
- **Easy to port.** Writing an interpreter for a new CPU is simpler than writing an optimizing compiler.
- **Great for debugging and profiling.** The JVM can easily watch what's happening.

### Disadvantage
- **Slow.** Each bytecode instruction needs extra overhead (fetch, decode, dispatch). If a loop runs a million times, the same bytecode is re-interpreted a million times.

This weakness is exactly why the **JIT compiler** was invented (section 9).

---

## 8. Why Was Java Interpreted by Design?

You guessed it right: **limited memory, limited disk and portability** were big reasons. Let's go through the history and reasons in detail.

### The world when Java was born (early 1990s)

Java began as the **"Oak"** project at Sun Microsystems (around 1991 to 1995), originally aimed at small **consumer devices**: TV set-top boxes, handheld gadgets and so on. Then the internet exploded, and Java was repositioned for the **web** (applets in browsers), launching publicly in **1995** (Java 1.0 in 1996).

What did computers look like then?

| Resource | Typical situation |
|----------|-------------------|
| **RAM** | A few megabytes (4 to 16 MB was normal on a PC!) |
| **Disk** | Tens or a few hundred megabytes |
| **CPU** | Slow by today's standards (tens of MHz up to ~200 MHz) |
| **Network** | Slow dial-up modems |
| **Devices** | Many different CPUs and operating systems |

### Reasons Java chose an interpreter first

**1. Low memory (RAM)**
A smart optimizing compiler needs lots of memory to analyze code and to store generated machine code. An interpreter is tiny. It just reads bytecode and runs it. On small devices, that mattered a lot.

**2. Low disk space and small downloads**
Bytecode is **compact**. Native machine code for every possible platform would be large. When downloading an applet over a slow modem, small bytecode files were a big win.

**3. Portability across many different machines**
Which CPU would the program run on? Unknown! Intel, SPARC, PowerPC, ARM… If you ship machine code, you need a separate version for each. If you ship bytecode and have an interpreter on each machine, **one file runs everywhere**. This was the main goal.

**4. Security**
The code might come from the internet and be untrusted. Running it through a controlled interpreter/VM let Java **verify and sandbox** it, instead of letting unknown machine code run directly on the CPU.

**5. Fast startup, the "feels fast" factor**
You asked about making it *feel fast*, and this is a real point. With an interpreter, the program **starts immediately** and a user sees something happening. A compiler would force a pause to compile before running. For small programs and applets, quick startup matters more than top speed.

**6. Simplicity and fast development of the JVM**
A basic interpreter was easier and faster to build, test and port to many platforms than a full optimizing native compiler. The goal was to get Java working everywhere quickly.

**7. Dynamic features**
Java can load classes dynamically while running. An interpreter handles this naturally.

### The price paid
Early Java was **noticeably slow**, often many times slower than C/C++. People in the late 90s used to say *"Java is slow."* That reputation stuck for years even after it stopped being true.

### Then JIT arrived: the "optimized later" part

As computers got more RAM, faster CPUs and bigger disks, the trade-off changed. Engineers asked: *"What if we keep the interpreter for fast startup, but compile the **busy parts** to machine code while the program runs?"*

That is the **JIT (Just-In-Time) compiler**. Rough timeline:

| Time | What happened |
|------|---------------|
| **1995 to 1996** | Java 1.0 launches with a bytecode **interpreter**. Slow. |
| **~1997 (JDK 1.1 era)** | First **JIT compilers** appear (including ones from third parties). Big speedups. |
| **~1999 to 2000** | Sun's **HotSpot** VM arrives (first as an add-on, then the default in Java 1.3). It introduced *adaptive* optimization: find hot code, optimize only that. |
| **Java 6/7 era** | JIT becomes smarter, with more optimizations. |
| **Java 8 onward** | **Tiered compilation** (C1 + C2 working together) is the default. Modern Java can match or beat C++ in some long-running workloads. |

So the story is: **start simple and small (interpreter) → grow clever (JIT) as hardware allowed it.** Best of both worlds.

---

## 9. The JIT Compiler and Optimization

### What is JIT?

**JIT = Just-In-Time compilation.**
The JVM compiles bytecode into **native machine code** *at runtime*, "just in time" right before that code needs to run fast.

> Instead of compiling the whole program ahead of time (like C does), the JVM watches your program, finds the busiest parts, and compiles only those.

### The key insight: the 80/20 rule (hot spots)

In most programs, **a small portion of code runs a huge number of times** (loops, frequently called methods), while the majority runs rarely or only once. These busy parts are called **hot spots**. That's literally where the name **HotSpot** JVM comes from.

It makes no sense to spend effort optimizing code that runs once. So the JVM:

1. **Starts by interpreting** (fast startup).
2. **Counts** how often each method and loop runs.
3. When something becomes **hot** (crosses a threshold), the JIT **compiles it to machine code**.
4. Next time, the JVM runs the **fast compiled version** instead.

```
                 ┌─────────────────────┐
  bytecode ────► │ Interpreter         │ ── counts how often code runs ──┐
                 └─────────────────────┘                                  │
                          ▲                                               ▼
                          │                                    "This is HOT!"
                          │                                               │
                 ┌────────┴────────────┐                                  ▼
                 │ Run compiled code   │ ◄──────────────── ┌──────────────────────┐
                 │ (very fast)         │                   │ JIT Compiler         │
                 └─────────────────────┘                   │ bytecode → machine   │
                                                           │ code (+ optimizations)│
                                                           └──────────────────────┘
```

The compiled machine code is stored in a memory area called the **Code Cache**.

### Tiered Compilation (C1 and C2)

HotSpot has **two JIT compilers**:

| Compiler | Nickname | Speed of compiling | Quality of generated code |
|----------|----------|--------------------|---------------------------|
| **C1** | Client compiler | Very fast | Good |
| **C2** | Server compiler | Slower | Excellent (heavy optimizations) |

**Tiered compilation** uses both, in levels:

```
Tier 0: Interpreter
        │  (code becomes warm)
        ▼
Tier 1–3: C1 compiler → quick, decent machine code (Tier 3 also collects profiling data)
        │  (code becomes very hot)
        ▼
Tier 4: C2 compiler → highly optimized machine code
```

This gives a very nice balance: **quick startup** *and* **great peak performance**.

### Profiling: the JVM learns while running

A big advantage JIT has over a normal ahead-of-time compiler is that it **sees the real behavior of your program**. While interpreting, the JVM collects *profile data*:

- Which branch of an `if` is almost always taken?
- Which actual class is used in this method call?
- How big do these loops run?

The JIT then uses that real-world information to make smarter choices. A traditional compiler has to *guess*, but a JIT *knows*.

### Common JIT Optimizations (explained simply)

#### 1. Method Inlining
Replace a method call with the method's body to avoid call overhead (and unlock more optimizations).

```java
int square(int x) { return x * x; }

int y = square(5) + square(6);
// after inlining, effectively:
// int y = 5 * 5 + 6 * 6;
```

Tiny getters like `getName()` basically become free.

#### 2. Constant Folding
Calculate things at compile time when the values are known.

```java
int seconds = 60 * 60 * 24;   // JIT/javac turns this into 86400
```

#### 3. Dead Code Elimination
Remove code whose result is never used or that can never run.

```java
int unused = expensiveCalc();   // if never used and has no side effects → removed
```

#### 4. Loop Optimizations
- **Loop unrolling**: do several iterations per step to reduce loop-control overhead.
- **Loop-invariant code motion**: move calculations that don't change out of the loop.

```java
for (int i = 0; i < n; i++) {
    total += i * (a + b);   // (a + b) never changes → computed once, outside the loop
}
```

- **Vectorization**: use SIMD CPU instructions to process several numbers at once.

#### 5. Escape Analysis
The JIT checks whether an object **"escapes"** a method (is shared outside). If it doesn't, it may:
- allocate the object on the **stack** (or even avoid creating it at all, called *scalar replacement*), so the GC has less work.

```java
double dist(double x, double y) {
    Point p = new Point(x, y);   // never leaves this method
    return Math.sqrt(p.x * p.x + p.y * p.y);   // JIT may avoid creating the Point object entirely
}
```

#### 6. Lock Elision and Lock Coarsening
- **Lock elision**: remove `synchronized` locks if the object is only used by one thread.
- **Lock coarsening**: merge many tiny lock/unlock operations into one bigger one.

#### 7. Devirtualization (Speculative Optimization)
In Java, methods are *virtual* by default. A call like `animal.speak()` could go to `Dog`, `Cat`, etc. Normally that needs a lookup. But if profiling shows that **only `Dog` is ever used here**, the JIT *bets* on it and turns it into a direct call (which can then be inlined).

#### 8. Speculative Optimization and Deoptimization (the clever safety net)
What if the JIT's bet is wrong later? For example, suddenly a `Cat` shows up.

- The JIT puts a tiny **guard check** in the compiled code.
- If the assumption breaks, the JVM performs **deoptimization**: it throws away the optimized code, goes **back to the interpreter**, and may later recompile with new knowledge.

So the JIT can be **bold** because it always has a safe way back. Ahead-of-time compilers can't do this.

#### 9. On-Stack Replacement (OSR)
Suppose a method contains a **long-running loop** that is already running in the interpreter. The JVM can **swap in compiled code in the middle of the loop**, without waiting for the method to be called again.

#### 10. Branch Prediction Hints and Code Layout
Using profiling data, the JIT arranges the machine code so that the most common path is the fastest.

### Warm-up: why Java can be slow at first and fast later

Because of this design, a Java program goes through phases:

```
Speed
  ▲                                         ┌───────────── peak performance
  │                              ┌──────────┘
  │                  ┌───────────┘   (C2 optimizes hot code)
  │            ┌─────┘ (C1 compiles warm code)
  │ ┌──────────┘
  │ │ interpreting (slowest)
  └─┴──────────────────────────────────────────────► Time
     start       "warm-up"                 steady state
```

This is called **warm-up**. For long-running servers (web backends, etc.), this is a great trade: slow for a few seconds, then extremely fast for days.

For **short scripts or command-line tools**, warm-up can be noticeable. That's why newer technologies exist to help:

- **CDS / AppCDS (Class Data Sharing)**: speeds startup by pre-loading class data.
- **GraalVM Native Image / AOT (Ahead-of-Time) compilation**: compile to a native executable in advance for ultra-fast startup and low memory (with some trade-offs).
- **Project CRaC and similar ideas**: restoring a pre-warmed application snapshot.

### Why JIT can beat "compile once" languages

| Ahead-of-Time (e.g. C/C++) | JIT (Java) |
|-----------------------------|------------|
| Compiles with only *static* knowledge | Compiles with *real runtime* knowledge |
| Must target a generic CPU (unless built for a specific one) | Can use the *exact* CPU features of the machine it runs on |
| Optimization decisions are final | Can re-optimize or deoptimize if things change |
| Fast from the first millisecond | Needs warm-up time |

### Quick JIT tips for beginners
- You never need to "turn on" the JIT. It is automatic.
- You can peek at it with the flag `-XX:+PrintCompilation`.
- When benchmarking Java, always **warm up** first (or use a proper tool like **JMH**), or your timing results will be misleading.

---

## 10. JRE: Java Runtime Environment

### What is it?
**JRE = Java Runtime Environment.**
It is everything you need to **run** Java programs, but **not** to write/compile them.

### What is inside the JRE?

```
┌───────────────────── JRE ─────────────────────┐
│                                               │
│   ┌───────────────────────────────────────┐   │
│   │                 JVM                   │   │
│   └───────────────────────────────────────┘   │
│                                               │
│   + Java Class Libraries (core API)           │
│        java.lang, java.util, java.io,         │
│        java.net, java.time, ...               │
│                                               │
│   + Supporting files and native libraries     │
│   + Launcher (the `java` command)             │
│                                               │
└───────────────────────────────────────────────┘
```

**Formula:**

> **JRE = JVM + Java Class Libraries + other supporting files**

### Why is the JVM alone not enough?
Your program uses `String`, `ArrayList`, `System.out.println`, `File`, and so on. Those are all **ready-made classes** that must exist on the computer. The JVM runs the bytecode, but those standard classes come from the **class libraries** in the JRE.

### Who needs only the JRE?
Someone who just wants to **run** a Java application (for example, a user running a Java desktop program), not develop one.

### Modern note (important!)
- Since **Java 9**, the JDK is **modular** (the Java Platform Module System).
- Since **Java 11**, Oracle no longer provides a separate downloadable JRE. You install a JDK, or you build a **custom small runtime** containing only the modules your app needs using the **`jlink`** tool.
- Some vendors (like Eclipse Temurin and others) still offer JRE builds for certain versions.

So the JRE *concept* is still true, but you will usually get it as part of a JDK or as a custom runtime image.

---

## 11. JDK: Java Development Kit

### What is it?
**JDK = Java Development Kit.**
It is the complete package for **developing** Java applications. It includes everything in the JRE **plus** the tools to write, compile, debug and package programs.

**Formula:**

> **JDK = JRE + Development Tools**
> (which means: **JDK = JVM + Class Libraries + Development Tools**)

```
┌──────────────────────── JDK ────────────────────────┐
│                                                      │
│  Development tools:                                  │
│    javac, jar, javadoc, jdb, jshell, jlink, ...      │
│                                                      │
│  ┌────────────────────── JRE ─────────────────────┐  │
│  │                                                │  │
│  │  ┌──────────────────────────────────────────┐  │  │
│  │  │                  JVM                     │  │  │
│  │  └──────────────────────────────────────────┘  │  │
│  │  + Java Class Libraries                        │  │
│  │                                                │  │
│  └────────────────────────────────────────────────┘  │
│                                                      │
└──────────────────────────────────────────────────────┘
```

### Common tools inside the JDK

| Tool | What it does |
|------|--------------|
| **`javac`** | The Java compiler (`.java` → `.class`) |
| **`java`** | The launcher that starts the JVM and runs your program |
| **`jar`** | Packs many `.class` files and resources into one `.jar` file |
| **`javadoc`** | Generates HTML documentation from special comments in your code |
| **`jdb`** | A command-line debugger |
| **`jshell`** | An interactive "try Java line by line" shell (since Java 9) |
| **`javap`** | Shows the bytecode of a class (a disassembler) |
| **`jlink`** | Builds a custom, smaller runtime with only needed modules |
| **`jpackage`** | Creates native installers (.exe, .dmg, .deb) for your app |
| **`jcmd`, `jconsole`, `jstack`, `jmap`** | Monitoring, diagnostics and troubleshooting tools |

### Who needs the JDK?
Every **developer**. If you want to write Java, you install the JDK.

### Popular JDK distributions
All based on the OpenJDK source code: **Oracle JDK**, **OpenJDK**, **Eclipse Temurin**, **Amazon Corretto**, **Azul Zulu**, **Microsoft Build of OpenJDK**, **Red Hat build of OpenJDK**, **BellSoft Liberica**, and others.

### A note about versions and LTS
Java releases a new version **every 6 months**. Some are marked **LTS (Long-Term Support)**, which get updates for many years. Well-known LTS versions are **Java 8, 11, 17, 21 and 25**. Companies usually stick to LTS versions.

---

## 12. JDK vs JRE vs JVM: Quick Comparison

| Feature | **JVM** | **JRE** | **JDK** |
|---------|---------|---------|---------|
| Full name | Java Virtual Machine | Java Runtime Environment | Java Development Kit |
| Main purpose | Execute bytecode | Provide an environment to **run** Java apps | Provide tools to **develop** and run Java apps |
| Contains | Class loader, memory areas, execution engine (interpreter, JIT, GC) | JVM + class libraries | JRE + compiler and dev tools |
| Includes `javac`? | No | No | **Yes** |
| Platform dependent? | **Yes** (one per OS/CPU) | Yes | Yes |
| Used by | The system, automatically | End users, servers | Developers |

**The "Russian doll" way to remember:**

```
   JDK  ⊃  JRE  ⊃  JVM

  (biggest)           (smallest)
```

Or in plain words:

- **JVM** = the engine that runs the program.
- **JRE** = the engine **plus** the standard libraries it needs.
- **JDK** = the engine, the libraries, **plus** the workshop tools to build programs.

---

## 13. Java Editions: SE, EE (Jakarta EE), ME and more

"Java" is not one single thing. It comes in several **editions (platforms)**, each aimed at a different kind of software. All of them use the same Java language, but offer different sets of libraries and rules.

```
                        ┌──────────────────────────────┐
                        │         Java Platforms       │
                        └──────────────────────────────┘
        ┌─────────────┬──────────────┬─────────────┬──────────────┐
        ▼             ▼              ▼             ▼              ▼
    Java SE      Jakarta EE       Java ME      Java Card       JavaFX
 (Standard)    (Enterprise)       (Micro)    (Smart cards)   (Rich GUI)
   core          big server       small         tiny chips     desktop UI
                 applications    devices
```

### 13.1 Java SE: Standard Edition

- **The core and foundation of everything.** Every other edition builds on it.
- Contains the **Java language**, the **JVM**, and the **core libraries**: collections, I/O, networking, concurrency (threads), security, date/time, streams, modules, and much more.
- Used for: desktop apps, command-line tools, general programming, and as the base for server-side apps.
- This is what you install as the **JDK** and what you learn first.

### 13.2 Java EE → Jakarta EE: Enterprise Edition

- Built **on top of Java SE** for large **enterprise/server-side applications**: web apps, REST services, database access, messaging, transactions, security for big business systems.
- Includes technologies such as **Servlets, JSP, JPA (database persistence), JAX-RS (REST), CDI (dependency injection), JMS (messaging), EJB**, and others.
- It was originally called **J2EE**, then **Java EE**.
- **Important history:** In 2017, Oracle handed Java EE over to the **Eclipse Foundation**. Because Oracle kept the "Java" trademark, it was renamed **Jakarta EE**. A big side effect: package names changed from `javax.*` to `jakarta.*` starting with **Jakarta EE 9**.
- It is used with **application servers** like WildFly, Payara, Open Liberty, GlassFish and Tomcat (partly).
- Many developers today use frameworks like **Spring / Spring Boot** instead, which work with (and alongside) many of these same standards.

### 13.3 Java ME: Micro Edition

- Made for **small, resource-limited devices**: old feature phones (the classic mobile Java games!), embedded devices, sensors, set-top boxes, and small gadgets.
- Uses a **smaller JVM** and a **trimmed-down set of libraries**, because memory and CPU are tiny.
- Built around **configurations** (like **CLDC**, the Connected Limited Device Configuration) and **profiles** (like **MIDP** for mobile phones).
- Today, Java ME is much less common. Smartphones moved on, and modern embedded work often uses Java SE (with `jlink`-trimmed runtimes), Embedded Java options or other languages.

> **Note:** Android apps are *not* Java ME. Android uses its own runtime (ART) and its own libraries. The Android code is often written in Java or Kotlin, but it is a separate platform.

### 13.4 Java Card

- A very small version of Java for **smart cards and secure chips**: SIM cards, bank/credit cards, ID cards, security tokens.
- Extremely limited memory, so only a small subset of Java is supported.
- Used where tiny, secure programs ("applets") must run on a chip.

### 13.5 JavaFX

- A modern framework for building **rich desktop and graphical user interfaces** (windows, buttons, animations, charts, media).
- It was bundled with the JDK for some time, but since Java 11 it is a **separate download** (maintained as the open-source **OpenJFX**).
- It replaced the older **AWT** and **Swing** as the recommended UI toolkit for many new desktop apps (though Swing is still included and widely used).

### 13.6 Editions at a glance

| Edition | Full name | Target | Notes |
|---------|-----------|--------|-------|
| **Java SE** | Standard Edition | General-purpose apps, the base platform | The foundation for everything |
| **Jakarta EE** (formerly Java EE / J2EE) | Enterprise Edition | Large server and web applications | Built on top of SE; now managed by Eclipse Foundation |
| **Java ME** | Micro Edition | Small and embedded devices | Trimmed JVM and libraries; mostly legacy today |
| **Java Card** | Java Card | Smart cards, secure chips | Tiny subset of Java |
| **JavaFX** | JavaFX / OpenJFX | Rich desktop GUIs | Separate download since Java 11 |

### How the editions relate

```
┌────────────────────────────── Jakarta EE ──────────────────────────────┐
│  Servlets, JPA, REST, CDI, messaging, transactions, ...                 │
│  ┌──────────────────────────── Java SE ──────────────────────────────┐  │
│  │  Language + JVM + core libraries (collections, I/O, threads, ...)  │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘

   Java ME  = a smaller, reduced cousin of SE for tiny devices
   Java Card = an even smaller cousin for chips on cards
```

---

## 14. Final Summary

Here is everything, in the simplest possible words.

1. **You write** `.java` source code.
2. **`javac`** (part of the JDK) compiles it into **bytecode** (`.class` files), a universal, platform-neutral language.
3. The **JVM** loads that bytecode with its **class loaders**, checks it with the **bytecode verifier** (security), and keeps data in its **memory areas** (heap, stack, metaspace).
4. The **execution engine** runs the code:
   - The **interpreter** starts running immediately (fast startup, tiny memory use).
   - The **JIT compiler** notices **hot spots** and compiles them into **optimized machine code** (inlining, escape analysis, loop optimizations, and more), with **deoptimization** as a safety net.
   - The **garbage collector** cleans unused objects automatically.
5. **Portability** exists because the same bytecode runs on any OS that has a JVM.
6. **Security** exists because of the safe language design, the verifier, class loader rules and the module system.
7. **JRE = JVM + libraries** (to *run* programs). **JDK = JRE + tools** (to *build* programs).
8. **Java was interpreted at first by design** because early computers had little **RAM, disk space and speed**, because it needed to be **portable, compact, secure and quick to start**. As hardware improved, **JIT** gave Java the speed to compete with native languages.
9. **Editions:** **Java SE** (core), **Jakarta EE** (enterprise), **Java ME** (small devices), **Java Card** (smart cards) and **JavaFX** (rich GUIs).

### One-line memory tricks

- **JVM** → *"the engine"*
- **JRE** → *"the engine + parts to run"*
- **JDK** → *"the engine + parts + the toolbox to build"*
- **Interpreter** → *"starts fast, runs slower"*
- **JIT** → *"learns while running, gets faster"*
- **Bytecode** → *"the universal language in the middle"*

---

### Mini Quiz (test yourself!)

1. What file does `javac` produce, and who runs it?
2. Which two things does the JRE contain?
3. Why does the JVM start by interpreting instead of compiling everything?
4. What is a "hot spot"?
5. What does deoptimization do?
6. Which Java edition is the base for all the others?

<details>
<summary>Click for answers</summary>

1. A `.class` bytecode file, run by the **JVM**.
2. The **JVM** and the **Java class libraries** (plus supporting files).
3. Interpreting gives **fast startup and low memory use**, and the JVM only spends compile effort on code that proves to be used a lot.
4. A piece of code (method or loop) that runs **very frequently**, so it's worth compiling and optimizing.
5. When a JIT assumption turns out to be wrong, it **discards the optimized code and falls back to the interpreter**, so the program stays correct.
6. **Java SE**.

</details>

---

*Happy learning, and happy coding!*
