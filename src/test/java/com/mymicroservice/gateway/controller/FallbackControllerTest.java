package com.mymicroservice.gateway.controller;

import com.mymicroservice.gateway.configuration.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(FallbackController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void userFallback_shouldReturnUserServiceUnavailableMessage() {
        webTestClient.get()
                .uri("/fallback/user")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("User Service is unavailable. Please try again later.");
    }

    @Test
    void orderFallback_shouldReturnOrderServiceUnavailableMessage() {
        webTestClient.get()
                .uri("/fallback/order")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Order Service is unavailable. Please try again later.");
    }

    @Test
    void authFallback_shouldReturnAuthServiceUnavailableMessage() {
        webTestClient.get()
                .uri("/fallback/auth")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Authentication Service is unavailable. Please try again later.");
    }

}

