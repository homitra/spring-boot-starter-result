---
title: Async Operations
sidebar_position: 2
---

# Async Operations

Handle asynchronous operations with CompletableFuture support.

## Async Controller Endpoints

```java
@RestController
public class AsyncController {
    
    @GetMapping("/users/{id}/async")
    public CompletableFuture<ResponseEntity<?>> getUserAsync(@PathVariable Long id) {
        return Result.async(() -> userService.findById(id))
            .thenApply(ResponseUtils::asResponse);
    }
}
```

## Async Service Methods

```java
@Service
public class UserService {
    
    @Async
    public CompletableFuture<Result<User>> processUserAsync(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate external API call
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Result.failure("Processing interrupted");
            }
            
            return findById(id);
        });
    }
}
```

## Combining Async Results

```java
public CompletableFuture<Result<UserProfile>> buildUserProfile(Long userId) {
    CompletableFuture<Result<User>> userFuture = Result.async(() -> userService.findById(userId));
    CompletableFuture<Result<List<Order>>> ordersFuture = Result.async(() -> orderService.findByUserId(userId));
    
    return userFuture.thenCombine(ordersFuture, (userResult, ordersResult) -> {
        if (!userResult.isSuccess()) return Result.failure(userResult.getError());
        if (!ordersResult.isSuccess()) return Result.failure(ordersResult.getError());
        
        UserProfile profile = new UserProfile(userResult.getData(), ordersResult.getData());
        return Result.success(profile);
    });
}
```