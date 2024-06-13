package com.kaki.doctrack.authservice.service;

import com.kaki.doctrack.authservice.dto.user.CreateUserDto;
import com.kaki.doctrack.authservice.dto.user.UpdateUserDto;
import com.kaki.doctrack.authservice.dto.user.UserResponseDto;
import com.kaki.doctrack.authservice.entity.User;
import com.kaki.doctrack.authservice.repository.RoleRepository;
import com.kaki.doctrack.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.internal.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public Mono<UserResponseDto> createUser(CreateUserDto userDto) {

        return roleRepository.findByName(userDto.role())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found")))
                .flatMap(role -> {
                    User user = new User();
                    user.setUsername(userDto.username());
                    user.setPassword(passwordEncoder.encode(userDto.password()));
                    user.setRole(role);
                    return userRepository.save(user)
                            .doOnSuccess(u -> logger.info("User created: {}", u))
                            .doOnError(e -> logger.error("Error creating user", e))
                            .flatMap(u -> Mono.just(new UserResponseDto(u.getId(),
                                    u.getFirstname(),
                                    u.getLastname(),
                                    u.getEmail(),
                                    u.getUsername(),
                                    u.getPhone(),
                                    u.getRole().getName())));
                });
    }

    public Mono<UserResponseDto> findByUsername(String username) {
        return userRepository.findByUsername(username).flatMap(user -> Mono.just(new UserResponseDto(user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getUsername(),
                user.getPhone(),
                user.getRole().getName())));
    }

    public Mono<UserResponseDto> findById(Long id) {
        return userRepository.findById(id).flatMap(user -> Mono.just(new UserResponseDto(user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getUsername(),
                user.getPhone(),
                user.getRole().getName())));
    }

    public Mono<Void> deleteById(Long id) {
        return userRepository.deleteById(id);
    }

    public Mono<UserResponseDto> update(Long id, UpdateUserDto userDto) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")))
                .flatMap(user -> {
                    if (StringHelper.isNotEmpty(userDto.firstName())) {
                        user.setFirstname(userDto.firstName());
                    }
                    if (StringHelper.isNotEmpty(userDto.lastName())) {
                        user.setLastname(userDto.lastName());
                    }

                    if (StringHelper.isNotEmpty(userDto.phone())) {
                        user.setPhone(userDto.phone());
                    }

                    if (StringHelper.isNotEmpty(userDto.email())) {
                        user.setEmail(userDto.email());
                    }

                    if (StringHelper.isNotEmpty(userDto.username())) {
                        user.setUsername(userDto.username());
                    }

                    return userRepository.save(user).flatMap(u -> Mono.just(new UserResponseDto(u.getId(),
                            u.getFirstname(),
                            u.getLastname(),
                            u.getEmail(),
                            u.getUsername(),
                            u.getPhone(),
                            u.getRole().getName())));
                });
    }

    public Mono<UserResponseDto> updatePassword(Long id, String password) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")))
                .flatMap(user -> {
                    user.setPassword(passwordEncoder.encode(password));
                    return userRepository.save(user).flatMap(u -> Mono.just(new UserResponseDto(u.getId(),
                            u.getFirstname(),
                            u.getLastname(),
                            u.getEmail(),
                            u.getUsername(),
                            u.getPhone(),
                            u.getRole().getName())));
                });
    }

    public Mono<UserResponseDto> updateRole(Long id, String role) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")))
                .flatMap(user -> {
                    return roleRepository.findByName(role)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found")))
                            .flatMap(r -> {
                                user.setRole(r);
                                return userRepository.save(user).flatMap(u -> Mono.just(new UserResponseDto(u.getId(),
                                        u.getFirstname(),
                                        u.getLastname(),
                                        u.getEmail(),
                                        u.getUsername(),
                                        u.getPhone(),
                                        u.getRole().getName())));
                            });
                });
    }

}
