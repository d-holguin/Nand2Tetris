package main

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
)

func parser(inputFile string, includeBootStrap bool) ([]VMCommand, error) {
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
	var cmds []VMCommand

	fileName := strings.Split(strings.Split(inputFile, "/")[len(strings.Split(inputFile, "/"))-1], ".")[0]
	fmt.Println("Parsing file:", fileName)

	if includeBootStrap {
		cmds = append(cmds, &BootstrapCommand{})
	}

	for scanner.Scan() {
		line := scanner.Text()
		line = strings.TrimSpace(strings.Split(line, "//")[0])

		if len(line) > 0 {
			parts := strings.Fields(line)
			switch parts[0] {
			case "push":
				index, _ := strconv.Atoi(parts[2])
				cmds = append(cmds, &PushCommand{Segment: parts[1], Index: index, OriginalCommand: line, Filename: fileName})
			case "pop":
				index, _ := strconv.Atoi(parts[2])
				cmds = append(cmds, &PopCommand{Segment: parts[1], Index: index, OriginalCommand: line, Filename: fileName})
			case "add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not":
				cmds = append(cmds, &ArithmeticCommand{Operation: parts[0], OriginalCommand: line})
			case "label":
				cmds = append(cmds, &LabelCommand{Label: parts[1], OriginalCommand: line})
			case "goto":
				cmds = append(cmds, &GotoCommand{Label: parts[1], OriginalCommand: line})
			case "if-goto":
				cmds = append(cmds, &IfGotoCommand{Label: parts[1], OriginalCommand: line})
			case "function":
				nVars, _ := strconv.Atoi(parts[2])
				cmds = append(cmds, &FunctionCommand{FunctionName: parts[1], NVars: nVars, OriginalCommand: line})
			case "call":
				nArgs, _ := strconv.Atoi(parts[2])
				cmds = append(cmds, &CallCommand{FunctionName: parts[1], NArgs: nArgs, OriginalCommand: line})
			case "return":
				cmds = append(cmds, &ReturnCommand{OriginalCommand: line})
			}
		}
	}

	if err := scanner.Err(); err != nil {
		return nil, err
	}

	return cmds, nil
}
