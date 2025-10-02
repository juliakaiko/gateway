package com.mymicroservice.gateway.controller;

import com.mymicroservice.gateway.dto.response.UserFromUserServiceResponse;
import com.mymicroservice.gateway.webclient.AuthServiceWebClient;
import com.mymicroservice.gateway.webclient.UserServiceWebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users/internal-delete")
@RequiredArgsConstructor
@Slf4j
public class UserDeletionController {

    private final UserServiceWebClient userServiceWebClient;
    private final AuthServiceWebClient authServiceWebClient;

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<UserFromUserServiceResponse>> deleteUser(@PathVariable Long id) {
        return userServiceWebClient.deleteUser(id)
                .flatMap(user ->
                        authServiceWebClient.deleteUser(id)
                                .thenReturn(user)
                )
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Failed to delete user {}: {}", id, e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

}
