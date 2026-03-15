package com.ecommerce_distributed_backend.inventory_service.service.Impl;


import com.ecommerce_distributed_backend.inventory_service.auth.UserContextHolder;
import com.ecommerce_distributed_backend.inventory_service.dtos.*;
import com.ecommerce_distributed_backend.inventory_service.entity.Inventory;
import com.ecommerce_distributed_backend.inventory_service.entity.InventoryReservation;
import com.ecommerce_distributed_backend.inventory_service.entity.enums.ReservationStatus;
import com.ecommerce_distributed_backend.inventory_service.exception.InsufficientStockException;
import com.ecommerce_distributed_backend.inventory_service.exception.InventoryNotFoundException;
import com.ecommerce_distributed_backend.inventory_service.exception.ReservationAlreadyProcessedException;
import com.ecommerce_distributed_backend.inventory_service.exception.ReservationNotFoundException;
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
    @Transactional
    public void confirmReservation(ConfirmReservationRequest request) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} confirming reservation. orderId={}, productId={}",
                userId,
                request.getOrderId(),
                request.getProductId());

        InventoryReservation reservation = reservationRepository
                .findByOrderIdAndProductId(
                        request.getOrderId(),
                        request.getProductId()
                )
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                request.getOrderId(),
                                request.getProductId()
                        )
                );

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new ReservationAlreadyProcessedException(reservation.getId());
        }

        Inventory inventory = inventoryRepository
                .findByProductId(reservation.getProductId())
                .orElseThrow(() ->
                        new InventoryNotFoundException(reservation.getProductId())
                );

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - reservation.getQuantity()
        );

        inventory.setSoldQuantity(
                inventory.getSoldQuantity() + reservation.getQuantity()
        );

        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.CONFIRMED);

        reservationRepository.save(reservation);
        log.info("Reservation confirmed. orderId={}, productId={}",
                reservation.getOrderId(),
                reservation.getProductId());
    }

    @Override
    @Transactional
    public void releaseReservation(ReleaseReservationRequest request) {

        Long userId = UserContextHolder.getCurrentUserId();
        log.info("User {} releasing reservation. orderId={}, productId={}",
                userId,
                request.getOrderId(),
                request.getProductId());

        InventoryReservation reservation = reservationRepository
                .findByOrderIdAndProductId(
                        request.getOrderId(),
                        request.getProductId()
                )
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                request.getOrderId(),
                                request.getProductId()
                        )
                );

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new ReservationAlreadyProcessedException(reservation.getId());
        }

        Inventory inventory = inventoryRepository
                .findByProductId(reservation.getProductId())
                .orElseThrow(() ->
                        new InventoryNotFoundException(reservation.getProductId())
                );

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - reservation.getQuantity()
        );

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + reservation.getQuantity()
        );

        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);
        log.info("Reservation released successfully. orderId={}, productId={}",
                reservation.getOrderId(),
                reservation.getProductId());
    }


    @Override
    public InventoryResponse getInventory(Long productId) {

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(productId)
                );

        return InventoryResponse.builder()
                .productId(inventory.getProductId())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .soldQuantity(inventory.getSoldQuantity())
                .build();
    }


    @Override
    @Transactional
    public void addStock(Long productId, Long warehouseId, Integer quantity) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} adding stock. productId={}, quantity={}",
                userId, productId, quantity);

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(productId)
                );

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + quantity
        );

        inventoryRepository.save(inventory);

        log.info("Stock added successfully. productId={}, newAvailable={}",
                productId,
                inventory.getAvailableQuantity());
    }

    @Override
    @Transactional
    public void removeStock(Long productId, Long warehouseId, Integer quantity) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} removing stock. productId={}, quantity={}",
                userId, productId, quantity);

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(productId)
                );

        if (inventory.getAvailableQuantity() < quantity) {

            throw new InsufficientStockException(productId, quantity);
        }

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() - quantity
        );

        inventoryRepository.save(inventory);

        log.info("Stock removed successfully. productId={}, newAvailable={}",
                productId,
                inventory.getAvailableQuantity());
    }
}
