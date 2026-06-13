package com.mymicroservice.gateway.unit.config;

import com.mymicroservice.gateway.config.WebClientConfig;
import com.mymicroservice.gateway.util.MdcUtil;
import com.mymicroservice.gateway.util.data.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = WebClientConfig.class)
@TestPropertySource(properties = {
        "services.userservice.url=http://userservice",
        "services.authservice.url=http://authservice",
        "services.orderservice.url=http://orderservice",
        "services.paymentservice.url=http://paymentservice"
})
class WebClientConfigTest {

    @Autowired
    @Qualifier("userServiceClient")
    private WebClient userServiceWebClient;

    @Autowired
    @Qualifier("authServiceClient")
    private WebClient authServiceWebClient;

    @Autowired
    @Qualifier("orderServiceClient")
    private WebClient orderServiceWebClient;

    @Autowired
    @Qualifier("paymentServiceClient")
    private WebClient paymentServiceWebClient;

    @Test
    void webClients_ShouldBeCreated_WhenConfigLoads() {
        assertNotNull(userServiceWebClient);
        assertNotNull(authServiceWebClient);
        assertNotNull(orderServiceWebClient);
        assertNotNull(paymentServiceWebClient);
    }

    @Test
    void mdcContextFilter_ShouldAddTraceAndInternalHeaders_WhenContextContainsValues() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        WebClient client = userServiceWebClient.mutate()
                .exchangeFunction(request -> {
                    capturedRequest.set(request);
                    return Mono.just(ClientResponse.create(HttpStatus.OK).build());
                })
                .build();

        StepVerifier.create(
                client.get()
                        .uri("/users")
                        .retrieve()
                        .toBodilessEntity()
                        .contextWrite(context -> context
                                .put(MdcUtil.TRACE_ID_KEY, TestConstants.TRACE_ID)
                                .put(MdcUtil.SERVICE_NAME_KEY, "userservice"))
        ).expectNextCount(1).verifyComplete();

        assertEquals(TestConstants.TRACE_ID, capturedRequest.get().headers().getFirst(MdcUtil.TRACE_ID_HEADER));
        assertEquals("true", capturedRequest.get().headers().getFirst("X-Internal-Call"));
        assertEquals("userservice", capturedRequest.get().headers().getFirst("X-Source-Service"));
    }

    @Test
    void mdcContextFilter_ShouldUseGatewayDefaults_WhenContextIsEmpty() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        WebClient client = authServiceWebClient.mutate()
                .exchangeFunction(request -> {
                    capturedRequest.set(request);
                    return Mono.just(ClientResponse.create(HttpStatus.OK).build());
                })
                .build();

        StepVerifier.create(
                client.post()
                        .uri("/auth/login")
                        .retrieve()
                        .toBodilessEntity()
        ).expectNextCount(1).verifyComplete();

        assertNull(capturedRequest.get().headers().getFirst(MdcUtil.TRACE_ID_HEADER));
        assertEquals("true", capturedRequest.get().headers().getFirst("X-Internal-Call"));
        assertEquals("GATEWAY", capturedRequest.get().headers().getFirst("X-Source-Service"));
    }
}
