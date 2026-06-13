package com.mymicroservice.gateway.integration.config;

import com.mymicroservice.gateway.config.properties.CorsProperties;
import com.mymicroservice.gateway.config.properties.GatewayCustomProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GatewayPropertiesIT {

    @Autowired
    private GatewayCustomProperties gatewayCustomProperties;

    @Autowired
    private CorsProperties corsProperties;

    @Test
    void getPublicPaths_ShouldContainExpectedPaths_WhenTestProfileIsActive() {
        assertThat(gatewayCustomProperties.getPublicPaths()).isNotNull();
        assertThat(gatewayCustomProperties.getPublicPaths().getPaths())
                .containsExactly(
                        "/v3/api-docs",
                        "/api-docs",
                        "/swagger-ui",
                        "/swagger-ui/**",
                        "/actuator",
                        "/auth/v3/api-docs",
                        "/api/users/v3/api-docs",
                        "/api/items/v3/api-docs",
                        "/api/payments/v3/api-docs"
                );
    }

    @Test
    void getInternalPaths_ShouldContainExpectedPaths_WhenTestProfileIsActive() {
        assertThat(gatewayCustomProperties.getInternalPaths()).isNotNull();
        assertThat(gatewayCustomProperties.getInternalPaths().getPaths())
                .containsExactly(
                        "/login",
                        "/register",
                        "/auth/refresh"
                );
    }

    @Test
    void getAllowedOriginPatterns_ShouldContainExpectedOrigins_WhenTestProfileIsActive() {
        assertThat(corsProperties.getAllowedOriginPatterns())
                .containsExactly(
                        "http://innowise-project.local",
                        "https://innowise-project.local",
                        "http://localhost:3000",
                        "http://127.0.0.1:[*]",
                        "http://frontend",
                        "http://gateway:8080"
                );
    }

    @Test
    void getAllowedMethods_ShouldContainExpectedMethods_WhenTestProfileIsActive() {
        assertThat(corsProperties.getAllowedMethods())
                .containsExactly("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
    }

    @Test
    void getAllowedHeaders_ShouldContainExpectedHeaders_WhenTestProfileIsActive() {
        assertThat(corsProperties.getAllowedHeaders())
                .containsExactly(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With",
                        "Accept",
                        "Origin",
                        "Access-Control-Request-Method",
                        "Access-Control-Request-Headers"
                );
    }

    @Test
    void getExposedHeaders_ShouldContainExpectedHeaders_WhenTestProfileIsActive() {
        assertThat(corsProperties.getExposedHeaders())
                .containsExactly("Authorization", "Content-Type");
    }

    @Test
    void getMaxAge_ShouldReturnConfiguredValue_WhenTestProfileIsActive() {
        assertThat(corsProperties.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    void getAllowCredentials_ShouldReturnTrue_WhenTestProfileIsActive() {
        assertThat(corsProperties.getAllowCredentials()).isTrue();
    }
}
