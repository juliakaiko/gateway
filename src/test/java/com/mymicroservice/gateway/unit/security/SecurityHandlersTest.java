package com.mymicroservice.gateway.unit.security;

import com.mymicroservice.gateway.config.properties.CorsProperties;
import com.mymicroservice.gateway.security.CustomAccessDeniedHandler;
import com.mymicroservice.gateway.security.CustomAuthenticationEntryPoint;
import com.mymicroservice.gateway.util.CorsHeadersWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityHandlersTest {

    private CustomAuthenticationEntryPoint authenticationEntryPoint;
    private CustomAccessDeniedHandler accessDeniedHandler;

    @BeforeEach
    void setUp() {
        CorsHeadersWriter corsHeadersWriter = new CorsHeadersWriter(createCorsProperties());
        authenticationEntryPoint = new CustomAuthenticationEntryPoint(corsHeadersWriter);
        accessDeniedHandler = new CustomAccessDeniedHandler(corsHeadersWriter);
    }

    @Test
    void commence_ShouldReturnUnauthorizedJsonWithCors_WhenAuthenticationFails() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/profile")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .build()
        );

        StepVerifier.create(authenticationEntryPoint.commence(exchange, new BadCredentialsException("Invalid token")))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
        assertEquals("http://localhost:3000", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("true", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));

        String body = exchange.getResponse().getBodyAsString().block();
        assertTrue(body.contains("\"status\": 401"));
        assertTrue(body.contains("\"message\": \"Authentication required\""));
    }

    @Test
    void handle_ShouldReturnForbiddenJsonWithCors_WhenAccessIsDenied() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/admin")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .build()
        );

        StepVerifier.create(accessDeniedHandler.handle(exchange, new AccessDeniedException("Forbidden")))
                .verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
        assertEquals("http://localhost:3000", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        String body = exchange.getResponse().getBodyAsString().block();
        assertTrue(body.contains("\"status\": 403"));
        assertTrue(body.contains("\"message\": \"Access denied\""));
    }

    private CorsProperties createCorsProperties() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOriginPatterns(List.of("http://localhost:3000"));
        properties.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        properties.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        properties.setExposedHeaders(List.of("Authorization", "Content-Type"));
        properties.setAllowCredentials(true);
        properties.setMaxAge(3600L);
        return properties;
    }
}
