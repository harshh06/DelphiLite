package compiler;

/** Typed SSA value or constant for expression codegen (subset uses i32 only). */
public final class LLVMValue {
    public static final String I32 = "i32";

    public final String llvmType;
    /**
     * Operand text for use after the type in LLVM instructions: an immediate ({@code 7})
     * or a register ({@code %t3}).
     */
    public final String operand;

    public LLVMValue(String llvmType, String operand) {
        this.llvmType = llvmType;
        this.operand = operand;
    }

    public static LLVMValue i32Const(int v) {
        return new LLVMValue(I32, String.valueOf(v));
    }
}
