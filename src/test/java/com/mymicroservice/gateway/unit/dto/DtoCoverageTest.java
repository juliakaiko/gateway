package com.mymicroservice.gateway.unit.dto;

import com.mymicroservice.gateway.dto.request.UserRegistrationRequest;
import com.mymicroservice.gateway.dto.response.AccessAndRefreshTokenResponse;
import com.mymicroservice.gateway.dto.response.RegistrationResponse;
import com.mymicroservice.gateway.dto.response.Role;
import com.mymicroservice.gateway.dto.response.UserFromUserServiceResponse;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;
import com.mymicroservice.gateway.util.AccessAndRefreshTokenResponseGenerator;
import com.mymicroservice.gateway.util.UserFromUserServiceResponseGenerator;
import com.mymicroservice.gateway.util.UserRegistrationRequestGenerator;
import com.mymicroservice.gateway.util.UserRegistrationResponseGenerator;
import com.mymicroservice.gateway.util.data.TestConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DtoCoverageTest {

    @Test
    void userRegistrationRequest_ShouldExposeAllFields_WhenValuesAreSet() {
        UserRegistrationRequest request = UserRegistrationRequestGenerator.generateUser();
        request.setRole(Role.ADMIN);

        assertEquals(TestConstants.USER_NAME, request.getName());
        assertEquals(TestConstants.USER_SURNAME, request.getSurname());
        assertEquals(TestConstants.USER_BIRTH_DATE, request.getBirthDate());
        assertEquals(TestConstants.USER_EMAIL, request.getEmail());
        assertEquals(TestConstants.USER_PASSWORD, request.getPassword());
        assertEquals(Role.ADMIN, request.getRole());
    }

    @Test
    void userRegistrationResponse_ShouldExposeAllFields_WhenValuesAreSet() {
        UserRegistrationResponse response = UserRegistrationResponseGenerator.generateUser();

        assertEquals(TestConstants.USER_NAME, response.getName());
        assertEquals(TestConstants.USER_SURNAME, response.getSurname());
        assertEquals(TestConstants.USER_BIRTH_DATE, response.getBirthDate());
        assertEquals(TestConstants.USER_EMAIL, response.getEmail());
        assertEquals(TestConstants.USER_PASSWORD, response.getPassword());
        assertNotNull(response.getRole());
    }

    @Test
    void userFromUserServiceResponse_ShouldExposeAllFields_WhenValuesAreSet() {
        UserFromUserServiceResponse response = UserFromUserServiceResponseGenerator.generateUser();

        assertEquals(TestConstants.USER_ID, response.getUserId());
        assertEquals(TestConstants.USER_NAME, response.getName());
        assertEquals(TestConstants.USER_SURNAME, response.getSurname());
        assertEquals(TestConstants.USER_BIRTH_DATE, response.getBirthDate());
        assertEquals(TestConstants.USER_EMAIL, response.getEmail());
    }

    @Test
    void accessAndRefreshTokenResponse_ShouldExposeAllFields_WhenValuesAreSet() {
        AccessAndRefreshTokenResponse response = AccessAndRefreshTokenResponseGenerator.generateTokens();

        assertEquals(TestConstants.ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TestConstants.REFRESH_TOKEN, response.getRefreshToken());
    }

    @Test
    void registrationResponse_ShouldExposeNestedDtos_WhenValuesAreSet() {
        RegistrationResponse response = new RegistrationResponse();
        response.setUserDto(UserFromUserServiceResponseGenerator.generateUser());
        response.setTokens(AccessAndRefreshTokenResponseGenerator.generateTokens());

        assertNotNull(response.getUserDto());
        assertNotNull(response.getTokens());
        assertEquals(TestConstants.USER_ID, response.getUserDto().getUserId());
        assertEquals(TestConstants.ACCESS_TOKEN, response.getTokens().getAccessToken());
    }

    @Test
    void roleEnum_ShouldContainUserAndAdmin_WhenValuesRequested() {
        assertEquals("USER", Role.USER.getAuthority());
        assertEquals("ADMIN", Role.ADMIN.getAuthority());
        assertNotEquals(Role.USER, Role.ADMIN);
    }

    @Test
    void userRegistrationRequest_ShouldAllowNullRole_WhenRoleIsOptional() {
        UserRegistrationRequest request = UserRegistrationRequestGenerator.generateUser();

        assertNull(request.getRole());
    }
}
