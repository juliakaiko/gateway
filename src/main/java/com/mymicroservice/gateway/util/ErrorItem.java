package com.mymicroservice.gateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorItem {

    private String message;
    private String timestamp;
    private String url;
    private int statusCode;

    private Map<String, String> fieldErrors;

    private static ObjectMapper objectMapper;

    public static ErrorItem hanleValidationException(WebExchangeBindException e,
                                                       ServerWebExchange exchange,
                                                       HttpStatus status) {
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
        error.setStatusCode(status.value());

        return error;
    }

    public static ErrorItem handleDownstreamResponseException(WebClientResponseException e,
                                                       ServerWebExchange exchange) {
        ErrorItem error = new ErrorItem();

        try {
            error = objectMapper.readValue(
                    e.getResponseBodyAsString(),
                    ErrorItem.class
            );
            error.setStatusCode(e.getRawStatusCode());
            return error;
        } catch (Exception ex) {
            error.setMessage("Server error");
            error.setTimestamp(formatDate());
            error.setUrl(exchange.getRequest().getURI().toString());
            error.setStatusCode(HttpStatus.BAD_REQUEST.value());

            return error;
        }
    }

    /**
     * Generates an ErrorItem object with error message, URL, status code and timestamp.
     *
     * @param e Exception
     * @param status HTTP status
     * @return ErrorItem with populated fields
     */
    public static ErrorItem generateMessage(Exception e, HttpStatus status, ServerWebExchange exchange) {
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
    public static String formatDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTimeFormatter.format(LocalDateTime.now());
    }
}
