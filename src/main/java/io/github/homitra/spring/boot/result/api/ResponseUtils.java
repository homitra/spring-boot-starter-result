package io.github.homitra.spring.boot.result.api;

import io.github.homitra.spring.boot.result.ResponseWrapper;
import io.github.homitra.spring.boot.result.Result;
import io.github.homitra.spring.boot.result.domain.errors.EntityAlreadyExistsError;
import io.github.homitra.spring.boot.result.domain.errors.EntityNotFoundError;
import io.github.homitra.spring.boot.result.domain.errors.Error;
import io.github.homitra.spring.boot.result.domain.errors.UnauthorizedError;
import io.github.homitra.spring.boot.result.domain.errors.ValidationError;
import io.github.homitra.spring.boot.result.infrastructure.config.ResultConstantsProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for converting Result objects to HTTP ResponseEntity objects.
 * 
 * <p>This class provides methods to automatically convert Result objects to appropriate
 * HTTP responses with correct status codes and response format.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Result<User> result = userService.findById(id);
 * return ResponseUtils.asResponse(result);
 * }</pre>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.1
 */
public final class ResponseUtils {

    public static <T> ResponseEntity<ResponseWrapper<T>> success(T data, String message, HttpStatus status) {
        return new ResponseEntity<>(ResponseWrapper.success(data, message), status);
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> success(T data, HttpStatus status) {
        return new ResponseEntity<>(
                ResponseWrapper.success(data, ResultConstantsProvider.getResultConstants().getSuccessMessage()),
                status);
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> success(T data) {
        return ResponseEntity
                .ok(ResponseWrapper.success(data, ResultConstantsProvider.getResultConstants().getSuccessMessage()));
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> failure(String message, HttpStatus status) {
        return new ResponseEntity<>(ResponseWrapper.failure(message), status);
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> failure(String message) {
        return ResponseEntity.badRequest().body(ResponseWrapper.failure(message));
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> failure() {
        return ResponseEntity.badRequest()
                .body(ResponseWrapper.failure(ResultConstantsProvider.getResultConstants().getSuccessMessage()));
    }

    /**
     * Converts a Result to an appropriate HTTP ResponseEntity.
     * 
     * <p>Automatically maps error types to HTTP status codes:</p>
     * <ul>
     *   <li>EntityNotFoundError → 404 NOT_FOUND</li>
     *   <li>ValidationError → 400 BAD_REQUEST</li>
     *   <li>UnauthorizedError → 401 UNAUTHORIZED</li>
     *   <li>EntityAlreadyExistsError → 409 CONFLICT</li>
     *   <li>Other errors → 500 INTERNAL_SERVER_ERROR</li>
     *   <li>Success → 200 OK</li>
     * </ul>
     * 
     * @param <T> the type of data
     * @param result the Result to convert
     * @return ResponseEntity with appropriate status code and response wrapper
     */
    public static <T> ResponseEntity<ResponseWrapper<T>> asResponse(Result<T> result) {
        if (!result.isSuccess())
            return asError(result.getError());

        return ResponseEntity.ok(ResponseWrapper.success(result.getData(), result.getMessage()));
    }

    public static <T> ResponseEntity<ResponseWrapper<T>> asError(Error exception) {
        return switch (exception) {
            case UnauthorizedError e ->
                new ResponseEntity<>(ResponseWrapper.failure(e.getMessage()), HttpStatus.UNAUTHORIZED);
            case EntityNotFoundError e ->
                new ResponseEntity<>(ResponseWrapper.failure(e.getMessage()), HttpStatus.NOT_FOUND);
            case EntityAlreadyExistsError e ->
                new ResponseEntity<>(ResponseWrapper.failure(e.getMessage()), HttpStatus.CONFLICT);
            case ValidationError e ->
                new ResponseEntity<>(ResponseWrapper.failure(e.getMessage()), HttpStatus.BAD_REQUEST);
            default -> new ResponseEntity<>(ResponseWrapper.failure(exception.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }
}
