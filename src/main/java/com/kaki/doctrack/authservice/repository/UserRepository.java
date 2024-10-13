package com.kaki.doctrack.authservice.repository;

import com.kaki.doctrack.authservice.dto.user.UserResponseDto;
import com.kaki.doctrack.authservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    Mono<User> findByUsername(String username);

    @Query("SELECT * FROM users WHERE " +
            "LOWER(username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(firstname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(lastname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "phone LIKE CONCAT('%', :searchTerm, '%') " +
            "LIMIT :limit OFFSET :offset")
    Flux<User> findAllBySearchTermWithLimit(@Param("searchTerm") String searchTerm,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset);

    @Query("SELECT COUNT(*) FROM users WHERE LOWER(username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(firstname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(lastname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR phone LIKE CONCAT('%', :searchTerm, '%')")
    Mono<Integer> countBySearchTermAsInteger(@Param("searchTerm") String searchTerm);
}
