package com.mymicroservice.gateway.unit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.gateway.config.JacksonConfig;
import com.mymicroservice.gateway.config.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigUnitTest {

    @Test
    void objectMapper_ShouldBeConfigured_WhenJacksonConfigUsed() {
        ObjectMapper mapper = new JacksonConfig().objectMapper();

        assertNotNull(mapper);
        assertFalse(mapper.getDeserializationConfig().isEnabled(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    void openAPI_ShouldContainBearerScheme_WhenOpenApiConfigUsed() {
        OpenAPI openAPI = new OpenApiConfig().openAPI();

        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication"));
        assertEquals("Gateway API", openAPI.getInfo().getTitle());
        assertEquals("http://localhost:8080", openAPI.getServers().getFirst().getUrl());
        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
    }
}
