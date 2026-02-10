package com.mymicroservice.gateway.webclient;

import com.mymicroservice.gateway.dto.response.UserFromUserServiceResponse;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceWebClient {

    private final WebClient webClient;

    public UserServiceWebClient(@Qualifier("userServiceClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<UserFromUserServiceResponse> createUser(UserRegistrationResponse userResponse) {
        return webClient.post()
                .uri("/api/internal/users/")
                .bodyValue(userResponse)
                .retrieve()
                .bodyToMono(UserFromUserServiceResponse.class);
    }

    public Mono<UserFromUserServiceResponse> deleteUser(Long id) {
        return webClient.delete()
                .uri("/api/internal/users/{id}", id)
                .retrieve()
                .bodyToMono(UserFromUserServiceResponse.class);
    }
}