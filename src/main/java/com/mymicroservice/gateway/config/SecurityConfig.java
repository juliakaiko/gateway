package com.mymicroservice.gateway.config;

import com.mymicroservice.gateway.security.CustomAccessDeniedHandler;
import com.mymicroservice.gateway.security.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/api/**").permitAll() //for frontend
                        .pathMatchers(
                                // Actuator
                                "/actuator/**",
                                "/actuator",
                                "/actuators/health",

                                // Auth
                                "/register",
                                "/auth/**",

                                // Swagger UI Gateway
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",

                                // API Docs Gateway
                                "/api-docs",
                                "/api-docs/**",
                                "/v3/api-docs/**",

                                //API's of downstream services
                                "/auth/v3/api-docs",
                                "/api/users/v3/api-docs",
                                "/api/items/v3/api-docs",
                                "/api/payments/v3/api-docs"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder()))
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .build();
    }

    /**
     * Creates and configures a ReactiveJwtDecoder for validating JWT tokens in Spring Security WebFlux.
     *
     * <p>This bean is the core component responsible for JWT authentication in the reactive security chain.
     * It performs the following critical security validations automatically:
     *
     * <ol>
     *   <li><b>Digital Signature Verification</b> - Validates the JWT signature using RSA public key cryptography:
     *     <ul>
     *       <li>Extracts the signature from the JWT token</li>
     *       <li>Computes hash of the header + payload sections</li>
     *       <li>Decrypts the signature using the provided public key</li>
     *       <li>Compares the decrypted hash with the computed hash</li>
     *       <li>Rejects the token if signatures don't match (prevents token tampering)</li>
     *     </ul>
     *   </li>
     *
     *   <li><b>Standard JWT Claims Validation</b> - Validates standard JWT claims including:
     *     <ul>
     *       <li>Expiration time (exp) - ensures token hasn't expired</li>
     *       <li>Not Before time (nbf) - ensures token is already valid</li>
     *       <li>Issued At time (iat) - validates issuance timestamp</li>
     *     </ul>
     *   </li>
     *
     *   <li><b>Cryptographic Security</b> - Uses RSA public key from {@code keys/public.pem} classpath resource:
     *     <ul>
     *       <li>Loads PEM-formatted public key from classpath</li>
     *       <li>Parses and converts to RSAPublicKey object</li>
     *       <li>Supports RS256, RS384, RS512 signature algorithms</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><b>Error Handling:</b> If signature validation fails or tokens are invalid, the decoder
     * throws appropriate exceptions that are handled by configured {@link CustomAuthenticationEntryPoint}
     * and {@link CustomAccessDeniedHandler}.
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        try {
            RSAPublicKey publicKey = getPublicKey();
            return NimbusReactiveJwtDecoder.withPublicKey(publicKey).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ReactiveJwtDecoder", e);
        }
    }

    protected RSAPublicKey getPublicKey() throws Exception {
        ClassPathResource resource = new ClassPathResource("keys/public.pem");
        String key = new String(resource.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(spec);
    }
}
