package com.mymicroservice.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationResponse {

    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private String password;
    private Role role;
}
