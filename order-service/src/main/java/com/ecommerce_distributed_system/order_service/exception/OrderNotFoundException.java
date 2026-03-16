package com.ecommerce_distributed_system.order_service.exception;

public class OrderNotFoundException extends RuntimeException {


    public OrderNotFoundException(String message) {
        super(message);
    }
}
