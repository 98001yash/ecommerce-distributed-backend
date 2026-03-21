package com.ecommerce_distributed_system.order_service.controller;


import com.ecommerce_distributed_system.order_service.dtos.CreateOrderRequest;
import com.ecommerce_distributed_system.order_service.dtos.CreateOrderResponse;
import com.ecommerce_distributed_system.order_service.dtos.OrderResponse;
import com.ecommerce_distributed_system.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {


    private final OrderService orderService;


    //  1. CREATE ORDER
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request
    ) {

        log.info("API: Create order request received for productId={}, quantity={}",
                request.getProductId(), request.getQuantity());

        CreateOrderResponse response = orderService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    //  2. GET ORDER BY ID
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId
    ) {

        log.info("API: Get order request for orderId={}", orderId);
        OrderResponse response = orderService.getOrder(orderId);
        return ResponseEntity.ok(response);
    }


    //   GET CURRENT USER ORDERS
    @GetMapping("/user")
    public ResponseEntity<List<OrderResponse>> getUserOrders() {

        log.info("API: Get user orders request");
        List<OrderResponse> response = orderService.getUserOrders();
        return ResponseEntity.ok(response);
    }


    //   GET ORDERS BY STATUS
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @PathVariable String status
    ) {

        log.info("API: Get orders by status={}", status);
        List<OrderResponse> response = orderService.getOrderByStatus(status);
        return ResponseEntity.ok(response);
    }


    //  CANCEL ORDER
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(
            @PathVariable Long orderId
    ) {

        log.info("API: Cancel order request for orderId={}", orderId);
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("Order cancelled successfully");
    }

}
