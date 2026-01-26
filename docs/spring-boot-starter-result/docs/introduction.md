---
slug: /
title: Introduction
sidebar_position: 1
---

# Spring Boot Result Starter

A Spring Boot library that implements the Result pattern for elegant error handling and response management.

## Installation

### Maven
```xml
<dependency>
    <groupId>io.github.homitra</groupId>
    <artifactId>spring-boot-starter-result</artifactId>
    <version>0.0.9</version>
</dependency>
```

### Gradle
```gradle
implementation 'io.github.homitra:spring-boot-starter-result:0.0.9'
```

## The Problem with Traditional Exception Handling

Traditional Spring Boot error handling has several issues:

```java
// Traditional approach - Problems:
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    try {
        User user = userService.findById(id); // Might throw!
        return ResponseEntity.ok(user);
    } catch (UserNotFoundException e) {
        return ResponseEntity.notFound().build();
    } catch (ValidationException e) {
        return ResponseEntity.badRequest().build();
    }
    // What if we forget to handle an exception? 💥
}
```

**Issues:**
- ❌ **Unpredictable Control Flow** - Exceptions jump out unexpectedly
- ❌ **Hidden Failure Points** - No way to know what exceptions a method throws
- ❌ **Forgotten Error Handling** - Easy to miss catch blocks
- ❌ **Inconsistent Responses** - Different error formats across API
- ❌ **Performance Impact** - Exception stack traces are expensive
- ❌ **Testing Complexity** - Hard to test all error scenarios

## What is the Result Pattern?

The Result pattern makes success and failure explicit in your code. Instead of throwing exceptions, methods return a `Result<T>` object that either contains:

- **Success**: The expected data of type `T`
- **Failure**: Error information with details about what went wrong

```java
// Result pattern - Clean and predictable:
@GetMapping("/users/{id}")
public ResponseEntity<?> getUser(@PathVariable Long id) {
    Result<User> result = userService.findById(id);
    return ResponseUtils.asResponse(result);
    // That's it! All errors handled automatically ✨
}
```

## Why This is Better

✅ **Explicit Error Handling** - Success and failure are part of the method signature  
✅ **Predictable Control Flow** - No unexpected jumps in execution  
✅ **Type Safety** - Compiler ensures you handle both success and failure cases  
✅ **Consistent API Responses** - Uniform response format across your application  
✅ **Better Performance** - No expensive exception stack traces  
✅ **Easier Testing** - Test success and failure scenarios with simple assertions  
✅ **Self-Documenting Code** - Method signatures tell you exactly what can go wrong  

## Quick Example

```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Result<User> result = userService.findById(id);
        return ResponseUtils.asResponse(result);
        // Automatically returns 404 if user not found
        // Returns 200 with user data if found
    }
}

@Service
public class UserService {
    
    public Result<User> findById(Long id) {
        return userRepository.findById(id)
            .map(Result::success)
            .orElse(Result.entityNotFoundError("User not found"));
        // Explicit success or failure - no surprises!
    }
}
```

## Response Format

All responses follow a consistent structure:

**Success Response:**
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "User not found",
  "data": null
}
```

## Key Features

### 🔒 Type-Safe Error Handling
No more forgotten exception handling. The compiler ensures you handle both success and failure cases.

### 🌐 Automatic HTTP Integration
Automatic status code mapping:
- `EntityNotFoundError` → 404 NOT_FOUND
- `ValidationError` → 400 BAD_REQUEST
- `UnauthorizedError` → 401 UNAUTHORIZED
- `ForbiddenError` → 403 FORBIDDEN
- `EntityAlreadyExistsError` → 409 CONFLICT

### 🔄 Smart Transaction Management

`@RollbackOnFailure` marks the entire transaction for rollback when the method returns a failed Result:

```java
@RollbackOnFailure
public Result<User> createUserWithProfile(CreateUserRequest request) {
    // All these operations happen first
    User user = userRepository.save(new User(request.getName(), request.getEmail()));
    profileService.createProfile(user.getId());
    auditService.logUserCreation(user.getId());
    
    // If this validation fails, ALL above operations are rolled back
    if (businessRuleViolated(user)) {
        return Result.validationError("Business rule violated");
    }
    
    return Result.success(user);
}
```

### ⚡ Fluent Validation Chains
```java
Result<User> result = Result.success(new User(name, email))
    .validate(user -> user.getName() != null, "Name is required")
    .validate(user -> user.getEmail().contains("@"), "Invalid email")
    .validate(user -> user.getName().length() >= 2, "Name too short");
```

### 🚀 Async Support
```java
@GetMapping("/users/{id}/async")
public CompletableFuture<ResponseEntity<?>> getUserAsync(@PathVariable Long id) {
    return Result.async(() -> userService.findById(id))
        .thenApply(ResponseUtils::asResponse);
}
```

### 📢 Event Publishing
```java
@PublishEvent(on = PublishEvent.EventType.SUCCESS)
public Result<User> createUser(CreateUserRequest request) {
    // Event published automatically on success
    return Result.success(userRepository.save(user));
}
```

## Architecture Benefits

### For Developers
- **Less Boilerplate** - No more try-catch blocks everywhere
- **Better IDE Support** - Autocomplete shows you all possible outcomes
- **Easier Debugging** - Clear error messages without stack traces
- **Functional Style** - Chain operations with `map`, `flatMap`, `validate`

### For Teams
- **Consistent Codebase** - Everyone handles errors the same way
- **Easier Code Reviews** - Error handling is explicit and visible
- **Better Documentation** - Method signatures are self-documenting
- **Reduced Bugs** - Compiler catches unhandled error cases

### For Applications
- **Better Performance** - No exception overhead
- **Consistent APIs** - Uniform response format
- **Better User Experience** - Predictable error messages
- **Easier Integration** - Clear success/failure indicators

## Requirements

- **Java**: 17+ (Java 21+ recommended for virtual threads)
- **Spring Boot**: 3.0+
- **Spring Framework**: 6.0+

## Next Steps

Ready to transform your error handling? Check out the [Quick Start Guide](./quickstart)!