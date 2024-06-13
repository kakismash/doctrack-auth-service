package com.kaki.doctrack.authservice.init;

import com.kaki.doctrack.authservice.entity.ERole;
import com.kaki.doctrack.authservice.entity.Role;
import com.kaki.doctrack.authservice.entity.User;
import com.kaki.doctrack.authservice.repository.RoleRepository;
import com.kaki.doctrack.authservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        initRoles()
                .then(initSuperAdminUser())
                .subscribe();
    }

    private Mono<Void> initRoles() {
        return Flux.fromArray(ERole.values())
                .flatMap(role -> roleRepository.findByName(role.name())
                        .switchIfEmpty(Mono.defer(() -> {
                            Role roleEntity = new Role();
                            roleEntity.setName(role.name());
                            return roleRepository.save(roleEntity);
                        }))
                )
                .then();
    }

    private Mono<Void> initSuperAdminUser() {
        return userRepository.findByUsername("kaki1991")
                .switchIfEmpty(Mono.defer(() -> roleRepository.findByName(ERole.SUPERADMIN.name())
                        .flatMap(superAdminRole -> {
                            User user = new User();
                            user.setUsername("kaki1991");
                            user.setPassword(passwordEncoder.encode("kaki1991"));
                            user.setRole(superAdminRole);
                            return userRepository.save(user);
                        })))
                .then();
    }
}
