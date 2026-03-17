package com.ecommerce_distributed_system.order_service.enums;

public enum OrderStatus {

    CREATED,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,

    PAYMENT_PENDING,
    PAYMENT_FAILED,

    CONFIRMED,
    DELIVERED,

    CANCELLED
}
