import org.junit.Before;

public abstract class TestBase {
    /** For derived test classes */
    protected int hintCount;
    /** For derived test classes */
    protected String hintContext;

    /**
     * Set up test
     */
    @Before
    public void setUp() {
        hintCount = 0;
        hintContext = "";
    }

    /**
     * Adds context and count to hint.
     *
     * @param partialHint the partial hint
     * @return the hint
     */
    protected String hint(String partialHint) {
        if (hintContext == null || hintContext.equals("")) {
            throw new RuntimeException("No hint context");
        }
        return "Hint: " + hintContext + " assert #" + (++hintCount)
            + " - " + partialHint;
    }
}
