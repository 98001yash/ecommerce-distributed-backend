package com.ecommerce_distributed_backend.inventory_service.exception;

public class InventoryNotFoundException extends InventoryException {
    public InventoryNotFoundException(Long productId) {
        super("Inventory Not Found for productId: "+productId);
    }
}
