package com.example.r2dbc.repository;

import com.example.r2dbc.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    
    Mono<User> findByUsername(String username);
    
    Mono<User> findByEmail(String email);
    
    Mono<User> findByUuid(UUID uuid);
}

