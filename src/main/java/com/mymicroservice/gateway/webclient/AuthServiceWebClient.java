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
                .bodyValue(userResponse)
                .retrieve()
                .bodyToMono(AccessAndRefreshTokenResponse.class);
    }

    public Mono<Void> deleteUser(Long userId) {
        return webClient.delete()
                .uri("/api/internal/auth/user/{id}", userId)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

}
