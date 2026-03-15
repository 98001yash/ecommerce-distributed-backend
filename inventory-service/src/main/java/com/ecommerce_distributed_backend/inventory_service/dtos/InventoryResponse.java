package com.ecommerce_distributed_backend.inventory_service.dtos;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryResponse {

    private Long productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer soldQuantity;
}
