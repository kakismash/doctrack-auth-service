package com.kaki.doctrack.authservice.service;

import com.kaki.doctrack.authservice.dto.LoginRequestDto;
import com.kaki.doctrack.authservice.entity.User;
import com.kaki.doctrack.authservice.security.jwt.JwtUtil;
import com.kaki.doctrack.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public Mono<ResponseEntity<String>> login(LoginRequestDto loginRequestDto) {
        return findByUsername(loginRequestDto.username())
                .doOnError(throwable -> logger.error("Error finding user by username: {}", loginRequestDto.username(), throwable))
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> {
                    if (passwordEncoder.matches(loginRequestDto.password(), user.getPassword())) {
                        String token = jwtUtil.generateToken(user.getUsername());
                        return Mono.just(ResponseEntity.ok(token));
                    } else {
                        return Mono.just(ResponseEntity.badRequest().body("Invalid password"));
                    }
                });
    }

    public Mono<ResponseEntity<String>> refreshToken(String token) {
        return Mono.just(jwtUtil.refreshToken(token))
                .map(ResponseEntity::ok);
    }

    private Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
