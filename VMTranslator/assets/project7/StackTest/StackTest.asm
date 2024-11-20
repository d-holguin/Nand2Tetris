// push constant 17
@17
D=A
@SP
A=M
M=D
@SP
M=M+1

// push constant 17
@17
D=A
@SP
A=M
M=D
@SP
M=M+1

// eq
@SP
AM=M-1
D=M
A=A-1
D=M-D
@LABEL_0
D;JEQ
@SP
A=M-1
M=0
@LABEL_1
0;JMP
(LABEL_0)
@SP
A=M-1
M=-1
(LABEL_1)

// push constant 17
@17
D=A
@SP
A=M
M=D
@SP
M=M+1

// push constant 16
@16
D=A
@SP
A=M
M=D
@SP
M=M+1

// eq
@SP
AM=M-1
D=M
A=A-1
D=M-D
@LABEL_2
D;JEQ
@SP
A=M-1
M=0
@LABEL_3
0;JMP
(LABEL_2)
@SP
A=M-1
M=-1
(LABEL_3)

// push constant 16
@16
D=A
@SP
A=M
M=D
@SP
M=M+1

// push constant 17
@17
D=A
@SP
A=M
M=D
@SP
M=M+1

// eq
@SP
AM=M-1
D=M
A=A-1
D=M-D
@LABEL_4
D;JEQ
@SP
A=M-1
M=0
@LABEL_5
0;JMP
(LABEL_4)
@SP
A=M-1
M=-1
(LABEL_5)

// push constant 892
@892
D=A
@SP
A=M
M=D
@SP
M=M+1

// push constant 891
@891
D=A
@SP
A=M
M=D
@SP
M=M+1

// lt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@LABEL_6
D;JLT
@SP
A=M-1
M=0
@LABEL_7
0;JMP
(LABEL_6)
@SP
A=M-1
M=-1
(LABEL_7)

// push constant 891
@891
D=A
@SP
A=M
M=D
@SP
M=M+1

// push constant 892
@892
D=A
@SP
A=M
M=D
@SP
M=M+1

// lt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@LABEL_8
D;JLT
@SP
A=M-1
M=0
@LABEL_9
0;JMP
(LABEL_8)
@SP
A=M-1
M=-1
(LABEL_9)

// push constant 891
@891
D=A
@SP
A=M
M=D
@SP
M=M+1

// push constant 891
@891
D=A
@SP
A=M
M=D
@SP
M=M+1

// lt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@LABEL_10
D;JLT
@SP
A=M-1
M=0
@LABEL_11
0;JMP
(LABEL_10)
@SP
A=M-1
M=-1
(LABEL_11)

// push constant 32767
@32767
D=A
@SP
A=M
M=D
@SP
M=M+1

// push constant 32766
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1

// gt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@LABEL_12
D;JGT
@SP
A=M-1
M=0
@LABEL_13
0;JMP
(LABEL_12)
@SP
A=M-1
M=-1
(LABEL_13)

// push constant 32766
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1

// push constant 32767
@32767
D=A
@SP
A=M
M=D
@SP
M=M+1

// gt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@LABEL_14
D;JGT
@SP
A=M-1
M=0
@LABEL_15
0;JMP
(LABEL_14)
@SP
A=M-1
M=-1
(LABEL_15)

// push constant 32766
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1

// push constant 32766
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1

// gt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@LABEL_16
D;JGT
@SP
A=M-1
M=0
@LABEL_17
0;JMP
(LABEL_16)
@SP
A=M-1
M=-1
(LABEL_17)

// push constant 57
@57
D=A
@SP
A=M
M=D
@SP
M=M+1

// push constant 31
@31
D=A
@SP
A=M
M=D
@SP
M=M+1

// push constant 53
@53
D=A
@SP
A=M
M=D
@SP
M=M+1

// add
@SP
AM=M-1
D=M
A=A-1
M=M+D

// push constant 112
@112
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

// neg
@SP
A=M-1
M=-M

// and
@SP
AM=M-1
D=M
A=A-1
M=M&D

// push constant 82
@82
D=A
@SP
A=M
M=D
@SP
M=M+1

// or
@SP
AM=M-1
D=M
A=A-1
M=M|D

// not
@SP
A=M-1
M=!M

