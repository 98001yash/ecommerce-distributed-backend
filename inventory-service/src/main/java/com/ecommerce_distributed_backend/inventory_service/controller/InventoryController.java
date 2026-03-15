package com.ecommerce_distributed_backend.inventory_service.controller;

import com.ecommerce_distributed_backend.inventory_service.auth.RoleAllowed;
import com.ecommerce_distributed_backend.inventory_service.dtos.*;
import com.ecommerce_distributed_backend.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;


    // RESERVE STOCK
    @PostMapping("/reserve")
    @RoleAllowed("ROLE_ADMIN")
    public ResponseEntity<ReserveStockResponse> reserveStock(
            @RequestBody ReserveStockRequest request) {

        log.info("API request received: reserve stock productId={}, quantity={}",
                request.getProductId(),
                request.getQuantity());

        ReserveStockResponse response = inventoryService.reserveStock(request);
        return ResponseEntity.ok(response);
    }


    // CONFIRM RESERVATION

    @PostMapping("/confirm")
    @RoleAllowed({"ROLE_ADMIN","ROLE_SELLER","ROLE_CUSTOMER"})
    public ResponseEntity<Void> confirmReservation(
            @RequestBody ConfirmReservationRequest request) {

        log.info("API request received: confirm reservation orderId={}, productId={}",
                request.getOrderId(),
                request.getProductId());

        inventoryService.confirmReservation(request);
        return ResponseEntity.ok().build();
    }


    // RELEASE RESERVATION

    @PostMapping("/release")
    @RoleAllowed({"ROLE_ADMIN","ROLE_SELLER","ROLE_CUSTOMER"})
    public ResponseEntity<Void> releaseReservation(
            @RequestBody ReleaseReservationRequest request) {

        log.info("API request received: release reservation orderId={}, productId={}",
                request.getOrderId(),
                request.getProductId());

        inventoryService.releaseReservation(request);
        return ResponseEntity.ok().build();
    }

    // GET INVENTORY

    @GetMapping("/{productId}")
    @RoleAllowed({"ROLE_ADMIN","ROLE_SELLER","ROLE_CUSTOMER"})
    public ResponseEntity<InventoryResponse> getInventory(
            @PathVariable Long productId) {

        log.info("API request received: get inventory productId={}", productId);

        InventoryResponse response = inventoryService.getInventory(productId);
        return ResponseEntity.ok(response);
    }



    // ADD STOCK
    @PostMapping("/add-stock")
    @RoleAllowed({"ROLE_ADMIN","ROLE_SELLER","ROLE_CUSTOMER"})
    public ResponseEntity<Void> addStock(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity) {

        log.info("API request received: add stock productId={}, quantity={}",
                productId,
                quantity);

        inventoryService.addStock(productId, warehouseId, quantity);
        return ResponseEntity.ok().build();
    }


    // REMOVE STOCK

    @PostMapping("/remove-stock")
    @RoleAllowed({"ROLE_ADMIN","ROLE_SELLER","ROLE_CUSTOMER"})
    public ResponseEntity<Void> removeStock(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity) {

        log.info("API request received: remove stock productId={}, quantity={}",
                productId,
                quantity);

        inventoryService.removeStock(productId, warehouseId, quantity);
        return ResponseEntity.ok().build();
    }
}
