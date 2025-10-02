package com.mymicroservice.gateway.util;

import com.mymicroservice.gateway.dto.response.AccessAndRefreshTokenResponse;

public class AccessAndRefreshTokenResponseGenerator {

    public static AccessAndRefreshTokenResponse generateTokens() {

        return  AccessAndRefreshTokenResponse.builder()
                .accessToken("test_access_token")
                .refreshToken("test_refresh_token")
                .build();
    }
}
