package io.github.homitra.spring.boot.result.infrastructure.config;

import java.util.ServiceLoader;

/**
 * Provider class for accessing {@link ResultConstants} implementations.
 * 
 * <p>This class uses a singleton pattern to provide access to ResultConstants
 * throughout the application. It attempts to load custom implementations via
 * Spring context first, then falls back to ServiceLoader, and finally uses
 * the default implementation.</p>
 * 
 * <p>Loading priority:</p>
 * <ol>
 *   <li>Spring Bean (if Spring context is available)</li>
 *   <li>ServiceLoader mechanism</li>
 *   <li>Default implementation</li>
 * </ol>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 1.0.0
 */
public final class ResultConstantsProvider {
    private static ResultConstants INSTANCE;

    static {
        INSTANCE = loadResultConstants();
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private ResultConstantsProvider() {
    }

    /**
     * Loads ResultConstants implementation using multiple strategies.
     * 
     * @return ResultConstants instance
     */
    private static ResultConstants loadResultConstants() {
        // First, try loading via Spring if running in a Spring Boot app
        ResultConstants springBean = getSpringBeanIfAvailable();
        if (springBean != null) {
            return springBean;
        }

        // Otherwise, fallback to normal ServiceLoader behavior
        ServiceLoader<ResultConstants> loader = ServiceLoader.load(ResultConstants.class);
        return loader.findFirst().orElse(new DefaultResultConstants());
    }

    /**
     * Gets the current ResultConstants instance.
     * 
     * @return the ResultConstants instance
     */
    public static ResultConstants getResultConstants() {
        return INSTANCE;
    }

    /**
     * Manually sets the ResultConstants instance.
     * 
     * @param constants the ResultConstants implementation to use
     * @throws IllegalArgumentException if constants is null
     */
    public static void setResultConstants(ResultConstants constants) {
        if (constants == null) {
            throw new IllegalArgumentException("ResultConstants instance cannot be null");
        }
        INSTANCE = constants;
    }

    /**
     * Attempts to load ResultConstants from Spring context if available.
     * 
     * @return ResultConstants from Spring context, or null if not available
     */
    private static ResultConstants getSpringBeanIfAvailable() {
        try {
            // Check if Spring is available by looking for the ApplicationContext class
            Class<?> springContextClass = Class.forName("org.springframework.context.ApplicationContext");
            Object context = springContextClass.getMethod("getBean", String.class).invoke(null, "resultConstants");

            if (context != null) {
                return (ResultConstants) context;
            }
        } catch (Exception e) {
            // Ignore exceptions if Spring context isn't available
        }
        return null;
    }
}
