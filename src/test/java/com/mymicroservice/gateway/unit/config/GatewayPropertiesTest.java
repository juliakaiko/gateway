package com.mymicroservice.gateway.unit.config;

import com.mymicroservice.gateway.config.properties.CorsProperties;
import com.mymicroservice.gateway.config.properties.GatewayCustomProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GatewayPropertiesTest {

    @Test
    void gatewayCustomProperties_ShouldSupportEqualsHashCodeAndToString() {
        GatewayCustomProperties first = createGatewayProperties();
        GatewayCustomProperties second = createGatewayProperties();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotNull(first.toString());
        assertNotNull(first.getPublicPaths().toString());
        assertNotNull(first.getInternalPaths().toString());
    }

    @Test
    void corsProperties_ShouldSupportEqualsHashCodeAndToString() {
        CorsProperties first = createFullCorsProperties();
        CorsProperties second = createFullCorsProperties();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotNull(first.toString());
    }

    private GatewayCustomProperties createGatewayProperties() {
        GatewayCustomProperties properties = new GatewayCustomProperties();
        GatewayCustomProperties.Public publicPaths = new GatewayCustomProperties.Public();
        publicPaths.setPaths(List.of("/actuator"));
        GatewayCustomProperties.Internal internalPaths = new GatewayCustomProperties.Internal();
        internalPaths.setPaths(List.of("/register"));
        properties.setPublicPaths(publicPaths);
        properties.setInternalPaths(internalPaths);
        return properties;
    }

    private CorsProperties createFullCorsProperties() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOriginPatterns(List.of("http://localhost:3000"));
        properties.setAllowedMethods(List.of("GET"));
        properties.setAllowedHeaders(List.of("Authorization"));
        properties.setExposedHeaders(List.of("Content-Type"));
        properties.setMaxAge(3600L);
        properties.setAllowCredentials(true);
        return properties;
    }

    @Test
    void gatewayCustomProperties_ShouldStorePaths_WhenSet() {
        GatewayCustomProperties properties = createGatewayProperties();

        assertEquals("/actuator", properties.getPublicPaths().getPaths().getFirst());
        assertEquals("/register", properties.getInternalPaths().getPaths().getFirst());
    }

    @Test
    void corsProperties_ShouldStoreCorsSettings_WhenSet() {
        CorsProperties properties = createFullCorsProperties();

        assertNotNull(properties.getAllowedOriginPatterns());
        assertEquals(3600L, properties.getMaxAge());
        assertEquals(true, properties.getAllowCredentials());
    }
}
