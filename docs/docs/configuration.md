---
title: Configuration
sidebar_position: 5
---

# Configuration

Spring Boot Result Starter requires manual configuration to enable advanced features.

## Basic Setup

The core `Result` class and `ResponseUtils` work without any configuration:

```java
// Works out of the box
Result<User> result = Result.success(user);
return ResponseUtils.asResponse(result);
```

## Enable Transaction Rollback

To use `@RollbackOnFailure`, configure the aspect:

```java
@Configuration
@EnableAspectJAutoProxy
public class ResultConfig {
    
    @Bean
    public TransactionRollbackAspect transactionRollbackAspect() {
        return new TransactionRollbackAspect();
    }
}
```

**Usage:**
```java
@Service
@Transactional
public class UserService {
    
    @RollbackOnFailure
    public Result<User> createUser(CreateUserRequest request) {
        // Transaction rolls back if Result indicates failure
        return Result.success(userRepository.save(user));
    }
}
```

## Enable Event Publishing

To use `@PublishEvent`, configure the aspect:

```java
@Configuration
@EnableAspectJAutoProxy
public class ResultConfig {
    
    @Bean
    public EventPublishingAspect eventPublishingAspect(ApplicationEventPublisher publisher) {
        return new EventPublishingAspect(publisher);
    }
}
```

**Usage:**
```java
@PublishEvent(on = PublishEvent.EventType.SUCCESS)
public Result<User> createUser(CreateUserRequest request) {
    // Event published on success
    return Result.success(userRepository.save(user));
}
```

## Enable Async Support

For async operations, enable async processing with virtual threads (Java 21+):

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public TaskExecutor taskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
```

**For Java 17/19:**
```java
@Bean
public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("result-async-");
    executor.initialize();
    return executor;
}
```

## Complete Configuration

Enable all features:

```java
@Configuration
@EnableAspectJAutoProxy
@EnableAsync
@EnableTransactionManagement
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
    public TaskExecutor taskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
```

## Feature Matrix

| Feature | Required Configuration | Dependencies |
|---------|----------------------|--------------|
| `Result<T>` | None | Core library |
| `ResponseUtils` | None | Core library |
| `@RollbackOnFailure` | `TransactionRollbackAspect` | `@EnableAspectJAutoProxy`, `@EnableTransactionManagement` |
| `@PublishEvent` | `EventPublishingAspect` | `@EnableAspectJAutoProxy` |
| `Result.async()` | None | Core library |
| `@Async` methods | `@EnableAsync` | Spring Boot |

## Minimal Configuration

If you only need basic Result pattern without aspects:

```java
// No configuration needed
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Result<User> result = userService.findById(id);
        return ResponseUtils.asResponse(result);
    }
}
```