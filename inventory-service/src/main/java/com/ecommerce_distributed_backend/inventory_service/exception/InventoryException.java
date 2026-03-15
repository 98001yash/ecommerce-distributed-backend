package com.ecommerce_distributed_backend.inventory_service.exception;

public class InventoryException extends RuntimeException {
    public InventoryException(String message) {
        super(message);
    }
}
