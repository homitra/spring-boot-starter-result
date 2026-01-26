---
title: Quick Start
sidebar_position: 2
---

# Quick Start Guide

Get up and running with Spring Boot Result Starter in minutes.

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

## Basic Usage

### 1. Create Results

```java
import io.github.homitra.spring.boot.result.Result;

// Success
Result<User> result = Result.success(user);

// Different error types
Result<User> result = Result.entityNotFoundError("User not found");
Result<User> result = Result.validationError("Invalid data");
Result<User> result = Result.unauthorizedError("Access denied");
Result<User> result = Result.forbiddenError("Insufficient permissions");
Result<User> result = Result.entityAlreadyExistsError("User exists");
```

### 2. HTTP Response Integration

```java
import io.github.homitra.spring.boot.result.api.ResponseUtils;

@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Result<User> result = userService.findById(id);
        return ResponseUtils.asResponse(result);
    }
}
```

### 3. Automatic Transaction Rollback

```java
import io.github.homitra.spring.boot.result.annotations.RollbackOnFailure;

@Service
public class UserService {
    
    @RollbackOnFailure  // Includes @Transactional
    public Result<User> createUser(CreateUserRequest request) {
        // Step 1: Save user to database
        User user = new User(request.getName(), request.getEmail());
        User savedUser = userRepository.save(user);
        
        // Step 2: Create audit log
        auditService.logUserCreation(savedUser.getId());
        
        // Step 3: Business validation
        if (savedUser.getEmail().endsWith("@blocked.com")) {
            // ALL database operations above will be rolled back
            return Result.validationError("Email domain not allowed");
        }
        
        return Result.success(savedUser);
    }
}
```

**Key Point:** The rollback happens **after** the method completes. If the returned Result indicates failure, Spring rolls back the entire transaction.

## HTTP Status Code Mapping

The library automatically maps error types to appropriate HTTP status codes:

| Error Type | HTTP Status | Code |
|------------|-------------|------|
| `EntityNotFoundError` | NOT_FOUND | 404 |
| `ValidationError` | BAD_REQUEST | 400 |
| `UnauthorizedError` | UNAUTHORIZED | 401 |
| `ForbiddenError` | FORBIDDEN | 403 |
| `EntityAlreadyExistsError` | CONFLICT | 409 |
| Other errors | INTERNAL_SERVER_ERROR | 500 |
| Success | OK | 200 |

## Configuration

The library requires manual configuration for advanced features. See the [Configuration Guide](./configuration) for details.

```java
@Configuration
@EnableAspectJAutoProxy  // For @RollbackOnFailure and @PublishEvent
public class ResultConfig {
    
    @Bean
    public TransactionRollbackAspect transactionRollbackAspect() {
        return new TransactionRollbackAspect();
    }
    
    @Bean
    public EventPublishingAspect eventPublishingAspect(ApplicationEventPublisher publisher) {
        return new EventPublishingAspect(publisher);
    }
}
```

## Next Steps

- Set up [Configuration](./configuration) for advanced features
- Explore [Examples](./examples) for practical usage
- Check [Advanced Features](./advanced/) for validation chains, async operations, and events

You can start using the basic Result pattern immediately. Advanced features require additional configuration.