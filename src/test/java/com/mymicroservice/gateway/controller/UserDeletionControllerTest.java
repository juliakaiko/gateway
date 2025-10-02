package com.mymicroservice.gateway.controller;

import com.mymicroservice.gateway.dto.response.UserFromUserServiceResponse;
import com.mymicroservice.gateway.webclient.AuthServiceWebClient;
import com.mymicroservice.gateway.webclient.UserServiceWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
public class UserDeletionControllerTest {

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
    void deleteUser_success() {
        UserFromUserServiceResponse userResponse = new UserFromUserServiceResponse();
        userResponse.setUserId(1L);
        userResponse.setEmail("test@test.com");

        Mockito.when(userServiceWebClient.deleteUser(anyLong())).thenReturn(Mono.just(userResponse));
        Mockito.when(authServiceWebClient.deleteUser(anyLong())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/users/internal-delete/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserFromUserServiceResponse.class)
                .value(resp -> {
                    assert resp.getUserId().equals(1L);
                    assert resp.getEmail().equals("test@test.com");
                });
    }

    @Test
    void deleteUser_authServiceFails_returnsInternalServerError() {
        UserFromUserServiceResponse userResponse = new UserFromUserServiceResponse();
        userResponse.setUserId(1L);
        userResponse.setEmail("test@test.com");

        Mockito.when(userServiceWebClient.deleteUser(anyLong())).thenReturn(Mono.just(userResponse));
        Mockito.when(authServiceWebClient.deleteUser(anyLong()))
                .thenReturn(Mono.error(new RuntimeException("AuthService failure")));

        webTestClient.delete()
                .uri("/users/internal-delete/1")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
