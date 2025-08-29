package com.mymicroservice.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthGatewayFilter implements GlobalFilter {

    /**
     * A {@link GlobalFilter} implementation for handling authentication and request forwarding in the Gateway.
     *
     * <p>This filter is applied to all incoming requests at the Gateway level before routing them
     * to downstream microservices. It performs the following responsibilities:
     *
     * <ul>
     *   <li><b>Endpoints Bypass:</b>
     *       Skips authentication for endpoints such as <code>/login</code> and <code>/register</code>.</li>
     *
     *   <li><b>Internal Service Calls:</b>
     *       Detects internal service communication paths (containing <code>/api/internal/</code>) and
     *       appends custom headers (<code>X-Internal-Call</code>, <code>X-Source-Service</code>) to
     *       identify the Gateway as the request source.</li>
     *
     *   <li><b>JWT Authentication:</b>
     *       Validates the presence and format of the <code>Authorization</code> header.
     *       <ul>
     *         <li>If missing or invalid → responds with <b>401 Unauthorized</b>.</li>
     *         <li>If valid → forwards the JWT token downstream in the <code>Authorization</code> header.</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <p><b>Usage:</b>
     * Registered as a Spring {@link Component}, this filter is automatically applied to all requests
     * in the Spring Cloud Gateway context.
     *
     * @author Your Name
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // skip "login" and "register" without checking
        if (path.contains("/login") || path.contains("/register")) {
            return chain.filter(exchange);
        }

        //  If this is an internal call from the Gateway to the services, add headers
        if (isInternalServiceCall(exchange.getRequest())) {
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-Internal-Call", "true")
                    .header("X-Source-Service", "GATEWAY")
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }

        // checking fo Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // pushing JWT further into the services without modification
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isInternalServiceCall(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return path.contains("/api/internal/");
    }
}
