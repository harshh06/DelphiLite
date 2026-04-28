package compiler;

/** Maps a Pascal variable to an LLVM pointer (global {@code @} or stack {@code %}). */
public final class VarBinding {
    public final String llvmType;
    /** e.g. {@code @x} or {@code %loc_2} */
    public final String ptrName;
    public final boolean global;

    public VarBinding(String llvmType, String ptrName, boolean global) {
        this.llvmType = llvmType;
        this.ptrName = ptrName;
        this.global = global;
    }
}
