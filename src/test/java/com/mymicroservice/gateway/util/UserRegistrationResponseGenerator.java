package com.mymicroservice.gateway.util;

import com.mymicroservice.gateway.dto.response.Role;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;

import static com.mymicroservice.gateway.util.data.TestConstants.USER_BIRTH_DATE;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_NAME;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_EMAIL;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_PASSWORD;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_SURNAME;

public class UserRegistrationResponseGenerator {

    public static UserRegistrationResponse generateUser() {
        UserRegistrationResponse response = new UserRegistrationResponse();
        response.setName(USER_NAME);
        response.setSurname(USER_SURNAME);
        response.setBirthDate(USER_BIRTH_DATE);
        response.setEmail(USER_EMAIL);
        response.setPassword(USER_PASSWORD);
        response.setRole(Role.USER);
        return response;
    }
}
