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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DtoCoverageTest {

    @Test
    void userRegistrationRequest_ShouldSupportEqualsAndHashCode_WhenSameValues() {
        UserRegistrationRequest first = UserRegistrationRequestGenerator.generateUser();
        UserRegistrationRequest second = UserRegistrationRequestGenerator.generateUser();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotNull(first.toString());
    }

    @Test
    void userRegistrationResponse_ShouldSupportEqualsAndHashCode_WhenSameValues() {
        UserRegistrationResponse first = UserRegistrationResponseGenerator.generateUser();
        UserRegistrationResponse second = UserRegistrationResponseGenerator.generateUser();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void userFromUserServiceResponse_ShouldSupportEqualsAndHashCode_WhenSameValues() {
        UserFromUserServiceResponse first = UserFromUserServiceResponseGenerator.generateUser();
        UserFromUserServiceResponse second = UserFromUserServiceResponseGenerator.generateUser();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void accessAndRefreshTokenResponse_ShouldSupportEqualsAndHashCode_WhenSameValues() {
        AccessAndRefreshTokenResponse first = AccessAndRefreshTokenResponseGenerator.generateTokens();
        AccessAndRefreshTokenResponse second = AccessAndRefreshTokenResponseGenerator.generateTokens();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void registrationResponse_ShouldSupportEqualsAndHashCode_WhenSameValues() {
        RegistrationResponse first = new RegistrationResponse(
                UserFromUserServiceResponseGenerator.generateUser(),
                AccessAndRefreshTokenResponseGenerator.generateTokens()
        );
        RegistrationResponse second = new RegistrationResponse(
                UserFromUserServiceResponseGenerator.generateUser(),
                AccessAndRefreshTokenResponseGenerator.generateTokens()
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void roleEnum_ShouldContainUserAndAdmin_WhenValuesRequested() {
        assertEquals("USER", Role.USER.getAuthority());
        assertEquals("ADMIN", Role.ADMIN.getAuthority());
        assertNotEquals(Role.USER, Role.ADMIN);
    }
}
