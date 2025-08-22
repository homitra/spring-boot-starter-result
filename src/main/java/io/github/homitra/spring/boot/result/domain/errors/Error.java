package io.github.homitra.spring.boot.result.domain.errors;

/**
 * Base class for all error types in the Result pattern.
 * 
 * <p>This class represents a generic error with a descriptive message.
 * Specific error types extend this class to provide more context about
 * the type of error that occurred.</p>
 * 
 * <p>Used by Result objects to carry error information when operations fail.</p>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.1
 */
public class Error {
    private String message;

    /**
     * Constructs an Error with the specified message.
     * 
     * @param message descriptive error message
     */
    public Error(String message) {
        this.message = message;
    }

    /**
     * Gets the error message.
     * 
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

}
