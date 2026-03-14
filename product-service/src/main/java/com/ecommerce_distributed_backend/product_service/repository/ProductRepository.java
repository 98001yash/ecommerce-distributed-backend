package com.ecommerce_distributed_backend.product_service.repository;

import com.ecommerce_distributed_backend.product_service.entity.Product;
import com.ecommerce_distributed_backend.product_service.entity.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {


    List<Product> findByStatus(ProductStatus status);

    List<Product> findByCategory(String category);

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByCategoryAndStatus(String category, ProductStatus status);
}
