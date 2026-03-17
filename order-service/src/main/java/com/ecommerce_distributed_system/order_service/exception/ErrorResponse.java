package com.ecommerce_distributed_system.order_service.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private String message;
    private int status;
    private LocalDateTime timestamp;
}
