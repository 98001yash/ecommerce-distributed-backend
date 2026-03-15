package com.ecommerce_distributed_backend.inventory_service.advices;

import com.ecommerce_distributed_backend.inventory_service.exception.InsufficientStockException;
import com.ecommerce_distributed_backend.inventory_service.exception.InventoryException;
import com.ecommerce_distributed_backend.inventory_service.exception.InventoryNotFoundException;
import com.ecommerce_distributed_backend.inventory_service.exception.ReservationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InventoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleInventoryNotFound(InventoryNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInsufficientStock(InsufficientStockException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleReservationNotFound(ReservationNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(InventoryException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleInventoryException(InventoryException ex) {
        return ex.getMessage();
    }

}
