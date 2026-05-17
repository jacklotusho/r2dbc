package com.example.r2dbc.service;

import com.example.r2dbc.dto.ResetPasswordRequest;
import com.example.r2dbc.entity.PasswordResetToken;
import com.example.r2dbc.exception.InvalidTokenException;
import com.example.r2dbc.exception.TokenExpiredException;
import com.example.r2dbc.repository.PasswordResetTokenRepository;
import com.example.r2dbc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.frontend-base-url:http://localhost:3000}")
    private String frontendBaseUrl;
    
    @Value("${app.reset-token-expiry-minutes:60}")
    private int expiryMinutes;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                EmailService emailService,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }
    
    public Mono<Void> forgotPassword(String email) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    String token = generateToken();
                    PasswordResetToken resetToken = PasswordResetToken.builder()
                            .userId(user.getId())
                            .token(token)
                            .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                            .createdAt(LocalDateTime.now())
                            .used(false)
                            .build();
                    
                    return tokenRepository.save(resetToken)
                            .flatMap(savedToken -> {
                                String resetLink = frontendBaseUrl + "/reset-password?token=" + token;
                                return emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
                            });
                })
                .then(); // Return empty Mono silently if user not found or after success
    }
    
    public Mono<Void> resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return Mono.error(new InvalidTokenException("Passwords do not match"));
        }
        
        return tokenRepository.findByToken(request.getToken())
                .switchIfEmpty(Mono.error(new InvalidTokenException("Invalid or already used reset token")))
                .flatMap(token -> {
                    if (token.isUsed()) {
                        return Mono.error(new InvalidTokenException("Invalid or already used reset token"));
                    }
                    if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return Mono.error(new TokenExpiredException("Reset token has expired"));
                    }
                    
                    return userRepository.findById(token.getUserId())
                            .flatMap(user -> {
                                user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                                return userRepository.save(user);
                            })
                            .then(Mono.defer(() -> {
                                token.setUsed(true);
                                token.setUsedAt(LocalDateTime.now());
                                return tokenRepository.save(token);
                            }));
                })
                .then();
    }
    
    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
