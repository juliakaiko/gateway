package com.mymicroservice.gateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Authentication response")
public class AccessAndRefreshTokenResponse {

    private String accessToken;
    private String refreshToken;
}
