package com.kaki.doctrack.authservice.service;

import com.kaki.doctrack.authservice.dto.user.CreateUserDto;
import com.kaki.doctrack.authservice.dto.user.UpdateUserDto;
import com.kaki.doctrack.authservice.dto.user.UserResponseDto;
import com.kaki.doctrack.authservice.entity.User;
import com.kaki.doctrack.authservice.repository.RoleRepository;
import com.kaki.doctrack.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService implements ReactiveUserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public Mono<UserResponseDto> createUser(CreateUserDto userDto) {

        return roleRepository.findByName(userDto.role())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found")))
                .flatMap(role -> {
                    User user = new User();
                    user.setUsername(userDto.username());
                    user.setPassword(userDto.password());
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

    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::buildUserDetails);
    }

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().getName())
                .build();
    }

    public Mono<UserResponseDto> findUserByUsername(String username) {
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
                    if (userDto.firstname().isPresent()) {
                        user.setFirstname(userDto.firstname().get());
                    }

                    if (userDto.lastname().isPresent()) {
                        user.setLastname(userDto.lastname().get());
                    }

                    if (userDto.phone().isPresent()) {
                        user.setPhone(userDto.phone().get());
                    }

                    if (userDto.email().isPresent()) {
                        user.setEmail(userDto.email().get());
                    }

                    if (userDto.username().isPresent()) {
                        user.setUsername(userDto.username().get());
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
                    user.setPassword(password);
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
                .flatMap(user -> roleRepository.findByName(role)
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
                        }));
    }

}
