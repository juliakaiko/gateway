package com.mymicroservice.gateway.util;

import com.mymicroservice.gateway.dto.request.UserRegistrationRequest;

import static com.mymicroservice.gateway.util.data.TestConstants.USER_BIRTH_DATE;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_EMAIL;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_NAME;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_PASSWORD;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_SURNAME;

public class UserRegistrationRequestGenerator {

    public static UserRegistrationRequest generateUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName(USER_NAME);
        request.setSurname(USER_SURNAME);
        request.setBirthDate(USER_BIRTH_DATE);
        request.setEmail(USER_EMAIL);
        request.setPassword(USER_PASSWORD);
        return request;
    }
}
