package com.kaki.doctrack.authservice.service;

import com.kaki.doctrack.authservice.dto.login.LoginRequestDto;
import com.kaki.doctrack.authservice.dto.login.LoginResponseDto;
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

    public Mono<ResponseEntity<?>> login(LoginRequestDto loginRequestDto) {
        return findByUsername(loginRequestDto.username())
                .doOnError(throwable -> logger.error("Error finding user by username: {}", loginRequestDto.username(), throwable))
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> {
                    if (passwordEncoder.matches(loginRequestDto.password(), user.getPassword())) {
                        String token = jwtUtil.generateToken(user);
                        String refreshToken = jwtUtil.generateRefreshToken(user);
                        return Mono.just(ResponseEntity.ok(new LoginResponseDto(token, refreshToken)));
                    } else {
                        return Mono.just(ResponseEntity.badRequest().body("Username or password is incorrect"));
                    }
                });
    }

    public Mono<ResponseEntity<?>> refreshToken(String token) {
        if (!jwtUtil.validateJwtRefreshToken(token)) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid token"));
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        return userRepository.findById(userId).flatMap(user -> Mono.just(jwtUtil.refreshToken(token, user)))
                .map(ResponseEntity::ok);
    }

    private Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean validateJwtToken(String token) {
        return jwtUtil.validateJwtToken(token);
    }
}
