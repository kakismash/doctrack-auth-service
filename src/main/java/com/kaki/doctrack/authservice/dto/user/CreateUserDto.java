package com.kaki.doctrack.authservice.dto.user;

public record CreateUserDto(String username,
                            String password,
                            String email,
                            String role,
                            String firstName,
                            String lastName,
                            String phone) {
    public CreateUserDto withPassword(String password) {
        return new CreateUserDto(this.username, password, this.email, this.role, this.firstName, this.lastName, this.phone);
    }
}
