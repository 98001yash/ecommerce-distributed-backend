package com.ecommerce_distributed_backend.payment_service.service.Impl;

import com.ecommerce_distributed_backend.payment_service.dtos.CreatePaymentRequest;
import com.ecommerce_distributed_backend.payment_service.dtos.PaymentResponse;
import com.ecommerce_distributed_backend.payment_service.entities.Payment;
import com.ecommerce_distributed_backend.payment_service.enums.PaymentStatus;
import com.ecommerce_distributed_backend.payment_service.exception.InvalidPaymentStateException;
import com.ecommerce_distributed_backend.payment_service.exception.PaymentNotFoundException;
import com.ecommerce_distributed_backend.payment_service.exception.PaymentProcessingException;
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

        log.info(" Payment created → paymentId={}, status=INITIATED",
                payment.getId());

        //  Process payment
        processPayment(payment.getId());
    }


    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {

        log.info("Creating payment for orderId={}, amount={}",
                request.getOrderId(), request.getAmount());

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .status(PaymentStatus.INITIATED)
                .transactionId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .build();

        paymentRepository.save(payment);
        return mapToResponse(payment);
    }

    @Override
    @Transactional
    public void processPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (payment.getStatus() != PaymentStatus.INITIATED) {
            throw new InvalidPaymentStateException(
                    "Cannot process payment in state: " + payment.getStatus()
            );
        }

        log.info(" Processing payment → paymentId={}", paymentId);

        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);

        try {
            //  MOCK PAYMENT LOGIC (simulate success/failure)
            boolean success = simulatePayment();

            if (success) {
                handlePaymentSuccess(paymentId);
            } else {
                handlePaymentFailure(paymentId, "Mock payment failure");
            }

        } catch (Exception ex) {
            throw new PaymentProcessingException(
                    "Error processing payment for paymentId=" + paymentId, ex
            );
        }
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            throw new InvalidPaymentStateException(
                    "Cannot mark success for state: " + payment.getStatus()
            );
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);

        log.info(" Payment SUCCESS → paymentId={}, orderId={}",
                paymentId, payment.getOrderId());

        // 🔥 TODO: Publish PaymentCompletedEvent
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
