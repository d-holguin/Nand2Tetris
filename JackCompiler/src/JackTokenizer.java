import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {


    // Matches any single character that is a Jack language symbol.
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[{}()\\[\\].,;+\\-*/&|<>=~]");
    // Matches any sequence of digits (0-9) representing an integer constant.
    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");
    // Matches any string constant enclosed in double quotes.
    private static final Pattern STRING_PATTERN = Pattern.compile("\"[^\"]*\"");
    // Matches any valid identifier, which must start with a letter or underscore and can contain letters, digits, and underscores thereafter.
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");


    public JackTokenizer() {
    }

    public static List<String> tokenizePass(List<String> lines) {
        List<String> tokens = new ArrayList<>();

        for (String line : lines) {
            int index = 0;

            while (index < line.length()) {
                char currentChar = line.charAt(index);

                // Skip whitespace
                if (Character.isWhitespace(currentChar)) {
                    index++;
                    continue;
                }

                // Check for symbols using SYMBOL_PATTERN
                if (SYMBOL_PATTERN.matcher(Character.toString(currentChar)).matches()) {
                    tokens.add("<symbol> " + JackTokens.escapeSymbol(currentChar) + " </symbol>");
                    index++;
                    continue;
                }

                // Check for integer constants
                Matcher intMatcher = INTEGER_PATTERN.matcher(line.substring(index));
                if (intMatcher.lookingAt()) {
                    String integerToken = intMatcher.group();
                    tokens.add("<integerConstant> " + integerToken + " </integerConstant>");
                    index += integerToken.length();
                    continue;
                }

                // Check for string constants
                Matcher stringMatcher = STRING_PATTERN.matcher(line.substring(index));
                if (stringMatcher.lookingAt()) {
                    String stringToken = stringMatcher.group();
                    tokens.add("<stringConstant> " + stringToken.substring(1, stringToken.length() - 1) + " </stringConstant>");
                    index += stringToken.length();
                    continue;
                }

                // Check for identifiers or keywords
                Matcher identifierMatcher = IDENTIFIER_PATTERN.matcher(line.substring(index));
                if (identifierMatcher.lookingAt()) {
                    String identifierToken = identifierMatcher.group();
                    JackTokens tokenType = JackTokens.getTokenType(identifierToken);
                    if (tokenType == JackTokens.KEYWORD) {
                        tokens.add("<keyword> " + identifierToken + " </keyword>");
                    } else {
                        tokens.add("<identifier> " + identifierToken + " </identifier>");
                    }
                    index += identifierToken.length();
                    continue;
                }

                // Advance if no match (error handling or unexpected character)
                index++;
            }
        }

        return tokens;
    }
}
