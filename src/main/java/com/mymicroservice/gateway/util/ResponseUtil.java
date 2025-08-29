package com.mymicroservice.gateway.util;

import com.mymicroservice.gateway.dto.request.UserRegistrationRequest;
import com.mymicroservice.gateway.dto.response.Role;
import com.mymicroservice.gateway.dto.response.UserRegistrationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ResponseUtil {

    /**
     * Generates a {@link UserRegistrationResponse} based on the provided {@link UserRegistrationRequest}.
     * <p>
     * The user may optionally specify a role in the request. If no role is provided,
     * the default role {@link Role#USER} will be assigned. Supported roles are {@link Role#USER} and {@link Role#ADMIN}.
     * Any other role value will result in an {@link IllegalArgumentException}.
     * <p>
     * Note: sensitive fields such as {@code password} and {@code role} are not exposed in the browser.
     *
     * @param request the user registration request containing user details and optionally a role
     * @return a {@link UserRegistrationResponse} populated with the request data and the determined role
     * @throws IllegalArgumentException if the specified role is unknown or unsupported
     */
    public UserRegistrationResponse generateUserResponse(UserRegistrationRequest request) {

        UserRegistrationResponse user = new UserRegistrationResponse();
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setBirthDate(request.getBirthDate());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        String roleAuthority = request.getRole() != null ? request.getRole().getAuthority() : null;

        if (roleAuthority == null || roleAuthority.equals("USER")) {
            user.setRole(Role.USER);
            log.info("Request to add new USER: {}", user.getEmail());
        } else if (roleAuthority.equals("ADMIN")) {
            user.setRole(Role.ADMIN);
            log.info("Request to add new ADMIN: {}", user.getEmail());
        } else {
            throw new IllegalArgumentException("Unknown or unsupported role: " + roleAuthority);
        }
        return user;
    }
}
