package com.ecommerce_distributed_backend.inventory_service.dtos;


import lombok.Data;

@Data
public class ConfirmReservationRequest {


    private Long orderId;
    private Long productId;
}
