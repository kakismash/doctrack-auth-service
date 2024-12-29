package com.kaki.doctrack.authservice.service;

import com.kaki.doctrack.authservice.dto.user.CreateUserDto;
import com.kaki.doctrack.authservice.dto.user.InviteUserDto;
import com.kaki.doctrack.authservice.dto.user.UpdateUserDto;
import com.kaki.doctrack.authservice.dto.user.UserResponseDto;
import com.kaki.doctrack.authservice.entity.ERole;
import com.kaki.doctrack.authservice.entity.EUserStatus;
import com.kaki.doctrack.authservice.entity.User;
import com.kaki.doctrack.authservice.repository.RoleRepository;
import com.kaki.doctrack.authservice.repository.UserRepository;
import com.kaki.doctrack.authservice.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.r2dbc.spi.Result;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements ReactiveUserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final @Lazy EmailService emailService;

    @Value("${doctrack.app.link}")
    private String link;

    @Value("${doctrack.app.invitationSecretKey}")
    private String secretKey;

    @Autowired
    private @Lazy PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
    }

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

    public Mono<Page<UserResponseDto>> findUsersBySearchTerm(String searchTerm, PageRequest pageRequest) {
        int limit = pageRequest.getPageSize() + 1;  // Fetch one extra record to check for more pages
        int offset = (int) pageRequest.getOffset();

        return userRepository.findAllBySearchTermWithLimit(searchTerm, limit, offset)
                .collectList()
                .map(users -> {
                    boolean hasNext = users.size() > pageRequest.getPageSize();  // Check if there's an extra record
                    List<UserResponseDto> paginatedUsers = users.stream()
                            .limit(pageRequest.getPageSize())  // Limit the list to the page size
                            .map(UserResponseDto::fromEntity)  // Convert User to UserResponseDto
                            .collect(Collectors.toList());

                    return new PageImpl<>(paginatedUsers, pageRequest, hasNext ? pageRequest.getOffset() + pageRequest.getPageSize() + 1 : pageRequest.getOffset());
                });
    }

    public Mono<Page<UserResponseDto>> findUsersByOrganizationId(
            Long organizationId,
            String searchTerm,
            PageRequest pageRequest) {
        int limit = pageRequest.getPageSize() + 1;  // Fetch one extra record to check for more pages
        int offset = (int) pageRequest.getOffset();

        return userRepository.findAllByOrganizationIdWithLimit(organizationId, searchTerm, limit, offset)
                .collectList()
                .map(users -> {
                    boolean hasNext = users.size() > pageRequest.getPageSize();  // Check if there's an extra record
                    List<UserResponseDto> paginatedUsers = users.stream()
                            .limit(pageRequest.getPageSize())  // Limit the list to the page size
                            .map(UserResponseDto::fromEntity)  // Convert User to UserResponseDto
                            .collect(Collectors.toList());

                    return new PageImpl<>(paginatedUsers, pageRequest, hasNext ? pageRequest.getOffset() + pageRequest.getPageSize() + 1 : pageRequest.getOffset());
                });
    }

    public Mono<Void> inviteUser(InviteUserDto inviteUserDto, String organizationName) {
        // Validate input data (optional)
        if (inviteUserDto.email() == null || inviteUserDto.email().isBlank()) {
            return Mono.error(new IllegalArgumentException("Email cannot be null or empty"));
        }

        // Build the invitation email content
        String subject = "You're Invited to Join Our Organization";
        String newOrganizationMessage = !StringUtils.isNotEmpty(organizationName)
                ? "create your organization. "
                : "join the organization".concat(organizationName).concat(". ");
        // Send the email and save user details asynchronously
        return roleRepository.findByName(inviteUserDto.role())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found")))
                .flatMap(role -> {
                    User user = new User();
                    user.setFirstname(inviteUserDto.firstname());
                    user.setLastname(inviteUserDto.lastname());
                    user.setEmail(inviteUserDto.email());
                    user.setUsername(inviteUserDto.email());
                    user.setPhone(inviteUserDto.phone());
                    user.setPassword(passwordEncoder.encode("thisPasswordCannotBeUsed"));
                    user.setRole(role);
                    user.setOrganizationId(inviteUserDto.organizationId());
                    user.setStatus(EUserStatus.INVITED.ordinal());
                    return userRepository.save(user);
                })
                .flatMap(savedUser -> {
                    logger.info("User details saved: {}", savedUser);
                    // Send the invitation email

                    String message = String.format(
                            "Dear %s %s,\n\n" +
                                    "You have been invited to " +
                                    newOrganizationMessage +
                                    "Please click the link below to register:\n\n" +
                                    "Registration Link: %s\n\n" +
                                    "Role: %s\n" +
                                    "Organization ID: %d\n\n" +
                                    "Best regards,\nThe Team",
                            inviteUserDto.firstname(),
                            inviteUserDto.lastname(),
                            generateRegistrationLink(savedUser),
                            inviteUserDto.role(),
                            inviteUserDto.organizationId()
                    );

                    return emailService.sendEmail(inviteUserDto.email(), subject, message)
                            .doOnSuccess(ignored -> logger.info("Invitation sent to email: {}", inviteUserDto.email()))
                            .doOnError(e -> logger.error("Failed to send invitation email: {}", e.getMessage()));
                })
                .doOnError(e -> logger.error("Failed to process invitation: {}", e.getMessage()))
                .then();
    }


    public String generateRegistrationLink(User user) {
        // Use a secure, properly sized secret key
        long expirationTime = 3 * 24 * 60 * 60 * 1000; // 3 days in milliseconds



        Claims claims = Jwts.claims();
        claims.put("email", user.getEmail());
        claims.put("id", user.getId());
        claims.put("role", user.getRole().getName());
        claims.put("firstname", user.getFirstname());
        claims.put("lastname", user.getLastname());

        String token = Jwts.builder()
                .setClaims(claims) // Add claims
                .setSubject("activation-link") // Set the purpose of the token
                .setIssuedAt(new Date()) // Set the issue time
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Set expiration to 3 days
                .signWith(getSigningKey(secretKey), SignatureAlgorithm.HS256) // Sign with the secret key
                .compact();

        return link.concat("?token=").concat(token);
    }

    private SecretKey getSigningKey(String base64SecretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(base64SecretKey);
        return Keys.hmacShaKeyFor(keyBytes); // Ensure the key is compatible with HMAC-SHA256
    }
}
