import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CompilationEngine {
    private static final Set<String> OPERATORS = Set.of("+", "-", "*", "/", "&", "|", "<", ">", "=");
    private final List<String> tokens;
    private int currentIndex;
    private final List<String> xmlOutput;
    private int indentLevel = 0;
    private int labelCounter = 0;
    private final SymbolTable symbolTable;
    private final VMWriter vmWriter;
    private String className;

    public CompilationEngine(List<String> tokens) {
        this.tokens = tokens;
        this.currentIndex = 0;
        this.xmlOutput = new ArrayList<>();
        this.symbolTable = new SymbolTable();
        this.vmWriter = new VMWriter();
    }

    public void compile() {

        addOutputLine("<class>");
        indentLevel++;

        matchToken("class");
        this.className = stripToken(currentToken());
        matchToken("className");
        matchToken("{");

        while (currentTokenMatches("static") || currentTokenMatches("field")) {
            compileClassVarDec();
        }

        // Parse subroutine declarations (constructor, function, method)
        while (currentTokenMatches("constructor") || currentTokenMatches("function") || currentTokenMatches("method")) {
            compileSubroutineDec();
        }

        matchToken("}");  // Matches the closing brace for the class body
        indentLevel--;
        addOutputLine("</class>");

    }

    public List<String> getXmlOutput() {
        return this.xmlOutput;
    }

    public List<String> getVmCodeOutput() {
        return this.vmWriter.getVMCode();
    }

    private void compileClassVarDec() {
        addOutputLine("<classVarDec>");
        indentLevel++;
        String kind;
        // Match 'static' or 'field'
        if (currentTokenMatches("static")) {
            kind = "static";
            matchToken("static");
        } else if (currentTokenMatches("field")) {
            kind = "field";
            matchToken("field");
        } else {
            throw new IllegalStateException("Syntax error: Expected 'static' or 'field', found " + stripToken(currentToken()));
        }
        String type = stripToken(currentToken());
        matchToken("type");            // "int", "char", "boolean", or a class name
        String name = stripToken(currentToken());
        matchToken("varName");         // variable name

        // Define the variable in the symbol table
        symbolTable.define(name, type, kind);

        while (currentTokenMatches(",")) {
            matchToken(",");
            name = stripToken(currentToken());
            matchToken("varName");
            symbolTable.define(name, type, kind);
        }

        matchToken(";");
        indentLevel--;
        addOutputLine("</classVarDec>");
    }


    private void compileSubroutineDec() {
        addOutputLine("<subroutineDec>");
        indentLevel++;

        symbolTable.startSubroutine();

        String subroutineType;

        // Match "constructor", "function", or "method"
        if (currentTokenMatches("constructor")) {
            subroutineType = "constructor";
            matchToken("constructor");
        } else if (currentTokenMatches("function")) {
            subroutineType = "function";
            matchToken("function");
        } else if (currentTokenMatches("method")) {
            subroutineType = "method";
            matchToken("method");
            symbolTable.define("this", className, "argument");  // Define 'this' in the symbol table
        } else {
            throw new IllegalStateException("Syntax error: Expected constructor, function, or method, found " + stripToken(currentToken()));
        }

        String returnType = stripToken(currentToken());
        matchToken("type");             // Match the return type (void, int, or class name)

        String subroutineName = stripToken(currentToken());
        matchToken("subroutineName");   // Match the subroutine name (an identifier)

        matchToken("(");
        compileParameterList();         // Parse the parameter list
        matchToken(")");

        compileSubroutineBody(subroutineName, subroutineType);  // Pass subroutineName and subroutineType
        indentLevel--;
        addOutputLine("</subroutineDec>");
    }


    private void compileParameterList() {
        addOutputLine("<parameterList>");
        indentLevel++;
        if (currentTokenMatches("type")) {
            String type = stripToken(currentToken());
            matchToken("type");
            String name = stripToken(currentToken());
            matchToken("varName");

            // define argument in the symbol table
            symbolTable.define(name, type, "argument");

            // handle additional parameters separated by commas
            while (currentTokenMatches(",")) {
                matchToken(",");
                type = stripToken(currentToken());
                matchToken("type");
                name = stripToken(currentToken());
                matchToken("varName");
                symbolTable.define(name, type, "argument");
            }
        }
        indentLevel--;
        addOutputLine("</parameterList>");
    }


    private void compileSubroutineBody(String subroutineName, String subroutineType) {
        addOutputLine("<subroutineBody>");
        indentLevel++;
        matchToken("{");

        // Parse local variable declarations (var) within the subroutine body
        while (currentTokenMatches("var")) {
            compileVarDec();
        }

        // After parsing local variables, get the count
        int numLocals = symbolTable.varCount("var");

        // Generate the VM function declaration
        vmWriter.writeFunction(className + "." + subroutineName, numLocals);

        // Handle constructor and method setup
        if (subroutineType.equals("constructor")) {
            // Allocate memory for the new object
            int fieldCount = symbolTable.varCount("field");
            vmWriter.writePush("constant", fieldCount);   // Allocate memory for the object
            vmWriter.writeCall("Memory.alloc", 1);       // Call Memory.alloc function
            vmWriter.writePop("pointer", 0);             // Set `this` to the new object's base address
        } else if (subroutineType.equals("method")) {
            vmWriter.writePush("argument", 0);           // Load the base address of the object
            vmWriter.writePop("pointer", 0);
        }

        // Parse statements inside the subroutine body
        compileStatements();

        matchToken("}");  // Ensure we match the closing brace for the subroutine body
        indentLevel--;
        addOutputLine("</subroutineBody>");
    }


    private void compileVarDec() {
        addOutputLine("<varDec>");
        indentLevel++;
        matchToken("var");              // matches "var" keyword

        String type = stripToken(currentToken());
        matchToken("type");             // matches the type

        String name = stripToken(currentToken());
        matchToken("varName");

        symbolTable.define(name, type, "var");

        // Handle additional variable names separated by commas
        while (currentTokenMatches(",")) {
            matchToken(",");
            name = stripToken(currentToken());
            matchToken("varName");
            symbolTable.define(name, type, "var");
        }

        matchToken(";");                // end of var declaration
        indentLevel--;
        addOutputLine("</varDec>");
    }


    private void compileStatements() {
        addOutputLine("<statements>");
        indentLevel++;

        while (true) {
            String tokenContent = stripToken(currentToken());
            switch (tokenContent) {
                case "let":
                    compileLetStatement();
                    break;
                case "if":
                    compileIfStatement();
                    break;
                case "while":
                    compileWhileStatement();
                    break;
                case "do":
                    compileDoStatement();
                    break;
                case "return":
                    compileReturnStatement();
                    break;
                default:
                    // Exit statements parsing as soon as we hit a non-statement token
                    indentLevel--;
                    addOutputLine("</statements>");
                    return;
            }
        }
    }


    private void popVariable(String name) {
        String kind = symbolTable.kindOf(name);
        if (kind == null) {
            throw new IllegalStateException("Variable '" + name + "' is not defined.");
        }
        int index = symbolTable.indexOf(name);

        // Map the variable kind to a VM segment
        String segment = mapSegment(kind);
        vmWriter.writePop(segment, index);  // Use VMWriter to generate the pop command
    }

    private String mapSegment(String kind) {
        switch (kind) {
            case "static":
                return "static";
            case "field":
                return "this";
            case "var":
                return "local";
            case "argument":
                return "argument";
            default:
                throw new IllegalStateException("Unknown variable kind: " + kind);
        }
    }

    private void pushVariable(String name) {
        String kind = symbolTable.kindOf(name);
        if (kind == null) {
            throw new IllegalStateException("Variable '" + name + "' is not defined.");
        }
        int index = symbolTable.indexOf(name);

        // Map the variable kind to a VM segment
        String segment = mapSegment(kind);
        vmWriter.writePush(segment, index);  // Use VMWriter to generate the push command
    }

    private String generateLabel(String base) {
        return className + "_" + base + "_" + (labelCounter++);
    }


    private void compileIfStatement() {
        addOutputLine("<ifStatement>");
        indentLevel++;

        matchToken("if");
        matchToken("(");
        compileExpression();  // Evaluate the condition
        matchToken(")");

        String trueLabel = generateLabel("IF_TRUE");
        String falseLabel = generateLabel("IF_FALSE");
        String endLabel = generateLabel("IF_END");

        // VM code to evaluate the condition and jump if false
        vmWriter.writeIf(trueLabel);
        vmWriter.writeGoto(falseLabel);
        vmWriter.writeLabel(trueLabel);

        // Compile the true block
        matchToken("{");
        compileStatements();
        matchToken("}");

        // Optional else block
        if (currentTokenMatches("else")) {
            vmWriter.writeGoto(endLabel);  // Jump to the end of the if-else structure
            vmWriter.writeLabel(falseLabel);

            matchToken("else");
            matchToken("{");
            compileStatements();
            matchToken("}");

            vmWriter.writeLabel(endLabel);
        } else {
            vmWriter.writeLabel(falseLabel);  // If there's no else, just jump here
        }

        indentLevel--;
        addOutputLine("</ifStatement>");
    }


    private void compileWhileStatement() {
        addOutputLine("<whileStatement>");
        indentLevel++;

        matchToken("while");

        String startLabel = generateLabel("WHILE_EXP");
        String endLabel = generateLabel("WHILE_END");

        vmWriter.writeLabel(startLabel);  // Loop start label

        matchToken("(");
        compileExpression();  // Evaluate the condition
        matchToken(")");

        vmWriter.writeArithmetic("not");  // Negate the condition for VM logic
        vmWriter.writeIf(endLabel);       // Jump to the end if the condition is false

        // Compile the loop body
        matchToken("{");
        compileStatements();
        matchToken("}");

        vmWriter.writeGoto(startLabel);  // Jump back to the start
        vmWriter.writeLabel(endLabel);  // End label for the loop

        indentLevel--;
        addOutputLine("</whileStatement>");
    }


    private void compileDoStatement() {
        addOutputLine("<doStatement>");
        indentLevel++;

        matchToken("do");
        compileSubroutineCall();  // Handle subroutine call
        matchToken(";");

        // Discard the return value of the call
        vmWriter.writePop("temp", 0);

        indentLevel--;
        addOutputLine("</doStatement>");
    }

    private void compileReturnStatement() {
        addOutputLine("<returnStatement>");
        indentLevel++;

        matchToken("return");

        // Handle return value
        if (!Objects.equals(stripToken(currentToken()), ";")) {
            if (stripToken(currentToken()).equals("this")) {
                // Handle `this` explicitly for constructors
                matchToken("keywordConstant");
                vmWriter.writePush("pointer", 0);  // Push `this` (pointer 0) onto the stack
            } else {
                compileExpression();  // Evaluate the return expression
            }
        } else {
            // Return void: push a dummy value
            vmWriter.writePush("constant", 0);
        }

        vmWriter.writeReturn();  // Generate return VM command
        matchToken(";");

        indentLevel--;
        addOutputLine("</returnStatement>");
    }


    private void compileSubroutineCall() {
        String identifier = stripToken(currentToken());
        matchToken("varName");  // Store the initial identifier

        String subroutineName;
        String fullName;
        int nArgs = 0;

        if (currentTokenMatches(".")) {
            matchToken(".");
            subroutineName = stripToken(currentToken());
            matchToken("subroutineName");

            if (symbolTable.kindOf(identifier) != null) {
                // It's a method call on an object instance
                String objType = symbolTable.typeOf(identifier);
                fullName = objType + "." + subroutineName;
                nArgs = 1;  // 'this' reference
                pushVariable(identifier);  // Push the object reference
            } else {
                // It's a function call or a static method call
                fullName = identifier + "." + subroutineName;
            }
        } else {
            // Subroutine call within the same class
            subroutineName = identifier;
            fullName = className + "." + subroutineName;
            nArgs = 1;  // 'this' reference
            vmWriter.writePush("pointer", 0);  // Push 'this' onto the stack
        }

        matchToken("(");
        nArgs += compileExpressionList();  // Compile arguments and get count
        matchToken(")");

        // Generate the VM call command
        vmWriter.writeCall(fullName, nArgs);
    }

    private void compileLetStatement() {
        addOutputLine("<letStatement>");
        indentLevel++;

        matchToken("let");
        String varName = stripToken(currentToken());
        matchToken("varName");

        boolean isArray = false;

        if (currentTokenMatches("[")) {
            isArray = true;
            matchToken("[");
            compileExpression();  // Parse the array index expression
            matchToken("]");

            // Compute the base address for array access
            pushVariable(varName);  // Push the base address of the array
            vmWriter.writeArithmetic("add");  // Add the index to the base address
        }

        matchToken("=");
        compileExpression();  // Evaluate the right-hand side expression
        matchToken(";");  // End of the let statement

        if (isArray) {
            // Array assignment
            vmWriter.writePop("temp", 0);      // Temporarily store the value
            vmWriter.writePop("pointer", 1);  // Set THAT to base + index
            vmWriter.writePush("temp", 0);    // Retrieve the value
            vmWriter.writePop("that", 0);     // Assign to the array element
        } else {
            // Regular variable assignment
            popVariable(varName);  // Assign the value to the variable
        }

        indentLevel--;
        addOutputLine("</letStatement>");
    }

    private void compileExpression() {
        addOutputLine("<expression>");
        indentLevel++;

        // Compile the first term
        compileTerm();

        // Handle additional terms and operators
        while (isOperator(currentToken())) {
            String operator = stripToken(currentToken());
            matchToken(operator);
            compileTerm();

            // Write the VM command for the operator
            vmWriter.writeArithmetic(getVmOperator(operator));
        }

        indentLevel--;
        addOutputLine("</expression>");
    }

    private void compileTerm() {
        addOutputLine("<term>");
        indentLevel++;

        if (isIntegerConstant(currentToken())) {
            // Integer constant
            String value = stripToken(currentToken());
            matchToken("integerConstant");
            vmWriter.writePush("constant", Integer.parseInt(value));

        } else if (isStringConstant(currentToken())) {
            // String constant
            String value = stripToken(currentToken());
            matchToken("stringConstant");

            // Create a new string in VM
            vmWriter.writePush("constant", value.length());
            vmWriter.writeCall("String.new", 1);

            // Append each character to the string
            for (char c : value.toCharArray()) {
                vmWriter.writePush("constant", (int) c);
                vmWriter.writeCall("String.appendChar", 2);
            }

        } else if (isKeywordConstant(currentToken())) {
            // Keyword constant
            String keyword = stripToken(currentToken());
            matchToken("keywordConstant");
            switch (keyword) {
                case "true":
                    vmWriter.writePush("constant", 0);
                    vmWriter.writeArithmetic("not");
                    break;
                case "false":
                case "null":
                    vmWriter.writePush("constant", 0);
                    break;
                case "this":
                    vmWriter.writePush("pointer", 0);
                    break;
            }

        } else if (currentTokenMatches("(")) {
            // Parenthesized expression
            matchToken("(");
            compileExpression();
            matchToken(")");

        } else if (isUnaryOperator(currentToken())) {
            // Unary operation
            String operator = stripToken(currentToken());
            matchToken(operator);
            compileTerm();  // Compile the term following the unary operator

            if (operator.equals("-")) {
                vmWriter.writeArithmetic("neg");
            } else if (operator.equals("~")) {
                vmWriter.writeArithmetic("not");
            }

        } else if (isIdentifier(currentToken())) {
            // Variable, array access, or subroutine call
            String name = stripToken(currentToken());
            if (nextTokenMatches("(") || nextTokenMatches(".")) {
                compileSubroutineCall();  // Subroutine call
            } else {
                matchToken("varName");

                if (currentTokenMatches("[")) {
                    // Array access
                    matchToken("[");
                    compileExpression();  // Compute the index
                    matchToken("]");

                    pushVariable(name);  // Push base address
                    vmWriter.writeArithmetic("add");  // Base + index
                    vmWriter.writePop("pointer", 1);  // Set THAT to base + index
                    vmWriter.writePush("that", 0);  // Push the value of the array element
                } else {
                    // Regular variable
                    pushVariable(name);
                }
            }
        } else {
            throw new IllegalStateException("Syntax error in term: unexpected token " + stripToken(currentToken()));
        }

        indentLevel--;
        addOutputLine("</term>");
    }

    private int compileExpressionList() {
        addOutputLine("<expressionList>");
        indentLevel++;

        int nArgs = 0;

        if (!stripToken(currentToken()).equals(")")) {
            compileExpression();  // Compile the first expression
            nArgs++;

            // Handle additional arguments separated by commas
            while (currentTokenMatches(",")) {
                matchToken(",");
                compileExpression();
                nArgs++;
            }
        }

        indentLevel--;
        addOutputLine("</expressionList>");
        return nArgs;  // Return the number of arguments
    }


    private void matchToken(String expected) {
        String tokenContent = stripToken(currentToken());
        if (tokenContent == null) {
            throw new IllegalStateException("Syntax error: Expected " + expected + ", but current token is null.");
        }
        switch (expected) {
            case "className":
                if (!isIdentifier(currentToken())) {
                    throw new IllegalStateException("Syntax error: Expected class name, found " + tokenContent);
                }
                className = tokenContent;
                break;
            case "varName":
                if (!isIdentifier(currentToken())) {
                    throw new IllegalStateException("Syntax error: Expected variable name, found " + tokenContent);
                }
                // Store variable name if needed
                break;
            case "subroutineName":
                // Expected an identifier
                if (!isIdentifier(currentToken())) {
                    throw new IllegalStateException(
                            "Syntax error: Expected an identifier for " + expected + ", found " + tokenContent);
                }
                break;

            case "type":
                // Expected a type (int, char, boolean, or class name)
                if (!isType(currentToken())) {
                    throw new IllegalStateException(
                            "Syntax error: Expected a type, found " + tokenContent);
                }
                break;

            case "integerConstant":
                // Expected an integer constant
                if (!isIntegerConstant(currentToken())) {
                    throw new IllegalStateException(
                            "Syntax error: Expected an integer constant, found " + tokenContent);
                }
                break;

            case "stringConstant":
                // Expected a string constant
                if (!isStringConstant(currentToken())) {
                    throw new IllegalStateException(
                            "Syntax error: Expected a string constant, found " + tokenContent);
                }
                break;

            case "keywordConstant":
                // Expected a keyword constant
                if (!isKeywordConstant(currentToken())) {
                    throw new IllegalStateException(
                            "Syntax error: Expected a keyword constant, found " + tokenContent);
                }
                break;

            default:
                // For any other expected value, check if tokenContent matches expected
                if (!tokenContent.equals(expected)) {
                    throw new IllegalStateException(
                            "Syntax error: Expected " + expected + ", found " + tokenContent);
                }
                break;
        }

        addOutputLine(currentToken());
        advance();
    }

    private String stripToken(String token) {
        if (token == null) {
            return null;
        }
        String content = token.replaceAll("<[^>]*>", "").trim();  // Removes XML tags and trims whitespace
        return unescapeXml(content);  // Unescape XML entities
    }


    private void addOutputLine(String line) {
        String indent = repeat(indentLevel);  // Two spaces per indentation level
        xmlOutput.add(indent + line);
    }

    private String repeat(int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }


    private String unescapeXml(String str) {
        return str.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
    }


    private String getVmOperator(String operator) {
        switch (operator) {
            case "+":
                return "add";
            case "-":
                return "sub";
            case "*":
                return "call Math.multiply 2";
            case "/":
                return "call Math.divide 2";
            case "&":
            case "&amp;":
                return "and";
            case "|":
                return "or";
            case "<":
            case "&lt;":
                return "lt";
            case ">":
            case "&gt;":
                return "gt";
            case "=":
                return "eq";
            default:
                throw new IllegalStateException("Unknown operator: " + operator);
        }
    }


    private boolean currentTokenMatches(String expected) {
        String token = currentToken();
        if (token == null) {
            return false;
        }

        switch (expected) {
            case "class":
            case "static":
            case "field":
            case "constructor":
            case "function":
            case "method":
            case "var":
            case "let":
            case "if":
            case "while":
            case "do":
            case "return":
                String tokenContent = stripToken(token);
                return tokenContent.equals(expected);
            case "type":
                return isType(token);
            case "varName":
            case "subroutineName":
                return isIdentifier(token);
            case "integerConstant":
                return isIntegerConstant(token);
            case "stringConstant":
                return isStringConstant(token);
            case "keywordConstant":
                return isKeywordConstant(token);
            case "op":
                return isOperator(token);
            case "unaryOp":
                return isUnaryOperator(token);
            default:
                // Handles symbols like '{', '}', etc.
                tokenContent = stripToken(token);
                return tokenContent.equals(expected);
        }
    }

    private boolean nextTokenMatches(String expected) {
        int nextIndex = currentIndex + 1;
        if (nextIndex >= tokens.size()) {
            return false;
        }
        String tokenContent = stripToken(tokens.get(nextIndex));

        switch (expected) {
            case "type":
                return isType(tokens.get(nextIndex));
            case "varName":
            case "subroutineName":
                return isIdentifier(tokens.get(nextIndex));
            case "integerConstant":
                return isIntegerConstant(tokens.get(nextIndex));
            case "stringConstant":
                return isStringConstant(tokens.get(nextIndex));
            case "keywordConstant":
                return isKeywordConstant(tokens.get(nextIndex));
            case "op":
                return isOperator(tokens.get(nextIndex));
            case "unaryOp":
                return isUnaryOperator(tokens.get(nextIndex));
            default:
                return tokenContent.equals(expected);
        }
    }


    private boolean isType(String token) {
        JackTokens tokenType = JackTokens.getTokenType(token);
        if (tokenType == JackTokens.KEYWORD) {
            String tokenContent = stripToken(token);
            return tokenContent.equals("int") || tokenContent.equals("char") ||
                    tokenContent.equals("boolean") || tokenContent.equals("void");
        } else return tokenType == JackTokens.IDENTIFIER; // Class name
    }

    private boolean isIdentifier(String token) {
        JackTokens tokenType = JackTokens.getTokenType(token);
        return tokenType == JackTokens.IDENTIFIER;
    }


    private boolean isIntegerConstant(String token) {
        JackTokens tokenType = JackTokens.getTokenType(token);
        return tokenType == JackTokens.INTEGER_CONSTANT;
    }


    private boolean isStringConstant(String token) {
        JackTokens tokenType = JackTokens.getTokenType(token);
        return tokenType == JackTokens.STRING_CONSTANT;
    }


    private boolean isKeywordConstant(String token) {
        // Extract the raw token content
        String tokenContent = stripToken(token);
        return "true".equals(tokenContent) || "false".equals(tokenContent) ||
                "null".equals(tokenContent) || "this".equals(tokenContent);
    }


    private boolean isOperator(String token) {
        return OPERATORS.contains(stripToken(token));
    }

    private boolean isUnaryOperator(String token) {
        String tokenContent = stripToken(token);
        return "-~".contains(tokenContent);
    }


    private void advance() {
        currentIndex++;
    }

    private String currentToken() {
        if (currentIndex >= tokens.size()) {
            return null;
        }
        return tokens.get(currentIndex);
    }

}
