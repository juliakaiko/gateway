package com.mymicroservice.gateway.unit.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.gateway.controller.RegistrationController;
import com.mymicroservice.gateway.dto.request.UserRegistrationRequest;
import com.mymicroservice.gateway.util.ErrorItem;
import com.mymicroservice.gateway.util.data.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorItemTest {

    private static final String TEST_URI = TestConstants.REGISTER_URI;

    private MockServerWebExchange exchange;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post(TEST_URI).build()
        );
        objectMapper = new ObjectMapper();
    }

    @Test
    void hanleValidationException_ShouldReturnErrorItemWithFieldErrors_WhenValidationFails() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
        bindingResult.addError(new FieldError("request", "email", "Email address may not be blank"));

        MethodParameter parameter = new MethodParameter(
                RegistrationController.class.getMethod("register", UserRegistrationRequest.class),
                0
        );
        WebExchangeBindException exception = new WebExchangeBindException(parameter, bindingResult);

        ErrorItem error = ErrorItem.hanleValidationException(exception, exchange, HttpStatus.BAD_REQUEST);

        assertEquals("Validation failed", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatusCode());
        assertTrue(error.getUrl().endsWith(TEST_URI));
        assertNotNull(error.getTimestamp());
        assertEquals("Email address may not be blank", error.getFieldErrors().get("email"));
    }

    @Test
    void generateMessage_ShouldReturnErrorItemWithMessage_WhenExceptionProvided() {
        Exception exception = new RuntimeException(TestConstants.AUTH_ERROR_MESSAGE);

        ErrorItem error = ErrorItem.generateMessage(exception, HttpStatus.BAD_REQUEST, exchange);

        assertEquals(TestConstants.AUTH_ERROR_MESSAGE, error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatusCode());
        assertTrue(error.getUrl().endsWith(TEST_URI));
        assertNotNull(error.getTimestamp());
    }

    @Test
    void formatDate_ShouldReturnFormattedDateTime_WhenCalled() {
        String formattedDate = ErrorItem.formatDate();

        assertNotNull(formattedDate);
        assertTrue(formattedDate.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
    }

    @Test
    void handleDownstreamResponseException_ShouldParseBody_WhenBodyIsValidJson() {
        String body = """
                {
                  "message": "Downstream validation failed",
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

        ErrorItem error = ErrorItem.handleDownstreamResponseException(exception, exchange, objectMapper);

        assertEquals("Downstream validation failed", error.getMessage());
        assertEquals(400, error.getStatusCode());
    }

    @Test
    void handleDownstreamResponseException_ShouldReturnGenericError_WhenBodyIsInvalidJson() {
        WebClientResponseException exception = WebClientResponseException.create(
                500,
                "Internal Server Error",
                HttpHeaders.EMPTY,
                "not-json".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        ErrorItem error = ErrorItem.handleDownstreamResponseException(exception, exchange, objectMapper);

        assertEquals("Server error", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatusCode());
        assertTrue(error.getUrl().endsWith(TEST_URI));
        assertNotNull(error.getTimestamp());
    }
}
