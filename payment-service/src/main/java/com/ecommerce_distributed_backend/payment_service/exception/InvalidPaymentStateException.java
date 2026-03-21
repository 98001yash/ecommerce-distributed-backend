package com.ecommerce_distributed_backend.payment_service.exception;

public class InvalidPaymentStateException extends PaymentException {
    public InvalidPaymentStateException(String message) {
        super(message);
    }
}
