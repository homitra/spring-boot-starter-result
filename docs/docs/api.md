---
title: API Reference
sidebar_position: 9
---

# API Reference

Complete reference for Spring Boot Result Starter based on actual implementation.

## Result&lt;T&gt;

Main class for the Result pattern.

### Static Factory Methods

```java
// Success
static <T> Result<T> success(T data)
static <T> Result<T> success(T data, String message)

// Errors
static <T> Result<T> failure(Error error)
static <T> Result<T> failure(String message)
static <T> Result<T> entityNotFoundError(String message)
static <T> Result<T> validationError(String message)
static <T> Result<T> unauthorizedError(String message)
static <T> Result<T> forbiddenError(String message)
static <T> Result<T> entityAlreadyExistsError(String message)
```

### Instance Methods

```java
// State checking
boolean isSuccess()
T getData()
Error getError()
String getMessage()

// Functional operations
<R> Result<R> map(Function<T, R> mapper)
<R> Result<R> flatMap(Function<T, Result<R>> mapper)

// Validation
Result<T> validate(Predicate<T> predicate, String errorMessage)
Result<T> filter(Predicate<T> predicate, String errorMessage)

// Conditional operations
Result<T> onSuccess(Consumer<T> action)
Result<T> onFailure(Consumer<Error> action)

// Fallback operations
Result<T> orElse(Result<T> alternative)
T orElseGet(Supplier<T> supplier)

// Async operations
static <T> CompletableFuture<Result<T>> async(Supplier<Result<T>> supplier)

// Bulk operations
static <T> Result<List<T>> combine(List<Result<T>> results)
static <T> Result<List<T>> combine(Result<T>... results)
```

## ResponseUtils

HTTP response utilities.

### Methods

```java
static <T> ResponseEntity<ResponseWrapper<T>> asResponse(Result<T> result)
static <T> ResponseEntity<ResponseWrapper<T>> success(T data)
static <T> ResponseEntity<ResponseWrapper<T>> success(T data, String message)
static <T> ResponseEntity<ResponseWrapper<T>> failure(String message)
```

### HTTP Status Mapping

| Error Type | Status Code |
|------------|-------------|
| EntityNotFoundError | 404 |
| ValidationError | 400 |
| UnauthorizedError | 401 |
| ForbiddenError | 403 |
| EntityAlreadyExistsError | 409 |
| Other Error | 500 |
| Success | 200 |

## Annotations

### @RollbackOnFailure

Marks the entire transaction for rollback when the annotated method returns a failed Result.

**Important:** The rollback happens **after** the method completes and **all database operations within the transaction are rolled back**, even if they were called by other methods.

```java
@Service
public class UserService {
    
    @RollbackOnFailure  // Also includes @Transactional
    public Result<User> createUserWithProfile(CreateUserRequest request) {
        // Step 1: Save user (this will be rolled back if method returns failure)
        User user = userRepository.save(new User(request.getName(), request.getEmail()));
        
        // Step 2: Create profile (this will also be rolled back)
        profileService.createProfile(user.getId(), request.getProfileData());
        
        // Step 3: Send notification (this will also be rolled back if it writes to DB)
        notificationService.createWelcomeNotification(user.getId());
        
        // Step 4: Validation at the end
        if (someBusinessRule.isViolated(user)) {
            // ALL previous database operations will be rolled back
            return Result.validationError("Business rule violated");
        }
        
        return Result.success(user);
    }
}
```

**How it works:**
1. Method executes completely with all database operations
2. Aspect checks the returned Result
3. If `Result.isSuccess() == false`, marks transaction for rollback
4. Spring rolls back ALL database changes made during the transaction

### @PublishEvent

Publishes events based on Result outcomes.

```java
@PublishEvent(on = PublishEvent.EventType.SUCCESS)
public Result<User> createUser(CreateUserRequest request) {
    // Event published on success
    return Result.success(userRepository.save(user));
}
```

#### EventType Options
- `SUCCESS` - Publish only on successful Results
- `FAILURE` - Publish only on failed Results  
- `BOTH` - Publish on both success and failure

## Error Types

All extend the base `Error` class:

- `EntityNotFoundError` - For 404 scenarios
- `ValidationError` - For 400 validation failures
- `UnauthorizedError` - For 401 authentication failures
- `ForbiddenError` - For 403 authorization failures
- `EntityAlreadyExistsError` - For 409 conflict scenarios