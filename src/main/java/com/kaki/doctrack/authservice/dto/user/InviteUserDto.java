package com.kaki.doctrack.authservice.dto.user;

public record InviteUserDto(
        String firstname,
        String lastname,
        String email,
        String role,
        Long organizationId,
        String phone) {
}
