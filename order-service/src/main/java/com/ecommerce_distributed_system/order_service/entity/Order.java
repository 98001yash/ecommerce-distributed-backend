package com.ecommerce_distributed_system.order_service.entity;


import com.ecommerce_distributed_system.order_service.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long productId;

    private Integer quantity;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;


    // SAGA / EXTERNAL REFERENCES
    private Long inventoryReservationId;

    private String paymentTransactionId;


    // AUDIT FIELDS
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    // OPTIMISTIC LOCKING (CRITICAL)
    @Version
    private Long version;



    // LIFECYCLE HOOKS
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = OrderStatus.CREATED;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setUpdatedAt(Instant now) {
    }
}
