package com.mymicroservice.gateway.util;

import com.mymicroservice.gateway.dto.response.AccessAndRefreshTokenResponse;

import static com.mymicroservice.gateway.util.data.TestConstants.ACCESS_TOKEN;
import static com.mymicroservice.gateway.util.data.TestConstants.REFRESH_TOKEN;

public class AccessAndRefreshTokenResponseGenerator {

    public static AccessAndRefreshTokenResponse generateTokens() {
        AccessAndRefreshTokenResponse response = new AccessAndRefreshTokenResponse();
        response.setAccessToken(ACCESS_TOKEN);
        response.setRefreshToken(REFRESH_TOKEN);
        return response;
    }
}
