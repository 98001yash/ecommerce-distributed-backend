package com.ecommerce_distributed_backend.product_service.service;

import com.ecommerce_distributed_backend.product_service.dtos.request.CreateProductRequest;
import com.ecommerce_distributed_backend.product_service.dtos.request.UpdateProductRequest;
import com.ecommerce_distributed_backend.product_service.dtos.response.ProductResponse;

import java.util.List;

public interface ProductService {


    ProductResponse createProduct(CreateProductRequest request, Long sellerId);

    ProductResponse updateProduct(Long productId, UpdateProductRequest request, Long sellerId);

    void deleteProduct(Long productId, Long sellerId);

    ProductResponse getProductById(Long productId);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getProductBySeller(Long sellerId);


}
