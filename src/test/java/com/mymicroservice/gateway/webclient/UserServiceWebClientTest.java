package com.mymicroservice.gateway.webclient;

import com.mymicroservice.gateway.dto.response.UserFromUserServiceResponse;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;
import com.mymicroservice.gateway.util.UserFromUserServiceResponseGenerator;
import com.mymicroservice.gateway.util.UserRegistrationResponseGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceWebClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec deleteRequestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec deleteRequestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;
    @InjectMocks
    private UserServiceWebClient userServiceWebClient;

    @Test
    void createUser_shouldReturnMonoUserFromUserServiceResponse() {
        UserRegistrationResponse userRequest = UserRegistrationResponseGenerator.generateUser();
        UserFromUserServiceResponse userResponse = UserFromUserServiceResponseGenerator.generateUser();

        // POST-mock
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/api/internal/users/")).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(userRequest)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserFromUserServiceResponse.class)).thenReturn(Mono.just(userResponse));

        StepVerifier.create(userServiceWebClient.createUser(userRequest))
                .expectNext(userResponse)
                .verifyComplete();
    }

    @Test
    void deleteUser_shouldReturnMonoUserFromUserServiceResponse() {
        Long userId = 123L;
        UserFromUserServiceResponse userResponse = new UserFromUserServiceResponse();

        // DELETE-mock
        when(webClient.delete()).thenReturn(deleteRequestHeadersUriSpec);
        when(deleteRequestHeadersUriSpec.uri("/api/internal/users/{id}", userId)).thenReturn(deleteRequestHeadersSpec);
        when(deleteRequestHeadersSpec.headers(any())).thenReturn(deleteRequestHeadersSpec);
        when(deleteRequestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserFromUserServiceResponse.class)).thenReturn(Mono.just(userResponse));

        StepVerifier.create(userServiceWebClient.deleteUser(userId))
                .expectNext(userResponse)
                .verifyComplete();
    }
}
