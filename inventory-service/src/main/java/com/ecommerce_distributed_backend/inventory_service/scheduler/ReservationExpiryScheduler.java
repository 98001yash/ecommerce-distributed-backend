package com.ecommerce_distributed_backend.inventory_service.scheduler;


import com.ecommerce_distributed_backend.inventory_service.entity.Inventory;
import com.ecommerce_distributed_backend.inventory_service.entity.InventoryReservation;
import com.ecommerce_distributed_backend.inventory_service.entity.enums.ReservationStatus;
import com.ecommerce_distributed_backend.inventory_service.repository.InventoryRepository;
import com.ecommerce_distributed_backend.inventory_service.repository.InventoryReservationRepository;
import com.ecommerce_distributed_backend.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiryScheduler {

    private final InventoryReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Runs every minute to clean expired reservations
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredReservations() {

        log.info("Running expired reservation cleanup job");

        List<InventoryReservation> expiredReservations =
                reservationRepository.findByStatusAndExpiresAtBefore(
                        ReservationStatus.RESERVED,
                        LocalDateTime.now()
                );

        if (expiredReservations.isEmpty()) {

            log.info("No expired reservations found");
            return;
        }

        for (InventoryReservation reservation : expiredReservations) {

            log.info("Releasing expired reservation id={}, orderId={}",
                    reservation.getId(),
                    reservation.getOrderId());

            Inventory inventory = inventoryRepository
                    .findByProductId(reservation.getProductId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Inventory not found for product " +
                                            reservation.getProductId()
                            )
                    );

            // restore stock
            inventory.setReservedQuantity(
                    inventory.getReservedQuantity() - reservation.getQuantity()
            );

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() + reservation.getQuantity()
            );

            inventoryRepository.save(inventory);

            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            log.info("Expired reservation released successfully. reservationId={}",
                    reservation.getId());
        }

        log.info("Expired reservation cleanup completed. processed={}",
                expiredReservations.size());
    }
}
