---
title: Examples
sidebar_position: 3
---

# Examples

Practical examples using Spring Boot Result Starter.

## Basic CRUD Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Result<User> result = userService.findById(id);
        return ResponseUtils.asResponse(result);
    }
    
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        Result<User> result = userService.createUser(request);
        return ResponseUtils.asResponse(result);
    }
}
```

## Service Layer

```java
@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public Result<User> findById(Long id) {
        return userRepository.findById(id)
            .map(Result::success)
            .orElse(Result.entityNotFoundError("User not found"));
    }
    
    @RollbackOnFailure
    public Result<User> createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return Result.entityAlreadyExistsError("Email already exists");
        }
        
        User user = new User(request.getName(), request.getEmail());
        return Result.success(userRepository.save(user));
    }
}
```

## Validation Chains

```java
public Result<User> validateAndCreateUser(CreateUserRequest request) {
    return Result.success(new User(request.getName(), request.getEmail()))
        .validate(user -> user.getName() != null, "Name is required")
        .validate(user -> user.getEmail() != null, "Email is required")
        .validate(user -> user.getEmail().contains("@"), "Invalid email")
        .map(userRepository::save);
}
```

## Async Operations

```java
@GetMapping("/users/{id}/async")
public CompletableFuture<ResponseEntity<?>> getUserAsync(@PathVariable Long id) {
    return Result.async(() -> userService.findById(id))
        .thenApply(ResponseUtils::asResponse);
}
```

## Bulk Operations

```java
@PostMapping("/users/bulk")
public ResponseEntity<?> createUsers(@RequestBody List<CreateUserRequest> requests) {
    List<Result<User>> results = requests.stream()
        .map(userService::createUser)
        .toList();
    
    Result<List<User>> bulkResult = Result.combine(results);
    return ResponseUtils.asResponse(bulkResult);
}
```

## Event Handling

```java
@Service
public class UserService {
    
    @PublishEvent(on = PublishEvent.EventType.SUCCESS, eventName = "user-created")
    @RollbackOnFailure
    public Result<User> createUser(CreateUserRequest request) {
        User user = new User(request.getName(), request.getEmail());
        return Result.success(userRepository.save(user));
    }
}

@Component
public class UserEventListener {
    
    @EventListener
    public void handleUserCreated(ResultEvent<?> event) {
        if ("user-created".equals(event.getEventName()) && event.isSuccess()) {
            User user = (User) event.getResult().getData();
            // Send welcome email, etc.
        }
    }
}
```

## Error Handling Patterns

```java
public Result<UserProfile> getUserProfile(Long userId) {
    return userService.findById(userId)
        .flatMap(user -> {
            UserProfile profile = new UserProfile(user);
            
            // Optional enrichment - don't fail if unavailable
            preferencesService.getPreferences(userId)
                .onSuccess(profile::setPreferences)
                .onFailure(error -> log.warn("Could not load preferences"));
            
            return Result.success(profile);
        });
}
```

## Testing

```java
@Test
void findById_WhenUserExists_ReturnsSuccess() {
    // Given
    User user = new User("John", "john@example.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    
    // When
    Result<User> result = userService.findById(1L);
    
    // Then
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isEqualTo(user);
}

@Test
void findById_WhenUserNotExists_ReturnsNotFoundError() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    
    // When
    Result<User> result = userService.findById(1L);
    
    // Then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getError()).isInstanceOf(EntityNotFoundError.class);
}
```