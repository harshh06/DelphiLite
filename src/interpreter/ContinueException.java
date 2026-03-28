package interpreter;

/**
 * Thrown when a 'continue' statement is encountered inside a loop.
 * Uses suppressed stack traces for performance (it's a control-flow signal, not an error).
 */
public class ContinueException extends RuntimeException {
    public ContinueException() {
        super(null, null, true, false); // disable stack trace
    }
}
