package com.mymicroservice.gateway.util;

import com.mymicroservice.gateway.dto.response.Role;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;

import java.time.LocalDate;

public class UserRegistrationResponseGenerator {

    public static UserRegistrationResponse generateUser() {

        return  UserRegistrationResponse.builder()
                .name("TestName")
                .surname("TestSurName")
                .birthDate(LocalDate.of(2000, 2, 2))
                .email("test@test.by")
                .password("pass_test")
                .role(Role.USER)
                .build();
    }
}
