package com.mymicroservice.gateway.filter;

import com.mymicroservice.gateway.util.MdcUtil;
import com.mymicroservice.gateway.config.properties.GatewayCustomProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

    private final GatewayCustomProperties gatewayCustomProperties;

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Getting or generating traceId
        String traceId = getOrGenerateTraceId(request);

        // Set MDC
        MdcUtil.setMdc(traceId, serviceName);

        logRequest(request);
        addTraceIdToResponse(exchange, traceId);

        // Handle OPTIONS requests (CORS preflight)
        if (isOptionsRequest(request.getMethod())) {
            return handleOptionsRequest(exchange, chain);
        }

        String path = request.getURI().getPath();

        // Skip JWT validation for public paths
        if (isPublicPath(path)) {
            return handlePublicPath(exchange, chain, traceId, path);
        }

        // Process request with authentication
        return processAuthenticatedRequest(exchange, chain, traceId, path);
    }

    /**
     * Gets or generates a trace ID from the request headers.
     *
     * @param request the HTTP request
     * @return existing traceId from header or newly generated UUID
     */
    private String getOrGenerateTraceId(ServerHttpRequest request) {
        String traceId = request.getHeaders().getFirst(MdcUtil.TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }

    /**
     * Logs the incoming request method and path.
     *
     * @param request the HTTP request
     */
    private void logRequest(ServerHttpRequest request) {
        log.info("{} {}", request.getMethod(), request.getURI().getPath());
    }

    /**
     * Adds trace ID header to the response.
     *
     * @param exchange the server web exchange
     * @param traceId  the trace ID
     */
    private void addTraceIdToResponse(ServerWebExchange exchange, String traceId) {
        exchange.getResponse().getHeaders().set(MdcUtil.TRACE_ID_HEADER, traceId);
    }

    /**
     * Checks if the request method is OPTIONS (CORS preflight).
     *
     * @param method the HTTP method
     * @return true if OPTIONS request
     */
    private boolean isOptionsRequest(HttpMethod method) {
        return method == HttpMethod.OPTIONS;
    }

    /**
     * Handles OPTIONS request (CORS preflight) - passes through without authentication.
     *
     * @param exchange the server web exchange
     * @param chain    the filter chain
     * @return Mono<Void> result
     */
    private Mono<Void> handleOptionsRequest(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .contextWrite(MdcUtil.createReactorContext())
                .doFinally(signalType -> MdcUtil.clearMdc());
    }

    /**
     * Checks if the request path is public (doesn't require JWT validation).
     *
     * @param path the request path
     * @return true if path is public
     */
    private boolean isPublicPath(String path) {
        List<String> publicPaths =
                gatewayCustomProperties.getPublicPaths() != null
                        ? gatewayCustomProperties.getPublicPaths().getPaths()
                        : List.of();

        return publicPaths.stream().anyMatch(path::startsWith);
    }

    /**
     * Handles public path request - passes through without JWT validation.
     *
     * @param exchange the server web exchange
     * @param chain    the filter chain
     * @param traceId  the trace ID for logging
     * @param path     the request path
     * @return Mono<Void> result
     */
    private Mono<Void> handlePublicPath(ServerWebExchange exchange, GatewayFilterChain chain,
                                        String traceId, String path) {
        log.info("Public path: {}, skipping JWT validation", path);
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(MdcUtil.TRACE_ID_HEADER, traceId)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();
        return chain.filter(mutatedExchange)
                .contextWrite(MdcUtil.createReactorContext())
                .doFinally(signalType -> MdcUtil.clearMdc());
    }

    /**
     * Processes authenticated request - validates JWT and forwards to services.
     *
     * @param exchange the server web exchange
     * @param chain    the filter chain
     * @param traceId  the trace ID for logging
     * @param path     the request path
     * @return Mono<Void> result
     */
    private Mono<Void> processAuthenticatedRequest(ServerWebExchange exchange, GatewayFilterChain chain,
                                                   String traceId, String path) {
        ServerHttpRequest mutatedRequest;

        // Check if this is an internal service-to-service call
        if (isInternalPath(path)) {
            mutatedRequest = buildInternalRequest(exchange.getRequest(), traceId);
        } else {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (isValidJwtToken(authHeader)) {
                mutatedRequest = buildAuthenticatedRequest(exchange.getRequest(), authHeader, traceId);
            } else {
                // Delegate authentication to Spring Security (401 via CustomAuthenticationEntryPoint)
                mutatedRequest = exchange.getRequest().mutate()
                        .header(MdcUtil.TRACE_ID_HEADER, traceId)
                        .build();
            }
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return forwardRequest(mutatedExchange, chain, traceId);
    }

    /**
     * Checks if the path is an internal service-to-service path.
     *
     * @param path the request path
     * @return true if internal path
     */
    private boolean isInternalPath(String path) {
        List<String> internalPaths =
                gatewayCustomProperties.getInternalPaths() != null
                        ? gatewayCustomProperties.getInternalPaths().getPaths()
                        : List.of();

        return internalPaths.stream().anyMatch(configuredPath -> matchesPath(path, configuredPath));
    }

    private boolean matchesPath(String path, String configuredPath) {
        return path.equals(configuredPath) || path.startsWith(configuredPath + "/");
    }

    /**
     * Builds request for internal service-to-service call (no JWT validation required).
     *
     * @param request the original request
     * @param traceId the trace ID
     * @return mutated request with internal call headers
     */
    private ServerHttpRequest buildInternalRequest(ServerHttpRequest request, String traceId) {
        return request.mutate()
                .header("X-Internal-Call", "true")
                .header("X-Source-Service", serviceName)
                .header(MdcUtil.TRACE_ID_HEADER, traceId)
                .build();
    }

    /**
     * Validates JWT token format.
     *
     * @param authHeader the Authorization header value
     * @return true if token is valid Bearer token
     */
    private boolean isValidJwtToken(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

    /**
     * Builds request with JWT token for authenticated service call.
     *
     * @param request    the original request
     * @param authHeader the Authorization header with JWT
     * @param traceId    the trace ID
     * @return mutated request with authentication headers
     */
    private ServerHttpRequest buildAuthenticatedRequest(ServerHttpRequest request, String authHeader, String traceId) {
        return request.mutate()
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .header("X-Internal-Call", "true")
                .header("X-Source-Service", serviceName)
                .header(MdcUtil.TRACE_ID_HEADER, traceId)
                .build();
    }

    /**
     * Forwards request to the next filter chain with MDC context propagation.
     *
     * @param exchange the server web exchange
     * @param chain    the filter chain
     * @param traceId  the trace ID for context restoration
     * @return Mono<Void> result
     */
    private Mono<Void> forwardRequest(ServerWebExchange exchange, GatewayFilterChain chain, String traceId) {
        return chain.filter(exchange)
                .contextWrite(MdcUtil.createReactorContext())
                .doOnEach(signal -> {
                    if (signal.isOnNext() || signal.isOnComplete() || signal.isOnError()) {
                        MdcUtil.restoreMdc(Context.of(
                                MdcUtil.TRACE_ID_KEY, traceId,
                                MdcUtil.SERVICE_NAME_KEY, serviceName
                        ));
                    }
                })
                .doFinally(signalType -> MdcUtil.clearMdc());
    }
}
