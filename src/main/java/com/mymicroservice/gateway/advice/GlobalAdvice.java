package com.mymicroservice.gateway.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.gateway.exception.AuthServiceException;
import com.mymicroservice.gateway.util.ErrorItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalAdvice {

    private final ObjectMapper objectMapper;

    /**
     * Handles validation exceptions thrown when a DTO fails validation annotations
     * such as @Valid, @NotNull, @NotBlank, @Size, @Past, @Email, and others.
     *
     * <p>This method captures field-specific validation errors and returns them
     * in a structured format. Each field that failed validation is included
     * in the `fieldErrors` map, where the key is the field name and the value
     * is the corresponding validation message. Additionally, a general message,
     * timestamp, request URL, and HTTP status code are included for context.
     *
     * <p>Example of the JSON response:
     * <pre>
     * {
     *   "message": "Validation failed",
     *   "fieldErrors": {
     *       "name": "Name cannot be blank",
     *       "birthDate": "Birth date must be in the past"
     *   },
     *   "timestamp": "2025-10-04 12:34",
     *   "url": "/register",
     *   "statusCode": 400
     * }
     * </pre>
     *
     * @param e the WebExchangeBindException containing validation errors for the request body
     * @param exchange the ServerWebExchange representing the current HTTP request
     * @return a ResponseEntity containing an ErrorItem with detailed field-level validation errors
     *         and a HTTP 400 (BAD_REQUEST) status
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorItem> handleWebExchangeBindException(
            WebExchangeBindException e,
            ServerWebExchange exchange) {

        ErrorItem error = new ErrorItem();

        Map<String, String> fieldErrors = e.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        x -> x.getField(),
                        x -> x.getDefaultMessage(),
                        (msg1, msg2) -> msg1 + "; " + msg2
                ));

        error.setFieldErrors(fieldErrors);
        error.setMessage("Validation failed");
        error.setTimestamp(formatDate());
        error.setUrl(exchange.getRequest().getURI().toString());
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles WebClientResponseException from downstream services.
     * Tries to parse the response body as {@link ErrorItem} (including field-specific errors).
     * If parsing fails, returns a generic "Server error".
     *
     * @param e the exception from WebClient call
     * @param exchange request context
     * @return ResponseEntity with {@link ErrorItem} containing error details and HTTP status
     */

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorItem> handleWebClientResponseException(
            WebClientResponseException e, ServerWebExchange exchange) {

        try {
            ErrorItem error = objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    ErrorItem.class
            );
            return new ResponseEntity<>(error, HttpStatus.valueOf(e.getRawStatusCode()));
        } catch (Exception ex) {
            ErrorItem error = new ErrorItem();
            error.setMessage("Server error");
            error.setTimestamp(formatDate());
            error.setUrl(exchange.getRequest().getURI().toString());
            error.setStatusCode(HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Handles authorization denied exceptions when a user lacks required permissions.
     *
     * @param e AuthorizationDeniedException containing authorization failure details
     * @return ResponseEntity with ErrorItem containing error details and HTTP 403 status (FORBIDDEN)
     */
    @ExceptionHandler({AuthorizationDeniedException.class})
    public ResponseEntity<ErrorItem> handleAuthorizationDeniedException(
            AuthorizationDeniedException e,
            ServerWebExchange exchange) {
        ErrorItem error = generateMessage(e, HttpStatus.FORBIDDEN, exchange);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({AuthServiceException.class})
    public ResponseEntity<ErrorItem> handleAuthServiceException(
            AuthServiceException e,
            ServerWebExchange exchange) {
        ErrorItem error = generateMessage(e, HttpStatus.BAD_REQUEST, exchange);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorItem> handleIllegalArgumentException(
            IllegalArgumentException e,
            ServerWebExchange exchange) {
        ErrorItem error = generateMessage(e, HttpStatus.BAD_REQUEST, exchange);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Generates an ErrorItem object with error message, URL, status code and timestamp.
     *
     * @param e Exception
     * @param status HTTP status
     * @return ErrorItem with populated fields
     */
    public ErrorItem generateMessage(Exception e, HttpStatus status, ServerWebExchange exchange) {
        ErrorItem error = new ErrorItem();
        error.setTimestamp(formatDate());
        error.setMessage(e.getMessage());
        error.setUrl(exchange.getRequest().getURI().toString());
        error.setStatusCode(status.value());
        return error;
    }

    /**
     * Formats the current date and time into a string with pattern "yyyy-MM-dd HH:mm".
     *
     * @return formatted date-time string
     */
    public String formatDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTimeFormatter.format(LocalDateTime.now());
    }

}