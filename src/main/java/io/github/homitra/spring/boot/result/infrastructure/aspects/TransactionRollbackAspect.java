package io.github.homitra.spring.boot.result.infrastructure.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import io.github.homitra.spring.boot.result.internal.TransactionalOperation;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * AOP aspect that automatically rolls back transactions for failed Results.
 * 
 * <p>This aspect intercepts methods annotated with {@code @RollbackOnFailure}
 * and automatically marks the current transaction for rollback when the
 * returned Result indicates failure.</p>
 * 
 * <p>Works by checking if the method result implements {@link TransactionalOperation}
 * and calling {@code shouldRollback()} to determine rollback behavior.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * @RollbackOnFailure
 * public Result<User> createUser(CreateUserRequest request) {
 *     // Transaction automatically rolls back if Result contains error
 *     return Result.success(userRepository.save(user));
 * }
 * }</pre>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 1.0.0
 */
@Aspect
@Component
public final class TransactionRollbackAspect {

    /**
     * Intercepts methods annotated with {@code @RollbackOnFailure} and handles transaction rollback.
     * 
     * <p>If the method returns a Result that indicates failure, the current transaction
     * is marked for rollback using Spring's transaction management.</p>
     * 
     * @param pjp the proceeding join point representing the intercepted method
     * @return the original method result
     * @throws Throwable if the intercepted method throws an exception
     */
    @Around("@annotation(RollbackOnFailure)")
    public Object handleTransactionRollback(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();

        if (result instanceof TransactionalOperation) {
            TransactionalOperation operation = (TransactionalOperation) result;
            if (operation.shouldRollback()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }
        return result;
    }
}
