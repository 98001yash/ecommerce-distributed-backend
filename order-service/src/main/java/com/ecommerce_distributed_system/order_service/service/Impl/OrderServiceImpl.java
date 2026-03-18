package com.ecommerce_distributed_system.order_service.service.Impl;


import com.ecommerce_distributed_system.order_service.dtos.CreateOrderRequest;
import com.ecommerce_distributed_system.order_service.dtos.CreateOrderResponse;
import com.ecommerce_distributed_system.order_service.dtos.OrderResponse;
import com.ecommerce_distributed_system.order_service.repository.OrderRepository;
import com.ecommerce_distributed_system.order_service.service.OrderService;
import com.redditApp.events.StockExpiredEvent;
import com.redditApp.events.StockReleasedEvent;
import com.redditApp.events.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;


    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        return null;
    }

    @Override
    public void handleStockReserved(StockReservedEvent event) {

    }

    @Override
    public void handleStockReleased(StockReleasedEvent event) {

    }

    @Override
    public void handleStockExpired(StockExpiredEvent event) {

    }

    @Override
    public void cancelOrder(Long orderId) {

    }

    @Override
    public OrderResponse getOrder(Long orderId) {
        return null;
    }

    @Override
    public List<OrderResponse> getUserOrders() {
        return List.of();
    }

    @Override
    public List<OrderResponse> getOrderByStatus(String status) {
        return List.of();
    }
}
