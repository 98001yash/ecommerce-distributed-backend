package com.ecommerce_distributed_system.order_service.repository;

import com.ecommerce_distributed_system.order_service.entity.Order;
import com.ecommerce_distributed_system.order_service.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // FIND BY USER
    List<Order> findByUserId(Long userId);


    // FIND BY STATUS
    List<Order> findByStatus(OrderStatus status);

    // FIND BY USER + STATUS
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    // FIND BY INVENTORY RESERVATION ID
    Optional<Order> findByInventoryReservationId(Long reservationId);


    // FIND BY PAYMENT TRANSACTION ID
    Optional<Order> findByPaymentTransactionId(String transactionId);


    // FETCH LATEST ORDER OF USER
    Optional<Order> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
