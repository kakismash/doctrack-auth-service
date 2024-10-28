package com.kaki.doctrack.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InternalApiKeyService {

    private final Logger logger = LoggerFactory.getLogger(InternalApiKeyService.class);

    @Value("${doctrack.app.jwt.internalSecret}")
    private String SECRET_KEY_BASE64;

    private SecretKey SECRET_KEY;

    @PostConstruct
    public void init() {
        // Decode the base64-encoded secret key from application properties
        byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY_BASE64);
        this.SECRET_KEY = Keys.hmacShaKeyFor(decodedKey);
    }

    public Mono<String> generateApiKey(String microserviceName) {
        return Mono.fromCallable(() -> {
            logger.info("Generating API key for microservice: {}", microserviceName);

            Map<String, Object> claims = new HashMap<>();
            claims.put("microservice", microserviceName);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject("internal-api-key")
                    .setIssuedAt(new Date())
                    .signWith(SECRET_KEY)
                    .compact();
        });
    }

    public Mono<String> decodeApiKey(String apiKey) {
        return Mono.fromCallable(() -> {
            logger.info("Decoding API key: {}", apiKey);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(apiKey)
                    .getBody();

            logger.info("Decoded API key: {}", claims);

            return claims.get("microservice", String.class);
        });
    }

}
