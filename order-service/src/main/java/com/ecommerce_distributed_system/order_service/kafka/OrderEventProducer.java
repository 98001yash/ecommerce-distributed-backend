package com.ecommerce_distributed_system.order_service.kafka;


import com.redditApp.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    //  ORDER CREATED
    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for orderId={}", event.getOrderId());
        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, event.getOrderId().toString(), event);
    }

    // 🔥 ORDER CANCELLED
    public void sendOrderCancelledEvent(OrderCancelledEvent event) {

        log.info("Publishing OrderCancelledEvent for orderId={}", event.getOrderId());

        kafkaTemplate.send(KafkaTopics.ORDER_CANCELLED, event.getOrderId().toString(), event);
    }

    // 🔥 ORDER COMPLETED
    public void sendOrderCompletedEvent(OrderCompletedEvent event) {

        log.info("Publishing OrderCompletedEvent for orderId={}", event.getOrderId());
        kafkaTemplate.send(KafkaTopics.ORDER_COMPLETED, event.getOrderId().toString(), event);
    }

    public void sendStockConfirmEvent(StockConfirmedEvent event) {

        log.info(" Publishing StockConfirmEvent → orderId={}", event.getOrderId());
        kafkaTemplate.send("inventory-stock-confirmed", event.getOrderId().toString(), event);
    }

    public void sendStockReleaseEvent(StockReleasedEvent event) {

        log.info(" Publishing StockReleaseEvent → orderId={}", event.getOrderId());
        kafkaTemplate.send("inventory-stock-released", event.getOrderId().toString(), event);
    }
}
