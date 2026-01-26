---
title: Event Publishing
sidebar_position: 3
---

# Event Publishing

Automatically publish events based on Result outcomes.

## Basic Event Publishing

```java
@Service
public class UserService {
    
    @PublishEvent(on = PublishEvent.EventType.SUCCESS, eventName = "user-created")
    @RollbackOnFailure
    public Result<User> createUser(CreateUserRequest request) {
        User user = new User(request.getName(), request.getEmail());
        return Result.success(userRepository.save(user));
        // Event published automatically on success
    }
}
```

## Event Types

- `SUCCESS` - Publish only when Result is successful
- `FAILURE` - Publish only when Result contains error
- `BOTH` - Publish on both success and failure

```java
@PublishEvent(on = PublishEvent.EventType.BOTH, eventName = "user-operation")
public Result<User> updateUser(Long id, UpdateUserRequest request) {
    // Event published regardless of outcome
    return userService.update(id, request);
}
```

## Event Listeners

```java
@Component
public class UserEventListener {
    
    @EventListener
    public void handleUserEvents(ResultEvent<?> event) {
        if ("user-created".equals(event.getEventName()) && event.isSuccess()) {
            User user = (User) event.getResult().getData();
            log.info("User created: {}", user.getName());
            // Send welcome email, update analytics, etc.
        }
    }
    
    @EventListener
    @Async
    public void handleAsyncUserEvents(ResultEvent<User> event) {
        if (event.isSuccess()) {
            emailService.sendWelcomeEmail(event.getResult().getData());
        }
    }
}
```

## ResultEvent Properties

```java
public class ResultEvent<T> extends ApplicationEvent {
    public String getEventName()        // Custom event name or method name
    public Result<T> getResult()        // The Result object
    public String getMethodName()       // Method that was executed
    public Object[] getArgs()           // Method arguments
    public boolean isSuccess()          // Convenience success check
}
```