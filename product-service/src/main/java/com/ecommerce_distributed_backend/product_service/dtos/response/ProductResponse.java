package com.ecommerce_distributed_backend.product_service.dtos.response;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Long sellerId;

    private String category;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
