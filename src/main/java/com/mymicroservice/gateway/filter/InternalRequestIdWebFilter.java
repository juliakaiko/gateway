package com.mymicroservice.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Getting or generating RequestId
        String requestId = request.getHeaders().getFirst(MdcUtil.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Adding it to the response headers
        exchange.getResponse().getHeaders().set(MdcUtil.REQUEST_ID_HEADER, requestId);

        // Мутируем запрос с requestId
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(MdcUtil.REQUEST_ID_HEADER, requestId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // Using contextWrite to pass values.
        String finalRequestId = requestId;
        return chain.filter(mutatedExchange)
                .contextWrite(context -> {
                    // Добавляем значения в контекст Reactor
                    Context updatedContext = context.put(MdcUtil.REQUEST_ID_KEY, finalRequestId)
                            .put(MdcUtil.SERVICE_NAME_KEY, serviceName);

                    // Также устанавливаем MDC для текущего потока
                    MdcUtil.setMdc(finalRequestId, serviceName);

                    return updatedContext;
                })
                .doFinally(signalType -> MdcUtil.clearMdc());
    }
}

