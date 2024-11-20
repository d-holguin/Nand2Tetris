import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Compiler {
    private final String inputPath;

    public Compiler(String inputPath) {
        this.inputPath = inputPath;
    }

    public void compile() {
        try {
            File inputFile = new File(inputPath);
            if (inputFile.isDirectory()) {
                processDirectory(inputPath);
            } else {
                processFile(inputPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processDirectory(String directoryPath) {
        File dir = new File(directoryPath);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jack"));
        if (files != null) {
            for (File file : files) {
                System.out.println("--- Reading file: " + file.getPath() + " ---");
                processFile(file.getPath());
            }
        }
    }

    private static void processFile(String inputFilePath) {
        System.out.println("Processing file: " + inputFilePath);

        // Sanitize lines by removing comments and whitespace
        List<String> lines = sanitize(inputFilePath);

        // Tokenize sanitized lines
        List<String> tokens = JackTokenizer.tokenizePass(lines);

        // output the XML for tokens
        //Compiler.outputTokensXML(tokens, inputFilePath);

        // output jack xml
        // outputJackXML(jackXML, inputFilePath);

        CompilationEngine compilationEngine = new CompilationEngine(tokens);
        compilationEngine.compile();
        List<String> code = compilationEngine.getVmCodeOutput();

        outputVMCode(code, inputFilePath);
    }

    private static void outputVMCode(List<String> vmcode, String inputFilePath) {
        String outputPath = Compiler.getOutputFilePath(inputFilePath, ".vm");
        System.out.println("--- writing file: " + outputPath + " ---");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (String code : vmcode) {
                writer.write(code);
                writer.newLine();
            }
        } catch (Exception e) {
            System.out.println("Error writing tokens to file: " + e.getMessage());
        }
    }

    private static void outputJackXML(List<String> tokens, String inputFilePath) {
        String outputPath = Compiler.getOutputFilePath(inputFilePath, ".xml");
        System.out.println("--- writing file: " + outputPath + " ---");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (String token : tokens) {
                writer.write(token);
                writer.newLine();
            }
        } catch (Exception e) {
            System.out.println("Error writing tokens to file: " + e.getMessage());
        }
    }

    private static void outputTokensXML(List<String> tokens, String inputFilePath) {
        String outputPath = Compiler.getOutputFilePath(inputFilePath, "T.xml");
        System.out.println("--- writing file: " + outputPath + " ---");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("<tokens>\n");
            for (String token : tokens) {
                writer.write(token);
                writer.newLine();
            }
            writer.write("</tokens>");
        } catch (Exception e) {
            System.out.println("Error writing tokens to file: " + e.getMessage());
        }
    }

    private static List<String> sanitize(String inputPath) {
        List<String> sanitizedLines = new ArrayList<>();
        boolean inBlockComment = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Remove leading and trailing spaces

                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                // Handle cases where a block comment starts and ends on the same line
                if (line.contains("/*") && line.contains("*/")) {
                    int startIdx = line.indexOf("/*");
                    int endIdx = line.indexOf("*/");
                    // Remove the comment and any surrounding whitespace
                    line = (line.substring(0, startIdx) + line.substring(endIdx + 2)).trim();
                }

                // Handle block comments spanning multiple lines
                if (inBlockComment) {
                    // Look for the end of the block comment
                    if (line.contains("*/")) {
                        line = line.substring(line.indexOf("*/") + 2).trim();
                        inBlockComment = false;
                    } else {
                        // Skip the entire line if we are still in a block comment
                        continue;
                    }
                }

                // Remove single-line comments (//)
                if (line.contains("//")) {
                    line = line.substring(0, line.indexOf("//")).trim();
                }

                // Check for start of a block comment (/*) after handling single-line comments
                if (line.contains("/*")) {
                    int commentStart = line.indexOf("/*");
                    line = line.substring(0, commentStart).trim();
                    inBlockComment = true;
                }

                // Add the line if it still has content and we're not in a block comment
                if (!line.isEmpty() && !inBlockComment) {
                    sanitizedLines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        return sanitizedLines;
    }

    private static String getOutputFilePath(String inputFilePath, String extension) {
        File file = new File(inputFilePath);
        String directoryPath = file.getParent();
        String fileNameWithoutExtension = file.getName().replaceFirst("[.][^.]+$", "");
        return (directoryPath != null ? directoryPath : ".") + File.separator + fileNameWithoutExtension + extension;
    }
}
