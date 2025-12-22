package com.mymicroservice.gateway.filter;

import com.mymicroservice.gateway.util.MdcUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Component
@Slf4j
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger TRACE_MDC_LOGGER = LoggerFactory.getLogger("TRACE_MDC_LOGGER");
    private static final String SERVICE_NAME = "GATEWAY";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Получаем или генерируем requestId
        String requestId = request.getHeaders().getFirst(MdcUtil.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Устанавливаем MDC
        MdcUtil.setMdc(requestId, SERVICE_NAME);

        // Логируем через TRACE_MDC_LOGGER
        TRACE_MDC_LOGGER.info("{} {}",
                request.getMethod(),
                request.getURI().getPath());

        // Добавляем заголовок в ответ
        exchange.getResponse().getHeaders().set(MdcUtil.REQUEST_ID_HEADER, requestId);

        HttpMethod method = request.getMethod();
        if (method == HttpMethod.OPTIONS) {
            return chain.filter(exchange)
                    .contextWrite(MdcUtil.createReactorContext())
                    .doFinally(signalType -> MdcUtil.clearMdc());
        }

        String path = request.getURI().getPath();
        ServerHttpRequest mutatedRequest;

        if (path.contains("/login") || path.contains("/register") || path.contains("/auth/refresh")) {
            mutatedRequest = request.mutate()
                    .header("X-Internal-Call", "true")
                    .header("X-Source-Service", SERVICE_NAME)
                    .header(MdcUtil.REQUEST_ID_HEADER, requestId)
                    .build();
        } else {
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            // checking for Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                TRACE_MDC_LOGGER.warn("Missing or invalid Authorization header");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                MdcUtil.clearMdc();
                return exchange.getResponse().setComplete();
            }

            // pushing JWT into the services without modification
            mutatedRequest = request.mutate()
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .header("X-Internal-Call", "true")
                    .header("X-Source-Service", SERVICE_NAME)
                    .header(MdcUtil.REQUEST_ID_HEADER, requestId)
                    .build();
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // Передаем контекст и поддерживаем MDC
        String finalRequestId = requestId;
        return chain.filter(mutatedExchange)
                .contextWrite(MdcUtil.createReactorContext())
                .doOnEach(signal -> {
                    if (signal.isOnNext() || signal.isOnComplete() || signal.isOnError()) {
                        MdcUtil.restoreMdc(Context.of(
                                MdcUtil.REQUEST_ID_KEY, finalRequestId,
                                MdcUtil.SERVICE_NAME_KEY, SERVICE_NAME
                        ));
                    }
                })
                .doFinally(signalType -> MdcUtil.clearMdc());
    }



    /*@Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String query = request.getURI().getQuery();
        log.info("🔍 Gateway Filter - Path: {}, Query: {}", request.getURI().getPath(), query);

        // --- X-Request-Id ---
        String requestId = request.getHeaders().getFirst("X-Request-Id");
        if (requestId == null) {
            requestId = java.util.UUID.randomUUID().toString();
        }
        exchange.getResponse().getHeaders().add("X-Request-Id", requestId);
        // --------------------

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
                    .header("X-Request-Id", requestId) // !!!!
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
                .header("X-Request-Id", requestId) // !!!
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }*/

}
