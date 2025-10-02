package com.mymicroservice.gateway.advice;

import com.mymicroservice.gateway.exception.AuthServiceException;
import com.mymicroservice.gateway.util.ErrorItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalAdvice {

    /**
     * Handles validation exceptions for DTO fields when data fails validation annotations
     * such as @Valid, @NotNull, @Size, @Pattern and others.
     *
     * @param e WebExchangeBindException containing validation error information
     * @return ResponseEntity with an ErrorItem object containing:
     *         - List of error messages
     *         - URL
     *         - Status code
     *         - Timestamp
     *         - HTTP 400 status (BAD_REQUEST)
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorItem> handleWebExchangeBindException(
            WebExchangeBindException e,
            ServerWebExchange exchange) {

        ErrorItem error = new ErrorItem();
        String errors = e.getFieldErrors()
                .stream()
                .map(x -> x.getField() + ": " + x.getDefaultMessage())
                .collect(Collectors.joining(", "));

        error.setMessage("Validation error: " + errors);
        error.setTimestamp(formatDate());
        error.setUrl(exchange.getRequest().getURI().toString());
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles validation exceptions for DTO fields when data fails validation annotations
     * such as @Valid, @NotNull, @Size, @Pattern and others.
     *
     * @param e WebClientResponseException containing validation error information
     * @return ResponseEntity with an ErrorItem object containing:
     *         - List of error messages
     *         - URL
     *         - Status code
     *         - Timestamp
     *         - HTTP 400 status (BAD_REQUEST)
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorItem> handleWebClientResponseException(
            WebClientResponseException e,
            ServerWebExchange exchange) {

        ErrorItem error = new ErrorItem();
        error.setMessage(e.getMessage());
        error.setTimestamp(Instant.now().toString());
        error.setUrl(exchange.getRequest().getURI().toString());
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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