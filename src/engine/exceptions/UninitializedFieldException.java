package engine.exceptions;

/**
 * UninitializedFieldException is thrown when a field is read before being
 * assigned a value.
 */
public class UninitializedFieldException extends RuntimeException {
    public UninitializedFieldException(String msg) {
        super(msg);
    }
}
