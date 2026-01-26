---
title: Validation Chains
sidebar_position: 1
---

# Validation Chains

Chain multiple validations together with early termination on first failure.

## Basic Validation

```java
public Result<User> validateUser(User user) {
    return Result.success(user)
        .validate(u -> u.getName() != null, "Name is required")
        .validate(u -> u.getEmail() != null, "Email is required")
        .validate(u -> u.getEmail().contains("@"), "Invalid email format");
}
```

## Complex Business Rules

```java
public Result<Order> validateOrder(Order order) {
    return Result.success(order)
        .validate(o -> o.getItems().size() > 0, "Order must have items")
        .validate(o -> o.getTotalAmount().compareTo(BigDecimal.ZERO) > 0, "Total must be positive")
        .validate(o -> o.getItems().size() <= 50, "Too many items")
        .flatMap(this::validateInventory)
        .flatMap(this::validateCustomer);
}

private Result<Order> validateInventory(Order order) {
    for (OrderItem item : order.getItems()) {
        if (!inventoryService.isAvailable(item.getProductId())) {
            return Result.validationError("Product " + item.getProductId() + " not available");
        }
    }
    return Result.success(order);
}
```