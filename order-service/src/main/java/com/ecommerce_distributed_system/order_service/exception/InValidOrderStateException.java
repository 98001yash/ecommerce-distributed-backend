package com.ecommerce_distributed_system.order_service.exception;

public class InValidOrderStateException extends RuntimeException {


    public InValidOrderStateException(String message) {
        super(message);
    }
}
