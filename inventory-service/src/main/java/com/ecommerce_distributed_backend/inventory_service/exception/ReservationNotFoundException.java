package com.ecommerce_distributed_backend.inventory_service.exception;

public class ReservationNotFoundException extends InventoryException {


    public ReservationNotFoundException(Long orderId, Long productId) {
        super("Reservation not found for orderId: " + orderId +
                " productId: " + productId);
    }

}
