package com.ecommerce_distributed_system.order_service.kafka;

import com.ecommerce_distributed_system.order_service.service.OrderService;
import com.redditApp.events.StockExpiredEvent;
import com.redditApp.events.StockReleasedEvent;
import com.redditApp.events.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderService orderService;

    // STOCK RESERVED
    @KafkaListener(
            topics = "inventory-stock-reserved",
            containerFactory = "orderKafkaListenerFactory"
    )
    public void handleStockReserved(StockReservedEvent event) {

        log.info("Received StockReservedEvent orderId={}", event.getOrderId());

        orderService.handleStockReserved(event);
    }


    // STOCK RELEASED
    @KafkaListener(
            topics = "inventory-stock-released",
            containerFactory = "orderKafkaListenerFactory"
    )
    public void handleStockReleased(StockReleasedEvent event) {

        log.info("Received StockReleasedEvent orderId={}", event.getOrderId());

        orderService.handleStockReleased(event);
    }



    // STOCK EXPIRED

    @KafkaListener(
            topics = "inventory-stock-expired",
            containerFactory = "orderKafkaListenerFactory"
    )
    public void handleStockExpired(StockExpiredEvent event) {

        log.info("Received StockExpiredEvent orderId={}", event.getOrderId());

        orderService.handleStockExpired(event);
    }
}
