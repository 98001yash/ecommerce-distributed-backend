package com.ecommerce_distributed_system.order_service.exception;

public class OrderNotFoundException extends RuntimeException {


    public OrderNotFoundException(String orderId) {
        super("Order not found with id: "+orderId);
    }
}
