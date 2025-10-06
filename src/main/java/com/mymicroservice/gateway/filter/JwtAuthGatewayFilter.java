package com.mymicroservice.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthGatewayFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String query = request.getURI().getQuery();
        log.info("🔍 Gateway Filter - Path: {}, Query: {}", request.getURI().getPath(), query);

        HttpMethod method = exchange.getRequest().getMethod();
        if (method == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();

        if (path.contains("/login") || path.contains("/register") || path.contains("/auth/refresh")) {
            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-Internal-Call", "true")
                    .header("X-Source-Service", "GATEWAY")
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        // checking for Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // pushing JWT into the services without modification
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .header("X-Internal-Call", "true")
                .header("X-Source-Service", "GATEWAY")
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

}
