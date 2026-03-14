package com.ecommerce_distributed_backend.product_service.controller;


import com.ecommerce_distributed_backend.product_service.auth.RoleAllowed;
import com.ecommerce_distributed_backend.product_service.auth.UserContextHolder;
import com.ecommerce_distributed_backend.product_service.dtos.request.CreateProductRequest;
import com.ecommerce_distributed_backend.product_service.dtos.request.UpdateProductRequest;
import com.ecommerce_distributed_backend.product_service.dtos.response.ProductResponse;
import com.ecommerce_distributed_backend.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @RoleAllowed("ROLE_SELLER")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("API request: create product by userId={}", userId);
        ProductResponse response =
                productService.createProduct(request, userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    @RoleAllowed("ROLE_SELLER")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("API request: update productId={} by userId={}", productId, userId);

        ProductResponse response =
                productService.updateProduct(productId, request, userId);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{productId}")
    @RoleAllowed({"ROLE_SELLER","ROLE_ADMIN"})
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId) {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("API request: delete productId={} by userId={}", productId, userId);
        productService.deleteProduct(productId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}")
    @RoleAllowed({"ROLE_ADMIN","ROLE_SELLER","ROLE_CUSTOMER"})
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long productId) {

        log.info("API request: fetch productId={}", productId);
        ProductResponse response =
                productService.getProductById(productId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @RoleAllowed({"ROLE_ADMIN","ROLE_SELLER","ROLE_CUSTOMER"})
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        log.info("API request: fetch all products");

        List<ProductResponse> response =
                productService.getAllProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/seller")
    @RoleAllowed("ROLE_SELLER")
    public ResponseEntity<List<ProductResponse>> getSellerProducts() {

        Long userId = UserContextHolder.getCurrentUserId();

        log.info("API request: fetch products for sellerId={}", userId);

        List<ProductResponse> response =
                productService.getProductBySeller(userId);
        return ResponseEntity.ok(response);
    }


}
