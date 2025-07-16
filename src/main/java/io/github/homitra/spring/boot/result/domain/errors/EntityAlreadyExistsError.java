package io.github.homitra.spring.boot.result.domain.errors;

/**
 * Error indicating an entity already exists when uniqueness is required.
 * 
 * <p>Used when attempting to create resources that already exist,
 * such as duplicate usernames or email addresses. Maps to HTTP 409 CONFLICT.</p>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 1.0.0
 */
public final class EntityAlreadyExistsError extends Error {
    /**
     * Constructs an EntityAlreadyExistsError with the specified message.
     * 
     * @param message description of what entity already exists
     */
    public EntityAlreadyExistsError(String message) {
        super(message);
    }
}
