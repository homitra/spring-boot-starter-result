package io.github.homitra.spring.boot.result.infrastructure.config;

/**
 * Default implementation of {@link ResultConstants} providing standard English messages.
 * 
 * <p>This class provides the default success and error messages used throughout
 * the Result pattern implementation. Applications can override these by providing
 * their own implementation of {@link ResultConstants}.</p>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.1
 */
public final class DefaultResultConstants implements ResultConstants {

    /**
     * Returns the default success message.
     * 
     * @return "Operation completed successfully."
     */
    @Override
    public String getSuccessMessage() {
        return "Operation completed successfully.";
    }

    /**
     * Returns a formatted error message with the provided error detail.
     * 
     * @param errorDetail specific error information
     * @return formatted error message: "An error occurred: {errorDetail}"
     */
    @Override
    public String getErrorMessage(String errorDetail) {
        return "An error occurred: "+errorDetail;
    }

}
