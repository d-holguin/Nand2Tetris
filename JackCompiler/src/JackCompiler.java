/**
 * Created by d.holguin for nand2tetris course project 10 and 11
 */
public class JackCompiler {

    private static final String USAGE = "Usage: java JackCompiler <input-file-or-directory>";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(USAGE);
            return;
        }

        String fileOrDirPath = args[0];

        try {
            Compiler compiler = new Compiler(fileOrDirPath);
            compiler.compile();
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
