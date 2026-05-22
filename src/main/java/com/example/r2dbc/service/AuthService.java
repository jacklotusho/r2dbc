package com.example.r2dbc.service;

import com.example.r2dbc.dto.AuthResponse;
import com.example.r2dbc.dto.LoginRequest;
import com.example.r2dbc.exception.LdapUserException;
import com.example.r2dbc.security.JwtUtil;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class AuthService implements ReactiveUserDetailsService {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * Login user and generate JWT token
     * Try username first, then email if username not found
     */
    public Mono<AuthResponse> login(LoginRequest request) {
        return userService.findByUsername(request.getUsernameOrEmail())
                .switchIfEmpty(userService.findByEmail(request.getUsernameOrEmail()))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                .flatMap(user -> {
                    // Block LDAP-managed users from using the local login endpoint
                    if (user.getPasswordHash() != null &&
                            user.getPasswordHash().startsWith(LdapAuthService.LDAP_PASSWORD_PREFIX)) {
                        return Mono.error(new LdapUserException(
                                "This account is managed by LDAP. Please use /auth/ldap/login"));
                    }

                    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                        return Mono.error(new RuntimeException("Invalid credentials"));
                    }
                    
                    if (!user.isEnabled()) {
                        return Mono.error(new RuntimeException("User account is disabled"));
                    }
                    
                    return userService.getUserRoles(user.getId())
                            .map(role -> role.getName())
                            .collectList()
                            .map(roles -> {
                                String token = jwtUtil.generateToken(user.getUuid(), roles);
                                return AuthResponse.builder()
                                        .token(token)
                                        .uuid(user.getUuid())
                                        .username(user.getUsername())
                                        .roles(roles)
                                        .build();
                            });
                });
    }
    
    /**
     * Resolve user by UUID (from JWT sub claim)
     * Used by security filter to load user details
     */
    public Mono<UserDetails> resolveUserByUuid(UUID uuid) {
        return userService.findByUuid(uuid)
                .flatMap(user -> 
                        userService.getUserRoles(user.getId())
                                .map(role -> new SimpleGrantedAuthority(role.getName()))
                                .collectList()
                                .map(authorities -> 
                                        org.springframework.security.core.userdetails.User.builder()
                                                .username(user.getUuid().toString())
                                                .password(user.getPasswordHash())
                                                .authorities(authorities)
                                                .disabled(!user.isEnabled())
                                                .build()
                                )
                );
    }
    
    /**
     * Load user by username (required by ReactiveUserDetailsService)
     * This is used for username-based authentication
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userService.findByUsername(username)
                .flatMap(user -> 
                        userService.getUserRoles(user.getId())
                                .map(role -> new SimpleGrantedAuthority(role.getName()))
                                .collectList()
                                .map(authorities -> 
                                        org.springframework.security.core.userdetails.User.builder()
                                                .username(user.getUsername())
                                                .password(user.getPasswordHash())
                                                .authorities(authorities)
                                                .disabled(!user.isEnabled())
                                                .build()
                                )
                )
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)));
    }
}
