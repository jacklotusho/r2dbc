package com.example.r2dbc.repository;

import com.example.r2dbc.entity.UserRole;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRoleRepository extends R2dbcRepository<UserRole, Long> {
    
    Flux<UserRole> findByUserId(Long userId);

    Mono<Void> deleteByUserIdAndRoleId(Long userId, Long roleId);
}
