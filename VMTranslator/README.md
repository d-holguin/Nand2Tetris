# VM Translator


Note the directory structure might seem strange it was to make it easier to submit for the coursera course. 

## To Run

```bash
go version `1.23.2`
go run . <filename>
```

## Push / Pop Commands
### Syntax
```vm
push segment index
pop segment index
```

### Memory Segments
- `local` — The local segment of the current function.
- `argument` — The argument segment of the current function.
- `this` — Points to the base address of the current object (used with objects).
- `that` — Points to the base address of the current array (used with arrays).
- `constant` — A constant value, e.g., `push constant 5`.
- `static` — A segment that represents variables shared across different functions.
- `pointer` — A segment to manipulate `this` and `that`.
- `temp` — Temporary storage between RAM addresses 5–12.

### Stack Behavior
- Every `push` command adds a value to the stack and increments the `SP` pointer.
- Every `pop` command removes a value from the stack and decrements the `SP` pointer.

## Arithmetic / Logical Commands

Returns integer:
- `add`, `sub`, `neg`

Returns boolean:
- `eq`, `gt`, `lt` (comparison returns true/false)
- `and`, `or`, `not` (logical operations)

## Branching Commands
- `label labelName` — Declares a label to be referenced later.
- `goto labelName` — Unconditional jump to a label.
- `if-goto labelName` — Conditional jump to a label if the topmost stack value is non-zero.

## Function Commands
- `function functionName nArgs` — Declares a function with `nArgs` local variables.
- `call functionName nArgs` — Calls a function and passes `nArgs` arguments.
- `return` — Returns from the current function.

### Return Address in Function Calls
- When calling a function, a **return address** is pushed onto the stack to ensure the program flow returns to the correct location after the function completes.

## VM Mapping on Hack

### SP
The `SP` predefined symbol points to the memory address just following the topmost stack value in the host RAM.

### LCL, ARG, THIS, THAT
The `LCL`, `ARG`, `THIS`, and `THAT` symbols point to the base addresses of the virtual segments `local`, `argument`, `this`, and `that` respectively, in the currently running VM function.

### R13-R15
These registers are general-purpose and can be used for any temporary storage.

### Static Variables
Each static variable `i` in file `xxx.vm` is translated into the assembly symbol `Xxx.i`.

### Bootstrap Code
- The VM-to-Hack translation often requires **bootstrap code** to initialize the `SP` and set up the environment before the VM code runs.


### Hack RAM

| Address Range  | Description                  |
| -------------- | ---------------------------- |
| 0              | `SP` (stack pointer)          |
| 1              | `LCL` (local segment base)    |
| 2              | `ARG` (argument segment base) |
| 3              | `THIS` (this segment base)    |
| 4              | `THAT` (that segment base)    |
| 5–12           | Temp segment                  |
| 13–15          | General-purpose registers     |
| 16–255         | Static variables              |
| 256–2047       | Stack                         |

