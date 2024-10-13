package com.kaki.doctrack.authservice.security.jwt;

import com.kaki.doctrack.authservice.dto.UserInfoDTO;
import com.kaki.doctrack.authservice.dto.login.LoginResponseDto;
import com.kaki.doctrack.authservice.entity.User;
import com.kaki.doctrack.authservice.exception.JWTException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public SecretKey getSigningKey() {
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
        try {
            Claims claims =  Jwts.parserBuilder() // Use the new parserBuilder() method
                    .setSigningKey(getSigningKey()) // Set the signing key
                    .build() // Build the parser
                    .parseClaimsJws(token) // Parse the token
                    .getBody(); // Get the claims body
            logger.info("Extracted claims: {}", claims);
            return claims;
        } catch (Exception e) {
            logger.error("Failed to parse JWT token: {}", token, e);
            throw new JWTException("Invalid JWT token", "JWT_INVALID");
        }

    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()  // Use parserBuilder()
                    .setSigningKey(getSigningKey())  // Set the signing key
                    .build()  // Build the parser
                    .parseClaimsJws(authToken);  // Parse and validate the token
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw new JWTException("INVALID_SIGNATURE", "Invalid JWT signature");
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new JWTException("INVALID_TOKEN", "Invalid JWT token");
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            throw new JWTException("INVALID_TOKEN", "JWT token is expired");
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw new JWTException("INVALID_TOKEN", "JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw new JWTException("INVALID_TOKEN", "JWT claims string is empty");
        }
    }

    private String createToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getName());
        claims.put("id", user.getId());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // Explicitly use HS256
                .compact();
    }

    private String createRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getName());
        claims.put("refresh", true);
        claims.put("id", user.getId());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs * 2L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // Explicitly use HS256
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()  // Use parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)  // Parse and get claims
                .getBody()
                .getSubject();

    }

    public String generateToken(User user) {
        return createToken(user);
    }

    public LoginResponseDto refreshToken(String token, User user) {
        return new LoginResponseDto(createToken(user), createRefreshToken(user));
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

    public Authentication getCurrentUserFromSecurityContext() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public UserInfoDTO getUserInfoFromJwtToken(String token) {
        Claims claims = extractAllClaims(token);
        return new UserInfoDTO(getUserNameFromJwtToken(token), claims.get("id", Long.class), claims.get("role", String.class));
    }
}
