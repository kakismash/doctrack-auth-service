package com.kaki.doctrack.authservice.dto.user;

public record UpdateUserDto(String username,
                             String email,
                             String firstName,
                             String lastName,
                             String phone) {
}
