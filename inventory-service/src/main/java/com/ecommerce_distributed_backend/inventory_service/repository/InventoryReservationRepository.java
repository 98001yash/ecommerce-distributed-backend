package com.ecommerce_distributed_backend.inventory_service.repository;

import com.ecommerce_distributed_backend.inventory_service.entity.InventoryReservation;
import com.ecommerce_distributed_backend.inventory_service.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {


    Optional<InventoryReservation> findByOrderIdAndProductId(Long orderId, Long productId);

    List<InventoryReservation> findByStatusAndExpiresAtBefore(
            ReservationStatus status,
            LocalDateTime time
    );
}
