package com.mymicroservice.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.userservice.url}")
    private String userServiceUrl;

    @Value("${services.authservice.url}")
    private String authServiceUrl;

    @Bean("userServiceClient")
    public WebClient userServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean("authServiceClient")
    public WebClient authServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(authServiceUrl)
                .build();
    }
}
