package io.github.homitra.spring.boot.result.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.transaction.annotation.Transactional;

/**
 * Annotation to automatically rollback transactions when a method returns a failed Result.
 * 
 * <p>When applied to a method that returns a Result, this annotation will automatically
 * mark the current transaction for rollback if the Result indicates failure.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @RollbackOnFailure
 * public Result<User> createUser(CreateUserRequest request) {
 *     // Transaction will rollback if Result contains error
 *     return Result.success(userRepository.save(user));
 * }
 * }</pre>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.1
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Transactional
public @interface RollbackOnFailure {
}
