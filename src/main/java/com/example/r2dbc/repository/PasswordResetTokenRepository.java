package com.example.r2dbc.repository;

import com.example.r2dbc.entity.PasswordResetToken;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PasswordResetTokenRepository extends R2dbcRepository<PasswordResetToken, Long> {
    
    Mono<PasswordResetToken> findByToken(String token);
    
    Mono<Void> deleteByUserId(Long userId);
}
