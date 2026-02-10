package com.mymicroservice.gateway.util;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class MdcUtil {

    public static final String REQUEST_ID_KEY = "requestId";
    public static final String SERVICE_NAME_KEY = "serviceName";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * Creates a Reactor context with MDC values
     */
    public static Context createReactorContext() {
        String requestId = MDC.get(REQUEST_ID_KEY);
        String serviceName = MDC.get(SERVICE_NAME_KEY);

        return Context.of(
                REQUEST_ID_KEY, Optional.ofNullable(requestId).orElse(UUID.randomUUID().toString()),
                SERVICE_NAME_KEY, Optional.ofNullable(serviceName).orElse("gateway")
        );
    }

    /**
     * Restores the MDC from the Reactor context
     */
    public static void restoreMdc(Context context) {
        if (context.hasKey(REQUEST_ID_KEY)) {
            String requestId = context.get(REQUEST_ID_KEY);
            if (!requestId.isEmpty()) {
                MDC.put(REQUEST_ID_KEY, requestId);
            }
        }
        if (context.hasKey(SERVICE_NAME_KEY)) {
            String serviceName = context.get(SERVICE_NAME_KEY);
            if (!serviceName.isEmpty()) {
                MDC.put(SERVICE_NAME_KEY, serviceName);
            }
        }
    }

    /**
     * Sets the MDC values
     */
    public static void setMdc(String requestId, String serviceName) {
        if (requestId != null && !requestId.isEmpty()) {
            MDC.put(REQUEST_ID_KEY, requestId);
        }
        if (serviceName != null && !serviceName.isEmpty()) {
            MDC.put(SERVICE_NAME_KEY, serviceName);
        }
    }

    /**
     * Cleans up the MDC
     */
    public static void clearMdc() {
        MDC.clear();
    }

    /**
     * Operator for maintaining MDC in the reactive chain
     */
    public static <T> Mono<T> withMdc(Mono<T> mono) {
        return Mono.deferContextual(contextView -> {
            // Restoring MDC from context
            restoreMdc(Context.of(contextView));
            return mono
                    .contextWrite(ctx -> ctx.putAll(contextView))
                    .doOnEach(signal -> {
                        // Support MDC for each signal
                        if (signal.isOnNext() || signal.isOnComplete() || signal.isOnError()) {
                            restoreMdc(Context.of(contextView));
                        }
                    });
        });
    }
}