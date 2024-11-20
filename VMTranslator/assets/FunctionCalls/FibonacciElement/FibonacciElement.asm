// bootstrap
// Bootstrap Code
// Set SP = 256
@256
D=A
@SP
M=D
// call Sys.init 0
// Push return address
@Sys.init$ret.0
D=A
@SP
A=M
M=D
@SP
M=M+1
// Push LCL
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
// Push ARG
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
// Push THIS
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
// Push THAT
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
// ARG = SP - nArgs - 5
@SP
D=M
@0
D=D-A
@5
D=D-A
@ARG
M=D
// LCL = SP
@SP
D=M
@LCL
M=D
// Goto Sys.init
@Sys.init
0;JMP
// (return address)
(Sys.init$ret.0)

// function Main.fibonacci 0
(Main.fibonacci)

// push argument 0
@0
D=A
@ARG
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1

// push constant 2
@2
D=A
@SP
A=M
M=D
@SP
M=M+1

// lt
// Start comparison (lt)
@SP
AM=M-1        // SP--
D=M           // D = *SP (Top of stack)
A=A-1         // A = SP-1 (Second to top of stack)
D=M-D         // D = *(SP-1) - *(SP) (compare the two topmost values)
@LABEL_0           // Jump to true label if condition holds
D;JLT
@SP
A=M-1         // Set false (0) at SP-1 if condition is false
M=0
@LABEL_1           // Unconditional jump to end
0;JMP
(LABEL_0)          // True label
@SP
A=M-1         // Set true (-1) at SP-1 if condition is true
M=-1
(LABEL_1)          // End label

// if-goto N_LT_2
@SP
AM=M-1
D=M
@N_LT_2
D;JNE

// goto N_GE_2
@N_GE_2
0;JMP

// label N_LT_2
(N_LT_2)

// push argument 0
@0
D=A
@ARG
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1

// return
// FRAME = LCL (store LCL in a temp variable)
@LCL
D=M
@R13
M=D
// RET = *(FRAME - 5) (get return address)
@5
A=D-A
D=M
@R14
M=D
// *ARG = pop() (store return value in ARG[0])
@SP
AM=M-1
D=M
@ARG
A=M
M=D
// SP = ARG + 1
@ARG
D=M+1
@SP
M=D
// Restore THAT
@R13
AM=M-1
D=M
@THAT
M=D
// Restore THIS
@R13
AM=M-1
D=M
@THIS
M=D
// Restore ARG
@R13
AM=M-1
D=M
@ARG
M=D
// Restore LCL
@R13
AM=M-1
D=M
@LCL
M=D
// Goto return address
@R14
A=M
0;JMP

// label N_GE_2
(N_GE_2)

// push argument 0
@0
D=A
@ARG
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1

// push constant 2
@2
D=A
@SP
A=M
M=D
@SP
M=M+1

// sub
@SP
AM=M-1
D=M
A=A-1
M=M-D

// call Main.fibonacci 1
// push return address
@Main.fibonacci$ret.0
D=A
@SP
A=M
M=D
@SP
M=M+1
// push LCL
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
// push ARG
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
// push THIS
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
// push THAT
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
// reposition ARG (SP - nArgs - 5)
@SP
D=M
@1
D=D-A
@5
D=D-A
@ARG
M=D
// reposition LCL
@SP
D=M
@LCL
M=D
// goto function
@Main.fibonacci
0;JMP
// (return address)
(Main.fibonacci$ret.0)

// push argument 0
@0
D=A
@ARG
A=M+D
D=M
@SP
A=M
M=D
@SP
M=M+1

// push constant 1
@1
D=A
@SP
A=M
M=D
@SP
M=M+1

// sub
@SP
AM=M-1
D=M
A=A-1
M=M-D

// call Main.fibonacci 1
// push return address
@Main.fibonacci$ret.1
D=A
@SP
A=M
M=D
@SP
M=M+1
// push LCL
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
// push ARG
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
// push THIS
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
// push THAT
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
// reposition ARG (SP - nArgs - 5)
@SP
D=M
@1
D=D-A
@5
D=D-A
@ARG
M=D
// reposition LCL
@SP
D=M
@LCL
M=D
// goto function
@Main.fibonacci
0;JMP
// (return address)
(Main.fibonacci$ret.1)

// add
@SP
AM=M-1
D=M
A=A-1
M=M+D

// return
// FRAME = LCL (store LCL in a temp variable)
@LCL
D=M
@R13
M=D
// RET = *(FRAME - 5) (get return address)
@5
A=D-A
D=M
@R14
M=D
// *ARG = pop() (store return value in ARG[0])
@SP
AM=M-1
D=M
@ARG
A=M
M=D
// SP = ARG + 1
@ARG
D=M+1
@SP
M=D
// Restore THAT
@R13
AM=M-1
D=M
@THAT
M=D
// Restore THIS
@R13
AM=M-1
D=M
@THIS
M=D
// Restore ARG
@R13
AM=M-1
D=M
@ARG
M=D
// Restore LCL
@R13
AM=M-1
D=M
@LCL
M=D
// Goto return address
@R14
A=M
0;JMP

// function Sys.init 0
(Sys.init)

// push constant 4
@4
D=A
@SP
A=M
M=D
@SP
M=M+1

// call Main.fibonacci 1
// push return address
@Main.fibonacci$ret.2
D=A
@SP
A=M
M=D
@SP
M=M+1
// push LCL
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
// push ARG
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
// push THIS
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
// push THAT
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
// reposition ARG (SP - nArgs - 5)
@SP
D=M
@1
D=D-A
@5
D=D-A
@ARG
M=D
// reposition LCL
@SP
D=M
@LCL
M=D
// goto function
@Main.fibonacci
0;JMP
// (return address)
(Main.fibonacci$ret.2)

// label END
(END)

// goto END
@END
0;JMP

