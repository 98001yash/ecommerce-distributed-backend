package com.ecommerce_distributed_backend.auth_service.controller;

import com.ecommerce_distributed_backend.auth_service.dtos.request.LoginRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.LogoutRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.RefreshTokenRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.RegisterRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.response.AuthResponse;
import com.ecommerce_distributed_backend.auth_service.dtos.response.TokenValidationResponse;
import com.ecommerce_distributed_backend.auth_service.service.AuthService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register API called for email: {}", request.getEmail());

        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login API called for email: {}", request.getEmail());

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        log.debug("Token validation request received");

        String token = authHeader.replace("Bearer ", "");
        TokenValidationResponse response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("Refresh token API called");

        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Valid @RequestBody LogoutRequest request) {

        log.info("Logout API called");

        authService.logout(request);
        return ResponseEntity.ok("User logged out successfully");
    }
}