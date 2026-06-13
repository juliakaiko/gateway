package com.mymicroservice.gateway.unit.util;

import com.mymicroservice.gateway.config.properties.CorsProperties;
import com.mymicroservice.gateway.util.CorsHeadersWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorsHeadersWriterTest {

    private CorsHeadersWriter corsHeadersWriter;

    @BeforeEach
    void setUp() {
        corsHeadersWriter = new CorsHeadersWriter(createCorsProperties());
    }

    @Test
    void applyCorsHeaders_ShouldSetAllowedOriginAndCredentials_WhenOriginIsAllowed() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/profile")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .build()
        );

        corsHeadersWriter.applyCorsHeaders(exchange);

        assertEquals("http://localhost:3000", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("true", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertEquals("GET, POST", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
        assertEquals("Authorization, Content-Type", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
        assertEquals("Authorization", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    @Test
    void applyCorsHeaders_ShouldSkipAllowOrigin_WhenOriginIsNotAllowed() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/profile")
                        .header(HttpHeaders.ORIGIN, "http://evil.example")
                        .build()
        );

        corsHeadersWriter.applyCorsHeaders(exchange);

        assertNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("true", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void applyCorsHeaders_ShouldSkipAllowOrigin_WhenOriginHeaderIsMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/profile").build()
        );

        corsHeadersWriter.applyCorsHeaders(exchange);

        assertNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("GET, POST", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    @Test
    void applyCorsHeaders_ShouldSkipOptionalHeaders_WhenListsAreEmpty() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOriginPatterns(List.of("http://localhost:3000"));
        properties.setAllowCredentials(false);
        properties.setMaxAge(null);
        CorsHeadersWriter writer = new CorsHeadersWriter(properties);

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/profile")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .build()
        );

        writer.applyCorsHeaders(exchange);

        assertEquals("http://localhost:3000", exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
        assertNull(exchange.getResponse().getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    private CorsProperties createCorsProperties() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOriginPatterns(List.of("http://localhost:3000"));
        properties.setAllowedMethods(List.of("GET", "POST"));
        properties.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        properties.setExposedHeaders(List.of("Authorization"));
        properties.setAllowCredentials(true);
        properties.setMaxAge(3600L);
        return properties;
    }
}
