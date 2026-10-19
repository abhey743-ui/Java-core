# Why Array Access Is Constant Time: O(1)

> Short and simple. Only what you need.

---

## 1. The One-Line Answer

When you write `arr[x]`, Java does **not search** for the element.
It **calculates** the exact memory address using a formula. One calculation, so the time is always the same: **O(1)**.

---

## 2. The Setup

```java
int[] arr = {10, 20, 30, 40, 50};
```

- The array object is created on the **heap**.
- Its elements are stored **side by side** in memory, one after another.
- Every element has the **same size** (an `int` is always 4 bytes).

```
 Heap memory (addresses are example numbers)

 address:   1000   1004   1008   1012   1016
          ┌──────┬──────┬──────┬──────┬──────┐
          │  10  │  20  │  30  │  40  │  50  │
          └──────┴──────┴──────┴──────┴──────┘
 index:       0      1      2      3      4
          ▲
     base address
     (where the array starts)
```

---

## 3. The Formula

```
address of arr[i]  =  base address  +  (i × size of one element)
```

| Part | Meaning |
|------|---------|
| **base address** | Where the array starts in memory |
| **i** | The index you asked for |
| **size of one element** | Bytes per element (`int` = 4) |

### Example

Base address = `1000`, type = `int` (4 bytes).

| You write | Calculation | Address |
|-----------|-------------|---------|
| `arr[0]` | 1000 + (0 × 4) | **1000** |
| `arr[1]` | 1000 + (1 × 4) | **1004** |
| `arr[3]` | 1000 + (3 × 4) | **1012** |
| `arr[4]` | 1000 + (4 × 4) | **1016** |

So `arr[3]` goes **straight to address 1012** and reads `40`. No looking at `arr[0]`, `arr[1]`, `arr[2]` first.

> Real JVMs add a small fixed header size (it stores the length etc.) before the first element. It is the same number every time, so it does not change the idea: `base + header + i × size`.

---

## 4. Why It Is O(1)

The work is always the same **two steps**, no matter how big the array is:

```
Step 1:  multiply   i × size
Step 2:  add        base + result
```

```java
int[] small = new int[5];
int[] huge  = new int[1_000_000];

small[3];          // 1 multiply + 1 add
huge[999_999];     // 1 multiply + 1 add   ← exactly the same work
```

| Array size | Steps to reach `arr[i]` |
|-----------|-------------------------|
| 5 elements | 2 |
| 1,000 elements | 2 |
| 1,000,000 elements | 2 |

**The size of the array does not matter. The time stays constant.** That is what **O(1)** means.

---

## 5. Fetch and Add Both Use the Same Formula

```java
int[] arr = {10, 20, 30, 40, 50};

int x = arr[2];      // FETCH (read)  → go to address of index 2 → O(1)
arr[2] = 99;         // ADD / STORE (write) → go to address of index 2 → O(1)
```

Both reading and writing by index calculate the address once and go there.

```
arr[2] = 99

  1000   1004   1008   1012   1016
 ┌──────┬──────┬──────┬──────┬──────┐
 │  10  │  20  │  99  │  40  │  50  │
 └──────┴──────┴──────┴──────┴──────┘
                  ▲
         1000 + (2 × 4) = 1008
```

---

## 6. Different Types, Different Size, Same Idea

The formula only changes the **size** value:

| Type | Size of one element |
|------|---------------------|
| `byte`, `boolean` | 1 byte (usually) |
| `short`, `char` | 2 bytes |
| `int`, `float` | 4 bytes |
| `long`, `double` | 8 bytes |

**Example with `long`** (8 bytes), base address = `2000`:

```
address of arr[3] = 2000 + (3 × 8) = 2024
```

**Example with `char`** (2 bytes), base address = `500`:

```
address of arr[10] = 500 + (10 × 2) = 520
```

---

## 7. This Is Why Arrays Need the Same Type and Fixed Size

The formula `i × size` only works if **every element has the same size** and the elements are **side by side**.

- Same type means same size, so the jump is always correct.
- Fixed length means the block of memory stays in one place.

### What about arrays of objects (`String[]`)?

Each slot holds a **reference** (an address), and every reference is the **same size**. So the formula still finds the slot in O(1). The object it points to lives elsewhere on the heap.

```
 names[1]  → slot found with the formula (O(1)) → follow the reference → the "Sara" object

  slots:   [ ref0 ][ ref1 ][ ref2 ]
              │       │
              ▼       ▼
           "Ali"   "Sara"
```

---

## 8. 2D Arrays: Still O(1)

`grid[row][col]` takes **two lookups**, each one O(1):

```
Step 1:  grid[row]        → find the row array (formula)
Step 2:  [col]            → find the cell in that row (formula)
```

Two O(1) steps is still **O(1)**.

(If you store a grid in one flat 1D array, the formula becomes `index = row × columns + col`.)

---

## 9. What Is NOT O(1)

Only **access by index** is O(1). These are different:

| Operation | Time | Why |
|-----------|------|-----|
| Read or write `arr[i]` | **O(1)** | Address formula |
| Find a **value** (e.g. "where is 40?") | **O(n)** | You must check elements one by one |
| **Insert in the middle** | **O(n)** | Elements after it must shift over |
| **Delete from the middle** | **O(n)** | Elements after it must shift back |
| **Grow the array** | **O(n)** | Make a new bigger array and copy everything |

```java
// O(1): you know the index
int v = arr[3];

// O(n): you only know the value
for (int i = 0; i < arr.length; i++) {
    if (arr[i] == 40) { /* found */ }
}
```

---

## 10. Summary

1. The array sits in the **heap**, elements **side by side**, all the **same size**.
2. `arr[i]` is found with:

   ```
   address = base address + (i × size of one element)
   ```
3. That is **one multiply and one add**, no matter how big the array is.
4. So reading or writing by index is **O(1), constant time**.
5. Searching by value, inserting, deleting and resizing are **O(n)**.
