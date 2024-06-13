package com.kaki.doctrack.authservice.rest;

import com.kaki.doctrack.authservice.dto.LoginRequestDto;
import com.kaki.doctrack.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${api.path}${api.version}/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;

    private final Logger logger = LoggerFactory.getLogger(AuthRestController.class);

    @GetMapping("/test")
    public Mono<ResponseEntity<String>> test() {
        logger.info("Test");
        return Mono.just(ResponseEntity.ok("Test"));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(LoginRequestDto loginRequestDto) {
        return authService.login(loginRequestDto);
    }

    @GetMapping("/refresh")
    public Mono<ResponseEntity<String>> refreshToken(String token) {
        return authService.refreshToken(token);
    }

}
