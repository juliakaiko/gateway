package com.mymicroservice.gateway.util.data;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class TestConstants {

    public static final String USER_NAME = "TestName";
    public static final String USER_SURNAME = "TestSurName";
    public static final LocalDate USER_BIRTH_DATE = LocalDate.of(2000, 2, 2);
    public static final String USER_EMAIL = "test@test.by";
    public static final String USER_PASSWORD = "pass_test";

    public static final Long USER_ID = 1L;
    public static final Long WEBCLIENT_USER_ID = 123L;

    public static final String ACCESS_TOKEN = "test_access_token";
    public static final String REFRESH_TOKEN = "test_refresh_token";

    public static final String REGISTER_URI = "/register";
    public static final String DELETE_USER_URI = "/users/internal-delete/1";

    public static final String AUTH_REGISTER_URI = "/auth/register";
    public static final String AUTH_DELETE_URI = "/api/internal/auth/user/{id}";
    public static final String USER_CREATE_URI = "/api/internal/users/";
    public static final String USER_DELETE_URI = "/api/internal/users/{id}";

    public static final String FALLBACK_USER_URI = "/fallback/user";
    public static final String FALLBACK_ORDER_URI = "/fallback/order";
    public static final String FALLBACK_AUTH_URI = "/fallback/auth";
    public static final String FALLBACK_PAYMENT_URI = "/fallback/payment";

    public static final String FALLBACK_USER_MESSAGE = "User Service is unavailable. Please try again later.";
    public static final String FALLBACK_ORDER_MESSAGE = "Order Service is unavailable. Please try again later.";
    public static final String FALLBACK_AUTH_MESSAGE = "Authentication Service is unavailable. Please try again later.";
    public static final String FALLBACK_PAYMENT_MESSAGE = "Payment Service is unavailable. Please try again later.";

    public static final String AUTH_ERROR_MESSAGE = "Auth error";
    public static final String AUTH_FAILURE_MESSAGE = "AuthService failure";
    public static final String ROLLBACK_MESSAGE = "AuthService failed. User rolled back.";

    public static final String DELETE_USER_EMAIL = "test@test.com";
    public static final String TRACE_ID = "test-trace-id";
    public static final String SERVICE_NAME = "gateway";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TEST_JWT = "test.jwt.token";
}
