package io.github.homitra.spring.boot.result.domain.events;

import io.github.homitra.spring.boot.result.Result;
import org.springframework.context.ApplicationEvent;

/**
 * Application event published when Result operations complete.
 * 
 * <p>This event is automatically published by the {@code @PublishEvent} annotation
 * and contains information about the Result, method execution context, and arguments.</p>
 * 
 * <p>Event listeners can subscribe to these events to implement cross-cutting concerns
 * like logging, metrics, notifications, or audit trails.</p>
 * 
 * <p>Example listener:</p>
 * <pre>{@code
 * @EventListener
 * public void handleResultEvent(ResultEvent<?> event) {
 *     if (event.isSuccess()) {
 *         log.info("Operation {} succeeded", event.getEventName());
 *     } else {
 *         log.error("Operation {} failed: {}", event.getEventName(), 
 *                   event.getResult().getError().getMessage());
 *     }
 * }
 * }</pre>
 * 
 * @param <T> the type of data in the Result
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.1
 */
public final class ResultEvent<T> extends ApplicationEvent {
    private final String eventName;
    private final Result<T> result;
    private final String methodName;
    private final Object[] args;

    /**
     * Constructs a ResultEvent with execution context information.
     * 
     * @param source the object that published the event
     * @param eventName custom event name or method name
     * @param result the Result object from method execution
     * @param methodName the name of the method that was executed
     * @param args the arguments passed to the method
     */
    public ResultEvent(Object source, String eventName, Result<T> result, String methodName, Object[] args) {
        super(source);
        this.eventName = eventName;
        this.result = result;
        this.methodName = methodName;
        this.args = args;
    }

    /**
     * Gets the event name.
     * @return custom event name or method name if not specified
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Gets the Result object from the method execution.
     * @return the Result containing success/failure information and data
     */
    public Result<T> getResult() {
        return result;
    }

    /**
     * Gets the name of the method that was executed.
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Gets the arguments passed to the method.
     * @return array of method arguments
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * Convenience method to check if the operation was successful.
     * @return true if the Result indicates success, false otherwise
     */
    public boolean isSuccess() {
        return result.isSuccess();
    }
}
