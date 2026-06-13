package com.mymicroservice.gateway.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.gateway.advice.GlobalAdvice;
import com.mymicroservice.gateway.controller.RegistrationController;
import com.mymicroservice.gateway.dto.request.UserRegistrationRequest;
import com.mymicroservice.gateway.dto.response.AccessAndRefreshTokenResponse;
import com.mymicroservice.gateway.dto.response.RegistrationResponse;
import com.mymicroservice.gateway.dto.response.UserFromUserServiceResponse;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;
import com.mymicroservice.gateway.util.AccessAndRefreshTokenResponseGenerator;
import com.mymicroservice.gateway.util.ResponseUtil;
import com.mymicroservice.gateway.util.UserFromUserServiceResponseGenerator;
import com.mymicroservice.gateway.util.UserRegistrationRequestGenerator;
import com.mymicroservice.gateway.util.UserRegistrationResponseGenerator;
import com.mymicroservice.gateway.util.data.TestConstants;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
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

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalAdvice(new ObjectMapper()))
                .validator(validator)
                .build();
    }

    @Test
    void register_ShouldReturnOk_WhenRegistrationSucceeds() {
        Mockito.when(responseUtil.generateUserResponse(any())).thenReturn(userResponse);
        Mockito.when(userServiceWebClient.createUser(userResponse)).thenReturn(Mono.just(userDtoFromUserService));
        Mockito.when(authServiceWebClient.register(userResponse)).thenReturn(Mono.just(tokens));

        webTestClient.post()
                .uri(TestConstants.REGISTER_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RegistrationResponse.class)
                .value(resp -> {
                    assert resp.getUserDto().getUserId().equals(TestConstants.USER_ID);
                    assert resp.getTokens().equals(tokens);
                });
    }

    @Test
    void register_ShouldReturnBadRequest_WhenAuthServiceFails() {
        Mockito.when(responseUtil.generateUserResponse(any())).thenReturn(userResponse);
        Mockito.when(userServiceWebClient.createUser(userResponse)).thenReturn(Mono.just(userDtoFromUserService));
        Mockito.when(authServiceWebClient.register(userResponse))
                .thenReturn(Mono.error(new RuntimeException(TestConstants.AUTH_ERROR_MESSAGE)));
        Mockito.when(userServiceWebClient.deleteUser(userDtoFromUserService.getUserId())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(TestConstants.REGISTER_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo(TestConstants.ROLLBACK_MESSAGE);
    }

    @Test
    void register_ShouldReturnBadRequest_WhenValidationFails() {
        UserRegistrationRequest invalidRequest = UserRegistrationRequestGenerator.generateUser();
        invalidRequest.setEmail("");

        webTestClient.post()
                .uri(TestConstants.REGISTER_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Validation failed")
                .jsonPath("$.fieldErrors.email").exists();
    }
}
