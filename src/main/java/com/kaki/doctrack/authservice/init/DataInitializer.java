package com.kaki.doctrack.authservice.init;

import com.kaki.doctrack.authservice.entity.ERole;
import com.kaki.doctrack.authservice.entity.EUserStatus;
import com.kaki.doctrack.authservice.entity.Role;
import com.kaki.doctrack.authservice.entity.User;
import com.kaki.doctrack.authservice.repository.RoleRepository;
import com.kaki.doctrack.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
//
//    private final UserRepository userRepository;
//    private final RoleRepository roleRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Bean
//    public ApplicationRunner initializer() {
//        return args -> initRoles()
//            .then(initSuperAdminUser()).subscribe();
//    }
//
//    private Mono<Void> initRoles() {
//        return createRoleIfNotExists(ERole.SUPER_ADMIN)
//                .then(createRoleIfNotExists(ERole.ADMIN))
//                .then(createRoleIfNotExists(ERole.ORGANIZATION_ADMIN))
//                .then(createRoleIfNotExists(ERole.ORGANIZATION_WORKER))
//                .then();
//    }
//
//    private Mono<Void> createRoleIfNotExists(ERole role) {
//        return roleRepository.findByName(role.name())
//                .switchIfEmpty(roleRepository.save(new Role(null, role.name())))
//                .then();
//    }
//
//    private Mono<Void> initSuperAdminUser() {
//        return roleRepository.findByName(ERole.SUPER_ADMIN.name())
//                .flatMap(role -> userRepository.findByUsername("kaki1991")
//                        .switchIfEmpty(Mono.defer(() -> {
//                            User user = new User();
//                            user.setUsername("kaki1991");
//                            user.setRole(role);
//                            user.setEmail("alfian1991@gmail.com");
//                            user.setFirstname("Joaquin");
//                            user.setLastname("Navarro");
//                            user.setStatus(EUserStatus.ACTIVE.ordinal());
//                            user.setPhone("0123456789");
//                            user.setPassword(passwordEncoder.encode("kaki1991"));
//                            return userRepository.save(user);
//                        }))
//                ).then();
//    }
}