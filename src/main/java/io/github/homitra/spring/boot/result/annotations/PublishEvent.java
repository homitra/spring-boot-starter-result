package io.github.homitra.spring.boot.result.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to automatically publish events based on Result outcomes.
 * 
 * <p>When applied to a method that returns a Result, this annotation will automatically
 * publish application events based on the success or failure of the operation.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @PublishEvent(on = PublishEvent.EventType.SUCCESS, eventName = "user-created")
 * public Result<User> createUser(CreateUserRequest request) {
 *     // Event published automatically on success
 *     return Result.success(userRepository.save(user));
 * }
 * }</pre>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublishEvent {
    /**
     * Specifies when to publish the event.
     * 
     * @return the event type condition
     */
    EventType on() default EventType.SUCCESS;
    
    /**
     * Custom name for the published event.
     * 
     * @return the event name, defaults to method name if empty
     */
    String eventName() default "";
    
    /**
     * Enumeration of event publishing conditions.
     */
    enum EventType {
        /** Publish event only on successful Results */
        SUCCESS, 
        /** Publish event only on failed Results */
        FAILURE, 
        /** Publish event on both successful and failed Results */
        BOTH
    }
}
