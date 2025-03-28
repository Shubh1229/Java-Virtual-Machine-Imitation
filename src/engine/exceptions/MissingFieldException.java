package engine.exceptions;

/**
 * MissingFieldException is thrown when an attempt is made to read a field that
 * doesn't exist.
 */
public class MissingFieldException extends RuntimeException {
    public MissingFieldException(String msg) {
        super(msg);
    }
}
