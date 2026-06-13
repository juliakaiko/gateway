package com.mymicroservice.gateway.unit.util;

import com.mymicroservice.gateway.util.MdcUtil;
import com.mymicroservice.gateway.util.data.TestConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MdcUtilTest {

    @BeforeEach
    @AfterEach
    void clearMdcState() {
        MdcUtil.clearMdc();
    }

    @Test
    void setMdc_ShouldPutValuesInMdc_WhenValidValuesProvided() {
        MdcUtil.setMdc(TestConstants.TRACE_ID, TestConstants.SERVICE_NAME);

        assertEquals(TestConstants.TRACE_ID, MDC.get(MdcUtil.TRACE_ID_KEY));
        assertEquals(TestConstants.SERVICE_NAME, MDC.get(MdcUtil.SERVICE_NAME_KEY));
    }

    @Test
    void clearMdc_ShouldRemoveAllValues_WhenMdcIsPopulated() {
        MdcUtil.setMdc(TestConstants.TRACE_ID, TestConstants.SERVICE_NAME);

        MdcUtil.clearMdc();

        assertNull(MDC.get(MdcUtil.TRACE_ID_KEY));
        assertNull(MDC.get(MdcUtil.SERVICE_NAME_KEY));
    }

    @Test
    void restoreMdc_ShouldRestoreValuesFromContext_WhenContextContainsKeys() {
        Context context = Context.of(
                MdcUtil.TRACE_ID_KEY, TestConstants.TRACE_ID,
                MdcUtil.SERVICE_NAME_KEY, TestConstants.SERVICE_NAME
        );

        MdcUtil.restoreMdc(context);

        assertEquals(TestConstants.TRACE_ID, MDC.get(MdcUtil.TRACE_ID_KEY));
        assertEquals(TestConstants.SERVICE_NAME, MDC.get(MdcUtil.SERVICE_NAME_KEY));
    }

    @Test
    void createReactorContext_ShouldUseExistingMdcValues_WhenMdcIsSet() {
        MdcUtil.setMdc(TestConstants.TRACE_ID, TestConstants.SERVICE_NAME);

        Context context = MdcUtil.createReactorContext();

        assertEquals(TestConstants.TRACE_ID, context.get(MdcUtil.TRACE_ID_KEY));
        assertEquals(TestConstants.SERVICE_NAME, context.get(MdcUtil.SERVICE_NAME_KEY));
    }

    @Test
    void createReactorContext_ShouldUseDefaults_WhenMdcIsEmpty() {
        Context context = MdcUtil.createReactorContext();

        assertNotNull(context.get(MdcUtil.TRACE_ID_KEY));
        assertEquals(TestConstants.SERVICE_NAME, context.get(MdcUtil.SERVICE_NAME_KEY));
    }

    @Test
    void withMdc_ShouldPropagateContextAndRestoreMdc_WhenMonoCompletes() {
        Mono<String> mono = Mono.deferContextual(contextView -> {
            MdcUtil.restoreMdc(Context.of(contextView));
            return Mono.just(MDC.get(MdcUtil.TRACE_ID_KEY));
        });

        StepVerifier.create(MdcUtil.withMdc(mono)
                        .contextWrite(Context.of(
                                MdcUtil.TRACE_ID_KEY, TestConstants.TRACE_ID,
                                MdcUtil.SERVICE_NAME_KEY, TestConstants.SERVICE_NAME
                        )))
                .expectNext(TestConstants.TRACE_ID)
                .verifyComplete();
    }

}
