package com.ecommerce_distributed_backend.auth_service.service;

import com.ecommerce_distributed_backend.auth_service.dtos.request.LoginRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.LogoutRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.RefreshTokenRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.RegisterRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.response.AuthResponse;
import com.ecommerce_distributed_backend.auth_service.dtos.response.TokenValidationResponse;

public interface AuthService {


    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    TokenValidationResponse validateToken(String token);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);
}
