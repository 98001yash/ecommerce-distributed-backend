package com.ecommerce_distributed_backend.payment_service.repository;

import com.ecommerce_distributed_backend.payment_service.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {


    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);
}
