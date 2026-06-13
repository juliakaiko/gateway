package com.mymicroservice.gateway.unit.controller;

import com.mymicroservice.gateway.controller.ActuatorController;
import com.mymicroservice.gateway.util.data.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActuatorControllerTest {

    @Mock
    private WebClient authServiceClient;

    @Mock
    private WebClient userServiceClient;

    @Mock
    private WebClient orderServiceClient;

    @Mock
    private WebClient paymentServiceClient;

    private ActuatorController actuatorController;

    @BeforeEach
    void setUp() {
        actuatorController = new ActuatorController(
                authServiceClient,
                userServiceClient,
                orderServiceClient,
                paymentServiceClient
        );
        ReflectionTestUtils.setField(actuatorController, "serviceName", TestConstants.SERVICE_NAME);
    }

    @Test
    void getAllServiceHealth_ShouldReturnUpForAllServices_WhenAllServicesAreHealthy() {
        mockHealthResponse(authServiceClient, "UP");
        mockHealthResponse(userServiceClient, "UP");
        mockHealthResponse(orderServiceClient, "UP");
        mockHealthResponse(paymentServiceClient, "UP");

        StepVerifier.create(actuatorController.getAllServiceHealth())
                .assertNext(statusMap -> {
                    assertEquals("UP", statusMap.get("authservice"));
                    assertEquals("UP", statusMap.get("userservice"));
                    assertEquals("UP", statusMap.get("orderservice"));
                    assertEquals("UP", statusMap.get("paymentservice"));
                })
                .verifyComplete();
    }

    @Test
    void getAllServiceHealth_ShouldReturnDownForFailedService_WhenHealthCheckFails() {
        mockHealthResponse(authServiceClient, "UP");
        mockHealthResponse(userServiceClient, "UP");
        mockHealthResponse(orderServiceClient, "UP");
        mockHealthError(paymentServiceClient);

        StepVerifier.create(actuatorController.getAllServiceHealth())
                .assertNext(statusMap -> {
                    assertEquals("UP", statusMap.get("authservice"));
                    assertEquals("UP", statusMap.get("userservice"));
                    assertEquals("UP", statusMap.get("orderservice"));
                    assertEquals("DOWN", statusMap.get("paymentservice"));
                })
                .verifyComplete();
    }

    @SuppressWarnings("rawtypes")
    private void mockHealthResponse(WebClient client, String status) {
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(client.get()).thenReturn(uriSpec);
        when(uriSpec.uri("/actuator/health")).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of("status", status)));
    }

    @SuppressWarnings("rawtypes")
    private void mockHealthError(WebClient client) {
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(client.get()).thenReturn(uriSpec);
        when(uriSpec.uri("/actuator/health")).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new RuntimeException("Service unavailable")));
    }
}
