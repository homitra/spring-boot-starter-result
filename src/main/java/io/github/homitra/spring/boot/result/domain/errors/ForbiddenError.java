package io.github.homitra.spring.boot.result.domain.errors;

/**
 * Error type representing a forbidden access condition.
 * 
 * <p>This error is used when a user is authenticated but lacks permission
 * to access a resource, typically resulting in a 403 HTTP status code.</p>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.5
 */
public final class ForbiddenError extends Error {
    
    /**
     * Creates a new ForbiddenError with the specified message.
     * 
     * @param message the error message
     */
    public ForbiddenError(String message) {
        super(message);
    }
}