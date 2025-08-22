package io.github.homitra.spring.boot.result.infrastructure.config;

/**
 * Interface for customizing default messages used in Result objects.
 * 
 * <p>Implement this interface to provide custom success and error messages
 * for your application. The default implementation provides standard English messages.</p>
 * 
 * <p>Example custom implementation:</p>
 * <pre>{@code
 * @Component
 * public class CustomResultConstants implements ResultConstants {
 *     @Override
 *     public String getSuccessMessage() {
 *         return "¡Operación exitosa!";
 *     }
 * }
 * }</pre>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.1
 */
public interface ResultConstants {
    /**
     * Gets the default success message for successful Results.
     * 
     * @return the success message
     */
    String getSuccessMessage();
    
    /**
     * Gets a formatted error message with error details.
     * 
     * @param errorDetail specific error information
     * @return formatted error message
     */
    String getErrorMessage(String errorDetail);
}
