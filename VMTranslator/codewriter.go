package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"
)

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

		// Write orginal vm code as comment
		if _, err := writer.WriteString(fmt.Sprintf("// %s\n", strings.TrimSpace(command.GetOriginalCommand()))); err != nil {
			fmt.Println("Error writing to file:", err)
		}

		assembly := strings.TrimSpace(command.GenerateAssembly())
		assembly = removeLeadingIndentation(assembly)
		if _, err := writer.WriteString(assembly + "\n\n"); err != nil {
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
	var cleanedLines []string
	for _, line := range lines {
		trimmedLine := strings.TrimSpace(line)
		if trimmedLine != "" {
			cleanedLines = append(cleanedLines, trimmedLine)
		}
	}
	return strings.Join(cleanedLines, "\n")
}
