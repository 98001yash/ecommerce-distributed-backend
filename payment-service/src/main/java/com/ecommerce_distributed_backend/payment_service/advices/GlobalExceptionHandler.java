package com.ecommerce_distributed_backend.payment_service.advices;


import com.ecommerce_distributed_backend.payment_service.exception.InvalidPaymentStateException;
import com.ecommerce_distributed_backend.payment_service.exception.PaymentFailedException;
import com.ecommerce_distributed_backend.payment_service.exception.PaymentNotFoundException;
import com.ecommerce_distributed_backend.payment_service.exception.PaymentProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<?> handleNotFound(PaymentNotFoundException ex) {
        return ResponseEntity.status(404).body(error(ex));
    }

    @ExceptionHandler({
            InvalidPaymentStateException.class,
            PaymentFailedException.class
    })
    public ResponseEntity<?> handleBusinessErrors(RuntimeException ex) {
        return ResponseEntity.badRequest().body(error(ex));
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<?> handleProcessing(PaymentProcessingException ex) {
        return ResponseEntity.status(500).body(error(ex));
    }

    private Map<String, Object> error(Exception ex) {
        return Map.of(
                "timestamp", Instant.now(),
                "error", ex.getMessage()
        );
    }
}
