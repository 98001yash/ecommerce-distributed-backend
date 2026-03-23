package com.ecommerce_distributed_system.order_service.service.Impl;

import com.ecommerce_distributed_system.order_service.auth.UserContextHolder;
import com.ecommerce_distributed_system.order_service.dtos.CreateOrderRequest;
import com.ecommerce_distributed_system.order_service.dtos.CreateOrderResponse;
import com.ecommerce_distributed_system.order_service.dtos.OrderResponse;
import com.ecommerce_distributed_system.order_service.entity.Order;
import com.ecommerce_distributed_system.order_service.enums.OrderStatus;
import com.ecommerce_distributed_system.order_service.exception.InvalidOrderStateException;
import com.ecommerce_distributed_system.order_service.exception.OrderNotFoundException;
import com.ecommerce_distributed_system.order_service.kafka.OrderEventProducer;
import com.ecommerce_distributed_system.order_service.repository.OrderRepository;
import com.ecommerce_distributed_system.order_service.service.OrderService;
import com.redditApp.events.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;


    // CREATE ORDER
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

        //  PUBLISH EVENT → ORDER CREATED
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .createdAt(Instant.now())
                .build();

        orderEventProducer.sendOrderCreatedEvent(event);

        log.info("OrderCreatedEvent published for orderId={}", order.getId());

        return CreateOrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus().name())
                .message("Order created successfully. Waiting for inventory reservation.")
                .build();
    }


    // STOCK RESERVED
    @Override
    @Transactional
    public void handleStockReserved(StockReservedEvent event) {

        log.info("Processing stockReservedEvent for orderId={}, productId={}",
                event.getOrderId(), event.getProductId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "order not found with id:" + event.getOrderId()
                ));

        if (order.getStatus() == OrderStatus.INVENTORY_RESERVED) {
            log.warn("Duplicate StockReservedEvent for orderId={}", order.getId());
            return;
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new InvalidOrderStateException(
                    "Cannot mark inventory reserved for state: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        orderRepository.save(order);

        log.info("Order updated → INVENTORY_RESERVED orderId={}", order.getId());

        //  NEXT STEP (COMING SOON)
        // Trigger Payment Service here
    }


    // STOCK RELEASED


    @Override
    @Transactional
    public void handleStockReleased(StockReleasedEvent event) {

        log.info("Processing StockReleasedEvent for orderId={}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "order not found with id:" + event.getOrderId()
                ));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        if (order.getStatus() != OrderStatus.INVENTORY_RESERVED) {
            throw new InvalidOrderStateException(
                    "Cannot release stock for order in state: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order marked as CANCELLED due to stock release orderId={}",
                order.getId());
    }


    // STOCK EXPIRED
    @Override
    @Transactional
    public void handleStockExpired(StockExpiredEvent event) {

        log.info("Processing StockExpiredEvent for orderId={}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "order not found with id:" + event.getOrderId()
                ));

        if (order.getStatus() == OrderStatus.INVENTORY_FAILED) {
            return;
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new InvalidOrderStateException(
                    "Cannot mark inventory failed for state: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.INVENTORY_FAILED);
        orderRepository.save(order);

        log.info("Order marked as INVENTORY_FAILED orderId={}", order.getId());
    }

    @Override
    @Transactional
    public void handleStockConfirmed(StockConfirmedEvent event) {

        log.info("Processing StockConfirmedEvent for orderId={}, productId={}",
                event.getOrderId(), event.getProductId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> {
                    log.error("Order not found for orderId={}", event.getOrderId());
                    return new OrderNotFoundException("Order not found: " + event.getOrderId());
                });

        // IDEMPOTENCY
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.warn("Duplicate StockConfirmedEvent for orderId={}", order.getId());
            return;
        }

        // VALID STATE
        if (order.getStatus() != OrderStatus.INVENTORY_RESERVED) {
            log.error("Invalid state transition for orderId={}, status={}",
                    order.getId(), order.getStatus());

            throw new InvalidOrderStateException(
                    "Cannot confirm order in state: " + order.getStatus()
            );
        }

        // UPDATE
        order.setStatus(OrderStatus.CONFIRMED);

        orderRepository.save(order);

        log.info("Order marked as CONFIRMED orderId={}", order.getId());
    }

    @Override
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        log.info("Processing paymentCompletedEvent -> orderId={}",event.getOrderId());
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(()->new OrderNotFoundException(
                        "Order not found with id:" + event.getOrderId()
                ));

        if(order.getStatus() == OrderStatus.CONFIRMED){
            log.warn("Duplicate PaymentCompletedEvent for orderId={}",order.getId());
            return;
        }

        if (order.getStatus() != OrderStatus.INVENTORY_RESERVED) {
            throw new InvalidOrderStateException(
                    "Cannot confirm order in state: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(Instant.from(LocalDateTime.from(Instant.now())));
        orderRepository.save(order);

        log.info(" Order CONFIRMED → orderId={}", order.getId());

        //  NEXT STEP (IMPORTANT)
        // event published
//        StockConfirmedEvent event1 = StockConfirmedEvent.builder()
//                .eventId(System.currentTimeMillis())
//                .orderId(order.getId())
//                .productId(order.getProductId())
//                .quantity(order.getQuantity())
//                .warehouseId(1L) // temp (same as inventory)
//                .timestamp(System.currentTimeMillis())
//                .build();
//
//        orderEventProducer.sendStockConfirmEvent(event1);

    }

    @Override
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {

        log.info("Processing PaymentFailedEvent → orderId={}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "order not found with id:" + event.getOrderId()
                ));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Duplicate PaymentFailedEvent for orderId={}", order.getId());
            return;
        }

        if (order.getStatus() != OrderStatus.INVENTORY_RESERVED) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order in state: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(java.time.Instant.now());
        orderRepository.save(order);

        log.info("❌ Order CANCELLED due to payment failure → orderId={}", order.getId());

        //  NEXT STEP (IMPORTANT)
        StockReleasedEvent event1 = StockReleasedEvent.builder()
                .eventId(System.currentTimeMillis())
                .orderId(order.getId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .warehouseId(1L)
                .timestamp(System.currentTimeMillis())
                .build();

        orderEventProducer.sendStockReleaseEvent(event1);
    }


    // CANCEL ORDER
    @Override
    @Transactional
    public void cancelOrder(Long orderId) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("User {} cancelling orderId={}", userId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "order not found with id:" + orderId
                ));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException("Cannot cancel confirmed order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order cancelled successfully orderId={}", orderId);

        //  PUBLISH EVENT → ORDER CANCELLED
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .reason("User cancelled order")
                .cancelledAt(Instant.now())
                .build();

        orderEventProducer.sendOrderCancelledEvent(event);
        log.info("OrderCancelledEvent published for orderId={}", orderId);
    }


    // GET ORDER
    @Override
    public OrderResponse getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "order not found with id:" + orderId
                ));

        return mapToResponse(order);
    }


    // GET USER ORDERS
    @Override
    public List<OrderResponse> getUserOrders() {

        Long userId = UserContextHolder.getCurrentUserId();

        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // GET BY STATUS
    @Override
    public List<OrderResponse> getOrderByStatus(String status) {

        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());

        return orderRepository.findByStatus(orderStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // MAPPER
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