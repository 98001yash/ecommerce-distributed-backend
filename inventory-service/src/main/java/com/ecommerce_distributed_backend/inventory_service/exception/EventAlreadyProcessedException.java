package com.ecommerce_distributed_backend.inventory_service.exception;

public class EventAlreadyProcessedException extends InventoryException {

    public EventAlreadyProcessedException(Long eventId) {
        super("Event already processed: " + eventId);
    }

}
