package com.mymicroservice.gateway.webclient;

import com.mymicroservice.gateway.dto.response.AccessAndRefreshTokenResponse;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;
import com.mymicroservice.gateway.util.AccessAndRefreshTokenResponseGenerator;
import com.mymicroservice.gateway.util.UserRegistrationResponseGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceWebClientTest {

    @Mock
    WebClient webClient;

    @Mock
    WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    WebClient.RequestBodySpec requestBodySpec;

    @Mock
    WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    WebClient.RequestHeadersUriSpec deleteRequestHeadersUriSpec;

    @Mock
    WebClient.RequestHeadersSpec deleteRequestHeadersSpec;

    @Mock
    WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AuthServiceWebClient authServiceWebClient;

    @Test
    void register_shouldReturnMono() {
        UserRegistrationResponse user = UserRegistrationResponseGenerator.generateUser();
        AccessAndRefreshTokenResponse tokenResponse = AccessAndRefreshTokenResponseGenerator.generateTokens();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/auth/register")).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(user)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AccessAndRefreshTokenResponse.class))
                .thenReturn(Mono.just(tokenResponse));

        StepVerifier.create(authServiceWebClient.register(user))
                .expectNext(tokenResponse)
                .verifyComplete();
    }

    @Test
    void deleteUser_shouldReturnMonoVoid() {
        Long userId = 123L;

        // DELETE-mock
        when(webClient.delete()).thenReturn(deleteRequestHeadersUriSpec);
        when(deleteRequestHeadersUriSpec.uri("/api/internal/auth/user/{id}", userId))
                .thenReturn(deleteRequestHeadersSpec);
        when(deleteRequestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.noContent().build()));

        StepVerifier.create(authServiceWebClient.deleteUser(userId))
                .verifyComplete(); // checking that Mono<Void> has completed successfully
    }

}
