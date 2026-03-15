package com.ecommerce_distributed_backend.inventory_service.kafka;


import com.redditApp.events.StockConfirmedEvent;
import com.redditApp.events.StockExpiredEvent;
import com.redditApp.events.StockReleasedEvent;
import com.redditApp.events.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    // STOCK RESERVED

    public void publishStockReservedEvent(StockReservedEvent event) {

        log.info("Publishing StockReservedEvent. orderId={}, productId={}, quantity={}",
                event.getOrderId(),
                event.getProductId(),
                event.getQuantity());

        kafkaTemplate.send(
                InventoryTopics.STOCK_RESERVED,
                String.valueOf(event.getOrderId()),
                event
        );

        log.info("StockReservedEvent published successfully. topic={}",
                InventoryTopics.STOCK_RESERVED);
    }


    // STOCK RELEASED

    public void publishStockReleasedEvent(StockReleasedEvent event) {

        log.info("Publishing StockReleasedEvent. orderId={}, productId={}, quantity={}",
                event.getOrderId(),
                event.getProductId(),
                event.getQuantity());

        kafkaTemplate.send(
                InventoryTopics.STOCK_RELEASED,
                String.valueOf(event.getOrderId()),
                event
        );

        log.info("StockReleasedEvent published successfully. topic={}",
                InventoryTopics.STOCK_RELEASED);
    }


    // STOCK CONFIRMED
    public void publishStockConfirmedEvent(StockConfirmedEvent event) {

        log.info("Publishing StockConfirmedEvent. orderId={}, productId={}, quantity={}",
                event.getOrderId(),
                event.getProductId(),
                event.getQuantity());

        kafkaTemplate.send(
                InventoryTopics.STOCK_CONFIRMED,
                String.valueOf(event.getOrderId()),
                event
        );

        log.info("StockConfirmedEvent published successfully. topic={}",
                InventoryTopics.STOCK_CONFIRMED);
    }


    // STOCK EXPIRED

    public void publishStockExpiredEvent(StockExpiredEvent event) {

        log.info("Publishing StockExpiredEvent. reservationId={}, productId={}, quantity={}",
                event.getReservationId(),
                event.getProductId(),
                event.getQuantity());

        kafkaTemplate.send(
                InventoryTopics.STOCK_EXPIRED,
                String.valueOf(event.getReservationId()),
                event
        );

        log.info("StockExpiredEvent published successfully. topic={}",
                InventoryTopics.STOCK_EXPIRED);
    }
}
