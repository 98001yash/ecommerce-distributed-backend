package com.ecommerce_distributed_backend.payment_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {

    private Long paymentId;
    private Long orderId;
    private Long userId;

    private BigDecimal amount;
    private String status;

    private String transactionId;
    private String paymentGateway;

    private String gatewayPaymentId;

    private Instant createdAt;
    private Instant updatedAt;
}
