package compiler;

/** Emits LLVM IR text: per-function SSA temps/labels reset for each function body. */
public final class IRBuilder {
    private final StringBuilder cur = new StringBuilder();
    private int tempCnt;
    private int labelCnt;

    public void resetForFunction() {
        tempCnt = 0;
        labelCnt = 0;
        cur.setLength(0);
    }

    public String takeBody() {
        String s = cur.toString();
        cur.setLength(0);
        tempCnt = 0;
        labelCnt = 0;
        return s;
    }

    public String freshTemp() {
        return "%t" + (++tempCnt);
    }

    public String freshLabel(String prefix) {
        return prefix + "_" + (++labelCnt);
    }

    public void label(String name) {
        cur.append(name).append(":\n");
    }

    public void instr(String line) {
        cur.append("  ").append(line).append("\n");
    }

    /** Append raw text (e.g. whole function prelude) without indent. */
    public void raw(String text) {
        cur.append(text);
    }

    public StringBuilder currentBuffer() {
        return cur;
    }
}
