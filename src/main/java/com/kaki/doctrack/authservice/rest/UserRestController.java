package com.kaki.doctrack.authservice.rest;

import com.kaki.doctrack.authservice.dto.user.CreateUserDto;
import com.kaki.doctrack.authservice.dto.user.UpdateUserDto;
import com.kaki.doctrack.authservice.dto.user.UserResponseDto;
import com.kaki.doctrack.authservice.security.jwt.JwtUtil;
import com.kaki.doctrack.authservice.service.AuthService;
import com.kaki.doctrack.authservice.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;

@RestController
@RequestMapping("${api.path}${api.version}/users")
@RequiredArgsConstructor
public class UserRestController {

    private final Logger logger = LoggerFactory.getLogger(UserRestController.class);

    private final UserService userService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @GetMapping("/roles")
    public Mono<String> getCurrentUserRoles(Authentication authentication) {
        return Mono.just("Roles: " + authentication.getAuthorities().toString());  // Get user roles
    }

    @GetMapping
    public Mono<ResponseEntity<Page<UserResponseDto>>> getUsers(
            ServerWebExchange exchange,
            Authentication authentication,
            @RequestParam(value = "buildingId", required = false) Long buildingId,
            @RequestParam(value = "page", defaultValue = "0") int page,  // Default page = 0
            @RequestParam(value = "size", defaultValue = "10") int size,  // Default size = 10
            @RequestParam(value = "search", required = false, defaultValue = "") String searchTerm) {

        PageRequest pageRequest = PageRequest.of(page, size);

        // Extract user roles
        // Extract user roles
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isSuperAdmin = authorities.stream().anyMatch(role -> role.getAuthority().equals("ROLE_SUPERADMIN"));
        boolean isAdmin = authorities.stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        Long finalBuildingId;
        // If the user is not an admin, use the buildingId from the JWT token
        if (!isAdmin && !isSuperAdmin) {
            // Assume the buildingId is stored as a claim in the JWT
            String token = extractTokenFromRequest(exchange);
            if (token != null) {
                // Extract buildingId from the token
                finalBuildingId = extractBuildingIdFromToken(token);
            } else {
                return Mono.just(ResponseEntity.badRequest().build());
            }
        } else {
            if (buildingId == null) {
                return userService.findUsersBySearchTerm(searchTerm, pageRequest)
                        .map(ResponseEntity::ok)
                        .defaultIfEmpty(ResponseEntity.notFound().build());
            } else {
                return null;
            }
        }

        return null;

    }



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
    public Mono<ResponseEntity<UserResponseDto>> updateUser(@PathVariable("id") Long id, @RequestBody Mono<UpdateUserDto> updateUserDtoMono) {
        return updateUserDtoMono.flatMap(updateUserDto ->
                userService.update(id, updateUserDto)
                        .flatMap(user -> {
                            logger.info("User updated: {}", user);
                            return Mono.just(ResponseEntity.ok(user));
                        })
                        .onErrorResume(e -> {
                            logger.error("Error updating user", e);
                            return Mono.just(ResponseEntity.badRequest().body(null));
                        })
        );
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

    // Helper method to extract the JWT token from the Authorization header
    private String extractTokenFromRequest(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Remove the "Bearer " prefix
        }
        return null;
    }

    private Long extractBuildingIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtUtil.getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("buildingId", Long.class);
        } catch (JwtException | IllegalArgumentException e) {
            throw e;
        }
    }
}
