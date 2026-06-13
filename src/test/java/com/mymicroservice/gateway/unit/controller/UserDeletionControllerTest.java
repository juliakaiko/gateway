package com.mymicroservice.gateway.unit.controller;

import com.mymicroservice.gateway.controller.UserDeletionController;
import com.mymicroservice.gateway.dto.response.UserFromUserServiceResponse;
import com.mymicroservice.gateway.util.UserFromUserServiceResponseGenerator;
import com.mymicroservice.gateway.util.data.TestConstants;
import com.mymicroservice.gateway.webclient.AuthServiceWebClient;
import com.mymicroservice.gateway.webclient.UserServiceWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class UserDeletionControllerTest {

    @Mock
    private UserServiceWebClient userServiceWebClient;

    @Mock
    private AuthServiceWebClient authServiceWebClient;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        UserDeletionController controller = new UserDeletionController(userServiceWebClient, authServiceWebClient);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void deleteUser_ShouldReturnOk_WhenDeletionSucceeds() {
        UserFromUserServiceResponse userResponse = UserFromUserServiceResponseGenerator.generateUser();

        Mockito.when(userServiceWebClient.deleteUser(anyLong())).thenReturn(Mono.just(userResponse));
        Mockito.when(authServiceWebClient.deleteUser(anyLong())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(TestConstants.DELETE_USER_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserFromUserServiceResponse.class)
                .value(resp -> {
                    assert resp.getUserId().equals(TestConstants.USER_ID);
                    assert resp.getEmail().equals(TestConstants.USER_EMAIL);
                });
    }

    @Test
    void deleteUser_ShouldReturnInternalServerError_WhenAuthServiceFails() {
        UserFromUserServiceResponse userResponse = UserFromUserServiceResponseGenerator.generateUser();

        Mockito.when(userServiceWebClient.deleteUser(anyLong())).thenReturn(Mono.just(userResponse));
        Mockito.when(authServiceWebClient.deleteUser(anyLong()))
                .thenReturn(Mono.error(new RuntimeException(TestConstants.AUTH_FAILURE_MESSAGE)));

        webTestClient.delete()
                .uri(TestConstants.DELETE_USER_URI)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserNotFound() {
        WebClientResponseException notFound = WebClientResponseException.create(
                404,
                "Not Found",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8
        );

        Mockito.when(userServiceWebClient.deleteUser(anyLong())).thenReturn(Mono.error(notFound));

        webTestClient.delete()
                .uri(TestConstants.DELETE_USER_URI)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteUser_ShouldReturnServiceUnavailable_WhenConnectionFails() {
        WebClientRequestException connectionError = new WebClientRequestException(
                new ConnectException("Connection refused"),
                HttpMethod.DELETE,
                URI.create("http://dummy"),
                HttpHeaders.EMPTY
        );

        Mockito.when(userServiceWebClient.deleteUser(anyLong())).thenReturn(Mono.error(connectionError));

        webTestClient.delete()
                .uri(TestConstants.DELETE_USER_URI)
                .exchange()
                .expectStatus().isEqualTo(503);
    }
}
