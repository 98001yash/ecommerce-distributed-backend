package com.ecommerce_distributed_backend.auth_service.service.Impl;

import com.ecommerce_distributed_backend.auth_service.dtos.request.LoginRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.LogoutRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.RefreshTokenRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.RegisterRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.response.AuthResponse;
import com.ecommerce_distributed_backend.auth_service.dtos.response.TokenValidationResponse;
import com.ecommerce_distributed_backend.auth_service.entity.User;
import com.ecommerce_distributed_backend.auth_service.exception.InvalidCredentialsException;
import com.ecommerce_distributed_backend.auth_service.exception.TokenExpiredException;
import com.ecommerce_distributed_backend.auth_service.exception.UserAlreadyExistsException;
import com.ecommerce_distributed_backend.auth_service.repository.UserRepository;
import com.ecommerce_distributed_backend.auth_service.security.JwtService;
import com.ecommerce_distributed_backend.auth_service.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;


    @Override
    public AuthResponse register(RegisterRequest request) {

        log.info("Register request received for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User already exists with email: {}", request.getEmail());
            throw new UserAlreadyExistsException("User already exists with this email");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isEnabled(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(user);

        log.info("User successfully registered with id: {}", savedUser.getId());
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(savedUser.getEmail());

        String accessToken = jwtService.generateToken(userDetails);
        return AuthResponse.builder()
                .userId(savedUser.getId())
                .accessToken(accessToken)
                .refreshToken(null)
                .role(savedUser.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

        } catch (BadCredentialsException ex) {

            log.error("Invalid login credentials for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtService.generateToken(userDetails);
        log.info("User login successful: {}", user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(null)
                .role(user.getRole().name())
                .build();
    }


    @Override
    public TokenValidationResponse validateToken(String token) {

        log.debug("Validating JWT token");

        try {

            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("User not found during token validation");
                        return new InvalidCredentialsException("User not found");
                    });

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);

            boolean valid = jwtService.validateToken(token, userDetails);

            return TokenValidationResponse.builder()
                    .valid(valid)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

        } catch (Exception ex) {

            log.error("Token validation failed: {}", ex.getMessage());
            throw new TokenExpiredException("Invalid or expired token");
        }
    }


    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        log.info("Refresh token request received");

        try {

            String email = jwtService.extractEmail(request.getRefreshToken());
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("User not found for refresh token");
                        return new InvalidCredentialsException("User not found");
                    });

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(user.getEmail());

            String newAccessToken = jwtService.generateToken(userDetails);

            log.info("Access token refreshed for user: {}", email);

            return AuthResponse.builder()
                    .userId(user.getId())
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken())
                    .role(user.getRole().name())
                    .build();

        } catch (Exception ex) {

            log.error("Refresh token failed: {}", ex.getMessage());
            throw new TokenExpiredException("Refresh token expired");
        }
    }


    @Override
    public void logout(LogoutRequest request) {

        log.info("Logout request received");

        // Future improvement
        // Store token in Redis blacklist

        log.info("User successfully logged out");
    }
}