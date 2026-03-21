package com.ecommerce_distributed_backend.payment_service.exception;

public class PaymentProcessingException extends PaymentException {

    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}