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

        log.info("Processing stockReservedEvent for orderId={}, productId={}",
                event.getOrderId(), event.getProductId());


        // fetch order
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(()-> {
                    log.error("Order not found for orderId={}",event.getOrderId());
                    return new OrderNotFoundException("order not found with id:"+event.getOrderId());
                });

        // IDEMPOTENCY CHECK
        if (order.getStatus() == OrderStatus.INVENTORY_RESERVED) {

            log.warn("Duplicate StockReservedEvent received for orderId={}",
                    order.getId());

            return; // already processed
        }

        // 3. State validation
        if (order.getStatus() != OrderStatus.CREATED) {

            log.error("Invalid state transition for orderId={}, currentStatus={}",
                    order.getId(), order.getStatus());

            throw new InvalidOrderStateException(
                    "Cannot mark inventory reserved for order in state: " + order.getStatus()
            );
        }


// 4. UPDATE ORDER

        order.setStatus(OrderStatus.INVENTORY_RESERVED);

//         NOTE: No reservationId in your event → skip it
//          order.setInventoryReservationId(...)
        orderRepository.save(order);

        log.info("Order updated successfully to INVENTORY_RESERVED for orderId={}, productId={}, quantity={}",
                order.getId(),
                event.getProductId(),
                event.getQuantity());


//              5. NEXT STEP (PAYMENT - WILL ADD NEXT)
        log.info("Inventory reserved. Ready to trigger payment for orderId={}",
                order.getId());

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
