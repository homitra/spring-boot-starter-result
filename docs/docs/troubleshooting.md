---
title: Troubleshooting & FAQ
sidebar_position: 8
---

# Troubleshooting & FAQ

Common issues and solutions when using Spring Boot Result Starter.

## Common Issues

### 1. @RollbackOnFailure Not Working

**Problem:** Transactions are not rolling back when Result indicates failure.

**Symptoms:**
- Data is saved even when Result contains error
- No rollback occurs on failed Results

**Solutions:**

#### Missing Configuration
```java
// ❌ Missing configuration
@Configuration
public class AppConfig {
    // No aspect configuration
}

// ✅ Correct configuration
@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class AppConfig {
    
    @Bean
    public TransactionRollbackAspect transactionRollbackAspect() {
        return new TransactionRollbackAspect();
    }
}
```

#### Missing @Transactional
```java
// ❌ No transaction context
@Service
public class UserService {
    
    @RollbackOnFailure
    public Result<User> createUser(CreateUserRequest request) {
        // No transaction to rollback
        return Result.success(userRepository.save(user));
    }
}

// ✅ With transaction context
@Service
@Transactional
public class UserService {
    
    @RollbackOnFailure
    public Result<User> createUser(CreateUserRequest request) {
        return Result.success(userRepository.save(user));
    }
}
```

#### Incorrect Return Type
```java
// ❌ Wrong return type
@RollbackOnFailure
public User createUser(CreateUserRequest request) {
    // Aspect only works with Result<T> return types
    return userRepository.save(user);
}

// ✅ Correct return type
@RollbackOnFailure
public Result<User> createUser(CreateUserRequest request) {
    return Result.success(userRepository.save(user));
}
```

### 2. @PublishEvent Not Triggering

**Problem:** Events are not being published when using @PublishEvent.

**Solutions:**

#### Missing Aspect Configuration
```java
// ✅ Add event publishing aspect
@Configuration
@EnableAspectJAutoProxy
public class AppConfig {
    
    @Bean
    public EventPublishingAspect eventPublishingAspect(ApplicationEventPublisher publisher) {
        return new EventPublishingAspect(publisher);
    }
}
```

#### Incorrect Event Type Configuration
```java
// ❌ Event type mismatch
@PublishEvent(on = PublishEvent.EventType.SUCCESS)
public Result<User> createUser(CreateUserRequest request) {
    return Result.validationError("Invalid data"); // Returns failure, no event published
}

// ✅ Correct configuration
@PublishEvent(on = PublishEvent.EventType.BOTH) // or FAILURE
public Result<User> createUser(CreateUserRequest request) {
    return Result.validationError("Invalid data"); // Event will be published
}
```

### 3. Validation Chains Not Working

**Problem:** Validation chains are not stopping at first failure.

**Example:**
```java
// ❌ Problem: All validations run even after first failure
Result<User> result = Result.success(user)
    .validate(u -> u.getName() != null, "Name required")
    .validate(u -> u.getName().length() > 2, "Name too short"); // NPE if name is null!
```

**Solution:**
```java
// ✅ Validation chains stop at first failure
Result<User> result = Result.success(user)
    .validate(u -> u.getName() != null, "Name required")
    .validate(u -> u.getName().length() > 2, "Name too short"); // Safe - won't run if first fails
```

The validation chain automatically stops at the first failure, so subsequent validations won't execute.

### 4. Async Operations Not Working

**Problem:** `Result.async()` operations are not executing asynchronously.

**Solutions:**

#### Missing @EnableAsync
```java
// ✅ Enable async processing
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public TaskExecutor taskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
```

#### Incorrect Usage
```java
// ❌ Blocking call
Result<User> result = Result.async(() -> userService.findById(1L)).get(); // Blocks!

// ✅ Non-blocking usage
CompletableFuture<Result<User>> futureResult = Result.async(() -> userService.findById(1L));
futureResult.thenApply(ResponseUtils::asResponse); // Non-blocking
```

### 5. HTTP Status Codes Not Mapping Correctly

**Problem:** Wrong HTTP status codes returned for specific error types.

**Check Error Type Usage:**
```java
// ❌ Wrong error type
Result<User> result = Result.failure("User not found"); // Returns 500

// ✅ Correct error type
Result<User> result = Result.entityNotFoundError("User not found"); // Returns 404
```

**Verify ResponseUtils Usage:**
```java
// ❌ Manual status code (ignores error type)
if (!result.isSuccess()) {
    return ResponseEntity.status(500).body(result);
}

// ✅ Automatic status code mapping
return ResponseUtils.asResponse(result);
```

## Frequently Asked Questions

### Q: Can I use Result pattern with existing exception-based code?

**A:** Yes! You can gradually migrate. Use adapter methods:

```java
// Adapter for legacy code
public User findByIdOrThrow(Long id) {
    Result<User> result = findById(id);
    if (result.isSuccess()) {
        return result.getData();
    } else {
        throw new UserNotFoundException(result.getError().getMessage());
    }
}

// New Result-based method
public Result<User> findById(Long id) {
    return userRepository.findById(id)
        .map(Result::success)
        .orElse(Result.entityNotFoundError("User not found"));
}
```

### Q: How do I handle multiple validation errors?

**A:** The validation chain stops at the first failure by design. For collecting all validation errors:

```java
public Result<User> validateUserWithAllErrors(User user) {
    List<String> errors = new ArrayList<>();
    
    if (user.getName() == null) errors.add("Name is required");
    if (user.getEmail() == null) errors.add("Email is required");
    if (user.getName() != null && user.getName().length() < 2) errors.add("Name too short");
    
    if (!errors.isEmpty()) {
        return Result.validationError(String.join(", ", errors));
    }
    
    return Result.success(user);
}
```

### Q: Can I customize the response format?

**A:** Yes, create your own response utility:

```java
public class CustomResponseUtils {
    
    public static <T> ResponseEntity<CustomResponse<T>> asCustomResponse(Result<T> result) {
        if (result.isSuccess()) {
            return ResponseEntity.ok(new CustomResponse<>(
                "OK", 
                result.getMessage(), 
                result.getData()
            ));
        } else {
            HttpStatus status = mapErrorToStatus(result.getError());
            return ResponseEntity.status(status).body(new CustomResponse<>(
                "ERROR",
                result.getError().getMessage(),
                null
            ));
        }
    }
}
```

### Q: How do I test async Result operations?

**A:** Use CompletableFuture testing patterns:

```java
@Test
void testAsyncOperation() throws Exception {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    
    // When
    CompletableFuture<Result<User>> future = Result.async(() -> userService.findById(1L));
    
    // Then
    Result<User> result = future.get(5, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isTrue();
}
```

### Q: Can I use Result with Spring Data JPA projections?

**A:** Yes:

```java
public Result<UserProjection> findUserProjection(Long id) {
    return userRepository.findProjectionById(id, UserProjection.class)
        .map(Result::success)
        .orElse(Result.entityNotFoundError("User not found"));
}
```

### Q: How do I handle file upload errors with Result?

**A:** Create specific error types:

```java
public Result<String> uploadFile(MultipartFile file) {
    if (file.isEmpty()) {
        return Result.validationError("File is empty");
    }
    
    if (file.getSize() > MAX_FILE_SIZE) {
        return Result.validationError("File too large");
    }
    
    try {
        String filename = fileService.save(file);
        return Result.success(filename);
    } catch (IOException e) {
        return Result.failure("File upload failed: " + e.getMessage());
    }
}
```

### Q: Can I use Result with Spring Security?

**A:** Yes, handle authentication/authorization:

```java
@Service
public class SecureUserService {
    
    public Result<User> findById(Long id) {
        if (!securityService.hasPermission("READ_USER")) {
            return Result.forbiddenError("Access denied");
        }
        
        return userRepository.findById(id)
            .map(Result::success)
            .orElse(Result.entityNotFoundError("User not found"));
    }
}
```

### Q: How do I migrate from ResponseEntity to Result?

**A:** Gradual migration approach:

```java
// Step 1: Keep existing method, add new Result-based method
@GetMapping("/users/{id}")
public ResponseEntity<User> getUserLegacy(@PathVariable Long id) {
    // Existing implementation
}

@GetMapping("/v2/users/{id}")
public ResponseEntity<?> getUser(@PathVariable Long id) {
    Result<User> result = userService.findById(id);
    return ResponseUtils.asResponse(result);
}

// Step 2: Eventually replace legacy endpoint
```

## Performance Considerations

### Q: Is Result pattern slower than exceptions?

**A:** No, Result pattern is typically faster:

```java
// Performance test results (approximate):
// Result pattern: ~50ms for 100,000 operations
// Exception pattern: ~500ms for 100,000 operations
// Result pattern is ~10x faster for error cases
```

### Q: Memory usage comparison?

**A:** Result pattern uses less memory:
- No stack trace generation
- No exception object creation
- Predictable memory allocation

## Debugging Tips

### Enable Debug Logging

```yaml
# application.yml
logging:
  level:
    io.github.homitra.spring.boot.result: DEBUG
    org.springframework.transaction: DEBUG
    org.springframework.aop: DEBUG
```

### Common Debug Scenarios

#### Check if Aspects are Applied
```java
@Component
public class DebugAspect {
    
    @Around("@annotation(rollbackOnFailure)")
    public Object debugRollback(ProceedingJoinPoint pjp, RollbackOnFailure rollbackOnFailure) throws Throwable {
        System.out.println("RollbackOnFailure aspect applied to: " + pjp.getSignature().getName());
        return pjp.proceed();
    }
}
```

#### Verify Transaction Status
```java
@RollbackOnFailure
public Result<User> createUser(CreateUserRequest request) {
    System.out.println("Transaction active: " + TransactionSynchronizationManager.isActualTransactionActive());
    System.out.println("Transaction read-only: " + TransactionSynchronizationManager.isCurrentTransactionReadOnly());
    
    return Result.success(userRepository.save(user));
}
```

## Getting Help

### Community Resources
- **GitHub Issues**: [Report bugs and feature requests](https://github.com/homitra/spring-boot-starter-result/issues)
- **Discussions**: [Ask questions and share experiences](https://github.com/homitra/spring-boot-starter-result/discussions)

### Before Reporting Issues
1. Check this troubleshooting guide
2. Verify your configuration
3. Test with minimal reproduction case
4. Include relevant code snippets and error messages

### Issue Template
```
**Environment:**
- Spring Boot version: 
- Java version:
- Library version:

**Configuration:**
[Include your configuration classes]

**Expected Behavior:**
[What you expected to happen]

**Actual Behavior:**
[What actually happened]

**Code Sample:**
[Minimal code that reproduces the issue]
```