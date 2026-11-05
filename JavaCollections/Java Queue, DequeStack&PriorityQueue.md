# Java `Queue`, `Deque`, Stack & `PriorityQueue` (Deep Dive)

> Easy-language notes with diagrams, examples, and interview points.

---

## Table of Contents

1. [Data Structure Fundamentals: Queue and Stack](#1-fundamentals)
2. [How to Implement Them in O(1)](#2-implementation-strategies)
3. [Java Collections Hierarchy for Queue](#3-java-hierarchy)
4. [Deque, ArrayDeque and LinkedList](#4-deque-arraydeque-and-linkedlist)
5. [Why ArrayDeque and Not Stack](#5-why-arraydeque-over-stack)
6. [Queue Operations: "Safe" vs "Unsafe" Methods](#6-queue-operations)
7. [PriorityQueue](#7-priorityqueue)
8. [Heap Data Structure](#8-heap)
9. [Heapify: Up and Down](#9-heapify)
10. [Time Complexity Cheat Sheet](#10-complexity-cheat-sheet)
11. [Common Mistakes](#11-common-mistakes)
12. [Interview Questions](#12-interview-questions)

---

## 1. Fundamentals

### 1.1 Queue: FIFO (First In, First Out)

A **queue** works like a **line at a ticket counter**. The first person who joins is the first person served.

- Insert at the **rear** (back).
- Remove from the **front**.

```
   FRONT (remove here)                       REAR (insert here)
      |                                            |
      v                                            v
   +-----+-----+-----+-----+-----+
   |  A  |  B  |  C  |  D  |  E  |   <-- new elements join here
   +-----+-----+-----+-----+-----+
      ^
   A leaves first (it came first)

 offer(F):   [A B C D E] -> [A B C D E F]
 poll():     [A B C D E F] -> returns A -> [B C D E F]
```

**Real-life uses:** printer jobs, customer support tickets, BFS (breadth-first search), message queues, CPU task scheduling.

### 1.2 Stack: LIFO (Last In, First Out)

A **stack** works like a **pile of plates**. You add and remove plates from the **top** only.

- Insertion **and** removal happen at the **same end**, called the **top**.

```
        push(40)        pop() -> 40
           |               ^
           v               |
        +------+        
  TOP-> |  40  |   <-- last in, first out
        +------+
        |  30  |
        +------+
        |  20  |
        +------+
        |  10  |
        +------+
         BOTTOM
```

**Real-life uses:** undo/redo, browser back button, function call stack, balanced-parentheses checking, DFS (depth-first search).

### 1.3 Side-by-Side

| | Queue | Stack |
|---|---|---|
| Rule | FIFO | LIFO |
| Insert at | Rear | Top |
| Remove from | Front | Top |
| Ends used | **Two** ends | **One** end |
| Analogy | Line at a counter | Pile of plates |

---

## 2. Implementation Strategies

Both can be built using an **array** or a **linked list**. The goal is **O(1) (constant time)** for insertion and deletion.

### 2.1 Stack with an Array: easy O(1)

Keep a `top` index. Push and pop only touch the end of the array.

```
 index:   0    1    2    3    4
        +----+----+----+----+----+
        | 10 | 20 | 30 |    |    |
        +----+----+----+----+----+
                    ^
                   top = 2

 push(40): top++ -> arr[3] = 40      O(1)
 pop():    return arr[top]; top--    O(1)
```

### 2.2 Stack with a Linked List: easy O(1)

Insert and remove at the **head**.

```
 top
  |
  v
 [30] -> [20] -> [10] -> null

 push(40): new node points to old head, head = new node   O(1)
 pop():    head = head.next                               O(1)
```

### 2.3 Queue with a Linked List: O(1) with two pointers

Keep **both** `front` (head) and `rear` (tail) pointers.

```
 front                    rear
   |                        |
   v                        v
  [A] -> [B] -> [C] -> [D] -> null

 enqueue(E): rear.next = new node; rear = new node    O(1)
 dequeue():  front = front.next                       O(1)
```

> Without a `rear` pointer you would walk the whole list to add at the end, which is O(n). The tail pointer fixes that.

### 2.4 Queue with an Array: the problem and the fix

**Naive approach:** removing from the front means shifting everything left, which is **O(n)**.

```
 [A B C D E]  -> remove A -> shift all -> [B C D E _]    O(n)  <- BAD
```

**Fix 1:** Move a `front` index instead of shifting. But then the space at the start is wasted.

```
 [_ _ C D E _ _]
      ^     ^
    front  rear     <- empty space on the left is never reused
```

**Fix 2: Circular array (ring buffer).** When `rear` reaches the end, it **wraps around** to index 0.

```
        index
     +---+---+---+---+---+---+
     | G | H |   |   | E | F |
     +---+---+---+---+---+---+
       0   1   2   3   4   5
           ^       ^
          rear    front

 Next index = (current + 1) % capacity
 rear = (rear + 1) % 6   ->  after index 5 comes index 0
```

This gives **O(1)** for both operations with no wasted space. This is exactly how **`ArrayDeque`** works.

---

## 3. Java Hierarchy

```
        Iterable<E>              <- gives for-each loop
            |
        Collection<E>            <- add, remove, size, contains...
            |
         Queue<E>                <- FIFO operations (offer, poll, peek)
            |
         Deque<E>                <- Double-Ended Queue (both ends)
          /     \
   ArrayDeque   LinkedList       <- main implementations

   Queue<E> is also implemented by:
         PriorityQueue<E>        <- ordered by priority (not arrival)
```

- **`Queue`** adds methods for FIFO behavior.
- **`Deque`** (pronounced "deck") extends `Queue` and allows insert/remove at **both ends**, so it can act as a **Queue** *and* as a **Stack**.
- `LinkedList` implements **both** `List` and `Deque`.

---

## 4. Deque, ArrayDeque and LinkedList

### 4.1 What is a Deque?

A **Double-Ended Queue**: you can add and remove from the **front and the back**.

```
        addFirst / offerFirst              addLast / offerLast
              |                                   |
              v                                   v
   <--  +-----+-----+-----+-----+-----+  <--
        |  A  |  B  |  C  |  D  |  E  |
   -->  +-----+-----+-----+-----+-----+  -->
              ^                                   ^
              |                                   |
        removeFirst / pollFirst            removeLast / pollLast
```

### 4.2 Deque Methods

| Operation | At the **front** | At the **rear** |
|---|---|---|
| Insert | `addFirst(e)` / `offerFirst(e)` | `addLast(e)` / `offerLast(e)` |
| Remove | `removeFirst()` / `pollFirst()` | `removeLast()` / `pollLast()` |
| Examine | `getFirst()` / `peekFirst()` | `getLast()` / `peekLast()` |

Plus the **Stack-style** names (all work on the **front**):

| Stack method | Same as |
|---|---|
| `push(e)` | `addFirst(e)` |
| `pop()` | `removeFirst()` |
| `peek()` | `peekFirst()` |

### 4.3 Using Deque as a Queue (FIFO)

```java
import java.util.*;

Deque<String> queue = new ArrayDeque<>();

queue.offer("A");      // add at rear
queue.offer("B");
queue.offer("C");

System.out.println(queue.peek());   // A  (look at front, don't remove)
System.out.println(queue.poll());   // A  (remove from front)
System.out.println(queue);          // [B, C]
```

### 4.4 Using Deque as a Stack (LIFO)

```java
Deque<Integer> stack = new ArrayDeque<>();

stack.push(10);
stack.push(20);
stack.push(30);

System.out.println(stack.peek());   // 30 (top)
System.out.println(stack.pop());    // 30
System.out.println(stack);          // [20, 10]
```

### 4.5 `ArrayDeque` vs `LinkedList`

| Feature | `ArrayDeque` | `LinkedList` |
|---|---|---|
| Internal structure | **Resizable circular array** | **Doubly linked list** |
| Insert/remove at ends | O(1) (amortized) | O(1) |
| Memory | Compact | Extra memory per node (prev + next pointers + node object) |
| Cache friendliness | Great (contiguous memory) | Poor (nodes scattered) |
| Allows `null`? | **No** (throws `NullPointerException`) | Yes |
| Also a `List`? | No | Yes (`get(i)`, `add(i, e)`) |
| Typical speed for queue/stack | **Faster** | Slower |

```
 ArrayDeque (circular array)           LinkedList (doubly linked)

 +---+---+---+---+---+                 null <- [A] <-> [B] <-> [C] -> null
 | D | E |   | B | C |                          head              tail
 +---+---+---+---+---+
```

> **Rule of thumb:** Need a queue or stack? Use **`ArrayDeque`**. Use `LinkedList` only if you need `null` elements or List features (index-based access).

---

## 5. Why ArrayDeque Over Stack

Java has an old class called **`java.util.Stack`**. The instructor advises **not using it**.

| Problem with `Stack` | Explanation |
|---|---|
| **Legacy** | Exists since Java 1.0, before the Collections Framework |
| **Extends `Vector`** | Every method is `synchronized`, so it is slow when you don't need thread safety |
| **Breaks the "stack" idea** | Since it is a `Vector`, you can call `get(i)` or `add(i, e)` and mess with the middle |
| **Worse performance** | Locking overhead + less cache-friendly |

```java
// AVOID
Stack<Integer> s = new Stack<>();
s.push(1);
s.get(0);   // allowed! A stack shouldn't allow random access

// PREFER
Deque<Integer> s2 = new ArrayDeque<>();
s2.push(1);
s2.pop();
```

> The `Deque` Javadoc itself recommends `Deque` implementations for stack use.

---

## 6. Queue Operations

The `Queue` interface provides **two versions** of each core operation. The lecture calls them **"safe"** and **"unsafe"**:

- **Safe** methods return a **special value** (`null` or `false`) when something goes wrong. No exception.
- **Unsafe** methods **throw an exception** when something goes wrong.

| Operation | Throws exception ("unsafe") | Returns special value ("safe") |
|---|---|---|
| **Insert** | `add(e)` | `offer(e)` |
| **Remove** (front) | `remove()` | `poll()` |
| **Examine** (front, no removal) | `element()` | `peek()` |

### What "failure" means

| Situation | `add(e)` | `offer(e)` |
|---|---|---|
| Queue is **full** (bounded queue) | throws `IllegalStateException` | returns `false` |

| Situation | `remove()` / `element()` | `poll()` / `peek()` |
|---|---|---|
| Queue is **empty** | throws `NoSuchElementException` | returns `null` |

```java
Queue<Integer> q = new ArrayDeque<>();

// Empty queue
System.out.println(q.poll());     // null   (safe)
System.out.println(q.peek());     // null   (safe)

try {
    q.remove();                   // throws NoSuchElementException
} catch (NoSuchElementException e) {
    System.out.println("Queue is empty!");
}
try {
    q.element();                  // throws NoSuchElementException
} catch (NoSuchElementException e) {
    System.out.println("Nothing to look at!");
}
```

**Which to use?**
- Prefer **`offer` / `poll` / `peek`** in normal code (an empty queue is not exceptional; just check for `null`).
- Use **`add` / `remove` / `element`** when an empty/full queue means a real **bug** and you want to fail loudly.

> Note: a bounded queue (like `ArrayBlockingQueue`) is where `add` vs `offer` really differs. `ArrayDeque` and `LinkedList` grow as needed, so their `add` effectively never fails.

> Because `poll()` and `peek()` return `null` on empty, **don't store `null` in queues** (you couldn't tell "empty" from "null element"). That is why `ArrayDeque` and `PriorityQueue` ban `null`.

---

## 7. PriorityQueue

### 7.1 What is it?

A queue where elements come out in order of **priority**, not arrival time.

**Analogy:** A hospital emergency room. A critical patient who arrives *later* is treated *before* a patient with a minor injury who arrived earlier.

```
 Normal Queue (FIFO):             PriorityQueue (default = smallest first):

 in:  5, 1, 8, 3                  in:  5, 1, 8, 3
 out: 5, 1, 8, 3                  out: 1, 3, 5, 8
```

### 7.2 Basic Example

```java
import java.util.*;

PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.offer(5);
pq.offer(1);
pq.offer(8);
pq.offer(3);

System.out.println(pq.peek());   // 1   (smallest = highest priority)
System.out.println(pq.poll());   // 1
System.out.println(pq.poll());   // 3
System.out.println(pq.poll());   // 5
System.out.println(pq.poll());   // 8
```

### 7.3 Max-Heap and Custom Priority

By default Java gives a **min-heap** (smallest first). Use a `Comparator` to change it.

```java
// Largest first (max-heap)
PriorityQueue<Integer> maxPQ = new PriorityQueue<>(Comparator.reverseOrder());
maxPQ.addAll(List.of(5, 1, 8, 3));
System.out.println(maxPQ.poll());   // 8

// Custom objects: serve the shortest task first
record Task(String name, int duration) {}

PriorityQueue<Task> tasks = new PriorityQueue<>(
        Comparator.comparingInt(Task::duration));
tasks.offer(new Task("Backup", 30));
tasks.offer(new Task("Email", 2));
tasks.offer(new Task("Report", 10));

System.out.println(tasks.poll().name());   // Email
System.out.println(tasks.poll().name());   // Report
```

> Elements must be **`Comparable`** or you must supply a **`Comparator`**, otherwise you get `ClassCastException`.

### 7.4 Important Facts

- Only `peek()` / `poll()` give the **highest-priority element**. Printing the PQ or iterating it does **not** give sorted order.

```java
pq.addAll(List.of(5, 1, 8, 3, 2));
System.out.println(pq);   // e.g. [1, 2, 8, 5, 3]  <- NOT fully sorted!
```

- No `null` elements.
- Not thread-safe (use `PriorityBlockingQueue` for concurrency).
- Unbounded (grows automatically).
- Equal-priority elements have **no guaranteed order** (not stable).

### 7.5 Real-Life Uses

Task schedulers, Dijkstra's shortest path, A* search, "Top K elements" problems, merging K sorted lists, event simulation, load balancing.

---

## 8. Heap

### 8.1 What is a binary heap?

`PriorityQueue` is built on a **binary heap**: a **complete binary tree** that follows the **heap property**.

**Complete binary tree:** every level is completely filled, except possibly the last, which is filled **from left to right** with no gaps.

**Min-heap property:** every **parent is ≤ its children**. So the **smallest element is always at the root**.
(Max-heap is the opposite: parent ≥ children.)

```
 MIN-HEAP example:

              1               <- root = smallest
            /   \
           3     2
          / \   / \
         7   5 4   6

 Check: 1<=3, 1<=2, 3<=7, 3<=5, 2<=4, 2<=6   OK
```

> Note: only parent-vs-child is ordered. Siblings (3 and 2) are **not** ordered against each other. That is why a heap is only *partially* sorted, and why printing a PQ doesn't look sorted.

### 8.2 Stored in a plain array (no node objects!)

Because the tree is **complete**, there are no gaps, so it fits perfectly into an array level by level.

```
 Tree:                          Array:
              1                 index:  0  1  2  3  4  5  6
            /   \                      +--+--+--+--+--+--+--+
           3     2                     | 1| 3| 2| 7| 5| 4| 6|
          / \   / \                    +--+--+--+--+--+--+--+
         7   5 4   6
```

**Index formulas (0-based):**

| Relation | Formula |
|---|---|
| Parent of `i` | `(i - 1) / 2` |
| Left child of `i` | `2*i + 1` |
| Right child of `i` | `2*i + 2` |

```
 Node 3 is at index 1:
   parent  = (1-1)/2 = 0   -> value 1  OK
   left    = 2*1+1   = 3   -> value 7  OK
   right   = 2*1+2   = 4   -> value 5  OK
```

Java's `PriorityQueue` uses exactly this: an `Object[] queue` array internally.

### 8.3 Height is small

A complete binary tree with `n` nodes has height ≈ **log₂(n)**.

| n (elements) | Height |
|---|---|
| 7 | 3 |
| 1,000 | ~10 |
| 1,000,000 | ~20 |

This small height is what makes heap operations O(log n).

---

## 9. Heapify

After inserting or removing, the heap property can break. **Heapify** repairs it by moving one element up or down.

### 9.1 Up-Heapify (a.k.a. "sift up" / "bubble up"): used on **insertion**

**Steps:**
1. Add the new element at the **end** of the array (the next free spot, keeping the tree complete).
2. Compare it with its **parent**. If it is **smaller** (min-heap), **swap**.
3. Repeat until the parent is smaller or you reach the root.

**Example:** Insert `0` into this min-heap.

```
 Start:
              1
            /   \
           3     2
          / \   / \
         7   5 4   6

 Step 1: place 0 at the end (next free slot)
              1
            /   \
           3     2
          / \   / \
         7   5 4   6
        /
       0                     array: [1,3,2,7,5,4,6,0]

 Step 2: compare 0 with parent 7 -> 0 < 7 -> swap
              1
            /   \
           3     2
          / \   / \
         0   5 4   6
        /
       7

 Step 3: compare 0 with parent 3 -> 0 < 3 -> swap
              1
            /   \
           0     2
          / \   / \
         3   5 4   6
        /
       7

 Step 4: compare 0 with parent 1 -> 0 < 1 -> swap
              0              <- new root
            /   \
           1     2
          / \   / \
         3   5 4   6
        /
       7

 Done! Heap property restored.
```

**Time: O(log n)** (at most one swap per level, going up).

### 9.2 Down-Heapify (a.k.a. "sift down" / "bubble down"): used on **removal**

We always remove the **root** (the smallest element).

**Steps:**
1. Save the root (that's the value to return).
2. Move the **last element** of the array to the **root** (keeps the tree complete).
3. Compare it with its **two children**. Swap with the **smaller child** if the element is larger than it.
4. Repeat until it is ≤ both children or it becomes a leaf.

**Example:** `poll()` on this min-heap.

```
 Start:
              1
            /   \
           3     2
          / \   / \
         7   5 4   6

 Step 1: save root (1) to return.
 Step 2: move last element (6) to the root, shrink the array
              6
            /   \
           3     2
          / \   /
         7   5 4

 Step 3: children are 3 and 2 -> smaller is 2. 6 > 2 -> swap with 2
              2
            /   \
           3     6
          / \   /
         7   5 4

 Step 4: 6's only child is 4. 6 > 4 -> swap
              2
            /   \
           3     4
          / \   /
         7   5 6

 Step 5: 6 is a leaf now -> done.   returns 1
```

> **Why swap with the SMALLER child?** If we swapped with the larger child, the smaller child would end up as a child of a bigger parent, which breaks the min-heap rule.

**Time: O(log n)** (at most one swap per level, going down).

### 9.3 Code Sketch (to understand the idea)

```java
class MinHeap {
    int[] a = new int[100];
    int size = 0;

    void offer(int x) {
        a[size] = x;
        siftUp(size);
        size++;
    }

    int poll() {
        int root = a[0];
        a[0] = a[--size];       // last element goes to the root
        siftDown(0);
        return root;
    }

    void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (a[i] >= a[parent]) break;
            swap(i, parent);
            i = parent;
        }
    }

    void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1, right = 2 * i + 2, smallest = i;
            if (left < size && a[left] < a[smallest])   smallest = left;
            if (right < size && a[right] < a[smallest]) smallest = right;
            if (smallest == i) break;
            swap(i, smallest);
            i = smallest;
        }
    }

    void swap(int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
}
```

### 9.4 Bonus: building a heap from many elements

Calling `offer` n times costs O(n log n). But if you already have all elements, you can build the heap faster by running **down-heapify from the last parent down to the root**. That takes **O(n)**.

```java
// Uses the O(n) bulk build internally
PriorityQueue<Integer> pq = new PriorityQueue<>(List.of(9, 4, 7, 1, 6));
```

---

## 10. Complexity Cheat Sheet

| Structure | Insert | Remove | Peek | Search |
|---|---|---|---|---|
| Queue / Stack (array or linked list) | O(1) | O(1) | O(1) | O(n) |
| `ArrayDeque` (both ends) | O(1) amortized | O(1) | O(1) | O(n) |
| `LinkedList` (both ends) | O(1) | O(1) | O(1) | O(n) |
| `PriorityQueue` | **O(log n)** | **O(log n)** (`poll`) | **O(1)** | O(n) |
| `PriorityQueue.remove(Object)` | - | O(n) | - | - |

```
Quick memory:
  Queue & Deque       -> everything at the ends is O(1)
  PriorityQueue       -> peek O(1), offer/poll O(log n)
  Up-heapify          -> O(log n)   (insert)
  Down-heapify        -> O(log n)   (remove)
```

---

## 11. Common Mistakes

1. **Using the legacy `Stack` class.** Use `ArrayDeque` instead.
2. **Mixing up `push`/`pop` ends and `offer`/`poll` ends on a Deque.**
   `push` adds at the **front**, `offer` adds at the **rear**. If you `push` and then `poll`, you'll still get the front, which is the item you just pushed.
3. **Putting `null` into `ArrayDeque` or `PriorityQueue`** causes `NullPointerException`.
4. **Expecting a `PriorityQueue` to print sorted.** Only `poll()` order is guaranteed.
5. **Using `remove()` / `element()` on an empty queue** throws `NoSuchElementException`. Use `poll()` / `peek()` and check for `null`.
6. **Forgetting a `Comparator`** for custom objects in a `PriorityQueue`, which causes `ClassCastException`.
7. **Thinking a PQ is a max-heap by default.** It's a **min-heap**. Use `Comparator.reverseOrder()` for max.
8. **Using `LinkedList` as a queue "because it's easy".** `ArrayDeque` is usually faster and uses less memory.

---

## 12. Interview Questions

**Q1. Queue vs Stack?**
Queue is FIFO (insert at rear, remove at front). Stack is LIFO (insert and remove at the top, one end only).

**Q2. How do you implement a queue with an array in O(1)?**
Use a circular array with `front` and `rear` indexes and `% capacity` to wrap around. That is how `ArrayDeque` works.

**Q3. How do you implement a queue with a linked list in O(1)?**
Keep both head (front) and tail (rear) pointers. Add at the tail, remove at the head.

**Q4. What is the hierarchy of Queue in Java?**
`Iterable` → `Collection` → `Queue` → `Deque` → `ArrayDeque` / `LinkedList`. `PriorityQueue` also implements `Queue`.

**Q5. Why prefer `ArrayDeque` over `Stack`?**
`Stack` is legacy, extends synchronized `Vector` (slow), and exposes non-stack methods. `ArrayDeque` is faster, lighter, and cleaner.

**Q6. `ArrayDeque` vs `LinkedList`?**
`ArrayDeque`: circular array, faster, less memory, no nulls. `LinkedList`: doubly linked nodes, allows nulls, also a `List`.

**Q7. `add()` vs `offer()`, `remove()` vs `poll()`, `element()` vs `peek()`?**
First of each pair throws an exception on failure; second returns `false`/`null`.

**Q8. How does `PriorityQueue` work internally?**
It's a binary min-heap stored in an array. Parent of `i` is `(i-1)/2`; children are `2i+1` and `2i+2`.

**Q9. Is the `PriorityQueue` iteration sorted?**
No. The heap is only partially ordered (parent ≤ children). Only `poll()` gives the next-smallest.

**Q10. Explain up-heapify and down-heapify.**
Up-heapify (insert): add at the end, swap with the parent while smaller. Down-heapify (remove): put the last element at the root, swap with the smaller child while larger. Both are O(log n).

**Q11. Time complexity of `PriorityQueue` operations?**
`offer` and `poll` O(log n), `peek` O(1), `remove(Object)` and `contains` O(n).

**Q12. How do you create a max-heap in Java?**
`new PriorityQueue<>(Comparator.reverseOrder())`.

**Q13. Why is a heap stored in an array and not with node objects?**
A heap is a *complete* binary tree, so there are no gaps and index math finds parent/children. This saves memory and is cache-friendly.

---

## Quick Revision Cheat Sheet

```
QUEUE (FIFO)  insert rear, remove front       -> line at a counter
STACK (LIFO)  insert/remove at top            -> pile of plates
Both can be O(1): array (circular for queue) or linked list (head+tail)

HIERARCHY:  Iterable > Collection > Queue > Deque > ArrayDeque / LinkedList
                                     Queue > PriorityQueue

USE:  Queue  -> ArrayDeque (offer / poll / peek)
      Stack  -> ArrayDeque (push / pop / peek)   NOT java.util.Stack

METHODS:        throws         returns null/false
  insert        add(e)         offer(e)
  remove        remove()       poll()
  examine       element()      peek()

PRIORITYQUEUE:  min-heap by default, array-based
   parent = (i-1)/2   left = 2i+1   right = 2i+2
   peek O(1) | offer O(log n) | poll O(log n)
   up-heapify   : insert at end, swap UP with bigger parent
   down-heapify : last -> root, swap DOWN with SMALLER child
   iteration/print order is NOT sorted
```
