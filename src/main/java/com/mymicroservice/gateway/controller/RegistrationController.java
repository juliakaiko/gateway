package com.mymicroservice.gateway.controller;

import com.mymicroservice.gateway.dto.response.RegistrationResponse;
import com.mymicroservice.gateway.dto.request.UserRegistrationRequest;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;
import com.mymicroservice.gateway.exception.AuthServiceException;
import com.mymicroservice.gateway.util.ResponseUtil;
import com.mymicroservice.gateway.webclient.AuthServiceWebClient;
import com.mymicroservice.gateway.webclient.UserServiceWebClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RegistrationController {

    private final UserServiceWebClient userServiceWebClient;
    private final AuthServiceWebClient authServiceWebClient;
    private final ResponseUtil responseUtil;

    @PostMapping
    public Mono<ResponseEntity<RegistrationResponse>> register(@RequestBody @Valid UserRegistrationRequest request) {
        UserRegistrationResponse userResponse = responseUtil.generateUserResponse(request);

        return userServiceWebClient.createUser(userResponse) //Mono<UserDto>
                .flatMap(userDto -> //flatMap for Mono<Tokens>, not Mono<Mono<Tokens>> (in "map" case)
                        authServiceWebClient.register(userResponse)
                                .map(tokens -> ResponseEntity.ok(new RegistrationResponse(userDto, tokens)))
                                .onErrorResume(e ->
                                        userServiceWebClient.deleteUser(userDto.getUserId())
                                                .onErrorResume(deleteError -> {
                                                    log.error("Failed to rollback user creation: {}", deleteError.getMessage());
                                                    return Mono.empty();
                                                })
                                                .then(Mono.error(new AuthServiceException("AuthService failed. User rolled back.")))
                                )
                );
    }

}
