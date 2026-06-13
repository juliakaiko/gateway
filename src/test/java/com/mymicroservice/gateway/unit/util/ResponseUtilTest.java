package com.mymicroservice.gateway.unit.util;

import com.mymicroservice.gateway.dto.request.UserRegistrationRequest;
import com.mymicroservice.gateway.dto.response.Role;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;
import com.mymicroservice.gateway.util.ResponseUtil;
import com.mymicroservice.gateway.util.UserRegistrationRequestGenerator;
import com.mymicroservice.gateway.util.data.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResponseUtilTest {

    private ResponseUtil responseUtil;

    @BeforeEach
    void setUp() {
        responseUtil = new ResponseUtil();
    }

    @Test
    void generateUserResponse_ShouldAssignUserRole_WhenRoleIsUser() {
        UserRegistrationRequest request = UserRegistrationRequestGenerator.generateUser();
        request.setRole(Role.USER);

        UserRegistrationResponse response = responseUtil.generateUserResponse(request);

        assertEquals(Role.USER, response.getRole());
        assertEquals(TestConstants.USER_EMAIL, response.getEmail());
    }

    @Test
    void generateUserResponse_ShouldAssignAdminRole_WhenRoleIsAdmin() {
        UserRegistrationRequest request = UserRegistrationRequestGenerator.generateUser();
        request.setRole(Role.ADMIN);

        UserRegistrationResponse response = responseUtil.generateUserResponse(request);

        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    void generateUserResponse_ShouldDefaultToUserRole_WhenRoleIsNull() {
        UserRegistrationRequest request = UserRegistrationRequestGenerator.generateUser();
        request.setRole(null);

        UserRegistrationResponse response = responseUtil.generateUserResponse(request);

        assertEquals(Role.USER, response.getRole());
    }

    @Test
    void generateUserResponse_ShouldThrowIllegalArgumentException_WhenRoleIsUnknown() {
        UserRegistrationRequest request = UserRegistrationRequestGenerator.generateUser();
        Role unknownRole = mock(Role.class);
        when(unknownRole.getAuthority()).thenReturn("SUPERADMIN");
        request.setRole(unknownRole);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> responseUtil.generateUserResponse(request)
        );

        assertEquals("Unknown or unsupported role: SUPERADMIN", exception.getMessage());
    }
}
