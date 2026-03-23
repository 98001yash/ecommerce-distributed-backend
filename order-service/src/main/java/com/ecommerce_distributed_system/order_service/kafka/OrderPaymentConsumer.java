package com.ecommerce_distributed_system.order_service.kafka;


import com.ecommerce_distributed_system.order_service.service.OrderService;
import com.redditApp.events.PaymentCompletedEvent;
import com.redditApp.events.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentConsumer {

    private final OrderService orderService;

    //  PAYMENT SUCCESS
    @KafkaListener(
            topics = "payment-completed",
            groupId = "order-service-group",
            containerFactory = "orderKafkaListenerFactory"
    )
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        log.info(" Received PaymentCompletedEvent → orderId={}",
                event.getOrderId());

        try {
            orderService.handlePaymentCompleted(event);
        } catch (Exception ex) {
            log.error(" Error processing PaymentCompletedEvent for orderId={}",
                    event.getOrderId(), ex);
            throw ex; // let retry happen
        }
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "order-service-group",
            containerFactory = "orderKafkaListenerFactory"
    )
    public void handlePaymentFailed(PaymentFailedEvent event) {

        log.info(" Received PaymentFailedEvent → orderId={}",
                event.getOrderId());

        try {
            orderService.handlePaymentFailed(event);
        } catch (Exception ex) {
            log.error("Error processing PaymentFailedEvent for orderId={}",
                    event.getOrderId(), ex);
            throw ex;
        }
    }
}
