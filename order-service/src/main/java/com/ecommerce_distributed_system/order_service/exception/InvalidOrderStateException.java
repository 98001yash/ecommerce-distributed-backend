package com.ecommerce_distributed_system.order_service.exception;

public class InvalidOrderStateException extends RuntimeException {


    public InvalidOrderStateException(String message) {
        super(message);
    }
}
