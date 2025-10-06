package com.mymicroservice.gateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Custom authentication entry point ensures that 401 authentication errors
 * include proper CORS headers so the frontend can detect them and trigger automatic
 * token refresh instead of being blocked by browser CORS policy
 *
 */
@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    /**
     * Handles authentication failures by returning 401 Unauthorized response
     * with proper CORS headers to allow frontend processing.
     *
     * <p>This method ensures that even authentication errors include necessary
     * CORS headers, enabling the frontend's axios interceptor to:
     * <ul>
     *   <li>Detect 401 status codes</li>
     *   <li>Trigger automatic token refresh</li>
     *   <li>Retry the original request with new tokens</li>
     *   <li>Redirect to login page if refresh fails</li>
     * </ul>
     *
     * @param exchange the current server exchange
     * @param ex the authentication exception that caused the invocation
     * @return a completion mono
     */
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        // Adding CORS headers
        HttpHeaders headers = response.getHeaders();
        headers.add("Access-Control-Allow-Origin", "http://localhost:3000");
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        headers.add("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With");
        headers.add("Access-Control-Expose-Headers", "Authorization, Content-Type");

        // For preflight OPTIONS queries
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            response.setStatusCode(HttpStatus.OK);
        }

        return response.setComplete();
    }
}
