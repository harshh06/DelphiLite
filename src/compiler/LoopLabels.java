package compiler;

/** break / continue targets for the innermost loop. */
public final class LoopLabels {
    public final String breakLabel;
    public final String continueLabel;

    public LoopLabels(String breakLabel, String continueLabel) {
        this.breakLabel = breakLabel;
        this.continueLabel = continueLabel;
    }
}
