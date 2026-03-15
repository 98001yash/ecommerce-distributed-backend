package com.ecommerce_distributed_backend.inventory_service.dtos;


import lombok.Data;

@Data
public class ReleaseReservationRequest {

    private Long orderId;

    private Long productId;
}
