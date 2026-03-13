package com.ecommerce_distributed_backend.auth_service.service.Impl;


import com.ecommerce_distributed_backend.auth_service.dtos.request.LoginRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.LogoutRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.RefreshTokenRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.request.RegisterRequest;
import com.ecommerce_distributed_backend.auth_service.dtos.response.AuthResponse;
import com.ecommerce_distributed_backend.auth_service.dtos.response.TokenValidationResponse;
import com.ecommerce_distributed_backend.auth_service.entity.User;
import com.ecommerce_distributed_backend.auth_service.exception.UserAlreadyExistsException;
import com.ecommerce_distributed_backend.auth_service.repository.UserRepository;
import com.ecommerce_distributed_backend.auth_service.security.JwtService;
import com.ecommerce_distributed_backend.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
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



    // Register new user
    @Override
    public AuthResponse register(RegisterRequest request) {

        log.info("Register request received for email: {}",request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())){
            log.warn("User already exists with email: {}",request.getEmail());;
            throw new UserAlreadyExistsException("User already exists with email");
        }


        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);

        log.info("User successfully registered with id: {}",savedUser.getId());

        String accessToken = jwtService.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .accessToken(accessToken)
                .refreshToken(null)
                .role(savedUser.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        return null;
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        return null;
    }

    @Override
    public void logout(LogoutRequest request) {

    }
}
