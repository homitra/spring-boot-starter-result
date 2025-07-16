package io.github.homitra.spring.boot.result.internal;

/**
 * Internal interface for operations that can trigger transaction rollbacks.
 * 
 * <p>This interface is implemented by Result classes to provide information
 * about whether a transaction should be rolled back based on the operation outcome.</p>
 * 
 * <p>Used internally by the {@code @RollbackOnFailure} annotation aspect
 * to determine rollback behavior.</p>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 1.0.0
 */
public interface TransactionalOperation {
    /**
     * Determines if the current transaction should be rolled back.
     * 
     * @return true if transaction should rollback, false otherwise
     */
    Boolean shouldRollback();
}
