package com.ecommerce_distributed_backend.inventory_service.dtos;


import lombok.Data;

@Data
public class ReserveStockRequest {

    private Long orderId;

    private Long productId;

    private Integer quantity;

    private Long warehouseId;
}
