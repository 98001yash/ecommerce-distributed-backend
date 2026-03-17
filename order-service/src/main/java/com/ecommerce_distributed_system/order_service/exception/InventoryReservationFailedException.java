package com.ecommerce_distributed_system.order_service.exception;

public class InventoryReservationFailedException extends RuntimeException {
    public InventoryReservationFailedException(Long productId, Integer quantity) {

        super("Failed to reserve inventory for productId=" + productId +
                ", quantity=" + quantity);
    }
}
