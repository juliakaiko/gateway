package com.mymicroservice.gateway.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/actuators")
public class ActuatorController {

    private final WebClient authServiceClient;
    private final WebClient userServiceClient;
    private final WebClient orderServiceClient;
    private final WebClient paymentServiceClient;

    @Value("${spring.application.name}")
    private String serviceName;
    private static final String INTERNAL_CALL_HEADER = "X-Internal-Call";
    private static final String SOURCE_SERVICE_HEADER = "X-Source-Service";

    public ActuatorController(
            @Qualifier("authServiceClient") WebClient authServiceClient,
            @Qualifier("userServiceClient") WebClient userServiceClient,
            @Qualifier("orderServiceClient") WebClient orderServiceClient,
            @Qualifier("paymentServiceClient") WebClient paymentServiceClient
    ) {
        this.authServiceClient = authServiceClient;
        this.userServiceClient = userServiceClient;
        this.orderServiceClient = orderServiceClient;
        this.paymentServiceClient = paymentServiceClient;
    }

    @GetMapping("/health")
    public Mono<Map<String, String>> getAllServiceHealth() {
        Mono<String> authHealth = getServiceHealth(authServiceClient);
        Mono<String> userHealth = getServiceHealth(userServiceClient);
        Mono<String> orderHealth = getServiceHealth(orderServiceClient);
        Mono<String> paymentHealth = getServiceHealth(paymentServiceClient);

        return Mono.zip(authHealth, userHealth, orderHealth, paymentHealth)
                .map(tuple -> {
                    Map<String, String> statusMap = new HashMap<>();
                    statusMap.put("authservice", tuple.getT1());
                    statusMap.put("userservice", tuple.getT2());
                    statusMap.put("orderservice", tuple.getT3());
                    statusMap.put("paymentservice", tuple.getT4());
                    return statusMap;
                });
    }

    private Mono<String> getServiceHealth(WebClient client) {
        return client.get()
                .uri("/actuator/health")
                .header(INTERNAL_CALL_HEADER, "true")
                .header(SOURCE_SERVICE_HEADER, serviceName)
                .retrieve()
                .bodyToMono(Map.class)
                .map(health -> health.getOrDefault("status", "UNKNOWN").toString())
                .onErrorReturn("DOWN");
    }

}
