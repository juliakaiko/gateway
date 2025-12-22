package com.mymicroservice.gateway.util;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;

@UtilityClass
public class MdcUtil {

    public static final String REQUEST_ID_KEY = "requestId";
    public static final String SERVICE_NAME_KEY = "serviceName";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * Создает контекст Reactor с MDC значениями
     */
    public static Context createReactorContext() {
        String requestId = MDC.get(REQUEST_ID_KEY);
        String serviceName = MDC.get(SERVICE_NAME_KEY);

        return Context.of(
                REQUEST_ID_KEY, Optional.ofNullable(requestId).orElse(""),
                SERVICE_NAME_KEY, Optional.ofNullable(serviceName).orElse("")
        );
    }

    /**
     * Восстанавливает MDC из контекста Reactor
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
     * Устанавливает MDC значения
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
     * Очищает MDC
     */
    public static void clearMdc() {
        MDC.clear();
    }

    /**
     * Оператор для поддержания MDC в реактивной цепочке
     */
    public static <T> Mono<T> withMdc(Mono<T> mono) {
        return Mono.deferContextual(contextView -> {
            // Восстанавливаем MDC из контекста
            restoreMdc(Context.of(contextView));
            return mono
                    .contextWrite(ctx -> ctx.putAll(contextView))
                    .doOnEach(signal -> {
                        // Поддерживаем MDC для каждого сигнала
                        if (signal.isOnNext() || signal.isOnComplete() || signal.isOnError()) {
                            restoreMdc(Context.of(contextView));
                        }
                    });
        });
    }
}