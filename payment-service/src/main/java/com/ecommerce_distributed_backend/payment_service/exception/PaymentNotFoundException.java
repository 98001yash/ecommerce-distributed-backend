package com.ecommerce_distributed_backend.payment_service.exception;

public class PaymentNotFoundException extends PaymentException {


    public PaymentNotFoundException(Long paymentId) {
        super("Payment not found with id: " + paymentId);
    }

    public PaymentNotFoundException(String orderId) {
        super("Payment not found for orderId: " + orderId);
    }
}
