package com.kaki.doctrack.authservice.dto.user;

import com.kaki.doctrack.authservice.entity.User;

public record UserResponseDto (Long id,
                               String firstname,
                               String lastname,
                               String email,
                               String username,
                               String phone,
                               String role) {
    static public UserResponseDto fromEntity(User user) {
        return new UserResponseDto(user.getId(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getUsername(), user.getPhone(), user.getRole().getName());
    }
}