package com.mymicroservice.gateway.unit.filter;

import com.mymicroservice.gateway.filter.InternalTraceIdWebFilter;
import com.mymicroservice.gateway.util.MdcUtil;
import com.mymicroservice.gateway.util.data.TestConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalTraceIdWebFilterTest {

    @Mock
    private WebFilterChain chain;

    private InternalTraceIdWebFilter filter;

    @BeforeEach
    void setUp() {
        filter = new InternalTraceIdWebFilter();
        ReflectionTestUtils.setField(filter, "serviceName", TestConstants.SERVICE_NAME);
    }

    @AfterEach
    void tearDown() {
        MdcUtil.clearMdc();
    }

    @Test
    void filter_ShouldPropagateProvidedTraceId_WhenHeaderIsPresent() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/register")
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
    void filter_ShouldGenerateTraceId_WhenHeaderIsMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/register").build()
        );
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        String responseTraceId = exchange.getResponse().getHeaders().getFirst(MdcUtil.TRACE_ID_HEADER);
        assertNotNull(responseTraceId);
        assertNotEquals("", responseTraceId);

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertEquals(responseTraceId, captor.getValue().getRequest().getHeaders().getFirst(MdcUtil.TRACE_ID_HEADER));
    }

    @Test
    void filter_ShouldClearMdc_WhenRequestCompletes() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/register")
                        .header(MdcUtil.TRACE_ID_HEADER, TestConstants.TRACE_ID)
                        .build()
        );
        MdcUtil.setMdc(TestConstants.TRACE_ID, TestConstants.SERVICE_NAME);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertNull(org.slf4j.MDC.get(MdcUtil.TRACE_ID_KEY));
        assertNull(org.slf4j.MDC.get(MdcUtil.SERVICE_NAME_KEY));
    }
}
