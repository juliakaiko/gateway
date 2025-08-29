package com.mymicroservice.gateway.util;

import com.mymicroservice.gateway.dto.response.UserFromUserServiceResponse;

import java.time.LocalDate;

public class UserFromUserServiceResponseGenerator {

    public static UserFromUserServiceResponse generateUser() {

        return  UserFromUserServiceResponse.builder()
                .userId(1l)
                .name("TestName")
                .surname("TestSurName")
                .birthDate(LocalDate.of(2000, 2, 2))
                .email("test@test.by")
                .build();
    }
}
