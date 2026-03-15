package com.ecommerce_distributed_backend.inventory_service.repository;

import com.ecommerce_distributed_backend.inventory_service.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryEventRepository extends JpaRepository<InventoryReservation, Long> {


    boolean existsByEventId(Long eventId);
}
