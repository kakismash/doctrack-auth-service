package com.kaki.doctrack.authservice.security.jwt;

import com.kaki.doctrack.authservice.dto.login.LoginResponseDto;
import com.kaki.doctrack.authservice.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Getter
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${doctrack.app.jwtSecret}")
    private String jwtSecret;

    @Value("${doctrack.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long getUserIdFromToken(String token) {
        return extractClaim(token, claims -> Long.parseLong(claims.get("id").toString()));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token).getPayload();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    private String createToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getName());

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    private String createRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getName());
        claims.put("refresh", true);
        claims.put("id", user.getId());

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs * 2L))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

    }

    public String generateToken(User user) {
        return createToken(user);
    }

    public LoginResponseDto refreshToken(String token, User user) {

//        if (isTokenExpired(token)) {
            return new LoginResponseDto(createToken(user), createRefreshToken(user));
//        } else {
//            throw new BadRequestException("Token is not expired yet");
//        }
    }

    public String generateRefreshToken(User user) {
        return createRefreshToken(user);
    }

    private boolean isRefreshToken(String token) {
        return extractClaim(token, claims -> claims.get("refresh", Boolean.class));
    }

    public boolean validateJwtRefreshToken(String token) {
        return isRefreshToken(token) && validateJwtToken(token);
    }
}
