package ast;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Breaks an input string into tokens.
 */
public class Tokenizer {
    /**
     * Tokens that will be broken out even when part of a longer "word". It's
     * important that the ones that are two characters long come before single
     * characters that may match them.
     */
    private static final List<String> specialTokens = List.of("(", ")", "{", "}", ";", "<=", ">=", "==", "!=", "<", ">",
        "+", "-", "*", "/", "=", ",", ".", "[", "]");

    /** Unused tokens. */
    private LinkedList<String> tokens;
    /** We keep the last ten around for debugging purposes. */
    private LinkedList<String> prevTokens;

    public Tokenizer(String code) {
        tokens = tokenize(code);
        prevTokens = new LinkedList<>();
    }

    public Queue<String> getTokens() {
        return tokens;
    }

    /**
     * Checks for a special token at the given index within a larger token.
     *
     * @param token the token
     * @param index the index
     * @return the length of the special, if found, otherwise zero.
     */
    private int containsSpecialAt(String token, int index) {
        nextSpecial:
        for (String special : specialTokens) {
            if (token.length() < index + special.length()) {
                continue;
            }
            for (int i = 0; i < special.length(); i++) {
                if (special.charAt(i) != token.charAt(index + i)) {
                    continue nextSpecial;
                }
            }
            return special.length();
        }
        return 0;
    }

    /**
     * Tokenizes input, and return a list of tokens.
     *
     * @param input the input
     * @return a list of tokens
     */
    private LinkedList<String> tokenize(String input) {
        LinkedList<String> stream = new LinkedList<>();

        String[] tokens = input.split("[\s\n]+");
        for (String token : tokens) {
            int i = 0;
            while (i < token.length()) {
                int specialLen = containsSpecialAt(token, i);
                if (specialLen > 0) {
                    if (i != 0) {
                        stream.add(token.substring(0, i));
                    }
                    stream.add(token.substring(i, i + specialLen));
                    token = token.substring(i + specialLen);
                    i = 0;
                } else {
                    i++;
                }
            }
            if (token.length() > 0) {
                stream.add(token);
            }
        }

        return stream;
    }

    /**
     * Tracks previous tokens for better debugging messages.
     *
     * @param token the latest token
     */
    private void updatePrev(String token) {
        prevTokens.addLast(token);
        if (prevTokens.size() > 10) {
            prevTokens.removeFirst();
        }
    }

    /**
     * Consumes the next token if it's the expected value, or throws an exception.
     *
     * @param expected the expected next token
     */
    public void consume(String expected) {
        String token = tokens.remove();
        if (!token.equals(expected)) {
            String context = String.join(" ", prevTokens) + " _ "
                + String.join(" ", tokens.subList(0, Math.min(10, tokens.size())));
            throw new RuntimeException("Expected " + expected + ", but got " + token + " at " + context);
        }
        updatePrev(token);
    }

    /**
     * Checks the next n tokens to see if they match the supplied tokens. * can be
     * used to match any token.
     *
     * @param expected the expected tokens
     * @return true if the next n tokens match
     */
    public boolean lookahead(String... expected) {
        if (tokens.size() < expected.length) {
            return false;
        }

        for (int i = 0; i < expected.length; i++) {
            if ("*".equals(expected[i])) {
                continue;
            }

            if (!tokens.get(i).equals(expected[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Removes the next token and returns it.
     * 
     * @return the next token
     */
    public String remove() {
        String token = tokens.remove();
        updatePrev(token);
        return token;
    }

    /**
     * Returns the next token without removing it. You should usally be using
     * lookahead() instead.
     *
     * @return the next token
     */
    public String peek() {
        return tokens.peek();
    }
}
