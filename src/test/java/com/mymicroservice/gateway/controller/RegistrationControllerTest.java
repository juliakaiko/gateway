package com.mymicroservice.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.gateway.advice.GlobalAdvice;
import com.mymicroservice.gateway.dto.request.UserRegistrationRequest;
import com.mymicroservice.gateway.dto.response.RegistrationResponse;
import com.mymicroservice.gateway.dto.response.UserFromUserServiceResponse;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;
import com.mymicroservice.gateway.dto.response.AccessAndRefreshTokenResponse;
import com.mymicroservice.gateway.util.AccessAndRefreshTokenResponseGenerator;
import com.mymicroservice.gateway.util.ResponseUtil;
import com.mymicroservice.gateway.util.UserFromUserServiceResponseGenerator;
import com.mymicroservice.gateway.util.UserRegistrationRequestGenerator;
import com.mymicroservice.gateway.util.UserRegistrationResponseGenerator;
import com.mymicroservice.gateway.webclient.AuthServiceWebClient;
import com.mymicroservice.gateway.webclient.UserServiceWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private UserServiceWebClient userServiceWebClient;

    @Mock
    private AuthServiceWebClient authServiceWebClient;

    @Mock
    private ResponseUtil responseUtil;

    @Mock
    private ObjectMapper objectMapper;

    private WebTestClient webTestClient;

    private UserRegistrationRequest request;
    private UserRegistrationResponse userResponse;
    private UserFromUserServiceResponse userDtoFromUserService;
    private AccessAndRefreshTokenResponse tokens;

    @BeforeEach
    void setup() {
        request = UserRegistrationRequestGenerator.generateUser();
        userResponse = UserRegistrationResponseGenerator.generateUser();
        userDtoFromUserService = UserFromUserServiceResponseGenerator.generateUser();
        tokens = AccessAndRefreshTokenResponseGenerator.generateTokens();

        RegistrationController controller = new RegistrationController(
                userServiceWebClient, authServiceWebClient, responseUtil
        );
        webTestClient = WebTestClient.bindToController(controller).build();

        GlobalAdvice globalAdvice = new GlobalAdvice(objectMapper);
        webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(globalAdvice)
                .build();
    }

    @Test
    void register_success() {

        Mockito.when(responseUtil.generateUserResponse(any())).thenReturn(userResponse);
        Mockito.when(userServiceWebClient.createUser(userResponse)).thenReturn(Mono.just(userDtoFromUserService));
        Mockito.when(authServiceWebClient.register(userResponse)).thenReturn(Mono.just(tokens));

        webTestClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RegistrationResponse.class)
                .value(resp -> {
                    assert resp.getUserDto().getUserId().equals(1L);
                    assert resp.getTokens().equals(tokens);
                });
    }

    @Test
    void register_authServiceFails_thenUserRolledBack() {

        Mockito.when(responseUtil.generateUserResponse(any())).thenReturn(userResponse);
        Mockito.when(userServiceWebClient.createUser(userResponse)).thenReturn(Mono.just(userDtoFromUserService));
        Mockito.when(authServiceWebClient.register(userResponse)).thenReturn(Mono.error(new RuntimeException("Auth error")));
        Mockito.when(userServiceWebClient.deleteUser(userDtoFromUserService.getUserId())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest() // isBadRequest() because the handler returns a BAD_REQUEST
                .expectBody()
                .jsonPath("$.message").isEqualTo("AuthService failed. User rolled back.");
    }
}

