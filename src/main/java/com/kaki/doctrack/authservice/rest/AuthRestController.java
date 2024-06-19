package com.kaki.doctrack.authservice.rest;

import com.kaki.doctrack.authservice.dto.RefreshTokenRequestDto;
import com.kaki.doctrack.authservice.dto.login.LoginRequestDto;
import com.kaki.doctrack.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public Mono<ResponseEntity<?>> login(@RequestBody LoginRequestDto loginRequestDto) {
        return authService.login(loginRequestDto);
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<?>> refreshToken(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        return authService.refreshToken(refreshTokenRequestDto.refreshToken());
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(@RequestParam("token") String token) {
        if (authService.validateJwtToken(token)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
