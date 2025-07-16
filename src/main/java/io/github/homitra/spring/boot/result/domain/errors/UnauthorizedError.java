package io.github.homitra.spring.boot.result.domain.errors;

/**
 * Error indicating unauthorized access or insufficient permissions.
 * 
 * <p>Used when authentication fails or user lacks required permissions.
 * Maps to HTTP 401 UNAUTHORIZED status.</p>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 1.0.0
 */
public final class UnauthorizedError extends Error {
    /**
     * Constructs an UnauthorizedError with the specified message.
     * 
     * @param message authorization error description
     */
    public UnauthorizedError(String message) {
        super(message);
    }
}
