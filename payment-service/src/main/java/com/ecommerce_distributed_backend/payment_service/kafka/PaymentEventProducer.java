package com.ecommerce_distributed_backend.payment_service.kafka;

import com.redditApp.events.PaymentCompletedEvent;
import com.redditApp.events.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    //  SUCCESS EVENT
    public void sendPaymentCompletedEvent(PaymentCompletedEvent event) {

        log.info("📤 Publishing PaymentCompletedEvent → orderId={}", event.getOrderId());

        kafkaTemplate.send(
                KafkaTopics.PAYMENT_COMPLETED,
                event.getOrderId().toString(),
                event
        );
    }

    //  FAILURE EVENT
    public void sendPaymentFailedEvent(PaymentFailedEvent event) {

        log.info("📤 Publishing PaymentFailedEvent → orderId={}", event.getOrderId());

        kafkaTemplate.send(
                KafkaTopics.PAYMENT_FAILED,
                event.getOrderId().toString(),
                event
        );
    }
}
