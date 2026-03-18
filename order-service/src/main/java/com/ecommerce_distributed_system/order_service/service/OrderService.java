package com.ecommerce_distributed_system.order_service.service;

import com.ecommerce_distributed_system.order_service.dtos.CreateOrderRequest;
import com.ecommerce_distributed_system.order_service.dtos.CreateOrderResponse;
import com.ecommerce_distributed_system.order_service.dtos.OrderResponse;
import com.redditApp.events.StockExpiredEvent;
import com.redditApp.events.StockReleasedEvent;
import com.redditApp.events.StockReservedEvent;

import java.util.List;

public interface OrderService {




    // CREATE ORDER(START SAGA)
    CreateOrderResponse createOrder(CreateOrderRequest request);


    // INVENTORY EVENTS (kafka CONSUMER handlers)
    void handleStockReserved(StockReservedEvent event);
    void handleStockReleased(StockReleasedEvent event);
    void handleStockExpired(StockExpiredEvent event);



    // TODO:
    // void handlePaymentCompleted(PaymentCompletedEvent event);
    // void handlePaymentFailed(PaymentFailedEvent event);


    // CANCEL ORDER
    void cancelOrder(Long orderId);


    // GET ORDER
    OrderResponse getOrder(Long orderId);


    // get USER ORDERS (uses UserContextHolder internally)
    List<OrderResponse> getUserOrders();


    // ADMIN/DEBUG
    List<OrderResponse>getOrderByStatus(String status);




}
