package com.ecommerce_distributed_backend.inventory_service.kafka;

import com.ecommerce_distributed_backend.inventory_service.entity.Inventory;
import com.ecommerce_distributed_backend.inventory_service.repository.InventoryRepository;
import com.redditApp.events.ProductCreatedEvent;
import com.redditApp.events.ProductDeletedEvent;
import com.redditApp.events.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final InventoryRepository inventoryRepository;

    private static final Long DEFAULT_WAREHOUSE_ID = 1L;


    // PRODUCT CREATED

    @KafkaListener(
            topics = "product-created",
            groupId = "inventory-service-group"
    )
    @Transactional
    public void handleProductCreated(ProductCreatedEvent event) {

        log.info("Received ProductCreatedEvent productId={}", event.getProductId());

        boolean exists = inventoryRepository
                .findByProductId(event.getProductId())
                .isPresent();

        if (exists) {

            log.warn("Inventory already exists for productId={}", event.getProductId());
            return;
        }

        Inventory inventory = Inventory.builder()
                .productId(event.getProductId())
                .warehouseId(DEFAULT_WAREHOUSE_ID)
                .availableQuantity(0)
                .reservedQuantity(0)
                .soldQuantity(0)
                .build();

        inventoryRepository.save(inventory);

        log.info("Inventory created automatically for productId={}", event.getProductId());
    }


    // PRODUCT UPDATED

    @KafkaListener(
            topics = "product-updated",
            groupId = "inventory-service-group"
    )
    public void handleProductUpdated(ProductUpdatedEvent event) {

        log.info("Received ProductUpdatedEvent productId={}", event.getProductId());

        // inventory usually doesn't need product updates
        // but we log it for observability

        log.info("No inventory update required for productId={}", event.getProductId());
    }

    // PRODUCT DELETED

    @KafkaListener(
            topics = "product-deleted",
            groupId = "inventory-service-group"
    )
    @Transactional
    public void handleProductDeleted(ProductDeletedEvent event) {

        log.info("Received ProductDeletedEvent productId={}", event.getProductId());

        inventoryRepository.findByProductId(event.getProductId())
                .ifPresent(inventory -> {

                    inventoryRepository.delete(inventory);

                    log.info("Inventory removed for deleted productId={}",
                            event.getProductId());
                });
    }
}