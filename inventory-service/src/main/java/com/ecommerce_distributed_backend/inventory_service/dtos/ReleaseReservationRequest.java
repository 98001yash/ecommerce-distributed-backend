package com.ecommerce_distributed_backend.inventory_service.dtos;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReleaseReservationRequest {

    private Long orderId;

    private Long productId;
}
