package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"
)

var labelCounter int = 0

func main() {

	if len(os.Args) < 2 {
		log.Fatalln("Usage: VMTranslator <inputfile.vm>")
	}

	inputFile := os.Args[1]

	if err := validateFile(inputFile); err != nil {
		log.Fatalln(err)
	}

	var commands, err = parser(inputFile)

	if err != nil {
		log.Fatalln("Error during parsing:", err)
	}
	outputFileName := fmt.Sprintf("%s.asm", strings.Split(inputFile, ".")[0])
	if err := codeWriter(commands, outputFileName); err != nil {
		log.Fatalln(err)
	}

	fmt.Println("Translation complete!")
}

func validateFile(filePath string) error {
	if _, err := os.Stat(filePath); err != nil {
		return fmt.Errorf("path does not exist: %v", err)
	}
	return nil
}

func parser(inputFile string) ([]VMCommand, error) {
	fmt.Println("Parsing file:", inputFile)
	file, err := os.Open(inputFile)
	if err != nil {
		return nil, err
	}
	defer func() {
		if err := file.Close(); err != nil {
			fmt.Println("Error closing file:", err)
		}
	}()

	scanner := bufio.NewScanner(file)
	var commands []VMCommand

	for scanner.Scan() {
		line := scanner.Text()
		line = strings.TrimSpace(strings.Split(line, "//")[0])
		fileName := strings.Split(strings.Split(inputFile, "/")[len(strings.Split(inputFile, "/"))-1], ".")[0]
		if len(line) > 0 {
			parts := strings.Fields(line)
			switch parts[0] {
			case "push":
				index, _ := strconv.Atoi(parts[2])
				commands = append(commands, &PushCommand{Segment: parts[1], Index: index, OriginalCommand: line, Filename: fileName})
			case "pop":
				index, _ := strconv.Atoi(parts[2])
				commands = append(commands, &PopCommand{Segment: parts[1], Index: index, OriginalCommand: line, Filename: fileName})
			case "add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not":
				commands = append(commands, &ArithmeticCommand{Operation: parts[0], OriginalCommand: line})
			case "label":
				commands = append(commands, &LabelCommand{Label: parts[1], OriginalCommand: line})
			case "goto":
				commands = append(commands, &GotoCommand{Label: parts[1], OriginalCommand: line})
			case "if-goto":
				commands = append(commands, &IfGotoCommand{Label: parts[1], OriginalCommand: line})
			case "function":
				nArgs, _ := strconv.Atoi(parts[2])
				commands = append(commands, &FunctionCommand{FunctionName: parts[1], NArgs: nArgs, OriginalCommand: line})
			case "call":
				nArgs, _ := strconv.Atoi(parts[2])
				commands = append(commands, &CallCommand{FunctionName: parts[1], NArgs: nArgs, OriginalCommand: line})
			case "return":
				commands = append(commands, &ReturnCommand{OriginalCommand: line})
			}
		}
	}

	if err := scanner.Err(); err != nil {
		return nil, err
	}

	return commands, nil
}

func codeWriter(commands []VMCommand, outputFile string) error {
	output, err := os.Create(outputFile)
	if err != nil {
		return err
	}
	defer func() {
		if err := output.Close(); err != nil {
			fmt.Println("Error closing file:", err)
		}
	}()

	writer := bufio.NewWriter(output)

	for _, command := range commands {

		if _, err := writer.WriteString("// " + fmt.Sprintf("%s", command.GetOriginalCommand())); err != nil {
			fmt.Println("Error writing to file:", err)
		}

		assembly := removeLeadingIndentation(command.GenerateAssembly())
		if _, err := writer.WriteString(assembly + "\n"); err != nil {
			fmt.Println("Error writing assembly:", err)
		}

		if err := writer.Flush(); err != nil {
			return fmt.Errorf("error writing to file: %v", err)
		}
	}

	return nil
}

func removeLeadingIndentation(s string) string {
	lines := strings.Split(s, "\n")
	for i, line := range lines {
		lines[i] = strings.TrimLeft(line, " \t")
	}
	return strings.Join(lines, "\n")
}

type VMCommand interface {
	GenerateAssembly() string
	GetOriginalCommand() string
}

type PushCommand struct {
	Segment         string
	Index           int
	OriginalCommand string
	Filename        string
}

func (p *PushCommand) GenerateAssembly() string {
	var assembly string
	switch p.Segment {
	case "constant":
		assembly = fmt.Sprintf(`
			@%d
			D=A
			@SP
			A=M
			M=D
			@SP
			M=M+1
		`, p.Index)
	case "local":
		assembly = fmt.Sprintf(`
			@%d
			D=A
			@LCL
			A=M+D
			D=M
			@SP
			A=M
			M=D
			@SP
			M=M+1
		`, p.Index)
	case "argument":
		assembly = fmt.Sprintf(`
			@%d
			D=A
			@ARG
			A=M+D
			D=M
			@SP
			A=M
			M=D
			@SP
			M=M+1
		`, p.Index)
	case "this":
		assembly = fmt.Sprintf(`
			@%d
			D=A
			@THIS
			A=M+D
			D=M
			@SP
			A=M
			M=D
			@SP
			M=M+1
		`, p.Index)
	case "that":
		assembly = fmt.Sprintf(`
			@%d
			D=A
			@THAT
			A=M+D
			D=M
			@SP
			A=M
			M=D
			@SP
			M=M+1
		`, p.Index)
	case "temp":
		assembly = fmt.Sprintf(`
			@%d
			D=M
			@SP
			A=M
			M=D
			@SP
			M=M+1
		`, p.Index+5)
	case "pointer":
		if p.Index == 0 {
			// push pointer 0 -> THIS (RAM[3])
			assembly = `
			@THIS
			D=M
			@SP
			A=M
			M=D
			@SP
			M=M+1
		`
		} else if p.Index == 1 {
			// push pointer 1 -> THAT (RAM[4])
			assembly = `
			@THAT
			D=M
			@SP
			A=M
			M=D
			@SP
			M=M+1
		`
		}

	case "static":
		// Static variables (e.g., Xxx.i) start at RAM[16]
		assembly = fmt.Sprintf(`
			@%s.%d
			D=M
			@SP
			A=M
			M=D
			@SP
			M=M+1
		`, p.Filename, p.Index)
	}

	return removeLeadingIndentation(assembly)
}

func (p *PushCommand) GetOriginalCommand() string {
	return p.OriginalCommand
}

type PopCommand struct {
	Segment         string
	Index           int
	OriginalCommand string
	Filename        string
}

func (p *PopCommand) GenerateAssembly() string {
	var assembly string
	switch p.Segment {
	case "local":
		assembly = fmt.Sprintf(`
			@%d
			D=A
			@LCL
			D=M+D
			@R13
			M=D
			@SP
			AM=M-1
			D=M
			@R13
			A=M
			M=D
		`, p.Index)
	case "argument":
		assembly = fmt.Sprintf(`
			@%d
			D=A
			@ARG
			D=M+D
			@R13
			M=D
			@SP
			AM=M-1
			D=M
			@R13
			A=M
			M=D
		`, p.Index)
	case "this":
		assembly = fmt.Sprintf(`
			@%d
			D=A
			@THIS
			D=M+D
			@R13
			M=D
			@SP
			AM=M-1
			D=M
			@R13
			A=M
			M=D
		`, p.Index)
	case "that":
		assembly = fmt.Sprintf(`
			@%d
			D=A
			@THAT
			D=M+D
			@R13
			M=D
			@SP
			AM=M-1
			D=M
			@R13
			A=M
			M=D
		`, p.Index)
	case "temp":
		assembly = fmt.Sprintf(`
			@SP
			AM=M-1
			D=M
			@%d
			M=D
		`, p.Index+5)
	case "pointer":
		if p.Index == 0 {
			// pop pointer 0 -> THIS (RAM[3])
			assembly = `
			@SP
			AM=M-1
			D=M
			@THIS
			M=D
			`
		} else if p.Index == 1 {
			// pop pointer 1 -> THAT (RAM[4])
			assembly = `
			@SP
			AM=M-1
			D=M
			@THAT
			M=D
		`
		}
	case "static":
		assembly = fmt.Sprintf(`
			@SP
			AM=M-1
			D=M
			@%s.%d
			M=D
		`, p.Filename, p.Index)
	}

	return removeLeadingIndentation(assembly)
}

func (p *PopCommand) GetOriginalCommand() string {
	return p.OriginalCommand
}

type ArithmeticCommand struct {
	Operation       string
	OriginalCommand string
}

func (a *ArithmeticCommand) GenerateAssembly() string {
	switch a.Operation {
	case "add":
		return `
		@SP
		AM=M-1
		D=M
		A=A-1
		M=M+D
		`
	case "sub":
		return `
		@SP
		AM=M-1
		D=M
		A=A-1
		M=M-D
		`
	case "neg":
		return `
		@SP
		A=M-1
		M=-M
		`
	case "eq", "gt", "lt":
		trueLabel := uniqueLabel()
		endLabel := uniqueLabel()
		compInstruction := map[string]string{
			"eq": "JEQ",
			"gt": "JGT",
			"lt": "JLT",
		}[a.Operation]
		return fmt.Sprintf(`
		@SP
		AM=M-1
		D=M
		A=A-1
		D=M-D
		@%s
		D;%s
		@SP
		A=M-1
		M=0
		@%s
		0;JMP
		(%s)
		@SP
		A=M-1
		M=-1
		(%s)
		`, trueLabel, compInstruction, endLabel, trueLabel, endLabel)
	case "and":
		return `
		@SP
		AM=M-1
		D=M
		A=A-1
		M=M&D
		`
	case "or":
		return `
		@SP
		AM=M-1
		D=M
		A=A-1
		M=M|D
		`
	case "not":
		return `
		@SP
		A=M-1
		M=!M
		`
	}
	return ""
}

func uniqueLabel() string {
	label := fmt.Sprintf("LABEL_%d", labelCounter)
	labelCounter++
	return label
}

func (a *ArithmeticCommand) GetOriginalCommand() string {
	return a.OriginalCommand
}

type LabelCommand struct {
	Label           string
	OriginalCommand string
}

func (l *LabelCommand) GenerateAssembly() string {
	return fmt.Sprintf("(%s)\n", l.Label)
}

func (l *LabelCommand) GetOriginalCommand() string {
	return l.OriginalCommand
}

type GotoCommand struct {
	Label           string
	OriginalCommand string
}

func (g *GotoCommand) GenerateAssembly() string {
	return fmt.Sprintf("@%s\n0:JMP\n", g.Label)
}

func (g *GotoCommand) GetOriginalCommand() string {
	return g.OriginalCommand
}

type IfGotoCommand struct {
	Label           string
	OriginalCommand string
}

func (i *IfGotoCommand) GenerateAssembly() string {
	assembly := fmt.Sprintf(`
		@SP
		AM=M-1
		D=M
		D;JNE
		`)
	return removeLeadingIndentation(assembly)
}

func (i *IfGotoCommand) GetOriginalCommand() string {
	return i.OriginalCommand
}

type FunctionCommand struct {
	FunctionName    string
	NArgs           int
	OriginalCommand string
}

func (f *FunctionCommand) GenerateAssembly() string {
	assembly := fmt.Sprintf("(%s)\n", f.FunctionName)

	for i := 0; i < f.NArgs; i++ {
		preformattedAssembly := fmt.Sprintf(`
					@SP
					A=M
					M=0
					@SP
					M=M+1
					`)
		assembly += removeLeadingIndentation(preformattedAssembly)
	}
	return assembly
}

func (f *FunctionCommand) GetOriginalCommand() string {
	return f.OriginalCommand
}

type CallCommand struct {
	FunctionName    string
	NArgs           int
	OriginalCommand string
}

func (c *CallCommand) GenerateAssembly() string {
	// TODO
	return fmt.Sprintf("")
}
func (c *CallCommand) GetOriginalCommand() string {
	return c.OriginalCommand
}

type ReturnCommand struct {
	OriginalCommand string
}

func (r *ReturnCommand) GenerateAssembly() string {
	// TODO
	return fmt.Sprintf("")
}

func (r *ReturnCommand) GetOriginalCommand() string {
	return r.OriginalCommand
}
