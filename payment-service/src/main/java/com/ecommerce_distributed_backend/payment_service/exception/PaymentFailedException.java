package com.ecommerce_distributed_backend.payment_service.exception;

public class PaymentFailedException extends PaymentException {

    public PaymentFailedException(String message) {
        super(message);
    }
}