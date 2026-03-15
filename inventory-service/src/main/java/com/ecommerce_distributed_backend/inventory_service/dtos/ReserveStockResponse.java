package com.ecommerce_distributed_backend.inventory_service.dtos;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReserveStockResponse {


    private Long reservationId;

    private Long productId;

    private Integer reservedQuantity;

    private String status;
}
