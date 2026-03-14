package com.ecommerce_distributed_backend.api_gateway.filter;

import com.ecommerce_distributed_backend.api_gateway.JwtService;
import com.ecommerce_distributed_backend.api_gateway.util.RouterValidator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter
        extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouterValidator routerValidator;
    private final JwtService jwtService;

    public AuthenticationFilter(RouterValidator routerValidator,
                                JwtService jwtService) {
        super(Config.class);
        this.routerValidator = routerValidator;
        this.jwtService = jwtService;
    }


    @Override
    public String name() {
        return "AuthenticationFilter";
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            log.info("Incoming request → {} {}", request.getMethod(), request.getURI());

            if (routerValidator.isSecured.test(request)) {

                log.info("Secured route detected: {}", request.getURI().getPath());

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                if (authHeader == null) {
                    log.warn("Authorization header missing");
                    return unauthorized(exchange, "Authorization header missing");
                }

                if (!authHeader.startsWith("Bearer ")) {
                    log.warn("Invalid Authorization header format");
                    return unauthorized(exchange, "Invalid Authorization header");
                }

                String token = authHeader.substring(7);

                try {

                    if (!jwtService.validateToken(token)) {
                        log.warn("JWT validation failed");
                        return unauthorized(exchange, "Invalid token");
                    }

                    String email = jwtService.extractEmail(token);
                    String role = jwtService.extractRole(token);
                    String userId = jwtService.extractUserId(token);

                    log.info("JWT validated successfully → userId={}, role={}", userId, role);

                    ServerHttpRequest mutatedRequest =
                            request.mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-User-Roles", role)
                                    .header("X-User-Email", email)
                                    .build();

                    exchange = exchange.mutate()
                            .request(mutatedRequest)
                            .build();

                    log.info("User context propagated to downstream service");

                } catch (Exception ex) {

                    log.error("JWT processing error → {}", ex.getMessage());

                    return unauthorized(exchange, "JWT processing failed");
                }
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

        log.error("Unauthorized access → {}", message);

        return exchange.getResponse().setComplete();
    }

    public static class Config {}
}