package com.kaki.doctrack.authservice.dto.user;

public record UserResponseDto (Long id,
                                                String firstname,
                                                String lastname,
                                                String email,
                                                String username,
                                                String phone,
                                                String roleName) {
}