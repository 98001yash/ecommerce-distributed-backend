package com.ecommerce_distributed_backend.inventory_service.service;

import com.ecommerce_distributed_backend.inventory_service.dtos.*;

public interface InventoryService {

    /**
     * Reserve stock for an order.
     * Called by Order Service when order is created.
     */
    ReserveStockResponse reserveStock(ReserveStockRequest request);

    /**
     * Confirm reservation after successful payment.
     */
    void confirmReservation(ConfirmReservationRequest request);

    /**
     * Release reserved stock when order is cancelled.
     */
    void releaseReservation(ReleaseReservationRequest request);

    /**
     * Get inventory details for a product.
     */
    InventoryResponse getInventory(Long productId);

    /**
     * Add stock to inventory (admin / warehouse operation).
     */
    void addStock(Long productId, Long warehouseId, Integer quantity);

    /**
     * Remove stock manually (admin / warehouse correction).
     */
    void removeStock(Long productId, Long warehouseId, Integer quantity);
}
