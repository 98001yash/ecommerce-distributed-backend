package com.ecommerce_distributed_backend.inventory_service.repository;

import com.ecommerce_distributed_backend.inventory_service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    Optional<Inventory> findByProductId(Long productId);
}
