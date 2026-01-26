---
title: Migration Guide
sidebar_position: 7
---

# Migration Guide

Step-by-step guide for migrating existing Spring Boot applications to use the Result pattern.

## Migration Strategy

### Phase 1: Add Dependency and Basic Setup

1. **Add the dependency** to your project
2. **Create configuration** for features you need
3. **Start with new endpoints** using Result pattern
4. **Gradually migrate existing code**

### Phase 2: Identify Migration Candidates

Look for these patterns in your existing code:

```java
// ❌ Exception-based error handling
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    try {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    } catch (UserNotFoundException e) {
        return ResponseEntity.notFound().build();
    } catch (ValidationException e) {
        return ResponseEntity.badRequest().build();
    }
}

// ❌ Inconsistent error responses
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<String> handleUserNotFound(UserNotFoundException e) {
    return ResponseEntity.status(404).body("User not found");
}

@ExceptionHandler(ValidationException.class)
public ResponseEntity<Map<String, String>> handleValidation(ValidationException e) {
    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
}
```

## Step-by-Step Migration

### Step 1: Migrate Service Layer Methods

**Before:**
```java
@Service
public class UserService {
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
    
    public User createUser(CreateUserRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException("Name is required");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        
        User user = new User(request.getName(), request.getEmail());
        return userRepository.save(user);
    }
}
```

**After:**
```java
@Service
public class UserService {
    
    public Result<User> findById(Long id) {
        return userRepository.findById(id)
            .map(Result::success)
            .orElse(Result.entityNotFoundError("User not found"));
    }
    
    @RollbackOnFailure
    public Result<User> createUser(CreateUserRequest request) {
        return Result.success(new User(request.getName(), request.getEmail()))
            .validate(user -> user.getName() != null && !user.getName().trim().isEmpty(), 
                     "Name is required")
            .validate(user -> !userRepository.existsByEmail(user.getEmail()), 
                     "Email already exists")
            .map(userRepository::save);
    }
}
```

### Step 2: Migrate Controller Layer

**Before:**
```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request);
            return ResponseEntity.ok(user);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }
}
```

**After:**
```java
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

### Step 3: Remove Exception Handlers

**Before:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        ErrorResponse error = new ErrorResponse("USER_NOT_FOUND", e.getMessage());
        return ResponseEntity.status(404).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExists(UserAlreadyExistsException e) {
        ErrorResponse error = new ErrorResponse("USER_EXISTS", e.getMessage());
        return ResponseEntity.status(409).body(error);
    }
}
```

**After:**
```java
// ✅ No more exception handlers needed!
// ResponseUtils.asResponse() handles all error mapping automatically
```

## Migration Patterns

### Pattern 1: Optional to Result

**Before:**
```java
public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
}

// Usage
Optional<User> userOpt = userService.findByEmail(email);
if (userOpt.isPresent()) {
    // handle success
} else {
    // handle not found
}
```

**After:**
```java
public Result<User> findByEmail(String email) {
    return userRepository.findByEmail(email)
        .map(Result::success)
        .orElse(Result.entityNotFoundError("User with email " + email + " not found"));
}

// Usage
Result<User> result = userService.findByEmail(email);
if (result.isSuccess()) {
    User user = result.getData();
    // handle success
} else {
    Error error = result.getError();
    // handle error
}
```

### Pattern 2: Void Methods with Exceptions

**Before:**
```java
public void deleteUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    userRepository.delete(user);
}
```

**After:**
```java
@RollbackOnFailure
public Result<Void> deleteUser(Long id) {
    return userRepository.findById(id)
        .map(user -> {
            userRepository.delete(user);
            return Result.<Void>success(null);
        })
        .orElse(Result.entityNotFoundError("User not found"));
}
```

### Pattern 3: Complex Validation Logic

**Before:**
```java
public User updateUser(Long id, UpdateUserRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    
    if (request.getName() == null || request.getName().trim().isEmpty()) {
        throw new ValidationException("Name is required");
    }
    
    if (request.getName().length() < 2) {
        throw new ValidationException("Name too short");
    }
    
    if (request.getEmail() != null && 
        !request.getEmail().equals(user.getEmail()) &&
        userRepository.existsByEmail(request.getEmail())) {
        throw new UserAlreadyExistsException("Email already exists");
    }
    
    user.setName(request.getName());
    if (request.getEmail() != null) {
        user.setEmail(request.getEmail());
    }
    
    return userRepository.save(user);
}
```

**After:**
```java
@RollbackOnFailure
public Result<User> updateUser(Long id, UpdateUserRequest request) {
    return userRepository.findById(id)
        .map(Result::success)
        .orElse(Result.entityNotFoundError("User not found"))
        .validate(user -> request.getName() != null && !request.getName().trim().isEmpty(),
                 "Name is required")
        .validate(user -> request.getName().length() >= 2,
                 "Name too short")
        .validate(user -> request.getEmail() == null || 
                         request.getEmail().equals(user.getEmail()) ||
                         !userRepository.existsByEmail(request.getEmail()),
                 "Email already exists")
        .map(user -> {
            user.setName(request.getName());
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
            return userRepository.save(user);
        });
}
```

## Testing Migration

### Before and After Comparison

**Before (Exception-based testing):**
```java
@Test
void findById_WhenUserNotExists_ThrowsException() {
    // Given
    when(userRepository.findById(999L)).thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(UserNotFoundException.class, () -> {
        userService.findById(999L);
    });
}
```

**After (Result-based testing):**
```java
@Test
void findById_WhenUserNotExists_ReturnsNotFoundError() {
    // Given
    when(userRepository.findById(999L)).thenReturn(Optional.empty());
    
    // When
    Result<User> result = userService.findById(999L);
    
    // Then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getError()).isInstanceOf(EntityNotFoundError.class);
    assertThat(result.getError().getMessage()).contains("User not found");
}
```

## Gradual Migration Strategy

### Phase 1: New Features Only
- Use Result pattern for all new endpoints
- Keep existing code unchanged
- Gain experience with the pattern

### Phase 2: High-Impact Areas
- Migrate critical business logic first
- Focus on areas with complex error handling
- Migrate frequently used endpoints

### Phase 3: Complete Migration
- Migrate remaining endpoints
- Remove old exception handlers
- Clean up unused exception classes

## Common Migration Challenges

### Challenge 1: Existing Client Dependencies

**Problem:** External clients expect specific error response formats.

**Solution:** Use custom response mapping temporarily:
```java
@GetMapping("/legacy/users/{id}")
public ResponseEntity<?> getUserLegacy(@PathVariable Long id) {
    Result<User> result = userService.findById(id);
    
    if (result.isSuccess()) {
        return ResponseEntity.ok(result.getData());
    } else {
        // Legacy error format
        return ResponseEntity.status(404).body(Map.of("error", result.getError().getMessage()));
    }
}
```

### Challenge 2: Mixed Result and Exception Code

**Problem:** Some methods return Results, others throw exceptions.

**Solution:** Create adapter methods:
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
```

### Challenge 3: Transaction Management

**Problem:** Existing `@Transactional` methods need to work with Results.

**Solution:** Use `@RollbackOnFailure` annotation:
```java
@Transactional
@RollbackOnFailure
public Result<User> complexUserOperation(UserRequest request) {
    // Complex multi-step operation
    // Transaction rolls back automatically if Result indicates failure
    return Result.success(processedUser);
}
```

## Migration Checklist

- [ ] Add Spring Boot Result Starter dependency
- [ ] Configure required aspects (`@RollbackOnFailure`, `@PublishEvent`)
- [ ] Identify methods to migrate (start with service layer)
- [ ] Convert exception-throwing methods to return Results
- [ ] Update controllers to use `ResponseUtils.asResponse()`
- [ ] Migrate tests to assert on Result objects
- [ ] Remove unused exception handlers
- [ ] Update API documentation
- [ ] Train team on Result pattern usage
- [ ] Monitor and validate migration success

## Benefits After Migration

✅ **Consistent Error Handling** - All endpoints return uniform error format  
✅ **Better Type Safety** - Compiler catches unhandled error cases  
✅ **Improved Performance** - No exception overhead  
✅ **Easier Testing** - Simple assertions on Result objects  
✅ **Self-Documenting APIs** - Method signatures show possible outcomes  
✅ **Reduced Boilerplate** - No more try-catch blocks everywhere  