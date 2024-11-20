import java.util.ArrayList;
import java.util.List;

public class VMWriter {
    private final List<String> vmCode;

    public VMWriter() {
        vmCode = new ArrayList<>();
    }

    public List<String> getVmCode() {
        return this.vmCode;
    }

    public void writePush(String segment, int index) {
        vmCode.add("push " + segment + " " + index);
    }

    public void writePop(String segment, int index) {
        vmCode.add("pop " + segment + " " + index);
    }

    public void writeArithmetic(String command) {
        vmCode.add(command);
    }

    public void writeLabel(String label) {
        vmCode.add("label " + label);
    }

    public void writeGoto(String label) {
        vmCode.add("goto " + label);
    }

    public void writeIf(String label) {
        vmCode.add("if-goto " + label);
    }

    public void writeCall(String name, int nArgs) {
        vmCode.add("call " + name + " " + nArgs);
    }

    public void writeFunction(String name, int nLocals) {
        vmCode.add("function " + name + " " + nLocals);
    }

    public void writeReturn() {
        vmCode.add("return");
    }

    public List<String> getVMCode() {
        return vmCode;
    }

    public void clear() {
        vmCode.clear();
    }
}
