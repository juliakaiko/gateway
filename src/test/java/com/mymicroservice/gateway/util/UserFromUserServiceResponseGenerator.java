package com.mymicroservice.gateway.util;

import com.mymicroservice.gateway.dto.response.UserFromUserServiceResponse;

import static com.mymicroservice.gateway.util.data.TestConstants.USER_BIRTH_DATE;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_EMAIL;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_ID;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_NAME;
import static com.mymicroservice.gateway.util.data.TestConstants.USER_SURNAME;

public class UserFromUserServiceResponseGenerator {

    public static UserFromUserServiceResponse generateUser() {
        UserFromUserServiceResponse response = new UserFromUserServiceResponse();
        response.setUserId(USER_ID);
        response.setName(USER_NAME);
        response.setSurname(USER_SURNAME);
        response.setBirthDate(USER_BIRTH_DATE);
        response.setEmail(USER_EMAIL);
        return response;
    }
}
