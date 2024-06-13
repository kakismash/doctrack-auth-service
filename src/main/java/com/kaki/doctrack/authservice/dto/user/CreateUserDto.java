package com.kaki.doctrack.authservice.dto.user;

public record CreateUserDto(String username,
                            String password,
                            String email,
                            String role,
                            String firstName,
                            String lastName,
                            String phone) {
}
