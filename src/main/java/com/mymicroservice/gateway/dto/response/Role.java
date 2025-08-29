package com.mymicroservice.gateway.dto.response;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
public enum Role implements GrantedAuthority{
    USER ("USER"),
    ADMIN("ADMIN");

    private final String roleName;

    @Override
    public String getAuthority() {
        return roleName;
    }
}
