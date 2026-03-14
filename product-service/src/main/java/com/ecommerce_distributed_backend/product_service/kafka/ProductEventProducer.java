package com.ecommerce_distributed_backend.product_service.kafka;

import com.redditApp.events.ProductCreatedEvent;
import com.redditApp.events.ProductDeletedEvent;
import com.redditApp.events.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PRODUCT_CREATED_TOPIC = "product-created";
    private static final String PRODUCT_UPDATED_TOPIC = "product-updated";
    private static final String PRODUCT_DELETED_TOPIC = "product-deleted";

    public void publishCreated(ProductCreatedEvent event) {

        log.info("Publishing ProductCreatedEvent for productId={}", event.getProductId());

        kafkaTemplate.send(
                PRODUCT_CREATED_TOPIC,
                event.getProductId().toString(),
                event
        );

        log.info("ProductCreatedEvent published successfully for productId={}", event.getProductId());
    }

    public void publishUpdated(ProductUpdatedEvent event) {
        log.info("Publishing ProductUpdatedEvent for productId={}", event.getProductId());

        kafkaTemplate.send(
                PRODUCT_UPDATED_TOPIC,
                event.getProductId().toString(),
                event
        );
        log.info("ProductUpdatedEvent published successfully for productId={}", event.getProductId());
    }


    public void publishDeleted(ProductDeletedEvent event) {

        log.info("Publishing ProductDeletedEvent for productId={}", event.getProductId());

        kafkaTemplate.send(
                PRODUCT_DELETED_TOPIC,
                event.getProductId().toString(),
                event
        );
        log.info("ProductDeletedEvent published successfully for productId={}", event.getProductId());
    }
}
