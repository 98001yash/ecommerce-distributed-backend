package com.ecommerce_distributed_backend.auth_service.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long userId;
    private String accessToken;
    private String refreshToken;
    private String role;
}
