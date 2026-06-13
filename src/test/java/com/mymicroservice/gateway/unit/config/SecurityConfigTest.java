package com.mymicroservice.gateway.unit.config;

import com.mymicroservice.gateway.config.SecurityConfig;
import com.mymicroservice.gateway.security.CustomAccessDeniedHandler;
import com.mymicroservice.gateway.security.CustomAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void reactiveJwtDecoder_ShouldCreateDecoder_WhenPublicKeyIsValid() {
        ReactiveJwtDecoder decoder = new SecurityConfig().reactiveJwtDecoder();

        assertNotNull(decoder);
    }

    @Test
    void reactiveJwtDecoder_ShouldThrowRuntimeException_WhenPublicKeyLoadingFails() {
        SecurityConfig config = new SecurityConfig() {
            @Override
            protected RSAPublicKey getPublicKey() throws Exception {
                throw new Exception("invalid key");
            }
        };

        RuntimeException exception = assertThrows(RuntimeException.class, config::reactiveJwtDecoder);

        assertEquals("Failed to create ReactiveJwtDecoder", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void securityWebFilterChain_ShouldBuild_WhenDependenciesProvided() {
        SecurityConfig config = new SecurityConfig();
        CustomAuthenticationEntryPoint entryPoint = mock(CustomAuthenticationEntryPoint.class);
        CustomAccessDeniedHandler accessDeniedHandler = mock(CustomAccessDeniedHandler.class);

        SecurityWebFilterChain chain = config.securityWebFilterChain(
                ServerHttpSecurity.http(),
                entryPoint,
                accessDeniedHandler
        );

        assertNotNull(chain);
    }
}
