package com.ecommerce_distributed_system.order_service.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderResponse {

    private Long orderId;

    private String status;

    private String message;
}
