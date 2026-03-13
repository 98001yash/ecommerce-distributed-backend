package com.ecommerce_distributed_backend.api_gateway.filter;

import com.ecommerce_distributed_backend.api_gateway.util.RouterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter {

    private final RouterValidator validator;
    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        if (validator.isSecured.test(request)) {

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new RuntimeException("Missing Authorization Header");
            }

            String authHeader =
                    request.getHeaders()
                            .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                Boolean isValid =
                        webClientBuilder.build()
                                .post()
                                .uri("http://AUTH-SERVICE/auth/validate")
                                .header(HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token)
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .block();

                if (!Boolean.TRUE.equals(isValid)) {
                    throw new RuntimeException("Invalid Token");
                }
            }
        }

        return chain.filter(exchange);
    }
}