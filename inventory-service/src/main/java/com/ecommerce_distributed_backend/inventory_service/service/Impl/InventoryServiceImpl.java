package com.ecommerce_distributed_backend.inventory_service.service.Impl;



import com.ecommerce_distributed_backend.inventory_service.auth.UserContextHolder;
import com.ecommerce_distributed_backend.inventory_service.dtos.*;
import com.ecommerce_distributed_backend.inventory_service.entity.Inventory;
import com.ecommerce_distributed_backend.inventory_service.entity.InventoryReservation;
import com.ecommerce_distributed_backend.inventory_service.entity.enums.ReservationStatus;
import com.ecommerce_distributed_backend.inventory_service.exception.*;
import com.ecommerce_distributed_backend.inventory_service.kafka.InventoryEventProducer;
import com.ecommerce_distributed_backend.inventory_service.repository.InventoryRepository;
import com.ecommerce_distributed_backend.inventory_service.repository.InventoryReservationRepository;
import com.ecommerce_distributed_backend.inventory_service.service.InventoryService;

import com.redditApp.events.StockConfirmedEvent;
import com.redditApp.events.StockReleasedEvent;
import com.redditApp.events.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryEventProducer inventoryEventProducer;

    private static final int RESERVATION_TIMEOUT_MINUTES = 15;


    // RESERVE STOCK

    @Override
    @Transactional
    public ReserveStockResponse reserveStock(ReserveStockRequest request) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} requesting stock reservation. productId={}, quantity={}",
                userId, request.getProductId(), request.getQuantity());

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(
                        request.getProductId(),
                        request.getWarehouseId())
                .orElseThrow(() ->
                        new InventoryNotFoundException(request.getProductId())
                );

        if (inventory.getAvailableQuantity() < request.getQuantity()) {

            log.warn("Insufficient stock for product {}. available={}, requested={}",
                    request.getProductId(),
                    inventory.getAvailableQuantity(),
                    request.getQuantity());

            throw new InsufficientStockException(
                    request.getProductId(),
                    request.getQuantity()
            );
        }

        // update inventory
        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() - request.getQuantity()
        );

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + request.getQuantity()
        );

        inventoryRepository.save(inventory);

        log.info("Inventory updated productId={} available={} reserved={}",
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
                .expiresAt(LocalDateTime.now().plusMinutes(RESERVATION_TIMEOUT_MINUTES))
                .build();

        reservationRepository.save(reservation);

        log.info("Stock reserved successfully reservationId={} orderId={}",
                reservation.getId(),
                reservation.getOrderId());

        // publish Kafka event
        StockReservedEvent event = StockReservedEvent.builder()
                .eventId(System.currentTimeMillis())
                .orderId(reservation.getOrderId())
                .productId(reservation.getProductId())
                .quantity(reservation.getQuantity())
                .warehouseId(request.getWarehouseId())
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .build();

        inventoryEventProducer.publishStockReservedEvent(event);

        log.info("StockReservedEvent published orderId={} productId={}",
                reservation.getOrderId(),
                reservation.getProductId());

        return ReserveStockResponse.builder()
                .reservationId(reservation.getId())
                .productId(reservation.getProductId())
                .reservedQuantity(reservation.getQuantity())
                .status(reservation.getStatus().name())
                .build();
    }


    // CONFIRM RESERVATION

    @Override
    @Transactional
    public void confirmReservation(@org.checkerframework.checker.nullness.qual.MonotonicNonNull Long request) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} confirming reservation orderId={} productId={}",
                userId,
                request.getOrderId(),
                request.getProductId());

        InventoryReservation reservation = reservationRepository
                .findByOrderIdAndProductId(
                        request.getOrderId(),
                        request.getProductId())
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                request.getOrderId(),
                                request.getProductId())
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

        log.info("Reservation confirmed orderId={} productId={}",
                reservation.getOrderId(),
                reservation.getProductId());

        // publish event
        StockConfirmedEvent event = StockConfirmedEvent.builder()
                .eventId(System.currentTimeMillis())
                .orderId(reservation.getOrderId())
                .productId(reservation.getProductId())
                .quantity(reservation.getQuantity())
                .warehouseId(inventory.getWarehouseId())
                .timestamp(System.currentTimeMillis())
                .build();

        inventoryEventProducer.publishStockConfirmedEvent(event);

        log.info("StockConfirmedEvent published orderId={} productId={}",
                reservation.getOrderId(),
                reservation.getProductId());
    }


    // RELEASE RESERVATION

    @Override
    @Transactional
    public void releaseReservation(@org.checkerframework.checker.nullness.qual.MonotonicNonNull Long request) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} releasing reservation orderId={} productId={}",
                userId,
                request.getOrderId(),
                request.getProductId());

        InventoryReservation reservation = reservationRepository
                .findByOrderIdAndProductId(
                        request.getOrderId(),
                        request.getProductId())
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                request.getOrderId(),
                                request.getProductId())
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

        log.info("Reservation released orderId={} productId={}",
                reservation.getOrderId(),
                reservation.getProductId());

        // publish event
        StockReleasedEvent event = StockReleasedEvent.builder()
                .eventId(System.currentTimeMillis())
                .orderId(reservation.getOrderId())
                .productId(reservation.getProductId())
                .quantity(reservation.getQuantity())
                .warehouseId(inventory.getWarehouseId())
                .timestamp(System.currentTimeMillis())
                .build();

        inventoryEventProducer.publishStockReleasedEvent(event);

        log.info("StockReleasedEvent published orderId={} productId={}",
                reservation.getOrderId(),
                reservation.getProductId());
    }


    // GET INVENTORY

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


    // ADD STOCK

    @Override
    @Transactional
    public void addStock(Long productId, Long warehouseId, Integer quantity) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} adding stock productId={} quantity={}",
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

        log.info("Stock added productId={} newAvailable={}",
                productId,
                inventory.getAvailableQuantity());
    }


    // REMOVE STOCK


    @Override
    @Transactional
    public void removeStock(Long productId, Long warehouseId, Integer quantity) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} removing stock productId={} quantity={}",
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

        log.info("Stock removed productId={} newAvailable={}",
                productId,
                inventory.getAvailableQuantity());
    }
}