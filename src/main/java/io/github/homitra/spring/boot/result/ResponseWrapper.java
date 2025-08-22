package io.github.homitra.spring.boot.result;

/**
 * A standardized response wrapper for HTTP API responses.
 * 
 * <p>This record provides a consistent structure for all API responses,
 * containing success status, message, and optional data payload.</p>
 * 
 * <p>Example JSON output:</p>
 * <pre>{@code
 * {
 *   "success": true,
 *   "message": "Operation completed successfully",
 *   "data": { "id": 1, "name": "John" }
 * }
 * }</pre>
 * 
 * @param <T> the type of data payload
 * @param success whether the operation was successful
 * @param message descriptive message about the operation
 * @param data the actual response data, null for failures
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 0.0.1
 */
public record ResponseWrapper<T>(boolean success, String message, T data) {

	/**
	 * Creates a successful response wrapper with data and message.
	 * 
	 * @param <T> the type of data
	 * @param data the response data
	 * @param message success message
	 * @return successful ResponseWrapper
	 */
	public static <T> ResponseWrapper<T> success(T data, String message) {
		return new ResponseWrapper<>(true, message, data);
	}

	/**
	 * Creates a failure response wrapper with error message.
	 * 
	 * @param <T> the type of data
	 * @param message error message
	 * @return failure ResponseWrapper with null data
	 */
	public static <T> ResponseWrapper<T> failure(String message) {
		return new ResponseWrapper<>(false, message, null);
	}
}
