package io.github.homitra.spring.boot.result.domain.errors;

/**
 * Error indicating that a requested entity was not found.
 * 
 * <p>This error is typically used when database queries or lookups
 * fail to find the requested resource. When used with ResponseUtils,
 * it automatically maps to HTTP 404 NOT_FOUND status.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * return userRepository.findById(id)
 *     .map(Result::success)
 *     .orElse(Result.entityNotFoundError("User not found with id: " + id));
 * }</pre>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 1.0.0
 */
public final class EntityNotFoundError extends Error {
    /**
     * Constructs an EntityNotFoundError with the specified message.
     * 
     * @param message descriptive message about what entity was not found
     */
    public EntityNotFoundError(String message) {
        super(message);
    }
}
