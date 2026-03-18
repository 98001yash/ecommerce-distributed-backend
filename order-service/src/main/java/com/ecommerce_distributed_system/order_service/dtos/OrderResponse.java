package com.ecommerce_distributed_system.order_service.dtos;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {

    private Long orderId;

    private Long userId;

    private Long productId;

    private Integer quantity;

    private BigDecimal totalAmount;

    private String status;

    private Long inventoryReservationId;

    private String paymentTransactionId;
}
