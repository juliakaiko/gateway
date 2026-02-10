package com.mymicroservice.gateway.filter;

import com.mymicroservice.gateway.util.MdcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

    @Value("${spring.application.name}")
    private String serviceName;

    private static final Set<String> INTERNAL_PATHS = Set.of(
            "/login", "/register", "/auth/refresh"
    );

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Getting or generating requestId
        String requestId = request.getHeaders().getFirst(MdcUtil.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Set MDC
        MdcUtil.setMdc(requestId, serviceName);

        log.info("{} {}",
                request.getMethod(),
                request.getURI().getPath());

        // Adding a header to the response
        exchange.getResponse().getHeaders().set(MdcUtil.REQUEST_ID_HEADER, requestId);

        HttpMethod method = request.getMethod();
        if (method == HttpMethod.OPTIONS) {
            return chain.filter(exchange)
                    .contextWrite(MdcUtil.createReactorContext())
                    .doFinally(signalType -> MdcUtil.clearMdc());
        }

        String path = request.getURI().getPath();
        ServerHttpRequest mutatedRequest;

        if (INTERNAL_PATHS.stream().anyMatch(path::contains)) {
            mutatedRequest = request.mutate()
                    .header("X-Internal-Call", "true")
                    .header("X-Source-Service", serviceName)
                    .header(MdcUtil.REQUEST_ID_HEADER, requestId)
                    .build();
        } else {
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            // Checking for Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                MdcUtil.clearMdc();
                return exchange.getResponse().setComplete();
            }

            // Pushing JWT into the services without modification
            mutatedRequest = request.mutate()
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .header("X-Internal-Call", "true")
                    .header("X-Source-Service", serviceName)
                    .header(MdcUtil.REQUEST_ID_HEADER, requestId)
                    .build();
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // Passing the context and supporting MDC Context
        String finalRequestId = requestId;
        return chain.filter(mutatedExchange)
                .contextWrite(MdcUtil.createReactorContext())
                .doOnEach(signal -> {
                    if (signal.isOnNext() || signal.isOnComplete() || signal.isOnError()) {
                        MdcUtil.restoreMdc(Context.of(
                                MdcUtil.REQUEST_ID_KEY, finalRequestId,
                                MdcUtil.SERVICE_NAME_KEY, serviceName
                        ));
                    }
                })
                .doFinally(signalType -> MdcUtil.clearMdc());
    }

}
