import java.util.Set;

public enum JackTokens {
    KEYWORD(Set.of("class", "constructor", "function", "method", "field", "static", "var",
            "int", "char", "boolean", "void", "true", "false", "null", "this", "let",
            "do", "if", "else", "while", "return")),

    SYMBOL(Set.of("{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|", "<", ">", "=", "~")),

    IDENTIFIER(Set.of()),
    INTEGER_CONSTANT(Set.of()),
    STRING_CONSTANT(Set.of());

    private final Set<String> values;

    // Constructor to assign specific values for KEYWORD and SYMBOL
    JackTokens(Set<String> values) {
        this.values = values;
    }


    // Static method to get the token type based on input
    public static JackTokens getTokenType(String token) {
        if (token == null) {
            return null;
        }
        token = token.trim();
        if (token.startsWith("<") && token.contains(">")) {
            int start = token.indexOf('<') + 1;
            int end = token.indexOf('>');
            String tokenTypeStr = token.substring(start, end);

            switch (tokenTypeStr) {
                case "keyword":
                    return KEYWORD;
                case "symbol":
                    return SYMBOL;
                case "integerConstant":
                    return INTEGER_CONSTANT;
                case "stringConstant":
                    return STRING_CONSTANT;
                case "identifier":
                    return IDENTIFIER;
                default:
                    return null; // Unknown token type
            }
        } else {
            return null;
        }
    }

    public static String escapeSymbol(char symbol) {
        switch (symbol) {
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '&':
                return "&amp;";
            case '\"':
                return "&quot;";
            default:
                return Character.toString(symbol);
        }
    }
}
