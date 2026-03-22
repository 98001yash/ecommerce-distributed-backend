package com.ecommerce_distributed_backend.payment_service.service;

import com.ecommerce_distributed_backend.payment_service.dtos.CreatePaymentRequest;
import com.ecommerce_distributed_backend.payment_service.dtos.PaymentResponse;
import com.redditApp.events.StockReservedEvent;

public interface PaymentService {

    //  Triggered when inventory is reserved (Saga start for payment)
    void handleStockReserved(StockReservedEvent event);

    //  Create payment manually (optional API)
    PaymentResponse createPayment(CreatePaymentRequest request);

    //  Process payment (internal step)
    void processPayment(Long paymentId);

    //  Handle payment success
    void handlePaymentSuccess(Long paymentId);

    //  Handle payment failure
    void handlePaymentFailure(Long paymentId, String reason);

    //  Get payment by ID
    PaymentResponse getPayment(Long paymentId);

    //  Get payment by orderId
    PaymentResponse getPaymentByOrderId(Long orderId);
}
