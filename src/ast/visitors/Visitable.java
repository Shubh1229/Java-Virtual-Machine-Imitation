package ast.visitors;

/**
 * Interface for objects that can be visited.
 */
public interface Visitable {
    /**
     * Accepts a Visitor.
     *
     * @param v the visitor
     */
    void accept(Visitor v);
}
