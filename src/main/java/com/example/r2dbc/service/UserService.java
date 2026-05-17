package com.example.r2dbc.service;

import com.example.r2dbc.dto.RegisterRequest;
import com.example.r2dbc.entity.Role;
import com.example.r2dbc.entity.User;
import com.example.r2dbc.entity.UserRole;
import com.example.r2dbc.exception.UserAlreadyExistsException;
import com.example.r2dbc.repository.RoleRepository;
import com.example.r2dbc.repository.UserRepository;
import com.example.r2dbc.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, RoleRepository roleRepository, 
                      UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Register a new user with ROLE_USER
     */
    public Mono<User> registerUser(RegisterRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .flatMap(existingUser -> Mono.<User>error(
                        new UserAlreadyExistsException("Username already exists")))
                .switchIfEmpty(userRepository.findByEmail(request.getEmail())
                        .flatMap(existingUser -> Mono.<User>error(
                                new UserAlreadyExistsException("Email already exists")))
                        .switchIfEmpty(Mono.defer(() -> {
                            User user = User.builder()
                                    .username(request.getUsername())
                                    .email(request.getEmail())
                                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                                    .enabled(true)
                                    .createdAt(LocalDateTime.now())
                                    .build();
                            
                            return userRepository.save(user)
                                    .flatMap(savedUser ->
                                            // Fetch the user again to get the database-generated UUID
                                            userRepository.findById(savedUser.getId())
                                                    .flatMap(userWithUuid ->
                                                            roleRepository.findByName("ROLE_USER")
                                                                    .flatMap(role -> {
                                                                        UserRole userRole = UserRole.builder()
                                                                                .userId(userWithUuid.getId())
                                                                                .roleId(role.getId())
                                                                                .build();
                                                                        return userRoleRepository.save(userRole)
                                                                                .thenReturn(userWithUuid);
                                                                    })
                                                    )
                                    );
                        }))
                );
    }
    
    /**
     * Find user by UUID
     */
    public Mono<User> findByUuid(UUID uuid) {
        return userRepository.findByUuid(uuid);
    }
    
    /**
     * Find user by username
     */
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Find user by email
     */
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Get all roles for a user by user ID
     */
    public Flux<Role> getUserRoles(Long userId) {
        return userRoleRepository.findByUserId(userId)
                .map(UserRole::getRoleId)
                .collectList()
                .flatMapMany(roleIds -> {
                    if (roleIds.isEmpty()) {
                        return Flux.empty();
                    }
                    return roleRepository.findByIdIn(roleIds);
                });
    }
    
    /**
     * Get all users (admin only)
     */
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }
}
