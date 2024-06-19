package com.kaki.doctrack.authservice.dto.user;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public record UserResponseDto (Long id,
                               String firstname,
                               String lastname,
                               String email,
                               String username,
                               String phone,
                               String roleName) {
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> roleName);
    }
}