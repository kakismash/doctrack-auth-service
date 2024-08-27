package com.kaki.doctrack.authservice.dto.user;

import java.util.Optional;

public record UpdateUserDto(Optional<String> username,
                            Optional<String> email,
                            Optional<String> firstname,
                            Optional<String> lastname,
                            Optional<String> phone) {
}
