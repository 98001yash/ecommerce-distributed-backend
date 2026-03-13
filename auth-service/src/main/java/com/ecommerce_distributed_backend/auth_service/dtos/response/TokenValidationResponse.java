package com.ecommerce_distributed_backend.auth_service.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationResponse {

    private boolean valid;
    private Long userId;
    private String email;
    private String role;
}
