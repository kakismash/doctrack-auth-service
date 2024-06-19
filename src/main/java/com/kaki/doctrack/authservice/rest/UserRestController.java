package com.kaki.doctrack.authservice.rest;

import com.kaki.doctrack.authservice.dto.user.CreateUserDto;
import com.kaki.doctrack.authservice.dto.user.UpdateUserDto;
import com.kaki.doctrack.authservice.dto.user.UserResponseDto;
import com.kaki.doctrack.authservice.service.AuthService;
import com.kaki.doctrack.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${api.path}${api.version}/users")
@RequiredArgsConstructor
public class UserRestController {

    private final Logger logger = LoggerFactory.getLogger(UserRestController.class);

    private final UserService userService;
    private final AuthService authService;

    @PostMapping
    public Mono<ResponseEntity<UserResponseDto>> createUser(@RequestBody CreateUserDto userDto) {
        if (userDto.password() == null || userDto.password().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        } else {
            String tempPass = userDto.password();
            userDto.withPassword(authService.encodePassword(tempPass));
        }
        return userService.createUser(userDto).flatMap(user -> {
            logger.info("User created: {}", user);
            return Mono.just(ResponseEntity.ok(user));
        }).onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(null)));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserResponseDto>> findById(@PathVariable("id") Long id) {
        return userService.findById(id)
                .flatMap(user -> {
                    logger.info("User found: {}", user);
                    return Mono.just(ResponseEntity.ok(user));
        }).onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserResponseDto>> updateUser(@PathVariable("id") Long id, @RequestBody UpdateUserDto userDto) {
        return userService.update(id, userDto)
                .flatMap(user -> {
                    logger.info("User updated: {}", user);
                    return Mono.just(ResponseEntity.ok(user));
                }).onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(null)));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable("id") Long id) {
        return userService.deleteById(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(e -> {
                    logger.error("Error deleting user with id {}: {}", id, e.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    @PutMapping("/{id}/password")
    public Mono<ResponseEntity<Void>> updatePassword(@PathVariable("id") Long id, @RequestBody String password) {
        return userService.updatePassword(id, authService.encodePassword(password))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(e -> {
                    logger.error("Error updating password for user with id {}: {}", id, e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @PutMapping("/{id}/role")
    public Mono<ResponseEntity<UserResponseDto>> updateRole(@PathVariable("id") Long id, @RequestBody String role) {
        return userService.updateRole(id, role)
                .flatMap(u -> Mono.just(ResponseEntity.ok(u)))
                .onErrorResume(e -> {
                    logger.error("Error updating role for user with id {}: {}", id, e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }
}
