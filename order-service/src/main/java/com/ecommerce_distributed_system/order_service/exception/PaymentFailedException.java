package com.ecommerce_distributed_system.order_service.exception;

public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(Long orderId) {
        super("Payment failed for orderId: " + orderId);
    }
}