package com.mymicroservice.gateway.unit.controller;

import com.mymicroservice.gateway.controller.FallbackController;
import com.mymicroservice.gateway.util.data.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

class FallbackControllerTest {

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToController(new FallbackController()).build();
    }

    @Test
    void userFallback_ShouldReturnUserServiceUnavailableMessage_WhenCalled() {
        webTestClient.get()
                .uri(TestConstants.FALLBACK_USER_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(TestConstants.FALLBACK_USER_MESSAGE);
    }

    @Test
    void orderFallback_ShouldReturnOrderServiceUnavailableMessage_WhenCalled() {
        webTestClient.get()
                .uri(TestConstants.FALLBACK_ORDER_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(TestConstants.FALLBACK_ORDER_MESSAGE);
    }

    @Test
    void authFallback_ShouldReturnAuthServiceUnavailableMessage_WhenCalled() {
        webTestClient.get()
                .uri(TestConstants.FALLBACK_AUTH_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(TestConstants.FALLBACK_AUTH_MESSAGE);
    }

    @Test
    void paymentFallback_ShouldReturnPaymentServiceUnavailableMessage_WhenCalled() {
        webTestClient.get()
                .uri(TestConstants.FALLBACK_PAYMENT_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(TestConstants.FALLBACK_PAYMENT_MESSAGE);
    }
}
