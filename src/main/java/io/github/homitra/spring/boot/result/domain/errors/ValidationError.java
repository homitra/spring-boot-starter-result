package io.github.homitra.spring.boot.result.domain.errors;

/**
 * Error indicating validation failure of input data.
 * 
 * <p>Used when input validation fails, such as missing required fields,
 * invalid formats, or business rule violations. Maps to HTTP 400 BAD_REQUEST.</p>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.1
 */
public final class ValidationError extends Error {
    
    /**
     * Constructs a ValidationError with the specified message.
     * 
     * @param message validation error description
     */
    public ValidationError(String message) {
        super(message);
    }
}
