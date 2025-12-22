package com.mymicroservice.gateway.controller;

import com.mymicroservice.gateway.dto.response.UserFromUserServiceResponse;
import com.mymicroservice.gateway.webclient.AuthServiceWebClient;
import com.mymicroservice.gateway.webclient.UserServiceWebClient;
import com.mymicroservice.gateway.util.MdcUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users/internal-delete")
@RequiredArgsConstructor
@Slf4j
public class UserDeletionController {

    private final UserServiceWebClient userServiceWebClient;
    private final AuthServiceWebClient authServiceWebClient;
    private static final Logger TRACE_MDC_LOGGER = LoggerFactory.getLogger("TRACE_MDC_LOGGER");

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<UserFromUserServiceResponse>> deleteUser(@PathVariable Long id) {
        // Используем MdcUtil.withMdc для автоматического поддержания контекста
        return MdcUtil.withMdc(
                Mono.defer(() -> {
                    TRACE_MDC_LOGGER.info("Starting deletion of user with id: {}", id);

                    return userServiceWebClient.deleteUser(id)
                            .flatMap(user ->
                                    authServiceWebClient.deleteUser(id)
                                            .thenReturn(user)
                            )
                            // specific: user-service returned 404
                            .map(ResponseEntity::ok)
                            .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                                TRACE_MDC_LOGGER.warn("User {} not found: {}", id, ex.getMessage());
                                return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
                            })
                            // specific: connection refused / service unavailable
                            .onErrorResume(WebClientRequestException.class, ex -> {
                                TRACE_MDC_LOGGER.error("Service unavailable when deleting user {}: {}", id, ex.getMessage());
                                return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
                            })
                            // fallback: any other error -> 500
                            .onErrorResume(ex -> {
                                TRACE_MDC_LOGGER.error("Failed to delete user {}: {}", id, ex.getMessage());
                                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                            });
                })
        );
    }

}
