package com.mymicroservice.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user")
    public Mono<String> userFallback() {
        return Mono.just("User Service is unavailable. Please try again later.");
    }

    @GetMapping("/order")
    public Mono<String> orderFallback() {
        return Mono.just("Order Service is unavailable. Please try again later.");
    }

    @GetMapping("/auth")
    public Mono<String> authFallback() {
        return Mono.just("Authentication Service is unavailable. Please try again later.");
    }

    @GetMapping("/payment")
    public Mono<String> paymentFallback() {
        return Mono.just("Payment Service is unavailable. Please try again later.");
    }
}
