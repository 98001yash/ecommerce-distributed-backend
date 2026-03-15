package com.ecommerce_distributed_backend.inventory_service.exception;

public class InsufficientStockException extends InventoryException {


    public InsufficientStockException(Long productId, int requestedBody) {
        super("Insufficient stock for productId: "+productId + "requested: "+requestedBody);
    }
}
