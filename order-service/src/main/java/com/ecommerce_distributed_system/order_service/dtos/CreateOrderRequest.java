package com.ecommerce_distributed_system.order_service.dtos;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderRequest {

    private Long productId;

    private Integer quantity;

    private Long warehouseId;
}
