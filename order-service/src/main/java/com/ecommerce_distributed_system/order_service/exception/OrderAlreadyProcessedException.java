package com.ecommerce_distributed_system.order_service.exception;

public class OrderAlreadyProcessedException extends RuntimeException {
    public OrderAlreadyProcessedException(Long orderId) {
        super("Order already processed: " + orderId);
    }
}
