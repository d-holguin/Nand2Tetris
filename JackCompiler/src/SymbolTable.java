import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Symbol> classTable;
    private final Map<String, Symbol> subroutineTable;
    private int staticIndex;
    private int fieldIndex;
    private int varIndex;
    private int argumentIndex;

    // Symbol class to represent an identifier
    static class Symbol {
        String type;
        String kind;
        int index;

        Symbol(String type, String kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }
    }

    // Constructor
    public SymbolTable() {
        classTable = new HashMap<>();
        subroutineTable = new HashMap<>();
        resetIndices();
    }

    // Reset subroutine-level symbol table
    public void startSubroutine() {
        subroutineTable.clear();
        varIndex = 0;
        argumentIndex = 0;
    }

    // Define a new identifier
    public void define(String name, String type, String kind) {
        Symbol symbol;
        if (kind.equals("static")) {
            symbol = new Symbol(type, kind, staticIndex++);
            classTable.put(name, symbol);
        } else if (kind.equals("field")) {
            symbol = new Symbol(type, kind, fieldIndex++);
            classTable.put(name, symbol);
        } else if (kind.equals("var")) {
            symbol = new Symbol(type, kind, varIndex++);
            subroutineTable.put(name, symbol);
        } else if (kind.equals("argument")) {
            symbol = new Symbol(type, kind, argumentIndex++);
            subroutineTable.put(name, symbol);
        }
    }

    // Count variables of a given kind
    public int varCount(String kind) {
        int count = 0;
        if (kind.equals("static") || kind.equals("field")) {
            count += (int) classTable.values().stream().filter(s -> s.kind.equals(kind)).count();
        } else if (kind.equals("var") || kind.equals("argument")) {
            count += (int) subroutineTable.values().stream().filter(s -> s.kind.equals(kind)).count();
        }
        return count;
    }

    // Return kind of a variable
    public String kindOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).kind;
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).kind;
        }
        return null;  // Not found
    }

    // Return type of a variable
    public String typeOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).type;
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).type;
        }
        return null;  // Not found
    }

    // Return index of a variable
    public int indexOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).index;
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).index;
        }
        return -1;  // Not found
    }

    // Reset indices
    private void resetIndices() {
        staticIndex = 0;
        fieldIndex = 0;
        varIndex = 0;
        argumentIndex = 0;
    }

    @Override
    public String toString() {
        return "Class Table: " + classTable + "\nSubroutine Table: " + subroutineTable;
    }

}
