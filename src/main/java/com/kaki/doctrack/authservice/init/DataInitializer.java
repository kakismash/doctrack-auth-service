package com.kaki.doctrack.authservice.init;

import com.kaki.doctrack.authservice.entity.ERole;
import com.kaki.doctrack.authservice.entity.Role;
import com.kaki.doctrack.authservice.entity.User;
import com.kaki.doctrack.authservice.repository.RoleRepository;
import com.kaki.doctrack.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Bean
    public ApplicationRunner initializer() {
        return args -> initRoles()
            .then(initSuperAdminUser()).subscribe();
    }

    private Mono<Void> initRoles() {
        return createRoleIfNotExists(ERole.SUPERADMIN)
                .then(createRoleIfNotExists(ERole.ADMIN))
                .then(createRoleIfNotExists(ERole.USER_READ_ONLY))
                .then(createRoleIfNotExists(ERole.USER_READ_WRITE))
                .then();
    }

    private Mono<Void> createRoleIfNotExists(ERole role) {
        return roleRepository.findByName(role.name())
                .switchIfEmpty(roleRepository.save(new Role(null, role.name())))
                .then();
    }

    private Mono<Void> initSuperAdminUser() {
        return roleRepository.findByName(ERole.SUPERADMIN.name())
                .flatMap(role -> userRepository.findByUsername("kaki1991")
                        .switchIfEmpty(Mono.defer(() -> {
                            User user = new User();
                            user.setUsername("kaki1991");
                            user.setRole(role);
                            user.setEmail("alfian1991@gmail.com");
                            user.setFirstname("Joaquin");
                            user.setLastname("Navarro");
                            user.setPhone("0123456789");

                            return userRepository.save(user);
                        }))
                ).then();
    }
}