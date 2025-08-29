package com.mymicroservice.gateway.util;

import com.mymicroservice.gateway.dto.request.UserRegistrationRequest;
import com.mymicroservice.gateway.dto.response.Role;

import java.time.LocalDate;

public class UserRegistrationRequestGenerator {

    public static UserRegistrationRequest generateUser() {

        return  UserRegistrationRequest.builder()
                .name("TestName")
                .surname("TestSurName")
                .birthDate(LocalDate.of(2000, 2, 2))
                .email("test@test.by")
                .password("pass_test")
                .build();
    }
}
