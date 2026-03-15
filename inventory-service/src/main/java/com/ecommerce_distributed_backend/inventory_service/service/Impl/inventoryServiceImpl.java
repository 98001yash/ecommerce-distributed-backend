package com.ecommerce_distributed_backend.inventory_service.service.Impl;


import com.ecommerce_distributed_backend.inventory_service.auth.UserContextHolder;
import com.ecommerce_distributed_backend.inventory_service.dtos.*;
import com.ecommerce_distributed_backend.inventory_service.entity.Inventory;
import com.ecommerce_distributed_backend.inventory_service.entity.InventoryReservation;
import com.ecommerce_distributed_backend.inventory_service.entity.enums.ReservationStatus;
import com.ecommerce_distributed_backend.inventory_service.exception.InsufficientStockException;
import com.ecommerce_distributed_backend.inventory_service.exception.InventoryNotFoundException;
import com.ecommerce_distributed_backend.inventory_service.repository.InventoryRepository;
import com.ecommerce_distributed_backend.inventory_service.repository.InventoryReservationRepository;
import com.ecommerce_distributed_backend.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class inventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;


    @Override
    @Transactional
    public ReserveStockResponse reserveStock(ReserveStockRequest request) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} requesting stock reservation, productId={},quantity={}",
                userId, request.getProductId(), request.getQuantity());

        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(request.getProductId(),
                request.getWarehouseId()).orElseThrow(()->
                new InventoryNotFoundException(request.getProductId()));

        if(inventory.getAvailableQuantity() < request.getQuantity()){

            log.warn("Insufficient stock for product {}, available={}," +
                    "requested={}",request.getProductId(),
                    inventory.getAvailableQuantity(), request.getQuantity());

            throw new InsufficientStockException(request.getProductId(), request.getQuantity());
        }

        // update inventory quantities
        inventory.setAvailableQuantity(inventory.getAvailableQuantity()- request.getQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.getQuantity());

        inventoryRepository.save(inventory);
        log.info("inventory updated , productId={}, available={}, reserved={}",
                inventory.getProductId(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity());

        // create reservation
        InventoryReservation reservation = InventoryReservation.builder()
                .orderId(request.getOrderId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .status(ReservationStatus.RESERVED)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        reservationRepository.save(reservation);

        log.info("Stock reserved successfully , reservationId={}, orderId={}",reservation.getId(),
                reservation.getOrderId());


        return ReserveStockResponse.builder()
                .reservationId(reservation.getId())
                .productId(reservation.getProductId())
                .reservedQuantity(reservation.getQuantity())
                .status(reservation.getStatus().name())
                .build();
    }

    @Override
    public void confirmReservation(ConfirmReservationRequest request) {

    }

    @Override
    public void releaseReservation(ReleaseReservationRequest request) {

    }

    @Override
    public InventoryResponse getInventory(Long productId) {
        return null;
    }

    @Override
    public void addStock(Long productId, Long warehouseId, Integer quantity) {

    }

    @Override
    public void removeStock(Long productId, Long warehouseId, Integer quantity) {

    }
}
