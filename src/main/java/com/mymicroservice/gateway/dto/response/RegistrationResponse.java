package com.mymicroservice.gateway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration response")
public class RegistrationResponse {

    private UserFromUserServiceResponse userDto;
    private AccessAndRefreshTokenResponse tokens;

}
