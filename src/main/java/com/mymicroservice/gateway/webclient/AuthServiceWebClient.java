package com.mymicroservice.gateway.webclient;

import com.mymicroservice.gateway.dto.response.AccessAndRefreshTokenResponse;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthServiceWebClient {

    private final WebClient webClient;

    public AuthServiceWebClient(@Qualifier("authServiceClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<AccessAndRefreshTokenResponse> register(UserRegistrationResponse userResponse) {
        return webClient.post()
                .uri("/auth/register")
                .headers(headers -> {
                    headers.set("X-Internal-Call", "true");
                    headers.set("X-Source-Service", "GATEWAY");
                })
                .bodyValue(userResponse)
                .retrieve()
                .bodyToMono(AccessAndRefreshTokenResponse.class);
    }
}
