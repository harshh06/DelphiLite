package interpreter;

/**
 * Thrown when a 'break' statement is encountered inside a loop.
 * Uses suppressed stack traces for performance (it's a control-flow signal, not an error).
 */
public class BreakException extends RuntimeException {
    public BreakException() {
        super(null, null, true, false); // disable stack trace
    }
}
