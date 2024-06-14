package com.kaki.doctrack.authservice.repository;

import com.kaki.doctrack.authservice.entity.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
    Mono<Role> findByName(String roleName);
}
