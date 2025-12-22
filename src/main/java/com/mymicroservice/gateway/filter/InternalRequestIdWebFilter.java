package com.mymicroservice.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import com.mymicroservice.gateway.util.MdcUtil;
import reactor.util.context.Context;

import java.util.UUID;

//  WebFilter - для всех requests к контроллерам
@Component
public class InternalRequestIdWebFilter implements WebFilter {

    private static final String SERVICE_NAME = "GATEWAY";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Получаем или генерируем requestId
        String requestId = request.getHeaders().getFirst(MdcUtil.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Добавляем в заголовки ответа
        exchange.getResponse().getHeaders().set(MdcUtil.REQUEST_ID_HEADER, requestId);

        // Мутируем запрос с requestId
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(MdcUtil.REQUEST_ID_HEADER, requestId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // Используем contextWrite для передачи значений
        String finalRequestId = requestId;
        return chain.filter(mutatedExchange)
                .contextWrite(context -> {
                    // Добавляем значения в контекст Reactor
                    Context updatedContext = context.put(MdcUtil.REQUEST_ID_KEY, finalRequestId)
                            .put(MdcUtil.SERVICE_NAME_KEY, SERVICE_NAME);

                    // Также устанавливаем MDC для текущего потока
                    MdcUtil.setMdc(finalRequestId, SERVICE_NAME);

                    return updatedContext;
                })
                .doFinally(signalType -> MdcUtil.clearMdc());
    }
}


/*    private static final String SERVICE_NAME = "GATEWAY";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Получаем или генерируем requestId
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Добавляем в заголовки ответа
        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        // Мутируем запрос с requestId
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // Заполняем MDC ПЕРЕД вызовом chain.filter()
        MDC.put("requestId", requestId);
        MDC.put("serviceName", SERVICE_NAME);

        // Используем doOnEach для поддержания MDC в реактивной цепочке
        String finalRequestId = requestId;
        return chain.filter(mutatedExchange)
                .doOnEach(signal -> {
                    // Восстанавливаем MDC для каждого сигнала
                    if (signal.isOnNext() || signal.isOnComplete() || signal.isOnError()) {
                        MDC.put("requestId", finalRequestId);
                        MDC.put("serviceName", SERVICE_NAME);
                    }
                })
                .doFinally(signalType -> MDC.clear()); // Очищаем после завершения
    }*/