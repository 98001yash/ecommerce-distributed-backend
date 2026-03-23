package com.ecommerce_distributed_backend.inventory_service.kafka;

import com.ecommerce_distributed_backend.inventory_service.dtos.ReserveStockRequest;
import com.ecommerce_distributed_backend.inventory_service.service.InventoryService;
import com.redditApp.events.OrderCreatedEvent;
import com.redditApp.events.StockConfirmedEvent;
import com.redditApp.events.StockReleasedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "order-created",
            containerFactory = "inventoryFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {

        log.info("📥 Received OrderCreatedEvent → orderId={}, productId={}, quantity={}",
                event.getOrderId(),
                event.getProductId(),
                event.getQuantity()
        );

        try {
            //  MAP EVENT → REQUEST DTO
            ReserveStockRequest request = ReserveStockRequest.builder()
                    .orderId(event.getOrderId())
                    .productId(event.getProductId())
                    .quantity(event.getQuantity())
                    .warehouseId(1L) // ⚠TEMP (or dynamic later)
                    .build();

            //  CALL YOUR EXISTING METHOD
            inventoryService.reserveStock(request);

            log.info(" Stock reservation successful for orderId={}", event.getOrderId());

        } catch (Exception ex) {
            log.error(" Stock reservation failed for orderId={}", event.getOrderId(), ex);

            //  IMPORTANT: Let Kafka retry / DLQ handle this
            throw ex;
        }
    }

    @KafkaListener(
            topics = "inventory-stock-confirmed",
            containerFactory = "inventoryFactory"
    )
    public void handleStockConfirmed(StockConfirmedEvent event) {

        log.info(" Received StockConfirmedEvent → orderId={}, productId={}",
                event.getOrderId(),
                event.getProductId()
        );

        try {
            inventoryService.confirmReservation(event.getOrderId());

            log.info(" Stock confirmed (RESERVED → SOLD) for orderId={}",
                    event.getOrderId());

        } catch (Exception ex) {
            log.error(" Error confirming stock for orderId={}",
                    event.getOrderId(), ex);

            throw ex; // retry / DLQ
        }
    }


    @KafkaListener(
            topics = "inventory-stock-released",
            containerFactory = "inventoryFactory"
    )
    public void handleStockReleased(StockReleasedEvent event) {

        log.info(" Received StockReleasedEvent → orderId={}, productId={}",
                event.getOrderId(),
                event.getProductId()
        );

        try {
            inventoryService.releaseReservation(event.getOrderId());

            log.info(" Stock released (RESERVED → AVAILABLE) for orderId={}",
                    event.getOrderId());

        } catch (Exception ex) {
            log.error(" Error releasing stock for orderId={}",
                    event.getOrderId(), ex);

            throw ex; // retry / DLQ
        }
    }
}
