package com.mymicroservice.gateway.util;

import com.mymicroservice.gateway.config.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.ServerWebExchange;

@Component
@RequiredArgsConstructor
public class CorsHeadersWriter {

    private final CorsProperties corsProperties;

    public void applyCorsHeaders(ServerWebExchange exchange) {
        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());
        corsConfiguration.setAllowedMethods(corsProperties.getAllowedMethods());
        corsConfiguration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        corsConfiguration.setExposedHeaders(corsProperties.getExposedHeaders());
        corsConfiguration.setAllowCredentials(corsProperties.getAllowCredentials());
        if (corsProperties.getMaxAge() != null) {
            corsConfiguration.setMaxAge(corsProperties.getMaxAge());
        }

        String origin = requestHeaders.getFirst(HttpHeaders.ORIGIN);
        if (origin != null) {
            String allowedOrigin = corsConfiguration.checkOrigin(origin);
            if (allowedOrigin != null) {
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
            }
        }

        if (Boolean.TRUE.equals(corsProperties.getAllowCredentials())) {
            responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        if (corsProperties.getAllowedMethods() != null && !corsProperties.getAllowedMethods().isEmpty()) {
            responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, String.join(", ", corsProperties.getAllowedMethods()));
        }

        if (corsProperties.getAllowedHeaders() != null && !corsProperties.getAllowedHeaders().isEmpty()) {
            responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(", ", corsProperties.getAllowedHeaders()));
        }

        if (corsProperties.getExposedHeaders() != null && !corsProperties.getExposedHeaders().isEmpty()) {
            responseHeaders.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, String.join(", ", corsProperties.getExposedHeaders()));
        }
    }
}
