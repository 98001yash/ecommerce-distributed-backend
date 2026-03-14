package com.ecommerce_distributed_backend.product_service.service.Impl;


import com.ecommerce_distributed_backend.product_service.auth.UserContextHolder;
import com.ecommerce_distributed_backend.product_service.dtos.request.CreateProductRequest;
import com.ecommerce_distributed_backend.product_service.dtos.request.UpdateProductRequest;
import com.ecommerce_distributed_backend.product_service.dtos.response.ProductResponse;
import com.ecommerce_distributed_backend.product_service.entity.Product;
import com.ecommerce_distributed_backend.product_service.entity.enums.ProductStatus;
import com.ecommerce_distributed_backend.product_service.exception.ProductNotFoundException;
import com.ecommerce_distributed_backend.product_service.kafka.ProductEventProducer;
import com.ecommerce_distributed_backend.product_service.repository.ProductRepository;
import com.ecommerce_distributed_backend.product_service.service.ProductService;
import com.redditApp.events.ProductCreatedEvent;
import com.redditApp.events.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;


    @Override
    public ProductResponse createProduct(CreateProductRequest request, Long sellerId) {


        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Creating product for userId={}",userId);

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .sellerId(userId)
                .status(ProductStatus.ACTIVE)
                .build();

        Product savedProduct = productRepository.save(product);

        log.info("Product saved successfully with id={}",savedProduct.getId());

        ProductCreatedEvent event =
                ProductCreatedEvent.builder()
                        .productId(savedProduct.getId())
                        .name(savedProduct.getName())
                        .price(savedProduct.getPrice())
                        .sellerId(savedProduct.getSellerId())
                        .category(savedProduct.getCategory())
                        .build();

        productEventProducer.publishCreated(event);
        log.info("ProductCreatedEvent published for productId={}",savedProduct.getId());
        return mapToResponse(savedProduct);

    }

    @Override
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request, Long sellerId) {


        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Updating productId:{} by userId: {}",productId, userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> {
                    log.error("Product not found with id={}",productId);
                    return new ProductNotFoundException("Product not found");
                });
        if (!product.getSellerId().equals(userId)) {
            log.warn("Unauthorized update attempt for productId={} by userId={}", productId, userId);
            throw new RuntimeException("You are not allowed to update this product");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully id={}",productId);

        ProductUpdatedEvent event =
                ProductUpdatedEvent.builder()
                        .productId(updatedProduct.getId())
                        .name(updatedProduct.getName())
                        .price(updatedProduct.getPrice())
                        .category(updatedProduct.getCategory())
                        .build();

        productEventProducer.publishUpdated(event);

        log.info("ProductUpdatedEvent published for productId={}", productId);
        return mapToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long productId, Long sellerId) {

    }

    @Override
    public ProductResponse getProductById(Long productId) {
        return null;
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return List.of();
    }

    @Override
    public List<ProductResponse> getProductBySeller(Long sellerId) {
        return List.of();
    }


    private ProductResponse mapToResponse(Product product) {

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .sellerId(product.getSellerId())
                .category(product.getCategory())
                .status(product.getStatus().name())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
