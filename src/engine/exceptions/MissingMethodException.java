package engine.exceptions;

/**
 * MissingMethodException is thrown when a method is called that doesn't exist.
 */
public class MissingMethodException extends RuntimeException {
    public MissingMethodException(String msg) {
        super(msg);
    }
}
