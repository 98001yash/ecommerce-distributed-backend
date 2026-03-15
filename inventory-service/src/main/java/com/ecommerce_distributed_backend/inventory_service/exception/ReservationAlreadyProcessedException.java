package com.ecommerce_distributed_backend.inventory_service.exception;

public class ReservationAlreadyProcessedException extends InventoryException{


    public ReservationAlreadyProcessedException(Long reservationId) {
        super("Reservation already processed. reservationId: " + reservationId);
    }
}
