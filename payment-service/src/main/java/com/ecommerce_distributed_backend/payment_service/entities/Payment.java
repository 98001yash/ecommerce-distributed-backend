package com.ecommerce_distributed_backend.payment_service.entities;


import com.ecommerce_distributed_backend.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(unique = true, nullable = false)
    private String transactionId;


    private String paymentGateway;
    private String gatewayPaymentId;

    //  AUDIT
    @Column(nullable = false)
    private Instant createdAt;
    private Instant updatedAt;

    @Version
    private Long version;
}
