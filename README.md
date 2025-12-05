# Spring Boot Result Starter

A Spring Boot library that implements the Result pattern for elegant error handling and response management.

## Installation

### Maven
```xml
<dependency>
    <groupId>io.github.homitra</groupId>
    <artifactId>spring-boot-starter-result</artifactId>
    <version>0.0.5</version>
</dependency>
```

### Gradle
```gradle
implementation 'io.github.homitra:spring-boot-starter-result:0.0.5'
```

## Usage

### Basic Result Pattern

```java
import io.github.homitra.spring.boot.result.Result;

// Success
Result<User> result = Result.success(user);

// Error
Result<User> result = Result.entityNotFoundError("User not found");
Result<User> result = Result.validationError("Invalid data");
Result<User> result = Result.unauthorizedError("Access denied");
Result<User> result = Result.forbiddenError("Insufficient permissions");
Result<User> result = Result.entityAlreadyExistsError("User exists");
```

### HTTP Response Integration

```java
import io.github.homitra.spring.boot.result.api.ResponseUtils;

@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Result<User> result = userService.findById(id);
        return ResponseUtils.asResponse(result);
    }
    
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        Result<User> result = userService.createUser(request);
        return ResponseUtils.asResponse(result);
    }
}
```

### Automatic Transaction Rollback

```java
import io.github.homitra.spring.boot.result.annotations.RollbackOnFailure;

@Service
public class UserService {
    
    @RollbackOnFailure
    public Result<User> createUser(CreateUserRequest request) {
        User user = new User(request.getName(), request.getEmail());
        User savedUser = userRepository.save(user);
        return Result.success(savedUser);
        // Transaction automatically rolls back if Result contains error
    }
    
    @RollbackOnFailure
    public Result<User> updateUser(Long id, UpdateUserRequest request) {
        return userRepository.findById(id)
            .map(user -> {
                user.setName(request.getName());
                return Result.success(userRepository.save(user));
            })
            .orElse(Result.entityNotFoundError("User not found"));
    }
}
```

## Advanced Features

### Validation Chain

```java
@PostMapping("/users")
public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
    Result<User> result = Result.success(new User(request.getName(), request.getEmail()))
        .validate(user -> user.getName() != null, "Name is required")
        .validate(user -> user.getEmail() != null, "Email is required")
        .validate(user -> user.getEmail().contains("@"), "Invalid email format")
        .validate(user -> user.getName().length() >= 2, "Name too short");
    
    return ResponseUtils.asResponse(result);
}
```

### Async Operations

```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}/async")
    public CompletableFuture<ResponseEntity<?>> getUserAsync(@PathVariable Long id) {
        return Result.async(() -> userService.findById(id))
            .thenApply(ResponseUtils::asResponse);
    }
}

@Service
public class UserService {
    
    @Async
    public CompletableFuture<Result<User>> createUserAsync(CreateUserRequest request) {
        return Result.execute(() -> {
            User user = new User(request.getName(), request.getEmail());
            return Result.success(userRepository.save(user));
        });
    }
}
```

### Bulk Operations

```java
@PostMapping("/users/bulk")
public ResponseEntity<?> createUsers(@RequestBody List<CreateUserRequest> requests) {
    List<Result<User>> results = requests.stream()
        .map(userService::createUser)
        .toList();
    
    Result<List<User>> bulkResult = Result.combine(results);
    return ResponseUtils.asResponse(bulkResult);
}

// Alternative: Stop on first failure
@PostMapping("/users/bulk-safe")
public ResponseEntity<?> createUsersSafe(@RequestBody List<CreateUserRequest> requests) {
    Result<List<User>> result = Result.combine(
        userService.createUser(requests.get(0)),
        userService.createUser(requests.get(1)),
        userService.createUser(requests.get(2))
    );
    return ResponseUtils.asResponse(result);
}
```

### Event Publishing

```java
import io.github.homitra.spring.boot.result.annotations.PublishEvent;

@Service
public class UserService {
    
    @PublishEvent(on = PublishEvent.EventType.SUCCESS)
    @RollbackOnFailure
    public Result<User> createUser(CreateUserRequest request) {
        User user = new User(request.getName(), request.getEmail());
        return Result.success(userRepository.save(user));
        // Event published automatically on success
    }
    
    @PublishEvent(on = PublishEvent.EventType.BOTH, eventName = "user-operation")
    public Result<User> updateUser(Long id, UpdateUserRequest request) {
        return userRepository.findById(id)
            .map(user -> {
                user.setName(request.getName());
                return Result.success(userRepository.save(user));
            })
            .orElse(Result.entityNotFoundError("User not found"));
    }
}
```

### Conditional Operations

```java
@Service
public class UserService {
    
    public Result<User> processUser(Long id) {
        return findById(id)
            .onSuccess(user -> log.info("Found user: {}", user.getName()))
            .onFailure(error -> log.error("User not found: {}", error.getMessage()))
            .flatMap(user -> updateLastLogin(user))
            .orElse(Result.success(getDefaultUser()));
    }
    
    public User getUserOrDefault(Long id) {
        return findById(id)
            .orElseGet(() -> new User("Guest", "guest@example.com"));
    }
}
```

## Response Format

All responses follow this structure:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "id": 1,
    "name": "John Doe"
  }
}
```

Error response:
```json
{
  "success": false,
  "message": "User not found",
  "data": null
}
```

## HTTP Status Codes

ResponseUtils automatically returns appropriate status codes:

- `EntityNotFoundError` → 404 NOT_FOUND
- `ValidationError` → 400 BAD_REQUEST  
- `UnauthorizedError` → 401 UNAUTHORIZED
- `ForbiddenError` → 403 FORBIDDEN
- `EntityAlreadyExistsError` → 409 CONFLICT
- Other errors → 500 INTERNAL_SERVER_ERROR
- Success → 200 OK

## Architecture

The library follows a clean architecture with controlled access:

### **Public API (Only accessible classes):**
- `Result<T>` - Main API class
- `ResponseUtils` - HTTP response utilities  
- `@RollbackOnFailure` - Transaction rollback annotation
- `@PublishEvent` - Event publishing annotation

### **Internal Structure:**
```
io.github.homitra.spring.boot.result/
├── Result.java                    // Main API
├── api/
│   └── ResponseUtils.java        // HTTP utilities
├── annotations/
│   ├── RollbackOnFailure.java   // Transaction annotation
│   └── PublishEvent.java        // Event annotation
├── domain/
│   ├── errors/                  // Sealed error hierarchy
│   └── events/                  // Domain events
├── infrastructure/
│   ├── aspects/                 // AOP implementations
│   ├── handlers/                // Exception handlers
│   └── config/                  // Configuration
└── internal/                    // Internal utilities
```

## Configuration

Enable all features:

```java
@Configuration
@EnableAsync
@EnableAspectJAutoProxy
public class ResultConfig {
    
    @Bean
    public TransactionRollbackAspect transactionRollbackAspect() {
        return new TransactionRollbackAspect();
    }
    
    @Bean
    public EventPublishingAspect eventPublishingAspect(ApplicationEventPublisher publisher) {
        return new EventPublishingAspect(publisher);
    }
    
    @Bean
    public GlobalResultExceptionHandler globalExceptionHandler() {
        return new GlobalResultExceptionHandler();
    }
}
```

## Features Summary

- ✅ **Type-safe Result Pattern** - Handle success/failure explicitly
- ✅ **HTTP Integration** - Automatic status codes and response formatting
- ✅ **Transaction Management** - Auto-rollback on failure
- ✅ **Validation Chain** - Fluent validation with early termination
- ✅ **Async Support** - Non-blocking operations with CompletableFuture
- ✅ **Bulk Operations** - Process multiple items atomically
- ✅ **Event Publishing** - Automatic event publishing on success/failure
- ✅ **Conditional Operations** - Functional composition with flatMap, onSuccess, onFailure
- ✅ **Sealed Classes** - Controlled inheritance and encapsulation
- ✅ **Clean Architecture** - Proper separation of concerns