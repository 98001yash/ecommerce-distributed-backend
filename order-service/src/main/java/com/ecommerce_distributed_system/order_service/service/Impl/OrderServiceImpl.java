package com.ecommerce_distributed_system.order_service.service.Impl;

import com.ecommerce_distributed_system.order_service.dtos.CreateOrderRequest;
import com.ecommerce_distributed_system.order_service.dtos.CreateOrderResponse;
import com.ecommerce_distributed_system.order_service.dtos.OrderResponse;
import com.ecommerce_distributed_system.order_service.entity.Order;
import com.ecommerce_distributed_system.order_service.enums.OrderStatus;
import com.ecommerce_distributed_system.order_service.exception.InvalidOrderStateException;
import com.ecommerce_distributed_system.order_service.exception.OrderNotFoundException;
import com.ecommerce_distributed_system.order_service.repository.OrderRepository;
import com.ecommerce_distributed_system.order_service.service.OrderService;
import com.redditApp.events.StockExpiredEvent;
import com.redditApp.events.StockReleasedEvent;
import com.redditApp.events.StockReservedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    // =========================================================
    // CREATE ORDER
    // =========================================================

    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} creating order for productId={}, quantity={}",
                userId, request.getProductId(), request.getQuantity());

        if (request.getQuantity() <= 0) {
            log.warn("Invalid quantity={} for productId={}",
                    request.getQuantity(), request.getProductId());
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Order order = Order.builder()
                .userId(userId)
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .status(OrderStatus.CREATED)
                .build();

        orderRepository.save(order);

        log.info("Order created successfully orderId={}, status={}",
                order.getId(), order.getStatus());

        return CreateOrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus().name())
                .message("Order created successfully. Waiting for inventory reservation.")
                .build();
    }

    // =========================================================
    // STOCK RESERVED
    // =========================================================

    @Override
    @Transactional
    public void handleStockReserved(StockReservedEvent event) {

        log.info("Processing stockReservedEvent for orderId={}, productId={}",
                event.getOrderId(), event.getProductId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> {
                    log.error("Order not found for orderId={}", event.getOrderId());
                    return new OrderNotFoundException("order not found with id:" + event.getOrderId());
                });

        if (order.getStatus() == OrderStatus.INVENTORY_RESERVED) {
            log.warn("Duplicate StockReservedEvent for orderId={}", order.getId());
            return;
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            log.error("Invalid state transition orderId={}, currentStatus={}",
                    order.getId(), order.getStatus());

            throw new InvalidOrderStateException(
                    "Cannot mark inventory reserved for state: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        orderRepository.save(order);

        log.info("Order updated → INVENTORY_RESERVED orderId={}", order.getId());

        log.info("Next step: trigger payment for orderId={}", order.getId());
    }

    // =========================================================
    // STOCK RELEASED (CANCEL / ROLLBACK)
    // =========================================================

    @Override
    @Transactional
    public void handleStockReleased(StockReleasedEvent event) {

        log.info("Processing StockReleasedEvent for orderId={}, productId={}",
                event.getOrderId(), event.getProductId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> {
                    log.error("Order not found for orderId={}", event.getOrderId());
                    return new OrderNotFoundException("order not found with id:" + event.getOrderId());
                });

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Duplicate StockReleasedEvent for orderId={}", order.getId());
            return;
        }

        if (order.getStatus() != OrderStatus.INVENTORY_RESERVED) {
            log.error("Invalid state for release orderId={}, status={}",
                    order.getId(), order.getStatus());

            throw new InvalidOrderStateException(
                    "Cannot release stock for order in state: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order marked as CANCELLED due to stock release orderId={}",
                order.getId());
    }

    // =========================================================
    // STOCK EXPIRED
    // =========================================================

    @Override
    @Transactional
    public void handleStockExpired(StockExpiredEvent event) {

        log.info("Processing StockExpiredEvent for orderId={}, productId={}",
                event.getOrderId(), event.getProductId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> {
                    log.error("Order not found for orderId={}", event.getOrderId());
                    return new OrderNotFoundException("order not found with id:" + event.getOrderId());
                });

        if (order.getStatus() == OrderStatus.INVENTORY_FAILED) {
            log.warn("Duplicate StockExpiredEvent for orderId={}", order.getId());
            return;
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            log.error("Invalid state transition orderId={}, status={}",
                    order.getId(), order.getStatus());

            throw new InvalidOrderStateException(
                    "Cannot mark inventory failed for state: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.INVENTORY_FAILED);
        orderRepository.save(order);

        log.info("Order marked as INVENTORY_FAILED orderId={}", order.getId());
    }

    // =========================================================
    // CANCEL ORDER (USER ACTION)
    // =========================================================

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} cancelling orderId={}", userId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("order not found with id:" + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Order already cancelled orderId={}", orderId);
            return;
        }

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException("Cannot cancel confirmed order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order cancelled successfully orderId={}", orderId);
    }

    // =========================================================
    // GET ORDER
    // =========================================================

    @Override
    public OrderResponse getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("order not found with id:" + orderId));

        return mapToResponse(order);
    }

    // =========================================================
    // GET USER ORDERS
    // =========================================================

    @Override
    public List<OrderResponse> getUserOrders() {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("Fetching orders for userId={}", userId);

        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // GET BY STATUS
    // =========================================================

    @Override
    public List<OrderResponse> getOrderByStatus(String status) {

        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());

        log.info("Fetching orders with status={}", orderStatus);

        return orderRepository.findByStatus(orderStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // MAPPER
    // =========================================================

    private OrderResponse mapToResponse(Order order) {

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .status(order.getStatus().name())
                .build();
    }
}