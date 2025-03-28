package engine.exceptions;

/**
 * TypeMismatchException is thrown when an operation can't be completed because
 * the types are incorrect.
 */
public class TypeMismatchException extends RuntimeException {
    public TypeMismatchException(String msg) {
        super(msg);
    }
}
