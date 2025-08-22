package io.github.homitra.spring.boot.result;

import io.github.homitra.spring.boot.result.domain.errors.Error;

/**
 * Base class for Result pattern implementation using sealed classes.
 * 
 * <p>This sealed class ensures type safety and controlled inheritance,
 * allowing only the Result class to extend it. It provides the fundamental
 * success/failure state and error information.</p>
 * 
 * <p>The sealed class pattern prevents external classes from extending
 * this base, maintaining the integrity of the Result pattern.</p>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.1
 */
sealed class ResultBase permits Result {
    private boolean success;
    private Error error;

    /**
     * Constructs a ResultBase with success status and optional error.
     * 
     * @param success whether the operation was successful
     * @param error the error information, null for successful results
     */
    public ResultBase(boolean success, Error error) {
        this.success = success;
        this.error = error;
    }

    /**
     * Checks if the result represents a successful operation.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the error information for failed results.
     * 
     * @return the error object, null for successful results
     */
    public Error getError() {
        return error;
    }
}
