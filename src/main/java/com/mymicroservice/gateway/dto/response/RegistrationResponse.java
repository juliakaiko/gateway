package com.mymicroservice.gateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Registration response")
public class RegistrationResponse {

    private UserFromUserServiceResponse userDto;
    private AccessAndRefreshTokenResponse tokens;

    public RegistrationResponse(UserFromUserServiceResponse userDto, AccessAndRefreshTokenResponse tokens) {
        this.userDto = userDto;
        this.tokens = tokens;
    }
}
