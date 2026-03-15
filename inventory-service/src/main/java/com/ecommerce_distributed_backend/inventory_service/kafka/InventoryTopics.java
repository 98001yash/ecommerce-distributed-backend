package com.ecommerce_distributed_backend.inventory_service.kafka;

public final class InventoryTopics {


    private InventoryTopics() {}

    public static final String STOCK_RESERVED = "inventory-stock-reserved";

    public static final String STOCK_RELEASED = "inventory-stock-released";

    public static final String STOCK_CONFIRMED = "inventory-stock-confirmed";

    public static final String STOCK_EXPIRED = "inventory-stock-expired";


}
