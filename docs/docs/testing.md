---
title: Testing Guide
sidebar_position: 4
---

# Testing Guide

Comprehensive guide for testing applications using Spring Boot Result Starter.

## Unit Testing Results

### Basic Result Testing

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void findById_WhenUserExists_ReturnsSuccess() {
        // Given
        Long userId = 1L;
        User user = new User("John Doe", "john@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When
        Result<User> result = userService.findById(userId);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(user);
        assertThat(result.getError()).isNull();
    }
    
    @Test
    void findById_WhenUserNotExists_ReturnsNotFoundError() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When
        Result<User> result = userService.findById(userId);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getData()).isNull();
        assertThat(result.getError()).isInstanceOf(EntityNotFoundError.class);
        assertThat(result.getError().getMessage()).contains("User not found");
    }
}
```

### Testing Validation Chains

```java
@Test
void createUser_WithInvalidData_ReturnsValidationError() {
    // Given
    CreateUserRequest request = new CreateUserRequest("", "invalid-email");
    
    // When
    Result<User> result = userService.createUser(request);
    
    // Then
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getError()).isInstanceOf(ValidationError.class);
    // Test stops at first validation failure
    assertThat(result.getError().getMessage()).isEqualTo("Name is required");
}

@Test
void createUser_WithValidData_ReturnsSuccess() {
    // Given
    CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com");
    User savedUser = new User("John Doe", "john@example.com");
    
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    
    // When
    Result<User> result = userService.createUser(request);
    
    // Then
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData().getName()).isEqualTo("John Doe");
    assertThat(result.getData().getEmail()).isEqualTo("john@example.com");
}
```

## Integration Testing

### Testing Controllers with Results

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void getUser_WhenUserExists_ReturnsSuccessResponse() {
        // Given
        User user = userRepository.save(new User("John Doe", "john@example.com"));
        
        // When
        ResponseEntity<ResponseWrapper> response = restTemplate.getForEntity(
            "/api/users/" + user.getId(), 
            ResponseWrapper.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).contains("successfully");
        assertThat(response.getBody().getData()).isNotNull();
    }
    
    @Test
    void getUser_WhenUserNotExists_ReturnsNotFoundResponse() {
        // When
        ResponseEntity<ResponseWrapper> response = restTemplate.getForEntity(
            "/api/users/999", 
            ResponseWrapper.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("not found");
        assertThat(response.getBody().getData()).isNull();
    }
}
```

### Testing Async Operations

```java
@Test
void getUserAsync_ReturnsCompletedFuture() throws Exception {
    // Given
    User user = new User("John Doe", "john@example.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    
    // When
    CompletableFuture<Result<User>> futureResult = Result.async(() -> userService.findById(1L));
    
    // Then
    Result<User> result = futureResult.get(5, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isEqualTo(user);
}
```

## Testing with MockMvc

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Test
    void getUser_WhenUserExists_ReturnsOk() throws Exception {
        // Given
        User user = new User("John Doe", "john@example.com");
        when(userService.findById(1L)).thenReturn(Result.success(user));
        
        // When & Then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("John Doe"))
            .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }
    
    @Test
    void getUser_WhenUserNotExists_ReturnsNotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Result.entityNotFoundError("User not found"));
        
        // When & Then
        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User not found"))
            .andExpect(jsonPath("$.data").isEmpty());
    }
}
```

## Testing Event Publishing

```java
@SpringBootTest
class EventPublishingTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @MockBean
    private UserRepository userRepository;
    
    @EventListener
    @Component
    static class TestEventListener {
        private final List<ResultEvent<?>> receivedEvents = new ArrayList<>();
        
        @EventListener
        public void handleResultEvent(ResultEvent<?> event) {
            receivedEvents.add(event);
        }
        
        public List<ResultEvent<?>> getReceivedEvents() {
            return receivedEvents;
        }
        
        public void clear() {
            receivedEvents.clear();
        }
    }
    
    @Autowired
    private TestEventListener eventListener;
    
    @Test
    void createUser_OnSuccess_PublishesEvent() {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com");
        User savedUser = new User("John Doe", "john@example.com");
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        eventListener.clear();
        
        // When
        Result<User> result = userService.createUser(request);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(eventListener.getReceivedEvents()).hasSize(1);
        
        ResultEvent<?> event = eventListener.getReceivedEvents().get(0);
        assertThat(event.getEventName()).isEqualTo("user-created");
        assertThat(event.isSuccess()).isTrue();
    }
}
```

## Testing Transaction Rollback

```java
@SpringBootTest
@Transactional
class TransactionRollbackTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void createUser_WhenValidationFails_RollsBackTransaction() {
        // Given
        CreateUserRequest request = new CreateUserRequest("", "john@example.com");
        long initialCount = userRepository.count();
        
        // When
        Result<User> result = userService.createUser(request);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(userRepository.count()).isEqualTo(initialCount); // No new user saved
    }
}
```

## Custom Test Utilities

### Result Matchers

```java
public class ResultMatchers {
    
    public static <T> Matcher<Result<T>> isSuccess() {
        return new TypeSafeMatcher<Result<T>>() {
            @Override
            protected boolean matchesSafely(Result<T> result) {
                return result.isSuccess();
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText("a successful Result");
            }
        };
    }
    
    public static <T> Matcher<Result<T>> isFailure() {
        return new TypeSafeMatcher<Result<T>>() {
            @Override
            protected boolean matchesSafely(Result<T> result) {
                return !result.isSuccess();
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText("a failed Result");
            }
        };
    }
    
    public static <T> Matcher<Result<T>> hasErrorType(Class<? extends Error> errorType) {
        return new TypeSafeMatcher<Result<T>>() {
            @Override
            protected boolean matchesSafely(Result<T> result) {
                return !result.isSuccess() && errorType.isInstance(result.getError());
            }
            
            @Override
            public void describeTo(Description description) {
                description.appendText("a Result with error type " + errorType.getSimpleName());
            }
        };
    }
}
```

### Usage with Custom Matchers

```java
@Test
void findById_WhenUserNotExists_ReturnsEntityNotFoundError() {
    // Given
    when(userRepository.findById(999L)).thenReturn(Optional.empty());
    
    // When
    Result<User> result = userService.findById(999L);
    
    // Then
    assertThat(result, isFailure());
    assertThat(result, hasErrorType(EntityNotFoundError.class));
}
```

## Performance Testing

```java
@Test
void resultPattern_PerformanceComparison() {
    int iterations = 100_000;
    
    // Test Result pattern performance
    long startTime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
        Result<String> result = Result.entityNotFoundError("Not found");
        if (!result.isSuccess()) {
            // Handle error
        }
    }
    long resultTime = System.nanoTime() - startTime;
    
    // Test exception performance
    startTime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
        try {
            throw new RuntimeException("Not found");
        } catch (RuntimeException e) {
            // Handle error
        }
    }
    long exceptionTime = System.nanoTime() - startTime;
    
    System.out.println("Result pattern: " + resultTime / 1_000_000 + "ms");
    System.out.println("Exception pattern: " + exceptionTime / 1_000_000 + "ms");
    
    // Result pattern should be significantly faster
    assertThat(resultTime).isLessThan(exceptionTime / 2);
}
```

## Best Practices for Testing

### 1. Test Both Success and Failure Cases
Always test both the happy path and error scenarios.

### 2. Use Descriptive Test Names
```java
// Good
void createUser_WhenEmailAlreadyExists_ReturnsEntityAlreadyExistsError()

// Bad  
void testCreateUser()
```

### 3. Test Validation Chains Thoroughly
```java
@ParameterizedTest
@ValueSource(strings = {"", " ", "a", "very-long-name-that-exceeds-fifty-characters-limit"})
void createUser_WithInvalidName_ReturnsValidationError(String invalidName) {
    CreateUserRequest request = new CreateUserRequest(invalidName, "john@example.com");
    Result<User> result = userService.createUser(request);
    assertThat(result, isFailure());
    assertThat(result, hasErrorType(ValidationError.class));
}
```

### 4. Test Async Operations with Timeouts
```java
@Test
@Timeout(5)
void asyncOperation_CompletesWithinTimeout() throws Exception {
    CompletableFuture<Result<User>> future = userService.createUserAsync(request);
    Result<User> result = future.get();
    assertThat(result, isSuccess());
}
```