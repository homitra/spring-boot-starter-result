package io.github.homitra.spring.boot.result.infrastructure.handlers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import io.github.homitra.spring.boot.result.ResponseWrapper;
import io.github.homitra.spring.boot.result.Result;
import io.github.homitra.spring.boot.result.api.ResponseUtils;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Global exception handler that converts common exceptions to Result-based responses.
 * 
 * <p>This {@code @ControllerAdvice} automatically catches exceptions thrown by
 * Spring MVC controllers and converts them to standardized Result responses
 * with appropriate HTTP status codes.</p>
 * 
 * <p>Handled exceptions:</p>
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} → ValidationError (400)</li>
 *   <li>{@link DataIntegrityViolationException} → EntityAlreadyExistsError (409)</li>
 *   <li>{@link Exception} → Generic Error (500)</li>
 * </ul>
 * 
 * <p>This provides consistent error responses across the application without
 * requiring manual exception handling in each controller.</p>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.1
 */
@ControllerAdvice
public final class GlobalResultExceptionHandler {

    /**
     * Handles validation errors from {@code @Valid} annotations.
     * 
     * <p>Extracts the first validation error message and returns it as a
     * ValidationError with HTTP 400 BAD_REQUEST status.</p>
     * 
     * @param e the validation exception
     * @return ResponseEntity with ValidationError and 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        Result<Void> result = Result.validationError(message);
        return ResponseUtils.asResponse(result);
    }

    /**
     * Handles database constraint violations.
     * 
     * <p>Converts database integrity violations (like unique constraint failures)
     * to EntityAlreadyExistsError with HTTP 409 CONFLICT status.</p>
     * 
     * @param e the data integrity violation exception
     * @return ResponseEntity with EntityAlreadyExistsError and 409 status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        Result<Void> result = Result.entityAlreadyExistsError("Resource already exists");
        return ResponseUtils.asResponse(result);
    }

    /**
     * Handles all other uncaught exceptions.
     * 
     * <p>Catches any exception not handled by more specific handlers and
     * returns a generic error with HTTP 500 INTERNAL_SERVER_ERROR status.</p>
     * 
     * @param e the uncaught exception
     * @return ResponseEntity with generic Error and 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handleGeneral(Exception e) {
        Result<Void> result = Result.failure(e.getMessage());
        return ResponseUtils.asResponse(result);
    }
}
