package com.ecommerce_distributed_backend.api_gateway.filter;

import com.ecommerce_distributed_backend.api_gateway.JwtService;
import com.ecommerce_distributed_backend.api_gateway.util.RouterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter
        extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouterValidator validator;
    private final JwtService jwtService;

    public AuthenticationFilter(RouterValidator validator, JwtService jwtService) {
        super(Config.class);
        this.validator = validator;
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            if (validator.isSecured.test(request)) {

                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing Authorization Header");
                }

                String authHeader =
                        request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                String token = authHeader.substring(7);

                // Validate JWT
                String email = jwtService.extractEmail(token);
                String role = jwtService.extractRole(token);

                // Propagate user context
                ServerHttpRequest modifiedRequest =
                        request.mutate()
                                .header("X-User-Email", email)
                                .header("X-User-Role", role)
                                .build();

                exchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {}
}