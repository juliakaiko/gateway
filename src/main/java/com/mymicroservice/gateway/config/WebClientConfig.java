package com.mymicroservice.gateway.config;

import lombok.extern.slf4j.Slf4j;
import com.mymicroservice.gateway.util.MdcUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${services.userservice.url}")
    private String userServiceUrl;

    @Value("${services.authservice.url}")
    private String authServiceUrl;

    @Value("${services.orderservice.url}")
    private String orderServiceUrl;

    @Value("${services.paymentservice.url}")
    private String paymentServiceUrl;

    @Bean("userServiceClient")
    public WebClient userServiceWebClient() {
        return WebClient.builder()
                .baseUrl(userServiceUrl)
                .filter(mdcContextFilter())
                .build();
    }

    @Bean("authServiceClient")
    public WebClient authServiceWebClient() {
        return WebClient.builder()
                .baseUrl(authServiceUrl)
                .filter(mdcContextFilter())
                .build();
    }

    @Bean("orderServiceClient")
    public WebClient orderServiceWebClient() {
        return WebClient.builder()
                .baseUrl(orderServiceUrl)
                .filter(mdcContextFilter())
                .build();
    }

    @Bean("paymentServiceClient")
    public WebClient paymentServiceWebClient() {
        return WebClient.builder()
                .baseUrl(paymentServiceUrl)
                .filter(mdcContextFilter())
                .build();
    }

    private ExchangeFilterFunction mdcContextFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest ->
                Mono.deferContextual(contextView -> {
                    // take values from the Reactor context
                    String requestId = contextView.getOrDefault(MdcUtil.REQUEST_ID_KEY, "");
                    String serviceName = contextView.getOrDefault(MdcUtil.SERVICE_NAME_KEY, "GATEWAY");

                    ClientRequest.Builder requestBuilder = ClientRequest.from(clientRequest);

                    if (!requestId.isEmpty()) {
                        requestBuilder.header(MdcUtil.REQUEST_ID_HEADER, requestId);
                    }

                    requestBuilder.header("X-Internal-Call", "true");
                    requestBuilder.header("X-Source-Service", serviceName);

                    return Mono.just(requestBuilder.build());
                })
        );
    }
}
