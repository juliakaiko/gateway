package com.mymicroservice.gateway.unit.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.gateway.advice.GlobalAdvice;
import com.mymicroservice.gateway.controller.RegistrationController;
import com.mymicroservice.gateway.dto.request.UserRegistrationRequest;
import com.mymicroservice.gateway.exception.AuthServiceException;
import com.mymicroservice.gateway.util.ErrorItem;
import com.mymicroservice.gateway.util.data.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class GlobalAdviceTest {

    private static final String TEST_URI = TestConstants.REGISTER_URI;

    private GlobalAdvice globalAdvice;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        globalAdvice = new GlobalAdvice(new ObjectMapper());
        exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post(TEST_URI).build()
        );
    }

    @Test
    void handleWebExchangeBindException_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
        bindingResult.addError(new FieldError("request", "email", "Email address may not be blank"));
        MethodParameter parameter = new MethodParameter(
                RegistrationController.class.getMethod("register", UserRegistrationRequest.class),
                0
        );
        WebExchangeBindException exception = new WebExchangeBindException(parameter, bindingResult);

        ResponseEntity<ErrorItem> response = globalAdvice.handleWebExchangeBindException(exception, exchange);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleWebClientResponseException_ShouldReturnParsedError_WhenBodyIsValid() {
        String body = """
                {
                  "message": "Downstream error",
                  "timestamp": "2025-01-01 12:00",
                  "url": "/auth/register",
                  "statusCode": 400
                }
                """;
        WebClientResponseException exception = WebClientResponseException.create(
                400,
                "Bad Request",
                HttpHeaders.EMPTY,
                body.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        ResponseEntity<ErrorItem> response = globalAdvice.handleWebClientResponseException(exception, exchange);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Downstream error", response.getBody().getMessage());
    }

    @Test
    void handleAuthorizationDeniedException_ShouldReturnForbidden_WhenAccessDenied() {
        AuthorizationDeniedException exception = new AuthorizationDeniedException(
                "Access Denied", mock(AuthorizationResult.class)
        );

        ResponseEntity<ErrorItem> response = globalAdvice.handleAuthorizationDeniedException(exception, exchange);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatusCode());
        assertEquals("Access Denied", response.getBody().getMessage());
    }

    @Test
    void handleAuthServiceException_ShouldReturnBadRequest_WhenAuthServiceFails() {
        AuthServiceException exception = new AuthServiceException(TestConstants.ROLLBACK_MESSAGE);

        ResponseEntity<ErrorItem> response = globalAdvice.handleAuthServiceException(exception, exchange);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TestConstants.ROLLBACK_MESSAGE, response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleWebClientResponseException_ShouldReturnGenericError_WhenBodyIsUnparseable() {
        WebClientResponseException exception = WebClientResponseException.create(
                500,
                "Internal Server Error",
                HttpHeaders.EMPTY,
                "not-json".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        ResponseEntity<ErrorItem> response = globalAdvice.handleWebClientResponseException(exception, exchange);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Server error", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest_WhenArgumentIsInvalid() {
        IllegalArgumentException exception = new IllegalArgumentException("Unknown or unsupported role: SUPERADMIN");

        ResponseEntity<ErrorItem> response = globalAdvice.handleIllegalArgumentException(exception, exchange);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unknown or unsupported role: SUPERADMIN", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatusCode());
    }
}
