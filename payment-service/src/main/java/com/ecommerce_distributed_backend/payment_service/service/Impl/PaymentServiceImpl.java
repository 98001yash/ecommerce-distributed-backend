package com.ecommerce_distributed_backend.payment_service.service.Impl;

import com.ecommerce_distributed_backend.payment_service.dtos.CreatePaymentRequest;
import com.ecommerce_distributed_backend.payment_service.dtos.PaymentResponse;
import com.ecommerce_distributed_backend.payment_service.entities.Payment;
import com.ecommerce_distributed_backend.payment_service.service.PaymentService;
import com.redditApp.events.StockReservedEvent;

import java.math.BigDecimal;

public class PaymentServiceImpl implements PaymentService {


    @Override
    public void handleStockReserved(StockReservedEvent event) {

    }

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        return null;
    }

    @Override
    public void processPayment(Long paymentId) {

    }

    @Override
    public void handlePaymentSuccess(Long paymentId) {

    }

    @Override
    public void handlePaymentFailure(Long paymentId, String reason) {

    }

    @Override
    public PaymentResponse getPayment(Long paymentId) {
        return null;
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        return null;
    }



    //  HELPER METHODS

    private BigDecimal calculateAmount(StockReservedEvent event) {
        //  MOCK: later fetch from product service
        return BigDecimal.valueOf(event.getQuantity() * 100);
    }

    private boolean simulatePayment() {
        return Math.random() > 0.2; // 80% success
    }

    private PaymentResponse mapToResponse(Payment payment) {

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .transactionId(payment.getTransactionId())
                .paymentGateway(payment.getPaymentGateway())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
