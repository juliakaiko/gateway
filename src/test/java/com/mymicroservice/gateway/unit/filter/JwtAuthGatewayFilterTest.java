package com.mymicroservice.gateway.unit.filter;

import com.mymicroservice.gateway.config.properties.GatewayCustomProperties;
import com.mymicroservice.gateway.filter.JwtAuthGatewayFilter;
import com.mymicroservice.gateway.util.MdcUtil;
import com.mymicroservice.gateway.util.data.TestConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthGatewayFilterTest {

    @Mock
    private GatewayFilterChain chain;

    private JwtAuthGatewayFilter filter;
    private GatewayCustomProperties gatewayCustomProperties;

    @BeforeEach
    void setUp() {
        gatewayCustomProperties = new GatewayCustomProperties();

        GatewayCustomProperties.Public publicPaths = new GatewayCustomProperties.Public();
        publicPaths.setPaths(List.of("/actuator", "/swagger-ui"));

        GatewayCustomProperties.Internal internalPaths = new GatewayCustomProperties.Internal();
        internalPaths.setPaths(List.of("/register", "/login"));

        gatewayCustomProperties.setPublicPaths(publicPaths);
        gatewayCustomProperties.setInternalPaths(internalPaths);

        filter = new JwtAuthGatewayFilter(gatewayCustomProperties);
        ReflectionTestUtils.setField(filter, "serviceName", TestConstants.SERVICE_NAME);
    }

    @AfterEach
    void tearDown() {
        MdcUtil.clearMdc();
    }

    @Test
    void filter_ShouldBypassJwtValidation_WhenPublicPathRequested() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health").build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertNotNull(exchange.getResponse().getHeaders().getFirst(MdcUtil.TRACE_ID_HEADER));
        assertNotNull(captor.getValue().getRequest().getHeaders().getFirst(MdcUtil.TRACE_ID_HEADER));
    }

    @Test
    void filter_ShouldAddInternalHeaders_WhenInternalPathRequested() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/register").build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        assertEquals("true", captor.getValue().getRequest().getHeaders().getFirst("X-Internal-Call"));
        assertEquals(TestConstants.SERVICE_NAME, captor.getValue().getRequest().getHeaders().getFirst("X-Source-Service"));
    }

    @Test
    void filter_ShouldForwardRequest_WhenJwtIsMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/profile").build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    void filter_ShouldNotTreatPathAsInternal_WhenPathOnlyContainsLoginSubstring() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/my-login-page")
                        .header(HttpHeaders.AUTHORIZATION, TestConstants.BEARER_PREFIX + TestConstants.TEST_JWT)
                        .build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertEquals("true", captor.getValue().getRequest().getHeaders().getFirst("X-Internal-Call"));
    }

    @Test
    void filter_ShouldForwardRequest_WhenValidBearerTokenProvided() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/profile")
                        .header(HttpHeaders.AUTHORIZATION, TestConstants.BEARER_PREFIX + TestConstants.TEST_JWT)
                        .build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        assertEquals(
                TestConstants.BEARER_PREFIX + TestConstants.TEST_JWT,
                captor.getValue().getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)
        );
        assertEquals("true", captor.getValue().getRequest().getHeaders().getFirst("X-Internal-Call"));
    }

    @Test
    void filter_ShouldPassThroughOptionsRequest_WhenPreflightRequested() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.options("/api/users/profile").build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_ShouldPropagateTraceId_WhenHeaderProvided() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health")
                        .header(MdcUtil.TRACE_ID_HEADER, TestConstants.TRACE_ID)
                        .build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(TestConstants.TRACE_ID, exchange.getResponse().getHeaders().getFirst(MdcUtil.TRACE_ID_HEADER));

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertEquals(TestConstants.TRACE_ID, captor.getValue().getRequest().getHeaders().getFirst(MdcUtil.TRACE_ID_HEADER));
    }

    @Test
    void filter_ShouldTreatNestedInternalPathAsInternal_WhenPathHasSubresource() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/login/oauth").build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertEquals("true", captor.getValue().getRequest().getHeaders().getFirst("X-Internal-Call"));
    }

    @Test
    void filter_ShouldForwardWithoutInternalHeaders_WhenAuthorizationHeaderIsInvalid() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Basic invalid")
                        .build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertNull(captor.getValue().getRequest().getHeaders().getFirst("X-Internal-Call"));
    }

    @Test
    void filter_ShouldGenerateTraceId_WhenHeaderIsBlank() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health")
                        .header(MdcUtil.TRACE_ID_HEADER, "   ")
                        .build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        String traceId = exchange.getResponse().getHeaders().getFirst(MdcUtil.TRACE_ID_HEADER);
        assertNotNull(traceId);
    }

    @Test
    void filter_ShouldUseEmptyPublicPaths_WhenPublicPathsAreNull() {
        gatewayCustomProperties.setPublicPaths(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/profile")
                        .header(HttpHeaders.AUTHORIZATION, TestConstants.BEARER_PREFIX + TestConstants.TEST_JWT)
                        .build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    void filter_ShouldUseEmptyInternalPaths_WhenInternalPathsAreNull() {
        gatewayCustomProperties.setInternalPaths(null);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/my-login-page")
                        .header(HttpHeaders.AUTHORIZATION, TestConstants.BEARER_PREFIX + TestConstants.TEST_JWT)
                        .build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertEquals("true", captor.getValue().getRequest().getHeaders().getFirst("X-Internal-Call"));
    }

    @Test
    void getOrder_ShouldReturnHighestPrecedence_WhenCalled() {
        assertEquals(Integer.MIN_VALUE, filter.getOrder());
    }
}
