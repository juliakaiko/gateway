package com.mymicroservice.gateway.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserRegistrationResponse {

    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private String password;
    private Role role;
}
