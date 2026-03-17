package com.ecommerce_distributed_system.order_service.config;

import com.ecommerce_distributed_system.order_service.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> handleOrderNotFound(OrderNotFoundException ex) {

        log.error("Order not found: {}", ex.getMessage());

        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<?> handleInvalidState(InvalidOrderStateException ex) {

        log.error("Invalid order state: {}", ex.getMessage());

        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InventoryReservationFailedException.class)
    public ResponseEntity<?> handleInventoryFailure(InventoryReservationFailedException ex) {

        log.error("Inventory reservation failed: {}", ex.getMessage());

        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<?> handlePaymentFailure(PaymentFailedException ex) {

        log.error("Payment failed: {}", ex.getMessage());

        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderAlreadyProcessedException.class)
    public ResponseEntity<?> handleAlreadyProcessed(OrderAlreadyProcessedException ex) {

        log.warn("Order already processed: {}", ex.getMessage());

        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {

        log.error("Unhandled exception", ex);

        return buildResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<?> buildResponse(String message, HttpStatus status) {

        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .build()
        );
    }
}
