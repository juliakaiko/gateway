package com.mymicroservice.gateway.integration.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class ConfigBeansIT {

    @Autowired
    private CorsWebFilter corsWebFilter;

    @Autowired
    @Qualifier("authServiceClient")
    private WebClient authServiceClient;

    @Autowired
    @Qualifier("userServiceClient")
    private WebClient userServiceClient;

    @Autowired
    @Qualifier("orderServiceClient")
    private WebClient orderServiceClient;

    @Autowired
    @Qualifier("paymentServiceClient")
    private WebClient paymentServiceClient;

    @Test
    void corsWebFilter_ShouldBeCreated_WhenContextLoads() {
        assertNotNull(corsWebFilter);
    }

    @Test
    void webClients_ShouldBeCreated_WhenContextLoads() {
        assertNotNull(authServiceClient);
        assertNotNull(userServiceClient);
        assertNotNull(orderServiceClient);
        assertNotNull(paymentServiceClient);
    }
}
