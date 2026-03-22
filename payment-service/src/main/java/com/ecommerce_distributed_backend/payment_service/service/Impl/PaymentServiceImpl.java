package com.ecommerce_distributed_backend.payment_service.service.Impl;

import com.ecommerce_distributed_backend.payment_service.dtos.CreatePaymentRequest;
import com.ecommerce_distributed_backend.payment_service.dtos.PaymentResponse;
import com.ecommerce_distributed_backend.payment_service.entities.Payment;
import com.ecommerce_distributed_backend.payment_service.enums.PaymentStatus;
import com.ecommerce_distributed_backend.payment_service.repository.PaymentRepository;
import com.ecommerce_distributed_backend.payment_service.service.PaymentService;
import com.redditApp.events.StockReservedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;


    @Override
    @Transactional
    public void handleStockReserved(StockReservedEvent event) {

        log.info("Received StockReservedEvent → orderId={}, amount will be calculated",
                event.getOrderId());

        //  Check idempotency (avoid duplicate payments)
        paymentRepository.findByOrderId(event.getOrderId())
                .ifPresent(existing -> {
                    log.warn(" Payment already exists for orderId={}", event.getOrderId());
                    return;
                });

        //  Create payment
        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .amount(calculateAmount(event))
                .status(PaymentStatus.INITIATED)
                .transactionId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .build();

        paymentRepository.save(payment);

        log.info("💰 Payment created → paymentId={}, status=INITIATED",
                payment.getId());

        // 🔥 Process payment
        processPayment(payment.getId());
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
